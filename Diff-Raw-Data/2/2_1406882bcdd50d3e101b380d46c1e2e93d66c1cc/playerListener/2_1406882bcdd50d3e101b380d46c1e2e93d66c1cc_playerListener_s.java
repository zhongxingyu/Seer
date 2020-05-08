 package com.Zolli.EnderCore.Listeners;
 
 import org.bukkit.World;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.Zolli.EnderCore.EnderCore;
 import com.Zolli.EnderCore.Events.playerTravelEnderEvent;
 import com.Zolli.EnderCore.Utils.ECPlayer;
 
 public class playerListener implements Listener {
 	
 	/**
 	 * The main class off the plugin
 	 */
 	EnderCore plugin;
 	
 	/**
 	 * Main world name
 	 */
 	String mainWorld = null;
 	
 	/**
 	 * End world name
 	 */
 	String endWorld = null;
 	
 	/**
 	 * Constructor
 	 * @param instance Plugin main class
 	 */
 	public playerListener(EnderCore instance) {
 		this.plugin = instance;
 		this.mainWorld = this.plugin.config.getString("worlds.mainWorld");
 		this.endWorld = this.plugin.config.getString("worlds.endWorld");
 	}
 	
 	/**
 	 * Handle player first join based on player info file exist
 	 * If this player joined first time insert a unique row to database
 	 * @param e Event
 	 */
 	@EventHandler
 	public void playerJoin(PlayerJoinEvent e) {
 		Player pl = e.getPlayer();
 		boolean played = pl.hasPlayedBefore();
 		int dragonCount = 0;
 
 		/* Write player to database if is joined the server first time */
 		if(!played) {
 			plugin.dbAction.addPlayer(pl);
 		}
 		/* END */
 		
 		/* Detecting ender dragons count on ender, and write it to settings */
 		World ender = plugin.getServer().getWorld(this.endWorld);
 		for(Entity ent : ender.getEntities()) {
 			if(ent instanceof EnderDragon) {
 				dragonCount++;
 			}
 		}
 		plugin.ffStorage.set("localSettings.dragonCount", dragonCount);
 		/* END */
 	}
 	
 	/**
 	 * Handle Player world changing
 	 * @param e Event
 	 */
 	@EventHandler
 	public void goNether(PlayerChangedWorldEvent e) {
 		Player pl = e.getPlayer();
 		ECPlayer epl = new ECPlayer(pl, plugin);
 		String toWorld = pl.getWorld().getName();
 		
 		/* If player traveling to the ender world */
 		if(toWorld.equalsIgnoreCase(this.endWorld)) {
 			playerTravelEnderEvent travelEvent = new playerTravelEnderEvent(pl, pl.getWorld(), e.getFrom(), epl.isDragonDefeted());
 			plugin.pluginManager.callEvent(travelEvent);
 			
 			/* If enough number of dragon in the ender */
 			if(plugin.config.getInt("dragons.desiredDragonCount") >= plugin.config.getInt("dragons.desiredDragonCount")) {
				pl.sendMessage("Hajr :)");
 			} else {
 				pl.teleport(plugin.getServer().getWorld(plugin.config.getString("worlds.endWorld")).getSpawnLocation());
 			}
 			/* END */
 		}
 		/* END */
 	}
 	
 }
