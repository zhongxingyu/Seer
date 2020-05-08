 package server.model;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import server.GameHandler;
 import server.listeners.CollisionListener;
 import server.listeners.PlayerMoveListener;
 import server.model.map.MapInfo;
 import server.model.map.PositionType;
 import server.model.map.TileMap;
 import server.properties.ApeProperties;
 
 public class Game {
 
 	// store players separatly.
 	private volatile Map<Integer, Player> players;
 	private volatile Map<Integer, Entity> entities;
 	TileMap map;
 	protected String mapName;
 	int width, height; // TODO: unused? (also in constructor)
 	private Collection<CollisionListener> collisionListeners;
 	
 	/**
 	 * True if game has already started, false if waiting for
 	 * <code>start()</code> signal
 	 */
 	private boolean running;
 
 	public Game(int width, int height) {
 		this.players = new HashMap<Integer, Player>();
 		this.collisionListeners = new LinkedList<CollisionListener>();
 		this.width = width;
 		this.height = height;
 		
 		this.loadMap(ApeProperties.getProperty("startMap"));
 	}
 
 	private void initEntities(MapInfo mapInfo) {
 		this.entities = new HashMap<Integer, Entity>();
 		for (Entity e : mapInfo.createEntities(this.getMap()))
 			this.addEntity(e);
 	}
 
 	/**
 	 * Launch this game
 	 */
 	public synchronized void start() {
 		this.running = true;
 		for (Entity e : entities.values()) {
 			if (e instanceof Barrier){
 				((Barrier) e).open();
 				e.type = "barrier_open";
 			}
 		}
 	}
 
 	public void addPlayer(int playerId, String playerName) {
 		float[] start = map.getFirstTileXY(PositionType.PlayerStart);
 		start[0] += this.players.size();
 		Player player = new Player(playerId, start[0], start[1], playerName);
 		player.setId(playerId);
 		player.addMoveListener(new PlayerMoveListener(this, map));
 		this.players.put(player.getId(), player);
 	}
 
 	public void removePlayer(int playerId) {
 		this.players.remove(playerId);
 	}
 
 	public Map<Integer, Player> getPlayers() {
 		return this.players;
 	}
 
 	public List<Player> getPlayersList() {
 		return new LinkedList<Player>(this.getPlayers().values());
 	}
 
 	public void setPlayerKeys(int playerId, List<Integer> keys) {
 		this.players.get(playerId).setKeysPressed(keys);
 	}
 
 	public Map<Integer, Player> getPlayersAsMap() {
 		return this.players;
 	}
 
 	public TileMap getMap() {
 		return map;
 	}
 
 	public void update() {
 		for (Entity entity : this.getAllEntites())
 			entity.brain(this);
 	}
 
 	public boolean hasPlayerWithId(int id) {
 		return this.players.containsKey(id);
 	}
 
 	/**
 	 * Returns true if this game (room) is empty.
 	 */
 	public boolean noPlayers() {
 		return this.players.isEmpty();
 	}
 
 	public void addCollisionListener(CollisionListener listener) {
 		this.collisionListeners.add(listener);
 	}
 
 	public void collision(Entity e) {
 		if (e.collisionState())
 			return;
 
 		// TODO: save (and read) collision state dependent on side of collision
 		e.setCollisionState(true);
 		for (CollisionListener listener : collisionListeners)
 			listener.collisionOccured(this, e);
 		// this.soundEvents.add("wall-collision");
 	}
 
 	public void addEntity(Entity e) {
 		this.entities.put(e.getId(), e);
 	}
 
 	public void removeEntity(Entity e) {
 		this.entities.remove(e.getId());
 	}
 
 	/**
 	 * 
 	 * @return all entities of this game INCLUDING the players
 	 */
 	public List<Entity> getAllEntites() {
 		List<Entity> list = new LinkedList<Entity>(this.entities.values());
 		list.addAll(this.getPlayersList());
 		return list;
 	}
 
 	public List<Entity> getEntities() {
 		return new LinkedList<Entity>(this.entities.values());
 	}
 
 	public void noCollision(Entity e) {
 		e.setCollisionState(false);
 	}
 
 	public Map<Integer, Entity> getAllEntitiesMap() {
 		Map<Integer, Entity> e = new HashMap<Integer, Entity>(this.players);
 		e.putAll(this.entities);
 		return e;
 	}
 
 	public GameEvent[] popEvents() {
 		Collection<GameEvent> result = new ArrayList<GameEvent>(EventHandler.getInstance().popEvents());
 		return result.toArray(new GameEvent[0]);
 	}
 
 	public void playerHit(Player player) {
 		EventHandler.getInstance().addEvent(new GameEvent(GameEvent.Type.SOUND, "kill"));
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void playerFinished(Player p) {
 		p.win();
 		EventHandler.getInstance().addEvent(new GameEvent(GameEvent.Type.SOUND, "win"));
 		/////TODO: FOR TESTING MAPCHANGE///
 		this.changeMap("map.json");
 	}
 	
 	public void changeMap(String map){
 		this.loadMap(map);
 		this.movePlayersToStartingPosition();
 		EventHandler.getInstance().addEvent(GameEvent.Type.MAPCHANGE, mapName);
 	}
 	
 	private void loadMap(String mapName){
 		this.mapName = mapName;
		String mapPath = GameHandler.getWebRoot()+File.separator+"maps"+File.separator+map;
 		MapInfo mapInfo = MapInfo.fromJSON(mapPath);
 		this.map = new TileMap(mapInfo);
 		this.initEntities(mapInfo);
 
 	}
 	
 	public void movePlayersToStartingPosition(){
 		int i = 0 ;
 		float[] start = this.map.getFirstTileXY(PositionType.PlayerStart);
 		for(Player player : this.getPlayersList()){
 			player.setX(start[0]+i);
 			player.setY(start[1]);
 			i++;
 		}
 	}
 	
 	public String getMapName(){
 		return this.mapName;
 	}
 
 }
