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
 	// TODO(jtibs): fix these paths
 	private static final String[] INPUT_DIRS = {"../../revhistories/negative", "../../revhistories/positive"};
 	
 	// TODO(jtibs): actually deal with these exceptions
 	public List<WikiDocument> parse() {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			List<WikiDocument> documents = new ArrayList<WikiDocument>();
 			String lastId = null;  // keep track of the ID of the last page we parsed, in case
 								   // revisions for a document are split across several files
 			WikiDocument result = null;
 			for (int i = 0; i < INPUT_DIRS.length; i++) {
 				File dir = new File(INPUT_DIRS[i]);
 				for (File file : dir.listFiles()) {
 					System.err.println(file);
 					Document xmlDocument = builder.parse(file);
 				
 					String fileName = file.getName();
 					String id = fileName.substring(0, fileName.indexOf('-'));
					if (! id.equals(lastId)) {
 						if (result != null) documents.add(result);  // we have finished parsing the previous document
 						result = new WikiDocument(fileName, i);  // start a new document
 						lastId = id;
 					}
 					parseDocument(xmlDocument, result);
 				}
 			}
 			return documents;
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Parses the given XML document and updates result.revisions
 	 */
 	private WikiDocument parseDocument(Document xmlDocument, WikiDocument result) {
 		NodeList revisions = xmlDocument.getElementsByTagName("rev");
 		for (int i = 0; i < revisions.getLength(); i++) {
 			NamedNodeMap attributes = revisions.item(i).getAttributes();
 
 			Node userAttr = attributes.getNamedItem("user");
 			String user = "";
 			if (userAttr != null) user = userAttr.getNodeValue();
 			
 			String timestamp = attributes.getNamedItem("timestamp").getNodeValue();
 	
 			Node commentAttr = attributes.getNamedItem("comment");
 			String comment = "";
 			if (commentAttr != null) comment = commentAttr.getNodeValue();
 			
 			Node sizeAttr = attributes.getNamedItem("size");
 			int size = 0;
 			if (sizeAttr != null) 
 				size = Integer.parseInt(sizeAttr.getNodeValue());
 
 			// TODO(jtibs): figure out why most revisions are missing the "id" attribute
 			result.revisions.add(new Revision("", timestamp, user, comment, size));
 		}
 		return result;
 	}
 }
