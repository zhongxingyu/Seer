 package me.arboriginal.Creepersday;
 
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class CreepersdayEntityListener extends EntityListener {
   private final Creepersday plugin;
 
   public CreepersdayEntityListener(final Creepersday plugin) {
     this.plugin = plugin;
   }
 
   @Override
   public void onCreatureSpawn(CreatureSpawnEvent event) {
     CreatureType type = event.getCreatureType();
 
     if (type != CreatureType.CREEPER) {
       Entity entity = event.getEntity();
       World world = entity.getWorld();
 
       if (plugin.isCreepersday(world) && plugin.shouldConvertEntity(entity, type, false)) {
         entity = plugin.convertEntity(entity, CreatureType.CREEPER);
 
         if (plugin.shouldPowerCreeper(world, type, false)) {
           plugin.givePower(entity);
         }
       }
     }
   }
 
   @Override
   public void onEntityDeath(EntityDeathEvent event) {
     super.onEntityDeath(event);
 
     Entity entity = event.getEntity();
     World world = entity.getWorld();
 
    if (plugin.shouldDisplayStats(world)) {
       if (entity instanceof Player) {
         String looser = ((Player) entity).getName();
 
         if (looser != null) {// Looser IS null! :D
           plugin.logStat(world, looser, "deaths");
         }
       }
       else if (entity instanceof Creeper) {
         String killer = getKillerName(entity);
 
         if (killer != null) {
           plugin.logStat(world, killer, "kills");
         }
       }
     }
   }
 
   private String getKillerName(Entity entity) {
     String playerName = null;
     EntityDamageEvent cause = entity.getLastDamageCause();
 
     if (cause instanceof EntityDamageByEntityEvent) {
       Entity killBy = ((EntityDamageByEntityEvent) cause).getDamager();
 
       if (killBy instanceof Player) {
         return ((Player) killBy).getName();
       }
 
       if (killBy instanceof Projectile) {
         killBy = ((Projectile) killBy).getShooter();
 
         if (killBy instanceof Player) {
           return ((Player) killBy).getName();
         }
       }
     }
 
     return playerName;
   }
 }
