 /* This file is part of FearTheReaper.
 
     FearTheReaper is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     FearTheReaper is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with FearTheReaper.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.herocraftonline.fearthereaper.spawnpoint;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.fearthereaper.FearTheReaper;
 import com.herocraftonline.fearthereaper.ReaperMarkers;
 
 public class SpawnPoint {
     
     public static void loadAllPoints() {
         if (!FearTheReaper.pointsDirectory.exists()) {
             return;
         }
         for (File file : FearTheReaper.pointsDirectory.listFiles())
             loadSpawnPoint(file);
     }
 
     public static void addSpawnPoint(Spawn point) {
         FearTheReaper.SpawnPointList.put(point.getName(), point);
         if (FearTheReaper.markers != null) {
             ReaperMarkers.updateMarker(point);
         }
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
             FearTheReaper.log.warning(e.getMessage());
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
             if (player.hasPermission("graveyard.spawn.*") || player.hasPermission(("graveyard.spawn." + point.getGroup()).toLowerCase())) {
                 allowed.put(point.getName(), point);
             }
         }
 
         return getClosest(player, allowed);
     }
 
     public static HashMap<String, Spawn> getWorldList(Player player) {
         HashMap<String, Spawn> worldPoints = new HashMap<String, Spawn>();
         for (Spawn point : FearTheReaper.SpawnPointList.values()) {
             if (point.getWorldName() == player.getWorld().getName()) {
                 worldPoints.put(point.getName(), point);
             }
         }
 
         return worldPoints;
     }
 
     public static boolean exists(String name) {
         return FearTheReaper.SpawnPointList.containsKey(name);
     }
 
     public static boolean deleteSpawnPoint(String name) {
         if (FearTheReaper.SpawnPointList.containsKey(name)) {
             Spawn point = FearTheReaper.SpawnPointList.remove(name);
             if (FearTheReaper.markers != null) {
                 ReaperMarkers.deleteMarker(point);
             }
            File pointconfig = new File(FearTheReaper.pointsDirectory, name + ".yml");
 
             if (pointconfig.exists()) {
                 pointconfig.delete();
             }
             return true;
         }
         return false;
     }
 }
