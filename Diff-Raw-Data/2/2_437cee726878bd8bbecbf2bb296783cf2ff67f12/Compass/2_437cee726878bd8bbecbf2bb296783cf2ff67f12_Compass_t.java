 package com.chrhsmt.eclipse.plugin.compass.actions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.externaltools.internal.IExternalToolConstants;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IPageListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 
 import com.chrhsmt.eclipse.plugin.compass.Activator;
 import com.chrhsmt.eclipse.plugin.compass.console.ConsoleLogger;
 import com.chrhsmt.eclipse.plugin.compass.internal.PluginLogger;
 import com.chrhsmt.eclipse.plugin.compass.process.ProcessUtils;
 
 
 /**
  * Our sample action implements workbench action delegate.
  * The action proxy will be created by the workbench and
  * shown in the UI. When the user tries to use the action,
  * this delegate will be created and execution will be 
  * delegated to it.
  * @see IWorkbenchWindowActionDelegate
  */
 @SuppressWarnings("restriction")
 public class Compass implements IWorkbenchWindowActionDelegate {
 
 	private static final String CONFIG_FILE_NAME = "config.rb";
 
 	public static final String PREF_KEY_RUBY_PATH = "RUBY_PATH";
 	public static final String PREF_KEY_GEM_PATH = "GEM_PATH";
 	public static final String PREF_KEY_COMPASS_PATH = "COMPASS_PATH";
 	public static final String PREF_KEY_OTHER_PATH = "OTHER_PATH";
 
 	private IWorkbenchWindow window;
 	private IWorkspaceRoot root;
 	
 	private List<IProject> targetProjects = new ArrayList<IProject>();
 
 	private ILaunch launch;
 
 	/**
 	 * The constructor.
 	 */
 	public Compass() {
 	}
 
 	/**
 	 * The action has been activated. The argument of the
 	 * method represents the 'real' action sitting
 	 * in the workbench UI.
 	 * @see IWorkbenchWindowActionDelegate#run
 	 */
 	public void run(IAction action) {
 
 		this.root = ResourcesPlugin.getWorkspace().getRoot();
 
 		if (action.isChecked()) {
 			// start
 			PluginLogger.log("start");
 			
 			this.targetProjects.clear();
 
 			// check config.rb
 			this.checkProjects();
 
 			if (!this.targetProjects.isEmpty()) {
 				Job job = new CompassJob("compass-watch");
 				job.setPriority(Job.LONG);
 				job.setSystem(true); // not reveal for UI.
 				job.schedule();
 			}
 
 //			try {
 //				IWorkbench workbench = 
 //					    PlatformUI.getWorkbench();
 //					WorkbenchWindow workbenchWindow = 
 //					    (WorkbenchWindow)workbench.
 //					    getActiveWorkbenchWindow();
 //					IActionBars bars = 
 //					    workbenchWindow.getActionBars();
 //					IStatusLineManager 
 //					    lineManager = bars.getStatusLineManager();
 //					IProgressMonitor monitor = 
 //					    lineManager.getProgressMonitor();
 //				ILaunchConfigurationType compasstype = manager.getLaunchConfigurationType("com.chrhsmt.eclipse.plugin.compass.CompassLaunchConfigurationType");
 //				ILaunch launch = compasstype.newInstance(null, Activator.PLUGIN_ID).launch(ILaunchManager.RUN_MODE, monitor);
 //				manager.addLaunch(launch);
 //			} catch (CoreException e2) {
 //				e2.printStackTrace();
 //			}
 //
 
 //			// start
 //			try {
 //				for (IProject project : this.targetProjects) {
 //					ConsoleLogger.output("compass", String.format("In %s", project.getName()));
 //					this.startCommand(path, project.getLocation().toOSString());
 //				}
 //			} catch (IOException | InterruptedException e) {
 //				PluginLogger.log(e.getMessage(), e);
 //				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
 //				ErrorDialog.openError(
 //						this.window.getShell(),
 //						"command 'compass' runtime error",
 //						e.getMessage(),
 //						status);
 //			}
 
 		} else {
 			// stop
 			this.stopCommand();
 		}
 	}
 
 	/**
 	 * specify target project.
 	 */
 	private void checkProjects() {
 		
 		for (IProject project : this.root.getProjects()) {
 			if (project.exists() && project.isOpen()) {
 				IFile file = project.getFile(CONFIG_FILE_NAME);
 				if (!file.exists() || !file.isAccessible()) {
 					ConsoleLogger.output("compass",
 							String.format("Project '%s' does not have config.rb or read it.", project.getName()));
 					continue;
 				} else {
 					ConsoleLogger.output("compass",
 							String.format("Project '%s' found config.rb.", project.getName()));
 					this.targetProjects.add(project);
 //					this.readProperties(file.getLocation().toOSString());
 				}
 			} else {
 				continue;
 			}
 		}
 	}
 
 	/**
 	 * stop process.
 	 */
 	private void stopCommand() {
 		if (this.launch != null) {
 			try {
 				this.launch.terminate();
 			} catch (DebugException e) {
 				Activator.getDefault().getLog().log(e.getStatus());
 			}
 		}
 		this.targetProjects.clear();
 	}
 
 	/**
 	 * Selection in the workbench has been changed. We 
 	 * can change the state of the 'real' action here
 	 * if we want, but this can only happen after 
 	 * the delegate has been created.
 	 * @see IWorkbenchWindowActionDelegate#selectionChanged
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 	}
 
 	/**
 	 * We can use this method to dispose of any system
 	 * resources we previously allocated.
 	 * @see IWorkbenchWindowActionDelegate#dispose
 	 */
 	public void dispose() {
 		PluginLogger.log("dispose");
 		this.stopCommand();
 	}
 
 	/**
 	 * We will cache window object in order to
 	 * be able to provide parent shell for the message dialog.
 	 * @see IWorkbenchWindowActionDelegate#init
 	 */
 	public void init(IWorkbenchWindow window) {
 		PluginLogger.log("init");
 		this.window = window;
 
 		// stop compass process when workbench shutdown.
 		this.window.addPageListener(new IPageListener() {
 			@Override
 			public void pageOpened(IWorkbenchPage page) {
 			}
 			
 			@Override
 			public void pageClosed(IWorkbenchPage page) {
 				stopCommand();
 			}
 			
 			@Override
 			public void pageActivated(IWorkbenchPage page) {
 			}
 		});
 	}
 
 	/**
	 * Compass Job.
 	 * @author chihiro
 	 *
 	 */
 	class CompassJob extends Job {
 
 		public CompassJob(String name) {
 			super(name);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
 		 */
 		@Override
 		public IStatus run(IProgressMonitor monitor) {
 			PluginLogger.log("========= start =========");
 			
 			try {
 				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
 				ILaunchConfiguration config = this.createConfig();
 				launch = config.launch(ILaunchManager.RUN_MODE, null, false, true);
 				
 				manager.addLaunch(launch);
 
 				while (!launch.isTerminated()) {
 					try {
 						Thread.sleep(500);
 					} catch (InterruptedException e) {
 						launch.terminate();
 						throw new OperationCanceledException();
 					}
 				}
 				PluginLogger.log("========= terminate =========");
 				launch.terminate();
 
 				PluginLogger.log("========= end =========");
 				return Status.OK_STATUS;
 			} catch (CoreException e) {
 				ErrorDialog.openError(
 						window.getShell(),
 						"command 'compass' runtime error",
 						"compass executable command does not exists. please check your path preference.",
 						e.getStatus()
 				);
 				return e.getStatus();
 			}
 		}
 
 		private ILaunchConfiguration createConfig() throws CoreException {
 
 			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
 			ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
 			String location = ProcessUtils.getEnvCommand();
 
 			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, Activator.PLUGIN_ID);
 			workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
 			workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, getArguments());
 			workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<>());
 			workingCopy.setAttribute(IExternalToolConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
 			return workingCopy;
 		}
 
 		private String getArguments() {
 			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 			String pathPhrase = ProcessUtils.buildEnviromentPathPhrase(
 					store.getString(PREF_KEY_RUBY_PATH) != null ? store.getString(PREF_KEY_RUBY_PATH) : null,
 					store.getString(PREF_KEY_GEM_PATH) != null ? store.getString(PREF_KEY_GEM_PATH) : null,
 					store.getString(PREF_KEY_OTHER_PATH) != null ? store.getString(PREF_KEY_OTHER_PATH) : null);
 			StringBuilder sb = new StringBuilder()
 			.append(pathPhrase)
 			.append("; ")
 			.append(ProcessUtils.buildExecuteCommandPhrase(targetProjects));
 			return sb.toString();
 		}
 	}
 }
