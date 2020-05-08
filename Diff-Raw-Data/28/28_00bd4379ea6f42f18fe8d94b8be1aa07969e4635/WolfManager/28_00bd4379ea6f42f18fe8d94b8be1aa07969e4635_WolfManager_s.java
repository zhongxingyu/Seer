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
 
 package com.halvors.Wolf.wolf;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 /**
  * Handle wolves
  * 
  * @author halvors
  */
 public class WolfManager {
     private final com.halvors.Wolf.Wolf plugin;
     
 //    private WolfInventoryManager wolfInventoryManager;
     
     public WolfManager(com.halvors.Wolf.Wolf plugin) {
         this.plugin = plugin;
 //        this.wolfInventoryManager = plugin.getWolfInventoryManager();
     }
     
     /*
     public WolfTable getWolfTable(String uniqueId) {
         return plugin.getDatabase().find(WolfTable.class).where()
             .eq("uniqueId", uniqueId).findUnique();
     }
     */
     
     /**
      * Get WolfTable
      * 
      * @param uniqueId
      * @return WolfTable
      */
     public WolfTable getWolfTable(UUID uniqueId) {
     	return plugin.getDatabase().find(WolfTable.class).where()
     		.eq("uniqueId", uniqueId.toString()).findUnique();
     }
     
     /**
      * Get WolfTable
      * 
      * @param name
      * @return WolfTable
      */
     public WolfTable getWolfTable(String name, String owner) {
         return plugin.getDatabase().find(WolfTable.class).where()
             .eq("name", name).eq("owner", owner).findUnique();
     }
 
     /**
      * Update WolfTable
      * 
      * @param wt
      */
     public void updateWolfTable(WolfTable wt) {
         plugin.getDatabase().update(wt);
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
      * Add a wolf
      * 
      * @param name
      * @param wolf
      */
     public void addWolf(Wolf wolf, String name) {
         if (wolf.isTamed()) {
             Player player = (Player)wolf.getOwner();
             
             // Check if a wolf with same name already exists.
             for (WolfTable wt : getWolfTables(player.getName())) {
                 if (wt.getName().equalsIgnoreCase(name)) {
                     // TODO: Maybe give a random name instead of return
                     
                     return;
                 }
             }
             
             // Create a new WolfTable
             WolfTable wt = new WolfTable();
             wt.setUniqueId(wolf.getUniqueId().toString());
             wt.setName(name);
             wt.setOwner(player.getName());
             wt.setWorld(wolf.getWorld().getName());
             wt.setInventory(false);
             
             // Save the wolf to the database
             plugin.getDatabase().save(wt);
             
             // Pull a fresh copy of the wolf to retrieve the database ID
             wt = getWolfTable(wolf.getUniqueId());
         }
     }
     
     /**
      * Add a wolf with a random name
      * 
      * @param wolf
      */
     public void addWolf(Wolf wolf) {
         addWolf(wolf, getRandomName());
     }
     
     /**
      * Remove a wolf
      * 
      * @param uniqueId
      */
     public void removeWolf(UUID uniqueId) {
         WolfTable wt = getWolfTable(uniqueId);
         
         if (wt != null) {
             plugin.getDatabase().delete(wt);
         }
     }
     
     /**
      * Remove a wolf
      * 
      * @param name
      * @param owner
      */
     public void removeWolf(String name, String owner) {
         removeWolf(getUniqueId(name, owner));
     }
     
     /**
      * Check if wolf exist by WolfTable
      * 
      * @param wt
      * @return Boolean
      */
     public boolean hasWolf(WolfTable wt) {
         if (wt != null) {
             return true;
         }
         
         return false;
     }
     
     /**
      * Check if wolf exist
      * 
      * @param name
      * @return Boolean
      */
     public boolean hasWolf(String name, String owner) {
         return hasWolf(getWolfTable(name, owner));
     }
     
     /** 
      * Check if wolf exists
      * 
      * @param uniqueId
      * @return Boolean
      */
     public boolean hasWolf(UUID uniqueId) {
         return hasWolf(getWolfTable(uniqueId));
     }
     
     /**
      * Get wolf by WolfTable
      * 
      * @param wt
      * @return Wolf
      */
     public Wolf getWolf(WolfTable wt) {
         for (Entity entity : plugin.getServer().getWorld(wt.getWorld()).getEntities()) {
             if (entity instanceof Wolf) {
                 if (UUID.fromString(wt.getUniqueId()) == entity.getUniqueId())  {
                     return (Wolf) entity;
                 }
             }
         }
         
         return null;
     }
     
     /**
      * Get wolf
      * 
      * @param uniqueId
      * @return Wolf
      */
     public Wolf getWolf(UUID uniqueId) {
         return getWolf(getWolfTable(uniqueId));
     }
     
     /**
      * Get wolf
      * 
      * @param name
      * @return Wolf
      */
     public Wolf getWolf(String name, String owner) {
         return getWolf(getWolfTable(name, owner));
     }
     
     /**
      * Get all wolves
      * 
      * @return
      */
     public List<Wolf> getWolves() {    
         List<WolfTable> wts = getWolfTables();
         List<Wolf> wolves = new ArrayList<Wolf>();
         
         for (WolfTable wt : wts) {
             for (Entity entity : plugin.getServer().getWorld(wt.getWorld()).getEntities()) {
                 if (entity instanceof Wolf && UUID.fromString(wt.getUniqueId()) == entity.getUniqueId()) {
                     wolves.add((Wolf) entity);
                 }
             }
         }
         
         return wolves;
     }
 
     /**
      * Get owners wolves
      * 
      * @param owner
      * @return List<Wolf>
      */
     public List<Wolf> getWolves(String owner) {    
         List<WolfTable> wts = getWolfTables(owner);
         List<Wolf> wolves = new ArrayList<Wolf>();
         
         for (WolfTable wt : wts) {
             for (Entity entity : plugin.getServer().getWorld(wt.getWorld()).getEntities()) {
                 if (entity instanceof Wolf && UUID.fromString(wt.getUniqueId()) == entity.getUniqueId()) {
                     wolves.add((Wolf) entity);
                 }
             }
         }
         
         return wolves;
     }
     
     /**
      * Get id
      * 
      * @param uniqueId
      * @return
      */
     public int getId(UUID uniqueId) {
     	WolfTable wt = getWolfTable(uniqueId);
     	
     	if (wt != null) {
     		return wt.getId();
     	}
     	
     	return 0;
     }
     
     /**
      * Get id
      * 
      * @param name
      * @param owner
      * @return
      */
     public int getId(String name, String owner) {
     	return getId(getUniqueId(name, owner));
     }
     
     /**
      * Get uniqueId
      * 
      * @param name
      * @param owner
      * @return UUID
      */
     public UUID getUniqueId(String name, String owner) {
         WolfTable wt = getWolfTable(name, owner);
         
         if (wt != null) {
             return UUID.fromString(wt.getUniqueId());
         }
         
         return null;
     }
     
     /**
      * Get name
      * 
      * @param uniqueId
      * @return String
      */
     public String getName(UUID uniqueId) {
         WolfTable wt = getWolfTable(uniqueId);
         
         if (wt != null) {
             return wt.getName();
         }
         
         return null;
     }
     
     /**
      * Get wolf's owner
      * 
      * @param uniqueId
      * @return Player
      */
     public Player getOwner(UUID uniqueId) {
         WolfTable wt = getWolfTable(uniqueId);
         
         if (wt != null) {
             for (Player player : plugin.getServer().getOnlinePlayers()) {
                 if (player.getName().equalsIgnoreCase(wt.getOwner())) {
                     return player;
                 }
             }
         }
         
         return null;
     }
 
     /**
      * Get wolf's world
      * 
      * @param uniqueId
      * @return World
      */
     public World getWorld(UUID uniqueId) {
         WolfTable wt = getWolfTable(uniqueId);
         
         if (wt != null) {
             return plugin.getServer().getWorld(wt.getWorld());
         }
         
         return null;
     }
     
     /**
      * Get wolf's world
      * 
      * @param name
      * @return World
      */
     public World getWorld(String name, String owner) {
         return getWorld(getUniqueId(name, owner));
     }
     
     /**
      * Check if wolf has inventory
      * 
      * @param uniqueId
      * @return Boolean
      */
     public boolean hasInventory(UUID uniqueId) {
     	WolfTable wt = getWolfTable(uniqueId);
     	
     	if (wt != null) {
     		return wt.isInventory();
     	}
     	
     	return false;
     }
     
     /**
      * Check if wolf has inventory
      * 
      * @param name
      * @param owner
      * @return Boolean
      */
     public boolean hasInventory(String name, String owner) {
     	return hasInventory(getUniqueId(name, owner));
     }
     
     /**
      * Add wolf inventory
      * 
      * @param uniqueId
      */
     public void addInventory(UUID uniqueId) {
     	WolfTable wt = getWolfTable(uniqueId);
     	
     	if (wt != null) {
     		wt.setInventory(true);
     		plugin.getWolfInventoryManager().addWolfInventory(uniqueId);
     	}
     }
     
     /**
      * Remove wolf inventory
      * 
      * @param uniqueId
      */
     public void removeInventory(UUID uniqueId) {
     	WolfTable wt = getWolfTable(uniqueId);
     	
     	if (wt != null) {
     		wt.setInventory(false);
     		
     		plugin.getWolfInventoryManager().removeWolfInventory(uniqueId);
     	}
     }
     
     /**
      * Spawn a wolf
      * 
      * @param player
      * @param world
      * @param location
      * @return Wolf
      */
     public Wolf spawnWolf(Player player, World world, Location location) {
         Wolf wolf = (Wolf) world.spawnCreature(location, CreatureType.WOLF);
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
     public Wolf spawnWolf(Player player) {
         return spawnWolf(player, player.getWorld(), player.getLocation());
     }
     
     /**
      * Release a wolf
      * 
      * @param wolf
      */
     public void releaseWolf(Wolf wolf) {
         if (hasWolf(wolf.getUniqueId())) {
             removeWolf(wolf.getUniqueId());
         }
         
         wolf.setTamed(false);
     }
     
     /**
      * Generate a random name
      * 
      * @return String
      */
     public String getRandomName() { // TODO: Improve this. 
         Random random = new Random();
         List<String> names = new ArrayList<String>();
         String name = "Wolf";
         
         try {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(WolfManager.class.getResourceAsStream("wolfnames.txt")));
            
             do {
                 String s1;
                 if ((s1 = bufferedReader.readLine()) == null) {
                     break;
                 }
                 
                 s1 = s1.trim();
                 
                 if (s1.length() > 0) {
                     names.add(s1);
                 }
             } while (true);
             
             name = (String)names.get(random.nextInt(names.size()));
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         return name;
     }
 }
