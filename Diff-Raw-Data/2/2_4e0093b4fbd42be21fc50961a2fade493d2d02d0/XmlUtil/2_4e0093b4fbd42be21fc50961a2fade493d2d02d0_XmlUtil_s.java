 /**
  * Copyright 2011 Glenn Maynard
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack.util;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 public class XmlUtil {
     /**
      * Parse an XML document, and return the resulting {@link Document}.
      *
      * @throws SAXException if XML parsing fails
      * @throws IOException if reading from stream fails
      */
     public static Document parseXML(InputSource stream) throws SAXException, IOException {
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         dbfac.setNamespaceAware(true);
         DocumentBuilder docBuilder;
         try {
             docBuilder = dbfac.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             throw new RuntimeException(e);
         }
 
         return docBuilder.parse(stream);
     }
 
     /**
      * Parse an XML document, and return the resulting root {@link Node}.
      *
      * @throws SAXException if XML parsing fails
      * @throws IOException if reading from stream fails
      */
     public static Element getXMLRootNode(InputSource stream) throws SAXException, IOException {
         Document doc = parseXML(stream);
         for(Element data: XmlUtil.getChildElements(doc))
             return data;
 
         throw new RuntimeException("Document had no root node");
     }
 
     /**
      * Return an iterable Collection<Node> of the child Elements of the specified
      * node.
      *
      * @param parent The parent node.
      * @return A collection of child nodes.
      */
     public static Collection<Element> getChildElements(Node node) {
         NodeList children = node.getChildNodes();
         ArrayList<Element> result = new ArrayList<Element>(children.getLength());
         for(int i = 0; i < children.getLength(); ++i) {
             Node child = children.item(i);
             if(!(child instanceof Element))
                 continue;
             result.add((Element) child);
         }
         return result;
     }
 
     /**
      * Implement Node.getTextContent(), which isn't available in DOM Level 2.
      */
     public static String getTextContent(Node node) {
         String result = "";
         if(node instanceof Text)
             result = ((Text) node).getData();
 
         for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
             result += getTextContent(child);
 
         return result;
     }
 
     /**
      * Return the current XML element as a string.
      * @param an {@link XmlPullParser} with a current event type of START_TAG
      * @return an XML string
      */
     public static String parserNodeToString(XmlPullParser parser) throws XmlPullParserException, IOException {
         StringBuilder content = new StringBuilder();
         parserNodeToString(content, parser, new HashMap<String, String>());
         return content.toString();
     }
 
     private static void parserNodeToString(StringBuilder content, XmlPullParser parser,
             Map<String, String> namespaceNodes)
                     throws XmlPullParserException, IOException {
         // This is made more complicated due to many XmlPullParser implementations
         // support neither FEATURE_XML_ROUNDTRIP nor FEATURE_REPORT_NAMESPACE_ATTRIBUTES.
         if (parser.getEventType() != XmlPullParser.START_TAG)
             throw new IllegalArgumentException("parseContent must start at a start tag");
 
         {
             HashMap<String,String> neededNamespaces = new HashMap<String,String>();
             content.append("<");
 
             // Append the element prefix, if any.
             String elemPrefix = parser.getPrefix();
             if(elemPrefix != null)
                 content.append(elemPrefix + ":");
             neededNamespaces.put(elemPrefix == null? "":elemPrefix, parser.getNamespace());
 
             content.append(parser.getName());
             for(int i = 0; i < parser.getAttributeCount(); ++i) {
                 content.append(" ");
 
                 // Append the attribute prefix, if any.
                 String attrPrefix = parser.getAttributePrefix(i);
                 if(attrPrefix != null)
                     neededNamespaces.put(attrPrefix, parser.getAttributeNamespace(i));
                 if(attrPrefix != null)
                     content.append(attrPrefix + ":");
 
                 String attrName = parser.getAttributeName(i);
                 content.append(attrName + "='");
                 String attrValue = parser.getAttributeValue(i);
                 content.append(StringUtils.escapeForXML(attrValue));
                 content.append("'");
             }
 
             // Check the prefixes used by this element and its attributes for ones that
             // aren't already declared, and output new xmlns attributes as needed.
             boolean clonedNamespaceMap = false;
             for(Map.Entry<String,String> entry: neededNamespaces.entrySet()) {
                 String prefix = entry.getKey();
                 String namespace = entry.getValue();
                 String existingNamespace = namespaceNodes.get(prefix);
 
                 if(existingNamespace != null && existingNamespace.equals(namespace))
                     continue;
 
                 // This is a new or changed namespace prefix.  Make a copy of the namespace
                 // map, if we havn't already, and output a namespace declaration.
                 if(!clonedNamespaceMap) {
                     clonedNamespaceMap = true;
 
                     HashMap<String, String> clonedNodes = new HashMap<String, String>();
                     clonedNodes.putAll(namespaceNodes);
                     namespaceNodes = clonedNodes;
                 }
 
                 namespaceNodes.put(prefix, namespace);
 
                 content.append(" xmlns");
                if(!prefix.isEmpty())
                     content.append(":" + prefix);
 
                 content.append("='");
                 content.append(StringUtils.escapeForXML(namespace));
                 content.append("'");
             }
             content.append(">");
         }
 
         while (true) {
             parser.next();
             switch(parser.getEventType()) {
             case XmlPullParser.START_TAG:
                 parserNodeToString(content, parser, namespaceNodes);
                 break;
             case XmlPullParser.END_TAG:
                 content.append("</");
                 if(parser.getPrefix() != null)
                     content.append(parser.getPrefix() + ":");
                 content.append(parser.getName() + ">");
                 return;
             case XmlPullParser.END_DOCUMENT:
                 return;
             default:
                 String text = parser.getText();
                 if(text == null)
                     throw new RuntimeException("Unexpected null result from parser.getText");
                 content.append(StringUtils.escapeForXML(text));
                 break;
             }
         }
     }
 };
