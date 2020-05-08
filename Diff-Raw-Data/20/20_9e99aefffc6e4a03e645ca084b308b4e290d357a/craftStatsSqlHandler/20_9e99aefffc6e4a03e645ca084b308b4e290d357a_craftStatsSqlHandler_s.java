 package me.masterodie.craftStats;
 
 
 import java.net.MalformedURLException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 
 import com.alta189.sqlLibrary.MySQL.mysqlCore;
 
 public class craftStatsSqlHandler {
 		
 	private static craftStats plugin;
 	
 	public craftStatsSqlHandler(craftStats instance) {
 		plugin = instance;
 	}
 	
 	public void mySqlConnect() {
 		
 		
 		craftStatsSqlHandler.plugin.manageMySql = new mysqlCore(craftStatsSqlHandler.plugin.log, craftStatsSqlHandler.plugin.logPrefix, craftStatsSqlHandler.plugin.mySqlHost, craftStatsSqlHandler.plugin.mySqlDb, craftStatsSqlHandler.plugin.mySqlUser, craftStatsSqlHandler.plugin.mySqlPasswd);
 		craftStatsSqlHandler.plugin.manageMySql.initialize();
 		craftStatsSqlHandler.plugin.log.info(craftStatsSqlHandler.plugin.logPrefix + "MysSQL has been initialized");
 		
 		boolean mySqlTableCheck = false;
 
 		try {
 					
 			if(craftStatsSqlHandler.plugin.manageMySql.checkConnection()) {
 				
 				craftStatsSqlHandler.plugin.log.info(craftStatsSqlHandler.plugin.logPrefix + "MySQL Connection Successful");
 				
 				if(craftStatsSqlHandler.plugin.manageMySql.checkTable(craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable)) {
 					mySqlTableCheck = true;
 				}
 				if(craftStatsSqlHandler.plugin.manageMySql.checkTable(craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlBlockTable)) {
 					mySqlTableCheck = true;
 				}
 				if(craftStatsSqlHandler.plugin.manageMySql.checkTable(craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlItemTable)) {
 					mySqlTableCheck = true;
 				}
 				
 				if(mySqlTableCheck) {
 					craftStatsSqlHandler.plugin.log.info(craftStatsSqlHandler.plugin.logPrefix + "Accessing tables: " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + ", "  + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlBlockTable + ", "  + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlItemTable);
 				} else {
 					craftStatsSqlHandler.plugin.log.info(craftStatsSqlHandler.plugin.logPrefix + "Creating table " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable);
 					String query0 = "CREATE TABLE `" + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + "` (" +
 					  "`ID` tinyint(4) NOT NULL AUTO_INCREMENT," +
 					  "`name` varchar(255) NOT NULL," +
 					  "`is_online` tinyint(1) DEFAULT '1'," +
 					  "`damage_taken` int(255) DEFAULT  '0'," +
 					  "`damage_dealt` int(255) DEFAULT '0'," +
 					  "`time_online` bigint DEFAULT '0'," +
 					  "`member_since` bigint DEFAULT  '0'," +
 					  "`meters_walked` double DEFAULT  '0'," +
 					  "PRIMARY KEY (`ID`)," +
 					  "UNIQUE KEY `name` (`name`)" +
 					") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8";
 						
 					craftStatsSqlHandler.plugin.manageMySql.createTable(query0); // Use mysqlCore.createTable(query) to create tables
 					
 					craftStatsSqlHandler.plugin.log.info(craftStatsSqlHandler.plugin.logPrefix + "Creating table " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlBlockTable);
 					String query1 = "CREATE TABLE `" + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlBlockTable + "` (";
 					
 					query1 += "`uID` tinyint(4) NOT NULL,";
 					for(int i = 1; i <= 96; i++) {
 						query1 += "placed_" + i + " int(11) DEFAULT '0' NOT NULL,";
 							if(i == 6 || i == 17 || i == 18 || i == 31) {
 								for(int j = 1; j <= 2; j++) {
 									query1 += "placed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							} else if (i == 35) {
 								for(int j = 1; j <= 15; j++) {
 									query1 += "placed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							} else if (i == 43 || i == 44) {
 								for(int j = 1; j <= 3; j++) {
 									query1 += "placed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							}
 					  	}
 
 					for(int i = 1; i <= 96; i++) {
 						query1 += "removed_" + i + " int(11) DEFAULT '0' NOT NULL,";
 							if(i == 6 || i == 17 || i == 18 || i == 31) {
 								for(int j = 1; j <= 2; j++) {
 									query1 += "removed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							} else if (i == 35) {
 								for(int j = 1; j <= 15; j++) {
 									query1 += "removed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							} else if (i == 43 || i == 44) {
 								for(int j = 1; j <= 3; j++) {
 									query1 += "removed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							}
 					  	}
 					
 					for(int i = 1; i <= 96; i++) {
 						query1 += "dropped_" + i + " int(11) DEFAULT '0' NOT NULL,";
 							if(i == 6 || i == 17 || i == 18 || i == 31) {
 								for(int j = 1; j <= 2; j++) {
 									query1 += "dropped_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							} else if (i == 35) {
 								for(int j = 1; j <= 15; j++) {
 									query1 += "dropped_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							} else if (i == 43 || i == 44) {
 								for(int j = 1; j <= 3; j++) {
 									query1 += "dropped_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							}
 					  	}
 					
 					query1 += "PRIMARY KEY (`uID`)";
 					query1 += ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
 
 						
 					craftStatsSqlHandler.plugin.manageMySql.createTable(query1);
 					
 					craftStatsSqlHandler.plugin.log.info(craftStatsSqlHandler.plugin.logPrefix + "Creating table " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlItemTable);
 					String query2 = "CREATE TABLE `" + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlItemTable + "` (";
 					
 					query2 += "`uID` tinyint(4) NOT NULL,";
 					for(int i = 256; i <= 358; i++) {
 						query2 += "crafted_" + i + " int(11) DEFAULT '0' NOT NULL,";
 							if(i == 263) {
 								query2 += "crafted_" + i + "_1 int(11) DEFAULT '0' NOT NULL,";
 							} else if (i == 351) {
 								for(int j = 1; j <= 15; j++) {
 									query2 += "crafted_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							}
 					  	}
 					
 					query2 += "crafted_2256 int(11) DEFAULT '0' NOT NULL,";
 					query2 += "crafted_2257 int(11) DEFAULT '0' NOT NULL,";
 					
 					for(int i = 256; i <= 358; i++) {
 						query2 += "destroyed_" + i + " int(11) DEFAULT '0' NOT NULL,";
 							if(i == 263) {
 								query2 += "destroyed_" + i + "_1 int(11) DEFAULT '0' NOT NULL,";
 							} else if (i == 351) {
 								for(int j = 1; j <= 15; j++) {
 									query2 += "destroyed_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							}
 					  	}
 					
 					query2 += "destroyed_2256 int(11) DEFAULT '0' NOT NULL,";
 					query2 += "destroyed_2257 int(11) DEFAULT '0' NOT NULL,";
 					
 					for(int i = 256; i <= 358; i++) {
 						query2 += "dropped_" + i + " int(11) DEFAULT '0' NOT NULL,";
 							if(i == 263) {
 								query2 += "dropped_" + i + "_1 int(11) DEFAULT '0' NOT NULL,";
 							} else if (i == 351) {
 								for(int j = 1; j <= 15; j++) {
 									query2 += "dropped_" + i + "_" + j +  " int(11) DEFAULT '0' NOT NULL,";
 								}
 							}
 					  	}
 					
 					query2 += "dropped_2256 int(11) DEFAULT '0' NOT NULL,";
 					query2 += "dropped_2257 int(11) DEFAULT '0' NOT NULL,";
 					
 					query2 += "PRIMARY KEY (`uID`)";
 					query2+= ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
 
 						
 					craftStatsSqlHandler.plugin.manageMySql.createTable(query2);
 				}
 			} else {
 				craftStatsSqlHandler.plugin.log.severe(craftStatsSqlHandler.plugin.logPrefix + "MySQL connection failed");
 			}
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	public int getMySqlUserId (String name) {
 		String query = "SELECT `ID` FROM " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + " WHERE name = '" + name + "' ;";
 		ResultSet result = null;
 		int id = 0;
 		
 		try {
 			
 			result = plugin.manageMySql.sqlQuery(query);
 					 
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			if (result != null && result.next()) {
 				id = result.getInt("ID");
 				return id;
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		return 0;
 	}
 
 	public void mySqlInsertQuery(String name, long value, String row, String type) {
 		
 		String query = null;
 		
 		Date date = new Date();
 
 		
		if((row == "is_online" || row == "health") && type == "player") {
 			query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + " ( `name`, `member_since` ) VALUES ( '" + name + "', '" + date.getTime() + "' ) ON DUPLICATE KEY UPDATE " + row + " = " + value + ";";
 		}
 		else { 
 			if(type == "player") 
 				query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + " ( `name` ) VALUES ( '" + name + "' ) ON DUPLICATE KEY UPDATE " + row + " = " + row + "+"  + value + ";";
 			if(type == "block")
 				query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlBlockTable + " (`uID`) VALUES ( '" + this.getMySqlUserId(name) + "' ) ON DUPLICATE KEY UPDATE " + row + "=" + row + "+" + value + ";";
 			if(type == "item")
 				query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlItemTable + " (`uID`) VALUES ( '" + this.getMySqlUserId(name) + "' ) ON DUPLICATE KEY UPDATE " + row + "=" + row + "+" + value + ";";
 		}
 		try {
 			plugin.manageMySql.insertQuery(query);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 public void mySqlInsertQuery(String name, double value, String row, String type) {
 		
 		String query = null;
 		
 		if(type == "player" && row == "last_login") {
 			query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + " ( `name` ) VALUES ( '" + name + "' ) ON DUPLICATE KEY UPDATE " + row + " = " + value + ";";
 		} else { 
 			if(type == "player") 
 				query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + " ( `name` ) VALUES ( '" + name + "' ) ON DUPLICATE KEY UPDATE " + row + " = " + row + "+"  + value + ";";
 			if(type == "block")
 				query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlBlockTable + " (`uID`) VALUES ( '" + this.getMySqlUserId(name) + "' ) ON DUPLICATE KEY UPDATE " + row + "=" + row + "+" + value + ";";
 			if(type == "item")
 				query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlItemTable + " (`uID`) VALUES ( '" + this.getMySqlUserId(name) + "' ) ON DUPLICATE KEY UPDATE " + row + "=" + row + "+" + value + ";";
 		}	
 		try {
 			plugin.manageMySql.insertQuery(query);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 public void mySqlInsertQuery(String name, String value, String row, String type) {
 	
 	String query = null;
 	
 	plugin.log.info("Test " + row + "   " + value);
 	
 	if(type == "player") 
 		query = "INSERT INTO " + craftStatsSqlHandler.plugin.mySqlTablePrefix + craftStatsSqlHandler.plugin.mySqlUserTable + " ( `name` ) VALUES ( '" + name + "' ) ON DUPLICATE KEY UPDATE " + row + " = '"  + value + "';";
 
 		try {
 			plugin.manageMySql.insertQuery(query);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
