 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.dltk.core.DLTKCore;
 
 import com.ibm.icu.text.DateFormat;
 import com.ibm.icu.text.MessageFormat;
 
 /**
  * Abstract implementation of a interpreter runner.
  * <p>
  * Clients implementing interpreter runners should subclass this class.
  * </p>
  * 
  * @see IInterpreterRunner
  * 
  */
 public abstract class AbstractInterpreterRunner implements IInterpreterRunner {
 	private IInterpreterInstall interpreterInstance;
 	
 	/**
 	 * Construct and return a String containing the full path of a interpreter
 	 * executable command such as 'tclsh.exe' or 'wish.exe'. If the
 	 * configuration specifies an explicit executable, that is used.
 	 * 
 	 * @return full path toscriptexecutable
 	 * @exception CoreException
 	 *                if unable to locate an executeable
 	 */
 	protected String constructProgramString(
 			InterpreterRunnerConfiguration config) throws CoreException {
 		File exe = interpreterInstance.getInstallLocation();
 		if (exe == null) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.StandardInterpreterRunner_Unable_to_locate_executable_for__0__1,
 									new String[] { interpreterInstance
 											.getName() }), null,
 					IDLTKLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
 		}
 		return exe.getAbsolutePath();
 	}
 
 	private static void addArgument(String arg, List v) {
 		if (arg != null) {
 			v.add(arg);
 		}
 	}
 
 	private static void addArguments(String[] args, List v) {
 		if (args != null) {
 			for (int i = 0; i < args.length; i++) {
 				v.add(args[i]);
 			}
 		}
 	}
 
 	private String[] extractCommandLine(InterpreterRunnerConfiguration config)
 			throws CoreException {
 		List arguments = new ArrayList();
 
 		addArgument(constructProgramString(config), arguments);
 		addArguments(combineInterpreterArgs(config, interpreterInstance),
 				arguments);
 		addArgument(config.getScriptToLaunch(), arguments);
 		addArguments(config.getProgramArguments(), arguments);
 
 		return (String[]) arguments.toArray(new String[arguments.size()]);
 	}
 
 	protected String getCmdLineAsString(String[] cmdLine) {
 		StringBuffer buff = new StringBuffer();
 		for (int i = 0, numStrings = cmdLine.length; i < numStrings; i++) {
 			buff.append(cmdLine[i]);
 			buff.append(' ');
 		}
 		return buff.toString().trim();
 	}
 
 	protected String[] alterCommandLine(String[] cmdLine, String id) {
 		return cmdLine; // Do nothing
 	}
 
 	protected String renderCommandLine(String[] commandLine) {
 		if (commandLine.length < 1)
 			return ""; //$NON-NLS-1$
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < commandLine.length; i++) {
 			buf.append(' ');
 			char[] characters = commandLine[i].toCharArray();
 			StringBuffer command = new StringBuffer();
 			boolean containsSpace = false;
 			for (int j = 0; j < characters.length; j++) {
 				char character = characters[j];
 				if (character == '\"') {
 					command.append('\\');
 				} else if (character == ' ') {
 					containsSpace = true;
 				}
 				command.append(character);
 			}
 			if (containsSpace) {
 				buf.append('\"');
 				buf.append(command.toString());
 				buf.append('\"');
 			} else {
 				buf.append(command.toString());
 			}
 		}
 		return buf.toString();
 	}
 
 	protected void abort(String message, Throwable exception, int code)
 			throws CoreException {
 		abort(getPluginIdentifier(), message, exception, code);
 	}
 
 	protected void abort(String pluginId, String message, Throwable exception,
 			int code) throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR, pluginId, code,
 				message, exception));
 	}
 
 	protected Process exec(String[] cmdLine, File workingDirectory)
 			throws CoreException {
 		return DebugPlugin.exec(cmdLine, workingDirectory);
 	}
 
 	protected Process exec(String[] cmdLine, File workingDirectory,
 			String[] envp) throws CoreException {
 		return DebugPlugin.exec(cmdLine, workingDirectory, envp);
 	}
 
 	protected Map getDefaultProcessMap() {
 		Map map = new HashMap();
 		map.put(IProcess.ATTR_PROCESS_TYPE, getProcessType());
 		return map;
 	}
 
 	protected String getProcessType() {
 		return IDLTKLaunchConfigurationConstants.ID_SCRIPT_PROCESS_TYPE;
 	}
 
 	protected AbstractInterpreterRunner(IInterpreterInstall install) {
 		interpreterInstance = install;
 	}
 
 	public String renderProcessLabel(String[] commandLine) {
 		String format = LaunchingMessages.StandardInterpreterRunner__0____1___2;
 		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
 				DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
 		return MessageFormat.format(format, new String[] { commandLine[0],
 				timestamp });
 	}
 
 	/**
 	 * Returns a new process aborting if the process could not be created.
 	 * 
 	 * @param launch
 	 *            the launch the process is contained in
 	 * @param p
 	 *            the system process to wrap
 	 * @param label
 	 *            the label assigned to the process
 	 * @param attributes
 	 *            values for the attribute map
 	 * @return the new process
 	 * @throws CoreException
 	 *             problems occurred creating the process
 	 * 
 	 */
 	protected IProcess newProcess(ILaunch launch, Process p, String label,
 			Map attributes) throws CoreException {
 		IProcess process = DebugPlugin.newProcess(launch, p, label, attributes);
 		if (process == null) {
 			p.destroy();
 			abort(LaunchingMessages.AbstractInterpreterRunner_0, null,
 					IDLTKLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
 		}
 		return process;
 	}
 
 	/**
 	 * Combines and returns Interpreter arguments specified by the runner
 	 * configuration, with those specified by the Interpreter install, if any.
 	 * 
 	 * @param configuration
 	 *            runner configuration
 	 * @param InterpreterInstall
 	 *            Interpreter install
 	 * @return combined Interpreter arguments specified by the runner
 	 *         configuration and Interpreter install
 	 * 
 	 */
 	protected String[] combineInterpreterArgs(
 			InterpreterRunnerConfiguration configuration,
 			IInterpreterInstall InterpreterInstall) {
 		String[] launchInterpreterArgs = configuration
 				.getInterpreterArguments();
 		String[] InterpreterInterpreterArgs = InterpreterInstall
 				.getInterpreterArguments();
 		if (InterpreterInterpreterArgs == null
 				|| InterpreterInterpreterArgs.length == 0) {
 			return launchInterpreterArgs;
 		}
 		String[] allInterpreterArgs = new String[launchInterpreterArgs.length
 				+ InterpreterInterpreterArgs.length];
 		System.arraycopy(launchInterpreterArgs, 0, allInterpreterArgs, 0,
 				launchInterpreterArgs.length);
 		System
 				.arraycopy(InterpreterInterpreterArgs, 0, allInterpreterArgs,
 						launchInterpreterArgs.length,
 						InterpreterInterpreterArgs.length);
 		return allInterpreterArgs;
 	}
 
 	/**
 	 * Returns the working directory to use for the launched Interpreter, or
 	 * <code>null</code> if the working directory is to be inherited from the
 	 * current process.
 	 * 
 	 * @return the working directory to use
 	 * @exception CoreException
 	 *                if the working directory specified by the configuration
 	 *                does not exist or is not a directory
 	 */
 	protected File getWorkingDir(InterpreterRunnerConfiguration config)
 			throws CoreException {
 		String path = config.getWorkingDirectory();
 		if (path == null) {
 			return null;
 		}
 		File dir = new File(path);
 		if (!dir.isDirectory()) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.StandardInterpreterRunner_Specified_working_directory_does_not_exist_or_is_not_a_directory___0__3,
 									new String[] { path }),
 					null,
 					IDLTKLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
 		}
 		return dir;
 	}
 
 	public void run(InterpreterRunnerConfiguration config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		IProgressMonitor subMonitor = getSubMonitor(monitor);
 
 		String[] cmdLine = extractCommandLine(config);
 
 		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
		
 		if (configuration != null) {
			boolean useDltk = configuration.getAttribute(
 					IDLTKLaunchConfigurationConstants.ATTR_USE_DLTK_OUTPUT,
					false);			
			if (useDltk) {				
 				String id = configuration.getAttribute(
 						IDLTKLaunchConfigurationConstants.ATTR_DLTK_CONSOLE_ID,
 						(String) null);
 				cmdLine = alterCommandLine(cmdLine, id);
 			}
 		}
 
 		String[] envp = config.getEnvironment();
 
 		subMonitor.worked(1);
 
 		// check for cancellation
 		if (subMonitor.isCanceled()) {
 			return;
 		}
 
 		subMonitor
 				.subTask(LaunchingMessages.StandardInterpreterRunner_Starting___3);
 
 		Process p = null;
 		File workingDir = getWorkingDir(config);
 		p = exec(cmdLine, workingDir, envp);
 		if (p == null) {
 			return;
 		}
 		// check for cancellation
 		if (subMonitor.isCanceled()) {
 			p.destroy();
 			return;
 		}
 
 		IProcess process = newProcess(launch, p, renderProcessLabel(cmdLine),
 				getDefaultProcessMap());
 		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
 
 		// XXX: is this still applicable, everything else has it commented out
 		// IStreamListener errorParser = getErrorParser(config.getProject());
 		// if (errorParser != null) {
 		// process.getStreamsProxy().getErrorStreamMonitor().addListener(
 		// errorParser);
 		// }
 
 		subMonitor.worked(1);
 		subMonitor.done();
 	}
 
 	private IProgressMonitor getSubMonitor(IProgressMonitor monitor) {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
 		subMonitor
 				.beginTask(
 						LaunchingMessages.StandardInterpreterRunner_Launching_Interpreter____1,
 						2);
 		subMonitor
 				.subTask(LaunchingMessages.StandardInterpreterRunner_Constructing_command_line____2);
 
 		return subMonitor;
 	}
 
 	protected abstract String getPluginIdentifier();
 }
