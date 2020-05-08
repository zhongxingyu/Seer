 package org.rubypeople.rdt.internal.launching;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.model.IProcess;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.SocketUtil;
 import org.rubypeople.rdt.internal.debug.core.RdtDebugCorePlugin;
 import org.rubypeople.rdt.internal.debug.core.RubyDebuggerProxy;
 import org.rubypeople.rdt.internal.debug.core.model.RubyDebugTarget;
 import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;
 import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.IVMRunner;
 import org.rubypeople.rdt.launching.VMRunnerConfiguration;
 
 public class StandardVMDebugger extends StandardVMRunner implements IVMRunner {
 
 	public StandardVMDebugger(IVMInstall vmInstance) {
 		super(vmInstance);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jdt.launching.IVMRunner#run(org.eclipse.jdt.launching.VMRunnerConfiguration,
 	 *      org.eclipse.debug.core.ILaunch,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
 		subMonitor.beginTask(LaunchingMessages.StandardVMDebugger_Launching_VM____1, 4);
 		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Finding_free_socket____2);
 
 		int port = SocketUtil.findFreePort();
 		if (port == -1) {
 			abort(LaunchingMessages.StandardVMDebugger_Could_not_find_a_free_socket_for_the_debugger_1, null, IRubyLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
 		}
 
 		subMonitor.worked(1);
 
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}
 
 		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Constructing_command_line____3);
 
 		RubyDebugTarget debugTarget = new RubyDebugTarget(launch, port);
 		String program = constructProgramString(config);
 		List<String> arguments = new ArrayList<String>(12);
 		arguments.add(program);
 
 		// VM args are the first thing after the ruby program so that users can
 		// specify
 		// options like '-client' & '-server' which are required to be the first
 		// options
 		String[] allVMArgs = combineVmArgs(config, fVMInstance);
 		addArguments(allVMArgs, arguments);
 
 		String[] cp = config.getLoadPath();
 		if (cp.length > 0) {
 			arguments.addAll(convertLoadPath(cp));
 		}
 
 		arguments.addAll(debugSpecificVMArgs(debugTarget));
 
 		arguments.add(StandardVMRunner.END_OF_OPTIONS_DELIMITER);
 
 		arguments.add(config.getFileToLaunch());
 		addArguments(config.getProgramArguments(), arguments);
 		String[] cmdLine = new String[arguments.size()];
 		arguments.toArray(cmdLine);
 
 		String[] envp = config.getEnvironment();
 
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}
 
 		subMonitor.worked(1);
 		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Starting_virtual_machine____4);
 
 		Process p = null;
 
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			return;
 		}
 
 		File workingDir = getWorkingDir(config);
 		p = exec(cmdLine, workingDir, envp);
 		if (p == null) {
 			return;
 		}
 
 		// check for cancellation
 		if (monitor.isCanceled()) {
 			p.destroy();
 			return;
 		}
 
 		IProcess process = newProcess(launch, p, renderProcessLabel(cmdLine), getDefaultProcessMap());
 		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
 		subMonitor.worked(1);
 		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Establishing_debug_connection____5);
 
 		debugTarget.setProcess(process);
		RubyDebuggerProxy proxy = new RubyDebuggerProxy(debugTarget, RDebugVMDebugger.getDirectoryOfRubyDebuggerFile(), true);
 		try {
 			proxy.start();
 			launch.addDebugTarget(debugTarget);
 		} catch (IOException iox) {
 			LaunchingPlugin.log(new Status(IStatus.ERROR, LaunchingPlugin.PLUGIN_ID, IStatus.ERROR, LaunchingMessages.RdtLaunchingPlugin_processTerminatedBecauseNoDebuggerConnection, null));
 			debugTarget.terminate();
 		} catch (RubyProcessingException e) {
 			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_5, e, IRubyLaunchConfigurationConstants.ERR_CONNECTION_FAILED);
 			debugTarget.terminate();
 		}
 		// if (p != null) {
 		// p.destroy();
 		// }
 	}
 
 	protected List<String> debugSpecificVMArgs(RubyDebugTarget debugTarget) {
 		List<String> arguments = new ArrayList<String>();
 		if (!debugTarget.isUsingDefaultPort()) {
 			arguments.add("-r" + debugTarget.getDebugParameterFile().getAbsolutePath());
 		}
 
 		if (RdtDebugCorePlugin.isRubyDebuggerVerbose() || isDebuggerVerbose()) {
 			arguments.add("-reclipseDebugVerbose");
 		} else {
 			arguments.add("-reclipseDebug");
 		}
 		// FIXME Somehow hook this into the loadpath stuff?
 		arguments.add("-I");
 		arguments.add(LaunchingPlugin.osDependentPath(getDirectoryOfRubyDebuggerFile().replace('/', File.separatorChar)));
 		return arguments;
 	}
 
 	protected static boolean isDebuggerVerbose() {
 		return LaunchingPlugin.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.VERBOSE_DEBUGGER);
 	}
 
 	public static String getDirectoryOfRubyDebuggerFile() {
 		return RubyCore.getOSDirectory(LaunchingPlugin.getDefault()) + "ruby";
 	}
 
 }
