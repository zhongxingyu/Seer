 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 
 /**
  * When a piston retracts.
  * @author UndeadScythes
  */
 public class BlockPistonRetract extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(final BlockPistonRetractEvent event) {
         if(event.isSticky()) {
             final Location pistonLocation = event.getBlock().getLocation();
             final Location blockLocation = event.getRetractLocation();
             final List<Region> blockRegions = regionsHere(blockLocation);
             if(!blockRegions.isEmpty()) {
                 boolean mixedRegions = false;
                 boolean totallyEnclosed = false;
                 final List<Region> pistonRegions = regionsHere(pistonLocation);
                 if(pistonRegions.isEmpty()) {
                     mixedRegions = true;
                 } else {
                     for(Region blockRegion : blockRegions) {
                         for(Region pistonRegion : pistonRegions) {
                             if(blockRegion.equals(pistonRegion)) {
                                 totallyEnclosed = true;
                             } else {
                                 mixedRegions = true;
                             }
                             if(mixedRegions && totallyEnclosed) {
                                 break;
                             }
                         }
                         if(mixedRegions && totallyEnclosed) {
                             break;
                         }
                     }
                 }
                 if(mixedRegions && !totallyEnclosed) {
                     switch(event.getDirection()) {
                         case DOWN:
                             event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 0, true);
                             break;
                         case UP:
                             event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 1, true);
                             break;
                         case NORTH: // Acually is East
                             event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 4, true);
                             break;
                        case SOUTH: // Acually is West
                             event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 5, true);
                             break;
                        case WEST: // Acually is North
                             event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 3, true);
                             break;
                        case EAST: // Acually is South
                            event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 2, true);
                            break;
                     }
                     event.getBlock().getRelative(event.getDirection()).setTypeId(0);
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
