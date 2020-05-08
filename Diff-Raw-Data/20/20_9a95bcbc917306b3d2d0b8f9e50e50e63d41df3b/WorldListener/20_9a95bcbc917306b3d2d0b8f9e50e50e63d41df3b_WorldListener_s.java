 package com.djdch.bukkit.onehundredgenerator.listener;
 
 import org.bukkit.event.world.WorldInitEvent;
 
 import com.djdch.bukkit.onehundredgenerator.OneHundredGenerator;
 
 /**
  * 
  * @author DjDCH
  */
public class WorldListener extends org.bukkit.event.world.WorldListener {
     /**
 	 * 
 	 */
     private OneHundredGenerator plugin;
 
     /**
      * 
      * @param plugin
      */
     public WorldListener(OneHundredGenerator plugin) {
         this.plugin = plugin;
     }
 
     /**
      * 
      * @param event
      */
     public void onWorldInit(WorldInitEvent event) {
         this.plugin.WorldInit(event.getWorld());
     }
 }
