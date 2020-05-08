 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 /**
  * When an entity is damaged.
  * @author UndeadScythes
  */
 public class EntityDamage extends ListenerWrapper implements Listener {
     @EventHandler
     public final void onEvent(final EntityDamageEvent event) {
         if((event.getEntity() instanceof Player)) {
             final SaveablePlayer player = UDSPlugin.getOnlinePlayers().get(((Player)event.getEntity()).getName());
             if(player.hasGodMode() || player.isAfk() || (event.getCause().equals(DamageCause.DROWNING) && player.hasScuba())) {
                 event.setCancelled(true);
             }
            if(((Damageable)event.getEntity()).getHealth() < event.getDamage()) {
                 for(Region arena : UDSPlugin.getRegions(RegionType.ARENA).values()) {
                     if(arena.contains(player.getLocation())) {
                         event.setCancelled(true);
                         player.teleport(arena.getWarp());
                         player.setHealth(player.getMaxHealth());
                     }
                 }
                 if(player.isDuelling()) {
                     challengeLoss(player);
                 }
             }
         } else if(UDSPlugin.getPassiveMobs().contains(event.getEntity().getType()) && !hasFlag(event.getEntity().getLocation(), RegionFlag.PVE)) {
             event.setCancelled(true);
         }
     }
     
     private void challengeLoss(final SaveablePlayer loser) {
         final SaveablePlayer winner = loser.getChallenger();
         winner.credit(2 * winner.getWager());
         winner.endChallenge();
         winner.sendMessage(Color.MESSAGE + "You won the challenge.");
         winner.setHealth(winner.getMaxHealth());
         loser.endChallenge();
         loser.sendMessage(Color.MESSAGE + "You lost the challenge.");
         loser.setHealth(loser.getMaxHealth());
     }
 }
