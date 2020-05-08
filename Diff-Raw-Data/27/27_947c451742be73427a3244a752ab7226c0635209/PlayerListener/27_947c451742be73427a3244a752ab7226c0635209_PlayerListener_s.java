 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 /**
  *
  */
 package com.andune.liftsign.listener;
 
 import com.andune.liftsign.*;
 import com.andune.minecraft.commonlib.Logger;
 import com.andune.minecraft.commonlib.Teleport;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import javax.inject.Inject;
 
 /**
  * @author andune
  */
 public class PlayerListener implements Listener {
     private final SignCache cache;
     private final PermissionCheck perm;
     private final Teleport teleport;
     private final Logger log;
     private final Util util;
     private final SignFactory factory;
 
     @Inject
     public PlayerListener(SignCache cache, SignFactory factory, PermissionCheck perm,
                           Teleport teleport, Logger log, Util util) {
         this.cache = cache;
         this.factory = factory;
         this.perm = perm;
         this.teleport = teleport;
         this.log = log;
         this.util = util;
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
             return;
 
         Sign sign = util.getSignState(event.getClickedBlock());
         if (sign != null) {
             final Player p = event.getPlayer();
 
             log.debug("Sign right clicked");
             SignDetail signDetail = cache.getCachedSignDetail(sign.getLocation());
             if (signDetail == null) {
                 log.debug("Instantiating SignDetail object");
                 signDetail = factory.create(sign, null);
             }
 
             // if the sign is not a lift sign, return now, don't do any further
             // processing.
             if (!signDetail.isLiftSign()) {
                 return;
             }
 
             SignDetail targetLift = null;
             targetLift = signDetail.getTargetLift();
 
             Sign targetSign = null;
             World world = null;
             if (targetLift != null) {
                 world = targetLift.getWorld();
                 Block signBlock = world.getBlockAt(targetLift.getLocation());
                 targetSign = util.getSignState(signBlock);
             }
 
             if (targetSign != null) {
                 // abort if player doesn't have permission
                 if (!perm.canUseNormalLift(p)) {
                     util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_NO_PERMISSION);
                     return;
                 }
                 log.debug("has permission");
 
                 // check to make sure targetblock is safe
                 Location playerLocation = event.getPlayer().getLocation();
                 Location finalLocation = null;
                 for (int y = targetSign.getY(); y >= targetSign.getY() - 1; y--) {
                     Location newLocation = new Location(playerLocation.getWorld(), playerLocation.getX(),
                             y, playerLocation.getZ(), playerLocation.getYaw(),
                             playerLocation.getPitch());
                     Block testBlock = world.getBlockAt(newLocation);
                     if (teleport.isSafeBlock(testBlock, 0))
                         finalLocation = newLocation;
                 }
 
                 if (finalLocation != null) {
                     event.getPlayer().teleport(finalLocation);
                     if (finalLocation.getBlockY() > playerLocation.getBlockY())
                         util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_UP_ONE_FLOOR);
                     else
                         util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_DOWN_ONE_FLOOR);
 
                     // cancel event so as not to place blocks accidentally
                     event.setCancelled(true);
                } else {
                     util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_DESTINATION_NOT_SAFE);
                 }
            } else {
                 log.debug("no target");
                 util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_NO_VALID_LIFT_TARGET);
             }
         }
     }
 }
