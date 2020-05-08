 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.meekers.plugins.automine;
 
 import org.bukkit.event.Listener;
 
 /**
  *
  * @author jaredm
  */
 class AutoMinePluginListener implements Listener {
 
     AutoMine plugin;
 
     public AutoMinePluginListener(AutoMine plugin) {
         this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
     
 }
