 package en.m477.EyeSpy.Logging;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import en.m477.EyeSpy.EyeSpy;
 import en.m477.EyeSpy.Util.ArgProcessing;
 
 	/**
 	 * 
 	 * @author M477h3w1012
 	 *
 	 */
 
 	/* Notes for creation
 	 * TODO Create a connection class
 	 * TODO Create a class for adding chest entries
 	 */
 
 public class Logging implements Runnable {
 	
 	public static Connection conn;
 	private static String host;
 	private static String database;
 	public static boolean sql;
 	
     public Logging(String host, String database, EyeSpy plugin) {
         Logging.host = EyeSpy.host;
         Logging.database = EyeSpy.database;
     }
 	
 	public void startSql() {
 		startConnection();
 		if (sql) {
 		  createTables();
 		  }
 		
 	}
 	
     protected void startConnection() {
     	sql = true;
         String sqlUrl = String.format("jdbc:mysql://%s/%s", host, database);
         
 	    String username = EyeSpy.username;
 	    String password = EyeSpy.password;
 	    
         Properties sqlStr = new Properties();
         sqlStr.put("user", username);
         sqlStr.put("password", password);
         try {
             conn = DriverManager.getConnection(sqlUrl, sqlStr);
         } catch (SQLException e) {
             EyeSpy.printSevere("A MySQL connection could not be made");
             sql = false;
         }
     }
     
     protected void createTables() {
         try {
             //Blocks
             EyeSpy.printInfo("Searching for Blocks table");
             ResultSet rs = conn.getMetaData().getTables(null, null, "blocks", null);
             if (!rs.next()) {
                 EyeSpy.printWarning("No 'blocks' table found, attempting to create one...");
                 PreparedStatement ps = conn
                 		.prepareStatement("CREATE TABLE IF NOT EXISTS `blocks` ( "
                 				+ "`spy_id` int unsigned not null auto_increment, "
                 				+ "`date` DATETIME not null, "
                 				+ "`player_id` mediumint unsigned not null, "
                 				+ "`worldname` varchar(30) not null, "
                 				+ "`blockname` mediumint unsigned not null, "
                 				+ "`blockdata` tinyint unsigned not null, "
                 				+ "`x` mediumint signed not null, "
                 				+ "`y` mediumint unsigned not null, "
                 				+ "`z` mediumint signed not null, "
                 				+ "`place/break` boolean, "
                 				+ "primary key (`spy_id`));" );
                 ps.executeUpdate();
                 ps.close();
                 EyeSpy.printWarning("'blocks' table created!");
             } else { 
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Chat
             EyeSpy.printInfo("Searching for Chat table");
             rs = conn.getMetaData().getTables(null, null, "chat", null);
             if (!rs.next()) {
                 EyeSpy.printWarning("No 'chat' table found, attempting to create one...");
                 PreparedStatement ps = conn
                         .prepareStatement("CREATE TABLE IF NOT EXISTS `chat` ( "
                                 + "`chat_id` mediumint unsigned not null auto_increment, "
                                 + "`player_id` mediumint unsigned not null, "
                                 + "`date` DATETIME not null, "
                                 + "`message` varchar(255) not null, "
                                 + "primary key (`chat_id`));" );
                 ps.executeUpdate();
                 ps.close();
                 EyeSpy.printWarning("'chat' table created!");
             } else {
                 EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Chat Channels
             EyeSpy.printInfo("Searching for Chat Channels table");
            rs = conn.getMetaData().getTables(null, null, "chatchannels", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'chatchannels' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `chatchannels` ( "
             					+ "`ch_id` tinyint unsigned not null auto_increment, "
             					+ "`ch_name` varchar(30) not null, "
             					+ "primary key (`ch_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'chatchannels' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Chests
             EyeSpy.printInfo("Searching for Chests table");
             rs = conn.getMetaData().getTables(null, null, "chests", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'chests' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `chests` ( "
             					+ "`access_id` mediumint unsigned not null auto_increment, "
             					+ "`data` DATETIME not null, "
             					+ "`player_id` mediumint unsigned not null, "
             					+ "`x` mediumint signed not null, "
             					+ "`y` mediumint unsigned not null, "
             					+ "`z` mediumint signed not null, "
             					+ "primary key (`access_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'chest' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Commands
             EyeSpy.printInfo("Searching for Commands table");
             rs = conn.getMetaData().getTables(null, null, "commands", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'command' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `commands` ( "
             					+ "`cmd_id` mediumint unsigned not null auto_increment, "
             					+ "`player_id` mediumint unsigned not null, "
             					+ "`date` DATETIME not null, "
             					+ "`command` varchar(255) not null, "
             					+ "primary key (`cmd_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'command' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Players
             EyeSpy.printInfo("Searching for players table");
             rs = conn.getMetaData().getTables(null, null, "players", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'players' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `players` ( "
             					+ "`player_id` mediumint unsigned not null auto_increment, "
             					+ "`pl_name` varchar(30) not null, "
             					+ "primary key (`player_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'players' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //World
             EyeSpy.printInfo("Searching for Worlds table");
             rs = conn.getMetaData().getTables(null, null, "world", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'world' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `world` ( "
             					+ "`world_id` mediumint unsigned not null auto_increment, "
             					+ "`wld_name` varchar(30) not null, "
             					+ "primary key (`world_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'world' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Users
             EyeSpy.printInfo("Searching for Users table");
             rs = conn.getMetaData().getTables(null, null, "users", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'users' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `users` ( "
             					+ "`user_id` mediumint unsigned not null auto_increment, "
             					+ "`username` varchar(30) not null, "
             					+ "`password` varchar(50) not null, "
             					+ "primary key (`user_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'users' table created!");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public static void maintainConnection() {
         PreparedStatement ps = null;
         try {
             ps = conn.prepareStatement("SELECT count(*) FROM `chat` limit 1;");
             ps.executeQuery();
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         EyeSpy.self.log.info("EyeSpy has checked in with database");
     }
     
     public static void addNewBlock(String name, int type, byte data, byte broken, int x, int y, int z, String world) {
     	try {
     		EyeSpy.printInfo("Block Hit!");
     		PreparedStatement ps = conn
     				.prepareStatement("INSERT INTO `blocks` (`date`, `player_id`, `worldname`, `blockname`, `blockdata`, "
     						+ "`x`, `y`, `z`, `place/break`) VALUES ( '"
     						+ ArgProcessing.getDateTime() + "', '3', '"
     						+ world + "', '"
     						+ type + "', '"
     						+ data + "', '"
     						+ x + "', '"
     						+ y + "', '"
     						+ z + "', '"
     						+ broken + "'); ");
     		ps.executeUpdate();
     		ps.close();
     		EyeSpy.printInfo("Block Added!");
     	} catch (SQLException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	}
     }
     
     public static void addNewChat(String name, String Message) {
     	try {
     		EyeSpy.printInfo("Chat Started");
 			PreparedStatement ps = conn
 					.prepareStatement("INSERT INTO `chat` (`player_id`, `date` , `message`) VALUES ('1', '"
 						+ ArgProcessing.getDateTime() + "', '"
 						+ Message + "');");
 			ps.executeUpdate();
 			ps.close();
 			EyeSpy.printInfo("Chat Successful!");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
     
     public static void addNewCommand(String name, String Message) {
     	try {
     		EyeSpy.printInfo("Command Started");
     		PreparedStatement ps = conn
     				.prepareStatement("INSERT INTO `commands` (`player_id`, `date`, `command`) VALUES ( '2', '"
     						+ ArgProcessing.getDateTime() + "', '"
     						+ Message + "');");
     		ps.executeUpdate();
     		ps.close();
     		EyeSpy.printInfo("Command Log Successful!");
     	} catch (SQLException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	}
     }
 
 	public void run() {
 		maintainConnection();
 	}
 	
 	public static void playerExists(String name) {
 		ResultSet rs = null;
 		PreparedStatement ps = null;
 		try {
 			ps = conn.prepareStatement("SELECT `pl_name` FROM `players` WHERE (pl_name = '" + name + "');" );
 			rs = ps.executeQuery();
 			if (!rs.next()) {
 				ps = conn.prepareStatement("INSERT INTO `players` (`pl_name`) VALUES ( '" + name + "');" );
 				ps.executeUpdate();
 				ps.close();
 				EyeSpy.printInfo(name + " added to the players table");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
