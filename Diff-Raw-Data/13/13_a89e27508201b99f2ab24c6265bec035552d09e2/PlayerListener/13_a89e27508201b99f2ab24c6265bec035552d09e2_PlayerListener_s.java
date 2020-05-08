 /*
  * Copyright (C) 2013 Dabo Ross <www.daboross.net>
  */
 package net.daboross.bungeedev.commoncommands;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.PlayerDisconnectEvent;
 import net.md_5.bungee.api.event.PostLoginEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 
 /**
  *
  * @author daboross
  */
 public class PlayerListener implements Listener {
 
     @EventHandler
     public void onJoin(PostLoginEvent evt) {
         for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
             if (!p.equals(evt.getPlayer())) {
                p.sendMessage(ChatColor.BLUE + p.getDisplayName() + ChatColor.YELLOW + " has joined.");
             }
         }
     }
 
     @EventHandler
     public void onQuit(PlayerDisconnectEvent evt) {
         for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
             if (!p.equals(evt.getPlayer())) {
                p.sendMessage(ChatColor.BLUE + p.getDisplayName() + ChatColor.YELLOW + " has left.");
             }
         }
     }
 }
