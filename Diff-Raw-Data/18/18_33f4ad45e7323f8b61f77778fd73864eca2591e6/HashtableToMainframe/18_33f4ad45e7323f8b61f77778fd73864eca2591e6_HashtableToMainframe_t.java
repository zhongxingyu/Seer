 /*************************************************************
  * This file is part of CB2XML.  
  * See the file "LICENSE" for copyright information and the
  * terms and conditions for copying, distribution and
  * modification of CB2XML.
  *************************************************************
  */
 
 package net.sf.cb2xml.convert;
 
 import org.jcopybook.Utils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import java.util.Hashtable;
 
 /**
  * this class handles converting a Hashtable representation of data into
  * its copybook equivalent StringBuffer (mainframe equivalent)
  * it recurses the DOM of the copybook definition (as XML) and checks if a
  * hashtable key exists for each data node.  if found, it packs the hashtable
  * value into the output string buffer and continues
  * the weird logic you may see would be to
  * <p/>
  * 1) checking for a leading 1 or 0 in the StringBuffer to identify if there was a child
  * element value in the hashtable corresponding to the branch (or child branches) being traversed
  * resorted to this 'hack' to simplify the signature of the method being recursed
  * <p/>
  * 2) due to 'REDEFINES' complications, a decision has to be made if the hashtable
  * has data for more than one of the redefined instances (should be XOR)
  * below, the code selects the first XOR instance, so attempts to ignore inconsistent inputs
  * <p/>
  * TODO : the special mainframe formats of COMP, packed-decimal etc are not supported yet
  * <p/>
  * * note that files within the "net.sf.cb2xml.convert" package are not stable
  *
  * @author Peter Thomas
  */
 
 public class HashtableToMainframe {
 
 	private Hashtable keyValuePairs = null;
 
 	private StringBuffer getRepeatedChars(char charToRepeat, int count) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < count; i++) {
 			sb.append(charToRepeat);
 		}
 		return sb;
 	}
 
 	public String convert(Hashtable keyValuePairs, Document copyBookXml) {
 		this.keyValuePairs = keyValuePairs;
 		Element documentElement = copyBookXml.getDocumentElement();
 		Element element = Utils.getFirstElement(documentElement);
 		String documentTagName = documentElement.getTagName();
 		String tagName = element.getAttribute("name");
 		String xpath = "/" + documentTagName + "/" + tagName;
 		return convertNode(element, xpath).deleteCharAt(0).toString();
 	}
 
 
 	private StringBuffer convertNode(Element element, String xpath) {
		boolean isCondition = "condition".equals(element.getNodeName());
 		StringBuffer segment = new StringBuffer();
		if (isCondition) return segment;
 		segment.append('0');
 		int position = Integer.parseInt(element.getAttribute("position"));
 		int length = Integer.parseInt(element.getAttribute("storage-length"));
 		int childElementCount = 0;
 		NodeList nodeList = element.getChildNodes();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			org.w3c.dom.Node node = nodeList.item(i);
 			if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
 				Element childElement = (Element) node;
				if ("condition".equals(childElement.getNodeName())) continue;
 				if (!childElement.getAttribute("level").equals("88")) {
 					String childElementName = childElement.getAttribute("name");
 					childElementCount++;
					int childPosition = -1;
					if (!"condition".equals(childElement.getNodeName()))
						childElementCount = Integer.parseInt(childElement.getAttribute(
								"position"));
 					StringBuffer tempBuffer = null;
 					if (childElement.hasAttribute("occurs")) {
 						tempBuffer = new StringBuffer();
 						tempBuffer.append('0');
 						int childOccurs = Integer.parseInt(childElement.getAttribute(
 								"occurs"));
 						int childLength = Integer.parseInt(childElement.getAttribute(
 								"storage-length"));
 						int singleChildLength = childLength / childOccurs;
 						for (int j = 0; j < childOccurs; j++) {
 							StringBuffer occursBuffer = convertNode(childElement,
 									xpath + "/" + childElementName + "[" + j + "]");
 							if (occursBuffer.charAt(0) == '1') {
 								tempBuffer.setCharAt(0, '1');
 							}
 							occursBuffer.deleteCharAt(0);
 							tempBuffer.append(occursBuffer);
 						}
 					} else {
 						tempBuffer = convertNode(childElement,
 								xpath + "/" + childElementName);
 					}
 					if (childElement.hasAttribute("redefines") &&
 							tempBuffer.charAt(0) == '1') {
 						tempBuffer.deleteCharAt(0);
 						int replacePosition = childPosition - position;
 						segment.replace(replacePosition,
 								replacePosition + tempBuffer.length(),
 								tempBuffer.toString());
 					} else {
						if (tempBuffer.length() > 0 && tempBuffer.charAt(0) == '1') {
 							segment.setCharAt(0, '1');
 						}
						if (tempBuffer.length() > 0) tempBuffer.deleteCharAt(0);
 						segment.append(tempBuffer);
 					}
 				}
 			}
 		}
 		if (childElementCount == 0) {
 			if (keyValuePairs.containsKey(xpath)) {
 				segment.setCharAt(0, '1');
 				segment.append(keyValuePairs.get(xpath));
 			} else {
 				if (element.hasAttribute("value")) {
 					segment.append(element.getAttribute("value"));
 				} else if (element.hasAttribute("spaces")) {
 					segment.append(getRepeatedChars(' ', length));
 				} else if (element.hasAttribute("zeros")) {
 					segment.append(getRepeatedChars('0', length));
 				}
 			}
 		}
 		return segment;
 	}
 
 }
