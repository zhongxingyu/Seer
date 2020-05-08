 package hci;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import java.io.File;
 import java.util.ArrayList;
 import hci.utils.Point;
 import hci.XMLOutput;
 
 public class ReadObjects {
 	ArrayList<ArrayList<hci.utils.Point>> objs = new ArrayList<ArrayList<hci.utils.Point>>();
 	ArrayList<hci.utils.Point> nodes = null;
 	ArrayList<String> links = null;
 	Point p = null;
 	String x = new String();
 	String y = new String();
 	ArrayList<String> labels = new ArrayList<String>();
 	XMLOutput xOut = new XMLOutput();
 	public XMLOutput loadFile(String path) {
 		try {
 			File fXmlFile = new File(path + ".xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 			
 			NodeList nList = doc.getElementsByTagName("objectNodes");
 			
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				Element obj = (Element) nList.item(temp);
 				String labelText = obj.getAttribute("label");
 				labels.add(labelText);
				System.out.println(labels);
 				nodes = new ArrayList<hci.utils.Point>();
 				NodeList nListTwo = doc.getElementsByTagName("nodePoint");
 				for (int tempNode = 0; tempNode < nListTwo.getLength(); tempNode++){
 					Node nNode = nListTwo.item(tempNode);
 					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 						 
 						Element eElement = (Element) nNode;
 				 
 						x = getTagValue("x", eElement);
 						y = getTagValue("y", eElement);
 						p = new hci.utils.Point(Integer.parseInt(x),Integer.parseInt(y));
 						nodes.add(p);
 				 
 					}
 				}
 				objs.add(nodes);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		xOut.setLabels(labels);
 		xOut.setObjects(objs);
 		return xOut;
 	}
 	
 	private static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 			 
 		Node nValue = (Node) nlList.item(0);
 			 
 		return nValue.getNodeValue();
 	}
 }
