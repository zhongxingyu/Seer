 package game.helper;
 
 import game.EtothGame;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class GameSounds {
 	
 	class SoundFile {
 		private String name;
 		private String sound;
 		
 		public SoundFile(String name, String sound) {
 			this.name = name;
 			this.sound = sound;
 		}
 		
 		public String getName() {
 			return this.name;
 		}
 		
 		public String getSound() {
 			return this.sound;
 		}
 	}
 	private EtothGame game;
 	private ArrayList<SoundFile> sounds;
 	
 	public GameSounds(EtothGame game) throws ParserConfigurationException, 
 			SAXException, IOException {
 		this.game = game;
 		sounds = new ArrayList<SoundFile>();		
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(game.getResourceURL(game.GAMESOUNDSSFILEPATH).openStream());
 		doc.getDocumentElement().normalize();
 		NodeList nodeLst = doc.getElementsByTagName("sound");
 		for (int i = 0; i < nodeLst.getLength(); i++) {
 			Node fstNode = nodeLst.item(i);
 			if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element fstElmnt = (Element) fstNode;
 
 				//TODO: Exceptions?
 				String name = fstElmnt.getAttribute("name");
 				String soundPath = fstElmnt.getTextContent();
 
 				
 				// Load sound files
 				String path = null;
 				try {
 					if(EtothGame.applet)
 					{
 						URL soundUrl = new URL( game.getResourceURL(game.SOUNDPATH) +
 									"/" +
 									soundPath);
 						path = soundUrl.toString();
 					} else {
 						path = game.SOUNDPATH +
 								"/" +
 								soundPath;
 					}
 				} catch(Exception e) {
 					System.out.println("Loading sound " + path + " failed!");
 					e.printStackTrace();
 				}
 				
 				sounds.add(
 					new SoundFile(
 							name,
 							path
 					)
 					
 				);
 			}
 		}
 	}
 	
 	public void playSound(String name) {
 		for (SoundFile soundFile : sounds) {
 			if (name.equals(soundFile.getName())) {
 				game.bsSound.play(soundFile.getSound());
 			}
 		}
 	}
 	
 	public void stopSound(String name) {
 		for (SoundFile soundFile : sounds) {
 			if (name.equals(soundFile.getName())) {
 				game.bsSound.stop(soundFile.getSound());
 			}
 		}
 	}
 }
