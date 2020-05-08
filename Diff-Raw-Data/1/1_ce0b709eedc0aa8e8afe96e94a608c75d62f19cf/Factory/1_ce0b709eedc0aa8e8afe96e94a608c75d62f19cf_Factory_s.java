 package vooga.rts.gamedesign.factories;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 import vooga.rts.gamedesign.sprite.Sprite;
 import vooga.rts.gamedesign.sprite.rtsprite.Resource;
 import vooga.rts.gamedesign.strategy.Strategy;
 import vooga.rts.gamedesign.strategy.attackstrategy.AttackStrategy;
 import vooga.rts.gamedesign.strategy.gatherstrategy.GatherStrategy;
 import vooga.rts.gamedesign.strategy.occupystrategy.OccupyStrategy;
 import vooga.rts.gamedesign.action.Action;
 import vooga.rts.gamedesign.sprite.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.buildings.UpgradeBuilding;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Unit;
 import vooga.rts.gamedesign.strategy.attackstrategy.CanAttack;
 import vooga.rts.gamedesign.upgrades.UpgradeNode;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 /** 
  *  This class is in charge of the loading of input XML files for different
  *  class types. It will figure out the class type this given file is in charge
  *  of, and pass the information to the corresponding decoder. All the decoders
  *  are loaded through an input file.
  *  
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu 
  */
 
 public class Factory {
	//BUGBUG: the file path will break code! :/ (two places: here and in main())
 
 	public static final String DECODER_MATCHING_FILE = "DecodeMatchUp";
 	Map<String, Decoder> myDecoders = new HashMap<String, Decoder>();
 	Map<String, Sprite> mySprites;
 	Map<String, Strategy> myStrategies;
 	
 	
 	public Factory() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, SAXException, IOException {
 		myDecoders = new HashMap<String, Decoder>();
 		loadDecoder(DECODER_MATCHING_FILE);
 		mySprites = new HashMap<String, Sprite>();
 		myStrategies = new HashMap<String, Strategy>();
 	}
 	
 	
 	public void put(String name, Sprite value){
 		mySprites.put(name, value);
 	}
 	
 	public void put(String name, Strategy value){
 		myStrategies.put(name, value);
 	}
 	
 	public AttackStrategy getAttackStrategy(String key){
 		return (AttackStrategy) myStrategies.get(key);
 	}
 	
 	public GatherStrategy getGatherStrategy(String key){
 		return (GatherStrategy) myStrategies.get(key);
 	}
 	
 	public OccupyStrategy getOccupyStrategy(String key){
 		return (OccupyStrategy) myStrategies.get(key);
 	}
 	
 	public Sprite getSprite(String key){
 		return mySprites.get(key);
 	}
 	
 	/**
 	 * Creates decoders by loading the input file that specifies the path of
 	 * each Decoder and the type of class it is in charge of. Puts the decoders
 	 * and their corresponding types into a map.
 	 * 
 	 * This method will be called when the Factory class is created.
 	 * 
 	 * @param fileName the name of the XML file that specifies decoder paths.
 	 * @throws ClassNotFoundException
 	 * @throws IllegalArgumentException
 	 * @throws SecurityException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 * @throws NoSuchMethodException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private void loadDecoder(String fileName) throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, SAXException, IOException {
 		File file = new File(getClass().getResource(fileName).getFile());
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(file);
 		doc.getDocumentElement().normalize();
 		
 		NodeList nodeLst = doc.getElementsByTagName("pair");
 		
 		for (int i = 0; i < nodeLst.getLength(); i++) {
 			Element pairElmnt = (Element) nodeLst.item(i);
 			
 			Element typeElmnt = (Element)pairElmnt.getElementsByTagName("type").item(0);
 			NodeList typeList = typeElmnt.getChildNodes();
 			String type = ((Node) typeList.item(0)).getNodeValue();
 			
 			Element pathElmnt = (Element)pairElmnt.getElementsByTagName("decoderPath").item(0);
 			NodeList pathList = pathElmnt.getChildNodes();
 			String path = ((Node) pathList.item(0)).getNodeValue();
 			
 			Class<?> headClass =
 					Class.forName(path);
 			Decoder decoder = (Decoder) headClass.getConstructor(Factory.class).newInstance(this);
 			myDecoders.put(type, decoder);
 		}
 	}
 	
 	/**
 	 * Loads the XML file passed in and determines the type of class it provides
 	 * information for. Then passes the input file to the corresponding decoder
 	 * in charge of that type of class.
 	 * 
 	 * @param fileName the name of the XML file that provides class information
 	 * and to be loaded
 	 */
 	public <T extends Object> T loadXMLFile(String fileName) {
 		Object result = new Object();
 		try {
 			File file = new File(getClass().getResource(fileName).getFile());
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(file);
 			doc.getDocumentElement().normalize();
 			System.out.println(doc.getDocumentElement().getNodeName());
 			result = myDecoders.get(doc.getDocumentElement().getNodeName()).create(doc);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		printTree((UpgradeTree) result);
 		return (T) result;
 	}
 	
 	/**
 	 * TESTING PURPOSE. PRINTS TREE.
 	 * @param upgradeTree
 	 */
 	private void printTree(UpgradeTree upgradeTree) {
 		for (UpgradeNode u: upgradeTree.getHead().getChildren()) {
 			UpgradeNode current = u;
 			while (!current.getChildren().isEmpty()) {
 				System.out.println("Type: " + current.getChildren().get(0).getUpgradeType() +
 						" Parent ID " + current.getID() + " ID " + 
 						current.getChildren().get(0).getID());
 				current = current.getChildren().get(0);
 			}
 		}
 	}
 	
 	/**
 	 * TESTING PURPOSE
 	 */
 	/**public static void main(String[] args) throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, SAXException, IOException {
 		//loads Upgrade XML - creates tree - updates activate state
 		Factory factory = new Factory();
 
 		factory.loadXMLFile("src/vooga/rts/gamedesign/factories/Factory.xml");
 
 		//creates an UpgradeBuilding
 		UpgradeBuilding upgradeBuilding = new UpgradeBuilding();
 		
 		//creates two Units - adds upgrade Actions to the UpgradeBuilding
 		//the first Unit needs to specify the UpgradeTree all Units will be using.
 		InteractiveEntity oneUnit = new Unit();
 		//oneUnit.setUpgradeTree(resultTree);
 		upgradeBuilding.addUpgradeActions(resultTree);
 		InteractiveEntity twoUnit = new Unit();
 		oneUnit.setAttackStrategy(new CanAttack());
 		twoUnit.setAttackStrategy(new CanAttack());
 		for (Action a: upgradeBuilding.getActions()) {
 			System.out.println("Action type: " + a.getName());
 		}
 		System.out.println(oneUnit.getMaxHealth());
 		System.out.println(twoUnit.getMaxHealth());
 		
 		//finds Action  - 
 		Action WorstArmorAction = upgradeBuilding.findAction("Boost1");
 		//WorstArmorAction.apply();
 		System.out.println(oneUnit.getMaxHealth());
 		System.out.println(twoUnit.getMaxHealth());
 	}*/
 }
