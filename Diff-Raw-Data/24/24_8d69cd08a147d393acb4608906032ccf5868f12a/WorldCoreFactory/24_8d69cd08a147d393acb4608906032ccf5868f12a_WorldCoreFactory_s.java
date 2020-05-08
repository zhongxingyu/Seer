 package com.github.joukojo.testgame.world.core;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WorldCoreFactory {
	private final static Logger LOG = LoggerFactory.getLogger(WorldCoreFactory.class);
	
	private static WorldCore INSTANCE; 
	
	public static synchronized WorldCore getWorld() {
		if( INSTANCE == null ) {
			LOG.debug("creating world instance"); 
			INSTANCE = new WorldCoreImpl();
 		}
		return INSTANCE; 
 	}
	
 
 }
