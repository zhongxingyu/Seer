 package com.TeamNovus.SupernaturalRaces.Listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.event.weather.WeatherChangeEvent;
 
 import com.TeamNovus.SupernaturalRaces.SupernaturalRaces;
 import com.TeamNovus.SupernaturalRaces.Models.SNPlayer;
 
 public class DefaultServerListener implements Listener {
 
 	@EventHandler
 	public void onWeatherChange(WeatherChangeEvent event) {
 		
 	}
 	
 	@EventHandler
 	public void onLightningStrike(LightningStrikeEvent event) {
 		
 	}
 	
 	public void onServerTick() {
 		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
 			SNPlayer snp = SupernaturalRaces.getPlayerManager().getPlayer(player);
 			if(snp.getRemainingBleeding() > 0) {
				snp.setRemainingBleeding(snp.getRemainingBleeding() - 1);				
				if((snp.getRemainingBleeding()/20.0) % 5.0 == 0) {
					player.damage(1);
 					player.sendMessage(ChatColor.RED + "Bleeding...");
 				}
 			} else if(snp.getRemainingBleeding() == 0) {
 				snp.setRemainingBleeding(snp.getRemainingBleeding() - 1);
 				player.sendMessage(ChatColor.GOLD + "You have stopped bleeding!");
 			}
 			
 			if(snp.getRemainingPhaseWalk() > 0) {
 				snp.setRemainingPhaseWalk(snp.getRemainingPhaseWalk() - 1);
 			} if(snp.getRemainingPhaseWalk() == 0) {
 				snp.setRemainingPhaseWalk(snp.getRemainingPhaseWalk() - 1);
 				player.sendMessage(ChatColor.GOLD + "You have returned to your body!");
 			}	
 			
 			if(snp.getRemainingIceWalk() > 0) {
 				snp.setRemainingIceWalk(snp.getRemainingIceWalk() - 1);
 			} if(snp.getRemainingIceWalk() == 0) {
 				snp.setRemainingIceWalk(snp.getRemainingIceWalk() - 1);
 				player.sendMessage(ChatColor.GOLD + "You no longer can walk on water!");
 			}
 		}
 	}
 	
 }
