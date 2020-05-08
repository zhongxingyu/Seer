 package vooga.fighter.objects;
 
 import java.awt.Rectangle;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import vooga.fighter.objects.utils.State;
<<<<<<< HEAD
 import vooga.fighter.objects.utils.UpdatableLocation;
import vooga.fighter.util.Pixmap;
=======
 import util.Pixmap;
>>>>>>> f1197b87aa65124773687aa36f73b32a4eb2c34a
 
 public class CharacterLoader extends ObjectLoader {
 	
 	private static final String CHARACTER_PATH = "src/vooga/fighter/config/objects.xml";
 	
 	private CharacterObject myChar;
 
 	public CharacterLoader (int charId, CharacterObject character) {
 		super(CHARACTER_PATH);
 		myChar = character;
 		load(charId);
 	}
 
 	public void load(int charId) {
 		Document doc = getDocument();
 		NodeList charNodes = doc.getElementsByTagName("character");
 
 		for (int i = 0; i < charNodes.getLength(); i++) {
 			Node node = charNodes.item(i);
 			int id = Integer.parseInt(node.getAttributes().getNamedItem("charID").getTextContent());
 			if (id == charId) {
 				String charName = getAttributeValue(node, "charName");
 				int maxHealth = Integer.parseInt(getAttributeValue(node, "maxHealth"));
 				myChar.setHealth(maxHealth);
 				NodeList stateNodes = doc.getElementsByTagName("state");
 				addStates(stateNodes);
 			}
 		}
 	}
 	
 	public void addStates(NodeList stateNodes) {
 		for (int i = 0; i < stateNodes.getLength(); i++) {
 			Element state = (Element) stateNodes.item(i);
 			String stateName = getAttributeValue(stateNodes.item(i), "stateName");
 			NodeList frameNodes = state.getElementsByTagName("frame");
 			for (int j = 0; j < frameNodes.getLength(); j++) {
 				State newState = new State(myChar, frameNodes.getLength());
 				newState.populateImage(new Pixmap(getAttributeValue(frameNodes.item(j), "image")), j);
 				Element frame = (Element) frameNodes.item(j);
 				Node hitboxNode = frame.getElementsByTagName("hitbox").item(0);
 				newState.populateRectangle(new Rectangle(Integer.parseInt(getAttributeValue(hitboxNode, "cornerX")),
 						Integer.parseInt(getAttributeValue(hitboxNode, "cornerY")), Integer.parseInt(getAttributeValue(hitboxNode, "rectX")),
 						Integer.parseInt(getAttributeValue(hitboxNode, "rectY"))), j);
 				myChar.addState(stateName, newState);
 			}
 		}
 	}
 }
