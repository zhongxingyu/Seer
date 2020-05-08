 /*******************************************************************************
  * Copyright (c) 2008 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.ws.creation.core.test.command;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.IStreamListener;
 import org.eclipse.debug.core.model.IStreamMonitor;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.IServerListener;
 import org.eclipse.wst.server.core.IServerType;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerEvent;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.IServer.IOperationListener;
 import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
 import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
 import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
 import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.tools.test.util.TestProjectProvider;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.ResourcesUtils;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 
 @SuppressWarnings("restriction")
 public abstract class AbstractJBossWSCommandTest extends TestCase {
 	public static final IVMInstall VM_INSTALL = JavaRuntime
 			.getDefaultVMInstall();
 	protected static final IWorkspace ws = ResourcesPlugin.getWorkspace();
 	protected static final IWorkbench wb = PlatformUI.getWorkbench();
 
 	protected static final int DEFAULT_STARTUP_TIME = 150000;
 	protected static final int DEFAULT_SHUTDOWN_TIME = 90000;
 
 	protected static final String JBOSSWS_HOME_DEFAULT = "D:/softinstall/jboss-4.2.3GA/jboss-4.2.3.GA";
 	public static final String JBOSSWS_42_HOME = "jbosstools.test.jboss.home.4.2";
 	public static final String JBOSS_RUNTIME_42 = "org.jboss.ide.eclipse.as.runtime.42";
 	public static final String JBOSS_AS_42_HOME = System.getProperty(
 			JBOSSWS_42_HOME, JBOSSWS_HOME_DEFAULT);
 	public static final String JBOSS_SERVER_42 = "org.jboss.ide.eclipse.as.42";
 
 	protected final Set<IResource> resourcesToCleanup = new HashSet<IResource>();
 
 	protected static final IProjectFacetVersion dynamicWebVersion;
 	protected static final IProjectFacetVersion javaVersion;
 	protected static final IProjectFacetVersion jbosswsFacetVersion;
 	private static final IProjectFacet jbosswsFacet;
 
 	static String wsdlFileName = "hello_world.wsdl";
 	static String BUNDLE = "org.jboss.tools.ws.creation.core.test";
 
 	IFacetedProject fproject;
 	protected IRuntime currentRuntime;
 	protected IServer currentServer;
 	protected ServerStateListener stateListener;
 
 	static {
 		javaVersion = ProjectFacetsManager.getProjectFacet("jst.java")
 				.getVersion("5.0");
 		dynamicWebVersion = ProjectFacetsManager.getProjectFacet("jst.web")
 				.getVersion("2.5");
 		jbosswsFacet = ProjectFacetsManager.getProjectFacet("jbossws.core");
 		jbosswsFacetVersion = jbosswsFacet.getVersion("2.0");
 
 	}
 
 	public AbstractJBossWSCommandTest() {
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// create jbossws web project
 
 		createServer(JBOSS_RUNTIME_42, JBOSS_SERVER_42, JBOSS_AS_42_HOME,
 				"default");
 		// first thing's first. Let's add a server state listener
 		stateListener = new ServerStateListener();
 		currentServer.addServerListener(stateListener);
 
 		JobUtils.delay(3000);
 	}
 
 	public IProject createProject(String prjName) throws CoreException {
 		TestProjectProvider provider = new TestProjectProvider(BUNDLE, "/projects/" + prjName,
 				prjName, true);
 		IProject prj = provider.getProject();
 		return prj;
 	}
 
 	protected void tearDown() throws Exception {
 		// Wait until all jobs is finished to avoid delete project problems
 
 		undeployWebProject();
 
 		boolean oldAutoBuilding = ResourcesUtils.setBuildAutomatically(false);
 		Exception last = null;
 
 		try {
 			JobUtils.delay(500);
 			for (IResource r : this.resourcesToCleanup) {
 				try {
 					System.out.println("Deleting " + r);
 					r.delete(true, null);
 					JobUtils.delay(500);
 				} catch (Exception e) {
 					System.out.println("Error deleting " + r);
 					e.printStackTrace();
 					last = e;
 				}
 			}
 		} finally {
 			ResourcesUtils.setBuildAutomatically(oldAutoBuilding);
 		}
 
 		if (last != null)
 			throw last;
 
 		resourcesToCleanup.clear();
 		// cleanProjectFromServer() ;
 		shutdown();
 		currentServer.removeServerListener(stateListener);
 		currentRuntime.delete();
 		currentServer.delete();
 
 		super.tearDown();
 	}
 
 	protected void createServer(String runtimeID, String serverID,
 			String location, String configuration) throws CoreException {
 		// if file doesnt exist, abort immediately.
 		assertTrue(new Path(location).toFile().exists());
 
 		currentRuntime = createRuntime(runtimeID, location, configuration);
 		IServerType serverType = ServerCore.findServerType(serverID);
 		IServerWorkingCopy serverWC = serverType.createServer(null, null,
 				new NullProgressMonitor());
 		serverWC.setRuntime(currentRuntime);
 		serverWC.setName(serverID);
 		serverWC.setServerConfiguration(null);
 		IPath path = new Path(location).append("server").append("default")
 				.append("deploy");
 		((ServerWorkingCopy) serverWC).setAttribute(
 				DeployableServer.DEPLOY_DIRECTORY, path.toOSString());
 		currentServer = serverWC.save(true, new NullProgressMonitor());
 
 	}
 
 	private IRuntime createRuntime(String runtimeId, String homeDir,
 			String config) throws CoreException {
 		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null,
 				runtimeId);
 		assertEquals("expects only one runtime type", runtimeTypes.length, 1);
 		IRuntimeType runtimeType = runtimeTypes[0];
 		IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null,
 				new NullProgressMonitor());
 		runtimeWC.setName(runtimeId);
 		runtimeWC.setLocation(new Path(homeDir));
 		((RuntimeWorkingCopy) runtimeWC).setAttribute(
 				IJBossServerRuntime.PROPERTY_VM_ID, VM_INSTALL.getId());
 		((RuntimeWorkingCopy) runtimeWC).setAttribute(
 				IJBossServerRuntime.PROPERTY_VM_TYPE_ID, VM_INSTALL
 						.getVMInstallType().getId());
 		((RuntimeWorkingCopy) runtimeWC).setAttribute(
 				IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);
 		IRuntime savedRuntime = runtimeWC.save(true, new NullProgressMonitor());
 		return savedRuntime;
 	}
 
 	protected ServiceModel createServiceModel() {
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 		model.setWsdlURI(wsdlFile.getLocation().toOSString());
 		model.addServiceName("SOAPService");
 		model.addPortTypes("Greeter");
 		model.setCustomPackage("org.apache.hello_world_soap_http");
 
 		return model;
 
 	}
 
 	protected void publishWebProject() throws CoreException {
 		IModule[] modules = ServerUtil.getModules(currentServer.getServerType()
 				.getRuntimeType().getModuleTypes());
 		IServerWorkingCopy serverWC = currentServer.createWorkingCopy();
 		serverWC.modifyModules(modules, null, null);
 		serverWC.save(true, null).publish(0, null);
 		currentServer.publish(IServer.PUBLISH_FULL, null);
 
 	}
 
 	protected void undeployWebProject() throws CoreException {
 		IModule[] modules = ServerUtil.getModules(currentServer.getServerType()
 				.getRuntimeType().getModuleTypes());
 		IServerWorkingCopy serverWC = currentServer.createWorkingCopy();
 		serverWC.modifyModules(null, modules, null);
 		serverWC.save(true, null).publish(0, null);
 		currentServer.publish(IServer.PUBLISH_FULL, null);
 
 	}
 
 	protected void cleanProjectFromServer() throws CoreException {
 		IModule[] modules = ServerUtil.getModules(currentServer.getServerType()
 				.getRuntimeType().getModuleTypes());
 		IServerWorkingCopy serverWC = currentServer.createWorkingCopy();
 		serverWC.modifyModules(null, modules, null);
 		currentServer.publish(0, null);
 		currentServer.stop(true);
 
 	}
 
 	protected boolean isServerSupplied() {
 		return false;
 	}
 
 	protected IFacetedProject createJBossWSProject(String baseProjectName,
 			boolean isServerSupplied) throws CoreException {
 		IProject project = createProject(baseProjectName);
 		final IFacetedProject fproj = ProjectFacetsManager.create(project);
 
 		// installDependentFacets(fproj);
 		fproj.installProjectFacet(jbosswsFacetVersion,
 				createJBossWSDataModel(isServerSupplied), null);
 
 		assertNotNull(project);
 
 		this.addResourceToCleanup(project);
 
 		return fproj;
 	}
 
 	abstract IDataModel createJBossWSDataModel(boolean isServerSupplied);
 
 	protected final void addResourceToCleanup(final IResource resource) {
 		this.resourcesToCleanup.add(resource);
 	}
 
 	protected File getJBossWSHomeFolder() {
 
 		String jbosshome = System.getProperty(JBOSSWS_42_HOME,
 				JBOSSWS_HOME_DEFAULT);
 		File runtimelocation = new File(jbosshome);
 		assertTrue("Please set JBoss EAP Home in system property:"
 				+ JBOSSWS_42_HOME, runtimelocation.exists());
 
 		String cmdFileLocation = jbosshome + File.separator + "bin"
 				+ File.separator + "wsconsume.sh";
 		assertTrue(jbosshome + " is not a valid jboss EAP home", new File(
 				cmdFileLocation).exists());
 		return runtimelocation;
 	}
 
 	protected void startup() {
 		startup(DEFAULT_STARTUP_TIME);
 	}
 
 	protected void startup(int maxWait) {
 		long finishTime = new Date().getTime() + maxWait;
 
 		// operation listener, which is only alerted when the startup is *done*
 		final StatusWrapper opWrapper = new StatusWrapper();
 		final IOperationListener listener = new IOperationListener() {
 			public void done(IStatus result) {
 				opWrapper.setStatus(result);
 			}
 		};
 
 		// a stream listener to listen for errors
 		ErrorStreamListener streamListener = new ErrorStreamListener();
 
 		// the thread to actually start the server
 		Thread startThread = new Thread() {
 			public void run() {
 				currentServer.start(ILaunchManager.RUN_MODE, listener);
 			}
 		};
 
 		startThread.start();
 
 		boolean addedStream = false;
 		while (finishTime > new Date().getTime()
 				&& opWrapper.getStatus() == null) {
 			// we're waiting for startup to finish
 			if (!addedStream) {
 				IStreamMonitor mon = getStreamMonitor();
 				if (mon != null) {
 					mon.addListener(streamListener);
 					addedStream = true;
 				}
 			}
 			try {
 				Display.getDefault().readAndDispatch();
 			} catch (SWTException swte) {
 			}
 		}
 
 		try {
 			assertTrue(
 					"Startup has taken longer than what is expected for a default startup",
 					finishTime >= new Date().getTime());
 			assertFalse("Startup had System.error output", streamListener
 					.hasError());
 		} catch (AssertionFailedError afe) {
 			// cleanup
 			currentServer.stop(true);
 			// rethrow
 			throw afe;
 		}
 		getStreamMonitor().removeListener(streamListener);
 	}
 
 	protected void shutdown() {
 		shutdown(DEFAULT_SHUTDOWN_TIME);
 	}
 
 	protected void shutdown(int maxWait) {
 		long finishTime = new Date().getTime() + maxWait;
 
 		// operation listener, which is only alerted when the startup is *done*
 		final StatusWrapper opWrapper = new StatusWrapper();
 		final IOperationListener listener = new IOperationListener() {
 			public void done(IStatus result) {
 				opWrapper.setStatus(result);
 			}
 		};
 
 		// a stream listener to listen for errors
 		ErrorStreamListener streamListener = new ErrorStreamListener();
 		if (getStreamMonitor() != null)
 			getStreamMonitor().addListener(streamListener);
 
 		// the thread to actually start the server
 		Thread stopThread = new Thread() {
 			public void run() {
 				currentServer.stop(false, listener);
 			}
 		};
 
 		stopThread.start();
 
 		while (finishTime > new Date().getTime()
 				&& opWrapper.getStatus() == null) {
 			// we're waiting for startup to finish
 			try {
 				Display.getDefault().readAndDispatch();
 			} catch (SWTException swte) {
 			}
 		}
 
 		try {
 			assertTrue(
 					"Startup has taken longer than what is expected for a default startup",
 					finishTime >= new Date().getTime());
 			assertFalse("Startup had System.error output", streamListener
 					.hasError());
 		} catch (AssertionFailedError afe) {
 			// cleanup
 			currentServer.stop(true);
 			// rethrow
 			throw afe;
 		}
 	}
 
 	protected IStreamMonitor getStreamMonitor() {
 		JBossServerBehavior behavior = (JBossServerBehavior) currentServer
 				.loadAdapter(JBossServerBehavior.class, null);
 		if (behavior != null) {
 			if (behavior.getProcess() != null) {
 				return behavior.getProcess().getStreamsProxy()
 						.getOutputStreamMonitor();
 			}
 		}
 		return null;
 	}
 
 	protected class ServerStateListener implements IServerListener {
 		private ArrayList<Integer> stateChanges;
 
 		public ServerStateListener() {
 			this.stateChanges = new ArrayList<Integer>();
 		}
 
 		public ArrayList<Integer> getStateChanges() {
 			return stateChanges;
 		}
 
 		public void serverChanged(ServerEvent event) {
 			if ((event.getKind() & ServerEvent.SERVER_CHANGE) != 0) {
 				if ((event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
 					if (event.getState() != IServer.STATE_STOPPED)
 						stateChanges.add(new Integer(event.getState()));
 				}
 			}
 		}
 	}
 
 	protected class ErrorStreamListener implements IStreamListener {
 		protected boolean errorFound = false;
 		String entireLog = "";
 
 		public void streamAppended(String text, IStreamMonitor monitor) {
 			entireLog += text;
 		}
 
 		// will need to be fixed or decided how to figure out errors
 		public boolean hasError() {
 			return errorFound;
 		}
 	}
 
 	public class StatusWrapper {
 		protected IStatus status;
 
 		public IStatus getStatus() {
 			return this.status;
 		}
 
 		public void setStatus(IStatus s) {
 			this.status = s;
 		}
 	}
 
 }
