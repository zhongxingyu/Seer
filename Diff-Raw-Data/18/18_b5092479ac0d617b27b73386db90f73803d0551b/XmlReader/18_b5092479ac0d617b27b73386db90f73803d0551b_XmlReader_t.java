 package Core;
 
 import Core.ResponseHandler.ResponseType;
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
 
 public class XmlReader {
 
 	private final MessageHandler msgHandler;
 
 	public XmlReader (MessageHandler msgHandler) {
 		this.msgHandler = msgHandler;
 	}
 
 	public boolean isDialogueNode (Node node) {
 		return node.getNodeType () == Node.ELEMENT_NODE && node.getNodeName ().equals ("node");
 	}
 
 	public DialogueNode ReadTreeRoot (String fileName) {
 		return ReadTreeRoot (new File (fileName));
 	}
 
 	public DialogueNode ReadTreeRoot (File file) {
 		try {
 			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
 			Document doc = docBuilder.parse (file);
 			doc.getDocumentElement ().normalize ();
 
 			NodeList nList = doc.getChildNodes ().item (0).getChildNodes ();
 			Node nNode = nList.item (0);
 			if (isDialogueNode (nNode)) {
 				Element e = (Element) nNode;
 				DialogueNode rootNode = parseDialogueNode (e, 0);
 				RecurseReadChildren (e, rootNode);
 
 				return rootNode;
 			}
 			else {
 				return null;
 			}
 		}
 		catch (ParserConfigurationException | SAXException | IOException ex) {
 			Logger.logException ("XmlReader::ReadTreeNode", ex);
 		}
 
 		return null;
 	}
 
 	private void RecurseReadChildren (Element e, DialogueNode parentNode) {
 		NodeList nList = e.getChildNodes ();
 
 		for (int i = 0; i < nList.getLength (); i++) {
 			Node nNode = nList.item (i);
 			if (isDialogueNode (nNode)) {
 				Element eTmp = (Element) nNode;
 				DialogueNode dNode = parseDialogueNode (eTmp, parentNode.id);
 				parentNode.childrenNodes.add (dNode);
 				RecurseReadChildren (eTmp, dNode);
 			}
 		}
 	}
 
 	private DialogueNode parseDialogueNode (Element e, int parentId) {
 		int id = Integer.parseInt (getTagValue ("id", e));
 		String name = getTagValue ("name", e);
 		String text = getTagValue ("text", e);
 		ResponseType type = ResponseType.valueOf (getTagValue ("type", e));
 
 		DialogueNode node = new DialogueNode (id, parentId, name, text, type, this.msgHandler);
 		node.setAvatar(getTagValue ("avatar", e));
 
 		return node;
 	}
 
 	private static String getTagValue (String tagName, Element e) {
 		NodeList eList = e.getElementsByTagName (tagName);
 
 		try {
 			if (eList.item (0) != null) {
 				NodeList nList = eList.item (0).getChildNodes ();
 				Node nValue = (Node) nList.item (0);
 				String nodeValue = nValue.getNodeValue ();
 				return nodeValue;
 			}
 		}
 		catch (Exception ex) {
 			Logger.logException ("XmlReader::getTagValue", ex);
 		}
 
 		return null;
 	}
 }
