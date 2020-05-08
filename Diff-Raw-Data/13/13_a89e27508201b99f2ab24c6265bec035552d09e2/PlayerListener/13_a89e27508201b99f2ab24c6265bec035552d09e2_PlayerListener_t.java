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
 
    public static final String JOIN_FORMAT = ChatColor.BLUE + "%s" + ChatColor.YELLOW + " has joined.";
    public static final String LEAVE_FORMAT = ChatColor.BLUE + "%s" + ChatColor.YELLOW + " has left.";

     @EventHandler
     public void onJoin(PostLoginEvent evt) {
        String message = String.format(JOIN_FORMAT, evt.getPlayer().getDisplayName());
         for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
             if (!p.equals(evt.getPlayer())) {
                p.sendMessage(message);
             }
         }
     }
 
     @EventHandler
     public void onQuit(PlayerDisconnectEvent evt) {
        String message = String.format(LEAVE_FORMAT, evt.getPlayer().getDisplayName());
         for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
             if (!p.equals(evt.getPlayer())) {
                p.sendMessage(message);
             }
         }
     }
 }
