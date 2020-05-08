 package com.github.joukojo.testgame.world.core;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WorldCoreFactory {
	private final static Logger LOG = LoggerFactory
			.getLogger(WorldCoreFactory.class);

	private static WorldCore instance;

	public WorldCore getWorld() {
		synchronized (WorldCoreFactory.class) {
			if (instance == null) {
				LOG.debug("creating world instance");
				instance = new WorldCoreImpl();
			}
 		}
		return instance;
 	}
 
 }
