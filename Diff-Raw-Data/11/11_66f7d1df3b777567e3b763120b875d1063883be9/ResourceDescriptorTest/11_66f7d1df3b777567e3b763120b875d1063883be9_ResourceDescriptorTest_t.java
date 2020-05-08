 package com.delcyon.capo.resourcemanager;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.datastream.StreamUtil;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.tests.util.TestServer;
 import com.delcyon.capo.tests.util.Util;
 import com.delcyon.capo.xml.XMLDiff;
 import com.delcyon.capo.xml.XPath;
 import com.delcyon.capo.xml.cdom.CElement;
 
 public abstract class ResourceDescriptorTest
 {
     protected ResourceDescriptor resourceDescriptor = null;
     
     @BeforeClass
     public static void setUpBeforeClass() throws Exception
     {
         Util.copyTree("test-data/capo", "capo", true, true);
         TestServer.start();
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception
     {
         TestServer.shutdown();
     }
    
     
     @Before
     public void setUp() throws Exception
     {
     	Util.copyTree("test-data/capo", "capo", true, true);
         this.resourceDescriptor = getResourceDescriptor();
     }
 
     protected abstract ResourceDescriptor getResourceDescriptor() throws Exception;
 
     @After
     public void tearDown() throws Exception
     {
    	if(this.resourceDescriptor != null)
    	{
    		this.resourceDescriptor.release(null);
    	}
     }
 
     @Test
     public abstract void testGetSupportedStreamTypes() throws Exception;
 
     @Test
     public abstract void testIsSupportedStreamType() throws Exception;
 
     @Test
     public abstract void testGetSupportedStreamFormats() throws Exception;
 
     @Test
     public abstract void testIsSupportedStreamFormat() throws Exception;
 
     @Test  
     public void testGetResourceState() throws Exception
     {
         resourceDescriptor.reset(State.NONE);
         Assert.assertSame(State.NONE,resourceDescriptor.getResourceState());
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         Assert.assertSame(State.INITIALIZED,resourceDescriptor.getResourceState());
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.open(null);
         Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.close(null);
         Assert.assertSame(State.CLOSED,resourceDescriptor.getResourceState());
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.release(null);
         Assert.assertSame(State.RELEASED,resourceDescriptor.getResourceState());
     }
 
     
 
     @Test    
     public void testPerformAction() throws Exception
     {
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         if (resourceDescriptor.isSupportedAction(Action.DELETE))
         {
         	Assert.assertTrue(resourceDescriptor.getResourceMetaData(null).exists());
         	resourceDescriptor.performAction(null, Action.DELETE);        
         	Assert.assertTrue(resourceDescriptor.getResourceMetaData(null).exists() == false);
         }
         if(resourceDescriptor.isSupportedAction(Action.CREATE))
         {
         	resourceDescriptor.performAction(null, Action.CREATE);
         	Assert.assertTrue(resourceDescriptor.getResourceMetaData(null).exists());
         }
     }
 
 
     
 
     
 
     @Test
     public void testIsSupportedAction() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         Assert.assertNotNull(resourceDescriptor.isSupportedAction(Action.CREATE));
         Assert.assertNotNull(resourceDescriptor.isSupportedAction(Action.DELETE));
     }
 
     @Test
     public void testIsRemoteResource() throws Exception
     {
     	Assert.assertTrue(resourceDescriptor.isRemoteResource() == false);
         
     }
 
     @Test
     public void testSetup() throws Exception
     {
         ResourceDescriptor  resourceDescriptor = this.resourceDescriptor.getResourceType().getResourceDescriptor(this.resourceDescriptor.getResourceURI().getResourceURIString());
         Assert.assertSame(State.NONE,resourceDescriptor.getResourceState());
         Assert.assertSame(this.resourceDescriptor.getResourceType(),resourceDescriptor.getResourceType());
         Assert.assertEquals(this.resourceDescriptor.getResourceURI(),resourceDescriptor.getResourceURI());
         Assert.assertSame(this.resourceDescriptor.getResourceType().getDefaultLifeCycle(),resourceDescriptor.getLifeCycle());
         resourceDescriptor.release(null);
     }
 
     @Test
     public void testInit() throws Exception
     {
     	ResourceDescriptor  resourceDescriptor = this.resourceDescriptor.getResourceType().getResourceDescriptor(this.resourceDescriptor.getResourceURI().getResourceURIString());
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         Assert.assertSame(State.INITIALIZED,resourceDescriptor.getResourceState());
         Assert.assertNotNull(resourceDescriptor.getResourceURI());
         Assert.assertNotNull(resourceDescriptor.getLocalName());
         //TODO check for initialization content meta data
         resourceDescriptor.release(null);
     }
 
     @Test
     public void testOpen() throws Exception
     {
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
       //TODO check for open content meta data
     }
 
     @Test
     public void testReadXML() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         resourceDescriptor.next(null);
         Assert.assertSame(State.STEPPING,resourceDescriptor.getResourceState());
         Element element = resourceDescriptor.readXML(null);
         Assert.assertNotNull(element);
         Assert.assertTrue(element.hasChildNodes() || element.hasAttributes());
         int count = 0;
         
         if(element.hasChildNodes())
         {
             NodeList nodeList = element.getChildNodes();
             for(int index = 0; index < nodeList.getLength(); index++)
             {
                 if (nodeList.item(index).getNodeType() == Node.ELEMENT_NODE)
                 {
                     count++;
                 }
             }
         }
         if(element.hasAttributes())
         {
             NamedNodeMap namedNodeMap = element.getAttributes();
             for(int index = 0; index < namedNodeMap.getLength(); index++)
             {
                 if (namedNodeMap.item(index).getNodeType() == Node.ATTRIBUTE_NODE)
                 {
                     count++;
                 }
             }
         }
         System.out.println("readXML found "+count+" child elements");
         XPath.dumpNode(element, System.out);       
         Assert.assertTrue(count > 0 || element.getTextContent().trim().isEmpty() == false);
         resourceDescriptor.release(null);
     }
 
     
 
     @Test
     public void testReadBlock() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         resourceDescriptor.next(null);
         Assert.assertSame(State.STEPPING,resourceDescriptor.getResourceState());
         byte[] data = resourceDescriptor.readBlock(null);
         Assert.assertTrue(data.length > 10);
         System.out.println("Read the following bloack data: '"+new String(data)+"'");
         
     }
 
     @Test
     public void testWriteXML() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT,StreamFormat.XML_BLOCK))
         {
         	Document document = CapoApplication.getDocumentBuilder().newDocument();
         	CElement rootElement = (CElement) document.createElementNS("BSNS","ns:testRootElement");
         	rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:ns","BSNS");
         	rootElement.setTextContent("This is a test");
         	resourceDescriptor.writeXML(null, rootElement);
         	Element readElement = resourceDescriptor.readXML(null);
         	XMLDiff xmlDiff = new XMLDiff();
         	Element diffElement = xmlDiff.getDifferences(rootElement, readElement);
         	if (XMLDiff.EQUALITY.equals(diffElement.getAttribute("xdiff:element")) == false)
         	{
         	    XPath.dumpNode(diffElement, System.err);
         	}
         	Assert.assertEquals(XMLDiff.EQUALITY,diffElement.getAttribute("xdiff:element"));
         }
     }
     
     
     @Test
     public void testWriteBlock() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT,StreamFormat.BLOCK))
         {
         	resourceDescriptor.writeBlock(null,"this is a test".getBytes());
         	Assert.assertArrayEquals("this is a test".getBytes(),resourceDescriptor.readBlock(null));
         }
         
     }
 
     @Test
     public void testNext() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, true);
         resourceDescriptor.open(null);
         int recordCount = 0;
         int hashCode = 0; 
         while(resourceDescriptor.next(null))
         {
             recordCount++;
             int tmpHashCode = new String(resourceDescriptor.readBlock(null)).hashCode();
             Assert.assertTrue("records don't differ",hashCode != tmpHashCode);
             hashCode = tmpHashCode;            
         }
         Assert.assertTrue(recordCount > 0);
 
         
     }
 
     @Test
     public void testProcessOutput() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.PROCESS))
         {
         	resourceDescriptor.processOutput(null);
         	//TODO figure out what exactly we expect to happen here, or if this method is even useful.        	
         }
         else
         {
         	//skip, do nothing because it isn't supported
         }
         
     }
 
     @Test
     public void testProcessInput() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.PROCESS))
         {
         	resourceDescriptor.processInput(null);
         	//TODO figure out what exactly we expect to happen here, or if this method is even useful.
         }
         else
         {
         	//skip, do nothing because it isn't supported
         }
         
     }
 
     @Test
     public void testGetInputStream() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         resourceDescriptor.next(null);
         if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.STREAM))
         {
         	InputStream inputStream = resourceDescriptor.getInputStream(null);
         	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         	Assert.assertTrue("expcted test data to be atleast 10 chars in length.",getExpectedResourceContentPrefix().length() > 10);
         	Assert.assertTrue(StreamUtil.readInputStreamIntoOutputStream(inputStream, byteArrayOutputStream) > 10);        	
         	Assert.assertTrue("Expected in put stream to start with'"+getExpectedResourceContentPrefix()+"' but got '"+new String(byteArrayOutputStream.toByteArray())+"'",new String(byteArrayOutputStream.toByteArray()).startsWith(getExpectedResourceContentPrefix()));
         }
         else
         {
         	//do nothing
         }
         
     }
 
     protected abstract String getExpectedResourceContentPrefix();
 
 	@Test
     public void testGetOutputStream() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         if (resourceDescriptor.isSupportedStreamFormat(StreamType.OUTPUT, StreamFormat.STREAM))
         {
         	OutputStream outputStream = resourceDescriptor.getOutputStream(null);
         	outputStream.write("this is a test".getBytes());
         	outputStream.close();
         	InputStream inputStream = resourceDescriptor.getInputStream(null);
         	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         	Assert.assertTrue(StreamUtil.readInputStreamIntoOutputStream(inputStream, byteArrayOutputStream) > 10);
         	Assert.assertTrue(new String(byteArrayOutputStream.toByteArray()).equals("this is a test"));
         }
         else
         {
         	//do nothing
         }
         
     }
 
     @Test
     public void testClose() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
         resourceDescriptor.close(null);
         Assert.assertSame(State.CLOSED,resourceDescriptor.getResourceState());        
     }
 
     @Test
     public void testRelease() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         Assert.assertSame(State.OPEN,resourceDescriptor.getResourceState());
         resourceDescriptor.close(null);
         Assert.assertSame(State.CLOSED,resourceDescriptor.getResourceState());
         resourceDescriptor.release(null);
         Assert.assertSame(State.RELEASED,resourceDescriptor.getResourceState());
         
     }
 
     @Test
     public void testGetResourceMetaData() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         resourceDescriptor.next(null);
         Assert.assertSame(State.STEPPING,resourceDescriptor.getResourceState());
         ContentMetaData contentMetaData = resourceDescriptor.getResourceMetaData(null);
         List<String> attributeList = contentMetaData.getSupportedAttributes();
         for (String attribute : attributeList)
 		{
         	System.out.println(attribute+" = "+contentMetaData.getValue(attribute));
 			Assert.assertNotNull("didn't expect "+attribute+" = "+contentMetaData.getValue(attribute)+" to be null",contentMetaData.getValue(attribute));
 		}
         
         
     }
 
     @Test
     public void testGetContentMetaData() throws Exception
     {
         resourceDescriptor.reset(State.NONE);
         resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
         resourceDescriptor.open(null);
         resourceDescriptor.next(null);
         resourceDescriptor.readXML(null);
         ContentMetaData contentMetaData = resourceDescriptor.getContentMetaData(null);
         List<String> attributeList = contentMetaData.getSupportedAttributes();
         for (String attribute : attributeList)
         {
             System.out.println(attribute+" = "+contentMetaData.getValue(attribute));
             Assert.assertNotNull(contentMetaData.getValue(attribute));
         }
 
     }
 
     @Test
     public void testGetLifeCycle() throws Exception
     {
     	Assert.assertNotNull(resourceDescriptor.getLifeCycle());
         
     }
 
     @Test
     public void testGetResourceURI() throws Exception
     {
     	Assert.assertNotNull(resourceDescriptor.getResourceURI());
         
     }
 
     @Test
     public void testGetLocalName() throws Exception
     {
     	Assert.assertNotNull(resourceDescriptor.getLocalName());
         
     }
 
     @Test
     public void testGetResourceType() throws Exception
     {
     	Assert.assertNotNull(resourceDescriptor.getResourceType());
     }
 
     @Test
     public void testAddResourceParameters() throws Exception
     {
         resourceDescriptor.addResourceParameters(null, new ResourceParameter("test","test"));        
     }
 
     @Test
     public void testGetChildResourceDescriptor() throws Exception
     {
     	resourceDescriptor.reset(State.NONE);
     	resourceDescriptor.init(null, null, LifeCycle.EXPLICIT, false);
     	resourceDescriptor.open(null);
     	if (resourceDescriptor.getResourceMetaData(null).isContainer())
     	{
     		List<ContentMetaData> childContentMetaDataList = resourceDescriptor.getResourceMetaData(null).getContainedResources();
     		for (ContentMetaData contentMetaData : childContentMetaDataList)
 			{
 				Assert.assertNotNull(resourceDescriptor.getChildResourceDescriptor(null, contentMetaData.getResourceURI().getBaseURI()));
 			}
     	}
         
     }
 
 }
