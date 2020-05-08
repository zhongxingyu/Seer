 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Wolf.
  *
  * Wolf is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Wolf is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Wolf.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.wolf.wolf;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 
 import com.halvors.wolf.WolfPlugin;
 
 /**
  * Handle wolves
  * 
  * @author halvors
  */
 public class WolfManager {
     private final WolfPlugin plugin;
     
     private final HashMap<UUID, Wolf> wolves;
     private List<String> wolfNames;
     
     public WolfManager(final WolfPlugin plugin) {
         this.plugin = plugin;
         this.wolves = new HashMap<UUID, Wolf>();
         this.wolfNames = new ArrayList<String>();
         
         initRandomNames();
     }
     
     /**
      * Get all WolfTables
      * 
      * @return List<WolfTable>
      */
     public List<WolfTable> getWolfTables() {
         return plugin.getDatabase().find(WolfTable.class).findList();
     }
     
     /**
      * Get all owners WolfTables
      * 
      * @param owner
      * @return List<WolfTable>
      */
     public List<WolfTable> getWolfTables(String owner) {
         return plugin.getDatabase().find(WolfTable.class).where()
             .ieq("owner", owner).findList();
     }
 
     /**
      * Load wolves from database
      */
     public void load() {
     	for (WolfTable wt : getWolfTables()) {
     		UUID uniqueId = UUID.fromString(wt.getUniqueId());
     		
     		wolves.put(uniqueId, new Wolf(plugin, uniqueId));
     	}
     }
    
     /**
      * Save wolves to database
      */
     public void save() {
     	wolves.clear();
     }
     
     /**
      * Add a wolf
      * 
      * @param name
      * @param wolf
      * @return boolean
      */
     public boolean addWolf(org.bukkit.entity.Wolf wolf, String name) {
         UUID uniqueId = wolf.getUniqueId();
         Player player = (Player)wolf.getOwner();
         
         boolean nameUnique = false;
         
         // Check if a wolf with same name already exists.
         do {
           	for (WolfTable wt : getWolfTables(player.getName())) {
            		if (wt.getName().equalsIgnoreCase(name)) {
            			name = getRandomName();
            		} else {
            			nameUnique = true;
            		}
            	}
           	
           	nameUnique = true;
         } while (!nameUnique);
         
         if (hasWolf(uniqueId)) {
         	return false;
         }
             
         // Create a new WolfTable
         WolfTable wt = new WolfTable();
         wt.setUniqueId(uniqueId.toString());
         wt.setName(name);
         wt.setOwner(player.getName());
         wt.setWorld(wolf.getWorld().getName());
         wt.setInventory(false);
             
         // Save the wolf to the database
         plugin.getDatabase().save(wt);
             
         if (wolves.containsKey(uniqueId)) {
           	wolves.remove(uniqueId);
         }
             
         Wolf wolf1 = new Wolf(plugin, uniqueId);
             
         wolves.put(uniqueId, wolf1);
 
 //		return wolves.get(uniqueId) == null ? false : true;
 		return hasWolf(uniqueId);
     }
     
     /**
      * Add a wolf with a random name
      * 
      * @param wolf
      * @return boolean
      */
     public boolean addWolf(org.bukkit.entity.Wolf wolf) {
         return addWolf(wolf, getRandomName());
     }
     
     /**
      * Remove a wolf
      * 
      * @param uniqueId
      * @return boolean
      */
     public boolean removeWolf(UUID uniqueId) {
         if (wolves.containsKey(uniqueId)) {     	
        	wolves.remove(uniqueId);
        	
         	plugin.getDatabase().delete(getWolf(uniqueId).getWolfTable()); // TODO: Improve?
         }
         
         return !(hasWolf(uniqueId));
     }
     
     
     /**
      * Remove a wolf
      * 
      * @param wolf
      * @return boolean
      */
     public boolean removeWolf(org.bukkit.entity.Wolf wolf) {
     	return removeWolf(wolf.getUniqueId());
     }
 
     /**
      * Check if wolf exists
      * 
      * @param uniqueId
      * @return boolean
      */
     public boolean hasWolf(UUID uniqueId) {
     	return wolves.containsKey(uniqueId);
     }
     
     /**
      * Check if wolf exists
      * 
      * @param wolf
      * @return boolean
      */
     public boolean hasWolf(org.bukkit.entity.Wolf wolf) {
     	return hasWolf(wolf.getUniqueId());
     }
     
     /**
      * Check if owner has specific named wolf
      * 
      * @param name
      * @param owner
      * @return boolean
      */
     public boolean hasWolf(String name, String owner) {
     	for (Wolf wolf : getWolves()) {
     		if (wolf.getOwner().getName().equalsIgnoreCase(owner) && wolf.getName().equalsIgnoreCase(name)) {
     			return true;
     		}
     	}
     	
     	return false;
     }
     
     /**
      * Check if owner has a wolf/wolves
      * 
      * @param owner
      * @return boolean
      */
     public boolean hasWolf(String owner) {
     	for (Wolf wolf : getWolves()) {
     		if (wolf.getOwner().getName().equalsIgnoreCase(owner)) {
     			return true;
     		}
     	}
     	
     	return false;
     }
     
     /**
      * Get wolf
      * 
      * @param uniqueId
      * @return Wolf
      */
     public Wolf getWolf(UUID uniqueId) {
     	return wolves.get(uniqueId);
     }
     
     /**
      * Get wolf
      * 
      * @param Wolf
      * @return Wolf
      */
     public Wolf getWolf(org.bukkit.entity.Wolf wolf) {
     	return getWolf(wolf.getUniqueId());
     }
     
     /**
      * Get wolf
      * 
      * @param name
      * @param owner
      * @return Wolf
      */
     public Wolf getWolf(String name, String owner) {
     	for (Wolf wolf : getWolves()) {
     		if (wolf.getOwner().getName().equalsIgnoreCase(owner) && wolf.getName().equalsIgnoreCase(name)) {
     			return wolf;
     		}
     	}
     	
     	return null;
     }
     
     /**
      * Get all wolves
      * 
      * @return
      */
     public List<Wolf> getWolves() {    
     	return new ArrayList<Wolf>(wolves.values());
     }
 
     /**
      * Get owners wolves
      * 
      * @param owner
      * @return List<Wolf>
      */
     public List<Wolf> getWolves(Player owner) {
     	List<Wolf> wolves = new ArrayList<Wolf>();
     	
         for (Wolf wolf : getWolves()) {
         	if (wolf.getOwner().getName().equalsIgnoreCase(owner.getName())) {
         		wolves.add(wolf);
         	}
         }
         
         return wolves;
     }
     
     /**
      * Spawn a wolf
      * 
      * @param player
      * @param world
      * @param location
      * @return Wolf
      */
     public org.bukkit.entity.Wolf spawnWolf(Player player, World world, Location location) {
     	org.bukkit.entity.Wolf wolf = (org.bukkit.entity.Wolf) world.spawnCreature(location, CreatureType.WOLF);
         wolf.setTamed(true);
         wolf.setOwner(player);
         
         return wolf;
     }
     
     /**
      * Spawn a wolf at player's position
      * 
      * @param player
      * @param tame
      * @return Wolf
      */
     public org.bukkit.entity.Wolf spawnWolf(Player player) {
         return spawnWolf(player, player.getWorld(), player.getLocation());
     }
     
     /**
      * Release a wolf
      * 
      * @param wolf
      */
     public void releaseWolf(org.bukkit.entity.Wolf wolf) {
         if (hasWolf(wolf)) {
             removeWolf(wolf);
         }
         
         wolf.setTamed(false);
     }
     
     /**
      * Generate the table of premade wolf names
      */
     private void initRandomNames() {  
         try {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(WolfManager.class.getResourceAsStream("wolfnames.txt")));
            
             do {
                 String s1;
                 if ((s1 = bufferedReader.readLine()) == null) {
                     break;
                 }
                 
                 s1 = s1.trim();
                 
                 if (s1.length() > 0) {
                     wolfNames.add(s1);
                 }
             } while (true);
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Generate a random name
      * 
      * @return String
      */
     public String getRandomName() {
         Random random = new Random();
         
         String name = wolfNames.get(random.nextInt(wolfNames.size()));
         
         return name;
     }
 }
