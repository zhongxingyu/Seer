 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package xquery4j;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class DOMUtils {
     
     public static QName uniqueQName(QName q) {
         String s = q.getNamespaceURI();
         while (s.endsWith("/")) { s = s.substring(0, s.length() - 1); }
         return new QName(s, q.getLocalPart());
     }
 
     public static Element getFirstElement(Node node) {
         NodeList l = node.getChildNodes();
         for (int i = 0; i < l.getLength(); i++) {
             if (l.item(i) instanceof Element) {
                 return (Element) l.item(i);
             }
         }
         return null;
     }
     
     public static Element findElement(QName elementName, List<Object> content) {
         for (Object o : content) {
             if (o instanceof Element) {
                 Element u = (Element) o;
                 QName n = new QName(u.getNamespaceURI(), u.getLocalName());
                 if (n.equals(elementName)) {
                     return u;
                 }
             }
         }
         return null;
     }
     
     /**
      * Convert a DOM node to a stringified XML representation.
      */
     static public String domToString(Node node) {
         if (node == null) {
             throw new IllegalArgumentException("Cannot stringify null Node!");
         }
 
         String value = null;
         short nodeType = node.getNodeType();
         if (nodeType == Node.ELEMENT_NODE || nodeType == Node.DOCUMENT_NODE) {
             // serializer doesn't handle Node type well, only Element
             OutputFormat f = new OutputFormat();
             f.setIndent(2);
             XMLSerializer ser = new XMLSerializer(f);
             ser.setNamespaces(true);
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             ser.setOutputByteStream(b);
 
             try {
                 if (node instanceof Document) {
                     ser.serialize((Document) node);
                 } else if (node instanceof Element) {
                     ser.serialize((Element) node);
                 } else {
                     throw new IllegalStateException();
                 }
             } catch (IOException e) {
                 throw new IllegalStateException("", e);
             }
 
             value = b.toString();
         } else {
             value = node.getNodeValue();
         }
         return value;
     }
 
     public static DocumentBuilderFactory getDocumentBuilderFactory() {
         return new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
 //        return new net.sf.saxon.dom.DocumentBuilderFactoryImpl();
     }
     
     public static XPathFactory getXPathFactory() {
         return new net.sf.saxon.xpath.XPathFactoryImpl();
     }
     
     public static Document parse(InputStream in) throws Exception {
         DocumentBuilderFactory f = DOMUtils.getDocumentBuilderFactory();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         f.setNamespaceAware(true);
         DocumentBuilder b = f.newDocumentBuilder();
         Document d = b.parse(in);
         return d;
     }
 
     public static Document parse(String in) throws Exception {
         return parse(new ByteArrayInputStream(in.getBytes()));
     }
 }
