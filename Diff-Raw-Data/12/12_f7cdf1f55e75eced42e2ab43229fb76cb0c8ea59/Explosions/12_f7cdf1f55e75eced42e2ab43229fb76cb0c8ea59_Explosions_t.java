 package com.pridemc.games.events;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
 public class Explosions implements Listener {
 	
 	@EventHandler
 	public void onCombust(EntityExplodeEvent event){
		event.blockList().clear();
 	}
 }
