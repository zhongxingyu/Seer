 package com.minecarts.normalizeddrops.listener;
 
 import org.bukkit.Location;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.entity.*;
 import org.bukkit.util.config.Configuration;
 
 import java.util.Vector;
 import java.util.ListIterator;
 import java.util.Random;
 
 public class EntityListener extends org.bukkit.event.entity.EntityListener{
     private Vector<EntityDeathBox> nearbyDeaths = new Vector<EntityDeathBox>();
     
     private int minDeaths, maxDeaths, timeFactor; 
     private double radius;
     private boolean debug;
 
     private final Random generator = new Random();
     
     public EntityListener(){
     }
     public void setConfigValues(Configuration config){
         this.minDeaths = config.getInt("minDeaths", 7);
         this.maxDeaths = config.getInt("maxDeaths", 15);
         this.timeFactor = config.getInt("timeFactor", 600);
         this.radius = config.getDouble("radius", 5);
         this.debug = config.getBoolean("debug", false);
         System.out.println(String.format("NormalizedDrops config values: %s, %s, %s, %s, %s",minDeaths,maxDeaths,timeFactor,radius,debug));
     }
 
     private int getNearbyDeathCount(EntityDeathPoint point){
         int deathCount = 0;
         ListIterator<EntityDeathBox> itr = nearbyDeaths.listIterator(nearbyDeaths.size());
         while(itr.hasPrevious()){
             EntityDeathBox previousDeath = (EntityDeathBox)itr.previous();
             if(previousDeath.deathTime < (System.currentTimeMillis() - (timeFactor * 1000))){
                 if(itr.previousIndex() >= 0){
                     nearbyDeaths.subList(0, itr.previousIndex()).clear();
                 }
                 break;
             } else { //check to see if it contains
                if(previousDeath.contains(point)){
                    deathCount++;
                }
            }
         }
         return deathCount;
     }
 
     @Override
     public void onEntityDeath(EntityDeathEvent e){
         if(e.getEntity() instanceof HumanEntity) return;
         if(e.getEntity() instanceof LivingEntity){
             if(e.getDrops().size() > 0){
                 Location loc = e.getEntity().getLocation();
                 EntityDeathPoint p = new EntityDeathPoint(loc.getX(), loc.getY(), loc.getZ());
                 EntityDeathBox box = new EntityDeathBox(loc.getX(),loc.getY(),loc.getZ(),radius);
                 int r = generator.nextInt(maxDeaths) + minDeaths;
                 int deathCount = getNearbyDeathCount(p);
                 if(deathCount > r){
                     if(this.debug){
                        System.out.println(String.format("[NormalizedDrops] Normalized drop at %.2f,%.2f,%.2f (Entity: %s, NearbyDeaths: %s > RND: %s, TrackerSize: %s)",p.x,p.y,p.z,deathCount,r,e.getEntity().toString(),nearbyDeaths.size()));
                     }
                     e.getDrops().clear();
                     return;
                 }
                 this.nearbyDeaths.add(box);
            }
         }
     }
 
     private class EntityDeathPoint{
         public double x,y,z;
         public EntityDeathPoint(double x, double y, double z){
             this.x = x;
             this.y = y;
             this.z = z;
         }
         public String toString(){
             return String.format("%s, %s, %s",x,y,z);
         }
     }
     private class EntityDeathBox{
         public long deathTime;
         public double xMin, xMax, yMin, yMax, zMin, zMax;
         
         public EntityDeathBox(double x,double y,double z, double radius){
             this.deathTime = System.currentTimeMillis();
             xMin = x - radius;
             yMin = y - radius;
             zMin = z - radius;
             xMax = x + radius;
             yMax = y + radius;
             zMax = z + radius;
         }
         
         public String toString(){
             return String.format("%s @ %.2f, %.2f, %.2f -> %.2f %.2f %.2f",deathTime,xMin,yMin,zMin,xMax,yMax,zMax);
         }
         
         public boolean contains(EntityDeathPoint p){
            // System.out.println(p);
            // System.out.println(this);
             if(p.x <= xMin || p.x >= xMax) { return false; }
             if(p.y <= yMin || p.y >= yMax) { return false; }
             return p.z > zMin && p.z < zMax;
         }
     }
 }
