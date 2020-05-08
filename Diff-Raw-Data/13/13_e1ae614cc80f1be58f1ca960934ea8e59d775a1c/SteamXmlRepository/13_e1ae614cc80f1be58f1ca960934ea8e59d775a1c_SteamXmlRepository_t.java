 package repository;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import model.*;
 
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 public class SteamXmlRepository {
 	
	XmlSteamMapper mapper = null;
	
	public SteamXmlRepository()
	{
		mapper = new XmlSteamMapper();
	}
	
 	private Document getXmlDoc(String uri, DocumentBuilderFactory dbf) {
 		Document result = null;
 		String generalErr = "Error resolving uri: %s - %s";
 		
 		//get the factory
 		try {
 	
 			//Using factory get an instance of document builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 	
 			//parse using builder to get DOM representation of the XML file
 			result = db.parse(uri);
 	
 		}catch(ParserConfigurationException pce) {
 			//pce.printStackTrace();
 			//TODO: log exception
 			System.err.println(String.format(generalErr, pce.getClass(), pce.getLocalizedMessage()));
 		}catch(SAXException se) {
 			//se.printStackTrace();
 			//TODO: log exception
 			System.err.println(String.format(generalErr, se.getClass(), se.getLocalizedMessage()));
 		}catch(IOException ioe) {
 			//ioe.printStackTrace();
 			//TODO: log exception
 			System.err.println(String.format(generalErr, ioe.getClass(), ioe.getLocalizedMessage()));
 		}
 		return result;
 	}
 	
 	public SteamProfile getSteamProfile(long id)
 	{
 		String uri = String.format("http://www.steamcommunity.com/profiles/%s?xml=1", id);
 		return resolveSteamProfile(uri);
 	}
 
 	public SteamProfile getSteamProfile(String name)
 	{
 		String uri = String.format("http://www.steamcommunity.com/id/%s?xml=1", name);
 		return resolveSteamProfile(uri);
 	}
 	
 	public SteamProfile resolveSteamProfile(String uri)
 	{
 		return resolveSteamProfile(uri, DocumentBuilderFactory.newInstance());
 	}
 
 	public SteamProfile resolveSteamProfile(String uri, DocumentBuilderFactory dbf)	
 	{
 		SteamProfile profile = null;
 
 		Document result = getXmlDoc(uri, dbf);
 		
 		if (result != null)
 		{
 			Element root = result.getDocumentElement();
 			//should be 'profile'
 			//System.err.println("Profile root node name: " + root.getNodeName());
 			
			profile = mapper.mapXmlToProfile(root);
 			
 		}
 		return profile;
 	}
 
 	public SteamGame[] resolveSteamGames(String uri)
 	{
 		return resolveSteamGames(uri, DocumentBuilderFactory.newInstance());
 	}
 	
 	public SteamGame[] resolveSteamGames(String uri, DocumentBuilderFactory dbf)	
 	{
 		ArrayList<SteamGame> gameList = new ArrayList<SteamGame>();
 
 		Document result = getXmlDoc(uri, dbf);
 		
 		
 		if (result != null)
 		{
 			Element root = result.getDocumentElement();
 			//should be 'profile'
 			//System.err.println("Profile root node name: " + root.getNodeName());
			gameList.addAll(mapper.mapXmlToGamesList(root));
 			
 		}
 		return gameList.toArray(new SteamGame[gameList.size()]);
 	}
 
 	public SteamGame[] getSteamGamesById(long id)
 	{
 		String uri = String.format("http://www.steamcommunity.com/profiles/%s/games?xml=1", id);
 		return resolveSteamGames(uri);
 	}
 
 	public SteamGame[] getSteamGamesByName(String name)
 	{
 		String uri = String.format("http://www.steamcommunity.com/id/%s/games?xml=1", name);
 		return resolveSteamGames(uri);
 	}	
 	
 }
