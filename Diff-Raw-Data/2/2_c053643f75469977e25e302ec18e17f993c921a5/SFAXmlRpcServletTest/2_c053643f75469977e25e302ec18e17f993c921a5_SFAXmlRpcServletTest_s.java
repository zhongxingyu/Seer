 package org.fiteagle.delivery.xmlrpc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import javax.servlet.ServletException;
 
 import junit.framework.Assert;
 import org.fiteagle.delivery.xmlrpc.util.FITeagleUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 public class SFAXmlRpcServletTest {
 
 	private FITeagleServlet servlet;
 	private String AM_PATH = "/am/v3";;
 	private String REGISTRY_PATH = "/registry/v1";
 	
 	@Before
 	public void setUp() throws ServletException {
 		this.servlet = new FITeagleServlet();
 		
 	}
 
 	@Test
 	public void testResultGetVersionStatic() throws IOException {
 		final String getVersionResult = this.servlet
 				.handleRequestGetVersionStatic();
 		Assert.assertNotNull(getVersionResult);
 		Assert.assertFalse(getVersionResult.isEmpty());
 		
 	}
 
 	@Test
 	public void testResultListResourcesStatic() throws IOException {
 		final String listResourcesResult = this.servlet
 				.handleRequestListResourcesStatic();
 		Assert.assertNotNull(listResourcesResult);
 		Assert.assertFalse(listResourcesResult.isEmpty());
 	}
 
 	@Test
 	// TODO: @Daniel: to be fixed
 	public void testCallListResources() throws IOException {
 		
 		Writer writer = new StringWriter();
 		InputStream inputStream = FITeagleUtils
 				.getFileAsInputStream("/org/fiteagle/delivery/xmlrpc/sfa/listresources_request.xml");
 		this.servlet.handleRequest(inputStream, writer, AM_PATH);
 		String resultXML = writer.toString();
 		Assert.assertFalse(resultXML.isEmpty());
 		
 		//TODO: add a more sophisticated assertion here
 		
 	}
 
 	@Test
 	public void testCallGetVersion() throws IOException {
 		
 		Writer writer = new StringWriter();
 		InputStream inputStream = FITeagleUtils
 				.getFileAsInputStream("/org/fiteagle/delivery/xmlrpc/sfa/getversion_request.xml");
 		this.servlet.handleRequest(inputStream, writer, AM_PATH);
 		String resultXML = writer.toString();
 		Assert.assertFalse(resultXML.isEmpty());
 		Assert.assertTrue(resultXML.contains("<name>geni_code</name><value><int>0</int></value>"));
 		
 	}
 	
 	@Test
 	public void testCredentialsEmpty() throws IOException{
 //		Writer writer = new StringWriter();
 //		InputStream inputStream = FITeagleUtils
 //				.getFileAsInputStream("/org/fiteagle/delivery/xmlrpc/sfa/credentialsNotValid_listresources_request.xml");
 //		this.servlet.handleRequest(inputStream, writer, AM_PATH);
 //		String resultXML = writer.toString();
 //		Assert.assertTrue(resultXML.contains("<name>geni_code</name><value><int>1</int></value>"));
 		
 	}
 	
	@Test
 	public void testGetSelfCredential() throws IOException{
 	  Writer writer = new StringWriter();
 	  InputStream inputStream = FITeagleUtils.getFileAsInputStream("/org/fiteagle/delivery/xmlrpc/sfa/getSelfCredential_request.xml");
 	  this.servlet.handleRequest(inputStream, writer, REGISTRY_PATH);
 	  String resultXML = writer.toString();
 	  System.out.println(resultXML);
 	  Assert.assertFalse(resultXML.isEmpty());
 	  
 	}
 }
