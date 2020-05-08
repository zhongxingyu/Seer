 /**
  *  Name:    ConcurrentSQLiteConnection.java
  *  Created: 17:37:33 - 8 maj 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  * 
  */
 
 package me.lucasemanuel.publiceconomy.threading;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import me.lucasemanuel.publiceconomy.Main;
 import me.lucasemanuel.publiceconomy.utils.SerializedLocation;
 
 public class ConcurrentSQLiteConnection {
 	
 	private String folder;
 	private Connection con;
 	
 	public ConcurrentSQLiteConnection(Main instance) {
 		folder = "" + instance.getDataFolder();
 		getConnection();
 	}
 	
 	private synchronized void testConnection() {
 		try {
 			con.getCatalog();
 		}
 		catch(SQLException e) {
 			System.out.println("Connection no longer valid! Trying to re-establish one...");
 			getConnection();
 		}
 	}
 	
 	private synchronized void getConnection() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			con = DriverManager.getConnection("jdbc:sqlite:" + folder + "/data.sqlite");
 			
 			Statement stmt = con.createStatement();
 			
 			stmt.execute("CREATE TABLE IF NOT EXISTS shopchests (serial_position VARHCAR(250) NOT NULL PRIMARY KEY)");
 			stmt.execute("CREATE TABLE IF NOT EXISTS player_accounts (playername VARHCAR(250) NOT NULL PRIMARY KEY, balance DOUBLE(255,2))");
 			
 			stmt.close();
 		}
 		catch(ClassNotFoundException | SQLException e) {
 			System.out.println("WARNING! SEVERE ERROR! Could not connect to SQLite-database in plugin-datafolder! This means it cannot load/store important data!");
 			System.out.println("Error message: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void closeConnection() {
 		try {
 			con.close();
 		}
 		catch (SQLException e) {
 			System.out.println("Error while closing connection, data might have been lost! Message: " + e.getMessage());
 		}
 	}
 
 	public synchronized void registerChest(SerializedLocation l) {
 		testConnection();
 		
 		try {
 			Statement stmt = con.createStatement();
 			stmt.execute("INSERT OR IGNORE INTO shopchests VALUES('" + l.toString() + "')");
 			
 			stmt.close();
 		}
 		catch (SQLException e) {
 			System.out.println("Error while registering shopchest! Message: " + e.getMessage());
 		}
 	}
 
 	public synchronized HashSet<String> loadChests() {
 		testConnection();
 		
 		HashSet<String> locations = new HashSet<String>();
 		
 		try {
 			Statement stmt = con.createStatement();
 			
 			ResultSet rs = stmt.executeQuery("SELECT * FROM shopchests");
 			
 			while(rs.next()) {
 				locations.add(rs.getString(1));
 			}
 			
 			rs.close();
 			stmt.close();
 		}
 		catch (SQLException e) {
 			System.out.println("Error while loading shopchests! Message: " + e.getMessage());
 			return null;
 		}
 		
 		return locations;
 	}
 
 	public synchronized void updateBalance(String playername, double m) {
 		testConnection();
 		
 		try {
 			Statement stmt = con.createStatement();
 			
 			stmt.executeUpdate("INSERT OR REPLACE INTO player_accounts VALUES('" + playername + "', " + m + ")");
 			
 			stmt.close();
 		}
 		catch (SQLException e) {
 			System.out.println("Error while updating balance for " + playername + "! Message: " + e.getMessage());
 		}
 	}
 
	public synchronized HashMap<String, Double> retrieveAccounts() {
 		testConnection();
 		
 		try {
 			HashMap<String, Double> accounts = new HashMap<String, Double>();
 			
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM player_accounts");
 			
 			while(rs.next()) {
 				accounts.put(rs.getString("playername"), rs.getDouble("balance"));
 			}
 			
 			rs.close();
 			stmt.close();
 			
 			return accounts;
 		}
 		catch (SQLException e) {
 			System.out.println("Error while loading accounts! Message: " + e.getMessage());
 			return null;
 		}
 	}
 }
 
 
 
 
 
