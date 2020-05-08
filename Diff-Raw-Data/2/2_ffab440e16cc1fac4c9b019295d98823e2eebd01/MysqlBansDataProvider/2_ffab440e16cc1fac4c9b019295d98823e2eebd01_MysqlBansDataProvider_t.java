 package net.serubin.serubans.dataproviders;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.BanInfo;
 
 public class MysqlBansDataProvider implements Runnable, BansDataProvider {
 
     private SeruBans plugin;
     private Connection conn;
     private String host;
     private String username;
     private String password;
     private String database;
 
     private int lastBanId;
 
     // TODO add on destroy to close connection
     /**
      * Initiates Mysql object
      * 
      * @param host
      *            of the mysql server
      * @param username
      *            of the mysql account
      * @param password
      *            of the mysql account
      * @param database
      *            to be used
      * @param plugin
      */
     public MysqlBansDataProvider(String host, String username, String password,
             String database, SeruBans plugin) {
         this.host = host;
         this.username = username;
         this.password = password;
         this.database = database;
         this.plugin = plugin;
 
         // Start db connection
         createConnection();
         createTable();
     }
 
     /**
      * Start sql connections
      */
 
     public void run() {
         maintainConnection();
     }
 
     /*
      * Database Connection
      */
 
     /**
      * Create MySQL connection
      */
     protected void createConnection() {
         String sqlUrl = String.format("jdbc:mysql://%s/%s", host, database);
 
         Properties sqlStr = new Properties();
         sqlStr.put("user", username);
         sqlStr.put("password", password);
         try {
             conn = DriverManager.getConnection(sqlUrl, sqlStr);
         } catch (SQLException e) {
             plugin.printError("A MySQL connection could not be made");
             e.printStackTrace();
         }
     }
 
     /**
      * Checks if tables are existing, then
      */
     protected void createTable() {
         try {
             plugin.printInfo("Searching for storage table");
             ResultSet rs = conn.getMetaData().getTables(null, null, "bans",
                     null);
             if (!rs.next()) {
                 plugin.printWarning("No 'bans' data table found, Attempting to create one...");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `bans` ( "
                         + "`id` mediumint unsigned not null auto_increment, "
                         + "`player_id` mediumint unsigned not null, "
                         + "`type` tinyint(2) not null, "
                         + "`length` bigint(20) not null, "
                         + "`mod` mediumint(8) unsigned not null, "
                         + "`date` DATETIME not null, "
                         + "`reason` varchar(255) not null, "
                         + "`display` tinyint(1) not null, "
                         + "primary key (`id`));");
                 ps.executeUpdate();
                 ps.close();
                 plugin.printWarning("'bans' data table created!");
             } else {
                 plugin.printInfo("Table found");
             }
             rs.close();
 
             plugin.printInfo("Searching for log table");
             rs = conn.getMetaData().getTables(null, null, "log", null);
             if (!rs.next()) {
                 plugin.printWarning("No 'log' data table found, Attempting to create one...");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `log` ( "
                         + "`id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT, "
                         + "`action` enum('delete','unban','update') NOT NULL, "
                         + "`banid` mediumint(8) unsigned NOT NULL, "
                         + "`ip` text NOT NULL, "
                         + "`data` text NOT NULL, "
                         + "primary key (`id`), key `id` (`id`));");
                 ps.executeUpdate();
                 ps.close();
                 plugin.printWarning("'log' data table created!");
             } else {
                 plugin.printInfo("Table found");
             }
             rs.close();
 
             plugin.printInfo("Searching for warns table");
             rs = conn.getMetaData().getTables(null, null, "warns", null);
             if (!rs.next()) {
                 plugin.printWarning("No 'warns' data table found, Attempting to create one...");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `warns` ( "
                         + "`id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT, "
                         + "`player_id` mediumint(8) unsigned NOT NULL, "
                         + "`ban_id` mediumint(8) unsigned NOT NULL, "
                         + "primary key (`id`), key `id` (`id`));");
                 ps.executeUpdate();
                 ps.close();
                 plugin.printWarning("'warns' data table created!");
             } else {
                 plugin.printInfo("Table found");
             }
             rs.close();
 
             plugin.printInfo("Searching for users table");
             rs = conn.getMetaData().getTables(null, null, "users", null);
             if (!rs.next()) {
                 plugin.printWarning("No 'users' data table found, Attempting to create one...");
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `users` ( "
                         + "`id` mediumint unsigned not null auto_increment, "
                         + "`name` varchar(16) not null, "
                         + "primary key (`id`), UNIQUE key `player` (`name`));");
                 ps.executeUpdate();
                 ps.close();
                 plugin.printWarning("'users' data table created!");
             } else {
                 plugin.printInfo("Table found");
             }
             rs.close();
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void maintainConnection() {
         PreparedStatement ps = null;
         try {
             ps = conn.prepareStatement("SELECT count(*) FROM bans limit 1;");
             ps.executeQuery();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         plugin.log.info("plugin has checked in with database");
     }
 
     /*
      * Get Player Info
      */
     /**
      * Gets ban info for player name on current ban
      * 
      * <pre>
      *  Note getPlayerTempBannedInfo
      * </pre>
      * 
      * @param name
      *            Player name
      * @return BanInfo Type for ban
      */
     public BanInfo getPlayerBannedInfo(String name) {
         return getPlayerBanInfo(name, SeruBans.BAN);
     }
 
     /**
      * Gets ban info for player name on current tempban
      * 
      * <pre>
      *  Note getPlayerBannedInfo
      * </pre>
      * 
      * @param name
      *            Player name
      * @return BanInfo Type for tempban
      */
     public BanInfo getPlayerTempBannedInfo(String name) {
         return getPlayerBanInfo(name, SeruBans.TEMPBAN);
     }
 
     private BanInfo getPlayerBanInfo(String name, int status) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT `type`, bans.id, `mod`, users.id, `name`, `length`, `reason`"
                     + " FROM `bans`"
                     + " INNER JOIN `users`"
                     + " ON bans.player_id=users.id"
                     + " WHERE `type`=?"
                     + " AND `name`=?");
             ps.setInt(1, status);
             ps.setString(2, name);
             rs = ps.executeQuery();
             while (rs.next()) {
                 BanInfo banInfo = new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("users.id"), rs.getInt("type"),
                         rs.getInt("mod"), rs.getLong("length"),
                         rs.getString("reason"), this);
                 return banInfo;
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * Gets all warns for given player
      * 
      * @param name
      *            Player name
      * @return a list of warns. Null if none
      */
     public List<BanInfo> getPlayerWarnsInfo(String name) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
            ps = conn.prepareStatement("SELECT `ban_id` users.id, users.name" + " FROM `warns`"
                     + " INNER JOIN `users`" + "  ON warns.player_id=id"
                     + " WHERE `name`=?");
             ps.setString(1, name);
             rs = ps.executeQuery();
 
             List<BanInfo> warnInfo = new ArrayList<BanInfo>();
 
             while (rs.next()) {
                 BanInfo banInfo = new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("users.id"), SeruBans.WARN, rs.getInt("mod"),
                         rs.getString("reason"), this);
                 warnInfo.add(banInfo);
             }
             return warnInfo;
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * Gets all player ban data
      * 
      * @param name
      *            player name
      * @return ban data
      */
     public List<BanInfo> getPlayerInfo(String name) {
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             ps = conn.prepareStatement("SELECT bans.id, p.name as playername, p.id as playerid, `date`, "
                     + "`display`, `type`, `length`, m.name as modname,p.id as modid, `reason` FROM bans,"
                     + " users p,users m WHERE bans.player_id = p.id AND bans.mod = m.id AND p.name=?");
             ps.setString(1, name);
             rs = ps.executeQuery();
 
             List<BanInfo> playerInfo = new ArrayList<BanInfo>();
 
             while (rs.next()) {
 
                 playerInfo.add(new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("playerid"), rs.getString("playername"),
                         rs.getInt("type"), rs.getInt("modid"),
                         rs.getString("modname"), rs.getLong("length"),
                         plugin.text().getUnixTimeStamp((Timestamp) rs.getObject("date")), rs.getString("reason"), this));
 
             }
             return playerInfo;
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
 
     }
 
     /**
      * Gets all player ban data for type
      * 
      * @param name
      *            player name
      * @return ban data
      */
     public List<BanInfo> getPlayerInfo(String name, int type) {
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             ps = conn.prepareStatement("SELECT bans.id, p.name as playername,p.id as playerid, `date`, "
                     + "`display`, `type`, `length`, m.name as modname,p.id as modid, `reason` FROM bans,"
                     + " users p,users m WHERE bans.player_id = p.id AND bans.mod = m.id AND p.name=? AND type=?");
             ps.setString(1, name);
             ps.setInt(2, type);
             rs = ps.executeQuery();
 
             List<BanInfo> playerInfo = new ArrayList<BanInfo>();
 
             while (rs.next()) {
 
                 playerInfo.add(new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("playerid"), rs.getString("playername"),
                         rs.getInt("type"), rs.getInt("modid"),
                         rs.getString("modname"), rs.getLong("length"),
                         plugin.text().getUnixTimeStamp(
                                 (Timestamp) rs.getObject("date")),
                         rs.getString("reason"), this));
 
             }
             return playerInfo;
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
 
     }
 
     /**
      * Gets ban info for id
      * 
      * @param id
      *            ban id
      * @return null if not found
      */
     public BanInfo getBanInfo(int id) {
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             ps = conn.prepareStatement("SELECT bans.id, p.name as playername,p.id as playerid, `date`, "
                     + "`display`, `type`, `length`, m.name as modname,p.id as modid, `reason` FROM bans,"
                     + " users p,users m WHERE bans.player_id = p.id AND bans.mod = m.id AND bans.id=?");
             ps.setInt(1, id);
             rs = ps.executeQuery();
 
             BanInfo banInfo = null;
 
             while (rs.next()) {
 
                 banInfo = new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("playerid"), rs.getString("playername"),
                         rs.getInt("type"), rs.getInt("modid"),
                         rs.getString("modname"), rs.getLong("length"),
                         plugin.text().getUnixTimeStamp(
                                 (Timestamp) rs.getObject("date")),
                         rs.getString("reason"), this);
 
             }
             return banInfo;
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * Get player name from id
      * 
      * <pre>
      *  will return null if it doesn't exist.
      *  use getPlayer(String name) to add a player to the database.
      * </pre>
      * 
      * @param id
      *            player id
      * @return player name
      */
     public String getPlayer(int id) {
 
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
             ps = conn.prepareStatement("SELECT `id`, `name` FROM `users` WHERE `id`=?");
             ps.setInt(1, id);
             rs = ps.executeQuery();
             if (rs.next()) {
                 return rs.getString("name");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Get player id from name
      * 
      * <pre>
      * Will create new id if the player is not in the database
      * @param name player name
      * @return player id
      */
     public int getPlayer(String name) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT `id`, `name` FROM `users` WHERE `name`=?");
             ps.setString(1, name);
             rs = ps.executeQuery();
             if (rs.next()) {
                 return rs.getInt("id");
             } else {
                 return addPlayer(name);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return 0;
     }
 
     /**
      * Checks if player is banned
      * 
      * @param name
      *            player
      * @return true : false
      */
     public boolean getPlayerStatus(String name) {
         name = name.toLowerCase();
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT bans.id, users.id, `name`, `type`"
                     + " FROM `bans` INNER JOIN `users`"
                     + " ON bans.player_id=users.id"
                     + " WHERE `name`=? AND (`type`=? OR `type`=?)");
             ps.setString(1, name);
             ps.setInt(2, SeruBans.BAN);
             ps.setInt(3, SeruBans.TEMPBAN);
             rs = ps.executeQuery();
             if (rs.next()) {
                 return true;
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     /**
      * Get BanInfo for players current ban.
      * 
      * @param name
      *            player
      * @return null of not found.
      */
     public BanInfo getCurrentBan(String name) {
         name = name.toLowerCase();
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT bans.player_id, `type`, bans.id, `mod`, users.id, `name`, `length`, `reason`"
                     + " FROM bans"
                     + " INNER JOIN users"
                     + " ON bans.player_id=users.id"
                     + " WHERE `type`=? OR `type`=?" + " AND `name`=?");
             ps.setInt(1, SeruBans.BAN);
             ps.setInt(2, SeruBans.TEMPBAN);
             ps.setString(3, name);
             rs = ps.executeQuery();
             while (rs.next()) {
                 BanInfo banInfo = new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("users.id"), rs.getInt("type"),
                         rs.getInt("mod"), rs.getLong("length"),
                         rs.getString("reason"), this);
                 return banInfo;
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /*
      * Player Actions
      */
 
     /**
      * Unban a player (temp or perm ban)
      * 
      * @param victim
      *            player
      */
     public boolean unbanPlayer(String player) {
         BanInfo banTest = getPlayerBannedInfo(player);
         if (banTest != null) {
             updateBanType(SeruBans.UNBAN, banTest.getBanId());
             return true;
         }
         banTest = getPlayerTempBannedInfo(player);
         if (banTest != null) {
             updateBanType(SeruBans.UNTEMPBAN, banTest.getBanId());
             return true;
         }
         return false;
     }
 
     /**
      * Adds ban
      * 
      * @param victim
      *            Player being banned
      * @param type
      *            Ban type
      * @param length
      *            (if tempban) Length of ban
      * @param mod
      *            Player that banned
      * @param reason
      *            Ban reason
      * @param display
      */
     public boolean addBan(String victim, int type, long length, String mod,
             String reason, int display) {
         victim = victim.toLowerCase();
         mod = mod.toLowerCase();
         PreparedStatement ps = null;
         ResultSet rs = null;
         // add player
         try {
             ps = conn.prepareStatement(
                     "INSERT INTO bans (`player_id`, `type`, `length`, `mod`, `date`, `reason`, `display`) VALUES(?,?,?,?,?,?,?);",
                     Statement.RETURN_GENERATED_KEYS);
             // Sets objects
             ps.setInt(1, getPlayer(victim));
             ps.setInt(2, type);
             ps.setLong(3, length);
             ps.setInt(4, getPlayer(mod));
             ps.setObject(5, plugin.text().getDateTime());
             ps.setString(6, reason);
             ps.setInt(7, display);
             ps.executeUpdate();
             // Gets generated keys
             rs = ps.getGeneratedKeys();
             if (rs.next()) {
                 Integer bId = rs.getInt(1);
                 lastBanId = bId;
 
                 plugin.printInfo(plugin.text().getBanTypeString(type) + ": "
                         + victim + " Ban Id: " + bId);
             } else {
                 plugin.printInfo("Error adding ban!");
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     private boolean updateBanType(int type, int bId) {
         PreparedStatement ps = null;
         try {
             ps = conn.prepareStatement("UPDATE `bans` SET `type`=? WHERE `id`=?;");
             ps.setInt(1, type);
             ps.setInt(2, bId);
             ps.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     /**
      * Update ban reason
      * 
      * @param id
      *            ban id
      * @param reason
      *            ban reason
      * @return
      */
     public boolean updateReason(int id, String reason) {
         PreparedStatement ps = null;
         PreparedStatement ps2 = null;
         try {
             ps = conn.prepareStatement("UPDATE `bans` SET `reason`=? WHERE `id`=?;");
             ps2 = conn.prepareStatement("INSERT INTO `log`(`action`, `banid`, `ip`, `data`) VALUES ('update',?,'In-Game',?)");
             String data = "UPDATE:Id=" + id + "Reason=" + reason;
             ps2.setInt(1, id);
             ps2.setString(2, data);
             ps2.executeUpdate();
             ps.setString(1, reason);
             ps.setInt(2, id);
             ps.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     private int addPlayer(String player) {
         player = player.toLowerCase();
         PreparedStatement ps = null;
         ResultSet rs = null;
         plugin.printInfo("Attempting to add player " + player + " to database;");
         // add player
         try {
             ps = conn.prepareStatement(
                     "INSERT INTO `users` (`name`) VALUES(?);",
                     Statement.RETURN_GENERATED_KEYS);
             ps.setString(1, player.toLowerCase());
             ps.executeUpdate();
             rs = ps.getGeneratedKeys();
             if (rs.next()) {
                 Integer playerId = rs.getInt(1);
                 plugin.printInfo("Player Added: " + player + " Id: " + playerId);
                 return playerId;
             } else {
                 plugin.printInfo("Error adding user!");
                 return 0;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return 0;
         }
     }
 
     /**
      * Add warn to database
      * 
      * @param playerId
      *            player
      * @param banId
      *            warn id
      */
 
     public boolean addWarn(int playerId, int banId) {
         PreparedStatement ps = null;
         try {
             ps = conn.prepareStatement(
                     "INSERT INTO `warns` (`player_id`, `ban_id`) VALUES(?,?);",
                     Statement.RETURN_GENERATED_KEYS);
             ps.setInt(1, playerId);
             ps.setInt(2, banId);
             ps.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     /**
      * Remove warn warning
      * 
      * @param playerId
      *            player Id
      * @param banId
      *            warn Id
      */
     public boolean removeWarn(int playerId, int banId) {
         PreparedStatement ps = null;
         try {
             ps = conn.prepareStatement("DELETE FROM `warns` WHERE `player_id`=? AND `ban_id`=?;");
             ps.setInt(1, playerId);
             ps.setInt(2, banId);
             ps.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     /*
      * Genera Ban Info
      */
     /**
      * 
      * Retrieves a list of permanent bans
      * 
      * @return list of bans
      */
     public List<BanInfo> getPermBans() {
         return getBans(SeruBans.BAN);
     }
 
     /**
      * Retrieves a list of temporary bans
      * 
      * @return a list of bans
      */
     public List<BanInfo> getTempBans() {
         return getBans(SeruBans.TEMPBAN);
     }
 
     public int getCurrentBanId(String name) {
         name = name.toLowerCase();
         int banId = getBanId(name, SeruBans.BAN);
         if (banId != 0) {
             return banId;
         }
         banId = getBanId(name, SeruBans.TEMPBAN);
         if (banId != 0) {
             return banId;
         }
         return 0;
     }
 
     private int getBanId(String name, int status) {
         name = name.toLowerCase();
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT bans.id, users.name, `type`, bans.player_id"
                     + " FROM `bans` INNER JOIN `users` ON bans.player_id=users.id"
                     + " WHERE users.name=? AND type=?");
             ps.setString(1, name);
             ps.setInt(2, status);
             rs = ps.executeQuery();
             while (rs.next()) {
                 return rs.getInt("bans.id");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return 0;
     }
 
     private List<BanInfo> getBans(int status) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT bans.id, bans.player_id, users.id, `mod`, `name`, `reason`, `length`"
                     + " FROM `bans`"
                     + " INNER JOIN `users`"
                     + " ON bans.player_id=users.id" + " WHERE `type`=?");
             ps.setInt(1, status);
             rs = ps.executeQuery();
             List<BanInfo> listInfo = new ArrayList<BanInfo>();
             while (rs.next()) {
                 BanInfo banInfo = new BanInfo(rs.getInt("bans.id"),
                         rs.getInt("users.id"), status, rs.getInt("mod"),
                         rs.getLong("length"), rs.getString("reason"), this);
                 listInfo.add(banInfo);
             }
             return listInfo;
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * Validate a ban id
      * 
      * @param ban
      *            Id
      * @return true : false
      */
     public boolean validateBanId(int id) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
             ps = conn.prepareStatement("SELECT `id` FROM `bans` WHERE `id`=?");
             ps.setInt(1, id);
             rs = ps.executeQuery();
             if (rs.next()) {
                 if (rs.getInt("id") != 0) {
                     return true;
                 }
             } else {
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return false;
     }
 
     /**
      * Gets last ban id.
      * 
      * @return lastBanId
      */
     public int getLastBanId() {
         return lastBanId;
     }
 }
