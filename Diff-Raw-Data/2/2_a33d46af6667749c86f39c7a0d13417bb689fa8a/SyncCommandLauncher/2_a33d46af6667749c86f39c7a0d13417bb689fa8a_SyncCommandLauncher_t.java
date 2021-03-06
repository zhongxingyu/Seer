 /*******************************************************************************
  * Copyright (c) 2009, 2010, 2012 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Jeff Overbey (Illinois) - Environment management support
  *******************************************************************************/
 package org.eclipse.ptp.rdt.sync.core.remotemake;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.cdt.core.CCorePlugin;
 import org.eclipse.cdt.core.ICommandLauncher;
 import org.eclipse.cdt.managedbuilder.core.IBuilder;
 import org.eclipse.cdt.managedbuilder.core.IConfiguration;
 import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.ptp.ems.core.EnvManagerRegistry;
 import org.eclipse.ptp.ems.core.EnvManagerProjectProperties;
 import org.eclipse.ptp.ems.core.IEnvManager;
 import org.eclipse.ptp.internal.rdt.core.index.IndexBuildSequenceController;
 import org.eclipse.ptp.internal.rdt.core.remotemake.RemoteProcessClosure;
 import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
 import org.eclipse.ptp.rdt.sync.core.BuildScenario;
 import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
 import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;
 import org.eclipse.ptp.rdt.sync.core.SyncFlag;
 import org.eclipse.ptp.rdt.sync.core.SyncManager;
 import org.eclipse.ptp.remote.core.IRemoteConnection;
 import org.eclipse.ptp.remote.core.IRemoteFileManager;
 import org.eclipse.ptp.remote.core.IRemoteProcess;
 import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
 import org.eclipse.ptp.remote.core.IRemoteServices;
 import org.eclipse.ptp.remote.core.RemoteProcessAdapter;
 import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
 
 /**
  * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
  * this API will work or that it will remain the same. Please do not use this API without consulting with the RDT team.
  * 
  * @author crecoskie
  * @author Jeff Overbey
  */
 // TODO (Jeff): Remove/replace NON_ESCAPED_ASCII_CHARS, static initializer, and escape(String) after Bug 371691 is fixed
 public class SyncCommandLauncher implements ICommandLauncher {
 
 	/** ASCII characters that do <i>not</i> need to be escaped on a Bash command line */
 	private static final Set<Character> NON_ESCAPED_ASCII_CHARS;
 
 	static {
 		NON_ESCAPED_ASCII_CHARS = new HashSet<Character>();
 		CharacterIterator it = new StringCharacterIterator("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/._-"); //$NON-NLS-1$
 		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
 			NON_ESCAPED_ASCII_CHARS.add(c);
 		}
 	}
 
 	protected IProject fProject;
 
 	protected Process fProcess;
 	protected IRemoteProcess fRemoteProcess;
 	protected boolean fShowCommand;
 	protected String[] fCommandArgs;
 	protected String lineSeparator = "\r\n"; //$NON-NLS-1$
 	protected String fErrorMessage;
 
 	protected Map<String, String> remoteEnvMap;
 
 	private boolean isCleanBuild;
 
 	/**
 	 * The number of milliseconds to pause between polling.
 	 */
 	protected static final long DELAY = 50L;
 
 	/**
 	 * 
 	 */
 	public SyncCommandLauncher() {
 	}
 
 	private boolean isCleanBuild(String[] args) {
 		for (int i = 0; i < args.length; i++) {
 			if (IBuilder.DEFAULT_TARGET_CLEAN.equals(args[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#execute(org.eclipse.core.runtime.IPath, java.lang.String[], java.lang.String[],
 	 * org.eclipse.core.runtime.IPath)
 	 */
 	@Override
 	public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, final IProgressMonitor monitor)
 			throws CoreException {
 		isCleanBuild = isCleanBuild(args);
 		IndexBuildSequenceController projectStatus = IndexBuildSequenceController.getIndexBuildSequenceController(getProject());
 
 		if (projectStatus != null) {
 			projectStatus.setRuntimeBuildStatus(null);
 		}
 
 		// if there is no project associated to us then we cannot function... throw an exception
 		if (getProject() == null) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					"org.eclipse.ptp.rdt.core", "RemoteCommandLauncher has not been associated with a project.")); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		// Set correct directory
 		// For managed projects and configurations other than workspace, the directory is incorrect and needs to be fixed.
 		IConfiguration configuration = ManagedBuildManager.getBuildInfo(getProject()).getDefaultConfiguration();
 		String projectLocalRoot = getProject().getLocation().toPortableString();
 		String projectActualRoot = BuildConfigurationManager.getInstance().getBuildScenarioForBuildConfiguration(configuration)
 				.getLocation(getProject());
 		changeToDirectory = new Path(changeToDirectory.toString().replaceFirst(projectLocalRoot, projectActualRoot));
 		fCommandArgs = constructCommandArray(commandPath.toPortableString(), args);
 
 		// Get and setup the connection and remote services for this build configuration.
 		BuildScenario bs = BuildConfigurationManager.getInstance().getBuildScenarioForBuildConfiguration(configuration);
 		if (bs == null) {
 			return null;
 		}
 		IRemoteConnection connection;
 		try {
 			connection = bs.getRemoteConnection();
 		} catch (MissingConnectionException e2) {
             throw new CoreException(new Status(IStatus.CANCEL,
                     "org.eclipse.ptp.rdt.sync.core", "Build canceled because connection does not exist")); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (!connection.isOpen()) {
 			try {
 				connection.open(monitor);
 			} catch (RemoteConnectionException e1) {
 				// rethrow as CoreException
 				throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.ptp.rdt.core", "Error opening connection.", e1)); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		IRemoteServices remoteServices = connection.getRemoteServices();
 		if (remoteServices == null) {
 			return null;
 		}
 		if (!remoteServices.isInitialized()) {
 			remoteServices.initialize();
 		}
 
 		// Set process's command and environment
 		List<String> command = constructCommand(commandPath, args, connection, monitor);
 
 		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, command);
 
 		remoteEnvMap = processBuilder.environment();
 
 		for (String envVar : env) {
			String[] splitStr = envVar.split("=", 2); //$NON-NLS-1$
 			if (splitStr.length > 1) {
 				remoteEnvMap.put(splitStr[0], splitStr[1]);
 			} else if (splitStr.length == 1) {
 				// Empty environment variable
 				remoteEnvMap.put(splitStr[0], ""); //$NON-NLS-1$
 			}
 		}
 
 		// set the directory in which to run the command
 		IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
 		if (changeToDirectory != null && fileManager != null) {
 			processBuilder.directory(fileManager.getResource(changeToDirectory.toString()));
 		}
 
 		// Synchronize before building
 		SyncManager.syncBlocking(null, getProject(), SyncFlag.FORCE, new SubProgressMonitor(monitor, 10), null);
 
 		IRemoteProcess p = null;
 		try {
 			p = processBuilder.start();
 		} catch (IOException e) {
 			if (projectStatus != null) {
 				projectStatus.setRuntimeBuildStatus(IndexBuildSequenceController.STATUS_INCOMPLETE);
 			}
 			// rethrow as CoreException
 			throw new CoreException(new Status(IStatus.ERROR,
 					"org.eclipse.ptp.rdt.sync.core", "Error launching remote process.", e)); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		if (projectStatus != null) {
 			if (!isCleanBuild) {
 				projectStatus.setBuildRunning();
 			}
 		}
 
 		fRemoteProcess = p;
 		fProcess = new RemoteProcessAdapter(p);
 		return fProcess;
 	}
 
 	private List<String> constructCommand(IPath commandPath, String[] args, IRemoteConnection connection, IProgressMonitor monitor) throws CoreException {
 		/*
 		 * Prior to Modules/SoftEnv support, this was the following:
 		 * 
 		 * List<String> command = new LinkedList<String>();
 		 * command.add(commandPath.toString());
 		 * for (int k = 0; k < args.length; k++) {
 		 *     command.add(args[k]);
 		 * }
 		 */
 
 		final EnvManagerProjectProperties projectProperties = new EnvManagerProjectProperties(getProject());
 		if (projectProperties.isEnvMgmtEnabled()) {
 			// Environment management is enabled for the build.  Issue custom Modules/SoftEnv commands to configure the environment.
 			IEnvManager envManager = EnvManagerRegistry.getEnvManager(monitor, connection);
 			try {
 				// Create and execute a Bash script which will configure the environment and then execute the command
 				final List<String> command = new LinkedList<String>();
 				command.add("bash"); //$NON-NLS-1$
 				command.add("-l"); //$NON-NLS-1$
 				final String bashScriptFilename = envManager.createBashScript(monitor, true, projectProperties, getCommandAsString(commandPath, args));
 				command.add(bashScriptFilename);
 				return command;
 			} catch (final Exception e) {
 				// An error occurred creating the Bash script, so attempt to put the whole thing onto the command line
 				RDTSyncCorePlugin.log("Error creating bash script for launch; reverting to bash -l -c", e); //$NON-NLS-1$
 				final List<String> command = new LinkedList<String>();
 				command.add("bash"); //$NON-NLS-1$
 				command.add("-l"); //$NON-NLS-1$
 				command.add("-c"); //$NON-NLS-1$
 				final String bashCommand = envManager.getBashConcatenation("; ", true, projectProperties, getCommandAsString(commandPath, args)); //$NON-NLS-1$
 				command.add(bashCommand);
 				return command;
 			}
 		} else {
 			// Environment management disabled.  Execute the build command in a login shell (so the default environment is configured).
 			final List<String> command = new LinkedList<String>();
 			command.add("bash"); //$NON-NLS-1$
 			command.add("-l"); //$NON-NLS-1$
 			command.add("-c"); //$NON-NLS-1$
 			command.add(getCommandAsString(commandPath, args));
 			return command;
 		}
 	}
 
 	private static String getCommandAsString(IPath commandPath, String[] args) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append(escape(commandPath.toOSString()));
 		sb.append(' ');
 		for (int k = 0; k < args.length; k++) {
 			sb.append(escape(args[k]));
 			sb.append(' ');
 		}
 		return sb.toString();
 	}
 
 	// See RemoteToolsProcessBuilder ctor and #charEscapify(String, Set<String>)
 	private static String escape(String inputString) {
 		if (inputString == null) {
 			return null;
 		}
 
 		final StringBuilder newString = new StringBuilder(inputString.length()+16);
 		final CharacterIterator it = new StringCharacterIterator(inputString);
 		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
 			if (c == '\'') {
 				newString.append("'\\\\\\''"); //$NON-NLS-1$
 			} else if (c > 127 || NON_ESCAPED_ASCII_CHARS.contains(c)) { // Do not escape non-ASCII characters (> 127)
 				newString.append(c);
 			} else {
 				newString.append("\\" + c); //$NON-NLS-1$
 			}
 		}
 		return newString.toString();
 	}
 
 	private String getCommandLine(String[] commandArgs) {
 
 		if (fProject == null) {
 			return null;
 		}
 
 		StringBuffer buf = new StringBuffer();
 		if (fCommandArgs != null) {
 			for (String commandArg : commandArgs) {
 				buf.append(commandArg);
 				buf.append(' ');
 			}
 			buf.append(lineSeparator);
 		}
 		return buf.toString();
 	}
 
 	/**
 	 * Constructs a command array that will be passed to the process
 	 */
 	protected String[] constructCommandArray(String command, String[] commandArgs) {
 		String[] args = new String[1 + commandArgs.length];
 		args[0] = command;
 		System.arraycopy(commandArgs, 0, args, 1, commandArgs.length);
 		return args;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#getCommandLine()
 	 */
 	@Override
 	public String getCommandLine() {
 		return getCommandLine(getCommandArgs());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#getCommandArgs()
 	 */
 	@Override
 	public String[] getCommandArgs() {
 		return fCommandArgs;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#getEnvironment()
 	 */
 	@Override
 	public Properties getEnvironment() {
 		return convertEnvMapToProperties();
 	}
 
 	private Properties convertEnvMapToProperties() {
 		Properties properties = new Properties();
 
 		for (String key : remoteEnvMap.keySet()) {
 			properties.put(key, remoteEnvMap.get(key));
 		}
 
 		return properties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#getErrorMessage()
 	 */
 	@Override
 	public String getErrorMessage() {
 		return fErrorMessage;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#setErrorMessage(java.lang.String)
 	 */
 	@Override
 	public void setErrorMessage(String error) {
 		fErrorMessage = error;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#showCommand(boolean)
 	 */
 	@Override
 	public void showCommand(boolean show) {
 		fShowCommand = show;
 
 	}
 
 	protected void printCommandLine(OutputStream os) {
 		if (os != null) {
 			String cmd = getCommandLine(getCommandArgs());
 			try {
 				os.write(cmd.getBytes());
 				os.flush();
 			} catch (IOException e) {
 				// ignore;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#waitAndRead(java.io.OutputStream, java.io.OutputStream)
 	 */
 	@Override
 	public int waitAndRead(OutputStream out, OutputStream err) {
 		if (fShowCommand) {
 			printCommandLine(out);
 		}
 
 		if (fProcess == null) {
 			return ILLEGAL_COMMAND;
 		}
 
 		RemoteProcessClosure closure = new RemoteProcessClosure(fRemoteProcess, out, err);
 		closure.runBlocking(); // a blocking call
 		return OK;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.cdt.core.ICommandLauncher#waitAndRead(java.io.OutputStream, java.io.OutputStream,
 	 * org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
 		if (fShowCommand) {
 			printCommandLine(output);
 		}
 
 		if (fProcess == null) {
 			return ILLEGAL_COMMAND;
 		}
 
 		RemoteProcessClosure closure = new RemoteProcessClosure(fRemoteProcess, output, err);
 		closure.runNonBlocking();
 		while (!monitor.isCanceled() && closure.isRunning()) {
 			try {
 				Thread.sleep(DELAY);
 			} catch (InterruptedException ie) {
 				// ignore
 			}
 		}
 		
 		// Poorly named function - actually closes streams and resets variables
 		closure.isAlive();
 
 		int state = OK;
 		final IndexBuildSequenceController projectStatus = IndexBuildSequenceController
 				.getIndexBuildSequenceController(getProject());
 		// Operation canceled by the user, terminate abnormally.
 		if (monitor.isCanceled()) {
 			closure.terminate();
 			state = COMMAND_CANCELED;
 			setErrorMessage(CCorePlugin.getResourceString("CommandLauncher.error.commandCanceled")); //$NON-NLS-1$
 			if (projectStatus != null) {
 				projectStatus.setRuntimeBuildStatus(IndexBuildSequenceController.STATUS_INCOMPLETE);
 			}
 		}
 
 		try {
 			fProcess.waitFor();
 		} catch (InterruptedException e) {
 			// ignore
 		}
 
 		try {
 			// Do not allow the cancel of the refresh, since the
 			// builder is external
 			// to Eclipse, files may have been created/modified
 			// and we will be out-of-sync.
 			// The caveat is that for huge projects, it may take a while
 			getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (CoreException e) {
 			// this should never happen because we should never be building from a
 			// state where ressource changes are disallowed
 		}
 
 		if (projectStatus != null) {
 			if (isCleanBuild) {
 
 				projectStatus.setBuildInCompletedForCleanBuild();
 
 			} else {
 
 				if (projectStatus.isIndexAfterBuildSet()) {
 
 					projectStatus.invokeIndex();
 				} else {
 					projectStatus.setFinalBuildStatus();
 				}
 
 			}
 		}
 
 		return state;
 	}
 
 	@Override
 	public IProject getProject() {
 		return fProject;
 	}
 
 	@Override
 	public void setProject(IProject project) {
 		fProject = project;
 	}
 
 }
