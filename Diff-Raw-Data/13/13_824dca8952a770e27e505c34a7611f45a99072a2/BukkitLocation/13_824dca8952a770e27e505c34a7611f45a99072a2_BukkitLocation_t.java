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
 
 import org.bukkit.Bukkit;
 
 import com.andune.minecraft.commonlib.server.api.Block;
 import com.andune.minecraft.commonlib.server.api.Effect;
 import com.andune.minecraft.commonlib.server.api.Location;
 import com.andune.minecraft.commonlib.server.api.Sound;
 import com.andune.minecraft.commonlib.server.api.World;
 import com.andune.minecraft.commonlib.server.api.impl.LocationAbstractImpl;
 
 /**
  * @author andune
  *
  */
 public class BukkitLocation extends LocationAbstractImpl implements Location {
     private final org.bukkit.Location bukkitLocation;
 
     public BukkitLocation(org.bukkit.Location bukkitLocation) {
         this.bukkitLocation = bukkitLocation;
     }
     
     public BukkitLocation(Location l) {
         org.bukkit.World w = Bukkit.getWorld(l.getWorld().getName());
         bukkitLocation = new org.bukkit.Location(w, l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
     }
     
     public BukkitLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
         org.bukkit.World w = Bukkit.getWorld(worldName);
         bukkitLocation = new org.bukkit.Location(w, x, y, z, yaw, pitch);
     }
     
     public org.bukkit.Location getBukkitLocation() {
         return bukkitLocation;
     }
     
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getBlock()
      */
     @Override
     public Block getBlock() {
         return new BukkitBlock(this);
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#setX(double)
      */
     @Override
     public void setX(double x) {
         bukkitLocation.setX(x);
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getX()
      */
     @Override
     public double getX() {
         return bukkitLocation.getX();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getBlockX()
      */
     @Override
     public int getBlockX() {
         return bukkitLocation.getBlockX();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#setY(double)
      */
     @Override
     public void setY(double y) {
         bukkitLocation.setY(y);
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getY()
      */
     @Override
     public double getY() {
         return bukkitLocation.getY();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getBlockY()
      */
     @Override
     public int getBlockY() {
         return bukkitLocation.getBlockY();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#setZ(double)
      */
     @Override
     public void setZ(double z) {
         bukkitLocation.setZ(z);
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getZ()
      */
     @Override
     public double getZ() {
         return bukkitLocation.getZ();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getBlockZ()
      */
     @Override
     public int getBlockZ() {
         return bukkitLocation.getBlockZ();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#setYaw(float)
      */
     @Override
     public void setYaw(float yaw) {
         bukkitLocation.setYaw(yaw);
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getYaw()
      */
     @Override
     public float getYaw() {
         return bukkitLocation.getYaw();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#setPitch(float)
      */
     @Override
     public void setPitch(float pitch) {
         bukkitLocation.setPitch(pitch);
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#getPitch()
      */
     @Override
     public float getPitch() {
         return bukkitLocation.getPitch();
     }
 
     /* (non-Javadoc)
      * @see com.andune.minecraft.hsp.server.api.Location#distance(com.andune.minecraft.hsp.server.api.Location)
      */
     @Override
     public double distance(Location o) {
         // we can only compare distance to other BukkitLocation objects.
         if( !(o instanceof BukkitLocation) )
             throw new IllegalArgumentException("invalid object class: "+o);
             
         return bukkitLocation.distance( ((BukkitLocation) o).bukkitLocation );
     }
 
     @Override
     public World getWorld() {
         return new BukkitWorld(bukkitLocation.getWorld());
     }
 
     @Override
     public void playEffect(Effect effect, int data) {
         // lightning is handled special, there's no Bukkit visual lightning
         // effect, we have to trigger lightning through a direct call
         if( effect == Effect.LIGHTNING ) {
             getBukkitLocation().getWorld().strikeLightningEffect(getBukkitLocation());
         }
         else {
             org.bukkit.World w = getBukkitLocation().getWorld();
             org.bukkit.Effect bukkitEffect = BukkitEffect.getBukkitEffect(effect);
             w.playEffect(getBukkitLocation(), bukkitEffect, 0);
         }
     }
     
     @Override
     public void playSound(Sound sound, float volume, float pitch) {
         org.bukkit.World w = getBukkitLocation().getWorld();
         org.bukkit.Sound bukkitSound = BukkitSound.getBukkitSound(sound);
         w.playSound(getBukkitLocation(), bukkitSound, volume, pitch);
     }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof BukkitLocation)
            return bukkitLocation.equals(((BukkitLocation) o).bukkitLocation);
        else
            return false;
    }

    public int hashCode() {
        return bukkitLocation.hashCode();
    }
 }
