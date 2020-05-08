 /**
  * 
  */
 package podsalinan;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @author bugman
  *
  */
 public class XmlReader {
 	private Document doc;
 
 	public XmlReader(String filename){
 		File file = new File(filename);
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			doc = db.parse(file);
 			doc.getDocumentElement().normalize();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getMenuCount(){
 		NodeList nodeList = doc.getElementsByTagName("menu");
 		return nodeList.getLength();
 	}
 	
 	public String getMenuName(int menuCount){
 		NodeList nodeList = doc.getElementsByTagName("menu");
 		Node currentNode = nodeList.item(menuCount);
 		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 			return getNodeData(currentNode,"title");
 		}
 		return null;
 	}
 
 	public int getMenuItemCount(int menuCount){
 		NodeList nodeList = doc.getElementsByTagName("menu");
 		Node currentNode = nodeList.item(menuCount);
 		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 			Element fstElmnt = (Element) currentNode;
 			NodeList menuItems = fstElmnt.getElementsByTagName("menuitem");
 			return menuItems.getLength();
 		}
 		return 0;
 	}
 	
 	public String getMenuItemValue(int menuCount, int menuItemCount, String tagName){
 		NodeList nodeList = doc.getElementsByTagName("menu");
 		Node currentNode = nodeList.item(menuCount);
 		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 			Element fstElmnt = (Element) currentNode;
 			NodeList menuItems = fstElmnt.getElementsByTagName("menuitem");
 			Node currentMenuItemNode = menuItems.item(menuItemCount);
 			if (currentMenuItemNode.getNodeType() == Node.ELEMENT_NODE){
 				return getNodeData(currentMenuItemNode, tagName);
 			}
 		}
 		return null;
 	}
 	
 	public String getFeedTitle(){
 		NodeList nodeList = doc.getElementsByTagName("channel");
 		for (int s = 0; s < nodeList.getLength(); s++) {
 			Node currentNode = nodeList.item(s);
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				return getNodeData(currentNode,"title");
 			}
 		}		
 		return null;				
 	}
 	
 	public int getDownloadCount(){
 		NodeList nodeList = doc.getElementsByTagName("channel");
 		for (int s = 0; s < nodeList.getLength(); s++) {
 			Node currentNode = nodeList.item(s);
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element fstElmnt = (Element) currentNode;
 				NodeList items = fstElmnt.getElementsByTagName("item");
 				return items.getLength();
 			}
 		}		
 		return 0;		
 	}
 	
 	/** Consolidating the same code used for 5 different functions into the one. This will be
 	 * used to extract requested data from the xml file.
 	 * 
 	 * @param itemNumber
 	 * @param tagName
 	 * @param attribute
 	 * @return The requested value, be it an attribute or tag
 	 */
 	public String getDownloadValue(int itemNumber, String tagName, String attribute){
 		NodeList nodeList = doc.getElementsByTagName("channel");
 		for (int s = 0; s < nodeList.getLength(); s++) {
 			Node currentNode = nodeList.item(s);
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element fstElmnt = (Element) currentNode;
 				NodeList items = fstElmnt.getElementsByTagName("item");
 				Node itemNode = items.item(itemNumber);
 				if (attribute==null){
 					return getNodeData(itemNode,tagName);
 				} else {
 					return getNodeAttribute(itemNode, tagName, attribute);
 				}
 			}
 		}		
 		return null;		
 	}
 
 	/**
 	 * This is used to pull the string data out of the xml file, if the tag does not exist
 	 * null value is returned for the string.  
 	 * @param currentNode passes in the current Node in the XML document.
 	 * @param tagName passes in the tag we are looking for in the current Node.
 	 * @return A string value. If the tag does not exist in the current Node, null will be
 	 *   returned.
 	 */
 	private String getNodeData (Node currentNode, String tagName){
 		String nodeValue;
 		Element fstElmnt = (Element) currentNode;
 		NodeList titleElmntLst = fstElmnt.getElementsByTagName(tagName);
 		Element titleElmnt = (Element) titleElmntLst.item(0);
 		if (titleElmnt != null) {
 			NodeList menuName = titleElmnt.getChildNodes();
			nodeValue = ((Node) menuName.item(0)).getNodeValue();
 		} else
 			nodeValue = null;
 		return nodeValue;
 	}
 	
 	/** getNodeAttributes is used to grab the data from the xml file. While supplying the
 	 * tag we want information from, and which attribute, it will then return a string
 	 * containing the value of the attribute.
 	 * @param currentNode 
 	 * @param tagName
 	 * @param attribute
 	 * @return The value of the attribute
 	 */
 	private String getNodeAttribute(Node currentNode, String tagName, String attribute) {
 		Element fstElement = (Element) currentNode;
 		NodeList elementList = fstElement.getElementsByTagName(tagName);
 		Element elementItem = (Element) elementList.item(0);
 		if (elementItem != null){
 			return elementItem.getAttribute(attribute);
 		} else {
 			return null;
 		}
 	}
 }
