 package mhyhre.lightrabbit.game.Levels;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import mhyhre.lightrabbit.MainActivity;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.util.Log;
 
 public class CharacterBase {
 
 	Map<Integer, Character> characters;
 
 	public CharacterBase(String filename) {
 
 		characters = new HashMap<Integer, Character>();
 
 		try {
 			InputStream is = MainActivity.Me.getAssets().open(MainActivity.LEVELS_FOLDER + filename);
 
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
 
 			Document doc = docBuilder.parse(is);
 
 			Element root = doc.getDocumentElement();
 			root.normalize();
 
 			parseXml(root);
 
 		} catch (IOException e) {
 			Log.w(MainActivity.DEBUG_ID, "CharacterBase IO:" + e);
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 
 		} catch (SAXException e) {
 			e.printStackTrace();
 
 		}
 		
 		Log.i(MainActivity.DEBUG_ID, " ");
 		Log.i(MainActivity.DEBUG_ID, "Character base " + filename + " loaded and contain " +characters.size()+ " elements:");
 		for (final Map.Entry<Integer, Character> entry : characters.entrySet()) {
 			Log.i(MainActivity.DEBUG_ID, "  ID: "+ entry.getKey() + " " + entry.getValue());
 		}
 	}
 
 	
	private static final String CHARACTER = "character";
	private static final String ID = "id";
	private static final String ICON = "icon";
	private static final String NAME = "name";
	
 	private void parseXml(Element rootElement) {
 
 		NodeList items = rootElement.getElementsByTagName(CHARACTER);
 
 		for (int i = 0; i < items.getLength(); i++) {
 
 			Element element = (Element) items.item(i);
 
 			int id = Integer.parseInt(element.getAttribute(ID));
 
 			Character character = new Character();
 			character.setName(element.getAttribute(NAME));
 			character.setIconName(element.getAttribute(ICON));
 
 			characters.put(id, character);
 		}

 	}
 
 }
