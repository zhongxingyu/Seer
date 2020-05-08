 package com.rylinaux.who;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class WhoListener implements Listener {
 
     @EventHandler
    public void onJoin(PlayerJoinEvent event) {
 
        Player player = event.getPlayer();
        String name = Who.getChat().getPlayerPrefix(player) + player.getName();
 
         player.sendMessage(Utilities.who());
 
        if (name.length() <= 16) {
             player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', name));
         }
     }
 }
