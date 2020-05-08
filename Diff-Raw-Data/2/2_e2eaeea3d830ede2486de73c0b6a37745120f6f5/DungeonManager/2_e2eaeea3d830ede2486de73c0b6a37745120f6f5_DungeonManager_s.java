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
 import java.util.List;
 import java.util.Map;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.Dungeon;
 import de.minestar.castaway.data.SingleSign;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
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
         ConsoleUtils.printInfo(CastAwayCore.NAME, "Loaded " + dungeonIDMap.size() + " dungeons from database");
 
         if (dungeonIDMap.isEmpty())
             return;
 
         // LOAD REGISTERED BLOCKS FROM DATABASE
         StringBuilder sBuilder = new StringBuilder("Loaded Dungeons: ");
         List<AbstractActionBlock> list = null;
         List<SingleSign> signList = null;
         for (Dungeon dungeon : dungeonIDMap.values()) {
             list = CastAwayCore.databaseManager.loadRegisteredActionBlocks(dungeon);
             signList = CastAwayCore.databaseManager.loadAllSigns(dungeon);
             dungeon.registerBlocks(list);
             dungeon.registerSigns(signList);
 
             sBuilder.append(dungeon.getName());
             sBuilder.append('(');
             sBuilder.append(list.size());
             sBuilder.append(" ActionBlocks ");
             sBuilder.append("), ");
 
             dungeonNameMap.put(dungeon.getName().toLowerCase(), dungeon);
         }
         ConsoleUtils.printInfo(CastAwayCore.NAME, sBuilder.substring(0, sBuilder.length() - 2));
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
         CastAwayCore.databaseManager.deleteInheritedSigns(dungeon);
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
         return dungeonNameMap.get(name.toLowerCase());
     }
 
     public Dungeon getDungeon(int id) {
         return dungeonIDMap.get(id);
     }
 
     public boolean exist(String dungeonName) {
         return dungeonNameMap.containsKey(dungeonName.toLowerCase());
     }
 
     public boolean addWinner(Dungeon dungeon, String playerName, long time) {
         return CastAwayCore.databaseManager.addWinner(dungeon, playerName, time);
     }
 }
