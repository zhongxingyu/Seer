 package com.sparkedia.valrix.ColorMe;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class ColorPlayerListener extends PlayerListener {
 	protected ColorMe plugin;
 	
 	public ColorPlayerListener(ColorMe plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void onPlayerChat(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName().toLowerCase();
 		if (ColorMe.colors.keyExists(name)) {
 			String color = ColorMe.colors.getString(name);
 			for (int i = 0; i <= 15; i++) {
 				String col = ChatColor.getByCode(i).name();
 				if (color.equalsIgnoreCase(col.toLowerCase().replace("_", ""))) {
					player.setDisplayName(ChatColor.valueOf(col)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
 					break;
 				}
 			}
 		} else {
 			player.setDisplayName(ChatColor.stripColor(player.getDisplayName()));
 		}
 	}
 }
