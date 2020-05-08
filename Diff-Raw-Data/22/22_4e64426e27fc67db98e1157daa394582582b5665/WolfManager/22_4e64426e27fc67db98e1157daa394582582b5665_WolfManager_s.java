 /* 
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  * Copyright (C) 2011 adamonline45 <adamonline45@gmail.com>
  * 
  * This file is part of Lupi.
  * 
  * Lupi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Lupi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Lupi.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.halvors.lupi.wolf;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.halvors.lupi.Lupi;
 import org.halvors.lupi.util.RandomNameUtil;
 import org.halvors.lupi.util.WolfUtil;
 import org.halvors.lupi.wolf.inventory.WolfInventoryManager;
 
 /**
  * Handle tamed wolves.
  * 
  * @author halvors
  */
 public class WolfManager {
 	private final Lupi plugin;
 //	private final EbeanServer database;
     private final WolfInventoryManager wolfInventoryManager;
 //    private final RandomNameManager randomNameManager;
     private final RandomNameUtil rnu;
     
     private final HashMap<UUID, Wolf> wolves;
     private final List<String> wolfNames;
     
     private static WolfManager instance;
     
     public WolfManager(Lupi plugin) {
         this.plugin = plugin;
 //        this.database = plugin.getDatabase();
         this.wolfInventoryManager = plugin.getWolfInventoryManager();
 //        this.randomNameManager = new RandomNameManager(plugin);
        this.rnu = new RandomNameUtil(plugin);
         this.wolves = new HashMap<UUID, Wolf>();
         this.wolfNames = new ArrayList<String>();
         
         WolfManager.instance = this;
         
         
 //        initRandomNames();
     }
     
     public static WolfManager getInstance() {
         return instance;
     }
     
     /**
      * Get WolfTable
      * 
      * @param uniqueId
      * @return
      */
     public WolfTable getWolfTable(UUID uniqueId) {
         return plugin.getDatabase().find(WolfTable.class).where()
             .ieq("uniqueId", uniqueId.toString()).findUnique();
     }
     
     /**
      * Get all WolfTables.
      * 
      * @return
      */
     public List<WolfTable> getWolfTables() {
         return plugin.getDatabase().find(WolfTable.class).where().findList();
     }
     
     /**
      * Get WolfTable's for world
      * 
      * @param world
      * @return
      */
     public List<WolfTable> getWolfTables(World world) {
         return plugin.getDatabase().find(WolfTable.class).where()
             .ieq("world", world.getName()).findList();
     }
     
     /**
      * Get WolfTable's for player.
      * 
      * @param player
      * @return
      */
     public List<WolfTable> getWolfTables(Player player) {
         return plugin.getDatabase().find(WolfTable.class).where()
             .ieq("owner", player.getName()).findList();
     }
     
     /**
      * Load wolves in world.
      */
     public void load() {
         for (WolfTable wt : getWolfTables()) {
             UUID uniqueId = UUID.fromString(wt.getUniqueId());
                 
             loadWolf(uniqueId);
         }
     }
    
     /**
      * Unload wolves in world.
      */
     public void unload() {
         for (Wolf wolf : getWolves()) {
             unloadWolf(wolf.getUniqueId());
         }
     }
     
     /**
      * Load wolves in world.
      */
     public void load(World world) {
         for (WolfTable wt : getWolfTables()) {
             if (wt.getWorld().equals(world.getName())) {
                 UUID uniqueId = UUID.fromString(wt.getUniqueId());
                 
                 loadWolf(uniqueId);
             }
         }
     }
    
     /**
      * Unload wolves in world.
      */
     public void unload(World world) {
         for (Wolf wolf : getWolves()) {
             if (wolf.getWorld().getName().equalsIgnoreCase(world.getName())) {
                 unloadWolf(wolf.getUniqueId());
             }
         }
     }
     
     /**
      * Load a wolf.
      * 
      * @param uniqueId
      * @return
      */
     public boolean loadWolf(UUID uniqueId) {
         if (!hasWolf(uniqueId)) {
             // Create the Wolf.
             Wolf wolf = new Wolf(uniqueId);
             
             // Load inventory if wolf has.
             if (wolf.hasInventory()) {
                 wolfInventoryManager.loadWolfInventory(uniqueId);
             }
             
             wolves.put(uniqueId, wolf);
             
             return true;
         }
         
         return false;
     }
     
     /**
      * Load a wolf.
      * 
      * @param wolf
      * @return
      */
     public boolean loadWolf(org.bukkit.entity.Wolf wolf) {
         return loadWolf(wolf.getUniqueId());
     }
     
     /**
      * Unload a wolf.
      * 
      * @param uniqueId
      * @return
      */
     public boolean unloadWolf(UUID uniqueId) {
         if (hasWolf(uniqueId)) {
             Wolf wolf = getWolf(uniqueId);
             
             // Unload inventory if it exists and is loaded.
             if (wolf.hasLoadedInventory()) {
                 wolfInventoryManager.unloadWolfInventory(uniqueId);
             }    
             
             wolves.remove(uniqueId);
             
             return true;
         }
         
         return false;
     }
     
     /**
      * Unload a wolf.
      * 
      * @param wolf
      * @return
      */
     public boolean unloadWolf(org.bukkit.entity.Wolf wolf) {
         return unloadWolf(wolf.getUniqueId());
     }
     
     /**
      * Add a wolf.
      * 
      * @param name
      * @param wolf
      * @return
      */
     public boolean addWolf(org.bukkit.entity.Wolf bukkitWolf, String name) {
         UUID uniqueId = bukkitWolf.getUniqueId();
         
         if (!hasWolf(uniqueId)) {
             Player player = (Player) bukkitWolf.getOwner();
         
             /*
             Random random = new Random();
             List<String> usedNames = new ArrayList<String>();
             boolean nameIsUnique = false;
             boolean needDynamic = false;
             
             // Check if a wolf with the same name already exists.
             for (WolfTable wt : getWolfTables(player)) {
                 usedNames.add(wt.getName());
             }
             
             if (usedNames.size() >= wolfNames.size()) {
                 needDynamic = true;
             }
 
             do {
                 if (usedNames.contains(name)) {
                     if (needDynamic) {
                         name = getRandomName() + random.nextInt(10);
                     } else {
                         name = getRandomName();
                     }
                 } else {
                     nameIsUnique = true;
                 }
             } while (!nameIsUnique);
             */
             
             // Create the WolfTable.
             WolfTable wt = new WolfTable();
             wt.setUniqueId(uniqueId.toString());
             wt.setName(name);
             wt.setOwner(player.getName());
             wt.setWorld(bukkitWolf.getWorld().getName());
             wt.setInventory(false);
             
             // Save the WolfTable to plugin.getDatabase().
             plugin.getDatabase().save(wt);
             
             // Create the Wolf.
             Wolf wolf = new Wolf(uniqueId);
             
             wolves.put(uniqueId, wolf);
             
             return true;
         }
         
         return false;
     }
     
     /**
      * Add a wolf with a random name.
      * 
      * @param wolf
      * @return
      */
     public boolean addWolf(org.bukkit.entity.Wolf wolf) {
         return addWolf(wolf, rnu.getRandomName());
     }
     
     /**
      * Remove a wolf.
      * 
      * @param uniqueId
      * @return
      */
     public boolean removeWolf(UUID uniqueId) {
         if (hasWolf(uniqueId)) {       
             Wolf wolf = getWolf(uniqueId);
             
             if (wolf.hasInventory() || wolf.hasLoadedInventory()) {
                 wolf.removeInventory();
             }
             
             wolves.remove(uniqueId);
             plugin.getDatabase().delete(getWolfTable(uniqueId));
             
             return true;
         }
         
         return false;
     }
     
     /**
      * Remove a wolf.
      * 
      * @param wolf
      * @return
      */
     public boolean removeWolf(org.bukkit.entity.Wolf wolf) {
         return removeWolf(wolf.getUniqueId());
     }
     
     /**
      * Check if wolf exists in database.
      * 
      * @param uniqueId
      * @return
      */
     public boolean hasWolfInDB(UUID uniqueId) {
         WolfTable wt = getWolfTable(uniqueId);
         
         if (wt != null) {
             return true;
         }
         
         return false;
     }
     
     /**
      * Check if wolf exists in database.
      * 
      * @param wolf
      * @return 
      * @return
      */
     public boolean hasWolfInDB(org.bukkit.entity.Wolf wolf) {
         return hasWolfInDB(wolf.getUniqueId());
     }
     
     /**
      * Check if loaded wolf exists.
      * 
      * @param uniqueId
      * @return
      */
     public boolean hasWolf(UUID uniqueId) {
         return wolves.containsKey(uniqueId);
     }
     
     /**
      * Check if loaded wolf exists.
      * 
      * @param wolf
      * @return
      */
     public boolean hasWolf(org.bukkit.entity.Wolf wolf) {
         return hasWolf(wolf.getUniqueId());
     }
     
     /**
      * Check if owner has specific named loaded wolf.
      * 
      * @param name
      * @param owner
      * @return
      */
     public boolean hasWolf(String name, String owner) {
         for (Wolf wolf : getWolves()) {
             return wolf.getName().equalsIgnoreCase(name) && wolf.getOwner().getName().equalsIgnoreCase(owner);
         }
         
         return false;
     }
     
     /**
      * Check if owner has a loaded wolf/wolves.
      * 
      * @param owner
      * @return
      */
     public boolean hasWolf(Player player) {
         for (Wolf wolf : getWolves()) {
             return wolf.getOwner().getName().equalsIgnoreCase(player.getName());
         }
         
         return false;
     }
     
     /**
      * Get wolf.
      * 
      * @param uniqueId
      * @return
      */
     public Wolf getWolf(UUID uniqueId) {
         if (!hasWolf(uniqueId)) {
             addWolf(WolfUtil.getBukkitWolf(uniqueId));
         }
         
         return wolves.get(uniqueId);
     }
     
     /**
      * Get wolf.
      * 
      * @param Wolf
      * @return
      */
     public Wolf getWolf(org.bukkit.entity.Wolf wolf) {
         return getWolf(wolf.getUniqueId());
     }
     
     /**
      * Get wolf.
      * 
      * @param name
      * @param owner
      * @return
      */
     public Wolf getWolf(String name, String owner) {
         for (Wolf wolf : getWolves()) {
             if (hasWolf(wolf.getUniqueId())) {
                 return wolf;
             }
         }
         
         return null;
     }
     
     /**
      * Get wolves.
      * 
      * @return
      */
     public List<Wolf> getWolves() {    
         return new ArrayList<Wolf>(wolves.values());
     }
     
     /**
      * Get wolves for world.
      * 
      * @param world
      * @return
      */
     public List<Wolf> getWolves(World world) {    
         List<Wolf> wolves = new ArrayList<Wolf>();
         
         for (Wolf wolf : getWolves()) {
             if (wolf.getWorld().equals(world)) {
                 wolves.add(wolf);
             }
         }
             
         return wolves;
     }
     
     /**
      * Get wolves for player.
      * 
      * @param player
      * @return
      */
     public List<Wolf> getWolves(Player player) {
         List<Wolf> wolves = new ArrayList<Wolf>();
         
         for (Wolf wolf : getWolves(player.getWorld())) {
             if (wolf.getOwner().getName().equals(player.getName())) {
                 wolves.add(wolf);
             }
         }
         
         return wolves;
     }
     
     /**
      * Generate the table of premade wolf names.
      */
     private void initRandomNames() {  
         try {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(WolfManager.class.getResourceAsStream("wolfNames.txt")));
            
             while (true) {
                 String s1;
                 
                 if ((s1 = bufferedReader.readLine()) == null) {
                     break;
                 }
                 
                 s1 = s1.trim();
                 
                 if (s1.length() > 0) {
                     wolfNames.add(s1);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Generate a random name.
      * 
      * @return String
      */
     public String getRandomName() {
 //        Random random = new Random();
         
 //        return wolfNames.get(random.nextInt(wolfNames.size() - 1));
     	
     	return "Wolf";
     }
     
     /**
      * Spawn a wolf.
      * 
      * @param location
      * @param player
      * @return
      */
     public org.bukkit.entity.Wolf spawnWolf(Location location, Player player) {
         World world = location.getWorld();
         org.bukkit.entity.Wolf wolf = (org.bukkit.entity.Wolf) world.spawnCreature(location, CreatureType.WOLF);
         
         wolf.setTamed(true);
         wolf.setOwner(player);
         
         return wolf;
     }
     
     /**
      * Release a wolf.
      * 
      * @param wolf
      */
     public void releaseWolf(org.bukkit.entity.Wolf wolf) {
         if (hasWolf(wolf)) {
             Wolf wolf1 = getWolf(wolf);
             
             // Drop inventory contents id wolf have inventory.
             if (wolf1.hasLoadedInventory()) {
                 wolf1.dropInventory();
             }
             
             removeWolf(wolf);
         }
         
         // Set wolf to wild.
         wolf.setTamed(false);
         
         // TODO: Set wild wolf health.
     }
 }
