 package net.robinjam.bukkit.eternalwolf;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 /**
  *
  * @author robinjam
  */
 public class EntityListener extends org.bukkit.event.entity.EntityListener {
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         // If the entity that was damaged is a wolf
         if (event.getEntity() instanceof Wolf) {
             Wolf wolf = (Wolf) event.getEntity();
 
             // ...and the wolf has an owner
             if (wolf.isTamed()) {
 
                 // If the wolf was damaged by another entity
                 if (event instanceof EntityDamageByEntityEvent && wolf.getOwner() instanceof Player) {
                     EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
 
                     Player owner = (Player) wolf.getOwner();
 
                     // If the wolf was damaged by its owner using a bone
                     if (damageEvent.getDamager().equals(owner) && owner.getItemInHand().getType() == Material.BONE) {
                         // Release the wolf
                         wolf.setOwner(null);
                         wolf.setSitting(false);
                         owner.sendMessage(ChatColor.RED + "You have released your wolf!");
                     }
 
                     // If the wolf was damaged by an op using a bone
                     else if(damageEvent.getDamager() instanceof Player) {
                         Player attacker = (Player) damageEvent.getDamager();
                        if (attacker.isOp() && attacker.getItemInHand().getType() == Material.BONE) {
                            if (!owner.isOp()) {
                                wolf.setOwner(null);
                                wolf.setSitting(false);
                                attacker.sendMessage(ChatColor.RED + "You have released " + owner.getDisplayName() + "'s wolf!");
                                if (owner.isOnline())
                                    owner.sendMessage(ChatColor.RED + attacker.getDisplayName() + " has released your wolf!");
                            } else {
                                attacker.sendMessage(ChatColor.RED + "You may not release another op's wolves!");
                            }
                         }
                     }
                 }
                 
                 event.setCancelled(true);
             }
         }
 
         // The following is a bugfix to prevent tamed wolves becoming agressive
         // if their owner hits them with an arrow at point-blank
         
         // If the entity that was damaged is a player
         if (event.getEntity() instanceof Player && event instanceof EntityDamageByEntityEvent) {
             Player player = (Player) event.getEntity();
             EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
 
             // ...and the attacker is their own wolf
             if (damageEvent.getDamager() instanceof Wolf) {
                 Wolf wolf = (Wolf) damageEvent.getDamager();
                 
                 if (wolf.isTamed() && wolf.getOwner() instanceof Player) {
                     Player owner = (Player) wolf.getOwner();
                     if (owner.equals(player)) {
                         wolf.setTarget(null);
                         event.setCancelled(true);
                     }
                 }
             }
 
             // ...or they're attacking themself (how is that even possible?)
             if (damageEvent.getDamager().equals(damageEvent.getEntity()))
                 event.setCancelled(true);
         }
     }
     
 }
