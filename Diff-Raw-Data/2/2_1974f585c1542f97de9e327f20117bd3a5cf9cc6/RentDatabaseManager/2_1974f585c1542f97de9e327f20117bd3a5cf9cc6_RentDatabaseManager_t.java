 package me.krotn.Rent;
 
 import java.io.File;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 /**
  * This class allows the Rent Bukkit plugin to easily manage its interactions with databases formatted for its use.
  */
 public class RentDatabaseManager {
 	/**
 	 * The default name of the database.<br/>
 	 * A {@code String}, "rent.db".
 	 */
 	private static final String defaultDatabaseName = "rent.db";
 	private String databaseName;
 	private Connection conn = null;
 	private String maindir = "plugins/Rent";
 	Logger log = Logger.getLogger("Minecraft");
 	RentLogManager logManager = new RentLogManager(log);
 	
 	/**
 	 * Default constructor. Constructs a RentDatabaseManager with the default database name. 
 	 */
 	public RentDatabaseManager(){
 		this.databaseName = defaultDatabaseName;
 	}
 	
 	/**
 	 * Constructs a RentDatabaseManager to manage the specified database.
 	 * @param databaseName the file name of the database to manage.
 	 */
 	public RentDatabaseManager(String databaseName){
 		this.databaseName = databaseName;
 	}
 	
 	/**
 	 * Connects RentDatabaseManager to the database. <br/>
 	 * This function <i>must</i> be called before working with the database.
 	 */
 	public void connect(){
 		if(conn!=null){
 			return;
 		}
 		RentDirectoryManager.createDirectory();
 		try{
 			Class.forName("org.sqlite.JDBC");
 		}catch(ClassNotFoundException e){
 			logManager.severe("Could not load SQLite database driver!");
 		}
 		try{
 			conn = DriverManager.getConnection("jdbc:sqlite:"+RentDirectoryManager.getPathInDir(databaseName));
 		}catch(SQLException e){
 			logManager.severe("Could not establish database connection!");
 		}
 		if(!isSetup()){
 			setup();
 		}
 	}
 	
 	/**
 	 * Disconnects RentDatabaseManager from the database and closes the database connection. <br/>
 	 * This function should be called before shutting down the plugin.
 	 */
 	public void disconnect(){
 		if(conn==null){
 			return;
 		}
 		try{
 			conn.close();
 		}catch(SQLException e){
 			logManager.severe("Could not close database connection!");
 		}catch(Exception e){
 			logManager.severe("Could not close database connection!");
 		}
 	}
 	
 	/**
 	 * The function returns whether or not the RentDatabaseManager is connected to the database.
 	 * @return {@code true} if the database is connected. {@code false} if the database is not connected.
 	 */
 	public boolean isConnected(){
 		if(conn==null){
 			return false;
 		}
 		try{
 			if(conn.isClosed()){
 				return false;
 			}
 		}catch(SQLException e){
 			logManager.warning("Unable to determine if database is connected!");
 		}
 		return true;
 	}
 	
 	/**
 	 * Sets up the database. It creates a database with the correct file name and with the correct schema.<br/>
 	 * <i><b>DO NOT</b></i> call this function if a correct database already exists!
 	 */
 	public void setup(){
 		if(!isConnected()){
 			connect();
 		}
 		logManager.info("Setting up the database...");
 		try{
 			Statement statement = conn.createStatement();
 			statement.executeUpdate("CREATE TABLE Months (id INTEGER PRIMARY KEY AUTOINCREMENT,ref TEXT,cost REAL);");
 			statement.executeUpdate("CREATE TABLE Players (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,payments REAL);");
 			statement.executeUpdate("CREATE TABLE Logins (id INTEGER PRIMARY KEY AUTOINCREMENT,player_id INTEGER,month_id INTEGER,FOREIGN KEY(player_id) REFERENCES Players(id),FOREIGN KEY (month_id) REFERENCES Months(id));");
 		}catch(SQLException e){
 			logManager.severe("Unable to set-up the database!");
 		}
 	}
 	
 	/**
 	 * Returns whether or not the database is already set up.<br/>
 	 * It attempts to connect to the database and it verifies that the correct database tables exist.
 	 * @return {@code true} if the correct tables exist in the database. {@code false} otherwise or if connection fails.
 	 */
 	public boolean isSetup(){
 		if(!isConnected()){
 			connect();
 		}
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;");
 			ArrayList<String> results = new ArrayList<String>();
 			while(resultSet.next()){
 				results.add(resultSet.getString("name"));
 			}
 			resultSet.close();
 			return results.contains("Months")&&results.contains("Players")&&results.contains("Logins");
 		}catch(SQLException e){
 			logManager.severe("Could not check if the database is set-up!");
 			return false;
 		}
 	}
 	/**
 	 * Adds the specified player to the database. {@code userName} is turned into a lowercase string before storage.<br/>
 	 * Adds the player even if a player with the same name is already stored.
 	 * @param userName The name of the player to add to the database.
 	 */
 	public void addPlayer(String userName){
 		String workingUserName = userName.toLowerCase();
 		try{
 			PreparedStatement statement = conn.prepareStatement("insert into \"Players\" (name,payments) values (?,?);");
 			statement.setString(1,workingUserName);
 			statement.setDouble(2,0);
 			statement.addBatch();
 			conn.setAutoCommit(false);
 			statement.executeBatch();
 			conn.setAutoCommit(true);
 		}catch(SQLException e){
 			logManager.severe("Could not add player \""+workingUserName+"\" to the database!");
 		}
 	}
 	
 	/**
 	 * Returns the database id number of the player with the specified {@code userName}.<br/>
 	 * {@code userName} is turned into a lowercase string before querying.
 	 * @param userName The name of the user for which the id is requested.
 	 * @return The {@code int} database identification number of the player. {@code -1} if the player does not exist or if an error occurs.
 	 */
 	public int getPlayerID(String userName){
 		String workingUserName = userName.toLowerCase();
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT id FROM Players WHERE name=\""+workingUserName+"\";");
 			if(!resultSet.isBeforeFirst()){
 				resultSet.close();
 				return -1;
 			}
 			int ID = resultSet.getInt("id");
 			resultSet.close();
 			return ID;
 		}catch(SQLException e){
 			logManager.severe("Could not get player ID for: "+workingUserName+"!");
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	/**
 	 * Returns the lowercase player name of the player with the specified id. {@code null} if the player is not in the database or if an error occurs. 
 	 * @param id The integer database id number of the player.
 	 * @return The lowercase name of the player corresponding to the id in the database. {@code null} if the player does not exist or if an error occurs.
 	 */
 	public String getPlayerFromID(int id){
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT name FROM Players WHERE id="+new Integer(id).toString()+";");
 			if(!resultSet.isBeforeFirst()){
 				resultSet.close();
 				return null;
 			}
 			String name = resultSet.getString("name").toLowerCase();
 			resultSet.close();
 			return name;
 		}catch(SQLException e){
 			logManager.severe("Could not get player from ID: "+new Integer(id).toString()+".");
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Returns the amount the player has paid as stored in the database.<br/>
 	 * Does nothing if the player does not exist.
 	 * @param id The integer database id of the player.
 	 * @return The amount the player has paid. Also can return 0 if there is an error.<br/>
 	 * Do not use the 0 return value to test for an error!
 	 */
 	public double getPlayerPayments(int id){
 		if(!playerExists(getPlayerFromID(id))){
 			return 0;
 		}
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT payments FROM Players WHERE id="+id+";");
 			if(!resultSet.isBeforeFirst()){
 				resultSet.close();
 				return 0;
 			}
 			double value = resultSet.getDouble("payments");
 			resultSet.close();
 			return value;
 		}catch(SQLException e){
 			logManager.severe("Error getting player payments by ID!");
 			e.printStackTrace();
 			return 0;
 		}
 	}
 	
 	/**
 	 * Sets the value of the payments column for the given user. <br/>
 	 * Does nothing if the player does not exist.
 	 * @param id The {@code int} database id of the target player.
 	 * @param newValue The value to which the player's payments should be set.
 	 */
 	private void setPlayerPayments(int id,double newValue){
 		if(!playerExists(getPlayerFromID(id))){
 			return;
 		}
 		try{
 			PreparedStatement statement = conn.prepareStatement("UPDATE Players SET payments=? WHERE id=?;");
 			statement.setDouble(1,newValue);
 			statement.setInt(2,id);
 			statement.addBatch();
 			conn.setAutoCommit(false);
 			statement.executeBatch();
 			conn.setAutoCommit(true);
 			statement.close();
 		}catch(SQLException e){
 			logManager.severe("Error setting player payments!");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method adds {@code valueToAdd} to the player's payments.<br/>
 	 * Does nothing if the player does not exist.
 	 * @param id The database id of the player.
 	 * @param valueToAdd The amount of payments to add to the player's account.
 	 */
 	public void addPlayerPayments(int id,double valueToAdd){
 		if(!playerExists(getPlayerFromID(id))){
 			return;
 		}
 		setPlayerPayments(id,getPlayerPayments(id)+valueToAdd);
 	}
 	
 	/**
 	 * This method subtracts {@code valueToRemove} from the player's payments.<br/>
 	 * Does nothing if the player does not exist.
 	 * @param id The database id of the player.
 	 * @param valueToRemove The amount of payments to subtract from the player's account.
 	 */
 	public void subtractPlayerPayments(int id,double valueToRemove){
 		if(!playerExists(getPlayerFromID(id))){
 			return;
 		}
 		setPlayerPayments(id,getPlayerPayments(id)-valueToRemove);
 	}
 	
 	/**
 	 * Returns whether or not the player with the specified name is stored in the database.<br/>
 	 * The check is performed by checking is the player's database id number is -1.
 	 * @param userName The lowercase username of the player.
 	 * @return {@code true} player with username {@code userName} exists. {@code false} otherwise.
 	 */
 	public boolean playerExists(String userName){
 		if(userName == null){
 			return false;
 		}
 		String workingUserName = userName.toLowerCase();
 		if(getPlayerID(workingUserName) == -1){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Inserts the given month and cost into the Months database table.
 	 * @param readableMonth The human readable {@code String} of the month to add (ie. "jan11").
 	 * @param cost The monthly cost of the server rental for the given month.
 	 */
 	public void addMonth(String readableMonth,double cost){
 		String workingMonth = readableMonth.toLowerCase();
 		try{
 			PreparedStatement statement = conn.prepareStatement("insert into \"Months\" (ref,cost) values (?,?);");
 			statement.setString(1,workingMonth);
 			statement.setDouble(2,cost);
 			statement.addBatch();
 			conn.setAutoCommit(false);
 			statement.executeBatch();
 			conn.setAutoCommit(true);
 		}catch(SQLException e){
 			logManager.severe("Could not add month \""+workingMonth+"\" to the database!");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns the ID of the requested month.
 	 * @param readableMonth The human readable {@code String} of the requested month.
 	 * @return The ID of the requested month or {@code -1} if the month does not exist of if an error occured.
 	 */
 	public int getMonthID(String readableMonth){
 		String workingMonth = readableMonth.toLowerCase();
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT id FROM Months WHERE ref=\""+workingMonth+"\";");
 			if(!resultSet.isBeforeFirst()){
 				resultSet.close();
 				return -1;
 			}
 			int id = resultSet.getInt("id");
 			resultSet.close();
 			return id;
 		}catch(SQLException e){
 			logManager.severe("Could not get ID for month \""+workingMonth+"\"!");
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	/**
 	 * Returns the human-readable name of the month with specified ID.
 	 * @param id The {@code int} database id of the requested month.
 	 * @return The human-readable {@code String} identifier of the month or {@code null} if month does not exist or if an error occured.
 	 */
 	public String getMonthFromID(int id){
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT ref FROM Months WHERE id="+new Integer(id).toString()+";");
 			if(!resultSet.isBeforeFirst()){
 				logManager.warning("No months with id: "+new Integer(id).toString()+", returning null!");
 				resultSet.close();
 				return null;
 			}
 			String readableMonth = resultSet.getString("ref");
 			resultSet.close();
 			return readableMonth;
 		}catch(SQLException e){
 			logManager.severe("Could not get name for month with id: "+new Integer(id).toString()+"!");
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Sets the requested month's cost value to the requested {@code newValue}.
 	 * @param monthID The {@code int} database id of the requested month.
 	 * @param newCost The {@code double} new cost to set in the requested month's record.
 	 */
 	public void setMonthCost(int monthID,double newCost){
 		if(getMonthFromID(monthID)==null){
 			return;
 		}
 		try{
 			PreparedStatement statement = conn.prepareStatement("UPDATE Months SET cost=? WHERE id=?;");
 			statement.setDouble(1,newCost);
 			statement.setInt(2, monthID);
 			statement.addBatch();
 			conn.setAutoCommit(false);
 			statement.executeBatch();
 			conn.setAutoCommit(true);
 			statement.close();
 		}catch(SQLException e){
 			logManager.severe("Error setting new cost for month: "+new Integer(monthID).toString()+" to: "+new Double(newCost).toString()+"!");
 		}
 	}
 	
 	/**
 	 * Returns the cost of the requested month.
 	 * @param monthID The {@code int} database ID of the requested month.
 	 * @return The {@code double} monthly server rental cost or {@code -1} if the month does not exist of if an error occured.
 	 */
 	public double getMonthCost(int monthID){
 		if(getMonthFromID(monthID)==null){
 			return -1;
 		}
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT cost FROM Months WHERE id="+new Integer(monthID).toString()+";");
 			if(!resultSet.isBeforeFirst()){
 				resultSet.close();
 				return -1;
 			}
 			double monthCost = resultSet.getDouble("cost");
 			resultSet.close();
 			return monthCost;
 		}catch(SQLException e){
 			logManager.severe("Error getting month cost for month: "+new Integer(monthID).toString()+"!");
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	/**
 	 * Returns whether or not the requested month is stored in the "Months" database table.
 	 * @param readableMonth The human-readable month string.
 	 * @return {@code true} if the month exists in the "Months" database table. {@code false} otherwise.
 	 */
 	public boolean monthExists(String readableMonth){
 		if(readableMonth==null){
 			return false;
 		}
 		String workingMonth = readableMonth.toLowerCase();
 		if(getMonthID(workingMonth)==-1){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Adds a player's login to the Logins table for the specified month. Does nothing if the given {@code playerID} or {@code monthID} does not exist.
 	 * @param playerID The database ID of the requested player.
 	 * @param monthID The database ID of the requested month.
 	 */
 	public void addLogin(int playerID, int monthID){
 		try{
 			if(!playerExists(getPlayerFromID(playerID))){
 				return;
 			}
 			if(!monthExists(getMonthFromID(monthID))){
 				return;
 			}
 			PreparedStatement statement = conn.prepareStatement("insert into Logins (player_id,month_id) values (?,?);");
 			statement.setInt(1, playerID);
 			statement.setInt(2, monthID);
 			statement.addBatch();
 			conn.setAutoCommit(false);
 			statement.executeBatch();
 			conn.setAutoCommit(true);
 			statement.close();
 		}catch(SQLException e){
 			logManager.severe("Error adding login for playerID: "+new Integer(playerID).toString()+" and monthID: "+new Integer(monthID).toString()+"!");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns whether or not the given player has logged in for the given month.
 	 * @param playerID The {@ int} database ID of the requested player.
 	 * @param monthID The {@ int} database ID of the requested month.
	 * @return {@code true} if the player has logged in during the month or {@code false} if the player has not logged in or if an error occured.
 	 */
 	public boolean hasPlayerLoggedIn(int playerID,int monthID){
 		if(!playerExists(getPlayerFromID(playerID))){
 			return false;
 		}
 		if(!monthExists(getMonthFromID(monthID))){
 			return false;
 		}
 		try{
 			Statement statement = conn.createStatement();
 			ResultSet resultSet = statement.executeQuery("SELECT id FROM Logins WHERE player_id="+new Integer(playerID).toString()+" AND month_id="+new Integer(monthID).toString()+";");
 			if(!resultSet.isBeforeFirst()){
 				resultSet.close();
 				return false;
 			}
 			return true;
 		}catch(SQLException e){
 			logManager.severe("Error checking if player: "+new Integer(playerID).toString()+" has logged in for month: "+new Integer(monthID).toString()+"!");
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
