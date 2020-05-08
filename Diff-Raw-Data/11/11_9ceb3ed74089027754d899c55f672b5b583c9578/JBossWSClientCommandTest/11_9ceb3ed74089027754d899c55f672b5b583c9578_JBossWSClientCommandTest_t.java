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
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.ws.internal.wsrt.IWebServiceClient;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceClientInfo;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceScenario;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntime;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntimeManager;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntimeClassPathInitializer.JBossWSRuntimeClasspathContainer;
 import org.jboss.tools.ws.core.facet.delegate.IJBossWSFacetDataModelProperties;
 import org.jboss.tools.ws.core.facet.delegate.JBossWSFacetInstallDataModelProvider;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCorePlugin;
 import org.jboss.tools.ws.creation.core.commands.ClientSampleCreationCommand;
 import org.jboss.tools.ws.creation.core.commands.InitialClientCommand;
 import org.jboss.tools.ws.creation.core.commands.RemoveClientJarsCommand;
 import org.jboss.tools.ws.creation.core.commands.WSDL2JavaCommand;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.ui.wsrt.JBossWebServiceClient;
 
 /**
  * @author Grid Qian
  */
 @SuppressWarnings("restriction")
 public class JBossWSClientCommandTest extends AbstractJBossWSCommandTest {
 
 	private static final String RuntimeName;
 	private static final boolean isDeployed;
 
 	static {
 		RuntimeName = "testjbosswsruntime";
 		isDeployed = false;
 	}
 
 	public JBossWSClientCommandTest() {
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		JBossWSRuntimeManager.getInstance().addRuntime(RuntimeName,
 				getJBossWSHomeFolder().toString(), "", true);
 
 		// create jbossws web project
 		fproject = createJBossWSProject("JBossWSTestProject",
 				isServerSupplied());
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 
 		assertTrue(wsdlFile.exists());
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		resourcesToCleanup.clear();
 		JBossWSRuntime runtime = JBossWSRuntimeManager.getInstance()
 				.findRuntimeByName(RuntimeName);
 		JBossWSRuntimeManager.getInstance().removeRuntime(runtime);
 	}
 
 	public void testInitialClientCommand() throws CoreException,
 			ExecutionException {
 
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
		model.setCustomPackage("org.apache.hello_world_soap_http");
 		// model.setWsdlURI(wsdlFile.getLocation().toOSString());
 
 		WebServiceClientInfo info = new WebServiceClientInfo();
 		info.setWsdlURL(wsdlFile.getLocation().toOSString());
 		IWebServiceClient ws = new JBossWebServiceClient(info);
 
 		// test initial command
 		InitialClientCommand cmdInitial = new InitialClientCommand(model, ws,
 				WebServiceScenario.CLIENT);
 		IStatus status = cmdInitial.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 
 		assertTrue(model.getServiceNames().contains("SOAPService"));
 		assertEquals(wsdlFile.getLocation().toOSString(), model.getWsdlURI());
 		assertTrue(model.getPortTypes().contains("Greeter"));
 		assertEquals("org.apache.hello_world_soap_http", model
 				.getCustomPackage());
 	}
 
 	public void testClientCodeGenerationCommand() throws ExecutionException {
 
 		ServiceModel model = createServiceModel();
 		IProject project = fproject.getProject();
 		// test wsdl2Javacommand
 		WSDL2JavaCommand cmdW2j = new WSDL2JavaCommand(model);
 		IStatus status = cmdW2j.execute(null, null);
 		assertFalse(status.getMessage(), Status.ERROR == status.getSeverity());
 		assertTrue(project.getFile(
 				"src/org/apache/hello_world_soap_http/Greeter.java").exists());
 
 		// test ClientSampleCreationCommand
 		ClientSampleCreationCommand cmdImpl = new ClientSampleCreationCommand(
 				model);
 		status = cmdImpl.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		assertTrue(
 				"failed to generate sample class",
 				project
 						.getFile(
 								"src/org/apache/hello_world_soap_http/clientsample/ClientSample.java")
 						.exists());
 
 	}
 
 	public void testRemoveClientJarsCommand() throws ExecutionException {
 
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 
 		RemoveClientJarsCommand command = new RemoveClientJarsCommand(model);
 		IStatus status = command.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		try {
 			IClasspathEntry[] entries = getJavaProjectByName(
 					fproject.getProject().getName()).getRawClasspath();
 
 			for (IClasspathEntry entry : entries) {
 				IClasspathContainer container = JavaCore.getClasspathContainer(
 						entry.getPath(), getJavaProjectByName(fproject
 								.getProject().getName()));
 				if (container instanceof JBossWSRuntimeClasspathContainer) {
 					boolean nojar = true;
 					for (IClasspathEntry jar : ((JBossWSRuntimeClasspathContainer) container)
 							.getClasspathEntries()) {
 						if (jar.getPath().toString().contains("jaxws-rt.jar")) {
 							nojar = false;
 						}
 					}
 					assertTrue(nojar);
 				}
 			}
 		} catch (JavaModelException e) {
 			JBossWSCreationCorePlugin.getDefault().logError(e);
 		}
 
 	}
 
 	public static IJavaProject getJavaProjectByName(String projectName)
 			throws JavaModelException {
 
 		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace()
 				.getRoot());
 		model.open(null);
 
 		IJavaProject[] projects = model.getJavaProjects();
 
 		for (IJavaProject proj : projects) {
 			if (proj.getProject().getName().equals(projectName)) {
 				return proj;
 			}
 		}
 
 		return null;
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
