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
 package com.andune.liftsign;
 
 import com.andune.minecraft.commonlib.Logger;
 import com.google.inject.assistedinject.Assisted;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 
 import javax.annotation.Nullable;
 import javax.inject.Inject;
 
 /**
  * Object to track details about a known sign. Details are stored so they
  * only have to be processed once.
  *
  * @author andune
  */
 public class SignDetail {
     private final SignCache cache;
     private final Logger log;
     private final Util util;
 
     private final Location location;
 
     // world, x y, and z are cached internally for efficient vertical processing
     private final World world;
     private final int x;
     private final int y;
     private final int z;
 
     private boolean isLiftSign;
     private boolean isLiftUp = false;
     private boolean isLiftDown = false;
     private SignDetail targetLift;
 
     /**
      * Create a new SignDetail object.
      *
      * @param cache
      * @param log
      * @param util
      * @param sign  the Sign backing this SignDetail object
      * @param lines intended to be used when called from SignChangeEvent, so
      *              the new lines can be passed in even though they aren't part of the block
      *              state yet. Leave null if you want this method to just use the lines from
      *              the Sign state object.
      */
     @Inject
     public SignDetail(final SignCache cache, final Logger log, final Util util,
                       @Assisted final Sign sign, @Assisted @Nullable final String[] inputLines) {
         this.cache = cache;
         this.log = log;
         this.util = util;
 
         this.location = sign.getLocation();
         this.world = this.location.getWorld();
         this.x = this.location.getBlockX();
         this.y = this.location.getBlockY();
         this.z = this.location.getBlockZ();
 
         String[] lines = inputLines;
         // fill lines from Sign if it is null
         if (lines == null)
             lines = sign.getLines();
 
         isLiftSign = false;
         log.debug("new SignDetail object. lines={}", lines);
         if (lines != null && lines.length > 1) {
             log.debug("lines[1]=", lines[1]);
             if (lines[1].equalsIgnoreCase("[lift up]")) {
                 log.debug("Sign is lift up");
                 isLiftUp = true;
                 isLiftSign = true;
            }
            else if (lines[1].equalsIgnoreCase("[lift down]")) {
                 log.debug("Sign is lift up");
                 isLiftDown = true;
                 isLiftSign = true;
            }
            else if (lines[1].equalsIgnoreCase("[lift]")) {
                 log.debug("Sign is lift target");
                 isLiftSign = true;
            }
            else {
                 log.debug("Sign is not a lift sign");
             }
         }
     }
 
     /**
      * Check a given sign to see if it's a possible target match for this
      * sign. To be a possible match, the given sign must be on the same
      * world and at the same x/z as this sign, as well as vertically above
      * or below depending on the sign direction.
      * <p/>
      * Note a return of true does NOT mean the given sign is exactly where
      * this sign would send a player; it's possible there is another sign
      * vertically in between the two. This just means that, in the absence
      * of another sign in between, the given sign is a POSSIBLE target for
      * this current sign.
      *
      * @param sign
      * @return
      */
     public boolean isPossibleTargetMatch(SignDetail sign) {
         // if this current sign is not a lift sign, then it's impossible
         // for it to target the given sign.
         if (!isLiftSign) {
             log.debug("isPossibleTargetMatch(): sign is not a lift sign");
             return false;
         }
 
         // we can only target other lift signs.
         if (!sign.isLiftSign()) {
             log.debug("isPossibleTargetMatch(): target sign is not a lift sign");
             return false;
         }
 
         Location signLocation = sign.getLocation();
         if (!signLocation.getWorld().equals(world) ||
                 signLocation.getBlockX() != x ||
                 signLocation.getBlockZ() != z) {
             log.debug("isPossibleTargetMatch(): world, x or z axis not a match");
             return false;
         }
 
         // if the sign is appropriately above or below us, then it is
         // a possible target
         if (isLiftUp && signLocation.getBlockY() > y) {
             return true;
        }
        else if (isLiftDown && signLocation.getBlockY() < y) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Return the target lift of this sign, if any (can be null).
      *
      * @return
      */
     public SignDetail getTargetLift() {
         if (!isLiftSign) {
             log.debug("getTargetLift(): sign is not a lift sign");
             return null;
         }
 
         if (!isLiftUp && !isLiftDown) {
             log.debug("getTargetLift(): sign is a destination lift sign; has no target");
             return null;
         }
 
         if (targetLift == null) {
             log.debug("getTargetLift(): targetLift is null, looking for new target");
             Block b = getLocation().getBlock();
             BlockFace face = BlockFace.UP;
             int max = world.getMaxHeight() - 1;
             if (!isLiftUp) {
                 face = BlockFace.DOWN;
                 max = 1;
             }
             log.debug("getTargetLift(): isLiftUp=", isLiftUp, ", face=", face, ", max=", max);
 
             Block next = b;
             // loop up or down looking for the first possible target sign.
             // if/when we find it, store that as our target and break loop
             for (int i = y; i != max; ) {
                 next = next.getRelative(face);
 
                 Sign sign = util.getSignState(next);
                 if (sign != null) {
                     SignDetail detail = cache.getCachedSignDetail(sign.getLocation());
 
                     // if no cached object exists for this sign, then create one
                     if (detail == null)
                         detail = cache.newSignCreated(sign);
 
                     if (isPossibleTargetMatch(detail)) {
                         targetLift = detail;
                         break;
                     }
                 }
 
                 // increment/decrement depending on direction
                 if (isLiftUp)
                     i++;
                 else
                     i--;
             }
         }
 
         return targetLift;
     }
 
     /**
      * Called for each cached SignDetail object when a Lift Sign has
      * been created or destroyed. It should invalidate any cached target
      * on the same vertical path so that the up/down locations are
      * recalculated on next use.
      *
      * @param signDetail
      */
     public void clearCache(SignDetail signDetail) {
         // don't do anything unless both signs are lift signs
         if (!(isLiftSign && signDetail.isLiftSign))
             return;
 
         // are we in the same vertical location as given sign? if yes, reset cache object
         if (signDetail.world == world &&
                 signDetail.x == x &&
                 signDetail.z == z) {
             targetLift = null;
         }
     }
 
     public World getWorld() {
         return world;
     }
 
     public boolean isLiftSign() {
         return isLiftSign;
     }
 
     public Location getLocation() {
         return location;
     }
 }
