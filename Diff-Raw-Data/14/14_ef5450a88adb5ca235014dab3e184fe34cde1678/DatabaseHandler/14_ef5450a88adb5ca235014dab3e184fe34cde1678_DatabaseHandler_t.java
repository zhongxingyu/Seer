 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of AdminStuff.
  * 
  * AdminStuff is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * AdminStuff is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with AdminStuff.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.AdminStuff.database;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.NoResultException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.ASPlayer;
 import de.minestar.minestarlibrary.database.AbstractDatabaseHandler;
 import de.minestar.minestarlibrary.database.DatabaseConnection;
 import de.minestar.minestarlibrary.database.DatabaseType;
 import de.minestar.minestarlibrary.database.DatabaseUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class DatabaseHandler extends AbstractDatabaseHandler {
 
     // Prepared Statements
     private PreparedStatement setMute;
     private PreparedStatement setBann;
     private PreparedStatement setGod;
     private PreparedStatement setDisplayName;
     private PreparedStatement setMode;
     private PreparedStatement setTempBann;
     private PreparedStatement setLastseen;
     private PreparedStatement setGlueLoc;
 
     private PreparedStatement addPlayer;
 
     private PreparedStatement getPlayerId;
 
     private PreparedStatement getInventory;
     private PreparedStatement deleteInventory;
     // /Prepared Statements
 
     public DatabaseHandler(String pluginName, File dataFolder) {
         super(pluginName, dataFolder);
     }
 
     @Override
     protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
         File configFile = new File(dataFolder, "sqlconfig.yml");
         if (!configFile.exists()) {
             DatabaseUtils.createDatabaseConfig(DatabaseType.MySQL, configFile, pluginName);
             return null;
         } else {
             YamlConfiguration config = new YamlConfiguration();
             config.load(configFile);
             return new DatabaseConnection(pluginName, DatabaseType.MySQL, config);
         }
     }
 
     @Override
     protected void createStructure(String pluginName, Connection con) throws Exception {
         DatabaseUtils.createStructure(getClass().getResourceAsStream("/structure.sql"), con, pluginName);
     }
 
     @Override
     protected void createStatements(String pluginName, Connection con) throws Exception {
         //@formatter:off
         setMute = con.prepareStatement(         "UPDATE player SET muted = ? WHERE accountName = ?");
         setBann = con.prepareStatement(         "UPDATE player SET banned = ? WHERE accountName = ?");
         setGod = con.prepareStatement(          "UPDATE player SET god = ? WHERE accountName = ?");
         setDisplayName = con.prepareStatement(  "UPDATE player SET displayName = ? WHERE accountName = ?");
         setMode = con.prepareStatement(         "UPDATE player SET mode = ? WHERE accountName = ?");
         setTempBann = con.prepareStatement(     "UPDATE player SET tempBann = ? WHERE accountName = ?");
         setLastseen = con.prepareStatement(     "UPDATE player SET lastSeen = NOW() WHERE accountName = ?");
         setGlueLoc = con.prepareStatement(      "UPDATE player SET glueLocation = ? WHERE accountName = ?");
         
         getPlayerId = con.prepareStatement(     "SELECT idplayer FROM player WHERE accountName = ?");
         
         addPlayer = con.prepareStatement(       "INSERT INTO player" +
        		                                "(accountName, displayName, afk, muted, banned, god, mode, tempBann, lastSeen, glueLocation)" +
         		                                "VALUES (" +
        		                                "?,?,FALSE,FALSE,FALSE,FALSE,"+GameMode.SURVIVAL.getValue()+", 0, NULL, NULL)");
         
         deleteInventory = con.prepareStatement( "DELETE FROM inventorybackup WHERE `player_has_inventorybackup`.`player` = ? AND `player_has_inventorybackup`.`inventory` = `inventorybackup`.`idinventory`");
         getInventory = con.prepareStatement(    "SELECT itemId, amount, subId FROM inventorybackup WHERE `player_has_inventorybackup`.`player` = ? AND `player_has_inventorybackup`.`inventory` = `inventorybackup`.`idinventory`");
         //@formatter:on
     }
 
     @Override
     public void updateDatabase(String pluginName, Connection con, File dataFolder) throws Exception {
         File batchFile = new File(dataFolder, "update.sql");
         // no update is needed
         if (!batchFile.exists())
             return;
 
         // import updates
         DatabaseUtils.createStructure(batchFile, con, pluginName);
 
         batchFile.renameTo(new File(dataFolder, "update_done.sql"));
     }
 
     // ******************************************************
     // *************** USER MODIFACTION *********************
     // ******************************************************
 
     public boolean addPlayer(String playerName) {
         try {
             addPlayer.setString(1, playerName);
            addPlayer.setString(2, playerName);
             return addPlayer.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't add player to database!");
             return false;
         }
     }
 
     public boolean saveMuted(String playerName, boolean isMuted) {
         try {
             setMute.setBoolean(1, isMuted);
             setMute.setString(2, playerName);
             return setMute.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update mute mode for player '" + playerName + "'!");
             return false;
         }
     }
 
     public boolean saveBanned(String playerName, boolean isBanned) {
         try {
             setBann.setBoolean(1, isBanned);
             setBann.setString(2, playerName);
             return setBann.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update bann status for player '" + playerName + "'!");
             return false;
         }
     }
 
     public boolean saveGod(String playerName, boolean isGod) {
         try {
             setGod.setBoolean(1, isGod);
             setGod.setString(2, playerName);
             return setGod.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update god mode for player '" + playerName + "'!");
             return false;
         }
     }
 
     public boolean saveDisplayName(String playerName, String displayName) {
         try {
             setDisplayName.setString(1, displayName);
             setDisplayName.setString(2, playerName);
             return setDisplayName.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update display name for player '" + playerName + "'!");
             return false;
         }
     }
 
     public boolean saveGameMode(String playerName, GameMode gameMode) {
         try {
             setMode.setInt(1, gameMode.getValue());
             setMode.setString(2, playerName);
             return setMode.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update game mode for player '" + playerName + "'!");
             return false;
         }
     }
 
     public boolean updateLastSeen(String playerName) {
         try {
             setLastseen.setString(1, playerName);
             return setLastseen.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update last seen for player '" + playerName + "'!");
             return false;
         }
     }
 
     // in a far far galaxy a long time ago ...
     public boolean saveTempBann(String playerName, long time) {
         try {
             setTempBann.setLong(1, time);
             setTempBann.setString(2, playerName);
             return setTempBann.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update temp bann for player '" + playerName + "'!");
             return false;
         }
     }
 
     public boolean saveGlueLocation(String playerName, Location loc) {
         try {
             setGlueLoc.setString(1, convertLocation(loc));
             setGlueLoc.setString(2, playerName);
             return setGlueLoc.executeUpdate() == 1;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't update glue location for player '" + playerName + "'!");
             return false;
         }
     }
 
     // ******************************************************
     // ************** INVENTORY HANDLING ********************
     // ******************************************************
 
     public boolean saveInventory(String playerName, ItemStack[] items) {
         try {
             // add items to database
             String query = getItemQuery(items);
             Statement s = dbConnection.getConnection().createStatement();
             if (s.executeUpdate(query, Statement.RETURN_GENERATED_KEYS) == 0) {
                 ConsoleUtils.printError(Core.NAME, "No item was saved in database!");
                 return false;
             }
             ResultSet rs = s.getGeneratedKeys();
 
             // get player id
             int playerId = getPlayerId(playerName);
 
             // connect items and player
             query = getInvQuery(rs, playerId);
 
             return s.executeUpdate(query) > 0;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't save inventory backup in database!");
             return false;
         }
     }
     // building a dynamic query to connecting items and player
     private String getInvQuery(ResultSet rs, int playerId) throws Exception {
         StringBuilder sBuilder = new StringBuilder("INSERT INTO player_has_inventorybackup() VALUES ");
         while (rs.next()) {
             sBuilder.append('(');
             sBuilder.append(playerId);
             sBuilder.append(',');
             sBuilder.append(rs.getInt(1));
             sBuilder.append(')');
             sBuilder.append(',');
         }
         sBuilder.setCharAt(sBuilder.length() - 1, ';');
         return sBuilder.toString();
     }
 
     // building a dynamic query to store items
     private String getItemQuery(ItemStack[] items) {
         StringBuilder sBuilder = new StringBuilder("INSERT INTO inventorybackup (itemId, subId, amount) VALUES ");
         for (ItemStack item : items) {
             sBuilder.append('(');
             sBuilder.append(item.getTypeId());
             sBuilder.append(',');
             sBuilder.append(item.getDurability());
             sBuilder.append(',');
             sBuilder.append(item.getAmount());
             sBuilder.append(')');
             sBuilder.append(',');
         }
         sBuilder.setCharAt(sBuilder.length() - 1, ';');
         return sBuilder.toString();
     }
 
     public ItemStack[] loadInventory(String playerName) {
         try {
 
             int id = getPlayerId(playerName);
 
             // SELECT itemId, subId, amount
             getInventory.setInt(1, id);
             ResultSet rs = getInventory.executeQuery();
             ArrayList<ItemStack> list = new ArrayList<ItemStack>(36);
             while (rs.next())
                 list.add(new ItemStack(rs.getInt(1), rs.getInt(2), rs.getByte(3)));
 
             if (list.size() == 0)
                 return null;
 
             ItemStack[] items = new ItemStack[list.size()];
             items = list.toArray(items);
             deleteInventory(id);
 
             return items;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load inventory from database!");
             return null;
         }
     }
 
     private boolean deleteInventory(int id) throws Exception {
         deleteInventory.setInt(1, id);
         return deleteInventory.executeUpdate() != 0;
     }
 
     public boolean deleteInventory(String playerName) {
         try {
             int id = getPlayerId(playerName);
 
             return deleteInventory(id);
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't delete inventory from database!");
             return false;
         }
     }
 
     private int getPlayerId(String playerName) throws Exception {
 
         getPlayerId.setString(1, playerName);
         ResultSet rs = getPlayerId.executeQuery();
         if (!rs.next())
             throw new NoResultException("Can't find player id for player '" + playerName + "'!");
 
         return rs.getInt(1);
     }
 
     // ******************************************************
     // ************ LOAD PLAYER FROM DATBASE ****************
     // ******************************************************
 
     public Map<String, ASPlayer> loadPlayer() {
         try {
             Map<String, ASPlayer> map = new HashMap<String, ASPlayer>();
             Statement s = dbConnection.getConnection().createStatement();
             ResultSet result = s.executeQuery("SELECT accountName, displayName, muted, banned, god, mode, tempBann, DATE_FORMAT(probeEndDate, '%d.%m.%Y %H:%i:%s'), gluelocation FROM player");
 
             ASPlayer player = null;
             String accountName = null;
             String displayName = null;
             String lastSeen = null;
             boolean muted = false;
             boolean banned = false;
             boolean god = false;
             long tempBanned = 0L;
             Location glueLocation = null;
             GameMode mode = null;
 
             // load data from query
             while (result.next()) {
                 accountName = result.getString(1);
                 displayName = result.getString(2);
                 muted = result.getBoolean(3);
                 banned = result.getBoolean(4);
                 god = result.getBoolean(5);
                 mode = GameMode.getByValue(result.getInt(6));
                 tempBanned = result.getLong(7);
                 lastSeen = result.getString(8);
                 glueLocation = extractLocation(result.getString(9));
 
                 player = new ASPlayer(accountName, displayName, muted, banned, god, mode, tempBanned, lastSeen, glueLocation);
                 map.put(accountName.toLowerCase(), player);
             }
             return map;
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load ASPlayers from database!");
             return null;
         }
 
     }
 
     // convert location in a storeAble string
     private String convertLocation(Location loc) {
         if (loc == null)
             return null;
 
         StringBuilder sBuilder = new StringBuilder(256);
         sBuilder.append(loc.getWorld().getName());
         sBuilder.append(',');
         sBuilder.append(loc.getX());
         sBuilder.append(',');
         sBuilder.append(loc.getY());
         sBuilder.append(',');
         sBuilder.append(loc.getZ());
         sBuilder.append(',');
         sBuilder.append(loc.getPitch());
         sBuilder.append(',');
         sBuilder.append(loc.getYaw());
         return sBuilder.toString();
     }
 
     // extract location from database string
     private Location extractLocation(String input) {
         if (input == null)
             return null;
 
         String[] args = input.split(",");
         World w = Bukkit.getWorld(args[0]);
         if (w == null) {
             ConsoleUtils.printError(Core.NAME, "World '" + args[0] + "' doesn't exist!");
             return null;
         }
         return new Location(w, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
     }
 
 }
