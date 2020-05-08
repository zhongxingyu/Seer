 package site;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 public class Inbox {
 	
 	private ArrayList<Message> messages;
 	
 	public Inbox(ArrayList<Message> messages) {
 		this.setMessages(messages);
 	}
 
 	public void addMessage(Message message) {
 		this.messages.add(message);
 	}
 	
 	public ArrayList<Message> getMessages() {
 		return messages;
 	}
 
 	public void setMessages(ArrayList<Message> messages) {
 		this.messages = messages;
 	}
 	
 	/**
 	 * 
 	 * @param user_id
 	 * @param interval the number of days prior to receive messages until (typically 3)
 	 * @return
 	 */
 	public static ArrayList<TextMessage> getRecentMessagesById(int user_id, int interval) {
 		ArrayList<TextMessage> messages = new ArrayList<TextMessage>();
 		
 		int messageId = 0;
 		String date = "";
 		String fromUserId = "";
 		String content = "";
 		int messageType = 0;
 		
 		AccountManager manager = new AccountManager();
 		Connection con = MyDB.getConnection();
 		try {
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM messages WHERE to_id="+user_id+" AND created_timestamp >= ( CURDATE() - INTERVAL "+interval+" DAY ) ORDER BY created_timestamp DESC";
 			ResultSet rs = stmt.executeQuery(query);
 			while(rs.next()) {
 				
 				messageId = rs.getInt("message_id");
 				String dateString = (new SimpleDateFormat ("MM/dd/yy hh:mm aa")).format(rs.getTimestamp("created_timestamp"));
 				date = dateString;
 				fromUserId = rs.getString("from_id");
 				content = rs.getString("content");
 				messageType = rs.getInt("message_type");
 				TextMessage message = new TextMessage(messageId, manager.getAccountById(fromUserId), manager.getAccountById(String.valueOf(user_id)), messageType, content, date);
 				messages.add(message);
 			}
 		}
 		catch(Exception e) { /*System.out.println(e);*/ }
 		
 		return messages;
 	}
 	
 	public static ArrayList<TextMessage> getMessagesById(int user_id) {
 		ArrayList<TextMessage> messages = new ArrayList<TextMessage>();
 		
 		int messageId = 0;
 		String date = "";
 		String fromUserId = "";
 		String content = "";
 		int messageType = 0;
 		
 		AccountManager manager = new AccountManager();
 		Connection con = MyDB.getConnection();
 		try {
 			Statement stmt = con.createStatement();
 			String query = "SELECT * FROM messages WHERE to_id="+user_id+" ORDER BY created_timestamp DESC";
 			ResultSet rs = stmt.executeQuery(query);
 			while(rs.next()) {
 				messageId = rs.getInt("message_id");
 				String dateString = (new SimpleDateFormat ("MM/dd/yy hh:mm aa")).format(rs.getTimestamp("created_timestamp"));
 				date = dateString;
 				fromUserId = rs.getString("from_id");
 				content = rs.getString("content");
 				messageType = rs.getInt("message_type");
 				TextMessage message = new TextMessage(messageId, manager.getAccountById(fromUserId), manager.getAccountById(String.valueOf(user_id)), messageType, content, date);
 				messages.add(message);
 			}
 		}
 		catch(Exception e) { /*System.out.println(e);*/ }
 		
 		return messages;
 	}
 	
 	public static boolean sendTextMessage(TextMessage message) {
 		Connection con = MyDB.getConnection();
 		
 		if(message.getFromUser() == null || message.getToUser() == null)
 			return false;
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			String query = "INSERT INTO messages (to_id, from_id, content, message_type)" +
 						   " VALUES("+message.getToUser().getId()+", "+message.getFromUser().getId()+", '"+message.getNote()+"', "+message.getMessageType()+")";
 			stmt.executeUpdate(query);
 			return true;
 		}
 		catch(Exception e) { /*System.out.println(e);*/ } 
 		
 		return false;
 	}
 	
 	public static boolean deleteTextMessage(int messageId) {
 		Connection con = MyDB.getConnection();
 		
 		//if(messageId == 0)
 		//	return false;
 		
 		try {
 			Statement stmt = con.createStatement();
 			String query = "DELETE FROM messages WHERE message_id="+messageId;
 			stmt.executeUpdate(query);
 			return true;
 		}
 		catch(Exception e) { /*System.out.println(e);*/ }
 		
 		return false;
 	}
 }
