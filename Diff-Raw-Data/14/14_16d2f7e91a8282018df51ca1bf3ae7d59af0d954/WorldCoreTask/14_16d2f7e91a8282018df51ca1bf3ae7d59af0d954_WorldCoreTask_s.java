 package com.github.joukojo.testgame.world.core;
 
 import java.util.List;
import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This task makes the world go around
  * 
  * @author joukojo
  * 
  */
 public class WorldCoreTask implements Runnable {
 
 	private volatile boolean isRunning;
	private final static Logger LOG = LoggerFactory
			.getLogger(WorldCoreTask.class);
 
 	public WorldCoreTask() {
 		isRunning = true;
 	}
 
 	@Override
 	public void run() {
 		final WorldCore worldCore = WorldCoreFactory.getWorld();
 		final int existingPriority = Thread.currentThread().getPriority();
 		final Thread currentThread = Thread.currentThread();
 		currentThread.setPriority(Thread.MIN_PRIORITY);
 
 		while (isRunning) {
 			LOG.debug("and world starts to move");
 			List<String> objectNames = worldCore.getMoveableObjectNames();
 
 			for (String objectName : objectNames) {
 
				final List<Moveable> moveableObjects = worldCore
						.getMoveableObjects(objectName);
				Object params[] ={objectName, moveableObjects.size()};
				LOG.debug("the size of the moveable objects:{}:{}",
						params);
 				for (final Moveable moveable : moveableObjects) {
 					LOG.trace("moving object: {}", moveable);
 					moveable.move();
 				}
 
 			}
 			worldCore.cleanMoveables();
 
 			LOG.debug("and world has moved");
 			try {
 				LOG.trace("Sleeping for 20 msecs");
 				Thread.sleep(20);
 				LOG.trace("Slept.");
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 
 		}
 
 		currentThread.setPriority(existingPriority);
 
 	}
 }
