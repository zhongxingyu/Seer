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
 
 package de.minestar.castaway.database;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.ActionBlockType;
 import de.minestar.castaway.data.BlockVector;
 import de.minestar.castaway.data.Dungeon;
 import de.minestar.castaway.data.SingleSign;
 import de.minestar.castaway.data.Winner;
 import de.minestar.minestarlibrary.database.AbstractMySQLHandler;
 import de.minestar.minestarlibrary.database.DatabaseUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class DatabaseManager extends AbstractMySQLHandler {
 
     public DatabaseManager(String pluginName, File SQLConfigFile) {
         super(pluginName, SQLConfigFile);
     }
 
     @Override
     protected void createStructure(String pluginName, Connection con) throws Exception {
         DatabaseUtils.createStructure(DatabaseManager.class.getResourceAsStream("/structure.sql"), con, pluginName);
     }
 
     @Override
     protected void createStatements(String pluginName, Connection con) throws Exception {
 
         /* DUNGEON */
 
         addDungeon = con.prepareStatement("INSERT INTO dungeon (name, creator, optionMask) VALUES (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
 
         deleteDungeon = con.prepareStatement("DELETE FROM dungeon WHERE id = ?");
 
         updateDungeonOption = con.prepareStatement("UPDATE dungeon SET optionMask = ? WHERE id = ?");
 
         addWinner = con.prepareStatement("INSERT INTO winner ( playerName, dungeon, date ) VALUES ( ?, ?, NOW() )");
 
         addHighScore = con.prepareStatement("INSERT INTO highscore ( player, dungeon, time, date ) VALUES ( ?, ?, ?, NOW() )");
 
         isWinner = con.prepareStatement("SELECT 1 FROM winner WHERE dungeon = ? AND playerName = ?");
 
         getWinner = con.prepareStatement("SELECT playerName, date FROM winner WHERE dungeon = ? ORDER BY date ASC LIMIT ?");
 
         /* ACTION BLOCKS */
 
         addActionBlock = con.prepareStatement("INSERT INTO actionBlock (dungeon, x, y, z, world, actionType) VALUES (?, ?, ?, ?, ?, ?)");
 
         deleteSingleRegisteredBlock = con.prepareStatement("DELETE FROM actionBlock WHERE dungeon = ? AND x = ? AND y = ? AND z = ? AND world = ?");
 
         deleteRegisteredBlocks = con.prepareStatement("DELETE FROM actionBlock WHERE dungeon = ?");
 
         /* SIGNS */
 
         addSign = con.prepareStatement("INSERT INTO sign (dungeon, x, y, z, world, subID) VALUES (?, ?, ?, ?, ?, ?)");
 
         loadSigns = con.prepareStatement("SELECT * FROM sign WHERE dungeon = ? ORDER BY ID ASC");
 
         deleteSingleSign = con.prepareStatement("DELETE FROM sign WHERE dungeon = ? AND x = ? AND y = ? AND z = ? AND world = ?");
 
         deleteInheritedSigns = con.prepareStatement("DELETE FROM sign WHERE dungeon = ?");
     }
     // ***************
     // *** DUNGEON ***
     // ***************
 
     private PreparedStatement addDungeon;
     private PreparedStatement deleteDungeon;
     private PreparedStatement updateDungeonOption;
     private PreparedStatement addWinner;
     private PreparedStatement addHighScore;
     private PreparedStatement isWinner;
     private PreparedStatement getWinner;
 
     public Map<Integer, Dungeon> loadDungeon() {
 
         Map<Integer, Dungeon> dungeonMap = new HashMap<Integer, Dungeon>();
         try {
             Statement stat = dbConnection.getConnection().createStatement();
            ResultSet rs = stat.executeQuery("SELECT id, name, creator, optionMask FROM dungeon");
 
             // TEMP VARIABLES
             int id;
             String name;
             String creator;
            int optionMask;
 
             while (rs.next()) {
                 id = rs.getInt(1);
                 name = rs.getString(2);
                 creator = rs.getString(3);
                optionMask = rs.getInt(4);
                dungeonMap.put(id, new Dungeon(id, name, creator, optionMask));
             }
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't load dungeons from database!");
             dungeonMap.clear();
         }
 
         return dungeonMap;
     }
 
     public boolean addDungeon(Dungeon dungeon) {
 
         try {
             addDungeon.setString(1, dungeon.getName());
             addDungeon.setString(2, dungeon.getAuthor());
             addDungeon.setInt(3, 0);
 
             addDungeon.executeUpdate();
 
             ResultSet rs = addDungeon.getGeneratedKeys();
 
             int id = 0, options = 0;
             if (rs.next()) {
                 id = rs.getInt(1);
                 options = rs.getInt(3);
                 dungeon.setID(id);
                 dungeon.setOptionMask(options);
                 return true;
             } else {
                 ConsoleUtils.printError(CastAwayCore.NAME, "Can't get the id for the dungeon = " + dungeon);
                 return false;
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't insert the dungeon = " + dungeon);
             return false;
         }
     }
 
     public boolean updateDungeonOption(Dungeon dungeon) {
         try {
             updateDungeonOption.setInt(1, dungeon.getOptionMask());
             updateDungeonOption.setInt(2, dungeon.getID());
             return updateDungeonOption.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't update the dungeonoptions = " + dungeon);
             return false;
         }
     }
 
     public boolean deleteDungeon(Dungeon dungeon) {
 
         try {
             deleteDungeon.setInt(1, dungeon.getID());
             return deleteDungeon.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't delete the dungeon = " + dungeon);
             return false;
         }
     }
 
     public boolean addWinner(Dungeon dungeon, String playerName, long time) {
         try {
 
             // PLAYER HAS NEVER BEATEN THIS DUNGEON BEFORE
             // STORE HIM ONCE IN WINNER TABLE
             if (!isWinner(dungeon, playerName)) {
                 addWinner.setString(1, playerName);
                 addWinner.setInt(2, dungeon.getID());
                 if (addWinner.executeUpdate() != 1) {
                     ConsoleUtils.printError(CastAwayCore.NAME, "Can't add a '" + playerName + "' to the winner table!");
                     return false;
                 }
             }
 
             // STORE CURRENT RUN
             addHighScore.setString(1, playerName);
             addHighScore.setInt(2, dungeon.getID());
             addHighScore.setLong(3, time);
 
             return addHighScore.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't add a the winner '" + playerName + "' for the dungeon = " + dungeon);
             return false;
         }
     }
 
     public boolean isWinner(Dungeon dungeon, String playerName) {
         try {
             isWinner.setInt(1, dungeon.getID());
             isWinner.setString(2, playerName);
             return isWinner.executeQuery().next();
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't check if '" + playerName + "' is a winner of the dungeon " + dungeon);
             return false;
         }
     }
 
     public List<Winner> getWinner(Dungeon dungeon, int topX) {
         List<Winner> winnerList = new LinkedList<Winner>();
 
         try {
             // FILL QUERY
             getWinner.setInt(1, dungeon.getID());
             getWinner.setInt(2, topX);
 
             // EXECUTE QUERY
             ResultSet rs = getWinner.executeQuery();
 
             String playerName;
             Timestamp date;
             int position = 1;
             while (rs.next()) {
                 // GET VALUES
                 playerName = rs.getString(1);
                 date = rs.getTimestamp(2);
 
                 // CREATE NEW WINNER
                 winnerList.add(new Winner(playerName, date.getTime(), position++, dungeon));
             }
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't load the " + topX + " winner of dungeon " + dungeon + "!");
             winnerList.clear();
         }
 
         return winnerList;
     }
 
     // *********************
     // *** ACTION_BLOCKS ***
     // *********************
 
     private PreparedStatement addActionBlock;
     private PreparedStatement deleteSingleRegisteredBlock;
     private PreparedStatement deleteRegisteredBlocks;
 
     public List<AbstractActionBlock> loadRegisteredActionBlocks(Dungeon dungeon) {
 
         List<AbstractActionBlock> actionBlockList = new LinkedList<AbstractActionBlock>();
         try {
             Statement stat = dbConnection.getConnection().createStatement();
             ResultSet rs = stat.executeQuery("SELECT id, dungeon, x, y, z, world, actionType FROM actionBlock WHERE dungeon = " + dungeon.getID());
 
             // TEMP VARS
 //            int id;
 //            int dungeonID;
             int x;
             int y;
             int z;
             String world;
             int actionType;
             BlockVector bVector;
             Class<? extends AbstractActionBlock> clazz;
             Constructor<?> constructor;
 
             while (rs.next()) {
                 // GET VALUES
 //                id = rs.getInt(1);
 //                dungeonID = rs.getInt(2)
                 x = rs.getInt(3);
                 y = rs.getInt(4);
                 z = rs.getInt(5);
                 world = rs.getString(6);
                 // CHECK IF WORLD EXISTS
                 if (Bukkit.getWorld(world) == null) {
                     ConsoleUtils.printWarning(CastAwayCore.NAME, "The world '" + world + "' was not found!");
                     continue;
                 }
                 bVector = new BlockVector(world, x, y, z);
 
                 // GET ACTION
                 actionType = rs.getInt(7);
 
                 // GET CLASS FOR THE ACTION TYPE
                 clazz = ActionBlockType.get(actionType).getClazz();
                 if (clazz == null) {
                     ConsoleUtils.printWarning(CastAwayCore.NAME, "Unknown action type id '" + actionType + "'!");
                     continue;
                 }
 
                 // CREATE AN INSTANCE OF THIS CLASS
                 constructor = clazz.getDeclaredConstructor(BlockVector.class, Dungeon.class);
                 AbstractActionBlock block = (AbstractActionBlock) constructor.newInstance(bVector, dungeon);
 
                 actionBlockList.add(block);
             }
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't load action blocks from database for dungeon = " + dungeon);
             actionBlockList.clear();
         }
 
         return actionBlockList;
     }
 
     public boolean addActionBlock(AbstractActionBlock actionBlock) {
         try {
             addActionBlock.setInt(1, actionBlock.getDungeon().getID());
             addActionBlock.setInt(2, actionBlock.getVector().getX());
             addActionBlock.setInt(3, actionBlock.getVector().getY());
             addActionBlock.setInt(4, actionBlock.getVector().getZ());
             addActionBlock.setString(5, actionBlock.getVector().getWorldName());
             addActionBlock.setInt(6, actionBlock.getBlockType().getID());
             return addActionBlock.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't add action block to database! ActionBlock = " + actionBlock);
             return false;
         }
     }
 
     public boolean deleteSingleRegisteredBlock(AbstractActionBlock actionBlock) {
         // DELETE FROM actionBlock WHERE dungeon = ? AND x = ? AND y = ? AND z =
         // ? AND world = ?
         try {
             deleteSingleRegisteredBlock.setInt(1, actionBlock.getDungeon().getID());
             deleteSingleRegisteredBlock.setInt(2, actionBlock.getVector().getX());
             deleteSingleRegisteredBlock.setInt(3, actionBlock.getVector().getY());
             deleteSingleRegisteredBlock.setInt(4, actionBlock.getVector().getZ());
             deleteSingleRegisteredBlock.setString(5, actionBlock.getVector().getWorldName());
 
             return deleteSingleRegisteredBlock.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't delete single registered blocks from database! ActionBlock =" + actionBlock);
             return false;
         }
     }
 
     public boolean deleteRegisteredBlocks(Dungeon dungeon) {
         try {
             deleteRegisteredBlocks.setInt(1, dungeon.getID());
 
             return deleteRegisteredBlocks.executeUpdate() >= 0;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't delete registered blocks from database! Dungeon = " + dungeon);
             return false;
         }
     }
 
     // *********************
     // *** SIGNS ***
     // *********************
 
     private PreparedStatement loadSigns;
     private PreparedStatement addSign;
     private PreparedStatement deleteSingleSign;
     private PreparedStatement deleteInheritedSigns;
 
     public List<SingleSign> loadAllSigns(Dungeon dungeon) {
         List<SingleSign> signList = new LinkedList<SingleSign>();
         try {
             this.loadSigns.setInt(1, dungeon.getID());
             ResultSet resultSet = this.loadSigns.executeQuery();
 
             // TEMP VARS
             int x;
             int y;
             int z;
             String world;
             byte subID;
             SingleSign sign;
             BlockVector bVector;
             while (resultSet != null && resultSet.next()) {
                 // GET VALUES
                 x = resultSet.getInt("x");
                 y = resultSet.getInt("y");
                 z = resultSet.getInt("z");
                 world = resultSet.getString("world");
                 subID = resultSet.getByte("subID");
 
                 // CHECK IF WORLD EXISTS
                 if (Bukkit.getWorld(world) == null) {
                     ConsoleUtils.printWarning(CastAwayCore.NAME, "The world '" + world + "' was not found!");
                     continue;
                 }
 
                 // create sign
                 bVector = new BlockVector(world, x, y, z);
                 sign = new SingleSign(dungeon, bVector, subID);
                 signList.add(sign);
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't load signs from database for dungeon = " + dungeon);
             signList.clear();
         }
         return signList;
     }
 
     public boolean addSign(SingleSign sign) {
         try {
             addSign.setInt(1, sign.getDungeon().getID());
             addSign.setInt(2, sign.getVector().getX());
             addSign.setInt(3, sign.getVector().getY());
             addSign.setInt(4, sign.getVector().getZ());
             addSign.setString(5, sign.getVector().getWorldName());
             addSign.setInt(6, sign.getSubData());
             return addSign.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't add sign to database! Sign = " + sign);
             return false;
         }
     }
 
     public boolean deleteSingleSign(SingleSign sign) {
         // DELETE FROM sign WHERE dungeon = ? AND x = ? AND y = ? AND z =
         // ? AND world = ?
         try {
             deleteSingleSign.setInt(1, sign.getDungeon().getID());
             deleteSingleSign.setInt(2, sign.getVector().getX());
             deleteSingleSign.setInt(3, sign.getVector().getY());
             deleteSingleSign.setInt(4, sign.getVector().getZ());
             deleteSingleSign.setString(5, sign.getVector().getWorldName());
 
             return deleteSingleSign.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't delete single sign from database! Sign =" + sign);
             return false;
         }
     }
 
     public boolean deleteInheritedSigns(Dungeon dungeon) {
         try {
             deleteInheritedSigns.setInt(1, dungeon.getID());
             return deleteInheritedSigns.executeUpdate() >= 0;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't delete inherited signs from database! Dungeon = " + dungeon);
             return false;
         }
     }
 }
