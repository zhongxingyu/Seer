 package net.krinsoft.orbsuite.databases;
 
 import net.krinsoft.orbsuite.OrbCore;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 /**
  * @author krinsdeath
  */
 public class Database {
     private OrbCore plugin;
     private Connection connection;
 
     private Map<String, PreparedStatement> prepared = new HashMap<String, PreparedStatement>();
 
     private Map<String, Integer> players = new HashMap<String, Integer>();
 
     public Database(OrbCore instance) {
         plugin = instance;
         if (connect()) {
             if (initialize()) {
                 plugin.debug("Database initialized successfully.");
             }
         }
     }
 
     public void save() {
         PreparedStatement prep = prepare("REPLACE INTO orbsuite_players (name, experience) VALUES(?, ?);");
         try {
             for (String name : new HashSet<String>(players.keySet())) {
                 prep.setString(1, name);
                 prep.setInt(2, check(name));
                 prep.executeUpdate();
             }
             connection.commit();
         } catch (NullPointerException e) {
             plugin.getLogger().warning("An error occurred while handling a prepared statement: " + e.getMessage());
         } catch (SQLException e) {
             plugin.getLogger().warning("An error occured while writing to the database: " + e.getMessage());
         }
     }
 
     public void close() {
         try {
             save();
             for (PreparedStatement prep : prepared.values()) {
                 prep.close();
             }
             connection.close();
         } catch (SQLException e) {
             plugin.getLogger().warning("An error occurred while closing the database connection: " + e.getMessage());
         }
     }
 
     public boolean connect() {
         try {
             if (connection != null && !connection.isClosed()) {
                 return true;
             }
             Class.forName("org.sqlite.JDBC");
             connection = DriverManager.getConnection("jdbc:sqlite:plugins/OrbSuite/orbs.db");
             connection.setAutoCommit(false);
             return true;
         } catch (SQLException e) {
             plugin.getLogger().warning("An error occurred while connecting to the database: " + e.getMessage());
         } catch (ClassNotFoundException e) {
             plugin.getLogger().warning("The SQLite JDBC driver was not found: " + e.getMessage());
         }
         return false;
     }
 
     private boolean initialize() {
         try {
             if (connect()) {
                 String query = "CREATE TABLE IF NOT EXISTS orbsuite_players (" +
                         "id INTEGER AUTO_INCREMENT, " +
                         "name TEXT UNIQUE, " +
                         "experience INTEGER, " +
                         "PRIMARY KEY (id)" +
                         ");";
                 Statement statement = connection.createStatement();
                 statement.executeUpdate(query);
                 statement.close();
             }
             prepare("SELECT * FROM orbsuite_players WHERE name LIKE ? LIMIT 1;");
             prepare("REPLACE INTO orbsuite_players (name, experience) VALUES(?, ?);");
             return true;
         } catch (SQLException e) {
             plugin.getLogger().warning("An error occurred while initializing the tables: " + e.getMessage());
         }
         return false;
     }
 
     public PreparedStatement prepare(String query) {
         if (connect()) {
             if (prepared.containsKey(query)) {
                 return prepared.get(query);
             } else {
                 try {
                     PreparedStatement statement = connection.prepareStatement(query);
                     prepared.put(query, statement);
                     return statement;
                 } catch (SQLException e) {
                     plugin.getLogger().warning("An error occurred while preparing a statement: " + e.getMessage());
                 }
             }
         }
         return null;
     }
 
     /**
      * Adds the specified number of experience points to the player's storage.
      * @param name The name of the player to which we're adding points.
      * @param pts The amount of experience points we're adding to the player's storage.
      * @return The player's new total experience points in storage.
      */
     public int deposit(String name, int pts) {
        Integer points = check(name);
         if (points == null) {
             points = 0;
         }
         points += pts;
         players.put(name, points);
         return points;
     }
 
     /**
      * Withdraws the specified number of experience points from the player's storage
      * @param name The name of the player from which we're withdrawing points.
      * @param pts The amount of points we're withdrawing from the player's storage.
      * @return The total amount of points withdrawn.
      */
     public int withdraw(String name, int pts) {
        Integer points = check(name);
         if (points == null) {
             points = 0;
         }
         if (pts > points) {
             pts = points;
         }
         players.put(name, points - pts);
         return pts;
     }
 
     /**
      * Checks the player's current experience storage.
      * @param name The name of the player whose experience points we're fetching.
      * @return The player's current experience points in storage.
      */
     public int check(String name) {
         Integer level = players.get(name);
        if (level != null) {
             return level;
         }
         level = 0;
         PreparedStatement prep = prepare("SELECT * FROM orbsuite_players WHERE name = ? LIMIT 1;");
         try {
             prep.setString(1, name);
             ResultSet result = prep.executeQuery();
             while (result.next()) {
                 level = result.getInt("experience");
             }
         } catch (SQLException e) {
             plugin.getLogger().warning("An error occurred while fetching a player record: " + e.getMessage());
         }
         players.put(name, level);
         return level;
     }
 
 }
