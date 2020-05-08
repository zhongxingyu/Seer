 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.gen;
 
 import static org.oobium.build.gen.ModelProcessor.GEN_MODELS;
 import static org.oobium.build.gen.ModelProcessor.GEN_SCHEMA;
 import static org.oobium.utils.StringUtils.varName;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.tools.JavaCompiler;
 import javax.tools.ToolProvider;
 
 import org.oobium.build.gen.model.PropertyDescriptor;
 import org.oobium.build.model.ModelAttribute;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.model.ModelRelation;
 import org.oobium.build.util.PersistConfig;
 import org.oobium.build.util.SourceFile;
 import org.oobium.build.util.PersistConfig.Service;
 import org.oobium.build.workspace.Application;
 import org.oobium.build.workspace.Bundle;
 import org.oobium.build.workspace.Module;
 import org.oobium.build.workspace.Workspace;
 import org.oobium.persist.Model;
 import org.oobium.persist.Paginator;
 import org.oobium.persist.migrate.Migration;
 import org.oobium.utils.StringUtils;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.json.JsonModel;
 
 
 public class ModelGenerator {
 
 	public static String generate(String classAnnotations, ModelDefinition model) {
 		SourceFile src = new SourceFile();
 
 		src.simpleName = model.getSimpleName() + "Model";
 		src.packageName = model.getPackageName();
 		src.superName = Model.class.getSimpleName();
 		src.isAbstract = true;
 
 		for(ModelAttribute attribute : model.attributes().values()) {
 			src.properties.put(attribute.getName(), new PropertyDescriptor(attribute));
 		}
 
 		for(ModelRelation relation : model.relations().values()) {
 			src.properties.put(relation.getName(), new PropertyDescriptor(relation));
 		}
 
 		src.imports.add(JsonModel.class.getCanonicalName());
 		src.imports.add(Model.class.getCanonicalName());
 		src.imports.add(List.class.getCanonicalName());
 		src.imports.add(Map.class.getCanonicalName());
 		src.imports.add(Paginator.class.getCanonicalName());
 		src.imports.add(SQLException.class.getCanonicalName());
 
 		boolean validate = false;
 		List<String> inits = new ArrayList<String>();
 		for(PropertyDescriptor property : src.properties.values()) {
 			src.imports.addAll(property.imports());
 			if(property.hasInit()) {
 				inits.add(createInitializer(property));
 			}
 			src.methods.putAll(property.methods());
 			if(property.isRequired()) {
 				validate = true;
 			}
 		}
 
 		createConstructor(src, inits);
 
 		createOverrideMethods(src, model);
 		
 		if(validate) {
 			src.methods.put("validateSave", createValidateMethod(src));
 		}
 		
 		src.staticMethods.put("finders", createStaticMethods(model.getSimpleName()));
 		
 		for(Iterator<String> iter = src.imports.iterator(); iter.hasNext(); ) {
 			if(model.getCanonicalName().equals(iter.next())) {
 				iter.remove();
 			}
 		}
 
 		return "/*\n" + classAnnotations + "*/\n" + src.toSource();
 	}
 
 	private static void createConstructor(SourceFile src, List<String> inits) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("\tpublic ").append(src.simpleName).append("() {\n");
 		sb.append("\t\tsuper();\n");
 		for(String init : inits) {
			sb.append("\t\t").append(init).append('\n');
 		}
 		sb.append("\t}");
 		src.constructors.put(0, sb.toString());
 	}
 	
 	private static void createOverrideMethods(SourceFile src, ModelDefinition model) {
 		String type = model.getSimpleName();
 		
 		src.methods.put("put(String field, Object value)", 	build(type, "put(String field, Object value)", "put(field, value)"));
 		src.methods.put("putAll(JsonModel model)", 			build(type, "putAll(JsonModel model)", "putAll(model)"));
 		src.methods.put("putAll(Map<String, Object> data)", build(type, "putAll(Map<String, Object> data)", "putAll(data)"));
 		src.methods.put("putAll(String json)", 				build(type, "putAll(String json)", "putAll(json)"));
 		src.methods.put("set(String field, Object value)", 	build(type, "set(String field, Object value)", "set(field, value)"));
 		src.methods.put("setAll(Map<String, Object> data)", build(type, "setAll(Map<String, Object> data)", "setAll(data)"));
 		src.methods.put("setAll(String json)", 				build(type, "setAll(String json)", "setAll(json)"));
 		src.methods.put("setId(int id)", 					build(type, "setId(int id)", "setId(id)"));
 	}
 
 	private static String build(String type, String sig, String body) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("\t@Override\n");
 		sb.append("\tpublic ").append(type).append(' ').append(sig).append(" {\n");
 		sb.append("\t\treturn (").append(type).append(") super.").append(body).append(";\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 	private static String createInitializer(PropertyDescriptor property) {
 		return "set(" + property.enumProp() + ", " + property.init() + ");";
 	}
 	
 	private static String createValidateMethod(SourceFile src) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("\t@Override\n");
 		sb.append("\tprotected void validateSave() {\n");
 		for(PropertyDescriptor property : src.properties.values()) {
 			if(property.isRequired()) {
 				String prop = property.enumProp();
 				if("String".equals(property.type())) {
 					src.staticImports.add(StringUtils.class.getCanonicalName() + ".blank");
 					sb.append("\t\tif((isNew() || isSet(").append(prop).append(")) && blank(get(").append(prop).append("))) {\n");
 					sb.append("\t\t\taddError(").append(prop).append(", \"cannot be blank\");\n");
 					sb.append("\t\t}\n");
 				} else {
 					sb.append("\t\tif((isNew() || isSet(").append(prop).append(")) && (get(").append(prop).append(") == null)) {\n");
 					sb.append("\t\t\taddError(").append(prop).append(", \"cannot be null\");\n");
 					sb.append("\t\t}\n");
 				}
 			}
 		}
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 	private static void appendDoc(StringBuilder sb, String javadoc, String...vars) {
 		StringBuilder doc = new StringBuilder(javadoc.length() + StringUtils.count(vars) + 5);
 		doc.append(javadoc);
 		int i = 0;
 		int pos = 0;
 		while((pos = doc.indexOf("?", pos)) != -1 && i < vars.length) {
 			doc.replace(pos, pos+1, vars[i++]);
 		}
 		
 		sb.append("\t/**\n");
 		for(String line : doc.toString().split("\n")) {
 			sb.append("\t * ").append(line).append('\n');
 		}
 		sb.append("\t*/\n");
 	}
 	
 	private static String createStaticMethods(String type) {
 		String var = varName(type);
 		StringBuilder sb = new StringBuilder();
 		appendDoc(sb, "Create a new instance of ? and set its id to the given value", type);
 		sb.append("\tpublic static ").append(type).append(" newInstance(int id) {\n");
 		sb.append("\t\t").append(type).append(' ').append(var).append(" = new ").append(type).append("();\n");
 		sb.append("\t\t").append(var).append(".setId(id);\n");
 		sb.append("\t\treturn ").append(var).append(";\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		appendDoc(sb, "Create a new instance of ? and initialize it with the given fields", type);
 		sb.append("\tpublic static ").append(type).append(" newInstance(Map<String, Object> fields) {\n");
 		sb.append("\t\t").append(type).append(' ').append(var).append(" = new ").append(type).append("();\n");
 		sb.append("\t\t").append(var).append(".setAll(fields);\n");
 		sb.append("\t\treturn ").append(var).append(";\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		appendDoc(sb, "Create a new instance of ? and initialize it with the given json data", type);
 		sb.append("\tpublic static ").append(type).append(" newInstance(String json) {\n");
 		sb.append("\t\t").append(type).append(' ').append(var).append(" = new ").append(type).append("();\n");
 		sb.append("\t\t").append(var).append(".setAll(json);\n");
 		sb.append("\t\treturn ").append(var).append(";\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		appendDoc(sb, "Find the ? with the given id", type);
 		sb.append("\tpublic static ").append(type).append(" find(int id) throws SQLException {\n");
 		sb.append("\t\treturn Model.find(").append(type).append(".class, id);\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		appendDoc(sb, "Find the ? with the given id and include the given fields.\nThe include option can start with 'include:', but it is not required.", type);
 		sb.append("\tpublic static ").append(type).append(" find(int id, String include) throws SQLException {\n");
 		sb.append("\t\tString sql = (include.startsWith(\"include:\") ? \"where id=? \" : \"where id=? include:\") + include;\n");
 		sb.append("\t\treturn Model.find(").append(type).append(".class, sql, id);\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		appendDoc(sb, "Find the ? with using the given sql query and values.  Note that only one instance will be returned.\nPrepend the query with 'where' to enter only the where clause.", type);
 		sb.append("\tpublic static ").append(type).append(" find(String sql, Object...values) throws SQLException {\n");
 		sb.append("\t\treturn Model.find(").append(type).append(".class, sql, values);\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		sb.append("\tpublic static List<").append(type).append("> findAll() throws SQLException {\n");
 		sb.append("\t\treturn Model.findAll(").append(type).append(".class);\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		sb.append("\tpublic static List<").append(type).append("> findAll(String sql, Object...values) throws SQLException {\n");
 		sb.append("\t\treturn Model.findAll(").append(type).append(".class, sql, values);\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		sb.append("\tpublic static Paginator<").append(type).append("> paginate(int page, int perPage) throws SQLException {\n");
 		sb.append("\t\treturn Paginator.paginate(").append(type).append(".class, page, perPage);\n");
 		sb.append("\t}\n");
 		sb.append("\n");
 		sb.append("\tpublic static Paginator<").append(type).append("> paginate(int page, int perPage, String sql, Object...values) throws SQLException {\n");
 		sb.append("\t\treturn Paginator.paginate(").append(type).append(".class, page, perPage, sql, values);\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 
 	public static File[] generate(Workspace workspace, Module module, File...models) {
 		return generate(workspace, module, GEN_MODELS, models);
 	}
 	
 	public static File[] generate(Workspace workspace, Module module, File model, int flags) {
 		return generate(workspace, module, flags, new File[] { model });
 	}
 	
 	public static File[] generate(Workspace workspace, Module module, List<File> models) {
 		return generate(workspace, module, GEN_MODELS, models.toArray(new File[models.size()]));
 	}
 	
 	public static void generateSchema(Workspace workspace, Application app, Mode mode) {
 		PersistConfig config = new PersistConfig(app, mode);
 		
 		List<File> models = new ArrayList<File>();
 		
 		for(PersistConfig modConfig : config.getModuleConfigs()) {
 			Module module = workspace.getModule(modConfig.getModule());
 			if(modConfig.isDb()) { // add all, then remove those that are not db
 				models.addAll(module.findModels());
 				for(Service service : modConfig.getServices()) {
 					if(!service.isDb()) {
 						for(String model : service.getModels()) {
 							models.remove(module.getModel(model));
 						}
 					}
 				}
 			} else { // add only those that are db
 				for(Service service : modConfig.getServices()) {
 					if(service.isDb()) {
 						for(String model : service.getModels()) {
 							models.add(module.getModel(model));
 						}
 					}
 				}
 			}
 		}
 		
 		generate(workspace, app, GEN_SCHEMA, models.toArray(new File[models.size()]));
 	}
 
 	private static File[] generate(Workspace workspace, Module module, int action, File...models) {
 		ModelGenerator gen = new ModelGenerator(workspace, module, action);
 		gen.process(models);
 		return gen.getFiles();
 	}
 
 
 	private final Workspace workspace;
 	private final Module module;
 	private final int action;
 	final List<File> files;
 
 	ModelGenerator(Workspace workspace, Module module, int action) {
 		this.workspace = workspace;
 		this.module = module;
 		this.action = action;
 		files = new ArrayList<File>();
 	}
 
 	public void addFile(File file) {
 		files.add(file);
 	}
 
 	File[] getFiles() {
 		return files.toArray(new File[files.size()]);
 	}
 
 	public Workspace getWorkspace() {
 		return workspace;
 	}
 	
 	void process(File[] models) {
 		if(models.length == 0) {
 			return;
 		}
 		
 		StringBuilder classpath = new StringBuilder();
 
 		Bundle builder = workspace.getBuildBundle();
 		if(builder == null) {
 			throw new IllegalStateException("Builder Bundle cannot be found");
 		}
 		
 		classpath.append(builder.file.getAbsolutePath());
 		if(builder.file.isDirectory()) {
 			classpath.append(File.separatorChar).append("bin");
 		}
 		
 		if((action & GEN_SCHEMA) != 0) {
 			Bundle migrator = workspace.getBundle(Migration.class.getPackage().getName());
 			if(migrator == null) {
 				throw new IllegalStateException("Migrator Bundle cannot be found");
 			}
 			classpath.append(File.pathSeparatorChar).append(migrator.file.getAbsolutePath());
 			if(migrator.file.isDirectory()) {
 				classpath.append(File.separatorChar).append("bin");
 			}
 		}
 		
 //		System.out.println(classpath);
 		for(String cpe : module.getClasspathEntries(workspace)) {
 //			System.out.println(cpe);
 			classpath.append(File.pathSeparatorChar).append(cpe);
 		}
 		
 		int i = 0;
 		String[] strs = new String[11 + models.length];
 		strs[i++] = "-Aname=" + module.name;
 		strs[i++] = "-Aversion=" + module.version.toString(true);
 		strs[i++] = "-Apath=" + module.file.getAbsolutePath();
 		strs[i++] = "-Atype=" + module.type.name();
 		strs[i++] = "-Aaction=" + action;
 		strs[i++] = "-Awebservice=" + module.hasNature(Module.NATURE_WEBSERVICE);
 		strs[i++] = "-cp";
 		strs[i++] = classpath.toString();
 		strs[i++] = "-processor";
 		strs[i++] = ModelProcessor.class.getCanonicalName();
 		strs[i++] = "-proc:only";
 		for(File model : models) {
 			strs[i++] = model.getAbsolutePath();
 		}
 
 		// hack work-around for different ClassLoader issue
 		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
 		try {
 			Thread.currentThread().setContextClassLoader(Workspace.class.getClassLoader());
 			ModelProcessor.generators.set(this);
 			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 			compiler.run(System.in, System.out, System.err, strs);
 		} finally {
 			ModelProcessor.generators.set(null);
 			Thread.currentThread().setContextClassLoader(contextClassLoader);
 		}
 	}
 
 }
