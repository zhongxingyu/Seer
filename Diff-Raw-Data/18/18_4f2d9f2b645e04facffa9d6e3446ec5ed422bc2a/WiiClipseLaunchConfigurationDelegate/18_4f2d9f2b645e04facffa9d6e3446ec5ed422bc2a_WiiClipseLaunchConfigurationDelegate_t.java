 package com.github.wiiclipse.launcher;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
 import org.eclipse.cdt.core.model.ICProject;
 import org.eclipse.cdt.debug.core.CDebugUtils;
 import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
 import org.eclipse.cdt.launch.LaunchUtils;
 import org.eclipse.cdt.launch.internal.LocalRunLaunchDelegate;
 import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 
 import com.github.wiiclipse.core.WiiClipsePathResolver;
 import com.github.wiiclipse.core.WiiClipsePlugin;
 
 @SuppressWarnings("restriction")
 public class WiiClipseLaunchConfigurationDelegate extends
 		LocalRunLaunchDelegate {
 	@Override
 	public void launch(ILaunchConfiguration config, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		monitor.beginTask("Launching Wii Application", 10);
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}
 		try {
			IBinaryObject exeFile = null;
 			monitor.worked(1);
 			IPath exePath = CDebugUtils.verifyProgramPath(config);
 			ICProject project = CDebugUtils.verifyCProject(config);
 			if (exePath != null) {
 				exeFile = verifyBinary(project, exePath);
 			}
 			String[] programArguments = LaunchUtils
 					.getProgramArgumentsArray(config);
 			File wd = getWorkingDirectory(config);
 			if (wd == null) {
 				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			monitor.worked(5);
 			ArrayList<String> command = new ArrayList<String>(
 					1 + programArguments.length);
 			IPath wiiLoadPath = WiiClipsePathResolver.getDevkitPPCBinPath()
 					.append("wiiload");
 			command.add(wiiLoadPath.toOSString());
 			command.add(exePath.toOSString());
 			command.addAll(Arrays.asList(programArguments));
 			String[] commandArray = (String[]) command
 					.toArray(new String[command.size()]);
 
 			monitor.worked(8);
 			boolean usePty = config.getAttribute(
 					ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
 					ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
 			Process process = exec(commandArray, getEnvironment(config), wd,
 					usePty);
 			monitor.worked(9);
 			DebugPlugin.newProcess(launch, process,
 					renderProcessLabel(commandArray[0]));
		} catch (Exception e) {
			//TODO log error
 		} finally {
 			monitor.done();
 		}
 	}
 
 	@Override
 	protected String[] getEnvironment(ILaunchConfiguration config)
 			throws CoreException {
 
 		int conMode = config.getAttribute(
 				WiiClipseLaunchConfigurationConstants.ATTR_CONNECTION_MODE,
 				WiiClipseLaunchConfigurationConstants.CONNECTION_MODE_DEFAULT);
 		ArrayList<String> env = new ArrayList<String>();
 		String wiiloadVariable = "";
 		if (conMode == WiiClipseLaunchConfigurationConstants.CONNECTION_MODE_TCP_IP) {
 			String hostname = config.getAttribute(
 					WiiClipseLaunchConfigurationConstants.ATTR_HOSTNAME, "");
 			wiiloadVariable = "tcp:" + hostname;
 		} else {
 			wiiloadVariable = config.getAttribute(
 					WiiClipseLaunchConfigurationConstants.ATTR_DEVICE, "");
 		}
 		wiiloadVariable = "WIILOAD=" + wiiloadVariable;
 
 		env.add(wiiloadVariable);
 		String[] confEnv = super.getEnvironment(config);
 		for (String var : confEnv) {
 			if (!var.startsWith("WIILOAD="))
 				env.add(var);
 		}
 		return env.toArray(new String[0]);
 	}
 
 	@Override
 	protected String getPluginID() {
 		return WiiClipsePlugin.ID;
 	}
 
 }
