 package net.alexben.Slayer.Core.Handlers;
 
 import net.alexben.Slayer.Core.Handlers.Connectors.MySQL;
 import net.alexben.Slayer.Core.Handlers.Connectors.SQLite;
 import net.alexben.Slayer.Core.Slayer;
 import net.alexben.Slayer.Utilities.ConfigUtil;
 import net.alexben.Slayer.Utilities.MiscUtil;
 
 public class Database
 {
 	// Define the database variables
 	private static MySQL mysql;
 	private static SQLite sqlite;
 
 	/**
 	 * Initializes the connection with the database.
 	 */
 	public static void initializeConnection()
 	{
 		// Handle SQLite
 		if(ConfigUtil.getSettingBoolean("data.sqlite.use"))
 		{
 			// Create the object
 			sqlite = new SQLite(Slayer.plugin.getLogger(), ConfigUtil.getSettingString("data.sqlite.database"), Slayer.plugin.getDataFolder().getPath());
 
 			// Open the connection and report back
 			sqlite.open();
 
 			if(sqlite.checkConnection())
 			{
 				MiscUtil.logSqlite("info", "Connection established.");
 			}
 		}
 
 		// Handle MySQL
 		if(ConfigUtil.getSettingBoolean("data.mysql.use"))
 		{
 			// Create the object
 			mysql = new MySQL(Slayer.plugin.getLogger(), ConfigUtil.getSettingString("data.mysql.host"), ConfigUtil.getSettingString("data.mysql.port"), ConfigUtil.getSettingString("data.mysql.database"), ConfigUtil.getSettingString("data.mysql.user"), ConfigUtil.getSettingString("data.mysql.pass"));
 
 			// Open the connection and report back
 			mysql.open();
 
 			if(mysql.checkConnection())
 			{
 				MiscUtil.logMysql("info", "Connection established.");
 			}
 		}
 
 		// Create the tables if need be
 		createTables();
 	}
 
 	/**
 	 * Creates the tables needed for SQL storage.
 	 */
 	public static void createTables()
 	{
 		// Define table queries
 		String players = "CREATE TABLE players (unique_id INT AUTO_INCREMENT, PRIMARY KEY (unique_id), player VARCHAR(24), points INT, join_date VARCHAR(128));";
		String assignments = "CREATE TABLE assignments (unique_id INT AUTO_INCREMENT, player_id INT, PRIMARY KEY (player_id), task BLOB, progress INT, expiration BIGINT, display TINYINT, active TINYINT, failed TINYINT, expired TINYINT, forfeited TINYINT);";
 
 		// Handle SQLite
 		if(sqlite != null && sqlite.checkConnection())
 		{
 			// Check if the tables exist and if not create them
 			if(!sqlite.checkTable("players"))
 			{
 				MiscUtil.logSqlite("info", "Creating \"players\" table...");
 				sqlite.createTable(players);
 				MiscUtil.logSqlite("info", "\"players\" table created!");
 			}
 
 			if(!sqlite.checkTable("assignments"))
 			{
 				MiscUtil.logSqlite("info", "Creating \"assignments\" table...");
 				sqlite.createTable(assignments);
 				MiscUtil.logSqlite("info", "\"assignments\" table created!");
 			}
 		}
 
 		// Handle MySQL
 		if(mysql != null && mysql.checkConnection())
 		{
 			// Check if the tables exist and if not create them
 			if(!mysql.checkTable("players"))
 			{
				MiscUtil.logSqlite("info", "Creating \"players\" table...");
 				mysql.createTable(players);
				MiscUtil.logSqlite("info", "\"players\" table created!");
 			}
 
 			if(!mysql.checkTable("assignments"))
 			{
				MiscUtil.logSqlite("info", "Creating \"assignments\" table...");
 				mysql.createTable(assignments);
				MiscUtil.logSqlite("info", "\"assignments\" table created!");
 			}
 		}
 	}
 }
