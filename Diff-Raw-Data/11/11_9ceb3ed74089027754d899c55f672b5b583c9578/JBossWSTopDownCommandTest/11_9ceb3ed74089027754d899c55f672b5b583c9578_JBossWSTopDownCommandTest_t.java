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
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.webapplication.ServletType;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.ServletMapping;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
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
 import org.jboss.tools.ws.creation.core.commands.ImplementationClassCreationCommand;
 import org.jboss.tools.ws.creation.core.commands.InitialCommand;
 import org.jboss.tools.ws.creation.core.commands.MergeWebXMLCommand;
 import org.jboss.tools.ws.creation.core.commands.WSDL2JavaCommand;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.ui.wsrt.JBossWebService;
 
 @SuppressWarnings("restriction")
 public class JBossWSTopDownCommandTest extends AbstractJBossWSCommandTest {
 	protected static final IWorkspace ws = ResourcesPlugin.getWorkspace();
 	protected static final IWorkbench wb = PlatformUI.getWorkbench();
 
 
 	
 	private static final String RuntimeName;
 	private static final boolean isDeployed;
 
 	static String wsdlFileName = "hello_world.wsdl";
 		
 	static {
 		RuntimeName = "testjbosswsruntime";
 		isDeployed = false;	
 	}
 
 	public JBossWSTopDownCommandTest() {
 	}
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		JBossWSRuntimeManager.getInstance().addRuntime(RuntimeName, getJBossWSHomeFolder().toString(), "", true);
 
 		//create jbossws web project
 		fproject = createJBossWSProject("JBossWSTestProject", isServerSupplied());
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 		
 		assertTrue(wsdlFile.exists());
 	}
 	
 	
 
 	protected void tearDown() throws Exception {
 		// Wait until all jobs is finished to avoid delete project problems
 		super.tearDown();
 		resourcesToCleanup.clear();
 		JBossWSRuntime runtime = JBossWSRuntimeManager.getInstance().findRuntimeByName(RuntimeName);
 		JBossWSRuntimeManager.getInstance().removeRuntime(runtime);	
 	}
 
 	
 	public void testInitialCommand() throws CoreException, ExecutionException{
 		
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
		model.setCustomPackage("org.apache.hello_world_soap_http");
 		//model.setWsdlURI(wsdlFile.getLocation().toOSString());
 
 		
 		WebServiceInfo info = new WebServiceInfo();
 		info.setWsdlURL(wsdlFile.getLocation().toOSString());
 		IWebService ws = new JBossWebService(info); 
 		
 		//test initial command
 		InitialCommand cmdInitial = new InitialCommand(model, ws, WebServiceScenario.TOPDOWN);
 		IStatus status = cmdInitial.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		
 		assertTrue(model.getServiceNames().contains("SOAPService"));
 		assertEquals(wsdlFile.getLocation().toOSString(), model.getWsdlURI());
 		assertTrue(model.getPortTypes().contains("Greeter"));
 		assertEquals("org.apache.hello_world_soap_http", model.getCustomPackage());
 		
 	}
 	
 	
 	public void testCodeGenerationCommand() throws ExecutionException{
 
 		ServiceModel model = createServiceModel();
 		IProject project = fproject.getProject();
 		//test wsdl2Javacommand
 		WSDL2JavaCommand cmdW2j = new WSDL2JavaCommand(model);
 		IStatus status = cmdW2j.execute(null, null);
 		assertTrue(status.getMessage(), status.getSeverity() != Status.ERROR);
 		
 		assertTrue(project.getFile("src/org/apache/hello_world_soap_http/Greeter.java").exists());
 		
 		// test ImplementationClassCreationCommand
 		model.setGenerateImplementatoin(false);
 		ImplementationClassCreationCommand cmdImpl = new ImplementationClassCreationCommand(model);
 		status = cmdImpl.execute(null, null);
 		assertTrue(status.getMessage(), status.getSeverity() != Status.ERROR);
 		assertFalse(project.getFile("src/org/apache/hello_world_soap_http/GreeterImpl.java").exists());
 		
 		model.setGenerateImplementatoin(true);
 		cmdImpl = new ImplementationClassCreationCommand(model);
 		status = cmdImpl.execute(null, null);
 		assertTrue(status.getMessage(), status.getSeverity() != Status.ERROR);
 		assertTrue("failed to generate implemenatation class", project.getFile("src/org/apache/hello_world_soap_http/GreeterImpl.java").exists());
 		
 		
 	}
 	
 	public void testMergeWebXMLCommand() throws ExecutionException{
 		ServiceModel model = createServiceModel();
 		model.setGenerateImplementatoin(true);
 		model.setUpdateWebxml(true);
 		model.setWebProjectName(fproject.getProject().getName());
 		model.addServiceClasses("org.apache.hello_world_soap_http.GreeterImpl");
 		MergeWebXMLCommand cmdweb = new MergeWebXMLCommand(model);
 		IStatus  status = cmdweb.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		
 		IProject project = fproject.getProject();
 		IFile webxml = project.getFile("WebContent/WEB-INF/web.xml");
 		assertTrue(webxml.exists());
 		IModelProvider provider = ModelProviderManager
 		.getModelProvider(project);
 		Object object = provider.getModelObject();
 		if (object instanceof WebApp) {
 			WebApp webApp = (WebApp) object;
 			assertTrue("failed to update web.xml ", webApp.getServlets().size() > 0);
 			Servlet servlet = (Servlet)webApp.getServlets().get(0);
 			assertEquals("the servlet with the name 'Greeger' was not created", servlet.getServletName(), "Greeter");
 			assertTrue("the servlet display names should contain 'Greeter'", servlet.getDisplayNames().contains("Greeter"));
 			assertEquals("org.apache.hello_world_soap_http.GreeterImpl", servlet.getServletClass());
 			
 			ServletMapping mapping = (ServletMapping)webApp.getServletMappings().get(0);
 			assertTrue("url patterns should contain '/Greeter'", mapping.getUrlPatterns().contains("/Greeter"));
 			assertEquals("Greeter", mapping.getServletName());
 		}else if(object instanceof org.eclipse.jst.j2ee.webapplication.WebApp){
 			org.eclipse.jst.j2ee.webapplication.WebApp webApp = (org.eclipse.jst.j2ee.webapplication.WebApp) object;
 			assertTrue("failed to update web.xml ", webApp.getServlets().size() > 0);
 			org.eclipse.jst.j2ee.webapplication.Servlet servlet = (org.eclipse.jst.j2ee.webapplication.Servlet)webApp.getServlets().get(0);
 			assertEquals("a servlet with the name 'Greeger' should be created", servlet.getServletName(), "Greeter");
 			assertEquals("servlet display name:","Greeter", servlet.getDisplayName());
 			if(servlet.getWebType() instanceof ServletType){
 				ServletType webtype = (ServletType)servlet.getWebType();
 				assertEquals("org.apache.hello_world_soap_http.GreeterImpl", webtype.getClassName());
 			}
 			
 			org.eclipse.jst.j2ee.webapplication.ServletMapping mapping = (org.eclipse.jst.j2ee.webapplication.ServletMapping)webApp.getServletMappings().get(0);
 			assertEquals("url pattern: ","/Greeter", mapping.getUrlPattern());
 			assertEquals("Greeter", mapping.getServlet().getServletName());
 		}
 		//ServerType d; d.createServer(id, file, monitor)
 		
 	}
 	
 	public void testDeployResult() throws ExecutionException, CoreException, IOException{
 		
 		//currentServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
		model.setCustomPackage("org.apache.hello_world_soap_http");
 		//model.setWsdlURI(wsdlFile.getLocation().toOSString());
 
 		
 		WebServiceInfo info = new WebServiceInfo();
 		info.setWsdlURL(wsdlFile.getLocation().toOSString());
 		IWebService ws = new JBossWebService(info); 
 		
 		//test initial command
 		AbstractDataModelOperation cmd = new InitialCommand(model, ws, WebServiceScenario.TOPDOWN);
 		IStatus status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.getSeverity() != Status.ERROR);
 		
 		cmd = new WSDL2JavaCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.getSeverity() != Status.ERROR);
 		
 		cmd = new ImplementationClassCreationCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.getSeverity() != Status.ERROR);
 		
 		cmd = new MergeWebXMLCommand(model);
 		status = cmd.execute(null, null);
 		assertTrue(status.getMessage(), status.isOK());
 		
 		fproject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 		fproject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
 		publishWebProject();
 		
 		assertTrue(currentServer.getModules().length > 0);
 		String webServiceUrl = "http://localhost:8080/JBossWSTestProject/Greeter?wsdl";
 		URL url = new URL(webServiceUrl);
 		URLConnection conn =  url.openConnection();
 		
 		startup();
 		
 		assertEquals("unable to start JBoss server",IServer.STATE_STARTED, currentServer.getServerState());
 
 		conn.connect();
 		conn.getContent();
 		
 		
 		
 	}
 	
 	protected ServiceModel createServiceModel(){
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(fproject.getProject().getName());
 		IFile wsdlFile = fproject.getProject().getFile(wsdlFileName);
 		model.setWsdlURI(wsdlFile.getLocation().toOSString());
 		model.addServiceName("SOAPService");
 		model.addPortTypes("Greeter");
 		model.setCustomPackage("org.apache.hello_world_soap_http");
 		
 		return model;
 		
 	}
 	
 	
 	protected boolean isServerSupplied(){
 		return false;
 	}
 	
 	
 	
 	protected IDataModel createJBossWSDataModel( boolean isServerSupplied) {
 		IDataModel config = (IDataModel) new JBossWSFacetInstallDataModelProvider().create();
 		if(isServerSupplied){
 			config.setBooleanProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_IS_SERVER_SUPPLIED, true);
 		}else{
 			config.setBooleanProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_DEPLOY, isDeployed);
 			config.setStringProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_ID, RuntimeName);
 			config.setStringProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_HOME, getJBossWSHomeFolder().toString());
 		}
 		return config;
 	}
 
 
 
 
 }
