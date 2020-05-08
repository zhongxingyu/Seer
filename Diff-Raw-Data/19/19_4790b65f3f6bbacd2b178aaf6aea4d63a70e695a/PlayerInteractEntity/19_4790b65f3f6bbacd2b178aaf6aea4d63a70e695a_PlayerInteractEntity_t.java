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
         if(event.getRightClicked() instanceof Tameable) {
            final Tameable pet = (Tameable)event.getRightClicked();
             if(pet.isTamed()) {
                final String ownerName = pet.getOwner().getName();
                if(ownerName.equals(event.getPlayer().getName()) && event.getPlayer().isSneaking()) {
                     UDSPlugin.getPlayers().get(event.getPlayer().getName()).selectPet(event.getRightClicked().getUniqueId());
                     event.getPlayer().sendMessage(Color.MESSAGE + "Pet selected.");
                     event.setCancelled(true);
                } else {
                     event.getPlayer().sendMessage(Color.MESSAGE + "This animal belongs to " + UDSPlugin.getPlayers().get(ownerName).getNick());
                 }
             }
         } else if(event.getRightClicked() instanceof ItemFrame) {
             final SaveablePlayer player = UDSPlugin.getPlayers().get(event.getPlayer().getName());
             if(!(player.canBuildHere(event.getRightClicked().getLocation()))) {
                 event.setCancelled(true);
                 player.sendMessage(Message.CANT_BUILD_HERE);
             }
         }
     }
 }
