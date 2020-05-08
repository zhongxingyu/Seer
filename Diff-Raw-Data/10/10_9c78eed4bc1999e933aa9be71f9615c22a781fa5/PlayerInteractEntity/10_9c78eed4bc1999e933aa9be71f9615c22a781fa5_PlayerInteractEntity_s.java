 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * When a player interacts with an entity.
  * @author UndeadScythes
  */
 public class PlayerInteractEntity extends ListenerWrapper implements Listener {
     @EventHandler
     public final void onEvent(final PlayerInteractEntityEvent event) {
         final Entity entity = event.getRightClicked();
         if(entity instanceof Tameable) {
             final Tameable pet = (Tameable)entity;
             if(pet.isTamed()) {
                 final String ownerName = pet.getOwner().getName();
                if(ownerName.equals(event.getPlayer().getName()) && event.getPlayer().isSneaking()) {
                    UDSPlugin.getPlayers().get(event.getPlayer().getName()).selectPet(entity.getUniqueId());
                    event.getPlayer().sendMessage(Color.MESSAGE + "Pet selected.");
                    event.setCancelled(true);
                 } else {
                     event.getPlayer().sendMessage(Color.MESSAGE + "This animal belongs to " + UDSPlugin.getPlayers().get(ownerName).getNick());
                     event.setCancelled(true);
                 }
             }
         } else if(entity instanceof ItemFrame) {
             final SaveablePlayer player = UDSPlugin.getPlayers().get(event.getPlayer().getName());
             if(!(player.canBuildHere(entity.getLocation()))) {
                 event.setCancelled(true);
                 player.sendMessage(Message.CANT_BUILD_HERE);
             }
         }
     }
 }
