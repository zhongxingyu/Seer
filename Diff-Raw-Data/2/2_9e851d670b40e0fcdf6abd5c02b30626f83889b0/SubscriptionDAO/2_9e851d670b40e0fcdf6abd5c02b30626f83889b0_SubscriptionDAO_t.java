 package edu.cmu.cs214.hw9.db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import edu.cmu.cs214.hw9.resources.Constants;
 
 public class SubscriptionDAO extends SQLiteAdapter{
 	
 	
 	public SubscriptionDAO(String url) throws Exception{
 		super(url);
 	}
 	
 	/**
 	 * Create a new Subscription.
 	 * @param toEmail email address of User subscribed to
 	 * @param fromEmail email address of User subscribing
 	 * @return true if Subscription was created
 	 */
 	public boolean createSubscription(String toEmail, String fromEmail)
 	{
 		// TODO add lookup here to see if link already exists
 		Subscription s = new Subscription(toEmail, fromEmail);
 		PreparedStatement ps;
 		String statement = "INSERT INTO " + Constants.SUBSCRIPTION_TABLE + " (toEmail, fromEmail) VALUES (?, ?)";
 		try{
 			ps = conn.prepareStatement(statement);
 			ps.setString(1, s.getToEmail());
 			ps.setString(2, s.getFromEmail());
 			ps.executeUpdate();
 		} catch(SQLException e){
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Get all of the Users that the User with email of 
 	 * email argument subscribes to
 	 * @param email get all Users followed by this User's email
 	 * @return ArrayList of Users
 	 */
 	public ArrayList<User> subscriptionsOf(String email)
 	{
 		ArrayList<User> ret = new ArrayList<User>();
 		ResultSet rs = null;
 		PreparedStatement ps = null;
 
 		try {
 			UserDAO userDAO = new UserDAO(url); // allow us to get access to User table
 
 			String statement = "SELECT * FROM " + Constants.SUBSCRIPTION_TABLE + " WHERE toEmail=?;";
 			ps = conn.prepareStatement(statement);
 			ps.setString(1, email);
 			
 			rs = ps.executeQuery();
 			while(rs.next()){
 				User u = userDAO.findUser(rs.getString("fromEmail")).get(0);
 				ret.add(u);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
             try{
             	if(rs != null){
             		rs.close();
             	}
             } catch (SQLException e){
             }
         }
 		
 		return ret;
 	}
 	
 	/**
 	 * Remove an existing Subscription.
 	 * @param toEmail email address of User subscribed to
 	 * @param fromEmail email address of User subscribing
 	 * @return true if Subscription was created
 	 */
	public boolean removeSubscription(String toEmail, String fromEmail)
 	{
 		Subscription s = new Subscription(toEmail, fromEmail);
 		PreparedStatement ps;
 		String statement = "DELETE FROM " + Constants.SUBSCRIPTION_TABLE + " WHERE toEmail=? AND fromEmail=?;";
 		try{
 			ps = conn.prepareStatement(statement);
 			ps.setString(1, s.getToEmail());
 			ps.setString(2, s.getFromEmail());
 			ps.executeUpdate();
 		} catch(SQLException e){
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 
 	}
 
 
 }
