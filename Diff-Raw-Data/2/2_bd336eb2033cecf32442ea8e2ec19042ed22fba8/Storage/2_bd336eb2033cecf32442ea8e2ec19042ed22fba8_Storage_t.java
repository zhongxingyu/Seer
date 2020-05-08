 package de.hikinggrass.WhoPlacedIt;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 
 /**
  * This Class manages the storage of the data that is produced in this plugin, at the moment the data is saved in a
  * simple file, later this data will be put into a database (mysql/sqlite)
  */
 public class Storage {
 
 	static String mainDirectory = "plugins" + File.separator + "WhoPlacedIt";
 	static File directory = new File(mainDirectory);
 	static File fileName = new File(mainDirectory + File.separator + "WhoPlacedIt.properties");
 
 	private int mode; // 0 = File, 1 = MySQL, 2 = SQLite
 
 	private ArrayList<Integer> inHand;
 
 	private Properties properties = new Properties();
 
 	Logger log;
 
 	public sqlCore manageSQLite;
 
 	public Boolean MySQL = false;
 
 	/**
 	 * 
 	 */
 	public Storage(Logger log) {
 		this.log = log;
 		this.inHand = new ArrayList<Integer>();
 		this.loadProperties();
 
 		if (this.properties.getProperty("database").equals("sqlite")) {
 			this.mode = 2;
 		}
 
		this.log.info("[WhoPlacedIt] SQLite Initializing");
 
 		// Declare SQLite handler
 		this.manageSQLite = new sqlCore(this.log, "[SQL INFO]", "WhoPlacedIt", directory.getPath());
 
 		// Initialize SQLite handler
 		this.manageSQLite.initialize();
 
 		// Check if the table exists, if it doesn't create it
 		if (!this.manageSQLite.checkTable("trackedBlocks")) {
 			this.log.info("Creating table trackedBlocks");
 			String query = "CREATE TABLE trackedBlocks (id INT AUTO_INCREMENT PRIMARY_KEY, createPlayer VARCHAR(255), createPlayerUUID VARCHAR(255), removePlayer VARCHAR(255), removePlayerUUID VARCHAR(255), x INT, y INT, z INT, createTime BIGINT, removeTime BIGINT);";
 			this.manageSQLite.createTable(query);
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
 			properties.setProperty("triggerItem", "280,50,266");
 			// Write properties file.
 			try {
 				properties.store(new FileOutputStream(fileName), null);
 			} catch (IOException ex) {
 			}
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
 				+ " AND z = " + block.getZ() + " LIMIT 3;";
 		ResultSet result = null;
 		ArrayList<BlockInfo> user = new ArrayList<BlockInfo>();
 
 		if (this.mode == 1) {
 
 		} else {
 			try {
 				result = this.manageSQLite.sqlQuery(query);
 
 				while (result != null && result.next()) {
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 					Date resultCreateDate = new Date(result.getLong("createTime"));
 					Date resultRemoveDate = new Date(result.getLong("removeTime"));
 
 					String info = "placed this block on " + sdf.format(resultCreateDate) + "\n";
 					if (!result.getString("removePlayer").isEmpty()) {
 						if (result.getString("removePlayerUUID").equals(player.getUniqueId().toString())) {
 							info += "You";
 						} else {
 							info += result.getString("removePlayer");
 						}
 						info += " removed this block on " + sdf.format(resultRemoveDate);
 					}
 					BlockInfo createPlayer;
 					if (result.getString("createPlayerUUID").equals(player.getUniqueId().toString())) {
 						createPlayer = new BlockInfo(ChatColor.GREEN, "You " + info);
 					} else {
 						createPlayer = new BlockInfo(ChatColor.YELLOW, result.getString("createPlayer") + info);
 					}
 
 					user.add(createPlayer);
 
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return user;
 
 	}
 }
