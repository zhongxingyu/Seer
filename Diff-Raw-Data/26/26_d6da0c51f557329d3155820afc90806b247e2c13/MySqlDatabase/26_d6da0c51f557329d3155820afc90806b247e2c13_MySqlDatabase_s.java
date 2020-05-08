 package net.serubin.serubans.util;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 import net.serubin.serubans.SeruBans;
 
 public class MySqlDatabase {
 
     static Connection conn;
     public static String host;
     public static String username;
     public static String password;
     public static String database;
     private SeruBans plugin;
     private static String reason;
     private static String mod;
 
     public MySqlDatabase(String host, String username, String password,
             String database, SeruBans plugin) {
         this.plugin = plugin;
         this.host = host;
         this.username = username;
         this.password = password;
         this.database = database;
     }
 
     public static void startSQL() {
         createConnection();
         createTable();
         getPlayer();
         getBans();
         getTempBans();
     }
 
     protected static void createConnection() {
         String sqlUrl = String.format("jdbc:mysql://%s/%s", host, database);
 
         Properties sqlStr = new Properties();
         sqlStr.put("user", username);
         sqlStr.put("password", password);
         try {
             conn = DriverManager.getConnection(sqlUrl, sqlStr);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     protected static void createTable() {
         try {
             SeruBans.printInfo("Searching for storage table");
             ResultSet rs = conn.getMetaData().getTables(null, null, "bans",
                     null);
             if (!rs.next()) {
                 SeruBans.printWarning("No 'bans' data table found, Attempting to create one...");
                 PreparedStatement ps = conn
                         .prepareStatement("CREATE TABLE IF NOT EXISTS `bans` ( "
                                 + "`id` mediumint unsigned not null auto_increment, "
                                 + "`player_id` mediumint unsigned not null, "
                                 + "`type` tinyint(2) not null, "
                                 + "`length` bigint(20) not null, "
                                 + "`mod` varchar(16) not null, "
                                 + "`date` DATETIME not null, "
                                 + "`reason` varchar(255) not null, "
                                 + "`display` tinyint(1) not null, "
                                 + "primary key (`id`));");
                 ps.executeUpdate();
                 ps.close();
                 SeruBans.printWarning("'bans' data table created!");
             } else {
                 SeruBans.printInfo("Table found");
             }
             rs.close();
 
             SeruBans.printInfo("Searching for users table");
             rs = conn.getMetaData().getTables(null, null, "users", null);
             if (!rs.next()) {
                 SeruBans.printWarning("No 'users' data table found, Attempting to create one...");
                 PreparedStatement ps = conn
                         .prepareStatement("CREATE TABLE IF NOT EXISTS `users` ( "
                                 + "`id` mediumint unsigned not null auto_increment, "
                                 + "`name` varchar(16) not null, "
                                 + "primary key (`id`), UNIQUE key `player` (`name`));");
                 ps.executeUpdate();
                 ps.close();
                 SeruBans.printWarning("'user' data table created!");
             } else {
                 SeruBans.printInfo("Table found");
             }
             rs.close();
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     // TODO create get tempbans
 
     public static void getPlayer() {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT * FROM users;");
             rs = ps.executeQuery();
             while (rs.next()) {
                 Integer pId = rs.getInt("id");
                 String pName = rs.getString("name");
                 HashMaps.setPlayerList(pName, pId);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static void getBans() {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn
                     .prepareStatement("SELECT bans.player_id, bans.id, users.name, users.id"
                             + " FROM bans"
                             + " INNER JOIN users"
                             + "  ON bans.player_id=users.id"
                             + " WHERE (type = 1 OR type = 2) ");
             rs = ps.executeQuery();
             while (rs.next()) {
                 Integer bId = rs.getInt("bans.id");
                 String pName = rs.getString("name");
                 HashMaps.setBannedPlayers(pName, bId);
             }
         } catch (SQLException e) {
 
             e.printStackTrace();
         }
     }
 
     public static void getTempBans() {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT id, length" + " FROM bans"
                     + " WHERE (type = 2) ");
             rs = ps.executeQuery();
             while (rs.next()) {
                 Integer bId = rs.getInt("id");
                 Long length = rs.getLong("length");
                 HashMaps.setTempBannedTime(bId, length);
             }
         } catch (SQLException e) {
 
             e.printStackTrace();
         }
 
     }
 
     public static void addBan(String victim, int type, long length, String mod,
             String reason, int display) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         // add player
         try {
             ps = conn
                     .prepareStatement(
                             "INSERT INTO bans (`player_id`, `type`, `length`, `mod`, `date`, `reason`, `display`) VALUES(?,?,?,?,?,?,?);",
                             Statement.RETURN_GENERATED_KEYS);
             ps.setInt(1, HashMaps.getPlayerList(victim.toLowerCase()));
             ps.setInt(2, type);
             ps.setLong(3, length);
            ps.setString(4, mod);
             ps.setObject(5, ArgProcessing.getDateTime());
             ps.setString(6, reason);
             ps.setInt(7, display);
             ps.executeUpdate();
             rs = ps.getGeneratedKeys();
             if (type == 1 || type == 2) {
                 if (rs.next()) {
                     Integer bId = rs.getInt(1);
                     HashMaps.setBannedPlayers(victim.toLowerCase(), bId);
                     if (type == 2) {
                         HashMaps.setTempBannedTime(bId, length);
                     }
                     SeruBans.printInfo("Banned: " + victim + " Ban Id: " + bId);
                 } else {
                     SeruBans.printInfo("Error adding ban!");
                 }
             }
         } catch (SQLException e) {
 
             e.printStackTrace();
         }
     }
 
     public static void updateBan(int type, int bId) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         // add player
         try {
             ps = conn.prepareStatement("UPDATE bans SET type=? WHERE id=?;");
             ps.setInt(1, type);
             ps.setInt(2, bId);
             ps.executeUpdate();
         } catch (SQLException e) {
 
             e.printStackTrace();
         }
     }
 
     public static void addPlayer(String victim) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         SeruBans.printInfo("Attempting to add player " + victim
                 + " to database;");
         // add player
         try {
             ps = conn.prepareStatement("INSERT INTO users (name) VALUES(?);",
                     Statement.RETURN_GENERATED_KEYS);
             ps.setString(1, victim);
             ps.executeUpdate();
             rs = ps.getGeneratedKeys();
             if (rs.next()) {
                 Integer pId = rs.getInt(1);
                 HashMaps.setPlayerList(victim, pId);
                 SeruBans.printInfo("Player Added: " + victim + " Id: " + pId);
             } else {
                 SeruBans.printInfo("Error adding user!");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static String getReason(int id) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         reason = "";
         try {
             ps = conn
                     .prepareStatement("SELECT id, reason FROM bans WHERE (id = ?);");
             ps.setInt(1, id);
             rs = ps.executeQuery();
             if (rs.next()) {
                 reason = rs.getString("reason");
             }
         } catch (SQLException e) {
             e.printStackTrace();
 
         }
         return reason;
     }
 
     public static String getMod(int id) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         mod = "";
         try {
             ps = conn
                     .prepareStatement("SELECT id, `mod` FROM bans WHERE (id = ?);");
             SeruBans.printInfo(ps.toString());
             ps.setInt(1, id);
             rs = ps.executeQuery();
             if (rs.next()) {
                 mod = rs.getString("mod");
             }
         } catch (SQLException e) {
             e.printStackTrace();
 
         }
         return mod;
     }
 }
