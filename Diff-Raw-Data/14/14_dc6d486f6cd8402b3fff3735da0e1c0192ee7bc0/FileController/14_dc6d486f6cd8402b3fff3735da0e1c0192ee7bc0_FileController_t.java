 package controller;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
import model.Game;
 import model.Player;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 public class FileController {
 	/*
 	 * Deze class is er om een XML op te slaan (of variant) zodat de scores
 	 * opgeslagen blijven voor elke LAN Tevens handelt deze class het inladen
 	 * in.
 	 */
 	
 	private String location = "src/resources/Lan4Life.xml";
 	public static FileController instance;
 	
 	public static FileController getInstance() {
 		if (instance == null)
 			instance = new FileController();
 		return instance;
 	}
 	
 	private FileController() {
 		// TODO: Read XML and push data through controllers
 		
 	}
 	
 	public void addGame(String name, boolean isFFA) {
 		
 	}
 	
 	public void addPlayer(String name) {
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.parse(location);
 			
 			Element players = doc.getElementById("Players");
 			
 			// Output
 			TransformerFactory transformerFactory = TransformerFactory
 					.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			File newFile = new File(location);
 			newFile.createNewFile();
 			StreamResult result = new StreamResult(newFile);
 			transformer.transform(source, result);
 			System.out.println("File updated!");
 			
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		} catch (TransformerException tfe) {
 			tfe.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		} catch (SAXException sae) {
 			sae.printStackTrace();
 		}
 	}
 	
 	public boolean L4LExists() {
 		return new File(location).isFile();
 	}
 	
 	public void makeL4Lfile() {
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			// TODO: Load shit from controllers
 			
 			// root elements
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("LAN4LIFE");
 			doc.appendChild(rootElement);
 			
 			// Players elements (1/3)
 			Element players = doc.createElement("Players");
 			
 			for (Player p : PlayerController.getInstance().getPlayers())
 				players.appendChild(p.toXML(doc));
 			
 			// playerName.setNodeValue("Rob");
 			// player.appendChild(playerName);
 			rootElement.appendChild(players);
 			
 			// Games elements (2/3)
 			Element games = doc.createElement("Games");
			for (Game g : GameController.getInstance().getGames())
				games.appendChild(g.toXML(doc));

 			// Rounds elements (3/3)
 			Element rounds = doc.createElement("Rounds");
 			rootElement.appendChild(rounds);
 			
 			// Output
 			TransformerFactory transformerFactory = TransformerFactory
 					.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			File newFile = new File(location);
 			newFile.createNewFile();
 			StreamResult result = new StreamResult(newFile);
 			transformer.transform(source, result);
 			System.out.println("File saved!");
 			
 		} catch (IOException e) {
 			e.printStackTrace(System.out);
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace(System.out);
 		} catch (TransformerException tfe) {
 			tfe.printStackTrace(System.out);
 		}
 	}
 	
 }
