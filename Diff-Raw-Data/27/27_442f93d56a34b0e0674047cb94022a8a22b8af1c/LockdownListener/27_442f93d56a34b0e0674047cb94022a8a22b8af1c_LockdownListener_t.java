 package com.caindonaghey.commandbin.listeners;
 
 import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
 
 import com.caindonaghey.commandbin.commands.LockdownCommand;
 
 public class LockdownListener implements Listener {
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent e) {
 		if(LockdownCommand.lockdownKick) {
 			if(!e.getPlayer().hasPermission("CommandBin.lockdownexempt")) {
 				e.disallow(Result.KICK_BANNED, ChatColor.RED + "The server is in lockdown mode.");
 			}
 		}
 	}
 	
 //	@EventHandler
 //	public void onPlayerMove(PlayerMoveEvent e) {
 //		for(Entity entities : e.getPlayer().getNearbyEntities(30, 30, 30)) {
 //			entities.setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1, e.getPlayer().getVelocity().getZ()));
 //		}
 //	}
 	// Fun code. Highly recommended.
 
 }
