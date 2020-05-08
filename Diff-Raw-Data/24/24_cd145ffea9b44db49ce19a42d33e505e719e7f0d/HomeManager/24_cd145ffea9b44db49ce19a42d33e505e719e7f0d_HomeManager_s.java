 /*
 * Copyright (c) 2013, LankyLord
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
 package net.lankylord.simplehomes.managers;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class HomeManager {
 
     private final HomeFileManager fileManager;
     private final Map<String, Map> loadedHomes;
 
     public HomeManager(HomeFileManager fileManager) {
         this.fileManager = fileManager;
         this.loadedHomes = new HashMap<>();
     }
 
     /**
      * Check whether a player has reached their maximum amount of homes
      *
      * @param playerName Name of the player
      * @return Whether a player has reached the maximum amount of homes
      */
     public boolean reachedMaxHomes(String playerName) {
         return getHomesSize(playerName) < ConfigManager.getMaxHomes();
     }
 
     /**
      * Get the amount of homes of a player
      *
      * @param playerName Name of the player
      * @return Amount of homes
      */
     public int getHomesSize(String playerName) {
         if (fileManager.getHomes().contains(playerName.toLowerCase())) {
             return fileManager.getHomes().getConfigurationSection(playerName.toLowerCase()).getKeys(false).size();
         }
         return 0;
     }
 
     private void saveHomeToFile(String playerName, Location location, String homeName) {
         ConfigurationSection home = fileManager.getHomes().getConfigurationSection(playerName.toLowerCase() + "." +
                 homeName.toLowerCase());
         if (home == null) {
             home = fileManager.getHomes().createSection(playerName.toLowerCase() + "." + homeName.toLowerCase());
         }
 
         home.set("world", location.getWorld().getName());
         home.set("x", location.getBlockX());
         home.set("y", location.getBlockY());
         home.set("z", location.getBlockZ());
 
         fileManager.saveHomes();
     }
 
     /**
      * Save a player's home
      *
      * @param player   The player
      * @param homeName Name of the home
      */
     public void saveHome(Player player, String homeName) {
         String playerName = player.getName().toLowerCase();
         Location location = player.getLocation();
 
         Map<String, Location> homeLocation = loadedHomes.get(playerName);
         if (homeLocation == null) {
             homeLocation = new HashMap<>();
         }
         homeLocation.put(homeName.toLowerCase(), location);
         loadedHomes.put(playerName, homeLocation);
 
         saveHomeToFile(playerName, location, homeName);
     }
 
     public void deleteHome(String playerName, String homeName) {
         Map homeLocations = loadedHomes.get(playerName.toLowerCase());
         if (homeLocations != null) {
             homeLocations.remove(homeName.toLowerCase());
             loadedHomes.put(playerName.toLowerCase(), homeLocations);
         }
         ConfigurationSection home = fileManager.getHomes().getConfigurationSection(playerName.toLowerCase());
         home.set(homeName.toLowerCase(), null);
         fileManager.saveHomes();
     }
 
     /**
      * Save a player's home
      *
      * @param playerName Name of the player
      * @param homeName   Name of the home
      * @param location   Location of the home
      */
     public void saveHome(String playerName, String homeName, Location location) {
         Map<String, Location> homeLocation = loadedHomes.get(playerName);
         if (homeLocation == null) {
             homeLocation = new HashMap<>();
         }
         homeLocation.put(homeName.toLowerCase(), location);
         loadedHomes.put(playerName, homeLocation);
 
         saveHomeToFile(playerName, location, homeName);
     }
 
     /**
      * Load a player's homes from file
      *
      * @param playerName Name of the player
      */
     public void loadPlayerHomes(String playerName) {
         ConfigurationSection homes = fileManager.getHomes().getConfigurationSection(playerName.toLowerCase());
        Map<String, Location> homeLocation = new HashMap<>();
 
        for (String homeName : homes.getKeys(false)) {
            ConfigurationSection home = homes.getConfigurationSection(homeName);
 
            String world = home.getString("world");
            int x = home.getInt("x");
            int y = home.getInt("y");
            int z = home.getInt("z");
 
            homeLocation.put(homeName.toLowerCase(), new Location(Bukkit.getWorld(world), x, y, z));
         }
        loadedHomes.put(playerName.toLowerCase(), homeLocation);
     }
 
     /**
      * Remove a player's homes from memory
      *
      * @param playerName Name of the player
      */
     public void unloadPlayerHomes(String playerName) {
         loadedHomes.remove(playerName.toLowerCase());
     }
 
     /**
      * Get a player's home from memory
      *
      * @param playerName Name of the player     * @param homeName   Name of the home
      * @return Location of home
      */
     public Location getPlayerHome(String playerName, String homeName) {
         Map<String, Location> homeLocations = loadedHomes.get(playerName.toLowerCase());
         if (homeLocations != null) {
             return homeLocations.get(homeName.toLowerCase());
         } else {
             return null;
         }
     }
 
     public Location getPlayerHomeFromFile(String playerName, String homeName) {
         ConfigurationSection homes = fileManager.getHomes().getConfigurationSection(playerName.toLowerCase());
         Map<String, Location> homeLocation = new HashMap<>();
 
         for (String home : homes.getKeys(false)) {
             ConfigurationSection homeSection = homes.getConfigurationSection(home);
             String world = homeSection.getString("world");
             int x = homeSection.getInt("x");
             int y = homeSection.getInt("y");
             int z = homeSection.getInt("z");
 
             homeLocation.put(home.toLowerCase(), new Location(Bukkit.getWorld(world), x, y, z));
         }
         return homeLocation.get(homeName.toLowerCase());
     }
 
     public Map getPlayerHomes(String playerName) {
         return loadedHomes.get(playerName.toLowerCase());
     }
 }
