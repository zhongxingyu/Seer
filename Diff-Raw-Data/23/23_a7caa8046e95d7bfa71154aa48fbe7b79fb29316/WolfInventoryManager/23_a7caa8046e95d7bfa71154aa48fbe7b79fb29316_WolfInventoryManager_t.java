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
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import com.halvors.wolf.WolfPlugin;
 
 /**
  * Handle WolfInventory's
  * 
  * @author halvors
  */
 public class WolfInventoryManager {
     private final WolfPlugin plugin;
     
     private final HashMap<UUID, WolfInventory> wolfInventorys;
     
     public WolfInventoryManager(final WolfPlugin plugin) {
         this.plugin = plugin;
         this.wolfInventorys = new HashMap<UUID, WolfInventory>();
     }
     
     /**
      * Get a WolfInventoryTable
      * 
      * @param uniqueId
      * @return WolfInventoryTable
      */
     public WolfInventoryTable getWolfInventoryTable(UUID uniqueId) {
         return plugin.getDatabase().find(WolfInventoryTable.class).where()
             .eq("uniqueId", uniqueId.toString()).findUnique();
     }
     
     /**
      * Get all WolfInventoryTables
      * 
      * @return List<WolfInventoryTable>
      */
     public List<WolfInventoryTable> getWolfInventoryTables() {
         return plugin.getDatabase().find(WolfInventoryTable.class).where().findList();
     }
     
     /**
      * Load inventorys from database
      */
     public void load() {
         List<WolfInventoryTable> wits = getWolfInventoryTables();
     
         for (WolfInventoryTable wit : wits) {
             addWolfInventory(UUID.fromString(wit.getUniqueId()), loadWolfInventory(wit));
         } 
     }
     
     /**
      * Save inventorys to database
      */
     public void save() {
         for (WolfInventory wi : wolfInventorys.values()) {
             WolfInventoryTable wit = getWolfInventoryTable(wi.getUniqueId());
             
             if (wit != null) {
                 String[] rows = wi.prepareTableForDB();
                 wit.setChestRows(rows);
                 
                 plugin.getDatabase().update(wit);
             } else {
                 wit = new WolfInventoryTable();
                 wit.setUniqueId(wi.getUniqueId().toString());
                 String[] rows = wi.prepareTableForDB();
                 wit.setChestRows(rows);
                 
                 plugin.getDatabase().save(wit);
             }
         }
     }
     
     public WolfInventory loadWolfInventory(WolfInventoryTable wit) {
         WolfInventory wi = new WolfInventory(UUID.fromString(wit.getUniqueId()));
         wi.fillFromDBTable(wit.getChestRows());
         
         return wi;
     }
     
     /**
      * Add a WolfInventory
      * 
      * @param uniqueId
      * @param wi
      */
     public void addWolfInventory(UUID uniqueId, WolfInventory wi) {
     	if (wolfInventorys.containsKey(uniqueId)) {
             wolfInventorys.remove(uniqueId);
         }
         
         wolfInventorys.put(uniqueId, wi);
     }
     
     /**
      * Add WolfInventory
      * 
      * @param uniqueId
      */
     public void addWolfInventory(UUID uniqueId) {
         addWolfInventory(uniqueId, new WolfInventory(uniqueId));
     }
     
     /**
      * Remove a WolfInventory
      * 
      * @param uniqueId
      */
     public void removeWolfInventory(UUID uniqueId) {
         if (wolfInventorys.containsKey(uniqueId)) {
             wolfInventorys.remove(uniqueId);
         }
     }
     
     /**
      * Check if wolf has inventory
      * 
      * @param uniqueId
      * @return
      */
     public boolean hasWolfInventory(UUID uniqueId) {
         return wolfInventorys.containsKey(uniqueId);
     }
     
     /**
      * Get wolf's inventory
      * 
      * @param uniqueId
      * @return
      */
     public WolfInventory getWolfInventory(UUID uniqueId) {
         return wolfInventorys.get(uniqueId);
     }
 }
