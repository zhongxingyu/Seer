 package quizweb.message;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import quizweb.User;
 import quizweb.XMLElement;
 import quizweb.database.DBConnection;
 
 public class FriendRequestMessage extends Message {
 	
 	private static final String DBTable = "friend_request";
 	private boolean isConfirmed;
 	private boolean isRejected;
 
 	public FriendRequestMessage(int uid1, int uid2) {
 		super(uid1, uid2, new String());
 		isConfirmed = false;
 		isRejected = false;
 	}
 	
 	public FriendRequestMessage(int id, int uid1, int uid2, Timestamp time, boolean isconfirmed, boolean isrejected) {
 		super(id, uid1, uid2, new String(), time);
 		isConfirmed = isconfirmed;
 		isRejected = isrejected;
 	}
 	
 	public boolean isPending() {
 		if(isConfirmed == false && isRejected == false)
 			return true;
 		else 
 			return false;
 	}
 	//static public void removeFriendRequest(Message)
 
 	@Override
 	public void addMessageToDB() {
 		try {
 			String statement = new String("INSERT INTO " + DBTable +
 					" (uid1, uid2, time, isConfirmed, isRejected) VALUES (?, ?, NOW(), ?, ?)");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, fromUser);
 			stmt.setInt(2, toUser);
 			stmt.setBoolean(3, isConfirmed);
 			stmt.setBoolean(4, isRejected);
 			DBConnection.DBUpdate(stmt);
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public static void rejectFriendRequest(int senderID, int receiverID) {
 		try {
 			String statement = new String("UPDATE " + DBTable + " SET isRejected = true " +
 					"WHERE uid1 = ? and uid2 = ? and isConfirmed = false and isRejected = false");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, senderID);
 			stmt.setInt(2, receiverID);
 			DBConnection.DBUpdate(stmt);
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public static void confirmFriendRequest(int senderID, int receiverID) {
 		try {
 			String statement = new String("UPDATE " + DBTable + " SET isConfirmed = true " +
 					"WHERE uid1 = ? and uid2 = ? and isConfirmed = false and isRejected = false");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, senderID);
 			stmt.setInt(2, receiverID);
 			DBConnection.DBUpdate(stmt);
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public static ArrayList<FriendRequestMessage> getMessagesByUserID(int userID) {
 		ArrayList<FriendRequestMessage> FriendRequestMessageQueue = new ArrayList<FriendRequestMessage>();
 		try {
 			String statement = new String("SELECT * FROM " + DBTable +" WHERE uid2 = ?");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, userID);
 			ResultSet rs = DBConnection.DBQuery(stmt);
 			rs.beforeFirst();
 			while(rs.next()) {
 				FriendRequestMessage fm = new FriendRequestMessage(
 						rs.getInt("mid"), rs.getInt("uid1"), rs.getInt("uid2"), 
 						rs.getTimestamp("time"), rs.getBoolean("isConfirmed"), rs.getBoolean("isRejected"));
 				if (!fm.isConfirmed && !fm.isRejected)
 					FriendRequestMessageQueue.add(fm); 
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 		return FriendRequestMessageQueue;
 	}
 	
 	public static int getUnreadCount(User user) {
 		int unreadCount = 0;
 		try {
 			String statement = new String("SELECT COUNT(mid) FROM " + DBTable + " WHERE uid2 = ? and isConfirmed = false and isRejected = false");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, user.userID);
 			ResultSet rs = stmt.executeQuery();
 			if (rs.next())
 				unreadCount = rs.getInt("COUNT(mid)");
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}		
 		return unreadCount;		
 	}
 
 
 	public static Message getFriendRequestByXMLElem(XMLElement root) {
 		User toUser = null;
 		User fromUser = null;
 		boolean isConfirmed = false;
 		boolean isRejected = false;
 		if (root.attributeMap.containsKey("isconfirmed") && root.attributeMap.get("isconfirmed").equals("true")) 
 			isConfirmed = true;
 		if (root.attributeMap.containsKey("isrejected") && root.attributeMap.get("isrejected").equals("true")) 
 			isRejected = true;
 		
 		for (int i = 0; i < root.childList.size(); i++) {
 			XMLElement elem = root.childList.get(i);
 			if (elem.name.equals("to")) {
 				toUser = User.getUserByUsername(elem.content);
 			} else if (elem.name.equals("from")) {
 				fromUser = User.getUserByUsername(elem.content);
 			} else {
 				System.out.println("Unrecognized field in challenge message " + elem.name);
 			}
 		}
 		if (fromUser == null) {
 			System.out.println("Unrecognized from user");
 			return null;
 		} else if (toUser == null) {
 			System.out.println("Unrecognized to user");
 			return null;
 		}
 		FriendRequestMessage message = new FriendRequestMessage(fromUser.userID, toUser.userID);
 		message.isConfirmed = isConfirmed;
 		message.isRejected = isRejected;
 		return message;
 	}
 }
