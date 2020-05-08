 package com.norcode.bukkit.livestocklock;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 import java.util.List;
 import java.util.UUID;
 
 public class EntityListener implements Listener {
 
     LivestockLock plugin;
 
     public EntityListener(LivestockLock plugin) {
         this.plugin = plugin;
     }
 
     public void checkExpiry(Entity e) {
         OwnedAnimal oa = plugin.getOwnedAnimal(e.getUniqueId());
         if (oa == null) return;
         if (plugin.getExpiryTime() > 0 && System.currentTimeMillis() - oa.getOwnerActivityTime() > plugin.getExpiryTime()) {
             plugin.removeOwnedAnimal(oa);
             if (plugin.isDespawnExpiredClaims()) {
                 e.remove();
             }
         }
     }
 
 
     @EventHandler
     public void onEntityTarget(EntityTargetEvent event) {
         checkExpiry(event.getEntity());
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onEntityDeath(EntityDeathEvent event) {
         OwnedAnimal oa = plugin.getOwnedAnimal(event.getEntity().getUniqueId());
         if (oa != null) {
             plugin.removeOwnedAnimal(oa);
         }
     }
 
 
 
     @EventHandler(ignoreCancelled=true)
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         checkExpiry(event.getEntity());
         OwnedAnimal oa = plugin.getOwnedAnimal(event.getEntity().getUniqueId());
         if (oa == null) return;
         Player damager = null;
         if (event.getDamager().getType() == EntityType.PLAYER) {
             damager = (Player) event.getDamager();
         } else if (event.getEntity() instanceof Projectile) {
             Projectile p = (Projectile) event.getDamager();
             if (p.getShooter() instanceof Player) {
                 damager = (Player) p.getShooter();
             }
         }
         if (damager != null && !oa.allowAccess(damager.getName())) {
             event.setCancelled(true);
             damager.sendMessage("That animal belongs to " + oa.getOwnerName());
             return;
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onEntityTamed(EntityTameEvent event) {
         if (!plugin.getOwnedAnimals().containsKey(event.getEntity().getUniqueId())) {
             if (plugin.getClaimableAnimals().containsKey(event.getEntityType().getTypeId())) {
                 if (plugin.getConfig().getBoolean("auto-claim-on-tame", true)) {
                     Player owner = plugin.getServer().getPlayerExact(event.getOwner().getName());
                     List<UUID> alreadyOwned = plugin.getOwnedAnimalIDs(owner.getName());
                     if (alreadyOwned.size() >= plugin.getPlayerClaimLimit(owner)) {
                         owner.sendMessage("You aren't allowed to own any more animals.");
                         return;
                     }
                     ClaimableAnimal ca = plugin.getClaimableAnimals().get(event.getEntity().getType().getTypeId());
                     event.setCancelled(true);
                     if (ca.takeCost(owner)) {
                         OwnedAnimal oa = new OwnedAnimal(plugin, event.getEntity().getUniqueId(), owner.getName());
                         oa.setEntityType(event.getEntity().getType());
                         plugin.saveOwnedAnimal(oa);
                         owner.sendMessage("This " + oa.getEntityType().name() + " now belongs to you.");
                     } else {
                         owner.sendMessage("Sorry, you don't have " + ca.getCostDescription());
                     }
                 }
             }
         }
     }
 
 
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         checkExpiry(event.getRightClicked());
         Player player = event.getPlayer();
         Entity animal = event.getRightClicked();
         if (plugin.getOwnedAnimals().containsKey(animal.getUniqueId())) {
             // This animal is owned, check for permission.
             OwnedAnimal oa = plugin.getOwnedAnimal(animal.getUniqueId());
             if (player.hasMetadata("livestocklock-abandon-pending")) {
                 player.removeMetadata("livestocklock-abandon-pending", plugin);
                 if (oa.getOwnerName().equals(player.getName()) || player.hasPermission("livestocklock.claimforothers")) {
                     player.sendMessage("This animal has been abandoned.");
                     plugin.removeOwnedAnimal(oa);
                     return;
                 } else {
                     player.sendMessage("This animal doesn't belong to you!");
                     return;
                 }
             } else if (!oa.allowAccess(player.getName())) {
                 player.sendMessage("Sorry, that animal belongs to " + oa.getOwnerName());
                 Player owner = plugin.getServer().getPlayerExact(oa.getOwnerName());
                 if (owner != null && owner.isOnline()) {
                     owner.sendMessage(player.getName() + " is trying to use your animal at " + formatLoc(player.getLocation()));
                 }
                 event.setCancelled(true);
                 return;
             }
             oa.setOwnerActivityTime(System.currentTimeMillis());
             plugin.saveOwnedAnimal(oa);
         } else if (player.hasMetadata("livestocklock-claim-pending") ||
                (plugin.getConfig().getBoolean("auto-claim-on-lead", true) && event.getPlayer().getItemInHand().equals(Material.LEASH))) {
             String ownerName = player.getName();
             if (player.hasMetadata("livestocklock-claim-pending")) {
                 ownerName = player.getMetadata("livestocklock-claim-pending").get(0).asString();
                 player.removeMetadata("livestocklock-claim-pending", plugin);
             }
             if (plugin.getClaimableAnimals().containsKey(animal.getType().getTypeId())) {
                 if (player.hasPermission("livestocklock.claim." + animal.getType().getTypeId())) {
                     if (animal instanceof Tameable && !((Tameable) animal).isTamed()) {
                         if (plugin.getConfig().getBoolean("require-taming", true)) {
                             player.sendMessage("You can't claim a wild animal.");
                             event.setCancelled(true);
                             return;
                         }
                     } else if ((animal instanceof Ageable) && !((Ageable) animal).isAdult()) {
                         if (plugin.getConfig().getBoolean("require-adulthood", true)) {
                             player.sendMessage("You can't claim a baby animal.");
                             event.setCancelled(true);
                             return;
                         }
                     }
                     ClaimableAnimal ca = plugin.getClaimableAnimals().get(animal.getType().getTypeId());
 
                     if (event.getPlayer().getItemInHand().getType() == Material.LEASH && plugin.getConfig().getBoolean("auto-claim-on-lead", true)) {
                         // We dont cancel the lead event.\
                     } else {
                         event.setCancelled(true);
                     }
 
                     if (ca.takeCost(player)) {
                         OwnedAnimal oa = new OwnedAnimal(plugin, animal.getUniqueId(), ownerName);
                         oa.setEntityType(animal.getType());
                         plugin.saveOwnedAnimal(oa);
                         player.sendMessage("This " + oa.getEntityType().name() + " now belongs to you.");
                     } else {
                         player.sendMessage("Sorry, you don't have " + ca.getCostDescription());
                     }
                 }
             }
         }
     }
 
     public static String formatLoc(Location l) {
         return l.getWorld().getName() + ": " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ();
     }
 }
