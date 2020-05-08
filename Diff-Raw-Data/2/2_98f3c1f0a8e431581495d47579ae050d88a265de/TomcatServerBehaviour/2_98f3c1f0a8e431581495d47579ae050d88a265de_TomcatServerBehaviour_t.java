 /**********************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
  *    IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.jst.server.tomcat.core.internal;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.*;
 import org.eclipse.debug.core.*;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.osgi.util.NLS;
 
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.model.*;
 import org.eclipse.wst.server.core.util.SocketUtil;
 /**
  * Generic Tomcat server.
  */
 public class TomcatServerBehaviour extends ServerBehaviourDelegate implements ITomcatServerBehaviour {
 	private static final String ATTR_STOP = "stop-server";
 
 	// the thread used to ping the server to check for startup
 	protected transient PingThread ping = null;
 	protected transient IProcess process;
 	protected transient IDebugEventSetListener processListener;
 
 	/**
 	 * TomcatServer.
 	 */
 	public TomcatServerBehaviour() {
 		super();
 	}
 
 	public void initialize(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	public TomcatRuntime getTomcatRuntime() {
 		if (getServer().getRuntime() == null)
 			return null;
 		
 		return (TomcatRuntime) getServer().getRuntime().loadAdapter(TomcatRuntime.class, null);
 	}
 	
 	public ITomcatVersionHandler getTomcatVersionHandler() {
 		if (getServer().getRuntime() == null || getTomcatRuntime() == null)
 			return null;
 
 		return getTomcatRuntime().getVersionHandler();
 	}
 	
 	public TomcatConfiguration getTomcatConfiguration() throws CoreException {
 		return getTomcatServer().getTomcatConfiguration();
 	}
 
 	public TomcatServer getTomcatServer() {
 		return (TomcatServer) getServer().getAdapter(TomcatServer.class);
 	}
 
 	/**
 	 * Return the runtime class name.
 	 *
 	 * @return the class name
 	 */
 	public String getRuntimeClass() {
 		return getTomcatVersionHandler().getRuntimeClass();
 	}
 	
 	/**
 	 * Returns the runtime base path for relative paths in the server
 	 * configuration.
 	 * 
 	 * @return the base path
 	 */
 	public IPath getRuntimeBaseDirectory() {
 		return getTomcatVersionHandler().getRuntimeBaseDirectory(this);
 	}
 
 	/**
 	 * Return the program's runtime arguments to start or stop.
 	 *
 	 * @param starting true if starting
 	 * @return an array of runtime program arguments
 	 */
 	protected String[] getRuntimeProgramArguments(boolean starting) {
 		IPath configPath = null;
 		if (getTomcatServer().isTestEnvironment())
 			configPath = getTempDirectory();
 		return getTomcatVersionHandler().getRuntimeProgramArguments(configPath, getTomcatServer().isDebug(), starting);
 	}
 
 	/**
 	 * Return the runtime (VM) arguments.
 	 *
 	 * @return an array of runtime arguments
 	 */
 	protected String[] getRuntimeVMArguments() {
 		IPath installPath = getServer().getRuntime().getLocation();
 		IPath configPath = null;
 		if (getTomcatServer().isTestEnvironment())
 			configPath = getTempDirectory();
 		else
 			configPath = installPath;
 		return getTomcatVersionHandler().getRuntimeVMArguments(installPath, configPath,
 				getTomcatServer().isTestEnvironment(), getTomcatServer().isSecure());
 	}
 	
 	protected static String renderCommandLine(String[] commandLine, String separator) {
 		if (commandLine == null || commandLine.length < 1)
 			return "";
 		StringBuffer buf= new StringBuffer(commandLine[0]);
 		for (int i = 1; i < commandLine.length; i++) {
 			buf.append(separator);
 			buf.append(commandLine[i]);
 		}	
 		return buf.toString();
 	}
 
 	public void setProcess(final IProcess newProcess) {
 		if (process != null)
 			return;
 
 		process = newProcess;
 		if (processListener != null)
 			DebugPlugin.getDefault().removeDebugEventListener(processListener);
 		if (newProcess == null)
 			return;
 		
 		processListener = new IDebugEventSetListener() {
 			public void handleDebugEvents(DebugEvent[] events) {
 				if (events != null) {
 					int size = events.length;
 					for (int i = 0; i < size; i++) {
 						if (process != null && process.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
 							DebugPlugin.getDefault().removeDebugEventListener(this);
 							stopImpl();
 						}
 					}
 				}
 			}
 		};
 		DebugPlugin.getDefault().addDebugEventListener(processListener);
 	}
 	
 	protected void setServerStarted() {
 		setServerState(IServer.STATE_STARTED);
 	}
 
 	protected void stopImpl() {
 		if (ping != null) {
 			ping.stop();
 			ping = null;
 		}
 		if (process != null) {
 			process = null;
 			DebugPlugin.getDefault().removeDebugEventListener(processListener);
 			processListener = null;
 		}
 		setServerState(IServer.STATE_STOPPED);
 	}
 
 	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
 		IPath installDir = getServer().getRuntime().getLocation();
 		IPath confDir = null;
 		if (getTomcatServer().isTestEnvironment()) {
 			confDir = getTempDirectory();
 			IStatus status = getTomcatConfiguration().prepareRuntimeDirectory(confDir);
 			if (status != null && !status.isOK())
 				throw new CoreException(status);
 /*			File temp = confDir.append("conf").toFile();
 			if (!temp.exists())
 				temp.mkdirs();*/
 		} else
 			confDir = installDir;
 
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		monitor.beginTask(Messages.publishServerTask, 500);
 		
 		IStatus status = getTomcatConfiguration().cleanupServer(confDir, installDir, ProgressUtil.getSubMonitorFor(monitor, 100));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		status = getTomcatConfiguration().backupAndPublish(confDir, !getTomcatServer().isTestEnvironment(), ProgressUtil.getSubMonitorFor(monitor, 400));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		monitor.done();
 		
 		setServerPublishState(IServer.PUBLISH_STATE_NONE);
 	}
 
 	/*
 	 * Publishes the given module to the server.
 	 */
 	protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor) throws CoreException {
 		if (getTomcatServer().isTestEnvironment())
 			return;
 
 		IPath path = getTempDirectory().append("publish.txt");
 		Properties p = new Properties();
 		try {
 			p.load(new FileInputStream(path.toFile()));
 		} catch (Exception e) {
 			// ignore
 		}
 		
 		IModule module = moduleTree[0];
 		
 		if (deltaKind == REMOVED) {
 			try {
 				String publishPath = (String) p.get(module.getId());
 				FileUtil.deleteDirectory(new File(publishPath), monitor);
 			} catch (Exception e) {
 				throw new CoreException(new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, "Could not remove module", e));
 			}
 		} else {
 			IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class, null);
 			IPath from = webModule.getLocation();
 			IPath to = getServer().getRuntime().getLocation().append("webapps").append(webModule.getContextRoot());
 			FileUtil.smartCopyDirectory(from.toOSString(), to.toOSString(), monitor);
 			p.put(module.getId(), to.toOSString());
 			setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);
 		}
 		
 		try {
 			p.store(new FileOutputStream(path.toFile()), "Tomcat publish data");
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	/**
 	 * Setup for starting the server.
 	 * 
 	 * @param launch ILaunch
 	 * @param launchMode String
 	 * @param monitor IProgressMonitor
 	 * @throws CoreException if anything goes wrong
 	 */
 	public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
 		if ("true".equals(launch.getLaunchConfiguration().getAttribute(ATTR_STOP, "false")))
 			return;
 		//if (getTomcatRuntime() == null)
 		//	throw new CoreException();
 		
 		IStatus status = getTomcatRuntime().validate();
 		if (status != null && status.getSeverity() == IStatus.ERROR)
 			throw new CoreException(status);
 
 		//setRestartNeeded(false);
 		TomcatConfiguration configuration = getTomcatConfiguration();
 	
 		// check that ports are free
 		Iterator iterator = configuration.getServerPorts().iterator();
 		List usedPorts = new ArrayList();
 		while (iterator.hasNext()) {
 			ServerPort sp = (ServerPort) iterator.next();
 			if (sp.getPort() < 0)
 				throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorPortInvalid, null));
 			if (SocketUtil.isPortInUse(sp.getPort(), 5)) {
 				usedPorts.add(sp);
 			}
 		}
 		if (usedPorts.size() == 1) {
 			ServerPort port = (ServerPort) usedPorts.get(0);
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorPortInUse, new String[] {port.getPort() + "", getServer().getName()}), null));
 		} else if (usedPorts.size() > 1) {
 			String portStr = "";
 			iterator = usedPorts.iterator();
 			boolean first = true;
 			while (iterator.hasNext()) {
 				if (!first)
 					portStr += ", ";
 				first = false;
 				ServerPort sp = (ServerPort) iterator.next();
 				portStr += "" + sp.getPort();
 			}
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorPortsInUse, new String[] {portStr, getServer().getName()}), null));
 		}
 		
 		// check that there is only one app for each context root
 		iterator = configuration.getWebModules().iterator();
 		List contextRoots = new ArrayList();
 		while (iterator.hasNext()) {
 			WebModule module = (WebModule) iterator.next();
 			String contextRoot = module.getPath();
 			if (contextRoots.contains(contextRoot))
 				throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDuplicateContextRoot, new String[] { contextRoot }), null));
 			
 			contextRoots.add(contextRoot);
 		}
 		
 		setServerState(IServer.STATE_STARTING);
 		setMode(launchMode);
 	
 		// ping server to check for startup
 		try {
 			String url = "http://localhost";
 			int port = configuration.getMainPort().getPort();
 			if (port != 80)
 				url += ":" + port;
 			ping = new PingThread(getServer(), url, 50, this);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Can't ping for Tomcat startup.");
 		}
 	}
 
 	/**
 	 * Cleanly shuts down and terminates the server.
 	 * 
 	 * @param force <code>true</code> to kill the server
 	 */
 	public void stop(boolean force) {
 		if (force) {
 			terminate();
 			return;
 		}
 		int state = getServer().getServerState();
 		if (state == IServer.STATE_STOPPED)
 			return;
 		else if (state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING) {
 			terminate();
 			return;
 		}
 
 		try {
 			Trace.trace(Trace.FINER, "Stopping Tomcat");
 			if (state != IServer.STATE_STOPPED)
 				setServerState(IServer.STATE_STOPPING);
 	
 			ILaunchConfiguration launchConfig = ((Server)getServer()).getLaunchConfiguration(true, null);
 			ILaunchConfigurationWorkingCopy wc = launchConfig.getWorkingCopy();
 			
 			String args = renderCommandLine(getRuntimeProgramArguments(false), " ");
 			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
 			wc.setAttribute(ATTR_STOP, "true");
 			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error stopping Tomcat", e);
 		}
 	}
 
 	/**
 	 * Terminates the server.
 	 */
 	protected void terminate() {
 		if (getServer().getServerState() == IServer.STATE_STOPPED)
 			return;
 
 		try {
 			setServerState(IServer.STATE_STOPPING);
 			Trace.trace(Trace.FINER, "Killing the Tomcat process");
 			if (process != null && !process.isTerminated()) {
 				process.terminate();
 				stopImpl();
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error killing the process", e);
 		}
 	}
 
 	public IPath getTempDirectory() {
 		return super.getTempDirectory();
 	}
 
 	/**
 	 * Return a string representation of this object.
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return "TomcatServer";
 	}
 	
 	protected static int getNextToken(String s, int start) {
 		int i = start;
 		int length = s.length();
 		char lookFor = ' ';
 		
 		while (i < length) {
 			char c = s.charAt(i);
 			if (lookFor == c) {
 				if (lookFor == '"')
 					return i+1;
 				return i;
 			}
 			if (c == '"')
 				lookFor = '"';
 			i++;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Merge the given arguments into the original argument string, replacing
 	 * invalid values if they have been changed.
 	 * 
 	 * @param originalArg
 	 * @param vmArgs
 	 * @return merged argument string
 	 */
 	public static String mergeArguments(String originalArg, String[] vmArgs) {
 		if (vmArgs == null)
 			return originalArg;
 		
 		if (originalArg == null)
 			originalArg = "";
 		
 		// replace and null out all vmargs that already exist
 		int size = vmArgs.length;
 		for (int i = 0; i < size; i++) {
 			int ind = vmArgs[i].indexOf(" ");
 			int ind2 = vmArgs[i].indexOf("=");
 			if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
 				int index = originalArg.indexOf(vmArgs[i].substring(0, ind + 1));
 				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 					// replace
 					String s = originalArg.substring(0, index);
 					int index2 = getNextToken(originalArg, index + ind + 1);
 					if (index2 >= 0)
 						originalArg = s + vmArgs[i] + originalArg.substring(index2);
 					else
 						originalArg = s + vmArgs[i];
 					vmArgs[i] = null;
 				}
 			} else if (ind2 >= 0) { // a=b style
 				int index = originalArg.indexOf(vmArgs[i].substring(0, ind2 + 1));
 				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 					// replace
 					String s = originalArg.substring(0, index);
 					int index2 = getNextToken(originalArg, index);
 					if (index2 >= 0)
 						originalArg = s + vmArgs[i] + originalArg.substring(index2);
 					else
 						originalArg = s + vmArgs[i];
 					vmArgs[i] = null;
 				}
 			} else { // abc style
 				int index = originalArg.indexOf(vmArgs[i]);
 				if (index == 0 || (index > 0 && originalArg.charAt(index-1) == ' ')) {
 					// replace
 					String s = originalArg.substring(0, index);
 					int index2 = getNextToken(originalArg, index);
 					if (index2 >= 0)
 						originalArg = s + vmArgs[i] + originalArg.substring(index2);
 					else
 						originalArg = s + vmArgs[i];
 					vmArgs[i] = null;
 				}
 			}
 		}
 		
 		// add remaining vmargs to the end
 		for (int i = 0; i < size; i++) {
 			if (vmArgs[i] != null) {
 				if (originalArg.length() > 0 && !originalArg.endsWith(" "))
 					originalArg += " ";
 				originalArg += vmArgs[i];
 			}
 		}
 		
 		return originalArg;
 	}
 
 	/**
 	 * Replace the current JRE container classpath with the given entry.
 	 * 
 	 * @param cp
 	 * @param entry
 	 */
 	public static void replaceJREContainer(List cp, IRuntimeClasspathEntry entry) {
 		int size = cp.size();
 		for (int i = 0; i < size; i++) {
 			IRuntimeClasspathEntry entry2 = (IRuntimeClasspathEntry) cp.get(i);
 			if (entry2.getPath().uptoSegment(2).isPrefixOf(entry.getPath())) {
 				cp.set(i, entry);
 				return;
 			}
 		}
 		
 		cp.add(0, entry);
 	}
 
 	/**
 	 * Merge a single classpath entry into the classpath list.
 	 * 
 	 * @param cp
 	 * @param entry
 	 */
 	public static void mergeClasspath(List cp, IRuntimeClasspathEntry entry) {
 		Iterator iterator = cp.iterator();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry2 = (IRuntimeClasspathEntry) iterator.next();
 			
 			if (entry2.getPath().equals(entry.getPath()))
 				return;
 		}
 		
 		cp.add(entry);
 	}
 
 	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
 		String existingProgArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, mergeArguments(existingProgArgs, getRuntimeProgramArguments(true)));
 
 		String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
		if (existingVMArgs != null && !getTomcatServer().isSecure()) {
 			// remove -Djava.security.manager and -Djava.security.policy="x x"
 			int index = existingVMArgs.indexOf("-Djava.security.manager");
 			if (index >= 0) {
 				if (index > 0 && existingVMArgs.charAt(index - 1) == ' ')
 					index --;
 				int index2 = existingVMArgs.indexOf(" ", index + 2);
 				existingVMArgs = existingVMArgs.substring(0, index) + existingVMArgs.substring(index2);
 			}
 			index = existingVMArgs.indexOf("-Djava.security.policy=");
 			if (index >= 0) {
 				if (index > 0 && existingVMArgs.charAt(index - 1) == ' ')
 					index --;
 				int index2 = existingVMArgs.indexOf("\"", index);
 				index2 = existingVMArgs.indexOf("\"", index2 + 1);
 				existingVMArgs = existingVMArgs.substring(0, index) + existingVMArgs.substring(index2+1);
 			}
 		}
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, mergeArguments(existingVMArgs, getRuntimeVMArguments()));
 		
 		ITomcatRuntime runtime = getTomcatRuntime();
 		IVMInstall vmInstall = runtime.getVMInstall();
 		if (vmInstall != null) {
 			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmInstall.getVMInstallType().getId());
 			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmInstall.getName());
 		}
 		
 		// update classpath
 		IRuntimeClasspathEntry[] originalClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(workingCopy);
 		int size = originalClasspath.length;
 		List oldCp = new ArrayList(originalClasspath.length + 2);
 		for (int i = 0; i < size; i++)
 			oldCp.add(originalClasspath[i]);
 		
 		List cp2 = runtime.getRuntimeClasspath();
 		Iterator iterator = cp2.iterator();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator.next();
 			mergeClasspath(oldCp, entry);
 		}
 		
 		if (vmInstall != null) {
 			try {
 				String typeId = vmInstall.getVMInstallType().getId();
 				replaceJREContainer(oldCp, JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER).append(typeId).append(vmInstall.getName()), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
 			} catch (Exception e) {
 				// ignore
 			}
 			
 			IPath jrePath = new Path(vmInstall.getInstallLocation().getAbsolutePath());
 			if (jrePath != null) {
 				IPath toolsPath = jrePath.append("lib").append("tools.jar");
 				if (toolsPath.toFile().exists())
 					mergeClasspath(oldCp, JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath));
 			}
 		}
 		
 		iterator = oldCp.iterator();
 		List list = new ArrayList();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator.next();
 			try {
 				list.add(entry.getMemento());
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Could not resolve classpath entry: " + entry, e);
 			}
 		}
 		
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, list);
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
 	}
 	
 	protected IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
 		return super.getPublishedResourceDelta(module);
 	}
 }
