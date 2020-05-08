 package com.minebans.antispam.checks;
 
 import java.util.Collections;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.minebans.antispam.AntiSpam;
 import com.minebans.antispam.data.PlayerData;
 import com.minebans.antispam.events.PlayerSpamDetectedEvent;
 import com.minebans.antispam.util.ListUtils;
 
 public class PlayerDataChecker implements Runnable {
 	
 	public AntiSpam plugin;
 	
 	public PlayerDataChecker(AntiSpam plugin){
 		this.plugin = plugin;
 	}
 	
 	private boolean isLoginSpammer(PlayerData playerData){
 		if (playerData.loginCount >= 4){
 			return true;
 		}
 		
 		if (playerData.loginDelays.size() < 2){
 			return false;
 		}
 		
 		if (Collections.min(playerData.loginDelays) < 140){
 			return true;
 		}
 		
 		return (ListUtils.stddev(playerData.loginDelays) < 25);
 	}
 	
 	private boolean isLogoutSpammer(PlayerData playerData){
 		if (playerData.logoutCount >= 4){
 			return true;
 		}
 		
 		if (playerData.logoutDelays.size() < 2){
 			return false;
 		}
 		
 		if (Collections.min(playerData.logoutDelays) < 140){
 			return true;
 		}
 		
 		return (ListUtils.stddev(playerData.logoutDelays) < 25);
 	}
 	
 	private boolean isChatSpamer(PlayerData playerData){
 		if (playerData.messageCount >= 8){
 			return true;
 		}
 		
 		if (playerData.messageDelays.size() < 2){
 			return false;
 		}
 		
 		if (Collections.min(playerData.messageDelays) < 100){
 			return true;
 		}
 		
 		return (ListUtils.stddev(playerData.messageDelays) < 20);
 	}
 	
 	public void run(){
 		String playerName;
 		PlayerData playerData;
 		
 		for (Entry<String, PlayerData> entry : plugin.dataManager.getAll()){
 			playerName = entry.getKey();
 			playerData = entry.getValue();
 			
 			if (this.isChatSpamer(playerData) || this.isLoginSpammer(playerData) || this.isLogoutSpammer(playerData)){
 				plugin.pluginManager.callEvent(new PlayerSpamDetectedEvent(playerName));
 				
 				if (playerData.warningCount > 3){
 					plugin.mineBans.tempBanPlayer(playerName, 1800);
 					plugin.dataManager.unregisterPlayer(playerName);
 					
 				}else{
 					++playerData.warningCount;
 					
 					Player player = plugin.server.getPlayer(playerName);
 					
 					if (player != null){
 						player.sendMessage(plugin.formatMessage(ChatColor.RED + "You have received a warning for spamming."));
 						player.sendMessage(plugin.formatMessage(ChatColor.RED + "More than three of these will result in a 30 minute ban."));
 					}
 					
 					playerData.resetCounters();
 					playerData.resetDelays();
 				}
 			}else{
 				playerData.resetCounters();
 			}
 		}
 	}
 	
 }
