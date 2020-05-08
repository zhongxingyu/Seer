 package com.caindonaghey.commandbin.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import com.caindonaghey.commandbin.Phrases;
 
 public class NearbyCommand implements CommandExecutor {
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args) {
 		if(l.equalsIgnoreCase("nearby")) {
 			if(!(s instanceof Player)) {
 				System.out.println(Phrases.get("no-console"));
 				return true;
 			}
 			
 			Player player = (Player) s;
 			
 			if(!player.hasPermission("CommandBin.nearby")) {
 				player.sendMessage(Phrases.get("no-permission"));
 				return true;
 			}
 			
 			StringBuilder x = new StringBuilder();
 			int count = 0;
 			for(Entity nearbyEntities : player.getNearbyEntities(100, 100, 100)) {
 				if(nearbyEntities instanceof Player) {
 					x.append(((Player) nearbyEntities).getName() + "(" + nearbyEntities.getLocation().getX() + ", " + nearbyEntities.getLocation().getY() + ", " + nearbyEntities.getLocation().getY() + "), ");
 					count++;
 				}
 			}
 			
 			player.sendMessage(Phrases.prefix + "Players near you (" + count + "): ");
			if(count != 0) player.sendMessage(Phrases.prefix + x.toString().trim());
 		}
 		return true;
 	}
 
 }
