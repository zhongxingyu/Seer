 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.soap.marshalers;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.net.URI;
 import java.util.Iterator;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.xml.namespace.QName;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.jaxp.BytesSource;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.apache.servicemix.soap.marshalers.SoapMarshaler;
 import org.apache.servicemix.soap.marshalers.SoapMessage;
 import org.apache.xpath.CachedXPathAPI;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.traversal.NodeIterator;
 
 /**
  * 
  * @author Guillaume Nodet
  * @version $Revision: 1.5 $
  */
 public class SoapMessageMarshalerTest extends TestCase {
 
 	private static final Log log = LogFactory.getLog(SoapMessageMarshalerTest.class);
 	
 	private SourceTransformer sourceTransformer = new SourceTransformer();
 	
 	public void testReadNonSoapMessage() throws Exception {
 		SoapMarshaler marshaler = new SoapMarshaler(false);
 		SoapMessage msg = marshaler.createReader().read(getClass().getResourceAsStream("soap.xml"));
 		assertNotNull(msg);
 		assertFalse(msg.hasAttachments());
 		assertFalse(msg.hasHeaders());
 		assertNotNull(msg.getSource());
 		Node node = sourceTransformer.toDOMNode(msg.getSource());
 		checkServiceNameNamespace(node);
 		checkUserIdNamespace(node);
 	}
 	
 	public void testWriteAndReadNonSoapMessage() throws Exception {
 		SoapMarshaler marshaler = new SoapMarshaler(false);
 		SoapMessage msg = new SoapMessage();
 		msg.setSource(new StreamSource(getClass().getResourceAsStream("soap.xml")));
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		marshaler.createWriter(msg).write(baos);
 		log.info(baos.toString());
 		
 		SoapMessage msg2 = marshaler.createReader().read(new ByteArrayInputStream(baos.toByteArray()));
 		assertNotNull(msg2);
 		assertFalse(msg2.hasAttachments());
 		assertFalse(msg2.hasHeaders());
 		assertNotNull(msg2.getSource());
 		Node node = sourceTransformer.toDOMNode(msg2.getSource());
 		checkServiceNameNamespace(node);
 		checkUserIdNamespace(node);
 	}
 	
 	public void testWriteAndReadNonSoapMessageWithAttachments() throws Exception {
 		SoapMarshaler marshaler = new SoapMarshaler(false);
 		SoapMessage msg = new SoapMessage();
 		msg.setSource(new StreamSource(getClass().getResourceAsStream("soap.xml")));
 		msg.addAttachment("servicemix.jpg", new DataHandler(new FileDataSource(new File(new URI(getClass().getResource("servicemix.jpg").toString())))));
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		SoapWriter writer = marshaler.createWriter(msg);
 		writer.write(baos);
 		log.info(baos.toString());
 		
 		SoapMessage msg2 = marshaler.createReader().read(new ByteArrayInputStream(baos.toByteArray()), writer.getContentType());
 		assertNotNull(msg2);
 		assertTrue(msg2.hasAttachments());
 		assertEquals(1, msg2.getAttachments().size());
 		DataHandler handler = (DataHandler) msg2.getAttachments().get(msg2.getAttachments().keySet().iterator().next());
 		assertNotNull(handler);
 		assertFalse(msg2.hasHeaders());
 		assertNotNull(msg2.getSource());
 		Node node = sourceTransformer.toDOMNode(msg2.getSource());
 		checkServiceNameNamespace(node);
 		checkUserIdNamespace(node);
 	}
 	
 	public void testWriteNonSoapMessageWithHeaders() throws Exception {
 		SoapMarshaler marshaler = new SoapMarshaler(false);
 		SoapMessage msg = new SoapMessage();
 		msg.addHeader(new QName("test"), null);
 		msg.setSource(null);
 		try {
 			marshaler.createWriter(msg).write(new ByteArrayOutputStream());
 			fail("Exception should have been thrown");
 		} catch (Exception e) {
 			// ok
 		}
 	}
 	
 	public void testSoapMessage() throws Exception {
 		SoapMarshaler marshaler = new SoapMarshaler(true);
 		
 		SoapMessage msg = marshaler.createReader().read(getClass().getResourceAsStream("soap.xml"));
 		assertNotNull(msg);
 		assertFalse(msg.hasAttachments());
 		assertTrue(msg.hasHeaders());
 		assertNotNull(msg.getSource());
 		
 		Iterator headers = msg.getHeaders().values().iterator();
 		assertTrue(headers.hasNext());
 		assertNotNull(headers.next());
 		assertTrue(headers.hasNext());
         checkServiceNameNamespace((DocumentFragment) headers.next());
 		assertFalse(headers.hasNext());
 
         Node node2 = sourceTransformer.toDOMNode(msg.getSource()); 
         checkUserIdNamespace(node2);
         
         msg.setSource(new DOMSource(node2));
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         marshaler.createWriter(msg).write(baos);
         Node node = sourceTransformer.toDOMNode(new BytesSource(baos.toByteArray()));
         checkUserIdNamespace(node);
 	}
 	
 	public void testWriteAndReadSoapMessageWithAttachments() throws Exception {
 		SoapMarshaler marshaler = new SoapMarshaler(true);
 		SoapMessage msg = marshaler.createReader().read(getClass().getResourceAsStream("soap.xml"));
 		msg.addAttachment("servicemix.jpg", new DataHandler(new FileDataSource(new File(new URI(getClass().getResource("servicemix.jpg").toString())))));
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		SoapWriter writer = marshaler.createWriter(msg);
 		writer.write(baos);
 		log.info(baos.toString());
 		
 		SoapMessage msg2 = marshaler.createReader().read(new ByteArrayInputStream(baos.toByteArray()), writer.getContentType());
 		assertNotNull(msg2);
 		assertTrue(msg2.hasAttachments());
 		assertEquals(1, msg2.getAttachments().size());
 		DataHandler handler = (DataHandler) msg2.getAttachments().get(msg2.getAttachments().keySet().iterator().next());
 		assertNotNull(handler);
 		assertNotNull(msg2.getSource());
         checkUserIdNamespace(sourceTransformer.toDOMNode(msg2.getSource()));
 		assertTrue(msg2.hasHeaders());
 		Iterator headers = msg2.getHeaders().values().iterator();
 		assertTrue(headers.hasNext());
 		assertNotNull(headers.next());
 		assertTrue(headers.hasNext());
         checkServiceNameNamespace((DocumentFragment) headers.next());
 		assertFalse(headers.hasNext());
 	}
 	
 	protected void checkUserIdNamespace(Node node) throws Exception {
         CachedXPathAPI cachedXPathAPI = new CachedXPathAPI(); 
         NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, "//*[local-name() = 'userId']"); 
         Element root = (Element) iterator.nextNode(); 
         QName qname = DOMUtil.createQName(root, root.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type")); 
         assertEquals("http://www.w3.org/2001/XMLSchema", qname.getNamespaceURI()); 
         assertEquals("string", qname.getLocalPart()); 
 	}
 	
 	protected void checkServiceNameNamespace(Node node) throws Exception {
         CachedXPathAPI cachedXPathAPI = new CachedXPathAPI(); 
         NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, "//*[local-name() = 'ServiceName']"); 
         Element root = (Element) iterator.nextNode(); 
         assertEquals(new QName("http://schemas.xmlsoap.org/ws/2003/03/addressing", "ServiceName"), 
         		     new QName(root.getNamespaceURI(), root.getLocalName()));
         QName qname = DOMUtil.createQName(root, DOMUtil.getElementText(root)); 
         assertEquals(new QName("uri:test", "MyConsumerService"), qname); 
 	}
 	
 	
 }
