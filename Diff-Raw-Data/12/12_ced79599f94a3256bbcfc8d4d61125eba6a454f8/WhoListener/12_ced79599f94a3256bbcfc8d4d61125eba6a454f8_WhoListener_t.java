 package com.rylinaux.who;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class WhoListener implements Listener {
 
     @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
 
        final Player player = event.getPlayer();
        final String name = Who.getChat().getPlayerPrefix(player) + player.getName();
 
         player.sendMessage(Utilities.who());
 
        if (name.length() >= 15) {
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', name.substring(0, 15)));
        } else {
             player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', name));
         }
     }
 }
