 package com.wimbli.onlineusers;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 public class OnlineUsersMySQL extends OnlineUsersDataSource {
 	
 	private String name = "OnlineUsers";
 
 	protected static final Logger log 		   = Logger.getLogger("Minecraft");
     private static String sqlTruncateTable	   = "TRUNCATE `"+OnlineUsers.table+"`";
     private static String sqlMakeTable 		   = "CREATE TABLE IF NOT EXISTS `"+OnlineUsers.table+"` ("+
     												"`name` varchar(32) NOT NULL, " +
     												"`time` datetime DEFAULT NULL, " +
     												"`time_total` int DEFAULT 0, " +
     												"PRIMARY KEY (`name`))";
     private static String sqlOnlineUser 	   = "INSERT INTO `"+OnlineUsers.table+"` (`name`, `time`, `online`) VALUES (?, NOW(), 1) ON DUPLICATE KEY UPDATE `time`=NOW(), `online`=1";
     private static String sqlOfflineUser 	   = "UPDATE `"+OnlineUsers.table+"` SET `time_total` = IF(`online`=1, `time_total` + TIMESTAMPDIFF(SECOND, `time`, NOW()), `time_total`), `online`=0 WHERE `name`=?";
     private static String sqlDeleteOfflineUser = "DELETE FROM `"+OnlineUsers.table+"` WHERE `name`=?";
     private static String sqlSetAllOffline     = "UPDATE `"+OnlineUsers.table+"` SET `time_total` = IF(`online`=1, `time_total` + TIMESTAMPDIFF(SECOND, `time`, NOW()), `time_total`), `online`=0";
 
 	// these are run see if update for older databases is needed, and then update them if so
     private static String sqlCheckTableExist   = "SHOW TABLES LIKE '"+OnlineUsers.table+"'";
     private static String sqlCheckTableTimeTt  = "SHOW COLUMNS FROM `"+OnlineUsers.table+"` WHERE `Field` = 'time_total'";
    private static String sqlAlterTableOnline  = "ALTER TABLE `"+OnlineUsers.table+"` ADD `online` bit(1) NOT NULL DEFAULT 0";
    private static String sqlAlterTableOnline2 = "ALTER TABLE `"+OnlineUsers.table+"` CHANGE  `online`  `online` TINYINT( 1 ) UNSIGNED NOT NULL DEFAULT  0";
     private static String sqlAlterTableTimeTtA = "ALTER TABLE `"+OnlineUsers.table+"` ADD `time_total` INT NOT NULL DEFAULT 0";
     private static String sqlAlterTableTimeTt1 = "ALTER TABLE `"+OnlineUsers.table+"` CHANGE `time_total` `time_total_old` TIME NOT NULL DEFAULT '00:00:00'";
     private static String sqlAlterTableTimeTt2 = "ALTER TABLE `"+OnlineUsers.table+"` ADD `time_total` INT NOT NULL DEFAULT 0";
     private static String sqlAlterTableTimeTt3 = "UPDATE `"+OnlineUsers.table+"` SET `time_total` = TIME_TO_SEC(`time_total_old`)";
     private static String sqlAlterTableTimeTt4 = "ALTER TABLE `"+OnlineUsers.table+"` DROP `time_total_old`";
 	
 	
 	@Override
 	public boolean init() {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (Exception e) {};
 		
 		return createTable();
 	}
 
 	@Override
 	public boolean addUser(String username) {
 		return execute(sqlOnlineUser,username);
 	}
 
 	@Override
 	public boolean removeUser(String username) {
 		return execute (sqlDeleteOfflineUser, username);
 	}
 
 	@Override
 	public boolean setUserOffline(String username) {
 		return execute (sqlOfflineUser, username);
 	}
 
 	@Override
 	public boolean setAllOffline() {
 		return execute (sqlSetAllOffline);
 	}
 	
 	@Override
 	public boolean removeAllUsers() {
 		return execute (sqlTruncateTable);
 	}
 	
 	private Connection getConnection() throws SQLException {
 		Connection conn = null;
 		try{
 			conn = DriverManager.getConnection(OnlineUsers.db,OnlineUsers.user,OnlineUsers.pass);
 		} catch (Exception e) {
 			log.severe(name + ": " + e.getMessage());
 		}
 		checkConnection(conn);
 		return conn;
 	}
 	
 	private boolean checkConnection (Connection conn) throws SQLException {
 		if (conn == null) {
 				log.severe("Could not connect to the database. Check your credentials in online-users.settings");
 			throw new SQLException();
 		}
 		if (!conn.isValid(5)) {
         	log.severe("Could not connect to the database.");
         	throw new SQLException();
         }
 		return true;
 	}
 	
 	private boolean execute(String sql) {
 		return execute(sql, null);
 	}
 	
 	private boolean execute(String sql, String player) {
 		Connection conn = null;
         PreparedStatement ps = null;
         try {
         	conn = getConnection();
         	ps = conn.prepareStatement(sql);
         	if (player != null && !player.equalsIgnoreCase("")) {
         		ps.setString(1, player);
         	}
         	
         	if (ps.execute()) {
         		return true;
         	}
         } catch (SQLException ex) {
         	log.severe(name + ": " + ex.getMessage());
         	String msg = name + ": could not execute the sql \"" + sql + "\"";
         	if (player != null ) {
         		msg += "    ?=" +player;
         	}
         	log.severe(msg);
         } finally {
             try {
                 if (ps != null) {
                     ps.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException ex) {
             	log.severe(name + ": " + ex.getMessage());
             }
         }
         return false;
 	}
 	
 	private boolean createTable() {
 		Connection conn = null;
         Statement s = null;
 		ResultSet rs = null;
         try {
         	conn = getConnection();
         	s = conn.createStatement();
         	s.executeUpdate(sqlMakeTable);
 			// Run sqlCheckTableTimeTt query to see if time_total column exists, and check column type
         	try {
	        	rs = s.executeQuery(sqlCheckTableTimeTt);
 	        	if (rs.first()) {
 					// `time_total` column exists, but does it need to be altered from TIME to INT?
 					if (!rs.getString("Type").toLowerCase().startsWith("int"))
 					{
		        		log.info(name + ": Updating Table, changing time_total column from TIME to INT");
 						// sadly altering a column directly from TIME to INT will reset all values to 0, so out of necessity,
 						// we first rename the column, then create a new one with the new type,
 						// then translate the values over, then delete the original column to clean up
 		        		s.executeUpdate(sqlAlterTableTimeTt1);
 		        		s.executeUpdate(sqlAlterTableTimeTt2);
 		        		s.executeUpdate(sqlAlterTableTimeTt3);
 		        		s.executeUpdate(sqlAlterTableTimeTt4);
 					}
 					rs.close();
 					s.close();
 					conn.close();
 					return true;
 				}
         		log.info(name + ": Updating Table");
         		s.executeUpdate(sqlAlterTableTimeTtA);
        		s.executeUpdate(sqlAlterTableOnline);
        		s.executeUpdate(sqlAlterTableOnline2);
         	} catch (SQLException ex2){}
         	rs = s.executeQuery(sqlCheckTableExist);
         	if (rs.first()) {
 				rs.close();
 				s.close();
 				conn.close();
         		return true;
         	}
         } catch (SQLException ex) {
         	log.severe(name + ": " + ex.getMessage());
         } finally {
             try {
 				if (rs != null)
 					rs.close();
                 if (s != null)
                     s.close();
                 if (conn != null)
                     conn.close();
             } catch (SQLException ex) {
             	log.severe(name + ": " + ex.getMessage());
             }
         }
         return false;
 	}
 }
