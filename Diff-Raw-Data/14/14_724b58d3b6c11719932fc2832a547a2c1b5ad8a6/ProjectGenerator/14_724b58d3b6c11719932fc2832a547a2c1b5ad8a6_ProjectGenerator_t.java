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
 
 import static org.oobium.build.util.ProjectUtils.getPrefsFileDate;
 import static org.oobium.utils.FileUtils.createFolder;
 import static org.oobium.utils.FileUtils.writeFile;
 import static org.oobium.utils.StringUtils.camelCase;
 import static org.oobium.utils.StringUtils.impliedType;
import static org.oobium.utils.StringUtils.*;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.oobium.app.AppService;
 import org.oobium.app.ModuleService;
 import org.oobium.app.controllers.ActionCache;
 import org.oobium.app.controllers.HttpController;
 import org.oobium.app.http.Action;
 import org.oobium.app.persist.ModelNotifier;
 import org.oobium.app.routing.AppRouter;
 import org.oobium.app.routing.Router;
 import org.oobium.app.server.Websocket;
 import org.oobium.build.util.SourceFile;
 import org.oobium.build.workspace.Bundle;
 import org.oobium.build.workspace.ExportedPackage;
 import org.oobium.build.workspace.Module;
 import org.oobium.build.workspace.Project.Type;
 import org.oobium.mailer.Mailer;
 import org.oobium.persist.Attribute;
 import org.oobium.persist.ModelDescription;
 import org.oobium.persist.Observer;
 import org.oobium.persist.Relation;
 import org.oobium.persist.Text;
 import org.oobium.persist.migrate.AbstractMigration;
 import org.oobium.persist.migrate.Migrations;
 import org.oobium.persist.migrate.db.DbMigratorService;
 import org.oobium.utils.Config;
 import org.oobium.utils.StringUtils;
 
 
 public class ProjectGenerator {
 
 	private static final int PTYPE_APP = 0;
 	private static final int PTYPE_APP_WS = 1;
 	private static final int PTYPE_TEST = 2;
 	private static final int PTYPE_MIG = 3;
 	private static final int PTYPE_MOD = 4;
 	private static final int PTYPE_MOD_WS = 5;
 	
 	private static File appFolder(File project)			{ return mainFolder(project); } // default app folder is the same as the main folder
 	
 	private static File assetsFolder(File project)		{ return new File(project, "assets"); }
 
 	private static File binFolder(File project)			{ return new File(project, "bin"); }
 
 	private static File controllersFolder(File project)	{ return new File(appFolder(project), "controllers"); }
 
 	public static File createActionCache(Module module, String name, String model, Action...actions) {
 		SourceFile src = new SourceFile();
 		
 		src.packageName = module.packageName(module.caches);
 		src.simpleName = name;
 		src.superName = ActionCache.class.getSimpleName();
 		if(actions.length > 0) {
 			src.imports.add(Action.class.getCanonicalName());
 		}
 		src.imports.add(ActionCache.class.getCanonicalName());
 		src.imports.add(module.packageName(module.models) + "." + model);
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("\tstatic { addCache(").append(name).append(".class, ").append(model).append(".class");
 		for(Action action : actions) {
 			sb.append(", Action.").append(action.name());
 		}
 		sb.append("); }\n\n//\tTODO override the Observer methods to implement sweeping for this ActionCache\n");
 
 		src.rawSource =  sb.toString();
 		
 		File folder = createFolder(module.caches);
 		return writeFile(folder, name + ".java", src.toSource());
 	}
 
 	private static void createAppActivator(File project, boolean createViews) {
 		SourceFile src = new SourceFile();
 
 		File main = new File(project, "src" + File.separator + project.getName().replace('.', File.separatorChar));
 		
 		src.packageName = project.getName();
 		src.simpleName = "Activator";
 		src.superName = AppService.class.getSimpleName();
 		src.imports.add(Config.class.getCanonicalName());
 		src.imports.add(AppService.class.getCanonicalName());
 		src.imports.add(AppRouter.class.getCanonicalName());
 		if(createViews) {
 			String path = viewsFolder(project).getAbsolutePath().substring(srcFolder(project).getAbsolutePath().length()+1).replace(File.separatorChar, '.');
 			src.imports.add(path + ".pages.Home");
 		}
 
 		String cType = Config.class.getSimpleName();
 		String rType = AppRouter.class.getSimpleName();
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("@Override\n");
 		sb.append("public void addRoutes(").append(cType).append(" config, ").append(rType).append(" router) {\n");
 		sb.append('\n');
 		sb.append("\t// TODO add routes specific to your application\n");
 		sb.append('\n');
 		if(createViews) {
 			sb.append("\trouter.addAssetRoutes();\n");
 			sb.append("\trouter.setHome(Home.class);\n");
 		}
 		sb.append("}");
 		src.methods.put("addRoutes", sb.toString());
 
 		writeFile(main, "Activator.java", src.toSource());
 	}
 	
 	/**
 	 * Create a full application.<br>
 	 * Convenience method which calls {@link #createApplication(File, Map)},
 	 * passing in an empty map.
 	 * @param project
 	 * @return
 	 * @see #createApplication(File, Map)
 	 */
 	public static File createApplication(File project) {
 		return createApplication(project, new HashMap<String, String>(0));
 	}
 
 	/**
 	 * Create a full application.<br>
 	 * Valid properties: createViews:true/false (default:true), ...
 	 */
 	public static File createApplication(File project, Map<String, String> properties) {
 		if(project.exists()) {
 			throw new UnsupportedOperationException(project.getName() + " already exists");
 		} else {
 			project.mkdirs();
 		}
 
 		createFolder(srcFolder(project));
 		createFolder(binFolder(project));
 		createFolder(genFolder(project));
 
 		createFolder(mainFolder(project));
 		createFolder(modelsFolder(project));
 		createFolder(controllersFolder(project));
 
 		boolean createViews = createViews(properties);
 		if(createViews) {
 			File views = viewsFolder(project);
 
 			createLayout(project);
 			
 			ViewGenerator.createView(views, "pages/Error404", "title Error: 404 - page not found\n\ndiv.errors Sorry, but the page you requested could not be found.\n");
 			ViewGenerator.createView(views, "pages/Error500", "title Error: 500 - server error\n\ndiv.errors Sorry, but an internal server error prevented the page you requested from being rendered.\n");
 			ViewGenerator.createView(views, "pages/Home", "div Hello from Oobium! :)");
 
 			createFolder(assetsFolder(project));
 			createFolder(assetsFolder(project), "i18n");
 			createFolder(assetsFolder(project), "scripts");
 			createFolder(assetsFolder(project), "styles");
 			createFolder(assetsFolder(project), "images");
 			createFolder(project, "assets.src/images");
 			
 			String src;
 			
 			src = StringUtils.getResourceAsString(ProjectGenerator.class, "templates/application.css");
 			writeFile(assetsFolder(project), "styles/application.css", src);
 
 			src = StringUtils.getResourceAsString(ProjectGenerator.class, "templates/application.js");
 			writeFile(assetsFolder(project), "scripts/application.js", src);
 
 			// the JQuery version must match between here and in EspCompiler#buildScript
 			src = StringUtils.getResourceAsString(ProjectGenerator.class, "templates/jquery-1.4.4.js");
 			writeFile(assetsFolder(project), "scripts/jquery-1.4.4.js", src);
 		}
 
 		createFolder(project, ".settings");
 
 		createProjectFile(project, PTYPE_APP, createViews);
 		createClasspathFile(project, createViews ? PTYPE_APP : PTYPE_APP_WS);
 		createBuildFile(project, createViews ? PTYPE_APP : PTYPE_APP_WS);
 		createManifestFile(project, PTYPE_APP, properties);
 		createPrefsFile(project);
 		createAppActivator(project, createViews);
 		createApplicationConfigFile(project, false);
 		createApplicationController(project);
 		
 		return project;
 	}
 	
 	private static void createApplicationConfigFile(File project, boolean webservice) {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("({\n");
 		sb.append('\n');
 		if(!webservice) {
 			sb.append("cache:   \"org.oobium.cache.file\",\n");
 		}
 		sb.append("persist: \"org.oobium.persist.db.derby.embedded\",\n");
 		sb.append('\n');
 		sb.append("dev: {\n");
 		sb.append("\thost: \"localhost\",\n");
 		sb.append("\tport: 5000,\n");
 		sb.append("\tmodules: [\n");
 		sb.append("\t\t\"org.oobium.app.dev_0.6.0\",\n");
 		sb.append("\t\t\"org.oobium.manager_0.6.0\"\n");
 		sb.append("\t],\n");
 		sb.append("},\n");
 		sb.append('\n');
 		sb.append("test: {\n");
 		sb.append("\thost: \"localhost\",\n");
 		sb.append("\tport: 5001,\n");
 		sb.append("},\n");
 		sb.append('\n');
 		sb.append("prod: {\n");
 		sb.append("\thost: \"my.domain.com\",\n");
 		sb.append("\tport: 80,\n");
 		sb.append("}\n");
 		sb.append('\n');
 		sb.append("});");
 		
 		writeFile(mainFolder(project), "configuration.js", sb.toString());
 	}
 	
 	private static void createApplicationController(File project) {
 		SourceFile src = new SourceFile();
 
 		src.packageName = project.getName() + ".controllers";
 		src.simpleName = "ApplicationController";
 		src.superName = HttpController.class.getSimpleName();
 		src.imports.add(HttpController.class.getCanonicalName());
 
 		writeFile(controllersFolder(project), "ApplicationController.java", src.toSource());
 	}
 
 	public static File createLayout(File project) {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("title ").append(project.getName()).append(": { titleize(getChild().getClass().getSimpleName()) }\n\n");
 		sb.append("head\n");
 		sb.append("\tscript(defaults)\n");
 		sb.append("\tstyle(defaults)\n\n");
 		sb.append("div <- yield\n");
 
 		return writeFile(layoutsFolder(project), "_Layout.esp", sb.toString());
 	}
 	
 	private static void createBuildFile(File project, int type) {
 		StringBuilder sb = new StringBuilder();
 		if(PTYPE_MIG == type) {
 			sb.append("source.. = src/,\\\n           generated/\n");
 		} else if(PTYPE_TEST == type) {
 			sb.append("source.. = src-functional/,\\\n");
 			sb.append("           src-integration/,\\\n");
 			sb.append("           src-unit/\n");
 		} else if(type >= 0) {
 			sb.append("source.. = src/,\\\n");
 			if(type != PTYPE_APP_WS && type != PTYPE_MOD_WS) {
 				sb.append("           assets/,\\\n");
 			}
 			sb.append("           generated/\n");
 		}
 		sb.append("output.. = bin/\n");
 		sb.append("bin.includes = META-INF/,\\\n");
 		sb.append(".\n");
 		if(PTYPE_TEST == type) {
 			sb.append("additional.bundles = org.oobium.cache\n");
 		}
 		writeFile(project, "build.properties", sb.toString());
 	}
 	
 	private static void createClasspathFile(File project, int type) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		sb.append("<classpath>\n");
 		if(type == PTYPE_TEST) {
 			sb.append("	<classpathentry kind=\"src\" path=\"src-functional\"/>\n");
 			sb.append("	<classpathentry kind=\"src\" path=\"src-integration\"/>\n");
 			sb.append("	<classpathentry kind=\"src\" path=\"src-unit\"/>\n");
 		} else {
 			sb.append("	<classpathentry kind=\"src\" path=\"src\"/>\n");
 			if(type == PTYPE_APP || type == PTYPE_APP_WS || type == PTYPE_MOD || type == PTYPE_MOD_WS) {
 				if(type != PTYPE_APP_WS && type != PTYPE_MOD_WS) {
 					sb.append("	<classpathentry kind=\"src\" path=\"assets\"/>\n");
 				}
 				sb.append("	<classpathentry kind=\"src\" path=\"generated\"/>\n");
 			} else if(type == PTYPE_MIG) {
 				sb.append("	<classpathentry kind=\"src\" path=\"generated\"/>\n");
 			}
 		}
 		sb.append("	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>\n");
 		sb.append("	<classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>\n");
 		sb.append("	<classpathentry kind=\"output\" path=\"bin\"/>\n");
 		sb.append("</classpath>");
 		writeFile(project, ".classpath", sb.toString());
 	}
 	
 	private static void createPrefsFile(File project) {
 		writeFile(createFolder(project, ".settings"), "org.eclipse.jdt.core.prefs",
 				"#" + getPrefsFileDate() + "\n" +
 				"org.eclipse.jdt.core.builder.resourceCopyExclusionFilter=*.launch,*.esp,*.ess,*.ejs,*.emt,site.js"
 			);
 	}
 	
 	public static File createMailer(Module module, String name, String...methods) {
 		SourceFile src = new SourceFile();
 		
 		String simpleName = StringUtils.camelCase(name);
 		if(!simpleName.endsWith("Mailer")) {
 			simpleName = simpleName + "Mailer";
 		}
 		src.packageName = module.name + ".mailers";
 		src.simpleName = simpleName;
 		
 		src.imports.add(Mailer.class.getCanonicalName());
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append('@').append(Mailer.class.getSimpleName()).append("( /* TODO set mailer properties */ )");
 		src.classAnnotations.put(1, sb.toString());
 
 		for(String methodName : methods) {
 			if(!methodName.startsWith("setup")) {
 				methodName = "setup" + StringUtils.camelCase(methodName);
 			}
 
 			String templateName = methodName.substring(5);
 			String imp = src.packageName + "." + underscored(simpleName.substring(0, simpleName.length()-6)) + "." + templateName;
 			
 			src.imports.add(imp);
 			
 			sb = new StringBuilder();
 			sb.append("protected void " + methodName + "() {\n");
 			sb.append("\t// TODO auto-generated method\n");
 			sb.append("\trender(new ").append(templateName).append("());\n");
 			sb.append("}");
 			
 			src.methods.put(methodName, sb.toString());
 		}
 		
 		return writeFile(module.mailers, simpleName + ".java", src.toSource());
 	}
 	
 	/**
 	 * Create the MANIFEST.MF file for an application, module, or test project
 	 * @param project
 	 * @param activator
 	 * @param tests
 	 */
 	private static void createManifestFile(File project, int projectType, Map<String, String> properties) {
 		if(properties == null) {
 			properties = new HashMap<String, String>(0);
 		} else if(properties.containsKey("Bundle-SymbolicName")) {
 			throw new IllegalArgumentException("Bundle-SymbolicName must equal the name of the project folder");
 		}
 
 		final String name = project.getName();
 		StringBuilder sb = new StringBuilder();
 		sb.append("Manifest-Version: ").append(property(properties, "Manifest-Version", "1.0")).append('\n');
 		sb.append("Bundle-ManifestVersion: ").append(property(properties, "Bundle-ManifestVersion", "2")).append('\n');
 		sb.append("Bundle-Name: ").append(property(properties, "Bundle-Name", name + " Bundle")).append('\n');
 		sb.append("Bundle-SymbolicName: ").append(name).append('\n');
 		sb.append("Bundle-Version: ").append(property(properties, "Bundle-Version", "1.0.0.qualifier")).append('\n');
 		sb.append("Bundle-RequiredExecutionEnvironment: ").append(property(properties, "Bundle-RequiredExecutionEnvironment", "JavaSE-1.6")).append('\n');
 
 		if(projectType >= 0) {
 			sb.append("Oobium-Type: ");
 			switch(projectType) {
 			case PTYPE_APP:
 			case PTYPE_APP_WS:
 				sb.append(underscored(Type.Application.name()));
 				break;
 			case PTYPE_MOD:
 			case PTYPE_MOD_WS:
 				sb.append(underscored(Type.Module.name()));
 				break;
 			case PTYPE_MIG:
 				sb.append(underscored(Type.Migrator.name()));
 				break;
 			case PTYPE_TEST:
 				sb.append(underscored(Type.TestSuite.name()));
 				break;
 			}
 			sb.append('\n');
 	
 			sb.append("Import-Package: org.osgi.framework;version=\"1.4.0\",\n");
 			sb.append(" org.jboss.netty.handler.codec.http,\n");
 			sb.append(" org.oobium.app,\n");
 			sb.append(" org.oobium.app.controllers,\n");
 			sb.append(" org.oobium.app.request,\n");
 			sb.append(" org.oobium.app.response,\n");
 			sb.append(" org.oobium.app.routing,\n");
 			if(projectType != PTYPE_APP_WS && projectType != PTYPE_MOD_WS) {
 				sb.append(" org.oobium.app.workers,\n");
 				sb.append(" org.oobium.app.views,\n");
 			}
 			sb.append(" org.oobium.app.http,\n");
 			sb.append(" org.oobium.cache,\n");
 			sb.append(" org.oobium.logging,\n");
 			sb.append(" org.oobium.persist,\n");
 			if(projectType == PTYPE_TEST) {
 				sb.append(" org.oobium.test,\n");
 			}
 			sb.append(" org.oobium.utils,\n");
 			sb.append(" org.oobium.utils.coercion,\n");
 			sb.append(" org.oobium.utils.json\n");
 			if(properties.containsKey("Import-Package")) {
 				throw new IllegalArgumentException("Import-Package not supported yet");
 			}
 			if(projectType == PTYPE_TEST) {
 				sb.append("Require-Bundle: org.junit;bundle-version=\"4.7.0\",\n ");
 				sb.append("org.mockito;bundle-version=\"1.8.0\",\n ");
 				sb.append(name.substring(0, name.length()-6)).append(",\n ");
 				sb.append(name.substring(0, name.length()-6)).append(".migrator").append('\n');
 			} else {
 				sb.append("Bundle-Activator: ").append(property(properties, "Bundle-Activator", name)).append(".Activator\n");
 				sb.append("Export-Package: ").append(name).append('\n');
 				if(properties.containsKey("Export-Package")) {
 					throw new IllegalArgumentException("Export-Package not supported yet");
 				}
 				sb.append("Bundle-ActivationPolicy: ").append(property(properties, "Bundle-Activator", "lazy")).append('\n');
 			}
 		}
 		
 		for(Entry<String, String> entry : properties.entrySet()) {
 			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
 		}
 		
 		File folder = createFolder(project, "META-INF");
 		writeFile(folder, "MANIFEST.MF", sb.toString());
 	}
 
 	public static File createMigrator(Module module, Set<Bundle> dependencies) {
 		File migrator = module.migrator;
 		if(migrator.exists()) {
 			throw new UnsupportedOperationException(migrator.getName() + " already exists");
 		} else {
 			migrator.mkdirs();
 		}
 
 		createFolder(srcFolder(migrator));
 		createFolder(binFolder(migrator));
 		createFolder(mainFolder(migrator));
 		createFolder(libFolder(migrator));
 		createFolder(genFolder(migrator));
 
 		createProjectFile(migrator, PTYPE_MIG, false);
 		createClasspathFile(migrator, PTYPE_MIG);
 		createBuildFile(migrator, PTYPE_MIG);
 		createMigrationManifestFile(migrator, module, dependencies);
 		createPrefsFile(migrator);
 		createMigratorFiles(migrator);
 		
 		return migrator;
 	}
 	
 	private static void createMigratorFiles(File migration) {
 		SourceFile src = new SourceFile();
 
 		// Migrator
 		src = new SourceFile();
 		src.packageName = migration.getName().replace(File.separatorChar, '.');
 		src.simpleName = "Migrator";
 		src.superName = DbMigratorService.class.getSimpleName();
 		src.imports.add(DbMigratorService.class.getCanonicalName());
 		src.imports.add(Migrations.class.getCanonicalName());
 		src.imports.add(src.packageName + ".migrations.CreateDatabase");
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("@Override\n");
 		sb.append("public void addMigrations(Migrations migrations) {\n");
 		sb.append("\tmigrations.add(CreateDatabase.class);\n");
 		sb.append("}");
 		src.methods.put("1", sb.toString());
 
 		writeFile(mainFolder(migration), src.simpleName + ".java", src.toSource());
 
 		// Migration
 		src = new SourceFile();
 		src.packageName = migration.getName().replace(File.separatorChar, '.') + ".migrations";
 		src.simpleName = "CreateDatabase";
 		src.superName = AbstractMigration.class.getSimpleName();
 		src.imports.add(AbstractMigration.class.getCanonicalName());
 		src.imports.add(SQLException.class.getCanonicalName());
 
 		sb = new StringBuilder();
 		sb.append("@Override\n");
 		sb.append("public void up() throws SQLException {\n");
 		sb.append("\t// TODO auto-generated method\n");
 		sb.append("}");
 		src.methods.put("1", sb.toString());
 
 		sb = new StringBuilder();
 		sb.append("@Override\n");
 		sb.append("public void down() throws SQLException {\n");
 		sb.append("\t// TODO auto-generated method\n");
 		sb.append("}");
 		src.methods.put("2", sb.toString());
 		
 		writeFile(mainFolder(migration) + File.separator + "migrations", src.simpleName + ".java", src.toSource());
 	}
 
 	/**
 	 * Create the MANIFEST.MF file for a Migration project
 	 * @param project
 	 * @param activator
 	 */
 	private static void createMigrationManifestFile(File project, Bundle bundle, Set<Bundle> dependencies) {
 		String name = project.getName();
 		
 		List<ExportedPackage> modelExports = new ArrayList<ExportedPackage>();
 		for(Bundle b : dependencies) {
 			ExportedPackage modelExport = b.getExportedPackage(b.name + ".models");
 			if(modelExport != null) {
 				modelExports.add(modelExport);
 			}
 		}
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("Manifest-Version: 1.0\n");
 		sb.append("Bundle-ManifestVersion: 2\n");
 		sb.append("Bundle-Name: ").append(name).append(" Bundle\n");
 		sb.append("Bundle-SymbolicName: ").append(name).append('\n');
 		sb.append("Bundle-Version: 1.0.0.qualifier\n");
 		sb.append("Oobium-Type: migrator\n");
 		sb.append("Require-Bundle: ").append(bundle.name).append(";bundle-version=\"").append(bundle.version).append("\"\n");
 		sb.append("Import-Package: org.osgi.framework;version=\"1.4.0\",\n");
 		sb.append(" org.oobium.app,\n");
 		sb.append(" org.oobium.logging,\n");
 		sb.append(" org.oobium.persist,\n");
 		sb.append(" org.oobium.persist.db,\n");
 		sb.append(" org.oobium.persist.migrate,\n");
 		sb.append(" org.oobium.persist.migrate.db,\n");
 		sb.append(" org.oobium.persist.migrate.defs,\n");
 		sb.append(" org.oobium.persist.migrate.defs.changes,\n");
 		sb.append(" org.oobium.persist.migrate.defs.columns,\n");
 		sb.append(" org.oobium.utils,\n");
 		sb.append(" org.oobium.utils.coercion,\n");
 		sb.append(" org.oobium.utils.json"); // note that new line is prepended below - careful when adding lines here
 		for(ExportedPackage modelExport : modelExports) {
 			sb.append(",\n ").append(modelExport.name).append(";version=\"").append(modelExport.version).append('"');
 		}
 		sb.append('\n');
 		sb.append("Export-Package: ").append(name).append('\n');
 		sb.append("Bundle-Activator: ").append(name).append(".Migrator\n");
 		sb.append("Bundle-RequiredExecutionEnvironment: JavaSE-1.6\n");
 		
 		File folder = createFolder(project, "META-INF");
 
 		writeFile(folder, "MANIFEST.MF", sb.toString());
 	}
 
 	private static void createModActivator(File project, boolean hasViews) {
 		SourceFile src = new SourceFile();
 
 		src.packageName = project.getName();
 		src.simpleName = "Activator";
 		src.superName = ModuleService.class.getSimpleName();
 		src.imports.add(Config.class.getCanonicalName());
 		src.imports.add(ModuleService.class.getCanonicalName());
 		src.imports.add(Router.class.getCanonicalName());
 
 		String cType = Config.class.getSimpleName();
 		String rType = Router.class.getSimpleName();
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("@Override\n");
 		sb.append("public void addRoutes(").append(cType).append(" config, ").append(rType).append(" router) {\n");
 		sb.append("\t// TODO add the routes specific to this module\n");
 		if(hasViews) {
 			sb.append("\trouter.addAssetRoutes();\n");
 		}
 		sb.append("}");
 		src.methods.put("addRoutes", sb.toString());
 		
 		writeFile(mainFolder(project), "Activator.java", src.toSource());
 	}
 
 	/**
 	 * Creates the model, if it does not exist; otherwise, this method simply returns.
 	 * @param module
 	 * @param name
 	 * @param properties
 	 * @throws IllegalArgumentException if an attribute cannot be parsed
 	 */
 	public static File createModel(Module module, String name, Map<String, String> properties) {
 		File file = module.getModel(name);
 		if(file.exists()) {
 			return file;
 		}
 		
 		SourceFile src = new SourceFile();
 		
 		src.packageName = module.packageName(module.models);
 		src.simpleName = name;
 		
 		src.imports.add(ModelDescription.class.getCanonicalName());
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append('@').append(ModelDescription.class.getSimpleName()).append('(');
 		if(properties.size() == 0) {
 			sb.append("timestamps=true)");
 		} else {
 			List<String> attrs = new ArrayList<String>();
 			List<String> ones = new ArrayList<String>();
 			List<String> manys = new ArrayList<String>();
 			
 			for(Entry<String, String> entry : properties.entrySet()) {
 				List<String> list;
 				String key = entry.getKey();
 				String type = impliedType(entry.getValue());
 				if("one".equalsIgnoreCase(type)) {
 					list = ones;
 				} else if("many".equalsIgnoreCase(type)) {
 					list = manys;
 				} else {
 					list = attrs;
 				}
 				if(list == attrs) {
 					if("Text".equals(type)) {
 						src.imports.add(Text.class.getCanonicalName());
 					} else if("Date".equals(type)) {
 						src.imports.add("java.util.Date");
 					}
 				} else {
 					type = camelCase(key);
 					src.imports.add(module.packageName(module.models) + "." + type);
 				}
 				list.add("name=\"" + key + "\", type=" + type + ".class)");
 			}
 			
 			sb.append('\n');
 			if(!attrs.isEmpty()) {
 				src.imports.add(Attribute.class.getCanonicalName());
 				sb.append("\tattrs = {\n");
 				for(Iterator<String> iter = attrs.iterator(); iter.hasNext(); ) {
 					sb.append("\t\t@").append(Attribute.class.getSimpleName()).append('(');
 					sb.append(iter.next());
 					if(iter.hasNext()) {
 						sb.append(",\n");
 					}
 				}
 				sb.append("\n\t},\n");
 			}
 			if(!ones.isEmpty()) {
 				src.imports.add(Relation.class.getCanonicalName());
 				sb.append("\thasOne = {\n");
 				for(Iterator<String> iter = ones.iterator(); iter.hasNext(); ) {
 					sb.append("\t\t@").append(Relation.class.getSimpleName()).append('(');
 					sb.append(iter.next());
 					if(iter.hasNext()) {
 						sb.append(",\n");
 					}
 				}
 				sb.append("\n\t},\n");
 			}
 			if(!manys.isEmpty()) {
 				src.imports.add(Relation.class.getCanonicalName());
 				sb.append("\thasMany = {\n");
 				for(Iterator<String> iter = manys.iterator(); iter.hasNext(); ) {
 					sb.append("\t\t@").append(Relation.class.getSimpleName()).append('(');
 					sb.append(iter.next());
 					if(iter.hasNext()) {
 						sb.append(",\n");
 					}
 				}
 				sb.append("\n\t},\n");
 			}
 			sb.append("\ttimestamps = true\n)");
 		}
 		
 		src.classAnnotations.put(1, sb.toString());
 
 		writeFile(file, src.toSource());
 		return file;
 	}
 	
 	public static File createModule(File project, Map<String, String> properties) {
 		if(project.exists()) {
 			throw new UnsupportedOperationException(project.getName() + " already exists");
 		} else {
 			project.mkdirs();
 		}
 
 		createFolder(srcFolder(project));
 		createFolder(binFolder(project));
 		createFolder(genFolder(project));
 		
 		createFolder(mainFolder(project));
 		createFolder(modelsFolder(project));
 		createFolder(controllersFolder(project));
 		
 		createFolder(project, ".settings");
 
 		boolean createViews = createViews(properties);
 		if(createViews) {
 			createFolder(viewsFolder(project));
 			createFolder(layoutsFolder(project));
 			
 			createLayout(project);
 			
 			createFolder(assetsFolder(project));
 			createFolder(assetsFolder(project), "i18n");
 			createFolder(assetsFolder(project), "scripts");
 			createFolder(assetsFolder(project), "styles");
 			createFolder(assetsFolder(project), "images");
 			createFolder(project, "assets.src/images");
 		}
 
 		createProjectFile(project, PTYPE_MOD, createViews);
 		createClasspathFile(project, createViews ? PTYPE_MOD : PTYPE_MOD_WS);
 		createBuildFile(project, createViews ? PTYPE_MOD : PTYPE_MOD_WS);
 		createManifestFile(project, PTYPE_MOD, properties);
 		createPrefsFile(project);
 		createModActivator(project, createViews);
 		createModuleConfigFile(project);
 
 		return project;
 	}
 	
 	private static void createModuleConfigFile(File project) {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("({\n");
 		sb.append('\n');
 		sb.append("dev: {\n");
 		sb.append("\t// TODO: add your development mode configuration\n");
 		sb.append("},\n");
 		sb.append('\n');
 		sb.append("test: {\n");
 		sb.append("\t// TODO: add your test mode configuration\n");
 		sb.append("},\n");
 		sb.append('\n');
 		sb.append("prod: {\n");
 		sb.append("\t// TODO: add your production mode configuration\n");
 		sb.append("}\n");
 		sb.append('\n');
 		sb.append("});");
 		
 		writeFile(mainFolder(project), "configuration.js", sb.toString());
 	}
 	
 	public static File createNotifier(Module module, String modelPackage, String modelName) {
 		SourceFile src = new SourceFile();
 
 		String name = modelName + "Notifier";
 		src.packageName = module.packageName(module.observers);
 		src.simpleName = name;
 		src.superName = ModelNotifier.class.getSimpleName() + "<" + modelName + ">";
 		src.imports.add(modelPackage + "." + modelName);
 		src.imports.add(ModelNotifier.class.getCanonicalName());
 		src.imports.add(Websocket.class.getCanonicalName());
 		src.imports.add(Action.class.getCanonicalName());
 
 		src.methods.put("select",
 			"@Override\n" +
 			"protected boolean select(Websocket socket, Action action) {\n" +
 			"\t// TODO Auto-generated method stub\n" +
 			"\treturn true;\n" +
 			"}");
 
 		return writeFile(module.observers, name + ".java", src.toSource());
 	}
 	
 	public static File createObserver(Module module, String modelPackage, String modelName) {
 		SourceFile src = new SourceFile();
 
 		String name = modelName + "Observer";
 		src.packageName = module.packageName(module.observers);
 		src.simpleName = name;
 		src.superName = Observer.class.getSimpleName() + "<" + modelName + ">";
 		src.imports.add(modelPackage + "." + modelName);
 		src.imports.add(Observer.class.getCanonicalName());
 
		src.methods.put("afterCreate", source(
				"@Override",
				"protected void afterCreate({mType} {mVar}) {",
				"\t// TODO Auto-generated method stub",
				"}"
			).replace("{mType}", modelName).replace("{mVar}", varName(modelName)));
 
 		return writeFile(module.observers, name + ".java", src.toSource());
 	}
 	
 	private static void createProjectFile(File project, int projectType, boolean hasViews) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		sb.append("<projectDescription>\n");
 		sb.append("\t<name>").append(project.getName()).append("</name>\n");
 		sb.append("\t<comment></comment>\n");
 		sb.append("\t<projects>\n");
 		sb.append("\t</projects>\n");
 		sb.append("\t<buildSpec>\n");
 		if(projectType == PTYPE_APP || projectType == PTYPE_APP_WS || projectType == PTYPE_MOD || projectType == PTYPE_MOD_WS) {
 			sb.append("\t\t<buildCommand>\n");
 			sb.append("\t\t\t<name>org.oobium.eclipse.workspace.OobiumBuilder</name>\n");
 			sb.append("\t\t\t<arguments>\n");
 			sb.append("\t\t\t</arguments>\n");
 			sb.append("\t\t</buildCommand>\n");
 		}
 		sb.append("\t\t<buildCommand>\n");
 		sb.append("\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n");
 		sb.append("\t\t\t<arguments>\n");
 		sb.append("\t\t</arguments>\n");
 		sb.append("\t\t</buildCommand>\n");
 		sb.append("\t\t<buildCommand>\n");
 		sb.append("\t\t<name>org.eclipse.pde.ManifestBuilder</name>\n");
 		sb.append("\t\t\t<arguments>\n");
 		sb.append("\t\t\t</arguments>\n");
 		sb.append("\t\t</buildCommand>\n");
 		sb.append("\t\t<buildCommand>\n");
 		sb.append("\t\t\t<name>org.eclipse.pde.SchemaBuilder</name>\n");
 		sb.append("\t\t\t<arguments>\n");
 		sb.append("\t\t\t</arguments>\n");
 		sb.append("\t\t</buildCommand>\n");
 		sb.append("\t</buildSpec>\n");
 		sb.append("\t<natures>\n");
 		if(projectType >= 0) {
 			sb.append("\t\t<nature>org.oobium.eclipse.OobiumNature</nature>\n");
 		}
 		switch(projectType) {
 		case PTYPE_APP_WS:	sb.append("\t\t<nature>").append(Module.NATURE_WEBSERVICE).append("</nature>\n");	// fall through
 		case PTYPE_APP:		sb.append("\t\t<nature>org.oobium.eclipse.OobiumApplication</nature>\n");	break;
 		case PTYPE_MIG:		sb.append("\t\t<nature>org.oobium.eclipse.OobiumMigration</nature>\n");		break;
 		case PTYPE_MOD_WS:	sb.append("\t\t<nature>").append(Module.NATURE_WEBSERVICE).append("</nature>\n");	// fall through
 		case PTYPE_MOD: 	sb.append("\t\t<nature>org.oobium.eclipse.OobiumModule</nature>\n");		break;
 		case PTYPE_TEST:	sb.append("\t\t<nature>org.oobium.eclipse.OobiumTestSuite</nature>\n");		break;
 		}
 		if(hasViews) {
 			sb.append("\t\t<nature>org.oobium.eclipse.esp.EspNature</nature>\n");
 		}
 		
 		sb.append("\t\t<nature>org.eclipse.pde.PluginNature</nature>\n");
 		sb.append("\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n");
 		sb.append("\t</natures>\n");
 		sb.append("</projectDescription>");
 		writeFile(project, ".project", sb.toString());
 	}
 	
 	public static File createTestSuite(Module module, Map<String, String> properties) {
 		File project = module.testSuite;
 		if(!project.exists()) {
 			project.mkdirs();
 		}
 
 		createFolder(funcFolder(project));
 		createFolder(intgFolder(project));
 		createFolder(unitFolder(project));
 		createFolder(binFolder(project));
 		createFolder(mainFolder(project));
 		createFolder(modelsFolder(project));
 		createFolder(viewsFolder(project));
 		createFolder(controllersFolder(project));
 
 		createProjectFile(project, PTYPE_TEST, false);
 		createClasspathFile(project, PTYPE_TEST);
 		createBuildFile(project, PTYPE_TEST);
 		createManifestFile(project, PTYPE_TEST, properties);
 		createPrefsFile(project);
 		
 		return project;
 	}
 	
 	private static boolean createViews(Map<String, String> properties) {
 		return (properties == null) || !"false".equals(properties.get("createViews"));
 	}
 	
 	public static File createWebservice(File project) {
 		return createWebservice(project, new HashMap<String, String>(0));
 	}
 	
 	public static File createWebservice(File project, Map<String, String> properties) {
 		if(project.exists()) {
 			throw new UnsupportedOperationException(project.getName() + " already exists");
 		} else {
 			project.mkdirs();
 		}
 
 		createFolder(srcFolder(project));
 		createFolder(binFolder(project));
 		createFolder(genFolder(project));
 
 		createFolder(mainFolder(project));
 		createFolder(modelsFolder(project));
 		createFolder(controllersFolder(project));
 		
 		createFolder(project, ".settings");
 
 		createProjectFile(project, PTYPE_APP_WS, false);
 		createClasspathFile(project, PTYPE_APP_WS);
 		createBuildFile(project, PTYPE_APP_WS);
 		createManifestFile(project, PTYPE_APP_WS, properties);
 		createPrefsFile(project);
 		createAppActivator(project, false);
 		createApplicationConfigFile(project, true);
 		createApplicationController(project);
 		
 		return project;
 	}
 	
 	public static File createFragment(File project, String host) {
 		return createFragment(project, host, null);
 	}
 	
 	public static File createFragment(File project, String host, Map<String, String> properties) {
 		if(project.exists()) {
 			throw new UnsupportedOperationException(project.getName() + " already exists");
 		} else {
 			project.mkdirs();
 		}
 
 		if(properties == null) {
 			properties = Collections.singletonMap("Fragment-Host", host);
 		} else {
 			properties.put("Fragment-Host", host);
 		}
 		
 		createFolder(srcFolder(project));
 		createFolder(binFolder(project));
 
 		createFolder(mainFolder(project));
 		
 		createFolder(project, ".settings");
 
 		createProjectFile(project, -1, false);
 		createClasspathFile(project, -1);
 		createBuildFile(project, -1);
 		createManifestFile(project, -1, properties);
 		createPrefsFile(project);
 		
 		return project;
 	}
 	
 	private static File mainFolder(File project)		{
 		String path = project.getName();
 		if(path.endsWith(".tests")) {
 			path = path.substring(0, path.length()-6);
 			return new File(unitFolder(project), path.replace('.', File.separatorChar));
 		} else {
 			return new File(srcFolder(project), path.replace('.', File.separatorChar));
 		}
 	}
 
 	private static File modelsFolder(File project) {
 		return new File(appFolder(project), "models");
 	}
 	
 	private static String property(Map<String, String> properties, String name, String defaultValue) {
 		return properties.containsKey(name) ? properties.remove(name) : defaultValue;
 	}
 	
 	private static File genFolder(File project)			{ return new File(project, "generated"); }
 	private static File libFolder(File project)			{ return new File(project, "lib"); }
 	private static File funcFolder(File project)			{ return new File(project, "src-functional"); }
 	private static File intgFolder(File project)			{ return new File(project, "src-integration"); }
 	private static File unitFolder(File project)			{ return new File(project, "src-unit"); }
 	private static File srcFolder(File project)			{ return new File(project, "src"); }
 	private static File layoutsFolder(File project)		{ return new File(viewsFolder(project), "_layouts"); }
 	private static File viewsFolder(File project)		{ return new File(appFolder(project), "views"); }
 	
 }
