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
 package org.jboss.tools.ws.core.test.command;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.AbstractConsole;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleListener;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.TextConsole;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.ws.internal.wsrt.IWebService;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceInfo;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceScenario;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntime;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntimeManager;
 import org.jboss.tools.ws.core.facet.delegate.IJBossWSFacetDataModelProperties;
 import org.jboss.tools.ws.core.facet.delegate.JBossWSFacetInstallDataModelProvider;
 import org.jboss.tools.ws.creation.core.commands.InitialCommand;
 import org.jboss.tools.ws.creation.core.commands.MergeWebXMLCommand;
 import org.jboss.tools.ws.creation.core.commands.RemoveClientJarsCommand;
 import org.jboss.tools.ws.creation.core.commands.ValidateWSImplCommand;
 import org.jboss.tools.ws.creation.core.commands.Java2WSCommand;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.ui.wsrt.JBossWebService;
 
 /**
  * @author Grid Qian
  */
 public class JBossWSJavaFirstCommandTest extends AbstractJBossWSCommandTest {
 	protected static final IWorkspace ws = ResourcesPlugin.getWorkspace();
 	protected static final IWorkbench wb = PlatformUI.getWorkbench();
 
	protected static final String JBOSSWS_HOME_DEFAULT = "D:\\softinstall\\jboss-4.2.3GA\\jboss-4.2.3.GA";
 	private static final String RuntimeName;
 	private static final boolean isDeployed;
 
 	static {
 		RuntimeName = "testjbosswsruntime";
 		isDeployed = false;
 	}
 
 	public JBossWSJavaFirstCommandTest() {
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		JBossWSRuntimeManager.getInstance().addRuntime(RuntimeName,
 				getJBossWSHomeFolder().toString(), "", true);
 		// create jbossws web project
 		fproject = createJBossWSProject("JavaFirstTestProject",
 				isServerSupplied());
 
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 		resourcesToCleanup.clear();
 		JBossWSRuntime runtime = JBossWSRuntimeManager.getInstance()
 				.findRuntimeByName(RuntimeName);
 		JBossWSRuntimeManager.getInstance().removeRuntime(runtime);
 	}
 
 	public void testInitialCommand() throws CoreException, ExecutionException {
 
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 		WebServiceInfo info = new WebServiceInfo();
 		info.setImplURL("org.example.www.helloworld.HelloWorld");
 		IWebService ws = new JBossWebService(info);
 
 		// test initial command
 		InitialCommand cmdInitial = new InitialCommand(model, ws,
 				WebServiceScenario.BOTTOMUP);
 		IStatus status = cmdInitial.execute(null, null);
 
 		assertTrue(status.getMessage(), status.isOK());
 		assertTrue(model.getServiceClasses().get(0).equals(
 				"org.example.www.helloworld.HelloWorld"));
 	}
 
 	public void testValidateWSImplCommand() throws ExecutionException {
 
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 		model.addServiceClasses("org.example.www.helloworld.HelloWorld");
 
 		ValidateWSImplCommand command = new ValidateWSImplCommand(model);
 		IStatus status = command.execute(null, null);
 
 		assertTrue(status.getMessage(), status.isOK());
 
 	}
 
 	public void testWSProviderInvokeCommand() throws ExecutionException, CoreException {
 
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 		model.addServiceClasses("org.example.www.helloworld.HelloWorld");
 		model.setGenWSDL(true);
 		IProject project = fproject.getProject();
 		
 		fproject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 		fproject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
 		
 		Java2WSCommand command = new Java2WSCommand(model);
 		IStatus status = command.execute(null, null);
 
 		assertFalse(status.getMessage(), Status.ERROR == status.getSeverity());
 		assertTrue(project.getFile(
 				"src/org/example/www/helloworld/jaxws/SayHello.java").exists());
		assertTrue(project.getFile("wsdl/HelloWorldService.wsdl")
 				.exists());
 	}
 
 	public void testDeployResult() throws ExecutionException, CoreException,
 			IOException {
 
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 
 		WebServiceInfo info = new WebServiceInfo();
 		info.setImplURL("org.example.www.helloworld.HelloWorld");
 		IWebService ws = new JBossWebService(info);
 
 		// test initial command
 		AbstractDataModelOperation cmd = new InitialCommand(model, ws,
 				WebServiceScenario.BOTTOMUP);
 		IStatus status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		
 		fproject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 		fproject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
 		
 		cmd = new Java2WSCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 
 		cmd = new MergeWebXMLCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 
 		fproject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 		fproject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
 		publishWebProject();
 
 		assertTrue(currentServer.getModules().length > 0);
 		String webServiceUrl = "http://localhost:8080/JavaFirstTestProject/HelloWorld?wsdl";
 		URL url = new URL(webServiceUrl);
 		URLConnection conn = url.openConnection();
 
 		startup();
 
 		assertEquals("unable to start JBoss server", IServer.STATE_STARTED,
 				currentServer.getServerState());
 
 		conn.connect();
 		conn.getContent();
 
 		IProject pro = createProject("ClientTest");
 		model = new ServiceModel();
 		model.setWebProjectName("ClientTest");
 
 		cmd = new RemoveClientJarsCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 
 		pro.open(null);
 		pro.refreshLocal(IResource.DEPTH_INFINITE, null);
 
 		ILaunchManager launchManager = DebugPlugin.getDefault()
 				.getLaunchManager();
 		ILaunchConfigurationType launchConfigurationType = launchManager
 				.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
 		ILaunchConfigurationWorkingCopy wc = launchConfigurationType
 				.newInstance(null, "ClientSample");
 		wc.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", "1");
 		wc.setAttribute("org.eclipse.jdt.launching.MAIN_TYPE",
 				"org.example.www.helloworld.clientsample.ClientSample");
 		wc.setAttribute("org.eclipse.jdt.launching.PROGRAM_ARGUMENTS", "Test");
 		wc.setAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", "ClientTest");
 		wc.doSave();
 		wc.launch(ILaunchManager.RUN_MODE, null);
 		IConsoleManager consolemanager = getConsoleManager();
 		checkText(consolemanager.getConsoles());
 	}
 
 	private void checkText(IConsole[] consoles) {
 		// test run result
 		for (IConsole console : consoles) {
 			if (console.getName().contains("ClientSample")) {
 				int i = 0;
 				while (i < 10
 						&& !isContainString(
 								console,
 								JBossWSCreationCoreMessages.Client_Sample_Run_Over)) {
 					delay(1000);
 					i++;
 				}
 				assertTrue("Sample run over!", isContainString(console,
 						JBossWSCreationCoreMessages.Client_Sample_Run_Over));
 			}
 		}
 	}
 
 	public static boolean isContainString(IConsole console, String str) {
 		return ((TextConsole) console).getDocument().get().contains(str);
 	}
 
 	public static void delay(long durationInMilliseconds) {
 		Display display = Display.getCurrent();
 		if (display != null) {
 			long t2 = System.currentTimeMillis() + durationInMilliseconds;
 			while (System.currentTimeMillis() < t2) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 			display.update();
 		} else {
 			try {
 				Thread.sleep(durationInMilliseconds);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	public static IConsoleManager getConsoleManager() {
 		IConsoleManager consolemanager = ConsolePlugin.getDefault()
 				.getConsoleManager();
 
 		consolemanager.addConsoleListener(new IConsoleListener() {
 			public void consolesAdded(IConsole[] consoles) {
 				for (int i = 0; i < consoles.length; i++) {
 					((AbstractConsole) consoles[i]).activate();
 				}
 
 			}
 
 			public void consolesRemoved(IConsole[] consoles) {
 				for (int i = 0; i < consoles.length; i++) {
 					((AbstractConsole) consoles[i]).destroy();
 				}
 
 			}
 		});
 		return consolemanager;
 	}
 
 	@Override
 	IDataModel createJBossWSDataModel(boolean isServerSupplied) {
 		IDataModel config = (IDataModel) new JBossWSFacetInstallDataModelProvider()
 				.create();
 		if (isServerSupplied) {
 			config
 					.setBooleanProperty(
 							IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_IS_SERVER_SUPPLIED,
 							true);
 		} else {
 			config.setBooleanProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_DEPLOY,
 					isDeployed);
 			config.setStringProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_ID,
 					RuntimeName);
 			config.setStringProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_HOME,
 					getJBossWSHomeFolder().toString());
 		}
 		return config;
 	}
 
 }
