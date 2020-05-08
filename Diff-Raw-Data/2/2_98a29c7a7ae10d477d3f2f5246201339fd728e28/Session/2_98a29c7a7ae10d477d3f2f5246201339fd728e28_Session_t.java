 package com.undeadscythes.udsplugin;
 
 import org.bukkit.util.*;
 
 /**
  * A WorldEdit session belonging to a player.
  * @author UndeadScythes
  */
 public class Session {
     private Vector v1 = null;
     private Vector v2 = null;
     private SaveablePlayer player;
     private WEWorld world = null;
 
     public Session(SaveablePlayer player) {
         this.player = player;
         this.world = new WEWorld(player.getWorld());
     }
 
     public Vector getV1() {
         return v1;
     }
 
     public Vector getV2() {
         return v2;
     }
 
     public WEWorld getWorld() {
         return world;
     }
 
     public void vert() {
         v1.setY(0);
         v2.setY(world.getMaxHeight());
     }
 
     public void setV1(Vector v1) {
         this.v1 = v1;
     }
 
     public void setV2(Vector v2) {
         this.v2 = v2;
     }
 
     public int getVolume() {
        return (Math.abs(v2.getBlockX() - v1.getBlockX()) + 1) * (Math.abs(v2.getBlockY() - v1.getBlockY()) + 1) * (Math.abs(v2.getBlockZ() - v1.getBlockZ()) + 1);
     }
 }
