 package com.delcyon.capo.tests.util;
 import java.io.File;
 import java.net.URLClassLoader;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.CapoApplication.ApplicationState;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.resourcemanager.types.FileResourceType;
 import com.delcyon.capo.resourcemanager.types.FileResourceContentMetaData.FileAttributes;
 import com.delcyon.capo.resourcemanager.types.ShellResourceDescriptor.Parameter;
 import com.delcyon.capo.tests.util.external.Util;
 import com.delcyon.capo.xml.XMLDiff;
 import com.delcyon.capo.xml.XPath;
 import com.delcyon.capo.xml.dom.ResourceDocument;
 
 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 /**
  * @author jeremiah
  * Starts up the test server in a different class loader to make testing of clients results possible 
  */
 public class ExternalTestServer
 {
 	
 	private URLClassLoader serverClassLoader;
 	@SuppressWarnings("rawtypes")
     private Class testServerClass;
 
 	
 	public ExternalTestServer() throws Exception
 	{
 		serverClassLoader = Util.getIndependentClassLoader();
 		testServerClass = serverClassLoader.loadClass("com.delcyon.capo.tests.util.TestServer");
 	}
 	
 	
 	@SuppressWarnings({ "unchecked" })
 	public  void startServer(String... args) throws Exception
 	{	   
 		testServerClass.getMethod("start",args.getClass()).invoke(null,(Object)args);
 	}
 	
 	@SuppressWarnings({ "unchecked" })
 	public  void shutdown() throws Exception
 	{
 	    System.err.println("=====================Calling Server Shutdown========================================");
 		testServerClass.getMethod("shutdown").invoke(null);
 	}
 	
 	@SuppressWarnings({ "unchecked" })
 	public  CopyOnWriteArrayList<Exception> getExceptionList() throws Exception
 	{
 		return (CopyOnWriteArrayList<Exception>) testServerClass.getMethod("getExceptionList").invoke(null);
 	}
 	
 	@Test
 	public void testExternalServer() throws Exception
 	{
 		Util.deleteTree("capo");
 	    Util.copyTree("test-data/capo", "capo");
 	    Util.copyTree("lib", "capo/server/lib");
 	    Util.setDefaultPreferences();	    
 		ExternalTestServer externalTestServer = new ExternalTestServer();
 		externalTestServer.startServer();
 		TestClient.start(ApplicationState.READY);
 		TestClient.shutdown();		
 		//CapoClient.main(new String[]{"-CLIENT_AS_SERVICE","false"});
 //		CapoClient capoClient = new CapoClient();
 //		capoClient.start(new String[]{});
 //		capoClient.shutdown();
 		//CapoClient.main();
 		//Thread.sleep(10000);
 		externalTestServer.shutdown();
 		
 		CopyOnWriteArrayList<Exception> exceptionList = externalTestServer.getExceptionList();
 		if (exceptionList.isEmpty() == false)
 		{
 			throw exceptionList.get(0);
 		}
 		
 		//verify that client got it's updates
 		
 		//CapoApplication.setVariable(FileResourceType.Parameters.ROOT_DIR.toString(), new File(".").getCanonicalPath());
 		String src = "capo/server/lib";
         String dest = "capo/client/lib";
         Assert.assertTrue(com.delcyon.capo.tests.util.Util.areSame(src, dest));
 		ResourceDescriptor sourceResourceDescriptor = new FileResourceType().getResourceDescriptor(src);
 		//Assert.assertTrue(sourceResourceDescriptor.getContentMetaData(null).exists());
         ResourceDescriptor destinationResourceDescriptor = new FileResourceType().getResourceDescriptor(dest);
         //Assert.assertTrue(destinationResourceDescriptor.getContentMetaData(null).exists());
         
         //use resource document to get results from both sides
         ResourceDocument baseDocument = new ResourceDocument(sourceResourceDescriptor);
         //XPath.dumpNode(baseDocument, System.out);
         ResourceDocument modDocument = new ResourceDocument(destinationResourceDescriptor);
         
         
         //use xml diff to generate diff between both side
         XMLDiff xmlDiff = new XMLDiff();
         xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.path.toString());
         xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.uri.toString());
         xmlDiff.addIgnoreableAttribute(null,ContentMetaData.Attributes.lastModified.toString());
         xmlDiff.addIgnoreableAttribute(null,FileAttributes.absolutePath.toString());
         xmlDiff.addIgnoreableAttribute(null,FileAttributes.canonicalPath.toString());
         Document diffDocument = xmlDiff.getDifferences(baseDocument, modDocument);
         //XPath.dumpNode(diffDocument, System.out);
         //verify that root element of xml diff contains mod = base
         baseDocument.close(LifeCycle.EXPLICIT);
         modDocument.close(LifeCycle.EXPLICIT);
        sourceResourceDescriptor.release(null);
        destinationResourceDescriptor.release(null);
         if (diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(XMLDiff.EQUALITY) == false)
         {
         	XPath.dumpNode(diffDocument, System.err);
         	
         }
         Assert.assertEquals("There is a difference between "+src+" and "+dest+" Client did not update correctly",XMLDiff.EQUALITY,diffDocument.getDocumentElement().getAttributeNS(XMLDiff.XDIFF_NAMESPACE_URI, XMLDiff.XDIFF_ELEMENT_ATTRIBUTE_NAME));
         
 	}
 }
  
  
 
