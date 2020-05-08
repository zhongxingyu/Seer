 
 package com.quartercode.minecartrevolution.util;
 
 import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
 
 public enum MinecartType {
 
     MINECART (Minecart.class), STORAGE (StorageMinecart.class), POWERED (PoweredMinecart.class);
 
     public static MinecartType getType(final Minecart minecart) {
 
         if (minecart instanceof StorageMinecart) {
             return STORAGE;
         } else if (minecart instanceof PoweredMinecart) {
             return POWERED;
         } else {
             return MINECART;
         }
     }
 
     private Class<? extends Minecart> c;
 
     private MinecartType(final Class<? extends Minecart> c) {
 
         this.c = c;
     }
 
     public Class<? extends Minecart> getCartClass() {
 
         return c;
     }
 
 }
