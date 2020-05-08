 package com.minecarts.miraclegrow.listener;
 
 import com.minecarts.miraclegrow.MiracleGrow;
 import org.bukkit.event.server.*;
 
 import com.minecarts.dbquery.DBQuery;
 import com.minecarts.dbconnector.DBConnector;
 
 public class ServerListener extends org.bukkit.event.server.ServerListener {
     
     private MiracleGrow plugin;
     
     public ServerListener(MiracleGrow plugin) {
         this.plugin = plugin;
     }
     
     
     @Override
     public void onPluginDisable(PluginDisableEvent event) {
         if(event.getPlugin() instanceof DBQuery || event.getPlugin() instanceof DBConnector) {
             plugin.getServer().getPluginManager().disablePlugin(plugin);
         }
     }
     
 }
