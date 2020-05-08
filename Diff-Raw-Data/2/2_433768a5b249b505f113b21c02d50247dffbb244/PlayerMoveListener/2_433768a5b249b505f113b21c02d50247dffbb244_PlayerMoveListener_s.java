 package com.github.leoverto.foolsgoldplugin;
 
 import java.util.HashMap;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class PlayerMoveListener implements Listener {
 	
 	protected static float slownessAmount = 0.1f;
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if ((Boolean) FoolsGoldPlugin.slowRainConfig.get("enabled")) {
 			Player player = event.getPlayer();
 			if (!player.getGameMode().equals(GameMode.CREATIVE)) { 
 				if (player.getWorld().hasStorm()) { //Make sure it's raining where the player is
 					Location playerLocation = event.getTo(); //Find where the player moved to
 					int highestBlockY = player.getWorld().getHighestBlockYAt(playerLocation); //Find the highest block where the player moved to
					int playerLocationY = playerLocation.getBlockY() + 1; //Find the player's height
 					if (highestBlockY < playerLocationY) { //If the highest block on the player's y is below the player...
 						if (weGaveSlow.containsKey(player.getName())) {
 							if (!weGaveSlow.get(player.getName())) { //If we did give the player a slow effect already...
 								 FoolsGoldPlugin.subtractWalkSpeed(player, slownessAmount); //Renew his slow effect.
 								 weGaveSlow.put(player.getName(), true);
 							 }
 						} else {
 							FoolsGoldPlugin.subtractWalkSpeed(player, slownessAmount);
 							weGaveSlow.put(player.getName(), true);
 						}
 					} else { //If the highest block on the player's y is above the player, e.g. he's under cover...
 						if (weGaveSlow.containsKey(player.getName())) { //Make sure he has the weGaveSlow metadata, otherwise this part is redundant.
 							if (weGaveSlow.get(player.getName())) { //If we gave the player a slow effect, and he has a slow effect...
 								 FoolsGoldPlugin.addWalkSpeed(player, slownessAmount);; //Remove his slow effect.
 								 weGaveSlow.put(player.getName(), false);
 							 }
 						}
 					}
 				} else {
 					if (weGaveSlow.containsKey(player.getName())) {
 						if (weGaveSlow.get(player.getName())) {
 							FoolsGoldPlugin.addWalkSpeed(player, slownessAmount);
 							weGaveSlow.put(player.getName(), false);
 						}
 					}
 				}
 			} else {
 				if (weGaveSlow.containsKey(player.getName())) {
 					if (weGaveSlow.get(player.getName())) {
 						FoolsGoldPlugin.addWalkSpeed(player, slownessAmount);
 						weGaveSlow.put(player.getName(), false);
 					}
 				}
 			}
 		}
 	}
 	
 	protected static HashMap<String, Boolean> weGaveSlow = new HashMap<String, Boolean>();
 	
 	
 }
