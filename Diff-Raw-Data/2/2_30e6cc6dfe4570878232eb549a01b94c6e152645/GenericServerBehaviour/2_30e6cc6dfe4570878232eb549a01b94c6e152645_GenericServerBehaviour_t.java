 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IDebugEventSetListener;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ArchiveType;
 import org.eclipse.jst.server.generic.servertype.definition.ArgumentPair;
 import org.eclipse.jst.server.generic.servertype.definition.Classpath;
 import org.eclipse.jst.server.generic.servertype.definition.LaunchConfiguration;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerPort;
 import org.eclipse.wst.server.core.internal.DeletedModule;
 import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 import org.eclipse.wst.server.core.util.SocketUtil;
 
 /**
  * Server behaviour delegate implementation for generic server.
  *
  * @author Gorkem Ercan
  */
 public class GenericServerBehaviour extends ServerBehaviourDelegate {
 	
 	private static final String ATTR_STOP = "stop-server"; //$NON-NLS-1$
     
 	// the thread used to ping the server to check for startup
 	protected transient PingThread ping;
     protected transient IDebugEventSetListener processListener;
     protected transient IProcess process;
     
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#publishServer(org.eclipse.core.runtime.IProgressMonitor)
      */
     public void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
         // do nothing
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#publishModule(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule, org.eclipse.core.runtime.IProgressMonitor)
      */
     public void publishModule(int kind, int deltaKind, IModule[] module,
             IProgressMonitor monitor) throws CoreException {
         GenericPublisher publisher = initializePublisher( kind, deltaKind, module );
         IStatus[] status = null;
     	if(REMOVED == deltaKind ){//TODO: check if the removed module is published to server
             status = publisher.unpublish(monitor);
         }
         else{
         	checkClosed(module);
             status= publisher.publish(null,monitor);
         }
         setModulePublishState( module, status );
     }
 
     private void setModulePublishState( IModule[] module, IStatus[] status ) throws CoreException {
         if(status==null || status.length < 1 ){
             setModulePublishState(module, IServer.PUBLISH_STATE_NONE);
         }else {
             for (int i=0; i < status.length; i++) {
                 if (IStatus.ERROR == status[i].getSeverity()){
                 	setModulePublishState(module, IServer.PUBLISH_STATE_UNKNOWN);
                     throw new CoreException(status[i]);
                 }
             }
         }
     }
 
     private void checkClosed(IModule[] module) throws CoreException
     {
     	for( int i=0; i < module.length; i++ ){
     		if( module[i] instanceof DeletedModule ){	
                 IStatus status = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0, NLS.bind(GenericServerCoreMessages.canNotPublishDeletedModule,module[i].getName()),null);
                 throw new CoreException(status);
     		}
     	}
     }
 
     private GenericPublisher initializePublisher(int kind, int deltaKind, IModule[] module ) throws CoreException {
         String publisherId = ServerTypeDefinitionUtil.getPublisherID(module[0], getServerDefinition());
         GenericPublisher publisher = PublishManager.getPublisher(publisherId);  
         if(publisher==null){
             IStatus status = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,NLS.bind(GenericServerCoreMessages.unableToCreatePublisher,publisherId),null);
             throw new CoreException(status);
         }
         publisher.initialize( module,getServer(), kind, deltaKind );
         return publisher;
     }
     
     
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#stop(boolean)
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
 		
 		shutdown(state);
     }
     
     /**
      * Shuts down the server via the launch configuration.
      */
     protected void shutdown(int state) {
 		GenericServerRuntime runtime = getRuntimeDelegate();
 		try {
 			Trace.trace(Trace.FINEST, "Stopping Server"); //$NON-NLS-1$
 			if (state != IServer.STATE_STOPPED)
 				setServerState(IServer.STATE_STOPPING);
 			String configTypeID = getConfigTypeID(); 
 			ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
 			ILaunchConfigurationType type = mgr.getLaunchConfigurationType(configTypeID);
 			String launchName = getStopLaunchName();
 			String uniqueLaunchName = mgr.generateUniqueLaunchConfigurationNameFrom(launchName);
 			ILaunchConfiguration conf = null;
 			ILaunchConfiguration[] lch = mgr.getLaunchConfigurations(type);
 			for (int i = 0; i < lch.length; i++) {
 				if (launchName.equals(lch[i].getName())) {
 					conf = lch[i];
 					break;
 				}
 			}
 
 			ILaunchConfigurationWorkingCopy wc = null;
 			if (conf != null) {
 				wc = conf.getWorkingCopy();
 			} else {
 				wc = type.newInstance(null, uniqueLaunchName);
 			}
 			
 			// To stop from appearing in history lists
 			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);		
 			// Set the stop attribute so that we know we are stopping
 			wc.setAttribute(ATTR_STOP, "true"); //$NON-NLS-1$
 			
 			// Setup the launch config for stopping the server
 			setupStopLaunchConfiguration(runtime, wc);
 			
 			// Launch the stop launch config
 			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error stopping Server", e); //$NON-NLS-1$
 		}
     }
 
     /**
      * Returns the String ID of the launch configuration type.
      * @return id
      */
 	protected String getConfigTypeID() {
 		return IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION;
 	}
 
 	/**
 	 * Returns the String name of the stop launch configuration.
 	 * @return launchname
 	 */
 	protected String getStopLaunchName() {
 		return "GenericServerStopper"; //$NON-NLS-1$
 	}
 	
 	private boolean isRemote(){
 		return (getServer().getServerType().supportsRemoteHosts()&& !SocketUtil.isLocalhost(getServer().getHost()) );
 	}	
 	/**
 	 * Sets up the launch configuration for stopping the server.
 	 */
 	protected void setupStopLaunchConfiguration(GenericServerRuntime runtime, ILaunchConfigurationWorkingCopy wc) {
 		if(isRemote())// Do not launch for remote servers.
 			return;
 		
 		wc.setAttribute(
 				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
 				getServerDefinition().getResolver().resolveProperties(this.getServerDefinition().getStop().getMainClass()));
 
 		IVMInstall vmInstall = runtime.getVMInstall();
 		wc.setAttribute(
 				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, runtime
 						.getVMInstallTypeId());
 		wc.setAttribute(
 				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
 				vmInstall.getName());
 
 		setupLaunchClasspath(wc, vmInstall, getStopClasspath());
 
         Map environVars = getEnvironmentVariables(getServerDefinition().getStop());
         if(!environVars.isEmpty()){
         	wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,environVars);
         }
         
 		wc.setAttribute(
 				IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
 				getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStop().getWorkingDirectory()));
 		wc.setAttribute(
 				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
 				getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStop().getProgramArgumentsAsString()));
 		wc.setAttribute(
 				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
 				getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStop().getVmParametersAsString()));				
 	}
 
     /**
      * Start class name
      * @return name
      */
     public String getStartClassName() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getMainClass());
     }
 
     /** 
      * Server definition
      * @return serverdef
      */
     public ServerRuntime getServerDefinition() {
         GenericServer server = (GenericServer)getServer().loadAdapter(ServerDelegate.class, null);
         return server.getServerDefinition();
     }
     
     protected GenericServerRuntime getRuntimeDelegate() {
        return (GenericServerRuntime)getServer().getRuntime().loadAdapter(GenericServerRuntime.class,null);
     }
 
     private List getStartClasspath() {
     	String cpRef = getServerDefinition().getStart().getClasspathReference();
     	return serverClasspath(cpRef);
     }
 
     /**
      * @param cpRef
      * @return classpath
      */
     protected List serverClasspath(String cpRef) {
     	Classpath classpath = getServerDefinition().getClasspath(cpRef);
     	
         List cpEntryList = new ArrayList(classpath.getArchive().size());
         Iterator iterator= classpath.getArchive().iterator();
         while(iterator.hasNext())
         {
         	ArchiveType archive = (ArchiveType)iterator.next();
         	String cpath = getServerDefinition().getResolver().resolveProperties(archive.getPath());
     	
     			cpEntryList.add(JavaRuntime.newArchiveRuntimeClasspathEntry(
     					new Path(cpath)));
          }
     	return cpEntryList;
     }
 
     /**
      * @param wc
      * @param vmInstall
      */
     protected void setupLaunchClasspath(ILaunchConfigurationWorkingCopy wc, IVMInstall vmInstall, List cp) {
 		//merge existing classpath with server classpath
 		try {
 			IRuntimeClasspathEntry[] existingCps = JavaRuntime.computeUnresolvedRuntimeClasspath(wc);
 			for (int i = 0; i < existingCps.length; i++) {
 				if(cp.contains(existingCps[i])==false){ 
 					cp.add(existingCps[i]);
 				}
 			}
 		} catch (CoreException e) {
 			// ignore
 		}
 		
     	wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, convertCPEntryToMemento(cp));
     	wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,false);
     }
 
 	private List convertCPEntryToMemento(List cpEntryList)
 	{
 		List list = new ArrayList(cpEntryList.size());
 		Iterator iterator = cpEntryList.iterator();
 		while(iterator.hasNext())
 		{
 			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)iterator.next();
 			try {
 				list.add(entry.getMemento());
 			} catch (CoreException e) {
 				// ignore
 			}
 		}
 		return list;
 	}
 
     private String getWorkingDirectory() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getWorkingDirectory());
     }
 
     protected String getProgramArguments() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getProgramArgumentsAsString());
     }
 
     protected Map getEnvironmentVariables(LaunchConfiguration config){
         List variables = config.getEnvironmentVariable();
         Map varsMap = new HashMap(variables.size());
         Iterator iterator= variables.iterator();
         while(iterator.hasNext()){
         	ArgumentPair pair = (ArgumentPair)iterator.next();
         	varsMap.put(pair.getName(),getServerDefinition().getResolver().resolveProperties(pair.getValue()));
         }
         return varsMap;
     }
     
     private String getVmArguments() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getVmParametersAsString());
     }
 
     public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
 		if(isRemote())// No launch for remote servers.
 			return;
     	
     	workingCopy.setAttribute(
                 IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                 getStartClassName());
 
         GenericServerRuntime runtime = getRuntimeDelegate();
 
         IVMInstall vmInstall = runtime.getVMInstall();
         workingCopy.setAttribute(
                 IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, runtime
                         .getVMInstallTypeId());
         workingCopy.setAttribute(
                 IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
                 vmInstall.getName());
 
         setupLaunchClasspath(workingCopy, vmInstall, getStartClasspath());
 
 
         workingCopy.setAttribute(
                 IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
                 getWorkingDirectory());
         
 
         Map environVars = getEnvironmentVariables(getServerDefinition().getStart());
         if(!environVars.isEmpty()){
         	workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,environVars);
         }
         
         String existingProgArgs  = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
         String serverProgArgs =  getProgramArguments();
         if( existingProgArgs==null ) {
             workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,serverProgArgs);
         }
         String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,(String)null);
         String serverVMArgs= getVmArguments();
         if( existingVMArgs==null ) {
             workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,serverVMArgs);
         }
 	}
     
     /**
      * Setup for starting the server. Checks all ports available 
      * and sets server state and mode.
      * 
      * @param launch ILaunch
      * @param launchMode String
      * @param monitor IProgressMonitor
      */
     protected void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
     	if ("true".equals(launch.getLaunchConfiguration().getAttribute(ATTR_STOP, "false")))  //$NON-NLS-1$ //$NON-NLS-2$
     		return;
 
     	String host = getServer().getHost();
     	ServerPort[] ports = getServer().getServerPorts(null);
     	ServerPort sp = null;
     	if(SocketUtil.isLocalhost(host)){
 	    	for(int i=0;i<ports.length;i++){
 	    		sp= ports[i];
 	    		if (SocketUtil.isPortInUse(ports[i].getPort(), 5))
 	    			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, NLS.bind(GenericServerCoreMessages.errorPortInUse,Integer.toString(sp.getPort()),sp.getName()),null));
 	    	}
     	}
     	setServerState(IServer.STATE_STARTING);
     	setMode(launchMode);
     }
     /**
      * Call to start Ping thread that will check for startup of the server.
      *
      */
     protected void startPingThread()
     {
     	try {
     		String url = "http://"+getServer().getHost();; //$NON-NLS-1$
     	  	ServerPort[] ports = getServer().getServerPorts(null);
         	ServerPort sp = null;
     	    for(int i=0;i<ports.length;i++){
     	    	if(ports[i].getProtocol().equalsIgnoreCase("http")){//$NON-NLS-1$
     	    		sp=ports[i];
     	    	}
     	    }
 	    	if(sp==null){
 	    		Trace.trace(Trace.SEVERE, "Can't ping for server startup."); //$NON-NLS-1$
 	    		return;
 	    	}
         	int port = sp.getPort();
     		if (port != 80)
    			url += ":" + port; //$NON-NLS-1$
     		ping = new PingThread(getServer(), url, this);
     	} catch (Exception e) {
     		Trace.trace(Trace.SEVERE, "Can't ping for server startup."); //$NON-NLS-1$
     	}  	
     }
    
     protected void setProcess(final IProcess newProcess) {
     	if (process != null)
     		return;
     	if(processListener!=null)
     		DebugPlugin.getDefault().removeDebugEventListener(processListener);
     	if (newProcess==null)
     		return;
     	process = newProcess;
     	processListener = new IDebugEventSetListener() {
     		public void handleDebugEvents(DebugEvent[] events) {
     			if (events != null) {
     				int size = events.length;
     				for (int i = 0; i < size; i++) {
     					if (process!= null &&  process.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
     						DebugPlugin.getDefault().removeDebugEventListener(this);
     						stopImpl();
     					}
     				}
     			}
     		}
     	};
     	DebugPlugin.getDefault().addDebugEventListener(processListener);
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
 
     /**
      * Terminates the server.
      * This method may be called before a process created while setting up the 
      * launch config. 
      */
     protected void terminate() {
     	if (getServer().getServerState() == IServer.STATE_STOPPED)
     		return;
     
     	try {
     		setServerState(IServer.STATE_STOPPING);
     		Trace.trace(Trace.FINEST, "Killing the Server process"); //$NON-NLS-1$
     		if (process != null && !process.isTerminated()) {
     			process.terminate();
     			
     		}
     		stopImpl();
     	} catch (Exception e) {
     		Trace.trace(Trace.SEVERE, "Error killing the process", e); //$NON-NLS-1$
     	}
     }
 
     private List getStopClasspath() {
     	String cpRef = getServerDefinition().getStop().getClasspathReference();
     	return serverClasspath(cpRef);
     }
 
     public void publishFinish(IProgressMonitor monitor) throws CoreException {
         IModule[] modules = this.getServer().getModules();
         boolean allpublished= true;
         for (int i = 0; i < modules.length; i++) {
         	if(this.getServer().getModulePublishState(new IModule[]{modules[i]})!=IServer.PUBLISH_STATE_NONE)
                 allpublished=false;
         }
         if(allpublished)
             setServerPublishState(IServer.PUBLISH_STATE_NONE);
     }
     
  	protected void setServerStarted() {
  		setServerState(IServer.STATE_STARTED);
  	}
 }
