 //BEGIN FILE DatabaseBuddy.java
 package controller;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * A database adapter for the TTTServlet
  * @author Mustache Cash Stash
  * @version 1.0
  *
  */
 //BEGIN CLASS DatabaseBuddy
 public class DatabaseBuddy {
 	
 	private Connection conn;
 	private Random rng;
 	
 	/**
 	 * Constructs an adapter to to the specified database
 	 * @param databaseURI The location of the database to use for this Servlet
 	 */
 	//BEGIN CONSTRUCTOR DatabaseBuddy(String databaseURI)
 	public DatabaseBuddy(String databaseURI)
 	{
 		try{
 			conn = DriverManager.getConnection(databaseURI);
 			//conn.createStatement().execute("CREATE TABLE users(userName VARCHAR(25) NOT NULL,characterCode INT NOT NULL,lastKeptAlive TIMESTAMP)");
 			//conn.createStatement().execute("CREATE TABLE requests(requesteeName VARCHAR(25) NOT NULL,requesteeCode INT NOT NULL,requesterName VARCHAR(25) NOT NULL,requesterCode INT NOT NULL)");
 			//conn.createStatement().execute("CREATE TABLE activeGames(gameNum INT NOT NULL,XName VARCHAR(25) NOT NULL,XCode INT NOT NULL,OName VARCHAR(25) NOT NULL,OCode INT NOT NULL)");
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		rng = new Random();
 	}
 	//END CONSTRUCTOR DatabaseBuddy(String databaseURI)
 	
 	/**
 	 * Empties the entire database
 	 */
 	//BEGIN METHOD public void deleteAll()
 	public void deleteAll()
 	{
 		this.deleteAllUsers();
 		this.deleteAllRequests();
 	}
 	//END METHOD public void deleteAll()
 	
 	/**
 	 * Removes all users from the database.
 	 */
 	//BEGIN METHOD public void deleteAllUsers()
 	public void deleteAllUsers()
 	{
 		try{
 			conn.createStatement().execute("DELETE FROM users");
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	//END METHOD public void deleteAllUsers()
 	
 	/**
 	 * Deletes all game requests from the database.
 	 */
 	//BEGIN METHOD public void deleteAllRequests()
 	public void deleteAllRequests()
 	{
 		try{
 			conn.createStatement().execute("DELETE FROM requests");
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	//END METHOD public void deleteAllRequests()
 	
 	/**
 	 * Adds a User to the database, returning whether this was successfull.
 	 * @param u The User to add
 	 * @return Whether adding the user was successful
 	 */
 	//BEGIN METHOD boolean addUser(User u)
 	public boolean addUser(User u)
 	{
 		if(!this.containsUser(u))
 		{
 			String query = "INSERT INTO users(userName, characterCode) VALUES('"+u.getUserName()+"', "+u.getCharacterCode()+")";
 			try{
 				Statement stmt = conn.createStatement();
 				stmt.execute(query);
 			}catch(SQLException sqle)
 			{
 				sqle.printStackTrace();
 			}
 			return true;
 		}
 		return false;
 	}
 	//END METHOD addUser(User U)
 	
 	/**
 	 * Ensures a user continues to be listed as online and available to play
 	 * @param u The User to keep alive
 	 * @return Whether the request was successful
 	 */
 	//BEGIN METHOD boolean keepAlive(User u)
 	public boolean keepAlive(User u)
 	{
 		try
 		{
 			conn.createStatement().execute("UPDATE users SET lastKeptAlive=CURRENT_TIMESTAMP WHERE userName='"+u.getUserName()+"' AND characterCode="+u.getCharacterCode());
 			return true;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 	//BEGIN METHOD keepAlive(User u)
 	
 	/**
 	 * Constructs a list of currently online players
 	 * The Servlet can pass this data back to a client to display a Lobby
 	 * @param timeToKeepAlive How recently a user has to have sent a request in order to be considered online
 	 * @return A List of currently online Users
 	 */
 	//BEGIN METHOD List<User> getLiveUsers(long timeToKeepAlive)
 	public List<User> getLiveUsers(long timeToKeepAlive)
 	{
 		Timestamp ts;
 		List<User> returnList = new ArrayList<User>();
 		try
 		{
 			ResultSet rs = conn.createStatement().executeQuery("VALUES CURRENT_TIMESTAMP");
 			rs.next();
 			ts = new Timestamp(rs.getTimestamp(1).getTime()-timeToKeepAlive);
 			String tsString = ts.toString();
 			rs = conn.createStatement().executeQuery("SELECT userName,characterCode FROM users WHERE lastKeptAlive>={ts '"+tsString+"'}");
 			while(rs.next())
 			{
 				returnList.add(new User(rs.getString("userName"), rs.getInt("characterCode")));
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return returnList;
 	}
 	//END METHOD List<User> getLiveUsers(long timeToKeepAlive)
 	
 	/**
 	 * Requests a game from requester to requestee. If there is a pending reciprocal request, a game is created and the game number is returned.
 	 * If the request is successful, 0 is returned.
 	 * If every thing goes pear-shaped, you get a -1.
 	 * @param requestee
 	 * @param requester
	 * @return
 	 */
 	//TODO needs guards
 	//BEGIN int requestGame(User requester, User requestee)
 	public int requestGame(User requester, User requestee)
 	{
 		String queryRecipRequest = "SELECT requesteeName, requesteeCode, requesterName, requesterCode FROM requests WHERE"
 				+" requesteeName='"+requester.getUserName()+"' AND requesteeCode="+requester.getCharacterCode()+" AND"
 				+" requesterName='"+requestee.getUserName()+"' AND requesterCode="+requestee.getCharacterCode();
 		try{
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(queryRecipRequest);
 			if(rs.next())
 			{
 				int i;
 				do{
 					i= rng.nextInt(8191)+1;
 				}while(conn.createStatement().executeQuery("SELECT gameNum FROM activeGames WHERE gameNum="+i).next());
 				if(rng.nextBoolean())
 				{
 					String newGameQuery = "INSERT INTO activeGames(XName, XCode, OName, OCode, gameNum) VALUES('"
 							+requestee.getUserName()+"', "+requestee.getCharacterCode()+", '"
 							+requester.getUserName()+"', "+requester.getCharacterCode()+", "+i+")";
 					conn.createStatement().execute(newGameQuery);
 					return i;
 				}
 			}
 			else
 			{
 				String query = "INSERT INTO requests(requesteeName, requesteeCode, requesterName, requesterCode) VALUES('"
 						+requestee.getUserName()+"', "+requestee.getCharacterCode()+", '"
 						+requester.getUserName()+"', "+requester.getCharacterCode()+")";
 				conn.createStatement().execute(query);
 				return 0;
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	//END METHOD int requestGame(User requester, User requestee)
 	
 	/**
 	 * Returns whether the database has a record of this User
 	 * @param newUser The User in question
 	 * @return Whether the database contains this user.
 	 */
 	//BEGIN METHOD boolean containsUser(User NewUser)
 	public boolean containsUser(User newUser) {
 		ResultSet rs;
 		String query = "SELECT userName,characterCode FROM Users WHERE userName='"+newUser.getUserName()+"' AND characterCode="+newUser.getCharacterCode();
 		try {
 			rs = conn.createStatement().executeQuery(query);
 			return rs.next();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	//END METHOD boolean containsUser(User NewUser)
 }
 //END CLASS DatabaseBuddy
 //END FILE DatabaseBuddy.java
