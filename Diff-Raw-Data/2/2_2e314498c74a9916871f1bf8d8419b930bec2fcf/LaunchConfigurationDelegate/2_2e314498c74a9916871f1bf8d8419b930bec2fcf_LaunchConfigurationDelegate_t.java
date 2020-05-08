 package org.nodeclipse.debug.launch;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.chromium.debug.core.ChromiumDebugPlugin;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
 import org.eclipse.debug.core.model.RuntimeProcess;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.nodeclipse.debug.util.Constants;
 import org.nodeclipse.debug.util.NodeDebugUtil;
 import org.nodeclipse.ui.Activator;
 import org.nodeclipse.ui.preferences.PreferenceConstants;
 
 public class LaunchConfigurationDelegate implements
 		ILaunchConfigurationDelegate {
 	private static RuntimeProcess nodeProcess = null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.
 	 * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
 	 * org.eclipse.debug.core.ILaunch,
 	 * org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		if(nodeProcess != null && !nodeProcess.isTerminated()) {
 			throw new CoreException(new Status(IStatus.OK, ChromiumDebugPlugin.PLUGIN_ID, null, null));
 		}
 		
 		// Using configuration to build command line
 		
 		String nodePath = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.NODE_PATH);
 		
 		// Check if the node location is correctly configured
 		File nodeFile = new File(nodePath);
 		if(!nodeFile.exists()){
 			// If the location is not valid than show a dialog which prompts the user to goto the preferences page
 			showPreferencesDialog();
 			return;
 		}
 		
 		List<String> cmdLine = new ArrayList<String>();
 		// Application path should be stored in preference.
 		cmdLine.add(nodePath);
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			cmdLine.add("--debug-brk=5858");
 		}
 		
 		String nodeArgs = configuration.getAttribute(Constants.ATTR_NODE_ARGUMENTS, "");
 		if(!nodeArgs.equals("")) {
 			String[] sa = nodeArgs.split(" ");
 			for(String s : sa) {
 				cmdLine.add(s);
 			}
 		}
 		
 		String file = 
 				configuration.getAttribute(Constants.KEY_FILE_PATH,	Constants.BLANK_STRING);
 		String filePath = 
 				ResourcesPlugin.getWorkspace().getRoot().findMember(file).getLocation().toOSString();
 		// path is relative, so can not found it.
 		cmdLine.add(filePath);
 
 		String programArgs = configuration.getAttribute(Constants.ATTR_PROGRAM_ARGUMENTS, "");
 		if(!programArgs.equals("")) {
 			String[] sa = programArgs.split(" ");
 			for(String s : sa) {
 				cmdLine.add(s);
 			}
 		}
 		
 		String[] cmds = {};
 		cmds = cmdLine.toArray(cmds);
 		// Launch a process to debug.eg,
 		Process p = DebugPlugin
 				.exec(cmds, (new File(filePath)).getParentFile());
 		RuntimeProcess process = (RuntimeProcess)DebugPlugin.newProcess(launch, p, Constants.PROCESS_MESSAGE);
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			if(!process.isTerminated()) { 
 				NodeDebugUtil.launch(mode, launch, monitor);
 			}
 		}
 		nodeProcess = process;
 	}
 
 	private void showPreferencesDialog() {
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 
				MessageDialog dialog = new MessageDialog(shell, "Nodeclipse", null, "Node.js runtime is not correctly configured.\n\n"
 						+ "Please goto Window -> Prefrences -> Nodeclipse and configure the correct location", MessageDialog.ERROR, new String[] { "Open Prefrences ...", "Cancel" }, 0);
 				int result = dialog.open();
 				if (result == 0) {
 					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(shell, PreferenceConstants.PREFERENCES_PAGE, null, null);
 					if (pref != null)
 						pref.open();
 				}
 			}
 		});
 	}
 	
 	public static void terminateNodeProcess() {
 		if(nodeProcess != null) {
 			try {
 				nodeProcess.terminate();
 			} catch (DebugException e) {
 				e.printStackTrace();
 			}
 			nodeProcess = null;
 		}
 	}
 }
