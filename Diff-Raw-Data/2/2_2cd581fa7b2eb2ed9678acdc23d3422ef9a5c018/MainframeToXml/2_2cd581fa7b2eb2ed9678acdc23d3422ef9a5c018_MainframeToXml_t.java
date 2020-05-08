 /*************************************************************
  * This file is part of CB2XML.  
  * See the file "LICENSE" for copyright information and the
  * terms and conditions for copying, distribution and
  * modification of CB2XML.
  *************************************************************
  */
 
 package net.sf.cb2xml.convert;
 
 import net.sf.cb2xml.util.XmlUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jcopybook.Utils;
 import org.w3c.dom.*;
 
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.StringReader;
 
 /**
  * routines to convert a copybook equivalent mainframe buffer into its XML form
  * given the XML form of the copybook
  * to-do: all the stuff related to COMP fields, packed decimal amd all other non-straightforward field types
  * <p/>
  * * note that files within the "net.sf.cb2xml.convert" package are not stable
  *
  * @author Peter Thomas
  */
 
 public class MainframeToXml {
 
 	private String mainframeBuffer = null;
 	private Document resultDocument = null;
 	private int globalOffset;
 
 	private static String stripNullChars(String in) {
 		try {
 			Reader reader = new BufferedReader(new StringReader(in));
 			StringBuffer buffer = new StringBuffer();
 			int ch;
 			while ((ch = reader.read()) > -1) {
 				if (ch != 0) {
 					buffer.append((char) ch);
 				} else {
 					buffer.append(' ');
 				}
 			}
 			reader.close();
 			return buffer.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public Document convert(String mainframeBuffer, Document copyBookXml) {
 		this.mainframeBuffer = stripNullChars(mainframeBuffer);
 		this.resultDocument = XmlUtils.getNewXmlDocument();
 		int bufferLength = mainframeBuffer.length();
 		Element documentElement = copyBookXml.getDocumentElement();
 
 		Element recordNode = Utils.getFirstElement(documentElement);
 		Element resultRoot = resultDocument.createElement(documentElement.getTagName());
 		int recordLength = Integer.parseInt(recordNode.getAttribute("storage-length"));
 
 		for (int offset = 0; offset < bufferLength; offset += recordLength) {
 			if ("true".equals(recordNode.getAttribute("redefined"))) continue;
 			Element resultTree = convertNode(recordNode);
 			resultRoot.appendChild(resultTree);
 		}
 		resultDocument.appendChild(resultRoot);
 		return resultDocument;
 	}
 
 	private Element convertNode(Element element) {
 		String resultElementName = element.getAttribute("name");
 		Element resultElement = resultDocument.createElement(resultElementName);
 		int length = Integer.parseInt(element.getAttribute("storage-length"));
 		int childElementCount = 0;
 		NodeList nodeList = element.getChildNodes();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			org.w3c.dom.Node node = nodeList.item(i);
 			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
 				Element childElement = (Element) node;
				if (!childElement.getAttribute("level").equals("88") && "item".equals(childElement.getNodeName())) {
 					childElementCount++;
 					if (childElement.hasAttribute("occurs")) {
 						Node countNode = getElementByAttr(resultElement, "NAME", childElement.getAttribute("depending-on"));
 						int childOccurs = Integer.parseInt(childElement.getAttribute("occurs"));
 						boolean dependOn = StringUtils.isNotEmpty(childElement.getAttribute("depending-on"));
 						if (dependOn && countNode != null) {
 							childOccurs = Integer.valueOf(countNode.getFirstChild().getNodeValue());
 						}
 						int childPosition = Integer.parseInt(childElement.getAttribute("position"));
 						int childLength = Integer.parseInt(childElement.getAttribute("storage-length"));
 						for (int j = 0; j < childOccurs; j++) {
 							resultElement.appendChild(convertNode(childElement));
 						}
 					} else {
 						if (!"item".equals(childElement.getAttribute("name")))
 							resultElement.appendChild(convertNode(childElement));
 					}
 				}
 			}
 		}
 		if (childElementCount == 0 && !"true".equals(element.getAttribute("redefined"))) {
 			String text = null;
 			try {
 				text = mainframeBuffer.substring(globalOffset, globalOffset + length);
 				globalOffset += length;
 			} catch (Exception e) {
 				System.err.println(e);
 				System.err.println("element = " + element.getAttribute("name"));
 				System.err.println("globalOffset = " + globalOffset);
 				System.err.println("length = " + length);
 				System.err.println("Mainframe buffer length = " +
 						mainframeBuffer.length());
 			}
 			Text textNode = resultDocument.createTextNode(text);
 			resultElement.appendChild(textNode);
 		}
 
 		return resultElement;
 	}
 
 	private int getChildsLength(NodeList childNodes) {
 		int len = 0;
 		for (int i = 0; i < childNodes.getLength(); i++) {
 			len += getElementLength(childNodes.item(i));
 		}
 		return len;
 	}
 
 	private int getElementLength(Node node) {
 		if (!"item".equals(node.getNodeName())) return 0;
 		String attribute = getAttribute(node, "display-length");
 		int len = 0;
 		if (StringUtils.isNotEmpty(attribute))
 			len = Integer.parseInt(attribute);
 		String occurs = getAttribute(node, "occurs");
 
 		//for loop we check the childs
 		if (StringUtils.isNotEmpty(occurs)) {
 			NodeList childs = node.getChildNodes();
 			for (int i = 0; i < childs.getLength(); i++) {
 				len += getElementLength(childs.item(i));
 			}
 		}
 
 		return len;
 	}
 
 	private String getAttribute(Node node, String attrName) {
 		if (node.getAttributes() == null) return null;
 		Node attr = node.getAttributes().getNamedItem(attrName);
 		return attr != null ? attr.getNodeValue() : null;
 	}
 
 	private Node getElementByAttr(Node parent, String attrName, String attrValue) {
 		NodeList childs = parent.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node node = childs.item(i);
 			if (attrValue.equals(node.getNodeName())) return node;
 		}
 		return null;
 	}
 
 }
