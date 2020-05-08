 package vooga.rts.gamedesign.factories;
 
 import java.awt.Dimension;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import vooga.rts.gamedesign.sprite.gamesprites.*;
 import vooga.rts.util.Location;
import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 
 
 /**
  * Decodes an XML file that contains resource information and instantiates those reasources. 
  * Once the resources and instantiated it puts them in the Sprite map that is found in the factory. 
  * The resource "name" is used as the key. 
  * @author agostif
  *
  */
 public class ResourceDecoder extends Decoder{
 	
 	private static final Dimension RESOURCE_SIZE = new Dimension(50,50);
 	
 	private static final String HEAD_TAG = "resources";
 	private static final String TYPE_TAG = "resource";
 	private static final String NAME_TAG = "name";
 	private static final String IMAGE_TAG = "img";
 	private static final String HEALTH_TAG = "health";
 	private static final String SOURCE_TAG = "src";
 	
 	private Factory myFactory;
 	
 	
 	public ResourceDecoder(Factory factory){
 		myFactory = factory;
 	}
 		
 	/**
 	 * Adds the resources defined in the XML file to the map of Sprites found in the factory. 
 	 * 
 	 * @Override
 	 * @throws ClassNotFoundException
 	 * @throws IllegalArgumentException
 	 * @throws SecurityException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 * @throws NoSuchMethodException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 */
 	public void create(Document doc) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		String path = doc.getElementsByTagName(HEAD_TAG).item(0).getAttributes().getNamedItem(SOURCE_TAG).getTextContent();
 		Class<?> headClass = null;
 		try {
 			headClass = Class.forName(path);
 		} catch (ClassNotFoundException e) {
 			//ERRR
 		}
 		NodeList nodeLst = doc.getElementsByTagName(TYPE_TAG);
 		
 		for(int i = 0 ; i < nodeLst.getLength() ; i++){
 			Element nElement = (Element) nodeLst.item(i);
 			String name = nElement.getElementsByTagName(NAME_TAG).item(0).getTextContent();
 			String img = nElement.getElementsByTagName(IMAGE_TAG).item(0).getTextContent();
 			int health = Integer.parseInt((nElement.getElementsByTagName(HEALTH_TAG).item(0).getTextContent()));
 			
 			
 			
 			Resource res = (Resource) headClass.getConstructor(Pixmap.class, 
													Location3D.class, 
 													Dimension.class, 
 													int.class, 
 													int.class).newInstance(new Pixmap(img),
																			new Location3D(0,0,0),
 																			RESOURCE_SIZE,
 																			0,
 																			health);																	
 			myFactory.put(name, res);
 		}
 	}
 }
