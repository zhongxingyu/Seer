 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 
 /**
  * Description.
  * @author UndeadScythes
  */
 public class BlockPistonRetract extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(BlockPistonRetractEvent event) {
         Location piston = event.getBlock().getLocation();
         Location block = event.getRetractLocation();
         ArrayList<Region> testRegions1 = regionsHere(block);
         if(!testRegions1.isEmpty()) {
             boolean mixedRegions = false;
             boolean totallyEnclosed = false;
             for(Region i : testRegions1) {
                 ArrayList<Region> testRegions2 = regionsHere(piston);
                 if(!testRegions2.isEmpty()) {
                     for(Region j : testRegions2) {
                         if(!i.equals(j)) {
                             mixedRegions = true;
                         }
                         if(i.equals(j)) {
                             totallyEnclosed = true;
                         }
                     }
                 }
                 else {
                     mixedRegions = true;
                 }
             }
             if(mixedRegions && !totallyEnclosed) {
                 if(event.isSticky()) {
                     if(event.getDirection() == BlockFace.DOWN) {
                         event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 0, true);
                     }
                     if(event.getDirection() == BlockFace.UP) {
                         event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 1, true);
                     }
                     if(event.getDirection() == BlockFace.NORTH) {
                        event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 2, true);
                     }
                     if(event.getDirection() == BlockFace.SOUTH) {
                        event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 3, true);
                     }
                     if(event.getDirection() == BlockFace.WEST) {
                        event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 4, true);
                     }
                     if(event.getDirection() == BlockFace.EAST) {
                        event.getBlock().setTypeIdAndData(Material.PISTON_STICKY_BASE.getId(), (byte) 5, true);
                     }
                     event.getBlock().getRelative(event.getDirection()).setTypeId(0);
                 }
                 event.setCancelled(true);
             }
         }
     }
 }
