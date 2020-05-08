 package com.patfgeneva.lobbies.worlds;
 
 import java.io.File;
import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
 import com.patfgeneva.lobbies.LobbiesPlugin;
 
 public class AdventureWorld extends LobbyWorld{
 
 	private String version;
 	private String author;
 	private String weblink;
 	private String creator;
 	private ArrayList<String> info;
 	private double mapStartTime;
 	private double mapTime;
 	//private whitelist whitelistFile;
 
 	public AdventureWorld(LobbiesPlugin plugin, String worldName, String sourceDirectory, String xmlPath, boolean netherworld, boolean endworld){
 		//pass info to super class
 		super(plugin, worldName, sourceDirectory, xmlPath, netherworld, endworld);
 		//record the tie when the map was created
 		mapStartTime = System.currentTimeMillis();
 		//TODO: read in xml file...
 		readXmlFile(xmlPath);
 
 	}
 
 	private void readXmlFile(String path){
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		try {
			doc = docBuilder.parse(new File(path));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
 		doc.getDocumentElement().normalize();
 
 		NodeList config = doc.getElementsByTagName("map");
 		Node configNode = config.item(0);
 		NamedNodeMap configAttr = configNode.getAttributes();
 		//get all the info from the xml file
 		try {
 			configAttr.getNamedItem("version").getTextContent();
 			configAttr.getNamedItem("author").getTextContent();
 			configAttr.getNamedItem("weblink").getTextContent();
 			configAttr.getNamedItem("creator").getTextContent();
 			configAttr.getNamedItem("info").getTextContent();
 			configAttr.getNamedItem("mapTime").getTextContent();			
 		} catch (Exception e) {
 
 		}
 
 	}
 }
