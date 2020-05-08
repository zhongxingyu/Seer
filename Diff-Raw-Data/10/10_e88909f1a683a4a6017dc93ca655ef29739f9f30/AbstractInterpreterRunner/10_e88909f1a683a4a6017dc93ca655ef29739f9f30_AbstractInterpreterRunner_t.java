 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.model.IProcess;
 
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
 	private IInterpreterInstall interpreterInstall;
 
 	/**
 	 * Construct and return a String containing the full path of a interpreter
 	 * executable command such as 'tclsh.exe' or 'wish.exe'. If the
 	 * configuration specifies an explicit executable, that is used.
 	 * 
 	 * @return full path toscriptexecutable
 	 * @exception CoreException
 	 *                if unable to locate an executeable
 	 */
 	protected String constructProgramString() throws CoreException {
 		File exe = interpreterInstall.getInstallLocation();
 		if (exe == null) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.StandardInterpreterRunner_Unable_to_locate_executable_for__0__1,
 									new String[] { interpreterInstall.getName() }),
 					null, IDLTKLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
 		}
 		return exe.getAbsolutePath();
 	}
 
 	public static String renderProcessLabel(String[] commandLine) {
 		String format = LaunchingMessages.StandardInterpreterRunner__0____1___2;
 		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
 				DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
 		return MessageFormat.format(format, new String[] { commandLine[0],
 				timestamp });
 	}
 
 	/**
 	 * String representation of the command line
 	 * 
 	 * @param commandLine
 	 * @return
 	 */
 	protected static String renderCommandLine(String[] commandLine) {
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
 
 	// Abort helpers
 	protected void abort(String message, Throwable exception, int code)
 			throws CoreException {
 		abort(getPluginId(), message, exception, code);
 	}
 
 	protected void abort(String pluginId, String message, Throwable exception,
 			int code) throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR, pluginId, code,
 				message, exception));
 	}
 
 	// Execution helpers
 	protected Process exec(String[] cmdLine, File workingDirectory)
 			throws CoreException {
 		return DebugPlugin.exec(cmdLine, workingDirectory);
 	}
 
 	protected Process exec(String[] cmdLine, File workingDirectory,
 			String[] envp) throws CoreException {
 		return DebugPlugin.exec(cmdLine, workingDirectory, envp);
 	}
 
 	// 
 	protected Map getDefaultProcessMap() {
 		Map map = new HashMap();
 		map.put(IProcess.ATTR_PROCESS_TYPE, getProcessType());
 		return map;
 	}
 
 	protected String getProcessType() {
 		return IDLTKLaunchConfigurationConstants.ID_SCRIPT_PROCESS_TYPE;
 	}
 
 	protected AbstractInterpreterRunner(IInterpreterInstall install) {
 		this.interpreterInstall = install;
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
 	 * Returns the working directory to use for the launched Interpreter, or
 	 * <code>null</code> if the working directory is to be inherited from the
 	 * current process.
 	 * 
 	 * @return the working directory to use
 	 * @exception CoreException
 	 *                if the working directory specified by the configuration
 	 *                does not exist or is not a directory
 	 */
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		IProgressMonitor subMonitor = getSubMonitor(monitor);
 
 		// ILaunchConfiguration configuration = launch.getLaunchConfiguration();
 		// if (configuration != null) {
 		// TODO: redesign
 		// boolean useDltk = configuration.getAttribute(
 		// IDLTKLaunchConfigurationConstants.ATTR_USE_DLTK_OUTPUT,
 		// false);
 		// if (useDltk) {
 		// String id = configuration.getAttribute(
 		// IDLTKLaunchConfigurationConstants.ATTR_DLTK_CONSOLE_ID,
 		// (String) null);
 		// cmdLine = alterCommandLine(cmdLine, id);
 		// }
 		// }
 
 		subMonitor.worked(1);
 
 		// check for cancellation
 		if (subMonitor.isCanceled()) {
 			return;
 		}
 
 		subMonitor
 				.subTask(LaunchingMessages.StandardInterpreterRunner_Starting___3);
 
 		String[] cmdLine = getRealCommandLine(config, launch);
 		Process p = exec(cmdLine, getRealWorkingDirectory(config, launch),
 				getRealEnvironment(config, launch));
		
		if (config.getProperty("NO_PROCESS") != null) {
			return;
		}
 
 		if (p == null) {
 			return;
 		}
 
 		if (subMonitor.isCanceled()) {
 			p.destroy();
 			return;
 		}
 
 		String label = renderProcessLabel(cmdLine);
 
 		IProcess process = newProcess(launch, p, label, getDefaultProcessMap());
 
 		process.setAttribute(IProcess.ATTR_CMDLINE, label);
 
 		subMonitor.worked(1);
 		subMonitor.done();
 	}
 
 	protected String[] getRealCommandLine(InterpreterConfig config,
 			ILaunch launch) throws CoreException {
 		String[] interpreterArgs = interpreterInstall.getInterpreterArguments();
 		
 		String exe = (String)config.getProperty("OVERRIDE_EXE");
 		if (exe == null) {
 			exe = constructProgramString();
 		}
 
 		return config.renderCommandLine(exe);
 	}
 
 	protected File getRealWorkingDirectory(InterpreterConfig config,
 			ILaunch launch) throws CoreException {
 		return config.getWorkingDirectory();
 	}
 
 	protected String[] getRealEnvironment(InterpreterConfig config,
 			ILaunch launch) throws CoreException {
 		return config.getEnvironmentAsStrings();
 	}
 
 	private static IProgressMonitor getSubMonitor(IProgressMonitor monitor) {
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
 
 	protected abstract String getPluginId();
 }
