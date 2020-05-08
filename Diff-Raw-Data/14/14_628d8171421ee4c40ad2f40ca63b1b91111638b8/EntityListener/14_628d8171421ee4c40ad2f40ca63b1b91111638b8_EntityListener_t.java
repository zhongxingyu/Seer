 package net.xenosmc.mob;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 import java.util.EnumSet;
 
 public class EntityListener implements Listener {
     private XenosMobController plugin;
     private EnumSet<EntityType> UNDEAD = EnumSet.of(EntityType.SKELETON, EntityType.ZOMBIE);
     public EntityListener(XenosMobController plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onEntitySpawnEvent(CreatureSpawnEvent event) {
         if (plugin.getSpawningPoint() == null && plugin.getConfig().getBoolean("prevent-natural-spawns", true)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onEntityCombust(EntityCombustEvent event) {
         if (UNDEAD.contains(event.getEntity().getType())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
         if (event.getDamager().hasMetadata("spawnPoint")) {
             if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                 String key = event.getDamager().getMetadata("spawnPoint").get(0).asString();
                 SpawnPoint pt = plugin.getSpawnPoints().get(key);
                 if (pt != null) {
                     event.setDamage(pt.getDamage());
                 }
             }
        } else if (event.getDamager() instanceof Projectile) {
            if (((Projectile) event.getDamager()).getShooter().hasMetadata("spawnPoint")) {
                String key = ((Projectile) event.getDamager()).getShooter().getMetadata("spawnPoint").get(0).asString();
                SpawnPoint pt = plugin.getSpawnPoints().get(key);
                if (pt != null) {
                    event.setDamage(pt.getDamage());
                }
            }
         }
     }
 
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event) {
         if (event.getEntity().hasMetadata("spawnPoint")) {
             String key = event.getEntity().getMetadata("spawnPoint").get(0).asString();
             SpawnPoint sp = plugin.getSpawnPoints().get(key);
             if (sp != null) {
                 sp.setEntityId(null);
                 sp.setLastDeathTime(System.currentTimeMillis());
             }
         }
     }
     @EventHandler
     public void onEntityExplode(EntityExplodeEvent event) {
         if (event.getEntity().hasMetadata("spawnPoint")) {
             String key = event.getEntity().getMetadata("spawnPoint").get(0).asString();
             SpawnPoint sp = plugin.getSpawnPoints().get(key);
             if (sp != null) {
                 sp.setEntityId(null);
                 sp.setLastDeathTime(System.currentTimeMillis());
             }
         }
     }
 
 
     @EventHandler
     public void onChunkUnload(ChunkUnloadEvent event) {
         for (Entity e: event.getChunk().getEntities()) {
             if (e.hasMetadata("spawnPoint")) {
                 plugin.debug("Removing " + e + " due to chunk unload.");
                 SpawnPoint pt = plugin.getSpawnPoints().get(e.getMetadata("spawnPoint").get(0).asString().toLowerCase());
                 e.remove();
                 pt.setLastDeathTime(System.currentTimeMillis());
                 pt.setEntityId(null);
             }
         }
     }
 }
