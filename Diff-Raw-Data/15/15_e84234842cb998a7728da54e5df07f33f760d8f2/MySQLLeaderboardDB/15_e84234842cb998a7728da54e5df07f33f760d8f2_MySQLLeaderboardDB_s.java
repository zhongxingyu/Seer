 package me.DDoS.Quarantine.leaderboard.mysql;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import me.DDoS.Quarantine.Quarantine;
 import me.DDoS.Quarantine.leaderboard.LeaderData;
 import me.DDoS.Quarantine.leaderboard.Leaderboard;
 import me.DDoS.Quarantine.leaderboard.LeaderboardDB;
 
 /**
  *
  * @author DDoS
  */
 public class MySQLLeaderboardDB implements LeaderboardDB {
 
     private final String tableName;
     private final int pageSize;
     private final Connection connection;
 
     public MySQLLeaderboardDB(String lbName, int pageSize) {
 
         this.tableName = Leaderboard.DB_NAME + "." + "q_" + lbName + "_lb";
         this.pageSize = pageSize;
 
         Connection conn = null;
 
         try {
 
             String url = "jdbc:mysql://" + Leaderboard.HOST + ":" + Leaderboard.PORT + "/" + Leaderboard.DB_NAME;
             conn = DriverManager.getConnection(url, Leaderboard.USER, Leaderboard.PASSWORD);
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't connect to MySQL server. Error: " + ex.getMessage());
 
         }
 
         connection = conn;
 
         if (connection == null) {
 
             return;
 
         }
 
         Quarantine.log.info("[Quarantine] Leaderboard connection to MySQL server for zone '" + lbName + "' was established.");
 
         try {
 
             verifyTable();
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't SQL close statement. Error: " + ex.getMessage());
 
         }
     }
 
     private void verifyTable() throws SQLException {
 
         String createString =
                 "CREATE TABLE IF NOT EXISTS " + tableName + " "
                 + "(player_name VARCHAR(20) NOT NULL, "
                 + "score INT, "
                 + "rank INT, "
                 + "PRIMARY KEY (player_name))";
 
         Statement stmt = null;
 
         try {
 
             stmt = connection.createStatement();
             stmt.executeUpdate(createString);
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't create SQL table 'leaderboard'. Error: " + ex.getMessage());
 
         } finally {
 
             if (stmt != null) {
 
                 stmt.close();
 
             }
         }
     }
 
     @Override
     public void disconnect() {
 
         if (connection == null) {
 
             return;
 
         }
 
         try {
 
             connection.close();
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't close connection to SQL server. Error: " + ex.getMessage());
 
         }
     }
 
     @Override
     public void rank(String member, int score) {
 
         if (connection == null) {
 
             return;
 
         }
 
         String updateString = "REPLACE INTO " + tableName + " "
                 + "SET player_name = ?, score = ?";
 
         PreparedStatement update = null;
 
         try {
 
             update = connection.prepareStatement(updateString);
 
             update.setString(1, member);
             update.setInt(2, score);
             update.executeUpdate();
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't update player score in SQL DB. Error: " + ex.getMessage());
 
         } finally {
 
             if (update != null) {
 
                 try {
 
                     update.close();
 
                 } catch (SQLException ex) {
 
                     Quarantine.log.info("[Quarantine] Couldn't close SQL statement. Error: " + ex.getMessage());
 
                 }
             }
         }
 
         try {
 
             sortTable();
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't close SQL statement. Error: " + ex.getMessage());
 
         }
     }
 
     private void sortTable() throws SQLException {
 
         String setString = "SET @incr=0";
         String updateString = "UPDATE " + tableName + " SET rank=(@incr:=@incr+1) ORDER BY score DESC";
 
         Statement set = null;
         Statement update = null;
 
         try {
 
             set = connection.createStatement();
             set.execute(setString);
 
             update = connection.createStatement();
             update.executeUpdate(updateString);
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't sort table by score in SQL DB. Error: " + ex.getMessage());
 
         } finally {
 
             if (set != null) {
 
                 set.close();
 
             }
 
             if (update != null) {
 
                 update.close();
 
             }
         }
     }
 
     @Override
     public int getScore(String member) {
 
         int score = 0;
 
         if (connection == null) {
 
             return score;
 
         }
 
         String statmenentString = "SELECT score FROM " + tableName + " "
                 + "WHERE player_name LIKE ?";
 
         PreparedStatement statement = null;
 
         try {
 
             statement = connection.prepareStatement(statmenentString);
             statement.setString(1, member);
             ResultSet results = statement.executeQuery();
 
             while (results.next()) {
 
                 score = results.getInt("score");
 
             }
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't get player score in SQL DB. Error: " + ex.getMessage());
 
         } finally {
 
             if (statement != null) {
 
                 try {
 
                     statement.close();
 
                 } catch (SQLException ex) {
 
                     Quarantine.log.info("[Quarantine] Couldn't close SQL statement. Error: " + ex.getMessage());
 
                 }
             }
         }
 
         return score;
 
     }
 
     @Override
     public boolean isMember(String member) {
 
         boolean isMember = false;
 
         if (connection == null) {
 
             return isMember;
 
         }
 
         String statmenentString = "SELECT EXISTS(SELECT 1 FROM " + tableName + " "
                 + "WHERE player_name LIKE ?)";
 
         PreparedStatement statement = null;
 
         try {
 
             statement = connection.prepareStatement(statmenentString);
             statement.setString(1, member);
             ResultSet results = statement.executeQuery();
 
             while (results.next()) {
 
                 isMember = results.getBoolean(1);
 
             }
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't verify for player presence in SQL DB. Error: " + ex.getMessage());
 
         } finally {
 
             if (statement != null) {
 
                 try {
 
                     statement.close();
 
                 } catch (SQLException ex) {
 
                     Quarantine.log.info("[Quarantine] Couldn't close SQL statement. Error: " + ex.getMessage());
 
                 }
             }
         }
 
         return isMember;
 
     }
 
     @Override
     public int getRank(String member) {
 
         int rank = 0;
 
         if (connection == null) {
 
             return rank;
 
         }
 
         String statmenentString = "SELECT rank FROM " + tableName + " "
                 + "WHERE player_name LIKE ?";
 
         PreparedStatement statement = null;
 
         try {
 
             statement = connection.prepareStatement(statmenentString);
             statement.setString(1, member);
             ResultSet results = statement.executeQuery();
 
             while (results.next()) {
 
                 rank = results.getInt("rank");
 
             }
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't get player rank in SQL DB. Error: " + ex.getMessage());
 
         } finally {
 
             if (statement != null) {
 
                 try {
 
                     statement.close();
 
                 } catch (SQLException ex) {
 
                     Quarantine.log.info("[Quarantine] Couldn't close SQL statement. Error: " + ex.getMessage());
 
                 }
             }
         }
 
         return rank;
 
     }
 
     @Override
     public List<LeaderData> getLeaders(int pageNumber) {
 
         final List<LeaderData> leaderData = new ArrayList<LeaderData>();
 
         if (connection == null) {
 
             return leaderData;
 
         }
 
         String statmenentString = "SELECT player_name,score,rank FROM " + tableName + " "
                 + "WHERE rank<=? AND rank>=? ORDER BY rank";
 
         PreparedStatement statement = null;
 
         try {
 
             statement = connection.prepareStatement(statmenentString);
            statement.setInt(1, pageNumber * 5);
             statement.setInt(2, ((pageNumber - 1) * pageSize) + 1);
             ResultSet results = statement.executeQuery();
 
             while (results.next()) {
 
                 leaderData.add(new LeaderData(results.getString("player_name"),
                         results.getInt("score"), results.getInt("rank")));
 
             }
 
         } catch (SQLException ex) {
 
             Quarantine.log.info("[Quarantine] Couldn't get to players from SQL DB. Error: " + ex.getMessage());
 
         } finally {
 
             if (statement != null) {
 
                 try {
 
                     statement.close();
 
                 } catch (SQLException ex) {
 
                     Quarantine.log.info("[Quarantine] Couldn't close SQL statement. Error: " + ex.getMessage());
 
                 }
             }
         }
 
         return leaderData;
 
     }
 }
