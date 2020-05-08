 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.security.CodeSource;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.IOUtils;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.python.copiedfromeclipsesrc.JavaVmLocationFinder;
 import org.python.pydev.core.IInterpreterInfo;
 import org.python.pydev.core.IInterpreterManager;
 import org.python.pydev.core.IPythonNature;
 import org.python.pydev.core.MisconfigurationException;
 import org.python.pydev.core.REF;
 import org.python.pydev.core.Tuple;
 import org.python.pydev.debug.newconsole.PydevConsoleConstants;
 import org.python.pydev.editor.codecompletion.revisited.ModulesManagerWithBuild;
 import org.python.pydev.plugin.PydevPlugin;
 import org.python.pydev.runners.SimpleJythonRunner;
 import org.python.pydev.ui.interpreters.JythonInterpreterManager;
 import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	/**
 	 * Setup the logging facilities
 	 */
 	transient private static final Logger logger = LoggerFactory.getLogger(Activator.class);
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "uk.ac.diamond.scisoft";
 
 	private static final String RUN_IN_ECLIPSE = "run.in.eclipse";
 
 	private static final String JYTHON_VERSION = "2.5.1";
 	private static final String INTERPRETER_NAME = "Jython" + JYTHON_VERSION;
 	private static final String GIT_SUFFIX = "_git";
 
 	// The shared instance
 	private static Activator plugin;
 
 	private static final String[] requiredKeys = {"org.python.pydev",
 		"uk.ac.gda.libs",
 		"cbflib",
 		"org.apache.commons.codec",
 		"org.apache.commons.math",
 		"uk.ac.diamond.CBFlib",
 		"uk.ac.diamond.jama",
 		"uk.ac.diamond.scisoft",
 		"uk.ac.diamond.scisoft.ncd",
 		"uk.ac.diamond.scisoft.ncd.rcp",
 		"uk.ac.gda.common",
 		"jhdf",
 		"com.springsource.slf4j",
 		"com.springsource.ch.qos.logback",
 		"com.springsource.org.castor",
 		"com.springsource.org.exolab.castor",
 		"com.springsource.org.apache.commons",
 		"com.springsource.javax.media",
 		"jtransforms",
 		"jai_imageio",
 		"it.tidalwave.imageio.raw",
 		"vecmath",
 		"jython",
 		"commons-math",
 		"uk.ac.diamond.org.apache.ws.commons.util",
 		"uk.ac.diamond.org.apache.xmlrpc.client",
 		"uk.ac.diamond.org.apache.xmlrpc.common",
 		"uk.ac.diamond.org.apache.xmlrpc.server",
 	};
 
 	private final static String[] pluginKeys = { "uk.ac.diamond", "uk.ac.gda", "ncsa.hdf" };
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	private Bundle libBundle = null;
 	
 	/**
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		
 		// First thing to do here is to try to set up the logging properly.
 		// during this, System.out will be used for logging
 		try {
 			System.out.println("Starting to Configure Logger");
 			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 			loggerContext.reset();
 			
 			System.out.println("Logger Context Reset");
 			
 			// now find the configuration file
 			ProtectionDomain pd = Activator.class.getProtectionDomain();
 			CodeSource cs = pd.getCodeSource();
 			URL url = cs.getLocation();
 			File file = new File(url.getFile(), "logging/log_configuration.xml");
 			url = new URL("File://"+file.getAbsolutePath());
 			
 			if (file.exists()) {
 				System.out.println("Logging Configuration File found at '"+url+"'");
 			} else {
 				System.out.println("Logging Configuration File Not found at '"+url+"'");
 			}
 			
 			String logloc = System.getProperty("log.folder");
 			if (logloc == null) {
 				System.out.println("Log folder property not set, setting this manualy to the temp directory");
 				String tmpDir = System.getProperty("java.io.tmpdir");
 				System.setProperty("log.folder", tmpDir);
 			}
 			
 			System.out.println("log.folder java property set to '"+System.getProperty("log.folder")+"'");
 			
 			
 			JoranConfigurator configurator = new JoranConfigurator();
 			configurator.setContext(loggerContext);
 			configurator.doConfigure(url);
 			
 			System.out.println("Logging Configuration complete");
 			
 		} catch (Exception e) {
 			System.out.println("Could not set up logging properly, loggin to stdout for now, error follows");
 			e.printStackTrace();
 			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 			loggerContext.reset();
 		} 
 		
 		
 		// Get the libraries bundle
 		Bundle[] bundles = context.getBundles();
 		
 		for (Bundle bundle : bundles) {
 			if(bundle.toString().contains("uk.ac.gda.libs")) {
 				libBundle = bundle;
 			}
 		}
 		
 		logger.debug("Librabry Bundle is {}", libBundle.toString());
 		
 		plugin = this;
 
 		// need to set some preferences to get the Pydev features working.
 		IPreferenceStore pydevDebugPreferenceStore =  new ScopedPreferenceStore(InstanceScope.INSTANCE,"org.python.pydev.debug");
 
 		pydevDebugPreferenceStore.setDefault(PydevConsoleConstants.INITIAL_INTERPRETER_CMDS, "#Configuring Jython Environment, please wait\nimport sys;sys.executable=''\n");
 		pydevDebugPreferenceStore.setDefault(PydevConsoleConstants.INTERACTIVE_CONSOLE_VM_ARGS, "-Xmx512m");
 		pydevDebugPreferenceStore.setDefault(PydevConsoleConstants.INTERACTIVE_CONSOLE_MAXIMUM_CONNECTION_ATTEMPTS, 500);
 
 		// We need to point Jython cache to a directory writable by a user
 		// Setting -Dpython.cachedir option to .jython_cachedir in users SDA workspace
 		IPreferenceStore pydevPreferenceStore =  new ScopedPreferenceStore(InstanceScope.INSTANCE,"org.python.pydev");
 		IPath cachdir_path = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".jython_cachedir");
 		pydevPreferenceStore.setDefault(IInterpreterManager.JYTHON_CACHE_DIR, cachdir_path.toOSString());
 
 		// Doing the pydev imports properly
 		logger.debug("Starting Jython Property process");
 
		Thread job = new Thread("Initialising script analyser") {
 			@Override
			public void run() {
 				try {
					initialiseInterpreter(new NullProgressMonitor());
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 		};
		job.start();
 	}
 
 	private void logPaths(String pathname, String paths) {
 		if (paths == null)
 			return;
 		logger.debug(pathname);
 		for (String p : paths.split(File.pathSeparator))
 			logger.debug("\t{}", p);
 	}
 
 	private void initialiseInterpreter(IProgressMonitor monitor) throws CoreException {
 
 		logger.debug("Initialising the Jython interpreter setup");
 
 		// Horrible Hack warning: This code is copied from parts of Pydev to set up the interpreter and save it.
 		{
 
 			String defaultPluginsDir = System.getenv("SDA_HOME");
 			File pluginsDir = null;
 			if (defaultPluginsDir != null) {
 				pluginsDir = new File(defaultPluginsDir);
 				logger.debug("SDA_HOME defined as " + defaultPluginsDir);
 				if (!pluginsDir.isDirectory()) {
 					logger.debug("But SDA_HOME does not exist or is not a directory");
 					pluginsDir = null;
 				}
 			}
 			if (pluginsDir == null)
 				pluginsDir = getPluginsDirectory();
 			if (pluginsDir == null) {
 				logger.error("Failed to find plugins directory!");
 				return;
 			}
 
 			
 			// Code copies from Pydev when the user chooses a Jython interpreter - these are the defaults		
 			String executable = new File(getInterpreterDirectory(pluginsDir), "jython.jar").getAbsolutePath();
 			
 			
 			if(!(new File(executable)).exists()) { 
 			
 				logger.warn("Could not find jython jar, looking again");
 				// try to find the jar another way
 				String bundleLoc = libBundle.getLocation().replace("reference:file:", "");
 				File jarPath = new File(bundleLoc,"jython2.5.1/jython.jar");
 				
 				executable = jarPath.getAbsolutePath();
 
 			}
 			
 			
 			logger.debug("executable path = {}", executable);
 
 			// check for the existence of this standard pydev script
 			final File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
 			if (!script.exists()) {
 				logger.error("The file specified does not exist: {} ", script);
 				throw new RuntimeException("The file specified does not exist: " + script);
 			}
 
 			logger.debug("Script path = {}", script.getAbsolutePath());
 			
 			
 			String[] cmdarray = {"java","-Xmx64m","-jar",executable, REF.getFileAbsolutePath(script)};
 			File workingDir = new File("/tmp/");
 			IPythonNature nature = null;//new PythonNature();
 			Tuple<Process, String> outTup2 = new SimpleJythonRunner().run(cmdarray, workingDir, nature, monitor);
 		
 			String outputString = "";
 			try {
 				outputString = IOUtils.toString(outTup2.o1.getInputStream());
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				logger.error("TODO put description of error here", e1);
 			}
 			  
 			logger.debug("Output String is {}", outputString);
 
 			// this is the main info object which contains the environment data
 			InterpreterInfo info = null;
 
 			try {
 				// HACK Otherwise Pydev shows a dialog to the user.
 				ModulesManagerWithBuild.IN_TESTS = true;
 				//info = InterpreterInfo.fromString(outTup.o1, false);
 				info = InterpreterInfo.fromString(outputString, false);
 			} catch (Exception e) {
 				logger.error("InterpreterInfo.fromString(outTup.o1) has failed in pydev setup with exception");
 				logger.error("{}",e);
 
 			} finally {
 				ModulesManagerWithBuild.IN_TESTS = false;
 			}		
 
 			if (info == null) {
 				logger.error("pydev info is set to null");
 				return;
 			}
 
 			// set of python paths
 			Set<String> pyPaths = new HashSet<String>();
 
 			// the executable is the jar itself
 			info.executableOrJar = executable;
 
 			if (System.getProperty("os.name").contains("Windows"))
 				logPaths("Library paths:", System.getenv("PATH"));
 			else
 				logPaths("Library paths:", System.getenv("LD_LIBRARY_PATH"));
 
 			logPaths("Class paths:", System.getProperty("java.library.path"));
 
 			// we have to find the jars before we restore the compiled libs
 			List<File> jars = JavaVmLocationFinder.findDefaultJavaJars();
 			for (File jar : jars) {
 				if (pyPaths.contains(jar.getAbsolutePath())) {
 					logger.warn("File already there!");
 				}
 				pyPaths.add(jar.getAbsolutePath());
 			}
 
 			// Defines all third party libs that can be used in scripts.
 			logger.debug("Adding files to library path");
 			final List<File> gdaJars = findJars(pluginsDir);
 			for (File file : gdaJars) {
 				if (pyPaths.contains(file.getAbsolutePath())) {
 					logger.warn("File already there!");
 				}
 				pyPaths.add(file.getAbsolutePath());
 				logger.debug("Adding jar file to library path : {} ", file.getAbsolutePath());
 			}
 
 			final List<File> gdaDirs = findDirs(pluginsDir);
 
 			logger.debug("All Jars prepared");
 
 
 			String runProp = System.getProperty(RUN_IN_ECLIPSE);
 			if (runProp != null && runProp.equalsIgnoreCase("true")) {
 				File bundles = pluginsDir.getParentFile();
 				if (bundles.isDirectory()) {
 					// ok checking for items inside the tp directory
 					bundles = new File(bundles, "tp");
 					if (bundles.isDirectory()) {
 						bundles = new File(bundles, "plugins");
 						final List<File> tJars = findJars(bundles);
 						for (File file : tJars) {
 							if (pyPaths.contains(file.getAbsolutePath())) {
 								logger.warn("File already there!");
 							}
 							pyPaths.add(file.getAbsolutePath());
 							logger.debug("Adding jar file to library path : {} ", file.getAbsolutePath());
 						}						
 					}
 				}
 
 				// add plugins and ScisoftPy package
 				for (File file: gdaDirs) {
 					File b = new File(file, "bin");
 					if (b.isDirectory()) {
 						if (pyPaths.contains(b.getAbsolutePath())) {
 							logger.warn("File already there!");
 						}
 						pyPaths.add(b.getAbsolutePath());
 						logger.debug("Adding dir to library path : {} ", b.getAbsolutePath());
 					} 
 					// also check for internal jars
 					File j = new File(file, "jars");
 					if (j.isDirectory()) {
 						for(File jar : j.listFiles()) {
 							if (pyPaths.contains(jar.getAbsolutePath())) {
 								logger.warn("File already there!");
 							}
 							pyPaths.add(jar.getAbsolutePath());
 							logger.debug("Adding jar to library path : {} ", jar.getAbsolutePath());
 						}
 					} 
 					// and 1 furthur possible place
 					// also check for internal jars
 					File jj = new File(j, "ext");
 					if (jj.isDirectory()) {
 						for(File jar : jj.listFiles()) {
 							if (pyPaths.contains(jar.getAbsolutePath())) {
 								logger.warn("File already there!");
 							}
 							pyPaths.add(jar.getAbsolutePath());
 							logger.debug("Adding jar to library path : {} ", jar.getAbsolutePath());
 						}
 					}
 
 				}
 			} else {
 				// and add all unjarred folders
 				for (File file: gdaDirs) {
 					if (pyPaths.contains(file.getAbsolutePath())) {
 						logger.warn("File already there!");
 					}
 					pyPaths.add(file.getAbsolutePath());
 					logger.debug("Adding dir to library path : {} ", file.getAbsolutePath());
 				}
 			}
 
 			info.libs.addAll(pyPaths);
 
 			// now set up the LD_LIBRARY_PATH, or PATH for windows
 			File libraryDir = new File(pluginsDir.getParentFile(), "lib");
 			String libraryPath;
 			if (libraryDir.exists()) {
 				libraryPath = libraryDir.getAbsolutePath();
 			} else {
 				StringBuilder allPaths = new StringBuilder();
 				String osarch = Platform.getOS() + "-" + Platform.getOSArch();
 				logger.debug("Using OS and ARCH: {}", osarch);
 				for (File dir : gdaDirs) {
 					File d = new File(dir, "lib");
 					if (d.isDirectory()) {
 						d = new File(d, osarch);
 						if (d.isDirectory()) {
 							if (allPaths.length() != 0) {
 								allPaths.append(File.pathSeparatorChar);
 							}
 							allPaths.append(d.getAbsolutePath());
 						}
 					}
 				}
 
 				libraryPath = allPaths.toString();
 			}
 
 			// TODO include 'Mac OS X' if needed "DYLD_LIBRARY_PATH" + others ...
 			String pathEnv = System.getProperty("os.name").contains("Windows") ? "PATH" : "LD_LIBRARY_PATH";
 			String env = pathEnv + "=" + libraryPath + File.pathSeparator + System.getenv(pathEnv);
 
 			logPaths("Setting " + pathEnv + " for dynamic libraries", env);
 
 			info.setEnvVariables(new String[] {env});
 
 			// java, java.lang, etc should be found now
 			info.restoreCompiledLibs(monitor);
 			info.setName(INTERPRETER_NAME);
 
 			logger.debug("Finalising the Jython interpreter manager");
 
 			final JythonInterpreterManager man = (JythonInterpreterManager) PydevPlugin.getJythonInterpreterManager();
 			HashSet<String> set = new HashSet<String>();
 			set.add(INTERPRETER_NAME);
 			man.setInfos(new IInterpreterInfo[] {info},set, monitor);
 
 			logger.debug("Finished the Jython interpreter setup");
 		}
 	}
 
 	private static String JYTHON_DIR = "jython" + JYTHON_VERSION;
 	private File getInterpreterDirectory(File pluginsDir) {
 
 		for (File file : pluginsDir.listFiles()) {
 			if(file.getName().startsWith("uk.ac.gda.libs_")) {
 				File d = new File(file, JYTHON_DIR);
 				return d;
 			}
 
 		}
 		logger.error("Could not find a folder for 'uk.ac.gda.libs' defaulting to standard");
 		return new File(pluginsDir, "uk.ac.gda.libs/" + JYTHON_DIR);
 	}
 
 	/**
 	 * Method returns recursively all the jars found in a directory
 	 * 
 	 * @return list of jar Files
 	 */
 	public static final List<File> findJars(File directoryName) {
 
 		final List<File> libs = new ArrayList<File>();
 
 		if (directoryName.exists() && directoryName.isDirectory()) {
 			for (File f : directoryName.listFiles()) {
 
 				// if the file is a jar, then add it
 				if (f.getName().endsWith(".jar")) {
 					if (isRequired(f, requiredKeys)) {
 						libs.add(f);
 					}
 				} else if (f.isDirectory()) {
 					for (File file : findJars(f)) {
 						libs.add(file);
 					}
 				}
 			}
 		}
 
 		return libs;
 	}
 
 	/**
 	 * Method returns path to directories
 	 * 
 	 * @return list of directories
 	 */
 	public static final List<File> findDirs(File directoryName) {
 
 		// ok we get the plugins directory here, so we need to explore a bit further for git
 		final List<File> libs = new ArrayList<File>();
 
 		// get the basic plugins directory
 		if (directoryName.exists() && directoryName.isDirectory()) {
 			for (File f : directoryName.listFiles()) {
 				if (f.isDirectory()) {
 					if (isRequired(f, pluginKeys))
 						logger.debug("Adding library directory {}", f);
 					libs.add(f);
 				}
 			}
 		}
 
 		// get down to the git checkouts
 		// only do this if we are running inside Eclipse
 
 		String runProp = System.getProperty(RUN_IN_ECLIPSE);
 		if (runProp != null && runProp.equalsIgnoreCase("true")) {
 
 			String gitpathname = directoryName.getParentFile().getAbsolutePath() + GIT_SUFFIX;
 
 			List<File> dirs = new ArrayList<File>();
 
 			for (File d : new File(gitpathname).listFiles()) {
 				if (d.isDirectory()) {
 					String n = d.getName();
 					if (n.endsWith(".git")) {
 						dirs.add(d);
 					} else if (n.equals("scisoft")) {
 						for (File f : d.listFiles()) {
 							if (f.isDirectory()) {
 								dirs.add(f);
 							}
 						}
 					}
 				}
 			}
 
 			for (File f : dirs) {
 				for (File plugin : f.listFiles()) {
 					if (plugin.isDirectory()) {
 						if (isRequired(plugin, pluginKeys))
 							libs.add(plugin);
 					}
 				}
 			}
 		}
 
 		return libs;
 	}
 
 	private static boolean isRequired(File file, String[] keys) {
 		String filename = file.getName();
 		logger.debug("Jar/dir found: {}", filename);
 		for (String key : keys) {
 			if (filename.startsWith(key)) return true;
 		}
 		return false;
 	}
 
 
 
 	/**
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	private File getPluginsDirectory() {
 		Bundle b = getBundle();
 		logger.debug("Bundle: {}", b);
 		try {
 			File f = FileLocator.getBundleFile(b);
 			logger.debug("Bundle location: {}", f.getParent());
 
 			String runProp = System.getProperty(RUN_IN_ECLIPSE);
 			if (runProp != null && runProp.equalsIgnoreCase("true")) {
 				File git = f.getParentFile().getParentFile().getParentFile();
 				File parent = git.getParentFile();
 				String projectName = git.getName().replace(GIT_SUFFIX, "");
 				File plugins = new File(parent, projectName + "/plugins");
 
 				if(plugins.isDirectory()) {
 					logger.debug("Plugins location: {}", plugins);
 					return plugins;
 				}
 			}
 
 			return f.getParentFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unused")
 	private boolean isInterpreter(final IProgressMonitor monitor) {
 
 		final InterpreterThread checkInterpreter = new InterpreterThread(monitor);
 		checkInterpreter.start();
 
 		int totalTimeWaited = 0;
 		while (!checkInterpreter.isFinishedChecking()) {
 			try {
 				if (totalTimeWaited > 4000) {
 					logger.error("Unable to call getInterpreterInfo() method on pydev, " +
 							"assuming interpreter is already created.");
 					return true;
 				}
 				Thread.sleep(100);
 				totalTimeWaited += 100;
 			} catch (InterruptedException ne) {
 				break;
 			}
 		}
 
 		if (checkInterpreter.isInterpreter())
 			return true;
 		return false;
 	}
 
 	private class InterpreterThread extends Thread {
 
 		private IInterpreterInfo info = null;
 		private IProgressMonitor monitor;
 		private boolean finishedCheck = false;
 
 		InterpreterThread(final IProgressMonitor monitor) {
 			super("Interpreter Info");
 			setDaemon(true);// This is not that important
 			this.monitor = monitor;
 		}
 
 		@Override
 		public void run() {
 			// Might never return...
 			try {
 				info = PydevPlugin.getJythonInterpreterManager().getInterpreterInfo(INTERPRETER_NAME,
 						monitor);
 			} catch (MisconfigurationException e) {
 				logger.error("Jython is not configured properly", e);
 			}
 			finishedCheck = true;
 		}
 
 		public boolean isInterpreter() {
 			return info != null;
 		}
 
 		public boolean isFinishedChecking() {
 			return finishedCheck;
 		}
 
 	}
 
 }
