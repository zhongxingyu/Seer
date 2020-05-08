 package com.judoguys.bukkit.gps;
 
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.judoguys.bukkit.gps.configuration.GPSConfiguration;
 import com.judoguys.bukkit.gps.configuration.GPSConfigurationType;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class GPSPlayerListener extends PlayerListener
 {
 	private Logger log;
 	
 	private GPS plugin;
 	
 	public GPSPlayerListener (GPS plugin)
 	{
 		this.plugin = plugin;
 		log = plugin.log;
 	}
 	
 	public GPS getPlugin ()
 	{
 		return plugin;
 	}
 	
 	@Override
	public void onPlayerLogin (PlayerLoginEvent event)
 	{
 		Player player = event.getPlayer();
 		String name = player.getName();
 		
 		if (getPlugin().configurations.containsKey(name)) {
 			// The configuration object for this player already exists
 			// (there was a save file for it); just return.
 			return;
 		}
 		
 		log.info(getPlugin().getLabel() + " Creating default config for player: " + name);
 		
 		GPSConfiguration config = new GPSConfiguration(plugin);
 		config.setPlayer(player);
 		config.reset();
 		
 		getPlugin().configurations.put(name, config);
 	}
 	
 	/**
 	 * Called when a player attempts to move location in a world.
 	 */
 	@Override
 	public void onPlayerMove (PlayerMoveEvent event)
 	{
 		// FIXME: Should this return if the event is canceled?
 //		if (event.isCancelled()) {
 //			return;
 //		}
 		
 		Player movedPlayer = event.getPlayer();
 		Location location = movedPlayer.getLocation();
 		
 		Iterator<GPSConfiguration> it = getPlugin().configurations.values().iterator();
 		while (it.hasNext()) {
 			// Iterate through each configuration object, checking for
 			// any that are following the player that just moved.
 			GPSConfiguration config = it.next();
 			if (config.getType() == GPSConfigurationType.FOLLOWING_PLAYER &&
 				config.getFollowedPlayer() == movedPlayer) {
 				// The player that moved is being followed by the player that
 				// owns this configuration object; set the owner's compass target
 				// to be the new location.
 				Player player = config.getPlayer();
 				try {
 					player.setCompassTarget(location);
 				} catch (Exception ex) {
 					// FIXME: This is going to spam the logs. Figure out why this is breaking.
 					log.info(getPlugin().getLabel() + " Error setting " + player.getName() + "'s compass target to " + player.getName() + "'s location");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Called when a player attempts to teleport to a new location
 	 * in a world.
 	 */
 	@Override
 	public void onPlayerTeleport (PlayerMoveEvent event)
 	{
 		onPlayerMove(event); // They are handled the same.
 	}
 }
