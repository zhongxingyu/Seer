 package arcade.database;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Creates and updates game table
  * this clearly needs to be refactored because there is duplicate code.
  * @author Natalia Carvalho
  */
 public class GameTable extends Table {
 
     private static final String TABLE_SEPARATOR = ": ";
     private static final String GAMENAME_COLUMN_FIELD = "gamename";  
     private static final String GAMEFILEPATH_COLUMN_FIELD = "gamefilepath";
     private static final String GENRE_COLUMN_FIELD = "genre";
     private static final String GAMEID_COLUMN_FIELD = "gameid";  
     
     private static final int GAMENAME_COLUMN_INDEX = 1;
     private static final int GAMEFILEPATH_COLUMN_INDEX = 2;
     private static final int GENRE_COLUMN_INDEX = 3;
     private static final int GAMEID_COLUMN_INDEX = 4;
     
     private static final String TABLE_NAME = "games";  
 
 
     private Connection myConnection;
     private PreparedStatement myPreparedStatement; 
     private ResultSet myResultSet;
     
     /**
      * GameTable constructor
      */
     public GameTable() {
         establishConnectionToDatabase();
     }
 
     void establishConnectionToDatabase() {
 
         try {
             Class.forName("org.postgresql.Driver");
         }
         catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         String url = "jdbc:postgresql://cgi.cs.duke.edu/nrc10";
         String user = "nrc10";
         String password = "aUsg5xj2f";
         
 
         try {
             myConnection = DriverManager.getConnection(url, user, password);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         myPreparedStatement = null; 
         myResultSet = null;
 
     }
 
     /**
      * Closes Connection, ResultSet, and PreparedStatements once done with database
      */
     public void closeConnection() {
         try {
             if (myPreparedStatement != null) {
                 myPreparedStatement.close();
             }
             if (myResultSet != null) {
                 myResultSet.close();
             }
             if (myConnection != null) {
                 myConnection.close();
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Returns true if gameName already exists, false otherwise
      * @param gameName is the name of game
      */
 
     public boolean gameNameExists(String gameName) {
         String stm = "SELECT gamename FROM games WHERE gamename='" + gameName + "'";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet  = myPreparedStatement.executeQuery();
             if (myResultSet.next()) {
                 return true;
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return false;
     }
     
     /**
      * Given a gameName, retrieves a gameID
      * @param gameName is the game's name
      */
     public String retrieveGameId(String gameName) {
         String stm = "SELECT * FROM " + TABLE_NAME + " WHERE " + GAMENAME_COLUMN_FIELD + "='" + gameName + "'";
         String gameid = "";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet  = myPreparedStatement.executeQuery();
             if (myResultSet.next()) {
                 gameid = myResultSet.getString(GAMEID_COLUMN_INDEX);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return gameid;
     }
     
     /**
      * Given the gameName, adds a game
      * @param gameName is the name of game
      * Adds a user to user table based on information
      * @param user is the username
      * @param pw is the password
      * @param firstname is firstname
      * @param lastname is lastname
      * @param dateOfBirth is date of birth
      */
     public boolean createGame(String gameName , String genre) {
         if (gameNameExists(gameName)) {
             return false;
         }
        String stm = "INSERT INTO " + TABLE_NAME + "(gamename, gamefilepath, genre) VALUES(?,?,?)";
        //String stm = "INSERT INTO " + TABLE_NAME + "(" + GAMENAME_COLUMN_FIELD + ") VALUES(?)";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myPreparedStatement.setString(GAMENAME_COLUMN_INDEX, gameName);
            myPreparedStatement.setString(GAMEFILEPATH_COLUMN_INDEX, "null");
             myPreparedStatement.setString(GENRE_COLUMN_INDEX, genre);
           // myPreparedStatement.setString(GAMEID_COLUMN_INDEX, "null");
             myPreparedStatement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return true;
     }
     
     /**
      * Returns a list of all the games
      */    
     public List<String> retrieveGameList() {
         String stm = "SELECT " + GAMENAME_COLUMN_FIELD + " FROM "  + TABLE_NAME;
         List<String> myGameNames = new ArrayList<String>();
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet = myPreparedStatement.executeQuery();
             while (myResultSet.next()) {
                 myGameNames.add(myResultSet.getString(GAMENAME_COLUMN_INDEX));
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return myGameNames;
         
     }
      
     /**
      * Given a game, deletes that game from gameTable
      * @param gameName is gameName
      */
     public void deleteGame(String gameName) {
         String stm = "DELETE FROM " + TABLE_NAME + " WHERE " + GAMENAME_COLUMN_FIELD + "='" + gameName + "'";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myPreparedStatement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
      
     
     void printEntireTable () {
         System.out.println();
         try {
             myPreparedStatement = myConnection.prepareStatement("SELECT * FROM " + TABLE_NAME);
             myResultSet = myPreparedStatement.executeQuery();
             while (myResultSet.next()) {
                 System.out.print(myResultSet.getString(GAMENAME_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(GAMEFILEPATH_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(GENRE_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.println(myResultSet.getString(GAMEID_COLUMN_INDEX));
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Given a username, retrieves avatar filepath
      * @param username is the username
      */
     public String getGenre(String gameName) {
         return retrieveEntry(gameName, GENRE_COLUMN_INDEX);
     }
     
     /**
      * Given a gamename and a column_index, returns that entire row entry
      * @param gameName is the gamename
      * @param columnIndex is the index that we want the information for
      */
     public String retrieveEntry(String gameName, int COLUMN_INDEX) {
         String stm = "SELECT * FROM " +TABLE_NAME + " WHERE " + GAMENAME_COLUMN_FIELD + "='" + gameName + "'";
         String entry = "";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet = myPreparedStatement.executeQuery();
             if (myResultSet.next()) {
                 entry = myResultSet.getString(COLUMN_INDEX);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return entry;
     }
 }
