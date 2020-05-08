 /*************************************************************
  * This file is part of CB2XML.  
  * See the file "LICENSE" for copyright information and the
  * terms and conditions for copying, distribution and
  * modification of CB2XML.
  *************************************************************
  */
 
 package net.sf.cb2xml.convert;
 
import org.jcopybook.Marshaller;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import java.util.Hashtable;
 
 /**
  * This routine takes the input XML, recurses the tree and produces a hashtable equivalent.
  * Then this uses the HashtableToMainframe utility for generatin a buffer in "mainframe" format.
  * This is a simpler approach and direct conversion of XML to the mainframe buffer was not attempted.
  * I prefer the hashtable approach, because it is much easier to create a Hashtable
  * than XML from a Java program and this approach is easier for passing data in either direction.
  * Thefore, I recommend using a hashtable to programmatically pass data or retrieve data
  * to (or from) a mainframe, (based on the copybook structure of course).
  * <p/>
  * * note that files within the "net.sf.cb2xml.convert" package are not stable
  *
  * @author Peter Thomas
  */
 
 public class XmlToMainframe {
 
 	private Hashtable keyValuePairs = new Hashtable();
 
 	public String convert(Document sourceDocument, Document copyBookXml) {
		return new Marshaller().process(sourceDocument, copyBookXml);
//		Element documentElement = sourceDocument.getDocumentElement();
//		Element element = Utils.getFirstElement(documentElement);
//		String xpath = "/" + documentElement.getTagName() +
//				"/" + element.getTagName();
//		convertNode(element, xpath);
//		FileUtils.writeFile(keyValuePairs.toString(), "hashtable.txt", false);
//		return new HashtableToMainframe().convert(keyValuePairs, copyBookXml);
 	}
 
 	private void convertNode(Element element, String xpath) {
 		NodeList nodeList = element.getChildNodes();
 		Hashtable childHash = new Hashtable(nodeList.getLength(), 1);
 		int index = 0;
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			org.w3c.dom.Node childNode = nodeList.item(i);
 			if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
 				Element childElement = (Element) childNode;
 				String childElementName = childElement.getTagName();
 				if (childHash.containsKey(childElementName)) {
 					index = Integer.parseInt(childHash.get(childElementName).toString()) +
 							1;
 					childHash.put(childElementName, index + "");
 				} else {
 					childHash.put(childElementName, "0");
 				}
 				childElement.setAttribute("index", index + "");
 			} else if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
 				keyValuePairs.put(xpath, childNode.getNodeValue());
 			}
 		}
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			org.w3c.dom.Node childNode = nodeList.item(i);
 
 			if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
 				Element childElement = (Element) childNode;
 				String childElementName = childElement.getTagName();
 				if (!childHash.get(childElementName).equals("0")) {
 					convertNode(childElement,
 							xpath + "/" + childElementName + "[" +
 									childElement.getAttribute("index") + "]");
 				} else {
 					convertNode(childElement, xpath + "/" + childElementName);
 				}
 			}
 		}
 	}
 
 }
