 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 
 public class foundationStoneListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	
 	public foundationStoneListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority=EventPriority.HIGH)
 	public void signPlace(SignChangeEvent e) {
		
 	}
 	
 }
