 package com.caindonaghey.commandbin.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.caindonaghey.commandbin.commands.VanishCommand;
 
 public class VanishListener implements Listener {
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e) {
 		for(Player allPlayers : Bukkit.getServer().getOnlinePlayers()) {
 			if(!allPlayers.isOp()) {
 				if(VanishCommand.vanishedPlayers.contains(allPlayers.getName())) {
 					e.getPlayer().hidePlayer(allPlayers);
 				}
 			}
 		}
 	}
 
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		for(Player allPlayers : Bukkit.getServer().getOnlinePlayers()) {
			if(!allPlayers.isOp()) {
				if(VanishCommand.vanishedPlayers.contains(allPlayers.getName())) {
					e.getPlayer().hidePlayer(allPlayers);
				}
			}
		}
	}

 }
