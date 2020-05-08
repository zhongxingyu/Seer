 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.launching;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.IProjectFragment;
 import org.eclipse.dltk.mod.core.IScriptProject;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.launching.AbstractInterpreterRunner;
 import org.eclipse.dltk.mod.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.mod.launching.IInterpreterInstall;
 import org.eclipse.dltk.mod.launching.IInterpreterRunner;
 import org.eclipse.dltk.mod.launching.InterpreterConfig;
 import org.eclipse.dltk.mod.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.mod.launching.debug.DbgpConstants;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;

 import org.eclipse.vjet.dsf.jsrunner.JsRunner;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.eclipse.launching.VjetLaunchingPlugin;
 import org.eclipse.vjet.vjo.tool.codecompletion.StringUtils;
 
 /**
  * Vjet implementation of a interpreter runner.
  * 
  * @see IInterpreterRunner
  * 
  */
 public class VjetInterpreterRunner extends AbstractInterpreterRunner {
 
 	private static final boolean	DEBUG_LAUNCH	= "true"
 															.equalsIgnoreCase(Platform
																	.getDebugOption("org.eclipse.vjet.eclipse/debug/launch"));
 
 	private final String			m_mode;
 
 	public VjetInterpreterRunner(IInterpreterInstall install, String mode) {
 		super(install);
 		m_mode = mode;
 	}
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		doRunImpl(config, launch);
 	}
 
 	/**
 	 * Launches new jvm instance and runs within it interpreter.
 	 * 
 	 * @param config
 	 *            launch configuration
 	 * @param launch
 	 *            the result of launching a debug session.
 	 * @throws CoreException
 	 *             if an error occurred when running interpreter
 	 * @see {@link ILaunch}
 	 * @see {@link }
 	 */
 	public void doRunImpl(InterpreterConfig config, ILaunch launch)
 			throws CoreException {
 
 		IScriptProject proj = AbstractScriptLaunchConfigurationDelegate
 				.getScriptProject(launch.getLaunchConfiguration());
 
 		// do not cross boundary to java sdk - looks like we are tied to Java
 		// IDE here
 		IJavaProject myJavaProject = JavaCore.create(proj.getProject());
 
 		IVMInstall vmInstall = myJavaProject.exists() ? JavaRuntime
 				.getVMInstall(myJavaProject) : JavaRuntime
 				.getDefaultVMInstall();
 
 		if (vmInstall == null) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					VjetPlugin.PLUGIN_ID, IStatus.ERROR, "No VMInstall", null));
 		}
 		IVMRunner vmRunner = vmInstall.getVMRunner(m_mode);
 		if (vmRunner == null) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					VjetPlugin.PLUGIN_ID, IStatus.ERROR, "No VMRunner", null));
 		}
 
 		IWorkspaceRoot workspaceRoot = proj.getProject().getWorkspace()
 				.getRoot();
 
 		// prepare js source search path
 		String sourceSearchPath = null;
 		// sourceSearchPath = prepareSourceSearchPath(proj, myJavaProject,
 		// workspaceRoot, sourceSearchPath);
 
 		// prepare java runtime class path
 		String[] clzPath = VjoRunnerInfo.getClassPath();
 
 		if (DEBUG_LAUNCH) {
 			for (String path : clzPath) {
 				System.out.println(path);
 			}
 		}
 		String[] jarPaths = null;
 		if (myJavaProject.exists()) {
 			// add this project's dependent jars (include those from all
 			// dependent projects)
 			// so the dependented javascript can be loaded from those jar files
 			jarPaths = getJarPaths(myJavaProject);
 
 		} else {
 			jarPaths = getJarPaths(proj);
 		}
 		if (jarPaths.length > 0) {
 			String[] newClzPath = new String[clzPath.length + jarPaths.length];
 			System.arraycopy(clzPath, 0, newClzPath, 0, clzPath.length);
 			System.arraycopy(jarPaths, 0, newClzPath, clzPath.length,
 					jarPaths.length);
 			clzPath = newClzPath;
 		}
 		VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
 				VjoRunnerInfo.getClassName(), clzPath);
 
 		IPath scriptFilePath = config.getScriptFilePath();
 		if (scriptFilePath == null) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					VjetPlugin.PLUGIN_ID, IStatus.ERROR,
 					"Script File name is not specified...", null));
 		} else if (!proj.exists() && !myJavaProject.exists()) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					VjetPlugin.PLUGIN_ID, IStatus.ERROR,
 					"Must run js from a VJET or JAVA project!", null));
 		}
 
 		// find vjo class
 		String jsFile = scriptFilePath.toFile().getAbsolutePath();
 		String vjoClz = "";
 		if (!jsFile.endsWith(".js")) {
 			// do nothing
 		} else {
 			// get vjo class name
 			String sourceRoot = null;
 			if (proj.exists()) {// get vjo class name from script project
 				IModelElement[] children = proj.getChildren();
 
 				for (IModelElement elem : children) {
 					if (elem instanceof IProjectFragment) {
 						IProjectFragment melem = (IProjectFragment) elem;
 						if (!melem.isReadOnly()) {
 							String absolutePath = proj.getProject()
 									.getLocation().toFile().getAbsolutePath();
 							String srcDir = absolutePath.substring(0,
 									absolutePath.indexOf(proj.getProject()
 											.getName()))
 									+ melem.getPath();
							srcDir = srcDir.replace("/", "\\");
							srcDir = srcDir.replace("\\\\", "\\");
 							if (jsFile.startsWith(srcDir)) {
 								sourceRoot = srcDir;
 								break;
 							}
 						}
 					}
 				}
 			}
 			if (StringUtils.isBlankOrEmpty(sourceRoot)
 					&& myJavaProject.exists()) {// get vjo class name from java
 				// project
 				IClasspathEntry[] classpathEntries = myJavaProject
 						.getRawClasspath();
 				for (IClasspathEntry entry : classpathEntries) {
 					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 						String path = getSourceFile(entry, workspaceRoot)
 								.getAbsolutePath();
 						if (jsFile.startsWith(path)) {
 							sourceRoot = path;
 							break;
 						}
 					}
 				}
 			}
 
 			if (sourceRoot != null) {
 				String vjoFile = jsFile.substring(sourceRoot.length() + 1);
 				int suffixIndex = vjoFile.indexOf(".");
 				if (suffixIndex > 0) {
 					vjoFile = vjoFile.substring(0, suffixIndex);
 				}
 				vjoClz = vjoFile.replace("/", ".");
 				vjoClz = vjoClz.replace("\\", ".");
 			}
 		}
 
 		ILaunchConfiguration launchConf = launch.getLaunchConfiguration();
 
 		String args = launchConf.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_SCRIPT_ARGUMENTS, "")
 				.trim();
 		Map env = launchConf.getAttribute(
 				ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) new HashMap());
 		
 		List<String> loadJSList = launchConf.getAttribute(org.eclipse.vjet.eclipse.launching.ILaunchConstants.ATTR_INCLUDE_PATH, new ArrayList<String>());
 		if(loadJSList.size()>0){
 			StringBuilder loadJSStr = new StringBuilder();
 			for(String js: loadJSList){
 				
 				//int kind = Integer.parseInt(js.substring(0, 1));
 				String path = js.substring(1);
 				
 				loadJSStr.append(path).append(JsRunner.LIST_SEPERATOR);
 			}
 			
 			env.put(JsRunner.LOAD_JS_KEY, loadJSStr.toString());
 		}
 		
 		List<String> pArgs = new ArrayList<String>();
 		if (vjoClz == "" && jsFile.indexOf("htm") != -1) {
 			env.put("type", "html");
 		}
 		addEnvOptions(env, pArgs); // add env options
 
 		pArgs.add(scriptFilePath.toFile().getAbsolutePath());
 		pArgs.add(vjoClz);
		// pArgs.add(findMainFunc(vjoClz, proj.getProject().getName()));
 
 		// add script arguments as line
 		pArgs.add(new ArgsNormalizer(args).normalize());
 
 		vmConfig.setProgramArguments(pArgs.toArray(new String[] {}));
 
 		List<String> vmArgs = new ArrayList<String>(4);
 		if (sourceSearchPath != null) {
 			vmArgs.add("-Djava.source.path=" + sourceSearchPath);
 		}
 
 		IFileStore s = org.eclipse.core.filesystem.EFS.getStore(proj
 				.getProject().getProject().getLocationURI());
 		File f = s.toLocalFile(0, null);
 
 		if (m_mode.equals(ILaunchManager.DEBUG_MODE)) {
 			vmArgs.add("-DVJETDebugHost="
 					+ config.getProperty(DbgpConstants.HOST_PROP));
 			vmArgs.add("-DVJETDebugPort="
 					+ config.getProperty(DbgpConstants.PORT_PROP));
 			vmArgs.add("-DVJETDebugSessionID="
 					+ config.getProperty(DbgpConstants.SESSION_ID_PROP));
 			vmArgs.add("-DVJETProjectPath=" + f.getAbsolutePath());
 		}
 
 		// org.eclipse.jdt.launching.PROJECT_ATTR
 		String javaprojectprop = "org.eclipse.jdt.launching.PROJECT_ATTR";
 		String projectName = myJavaProject.getProject().getName();
 
 		ILaunchConfigurationWorkingCopy copy = launch.getLaunchConfiguration()
 				.getWorkingCopy();
 
 		copy.setAttribute(javaprojectprop, projectName);
 		copy.doSave();
 
 		setupProxySrcLocator(launch, copy);
 
 		vmConfig.setVMArguments(vmArgs.toArray(new String[vmArgs.size()]));
 
 		// ILaunch launchr = new Launch(launch.ge, m_mode, null);
 		//		
 
 		// ISourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
 		// sourceLocator
 		// .setSourcePathComputer(DebugPlugin.getDefault().getLaunchManager()
 		// .getSourcePathComputer(
 		//							"org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer")); //$NON-NLS-1$
 		// sourceLocator.initializeDefaults(launchConf);
 		//	
 		// launch.setSourceLocator(sourceLocator);
 
 		vmRunner.run(vmConfig, launch, null);
 		IDebugTarget[] debugTargets = launch.getDebugTargets();
 		if (debugTargets.length > 0) {
 			VjetLaunchingPlugin.getDefault().getLog().log(
 					new Status(IStatus.INFO, VjetPlugin.PLUGIN_ID,
 							IStatus.INFO, "!USAGE_TRACKING: NAME="
 									+ VjetPlugin.PLUGIN_ID
 									+ ".debug; ACCESS_TIME="
 									+ new Date().toString() + ";", null));
 		} else {
 			VjetLaunchingPlugin.getDefault().getLog().log(
 					new Status(IStatus.INFO, VjetPlugin.PLUGIN_ID,
 							IStatus.INFO, "!USAGE_TRACKING: NAME="
 									+ VjetPlugin.PLUGIN_ID
 									+ ".run; ACCESS_TIME="
 									+ new Date().toString() + ";", null));
 
 		}
 		// for (int a = 0; a < debugTargets.length; a++) {
 		//
 		// launch.addDebugTarget(debugTargets[a]);
 		// }
 		//		
 		// IProcess[] processes = launch.getProcesses();
 		// for (int a = 0; a < processes.length; a++) {
 		// launch.addProcess(processes[a]);
 		// }
 	}
 
 	private String[] getJarPaths(IScriptProject scriptProject) {
 		return LauncherUtil.getOptimizedBuildpath(scriptProject);
 	}
 
 	private void setupProxySrcLocator(ILaunch launch,
 			ILaunchConfiguration config) {
 		ISourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
 		sourceLocator
 				.setSourcePathComputer(DebugPlugin
 						.getDefault()
 						.getLaunchManager()
 						.getSourcePathComputer(
 								"org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer")); //$NON-NLS-1$
 		try {
 			sourceLocator.initializeDefaults(config);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		launch.setSourceLocator(new SourceLocatorProxy(sourceLocator, launch
 				.getSourceLocator()));
 	}
 
 	private String prepareSourceSearchPath(IScriptProject proj,
 			IJavaProject myJavaProject, IWorkspaceRoot workspaceRoot,
 			String sourceSearchPath) throws ModelException, JavaModelException {
 		if (!myJavaProject.exists()) {
 			sourceSearchPath = getSourceSearchPath(proj);
 
 		} else {
 			sourceSearchPath = getSourceSearchPath(myJavaProject, workspaceRoot);
 		}
 		return sourceSearchPath;
 	}
 
 	private static class ArgsNormalizer {
 
 		private static Pattern	quotedPattern	= Pattern
 														.compile("^(\'|\")[\\w\\p{Punct}\\s]+(\'|\")");
 		private static Pattern	numberPattern	= Pattern
 														.compile("^[-+]?[0-9]*\\.?[0-9]+(\\s|$)");
 		private static Pattern	wordPattern		= Pattern
 														.compile("^[\\w\\p{Punct}]+(\\s|$)");
 
 		private String			rawArgs;
 		private boolean			first			= true;
 
 		private ArgsNormalizer(String rawArgs) {
 			this.rawArgs = rawArgs;
 		}
 
 		String normalize() {
 
 			StringBuffer buf = new StringBuffer();
 			// String arg = rawArgs.replace("\"", "'").trim();
 			String arg = rawArgs.trim();
 
 			while (arg.length() > 0) {
 
 				Matcher quotedArgMatcher = quotedPattern.matcher(arg);
 				Matcher numberMatcher = numberPattern.matcher(arg);
 				Matcher wordMatcher = wordPattern.matcher(arg);
 
 				if (quotedArgMatcher.find()) {
 					arg = match(arg, quotedArgMatcher.group(), buf, false);
 				} else if (numberMatcher.find()) {
 					arg = match(arg, numberMatcher.group().trim(), buf, false);
 				} else if (wordMatcher.find()) {
 					arg = match(arg, wordMatcher.group().trim(), buf, true);
 				} else {
 					// Unrecognized value detected
 					// TODO Log this error
 					// WHAT SHOULD WE DO IN THIS CASE?
 					return "";
 				}
 
 			}
 
 			return buf.toString();
 		}
 
 		private String match(String arg, String group, StringBuffer buf,
 				boolean needToQuoted) {
 
 			if (!first) {
 				buf.append(" ");
 			}
 
 			first = false;
 
 			if (needToQuoted)
 				buf.append("\"");
 			buf.append(group);
 			if (needToQuoted)
 				buf.append("\"");
 
 			return arg.substring(group.length()).trim();
 		}
 	}
 
 	private void addEnvOptions(Map<?, ?> options, List<String> pArgs) {
 		if (options != null) {
 			for (Object optionKey : options.keySet()) {
 				String key = (String) optionKey;
 				String value = (String) options.get(optionKey);
 				pArgs.add("-V" + key + "=" + value);
 				if ((key.equals(JsRunner.BROWSER_DISPLAY_KEY) && "true".equalsIgnoreCase(value)) ||
 					(key.equals(JsRunner.DAP_MODE_KEY) && "W".equalsIgnoreCase(value))){
 					BrowserService s = BrowserService.getInstance();
 					pArgs.add("-V" + JsRunner.BROWSER_SERVICE_URL_KEY + "=http://localhost:" + s.getPort());					
 				}				
 			}
 		}
 	}
 
 	private static final String	PathSeparator	= System
 														.getProperty("path.separator");
 
 	/**
 	 * Get all the source search paths from script project
 	 * 
 	 * @param proj
 	 * @return
 	 * @throws ModelException
 	 */
 	private static String getSourceSearchPath(IScriptProject proj)
 			throws ModelException {
 		IModelElement[] children = proj.getChildren();
 		StringBuilder sb = new StringBuilder();
 
 		for (IModelElement elem : children) {
 			if (elem instanceof IProjectFragment) {
 				IProjectFragment melem = (IProjectFragment) elem;
 				if (!melem.isReadOnly()) {
 					String absolutePath = proj.getProject().getLocation()
 							.toFile().getAbsolutePath();
 					String srcDir = absolutePath.substring(0, absolutePath
 							.indexOf(proj.getProject().getName()))
 							+ melem.getPath();
 					srcDir = srcDir.replace("/", "\\");
 					srcDir = srcDir.replace("\\\\", "\\");
 
 					sb.append(srcDir);
 					sb.append(PathSeparator);
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Get all the source search paths from java project
 	 * 
 	 * @param javaProject
 	 * @param workspaceRoot
 	 * @return
 	 * @throws JavaModelException
 	 */
 	private static String getSourceSearchPath(IJavaProject javaProject,
 			IWorkspaceRoot workspaceRoot) throws JavaModelException {
 		Map<String, IJavaProject> transitiveClosureProjectList = new LinkedHashMap<String, IJavaProject>();
 		LauncherUtil.getTransitiveClosureProjectDependnecyList(javaProject,
 				transitiveClosureProjectList);
 		List<File> sourcePaths = new ArrayList<File>();
 		getProjectSourcePaths(sourcePaths, workspaceRoot, javaProject
 				.getResolvedClasspath(true));
 		for (IJavaProject project : transitiveClosureProjectList.values()) {
 			getProjectSourcePaths(sourcePaths, workspaceRoot, project
 					.getResolvedClasspath(true));
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append(javaProject.getPath().toFile().getAbsolutePath());
 		for (File file : sourcePaths) {
 			sb.append(PathSeparator).append(file.getAbsolutePath());
 		}
 		return sb.toString();
 	}
 
 	private static void getProjectSourcePaths(List<File> sourcePaths,
 			IWorkspaceRoot workspaceRoot, IClasspathEntry[] classPathEntries) {
 		if (classPathEntries != null) {
 			for (IClasspathEntry classPathEntry : classPathEntries) {
 				if (classPathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					sourcePaths
 							.add(getSourceFile(classPathEntry, workspaceRoot));
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * IClasspathEntry's EntryKind IClasspathEntry.CPE_SOURCE
 	 */
 	private static File getSourceFile(IClasspathEntry classPathEntry,
 			IWorkspaceRoot workspaceRoot) {
 		if (classPathEntry.getPath().segmentCount() == 1) {
 			IProject project = workspaceRoot.getProject(classPathEntry
 					.getPath().lastSegment());
 			return project.getLocation().toFile();
 		} else {
 			IFile sourceIFile = workspaceRoot.getFile(classPathEntry.getPath());
 			return sourceIFile.getLocation().toFile();
 		}
 	}
 
 	private static String[] getJarPaths(IJavaProject javaProject)
 			throws JavaModelException {
 		return LauncherUtil.getOptimizedClasspath(javaProject);
 	}
 
 }
