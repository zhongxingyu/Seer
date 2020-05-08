 package game.gameplayStates;
 
 import game.Dialogue;
 import game.Enemy;
 import game.GameObject;
 import game.Loadable;
 import game.PauseMenu;
 import game.Scene;
 import game.StateManager;
 import game.GameObject.Types;
 import game.cameras.Camera;
 import game.cameras.PlayerCamera;
 import game.interactables.Interactable;
 import game.interactables.Interactables;
 import game.player.Player;
 import game.quests.Quest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.particles.ParticleEmitter;
 import org.newdawn.slick.particles.ParticleSystem;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.util.pathfinding.PathFindingContext;
 import org.newdawn.slick.util.pathfinding.TileBasedMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * All game states that are in gameplay-mode need to inherit from this class
  * -- pausemenu
  * -- text/dialouge 
  */
 public abstract class GamePlayState extends BasicGameState implements Loadable<GamePlayState> {
 
 	protected int m_subState = 0;
 	protected Camera m_camera;
 	protected boolean m_isPaused = false;
 	protected boolean m_inDialogue = false;
 	protected PauseMenu m_pauseMenu = null;
 	protected boolean m_disableTopLayer = false;
 
 	int m_stateID = 0;
 	protected int inputDelta = 0;
 	public enum Direction {UP, DOWN, LEFT, RIGHT}
 	protected TiledMap m_tiledMap;
 	protected String m_mapPath; //path to the tiled map file
 	protected Player m_player;
 	protected int m_playerX, m_playerY;
 	protected ArrayList<Enemy> m_enemies;
 
 	protected boolean[][] m_blocked; // 2D array indicating spaces that are blocked
 	protected static final int SIZE = 64; // block size
 	//protected Rectangle m_viewport;
 
 	protected simpleMap m_map;
 
 
 	// hack - variables for hospitalBase
 	protected Image m_flash;
 	protected boolean m_isFlash = false;
 
 	@Deprecated
 	protected HashMap<Integer, Dialogue> m_dialogue;
 	@Deprecated
 	protected int m_dialogueNum; // represents which dialogue to use
 
 	private boolean m_loaded; //true if the state has already been loaded from file.
 	public boolean isLoaded() { return m_loaded; }
 
 	private boolean m_isActive; //true if the state is the active state
 	public boolean isActive() { return m_isActive; }
 
 	private boolean[][] m_isInitialized; //values are indexed by cityState, dreamState. True if the state
 	//has been visited for that city/dream state combo.
 
 
 
 	private boolean m_entered; //true if the state has been entered.
 	/**
 	 * Returns true if the state has been entered.
 	 * @return
 	 */
 	public boolean isEntered() { return m_entered; }
 
 
 	/**
 	 *  key is represented by 'xPos' + 'yPos'
 	 *  example: if object has position (2,3), key = 23.
 	 */
 	private HashMap<String, GameObject> m_objects;
 	private HashMap<String, Interactable> m_interactables;
 	//true if the state is in a scene.
 	private boolean m_inScene; 
 	private Dialogue m_sceneDialogue;
 	private boolean m_invisiblePlayer; //true if the player is invisible.
 	public void setInvisiblePlayer(boolean b) { m_invisiblePlayer = b; }
 
 	protected Music m_bgm; //background music
 
 	/**
 	 * mini mode
 	 */
 	private boolean m_mini;
 	/**
 	 * Sets the state's mini mode.
 	 * @param b
 	 */
 	public void setMini(boolean b) { m_mini = b; }
 
 
 	public void setPauseState(boolean state) { m_isPaused = state; }
 	public boolean getPauseState() { return m_isPaused; }
 
 	public void setDialogueState(boolean state) { m_inDialogue = state; }
 	public boolean getDialogueState() { return m_inDialogue; }
 
 	/**
 	 * This is used by subclasses to do level-specific initialization.
 	 * @param container
 	 * @param stateManager
 	 * @throws SlickException 
 	 */
 	public void additionalInit(GameContainer container, StateBasedGame stateManager) throws SlickException {}
 
 	/**
 	 * This is used by subclasses to add updating functionality.
 	 * @param container
 	 * @param stateManager
 	 * @param delta
 	 */
 	public void additionalUpdate(GameContainer container, StateBasedGame stateManager, int delta) {}
 
 	/**
 	 * This is used by subclasses to add rendering functionality.
 	 * @param container
 	 * @param stateManager
 	 * @param g
 	 */
 	public void additionalRender(GameContainer container, StateBasedGame stateManager, Graphics g) {}
 
 	/**
 	 * this is used by subclasses to add on enter functionality
 	 */
 	public void additionalEnter(GameContainer container, StateBasedGame stateManager) {}
 
 	/**
 	 * sets up the objects in this state based on the state of the game
 	 * called on enter state
 	 * NOTE: objects that are in all states of this gameplaystate are created in the additionalInit method
 	 * possible city/dream combos are: 3-3, 3-2, 2-2, 2-1, 1-1, 1-0, 0-0
 	 * @param city - State of the city's degradation
 	 * @param dream - state of the dreams (how many are left)
 	 */
 	public abstract void setupObjects(int city, int dream) throws SlickException;
 
 	/**
 	 * This sets the state's BGM from a path.
 	 * @param path - String
 	 */
 	public void setMusic(String path) {
 		try {
 			m_bgm = new Music(path);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Resets the state's objects.
 	 */
 	public void clearObjects() {
 		m_objects = new HashMap<String, GameObject>();
 		m_interactables = new HashMap<String, Interactable>();
 	}
 
 	/**
 	 * Adds a GameObject to the state. If the object is an interactable,
 	 * it is added to the list of interactables as well.
 	 * @param o
 	 */
 	public void addObject(GameObject o, boolean isInteractable) {
 		m_objects.put(o.getName(), o);
 		if(isInteractable) {
 			Interactable i = (Interactable) o;
 			m_interactables.put(i.getName(), i);
 		}
 	}
 
 	/**
 	 * Gets a GameObject in the state.
 	 * @param key
 	 * @return GameObject, or null
 	 */
 	public GameObject getObject(String key) {
 		return m_objects.get(key);
 	}
 
 	/**
 	 * Gets an interactable in the state.
 	 * @param key
 	 * @return Interactable, or null
 	 */
 	public Interactable getInteractable(String key) {
 		return m_interactables.get(key);
 	}
 
 	/**
 	 * Removes an object from the state. Checks both
 	 * objects and interactables.
 	 * @param key
 	 */
 	public void removeObject(String key) {
 		if (m_objects.containsKey(key))
 			m_objects.remove(key);
 		if (m_interactables.containsKey(key))
 			m_interactables.remove(key);
 	}
 
 	/**
 	 * Removes an enemy from the state, given its name.
 	 * @param name
 	 */
 	public void removeEnemy(String name) {
 		for(int i = 0; i<m_enemies.size(); i++) {
 			Enemy e = m_enemies.get(i);
 			if(e.getName().equals(name)) {
 				m_enemies.remove(i);
 				break;
 			}
 		}
 	}
 	/**
 	 * sets up the dialogue in this state based on the state of the game
 	 * called on enter state
 	 * possible city/dream combos are: 4-4, 4-3, 3-3, 3-2, 2-2, 2-1, 1-1, 1-0, 0-0
 	 * NOTE: dialogue that are in all states of this gameplaystate are created in the additionalInit method
 	 */
 	public abstract void setupDialogue(GameContainer container, int city, int dream) throws SlickException;
 
 	@Override
 	public final void enter(GameContainer container, StateBasedGame stateManager) throws SlickException {
 		m_player.setX(m_playerX);
 		m_player.setY(m_playerY);
 		m_player.setGame(this);	
 		if(m_isInitialized[StateManager.m_cityState][StateManager.m_dreamState]==false) {
 			setupObjects(StateManager.m_cityState, StateManager.m_dreamState);
 			setupDialogue(container, StateManager.m_cityState, StateManager.m_dreamState);
 			m_isInitialized[StateManager.m_cityState][StateManager.m_dreamState] = true;
 		}
 		if(m_bgm!=null) {
 			m_bgm.loop();
 		}
 		additionalEnter(container, stateManager);
 		m_entered = true;
 	}
 
 	@Override
 	public final void leave(GameContainer container, StateBasedGame stateManager) throws SlickException {
 		if(m_bgm!=null)
 			m_bgm.stop();
 		this.additionalLeave(container, stateManager);
 	}
 
 	/**
 	 * Used for defining additional behavior upon leaving state.
 	 * @param container
 	 * @param stateManager
 	 */
 	public void additionalLeave(GameContainer container, StateBasedGame stateManager) {}
 
 	@Override
 	public final void init(GameContainer container, StateBasedGame stateManager) throws SlickException {
 		// setup menu
 		if(this.isLoaded()==false) {
			m_isInitialized = new boolean[5][5];
 			m_camera = new PlayerCamera(container, m_player);
 			m_pauseMenu = new PauseMenu(this, container);
 			m_interactables = new HashMap<String, Interactable>();
 			m_objects = new HashMap<String, GameObject>();
 			m_dialogue = new HashMap<Integer, Dialogue>();
 			m_enemies = new ArrayList<Enemy>();
 
 			this.additionalInit(container, stateManager);
 			this.m_loaded = true;
 		}
 	}
 
 	@Override
 	public final void update(GameContainer container, StateBasedGame stateManager, int delta) throws SlickException {
 
 		if (m_isPaused && m_pauseMenu!=null)
 			m_pauseMenu.update(container, stateManager, delta);
 
 		//check for game over state
 		if(m_player.getHealth().getVal()<=0){
 			stateManager.enterState(StateManager.GAME_OVER_STATE);
 		}
 
 		if (!m_isPaused && !m_inDialogue){
 			m_player.update(container, delta);
 			if (m_enemies != null) {
 				int enemiesCount = m_enemies.size();
 				for(Enemy e : m_enemies) {
 					e.update(delta);
 					if(m_enemies.size()!= enemiesCount) {
 						break;
 					}
 				}
 			}
 		}
 
 		if (m_inDialogue) {
 			//if(m_inScene)
 			m_sceneDialogue.update(container, stateManager, delta);
 			//else
 			//	m_dialogue.get(m_dialogueNum).update(container, stateManager, delta);
 		}
 
 		Input input = container.getInput();
 		inputDelta-=delta;
 
 		// creates menu screen
 		if (inputDelta<0 && input.isKeyDown(Input.KEY_ESCAPE)) {
 			if (!m_inDialogue) { // must not be in dialogue
 				if (!m_isPaused)
 					m_isPaused = true;
 				else
 					m_isPaused = false;
 			}
 			if(m_mini)
 				m_isPaused = false;
 			inputDelta = 500;
 		}
 
 		if(this.m_inDialogue==false)
 			this.m_camera.update(delta);
 
 		this.additionalUpdate(container, stateManager, delta);
 
 		// for testing purposes only
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_EQUALS)) {
 			if (StateManager.m_cityState < 4)
 				StateManager.m_cityState++;
 			inputDelta = 200;
 			System.out.println("cityState: " + StateManager.m_cityState + 
 					" | dreamState: " + StateManager.m_dreamState);
 		}
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_MINUS)) {
 			if (StateManager.m_cityState > 0)
 				StateManager.m_cityState--;
 			inputDelta = 200;
 			System.out.println("cityState: " + StateManager.m_cityState + 
 					" | dreamState: " + StateManager.m_dreamState);	
 		}
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_0)) {
 			if (StateManager.m_dreamState < 4)
 				StateManager.m_dreamState++;
 			inputDelta = 200;
 			System.out.println("cityState: " + StateManager.m_cityState + 
 					" | dreamState: " + StateManager.m_dreamState);
 		}
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_9)) {
 			if (StateManager.m_dreamState > 0)
 				StateManager.m_dreamState--;
 			inputDelta = 200;
 			System.out.println("cityState: " + StateManager.m_cityState + 
 					" | dreamState: " + StateManager.m_dreamState);
 		}
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_8)) {
 			StateManager.getInstance().enterState(StateManager.DOLPHIN_ENTRANCE);
 		}
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_7)) {
 			StateManager.getInstance().enterState(StateManager.HOSPITAL_ENTRANCE_STATE);
 		}
 		if (inputDelta<0 && input.isKeyPressed(Input.KEY_6)) {
 			StateManager.getInstance().enterState(StateManager.HOSPITAL_BASE_STATE);
 		}
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame stateManager, Graphics g) 
 			throws SlickException {
 		int[] offsets = m_camera.getOffset();
 		int offsetX = offsets[0];
 		int offsetY = offsets[1];
 		int[] playerOffsets = m_camera.getPlayerOffset();
 		// render background - used in hospital base
 		if (m_isFlash)
 			m_flash.draw(0,0);
 		// render map
 		m_tiledMap.render(-offsetX, -offsetY);
 		// render objects before player 
 		ArrayList<GameObject> objectsToRenderAfter = new ArrayList<GameObject>();
 		for (Entry<String, GameObject> e : m_objects.entrySet()) {
 			GameObject o = e.getValue();
 			if(o instanceof Enemy)
 				continue;
 			if(o.renderAfter())
 				objectsToRenderAfter.add(o);
 			else
 				o.getImage().draw(o.getX()-offsetX, o.getY()-offsetY);
 
 		}
 		// render player
 
 		if(m_invisiblePlayer == false)
 			m_player.getAnimation().draw(playerOffsets[0], playerOffsets[1]);
 		// render enemies
 		if (m_enemies != null)
 			for(Enemy m_enemy : m_enemies)
 				m_enemy.getAnimation().draw(m_enemy.getX()-offsetX, m_enemy.getY()-offsetY);
 
 		// render item usage
 		if (m_player.m_usingItem)
 			m_player.renderItem(container, stateManager, g);
 
 		// render objects after player
 		for (GameObject o : objectsToRenderAfter)
 			o.getImage().draw(o.getX()-offsetX, o.getY()-offsetY);
 
 
 		this.additionalRender(container, stateManager, g);
 
 		if (m_inDialogue) {
 			m_sceneDialogue.render(g);
 		}
 
 		if (!m_disableTopLayer) {
 			// render health
 			if(!m_mini)
 				m_player.getHealth().render();
 
 			// render inventory
 			if (m_player.m_inInventory) { m_player.getInventory().render(g); }
 		}
 
 
 
 		if (m_isPaused && m_pauseMenu!=null)
 			m_pauseMenu.render(g);
 	}
 
 	/**
 	 * This is called in order to end the game state.
 	 * @param endCode
 	 */
 	public void stateEnd(int endCode) {
 
 	}
 
 	/**
 	 * Sets the blocked tiles, using the tiledMap, assuming m_tiledMap is already
 	 * initialized.
 	 * TODO: set blocked tiles with gameObjects as well.
 	 */
 	protected void setBlockedTiles() {
 		m_blocked = new boolean[m_tiledMap.getWidth()][m_tiledMap.getHeight()];
 		for (int xAxis=0; xAxis<m_tiledMap.getWidth(); xAxis++) {
 			for (int yAxis=0; yAxis<m_tiledMap.getHeight(); yAxis++) {
 				int tileID = m_tiledMap.getTileId(xAxis, yAxis, 0);
 				String value = m_tiledMap.getTileProperty(tileID, "blocked", "false");
 				if ("true".equals(value)) {
 					m_blocked[xAxis][yAxis] = true;
 				}
 			}
 		}
 		for(Entry<String, GameObject> e : this.m_objects.entrySet()) {
 			if(e.getValue().isBlocking()) {
 				int[] position = e.getValue().getSquare();
 				m_blocked[position[0]][position[1]] = true;
 			}
 		}
 	}
 
 	/**
 	 * Given a position as an array of two integers, returns
 	 * an integer consisting of the two integers concatenated.
 	 * @param position
 	 * @return integer
 	 */
 	public static int positionToKey(int[] position) {
 		String s1 = String.valueOf(position[0]);
 		String s2 = String.valueOf(position[1]);
 		return Integer.parseInt(s1+s2);
 	}
 
 
 	@Override
 	public int getID() {
 		return m_stateID;
 	}
 
 	public Player getPlayer() {
 		return this.m_player;
 	}
 
 	public void setPlayer(Player p) {
 		this.m_player = p;
 	}
 
 	/**
 	 * Sets the player's start/end location.
 	 * @param xLoc
 	 * @param yLoc
 	 */
 	public void setPlayerLocation(int xLoc, int yLoc) {
 		m_playerX = xLoc;
 		m_playerY = yLoc;
 	}
 
 	public String getMapPath() {
 		return this.m_mapPath;
 	}
 
 	/**
 	 * Removes a given object, and unblocks the map space it occupied.
 	 * @param key
 	 * @param xLoc
 	 * @param yLoc
 	 */
 	public void removeObject(String key, int xLoc, int yLoc) {
 		m_interactables.remove(key);
 		m_blocked[xLoc][yLoc] = false;
 		m_objects.remove(key);
 	}
 
 	/**
 	 * Returns true if the tile is blocked, and false otherwise.
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean blocked(int x, int y) {
 		return m_blocked[x][y];
 	}
 
 	/**
 	 * Sets tile at x,y to blocked, and returns true.
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean blockTile(int x, int y) {
 		m_blocked[x][y] = true;
 		return m_blocked[x][y];
 	}
 
 	/**
 	 * Fires an interaction, and returns the Interactable of an interaction.
 	 * @param interactSquare
 	 * @return
 	 */
 	public Interactable interact(int[] interactSquare, int[] squareOn) {
 		for(Entry<String, Interactable> e: m_interactables.entrySet()){
 			Interactable i = e.getValue();
 			int[] loc = i.getSquare();
 			if((loc[0]==interactSquare[0]&&loc[1]==interactSquare[1])||(loc[0]==squareOn[0]&&loc[1]==squareOn[1])){
 				dialogueListener(i);
 				i.fireAction(this, m_player);
 				return i;
 			}
 		}
 		return null;
 	}
 
 	@Deprecated
 	public abstract void dialogueListener(Interactable i);
 
 	public simpleMap getMap(){
 		return m_map;
 	}
 
 	public ArrayList<Interactable> getInteractables() {
 		ArrayList<Interactable> ret = new ArrayList<Interactable>();
 		for (Entry<String, Interactable> e : m_interactables.entrySet()) {
 			Interactable i = e.getValue();
 			ret.add(i);
 		}
 		return ret;
 	}
 
 
 	/**
 	 * Enters a scene.
 	 * @param scene
 	 */
 	public void enterScene() {
 		m_inScene = true;
 	}
 
 	public void exitScene() {
 		m_inScene = false;
 		m_inDialogue = false;
 		m_invisiblePlayer = false;
 		m_camera = new PlayerCamera(StateManager.getInstance().getContainer(),m_player);
 		additionalExitScene();
 	}
 
 	/**
 	 * Override if more needs to be done upon exiting scene
 	 */
 	public void additionalExitScene(){}
 
 	public void exitDialogueScene() {
 		//m_inScene = false;
 		m_inDialogue = false;
 	}
 
 	/**
 	 * Enters a particular dialogue
 	 * @param dialogue
 	 */
 	public void displayDialogue(String[] dialogue) {
 		//m_inScene = true;
 		m_inDialogue = true;
 		m_sceneDialogue = new Dialogue(this, StateManager.getInstance().getContainer(), dialogue, null);	
 	}
 
 	/**
 	 * Writes the data contained in the room to XML, not including
 	 * the player data.
 	 * @param writer
 	 * @throws XMLStreamException
 	 */
 	public void writeToXML(XMLStreamWriter writer) throws XMLStreamException {
 		writer.writeStartElement("GamePlayState");
 		if(m_mapPath!=null)
 			writer.writeAttribute("m_mapPath", m_mapPath);
 		writer.writeAttribute("id", String.valueOf(this.m_stateID));
 
 		writer.writeAttribute("m_playerX", String.valueOf(m_playerX));
 		writer.writeAttribute("m_playerY", String.valueOf(m_playerY));
 		//write attributes...
 		this.writeAttributes(writer);
 		writer.writeCharacters("\n");
 
 		writer.writeStartElement("GameObjects");
 		for (Entry<String, GameObject> e : m_objects.entrySet()) {
 			writer.writeStartElement("GameObject");
 			writer.writeAttribute("name", e.getKey());
 			writer.writeAttribute("id", String.valueOf(e.getValue().getKey()));
 			writer.writeEndElement();
 			writer.writeCharacters("\n");
 		}
 		writer.writeEndElement();
 		writer.writeCharacters("\n");
 
 		writer.writeStartElement("Interactables");
 		for (Entry<String, Interactable> e : m_interactables.entrySet()) {
 			writer.writeStartElement("Interactable");
 			writer.writeAttribute("name", e.getKey());
 			writer.writeAttribute("id", String.valueOf(e.getValue().getKey()));
 			writer.writeEndElement();
 			writer.writeCharacters("\n");
 		}
 		writer.writeEndElement();
 		writer.writeCharacters("\n");
 
 		writer.writeStartElement("Enemies");
 		for(Enemy e : m_enemies) {
 			writer.writeStartElement("Enemy");
 			writer.writeAttribute("id", String.valueOf(e.getKey()));
 			writer.writeCharacters("\n");
 		}
 		writer.writeEndElement();
 
 		writer.writeEndElement();
 	}
 
 	/**
 	 * Writes additional subclass-specific attributes.
 	 * @param writer
 	 * @throws XMLStreamException
 	 */
 	public void writeAttributes(XMLStreamWriter writer) throws XMLStreamException {
 
 	}
 	public void setCamera(Camera c){
 		m_camera = c;
 	}
 	public void shakeCamera(int length){
 		m_camera.shake(length);
 	}
 	@Override
 	public GamePlayState loadFromXML(Node n, GameContainer c, StateManager g) throws SlickException {
 		this.m_interactables = new HashMap<String, Interactable>();
 		this.m_objects = new HashMap<String, GameObject>();
 		NodeList children = n.getChildNodes();
 		for(int i = 0; i<children.getLength(); i++) {
 			Node child = children.item(i);
 			if(child.getNodeName().equals("Interactables")) {
 				Node c2 = child;
 				NodeList interactables = c2.getChildNodes();
 				for(int j = 0; j< interactables.getLength(); j++) {
 					Node c3 = interactables.item(j);
 					if(c3.getNodeName().equals("Interactable")) {
 						String name = c3.getAttributes().getNamedItem("name").getNodeValue();
 						int id = Integer.parseInt(c3.getAttributes().getNamedItem("id").getNodeValue());
 						Interactable o = (Interactable) StateManager.getObject(id);
 						this.m_interactables.put(name, o);
 					}
 				}
 			}
 			if(child.getNodeName().equals("GameObjects")) {
 				Node c2 = child;
 				NodeList interactables = c2.getChildNodes();
 				for(int j = 0; j< interactables.getLength(); j++) {
 					Node c3 = interactables.item(j);
 					if(c3.getNodeName().equals("GameObject")) {
 						String name = c3.getAttributes().getNamedItem("name").getNodeValue();
 						int id = Integer.parseInt(c3.getAttributes().getNamedItem("id").getNodeValue());
 						GameObject o = StateManager.getObject(id);
 						this.m_objects.put(name, o);
 					}
 				}
 			}
 		}
 		this.m_loaded = true;
 		return this;
 	}
 	//why is this in the class!?
 	//it needs to call set the state to game over.
 	//lol
 	public void gameOverEvent(){
 		//s.enterState(s.GAME_OVER_STATE);
 		System.out.println("GAME OVER");
 	}
 	class simpleMap implements TileBasedMap{
 		final int HEIGHT = m_tiledMap.getHeight();
 		final int WIDTH = m_tiledMap.getWidth();
 
 		public float getCost(PathFindingContext ctx, int x, int y){
 			return 1.0f;
 		}
 		public boolean blocked(PathFindingContext ctx, int x, int y){
 			if(m_invisiblePlayer)
 				return false;
 			return m_blocked[x][y];
 		}
 		public int getHeightInTiles(){
 			return HEIGHT;
 		}
 		public int getWidthInTiles(){
 			return WIDTH;
 		}
 		public void pathFinderVisited(int x, int y){};
 	}
 }
