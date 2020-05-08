 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
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
 
 package com.halvors.lupi.wolf.inventory;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import com.halvors.lupi.Lupi;
 
 /**
  * Handle WolfInventory's
  * 
  * @author halvors
  */
 public class WolfInventoryManager {
     private final static HashMap<UUID, WolfInventory> wolfInventorys = new HashMap<UUID, WolfInventory>();
     
     /**
      * Get a WolfInventoryTable
      * 
      * @param uniqueId
      * @return WolfInventoryTable
      */
     public static WolfInventoryTable getWolfInventoryTable(UUID uniqueId) {
        return Lupi.getDb().find(WolfInventoryTable.class).where()
             .ieq("uniqueId", uniqueId.toString()).findUnique();
     }
     
     /**
      * Get all WolfInventoryTables
      * 
      * @return List<WolfInventoryTable>
      */
     public static List<WolfInventoryTable> getWolfInventoryTables() {
        return Lupi.getDb().find(WolfInventoryTable.class).where().findList();
     }
     
     /**
      * Load inventorys from database
      */
     public static void load() {
         List<WolfInventoryTable> wits = getWolfInventoryTables();
     
         for (WolfInventoryTable wit : wits) {
             addWolfInventory(UUID.fromString(wit.getUniqueId()), loadWolfInventory(wit));
         } 
     }
     
     /**
      * Unload inventorys from database
      */
     public static void unload() {
         for (WolfInventory wi : wolfInventorys.values()) {
             WolfInventoryTable wit = getWolfInventoryTable(wi.getUniqueId());
             
             if (wit != null) {
                 String[] rows = wi.prepareTableForDB();
                 wit.setChestRows(rows);
                 
                Lupi.getDb().update(wit);
             } else {
                 wit = new WolfInventoryTable();
                 wit.setUniqueId(wi.getUniqueId().toString());
                 String[] rows = wi.prepareTableForDB();
                 wit.setChestRows(rows);
                 
                Lupi.getDb().save(wit);
             }
         }
     }
     
     public static WolfInventory loadWolfInventory(WolfInventoryTable wit) {
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
     public static void addWolfInventory(UUID uniqueId, WolfInventory wi) {
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
     public static void addWolfInventory(UUID uniqueId, String name) {
         WolfInventory wi = new WolfInventory(uniqueId, name);
         
         addWolfInventory(uniqueId, wi);
     }
     
     /**
      * Remove a WolfInventory
      * 
      * @param uniqueId
      */
     public static void removeWolfInventory(UUID uniqueId) {
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
     public static boolean hasWolfInventory(UUID uniqueId) {
         return wolfInventorys.containsKey(uniqueId);
     }
     
     /**
      * Get wolf's inventory
      * 
      * @param uniqueId
      * @return
      */
     public static WolfInventory getWolfInventory(UUID uniqueId) {
         return wolfInventorys.get(uniqueId);
     }
 }
