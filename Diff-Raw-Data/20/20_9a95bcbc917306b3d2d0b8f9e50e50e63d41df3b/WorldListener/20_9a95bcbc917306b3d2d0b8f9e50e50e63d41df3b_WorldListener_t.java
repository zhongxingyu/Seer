 package com.djdch.bukkit.onehundredgenerator.listener;
 
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
 import org.bukkit.event.world.WorldInitEvent;
 
 import com.djdch.bukkit.onehundredgenerator.OneHundredGenerator;
 
 /**
  * 
  * @author DjDCH
  */
public class WorldListener implements Listener {
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
    @EventHandler(priority = EventPriority.HIGH)
     public void onWorldInit(WorldInitEvent event) {
         this.plugin.WorldInit(event.getWorld());
     }
 }
