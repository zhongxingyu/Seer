 package client;
 
 import initial3d.engine.RenderWindow;
 import initial3d.engine.SceneManager;
 import initial3d.engine.Vec3;
 import client.factories.EntityFactory;
 import client.networking.*;
 import common.Log;
 import common.entity.Player;
 
 public class Client {
 	private AbstractState gameState = null;
 	public Log log;
 	public NetworkClient net;
 	private RenderWindow window;
 	private SceneManager scenemgr;
 	private Game gameWorld;
 
 	protected boolean running = false;
 
 	private final int updatesPerSecond = 60;
 	
 	
 	//TODO to be changed later for resizing via options pane etc.
 	//window stuff
 	private int width = 848;
 	private int height = 480;
 
 	public Client() {
 		this.log = new Log("client.log", true, System.out);
 		this.net = new NetworkClient(this);
 		
 		window = RenderWindow.create(width, height);
 		window.setLocationRelativeTo(null);
 		window.setVisible(true);
 		scenemgr = new SceneManager(width, height);
 		scenemgr.setDisplayTarget(window);
 		gameWorld = new Game();
 		
 		
 		//temp
 		//Adding player into the game
 		Player p = EntityFactory.createPlayer(Vec3.zero);
 		gameWorld.addMoveableEntity(p);
 		gameWorld.setPlayer(p.id);
 		
 	}
 
 	public void setState(AbstractState newState) {
		log.printf("{GAMESTATE} Changing gamestate to <%s>\n",newState.toString());
 		
 		//set up and attach the new scene from gamestate
 		newState.setup(this, this.gameWorld);
 		gameState = newState;
 		scenemgr.attachToScene(gameState.getScene());
 	}
 	
 	public Game getGameWorld(){
 		return gameWorld;
 	}
 	
 	//(OTHER)BEN's RUNNING CoDe
 	public void run() {
		this.setState(new StartupState());
 		this.running = true;
 
 		long maxTimePerUpdate = (1000 / updatesPerSecond);
 		long lastStartTime = System.currentTimeMillis();
 		long elapsedTimeSinceLastUpdate = System.currentTimeMillis();
 
 		while (this.running) {
 			elapsedTimeSinceLastUpdate = System.currentTimeMillis()
 					- lastStartTime;
 			lastStartTime = System.currentTimeMillis();
 
 			this.gameState.update(elapsedTimeSinceLastUpdate);
 
 			long currentTimeForTick = System.currentTimeMillis()
 					- lastStartTime;
 			if (currentTimeForTick < maxTimePerUpdate) {
 				try {
 					Thread.sleep((long) (maxTimePerUpdate - currentTimeForTick));
 				} catch (Exception e) {
 					System.out.printf("{ERROR} %s", e.toString());
 				}
 			} else
 				this.log.printf(
 						"Client isn't updating() fast enough. This is a problem. Last update() took: %d. maxTime=%d\n",
 						currentTimeForTick, maxTimePerUpdate);
 		}
 	}
 }
