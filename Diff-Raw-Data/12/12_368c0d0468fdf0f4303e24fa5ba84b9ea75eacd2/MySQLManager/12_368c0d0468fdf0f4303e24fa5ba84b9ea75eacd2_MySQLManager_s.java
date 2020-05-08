 package uk.co.CyniCode.CyniChat.DatabaseManagers;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import uk.co.CyniCode.CyniChat.CyniChat;
 import uk.co.CyniCode.CyniChat.DataManager;
 import uk.co.CyniCode.CyniChat.objects.Channel;
 import uk.co.CyniCode.CyniChat.objects.UserDetails;
 
 /**
  * Deal with all things MySQL'y
  * @author Matthew Ball
  *
  */
 public class MySQLManager {
 
 	private String hostname;
 	private int port;
 	private String Username;
 	private String Password;
 	private String Database;
 	private String Prefix;
 	private Connection conn;
 	
 	private static Map<String,UserDetails> Players = new HashMap<String, UserDetails>();
 	private static Map<String,Channel> channels = new HashMap<String, Channel>();
 	
 	private PreparedStatement InsertChannel;
 	private PreparedStatement InsertPlayer;
 	private PreparedStatement AddBan;
 	private PreparedStatement AddMute;
 	private PreparedStatement JoinChannel;
 	private PreparedStatement UpdateChannel;
 	private PreparedStatement UpdatePlayer;
 	private PreparedStatement AddIgnoring;
 	
 	/**
 	 * Boot up the plugin and do all the connecting bullshit.
 	 * Basically... connect, generate tables, make statements
 	 * if @throws error then we connect to JSON instead 
 	 * @param plugin : The thing we use to get the config options
 	 * @return true when complete
 	 */
 	public boolean startConnection( CyniChat plugin ) {
 		this.hostname = plugin.getConfig().getString("CyniChat.database.host");
 		this.port = 3306; //plugin.getConfig().getInt("CyniChat.database.port");
 		this.Username = plugin.getConfig().getString("CyniChat.database.username");
 		this.Password = plugin.getConfig().getString("CyniChat.database.password");
 		this.Database = plugin.getConfig().getString("CyniChat.database.database");
 		this.Prefix = plugin.getConfig().getString("CyniChat.database.prefix");
 		
 		if ( connect() == false ) {
 			CyniChat.SQL = false;
 			CyniChat.JSON = true;
 			CyniChat.printSevere("Switching to JSON data usage!");
 			return false;
 		}
 		if ( generateTables( Prefix ) == false ) {
 			CyniChat.SQL = false;
 			CyniChat.JSON = true;
 			CyniChat.printSevere("Switching to JSON data usage!");
 			return false;
 		}
 		if ( prepareStatements() == false ) {
 			CyniChat.SQL = false;
 			CyniChat.JSON = true;
 			CyniChat.printSevere("Switching to JSON data usage!");
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Actually connect to the database with the information we've been given
 	 * @return true upon completion
 	 */
 	private boolean connect() {
 		String sqlUrl = String.format("jdbc:mysql://%s:%s/%s", hostname, port, Database);
 		
 		Properties sqlStr = new Properties();
 		sqlStr.put("user", Username);
 		sqlStr.put("password", Password);
 		sqlStr.put("autoReconnect", "true");
 		CyniChat.printDebug("H:"+hostname+" P:"+port+" D:"+Database+" U:"+Username+" Pass:"+Password);
 		try {
 			conn = DriverManager.getConnection(sqlUrl, sqlStr);
 		} catch (SQLException e) {
 			CyniChat.printSevere("A MySQL connection could not be made!");
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Make the statements that we're going to use a fuckton of times
 	 * @return true when complete
 	 */
 	private boolean prepareStatements() {
 		try {
 			InsertPlayer = conn.prepareStatement("INSERT INTO `"+Prefix+"players` "
 					+ "(`player_name`,`player_name_clean`,`active_channel`,`can_ignore`) "
 					+ "VALUES (?,?,?,'1')", Statement.RETURN_GENERATED_KEYS);
 			InsertChannel = conn.prepareStatement("INSERT INTO `"+Prefix+"channels` "
 					+ "(`channel_name`,`channel_name_clean`,`channel_nickname`,`channel_pass`) "
 					+ "VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
 			AddBan = conn.prepareStatement("INSERT INTO `"+Prefix+"banned` "
 					+ "(`bannee_id`,`channel_id`) VALUES (?,?) "
 					+ "ON DUPLICATE KEY UPDATE `bannee_id`=`bannee_id`", Statement.RETURN_GENERATED_KEYS);
 			AddMute = conn.prepareStatement("INSERT INTO `"+Prefix+"muted` "
 					+ "(`mutee_id`,`channel_id`) VALUES (?,?) "
 					+ "ON DUPLICATE KEY UPDATE `mutee_id`=`mutee_id`", Statement.RETURN_GENERATED_KEYS);
 			JoinChannel = conn.prepareStatement("INSERT INTO `"+Prefix+"current_channel` "
 					+ "(`player_id`,`channel_id`) "
 					+ "VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
 			UpdateChannel = conn.prepareStatement("UPDATE `"+Prefix+"channels` "
 					+ "SET `channel_desc`=?, "
 					+ "`channel_irc_name`=?, "
 					+ "`channel_irc_pass`=?, "
 					+ "`channel_colour`=?, "
 					+ "`channel_protected`=? "
 					+ "WHERE `channel_id`=?", Statement.RETURN_GENERATED_KEYS);
 			UpdatePlayer = conn.prepareStatement("UPDATE `"+Prefix+"players` "
 					+ "SET `active_channel`=?, "
 					+ "`player_silenced`=?, "
 					+ "`can_ignore`=? "
 					+ "WHERE `player_id`=?", Statement.RETURN_GENERATED_KEYS);
 			AddIgnoring = conn.prepareStatement("INSERT INTO `"+Prefix+"ignoring` "
 					+ "(`ignorer_id`,`ignoree_id`) VALUES (?,?) "
 					+ "ON DUPLICATE KEY UPDATE `ignorer_id`=`ignorer_id`", Statement.RETURN_GENERATED_KEYS);
 		} catch (SQLException e) {
 			CyniChat.printSevere("Statement preparation has failed!");
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Save the channel data
 	 * @param channels : Everything we're going to save
 	 * 
 	 * @WARNING This is an inefficient and shitty method of updating no-matter-what. Even if nothing has changed.
 	 * Forgive me...
 	 */
 	public void saveChannels(Map<String, Channel> channels) {
 		Set<String> keys = channels.keySet();
 		Iterator<String> keyIterate = keys.iterator();
 		while (keyIterate.hasNext()) {
 			Channel current = channels.get(keyIterate.next());
 			try {
 				PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM `"+Prefix+"channels` WHERE `channel_id`='"+current.getID()+"'");
 				ResultSet rs = ps.executeQuery();
 				current.printAll();
 				while (rs.next())
 					if ( rs.getInt(1) == 0 ) {
 						InsertChannel.setString(1, current.getName());
 						InsertChannel.setString(2, current.getName().toLowerCase());
 						InsertChannel.setString(3, current.getNick());
 						InsertChannel.setString(4, current.getPass());
 						InsertChannel.execute();
 						
 						ResultSet generatedKeys = InsertChannel.getGeneratedKeys();
 						if (generatedKeys.next()) {
 							current.setId( generatedKeys.getInt(1) );
 						}
 						
 						InsertChannel.clearParameters();
 					}
 				UpdateChannel.setString(1, current.getDesc());
 				UpdateChannel.setString(2, current.getIRC());
 				UpdateChannel.setString(3, current.getIRCPass());
 				UpdateChannel.setString(4, current.getColour().name());
 				UpdateChannel.setBoolean(5, current.isProtected());
 				UpdateChannel.setInt( 6, current.getID() );
 				UpdateChannel.execute();
 				UpdateChannel.clearParameters();
 			} catch (SQLException e) {
 				CyniChat.printSevere("Saving failed on channel: "+current.getName());
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Save users that have been online at some point since the last save
 	 * @param loadedPlayers : The active players that we're saving
 	 * 
 	 * @WARNING : See last @WARNING.
 	 */
 	public void saveUsers(Map<String, UserDetails> loadedPlayers) {
 		Set<String> keys = loadedPlayers.keySet();
 		Iterator<String> keyIterate1 = keys.iterator();
 		while (keyIterate1.hasNext()) {
 			String username = keyIterate1.next();
 			UserDetails current = loadedPlayers.get( username );
 			try {
 				PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM `"+Prefix+"players` WHERE `player_id`='"+current.getID()+"'");
 				ResultSet rs = ps.executeQuery();
 				while (rs.next())
 					if (rs.getInt(1) == 0) {
 						InsertPlayer.setString(1, username );
 						InsertPlayer.setString(2, username.toLowerCase());
 						InsertPlayer.setInt(3, DataManager.returnAllChannels().get( current.getCurrentChannel() ).getID());
 						InsertPlayer.execute();
 						
 						ResultSet generatedKeys = InsertPlayer.getGeneratedKeys();
 						if (generatedKeys.next()) {
 							current.setId( generatedKeys.getInt(1) );
 						}
 						
 						InsertPlayer.clearParameters();
 					} else {
 						UpdatePlayer.setInt(1, DataManager.returnAllChannels().get( current.getCurrentChannel() ).getID());
 						UpdatePlayer.setBoolean(2, current.getSilenced());
 						UpdatePlayer.setBoolean(3, current.canIgnore());
 						UpdatePlayer.setInt(4, current.getID() );
 						UpdatePlayer.execute();
 						UpdatePlayer.clearParameters();
 					}
 				ps.close();
 				rs.close();
 			} catch (SQLException e) {
 			}
 		}
 		
 		Iterator<String> keyIterate2 = keys.iterator();
 		while (keyIterate2.hasNext()) {
 			String username = keyIterate2.next();
 			UserDetails current = loadedPlayers.get( username );
 			try {
 				PreparedStatement ps2 = conn.prepareStatement("DELETE FROM `"+Prefix+"current_channel` WHERE `player_id`='"+current.getID()+"'");
 				ps2.execute();
 				ps2.close();
 				List<String> Joined = current.getAllChannels();
 				Iterator<String> joinIter = Joined.iterator();
 				while (joinIter.hasNext()) {
 					Channel curChan = DataManager.getChannel(joinIter.next());
 					JoinChannel.setInt(1, current.getID());
 					JoinChannel.setInt(2, curChan.getID());
 					JoinChannel.execute();
 					JoinChannel.clearParameters();
 				}
 				
 				PreparedStatement ps4 = conn.prepareStatement("DELETE FROM `"+Prefix+"ignoring` WHERE `ignorer_id`='"+current.getID()+"'");
 				ps4.execute();
 				ps4.close();
 				List<String> Ignoring = current.getIgnoring();
 				Iterator<String> ignoIter = Ignoring.iterator();
 				while (ignoIter.hasNext()) {
 					AddIgnoring.setInt(1, current.getID());
 					AddIgnoring.setInt(2, DataManager.getDetails(ignoIter.next()).getID());
 					AddIgnoring.execute();
 					AddIgnoring.clearParameters();
 				}
 				
 				PreparedStatement ps5 = conn.prepareStatement("DELETE FROM `"+Prefix+"muted` WHERE `mutee_id`='"+current.getID()+"'");
 				ps5.execute();
 				ps5.close();
 				List<String> mutedIn = current.getMutedChannels();
 				Iterator<String> muteIter = mutedIn.iterator();
 				while ( muteIter.hasNext() ) {
 					AddMute.setInt( 1, current.getID() );
 					AddMute.setInt( 2, DataManager.getChannel( muteIter.next() ).getID() );
 					AddMute.execute();
 					AddMute.clearParameters();
 				}
 				
 				PreparedStatement ps6 = conn.prepareStatement("DELETE FROM `"+Prefix+"banned` WHERE `bannee_id`='"+current.getID()+"'");
 				ps6.execute();
 				ps6.close();
 				List<String> bannedIn = current.getBannedChannels();
 				Iterator<String> bannIter = bannedIn.iterator();
 				while ( bannIter.hasNext() ) {
 					AddBan.setInt( 1, current.getID() );
 					AddBan.setInt( 2, DataManager.getChannel( bannIter.next() ).getID() );
 					AddBan.execute();
 					AddBan.clearParameters();
 				}
 				
 			} catch (SQLException e) {
 				CyniChat.printSevere("Saving failed on player: "+username);
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Get all the players and their information from the database
 	 * @return a Map of all the players and their details
 	 */
 	public Map<String, UserDetails> returnPlayers() {
 		try {
 			PreparedStatement ps = conn.prepareStatement(
 					  "SELECT `player_id`,`player_name`,`channel_name`,`player_silenced`,`can_ignore` FROM `"+Prefix+"players` "
 					+ "INNER JOIN `"+Prefix+"channels` ON "+Prefix+"channels.`channel_id`="+Prefix+"players.`active_channel`" );
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				String name;
 				String active;
 				Boolean silenced;
 				Boolean canIgnore;
 				List<String> JoinedChannels = new ArrayList<String>();
 				List<String> Ignoring = new ArrayList<String>();
 				List<String> BannedFrom = new ArrayList<String>();
 				List<String> MutedIn = new ArrayList<String>();
 				name = rs.getString(2);
 				active = rs.getString(3);
 				silenced = rs.getBoolean(4);
 				canIgnore = rs.getBoolean(5);
 				
 				PreparedStatement ps2 = conn.prepareStatement("SELECT `channel_name` FROM `"+Prefix+"current_channel` "
 						+ "INNER JOIN `"+Prefix+"channels` ON "+Prefix+"channels.channel_id="+Prefix+"current_channel.channel_id "
 						+ "WHERE "+Prefix+"current_channel.player_id='"+rs.getInt(1)+"'");
 				ResultSet rs2 = ps2.executeQuery();
 				while (rs2.next()) {
 					JoinedChannels.add( rs2.getString(1) );
 				}
 				rs2.close();
 				ps2.close();
 				
 				PreparedStatement ps3 = conn.prepareStatement("SELECT `player_name` FROM `"+Prefix+"ignoring` "
 						+ "INNER JOIN `"+Prefix+"players` ON "+Prefix+"players.player_id="+Prefix+"ignoring.ignoree_id "
 						+ "WHERE "+Prefix+"ignoring.ignorer_id='"+rs.getInt(1)+"'");
 				ResultSet rs3 = ps3.executeQuery();
 				while ( rs3.next() )
 					Ignoring.add( rs3.getString(1) );
 				rs3.close();
 				ps3.close();
 				
 				PreparedStatement ps4 = conn.prepareStatement("SELECT `channel_name` FROM `"+Prefix+"banned` "
 						+ "INNER JOIN `"+Prefix+"channels` ON "+Prefix+"channels.`channel_id`="+Prefix+"banned.`channel_id` "
 						+ "WHERE "+Prefix+"banned.bannee_id='"+rs.getInt(1)+"'");
 				ResultSet rs4 = ps4.executeQuery();
 				while ( rs4.next() )
 					BannedFrom.add( rs4.getString(1) );
 				ps4.close();
 				rs4.close();
 				
 				PreparedStatement ps5 = conn.prepareStatement( "SELECT `channel_name` FROM `"+Prefix+"muted` "
 						+ "INNER JOIN `"+Prefix+"channels` ON "+Prefix+"channels.channel_id="+Prefix+"muted.channel_id "
 						+ "WHERE "+Prefix+"muted.`mutee_id`='"+rs.getInt(1)+"'");
 				ResultSet rs5 = ps5.executeQuery();
 				while ( rs5.next() )
 					MutedIn.add( rs5.getString(1) );
 				
 				UserDetails current = new UserDetails();
 				current.loadData( rs.getInt(1), active, silenced, canIgnore, JoinedChannels, MutedIn, BannedFrom, Ignoring);
 				Players.put(name, current);
 			}
 		} catch (SQLException e) {
 			CyniChat.printSevere("Player loading has failed!");
 			e.printStackTrace();
 			CyniChat.killPlugin();
 		}
 		return Players;
 	}
 	
 	/**
 	 * Generate a map of all the channels in the various tables
 	 * @return the map of all the channels along with their information
 	 */
 	public Map<String, Channel> returnChannels() {
 		try {
 			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `"+Prefix+"channels`");
 			ResultSet rs = ps.executeQuery();
 			while ( rs.next() ) {
 				int ID = rs.getInt(1);
 				String name = rs.getString(2);
 				String nick = rs.getString(4);
				String irc = "";
				String ircPass = "";
				String desc = rs.getString(5);
				String pass = rs.getString(6);
				String colour = rs.getString(7);
				Boolean protect = rs.getBoolean(8);
 				Channel current = new Channel();
 				current.loadChannel(ID, name, nick, irc, ircPass, desc, pass, colour, protect);
 				current.printAll();
 				try {
 					channels.put(rs.getString(2).toLowerCase(),current);
 				} catch (NullPointerException e) {
 					CyniChat.printSevere("Null Pointer found!");
 					e.printStackTrace();
 				}
 			}
 		} catch (SQLException e) {
 			CyniChat.printSevere("Channel loading has failed!");
 			e.printStackTrace();
 			CyniChat.killPlugin();
 		}
 		return channels;
 	}
 	
 	/**
 	 * Check and make all the tables required for this plugin
 	 * @param prefix : The prefix of the tables that you want
 	 * @return true when complete
 	 */
 	private boolean generateTables( String prefix ) {
 		ResultSet rs;
 		try {
 			
 			//Channels
 			CyniChat.printInfo("Searching for channels table");
 			rs = conn.getMetaData().getTables(null, null, prefix + "channels", null);
 			if (!rs.next()) {
 				CyniChat.printWarning("No 'channels' table found, attempting to regenerate...");
 				PreparedStatement ps = conn
 						.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "channels` ( "
 								+ "`channel_id` int not null auto_increment, "
 								+ "`channel_name` varchar(64) not null, "
 								+ "`channel_name_clean` varchar(64) not null, "
 								+ "`channel_nickname` varchar(8) not null, "
 								+ "`channel_irc_name` varchar(32) default '', "
 								+ "`channel_irc_pass` varchar(32) default '', "
 								+ "`channel_desc` varchar(128), "
 								+ "`channel_pass` varchar(32) not null DEFAULT '', "
 								+ "`channel_colour` VARCHAR(16) not null DEFAULT 'GRAY', "
 								+ "`channel_protected` tinyint(1) null, "
 								+ "PRIMARY KEY (channel_id) "
 								+ ");" );
 				ps.executeUpdate();
 				ps.close();
 				CyniChat.printWarning("'channels' table created!");
 				PreparedStatement ps2 = conn.prepareStatement("INSERT INTO `"+prefix+"channels` "
 						+ "(`channel_name`,`channel_name_clean`,`channel_nickname`,`channel_desc`) "
 						+ "VALUES ('Global','global','g','The global channel for everyone!')");
 				ps2.executeUpdate();
 				ps2.close();
 				CyniChat.printWarning("'channels' table populated!");
 			} else {
 				CyniChat.printInfo("Channels table found");
 			}
 			rs.close();
 			
 			//Players
 			CyniChat.printInfo("Searching for players table");
 			rs = conn.getMetaData().getTables(null, null, prefix + "players", null);
 			if (!rs.next()) {
 				CyniChat.printWarning("No 'players' table found, attempting to regenerate...");
 				PreparedStatement ps = conn
 						.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "players` ( "
 								+ "`player_id` int not null auto_increment, "
 								+ "`player_name` varchar(64) not null, "
 								+ "`player_name_clean` varchar(64) not null, "
 								+ "`active_channel` int not null DEFAULT '1', "
 								+ "`player_silenced` tinyint(1) null, "
 								+ "`can_ignore` tinyint(1) null, "
 								+ "PRIMARY KEY (player_id), "
 								+ "FOREIGN KEY (active_channel) REFERENCES "+prefix+"channels(channel_id) "
 								+ ");" );
 				ps.executeUpdate();
 				ps.close();
 				CyniChat.printWarning("'players' table created!");
 			} else {
 				CyniChat.printInfo("Players table found");
 			}
 			rs.close();
 			
 			//Current Channel
 			CyniChat.printInfo("Searching for current channel table");
 			rs = conn.getMetaData().getTables(null, null, prefix + "current_channel", null);
 			if (!rs.next()) {
 				CyniChat.printWarning("No 'current_channel' table found, attempting to regenerate...");
 				PreparedStatement ps = conn
 						.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "current_channel` ( "
 								+ "`player_id` int not null, "
 								+ "`channel_id` int not null, "
 								+ "`mod` tinyint(1) null, "
 								+ "UNIQUE KEY `current_chan` (`player_id`,`channel_id`), "
 								+ "FOREIGN KEY (player_id) REFERENCES "+prefix+"players(player_id), "
 								+ "FOREIGN KEY (channel_id) REFERENCES "+prefix+"channels(channel_id) "
 								+ ");" );
 				ps.executeUpdate();
 				ps.close();
 				CyniChat.printWarning("'current_channel' table created!");
 			} else {
 				CyniChat.printInfo("Current channel table found");
 			}
 			rs.close();
 			
 			//Banned
 			CyniChat.printInfo("Searching for banned table");
 			rs = conn.getMetaData().getTables(null, null, prefix + "banned", null);
 			if (!rs.next()) {
 				CyniChat.printWarning("No 'banned' table found, attempting to regenerate...");
 				PreparedStatement ps = conn
 						.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "banned` ( "
 								+ "`bannee_id` int not null, "
 								+ "`channel_id` int not null, "
 								+ "UNIQUE KEY `banned_chan` (`bannee_id`,`channel_id`), "
 								+ "FOREIGN KEY (bannee_id) REFERENCES "+prefix+"players(player_id), "
 								+ "FOREIGN KEY (channel_id) REFERENCES "+prefix+"channels(channel_id) "
 								+ ");" );
 				ps.executeUpdate();
 				ps.close();
 				CyniChat.printWarning("'banned' table created!");
 			} else {
 				CyniChat.printInfo("Banned table found");
 			}
 			rs.close();
 			
 			//Ignoring
 			CyniChat.printInfo("Searching for ignoring table");
 			rs = conn.getMetaData().getTables(null, null, prefix + "ignoring", null);
 			if (!rs.next()) {
 				CyniChat.printWarning("No 'ignoring' table found, attempting to create one...");
 				PreparedStatement ps = conn
 						.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "ignoring` ( "
 								+ "`ignorer_id` int not null, "
 								+ "`ignoree_id` int not null, "
 								+ "UNIQUE KEY `ignoring_chan` (`ignorer_id`,`ignoree_id`), "
 								+ "FOREIGN KEY (ignorer_id) REFERENCES "+prefix+"players(player_id), "
 								+ "FOREIGN KEY (ignoree_id) REFERENCES "+prefix+"players(player_id) "
 								+ ");" );
 				ps.executeUpdate();
 				ps.close();
 				CyniChat.printWarning("'ignoring' table created!");
 			} else {
 				CyniChat.printInfo("Ignoring table found");
 			}
 			
 			//Muted
 			CyniChat.printInfo("Searching for muted table");
 			rs = conn.getMetaData().getTables(null, null, prefix + "muted", null);
 			if (!rs.next()) {
 				CyniChat.printWarning("No 'muted' table found, attempting to create one...");
 				PreparedStatement ps = conn
 						.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "muted` ( "
 								+ "`channel_id` int not null, "
 								+ "`mutee_id` int not null, "
 								+ "UNIQUE KEY `muted_chan` (`channel_id`,`mutee_id`), "
 								+ "FOREIGN KEY (channel_id) REFERENCES "+prefix+"channels(channel_id), "
 								+ "FOREIGN KEY (mutee_id) REFERENCES "+prefix+"players(player_id) "
 								+ ");" );
 				ps.executeUpdate();
 				ps.close();
 				CyniChat.printWarning("'muted' table created!");
 			} else {
 				CyniChat.printInfo("Muted table found");
 			}
 			
 			rs.close();
 			
 		} catch (SQLException e) {
 			CyniChat.printSevere("The tables could not be generated!");
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 }
