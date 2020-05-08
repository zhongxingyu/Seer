 package edu.cmu.cs214.hw9.db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class FriendsDAO extends SQLiteAdapter {
 	public FriendsDAO() throws Exception{
 		super();
 	}
 
 	/**
 	 * Purpose: determine if email1 is friends with email2
 	 * @param email1 Friend source
 	 * @param email2 Friend destination
 	 * @return Whether or not email1 is friends with email2
 	 */
 	public boolean isFriend(String email1, String email2) {
 		PreparedStatement ps;
 		ResultSet rs = null;
 		boolean ret;
 		String statement = "SELECT * FROM " + Constants.FRIENDS_TABLE + " WHERE email1=? AND email2=?;";
 		try{
 			ps = conn.prepareStatement(statement);
 			ps.setString(1, email1);
 			ps.setString(2, email2);
 			rs = ps.executeQuery();
 			return rs.isBeforeFirst();
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
 	 * emailModifying is modifying friendship with emailModified.
 	 * This applies to the Add/Remove button.
 	 * The changes affect the friends table, where bidirectional entries
 	 * are required to consider two users to be friends.
 	 * The cases are broken down as following, referring to parameter one
 	 * as user A, and parameter two as user B:
 	 * 1) User A has an existing request for User B,
 	 * but B has not yet accepted.
 	 * Then, modifyFriend(A,B) will remove that request,
 	 * and they will not be friends.
 	 * 2) User B has an existing request for User A,
 	 * but A has not yet accepted.
 	 * Then, modifyFriend(A,B) will accept that request,
 	 * and they will be friends.
 	 * 3) User A and User B have no connection at all.
 	 * Then, modifyFriend(A,B) will send a friend request to User B from User A
 	 * 4) User A and User B are friends.
 	 * Then, modifyFriend(A,B) will remove the friendship.
 	 * @param emailAdding user modifying the friend request
 	 * @param emailAdded user who is being modified with
 	 * @return whether or not the query was successful
 	 */
 	public boolean modifyFriend(String emailModifying, String emailModified) {
 		
 		PreparedStatement ps;
 		String statement;
 		
 		//if emailModifying is already friends (or is pending to) with
 		// emailModified, remove that relationship
 		if(isFriend(emailModifying, emailModified)) {
			statement = "DELETE FROM " + Constants.FRIENDS_TABLE + " WHERE email1=? AND email2=?;";
 		}
 		
 		//otherwise, add that relationship
 		else {
 			statement = "INSERT INTO " + Constants.FRIENDS_TABLE + " (email1, email2) VALUES (?, ?);";
 		}
 		
 		try{
 			ps = conn.prepareStatement(statement);
 			ps.setString(1, emailModifying);
 			ps.setString(2, emailModified);
 			ps.executeUpdate();
 		} catch(SQLException e){
 			e.printStackTrace();
 			return false;
 		}
 		
 		//only modify any of emailModified's relationships if there
 		// is mutual friendship. In that case, severe the tie from emailModified
 		// to emailModifying (this is the remove friend case when both are already friends)
 		if(isFriend(emailModifying, emailModified) &&
 				isFriend(emailModified, emailModifying)) {
			statement = "DELETE FROM " + Constants.FRIENDS_TABLE + " WHERE email1=? AND email2=?;";
 			
 			try{
 				ps = conn.prepareStatement(statement);
 				ps.setString(1, emailModified);
 				ps.setString(2, emailModifying);
 				ps.executeUpdate();
 			} catch(SQLException e){
 				e.printStackTrace();
 				return false;
 			}
 		}
 		
 		return true;
 
 	}
 	
 	/**
 	 * Return list of friends of the given email
 	 * @param email user id for user that list of friends relates to
 	 * @return list of friends in CSV format, with five periods separating
 	 * 			actual and pending friends
 	 */
 	public String listFriends(String email){
 		
 		ResultSet rs = null;
 		String friendList = "";
 		String pendingFriendList = "";
 		try {
 			String statement = "SELECT * FROM " + Constants.FRIENDS_TABLE + " WHERE email2=?;";
 			PreparedStatement ps = conn.prepareStatement(statement);
 			ps.setString(1, email);
 			rs = ps.executeQuery();
 			while(rs.next()){
 				String friend = rs.getString("email1");
 				
 				// actual friend
 				if(isFriend(friend, email)) {
 					friendList += friend + ",";
 				}
 				
 				// pending friend
 				else {
 					pendingFriendList += friend + ",";
 				}
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
 		
 		return friendList + "....." + pendingFriendList;
 	}
 }
