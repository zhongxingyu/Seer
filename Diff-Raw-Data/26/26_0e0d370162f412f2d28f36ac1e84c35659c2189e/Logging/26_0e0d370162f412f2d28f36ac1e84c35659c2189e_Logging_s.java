 package uk.co.m4numbers.EyeSpy.Logging;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 import uk.co.m4numbers.EyeSpy.EyeSpy;
 import uk.co.m4numbers.EyeSpy.Util.ArgProcessing;
 
 	/**
 	 * Main logging class... logs stuff.
 	 * @author M477h3w1012
 	 * 
 	 */
 
 	/* Notes for creation
 	 * TODO Create a class for adding chest entries
 	 */
 
 public class Logging {
 	
 	//Start with all the usual variables to maintain a connection
 	public static Connection conn;
 	private static String host;
 	private static String database;
 	public static boolean sql;
 	
 	//List all the prepared statements that we're going to be using at one point or another
 	private static PreparedStatement Maintain;
 	private static PreparedStatement InsertChat;
 	private static PreparedStatement SelectChannel;
 	private static PreparedStatement InsertChannel;
 	private static PreparedStatement SelectPlayer;
 	private static PreparedStatement InsertPlayer;
 	private static PreparedStatement SelectServer;
 	private static PreparedStatement InsertServer;
 	private static PreparedStatement SelectWorld;
 	private static PreparedStatement InsertWorld;
 	private static PreparedStatement InsertCommand;
 	private static PreparedStatement InsertBlock;
 	
 	/**
 	 * Gets the information required for connecting.
 	 */
     public Logging() {
         Logging.host = EyeSpy.host;
         Logging.database = EyeSpy.database;
     }
 	
     /**
      * This is called once, upon startup of the server, and performs the checks to make sure that the plugin is ready to go.
      */
 	public void startSql() {
 		String prefix = EyeSpy.prefix;
 		startConnection();
 		if (sql) {
 		  createTables( prefix );
 		  }
 		prepareStatements( prefix );
		
 	}
 	
 	/**
 	 * This is called upon startup and logs into the database using the information found in the config file.
 	 */
     protected void startConnection() {
     	sql = true;
        String sqlUrl = String.format("jdbc:mysql://%s/%s", host, database);
         
 	    String username = EyeSpy.username;
 	    String password = EyeSpy.password;
 	    
         Properties sqlStr = new Properties();
         sqlStr.put("user", username);
         sqlStr.put("password", password);
         sqlStr.put("autoReconnect", "true");
         try {
             conn = DriverManager.getConnection(sqlUrl, sqlStr);
         } catch (SQLException e) {
             EyeSpy.printSevere("A MySQL connection could not be made");
             e.printStackTrace();
             sql = false;
         }
     }
     
     /**
      * Kill the connection cleanly. Basically just the one command, but hey... gotta do something.
      */
     public static void killConnection() {
     	if ( sql ) {
     		EyeSpy.printInfo("Closing the connection...");
     		try {
     			conn.close();
     		} catch (SQLException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
     		EyeSpy.printInfo("Connection has been closed.");
     	}
     }
     
     /**
      * Alright, we have all the statements, lets build them up and create usable commands from them.
      * @param prefix : Use this to build the statements, affixing it to all the table descriptors.
      */
     protected void prepareStatements( String prefix ) {
     	try {
     		Maintain = conn.prepareStatement("SELECT count(*) FROM `" + prefix + "chat` limit 1;");
     		
     		
     		InsertChat = conn.prepareStatement("INSERT INTO `" + prefix + "chat` (`player_id`, `ch_id`, `ser_id`, `date`, `message`) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
     		
     		SelectChannel = conn.prepareStatement("SELECT * FROM `" + prefix + "chatchannels` WHERE (ch_name = ?)", Statement.RETURN_GENERATED_KEYS);
     		InsertChannel = conn.prepareStatement("INSERT INTO `" + prefix + "chatchannels` (`ch_name`) VALUES ( ? )", Statement.RETURN_GENERATED_KEYS);
     		
     		SelectPlayer = conn.prepareStatement("SELECT * FROM `" + prefix + "players` WHERE (pl_name = ?)", Statement.RETURN_GENERATED_KEYS);
     		InsertPlayer = conn.prepareStatement("INSERT INTO `" + prefix + "players` (`pl_name`) VALUES ( ? )", Statement.RETURN_GENERATED_KEYS);
     		
     		SelectServer = conn.prepareStatement("SELECT * FROM `" + prefix + "servers` WHERE (ser_name = ?)", Statement.RETURN_GENERATED_KEYS);
     		InsertServer = conn.prepareStatement("INSERT INTO `" + prefix + "servers` (`ser_name`) VALUES ( ? )", Statement.RETURN_GENERATED_KEYS);
     		
     		SelectWorld = conn.prepareStatement("SELECT `wld_name` FROM `" + prefix + "world` WHERE (wld_name = ?)", Statement.RETURN_GENERATED_KEYS);
     		InsertWorld = conn.prepareStatement("INSERT INTO `" + prefix + "world` (`wld_name`) VALUES ( ? )", Statement.RETURN_GENERATED_KEYS);
     		
     		InsertCommand = conn.prepareStatement("INSERT INTO `" + prefix + "commands` (`player_id`, `ser_id`, `date`, `command`) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
     		
     		InsertBlock = conn.prepareStatement("INSERT INTO `" + prefix + "blocks` ( `date`, `player_id`, `world_id`, `blockname`, `blockdata`, `x`, `y`, `z`, `place/break` ) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
     	} catch (SQLException e) {
     		e.printStackTrace();
     	}
     }
     
     /**
      * This is called whenever the plugin runs from startup, it will systematically run through the database, making sure that all tables exist, if it finds a missing table, it will add it.
      */
     protected void createTables( String prefix ) {
     	ResultSet rs;
         try {
 
             //Players
             EyeSpy.printInfo("Searching for players table");
             rs = conn.getMetaData().getTables(null, null, prefix + "players", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'players' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "players` ( "
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
         
             //Servers
             EyeSpy.printInfo("Searching for Servers table");
             rs = conn.getMetaData().getTables(null, null, prefix + "servers", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'servers' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "servers` ( "
             					+ "`ser_id` mediumint unsigned not null auto_increment, "
             					+ "`ser_name` varchar(50) not null, "
             					+ "primary key (`ser_id`) )");
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'servers' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             //Chat Channels
             EyeSpy.printInfo("Searching for Chat Channels table");
             rs = conn.getMetaData().getTables(null, null, prefix + "chatchannels", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'chatchannels' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "chatchannels` ( "
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
             
             /*Blocks Removed from this build
             EyeSpy.printInfo("Searching for Blocks table");
             rs = conn.getMetaData().getTables(null, null, prefix + "blocks", null);
             if (!rs.next()) {
                 EyeSpy.printWarning("No 'blocks' table found, attempting to create one...");
                 PreparedStatement ps = conn
                 		.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "blocks` ( "
                 				+ "`spy_id` int unsigned not null auto_increment, "
                 				+ "`date` DATETIME not null, "
                 				+ "`player_id` mediumint unsigned not null, "
                 				+ "`world_id` tinyint not null, "
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
             rs.close();*/
             
             //Chat
             EyeSpy.printInfo("Searching for Chat table");
             rs = conn.getMetaData().getTables(null, null, prefix + "chat", null);
             if (!rs.next()) {
                 EyeSpy.printWarning("No 'chat' table found, attempting to create one...");
                 PreparedStatement ps = conn
                         .prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "chat` ( "
                                 + "`chat_id` mediumint unsigned not null auto_increment, "
                                 + "`player_id` mediumint unsigned not null, "
                                 + "`date` int(11) unsigned not null, "
                                 + "`ch_id` tinyint unsigned not null, "
                                 + "`ser_id` mediumint unsigned not null, "
                                 + "`message` text not null, "
                                 + "primary key (`chat_id`), "
                                 + "foreign key (`player_id`) REFERENCES " + prefix + "players(`player_id`), "
                                 + "foreign key (`ser_id`) REFERENCES " + prefix + "servers(`ser_id`), "
                                 + "foreign key (`ch_id`) REFERENCES " + prefix + "chatchannels(`ch_id`) );" );
                 ps.executeUpdate();
                 ps.close();
                 EyeSpy.printWarning("'chat' table created!");
             } else {
                 EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             /*Chests Removed from this build
             EyeSpy.printInfo("Searching for Chests table");
             rs = conn.getMetaData().getTables(null, null, prefix + "chests", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'chests' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "chests` ( "
             					+ "`access_id` mediumint unsigned not null auto_increment, "
             					+ "`date` mediumint not null, "
             					+ "`player_id` mediumint unsigned not null, "
             					+ "`x` mediumint signed not null, "
             					+ "`y` mediumint unsigned not null, "
             					+ "`z` mediumint signed not null, "
             					+ "`blockchanged` mediumint unsigned not null, "
             					+ "`quantity` mediumint signed not null, "
             					+ "primary key (`access_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'chest' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close(); */
             
             //Commands
             EyeSpy.printInfo("Searching for Commands table");
             rs = conn.getMetaData().getTables(null, null, prefix + "commands", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'command' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "commands` ( "
             					+ "`cmd_id` mediumint unsigned not null auto_increment, "
             					+ "`player_id` mediumint unsigned not null, "
             					+ "`ser_id` mediumint unsigned not null, "
             					+ "`date` int(11) unsigned not null, "
             					+ "`command` varchar(255) not null, "
             					+ "primary key (`cmd_id`), "
             					+ "foreign key (`player_id`) REFERENCES " + prefix + "players(`player_id`), "
             					+ "foreign key (`ser_id`) REFERENCES " + prefix + "servers(`ser_id`) )" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'command' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close();
             
             /*World Removed from this build
             EyeSpy.printInfo("Searching for Worlds table");
             rs = conn.getMetaData().getTables(null, null, prefix + "world", null);
             if (!rs.next()) {
             	EyeSpy.printWarning("No 'world' table found, attempting to create one...");
             	PreparedStatement ps = conn
             			.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "world` ( "
             					+ "`world_id` tinyint unsigned not null auto_increment, "
             					+ "`wld_name` varchar(30) not null, "
             					+ "primary key (`world_id`));" );
             	ps.executeUpdate();
             	ps.close();
             	EyeSpy.printWarning("'world' table created!");
             } else {
             	EyeSpy.printInfo("Table found");
             }
             rs.close(); */
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Get the runnable stuff sorted.
      */
    public Runnable maintainConnection = new Runnable() {
     	
     	public void run() {
     		try {
                 Maintain.executeQuery();
     		} catch (SQLException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
     		}
     		EyeSpy.printInfo("EyeSpy has checked in with database");
     	}
     };
     
     /**
      * Adds the new block events found in the uk.co.m4numbers.EyeSpy.Listeners.BlockListener class. Each one fulfils the requirements for a standard SQL string.
      * @param name The name of the entity that triggered the block change
      * @param type The block that was changed, in ID form. This will be translated within the website
      * @param data Any additional data that the block possesses, such as direction
      * @param broken The state of the block, 0 is broken, 1 is placed, 2 is ignited, 3 is moved
      * @param x X Co-ordinate of the block
      * @param y Y Co-ordinate of the block
      * @param z Z Co-ordinate of the block
      * @param world The world that the block resides in
      * @throws e.printStackTrace should the SQL string fail
      * @deprecated For this build
      */
     public static void addNewBlock(String name, int type, byte data, byte broken, int x, int y, int z, String world) {
     	try {
     		InsertBlock.setLong( 1, ArgProcessing.getDateTime() );
     		InsertBlock.setInt( 2, playerExists(name) );
     		InsertBlock.setInt( 3, worldExists(world) );
     		InsertBlock.setInt( 4, type );
     		InsertBlock.setInt( 5, data );
     		InsertBlock.setInt( 6, x );
     		InsertBlock.setInt( 7, y );
     		InsertBlock.setInt( 8, z );
     		InsertBlock.setInt( 9, broken );
     		InsertBlock.executeUpdate();
     		InsertBlock.clearParameters();
     		EyeSpy.printInfo("Block Added!");
     	} catch (SQLException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	}
     }
     
     /**
      * Adds any chatter that occurs on the server into the database. This requires that the server is running HeroChat
      * @param name The name of the player sending the chatter
      * @param ch_name The name of the channel the chatter is occurring within
      * @param Server The name of the server that everything is being sent on
      * @param Message The actual chatter
      * @throws e.printStackTrace If the command fails
      */
     public static void addNewChat(String name, String ch_name, String Server, String Message) {
     	try {
 			InsertChat.setInt( 1, playerExists(name) );
 			InsertChat.setInt( 2, channelExists(ch_name) );
 			InsertChat.setInt( 3, ServerExists(Server) );
 			InsertChat.setLong( 4, ArgProcessing.getDateTime() );
 			InsertChat.setString( 5, Message );
 			InsertChat.executeUpdate();
 			InsertChat.clearParameters();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
     
     /**
      * Adds any commands used on the server into the database.
      * @param name This is the name of the entity sending the command
      * @param Server The name of the server that the commands are being executed on
      * @param Message This is the command itself
      * @throws e.printStackTrace If the command should fail
      */
     public static void addNewCommand(String name, String Server, String Message) {
     	try {
     		InsertCommand.setInt( 1, playerExists(name) );
     		InsertCommand.setInt(2, ServerExists( Server ) );
     		InsertCommand.setLong( 3, ArgProcessing.getDateTime() );
     		InsertCommand.setString( 4, Message );
     		InsertCommand.executeUpdate();
     		InsertCommand.clearParameters();
     	} catch (SQLException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	}
     }
     	
 	/**
 	 * Checks within the chatchannels database for any existing channels of that name. If it doesn't find it, it will add it to the table, and return the id no matter what
 	 * @param ch_name The name of the channel that is being checked
 	 * @return chId This is the ID of the channel in the chatchannel table, passed back to the chat table
 	 * @throws e.printStackTrace If the command should fail
 	 */
 	public static int channelExists(String ch_name) {
 		ResultSet rs = null;
 		int chId = 0;
 		try {
 			SelectChannel.setString(1, ch_name);
 			rs = SelectChannel.executeQuery();
 			if (!rs.next()) {
 				InsertChannel.setString(1, ch_name);
 				InsertChannel.executeUpdate();
 				InsertChannel.clearParameters();
 			}
 			rs = SelectChannel.executeQuery();
 			rs.first();
 			SelectChannel.clearParameters();
 			chId = rs.getInt("ch_id");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return chId;
 	}
 	
 	/**
 	 * Checks within the players table for any existing players that match the parameter. If there are no matching names, the player will be added and their ID returned no matter what.
 	 * @param name This is the players name which is being checked
 	 * @return plId This is the ID of the player that was checked
 	 * @throws e.printStackTrace If the command should fail
 	 */
 	public static int playerExists(String name) {
 		ResultSet rs = null;
 		int plId = 0;
 		try {
 			SelectPlayer.setString(1, name);
 			rs = SelectPlayer.executeQuery();
 			if (!rs.next()) {
 				InsertPlayer.setString(1, name);
 				InsertPlayer.executeUpdate();
 				InsertPlayer.clearParameters();
 			}
 			rs = SelectPlayer.executeQuery();
 			rs.first();
 			SelectPlayer.clearParameters();
 			plId = rs.getInt("player_id");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return plId;
 	}
 	
 	/**
 	 * Lets look at the servers we have available and give the log a server reference.
 	 * @param Server The server 
 	 * @return serId : The ID number of the server that we're working with
 	 */
 	public static int ServerExists(String Server) {
 		ResultSet rs = null;
 		int serId = 0;
 		try {
 			SelectServer.setString(1, Server);
 			rs = SelectServer.executeQuery();
 			if (!rs.next()) {
 				InsertServer.setString(1, Server);
 				InsertServer.executeUpdate();
 				InsertServer.clearParameters();
 			}
 			rs = SelectServer.executeQuery();
 			rs.first();
 			SelectServer.clearParameters();
 			serId = rs.getInt("ser_id");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return serId;
 	}
 	
 	/**
 	 * This checks the worlds table for any existing worlds with that name. If no worlds are found which match the parameter, a new row will be inserted into the table, the ID will be returned no matter what.
 	 * @param world This is the name of the world that is to be checked
 	 * @return wlId This is the ID of the world that was checked
 	 * @throws e.printStackTrace If the command should fail
 	 */
 	public static int worldExists(String world) {
 		ResultSet rs = null;
 		int wlId = 0;
 		try {
 			SelectWorld.setString(1, world);
 			rs = SelectWorld.executeQuery();
 			if (!rs.next()) {
 				InsertWorld.setString(1, world);
 				InsertWorld.executeUpdate();
 				InsertWorld.clearParameters();
 				EyeSpy.printInfo(world + " added to the worlds table");
 			}
 			rs = SelectWorld.executeQuery();
 			rs.first();
 			SelectWorld.clearParameters();
 			wlId = rs.getInt("world_id");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return wlId;
 	}
 }
