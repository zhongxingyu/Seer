 package vooga.fighter.model.loaders;
 
 import java.awt.Rectangle;
 import java.io.File;
 import java.util.ResourceBundle;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import util.Pixmap;
 import vooga.fighter.model.objects.GameObject;
 import vooga.fighter.model.utils.State;
 
 /**
  * Abstract class with shared methods that all ObjectLoader-derived classes may need.
  * 
  * @author Dayvid, alanni
  *
  */
 public abstract class ObjectLoader {
 
 	private static final String RESOURCE_PATH = "vooga.fighter.config.objects";
 	private static final String RESOURCE_DEFAULT_VALUES_PATH="vooga.fighter.config.defaultvalues";
 	private File myObjectFile;
 	private Document myDocument;
 	private ResourceBundle myResources;
 	private ResourceBundle myDefaults; 
 
 	/**
 	 * Points to the xml file that the loader will be parsing
 	 * 
 	 * @param objectPath
 	 */
 	public ObjectLoader (String pathName) {
 		myResources = ResourceBundle.getBundle(RESOURCE_PATH);
		myDefaults= ResourceBundle.getBundle(RESOURCE_DEFAULT_VALUES_PATH);
 		String objectPath = myResources.getString(pathName);
 		myObjectFile = new File(objectPath);
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder;
 		try {
 			dBuilder = dbFactory.newDocumentBuilder();
 			myDocument = dBuilder.parse(myObjectFile);
 			myDocument.getDocumentElement().normalize();
 		} catch (Exception e) {
 			myDocument = null;
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Loads object based on the name given
 	 * @param name
 	 */
 	protected abstract void load(String name);
 
 	/**
 	 * Returns the xml document which the loader points to
 	 * @return
 	 */
 	protected Document getDocument() {
 		return myDocument;
 	}
 
 	/**
 	 * Returns resource bundle which contains what the names of nodes in the xml files should be.
 	 */
 	protected ResourceBundle getResourceBundle() {
 		return myResources;
 	}
 
 	/**
 	 * Returns the string attribute value of the specified tag for the specified mode.
 	 * @param node
 	 * @param tag
 	 * @return
 	 */
 	protected String getAttributeValue(Node node, String tag) {
		try{
			String value= node.getAttributes().getNamedItem(tag).getTextContent();
			return value; 
		}
		catch(NullPointerException e){
			String value= myDefaults.getString(getClass().getSimpleName()+tag);
			System.err.println("Property "+ tag+ " not set.  Default value is "+value);
			return value;
 		}
 	}
 
 	/**
 	 * Returns the value of the child node with the specified tag for the element.
 	 * @param tag
 	 * @param element
 	 * @return
 	 */
 	protected String getChildValue(String tag, Element element) {
 		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
 		Node node = (Node) nodes.item(0);
 		return node.getNodeValue();
 	}
 
 	/**
 	 * Loads and adds states for the GameObject.
 	 * @param stateNodes
 	 * @param myObject
 	 */
 	protected void addStates(NodeList stateNodes, GameObject myObject) {
 		for (int i = 0; i < stateNodes.getLength(); i++) {
 			Element state = (Element) stateNodes.item(i);
 			String stateName = getAttributeValue(stateNodes.item(i), "stateName");
 			NodeList frameNodes = state.getElementsByTagName("frame");
 			State newState = new State(myObject, frameNodes.getLength());
 			getImageAndHitboxProperties(frameNodes, newState);
 			myObject.addState(stateName, newState);
 		}
 	}
 
 	/**
 	 * Loads frames and states for the objects 
 	 */
 	protected void getImageAndHitboxProperties(NodeList frameNodes, State newState){
 		for (int j = 0; j < frameNodes.getLength(); j++) {
 			Element frame = (Element) frameNodes.item(j);
 			if (frame.getAttributes().getNamedItem("image") != null){
 				newState.populateImage(new Pixmap(getAttributeValue(frame, "image")), j);
 			}
 			NodeList hitboxNodes = frame.getElementsByTagName("hitbox"); 
 			for (int k=0; k<hitboxNodes.getLength(); k++){
 				newState.populateRectangle(new Rectangle(Integer.parseInt(getAttributeValue(hitboxNodes.item(k), "cornerX")),
 						Integer.parseInt(getAttributeValue(hitboxNodes.item(k), "cornerY")), Integer.parseInt(getAttributeValue(hitboxNodes.item(k), "rectX")),
 						Integer.parseInt(getAttributeValue(hitboxNodes.item(k), "rectY"))), j);
 			}
 		}
 	}
 }
 
