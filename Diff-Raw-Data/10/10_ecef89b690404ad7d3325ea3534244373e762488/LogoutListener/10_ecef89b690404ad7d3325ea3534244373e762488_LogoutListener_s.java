 package com.minecraftdimensions.bungeesuitechat.listeners;
 
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.minecraftdimensions.bungeesuitechat.managers.PlayerManager;
 import com.minecraftdimensions.bungeesuitechat.objects.ServerData;
 
 
 public class LogoutListener implements Listener {
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void setFormatChat(PlayerQuitEvent e) {
 		PlayerManager.unloadPlayer(e.getPlayer().getName());
 		if(ServerData.getConnectionMessage()){
 			e.setQuitMessage(null);
 		}
 	}
 
 	
 
 }
