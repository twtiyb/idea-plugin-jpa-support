package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.config.TablesConfig;
import com.ifengxue.plugin.util.StringHelper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class ServiceImpSourceParser implements SourceParser, VelocityEngineAware {

	private VelocityEngine velocityEngine;
	private String encoding;

	@Override
	public String parse(GeneratorConfig config, Table table) {
		VelocityContext context = new VelocityContext();
		TablesConfig tablesConfig = config.getTablesConfig();
		if (tablesConfig.getBasePackageName().isEmpty()) {
			context.put("package", "");
			context.put("importClassList", Collections.emptyList());
		} else {
			context.put("package", tablesConfig.getServiceSubPackageName()+".imp");
			context.put("importClassList", Arrays.asList(tablesConfig.getEntityPackageName() + "." + table.getEntityName()
					, config.getTablesConfig().getServiceSubPackageName() + "." + table.getEntityName() + "Service"
					, config.getTablesConfig().getRepositoryPackageName() + "." + table.getEntityName() + "Repository"
			));
		}

		context.put("simpleName", table.getEntityName() + "ServiceImp");
		context.put("entitySimpleName", table.getEntityName());
		context.put("superClassName", tablesConfig.getExtendsServiceName());
		context.put("superImpClassName", tablesConfig.getExtendsServiceImpName());
		context.put("primaryKeyDataType",
				Optional.ofNullable(table.getPrimaryKeyClassType())
						.map(StringHelper::getWrapperClass)
						.map(Class::getSimpleName)
						.orElse("Void"));
		StringWriter writer = new StringWriter();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("template/ServiceImp.vm")) {
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			velocityEngine.evaluate(context, writer, "repository", new String(buffer, encoding));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	@Override
	public void setVelocityEngine(VelocityEngine ve, String encoding) {
		this.velocityEngine = ve;
		this.encoding = encoding;
	}
}
