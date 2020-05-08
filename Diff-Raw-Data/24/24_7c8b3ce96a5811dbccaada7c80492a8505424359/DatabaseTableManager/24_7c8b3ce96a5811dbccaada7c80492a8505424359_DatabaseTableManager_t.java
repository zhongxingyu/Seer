 package com.minecraftdimensions.bungeesuite.managers;
 
 import java.sql.SQLException;
 
 
 public class DatabaseTableManager {
 	
 	public static void runTableQuery(String name, String query) throws SQLException {
 		boolean tableExists = false;
 		tableExists = SQLManager.doesTableExist(name);
 		if (!tableExists) {
 			SQLManager.standardQuery(query);
 		}
 	}
 
 	public static void insertData(String table, String column, String data)
 			throws SQLException {
 		SQLManager.standardQuery("INSERT INTO " + table + " (" + column + ") VALUE ("
 				+ data + ");");
 	}
 	
 	public static boolean doesTableExist(String table){
 		return SQLManager.doesTableExist(table);
 	}
 	
 	public static void createDefaultTables(){
 		//BungeePlayers
 		try {
 			runTableQuery("BungeePlayers", "CREATE TABLE BungeePlayers (playername VARCHAR(100), lastonline DATETIME NOT NULL, ipaddress VARCHAR(100), nickname VARCHAR(100), channel VARCHAR(100),muted TINYINT(1) DEFAULT 0, chat_spying TINYINT(1) DEFAULT 0, dnd TINYINT(1) DEFAULT 0, tps TINYINT(1) DEFAULT 1,CONSTRAINT pk_playername PRIMARY KEY (playername))");
 		//BungeeBans
 		runTableQuery("BungeeBans", "CREATE TABLE BungeeBans (player VARCHAR(100), banned_by VARCHAR(100), reason VARCHAR(255), type VARCHAR(100), banned_on DATETIME NOT NULL, banned_until DATETIME, CONSTRAINT pk_bannedPlayer PRIMARY KEY (player))");
 		//BungeeChatIgnores
 		runTableQuery("BungeeChatIgnores", "CREATE TABLE BungeeChatIgnores (player VARCHAR(100), ignoring VARCHAR(100), CONSTRAINT pk_ignored PRIMARY KEY (player,ignoring), CONSTRAINT fk_player FOREIGN KEY (player) REFERENCES BungeePlayers (playername) ON UPDATE CASCADE ON DELETE CASCADE, CONSTRAINT fk_ignored FOREIGN KEY (ignoring) REFERENCES BungeePlayers (playername) ON UPDATE CASCADE ON DELETE CASCADE)");
 		//BungeeChannelMembers
 		runTableQuery("BungeeChannelMembers","CREATE TABLE BungeeChannelMembers (player VARCHAR(100), channel VARCHAR(100), CONSTRAINT pk_channelmember PRIMARY KEY (player,channel), CONSTRAINT fk_playermember FOREIGN KEY (player) REFERENCES BungeePlayers (playername) ON UPDATE CASCADE ON DELETE CASCADE)");
 		//BungeeCustomChannels
		runTableQuery("BungeeCustomChannels","CREATE TABLE BungeeCustomChannels (channelname VARCHAR(100), owner VARCHAR(100), format VARCHAR(300), open TINYINT(1), CONSTRAINT pk_channelunique PRIMARY KEY (channelname))");
 		//BungeeHomes
 		runTableQuery("BungeeHomes","CREATE TABLE BungeeHomes (player VARCHAR(100), home_name VARCHAR(100), server VARCHAR(100), world VARCHAR(100), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, CONSTRAINT pk_home PRIMARY KEY (player,home_name), CONSTRAINT fk_playerhome FOREIGN KEY (player) REFERENCES BungeePlayers (playername) ON UPDATE CASCADE ON DELETE CASCADE)");
 		//BungeePortals
 		runTableQuery("BungeePortals", "CREATE TABLE BungeePortals(portalname VARCHAR(100), server VARCHAR(100), toserver VARCHAR(100), towarp VARCHAR(100), world VARCHAR(100), filltype VARCHAR(100) DEFAULT 'AIR', xmax INT(11), xmin INT(11), ymax INT(11), ymin INT(11), zmax INT(11), zmin INT(11), CONSTRAINT pk_portalname PRIMARY KEY (portalname))");
 		//BungeeSpawns
 		runTableQuery("BungeeSpawns","CREATE TABLE BungeeSpawns (spawnname VARCHAR(100), server VARCHAR(100), world VARCHAR(100), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, CONSTRAINT pk_spawnname PRIMARY KEY (spawnname))");
 		//BungeeWarps
 		runTableQuery("BungeeWarps", "CREATE TABLE BungeeWarps (warpname VARCHAR(100), server VARCHAR(100), world VARCHAR(100), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, private TINYINT(1), CONSTRAINT pk_warp PRIMARY KEY (warpname))");
 		runTableUpdates();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void runTableUpdates() throws SQLException {
 		//add dnd to BungeePlayers
 		runTableQuery("BungeePlayers", "ALTER TABLE BungeePlayers ADD dnd TINYINT(1), ADD tps TINYInT(1)");
 		
 	}
 }
