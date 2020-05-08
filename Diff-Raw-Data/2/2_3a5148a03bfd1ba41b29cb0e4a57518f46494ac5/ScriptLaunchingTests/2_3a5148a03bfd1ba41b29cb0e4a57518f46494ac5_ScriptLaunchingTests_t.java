 package org.eclipse.dltk.core.tests.launching;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IDebugEventSetListener;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchDelegate;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IStreamMonitor;
 import org.eclipse.debug.core.model.IStreamsProxy;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.internal.debug.core.model.ScriptLineBreakpoint;
 import org.eclipse.dltk.internal.launching.InterpreterDefinitionsContainer;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.launching.ScriptRuntime;
 
 public abstract class ScriptLaunchingTests extends AbstractModelTests {
 
 	public static class MyInterpretersUpdater {
 
 		private InterpreterDefinitionsContainer fOriginalInterpreters;
 
 		private void saveCurrentAsOriginal() {
 			fOriginalInterpreters = new InterpreterDefinitionsContainer();
 
 			final String[] natureIds = ScriptRuntime.getInterpreterNatures();
 			for (int i = 0; i < natureIds.length; i++) {
 				final String natureId = natureIds[i];
 
 				IInterpreterInstall def = ScriptRuntime
 						.getDefaultInterpreterInstall(natureId);
 
 				if (def != null) {
 					fOriginalInterpreters
 							.setDefaultInterpreterInstallCompositeID(natureId,
 									ScriptRuntime
 											.getCompositeIdFromInterpreter(def));
 				}
 			}
 
 			final IInterpreterInstallType[] types = ScriptRuntime
 					.getInterpreterInstallTypes();
 			for (int i = 0; i < types.length; i++) {
 				IInterpreterInstall[] installs = types[i]
 						.getInterpreterInstalls();
 				if (installs != null) {
 					for (int j = 0; j < installs.length; j++) {
 						fOriginalInterpreters.addInterpreter(installs[j]);
 					}
 				}
 			}
 		}
 
 		private void saveInterpreterDefinitions(
 				final InterpreterDefinitionsContainer container) {
 			try {
 				final String xml = container.getAsXML();
 				ScriptRuntime.getPreferences().setValue(
 						ScriptRuntime.PREF_INTERPRETER_XML, xml);
 				ScriptRuntime.savePreferences();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (TransformerException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public MyInterpretersUpdater() {
 			saveCurrentAsOriginal();
 		}
 
 		public boolean updateInterpreterSettings(String langNatureId,
 				IInterpreterInstall[] interpreters,
 				IInterpreterInstall defaultInterpreter) {
 			// Create a Interpreter definition container
 			InterpreterDefinitionsContainer container = new InterpreterDefinitionsContainer();
 
 			// Default interpreter id for natureId
 			if (defaultInterpreter != null) {
 				final String defaultId = ScriptRuntime
 						.getCompositeIdFromInterpreter(defaultInterpreter);
 				container.setDefaultInterpreterInstallCompositeID(langNatureId,
 						defaultId);
 			} else {
 				container.setDefaultInterpreterInstallCompositeID(langNatureId,
 						null);
 			}
 
 			// Interpreters for natureId
 			for (int i = 0; i < interpreters.length; i++) {
 				container.addInterpreter(interpreters[i]);
 			}
 
 			// Default interpreters for other languages
 			final String[] natureIds = fOriginalInterpreters
 					.getInterpreterNatures();
 			for (int i = 0; i < natureIds.length; i++) {
 				final String natureId = natureIds[i];
 				if (!langNatureId.equals(natureId)) {
 					final String defaultId = fOriginalInterpreters
 							.getDefaultInterpreterInstallCompositeID(natureId);
 					container.setDefaultInterpreterInstallCompositeID(natureId,
 							defaultId);
 				}
 			}
 
 			// Save interpreters from other languages to the container
 			final Iterator it = fOriginalInterpreters.getInterpreterList()
 					.iterator();
 			while (it.hasNext()) {
 				final IInterpreterInstall install = (IInterpreterInstall) it
 						.next();
 				if (!langNatureId.equals(install.getInterpreterInstallType()
 						.getNatureId())) {
 					container.addInterpreter(install);
 				}
 			}
 
 			saveInterpreterDefinitions(container);
 
 			saveCurrentAsOriginal();
 
 			return true;
 		}
 	}
 
 	protected IScriptProject scriptProject;
 
 	protected IInterpreterInstall[] interpreterInstalls;
 
 	public ScriptLaunchingTests(String testProjectName, String name) {
 		super(testProjectName, name);
 	}
 
 	// Configuration
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
 		scriptProject = setUpScriptProject(getProjectName());
 
 		final IProject project = scriptProject.getProject();
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] { getNatureId() });
 		project.setDescription(description, null);
 
 		interpreterInstalls = searchInstalls(getNatureId());
 	}
 
 	public void tearDownSuite() throws Exception {
 		deleteProject(getProjectName());
 		super.tearDownSuite();
 	}
 
 	// Helper methods
 	protected ILaunchConfiguration createTestLaunchConfiguration(
 			final String natureId, final String projectName,
 			final String script, final String arguments) {
 		return new ILaunchConfiguration() {
 			public boolean contentsEqual(ILaunchConfiguration configuration) {
 				return false;
 			}
 
 			public ILaunchConfigurationWorkingCopy copy(String name)
 					throws CoreException {
 				return null;
 			}
 
 			public void delete() throws CoreException {
 
 			}
 
 			public boolean exists() {
 				return false;
 			}
 
 			public boolean getAttribute(String attributeName,
 					boolean defaultValue) throws CoreException {
 				if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_DEFAULT_BUILDPATH)) {
 					return true;
 				}
 
 				return false;
 			}
 
 			public int getAttribute(String attributeName, int defaultValue)
 					throws CoreException {
 				return 0;
 			}
 
 			public List getAttribute(String attributeName, List defaultValue)
 					throws CoreException {
 				return null;
 			}
 
 			public Set getAttribute(String attributeName, Set defaultValue)
 					throws CoreException {
 				return null;
 			}
 
 			public Map getAttribute(String attributeName, Map defaultValue)
 					throws CoreException {
 				return null;
 			}
 
 			public String getAttribute(String attributeName, String defaultValue)
 					throws CoreException {
 
 				if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME)) {
 					return script;
 				} else if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME)) {
 					return projectName;
 				} else if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY)) {
 					return null;
 				} else if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_SCRIPT_ARGUMENTS)) {
 					return arguments;
 				} else if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_ARGUMENTS)) {
 					return "";
 				} else if (attributeName
 						.equals(ScriptLaunchConfigurationConstants.ATTR_SCRIPT_NATURE)) {
 					return natureId;
 				}
 
 				return null;
 			}
 
 			public Map getAttributes() throws CoreException {
 				return null;
 			}
 
 			public String getCategory() throws CoreException {
 				return null;
 			}
 
 			public IFile getFile() {
 				return null;
 			}
 
 			public IPath getLocation() {
 				return null;
 			}
 
 			public IResource[] getMappedResources() throws CoreException {
 				return null;
 			}
 
 			public String getMemento() throws CoreException {
 				return null;
 			}
 
 			public Set getModes() throws CoreException {
 				return null;
 			}
 
 			public String getName() {
 				return null;
 			}
 
 			public ILaunchDelegate getPreferredDelegate(Set modes)
 					throws CoreException {
 				return null;
 			}
 
 			public ILaunchConfigurationType getType() throws CoreException {
 				return null;
 			}
 
 			public ILaunchConfigurationWorkingCopy getWorkingCopy()
 					throws CoreException {
 				return null;
 			}
 
 			public boolean isLocal() {
 				return false;
 			}
 
 			public boolean isMigrationCandidate() throws CoreException {
 				return false;
 			}
 
 			public boolean isReadOnly() {
 				return false;
 			}
 
 			public boolean isWorkingCopy() {
 				return false;
 			}
 
 			public ILaunch launch(String mode, IProgressMonitor monitor)
 					throws CoreException {
 				return null;
 			}
 
 			public ILaunch launch(String mode, IProgressMonitor monitor,
 					boolean build) throws CoreException {
 				return null;
 			}
 
 			public ILaunch launch(String mode, IProgressMonitor monitor,
 					boolean build, boolean register) throws CoreException {
 				return null;
 			}
 
 			public void migrate() throws CoreException {
 
 			}
 
 			public boolean supportsMode(String mode) throws CoreException {
 				return false;
 			}
 
 			public Object getAdapter(Class adapter) {
 				return null;
 			}
 		};
 	}
 
 	public IInterpreterInstall[] searchInstalls(String natureId) {
 		final List installs = new ArrayList();
 		final InterpreterSearcher searcher = new InterpreterSearcher();
 
 		searcher.search(natureId, null, 1, null);
 
 		if (searcher.hasResults()) {
 			File[] files = searcher.getFoundFiles();
 			IInterpreterInstallType[] types = searcher.getFoundInstallTypes();
 
 			for (int i = 0; i < files.length; ++i) {
 				final File file = files[i];
 				final IInterpreterInstallType type = types[i];
 
 				final IInterpreterInstall install = type
 						.createInterpreterInstall(getNatureId() + "_"
 								+ Integer.toString(i));
 				install.setName(file.toString());
 				install.setInstallLocation(file);
 				install.setLibraryLocations(null);
 
 				installs.add(install);
 			}
 		}
 
 		return (IInterpreterInstall[]) installs
 				.toArray(new IInterpreterInstall[installs.size()]);
 	}
 
 	public void testRun() throws Exception {
 		if (interpreterInstalls.length == 0) {
 			fail("No interperters found for nature " + getNatureId());
 		}
 
 		for (int i = 0; i < interpreterInstalls.length; ++i) {
 			final IInterpreterInstall install = interpreterInstalls[i];
 			System.out.println("Interpreter install location (run): "
 					+ install.getInstallLocation().toString());
 
 			MyInterpretersUpdater updater = new MyInterpretersUpdater();
 			updater.updateInterpreterSettings(getNatureId(),
 					interpreterInstalls, install);
 
 			final long time = System.currentTimeMillis();
 			final String stdoutTest = Long.toString(time) + "_stdout";
 			final String stderrTest = Long.toString(time) + "_stderr";
 
 			final ILaunchConfiguration configuration = createLaunchConfiguration(stdoutTest
 					+ " " + stderrTest);
 
 			final ILaunch launch = new Launch(configuration,
 					ILaunchManager.RUN_MODE, null);
 
 			startLaunch(launch);
 
 			IProcess[] processes = launch.getProcesses();
 			assertEquals(1, processes.length);
 
 			final IProcess process = processes[0];
 			final IStreamsProxy proxy = process.getStreamsProxy();
 			assertNotNull(proxy);
 
 			final IStreamMonitor outputMonitor = proxy.getOutputStreamMonitor();
 			assertNotNull(outputMonitor);
 
 			final IStreamMonitor errorMonitor = proxy.getErrorStreamMonitor();
 			assertNotNull(errorMonitor);
 
 			while (!process.isTerminated()) {
 				Thread.sleep(200);
 			}
 
 			assertTrue(process.isTerminated());
 
 			final int exitValue = process.getExitValue();
 			assertEquals(0, exitValue);
 
 			final String output = outputMonitor.getContents();
 			assertEquals(stdoutTest, output);
 
 			final String error = errorMonitor.getContents();
 			assertEquals(stderrTest, error);
 		}
 	}
 
 	private static class DebugEventStats implements IDebugEventSetListener {
 		private int suspendCount;
 		private int resumeCount;
 
 		public DebugEventStats() {
 			this.suspendCount = 0;
 			this.resumeCount = 0;
 		}
 
 		public void handleDebugEvents(DebugEvent[] events) {
 			for (int i = 0; i < events.length; ++i) {
 				DebugEvent event = events[i];
 
 				final int kind = event.getKind();
 				switch (kind) {
 				case DebugEvent.RESUME:
 					resumeCount += 1;
 					break;
 				case DebugEvent.SUSPEND:
 					suspendCount += 1;
 					try {
 						final Object source = event.getSource();
 						if (source instanceof IScriptStackFrame) {
 							((IScriptStackFrame) source).resume();
 						} else if (source instanceof IScriptThread) {
 							((IScriptThread) source).resume();
 						}
 					} catch (DebugException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 					break;
 				case DebugEvent.CREATE:
 					break;
 				case DebugEvent.TERMINATE:
 					break;
 				case DebugEvent.CHANGE:
 					break;
 				}
 			}
 		}
 
 		public void reset() {
 			suspendCount = 0;
 			resumeCount = 0;
 		}
 
 		public int getSuspendCount() {
 			return suspendCount;
 		}
 
 		public int getResumeCount() {
 			return resumeCount;
 		}
 	}
 
 	public void testDebug() throws Exception {
 		if (interpreterInstalls.length == 0) {
 			fail("No interperters found for nature " + getNatureId());
 		}
 
 		// Debug
 		final IFile file = getFile("/launching/src/test.rb");
 
 		// Setting breakpoint
 		IScriptLineBreakpoint b = new ScriptLineBreakpoint(getDebugModelId(),
				file, 1, -1, -1, 0, true);
 
 		DebugEventStats stats = new DebugEventStats();
 
 		DebugPlugin.getDefault().addDebugEventListener(stats);
 
 		for (int i = 0; i < interpreterInstalls.length; ++i) {
 			final IInterpreterInstall install = interpreterInstalls[i];
 
 			System.out.println("Interperter install location (debug): "
 					+ install.getInstallLocation());
 
 			final MyInterpretersUpdater updater = new MyInterpretersUpdater();
 			updater.updateInterpreterSettings(getNatureId(),
 					interpreterInstalls, install);
 
 			stats.reset();
 
 			final ILaunch launch = new Launch(createLaunchConfiguration(""),
 					ILaunchManager.DEBUG_MODE, null);
 
 			final long time = System.currentTimeMillis();
 
 			startLaunch(launch);
 
 			IProcess[] processes = launch.getProcesses();
 			assertEquals(1, processes.length);
 
 			final IProcess process = processes[0];
 			final IStreamsProxy proxy = process.getStreamsProxy();
 			// assertNotNull(proxy);
 
 			while (!process.isTerminated()) {
 				Thread.sleep(200);
 			}
 
 			assertTrue(process.isTerminated());
 
 			int suspendCount = stats.getSuspendCount();
 			assertEquals(1, suspendCount);
 
 			int resumeCount = stats.getResumeCount();
 
 			final int exitValue = process.getExitValue();
 			assertEquals(0, exitValue);
 		}
 	}
 
 	protected abstract String getProjectName();
 
 	protected abstract String getNatureId();
 
 	protected abstract String getDebugModelId();
 
 	protected abstract void startLaunch(ILaunch launch) throws CoreException;
 
 	protected abstract ILaunchConfiguration createLaunchConfiguration(
 			String arguments);
 }
