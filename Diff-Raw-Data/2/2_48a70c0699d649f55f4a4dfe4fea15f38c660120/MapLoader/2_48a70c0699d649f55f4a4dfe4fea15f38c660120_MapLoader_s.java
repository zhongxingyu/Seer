 package vooga.fighter.objects;
 
 
 import java.awt.Dimension;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import util.Pixmap;
 import vooga.fighter.objects.utils.State;
 import vooga.fighter.objects.utils.UpdatableLocation;
 
 
 /**
  * 
  * @author alanni
  * Loads the data to the map
  */
 
 public class MapLoader extends ObjectLoader {
 
 
 	private static final String CHARACTER_PATH = "src/vooga/fighter/config/maps.xml";
 
 	private MapObject myMap;
 
 	public MapLoader (int mapId, MapObject map) {
 		super(CHARACTER_PATH);
 		myMap = map;
 		load(mapId);
 	}
 
 	/**
 	 * Loads map from xml data
 	 */
 	public void load(int mapId) {
 		Document doc = getDocument();
 		NodeList mapNodes = doc.getElementsByTagName("map");
 
 		for (int i = 0; i < mapNodes.getLength(); i++) {
 			Element node = (Element) mapNodes.item(i);
 			int id = Integer.parseInt(getAttributeValue(node, "mapID"));
 			if (id == mapId) {
 				State mapState = new State(myMap, 1);
 				mapState.populateImage(new Pixmap(getAttributeValue(node, "enviroBackground")), 0);
				Node mapSize = node.getElementsByTagName("enviroObject").item(0);
 				mapState.populateSize(new Dimension(Integer.parseInt(getAttributeValue(mapSize, "xSize")),
 						Integer.parseInt(getAttributeValue(mapSize, "ySize"))), 0);;
 				myMap.addState("background", mapState);
 				NodeList enviroObjectNodes = node.getElementsByTagName("enviroObject");
 				addEnviroObjects(enviroObjectNodes);
 				NodeList startingPosNodes= node.getElementsByTagName("startingPos");
 				addStartingPositions(startingPosNodes);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Adds starting position for the characters
 	 */
 
 	private void addStartingPositions(NodeList startingPosNodes) {
 		for (int i=0; i<startingPosNodes.getLength(); i++){
 			Node startingPosition= startingPosNodes.item(i);
 			int xCoord= Integer.parseInt(getAttributeValue(startingPosition, "xCoord"));
 			int yCoord= Integer.parseInt(getAttributeValue(startingPosition, "yCoord"));
 			myMap.addStartPosition(new UpdatableLocation(xCoord,yCoord));
 		}
 
 	}
 
 	/**
 	 * Creates environment objects based on XML data
 	 */
 	private void addEnviroObjects(NodeList enviroObjectNodes) {
 		for (int i = 0; i < enviroObjectNodes.getLength(); i++) {
 			Node environmentObject = enviroObjectNodes.item(i);
 			//String imagePath= getAttributeValue(environmentObject, "image");
 			int xCoord= Integer.parseInt(getAttributeValue(environmentObject, "xCoord"));
 			int yCoord= Integer.parseInt(getAttributeValue(environmentObject, "yCoord"));
 			EnvironmentObject toAdd= new EnvironmentObject(new UpdatableLocation(xCoord, yCoord));
 			NodeList stateNodes = ((Element) environmentObject).getElementsByTagName("state");
 			addStates(stateNodes, toAdd);
 		}
 	}
 
 
 }
