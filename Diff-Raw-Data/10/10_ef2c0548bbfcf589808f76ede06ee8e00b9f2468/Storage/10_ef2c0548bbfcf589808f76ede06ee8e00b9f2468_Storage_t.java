 package de.hikinggrass.WhoPlacedIt;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import com.alta189.sqlLibrary.MySQL.mysqlCore;
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 
 /**
  * This Class manages the storage of the data that is produced in this plugin, at the moment the data is saved in a
  * simple file, later this data will be put into a database (mysql/sqlite)
  */
 public class Storage {
 
 	protected static String mainDirectory = "plugins" + File.separator + "WhoPlacedIt";
 	protected static File directory = new File(mainDirectory);
 	protected static File fileName = new File(mainDirectory + File.separator + "WhoPlacedIt.properties");
 
 	protected int mode; // 1 = MySQL, 2 = SQLite
 
 	protected ArrayList<Integer> inHand;
 
 	protected Properties properties = new Properties();
 
 	protected Logger log;
 
 	protected sqlCore manageSQLite;
 	protected mysqlCore manageMySQL;
 
 	protected String mysqlHost;
 	protected String mysqlUser;
 	protected String mysqlPassword;
 	protected String mysqlDatabase;
 
 	/**
 	 * 
 	 */
 	public Storage(Logger log) {
 		this.log = log;
 		this.inHand = new ArrayList<Integer>();
 		this.loadProperties();
 
 		if (this.properties.getProperty("database") != null && this.properties.getProperty("database").equals("mysql")) {
 			if (this.properties.getProperty("mysqlHost") == null || this.properties.getProperty("mysqlHost").equals("")
 					|| this.properties.getProperty("mysqlUser") == null
 					|| this.properties.getProperty("mysqlUser").equals("")
 					|| this.properties.getProperty("mysqlPassword") == null
 					|| this.properties.getProperty("mysqlPassword").equals("")
 					|| this.properties.getProperty("mysqlDatabase") == null
 					|| this.properties.getProperty("mysqlDatabase").equals("")) {
 				this.mode = 2;
 			} else {
 				this.mode = 1;
 			}
 
 		} else {
 			this.mode = 2;
 		}
 
 		if (this.mode == 1) {
 			this.log.info("[WhoPlacedIt] MySQL Initializing");
 			this.mysqlHost = properties.getProperty("mysqlHost");
 			this.mysqlUser = properties.getProperty("mysqlUser");
 			this.mysqlPassword = properties.getProperty("mysqlPassword");
 			this.mysqlDatabase = properties.getProperty("mysqlDatabase");
 
 			log.info("[WhoPlacedIt] using database " + this.mysqlDatabase);
 
 			// Declare MySQL Handler
 			this.manageMySQL = new mysqlCore(this.log, "[WhoPlacedIt]", this.mysqlHost, this.mysqlDatabase,
 					this.mysqlUser, this.mysqlPassword);
 
 			// Initialize MySQL Handler
 			this.manageMySQL.initialize();
 
 			try {
 				if (this.manageMySQL.checkConnection()) { // Check if the Connection was successful
 					this.log.info("[WhoPlacedIt] MySQL connection successful");
 
 					if (!this.manageMySQL.checkTable("trackedBlocks")) { // Check if the table exists in the database if
 																		 // not
 						// create it
 						this.log.info("[WhoPlacedIt] Creating table trackedBlocks");
 						String query = "CREATE TABLE trackedBlocks (id INT AUTO_INCREMENT, createPlayer VARCHAR(255), createPlayerUUID VARCHAR(255), removePlayer VARCHAR(255), removePlayerUUID VARCHAR(255), x INT, y INT, z INT, createTime BIGINT, removeTime BIGINT, cause VARCHAR(255) NOT NULL DEFAULT 'player', PRIMARY KEY (id));";
 						this.manageMySQL.createTable(query); // Use mysqlCore.createTable(query) to create tables
 					}
 
 					String query = "SHOW COLUMNS FROM trackedBlocks LIKE 'cause'";
 					ResultSet result = null;
 					result = this.manageMySQL.sqlQuery(query);
 
 					if (result == null || !result.next()) {
 						this.log.info("[WhoPlacedIt] Missing Database field cause, adding it");
 						query = "ALTER TABLE trackedBlocks ADD cause VARCHAR(255) NOT NULL DEFAULT 'player';";
 						this.manageMySQL.updateQuery(query);
 					}
 
 				} else {
 					this.log.severe("[WhoPlacedIt] MySQL connection failed, falling back to sqlite");
 					this.mode = 2;
 				}
 			} catch (NullPointerException e) {
 				log.severe("[WhoPlacedIt] Could not establish connection to mysql server, falling back to sqlite");
 				this.mode = 2;
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				log.info("[WhoPlacedIt] Error, something went wrong with the sql query");
 			}
 		}
 		if (this.mode == 2) {
 			this.log.info("[WhoPlacedIt] SQLite Initializing");
 
 			// Declare SQLite handler
 			this.manageSQLite = new sqlCore(this.log, "[SQL INFO]", "WhoPlacedIt", directory.getPath());
 
 			// Initialize SQLite handler
 			this.manageSQLite.initialize();
 
 			// Check if the table exists, if it doesn't create it
 			if (!this.manageSQLite.checkTable("trackedBlocks")) {
 				this.log.info("[WhoPlacedIt] Creating table trackedBlocks");
 				String query = "CREATE TABLE trackedBlocks (id INT AUTO_INCREMENT PRIMARY_KEY, createPlayer VARCHAR(255), createPlayerUUID VARCHAR(255), removePlayer VARCHAR(255), removePlayerUUID VARCHAR(255), x INT, y INT, z INT, createTime BIGINT, removeTime BIGINT, cause VARCHAR(255) NOT NULL DEFAULT 'player');";
 				this.manageSQLite.createTable(query);
 			}
 
 			String query = "SELECT * FROM trackedBlocks;";
 			ResultSet result = null;
 			result = this.manageSQLite.sqlQuery(query);
 			ResultSetMetaData resultMetaData;
 			try {
 				resultMetaData = result.getMetaData();
 				int columnCount = resultMetaData.getColumnCount();
 				boolean cause = false;
 				for (int i = 1; i < columnCount + 1; i++) {
 					if (resultMetaData.getColumnName(i).equals("cause")) {
 						cause = true;
 						break;
 					}
 				}
 				if (!cause) {
 					this.log.info("[WhoPlacedIt] Missing Database field cause, adding it");
 					query = "ALTER TABLE trackedBlocks ADD cause VARCHAR(255) NOT NULL DEFAULT 'player';";
 					this.manageSQLite.updateQuery(query);
 				}
 			} catch (SQLException e) {
 				log.info("[WhoPlacedIt] Error, something went wrong with the sql query");
 			}
 		}
 	}
 
 	/**
 	 * @return the mode
 	 */
 	public int getMode() {
 		return mode;
 	}
 
 	private void loadProperties() {
 		// Read properties file.
 		try {
 			properties.load(new FileInputStream(fileName));
 		} catch (IOException e) {
 			// store default values
 			log.info("[WhoPlacedIt] Error, found no properties file, creating one with default values");
 			properties.setProperty("database", "sqlite");
 			properties.setProperty("triggerItem", "280");
 			properties.setProperty("dateFormat", "yyyy-MM-dd HH:mm:ss");
 			properties.setProperty("enableHistory", "true");
 			properties.setProperty("enableStats", "true");
 			properties.setProperty("mysqlHost", "localhost");
 			properties.setProperty("mysqlUser", "");
 			properties.setProperty("mysqlPassword", "");
 			properties.setProperty("mysqlDatabase", "whoplacedit");
 			// Write properties file.
 			try {
 				properties.store(new FileOutputStream(fileName), null);
 			} catch (IOException ex) {
 				log.info("[WhoPlacedIt] Error, could not write properties file");
 			}
 		}
 		if (properties.getProperty("enableHistory") == null) {
 			properties.setProperty("enableHistory", "true");
 		}
 		if (properties.getProperty("enableStats") == null) {
 			properties.setProperty("enableStats", "true");
 		}
 		if (this.properties.getProperty("dateFormat") == null) {
 			properties.setProperty("dateFormat", "yyyy-MM-dd HH:mm:ss");
 		}
 	}
 
 	/**
 	 * @return the inHand
 	 */
 	public ArrayList<Integer> getInHand() {
 		if (inHand.isEmpty()) {
 			String triggerItem = this.properties.getProperty("triggerItem");
 			for (String split : triggerItem.split(",")) {
 				try {
 					this.inHand.add(Integer.valueOf(split.trim()));
 				} catch (NumberFormatException e) {
 					// silently fail
 				}
 			}
 		}
 		return inHand;
 	}
 
 	/**
 	 * writes the information into the database that a block has been placed by the given player at the given time
 	 * 
 	 * @param block
 	 * @param player
 	 * @param createTime
 	 */
 	public void placeBlock(Block block, Player player, long createTime) {
 		String query = "INSERT INTO trackedBlocks (createPlayer, createPlayerUUID, removePlayer, removePlayerUUID, x, y, z, createTime, removeTime) VALUES ('"
 				+ player.getName()
 				+ "','"
 				+ player.getUniqueId().toString()
 				+ "', '','', "
 				+ block.getX()
 				+ ", "
 				+ block.getY() + ", " + block.getZ() + ", " + createTime + ", " + 0 + ");";
 
 		if (this.mode == 1) {
 			try {
 				this.manageMySQL.insertQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			this.manageSQLite.insertQuery(query);
 		}
 	}
 
 	/**
 	 * Sets the remove time of the given block
 	 * 
 	 * @param block
 	 * @param removeTime
 	 */
 	public void removeBlock(Block block, Player player, long removeTime) {
 		String query = "UPDATE trackedBlocks SET removeTime = " + removeTime + ", removePlayer = '" + player.getName()
 				+ "', removePlayerUUID = '" + player.getUniqueId().toString() + "' WHERE x = " + block.getX()
 				+ " AND y = " + block.getY() + " AND z = " + block.getZ() + " AND removeTime = 0;";
 		if (this.mode == 1) {
 			try {
 				this.manageMySQL.updateQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			this.manageSQLite.updateQuery(query);
 		}
 	}
 
 	public void burnBlock(Block block, long removeTime) {
 		String query = "SELECT * FROM trackedBlocks WHERE x = " + block.getX() + " AND y = " + block.getY()
 				+ " AND z = " + block.getZ() + " AND removeTime = 0 LIMIT 1";
 		ResultSet result = null;
 
 		if (this.mode == 1) {
 			try {
 				result = this.manageMySQL.sqlQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			result = this.manageSQLite.sqlQuery(query);
 		}
 
 		try {
 			if (result != null && result.next()) {
 				log.info("burnt block was placed by player");
 				query = "UPDATE trackedBlocks SET removeTime = " + removeTime + ", cause = 'fire' WHERE x = "
 						+ block.getX() + " AND y = " + block.getY() + " AND z = " + block.getZ()
 						+ " AND removeTime = 0;";
 			} else {
 				log.info("burn block was placed by the game");
				query = "INSERT INTO trackedBlocks (createPlayer, createPlayerUUID, removePlayer, removePlayerUUID, x, y, z, createTime, removeTime, cause) VALUES ("
						+ "'',"
						+ "'',"
						+ "'',"
						+ "'',"
 						+ block.getX()
 						+ ", "
 						+ block.getY()
 						+ ", "
 						+ block.getZ()
 						+ ", " + removeTime + ", " + removeTime + ", 'fire');";
 
 			}
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		if (this.mode == 1) {
 			try {
 				this.manageMySQL.updateQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			this.manageSQLite.updateQuery(query);
 		}
 	}
 
 	/**
 	 * Returns information about the given block in the following format: <br />
 	 * USERNAME created on yyyy-MM-dd HH:mm:ss deleted on yyyy-MM-dd HH:mm:ss
 	 * 
 	 * @param block
 	 * @return
 	 */
 	public ArrayList<BlockInfo> getBlockInfo(Block block, Player player) {
 		String query = "SELECT * FROM trackedBlocks WHERE x = " + block.getX() + " AND y = " + block.getY()
 				+ " AND z = " + block.getZ() + " ORDER BY createTime DESC LIMIT ";
 
 		if (properties.getProperty("enableHistory").equals("true")) {
 			query += "3;";
 		} else {
 			query += "1;";
 		}
 		ResultSet result = null;
 		ArrayList<BlockInfo> user = new ArrayList<BlockInfo>();
 
 		if (this.mode == 1) {
 			try {
 				result = this.manageMySQL.sqlQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			result = this.manageSQLite.sqlQuery(query);
 		}
 		try {
 			while (result != null && result.next()) {
 				SimpleDateFormat sdf = new SimpleDateFormat(this.properties.getProperty("dateFormat"));
 
 				Date resultCreateDate = new Date(result.getLong("createTime"));
 				Date resultRemoveDate = new Date(result.getLong("removeTime"));
 
 				BlockInfo createPlayer = null;
 				if (result.getString("createPlayerUUID").equals(player.getUniqueId().toString())) {
 					createPlayer = new BlockInfo(ChatColor.GREEN, "You placed this block on "
 							+ sdf.format(resultCreateDate), resultCreateDate);
 				} else if (!result.getString("createPlayerUUID").equals("")) {
 					createPlayer = new BlockInfo(ChatColor.YELLOW, result.getString("createPlayer")
 							+ " placed this block on " + sdf.format(resultCreateDate), resultCreateDate);
 				}
 				BlockInfo removePlayer = null;
 
 				if (!result.getString("removePlayer").isEmpty()) {
 					if (result.getString("removePlayerUUID").equals(player.getUniqueId().toString())) {
 						removePlayer = new BlockInfo(ChatColor.GREEN, "You removed this block on "
 								+ sdf.format(resultRemoveDate), resultRemoveDate);
 					} else {
 						removePlayer = new BlockInfo(ChatColor.YELLOW, result.getString("removePlayer")
 								+ " removed this block on " + sdf.format(resultRemoveDate), resultRemoveDate);
 					}
 				} else if (result.getString("cause").equals("fire")) {
 					removePlayer = new BlockInfo(ChatColor.RED, "Fire burnt this block on "
 							+ sdf.format(resultRemoveDate), resultRemoveDate);
 					;
 
 				}
 				if (createPlayer != null) {
 					user.add(createPlayer);
 				}
 				if (removePlayer != null) {
 					user.add(removePlayer);
 				}
 			}
 		} catch (SQLException e) {
 			log.info("[WhoPlacedIt] Error, something went wrong with the sql query");
 		}
 		Collections.sort(user);
 
 		return user;
 	}
 
 	public int getPlacedBlockCount(Player player) {
 		String query = "SELECT COUNT(id) AS rowcount FROM trackedBlocks WHERE createTime != '' AND createPlayerUUID = '"
 				+ player.getUniqueId().toString() + "'";
 		ResultSet result = null;
 
 		if (this.mode == 1) {
 			try {
 				result = this.manageMySQL.sqlQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			result = this.manageSQLite.sqlQuery(query);
 		}
 		try {
 			while (result != null && result.next()) {
 				return result.getInt("rowcount");
 			}
 		} catch (SQLException e) {
 			log.info("[WhoPlacedIt] Error, something went wrong with the sql query");
 		}
 		return 0;
 	}
 
 	public int getRemovedBlockCount(Player player) {
 		String query = "SELECT COUNT(id) AS rowcount FROM trackedBlocks WHERE removeTime != '' AND removePlayerUUID = '"
 				+ player.getUniqueId().toString() + "'";
 		ResultSet result = null;
 
 		if (this.mode == 1) {
 			try {
 				result = this.manageMySQL.sqlQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else {
 			result = this.manageSQLite.sqlQuery(query);
 		}
 		try {
 			while (result != null && result.next()) {
 				return result.getInt("rowcount");
 			}
 		} catch (SQLException e) {
 			log.info("[WhoPlacedIt] Error, something went wrong with the sql query");
 		}
 		return 0;
 	}
 }
