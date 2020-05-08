 package com.gmail.firecopy1.shield;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 public class DamageListener implements Listener {
 
     private final Shield plugin;
 
     public DamageListener(Shield instance) {
         this.plugin = instance;
     }
 
     @EventHandler
     public void whenDamaged(EntityDamageByEntityEvent event) {
        final int shieldID = 34;

         Entity damaged = event.getEntity();
         Entity damager = event.getDamager();
 
        //If a player is attacking,
         if (damager instanceof Player) {
             Player PlayerDamager = (Player) damager;
             //and they have permission to use the shield,
             if (PlayerDamager.hasPermission("shield.use")) {
                 //and it is in their hand,
                 if (PlayerDamager.getItemInHand().getTypeId() == shieldID) {
                     //they do no damage.
                     event.setDamage(0);
                 }
             }
         }
 
         //If a player is attacked,
         if (damaged instanceof Player) {
             Player player = (Player) damaged;
             boolean sendMessages = !plugin.disabledPlayers.contains(player);
 
             //and they have permission to use the shield,
             if (player.hasPermission("shield.use")) {
                 //and they are holding the shield,
                 if (player.getItemInHand().getTypeId() == shieldID) {
                     int currentdurability = player.getItemInHand().getDurability();
                     int DAMAGE_BLOCKED = plugin.damageBlocked;
                     int maxdurability = plugin.durability;
                     int newdurability = currentdurability + event.getDamage();
                     String someDamage = Shield.prefix + Shield.yellow + plugin.messageSomeDamage;
                     String noDamage = Shield.prefix + Shield.green + plugin.messageNoDamage;
 
                     if (newdurability < maxdurability) {
                         event.setDamage(0);
                         if (sendMessages) {
                             player.sendMessage(noDamage);
                         }
                     } else if (newdurability == maxdurability) {
                         event.setDamage(0);
                         player.setItemInHand(null);
                         if (sendMessages) {
                             player.sendMessage(noDamage);
                         }
                     } else if (newdurability > maxdurability) {
                         event.setDamage(newdurability - maxdurability);
                         player.setItemInHand(null);
                         if (sendMessages) {
                             player.sendMessage(someDamage);
                         }
                     }
                 }
             } else {
                 if (sendMessages) {
                     player.sendMessage("Your shield is useless, if you don't have permission!");
                 }
             }
         }
     }
}
