 /*******************************************************************************
  * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc. and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *     SAP AG - moving to Eclipse Libra project and enhancements
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IDebugEventSetListener;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.libra.framework.editor.core.IOSGiFrameworkAdmin;
 import org.eclipse.libra.framework.editor.core.IOSGiFrameworkConsole;
 import org.eclipse.libra.framework.editor.core.model.IBundle;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IOConsoleOutputStream;
 import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
 import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.IServerDeployer;
 import org.eclipse.virgo.ide.runtime.core.IServerRuntime;
 import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
 import org.eclipse.virgo.ide.runtime.core.ServerUtils;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.model.IModuleResourceDelta;
 import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
 
 /**
  * Default dm server behavior.
  * @author Christian Dupuis
  * @author Kaloyan Raev
  * @since 1.0.0
  */
 @SuppressWarnings("restriction")
 public class ServerBehaviour extends ServerBehaviourDelegate implements IServerBehaviour, IOSGiFrameworkAdmin, IOSGiFrameworkConsole {
 
 	private final static String WEB_CONTEXT_PATH_MANIFEST_HEADER = "Web-ContextPath";
 
 	protected Map<String, DeploymentIdentity> deploymentIdentities = new ConcurrentHashMap<String, DeploymentIdentity>();
 
 	protected transient ILaunch launch;
 
 	protected transient ServerStatusPingThread pingThread;
 
 	protected transient IDebugEventSetListener processListener;
 
 	private transient ProcessConsole processConsole;
 
 	private transient IServerDeployer serverDeployer;
 
 	private transient List<ServerLogTail> traceTailJobs = Collections.synchronizedList(new ArrayList<ServerLogTail>());
 
 	private transient Map<String, Long> traceFileSizes = new ConcurrentHashMap<String, Long>();
 
 	public IStatus cleanServerWorkDir(IProgressMonitor monitor) throws CoreException {
 		String path = getServerDeployDirectory().toString();
 		File file = new File(path);
 		file.delete();
 		return Status.OK_STATUS;
 	}
 
 	public IPath getModuleDeployDirectory(IModule module) {
 		return ServerUtils.getServer(this).getModuleDeployDirectory(module);
 	}
 
 	public IPath getModuleDeployUri(IModule module) {
 		return getModuleDeployDirectory(module);
 	}
 
 	public IPath getRuntimeBaseDirectory() {
 		return ServerUtils.getServer(this).getRuntimeBaseDirectory();
 	}
 
 	public IPath getServerDeployDirectory() {
 		return ServerUtils.getServer(this).getServerDeployDirectory();
 	}
 
 	public IServerVersionHandler getVersionHandler() {
 		return ServerUtils.getServer(this).getVersionHandler();
 	}
 
 	@Override
 	public void handleResourceChange() {
 		if (getServer().getServerRestartState()) {
 			return;
 		}
 
 		Iterator<IModule[]> iterator = getAllModules().iterator();
 		while (iterator.hasNext()) {
 			IModule[] module = (IModule[]) iterator.next();
 			IModuleResourceDelta[] delta = getPublishedResourceDelta(module);
 			if (delta == null || delta.length == 0) {
 				continue;
 			}
 		}
 	}
 
 	public void onModulePublishStateChange(IModule[] module, int state) {
 		setModulePublishState(module, state);
 	}
 
 	public void onModuleStateChange(IModule[] module, int state) {
 		setModuleState(module, state);
 	}
 
 	public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
 		IStatus status = ServerUtils.getServerRuntime(this).validate();
 		if (status != null && status.getSeverity() == IStatus.ERROR) {
 			throw new CoreException(status);
 		}
 		
 		getVersionHandler().preStartup(this);
 		
 		setServerRestartState(false);
 		setServerState(IServer.STATE_STARTING);
 		setMode(launchMode);
 		this.launch = launch;
 		this.pingThread = new ServerStatusPingThread(this);
 	}
 
 	@Override
 	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
 			throws CoreException {
 
 		String existingProgArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
 				(String) null);
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, LaunchArgumentUtils
 				.mergeArguments(existingProgArgs, getRuntimeProgramArguments(),
 						getExcludedRuntimeProgramArguments(true), true));
 
 		String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
 				(String) null);
 		String[] configVMArgs = getRuntimeVMArguments();
 
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, LaunchArgumentUtils
 				.mergeArguments(existingVMArgs, configVMArgs, null, false));
 
 		IServerRuntime runtime = ServerUtils.getServerRuntime(this);
 		IVMInstall vmInstall = runtime.getVMInstall();
 		if (vmInstall != null) {
 			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime
 					.newJREContainerPath(vmInstall).toPortableString());
 		}
 
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, getRuntimeBaseDirectory()
 				.toOSString());
 
 		IRuntimeClasspathEntry[] originalClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(workingCopy);
 		int size = originalClasspath.length;
 		List<IRuntimeClasspathEntry> oldCp = new ArrayList<IRuntimeClasspathEntry>(originalClasspath.length + 2);
 		for (int i = 0; i < size; i++) {
 			oldCp.add(originalClasspath[i]);
 		}
 
 		List<IRuntimeClasspathEntry> cp2 = runtime.getRuntimeClasspath();
 		Iterator<IRuntimeClasspathEntry> iterator = cp2.iterator();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry = iterator.next();
 			LaunchArgumentUtils.mergeClasspath(oldCp, entry);
 		}
 
 		if (vmInstall != null) {
 			try {
 				String typeId = vmInstall.getVMInstallType().getId();
 				LaunchArgumentUtils.replaceJREContainer(oldCp, JavaRuntime.newRuntimeContainerClasspathEntry(new Path(
 						JavaRuntime.JRE_CONTAINER).append(typeId).append(vmInstall.getName()),
 						IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
 			}
 			catch (Exception e) {
 				// ignore
 			}
 
 			IPath jrePath = new Path(vmInstall.getInstallLocation().getAbsolutePath());
 			if (jrePath != null) {
 				IPath toolsPath = jrePath.append("lib").append("tools.jar");
 				if (toolsPath.toFile().exists()) {
 					IRuntimeClasspathEntry toolsJar = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
 					int toolsIndex;
 					for (toolsIndex = 0; toolsIndex < oldCp.size(); toolsIndex++) {
 						IRuntimeClasspathEntry entry = oldCp.get(toolsIndex);
 						if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE
 								&& entry.getPath().lastSegment().equals("tools.jar")) {
 							break;
 						}
 					}
 					if (toolsIndex < oldCp.size()) {
 						oldCp.set(toolsIndex, toolsJar);
 					}
 					else {
 						LaunchArgumentUtils.mergeClasspath(oldCp, toolsJar);
 					}
 				}
 			}
 		}
 
 		iterator = oldCp.iterator();
 		List<String> list = new ArrayList<String>();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry = iterator.next();
 			try {
 				list.add(entry.getMemento());
 			}
 			catch (Exception e) {
 			}
 		}
 
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, list);
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
 	}
 
 	@Override
 	public void stop(boolean force) {
 		if (force) {
 			immediateShutdown();
 		}
 		else {
 			shutdown();
 		}
 	}
 
 	public void stopServer() {
 
 		// if the server is in starting mode make sure that the state transition
 		// is allowed
 		if (getServer().getServerState() != IServer.STATE_STARTED) {
 			setServerState(IServer.STATE_STOPPING);
 		}
 
 		if (pingThread != null) {
 			pingThread.stop();
 			pingThread = null;
 		}
 		if (processListener != null) {
 			DebugPlugin.getDefault().removeDebugEventListener(processListener);
 			processListener = null;
 		}
 		if (processConsole != null && traceTailJobs.size() > 0) {
 			for (ServerLogTail job : traceTailJobs) {
 				job.stopTailing();
 			}
 			traceTailJobs.clear();
 			traceFileSizes.clear();
 			processConsole = null;
 		}
 
 		setServerState(IServer.STATE_STOPPED);
 	}
 
 	public void tail(DeploymentIdentity identity) {
 		// add setting to enable/disable tailing
 		if (processConsole != null && ServerUtils.getServer(this).shouldTailTraceFiles() && identity != null
 				&& identity.getSymbolicName() != null && identity.getVersion() != null) {
 			ServerLogTail tail = new ServerLogTail(this, identity, processConsole);
 			tail.setSystem(true);
 			tail.schedule();
 			traceTailJobs.add(tail);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "dm Server";
 	}
 
 	protected void addProcessListener(final IProcess newProcess) {
 		if (processListener != null || newProcess == null) {
 			return;
 		}
 
 		processListener = new IDebugEventSetListener() {
 			public void handleDebugEvents(DebugEvent[] events) {
 				if (events != null) {
 					int size = events.length;
 					for (int i = 0; i < size; i++) {
 						if (newProcess != null && newProcess.equals(events[i].getSource())
 								&& events[i].getKind() == DebugEvent.TERMINATE) {
 							stopServer();
 						}
 					}
 				}
 			}
 		};
 		DebugPlugin.getDefault().addDebugEventListener(processListener);
 
 		for (IConsole console : ConsolePlugin.getDefault().getConsoleManager().getConsoles()) {
 			if (console instanceof ProcessConsole && newProcess.equals(((ProcessConsole) console).getProcess())) {
 				this.processConsole = (ProcessConsole) console;
 			}
 		}
 
 		File traceDirectory = new File(getRuntimeBaseDirectory().toOSString() + File.separator + "serviceability"
 				+ File.separator + "trace");
 		if (traceDirectory.exists()) {
 			for (File applicationTraceDirectory : traceDirectory.listFiles()) {
 				if (applicationTraceDirectory.isDirectory()) {
 					File traceFile = new File(applicationTraceDirectory.getAbsolutePath() + File.separator
 							+ "trace.log");
 					if (traceFile.exists()) {
 						traceFileSizes.put(traceFile.getAbsolutePath(), traceFile.length());
 					}
 				}
 			}
 		}
 
 	}
 
 	public URL getModuleRootURL(IModule module) {
 		try {
 			// check pre condition; only dynamic web projects and java projects are allowed
 			IProject project = module.getProject();
			if (!FacetedProjectFramework.hasProjectFacet(project, FacetCorePlugin.WEB_FACET_ID)
					|| !project.hasNature(JavaCore.NATURE_ID)) {
 				return null;
 			}
 
 			String contextPath = null;
 
 			BundleManifest bundleManifest = BundleManifestCorePlugin.getBundleManifestManager().getBundleManifest(
 					JavaCore.create(project));
 			if (bundleManifest != null) {
 				Dictionary<String, String> manifest = bundleManifest.toDictionary();
 				if (manifest != null && manifest.get(WEB_CONTEXT_PATH_MANIFEST_HEADER) != null) {
 					contextPath = manifest.get(WEB_CONTEXT_PATH_MANIFEST_HEADER);
 				}
 
 			}
 			if (contextPath == null) {
 				contextPath = module.getName();
 			}
 
 			// TODO: CD make port configurable
 			int port = 8080;
 			StringBuilder urlBuilder = new StringBuilder();
 			urlBuilder.append("http://localhost:");
 			urlBuilder.append(port);
 			urlBuilder.append("/");
 			urlBuilder.append(contextPath);
 
 			String url = urlBuilder.toString();
 			if (!url.endsWith("/")) {
 				urlBuilder.append("/");
 			}
 
 			return new URL(urlBuilder.toString());
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Map<String, DeploymentIdentity> getDeploymentIdentities() {
 		return this.deploymentIdentities;
 	}
 
 	protected String[] getExcludedRuntimeProgramArguments(boolean starting) {
 		return getVersionHandler().getExcludedRuntimeProgramArguments(starting);
 	}
 
 	@Override
 	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
 		return super.getPublishedResourceDelta(module);
 	}
 
 	@Override
 	public IModuleResource[] getResources(IModule[] module) {
 		return super.getResources(module);
 	}
 
 	protected String[] getRuntimeProgramArguments() {
 		return getVersionHandler().getRuntimeProgramArguments(this);
 	}
 
 	protected String[] getRuntimeVMArguments() {
 		IPath installPath = getServer().getRuntime().getLocation();
 		IPath configPath = getRuntimeBaseDirectory();
 		IPath deployPath = getServerDeployDirectory();
 		return getVersionHandler().getRuntimeVMArguments(this, installPath, configPath, deployPath);
 	}
 
 	protected void immediateShutdown() {
 		if (getServer().getServerState() == IServer.STATE_STOPPED) {
 			return;
 		}
 
 		try {
 			setServerState(IServer.STATE_STOPPING);
 			// Revise once on WTP 3
 			// ILaunch launch = getServer().getLaunch();
 			if (launch != null) {
 				launch.terminate();
 				stopServer();
 			}
 		}
 		catch (Exception e) {
 		}
 	}
 
 	@Override
 	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
 		if (getServer().getRuntime() == null) {
 			return;
 		}
 		setServerPublishState(IServer.PUBLISH_STATE_NONE);
 	}
 
 	protected void setServerStarted() {
 		// Deploy modules
 		serverDeployer.deploy(getServer().getModules());
 
 		// Set server started
 		setServerState(IServer.STATE_STARTED);
 	}
 
 	protected void shutdown() {
 		if (getServer().getServerState() == IServer.STATE_STOPPED) {
 			return;
 		}
 
 		setServerState(IServer.STATE_STOPPING);
 		// Revise once on WTP 3
 		// ILaunch launch = getServer().getLaunch();
 		if (launch != null) {
 			try {
 				getServerDeployer().shutdown();
 			}
 			catch (TimeoutException e) {
 				immediateShutdown();
 			}
 			catch (IOException e) {
 				immediateShutdown();
 			}
 		}
 	}
 
 	public synchronized IServerDeployer getServerDeployer() {
 		if (serverDeployer == null) {
 			this.serverDeployer = new DefaultServerDeployer(this);
 		}
 		return this.serverDeployer;
 	}
 
 	public String getMBeanServerIp() {
 		return launch.getAttribute(PROPERTY_MBEAN_SERVER_IP);
 	}
 
 	class ServerLogTail extends Job {
 
 		private IOConsoleOutputStream stream;
 
 		public ServerLogTail(ServerBehaviour server, DeploymentIdentity identity, ProcessConsole console) {
 			super(server.getServer().getName());
 			this.logfile = new File(server.getRuntimeBaseDirectory().toOSString() + File.separator + "serviceability"
 					+ File.separator + "trace" + File.separator + identity.getSymbolicName() + "-"
 					+ identity.getVersion() + File.separator + "trace.log");
 			this.stream = console.newOutputStream();
 			Long fileSize = traceFileSizes.get(logfile.getAbsolutePath());
 			if (fileSize != null) {
 				filePointer = fileSize;
 			}
 		}
 
 		/** How frequently to check for file changes; defaults to 5 seconds */
 		private long sampleInterval = 1000;
 
 		/** The log file to tail */
 		private File logfile;
 
 		/** Is the tailer currently tailing? */
 		private boolean tailing = false;
 
 		/** The file pointer keeps track of where we are in the file */
 		private long filePointer = 0;
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			this.tailing = true;
 			while (this.tailing && !logfile.exists()) {
 				try {
 					Thread.sleep(sampleInterval);
 				}
 				catch (InterruptedException e) {
 				}
 			}
 
 			try {
 				// Start tailing
 				RandomAccessFile file = new RandomAccessFile(logfile, "r");
 				while (this.tailing) {
 					try {
 						// Compare the length of the file to the file pointer
 						long fileLength = this.logfile.length();
 						if (fileLength < filePointer) {
 							// Log file must have been rotated or deleted;
 							// reopen the file and reset the file pointer
 							file = new RandomAccessFile(logfile, "r");
 							filePointer = 0;
 						}
 
 						if (fileLength > filePointer) {
 							// There is data to read
 							file.seek(filePointer);
 							byte[] line = new byte[1024];
 							int length = file.read(line);
 							while (length > 0) {
 								stream.write(line, 0, length);
 								line = new byte[1024];
 								length = file.read(line);
 							}
 							filePointer = file.getFilePointer();
 						}
 
 						// Sleep for the specified interval
 						Thread.sleep(this.sampleInterval);
 					}
 					catch (Exception e) {
 					}
 				}
 
 				// Close the file that we are tailing
 				file.close();
 			}
 			catch (Exception e) {
 
 			}
 
 			return Status.OK_STATUS;
 		}
 
 		public void stopTailing() {
 			this.tailing = false;
 		}
 	}
 
 	public Map<Long, IBundle> getBundles(IProgressMonitor monitor) throws CoreException {
 		try {
 			return getVersionHandler().getServerBundleAdminCommand(this).execute();
 		} catch (IOException e) {
 		} catch (TimeoutException e) {
 		}
 		return Collections.emptyMap();
 	}
 
 	public void startBundle(long bundleId) throws CoreException {
 		executeCommand("start " + bundleId);
 	}
 
 	public void stopBundle(long bundleId) throws CoreException {
 		executeCommand("stop " + bundleId);
 	}
 
 	public void refreshBundle(long bundleId) throws CoreException {
 		executeCommand("refresh " + bundleId);
 	}
 
 	public void updateBundle(long bundleId) throws CoreException {
 		executeCommand("update " + bundleId);
 	}
 
 	public String executeCommand(String command) throws CoreException {
 		try {
 			return getVersionHandler().getServerBundleAdminExecuteCommand(this, command).execute();
 		} catch (IOException e) {
 		} catch (TimeoutException e) {
 		}
 		return "<error>";
 	}
 
 }
