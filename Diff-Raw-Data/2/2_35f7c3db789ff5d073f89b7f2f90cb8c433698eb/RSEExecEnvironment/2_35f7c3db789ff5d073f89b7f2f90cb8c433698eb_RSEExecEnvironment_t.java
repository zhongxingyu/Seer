 package org.eclipse.dltk.core.internal.rse;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionLogger;
 import org.eclipse.dltk.core.internal.rse.perfomance.RSEPerfomanceStatistics;
 import org.eclipse.dltk.internal.launching.execution.EFSDeployment;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.subsystems.ISubSystem;
 import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
 import org.eclipse.rse.services.files.IFileService;
 import org.eclipse.rse.services.shells.IHostShell;
 import org.eclipse.rse.services.shells.IShellService;
 import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
 import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
 
 public class RSEExecEnvironment implements IExecutionEnvironment {
 
 	private static final String EXEC_BIN_SH = "exec /bin/sh "; //$NON-NLS-1$
 	private static final String TOKEN_PREFIX = "DLTK_INITIAL_PREFIX_EXECUTION_STRING:"; //$NON-NLS-1$
 
 	private final RSEEnvironment environment;
 	private static int counter = -1;
 
 	private static final Map<IHost, Map<String, String>> hostToEnvironment = new HashMap<IHost, Map<String, String>>();
 
 	public RSEExecEnvironment(RSEEnvironment env) {
 		this.environment = env;
 	}
 
 	public IDeployment createDeployment() {
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			RSEPerfomanceStatistics
 					.inc(RSEPerfomanceStatistics.DEPLOYMENTS_CREATED);
 		}
 		if (!getEnvironment().connect()) {
 			return null;
 		}
 		String tmpDir = getTempDir();
 		if (tmpDir != null) {
 			String rootPath = tmpDir + environment.getSeparator()
 					+ getTempName("dltk", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
 			URI rootUri = createRemoteURI(environment.getHost(), rootPath);
 			try {
 				return new EFSDeployment(environment, rootUri);
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	private URI createRemoteURI(IHost host, String rootPath) {
 		return RSEEnvironment.getURIFor(host, rootPath);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <SUBSYSTEM extends ISubSystem> SUBSYSTEM getSubSystem(IHost host,
 			Class<SUBSYSTEM> clazz) {
 		ISubSystem[] subsys = host.getSubSystems();
 		for (int i = 0; i < subsys.length; i++) {
 			if (clazz.isInstance(subsys[i]))
 				return (SUBSYSTEM) subsys[i];
 		}
 		return null;
 	}
 
 	private String getTempName(String prefix, String suffix) {
 		if (counter == -1) {
 			counter = new Random().nextInt() & 0xffff;
 		}
 		counter++;
 		return prefix + Integer.toString(counter) + suffix;
 	}
 
 	private String getTempDir() {
 		final IHost host = environment.getHost();
 		final IShellServiceSubSystem system = getSubSystem(host,
 				IShellServiceSubSystem.class);
 		if (system == null) {
 			DLTKRSEPlugin.logWarning(NLS.bind(
 					Messages.RSEExecEnvironment_hostNotFound, host.getName()));
 			return null;
 		}
 		try {
 			system.connect(new NullProgressMonitor(), false);
 			final String tmp = system.getConnectorService().getTempDirectory();
 			if (tmp != null && tmp.length() != 0) {
 				return tmp;
 			} else {
 				return "/tmp"; //$NON-NLS-1$
 			}
 		} catch (Exception e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public Process exec(String[] cmdLine, IPath workingDir, String[] environment)
 			throws CoreException {
 		return exec(cmdLine, workingDir, environment, null);
 	}
 
 	public Process exec(String[] cmdLine, IPath workingDir,
 			String[] environment, IExecutionLogger logger) throws CoreException {
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			RSEPerfomanceStatistics
 					.inc(RSEPerfomanceStatistics.EXECUTION_COUNT);
 		}
 		final long start = RSEPerfomanceStatistics.PERFOMANCE_TRACING ? System
 				.currentTimeMillis() : 0;
 		final IHost host = this.environment.getHost();
 
 		// obtain IFileService
 		final IFileServiceSubSystem fileService = getSubSystem(host,
 				IFileServiceSubSystem.class);
 		if (fileService == null) {
 			throw new CoreException(newStatus(
 					RSEStatusConstants.NO_FILE_SERVICE, NLS.bind(
 							Messages.RSEExecEnvironment_NoFileServicerError,
 							host.getAliasName()), null));
 		}
		if (!getEnvironment().connect()) {
 			throw new CoreException(newStatus(
 					RSEStatusConstants.NO_FILE_SERVICE, NLS.bind(
 							Messages.RSEExecEnvironment_NotConnected, host
 									.getAliasName()), null));
 		}
 
 		// remote path for launcher file
 		final String tmpLauncherDir = getTempDir();
 		final String tmpLauncher = "dltk-" + fileService.getUserId() + System.currentTimeMillis() + ".sh"; //$NON-NLS-1$ //$NON-NLS-2$
 		final String tmpLauncherPath = tmpLauncherDir
 				+ fileService.getSeparatorChar() + tmpLauncher;
 
 		// build commands
 		final List<String> commands = new ArrayList<String>();
 		if (workingDir != null) {
 			final String p = this.environment.convertPathToString(workingDir);
 			commands.add("cd " + p); //$NON-NLS-1$
 		} else {
 			commands.add("cd /"); //$NON-NLS-1$
 		}
 		/*
 		 * Sometimes environment variables aren't set by the runCommand() call,
 		 * so use export.
 		 */
 		if (environment != null) {
 			// TODO Skip environment variables which are already in shell?
 			for (int i = 0; i < environment.length; i++) {
 				commands.add(buildExportCommand(environment[i]));
 			}
 		}
 		final String token = TOKEN_PREFIX + System.currentTimeMillis();
 		final String echoCmd = "echo \"" + token + "\""; //$NON-NLS-1$ //$NON-NLS-2$
 		commands.add(echoCmd);
 		commands.add(buildCommand(cmdLine));
 		commands.add(echoCmd);
 		commands.add("rm -f " + tmpLauncherPath); //$NON-NLS-1$
 		if (logger != null) {
 			logger.logLine("launcher=" + tmpLauncherDir + '/' + tmpLauncher); //$NON-NLS-1$
 			for (String command : commands) {
 				logger.logLine("launcher:" + command); //$NON-NLS-1$
 			}
 			logger.logLine("launcher:END"); //$NON-NLS-1$
 		}
 
 		// save launcher to the remote location
 		try {
 			final OutputStream os = fileService.getFileService()
 					.getOutputStream(tmpLauncherDir, tmpLauncher,
 							IFileService.TEXT_MODE, new NullProgressMonitor());
 			try {
 				final Writer writer = new OutputStreamWriter(
 						new BufferedOutputStream(os, 4096), fileService
 								.getRemoteEncoding());
 				try {
 					for (String command : commands) {
 						writer.write(command);
 						writer.write('\n');
 					}
 					writer.flush();
 				} finally {
 					try {
 						writer.close();
 					} catch (IOException e) {
 						// ignore
 					}
 				}
 			} finally {
 				try {
 					os.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 		} catch (Exception e) {
 			final String msg = NLS.bind(
 					Messages.RSEExecEnvironment_LauncherUploadError, host
 							.getAliasName(), e.getMessage());
 			throw new CoreException(newStatus(
 					RSEStatusConstants.LAUNCHER_UPLOAD_ERROR, msg, e));
 		}
 
 		// execute uploaded launcher in remote shell
 		final IShellServiceSubSystem shell = getSubSystem(host,
 				IShellServiceSubSystem.class);
 		if (shell == null) {
 			throw new CoreException(newStatus(
 					RSEStatusConstants.NO_SHELL_SERVICE, NLS.bind(
 							Messages.RSEExecEnvironment_NoShellService, host
 									.getAliasName()), null));
 		}
 		try {
 			shell.connect(new NullProgressMonitor(), false);
 		} catch (Exception e) {
 			throw new CoreException(newStatus(RSEStatusConstants.CONNECT_ERROR,
 					NLS.bind(Messages.RSEExecEnvironment_ErrorConnecting, host
 							.getAliasName(), e.getMessage()), e));
 		}
 
 		if (!shell.isConnected()) {
 			throw new CoreException(newStatus(
 					RSEStatusConstants.NOT_CONNECTED_ERROR, NLS.bind(
 							Messages.RSEExecEnvironment_NotConnected, host
 									.getAliasName()), null));
 		}
 		// TODO try to use "exec" channel instead of "shell" one.
 		final IShellService shellService = shell.getShellService();
 		final String command = EXEC_BIN_SH + tmpLauncherPath;
 		final IHostShell hostShell;
 		try {
 			hostShell = shellService.runCommand(Util.EMPTY_STRING, command,
 					environment, new NullProgressMonitor());
 		} catch (SystemMessageException e) {
 			throw new CoreException(newStatus(
 					RSEStatusConstants.COMMAND_RUN_ERROR, NLS.bind(
 							Messages.RSEExecEnvironment_ErrorRunningCommand,
 							host.getAliasName(), e.getMessage()), e));
 		}
 
 		// wrap shell as java.lang.Process and return
 		try {
 			return new MyHostShellProcessAdapter(hostShell, token, logger);
 		} catch (Exception e) {
 			hostShell.writeToShell(MyHostShellProcessAdapter.CTRL_C);
 			hostShell.exit();
 			throw new CoreException(newStatus(
 					RSEStatusConstants.INTERNAL_ERROR, NLS.bind(
 							Messages.RSEExecEnvironment_ProcessCreateError, e
 									.getMessage()), e));
 		} finally {
 			if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 				RSEPerfomanceStatistics.inc(
 						RSEPerfomanceStatistics.EXECUTION_TIME, System
 								.currentTimeMillis()
 								- start);
 			}
 		}
 	}
 
 	private static String buildExportCommand(String envEntry) {
 		return toShellArguments(envEntry) + ";export " + extractName(envEntry); //$NON-NLS-1$
 	}
 
 	private static Status newStatus(int code, String msg,
 			final Throwable exception) {
 		return new Status(IStatus.ERROR, DLTKRSEPlugin.PLUGIN_ID, code, msg,
 				exception);
 	}
 
 	/**
 	 * @param environmentEntry
 	 * @return
 	 */
 	private static String extractName(String environmentEntry) {
 		final int pos = environmentEntry.indexOf('=');
 		return pos > 0 ? environmentEntry.substring(0, pos) : environmentEntry;
 	}
 
 	private static String toShellArguments(String cmd) {
 		String replaceAll = cmd.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
 		return replaceAll;
 	}
 
 	private String buildCommand(String[] cmdLine) {
 		StringBuffer cmd = new StringBuffer();
 		for (int i = 0; i < cmdLine.length; i++) {
 			if (i != 0) {
 				cmd.append(" "); //$NON-NLS-1$
 			}
 			cmd.append(cmdLine[i]);
 		}
 		return cmd.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map getEnvironmentVariables(boolean realyNeed) {
 		if (!realyNeed) {
 			return new HashMap<String, String>();
 		}
 		final long start = System.currentTimeMillis();
 		synchronized (hostToEnvironment) {
 			final Map<String, String> result = hostToEnvironment
 					.get(environment.getHost());
 			if (result != null) {
 				return new HashMap<String, String>(result);
 			}
 		}
 		final Map<String, String> result = new HashMap<String, String>();
 		try {
 			Process process = exec(new String[] { "set" }, Path.EMPTY, null); //$NON-NLS-1$
 			if (process != null) {
 				final BufferedReader input = new BufferedReader(
 						new InputStreamReader(process.getInputStream()));
 				Thread t = new Thread(NLS.bind(
 						Messages.RSEExecEnvironment_fetchEnvVars, environment
 								.getHost().getName())) {
 					public void run() {
 						try {
 							while (true) {
 								String line = input.readLine();
 								if (line == null) {
 									break;
 								}
 								line = line.trim();
 								int pos = line.indexOf("="); //$NON-NLS-1$
 								if (pos != -1) {
 									String varName = line.substring(0, pos);
 									String varValue = line.substring(pos + 1);
 									result.put(varName, varValue);
 								}
 							}
 						} catch (IOException e) {
 							if (DLTKCore.DEBUG)
 								DLTKRSEPlugin.log(e);
 						}
 					}
 				};
 				t.start();
 				try {
 					t.join(25000);// No more than 25 seconds
 				} catch (InterruptedException e) {
 					DLTKRSEPlugin.log(e);
 				}
 				process.destroy();
 			}
 		} catch (CoreException e) {
 			DLTKRSEPlugin.log(e);
 		}
 		if (!result.isEmpty()) {
 			synchronized (hostToEnvironment) {
 				hostToEnvironment.put(environment.getHost(), Collections
 						.unmodifiableMap(result));
 			}
 		}
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			final long end = System.currentTimeMillis();
 			RSEPerfomanceStatistics
 					.inc(RSEPerfomanceStatistics.ENVIRONMENT_RECEIVE_COUNT);
 			RSEPerfomanceStatistics.inc(
 					RSEPerfomanceStatistics.ENVIRONMENT_RECEIVE_TIME,
 					(end - start));
 		}
 		return result;
 	}
 
 	public IEnvironment getEnvironment() {
 		return environment;
 	}
 
 	public boolean isValidExecutableAndEquals(String possibleName, IPath path) {
 		if (environment.getHost().getSystemType().isWindows()) {
 			possibleName = possibleName.toLowerCase();
 			String fName = path.removeFileExtension().toString().toLowerCase();
 			String ext = path.getFileExtension();
 			if (possibleName.equals(fName)
 					&& ("exe".equalsIgnoreCase(ext) || "bat".equalsIgnoreCase(ext))) { //$NON-NLS-1$ //$NON-NLS-2$
 				return true;
 			}
 		} else {
 			String fName = path.lastSegment();
 			if (fName.equals(possibleName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
