 package com.gmail.zant95.LiveChat.Listeners;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.gmail.zant95.LiveChat.LiveChat;
 import com.gmail.zant95.LiveChat.PlayerDisplayName;
 
 public class JoinListener implements Listener {
 	LiveChat plugin;
 
 	public JoinListener(LiveChat instance) {
 		plugin = instance;
 	}
 
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		PlayerDisplayName.main(event.getPlayer());
 	}
 }
