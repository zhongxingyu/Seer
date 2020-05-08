 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.material.*;
 import org.bukkit.util.*;
 
 /**
  * Lets a player 'sit' on stair blocks.
  * @author UndeadScythes
  */
 public class SitCmd extends PlayerCommandExecutor {
     /**
      * @inheritDocs
      */
     @Override
     public void playerExecute(SaveablePlayer player, String[] args) {
         if(!player.isInsideVehicle()) {
             final Block target = player.getTargetBlock(null, 3);
             final boolean a = target.getTypeId() == 53;
             final boolean b = target.getTypeId() == 67;
             final boolean c = target.getTypeId() == 108;
             final boolean d = target.getTypeId() == 109;
             final boolean e = target.getTypeId() == 114;
             final boolean f = target.getTypeId() == 128;
             final boolean g = target.getRelative(BlockFace.DOWN).getTypeId() != 0;
             if((a || b || c || d || e || f) && g) {
                 final Item seat = player.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(Material.SNOW_BALL));
                 seat.setPickupDelay(2147483647);
                seat.teleport(target.getLocation().add(0.5, 0.2, 0.5));
                 seat.setVelocity(new Vector(0, 0, 0));
                 final Stairs chair = (Stairs)target.getState().getData();
                 Location view = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                 if(chair.getDescendingDirection() == BlockFace.NORTH) {
                     view.setYaw(180);
                } else if(chair.getDescendingDirection() == BlockFace.EAST) {
                     view.setYaw(270);
                } else if(chair.getDescendingDirection() == BlockFace.SOUTH) {
                     view.setYaw(0);
                } else {
                    view.setYaw(90);
                 }
                 player.teleport(view);
                 seat.setPassenger(player);
             }
         } else {
             final Entity seat = player.getVehicle();
             seat.eject();
             seat.remove();
             final Stairs chair = (Stairs)player.getWorld().getBlockAt(player.getLocation()).getState().getData();
             if(chair.getDescendingDirection() == BlockFace.NORTH) {
                 player.teleport(player.getLocation().add(-1, 0, 0));
             } else if(chair.getDescendingDirection() == BlockFace.EAST) {
                 player.teleport(player.getLocation().add(0, 0, -1));
             } else if(chair.getDescendingDirection() == BlockFace.SOUTH) {
                 player.teleport(player.getLocation().add(1, 0, 0));
             } else {
                 player.teleport(player.getLocation().add(0, 0, 1));
             }
         }
     }
 }
