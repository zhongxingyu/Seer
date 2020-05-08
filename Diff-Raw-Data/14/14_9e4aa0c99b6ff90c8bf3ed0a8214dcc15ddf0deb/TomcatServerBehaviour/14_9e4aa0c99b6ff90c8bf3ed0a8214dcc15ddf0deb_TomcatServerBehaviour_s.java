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
 import org.eclipse.jst.server.core.PublishUtil;
 import org.eclipse.osgi.util.NLS;
 
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.IModulePublishHelper;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.model.*;
 import org.eclipse.wst.server.core.util.SocketUtil;
 /**
  * Generic Tomcat server.
  */
 public class TomcatServerBehaviour extends ServerBehaviourDelegate implements ITomcatServerBehaviour, IModulePublishHelper {
 	private static final String ATTR_STOP = "stop-server";
 
 	// the thread used to ping the server to check for startup
 	protected transient PingThread ping = null;
 	protected transient IProcess process;
 	protected transient IDebugEventSetListener processListener;
 
 	/**
 	 * TomcatServerBehaviour.
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
 		return (TomcatServer) getServer().loadAdapter(TomcatServer.class, null);
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
 
 	protected String[] getExcludedRuntimeProgramArguments(boolean starting) {
 		return getTomcatVersionHandler().getExcludedRuntimeProgramArguments(getTomcatServer().isDebug(), starting);
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
 				getTomcatServer().isTestEnvironment());
 	}
 	
 	protected String getRuntimePolicyFile() {
 		IPath configPath;
 		if (getTomcatServer().isTestEnvironment())
 			configPath = getTempDirectory();
 		else
 			configPath = getServer().getRuntime().getLocation();
 		return getTomcatVersionHandler().getRuntimePolicyFile(configPath);
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
 		if (getServer().getRuntime() == null)
 			return;
 
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
 		monitor.beginTask(Messages.publishServerTask, 600);
 		
 		IStatus status = getTomcatConfiguration().cleanupServer(confDir, installDir, ProgressUtil.getSubMonitorFor(monitor, 100));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		status = getTomcatConfiguration().backupAndPublish(confDir, !getTomcatServer().isTestEnvironment(), ProgressUtil.getSubMonitorFor(monitor, 400));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		getTomcatConfiguration().localizeConfiguration(confDir.append("conf"), getTomcatServer(), ProgressUtil.getSubMonitorFor(monitor, 100));
 		
 		monitor.done();
 		
 		setServerPublishState(IServer.PUBLISH_STATE_NONE);
 	}
 
 	/*
 	 * Publishes the given module to the server.
 	 */
 	protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor) throws CoreException {
 		if (getServer().getServerState() != IServer.STATE_STOPPED) {
 			if (deltaKind == ServerBehaviourDelegate.ADDED || deltaKind == ServerBehaviourDelegate.REMOVED)
 				setServerRestartState(true);
 		}
 		if (getTomcatServer().isTestEnvironment())
 			return;
 
 		IPath path = getTempDirectory().append("publish.txt");
 		Properties p = new Properties();
 		FileInputStream fin = null;
 		try {
 			fin = new FileInputStream(path.toFile());
 			p.load(fin);
 		} catch (Exception e) {
 			// ignore
 		} finally {
 			try {
 				fin.close();
 			} catch (Exception ex) {
 				// ignore
 			}
 		}
 		
 		if (moduleTree.length == 1) // web module
 			publishDir(deltaKind, p, moduleTree, monitor);
 		else // utility jar
 			publishJar(kind, deltaKind, p, moduleTree, monitor);
 		
 		setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);
 		
 		try {
 			p.store(new FileOutputStream(path.toFile()), "Tomcat publish data");
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	/**
 	 * Publish a web module.
 	 * 
 	 * @param deltaKind
 	 * @param p
 	 * @param module
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void publishDir(int deltaKind, Properties p, IModule module[], IProgressMonitor monitor) throws CoreException {
 		List status = new ArrayList();
 		if (deltaKind == REMOVED) {
 			try {
 				String publishPath = (String) p.get(module[0].getId());
 				File f = new File(publishPath);
 				if (f.exists()) {
 					IStatus[] stat = PublishUtil.deleteDirectory(f, monitor);
 					PublishOperation2.addArrayToList(status, stat);
 				}
 			} catch (Exception e) {
 				throw new CoreException(new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, "Could not remove module", e));
 			}
 		} else {
 			IPath to = getServer().getRuntime().getLocation().append("webapps").append(module[0].getName());
 			IModuleResource[] mr = getResources(module);
 			IStatus[] stat = PublishUtil.publishSmart(mr, to, monitor);
 			PublishOperation2.addArrayToList(status, stat);
 			p.put(module[0].getId(), to.toOSString());
 		}
 		PublishOperation2.throwException(status);
 	}
 
 	/**
 	 * Publish a jar file.
 	 * 
 	 * @param deltaKind
 	 * @param p
 	 * @param module
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void publishJar(int kind, int deltaKind, Properties p, IModule[] module, IProgressMonitor monitor) throws CoreException {
 		if (deltaKind == REMOVED) {
 			try {
 				String publishPath = (String) p.get(module[1].getId());
 				new File(publishPath).delete();
 			} catch (Exception e) {
 				throw new CoreException(new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, "Could not remove module", e));
 			}
 		} else {
 			IPath path = getServer().getRuntime().getLocation().append("webapps").append(module[0].getName());
 			path = path.append("WEB-INF").append("lib");
 			IPath jarPath = path.append(module[1].getName() + ".jar");
 			if (!path.toFile().exists()) {
 				path.toFile().mkdirs();
 			} else {
 				// If file still exists and we are not forcing a new one to be built
 				if (jarPath.toFile().exists() && kind != IServer.PUBLISH_CLEAN && kind != IServer.PUBLISH_FULL) {
 					// avoid changes if no changes to module since last publish
 					IModuleResourceDelta[] delta = getPublishedResourceDelta(module);
 					if (delta == null || delta.length == 0)
 						return;
 				}
 			}
 			
 			IModuleResource[] mr = getResources(module);
 			IStatus[] stat = PublishUtil.publishZip(mr, jarPath, monitor);
 			List status = new ArrayList();
 			PublishOperation2.addArrayToList(status, stat);
 			PublishOperation2.throwException(status);
 			p.put(module[1].getId(), jarPath.toOSString());
 		}
 	}
 
 	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
 		IPath baseDir;
 		if (getTomcatServer().isTestEnvironment())
 			baseDir = getTempDirectory();
 		else
 			baseDir = getServer().getRuntime().getLocation();
 		
 		// Publish context configuration for servers that support META-INF/context.xml
 		IStatus status = getTomcatConfiguration().publishContextConfig(baseDir, monitor);
 		if (!status.isOK())
 			throw new CoreException(status);
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
 		
 		setServerRestartState(false);
 		setServerState(IServer.STATE_STARTING);
 		setMode(launchMode);
 		
 		// ping server to check for startup
 		try {
 			String url = "http://localhost";
 			int port = configuration.getMainPort().getPort();
 			if (port != 80)
 				url += ":" + port;
 			ping = new PingThread(getServer(), url, -1, this);
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
 			wc.setAttribute("org.eclipse.debug.ui.private", true);
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
 	 * invalid values if they have been changed.  Special handling is provided
 	 * if the keepActionLast argument is true and the last vmArg is a simple
 	 * string.  The vmArgs will be merged such that the last vmArg is guaranteed
 	 * to be the last argument in the merged string.
 	 * 
 	 * @param originalArg String of original arguments.
 	 * @param vmArgs Arguments to merge into the original arguments string
 	 * @param keepActionLast If <b>true</b> the vmArguments are assumed to be Tomcat
 	 * program arguments, the last of which is the action to perform which must
 	 * remain the last argument.  This only has an impact if the last vmArg is
 	 * a simple string argument, like &quot;start&quot;.
 	 * @return merged argument string
 	 */
 	public static String mergeArguments(String originalArg, String[] vmArgs, String[] excludeArgs, boolean keepActionLast) {
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
 					if (!keepActionLast || i < (size - 1)) {
 						if (index2 >= 0)
 							originalArg = s + vmArgs[i] + originalArg.substring(index2);
 						else
 							originalArg = s + vmArgs[i];
 						vmArgs[i] = null;
 					}
 					else {
 						// The last VM argument needs to remain last,
 						// remove original arg and append the vmArg later
 						if (index2 >= 0)
 							originalArg = s + originalArg.substring(index2);
 						else
 							originalArg = s;
 					}
 				}
 			}
 		}
 
 		// remove excluded arguments
 		if (excludeArgs != null && excludeArgs.length > 0) {
 			for (int i = 0; i < excludeArgs.length; i++) {
 				int ind = excludeArgs[i].indexOf(" ");
 				int ind2 = excludeArgs[i].indexOf("=");
 				if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
 					int index = originalArg.indexOf(excludeArgs[i].substring(0, ind + 1));
 					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 						// remove
 						String s = originalArg.substring(0, index);
 						int index2 = getNextToken(originalArg, index + ind + 1);
 						if (index2 >= 0) {
 							// If remainder will become the first argument, remove leading blanks
 							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ')
 								index2 += 1;
 							originalArg = s + originalArg.substring(index2);
 						}
 						else
 							originalArg = s;
 					}
 				} else if (ind2 >= 0) { // a=b style
 					int index = originalArg.indexOf(excludeArgs[i].substring(0, ind2 + 1));
 					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 						// remove
 						String s = originalArg.substring(0, index);
 						int index2 = getNextToken(originalArg, index);
 						if (index2 >= 0) {
 							// If remainder will become the first argument, remove leading blanks
 							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ')
 								index2 += 1;
 							originalArg = s + originalArg.substring(index2);
 						}
 						else
 							originalArg = s;
 					}
 				} else { // abc style
 					int index = originalArg.indexOf(excludeArgs[i]);
 					if (index == 0 || (index > 0 && originalArg.charAt(index-1) == ' ')) {
 						// remove
 						String s = originalArg.substring(0, index);
 						int index2 = getNextToken(originalArg, index);
 						if (index2 >= 0) {
 							// Remove leading blanks
 							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ')
 								index2 += 1;
 							originalArg = s + originalArg.substring(index2);
 						}
 						else
 							originalArg = s;
 					}
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
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
 				mergeArguments(existingProgArgs, getRuntimeProgramArguments(true), getExcludedRuntimeProgramArguments(true), true));
 		
 		String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
 		String[] parsedVMArgs = null;
 		if (null != existingVMArgs) {
 			parsedVMArgs = DebugPlugin.parseArguments(existingVMArgs);
 		}
 		String [] configVMArgs = getRuntimeVMArguments();
 		if (getTomcatServer().isSecure()) {
 			boolean addSecurityArgs = true;
 			if (null != parsedVMArgs) {
 				for (int i = 0; i < parsedVMArgs.length; i++) {
 					if (parsedVMArgs[i].startsWith("wtp.configured.security")) {
 						addSecurityArgs = false;
 						break;
 					}
 				}
 			}
 			if (addSecurityArgs) {
 				String [] newVMArgs = new String [configVMArgs.length + 3];
 				System.arraycopy(configVMArgs, 0, newVMArgs, 0, configVMArgs.length);
 				newVMArgs[configVMArgs.length] = "-Djava.security.manager";
 				newVMArgs[configVMArgs.length + 1] = "-Djava.security.policy=\""
 						+ getRuntimePolicyFile() +"\"";
 				newVMArgs[configVMArgs.length + 2] = "-Dwtp.configured.security=true";
 				configVMArgs = newVMArgs;
 			}
 		}
 		else if (null != parsedVMArgs){
 			boolean removeSecurityArgs = false;
 			for (int i = 0; i < parsedVMArgs.length; i++) {
 				if (parsedVMArgs[i].startsWith("-Dwtp.configured.security")) {
 					removeSecurityArgs = true;
 					break;
 				}
 			}
 			if (removeSecurityArgs) {
 				StringBuffer filteredVMArgs = new StringBuffer();
 				for (int i = 0; i < parsedVMArgs.length; i++) {
 					String arg = parsedVMArgs[i];
 					if (!arg.startsWith("-Djava.security.manager")
 							&& !arg.startsWith("-Djava.security.policy=")
 							&& !arg.startsWith("-Dwtp.configured.security=")) {
 						if (filteredVMArgs.length() > 0) {
 							filteredVMArgs.append(' ');
 						}
 						filteredVMArgs.append(arg);
 					}
 				}
 				existingVMArgs = filteredVMArgs.toString();
 			}
 		}
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
 				mergeArguments(existingVMArgs, configVMArgs, null, false));
 		
 		ITomcatRuntime runtime = getTomcatRuntime();
 		IVMInstall vmInstall = runtime.getVMInstall();
 		if (vmInstall != null)
 			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
 		
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
 				if (toolsPath.toFile().exists()) {
 					IRuntimeClasspathEntry toolsJar = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
 					// Search for index to any existing tools.jar entry
 					int toolsIndex;
 					for (toolsIndex = 0; toolsIndex < oldCp.size(); toolsIndex++ ) {
 						IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) oldCp.get(toolsIndex);
 						if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE
 								&& entry.getPath().lastSegment().equals("tools.jar")) {
 							break;
 						}
 					}
 					// If existing tools.jar found, replace in case it's different.  Otherwise add.
 					if (toolsIndex < oldCp.size())
 						oldCp.set(toolsIndex, toolsJar); 
 					else
 						mergeClasspath(oldCp, toolsJar);
 				}
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
 
 	protected IModuleResource[] getResources(IModule[] module) {
 		return super.getResources(module);
 	}
 
 	protected IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
 		return super.getPublishedResourceDelta(module);
 	}
 
 	/**
 	 * @see ServerBehaviourDelegate#handleResourceChange()
 	 */
 	public void handleResourceChange() {
 		if (getServer().getServerRestartState())
 			return;
 		
 		Iterator iterator = getAllModules().iterator();
 		while (iterator.hasNext()) {
 			IModule[] module = (IModule[]) iterator.next();
 			IModuleResourceDelta[] delta = getPublishedResourceDelta(module);
 			if (delta == null || delta.length == 0)
 				continue;
 			
 			if (containsNonResourceChange(delta)) {
 				setServerRestartState(true);
 				return;
 			}
 		}
 	}
 
 	protected boolean containsNonResourceChange(IModuleResourceDelta[] delta) {
 		int size = delta.length;
 		for (int i = 0; i < size; i++) {
 			IModuleResourceDelta d = delta[i];
 			if (d.getModuleRelativePath().segmentCount() == 0) {
 				if ("WEB-INF".equals(d.getModuleResource().getName())) {
 					return containsNonResourceChange(d.getAffectedChildren());
 				}
 				continue;
 			}
 			if (d.getModuleResource() instanceof IModuleFile)
 				return true;
 			
 			boolean b = containsNonAddChange(d.getAffectedChildren());
 			if (b)
 				return true;
 		}
 		return false;
 	}
 
 	protected boolean containsNonAddChange(IModuleResourceDelta[] delta) {
 		if (delta == null)
 			return false;
 		int size = delta.length;
 		for (int i = 0; i < size; i++) {
 			IModuleResourceDelta d = delta[i];
 			if (d.getModuleResource() instanceof IModuleFile) {
 				if (d.getKind() != IModuleResourceDelta.ADDED)
 					return true;
 			}
 			
 			boolean b = containsNonAddChange(d.getAffectedChildren());
 			if (b)
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Temporary method to help web services team. Returns the path that the module is
 	 * published to when in test environment mode.
 	 * 
 	 * @param module a module on the server 
 	 * @return the path that the module is published to when in test environment mode,
 	 *    or null if not running as a test environment or the module is not a web module
 	 */
 	public IPath getPublishDirectory(IModule[] module) {
 		if (!getTomcatServer().isTestEnvironment() || module == null || module.length != 1)
 			return null;
 		
 		return getTempDirectory().append("webapps").append(module[0].getName());
 	}
 
 	public void setModulePublishState2(IModule[] module, int state) {
 		setModulePublishState(module, state);
 	}
 }
