 package database;
 
 public class FriendBank {
 
 	private DBConnection connection;
 	
 	/*
 	 * Initializes the database Connection for later use.
 	 */
 	public FriendBank() {
 		connection = DBConnection.getDBConnection();
 	}
 	
 	public void AddFriends(int user1, int user2) {
 		connection.ExecuteUpdate("INSERT INTO friends VALUES (" + user1 + ", " + user2 +", current_date)");
 	}
 	
 	public void RemoveFriends(int user1, int user2) {
		connection.ExecuteUpdate("DELETE FROM friends WHERE (user1=" + user1 + " AND user2=" + user2 + ") OR (user1=" + user2 + " AND user2=" + user1 + ")");
 	}
 }
