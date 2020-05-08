 package writer;
 
 import game.Game;
 import game.agent.Agent;
 import game.agent.GroupAgent;
 import game.base.Base;
 import game.player.Bank;
 import game.player.IAPlayer;
 import game.player.Player;
 import game.player.RealPlayer;
 import game.tower.Tower;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.vecmath.Vector2f;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 public class XmlReader {
 
 	/**
 	 * Creates and initiates the game from the informations contained in a XML file. 
 	 * @param game	The instance of the Game
 	 * @param fileName	the name of the XML file
 	 * @throws JDOMException
 	 * @throws IOException
 	 * @see SAXBuilder
 	 * @see Element
 	 * @see #createBases(Game, Element)
 	 * @see #createAgents(Game, Element)
 	 * @see #createPlayers(Game, Element)
 	 * @see #createTowers(Game, Element)
 	 */
 	public static void createGame(Game game, String fileName) throws JDOMException, IOException {
 		
 		SAXBuilder sxb = new SAXBuilder();
 		
 		// Extraction of the root (normally named "game")
 		Document document = sxb.build(new File("files/"+fileName));
 		Element root = document.getRootElement();
 		
 		// Creation of the players from the Element "players"
 		Element playersElement = root.getChild("players");
 		createPlayers(game, playersElement);
 		
 		// Creation of the bases from the Element "bases"
 		Element basesElement = root.getChild("bases");
 		createBases(game, basesElement);
 		
 		// Creation of the towers from the Element "towers"
 		Element towersElement = root.getChild("towers");
 		createTowers(game, towersElement);
 		
 		// Creation of the agents from the Element "agents"
 		/*Element agentsElement = root.getChild("agents");
 		createAgents(game, agentsElement);*/
 
 	}
 	
 	/**
 	 * Creates and initiates the players from the XML Element playersElement
 	 * @param game	The instance of the Game
 	 * @param playersElement	The XML Element containing the players informations
 	 * @see Player
 	 * @see Element
 	 * @see #createBases(Game, Element)
 	 * @see #createAgents(Game, Element)
 	 * @see #createTowers(Game, Element)
 	 */
 	public static void createPlayers(Game game, Element playersElement) {
 		// We list all the players = all the children "player" of playersElement
 		List<Element> players = playersElement.getChildren("player");
 		Iterator<Element> i = players.iterator();
 		// For each playerElement we create a new Player (name, type, money)
 		while (i.hasNext()) {
 			Element playerElement = (Element)i.next();
 			
 			String name = playerElement.getAttributeValue("name");
 			String type = playerElement.getAttributeValue("type");
			float money = Float.parseFloat(playerElement.getAttributeValue("money"));
 			String color = playerElement.getAttributeValue("color");
 			Color c = new Color(Integer.parseInt(color));
 			
 			// RealPlayer or IAPlayer ?
 			Player player = (type.equals("RealPlayer")) ? 
 					new RealPlayer(name, new Bank(money), c) : new IAPlayer(name, new Bank(money), c); 
 			
 			// finally we add the Player
 			game.getPlayerManager().addPlayer(player);
 		}
 	}
 	
 	/**
 	 * Creates and initiates the bases from the XML Element basesElement
 	 * @param game	The instance of the Game
 	 * @param basesElement	The XML Element containing the bases informations
 	 * @see Base
 	 * @see Element
 	 * @see #createPlayers(Game, Element)
 	 * @see #createAgents(Game, Element)
 	 * @see #createTowers(Game, Element)
 	 */
 	public static void createBases(Game game, Element basesElement) {
 		List<Element> bases = basesElement.getChildren("base");
 		Iterator<Element> i = bases.iterator();
 		int index = 0;
 		while (i.hasNext()) {
 			Element baseElement = (Element)i.next();
 			
 			int id = Integer.parseInt(baseElement.getAttributeValue("id"));
 			float x = Float.parseFloat(baseElement.getAttributeValue("x"));
 			float y = Float.parseFloat(baseElement.getAttributeValue("y"));
 			String playerName = baseElement.getAttributeValue("player");
 			int might = Integer.parseInt(baseElement.getAttributeValue("might"));
 			int nbAgents = Integer.parseInt(baseElement.getAttributeValue("nbAgents"));
 			
 			Vector2f position = new Vector2f(x, y);
 			Player player = game.getPlayerManager().getPlayer(playerName);
 			
 			
 			//Base base = new Base(id, might, player, position, nbAgents);
 			Base base = new Base(might, player, position, nbAgents);
 			base.setId(index);	
 			++index;
 			game.getBaseManager().addBase(base);
 			
 		}
 	}
 	
 	/**
 	 * Creates and initiates the towers from the XML Element towersElement
 	 * @param game	The instance of the Game
 	 * @param towersElement	The XML Element containing the towers informations
 	 * @see Tower
 	 * @see Element
 	 * @see #createPlayers(Game, Element)
 	 * @see #createAgents(Game, Element)
 	 * @see #createBases(Game, Element)
 	 */
 	public static void createTowers(Game game, Element towersElement) {
 		List<Element> towers = towersElement.getChildren("tower");
 		Iterator<Element> i = towers.iterator();
 		while (i.hasNext()) {
 			Element towerElement = (Element)i.next();
 			
 			/*Tower tower = new Tower();
 			
 			game.getTowerManager().addTower(tower);*/
 		}
 	}
 	
 	/**
 	 * Creates and initiates the agents from the XML Element agentsElement
 	 * @param game	The instance of the Game
 	 * @param agentsElement	The XML Element containing the agents informations
 	 * @see Agent
 	 * @see Element
 	 * @see #createPlayers(Game, Element)
 	 * @see #createBases(Game, Element)
 	 * @see #createTowers(Game, Element)
 	 */
 	public static void createAgents(Game game, Element agentsElement) {
 		/*List<Element> agents = agentsElement.getChildren("agent");
 		Iterator<Element> i = agents.iterator();
 		while (i.hasNext()) {
 			Element agentElement = (Element)i.next();
 			
 			// ...
 			
 			Agent agent = new Agent();
 			
 			//At commit 9b0f500 It was : game.getAgentManager().addAgent(agent); I choose to cast, but ?
 			game.getAgentManager().addAgent((GroupAgent) agent);
 		}*/
 	}
 	
 	
 	
 	public static void main(String[] args) {
 		
 		Game game = Game.getInstance();
 		
 		try {
 			createGame(game, "game.xml");
 		} catch (JDOMException e) {
 			System.out.println("Error ! the game.xml is corrupted !");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error ! the game.xml file doesn't exist !");
 			e.printStackTrace();
 		}
 		
 		System.out.println(game);
 	}
 
 }
