 package com.martinbrook.tesseractuhc.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.martinbrook.tesseractuhc.UhcMatch;
 
 public class ChatListener implements Listener {
 	private UhcMatch m;
 	public ChatListener(UhcMatch m) { this.m = m; }
 
 	@EventHandler
 	public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
		if (m.isChatMuted() && !m.getPlayer(e.getPlayer()).isAdmin())
 			e.setCancelled(true);
 		
		// admins get gold text automatically
		if (m.getPlayer(e.getPlayer()).isAdmin()) {
			e.setMessage(ChatColor.GOLD + e.getMessage());
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerComandPreprocessEvent(PlayerCommandPreprocessEvent e) {
 		if (m.isChatMuted() && !m.getPlayer(e.getPlayer()).isAdmin() && e.getMessage().toLowerCase().startsWith("/me"))
 			e.setCancelled(true);
 	}
 
 }
