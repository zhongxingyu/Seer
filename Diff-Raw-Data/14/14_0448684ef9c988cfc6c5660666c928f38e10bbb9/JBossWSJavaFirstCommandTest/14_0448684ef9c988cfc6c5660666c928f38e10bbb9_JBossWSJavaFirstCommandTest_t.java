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
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.TextConsole;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.ws.internal.wsrt.IWebService;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceInfo;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceScenario;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.ws.creation.core.commands.InitialCommand;
 import org.jboss.tools.ws.creation.core.commands.Java2WSCommand;
 import org.jboss.tools.ws.creation.core.commands.MergeWebXMLCommand;
 import org.jboss.tools.ws.creation.core.commands.RemoveClientJarsCommand;
 import org.jboss.tools.ws.creation.core.commands.ValidateWSImplCommand;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.core.test.util.JBossWSCreationCoreTestUtils;
 import org.jboss.tools.ws.creation.ui.wsrt.JBossWebService;
 
 /**
  * @author Grid Qian
  */
 @SuppressWarnings("restriction")
 public class JBossWSJavaFirstCommandTest extends AbstractJBossWSGenerationTest {
 	private IProject clientProject;
 
 	public JBossWSJavaFirstCommandTest() {
 	}
 
 	public void setUp() throws Exception {
 		super.setUp();
 
 		//create jbossws web project
 		fproject = createJBossWSProject("JavaFirstTestProject");
 		model = createServiceModel();
 		
 		if (!ResourcesPlugin.getWorkspace().getRoot().getProject("ClientTest")
 				.exists()) {
 			createProject("ClientTest");
 		}
 	}
 	
 
 	public void testDeployResult() throws ExecutionException, CoreException,IOException {
 		doInitialCommand();
 		doValidateWSImplCommand();
 		doJava2WSCommand();
 
 		IProject project = fproject.getProject();
 		project.refreshLocal(IResource.DEPTH_INFINITE, null);
 		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
 		
 		startup(currentServer);
 		publishWebProject();
		JobUtils.delay(18000);
		String webServiceUrl = "http://localhost:8080/JavaFirstTestProject/HelloWorld?wsdl";
 		URL url = new URL(webServiceUrl);
 		URLConnection conn = url.openConnection();
 		assertEquals("unable to start JBoss server", IServer.STATE_STARTED,currentServer.getServerState());
 		conn.connect();
 		assertFalse("The url connection's status is "+ ((HttpURLConnection) conn).getResponseMessage(), "Ok".equals(((HttpURLConnection) conn).getResponseMessage()));
 
 		model.setWebProjectName("ClientTest");
 		RemoveClientJarsCommand cmd = new RemoveClientJarsCommand(model);
 		assertTrue(cmd.execute(null, null).getMessage(), cmd.execute(null, null).isOK());
 
 		clientProject = ResourcesPlugin.getWorkspace().getRoot().getProject("ClientTest");
 		clientProject.open(null);
 		clientProject.refreshLocal(IResource.DEPTH_INFINITE, null);
 
 		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
 		ILaunchConfigurationWorkingCopy wc = launchConfigurationType.newInstance(null, "ClientSample");
 		wc.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", "1");
 		wc.setAttribute("org.eclipse.jdt.launching.MAIN_TYPE","org.example.www.helloworld.clientsample.ClientSample");
 		wc.setAttribute("org.eclipse.jdt.launching.PROGRAM_ARGUMENTS", "Test");
 		wc.setAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", "ClientTest");
 		wc.doSave();
 		wc.launch(ILaunchManager.RUN_MODE, null);
 		IConsoleManager consolemanager = JBossWSCreationCoreTestUtils.getConsoleManager();
 		checkText(consolemanager.getConsoles());
 	}
 
 	public void doInitialCommand() throws CoreException, ExecutionException {
 		WebServiceInfo info = new WebServiceInfo();
 		info.setImplURL("org.example.www.helloworld.HelloWorld");
 		IWebService ws = new JBossWebService(info);
 
 		// test initial command
 		InitialCommand cmdInitial = new InitialCommand(model, ws, WebServiceScenario.BOTTOMUP);
 		IStatus status = cmdInitial.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		assertTrue(model.getServiceClasses().get(0).equals("org.example.www.helloworld.HelloWorld"));
 	}
 
 	public void doValidateWSImplCommand() throws ExecutionException {
 		ValidateWSImplCommand command = new ValidateWSImplCommand(model);
 		IStatus status = command.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 	}
 
 	public void doJava2WSCommand() throws ExecutionException, CoreException {
 		model.setGenWSDL(true);
 		IProject project = fproject.getProject();
 		Java2WSCommand command = new Java2WSCommand(model);
 		IStatus status = command.execute(null, null);
 		assertFalse(status.getMessage(), Status.ERROR == status.getSeverity());
 		assertTrue(project.getFile("src/org/example/www/helloworld/jaxws/SayHello.java").exists());
 		assertTrue(project.getFile("wsdl/HelloWorldService.wsdl").exists());
 		
 		MergeWebXMLCommand cmd = new MergeWebXMLCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 	}
 
 	private void checkText(IConsole[] consoles) {
 		// test run result
 		for (IConsole console : consoles) {
 			if (console.getName().contains("ClientSample")) {
 				int i = 0;
 				while (i < 30&& !isContainString(console,JBossWSCreationCoreMessages.Client_Sample_Run_Over)) {
 					JBossWSCreationCoreTestUtils.delay(1000);
 					i++;
 				}
 				assertTrue(((TextConsole) console).getDocument().get(),isContainString(console,JBossWSCreationCoreMessages.Client_Sample_Run_Over));
 			}
 		}
 	}
 
 	public static boolean isContainString(IConsole console, String str) {
 		return ((TextConsole) console).getDocument().get().contains(str);
 	}
 }
