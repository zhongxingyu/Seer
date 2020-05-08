 package game;
 
 import java.util.*;
 
 import game.entity.Damageable;
 import game.entity.Entity;
 import game.entity.moveable.ItemEntity;
 import game.entity.moveable.PlayerEntity;
 import game.entity.moveable.ProjectileEntity;
 import game.entity.trigger.DynamicTriggerEntity;
 import game.event.AvoidEvent;
 import game.event.ContactEvent;
 import game.event.DeltaHealthEvent;
 import game.event.RemoveEntityEvent;
 import game.item.Item;
 import game.level.Level;
 import game.net.NetworkingClient;
 import initial3d.*;
 import initial3d.engine.*;
 import initial3d.engine.xhaust.Component;
 import initial3d.engine.xhaust.DialogPane;
 import initial3d.engine.xhaust.Pane;
 import game.net.*;
 import game.net.packets.ChangeAttributePacket;
 import game.net.packets.MovementPacket;
 import game.net.packets.ProjectileLifePacket;
 import game.net.packets.SetReadyPacket;
 import game.states.*;
 
 /***
  * Main game class
  * 
  * @author Ben
  * 
  */
 public class Game implements Runnable {
 	private static Game gameInstance = null;
 
 	public static Game getInstance() {
 		if (gameInstance == null) gameInstance = new Game();
 
 		return gameInstance;
 	}
 
 	public static long time() {
 		return getInstance().getGameTime();
 	}
 
 	private final int gameHz = 30;
 	private final long optimalTime = 1000000000 / gameHz;
 
 	private DialogPane invenPopUp;
 	private Pane inventoryHolder;
 	private GameState currentGameState;
 	private GameState previousState = null; 
 	private Thread gameThread = null;
 	private boolean gameRunning = false;
 	private RenderWindow gameWindow = null;
 	private SceneManager sceneManager = null;
 	private int updatesPerSecond = 0;
 	private NetworkingClient network = null;
 	private NetworkingHost nhost = null;
 	private int playerIndex = -1;
 
 	private Level currentLevel = null;
 	private boolean isPregameReady = false;
 
 	private long predictedLatency = 0;
 	private long timeOffset = 0;
 	private int maxPlayers = 1;
 
 	private Map<Integer, PlayerEntity> players = new HashMap<Integer, PlayerEntity>();
 
 	private Game() {
 	}
 
 	/***
 	 * Starts the main game including the game loop
 	 */
 	public void start() {
 		this.changeState(new PreloadGameState());
 		this.gameRunning = true;
 		this.gameThread = new Thread(this);
 		this.gameThread.start();
 	}
 
 	public void stop() {
 		this.gameRunning = false;
 		try {
 			this.gameThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/***
 	 * The main game loop thread - contains a much better implementation of ben
 	 * a's update loop
 	 */
 	public void run() {
 		long lastUpdateTime = System.nanoTime();
 		long lastUpsTime = 0;
 		int ups = 0;
 
 		while (gameRunning) {
 			long now = System.nanoTime();
 			long updateLength = now - lastUpdateTime;
 			lastUpdateTime = now;
 			double delta = updateLength / 1000000000d;
 
 			lastUpsTime += updateLength;
 			ups++;
 
 			if (lastUpsTime >= 1000000000) {
 				this.updatesPerSecond = ups;
 				lastUpsTime = 0;
 				ups = 0;
 			}
 			this.getState().update(delta);
 
 			try {
 				long sleepTime = (lastUpdateTime - System.nanoTime() + optimalTime) / 1000000;
 				if (sleepTime > 0) Thread.sleep(sleepTime);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/***
 	 * Changes the main game state to the state supplied
 	 * 
 	 * @param gs
 	 *            The state to be used as the main game state from now on
 	 */
 	public void changeState(GameState gs) {
 		Profiler p = new Profiler();
 		p.setResetOutput(System.out);
 
 		p.startSection("ChangeState-DestroyOldGameState()");
 		if (this.previousState != null) this.previousState.destroy();
 		p.endSection("ChangeState-DestroyOldGameState()");
 		this.previousState = this.currentGameState;
 		this.currentGameState = gs;
 
 		p.startSection("ChangeState-InitaliseNewState()");
 		this.currentGameState.initalise();
 		p.endSection("ChangeState-InitaliseNewState()");
 
 		p.startSection("ChangeState-AttachToScene()");
 		if (this.sceneManager != null) this.sceneManager.attachToScene(this.currentGameState.scene);
 		p.endSection("ChangeState-AttachToScene()");
 
 		// This is used to figure out how long stuff is going.. disabled most of
 		// the time!
 		// p.reset();
 	}
 	
 	public void revertState() {
 		if (this.currentGameState != null) this.currentGameState.destroy();
 
 		this.currentGameState = this.previousState;
 
 		if (this.sceneManager != null) this.sceneManager.attachToScene(this.currentGameState.scene);
 
 
 		// This is used to figure out how long stuff is going.. disabled most of
 		// the time!
 		// p.reset();
 	}
 
 	/***
 	 * Returns the game state currently being run by the game
 	 * 
 	 * @return The current GameState
 	 */
 	public GameState getState() {
 		return this.currentGameState;
 	}
 
 	public int getUps() {
 		return this.updatesPerSecond;
 	}
 
 	/***
 	 * Ensures the game has a valid game window
 	 */
 	public void createWindow() {
 		this.gameWindow = RenderWindow.create(800, 600);
 		this.gameWindow.setVisible(true);
 		this.sceneManager = new SceneManager(800, 600);
 		RenderWindow rwin = this.gameWindow;
 		this.sceneManager.setDisplayTarget(rwin);
 		rwin.addKeyListener(sceneManager);
 		rwin.addCanvasMouseListener(sceneManager);
 		rwin.addCanvasMouseMotionListener(sceneManager);
 		rwin.addMouseWheelListener(sceneManager);
 
 		this.sceneManager.getProfiler().setResetOutput(null);
 	}
 
 	public RenderWindow getWindow() {
 		return this.gameWindow;
 	}
 
 	public NetworkingClient getNetwork() {
 		return this.network;
 	}
 
 	public void setNetwork(NetworkingClient nm) {
 		if (this.network != null) this.network.destroy();
 
 		this.network = nm;
 	}
 
 	public void setHost(NetworkingHost networkingHost) {
 		if (this.nhost != null) this.nhost.destroy();
 
 		this.nhost = networkingHost;
 	}
 
 	public void setPlayerIndex(int pIndex) {
 		this.playerIndex = pIndex;
 
 		PlayerEntity pe = new PlayerEntity(0, Vec3.create(pIndex + 2, 0.220, pIndex + 2), 0.125, pIndex);
 
 		// This needs to add the main player
 		addPlayer(pIndex, pe);
 		
 		System.out.printf("I am player #%d\n", pIndex);
 	}
 
 	public void setMaxPlayers(short _maxPlayers) {
 		// These can add the rest of the players
 		for (int i = 0; i < _maxPlayers; i++) {
 			if (i == this.playerIndex) continue;
 			
 			PlayerEntity p = new PlayerEntity(System.nanoTime(), Vec3.create(i + 2, 0.125, i + 2), 0.125, i);
 			addPlayer(i, p);
 		}
 
 		this.maxPlayers = _maxPlayers;
 		
 		System.out.printf("There are %d players\n", _maxPlayers);
 	}
 
 	public int getPlayerIndex() {
 		return this.playerIndex;
 	}
 	
 	public PlayerEntity getPlayer() {
 		return this.players.get(this.playerIndex);
 	}
 
 	public void addPlayer(int index, PlayerEntity e) {
 		this.players.put(index, e);
 	}
 
 	public void movePlayer(int playerIndex, Vec3 position, Vec3 velocity, Quat orientation) {
 		PlayerEntity me = this.players.get(playerIndex);
 		if (me == null) return;
 
 		// TODO need to transmit orientation and angular velocity
 		me.updateMotion(position, velocity, orientation, Vec3.zero, this.getGameTime());
 	}
 
 	public void transmitPlayerPosition() {
 		MovementPacket mp = new MovementPacket(this.getPlayerIndex(), this.getPlayer().getPosition(), this.getPlayer().getLinVelocity(), this.getPlayer().getOrientation());
 		this.getNetwork().send(mp.toData());
 	}
 
 	public void addEntity(Entity _entity)
 	{
 		if(this.isHost())
 			selfAddEntity(_entity);
 	}
 	
 	public void selfAddEntity(Entity _entity)
 	{
 		currentLevel.addEntity(_entity);
 		_entity.addToScene(currentGameState.scene);
 	}
 
 	public void removeEntity(long _eid) {
 		if(this.isHost())
 			selfRemoveEntity(_eid);
 	}
 	
 
 	public void selfRemoveEntity(long eid) {
 		Entity e = currentLevel.removeEntity(eid);
 		if(e!=null){
 			currentGameState.scene.removeDrawables(e.getMeshContexts());
 		}
 	}
 	
 	public void spawnItem(Item _item, Vec3 _position){
 		addEntity(new ItemEntity(_position, _item));
 	}
 	
 	public void addItemToPlayer(long _eid, Item _item){
 		for(PlayerEntity p : getPlayers()){
 			if(p.id == _eid){
 				if(p.getInventory().containsItem(_item)){
 					break;
 				}
 				p.getInventory().addItem(_item);
 				invenPopUp.getRoot().repaint();
 				break;
 			}
 		}
     	
 	}
 	
 	public void removeItemFromPlayer(long _eid, Item _item){
 	}
 
 	public boolean isHost() {
 		return this.nhost != null;
 	}
 
 	public long getGameTime() {
 		return System.currentTimeMillis() + this.timeOffset;
 	}
 	
 
 	public void setTimeOffset(long offset) {
 		this.timeOffset = offset;
 	}
 
 	public void setPredictedLatency(long pl) {
 		this.predictedLatency = pl;
 	}
 
 	public Level getLevel() {
 		return currentLevel;
 	}
 
 	public void setLevel(Level level) {
 		currentLevel = level;
 	}
 
 	public int getCurrentLevelNumber() {
 		return 5;
 	}
 
 	public NetworkingHost getHost() {
 		return this.nhost;
 	}
 
 	public int getMaxPlayers() {
 		return this.maxPlayers;
 	}
 
 	public PlayerEntity[] getPlayers() {
 		PlayerEntity[] ps = new PlayerEntity[this.players.size()];
 		for (int i = 0; i < this.players.size(); i++)
 			ps[i] = this.players.get(i);
 
 		return ps;
 	}
 
 	public void createProjectile() {
 		long id = System.nanoTime();
 		Vec3 pos = this.getPlayer().getPosition();
 		Vec3 vel = this.currentGameState.scene.getCamera().getNormal().flattenY().unit().scale(4);
 		Quat orientation = this.getPlayer().getOrientation();
 		short creator = (short) this.getPlayerIndex();
 		long createTime = System.currentTimeMillis();
 		
 		ProjectileLifePacket pl = new ProjectileLifePacket();
 		pl.eid = id;
 		pl.pos = pos;
 		pl.vel = vel;
 		pl.ori = orientation;
 		pl.creator = creator;
 		pl.createTime = createTime;
 		pl.setCreateMode();
 		
 		this.getNetwork().send(pl.toData());
 		
 		System.out.println("Sent projectile request");
 	}
 
 	public void selfCreateProjectile(long id, Vec3 position, Vec3 velocity, Quat orientation, short creator, long createTime) {
 //		public ProjectileEntity(long _id, long _parid, int _delta, Vec3 _pos, Vec3 _vel)
		ProjectileEntity pe = new ProjectileEntity(Game.getInstance().getPlayers()[creator].id, -5, position, velocity, orientation);		
 		pe.addToLevel(Game.getInstance().getLevel());
 		pe.addToScene(Game.getInstance().currentGameState.scene);
 		
 		System.out.println("created projectile!");
 	}
 
 	public void setSelfPregameReady(boolean b) {
 		Game.getInstance().getPlayer().setPregameReadyState(b);
 		SetReadyPacket srp = new SetReadyPacket();
 		srp.newReadyStatus = Game.getInstance().getPlayer().getPregameReadyState();
 		srp.pIndex = this.getPlayerIndex();
 		byte[] data = srp.toData().getData();
 		for(int i = 0; i < data.length; i++)
 			System.out.printf("0x%02X ", data[i]);
 		System.out.println();
 		this.getNetwork().send(srp.toData());
 	}
 	
 	public void setPregameReady(int pIndex, boolean b)
 	{
 		Game.getInstance().getPlayers()[pIndex].setPregameReadyState(b);
 	}
 
 	public boolean isPregameReady() {
 		return this.isPregameReady;
 	}
 	
 	public void setInvenPopUp(DialogPane invenPopUp) {
 		this.invenPopUp = invenPopUp;
 	}
 	
 	public DialogPane getInvenPopUp() {
 		return invenPopUp;
 	}
 
 	public Pane getInventoryHolder() {
 		return inventoryHolder;
 	}
 
 	public void setInventoryHolder(Pane inventoryHolder) {
 		this.inventoryHolder = inventoryHolder;
 	}
 
 	public void updatePregameScreen() {
 		if(this.currentGameState instanceof PregameState)
 		{
 			((PregameState)this.currentGameState).updatePregameScreen();
 		}
 	}
 
 	public void setGameStarting() {
 		if(this.currentGameState instanceof PregameState)
 		{
 			((PregameState)this.currentGameState).setGameStarting();
 		}
 	}
 
 	public void requestStart() {
 		if(this.isHost())
 			this.getHost().requestStart();
 	}
 
 	public void setLobbyCurrentPlayers(int nPlayers) {
 		if(this.currentGameState instanceof LobbyState)
 		{
 			((LobbyState)this.currentGameState).setNumPlayers(nPlayers);
 		}
 	}
 
 	public void setEntityHealth(long id, int delta)
 	{
 		ChangeAttributePacket cap = new ChangeAttributePacket();
 		cap.setHealth();
 		cap.eid = id;
 		cap.newVal = delta;
 		this.nhost.notifyAllClients(cap);
 	}
 	
 	
 	public void selfSetEntityHealth(long id, int i)
 	{
 		System.out.printf("Changing eid=%d to hp=%d\n", id, i);
 		Damageable d = (Damageable) this.currentLevel.getEntity(id);
 		if(d == null) return;
 		d.setCurrentHealth(i);
 	}
 
 }
