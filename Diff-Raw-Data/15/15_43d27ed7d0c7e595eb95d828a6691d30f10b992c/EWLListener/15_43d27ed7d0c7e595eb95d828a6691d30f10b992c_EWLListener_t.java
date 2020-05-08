 package com.emergencywhitelist.listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 import com.emergencywhitelist.EmergencyWhitelist;
 
 public class EWLListener implements Listener
 {	
 	EmergencyWhitelist plugin;
 	
 	public EWLListener(EmergencyWhitelist plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event)
 	{
 		Player player = event.getPlayer();
 		if (EmergencyWhitelist.isEnabled == true)
 		{
			if (!player.hasPermission("emergencywhitelist.whitelist"))
 			{
 				event.disallow(Result.KICK_WHITELIST, "EmergencyWhitelist has been enabled.");
 				plugin.logger().info(player.getName() + " failed to connect.");
 			}
 		}
 	}
 }
