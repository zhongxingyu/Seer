 package com.github.limdingwen.RealSleep;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class SleepEffect {
 	public static boolean refreshEffects() {
 		// To optimize performance, we only check effects when it changes.
 		
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 		Map<String, Float> data = new HashMap<String, Float>();
 		Logger log = Logger.getLogger("RealSleepEffect");
 		
 		try {
 			data = (Map) SLAPI.load("RealSleepData");
 		} catch (Exception e) {
 			log.warning("Cannot refresh effects! Reason: Cannot load file.");
 			
 			return false;
 		}
 		
 		for (int i = 0; i < players.length; i++) {
 			if (data.containsKey(players[i].getName())) {
 				if (data.get(players[i].getName()) < 20) {
					players[i].addPotionEffects((Collection<PotionEffect>) PotionEffectType.CONFUSION);
 					
 					return true;
 				}
 				else {
 					players[i].removePotionEffect(PotionEffectType.CONFUSION);
 					
 					return true;
 				}
 			}
 			else {
 				log.warning("Cannot update effect for " + players[i].getName() + "! Continued to not affect other players.");
 				players[i].sendMessage(ChatColor.RED + "Due to an internal error your sleep effect cannot be updated!");
 			}
 		}
 		
 		return true;
 	}
 }
