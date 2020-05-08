 package edu.cmu.cs214.hw9.db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class SubscriptionsDAO extends SQLiteAdapter {
 	public SubscriptionsDAO() throws Exception{
 		super();
 	}
 
 	/**
 	 * Purpose: determine if email1 is subscribed to email2
 	 * @param email1 Friend source
 	 * @param email2 Friend destination
 	 * @return Whether or not email1 is friends with email2
 	 */
 	public boolean isSubscribed(String email1, String email2) {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		boolean ret;
 		String statement = "SELECT * FROM " + Constants.SUBSCRIPTIONS_TABLE + " WHERE email1=? AND email2=?;";
 		try{
 			ps = conn.prepareStatement(statement);
 			ps.setString(1, email1);
 			ps.setString(2, email2);
 			rs = ps.executeQuery();
 		} catch(SQLException e){
 			e.printStackTrace();
 		}
 		finally {
             try{
             	if(rs != null){
             		ret = rs.isBeforeFirst();
             		rs.close();
             		return ret;
             	}
             } catch (SQLException e){
             	e.printStackTrace();
             }
         }
 		
 		return false;
 	}
 	
 	/**
 	 * Modify subscription status of emailSubscriber to emailSubscribed
 	 * @param emailSubscriber user who's either subscribing or unsubscribing
 	 * @param emailSubscribed user who's being subscribed or unsubscribed to
 	 * @return
 	 */
 	public boolean modifySubscription(String emailSubscriber, String emailSubscribed) {
 		
 		//if emailSubscriber is already subscribed to emailSubscribed,
 		//	then remove the subscription
 		if(isSubscribed(emailSubscriber, emailSubscribed)) {
 			PreparedStatement ps;
			String statement = "DELETE FROM " + Constants.SUBSCRIPTIONS_TABLE + " WHERE email1=? AND email2=?;";
 			try{
 				ps = conn.prepareStatement(statement);
 				ps.setString(1, emailSubscriber);
 				ps.setString(2, emailSubscribed);
 				ps.executeUpdate();
 			} catch(SQLException e){
 				e.printStackTrace();
 				return false;
 			}
 		}
 		
 		//otherwise, add the subscription
 		else {
 			PreparedStatement ps;
 			String statement = "INSERT INTO " + Constants.SUBSCRIPTIONS_TABLE + " (email1, email2) VALUES (?, ?);";
 			try{
 				ps = conn.prepareStatement(statement);
 				ps.setString(1, emailSubscriber);
 				ps.setString(2, emailSubscribed);
 				ps.executeUpdate();
 			} catch(SQLException e){
 				e.printStackTrace();
 				return false;
 			}
 		}
 			
 		return true;
 	}
 	
 	/**
 	 * Return list of subscriptions of the given email
 	 * @param email user id for user that list of subscriptions relates to
 	 * @return list of subscriptions in CSV format
 	 */
 	public String listSubscriptions(String email){
 		
 		ResultSet rs = null;
 		String subscriptionList = "";
 		try {
 			String statement = "SELECT * FROM " + Constants.SUBSCRIPTIONS_TABLE + " WHERE email1=?;";
 			PreparedStatement ps = conn.prepareStatement(statement);
 			ps.setString(1, email);
 			rs = ps.executeQuery();
 			while(rs.next()){
 				subscriptionList += rs.getString("email2") + ",";
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
             try{
             	if(rs != null){
             		rs.close();
             	}
             } catch (SQLException e){
             	e.printStackTrace();
             }
         }
 		
 		return subscriptionList;
 	}
 }
