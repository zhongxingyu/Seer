 package org.eclipse.jst.server.generic.core.internal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
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
 import org.eclipse.jst.server.generic.servertype.definition.Classpath;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerPort;
 import org.eclipse.wst.server.core.model.RuntimeDelegate;
 import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 import org.eclipse.wst.server.core.util.PingThread;
 import org.eclipse.wst.server.core.util.SocketUtil;
 
 /**
  * Server behaviour delegate implementation for generic server.
  *
  * @author Gorkem Ercan
  */
 public class GenericServerBehaviour extends ServerBehaviourDelegate {
 	
 	private static final String ATTR_STOP = "stop-server";
     
 	// the thread used to ping the server to check for startup
 	protected transient PingThread ping = null;
     protected transient IDebugEventSetListener processListener;
     protected transient IProcess process;
     
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#publishServer(org.eclipse.core.runtime.IProgressMonitor)
      */
     public void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
         
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#publishModule(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule, org.eclipse.core.runtime.IProgressMonitor)
      */
     public void publishModule(int kind, int deltaKind, IModule[] module,
             IProgressMonitor monitor) throws CoreException {
  
         if(REMOVED == deltaKind){
             removeFromServer(module,monitor);
         }
         else{
             Module m = getServerDefinition().getModule(module[0].getModuleType().getId());
             String publisherId = m.getPublisherReference();
             GenericPublisher publisher = PublishManager.getPublisher(publisherId);
             if(publisher==null){
                 IStatus status = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,"Unable to create publisher",null);
                 throw new CoreException(status);
             }
             publisher.initialize(module,getServerDefinition());
             IStatus[] status= publisher.publish(null,monitor);
             if(status==null)
                 setModulePublishState(module, IServer.PUBLISH_STATE_NONE);
         }
     }
 
     private void removeFromServer(IModule[] module, IProgressMonitor monitor) throws CoreException
     {
         Module m = getServerDefinition().getModule(module[0].getModuleType().getId());
         String publisherId = m.getPublisherReference();
         GenericPublisher publisher = PublishManager.getPublisher(publisherId);  
         if(publisher==null){
             IStatus status = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,"Unable to create publisher to remove module",null);
             throw new CoreException(status);
         }
         publisher.initialize(module,getServerDefinition());
         publisher.unpublish(monitor);
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
 
 		try {
 			Trace.trace(Trace.FINEST, "Stopping Server");
 			if (state != IServer.STATE_STOPPED)
 				setServerState(IServer.STATE_STOPPING);
 			ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
 
 			ILaunchConfigurationType type =
 				mgr.getLaunchConfigurationType(
 					IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
 
 			String launchName = "GenericServerStopper";
 			String uniqueLaunchName =
 				mgr.generateUniqueLaunchConfigurationNameFrom(launchName);
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
 			//To stop from appearing in history lists
 			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);			
 	
 			wc.setAttribute(
 					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
 					getServerDefinition().getResolver().resolveProperties(this.getServerDefinition().getStop().getMainClass()));
 
 			GenericServerRuntime runtime = (GenericServerRuntime) getRuntimeDelegate();
 
 			IVMInstall vmInstall = runtime.getVMInstall();
 			wc.setAttribute(
 					IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, runtime
 							.getVMInstallTypeId());
 			wc.setAttribute(
 					IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
 					vmInstall.getName());
 
 			setupLaunchClasspath(wc, vmInstall, getStopClasspath());
 
 			wc.setAttribute(
 					IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
 					getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStop().getWorkingDirectory()));
 			wc.setAttribute(
 					IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
 					getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStop().getProgramArguments()));
 			wc.setAttribute(
 					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
 					getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStop().getVmParameters()));				
 			wc.setAttribute(ATTR_STOP, "true");
 			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error stopping Server", e);
 		}
 	
 
     }
 
     public String getStartClassName() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getMainClass());
     }
 
     public ServerRuntime getServerDefinition() {
         GenericServer server = (GenericServer)getServer().getAdapter(ServerDelegate.class);
         return server.getServerDefinition();
     }
     
     private RuntimeDelegate getRuntimeDelegate()
     {
        return (RuntimeDelegate)getServer().getRuntime().getAdapter(RuntimeDelegate.class);
     }
 
 
 
     private List getStartClasspath() {
     	String cpRef = getServerDefinition().getStart().getClasspathReference();
     	return serverClasspath(cpRef);
     }
 
     /**
      * @param cpRef
      * @return
      */
     private List serverClasspath(String cpRef) {
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
     private void setupLaunchClasspath(ILaunchConfigurationWorkingCopy wc, IVMInstall vmInstall, List cp) {
 		// TODO: Remove adding tools.jar after the configurable JREs
     	addToolsJar(vmInstall, cp);
 	
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
 	private void addToolsJar(IVMInstall vmInstall, List cp) {
 		if (vmInstall != null) {
     		try {
     			cp.add(JavaRuntime
     							.newRuntimeContainerClasspathEntry(
     									new Path(JavaRuntime.JRE_CONTAINER)
     											.append(
     													"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType")
     											.append(vmInstall.getName()),
     									IRuntimeClasspathEntry.BOOTSTRAP_CLASSES)
     							);
     		} catch (Exception e) {
 				// ignore
     		}
     
     		IPath jrePath = new Path(vmInstall.getInstallLocation()
     				.getAbsolutePath());
     		if (jrePath != null) {
     			IPath toolsPath = jrePath.append("lib").append("tools.jar");
     			if (toolsPath.toFile().exists()) {
      					cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(
     							toolsPath));
      			}
     		}
     	}
 	}
 
     private String getWorkingDirectory() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getWorkingDirectory());
     }
 
     private String getProgramArguments() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getProgramArguments());
     }
 
     private String getVmArguments() {
     	return getServerDefinition().getResolver().resolveProperties(getServerDefinition().getStart().getVmParameters());
     }
 
     public void setupLaunchConfiguration(
             ILaunchConfigurationWorkingCopy workingCopy,
             IProgressMonitor monitor) throws CoreException {
 
 
         workingCopy.setAttribute(
                 IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                 getStartClassName());
 
         GenericServerRuntime runtime = (GenericServerRuntime) getRuntimeDelegate();
 
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
         
         String existingProgArgs  = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
         String serverProgArgs =  getProgramArguments();
         if(existingProgArgs==null || existingProgArgs.indexOf(serverProgArgs)<0) {
             workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,serverProgArgs);
         }
         String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,(String)null);
         String serverVMArgs= getVmArguments();
         if(existingVMArgs==null || existingVMArgs.indexOf(serverVMArgs)<0) {
             workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,serverVMArgs);
         }
     }
     /**
     	 * Setup for starting the server.
     	 * 
     	 * @param launch ILaunch
     	 * @param launchMode String
     	 * @param monitor IProgressMonitor
     	 */
     	public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
     		if ("true".equals(launch.getLaunchConfiguration().getAttribute(ATTR_STOP, "false")))
     			return;
     //		IStatus status = getRuntime().validate();
     //		if (status != null && !status.isOK())
     //			throw new CoreException(status);
     
     	
     		ServerPort[] ports = getServer().getServerPorts();
     		ServerPort sp = null;
     		for(int i=0;i<ports.length;i++){
     			sp= ports[i];
     			if (SocketUtil.isPortInUse(ports[i].getPort(), 5))
     				throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, GenericServerCoreMessages.getFormattedString("errorPortInUse",new String[] {Integer.toString(sp.getPort()),sp.getName()}),null));
     		}
     		
     		setServerState(IServer.STATE_STARTING);
     	
     		// ping server to check for startup
     		try {
     			String url = "http://localhost";
     			int port = sp.getPort();
     			if (port != 80)
     				url += ":" + port;
     			ping = new PingThread(getServer(),this, url, 50);
     		} catch (Exception e) {
     			Trace.trace(Trace.SEVERE, "Can't ping for server startup.");
     		}
     	}
 
     public void setProcess(final IProcess newProcess) {
     	if (process != null)
     		return;
     
     	process = newProcess;
     	processListener = new IDebugEventSetListener() {
     		public void handleDebugEvents(DebugEvent[] events) {
     			if (events != null) {
     				int size = events.length;
     				for (int i = 0; i < size; i++) {
     					if (process.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
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
     		ping.stopPinging();
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
      */
     public void terminate() {
     	if (getServer().getServerState() == IServer.STATE_STOPPED)
     		return;
     
     	try {
     		setServerState(IServer.STATE_STOPPING);
     		Trace.trace(Trace.FINEST, "Killing the Server process");
     		if (process != null && !process.isTerminated()) {
     			process.terminate();
     			stopImpl();
     		}
     	} catch (Exception e) {
     		Trace.trace(Trace.SEVERE, "Error killing the process", e);
     	}
     }
 
     private List getStopClasspath() {
     	String cpRef = getServerDefinition().getStop().getClasspathReference();
     	return serverClasspath(cpRef);
     }
     public void initialize() {
       super.initialize();
      setModules(getServer().getModules());
     }
     public void publishFinish(IProgressMonitor monitor) throws CoreException {
         IModule[] modules = this.getServer().getModules();
         boolean allpublished= true;
         for (int i = 0; i < modules.length; i++) {
             if(this.getServer().getModulePublishState(modules)!=IServer.PUBLISH_STATE_NONE)
                 allpublished=false;
         }
         if(allpublished)
             setServerPublishState(IServer.PUBLISH_STATE_NONE);
     }
 }
