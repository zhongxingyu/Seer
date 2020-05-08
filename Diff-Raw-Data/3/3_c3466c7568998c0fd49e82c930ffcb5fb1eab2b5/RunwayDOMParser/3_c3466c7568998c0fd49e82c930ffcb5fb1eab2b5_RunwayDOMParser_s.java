 package xmlParserTest;
 
 import java.io.File;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 
 
 import org.w3c.dom.Document;
 
 public class RunwayDOMParser {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 
 
 
 		try {

			File fXmlFile = new File("/Users/armiller5/Documents/workspace/RunwayTemplate/doc/individual/schema(0001352140861497)HelloWorld.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 
 			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
 			NodeList mdBusinessNodeList = doc.getElementsByTagName(XMLTags.MD_BUSINESS_TAG);
 			System.out.println("-----------------------");
 
 			for (int i = 0; i < mdBusinessNodeList.getLength(); i++) {
 
 				Node mdBusinessnNode = mdBusinessNodeList.item(i);
 				NodeList attributesNodeList = mdBusinessnNode.getChildNodes();
 
 
 				for (int j = 0; j < attributesNodeList.getLength(); j++) {
 					Node attributeNode = attributesNodeList.item(j);
 
 
 				}
 
 
 				//System.out.println("Attribute : " + getTagValue(XMLTags.ATTRIBUTES_TAG, eElement));
 
 
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 
 		Node nValue = (Node) nlList.item(0);
 
 		return nValue.getNodeValue();
 	}
 	
 	private static String getTagValueExperimental(String sTag, Node node) {
 
 	return null;
 	}
 
 }
 
