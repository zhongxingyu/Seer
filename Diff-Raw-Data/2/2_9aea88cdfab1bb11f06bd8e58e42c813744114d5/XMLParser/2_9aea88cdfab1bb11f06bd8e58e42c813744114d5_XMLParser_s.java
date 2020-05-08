 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class XMLParser {
 	// TODO(jtibs): fix this path
	private static final String INPUT_DIR = "../revhistories";
 	
 	// TODO(jtibs): actually deal with these exceptions
 	public List<WikiDocument> parse() {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			List<WikiDocument> documents = new ArrayList<WikiDocument>();
 			File dir = new File(INPUT_DIR);
 			for (File file : dir.listFiles()) {
 				Document xmlDocument = builder.parse(file);
 				documents.add(parseDocument(xmlDocument));
 				break;  // temporary!!
 			}
 			return documents;
 		} catch (IOException e) {
 		} catch (ParserConfigurationException e) {
 		} catch (SAXException e) {}
 		return null;
 	}
 
 	private WikiDocument parseDocument(Document xmlDocument) {
 		NodeList revisions = xmlDocument.getElementsByTagName("rev");
 		WikiDocument document = new WikiDocument("fake title", 0);
 		for (int i = 0; i < revisions.getLength(); i++) {
 			NamedNodeMap attributes = revisions.item(i).getAttributes();
 
 			String id = attributes.getNamedItem("user").getNodeValue();
 			String timestamp = attributes.getNamedItem("timestamp").getNodeValue();
 			String user = attributes.getNamedItem("user").getNodeValue();
 			String comment = attributes.getNamedItem("comment").getNodeValue();
 			
 			Node sizeAttr = attributes.getNamedItem("size");
 			int size = 0;
 			if (sizeAttr != null) 
 				size = Integer.parseInt(sizeAttr.getNodeValue());
 
 			document.revisions.add(new Revision(id, timestamp, user, comment, size));
 		}
 		return document;
 	}
 }
