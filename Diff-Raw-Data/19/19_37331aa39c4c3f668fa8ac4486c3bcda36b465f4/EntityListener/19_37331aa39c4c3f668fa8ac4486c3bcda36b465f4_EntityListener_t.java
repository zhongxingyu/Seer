 package net.robinjam.bukkit.eternalwolf;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 /**
  *
  * @author robinjam
  */
 public class EntityListener implements Listener {
 
     @EventHandler(priority=EventPriority.NORMAL)
     public void onEntityDamage(EntityDamageEvent event) {
         // If the entity that was damaged is a wolf
         if (event.getEntity() instanceof Wolf) {
             Wolf wolf = (Wolf) event.getEntity();
 
             // ...and the wolf has an owner
             if (wolf.isTamed()) {
 
                 // If the wolf was damaged by another entity
                 if (event instanceof EntityDamageByEntityEvent) {
                     EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
 
                     // If the wolf was damaged by its owner using a bone
                     if (damageEvent.getDamager() instanceof Player && ((Player)damageEvent.getDamager()).equals(wolf.getOwner())) {
                         Player player = (Player) damageEvent.getDamager();
 
                         // Check if the player has permission to release their own wolves
                         if (player.getItemInHand().getType() == Material.BONE && player.hasPermission("eternalwolf.release_own_wolves")) {
                             // Release the wolf
                             wolf.setOwner(null);
                             wolf.setSitting(false);
                             player.sendMessage(ChatColor.RED + "You have released your wolf!");
                         }
 
                         event.setCancelled(true);
                     }
 
                     // If the player has permission to release other peoples' wolves
                     else if(damageEvent.getDamager() instanceof Player) {
                         Player attacker = (Player) damageEvent.getDamager();
                         if (attacker.hasPermission("eternalwolf.release_other_wolves") && attacker.getItemInHand().getType() == Material.BONE) {
                             attacker.sendMessage(ChatColor.RED + "You have released " + ((OfflinePlayer) wolf.getOwner()).getName() + "'s wolf!");
                             if (wolf.getOwner() instanceof Player && ((Player)wolf.getOwner()).isOnline())
                                 ((Player)wolf.getOwner()).sendMessage(ChatColor.RED + attacker.getDisplayName() + " has released your wolf!");
                             
                             wolf.setOwner(null);
                             wolf.setSitting(false);
                             event.setCancelled(true);
                         }
                     }
                 }
 
                 // If the wolf's owner is offline or they have permission to have invincible wolves, cancel the event
                 if (wolf.getOwner() instanceof Player) {
                     Player owner = (Player) wolf.getOwner();
                     if (!owner.isOnline() || owner.hasPermission("eternalwolf.invincible_wolves")) {
                         event.setCancelled(true);
                     }
                 } else {
                     event.setCancelled(true);
                 }
             }
         }
 
         // The following is a bugfix to prevent tamed wolves becoming agressive
         // if their owner attacks himself with an arrow
 
         if (event.getEntity() instanceof Player && event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
 
             if (damageEvent.getDamager() instanceof Player && damageEvent.getDamager().equals(damageEvent.getEntity()))
                 event.setCancelled(true);
         }
     }
     
 }
