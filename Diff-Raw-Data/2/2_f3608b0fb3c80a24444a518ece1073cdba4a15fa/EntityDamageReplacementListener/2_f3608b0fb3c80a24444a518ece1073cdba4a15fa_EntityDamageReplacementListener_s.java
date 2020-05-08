 package com.herocraftonline.dev.heroes.health;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class EntityDamageReplacementListener extends EntityListener {
 
     Heroes plugin;
     public HashMap<Integer, Integer> mobHeal = new HashMap<Integer, Integer>();
     public EntityDamageReplacementListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     public void onEntityDamage(EntityDamageEvent event) {
         Properties prop = plugin.getConfigManager().getProperties();
 
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
             Hero hero = plugin.getHeroManager().getHero(player);
 
             if (event instanceof EntityDamageByEntityEvent) {
                 EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                 // Player VS Player Damage
                 if (subEvent.getEntity() instanceof Player) {
                     Player attacker = (Player) subEvent.getDamager();
                     if (prop.damages.containsKey(attacker.getItemInHand())) {
                         event.setCancelled(true);
                         hero.dealDamage(prop.damages.get(attacker.getItemInHand()));
                     } else {
                        plugin.log(Level.INFO, "You haven't got (" + attacker.getItemInHand().toString() + ") in your damage.yml - defaulting");
                     }
                 }
                 // Monsters VS Player Damage
                 if (getCreatureType(subEvent.getDamager()) != null) {
                     if (prop.damages.containsKey(getCreatureType(subEvent.getDamager()))) {
                         event.setCancelled(true);
                         hero.dealDamage(prop.damages.get(getCreatureType(subEvent.getDamager())));
                     } else {
                         plugin.log(Level.INFO, "You haven't got (" + getCreatureType(subEvent.getDamager()) + ") in your damage.yml - defaulting");
                     }
                 }
             } else if (event instanceof EntityDamageByProjectileEvent) {
                 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
                 // Projectile VS Player Damage
                 if (prop.damages.containsKey(subEvent.getProjectile().toString())) {
                     event.setCancelled(true);
                     hero.dealDamage(prop.damages.get(subEvent.getProjectile().toString()));
                 } else {
                     plugin.log(Level.INFO, "You haven't got (" + subEvent.getProjectile().toString() + ") in your damage.yml - defaulting");
                 }
             } else {
                 // General enviromental damage
                 if (prop.damages.containsKey(event.getCause().toString())) {
                     event.setCancelled(true);
                     hero.dealDamage(prop.damages.get(event.getCause().toString()));
                 } else {
                     plugin.log(Level.INFO, "You haven't got (" + event.getCause().toString() + ") in your damage.yml - defaulting");
                 }
             }
         }else {
             if (event instanceof EntityDamageByEntityEvent) {
                 EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
 
                 if (getCreatureType(subEvent.getDamager()) != null) {
                     if (prop.damages.containsKey(getCreatureType(subEvent.getDamager()))) {
                         event.setCancelled(true);
                         if(!mobHeal.containsKey(event.getEntity().getEntityId())) {
                             mobHeal.put(event.getEntity().getEntityId(), 100);
                         }
                         damageMob(prop.damages.get(getCreatureType(subEvent.getDamager())), mobHeal.get(event.getEntity().getEntityId()), event.getEntity().getWorld());
                     } else {
                         plugin.log(Level.INFO, "You haven't got (" + getCreatureType(subEvent.getDamager()) + ") in your damage.yml - defaulting");
                     }
                 }
             } else if (event instanceof EntityDamageByProjectileEvent) {
                 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
 
                 if (prop.damages.containsKey(subEvent.getProjectile().toString())) {
                     event.setCancelled(true);
                     if(!mobHeal.containsKey(event.getEntity().getEntityId())) {
                         mobHeal.put(event.getEntity().getEntityId(), 100);
                     }
                     damageMob(prop.damages.get(subEvent.getProjectile().toString()), mobHeal.get(event.getEntity().getEntityId()), event.getEntity().getWorld());
                 } else {
                     plugin.log(Level.INFO, "You haven't got (" + subEvent.getProjectile().toString() + ") in your damage.yml - defaulting");
                 }
             } else {
                 if (prop.damages.containsKey(event.getCause().toString())) {
                     event.setCancelled(true);
                     if(!mobHeal.containsKey(event.getEntity().getEntityId())) {
                         mobHeal.put(event.getEntity().getEntityId(), 100);
                     }
                     damageMob(prop.damages.get(event.getCause().toString()), mobHeal.get(event.getEntity().getEntityId()), event.getEntity().getWorld());
                 } else {
                     plugin.log(Level.INFO, "You haven't got (" + event.getCause().toString() + ") in your damage.yml - defaulting");
                 }  
             }
         }
     }
 
     public CreatureType getCreatureType(Entity entity) {
         CreatureType type = null;
         try {
             Class<?>[] interfaces = entity.getClass().getInterfaces();
             for (Class<?> c : interfaces) {
                 if (LivingEntity.class.isAssignableFrom(c)) {
                     type = CreatureType.fromName(c.getSimpleName());
                     return type;
                 }
             }
         } catch (IllegalArgumentException e) {
         }
         return type;
     }
 
     public void damageMob(Double damage, int mob, World world) {
         mobHeal.put(mob, (int) (mobHeal.get(mob) - damage));
         if(mobHeal.get(mob) <= 0) {
             for(Entity entity : world.getEntities()) {
                 if(entity.getEntityId() == mob) {
                     LivingEntity entityL = (LivingEntity) entity;
                     entityL.damage(0);
                     return;
                 }
             }
         }
     }
 }
