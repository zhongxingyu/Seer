 package com.webkonsept.bukkit.memorywarning;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class MemoryWarningPlayerListener extends PlayerListener {
 	MemoryWarning plugin;
 	
 	public MemoryWarningPlayerListener (MemoryWarning instance){
 		plugin = instance;
 	}
 	
 	public void onPlayerJoin(PlayerJoinEvent event){
 		if (!plugin.isEnabled()) return;
 		if (plugin.poller.isPanicing() && ! event.getPlayer().isOp()){
 			event.getPlayer().kickPlayer("Sorry mate, the server is in Memory Panic.  Try again later.");
			event.setJoinMessage(ChatColor.RED+event.getPlayer().getName()+" joined, but is rejected due to the current Memory Panic.");
 		}
 	}
 
 }
