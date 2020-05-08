 package fart.dungeoncrawler;
 
import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JPanel;
 
 import Utils.Vector2;
 
 import fart.dungeoncrawler.actor.*;
 import fart.dungeoncrawler.enums.*;
 import fart.dungeoncrawler.gamestates.*;
 import fart.dungeoncrawler.items.*;
 import fart.dungeoncrawler.network.Server;
 import fart.dungeoncrawler.network.messages.game.GamePositionMessage;
 
 @SuppressWarnings("serial")
 public class Game extends JPanel implements Runnable
 {
 	//Variables used for timing the GameLoop
 	private long frameDelta;
 	private long frameLast;
 	private final long frameTime = 32l;
 	private boolean isRunning = true;
 	
 	private NewPlayer player;
 	public NewPlayer getPlayer() { return player; }
 	
 	private Tilemap map;
 	public Tilemap getMap() { return map ; }
 	
 	private DynamicObjectManager manager;
 	public DynamicObjectManager getDynamicManager() { return manager; }
 	
 	private StaticObjectManager sManager;
 	public StaticObjectManager getStaticManager() { return sManager; }
 	
 	private Controller controller;
 	public Controller getController() { return controller; }
 	
 	private Collision collision;
 	public Collision getCollision() { return collision; }
 	
 	private Menu menu;
 	public Menu getMenu() { return menu; }
 	
 	private HashMap<GameState, BaseGameState> states;
 	private GameState currentState;
 	public GameState getState() { return currentState; }
 	private BaseGameState currentGameState;
 	public BaseGameState getGameState() { return currentGameState; }
 	
 	private CheckPoint checkPoint = null;
 	private String currentMapPath;
 	
 	private boolean isInNetwork;
 	private boolean isServer;
 	private byte playerID;
 	private int numPlayers;
 	private Vector2[] startPositions;
 	
 	//DEBUG
 	private static final Vector2 PLAYER_START_POS = new Vector2(1 * Tilemap.TILE_SIZE, 13 * Tilemap.TILE_SIZE);
 	public static boolean debugDraw = false;
 	private ArrayList<NewPlayer> otherPlayers;
 	private ArrayList<NewPlayer> allPlayers;
 	
 	public Game(byte ID, int numPlayers, boolean isServer)
 	{
 		super();
 		
 		this.playerID = ID;
 		this.numPlayers = numPlayers;
 		this.isServer = isServer;
 		if(!isServer) {
 			setFocusable(true);
 			requestFocusInWindow();
			//setVisible(false);
 		} else {
 			setVisible(false);
 		}
 		
 		initGame();
		//createPlayers(/*ID, numPlayers*/);
 	}
 	
 	public ArrayList<NewPlayer> getAllPlayers() {
 		if(allPlayers == null) {
 			allPlayers = new ArrayList<NewPlayer>();
 			if(player != null)
 				allPlayers.add(player);
 			for(NewPlayer p : otherPlayers)
 				allPlayers.add(p);
 		}
 		
 		return allPlayers;
 	}
 	
 	public boolean isServer() {
 		return isServer;
 	}
 	
 	public void setInNetwork(boolean isInNetwork) {
 		this.isInNetwork = isInNetwork;
 		
 		boolean updateLogic = true;
 		if(isInNetwork && !isServer)
 			updateLogic = false;
 		manager.setUpdateLogic(updateLogic);
 	}
 	
 	public boolean isInNetwork() {
 		return isInNetwork;
 	}
 	
 	public void initGame()
 	{
 		frameLast = System.currentTimeMillis();
 		
 		if(!isServer) {
 			controller = new Controller();
 			this.addKeyListener(controller);
 			menu = new Menu(this, controller);
 		}
 		
 		manager = new DynamicObjectManager(this, true);
 		sManager = new StaticObjectManager();
 		collision = new Collision();
 		map = new Tilemap(this, sManager, manager, collision);
 		otherPlayers = new ArrayList<NewPlayer>();
 		
 		states = new HashMap<GameState, BaseGameState>();
 		states.put(GameState.InMenu, new GameStateInMenu(this));
 		states.put(GameState.InGame, new GameStateInGame(this));
 		states.put(GameState.InShop, new GameStateInShop(this));
 		states.put(GameState.InInventory, new GameStateInInventory(this));
 		states.put(GameState.InConversation, new GameStateInConversation(this));
 		states.put(GameState.InLobby, new GameStateInLobby(this));
 		states.put(GameState.InNetworkGame, new GameStateInGame(this));
 		states.put(GameState.InStatsMenu, new GameStateInStatsMenu(this));
 		states.put(GameState.InQuestLog, new GameStateInQuestLog(this));
 		
 		setGameState(GameState.InMenu);
 		((GameStateInShop)states.get(GameState.InShop)).setCurrentShop(new Shop(controller));
 
 		ItemCollection.createNewInstace();
 		QuestCollection.getInstance();
 		
 		startPositions = new Vector2[4];
 		startPositions[0] = new Vector2(27 * 32, 16 * 32);
 		//startPositions[1] = new Vector2(27 * 32, 4 * 32);
 		startPositions[1] = new Vector2(27 * 32, 13 * 32);
 		startPositions[2] = new Vector2(4 * 32, 4 * 32);
 		startPositions[3] = new Vector2(4 * 32, 16 * 32);
 	}
 	
 	public void createPlayers() {
 		allPlayers = new ArrayList<NewPlayer>();
 		
 		for(int i = 0; i < numPlayers; i++) {
 			ActorDescription actDesc = new ActorDescription("res/player.png", 1, 0, new Stats(12, 8, 7, 8, 0, 8, 0), Heading.Up);
 			Vector2 plPos = new Vector2(PLAYER_START_POS.x + i * 32 + i, PLAYER_START_POS.y);
 			if(isInNetwork)
 				plPos = new Vector2(startPositions[i]);
 			
 			if(!isServer && playerID == i) {
 				if(isInNetwork)
 					player = new NewPlayer(this, actDesc, plPos, true, playerID);
 				else
 					player = new NewPlayer(this, actDesc, plPos, true);
 				allPlayers.add(player);
 				continue;
 			}
 			
 			NewPlayer p = new NewPlayer(this, actDesc, plPos, false, i);
 			allPlayers.add(p);
 			otherPlayers.add(p);
 		}
 	}
 	
 	public void createPlayers(byte ID, int numPlayers) {
 		playerID = ID;
 		this.numPlayers = numPlayers;
 		
 		createPlayers();
 	}
 	
 	public void setGameState(GameState state) {
 		currentState = state;
 		if(currentGameState != null)
 			currentGameState.exit();
 		
 		currentGameState = states.get(state);
 		currentGameState.activate();
 	}
 	
 	public void setShopActor(Actor act) {
 		((GameStateInShop)states.get(GameState.InShop)).setCurrentActor(act);
 	}
 	
 	public void startGame(boolean newGame) {
 		startGame(newGame, "res/maps/L0R0.xml");
 	}
 	
 	public void startGame(boolean newGame, String mapPath) {
 		manager.clearObjects();
 		sManager.clearObjects();
 		collision.clearDynamicObjects();
 		collision.clearTriggers();
 		
		//createPlayers(/*playerID, numPlayers*/);
 		if(!isServer) {
 			player.setInNetwork(isInNetwork);
 			resetPlayer();
 		}
 		
 		map.loadMap(mapPath);
 		currentMapPath = mapPath;
 		
 		if(player != null) {
 			collision.addDynamicObject(player);
 			manager.addObject(player);
 		}
 		
 		for(int i = 0; i < otherPlayers.size(); i++) {
 			otherPlayers.get(i).setInNetwork(isInNetwork);
 			collision.addDynamicObject(otherPlayers.get(i));
 			manager.addObject(otherPlayers.get(i));
 		}
		
		ArrayList<Integer> qs = new ArrayList<Integer>();
		qs.add(0);
		qs.add(1);
		NPCQuest qg = new NPCQuest(this, 
				new Vector2(200, 200), 
				new NPCDescription("res/shop.png", 
						NPCType.Quest.ordinal(), 
						new ActorDescription(new Dimension(32, 32), 1, 0, new Stats(), Heading.Down)),
						new Rectangle(190, 190, 52, 52), qs);
		
		//manager.addObject(qg);
		collision.addTriggerOnKey(qg);
 	}
 	
 	public void playerDead() {
 		if(checkPoint != null) {
 			if(checkPoint.load()) {
 				saveCheckPoint(checkPoint);
 				currentMapPath = checkPoint.getMapName();
 				collision.addDynamicObject(player);
 			}
 			else {
 				setGameState(GameState.InMenu);
 				menu.setGameStarted(false);
 				checkPoint = null;
 			}
 			
 			return;
 		}
 		menu.setGameStarted(false);
 		setGameState(GameState.InMenu);
 		resetPlayer();
 	}
 	
 	public void playerDeadInNetwork(int actorID) {
 		if(isServer) 
 			System.out.println("**SERVER: playerDeadInNetwork " + actorID);
 		else
 			System.out.println("**CLIENT: playerDeadInNetwork " + actorID);
 		NewPlayer a = allPlayers.get(actorID);
 		a.getHealth().fillHealth();
 		a.getMana().fillMana();
 		a.setScreenPosition(new Vector2(startPositions[actorID]));
 		while(collision.isCollidingDynamic(a))
 			a.setScreenPosition(new Vector2(a.getPosition().x + 1, a.getPosition().y));
 		a.setState(DynamicObjectState.Idle);
 		
 		if(isServer) {
 			Server.getInstance().broadcastMessage(new GamePositionMessage(a));
 		}
 	}
 	
 	private void resetPlayer() {
 		/*startPositions[0] = new Vector2(27 * 32, 16 * 32);
 		startPositions[1] = new Vector2(27 * 32, 4 * 32);
 		startPositions[2] = new Vector2(4 * 32, 4 * 32);
 		startPositions[3] = new Vector2(4 * 32, 16 * 32);*/
 		
 		Vector2 plPos = new Vector2(PLAYER_START_POS.x + playerID * Tilemap.TILE_SIZE + playerID, PLAYER_START_POS.y);
 		if(isInNetwork)
 			plPos = new Vector2(startPositions[playerID]);
 		
 		player.setScreenPosition(plPos);
 		player.getHealth().fillHealth();
 		player.getMana().fillMana();
 		player.setState(DynamicObjectState.Idle);
 		player.setHeading(Heading.Up);
 		player.setVelocity(Vector2.Zero);
 	}
 	
 	public void saveCheckPoint(CheckPoint cp) {
 		checkPoint = cp;
 		cp.save(map);
 	}
 	
 	public void playerWins() {
 		setGameState(GameState.InMenu);
 		menu.setGameStarted(false);
 	}
 	
 	public void changeMap(String mapTo, Vector2 position) {
 		map.loadMap(mapTo);
 		player.setScreenPosition(new Vector2(position));
 		manager.addObject(player);
 		collision.addDynamicObject(player);
 		currentMapPath = mapTo;
 	}
 	
 	public String getMapPath() {
 		return currentMapPath;
 	}
 	
 	public String getMapName() {
 		return map.getName();
 	}
 	
 	public void startGameLoop() {
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 	
 	@Override
 	public void run()
 	{
 		long frameAct;
 		Long frameSleep;
 		
 		while(isRunning) {
 			frameAct = System.currentTimeMillis();
 			frameDelta = frameAct - frameLast;
 			frameSleep = (frameTime - frameDelta);
 			if(frameSleep < 2)
 				frameSleep = 2l;
 
 			frameLast = frameAct;
 			
 			if(!isServer)
 				controller.update();
 			currentGameState.update(frameTime);
 			
 			repaint();
 			
 			try {
 				Thread.sleep(frameSleep);
 			} catch (IllegalArgumentException e) {
 				System.err.print("Exception in GameLoop:\n");
 				e.printStackTrace();
 				System.exit(1);
 			} catch (InterruptedException e) {
 				System.err.print("Exception in GameLoop:\n");
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		if(!isServer) {
 			super.paintComponent(g);
 			
 			Graphics2D g2d = (Graphics2D)g;
 			currentGameState.draw(g2d);
 		}
 	}
 }
