 /* *
 
  * HiddenSwitch - Hidden switches and buttons for Bukkit 
  * Copyright (C) 2011-2012  Luphie (devLuphie) luphie@lumpcraft.com
 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
  * */package lc.Luphie.hiddenswitch.conf;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import lc.Luphie.hiddenswitch.HiddenSwitch;
 import lc.Luphie.hiddenswitch.utilities.KeyBlock;
 
 /**
  * @author Luphie
  *
  */
 public class DatabaseHandler {
 
 	private Connection connection;
 	private Statement statement;
 	private HiddenSwitch me;
 	private PreparedStatement prepIns;
 	private PreparedStatement prepDel;
 	private int updatesI = 0;
 
 	public DatabaseHandler() {
 
 		me = HiddenSwitch.instance;
 		
 		try {
 
 			Class.forName("org.sqlite.JDBC");
 			connection = DriverManager.getConnection("jdbc:sqlite:"+me.getDataFolder().getPath()+"\\data.db");
 			statement = connection.createStatement();
 			statement.executeUpdate("CREATE TABLE IF NOT EXISTS blocks (idstring TEXT, world TEXT, x INTEGER, y INTEGER, z INTEGER, user TEXT, key TEXT, owner TEXT)");
 
 			connection.setAutoCommit(false);
 			
 			prepIns = connection.prepareStatement("INSERT INTO blocks VALUES (?,?,?,?,?,?,?,?);");
 			prepDel = connection.prepareStatement("DELETE FROM blocks WHERE idstring=?;");
 			
 		} catch (Exception e) {
 
 			me.log.severe(HiddenSwitch.logName + me.lang.getLang().getString("language.errors.cannotdatabase"));
 			me.log.severe(HiddenSwitch.logName + e.getMessage());
 			e.printStackTrace();
 			
 		}
 		
 		load();
 		
 	}
 
 	/**
 	 * Grabs every entry in the blocks table.
 	 *
 	 * @return ResultSet containing every KeyBlock in the database.
 	 */
 	public ResultSet load(){
 
 		try {
 
 			return statement.executeQuery("SELECT * FROM blocks");
 
 		} catch (SQLException e) {
 
 			me.log.severe(HiddenSwitch.logName + me.lang.getLang().getString("language.errors.cannotloadkeyblocks"));
 			me.log.severe(HiddenSwitch.logName + e.getMessage());
 			e.printStackTrace();
 			
 		}
 		return null;
 	}
 
 	/**
 	 * Delete a KeyBlock record from the database.
 	 * 
 	 * @param String
 	 *            stringid - the string id belonging to the record that is being
 	 *            deleted.
 	 */
 	public void dropRecord(String stringid) {
 
 		try {
 		
 			prepDel.setString(1, stringid);
 			prepDel.executeUpdate();
 		
 		} catch (Exception e) {
 
 			me.log.severe(HiddenSwitch.logName + me.lang.getLang().getString("language.errors.cannotprepsqlstatement"));
 			me.log.severe(HiddenSwitch.logName + e.getMessage());
 			e.printStackTrace();
 			return;
 
 		}
 		
 		updatesI++;
 		updates();
 	}
 
 	/**
 	 * Delete a KeyBlock record from the database.
 	 * 
 	 * @param KeyBlock
 	 *            keyblock - the block that will be removed from the database.
 	 */
 	public void dropRecord(KeyBlock keyblock) {
 		dropRecord(keyblock.id);
 	}
 	
 	/**
 	 * Insert the values from an instance of KeyBlock into a new row in the
 	 * database.
 	 * 
 	 * @param KeyBlock
 	 *            The KeyBlock instance to pull the data from
 	 */
 	public void newRecord(KeyBlock key) {
 
 		newRecord(
 				key.id,
 				key.world,
 				key.x,
 				key.y,
 				key.z,
 				key.users,
 				key.key,
 				key.owner);
 
 	}
 	
 	/**
 	 * Insert a new record into the database with the provided info.
 	 * 
 	 * @param idstring
 	 *            the idstring
 	 * @param world
 	 *            The name of the world the KeyBlock is in.
 	 * @param x
 	 *            The x coordinate of the KeyBlock.
 	 * @param y
 	 *            The y coordinate of the KeyBlock.
 	 * @param z
 	 *            The z coordinate of the KeyBlock.
 	 * @param user
 	 *            A comma seperated list of users who are allowed to use this
 	 *            KeyBlock.
 	 * @param key
 	 *            The item that must be held to use this KeyBlock.
 	 * @param owner
 	 *            The name of the player that created this KeyBlock.
 	 */
 	public void newRecord(String idstring, String world, int x, int y, int z,
 			String user, String key, String owner) {
 
 			try {
 				
 				prepIns.setString(1, idstring);
 				prepIns.setString(2, world);
 				prepIns.setInt(3, x);
 				prepIns.setInt(4, y);
 				prepIns.setInt(5, z);
 				prepIns.setString(6, user);
 				prepIns.setString(7, key);
 				prepIns.setString(8, owner);
 				
 				prepIns.executeUpdate();
 
 				
 			} catch (SQLException e) {
 
 				me.log.severe(HiddenSwitch.logName + me.lang.getLang().getString("language.errors.cannotprepsqlstatement"));
 				me.log.severe(HiddenSwitch.logName + e.getMessage());
 				e.printStackTrace();
 				return;
 
 			}
 			updatesI++;
 			updates();
 	}
 	
 	
 	/**
 	 * Checks to see if the number of open PreparedStatements is greater than or
 	 * equal to the autosave value set in the configuration file. If it is equal
 	 * to or greater, then call the {@link saveAll()} method and save the
 	 * 'floating' KeyBlocks to the database.
 	 */
 	private void updates() {
 
		if(this.updatesI >= me.getConfig().getInt("lchs.dbcontrol.autosave")) {
 			saveAll();
 		}
 
 	}
 	
 	/**
 	 * Commits all open PreparedStatements.
 	 */
 	public void saveAll() {
 
 		try {
 			connection.commit();
 		} catch (SQLException e) {
 			
 			me.log.severe(HiddenSwitch.logName + "Could not write updates to database!");
 			me.log.severe(HiddenSwitch.logName + e.getMessage());
 			e.printStackTrace();
 			return;
 			
 		}
 
 		if(HiddenSwitch.debug) {
 
 			me.log.info(HiddenSwitch.logName + "Wrote " + Integer.toString(updatesI) + " changes to the database.");
 
 		}
 		
 		// Reset the counter
 		updatesI = 0;
 	}
 }
