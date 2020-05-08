 package com.minecarts.oldmcdonald.helper;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 
 import java.util.HashMap;
 
/**
 * Created by IntelliJ IDEA.
 * User: stephen
 * Date: 12/13/11
 * Time: 6:53 PM
 * To change this template use File | Settings | File Templates.
 */
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
            if(type.isAssignableFrom(e.getClass()) && e.getLocation().distance(loc) <= distance){
                 count++;
             }
         }
         return count;
     }
 }
