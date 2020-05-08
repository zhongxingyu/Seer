 package com.minebans.antispam.data;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.minebans.antispam.AntiSpam;
 
 public class PlayerDataListener implements Listener {
 	
 	private AntiSpam plugin;
 	
 	public PlayerDataListener(AntiSpam plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		
 		String playerName = player.getName();
 		
 		if (plugin.dataManager.gotDataFor(playerName) == false){
 			plugin.dataManager.registerPlayer(playerName);
 		}
 		
 		PlayerData playerData = plugin.dataManager.getPlayerData(playerName);
 		long currentTime = System.currentTimeMillis();
 		
 		++playerData.loginCount;
 		
 		if (playerData.lastLoginTime != 0L){
 			playerData.loginDelays.add(currentTime - playerData.lastLoginTime);
 			
 			if (playerData.loginDelays.size() > 50){
 				playerData.loginDelays.remove(0);
 			}
 		}
 		
 		playerData.lastLoginTime = currentTime;
 		
 		playerData.joinLocation = player.getLocation();
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event){
 		String playerName = event.getPlayer().getName();
 		
 		if (plugin.dataManager.gotDataFor(playerName) == false){
 			return;
 		}
 		
 		PlayerData playerData = plugin.dataManager.getPlayerData(playerName);
 		long currentTime = System.currentTimeMillis();
 		
 		++playerData.logoutCount;
 		
 		if (playerData.lastLogoutTime != 0L){
 			playerData.logoutDelays.add(currentTime - playerData.lastLogoutTime);
 			
 			if (playerData.logoutDelays.size() > 50){
 				playerData.loginDelays.remove(0);
 			}
 		}
 		
 		playerData.lastLogoutTime = currentTime;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		PlayerData playerData = plugin.dataManager.getPlayerData(event.getPlayer().getName());
		
		if (event.getFrom().equals(playerData.joinLocation)){
			playerData.joinLocation = event.getTo();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event){
 		PlayerData playerData = plugin.dataManager.getPlayerData(event.getPlayer().getName());
 		long currentTime = System.currentTimeMillis();
 		
 		++playerData.messageCount;
 		
 		if (playerData.lastMessageTime != 0L){
 			playerData.messageDelays.add(currentTime - playerData.lastMessageTime);
 			
 			if (playerData.messageDelays.size() > 50){
 				playerData.messageDelays.remove(0);
 			}
 		}
 		
 		playerData.lastMessageTime = currentTime;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		this.onPlayerChat(event);
 	}
 	
 }
