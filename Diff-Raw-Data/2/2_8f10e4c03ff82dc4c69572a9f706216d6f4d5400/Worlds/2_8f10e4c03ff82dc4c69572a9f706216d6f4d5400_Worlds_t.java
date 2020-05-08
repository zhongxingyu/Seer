 package com.thepastimers.Worlds;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 8/6/13
  * Time: 10:27 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Worlds extends JavaPlugin implements Listener {
     @Override
     public void onEnable() {
         getLogger().info("Worlds init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         getLogger().info("Worlds init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Worlds disable");
     }
 
     @EventHandler
     public void spawn(CreatureSpawnEvent event) {
         EntityType type = event.getEntityType();
 
        //getLogger().info("Creature spawning");
         if (!"economy".equalsIgnoreCase(event.getLocation().getWorld().getName())) {
             return;
         }
 
         if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN
                 || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) {
             event.setCancelled(true);
            event.getEntity().setHealth(0);
         } else {
             getLogger().info("CREATURE_SPAWN: " + event.getSpawnReason().name());
         }
     }
 }
