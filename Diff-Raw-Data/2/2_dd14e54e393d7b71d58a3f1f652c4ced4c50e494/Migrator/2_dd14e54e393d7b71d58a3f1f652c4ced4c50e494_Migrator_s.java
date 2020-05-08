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
 package org.oobium.build.workspace;
 
 import static org.oobium.utils.Config.MIGRATION_SERVICE;
 import static org.oobium.utils.StringUtils.blank;
 
 import java.io.File;
 import java.util.Set;
 import java.util.jar.Manifest;
 
 import org.oobium.utils.Config;
 import org.oobium.utils.Config.Mode;
 
 public class Migrator extends Bundle {
 
 	/**
 	 * this Migrator class file
 	 */
 	public final File migrator;
 	
 	/**
 	 * this migrator's configuration file
 	 */
 	public final File config;
 	
 	/**
 	 * this migrator's migrations directory
 	 */
 	public final File migrations;
 	
 	/**
 	 * this migrator's generated directory
 	 */
 	public final File generated;
 	
 	/**
 	 * this migrator's main generated source directory
 	 */
 	public final File genMain;
 	
 	/**
 	 * this migrator's generated migrations directory
 	 */
 	public final File genMigrations;
 	
 	/**
 	 * the name of this migration's module bundle
 	 */
 	public final String module;
 	
 	Migrator(Type type, File file, Manifest manifest) {
 		super(type, file, manifest);
 		this.migrator = new File(main, "Migrator.java");
 		this.config = new File(main, "configuration.js");
 		this.migrations = new File(main, "migrations");
 		this.generated = new File(file, "generated");
 		this.genMain = new File(generated, name.replaceAll("\\.", File.separator));
 		this.genMigrations = new File(genMain, "migrations");
		this.module = name.substring(0, name.length() - 10);
 	}
 
 	public Bundle getMigratorService(Workspace workspace, Mode mode) {
 		Config config = loadConfiguration();
 		return workspace.getBundle(config.getString(MIGRATION_SERVICE, mode));
 	}
 
 	public Config loadConfiguration() {
 		return Config.loadConfiguration(config);
 	}
 
 	@Override
 	protected void addDependencies(Workspace workspace, Mode mode, Set<Bundle> dependencies) {
 		super.addDependencies(workspace, mode, dependencies);
 		
 		Config configuration = loadConfiguration();
 		
 		addDependency(workspace, mode, configuration.getString(MIGRATION_SERVICE, mode), dependencies);
 	}
 
 	protected void addDependency(Workspace workspace, Mode mode, String fullName, Set<Bundle> dependencies) {
 		if(!blank(fullName)) {
 			Bundle bundle = workspace.getBundle(fullName);
 			if(bundle != null) {
 				dependencies.add(bundle);
 				bundle.addDependencies(workspace, mode, dependencies);
 			} else {
 				throw new IllegalStateException(this + " has an unresolved requirement: " + fullName);
 			}
 		}
 	}
 
 }
