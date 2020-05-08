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
 
 public class DatabaseBuddy {
 	
 	Connection conn;
 	Random rng;
 	
 	public DatabaseBuddy(String databaseURI)
 	{
 		try{
 			conn = DriverManager.getConnection(databaseURI);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		rng = new Random();
 	}
 	
 	public boolean addUser(User u)
 	{
 		String query = "INSERT INTO users(characterName, characterCode, isOnline) VALUES('"+u.getUserName()+"', "+u.getCharacterCode()+", FALSE)";
 		try{
 			Statement stmt = conn.createStatement();
			stmt.executeQuery(query);
 		}catch(SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		return true;
 	}
 	
 	public boolean keepAlive(User u)
 	{
 		try
 		{
 			conn.createStatement().executeQuery("UPDATE users SET lastKeptAlive=CURRENT_TIMESTAMP WHERE userName='"+u.getUserName()+"' characterCode="+u.getCharacterCode());
 			return true;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public List<User> getLiveUsers(long timeToKeepAlive)
 	{
 		Timestamp ts;
 		List<User> returnList = new ArrayList<User>();
 		try
 		{
 			ResultSet rs = conn.createStatement().executeQuery("VALUES CURRENT_TIMESTAMP");
 			rs.next();
 			ts = new Timestamp(rs.getTimestamp(1).getTime()-timeToKeepAlive);
			rs = conn.createStatement().executeQuery("SELECT userName,characterCode WHERE lastKeptAlive>="+ts.toString());
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
 	
 	/**
 	 * Requests a game from requester to requestee. If there is a pending reciprocal request, a game is created and the game number is returned.
 	 * If the request is successful, 0 is returned.
 	 * If every thing goes pear-shaped, you get a -1.
 	 * @param requestee
 	 * @param requester
 	 * @return
 	 */
 	//TODO needs guards
 	public int requestGame(User requester, User requestee)
 	{
 		String queryRecipRequest = "SELECT requesteeName, requesteeCode, requesterName, requesterCode FROM requests WHERE"
 				+" requesteeName='"+requester.getUserName()+" AND requesteeCode="+requester.getCharacterCode()+" AND"
 				+" requesterName='"+requestee.getUserName()+" AND requesterCode="+requestee.getCharacterCode();
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
 					String newGameQuery = "INSERT INTO activeGames(XName, XCode, OName, OCode) VALUES('"
 							+requestee.getUserName()+"', "+requestee.getCharacterCode()+", '"
 							+requester.getUserName()+"', "+requester.getCharacterCode()+")";
 					conn.createStatement().executeQuery(newGameQuery);
 					return i;
 				}
 			}
 			else
 			{
 				String query = "INSERT INTO requests(requesteeName, requesteeCode, requesterName, requesterCode) VALUES('"
 						+requestee.getUserName()+"', "+requestee.getCharacterCode()+", '"
 						+requester.getUserName()+"', "+requester.getCharacterCode()+")";
 				conn.createStatement().executeQuery(query);
 				return 0;
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public boolean containsUser(User newUser) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
