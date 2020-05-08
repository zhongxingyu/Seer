 package com.tyzoid.jailr.events;
 
 import com.tyzoid.jailr.api.JailAPI;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Class that implements event handlers that prevent interaction
  * from players who are frozen.
  *
  * @author Sushi
  */
 public class FrozenListener implements Listener {
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         Player player = event.getPlayer();

         if (JailAPI.isFrozen(player.getName())) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (to.getX() == from.getX() &&
                    to.getZ() == from.getZ() &&
                    to.getY() == from.getY() &&
                    to.getWorld().getName().equals(from.getWorld().getName())) return;

            event.setCancelled(true);
         }
     }
 }
