 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of CastAway.
  * 
  * CastAway is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * CastAway is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CastAway.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.castaway.manager;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.Dungeon;
 
 public class DungeonManager {
 
     private Map<Integer, Dungeon> dungeonIDMap;
     private Map<String, Dungeon> dungeonNameMap;
 
     public DungeonManager() {
         dungeonIDMap = new HashMap<Integer, Dungeon>();
         dungeonNameMap = new HashMap<String, Dungeon>();
     }
 
     public void init() {
         loadDungeons();
     }
 
     private void loadDungeons() {
         dungeonIDMap = CastAwayCore.databaseManager.loadDungeon();
         for (Dungeon dungeon : dungeonIDMap.values()) {
             dungeon.registerBlocks(CastAwayCore.databaseManager.loadRegisteredActionBlocks(dungeon));
             dungeonNameMap.put(dungeon.getName().toLowerCase(), dungeon);
         }
     }
 
     public void addDungeon(String dungeonName, String creatorName) {
         Dungeon dungeon = new Dungeon(dungeonName, creatorName);
         CastAwayCore.databaseManager.addDungeon(dungeon);
         this.dungeonIDMap.put(dungeon.getID(), dungeon);
         this.dungeonNameMap.put(dungeon.getName(), dungeon);
     }
 
     public void deleteDungeon(Dungeon dungeon) {
         // REMOVE REFERENCES IN GAME
         CastAwayCore.gameManager.unRegisterBlocks(dungeon.getRegisteredBlocks().values());
 
         // REMOVE DATA FROM DATABASE
         CastAwayCore.databaseManager.deleteRegisteredBlocks(dungeon);
         CastAwayCore.databaseManager.deleteDungeon(dungeon);
 
         // REMOVE REFERENCES IN THIS MANAGER
         this.dungeonIDMap.remove(dungeon.getID());
         this.dungeonNameMap.remove(dungeon.getName());
     }
 
     public Collection<Dungeon> getDungeons() {
         return dungeonIDMap.values();
     }
 
     public Dungeon getDungeon(String name) {
        return dungeonNameMap.get(name);
     }
 
     public Dungeon getDungeon(int id) {
         return dungeonIDMap.get(id);
     }
 
     public boolean exist(String dungeonName) {
         return dungeonNameMap.containsKey(dungeonName.toLowerCase());
     }
 
     public void addWinner(Dungeon dungeon, String playerName, long time) {
 
         CastAwayCore.databaseManager.addWinner(dungeon, playerName, time);
     }
 }
