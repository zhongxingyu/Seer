 package com.mcnsa.mcnsachat2.listeners;
 
 import com.mcnsa.mcnsachat2.MCNSAChat2;
 
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class PListener extends PlayerListener {
 	// store the plugin for later access
 	private MCNSAChat2 plugin;
 	public PListener(MCNSAChat2 instance) {
 		plugin = instance;
 	}
 
	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		// a player joined!
 		// sort out their channel etc
 	}
 
	@Override
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		// if the event is cancelled,
 		// get out of here!
 		if(event.isCancelled()) {
 			return;
 		}
 
 		// TODO: handle commands!
 		// (pass to the command manager)
 	}
 }
