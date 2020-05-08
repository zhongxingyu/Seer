 package com.herocraftonline.fearthereaper.spawnpoint;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.fearthereaper.FearTheReaper;
 
 public class SpawnPoint {
    
     public static void loadAllPoints() {
         for (File file : FearTheReaper.pointsDirectory.listFiles())
             loadSpawnPoint(file);
     }
 
     public static void addSpawnPoint(Spawn point) {
         FearTheReaper.SpawnPointList.put(point.getName(), point);
     }
 
     public static void loadSpawnPoint(File file) {
         Spawn newpoint;
         try {
             newpoint = Spawn.loadConfig(file);
             FearTheReaper.SpawnPointList.put(newpoint.getName(), newpoint);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();
         }
     }
 
     public static boolean save(Spawn point) {
         return point.save();
     }
 
     public static Spawn get(String name) {
         if (FearTheReaper.SpawnPointList.containsKey(name)) {
             return FearTheReaper.SpawnPointList.get(name);
         }
         return null;
     }
 
     public static Spawn getClosest(Player player) {
         Collection<Spawn> list = getWorldList(player).values();
         if (list.isEmpty()) {
             return null;
         }
         
         Location loc = player.getLocation();
         Spawn spawn = null;
         int lastDist = -1;
         for (Spawn point : list) {
             //This should happen, but check just in case
             if (!point.getWorld().equals(player.getWorld())) {
                 continue;
             }
             int newDist = distSq(loc, point);
             if (newDist < lastDist || lastDist < 0) {
                 lastDist = newDist;
                 spawn = point;
             }
         }
         return spawn;
     }
 
     public static Spawn getClosest(Player player, HashMap<String, Spawn> spawnList) {
         List<Spawn> list = new ArrayList<Spawn>(spawnList.values());
         if (list.isEmpty()) {
             return null;
         }
         
         Location loc = player.getLocation();
         Spawn spawn = null;
         int lastDist = -1;
         for (Spawn point : list) {
             //This should happen, but check just in case
             if (!point.getWorld().equals(player.getWorld())) {
                 continue;
             }
             int newDist = distSq(loc, point);
             if (newDist < lastDist || lastDist < 0) {
                 lastDist = newDist;
                 spawn = point;
             }
         }
         return spawn;
     }
 
     public static int distSq(Location location, Spawn point) {
         int x1 = (int) point.getX();
         int z1 = (int) point.getZ();
         int x2 = location.getBlockX();
         int z2 = location.getBlockZ();
         x1 -= x2;
         z1 -= z2;
         return x1 * x1 + z1 * z1;
     }
 
     public static HashMap<String, Spawn> getAllowedList(Player player) {
         HashMap<String, Spawn> allowed = new HashMap<String, Spawn>();
         for (Spawn point : FearTheReaper.getSpawnList().values())
         {
             if (player.hasPermission(("graveyard.spawn." + point.getGroup()).toLowerCase())) {
                 allowed.put(point.getName(), point);
             }
         }
 
         return allowed;
     }
 
     public static Spawn getClosestAllowed(Player player) {
         HashMap<String, Spawn> allowed = new HashMap<String, Spawn>();
         for (Spawn point : FearTheReaper.getSpawnList().values()) {
             if (player.hasPermission(("graveyard.spawn." + point.getGroup()).toLowerCase())) {
                 allowed.put(point.getName(), point);
             }
         }
 
         return getClosest(player, allowed);
     }
 
     public static Spawn getRandomAllowed(Player player) {
         List<Spawn> allowed = new ArrayList<Spawn>();
         for (Spawn point : FearTheReaper.getSpawnList().values()) {
             if (player.hasPermission(("graveyard.spawn." + point.getGroup()).toLowerCase())) {
                 allowed.add(point);
             }
         }
 
         Random randomGenerator = new Random();
         int randomInt = randomGenerator.nextInt(allowed.size());
 
         return allowed.get(randomInt);
     }
 
     public static HashMap<String, Spawn> getWorldList(Player player) {
         HashMap<String, Spawn> worldPoints = new HashMap<String, Spawn>();
         for (Spawn Spawns : FearTheReaper.SpawnPointList.values())
         {
             if (Spawns.getWorldName() == player.getWorld().getName()) {
                 worldPoints.put(Spawns.getName(), Spawns);
             }
         }
 
         return worldPoints;
     }
 
     public static boolean exists(String name) {
         return FearTheReaper.SpawnPointList.containsKey(name);
     }
 
     public static boolean deleteSpawnPoint(String name) {
         if (FearTheReaper.SpawnPointList.containsKey(name)) {
             FearTheReaper.SpawnPointList.remove(name);
             File pointconfig = new File(FearTheReaper.pointsDirectory, name + ".cfg");
 
             if (pointconfig.exists()) {
                 pointconfig.delete();
             }
             return true;
         }
         return false;
     }
 }
