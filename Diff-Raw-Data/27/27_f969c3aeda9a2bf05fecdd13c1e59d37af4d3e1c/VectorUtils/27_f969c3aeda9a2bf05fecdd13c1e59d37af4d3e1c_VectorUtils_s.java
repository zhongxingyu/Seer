 package com.undeadscythes.udsplugin.utilities;
 
 import org.bukkit.block.*;
 import org.bukkit.util.*;
 
 /**
  * Utility class for handling manipulation of
  * {@link org.bukkit.util.Vector} objects.
  * 
  * @author UndeadScythes
  */
 public class VectorUtils {
     public static Vector getFlooredVector(final Vector vector) {
         return new Vector(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
     }
     
     public static Vector getVector(final String vector) {
         final String[] split = vector.replace("(", "").replace(")", "").split(",");
         final double x = Double.parseDouble(split[0]);
         final double y = Double.parseDouble(split[1]);
         final double z = Double.parseDouble(split[2]);
         return new Vector(x, y, z);
     }
     
     public static Vector toVector(final BlockFace blockFace) {
         switch(blockFace) {
             case UP:
                 return new Vector(0, 1, 0);
             case DOWN:
                 return new Vector(0, -1, 0);
             case NORTH:
                 return new Vector(0, 0, -1);
             case SOUTH:
                 return new Vector(0, 0, 1);
             case EAST:
                 return new Vector(1, 0, 0);
             case WEST:
                 return new Vector(-1, 0, 0);
             default:
                 return new Vector(0, 0, 0);
         }
     }
 
     private VectorUtils() {}
 }
