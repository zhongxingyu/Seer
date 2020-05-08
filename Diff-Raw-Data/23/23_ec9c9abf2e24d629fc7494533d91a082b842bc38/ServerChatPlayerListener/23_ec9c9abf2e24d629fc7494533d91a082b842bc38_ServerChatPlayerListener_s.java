package me.cdsandrade.testplugin;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class ServerChatPlayerListener implements Listener {
 	
 	public static TestPlugin plugin;
 	
 	public ServerChatPlayerListener(TestPlugin instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent chat) {
 		Player p = chat.getPlayer();
 		String message = chat.getMessage().toLowerCase();
 		ChatColor RED = ChatColor.RED;
 		ChatColor WHITE = ChatColor.WHITE;
 		if(message.contains("hi") && message.contains("server")) {
 			p.sendMessage(RED + "[Server] " + WHITE + "Hello, " + p.getName() + "!");
 			chat.setCancelled(true);
 		}
 	}
 	
 }
