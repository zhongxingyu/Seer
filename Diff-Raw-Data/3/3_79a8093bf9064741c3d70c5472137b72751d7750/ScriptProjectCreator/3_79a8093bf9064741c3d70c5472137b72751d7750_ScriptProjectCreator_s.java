 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 package uk.ac.gda.pydev;
 
 import gda.configuration.properties.LocalProperties;
 import gda.jython.JythonServerFacade;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IPerspectiveListener;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.IWorkingSetManager;
 import org.eclipse.ui.PlatformUI;
 import org.python.copiedfromeclipsesrc.JavaVmLocationFinder;
 import org.python.pydev.core.IInterpreterInfo;
 import org.python.pydev.core.IPythonNature;
 import org.python.pydev.core.MisconfigurationException;
 import org.python.pydev.core.REF;
 import org.python.pydev.core.Tuple;
 import org.python.pydev.editor.codecompletion.revisited.ModulesManagerWithBuild;
 import org.python.pydev.plugin.PydevPlugin;
 import org.python.pydev.plugin.nature.PythonNature;
 import org.python.pydev.runners.SimpleJythonRunner;
 import org.python.pydev.ui.interpreters.JythonInterpreterManager;
 import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.ClientManager;
 import uk.ac.gda.jython.PydevConstants;
 import uk.ac.gda.pydev.extension.Activator;
 import uk.ac.gda.pydev.extension.builder.ConfigurationXMLNature;
 import uk.ac.gda.pydev.extension.builder.ExtendedSyntaxNature;
 import uk.ac.gda.pydev.extension.ui.perspective.JythonPerspective;
 import uk.ac.gda.pydev.ui.preferences.PreferenceConstants;
 import uk.ac.gda.ui.utils.ProjectUtils;
 
 /**
  * Class creates a project for the scripts if it does not exist.
  */
 public class ScriptProjectCreator implements IStartup {
 
 	private static final Logger logger = LoggerFactory.getLogger(ScriptProjectCreator.class);
 	private static Map<String, IProject> pathProjectMap = new HashMap<String, IProject>();
 
 	/**
 	 * Important configuration of pydev. The other place which this is done is when the Jython interpreter is set from
 	 * the client plugin before the workbench is started.
 	 */
 	@Override
 	public void earlyStartup() {
 
 		if (!ClientManager.isClient())
 			return;
 
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				// Attempt to refresh the pydev package explorer.
 				createPerspectiveListener();
 			}
 
 		});
 	}
 
 	public static String getProjectNameXMLConfig() {
 		return getProjectName("gda.scripts.user.xml.project.name", "XML - Config");
 	}
 
 	private static String getProjectName(final String property, final String defaultValue) {
 		String projectName = LocalProperties.get(property);
 		if (projectName == null)
 			projectName = defaultValue;
 		return projectName;
 	}
 
 	static public void handleShowXMLConfig(IProgressMonitor monitor) throws CoreException {
 		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		if (store.getBoolean(PreferenceConstants.SHOW_XML_CONFIG)) {
 			ProjectUtils.createImportProjectAndFolder(getProjectNameXMLConfig(), "src",
 					LocalProperties.get(LocalProperties.GDA_CONFIG) + "/xml", ConfigurationXMLNature.ID, null, monitor);
 		} else {
 			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 			final IProject project = root.getProject(getProjectNameXMLConfig());
 			if (project.exists()) {
 				// exists so delete
 				project.delete(false, true, monitor);
 			}
 		}
 
 	}
 
 	/**
 	 * We programmatically create a Jython Interpreter so that the user does not have to.
 	 * 
 	 * @throws CoreException
 	 */
 	static void createInterpreter(IProgressMonitor monitor) throws CoreException {
 
 		if (System.getProperty("gda.client.jython.automatic.interpreter") != null)
 			return;
 		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 
 		// Horrible Hack warning: This code is copied from parts of Pydev to set up the interpreter and save it.
 		monitor.subTask("Checking if interpreter already exists");
 		if (!isInterpreter(monitor)) {
 
 			// Code copies from Pydev when the user chooses a Jython interpreter - these are the defaults.
 			final String interpreterPath = LocalProperties.getInstallationWorkspaceDir()
 					+ "plugins/uk.ac.gda.libs/jython2.5.1/";
 			final String executable = interpreterPath + "jython.jar";
 
 			final File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
 			if (!script.exists()) {
 				throw new RuntimeException("The file specified does not exist: " + script);
 			}
 			monitor.subTask("Creating interpreter");
 			// gets the info for the python side
 			Tuple<String, String> outTup = new SimpleJythonRunner().runAndGetOutputWithJar(
 					REF.getFileAbsolutePath(script), executable, null, null, null, monitor);
 
 			InterpreterInfo info = null;
 			try {
 				// HACK Otherwise Pydev shows a dialog to the user.
 				ModulesManagerWithBuild.IN_TESTS = true;
 				info = InterpreterInfo.fromString(outTup.o1, false);
 			} catch (Exception e) {
				logger.error(
						"gda.root is defined incorrectly. It should be the plugins folder not the top level of GDA.", e);
 			} finally {
 				ModulesManagerWithBuild.IN_TESTS = false;
 			}
 
 			if (info == null) {
 				// cancelled
 				return;
 			}
 			// the executable is the jar itself
 			info.executableOrJar = executable;
 
 			// we have to find the jars before we restore the compiled libs
 			if (preferenceStore.getBoolean(PreferenceConstants.GDA_PYDEV_ADD_DEFAULT_JAVA_JARS)) {
 				List<File> jars = JavaVmLocationFinder.findDefaultJavaJars();
 				for (File jar : jars) {
 					info.libs.add(REF.getFileAbsolutePath(jar));
 				}
 			}
 
 			// Defines all third party libs that can be used in scripts.
 			if (preferenceStore.getBoolean(PreferenceConstants.GDA_PYDEV_ADD_GDA_LIBS_JARS)) {
 				final List<String> gdaJars = LibsLocationFinder.findGdaLibs();
 				info.libs.addAll(gdaJars);
 			}
 
 			// Defines gda classes which can be used in scripts.
 			final String gdaInterfacePath = LibsLocationFinder.findGdaInterface();
 			if (gdaInterfacePath != null) {
 				info.libs.add(gdaInterfacePath);
 			}
 
 			List<String> allScriptProjectFolders = JythonServerFacade.getInstance().getAllScriptProjectFolders();
 			for (String s : allScriptProjectFolders) {
 				info.libs.add(s);
 			}
 
 			// java, java.lang, etc should be found now
 			info.restoreCompiledLibs(monitor);
 			info.setName(PydevConstants.INTERPRETER_NAME);
 
 			final JythonInterpreterManager man = (JythonInterpreterManager) PydevPlugin.getJythonInterpreterManager();
 			HashSet<String> set = new HashSet<String>();
 			set.add(PydevConstants.INTERPRETER_NAME);
 			man.setInfos(new IInterpreterInfo[] { info }, set, monitor);
 
 			logger.info("Jython interpreter registered: " + PydevConstants.INTERPRETER_NAME);
 		}
 	}
 
 	static public void createProjects(IProgressMonitor monitor) throws CoreException {
 		monitor.subTask("Checking existence of projects");
 		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		boolean chkGDASyntax = store.getBoolean(PreferenceConstants.CHECK_SCRIPT_SYNTAX);
 
 		if (chkGDASyntax)
 			createInterpreter(monitor);
 
 		List<IAdaptable> scriptProjects = new ArrayList<IAdaptable>();
 
 		for (String path : JythonServerFacade.getInstance().getAllScriptProjectFolders()) {
 			String projectName = JythonServerFacade.getInstance().getProjectNameForPath(path);
 			boolean shouldHideProject = shouldHideProject(path, store);
 			if (!shouldHideProject) {
 				final IProject newProject = createJythonProject(projectName, path, chkGDASyntax, monitor);
 				scriptProjects.add(newProject);
 				pathProjectMap.put(path, newProject);
 			} else {
 				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 				final IProject project = root.getProject(projectName);
 				if (project.exists()) {
 					// exists so delete rather than hide for efficiency reasons
 					try {
 						project.delete(false, true, monitor);
 					} catch (CoreException e) {
 						// TODO Auto-generated catch block
 						logger.warn("Error deleting project " + projectName, e);
 					}
 				}
 			}
 		}
 
 		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
 		IWorkingSet workingSet = workingSetManager.getWorkingSet("Scripts");
 		if (workingSet == null) {
 			monitor.subTask("Adding Scripts working set");
 			workingSetManager.addWorkingSet(workingSetManager.createWorkingSet("Scripts",
 					scriptProjects.toArray(new IAdaptable[] {})));
 		} else {
 			for (IAdaptable element : scriptProjects) {
 				workingSetManager.addToWorkingSets(element, new IWorkingSet[] { workingSet });
 			}
 		}
 	}
 
 	public static boolean shouldHideProject(String path, IPreferenceStore store) throws RuntimeException {
 		if (JythonServerFacade.getInstance().projectIsUserType(path)) {
 			return false;
 		}
 		if (JythonServerFacade.getInstance().projectIsConfigType(path)) {
 			return !store.getBoolean(PreferenceConstants.SHOW_CONFIG_SCRIPTS);
 		}
 		if (JythonServerFacade.getInstance().projectIsCoreType(path)) {
 			return !store.getBoolean(PreferenceConstants.SHOW_GDA_SCRIPTS);
 		}
 		throw new RuntimeException("Unknown type of Jython Script Project: " + path + " = "
 				+ JythonServerFacade.getInstance().getProjectNameForPath(path));
 	}
 
 	private void createPerspectiveListener() {
 		PlatformUI.getWorkbench().getWorkbenchWindows()[0].addPerspectiveListener(new IPerspectiveListener() {
 
 			@Override
 			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
 			}
 
 			@Override
 			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
 				if (perspective.getId().equals(JythonPerspective.ID)) {
 					closeRichBeanEditors(page);
 				}
 			}
 
 		});
 	}
 
 	protected void closeRichBeanEditors(final IWorkbenchPage page) {
 
 		if (!Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CLOSE_RICH_BEAN_EDITORS))
 			return;
 
 		final IEditorReference[] refs = page.getEditorReferences();
 		final List<IEditorReference> toClose = new ArrayList<IEditorReference>(3);
 		for (int i = 0; i < refs.length; i++) {
 			if (refs[i].getPartProperty("RichBeanEditorPart") != null) {
 				toClose.add(refs[i]);
 			}
 		}
 
 		if (!toClose.isEmpty())
 			page.closeEditors(toClose.toArray(new IEditorReference[toClose.size()]), false);
 	}
 
 	/**
 	 * @param projectName
 	 * @param importFolder
 	 * @param chkGDASyntax
 	 * @param monitor
 	 * @return IProject created
 	 * @throws CoreException
 	 */
 	static private IProject createJythonProject(final String projectName, final String importFolder,
 			final boolean chkGDASyntax, IProgressMonitor monitor) throws CoreException {
 
 		IProject project2 = ProjectUtils.createImportProjectAndFolder(projectName, "src", importFolder, null, null,
 				monitor);
 		ProjectUtils.addRemoveNature(project2, monitor, chkGDASyntax, ExtendedSyntaxNature.ID);
 		boolean hasPythonNature = PythonNature.getPythonNature(project2) != null;
 
 		if (chkGDASyntax) {
 			if (!hasPythonNature) {
 				// Assumes that the interpreter named PydevConstants.INTERPRETER_NAME has been created.
 				PythonNature.addNature(project2, monitor, IPythonNature.JYTHON_VERSION_2_5,
 				// NOTE Very important to start the name with a '/'
 				// or pydev creates the wrong kind of nature.
 						"/" + project2.getName() + "/src", null, PydevConstants.INTERPRETER_NAME, null);
 			}
 		} else {
 			if (hasPythonNature) {
 				// This should do the same as removing the Pydev config but neither
 				// prevent it being added when a python file is edited.
 				PythonNature.removeNature(project2, monitor);
 			}
 		}
 		return project2;
 	}
 
 	public static IProject projectForPath(String path) {
 		return pathProjectMap.get(path);
 	}
 
 	/**
 	 * The method PydevPlugin.getJythonInterpreterManager().getInterpreterInfo(...) can never return in some
 	 * circumstances because of a bug in pydev.
 	 * 
 	 * @return true if new interpreter required
 	 */
 	static boolean isInterpreter(final IProgressMonitor monitor) {
 
 		final InterpreterThread checkInterpreter = new InterpreterThread(monitor);
 		checkInterpreter.start();
 
 		int totalTimeWaited = 0;
 		while (!checkInterpreter.isFinishedChecking()) {
 			try {
 				if (totalTimeWaited > 4000) {
 					logger.error("Unable to call getInterpreterInfo() method on pydev, assuming interpreter is already created.");
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
 
 }
 
 class InterpreterThread extends Thread {
 	private static final Logger logger = LoggerFactory.getLogger(InterpreterThread.class);
 
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
 			info = PydevPlugin.getJythonInterpreterManager().getInterpreterInfo(PydevConstants.INTERPRETER_NAME,
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
