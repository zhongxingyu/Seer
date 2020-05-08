 package me.supermaxman.xestats.utils.MySQL;
 
 import me.supermaxman.xestats.XeStats;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import java.sql.*;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 /**
  * User: Benjamin
  * Date: 26/08/12
  * Time: 21:53
  */
 public class QueryManager {
 
     private static final FileConfiguration config = XeStats.conf;
     private static final String url = config.getString("xs.config.database");
     private static final String user = config.getString("xs.config.user");
     private static final String pass = config.getString("xs.config.password");
     private static Connection conn = null;
 
     public QueryManager() {
         initDB();
         createTables();
 
         new Thread() {
             @Override
             public void run() {
                 while (true) {
                     try {
                         HashMap<String, StatsUser> statsUserMap = (HashMap<String, StatsUser>) XeStats.statsUserMap.clone();
                         for (StatsUser statsUser : statsUserMap.values()) {
                             statsUser.dumpToDB();
                         }
 
                         sleep(5000);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
         }.start();
     }
 
     void initDB() {
         try {
             conn = DriverManager.getConnection(url, user, pass); //Creates the connection
         } catch (SQLException e) {
             printError(e.getStackTrace());
         }
     }
 
     public void closeConn() {
         try {
             conn.close();
         } catch (SQLException e) {
             printError(e.getStackTrace());
         }
     }
 
     static Logger log = Bukkit.getLogger();
 
     public void createTables() {
         try {
             PreparedStatement Statement = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS `XeStats` (" +
                             "  `ID` int(11) NOT NULL AUTO_INCREMENT," +
                             "  `UserName` varchar(20) NOT NULL," +
                             "  `Kills` mediumint(7) NOT NULL DEFAULT '0'," +
                             "  `Deaths` mediumint(7) NOT NULL DEFAULT '0'," +
                             "  `Hits` mediumint(7) NOT NULL DEFAULT '0'," +
                             "  `Swings` mediumint(7) NOT NULL DEFAULT '0'," +
                             "  `LongestKillRange` double NOT NULL DEFAULT '0'," +
                             "  PRIMARY KEY (`ID`)," +
                             "  UNIQUE KEY `UserName` (`UserName`)" +
                             ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;"
             );
             Statement.executeUpdate(); //Executes the query
             Statement.close(); //Closes the query
         } catch (SQLException e) {
             printError(e.getStackTrace());
         }
 
     }
 
 
     /**
      * Inits row in db based on player.getName()
      *
      * @param player The Player
      */
     public void CreateUser(Player player) {
         try {
             PreparedStatement Statement = conn.prepareStatement("INSERT IGNORE INTO `dev_database`.`XeStats` (`ID`, `UserName`, `Kills`, `Deaths`, `Hits`, `Swings`, `LongestKillRange`) VALUES (NULL, '" + player.getName() + "', '0', '0', '0', '0', '0');");
             Statement.executeUpdate(); //Executes the query
             Statement.close(); //Closes the query
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Adds player to list via DB record.
      *
      * @param player The Player
      */
     public void AddUser(Player player) {
         try {
             PreparedStatement Statement = conn.prepareStatement("SELECT * FROM  `XeStats` WHERE  `UserName` =  '" + player.getName() + "' LIMIT 0 , 30");
             ResultSet rs = Statement.executeQuery(); //Executes the query
            synchronized (XeStats.statsUserMap) {
                if (!XeStats.statsUserMap.containsKey(player.getName())) {
                    XeStats.statsUserMap.put(player.getName(),
                            new StatsUser(
                                    rs.getInt("ID"),
                                    rs.getString("UserName"),
                                    rs.getLong("Kills"),
                                    rs.getLong("Deaths"),
                                    rs.getLong("Hits"),
                                    rs.getLong("Swings"),
                                    rs.getFloat("LongestKillRange")
                            )
                    );
                 }
             }
             rs.close();
             Statement.close(); //Closes the query
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     void DumpStatsUser(StatsUser statsUser) {
         try {
             PreparedStatement Statement = conn.prepareStatement(
                     "UPDATE `XeStats`" +
                             " SET " +
                             "`Kills` =  '" + statsUser.getKills() + "'," +
                             "`Deaths` =  '" + statsUser.getDeaths() + "'," +
                             "`Hits` =  '" + statsUser.getHits() + "'," +
                             "`Swings` =  '" + statsUser.getSwings() + "'," +
                             "`LongestKillRange` =  '" + statsUser.getLongestKillRange() + "'" +
                             " WHERE  `XeStats`.`ID` =" + statsUser.getID() + ";"
             );
             Statement.executeUpdate(); //Executes the query
             Statement.close(); //Closes the query
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
     }
 
     public void handleQuit(Player player) {
         XeStats.statsUserMap.get(player.getName()).dumpToDB();
         XeStats.statsUserMap.remove(player.getName());
     }
 
     public void handleJoin(Player player) {
         CreateUser(player);
         AddUser(player);
     }
 
 
     void printError(StackTraceElement[] stackTraceElements) {
         log.severe("### MySQL Error ###");
         for (StackTraceElement element : stackTraceElements) {
             log.severe(element.toString());
         }
         log.severe("### End of error ###");
     }
 
 
 }
