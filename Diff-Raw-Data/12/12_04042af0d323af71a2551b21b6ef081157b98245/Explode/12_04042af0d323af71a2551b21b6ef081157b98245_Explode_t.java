 package me.GudfareN.ExplodingEggs;
 
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityListener;
 

 public class Explode extends EntityListener {
 
     public static Main plugin;
 
     public Explode(Main instance) {
         plugin = instance;
     }
     
     
     
     public void onCreatureSpawn(CreatureSpawnEvent event)
     {
         if (event.getSpawnReason() == SpawnReason.EGG) {
             event.setCancelled(true); //Cancel Egg Spawn
             event.getEntity().getWorld().createExplosion(event.getLocation(), 2); //Where to explode and radius
   }  
  }
 }
 
