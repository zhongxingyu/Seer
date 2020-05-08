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
        	if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())
			{
				Location loc = event.getFrom();
				loc.setPitch(event.getTo().getPitch());
				loc.setYaw(event.getTo().getYaw());
				event.getPlayer().teleport(loc);
			}
         }
     }
 }
