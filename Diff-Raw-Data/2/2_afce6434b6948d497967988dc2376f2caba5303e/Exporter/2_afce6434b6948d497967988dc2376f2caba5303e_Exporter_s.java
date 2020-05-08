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
 
 import static org.oobium.utils.FileUtils.EXECUTABLE;
 import static org.oobium.utils.FileUtils.copy;
 import static org.oobium.utils.FileUtils.createFolder;
 import static org.oobium.utils.FileUtils.deleteContents;
 import static org.oobium.utils.FileUtils.writeFile;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.oobium.build.BuildBundle;
 import org.oobium.logging.Logger;
 import org.oobium.utils.FileUtils;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.Config.OsgiRuntime;
 
 public class Exporter {
 
 	/**
 	 * Bundles that are Applications only
 	 */
 	public static final int APP			= 1 << 0;
 	
 	/**
 	 * Bundles that are either Applications or Modules (application extends module)
 	 */
 	public static final int MODULE		= 1 << 1;
 	
 	/**
 	 * Bundles that are Migrations
 	 */
 	public static final int MIGRATION	= 1 << 2;
 
 	/**
 	 * Bundles that export a Service
 	 */
 	public static final int SERVICE		= 1 << 3;
 
 	
 	private static final String START_SCRIPT = 	"#!/bin/bash\n" +
 												"pidfile=running.pid\n" +
 												"if [ -e $pidfile ]; then\n" +
 												"  echo \"Already running\"\n" +
 												"  exit 1\n" +
 												"else\n" +
 												"  java -jar bin/felix.jar &\n" +
 												"  echo $! > $pidfile\n" +
 												"fi";
 
 	private static final String STOP_SCRIPT = 	"#!/bin/bash\n" +
 												"pidfile=running.pid\n" +
 												"if [ -e $pidfile ]; then\n" +
 												"  kill `cat $pidfile`\n" +
 												"  rm $pidfile\n" +
 												"  echo \"Process stopped\"\n" +
 												"else\n" +
 												"  echo \"Cannot find $pidfile file - is process actually running?\"\n" +
 												"  exit 1\n" +
 												"fi";
 
 
 	private final Logger logger;
 	
 	private final Workspace workspace;
 	private final Set<Application> applications;
 	private final Set<Bundle> start;
 	private final Set<Bundle> includes;
 	private final File exportDir;
 	private final File binDir;
 	private final File bundleDir;
 
 	private boolean clean;
 	private boolean cleanCache;
 	private Mode mode;
 	
 	private Set<Bundle> exportedBundles;
 	private Set<Bundle> exportedStart;
 	
 	private int startTypes;
 	
 	public Exporter(Workspace workspace) {
 		this(workspace, workspace.getApplications());
 	}
 	
 	public Exporter(Workspace workspace, Application...applications) {
 		this(workspace, Arrays.asList(applications));
 	}
 	
 	public Exporter(Workspace workspace, Collection<Application> applications) {
 		this.logger = Logger.getLogger(BuildBundle.class);
 		this.workspace = workspace;
 		this.applications = new LinkedHashSet<Application>(applications);
 		this.start = new LinkedHashSet<Bundle>();
 		this.includes = new LinkedHashSet<Bundle>();
 		this.exportDir = new File(workspace.getWorkingDirectory(), "export");
 		this.binDir = new File(exportDir, "bin");
 		this.bundleDir = new File(exportDir, "bundles");
 
 		this.mode = Mode.DEV;
 
 		this.exportedBundles = new LinkedHashSet<Bundle>();
 		this.exportedStart = new LinkedHashSet<Bundle>();
 		
 		setStartTypes(MODULE | SERVICE);
 	}
 
 	
 	public void setStartTypes(int startTypes) {
 		this.startTypes = startTypes;
 	}
 	
 	public void clearServices() {
 		includes.clear();
 	}
 	
 	public void clearStartBundles() {
 		start.clear();
 	}
 	
 	public void add(Bundle...bundles) {
 		add(Arrays.asList(bundles));
 	}
 	
 	public void add(Collection<? extends Bundle> bundles) {
 		includes.addAll(bundles);
 	}
 	
 	public void addStart(Bundle...bundles) {
 		add(bundles);
 		addStart(Arrays.asList(bundles));
 	}
 	
 	public void addStart(Collection<? extends Bundle> bundles) {
 		add(bundles);
 		start.addAll(bundles);
 	}
 	
 	/**
 	 * Only good for Apache Felix - will need to modify if supporting
 	 * additional runtimes in the future...
 	 */
 	private void createConfig() {
 		File configDir = createFolder(exportDir, "conf");
 		
 		File system = new File(configDir, "system.properties");
 		StringBuilder sb = new StringBuilder();
 		sb.append(Mode.SYSTEM_PROPERTY).append("=").append(mode).append('\n');
 		switch(mode) {
 		case DEV:
 			sb.append(Logger.SYS_PROP_CONSOLE).append('=').append(Logger.DEBUG).append('\n');
 			sb.append(Logger.SYS_PROP_EMAIL).append('=').append(Logger.NEVER).append('\n');
 			sb.append(Logger.SYS_PROP_FILE).append('=').append(Logger.DEBUG);
 			break;
 		case TEST:
 			sb.append(Logger.SYS_PROP_CONSOLE).append('=').append(Logger.DEBUG).append('\n');
 			sb.append(Logger.SYS_PROP_EMAIL).append('=').append(Logger.NEVER).append('\n');
 			sb.append(Logger.SYS_PROP_FILE).append('=').append(Logger.INFO);
 			break;
 		case PROD:
 			sb.append(Logger.SYS_PROP_CONSOLE).append('=').append(Logger.TRACE).append('\n');
 			sb.append(Logger.SYS_PROP_EMAIL).append('=').append(Logger.NEVER).append('\n');
 			sb.append(Logger.SYS_PROP_FILE).append('=').append(Logger.INFO);
 			break;
 		}
 		writeFile(system, sb.toString());
 
 		File config = new File(configDir, "config.properties");
 
 		List<Bundle> installed = new ArrayList<Bundle>();
 		List<Bundle> started1 = new ArrayList<Bundle>();
 		List<Bundle> started2 = new ArrayList<Bundle>();
 		for(Bundle bundle : exportedBundles) {
 			if(!bundle.isFramework()) {
				if(bundle.name.equals("org.oobium.logging")) {
 					started1.add(bundle);
 				} else if(exportedStart.contains(bundle)) {
 					started2.add(bundle);
 				} else {
 					installed.add(bundle);
 				}
 			}
 		}
 
 		sb = new StringBuilder();
 		sb.append("felix.auto.install.1=");
 		for(Bundle bundle : installed) {
 			sb.append(" \\\n file:bundles/").append(bundle.file.getName());
 		}
 		sb.append("\n\nfelix.auto.start.1=");
 		for(Bundle bundle : started1) {
 			sb.append(" \\\n file:bundles/").append(bundle.file.getName());
 		}
 		sb.append("\n\nfelix.auto.start.2=");
 		for(Bundle bundle : started2) {
 			sb.append(" \\\n file:bundles/").append(bundle.file.getName());
 		}
 		sb.append("\n\norg.osgi.framework.startlevel.beginning=2");
 
 		writeFile(config, sb.toString());
 	}
 	
 	private void createScripts() {
 		File startScript = new File(exportDir, "start.sh");
 		if(startScript.exists()) {
 			logger.info("skipping start script");
 		} else {
 			logger.info("writing start script");
 			writeFile(startScript, START_SCRIPT, EXECUTABLE);
 		}
 		
 		File stopScript = new File(exportDir, "stop.sh");
 		if(stopScript.exists()) {
 			logger.info("skipping stop script");
 		} else {
 			logger.info("writing stop script");
 			writeFile(stopScript, STOP_SCRIPT, EXECUTABLE);
 		}
 	}
 
 	private Bundle doExport(Bundle bundle) throws IOException {
 		File jar;
 		if(bundle.isJar) {
 			jar = "felix.jar".equals(bundle.file.getName()) ? new File(binDir, bundle.file.getName()) : new File(bundleDir, bundle.file.getName());
 			if(jar.exists()) {
 				logger.info("  skipping " + bundle);
 			} else {
 				logger.info("  copying " + bundle);
 				copy(bundle.file, jar);
 			}
 		} else {
 			Date date = new Date(FileUtils.getLastModified(bundle.bin));
 			Version version = bundle.version.resolve(date);
 			jar = new File(bundleDir, bundle.name + "_" + version + ".jar");
 			if(jar.exists()) {
 				logger.info("  skipping " + bundle);
 			} else {
 				logger.info("  creating " + bundle);
 				bundle.createJar(jar, version);
 			}
 		}
 		
 		Bundle exportedBundle = Bundle.create(jar);
 		if(start.contains(bundle)) {
 			exportedStart.add(exportedBundle);
 		} else {
 			if((startTypes & APP) != 0 && exportedBundle.isApplication()) {
 				exportedStart.add(exportedBundle);
 			}
 			if((startTypes & MODULE) != 0 && exportedBundle.isModule()) {
 				exportedStart.add(exportedBundle);
 			}
 			if((startTypes & SERVICE) != 0 && exportedBundle.isService()) {
 				exportedStart.add(exportedBundle);
 			}
 			if((startTypes & MIGRATION) != 0 && exportedBundle.isMigration()) {
 				exportedStart.add(exportedBundle);
 			}
 		}
 		exportedBundles.add(exportedBundle);
 		return exportedBundle;
 	}
 
 	/**
 	 * Export the application, configured for the given mode.
 	 * @return a File object for the folder where the application was exported.
 	 * @throws IOException
 	 */
 	public File export() throws IOException {
 		if(exportDir.exists()) {
 			if(clean) {
 				FileUtils.deleteContents(exportDir);
 			} else if(cleanCache) {
 				File felixCache = new File(exportDir, "felix-cache");
 				if(felixCache.exists()) {
 					FileUtils.delete(felixCache);
 				}
 			}
 		} else {
 			exportDir.mkdirs();
 		}
 
 		logger.info("determining required bundles");
 		Set<Bundle> bundles = new TreeSet<Bundle>();
 		for(Application application : applications) {
 			if(!bundles.contains(application)) {
 				bundles.addAll(application.getDependencies(workspace, mode));
 				bundles.add(application);
 			}
 		}
 		for(Bundle bundle : includes) {
 			if(!bundles.contains(bundle)) {
 				bundles.addAll(bundle.getDependencies(workspace, mode));
 				bundles.add(bundle);
 			}
 		}
 		workspace.setRuntimeBundle(OsgiRuntime.Felix, bundles);
 		
 		for(Bundle bundle : start) {
 			if(!bundles.contains(bundle)) {
 				throw new IllegalStateException("bundle is in the start list, but is not being exported: " + bundle);
 			}
 		}
 		
 		if(logger.isLoggingInfo()) {
 			for(Bundle bundle : bundles) {
 				logger.info("  " + bundle);
 			}
 		}
 		
 		logger.info("creating and copying bundles");
 		for(Bundle bundle : bundles) {
 			doExport(bundle);
 		}
 
 		deleteContents(new File(exportDir, "configuration"));
 		createConfig();
 		createScripts();
 		
 		return exportDir;
 	}
 	
 	/**
 	 * Export an individual bundle
 	 * @param mode
 	 * @param bundle
 	 * @return the exported Bundle (note that this bundle will not be available to Workspace.getBundle())
 	 * @throws IOException
 	 */
 	public Bundle export(Bundle bundle) throws IOException {
 		if(exportDir.exists()) {
 			if(clean) {
 				FileUtils.deleteContents(exportDir);
 			} else if(cleanCache) {
 				File felixCache = new File(exportDir, "felix-cache");
 				if(felixCache.exists()) {
 					FileUtils.delete(felixCache);
 				}
 			}
 		} else {
 			exportDir.mkdirs();
 		}
 		
 		return doExport(bundle);
 	}
 
 	private void exportMigration(Application application, Mode mode) throws IOException {
 		Migrator migration = workspace.getMigratorFor(application);
 		if(migration != null) {
 			if(!workspace.getWorkingDirectory().exists()) {
 				workspace.getWorkingDirectory().mkdirs();
 			}
 
 			// make sure that the schema files have been copied to the bin directory
 			
 			logger.info("determining required bundles");
 			Set<Bundle> bundles = migration.getDependencies(workspace, mode);
 			bundles.add(migration);
 			
 			workspace.removeRuntimeBundle(bundles);
 			
 			// export only those bundles directly related to the migration
 			bundles.remove(application);
 			bundles.removeAll(application.getDependencies(workspace, mode));
 			
 			if(logger.isLoggingInfo()) {
 				for(Bundle bundle : bundles) {
 					logger.info("  " + bundle);
 				}
 			}
 			
 			logger.info("creating and copying bundles");
 			for(Bundle bundle : bundles) {
 				doExport(bundle);
 			}
 			
 		}
 	}
 
 	/**
 	 * Export all migrations for this application and the dependencies
 	 * configured for the given mode.
 	 * @param mode
 	 * @return a list of bundles that have been exported during this operation. Note that these
 	 * are only bundles related to the migration; bundles for the application export are not
 	 * exported during this operation, nor are they returned with this list.
 	 * @throws IOException
 	 */
 	public Set<Bundle> exportMigration(Mode mode) throws IOException {
 		for(Application application : applications) {
 			exportMigration(application, mode);
 		}
 		return exportedBundles;
 	}
 	
 	public boolean getClean() {
 		return clean;
 	}
 	
 	public boolean getCleanCache() {
 		return cleanCache;
 	}
 	
 	public File getExportDir() {
 		return exportDir;
 	}
 	
 	public File getExportedJar(Bundle bundle) {
 		if(!exportDir.exists()) {
 			return null;
 		}
 
 		if("felix.jar".equals(bundle.file.getName())) {
 			File exported = new File(exportDir + File.separator + "bin" + File.separator + "felix.jar");
 			return exported.exists() ? exported : null;
 		} else {
 			File bundleDir = new File(exportDir + File.separator + "bundles");
 			final String bundleName = bundle.file.getName() + "_";
 			File[] exportedBundles = bundleDir.listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(File dir, String name) {
 					return name.startsWith(bundleName) && name.endsWith(".jar");
 				}
 			});
 			if(exportedBundles != null && exportedBundles.length > 0) {
 				File exported = exportedBundles[0];
 				for(int i = 1; i < exportedBundles.length; i++) {
 					if(exported.getName().compareTo(exportedBundles[i].getName()) < 0) {
 						exported = exportedBundles[i];
 					}
 				}
 				return exported;
 			}
 			return null;
 		}
 	}
 	
 	public Mode getMode() {
 		return mode;
 	}
 	
 	public void setClean(boolean clean) {
 		this.clean = clean;
 	}
 
 	public void setCleanCache(boolean cleanCache) {
 		this.cleanCache = cleanCache;
 	}
 	
 	public void setMode(Mode mode) {
 		this.mode = mode;
 	}
 
 }
