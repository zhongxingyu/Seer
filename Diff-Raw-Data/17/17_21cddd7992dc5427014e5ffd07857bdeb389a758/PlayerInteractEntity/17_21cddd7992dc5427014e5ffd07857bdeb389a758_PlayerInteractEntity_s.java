 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * Description.
  * @author UndeadScythes
  */
 public class PlayerInteractEntity extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(PlayerInteractEntityEvent event) {
         if(event.getRightClicked() instanceof Tameable) {
             final Tameable pet = (Tameable) event.getRightClicked();
             if(pet.isTamed()) {
                 String ownerName = "";
                 if(pet instanceof Wolf) {
                     ownerName = ((Wolf)pet).getOwner().getName();
                 }
                 if(pet instanceof Ocelot) {
                     ownerName = ((Ocelot)pet).getOwner().getName();
                 }
                 if(!ownerName.equals(event.getPlayer().getName())) {
                     event.getPlayer().sendMessage(Color.MESSAGE + "This animal belongs to " + UDSPlugin.getPlayers().get(ownerName).getDisplayName());
                 } else if(event.getPlayer().isSneaking()){
                     UDSPlugin.getPlayers().get(event.getPlayer().getName()).selectPet(event.getRightClicked().getUniqueId());
                     event.getPlayer().sendMessage(Color.MESSAGE + "Pet selected.");
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
