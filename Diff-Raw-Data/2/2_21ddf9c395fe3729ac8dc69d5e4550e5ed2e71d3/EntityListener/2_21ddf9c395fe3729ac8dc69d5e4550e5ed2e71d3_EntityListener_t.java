 package com.minecarts.normalizeddrops.listener;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.WeakHashMap;
 import java.util.ListIterator;
 import java.util.Random;
 
 import org.bukkit.util.config.Configuration;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 
 
 public class EntityListener extends org.bukkit.event.entity.EntityListener {
     
     private HashMap<World, ArrayList<EntityDeathBox>> nearbyDeathTracker = new HashMap<World, ArrayList<EntityDeathBox>>();
     private WeakHashMap<Entity, ArrayList<EntityDamageEvent>> damageTracker = new WeakHashMap<Entity, ArrayList<EntityDamageEvent>>();
     private Configuration config = null;
     
     
     private final Random generator = new Random();
     
     public void setConfig(Configuration config) {
         this.config = config;
     }
     
 
     private int getNearbyDeathCount(Location point) {
         int deathCount = 0;
         if(!nearbyDeathTracker.containsKey(point.getWorld())) return 0;
         
         ArrayList<EntityDeathBox> nearbyDeaths = nearbyDeathTracker.get(point.getWorld());
         ListIterator<EntityDeathBox> itr = nearbyDeaths.listIterator(nearbyDeaths.size());
         
         while(itr.hasPrevious()){
             EntityDeathBox previousDeath = (EntityDeathBox)itr.previous();
             String world = point.getWorld().getName();
             
             if(previousDeath.deathTime < (System.currentTimeMillis() - (config.getInt(world + ".timeFactor", 600) * 1000))) {
                 if(itr.previousIndex() >= 0) {
                     nearbyDeaths.subList(0, itr.previousIndex()).clear();
                 }
                 break;
             }
             else {
                 //check to see if it contains
                 if(previousDeath.contains(point)) {
                     deathCount++;
                 }
            }
         }
         
         return deathCount;
     }
 
     @Override
     public void onCreatureSpawn(CreatureSpawnEvent event) {
         if(event.isCancelled()) return;
         
         Location loc = event.getLocation();
         String world = loc.getWorld().getName();
         
         if(!config.getBoolean(world + ".disableSpawns", false)) return;
 
         int r = generator.nextInt(config.getInt(world + ".maxDeaths", 15)) + config.getInt(world + ".minDeaths", 7);
         int deathCount = getNearbyDeathCount(loc);
         
         if(deathCount > r) {
             event.setCancelled(true);
             
             if(config.getBoolean(world + ".debug", true)) {
                 System.out.println(String.format("[NormalizedDrops] Normalized spawn in %s @ %.2f,%.2f,%.2f (Entity: %s, NearbyDeaths: %s > RND: %s, TrackerSize: %s)", world, loc.getX(), loc.getY(), loc.getZ(), event.getEntity().toString(), deathCount, r, nearbyDeathTracker.get(loc.getWorld()).size()));
             }
             
             return;
         }
     }
     
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         final Entity entity = event.getEntity();
         
         // don't normalize drops for human entities
         if(entity instanceof HumanEntity) return;
         
         if(entity instanceof LivingEntity) {
             // normalize items dropped
             if(event.getDrops().size() > 0) {
                 Location loc = entity.getLocation();
                 String world = loc.getWorld().getName();
                 EntityDeathBox box = new EntityDeathBox(loc.getX(), loc.getY(), loc.getZ(), config.getDouble(world + ".radius", 5));
                 int r = generator.nextInt(config.getInt(world + ".maxDeaths", 15)) + config.getInt(world + ".minDeaths", 7);
                 int deathCount = getNearbyDeathCount(loc);
                 if(deathCount > r) {
                     event.getDrops().clear();
                     if(config.getBoolean(world + ".debug", true)) {
                         System.out.println(String.format("[NormalizedDrops] Normalized drop in %s @ %.2f,%.2f,%.2f (Entity: %s, NearbyDeaths: %s > RND: %s, TrackerSize: %s)", world, loc.getX(), loc.getY(), loc.getZ(), event.getEntity().toString(), deathCount, r, nearbyDeathTracker.get(loc.getWorld()).size()));
                     }
                     return;
                 }
                 if(!nearbyDeathTracker.containsKey(loc.getWorld())) {
                     nearbyDeathTracker.put(loc.getWorld(), new ArrayList<EntityDeathBox>());
                 }
                 this.nearbyDeathTracker.get(loc.getWorld()).add(box);
             }
             
             
             // normalize experience dropped
             int exp = event.getDroppedExp();
             
             ArrayList<EntityDamageEvent> history = damageTracker.get(entity);
             damageTracker.remove(entity);
             
             if(exp > 0 && history != null) {
                 int totalDamage = 0;
                 int playerDamage = 0;
                 
                 for(EntityDamageEvent damageEvent : history) {
                     totalDamage += damageEvent.getDamage();
                     
                     if(damageEvent instanceof EntityDamageByEntityEvent) {
                         Entity attacker = ((EntityDamageByEntityEvent) damageEvent).getDamager();
                         
                         if(attacker instanceof Player || (attacker instanceof Tameable && ((Tameable) attacker).getOwner() instanceof Player)) {
                             playerDamage += damageEvent.getDamage();
                         }
                     }
                 }
                 
                 // normalize!
                event.setDroppedExp(totalDamage > 0 && playerDamage > 0 ? Math.round(exp * (playerDamage / totalDamage)) : 0);
             }
         }
     }
     
     
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         if(event.isCancelled()) return;
         
         ArrayList<EntityDamageEvent> history = damageTracker.get(event.getEntity());
         
         if(history == null) {
             history = new ArrayList<EntityDamageEvent>();
             damageTracker.put(event.getEntity(), history);
         }
         
         history.add(event);
     }
     
 
     private class EntityDeathBox {
         public long deathTime;
         public double xMin, xMax, yMin, yMax, zMin, zMax;
         
         public EntityDeathBox(double x, double y, double z, double radius) {
             this.deathTime = System.currentTimeMillis();
             xMin = x - radius;
             yMin = y - radius;
             zMin = z - radius;
             xMax = x + radius;
             yMax = y + radius;
             zMax = z + radius;
         }
         
         public String toString() {
             return String.format("%s @ %.2f, %.2f, %.2f -> %.2f %.2f %.2f", deathTime, xMin, yMin, zMin, xMax, yMax, zMax);
         }
         
         public boolean contains(Location p) {
             if(p.getX() <= xMin || p.getX() >= xMax) { return false; }
             if(p.getY() <= yMin || p.getY() >= yMax) { return false; }
             return p.getZ() > zMin && p.getZ() < zMax;
         }
     }
 }
