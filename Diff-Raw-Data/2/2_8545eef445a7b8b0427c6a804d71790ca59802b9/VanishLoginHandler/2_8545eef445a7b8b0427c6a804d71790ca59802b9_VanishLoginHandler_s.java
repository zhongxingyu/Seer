 package com.connor.vanish;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 public class VanishLoginHandler implements Listener {
     
     private Vanish plugin;
 
     public VanishLoginHandler(Vanish plugin) {
         this.plugin = plugin;
     }
     
     @EventHandler
     public void handleLogin(PlayerLoginEvent event) {
         Player player = event.getPlayer();
 
         if (plugin.isVanished(player)) {
             plugin.showPlayer(player);
         }
         if (player.hasPermission("vanish.seeall")) return;
         for (Player p1 : plugin.getServer().getOnlinePlayers()) {
            if (plugin.isVanished(p1)) {
                 player.hidePlayer(p1);
             }
         }
     }
     
 }
