 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.meekers.plugins.aura;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author jaredm
  */
 class AuraPluginListener implements Listener {
 
     Aura plugin;
 
     public AuraPluginListener(Aura plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onEntityTarget(EntityTargetEvent event) {
         boolean repel = false;
         Entity e = event.getEntity();
         Entity t = event.getTarget();
         
         // If an entity dies, e may be null
         if (e == null) {
             return;
         }
         if (t.getType() == EntityType.PLAYER && e instanceof LivingEntity) {
             Player p = (Player) event.getTarget();
             int playerxp = p.getLevel();
             double speed = plugin.getConfig().getDouble("repelspeed");
             int zoxp = plugin.getConfig().getInt("zoxp");
             int spxp = plugin.getConfig().getInt("spxp");
             int skxp = plugin.getConfig().getInt("skxp");
             int crxp = plugin.getConfig().getInt("crxp");
             int enxp = plugin.getConfig().getInt("enxp");
 
             // Zombie
            if (zoxp > 0 && playerxp >= zoxp && e.getType() == EntityType.ZOMBIE) {
                 repel = true;
             }
             
             // Spider
            if (spxp > 0 && playerxp >= spxp && e.getType() == EntityType.SPIDER) {
                 repel = true;
             }
             
             // Skeleton
            if (skxp > 0 && playerxp >= skxp && e.getType() == EntityType.SKELETON) {
                 repel = true;
             }
 
             // Creeper
            if (crxp > 0 && playerxp >= crxp && e.getType() == EntityType.CREEPER) {
                 repel = true;
             }
             
             // Enderman
            if (enxp > 0 && playerxp >= enxp && e.getType() == EntityType.ENDERMAN) {
                 repel = true;
             }
             
             if (repel == true) {
                 // Get velocity unit vector:
                 Vector unitVector = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                 // Set speed and push entity:
                 e.setVelocity(unitVector.multiply(speed));
                 //p.sendMessage("Your XP just repelled a " + e.getType().getName());
                 event.setCancelled(true);
             }
         }
     }
 }
