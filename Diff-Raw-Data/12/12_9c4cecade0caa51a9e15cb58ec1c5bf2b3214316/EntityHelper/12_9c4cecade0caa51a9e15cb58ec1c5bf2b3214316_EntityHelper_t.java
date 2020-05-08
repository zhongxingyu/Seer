 package com.minecarts.oldmcdonald.helper;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 
 import java.util.HashMap;
 
 public class EntityHelper {
     public static int getTotalEntities(Class type, World world){
         int count = 0;
         for(Entity e : world.getEntities()){
             if(type.isAssignableFrom(e.getClass())){
                 count++;
             }
         }
         return count;
     }
     public static int countNearbyEntities(Class type, Location loc, int distance){
         int count = 0;
         for(Entity e : loc.getWorld().getEntities()){
            Location entityLocation = e.getLocation();
            if(entityLocation == null || loc == null || !entityLocation.getWorld().equals(loc.getWorld())) continue;
            if(type.isAssignableFrom(e.getClass()) && entityLocation.distance(loc) <= distance){
                 count++;
             }
         }
         return count;
     }
 }
