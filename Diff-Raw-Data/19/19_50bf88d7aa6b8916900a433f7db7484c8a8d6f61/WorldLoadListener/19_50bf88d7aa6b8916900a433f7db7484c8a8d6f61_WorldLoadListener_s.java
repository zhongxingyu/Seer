 package net.erbros.PvPTime;
 
import java.util.HashMap;

 import org.bukkit.World;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.event.world.WorldListener;
import org.bukkit.util.config.Configuration;
 
 public class WorldLoadListener extends WorldListener {
     private PvPTime plugin;
         
     WorldLoadListener(PvPTime instance) {
         plugin = instance;
     }
     
     public void onWorldInit( WorldInitEvent event )
     {
         World world = event.getWorld();
         plugin.loadWorldConfig(world);
         
     }
     
     
     
 }
