 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.mctitan.redharvest.plugin;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 /**
  *
  * @author Czahrien
  */
 public class EntityListener implements Listener {
     
     RedHarvestPlugin myPlugin;
     
     public EntityListener(RedHarvestPlugin p) {
         myPlugin = p;
     }
     
     @EventHandler
     public void pigZombieSpawnCheck(CreatureSpawnEvent e) {
         Location l = e.getLocation();
         World w = l.getWorld();
         l.add(0,-1.0,0);
         // natural spawns on top of netherrack in the overworld should be 
         // replaced with pig zombies.
         if( !e.isCancelled() && 
                 e.getEntityType() != EntityType.PIG_ZOMBIE &&
                 w.getEnvironment() == Environment.NORMAL &&
                 e.getSpawnReason() == SpawnReason.NATURAL &&  
                (l.getBlock().getType() == Material.NETHERRACK) || l.getBlock().getType() == Material.SOUL_SAND) {    
             e.setCancelled(true);
             w.spawnCreature(e.getLocation(), EntityType.PIG_ZOMBIE);
         }
     }
 }
