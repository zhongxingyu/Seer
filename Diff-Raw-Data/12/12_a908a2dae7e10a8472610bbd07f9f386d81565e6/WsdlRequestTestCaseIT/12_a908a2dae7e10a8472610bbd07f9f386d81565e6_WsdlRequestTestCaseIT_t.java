 /*
  *  soapUI, copyright (C) 2004-2012 smartbear.com 
  *
  *  soapUI is free software; you can redistribute it and/or modify it under the 
  *  terms of version 2.1 of the GNU Lesser General Public License as published by 
  *  the Free Software Foundation.
  *
  *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU Lesser General Public License for more details at gnu.org.
  */
 
 package com.eviware.soapui.impl.wsdl;
 
 import static org.junit.Assert.assertNotNull;
 
 import com.eviware.soapui.support.JettyTestCaseBase;
 import junit.framework.JUnit4TestAdapter;
 
 import org.junit.Test;
 
 import com.eviware.soapui.impl.WsdlInterfaceFactory;
 import com.eviware.soapui.model.iface.Response;
 import com.eviware.soapui.support.TestCaseWithJetty;
 
 // TODO Move this integration test to the it folder.
 public class WsdlRequestTestCaseIT extends JettyTestCaseBase
 {
 
 	@Test
 	public void testRequest() throws Exception
 	{
 		replaceInFile("test1/TestService.wsdl","8082","" + getPort());
		replaceInFile("test1/TestService.wsdl","www.eviware.com","localhost:" + getPort());

 		// create new project
 		WsdlProject project = new WsdlProject();
 
 		// import amazon wsdl
 		WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, "http://localhost:" + getPort() + "/test1/TestService.wsdl",
 true )[0];
 
 		// get "Help" operation
 		WsdlOperation operation = ( WsdlOperation )iface.getOperationByName( "GetPage" );
 
 		// create a new empty request for that operation
 		WsdlRequest request = operation.addNewRequest( "My request" );
 
 		// generate the request content from the schema
 		request.setRequestContent( operation.createRequest( true ) );
 
 		// submit the request
 		WsdlSubmit submit = ( WsdlSubmit )request.submit( new WsdlSubmitContext( request ), false );
 
 		// wait for the response
 		Response response = submit.getResponse();
 
 		// print the response
 		String content = response.getContentAsString();
 		// System.out.println( content );
 		assertNotNull( content );
 	}
 }
