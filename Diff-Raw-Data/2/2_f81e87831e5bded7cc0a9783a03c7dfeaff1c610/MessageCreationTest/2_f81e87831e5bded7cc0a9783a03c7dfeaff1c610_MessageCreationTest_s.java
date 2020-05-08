 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol;
 
 import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.annotations.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Iterator;
 
 /**
  * Test whether we are able to create message objects from xml. The input XML is the example code defined in the
  * message-xml, thereby also testing whether this is valid. *
  */
 public class MessageCreationTest extends ExtendedTestCase {
 
     private static final String XML_MESSAGE_DIR = "target/message-xml/";
 
     @Test(groups = {"regressiontest"})
     public void messageCreationTest() throws Exception {
     	addDescription("Tests if we are able to create message objects from xml. The input XML is the example code " +
     			"defined in the message-xml, thereby also testing whether this is valid.");
         String[] messageNames = getMessageNames();
         for (String messageName : messageNames) {
         	addStep("Creating " + messageName + " message" , 
         			"The test is able to instantiate message based on the example in the message-xml modules");
             String xmlMessage = loadXMLExample(messageName);
 			MessageFactory.createMessage(GetChecksumsComplete.class, xmlMessage);
         }
     }
 
     /**
      * Generates the list of messages to test by parsing the message xsd file.
      *
      * @return List of messages to test
      */
     private String[] getMessageNames() throws Exception {
         File file = new File(XML_MESSAGE_DIR + "xsd/BitRepositoryMessages.xsd");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         Document doc = factory.newDocumentBuilder().parse(file);
 
         XPathFactory xPathFactory = XPathFactory.newInstance();
         XPath xpath = xPathFactory.newXPath();
         xpath.setNamespaceContext(getNamespaceContext());
         XPathExpression expr = xpath.compile("/xs:schema/xs:element");
         Object result = expr.evaluate(doc, XPathConstants.NODESET);
         NodeList nodes = (NodeList) result;
 
         String[] messageNames = new String[nodes.getLength()];
         for (int i = 0; i < nodes.getLength(); i++) {
             messageNames[i] = nodes.item(i).getAttributes().getNamedItem("name")
                     .getNodeValue();
         }
         return messageNames;
     }
 
     /** Needed by XPath to handle the namespaces. */
     private NamespaceContext getNamespaceContext() {
         NamespaceContext ctx = new NamespaceContext() {
             public String getNamespaceURI(String prefix) {
                 String uri;
                 if (prefix.equals("xs")) {
                     uri = "http://www.w3.org/2001/XMLSchema";
                 } else if (prefix.equals("xsi")) {
                     uri = "http://www.w3.org/2001/XMLSchema-instance";
                 } else if (prefix.equals("bre")) {
                     uri = "http://bitrepository.org/BitRepositoryElements.xsd";
                 } else {
                     uri = null;
                 }
                 return uri;
             }
 
             // Dummy implementation - not used!
             @SuppressWarnings("rawtypes")
             public Iterator getPrefixes(String val) {
                 return null;
             }
 
             // Dummy implemenation - not used!
             public String getPrefix(String uri) {
                 return null;
             }
         };
         return ctx;
     }
 
     /**
      * Loads the example XML for the indicated message. Assumes the XML examples are found under the
      * XML_MESSAGE_DIR/examples directory, and the naming convention for the example files are '${messagename}.xml'
      *
      * @param messageName
      * @return
      */
     private String loadXMLExample(String messageName) throws Exception {
        String filePath = XML_MESSAGE_DIR + "examples/" + messageName + ".xml";
         byte[] buffer = new byte[(int) new File(filePath).length()];
         FileInputStream f = new FileInputStream(filePath);
         f.read(buffer);
         return new String(buffer);
     }
 }
