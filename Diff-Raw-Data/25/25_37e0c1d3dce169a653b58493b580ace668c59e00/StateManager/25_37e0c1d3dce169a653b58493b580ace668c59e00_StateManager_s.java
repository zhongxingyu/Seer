 package game;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import game.gameplayStates.DolphinChamber;
 import game.gameplayStates.DolphinEntrance;
 import game.gameplayStates.GamePlayState;
 import game.gameplayStates.Home;
 import game.gameplayStates.HospitalBase;
 import game.gameplayStates.HospitalEntrance;
 import game.gameplayStates.HospitalMaze;
 import game.gameplayStates.Kitchen;
 import game.gameplayStates.Room;
 import game.gameplayStates.Shop;
 import game.gameplayStates.TownDay;
 import game.gameplayStates.TownNight;
 import game.gameplayStates.VirtualRealityHome;
 import game.gameplayStates.VirtualRealityRoom;
 import game.io.GameObjectLoader;
 import game.io.LoadGame;
 import game.player.Player;
 import game.quests.Quest;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class StateManager extends StateBasedGame {
 	
 	private static final StateManager instance;
 	//Used to store all game objects
 	private static HashMap<Integer, GameObject> gameObjects;
 	private static HashMap<Integer, Quest> quests;
 	private static boolean isLoading;
 	  
 	static {
 	    instance = new StateManager();
 	    gameObjects = new HashMap<Integer, GameObject>();
 	    quests = new HashMap<Integer, Quest>();
 	}
 	 
 	public static StateManager getInstance() {
 		return instance;
 	}
 	
 	private static int m_currentKey = 0;
 	
 	/**
 	 * This returns a key that is guaranteed unique among all game
 	 * objects.
 	 * @return int
 	 */
 	public static int getKey() {
 		m_currentKey +=1;
 		return m_currentKey;
 	}
 	
 	/**
 	 * Adds an object to the global object store.
 	 * @param key
 	 * @param o
 	 */
 	public static void addObject(int key, GameObject o) {
 		if(!isLoading)
 			gameObjects.put(key, o);
 	}
 	
 	
 	/**
 	 * Gets an object.
 	 * @param key
 	 * @return GameObject, or null
 	 */
 	public static GameObject getObject(int key) {
 		return gameObjects.get(key);
 	}
 	
 	/**
 	 * Adds a quest to the global object store.
 	 * @param key
 	 * @param q
 	 */
 	public static void addQuest(int key, Quest q) {
 		quests.put(key, q);
 	}
 	
 	public static Quest getQuest(int key) {
 		return quests.get(key);
 	}
 	  
 	// black box test states
 
 	public static boolean m_debugMode = false;
 	public static final int KITCHEN_STATE = 1002;
 	public static final int ROOM_STATE = 1001;
 	
 	// real states
 	public static final int GAME_OVER_STATE = 99;
 	public static final int SHOP_STATE = 11;
 	public static final int HOSPITAL_ENTRANCE_STATE = 10;
 	public static final int HOSPITAL_BASE_STATE = 9;
 	public static final int HOSPITAL_MAZE_STATE = 8;
 	public static final int VIRTUAL_REALITY_HOME_STATE = 7;
 	public static final int VIRTUAL_REALITY_ROOM_STATE = 6;
 	public static final int DOLPHIN_ENTRANCE = 5;
 	public static final int DOLPHIN_STATE = 4;
 	public static final int TOWN_NIGHT_STATE = 3;
 	public static final int TOWN_DAY_STATE = 2;
 	public static final int HOME_STATE = 1;
 	public static final int MAINMENU_STATE = 0;
 	
 	// this represents the city degradation. 4 beings the least degraded and 0 being the most
 
 	public static int m_cityState = 4;
 	// this represents the number of dreams you have left
	public static int m_dreamState = 3;
 
 	
 	private AppGameContainer m_app;
 	private LoadGame m_loader;
 	private Player m_player;
 	
 	private StateManager() {
 		super("chicken salad and cucumber on croissant");
 		
 		try {
 			m_app = new AppGameContainer(this);
 			m_app.setDisplayMode(600, 600, false);
 			//app.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private StateManager(LoadGame l) {
 		super("chicken salad and cucumber on croissant");
 		this.m_loader = l;
 		try {
 			m_app = new AppGameContainer(this);
 			m_app.setDisplayMode(600, 600, false);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void setLoader(LoadGame l) {
 		this.m_loader = l;
 	}
 	
 	public GameContainer getGameContainer() {
 		return this.m_app;
 	}
 	
 	public void setup() throws SlickException {
 		this.m_app.setShowFPS(false);
 		// Test states
 		if (m_debugMode) {
 			Room room = new Room(ROOM_STATE);
 			addState(room);
 			Kitchen kitchen = new Kitchen(KITCHEN_STATE);
 			addState(kitchen);
 			// give player to gameplaystates
 			m_player = new Player(room, getGameContainer(), 0, 0);
 			room.setPlayer(m_player);
 			kitchen.setPlayer(m_player);
 		}
 		else {
 			Home home = new Home(HOME_STATE);
 			addState(home);
 			TownDay townDay = new TownDay(TOWN_DAY_STATE);
 			addState(townDay);
 			TownNight townNight = new TownNight(TOWN_NIGHT_STATE);
 			addState(townNight);
 			DolphinChamber dolphinChamber = new DolphinChamber(DOLPHIN_STATE);
 			addState(dolphinChamber);
 			DolphinEntrance dolphinEntrance = new DolphinEntrance(DOLPHIN_ENTRANCE);
 			addState(dolphinEntrance);
 			VirtualRealityRoom virtualRealityRoom = new VirtualRealityRoom(VIRTUAL_REALITY_ROOM_STATE);
 			addState(virtualRealityRoom);
 			VirtualRealityHome virtualRealityHome = new VirtualRealityHome(VIRTUAL_REALITY_HOME_STATE);
 			addState(virtualRealityHome);
 			HospitalMaze hospitalMaze = new HospitalMaze(HOSPITAL_MAZE_STATE);
 			addState(hospitalMaze);
 			HospitalBase hospitalBase = new HospitalBase(HOSPITAL_BASE_STATE);
 			addState(hospitalBase);
 			HospitalEntrance hospitalEntrance = new HospitalEntrance(HOSPITAL_ENTRANCE_STATE);
 			addState(hospitalEntrance);
 			Shop shop = new Shop(SHOP_STATE);
 			addState(shop);
 			
 			m_player = new Player(home, getGameContainer(), 0, 0);
 			home.setPlayer(m_player);
 			townDay.setPlayer(m_player);
 			townNight.setPlayer(m_player);
 			dolphinChamber.setPlayer(m_player);
 			dolphinEntrance.setPlayer(m_player);
 			virtualRealityRoom.setPlayer(m_player);
 			virtualRealityHome.setPlayer(m_player);
 			hospitalMaze.setPlayer(m_player);
 			hospitalBase.setPlayer(m_player);
 			hospitalEntrance.setPlayer(m_player);
 			shop.setPlayer(m_player);
 		}
 		GameOver go = new GameOver(GAME_OVER_STATE);
 		addState(go);
 		// start!
 		addState(new MainMenu(MAINMENU_STATE));
 		enterState(MAINMENU_STATE);
 	}
 	
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		setup();
 		
 		// setup states
 		getState(MAINMENU_STATE).init(container, this);
 		getState(GAME_OVER_STATE).init(container, this);
 		if (m_debugMode) {
 			getState(KITCHEN_STATE).init(container, this);
 			getState(ROOM_STATE).init(container, this);
 			
 		}
 		else {
 			getState(TOWN_DAY_STATE).init(container, this);
 			getState(TOWN_NIGHT_STATE).init(container, this);
 			getState(HOME_STATE).init(container, this);
 			getState(DOLPHIN_STATE).init(container, this);
 			getState(DOLPHIN_ENTRANCE).init(container, this);
 			getState(VIRTUAL_REALITY_ROOM_STATE).init(container, this);
 			getState(VIRTUAL_REALITY_HOME_STATE).init(container, this);
 			getState(HOSPITAL_MAZE_STATE).init(container, this);
 			getState(HOSPITAL_BASE_STATE).init(container, this);
 			getState(HOSPITAL_ENTRANCE_STATE).init(container, this);
 			getState(SHOP_STATE).init(container, this);
 		}
 		
 		if(this.m_loader==null) {
 			
 		} else {
 			try {
 				m_loader.load(this);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	//I suspect that more needs to be done here, but I'm not sure what
 	public void reset() throws SlickException{
 		m_cityState = 4;
 		m_dreamState = 4;
 		//System.out.println("GAME OVER STATE CALLED RESET ENTERED");
 		
 		//not totally certain abt this stuff
 		if(m_debugMode){
 			Room room = new Room(ROOM_STATE);
 			addState(room);
 			Kitchen kitchen = new Kitchen(KITCHEN_STATE);
 			addState(kitchen);
 			//game over state added
 			// give player to gameplaystates
 			m_player = new Player(room, getGameContainer(), 0, 0);
 			room.setPlayer(m_player);
 			kitchen.setPlayer(m_player);
 		}else{
 			Home home = new Home(HOME_STATE);
 			addState(home);
 			TownDay townDay = new TownDay(TOWN_DAY_STATE);
 			addState(townDay);
 			TownNight townNight = new TownNight(TOWN_NIGHT_STATE);
 			addState(townNight);
 			DolphinChamber dolphinChamber = new DolphinChamber(DOLPHIN_STATE);
 			addState(dolphinChamber);
 			DolphinEntrance dolphinEntrance = new DolphinEntrance(DOLPHIN_ENTRANCE);
 			addState(dolphinEntrance);
 			VirtualRealityRoom virtualRealityRoom = new VirtualRealityRoom(VIRTUAL_REALITY_ROOM_STATE);
 			addState(virtualRealityRoom);
 			VirtualRealityHome virtualRealityHome = new VirtualRealityHome(VIRTUAL_REALITY_HOME_STATE);
 			HospitalMaze hospitalMaze = new HospitalMaze(HOSPITAL_MAZE_STATE);
 			addState(hospitalMaze);
 			HospitalBase hospitalBase = new HospitalBase(HOSPITAL_BASE_STATE);
 			addState(hospitalBase);
 			HospitalEntrance hospitalEntrance = new HospitalEntrance(HOSPITAL_ENTRANCE_STATE);
 			addState(hospitalEntrance);
 			Shop shop = new Shop(SHOP_STATE);
 			addState(shop);
 			
 			m_player = new Player(home, getGameContainer(), 0, 0);
 			home.setPlayer(m_player);
 			townDay.setPlayer(m_player);
 			townNight.setPlayer(m_player);
 			dolphinChamber.setPlayer(m_player);
 			dolphinEntrance.setPlayer(m_player);
 			virtualRealityRoom.setPlayer(m_player);
 			virtualRealityHome.setPlayer(m_player);
 			hospitalMaze.setPlayer(m_player);
 			hospitalBase.setPlayer(m_player);
 			hospitalEntrance.setPlayer(m_player);
 			shop.setPlayer(m_player);
 		}
 		
 		GameOver go = new GameOver(GAME_OVER_STATE);
 		addState(go);
 		addState(new MainMenu(MAINMENU_STATE));
 		initStatesList(getGameContainer());
 		enterState(MAINMENU_STATE);
 	}
 	public void run() {
 		try {
 			m_app.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Writes GameObjects to the writer.
 	 * @param writer
 	 * @throws XMLStreamException
 	 */
 	public static void writeToXML(XMLStreamWriter writer) throws XMLStreamException {
 		writer.writeStartElement("GameObjects");
 		for(Entry<Integer, GameObject> e : gameObjects.entrySet()) {
 			if(e.getValue() instanceof Player)
 				continue;
 			e.getValue().writeToXML(writer);
 			writer.writeCharacters("\n");
 		}
 		writer.writeEndElement();
 	}
 	
 	/**
 	 * Loads all the GameObjects.
 	 * @param node
 	 * @throws SlickException 
 	 */
 	public static void loadObjects(Node node) throws SlickException {
 		gameObjects = new HashMap<Integer, GameObject>();
 		NodeList children = node.getChildNodes();
 		isLoading = true;
 		for(int i = 0; i<children.getLength(); i++) {
 			Node c = children.item(i);
 			if(c.getNodeName().equals("GameObject")) {
 				GameObject o = GameObjectLoader.loadFromNode(c);
 				gameObjects.put(o.getKey(), o);
 			}
 		}
 	}
 
 }
