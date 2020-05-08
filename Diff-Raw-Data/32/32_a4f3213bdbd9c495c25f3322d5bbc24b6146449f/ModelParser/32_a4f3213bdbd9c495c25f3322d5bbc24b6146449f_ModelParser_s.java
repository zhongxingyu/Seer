 package model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class ModelParser {
 	
 	private static Hashtable<String,Model[]> loadedModels = new Hashtable<String,Model[]>();
 
 	public static Model[] getModel(String loc) {
 		return (loadedModels.containsKey(loc)) ? (loadedModels.get(loc)) : parseModel(loc);
 	}
 	
 	private static Model[] parseModel(String loc) {
 		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
 		
 		DocumentBuilder builder = null;
 		try {
 			builder = builderFactory.newDocumentBuilder();
 		} catch(ParserConfigurationException exception) { 
 			exception.printStackTrace();
 		}
 		
 		Document document = null;
 		try {
 			document = builder.parse(new File("res/model/" + loc));
 		} catch(IOException exception) {
 			exception.printStackTrace();
 		} catch(SAXException exception) {
 			exception.printStackTrace();
 		}
 		
 		document.getDocumentElement().normalize();
 		
 		NodeList nodes = document.getElementsByTagName("geometry");
 		ModelData[] modelData = new ModelData[nodes.getLength()];
 		for(int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
 			
			ModelData currentData = new ModelData();
			NamedNodeMap map = node.getAttributes();
			currentData.name = map.item(0).getNodeValue();
 			
			Node meshNode = node.getChildNodes().item(0);
			NodeList 
 			
 		}
 		
 		return null;
 	}
 }
