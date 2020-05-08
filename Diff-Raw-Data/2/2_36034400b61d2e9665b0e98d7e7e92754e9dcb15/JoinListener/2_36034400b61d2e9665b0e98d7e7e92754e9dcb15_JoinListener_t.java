 /*
  * Copyright (C) 2013 Dabo Ross <www.daboross.net>
  */
 package net.daboross.bungeedev.nchat;
 
 import net.daboross.bungeedev.ncommon.utils.ConnectorUtils;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ServerConnectedEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 
 /**
  *
  * @author daboross
  */
 public class JoinListener implements Listener {
 
     private final NChatPlugin plugin;
 
     public JoinListener(NChatPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onJoin(ServerConnectedEvent evt) {
         final ProxiedPlayer p = evt.getPlayer();
         String name = plugin.getDisplayNameDatabase().getDisplayName(p.getName());
         if (name == null) {
            name = ChatSensor.formatPlayerDisplayname(name);
         }
         p.setDisplayName(name);
         ConnectorUtils.setDisplayName(evt.getServer(), name);
     }
 }
