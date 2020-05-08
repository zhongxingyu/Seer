 package com.github.joukojo.testgame;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.joukojo.testgame.world.core.WorldCore;
 import com.github.joukojo.testgame.world.core.WorldCoreFactory;
 import com.github.joukojo.testgame.world.core.WorldCoreTask;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
 	private static final Logger LOG = LoggerFactory.getLogger(App.class);
 
 	public static void main(String[] args) {
 		LOG.debug("starting up the graphic engine");
 		final GraphicEngine engine = new GraphicEngine();
 
 //		PlayerMoveListener mouseListener = new PlayerMoveListener();
 //		
 //		engine.addMouseListener(mouseListener );
 //		engine.addMouseMotionListener(mouseListener);
 		WorldCore worldCore = WorldCoreFactory.getWorld();		
 		Player player = new Player();
 		player.positionX = 500; 
 		player.positionY = 100; 
 		
 		
 
 		worldCore.addMoveable("player", player);
 		Thread monsterCreator = new Thread(new MonsterCreatorTask(), "monster-creator");
 		monsterCreator.setPriority(Thread.MIN_PRIORITY);
 		monsterCreator.start();
 		
 		CollisionDetectionWorker collisionDetector = new CollisionDetectionWorker(); 
 		Thread detectorThread = new Thread(collisionDetector, "collision-detector");
 		detectorThread.setPriority(Thread.MIN_PRIORITY);
 		detectorThread.start();
 		
 		GraphicEngineWorker worker = new GraphicEngineWorker(engine);
 		Thread t = new Thread(worker, "graphicengine-worker");
 		t.start();
 		
 		WorldCoreTask worldCoreTask = new WorldCoreTask();
 		Thread worldThread = new Thread(worldCoreTask, "worldcore-worker");
 		worldThread.setPriority(Thread.MIN_PRIORITY);
 		worldThread.start();
 	}
 }
