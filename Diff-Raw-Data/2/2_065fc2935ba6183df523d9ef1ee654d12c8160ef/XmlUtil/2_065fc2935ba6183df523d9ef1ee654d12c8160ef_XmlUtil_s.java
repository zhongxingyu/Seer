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
 import java.util.LinkedList;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jivesoftware.smack.XMPPException;
 import org.w3c.dom.Attr;
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
      * Retrieve a namespace-aware DocumentBuilder.
      * @return {@link DocumentBuilder}.
      */
     public static DocumentBuilder getDocumentBuilder() {
         // XXX: stash the DocumentBuilder; don't make a new one on every stanza
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         dbfac.setNamespaceAware(true);
         DocumentBuilder docBuilder;
         try {
             docBuilder = dbfac.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             throw new RuntimeException("Unexpected parser error", e);
         }
         return docBuilder;
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
                 // Some XML parsers include xmlns attributes and some don't.  We handle them below
                 // in case they don't, so exclude any provided xmlns attributes.
                 String attrName = parser.getAttributeName(i);
                 if(attrName.equals("xmlns"))
                     continue;
 
                 content.append(" ");
 
                 // Append the attribute prefix, if any.
                 String attrPrefix = parser.getAttributePrefix(i);
                 if(attrPrefix != null)
                     neededNamespaces.put(attrPrefix, parser.getAttributeNamespace(i));
                 if(attrPrefix != null)
                     content.append(attrPrefix + ":");
 
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
                if(prefix.length() == 0)
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
 
 
     private static void ReadDomAttributesFromXmlPull(Document doc, Element tag, XmlPullParser parser)
     {
         for(int i = 0; i < parser.getAttributeCount(); ++i)
         {
             String name = parser.getAttributeName(i);
             String namespace = parser.getAttributeNamespace(i);
 
             /* Converting namespace declarations back and forth between DOM and XmlPullParser
              * is annoying, because XmlPullParser only tells us the current list, not what's
              * actually changed; we'd need to compare the current list against the parent's.
              * This information is only needed to explicitly detecting namespace declarations,
              * not to retain node namespaces, so for simplicity and efficiency, this isn't done. */
 /*
                 for(int i = 0; i < parser.getNamespaceCount(parser.getDepth()); ++i)
                 {
                     String prefix = parser.getNamespacePrefix(i);
                     String uri = parser.getNamespaceUri(i);
                     tag.setAttribute("xmlns:" + prefix, uri);
                 }
 */
 
             /* For XmlPullParser, no namespace is "".  For DOM APIs, no namespace is null. */
             if(namespace == "")
                 namespace = null;
 
             String value = parser.getAttributeValue(i);
 
             Attr attr = doc.createAttributeNS(namespace, name);
             attr.setValue(value);
             tag.setAttributeNode(attr);
         }
     }
 
     /**
      * Read a single START_TAG node from an {@link XmlPullParser}, returning an Element with attributes set.
      * Does not advance the XmlPullParser.
      */
     public static Element ReadElementFromXmlPullNonRecursive(XmlPullParser parser) throws XMPPException, IOException
     {
         DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
         Document doc = docBuilder.newDocument();
         Element tag = doc.createElementNS(parser.getNamespace(), parser.getName());
         ReadDomAttributesFromXmlPull(doc, tag, parser);
         return tag;
     }
 
     /**
      * Read a single complete XMPP stanza from parser, returning it as a DOM Element.
      */
     public static Element ReadNodeFromXmlPull(XmlPullParser parser) throws XMPPException, IOException
     {
         try {
             DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
             Document doc = docBuilder.newDocument();
 
             LinkedList<Node> documentTree = new LinkedList<Node>();
             while(true)
             {
                 switch(parser.getEventType())
                 {
                 case XmlPullParser.START_TAG:
                 {
                     Element tag = doc.createElementNS(parser.getNamespace(), parser.getName());
                     if(!documentTree.isEmpty())
                     {
                         Node parent = documentTree.getLast();
                         parent.appendChild(tag);
                     }
 
                     ReadDomAttributesFromXmlPull(doc, tag, parser);
 
                     documentTree.add(tag);
                     break;
                 }
                 case XmlPullParser.END_TAG:
                 {
                     Node removed = documentTree.removeLast();
 
                     /* If we popped the top-level node, then it's the final result. */
                     if(documentTree.isEmpty())
                         return (Element) removed;
                     break;
                 }
                 case XmlPullParser.TEXT:
                 {
                     Node tag = doc.createTextNode(parser.getText());
                     Node parent = documentTree.getLast();
                     parent.appendChild(tag);
                     break;
                 }
                 case XmlPullParser.END_DOCUMENT:
                     // Normally, we'll never receive END_DOCUMENT, because we're parsing a
                     // single sub-tree; we stop when we reach the end tag matching the open
                     // tag we started on, and never continue to receive END_DOCUMENT. If we
                     // receive END_DOCUMENT, that means the document ended mid-stanza.
                     throw new XMPPException("Stream closed unexpectedly");
                 }
 
                 parser.next();
             }
         }
         catch (XmlPullParserException e) {
             throw new XMPPException("XML error", e);
         }
     }
 };
