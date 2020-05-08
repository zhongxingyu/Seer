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
 package com.andune.minecraft.commonlib.server.bukkit;
 
 import java.util.Random;
 
 import javax.inject.Singleton;
 
 import com.andune.minecraft.commonlib.Logger;
 import com.andune.minecraft.commonlib.LoggerFactory;
 import com.andune.minecraft.commonlib.server.api.Factory;
 import com.andune.minecraft.commonlib.server.api.Location;
 import com.andune.minecraft.commonlib.server.api.Player;
 import com.andune.minecraft.commonlib.server.api.Teleport;
 import com.andune.minecraft.commonlib.server.api.TeleportOptions;
 import com.google.inject.Inject;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * @author andune
  *
  */
 @Singleton
 public class BukkitTeleport implements Teleport {
     private static final Logger log = LoggerFactory.getLogger(BukkitTeleport.class);
     
     private final Random random = new Random(System.currentTimeMillis());
     private com.andune.minecraft.commonlib.Teleport teleportUtil;
     private Factory factory;
     
     @Inject
     public BukkitTeleport(com.andune.minecraft.commonlib.Teleport teleportUtil, Factory factory) {
         this.teleportUtil = teleportUtil;
         this.factory = factory;
     }
 
     @Override
     public Location safeLocation(Location location) {
         org.bukkit.Location safeLoc = teleportUtil.safeLocation(((BukkitLocation) location).getBukkitLocation());
        return new BukkitLocation(safeLoc);
     }
 
     @Override
     public Location safeLocation(Location location, TeleportOptions options) {
         org.bukkit.Location safeLoc = teleportUtil.safeLocation(((BukkitLocation) location).getBukkitLocation(),
                 getBounds(options), getFlags(options));
        return new BukkitLocation(safeLoc);
     }
 
     @Override
     public void safeTeleport(Player player, Location location) {
         ((BukkitPlayer) player).teleport(safeLocation(location));
     }
 
     @Override
     public void teleport(Player player, Location location, TeleportOptions options) {
         ((BukkitPlayer) player).teleport(safeLocation(location, options));
     }
 
     /**
      * Convert TeleportOptions into the Bounds object used by the Teleport util class.
      * 
      * @param options
      * @return
      */
     private com.andune.minecraft.commonlib.Teleport.Bounds getBounds(TeleportOptions options) {
         com.andune.minecraft.commonlib.Teleport.Bounds bounds = new com.andune.minecraft.commonlib.Teleport.Bounds();
         if( options != null ) {
             bounds.minY = options.getMinY();
             bounds.maxY = options.getMaxY();
             bounds.maxRange = options.getMaxRange();
         }
         return bounds;
     }
 
     /**
      * Convert TeleportOptions into flags used by the Teleport util class.
      * @param options
      * @return
      */
     private int getFlags(TeleportOptions options) {
         int flags = 0;
 
         if( options != null ) {
             if( options.isNoTeleportOverIce() )
                 flags |= com.andune.minecraft.commonlib.Teleport.FLAG_NO_ICE;
             if( options.isNoTeleportOverLeaves() )
                 flags |= com.andune.minecraft.commonlib.Teleport.FLAG_NO_LEAVES;
             if( options.isNoTeleportOverLilyPad() )
                 flags |= com.andune.minecraft.commonlib.Teleport.FLAG_NO_LILY_PAD;
             if( options.isNoTeleportOverWater() )
                 flags |= com.andune.minecraft.commonlib.Teleport.FLAG_NO_WATER;
         }
         
         return flags;
     }
 
     /** Given a min and max (that define a square cube "region"), randomly pick
      * a location in between them, and then find a "safe spawn" point based on
      * that location (ie. that won't suffocate or be right above lava, etc).
      * 
      * @param min
      * @param max
      * @return the random safe Location, or null if one couldn't be located
      */
     @Override
     public Location findRandomSafeLocation(Location min, Location max, TeleportOptions options) {
         if( min == null || max == null )
             return null;
 
         checkNotNull(min.getWorld());
         checkNotNull(max.getWorld());
 
         if( !min.getWorld().getName().equals(max.getWorld().getName()) ) {
             log.warn("Attempted to find random location between two different worlds: {}, {}", min.getWorld(), max.getWorld());
             return null;
         }
         
         log.debug("findRandomSafeLocation(): min: {}, max: {}", min, max);
 
         int minY = min.getBlockY();
         if( minY < options.getMinY() )
             minY = options.getMinY();
         int maxY = max.getBlockY();
         if( maxY > options.getMaxY() )
             maxY = options.getMaxY();
 
         int x = randomDeltaInt(min.getBlockX(), max.getBlockX());
         int y = randomDeltaInt(minY, maxY);
         int z = randomDeltaInt(min.getBlockZ(), max.getBlockZ());
         
         Location newLoc = factory.newLocation(min.getWorld().getName(), x, y, z, 0, 0);
         log.debug("findRandomSafeLocation(): newLoc={}",newLoc);
         Location safeLoc = safeLocation(newLoc, options);
         log.debug("findRandomSafeLocation(): safeLoc={}",safeLoc);
 
         return safeLoc;
     }
 
     /** Given two equal integer values, figure out their "distance delta".
      * For example, assume two Locations A and B where we're trying to find
      * the distanceDelta between A.x and B.x. Here are examples:
      * 
      *   A.x: -550, B.x: -570 = 20
      *   A.x: 550, B.x: 570 = 20
      *   A.x: -50, B.x: 50 = 100
      *   
      * @param i
      * @param j
      * @return
      */
     private int getDistanceDelta(int i, int j) {
         int highest = i;
         int lowest = j;
         // swap them if it's wrong
         if( lowest > highest ) {
             highest = j;
             lowest = i;
         }
         
         // if both are < 0, swap sign and subtract lowest from highest
         // (since with swapped sign, the lowest/highest will be reversed)
         if( highest < 0 && lowest < 0 )
             return Math.abs(lowest) - Math.abs(highest);
         else
             return highest - lowest;
     }
     
     /** Given two integers representing a location component (such as x, y or z),
      * pick a random number that falls between them.
      * 
      * @param i
      * @param j
      * @return
      */
     private int randomDeltaInt(int i, int j) {
         int result = 0;
         int delta = getDistanceDelta(i, j);
         log.debug("randomDeltaInt(): i={}, j={}, delta={}", i, j, delta);
         if( delta == 0 )
             return i;
         
         int r = random.nextInt(delta);
         if( i < j )
             result = i + r;
         else
             result = j + r;
         
         log.debug("randomDeltaInt(): i={}, j={}, delta={}, r={}, result={}", i, j, delta, r, result);
         return result;
     }
 }
