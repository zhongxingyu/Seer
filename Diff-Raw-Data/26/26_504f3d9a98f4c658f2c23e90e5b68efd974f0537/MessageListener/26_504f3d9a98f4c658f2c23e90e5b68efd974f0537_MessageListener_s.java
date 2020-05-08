 /*
  * Copyright (C) 2013 Dabo Ross <www.daboross.net>
  */
 package net.daboross.bukkitdev.displaynamemessages;
 
 import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  *
  * @author daboross
  */
public class MessageListener {
 
     private final DisplayNameMessagesPlugin plugin;
 
     public MessageListener(DisplayNameMessagesPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent evt) {
         evt.setJoinMessage(null);
         final Player p = evt.getPlayer();
         new BukkitRunnable() {
             @Override
             public void run() {
                 if (p.isOnline()) {
                     for (String message : Parser.getParsed(p, plugin.getMConfig().getLoginCopy())) {
                         Bukkit.broadcastMessage(message);
                     }
                 }
             }
         }.runTaskLater(plugin, 2);
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent evt) {
         evt.setQuitMessage(null);
         Player p = evt.getPlayer();
         for (String message : Parser.getParsed(p, plugin.getMConfig().getLogoutCopy())) {
             Bukkit.broadcastMessage(message);
         }
     }
 }
