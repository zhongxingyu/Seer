 package com.djdch.bukkit.deathlog.listener;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.djdch.bukkit.deathlog.DeathLog;
 
 /**
  * Class who listen for any player death.
  * 
  * @author DjDCH
  */
 public class DeathListener implements Listener {
     /**
      * Contains the Logger instance.
      */
     protected final Logger logger;
 
     /**
      * Constructor for the initialization of the DeathListener class.
      * 
      * @param deathlog Contains the DeathLog instance.
      */
     public DeathListener(DeathLog deathlog) {
         this.logger = deathlog.getPluginLogger();
     }
 
     /**
      * Method who is called each time an entity die in the game.
      * 
      * @param event Contains the EntityDeathEvent instance.
      */
     @EventHandler(priority = EventPriority.MONITOR)
     public void onEntityDeath(EntityDeathEvent event) {
         String msg = "";
 
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
             String name = player.getDisplayName();
 
             if (player.getLastDamageCause() != null) {
                 EntityDamageEvent lastDamageEvent = player.getLastDamageCause();
                 DamageCause cause = lastDamageEvent.getCause();
 
                 if (lastDamageEvent instanceof EntityDamageByEntityEvent) {
                     EntityDamageByEntityEvent lastDamageByEntityEvent = (EntityDamageByEntityEvent) lastDamageEvent;
                     Entity damager = lastDamageByEntityEvent.getDamager();
 
                    if (damager instanceof Arrow) {
                        Arrow arrow = (Arrow) damager;
 
                        msg = name + " was shot by " + getNameFromLivingEntity(arrow.getShooter());
                     } else if (cause.equals(DamageCause.ENTITY_EXPLOSION)) {
                         msg = name + " blew up";
                     } else if (damager instanceof LivingEntity) {
                         LivingEntity livingEntity = (LivingEntity) damager;
 
                         msg = name + " was slain by " + getNameFromLivingEntity(livingEntity);
                     } else {
                         msg = name + " died 'EntityDamageByEntityEvent'";
                     }
                 } else if (lastDamageEvent instanceof EntityDamageByBlockEvent) {
                     EntityDamageByBlockEvent lastDamageByBlockEvent = (EntityDamageByBlockEvent) lastDamageEvent;
                     Block damager = lastDamageByBlockEvent.getDamager();
 
                     if (cause.equals(DamageCause.CONTACT)) {
                         if (damager.getType() == Material.CACTUS) {
                             msg = name + " was pricked to death";
                         } else {
                             msg = name + " died 'CONTACT','EntityDamageByBlockEvent'";
                         }
                     } else if (cause.equals(DamageCause.LAVA)) {
                         msg = name + " tried to swim in lava";
                     } else if (cause.equals(DamageCause.VOID)) {
                         msg = name + " fell out of the world";
                     } else {
                         msg = name + " died 'EntityDamageByBlockEvent'";
                     }
                 } else {
                     if (cause.equals(DamageCause.FIRE)) {
                         msg = name + " went up in flames";
                     } else if (cause.equals(DamageCause.FIRE_TICK)) {
                         msg = name + " burned to death";
                     } else if (cause.equals(DamageCause.SUFFOCATION)) {
                         msg = name + " suffocated in a wall";
                     } else if (cause.equals(DamageCause.DROWNING)) {
                         msg = name + " drowned";
                     } else if (cause.equals(DamageCause.STARVATION)) {
                         msg = name + " starved to death";
                     } else if (cause.equals(DamageCause.FALL)) {
                         msg = name + " hit the ground too hard";
                     } else {
                         msg = name + " died 'EntityDamageEvent'";
                     }
                 }
             } else {
                 msg = name + " died";
             }
 
             if (!msg.isEmpty()) {
                 Location location = player.getLocation();
 
                 this.logger.info(msg + " ([" + location.getWorld().getName() + "] " + ((int) location.getX()) + ", " + ((int) location.getY()) + ", " + ((int) location.getZ()) + ")");
             }
         }
     }
 
     /**
      * Method who return a display name depending of the livingEntity type.
      * 
      * @param livingEntity Contains the LivingEntity instance.
      * @return Contains the display name string.
      */
     public static String getNameFromLivingEntity(LivingEntity livingEntity) {
         String name = "";
 
         if (livingEntity instanceof Player) {
             name = ((Player) livingEntity).getDisplayName();
         } else {
             name = livingEntity.toString().substring(5);
         }
 
         return name;
     }
 }
