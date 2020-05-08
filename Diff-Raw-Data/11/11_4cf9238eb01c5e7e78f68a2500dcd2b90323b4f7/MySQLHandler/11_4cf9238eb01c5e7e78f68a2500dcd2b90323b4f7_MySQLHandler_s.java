 package Staartvin.FoundOres.MySQL;
 
 import Staartvin.FoundOres.FoundOres;
 import Staartvin.FoundOres.LogClass.eventTypes;
 import Staartvin.FoundOres.Database.DatabaseConnector;
 
 public class MySQLHandler {
 
 	FoundOres plugin;
 	private String table = "foundores";
 	private MySQL mysql;
 	private String host;
 	private String user;
 	private String password;
 	private String database;
 
 	public MySQLHandler(FoundOres instance) {
 		plugin = instance;
 	}
 
 	private void getConfigData() {
 		host = plugin.getConfig().getString("MySQL.host");
 		user = plugin.getConfig().getString("MySQL.user");
 		password = plugin.getConfig().getString("MySQL.password");
 		database = plugin.getConfig().getString("MySQL.database");
 	}
 
 	public void setupSQL() {
 
 		getConfigData();
 
		//System.out.print("Hostname: " + host);
		//System.out.print("Username: " + user);
		//System.out.print("Password: " + password);
		//System.out.print("Database: " + database);
 		mysql = new MySQL(host, user, password, database);
 
 		if (mysql.connect()) {
 			plugin.getLogger().info("MySQL database connected!");
 			plugin.log.logToFile("Connected to database '" + database
 					+ "' on host " + host, eventTypes.MYSQL_CONNECTION);
 		} else {
 			plugin.getLogger().info("Could not connect to MySQL database!");
 		}
 	}
 
 	public void constructTables() {
 		// If the MySQL Connection is somehow closed, open it again.
 		if (mysql.isClosed()) {
 			mysql.connect();
 		}
 
 		String statement = "CREATE TABLE IF NOT EXISTS " + table + " "
 				+ "(id INT PRIMARY KEY AUTO_INCREMENT,"
 				+ " playerName VARCHAR(32) not NULL UNIQUE,"
 				+ " world VARCHAR(100) not NULL UNIQUE,"
 				+ " stone BIGINT not NULL default '0',"
 				+ " coal BIGINT not NULL default '0',"
 				+ " iron BIGINT not NULL default '0',"
 				+ " gold BIGINT not NULL default '0',"
 				+ " redstone BIGINT not NULL default '0',"
 				+ " lapis BIGINT not NULL default '0',"
 				+ " diamond BIGINT not NULL default '0',"
 				+ " emerald BIGINT not NULL default '0',"
 				+ " netherquartz BIGINT not NULL default '0');";
 		mysql.execute(statement);
 	}
 
 	// Add extra blocks if they aren't in the database yet.
 	// If the database is from Minecraft Beta 1.8, it will not have emeralds as a column. We need to add that
 	public void updateToLatestMinecraftVersion() {
 
 		// If the MySQL Connection is somehow closed, open it again.
 		if (mysql.isClosed()) {
 			mysql.connect();
 		}
 
 		// Update for emerald
 		String statement = " ALTER TABLE " + table
 				+ " ADD emerald BIGINT NOT NULL default '0';";
 		mysql.execute(statement);
 		plugin.getLogger().info("Database check for emerald");
 
 		// Update netherquartz
 		statement = " ALTER TABLE " + table
 				+ " ADD netherquartz BIGINT NOT NULL default '0';";
 		mysql.execute(statement);
 		plugin.getLogger().info("Database check for netherquartz");
 
 		plugin.getLogger().info("Database is up-to-date!");
 	}
 
 	public void incrementBlockCount(String world, String player, int blockID) {
 		// If the MySQL Connection is somehow closed, open it again.
 		if (mysql.isClosed()) {
 			mysql.connect();
 		}
 
 		String block = null;
 		DatabaseConnector dCon = plugin.dCon;
 		int blockCount = 0;
 
 		if (blockID == 1) {
 			block = "stone";
 			blockCount = dCon.getBrokenStone(player, world);
 		} else if (blockID == 14) {
 			block = "gold";
 			blockCount = dCon.getBrokenGold(player, world);
 		} else if (blockID == 15) {
 			block = "iron";
 			blockCount = dCon.getBrokenIron(player, world);
 		} else if (blockID == 16) {
 			block = "coal";
 			blockCount = dCon.getBrokenCoal(player, world);
 		} else if (blockID == 21) {
 			block = "lapis";
 			blockCount = dCon.getBrokenLapisLazuli(player, world);
 		} else if (blockID == 56) {
 			block = "diamond";
 			blockCount = dCon.getBrokenDiamond(player, world);
 		} else if (blockID == 73) {
 			block = "redstone";
 			blockCount = dCon.getBrokenRedstone(player, world);
 		} else if (blockID == 129) {
 			block = "emerald";
 			blockCount = dCon.getBrokenEmerald(player, world);
 		} else if (blockID == 153) {
 			block = "netherquartz";
 			blockCount = dCon.getBrokenNetherQuartz(player, world);
 		} else {
 			block = "stone";
 			blockCount = dCon.getBrokenStone(player, world);
 		}
 
 		String statement = "" + "INSERT INTO " + table
 				+ " (playerName, world, " + block + ")" + " VALUES ('" + player
 				+ "', '" + world + "', " + blockCount + ")"
 				+ " ON DUPLICATE KEY UPDATE " + block + "=" + blockCount;
 		mysql.execute(statement);
 	}
 }
