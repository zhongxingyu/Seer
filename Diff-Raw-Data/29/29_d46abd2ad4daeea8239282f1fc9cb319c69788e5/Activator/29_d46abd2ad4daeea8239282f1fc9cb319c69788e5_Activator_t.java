 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
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
 	
 	public static final String INTERPRETER_NAME = "Jython2.5.1";
 
 	// The shared instance
 	private static Activator plugin;
 
 	private static String[] requiredKeys = {"org.python.pydev",
 											"uk.ac.gda.libs",
 											"cbflib",
 											"org.apache.commons.codec",
 											"org.apache.commons.math",
 											"uk.ac.diamond.CBFlib",
 											"uk.ac.diamond.jama",
 											"uk.ac.diamond.scisoft",
 											"jnexus",
 											"uk.ac.gda.nexus",
 											"uk.ac.gda.common",
 											"uk.ac.gda.server.ncd",
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
 
 	private static String[] pluginKeys = { "uk.ac.diamond", "uk.ac.gda", "ncsa.hdf" };
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
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
 		
 		Job job = new Job("Initialising script analyser"){
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				
 				monitor.setTaskName("Initialising interpreter");
 				try {
 					initialiseInterpreter(monitor);
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(false);
 		job.schedule();
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
 			final File interpreterDir = getInterpreterDirectory(pluginsDir);
 			final String executable = new File(interpreterDir, "jython.jar").getAbsolutePath();
 
 			logger.debug("interpreter path = {}", interpreterDir.getAbsolutePath());
 			logger.debug("executable path = {}", executable);
 
 			// check for the existence of this standard pydev script
 			final File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
 			if (!script.exists()) {
 				logger.error("The file specified does not exist: {} ", script);
 				throw new RuntimeException("The file specified does not exist: " + script);
 			}
 
 			logger.debug("Script path = {}", script.getAbsolutePath());
 
 			// gets the info for the python side
 			Tuple<String, String> outTup = new SimpleJythonRunner().runAndGetOutputWithJar(
 					REF.getFileAbsolutePath(script), executable, null, null, null, monitor);
 
 			logger.debug("outTup = {}", outTup);
 
 			// this is the main info object which contains the environment data
 			InterpreterInfo info = null;
 
 			try {
 				// HACK Otherwise Pydev shows a dialog to the user.
 				ModulesManagerWithBuild.IN_TESTS = true;
 				info = InterpreterInfo.fromString(outTup.o1, false);
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
			
 			// thirdparty/bundles jars for running in eclipse
 			String runProp = System.getProperty("run.in.eclipse");
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
 				// add ScisoftPy package
 				for (File file: gdaDirs) {
 					File b = new File(file, "scisoftpy");
 					if (b.isDirectory()) {
 						if (pyPaths.contains(file.getAbsolutePath())) {
 							logger.warn("File already there!");
 						}
 						pyPaths.add(file.getAbsolutePath());
 						logger.debug("Adding dir to library path : {} ", file.getAbsolutePath());
 						break;
 					}
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
 
 	
 	private File getInterpreterDirectory(File pluginsDir) {
 		
 		for (File file : pluginsDir.listFiles()) {
 			if(file.getName().startsWith("uk.ac.gda.libs_")) {
 				File d = new File(file, "jython2.5.1");
 				return d;
 			}
 			
 		}
 		logger.error("Could not find a folder for 'uk.ac.gda.libs' defaulting to standard");
 		return new File(pluginsDir, "uk.ac.gda.libs/jython2.5.1/");
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
 						libs.add(f);
 				}
 			}
 		}
 		
 		// get down to the git checkouts
		// only do this if we are running inside Eclipse
		
		String runProp = System.getProperty("run.in.eclipse");
		if (runProp != null && runProp.equalsIgnoreCase("true")) {
 		
			String gitpathname = directoryName.getParentFile().getAbsolutePath()+"_git";
			File git = new File(gitpathname, "scisoft");
			
			if (git.exists() && git.isDirectory()) {
			
				for (File f : git.listFiles()) {
					if (f.isDirectory()) {
						for (File plugin : f.listFiles()) {
							if (plugin.isDirectory()) {
								if (isRequired(plugin, pluginKeys))
									libs.add(plugin);
							}
						}
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
 			if(filename.startsWith(key)) return true;
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
 			logger.debug("Bundle loc: {}", f.getParent());
 			
 			String runProp = System.getProperty("run.in.eclipse");
 			if (runProp != null && runProp.equalsIgnoreCase("true")) {
 				File git = f.getParentFile().getParentFile().getParentFile();
 				File parent = git.getParentFile();
 				String projectName = git.getName().replace("_git", "");
 				File plugins = new File(parent, projectName+"/plugins");
 				
 				if(plugins.isDirectory()) {
 					logger.debug("Plugins Locaction: {}", plugins);
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
 				info = PydevPlugin.getJythonInterpreterManager().getInterpreterInfo("Jython2.5.1",
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
