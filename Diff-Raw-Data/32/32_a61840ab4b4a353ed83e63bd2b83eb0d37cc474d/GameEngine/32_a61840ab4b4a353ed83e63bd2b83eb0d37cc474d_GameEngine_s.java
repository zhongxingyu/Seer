 package com.github.joukojo.testgame;
 
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.joukojo.testgame.world.core.WorldCore;
 import com.github.joukojo.testgame.world.core.WorldCoreFactory;
 import com.github.joukojo.testgame.world.core.WorldCoreTask;
 import com.github.joukojo.testgame.world.graphics.GraphicEngine;
 import com.github.joukojo.testgame.world.graphics.GraphicEngineWorker;
 
 public class GameEngine {
 	private final static Logger LOG = LoggerFactory.getLogger(GameEngine.class);
	private GraphicEngine engine;
 
 	private MonsterCreatorTask monsterCreatorTask;
 	private CollisionDetectionWorker collisionDetector;
 	private GraphicEngineWorker graphicEngineWorker;
 	private WorldCoreTask worldCoreTask;
 
 	private ExecutorService executorService;
 	private static GameEngine INSTANCE;
 
 	public void init() {
 		
		GraphicsEnvironment env = GraphicsEnvironment
 				.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = env.getScreenDevices();
 		
		
		
		GraphicsConfiguration graphicsEngine = devices[0].getDefaultConfiguration();;
		engine = new GraphicEngine(graphicsEngine);
 
 		final WorldCore worldCore = WorldCoreFactory.getWorld();
 		final Player player = new Player();
 		player.setPositionX(500);
 		player.setPositionY(100);
 
 		worldCore.addMoveable(Constants.PLAYER, player);
 		monsterCreatorTask = new MonsterCreatorTask();
 
 		collisionDetector = new CollisionDetectionWorker();
 
 		graphicEngineWorker = new GraphicEngineWorker(engine);
 
 		worldCoreTask = new WorldCoreTask();
 
 		executorService = Executors.newCachedThreadPool();
 
 	}
 
 	public void startGame() {
 		LOG.debug("starting up the game engine");
 		executorService.execute(monsterCreatorTask);
 		executorService.execute(collisionDetector);
 		executorService.execute(graphicEngineWorker);
 		executorService.execute(worldCoreTask);
 
 	}
 
 	public void stopGame() {
 		monsterCreatorTask.setIsrunning(false);
 		collisionDetector.setRunning(false);
 		graphicEngineWorker.setRunning(false);
 		worldCoreTask.setRunning(false);
 
 		executorService.shutdownNow();
 
 	}
 
	public static synchronized GameEngine getInstance() {
 
		if (INSTANCE == null) {
			INSTANCE = new GameEngine();
 		}
 
 		return INSTANCE;
 	}
 
	public void reset() {

	}

 }
