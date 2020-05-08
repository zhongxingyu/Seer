 package Core;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class XmlWriter {
 
 	public static void WriteTree(File file, DialogueNode root) {
 		if (root == null) {
 			return;
 		}
 
 		try {
 			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("dialogue");
 			doc.appendChild(rootElement);
 
 			//Root node
 			RecurseWriteChild(root, rootElement, doc);
 
 			Transformer transformer = TransformerFactory.newInstance().newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result = new StreamResult(file);
 
 			transformer.transform(source, result);
 		} catch (ParserConfigurationException | DOMException | TransformerException ex) {
 			Logger.logException("XmlWriter::WriteTree", ex);
 		}
 	}
 
 	private static void appendInfo(Element eNode, Document doc, String eName, String data) {
 		Element e = doc.createElement(eName);
 		e.appendChild(doc.createTextNode(data));
 		eNode.appendChild(e);
 	}
 
 	private static String writeImage (ImageIcon img, String fileName) {
 		RenderedImage renderedImg = null;
 		
 		if (img.getImage() instanceof RenderedImage) {
 			renderedImg = (RenderedImage) img.getImage ();
 		}
 		else {
 			BufferedImage buffered = new BufferedImage (
 					img.getIconWidth(),
 					img.getIconHeight(),
 					BufferedImage.TYPE_INT_RGB);
 			Graphics2D g2 = buffered.createGraphics();
 			g2.drawImage(img.getImage(), 0, 0, null);
 			g2.dispose();
 			renderedImg = buffered;
 		}
 		
 		String path = "./Avatars/" + fileName + ".png";
 		try {
 			ImageIO.write(renderedImg, "PNG", new File (path));
 		}
 		catch (Exception ex) {
 			Logger.logException("XMLWriter::writeImage", ex);
 		}
 		return path;
 	}
 
 	private static void RecurseWriteChild(DialogueNode node, Element eParent, Document doc) {
 		Element eNode = doc.createElement("node");
 		eParent.appendChild(eNode);
 
 		//id
 		appendInfo(eNode, doc, "id", "" + node.id);
 		//avatar
 		String path = "./Avatars/" + node.playerName + ".png";
		FileIO.writeImage (node.avatar, path, "PNG");
 		appendInfo(eNode, doc, "avatar", path);
 		//name
 		appendInfo(eNode, doc, "name", node.playerName);
 		//text
 		appendInfo(eNode, doc, "text", node.text);
 		//response
 		appendInfo(eNode, doc, "type", node.type.toString());
 
 		if (node.childrenNodes != null) {
 			for (DialogueNode child : node.childrenNodes) {
 				RecurseWriteChild(child, eNode, doc);
 			}
 		}
 	}
 }
