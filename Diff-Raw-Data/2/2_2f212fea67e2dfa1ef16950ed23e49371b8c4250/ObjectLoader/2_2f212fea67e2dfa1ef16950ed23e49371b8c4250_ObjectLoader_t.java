 package vooga.fighter.objects;
 
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import vooga.fighter.util.Location;
 import vooga.fighter.util.Pixmap;
 
 public class ObjectLoader {
 	
 	private int[] myMovespeeds;
 	private Pixmap[] myImages;
 	
 	public ObjectLoader () {
 		myImages = new Pixmap[10];
 		myMovespeeds = new int[10];
 		init();
 	}
 
 	public void init() {
 		try {
 
 			File objectFile = new File("src/vooga/fighter/config/objects.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(objectFile);
 			doc.getDocumentElement().normalize();
 
 			NodeList nodes = doc.getElementsByTagName("character");
 
 			for (int i = 0; i < nodes.getLength(); i++) {
 				Node node = nodes.item(i);
 
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					Element element = (Element) node;
 					int id = Integer.parseInt(getValue("id", element));
 					Pixmap image = new Pixmap(getValue("image",element));
 					int movespeed = Integer.parseInt(getValue("movespeed", element));
 					myImages[0] = image;
					myMovespeeds[0] = movespeed;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static String getValue(String tag, Element element) {
 		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
 		Node node = (Node) nodes.item(0);
 		return node.getNodeValue();
 	}
 	
 	public CharacterObject getTestCharacter () {
 		Location beginningLocation = new Location(100, 100);
 		return new CharacterObject(myImages[0], beginningLocation, myMovespeeds[0]);
 	}
 }
