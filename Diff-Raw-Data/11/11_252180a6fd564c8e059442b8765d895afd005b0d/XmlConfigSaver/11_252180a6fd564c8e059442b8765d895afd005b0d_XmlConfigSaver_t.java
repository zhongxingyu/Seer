 package ghosty.config.saver;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import ghosty.config.utils.ConfigMapList;
 import ghosty.config.utils.ConfigMapListIterator;
 import ghosty.files.FileFactory;
 
 public class XmlConfigSaver implements ConfigSaverInterface {
 
 	@Override
 	public void save(String path, ConfigMapList config) throws SaveException {
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		
 		try {
 			docBuilder = dbFactory.newDocumentBuilder();
 			Document doc;
 			
 			doc = docBuilder.newDocument();
 			
 			ConfigMapListIterator i = (ConfigMapListIterator) config.iterator();
 			
 			Element rootNode = doc.createElement("configuration");
 			doc.appendChild(rootNode);
 
 			while (i.hasNext()) {
 				Element option = doc.createElement("option");
 				
 				Element key = doc.createElement("key");
 				key.appendChild(doc.createTextNode(i.nextKey()));
 				
 				Element value = doc.createElement("value");
 				value.appendChild(doc.createTextNode(i.next()));
 				
 				option.appendChild(key);
 				option.appendChild(value);
 				
				rootNode.appendChild(option);
				
 			}
 			
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(FileFactory.getInstance().getFile(path).getOutputStream());
 			
 			// Testing
			//StreamResult result = new StreamResult(System.out);
 			
			transformer.transform(source, result);
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new SaveException("Impossible to save the configuration in file \"" + path + "\"");
 		}
 
 	}
 
 }
