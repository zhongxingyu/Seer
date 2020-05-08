 package models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class MessageFactory {
 
 	public MessageFactory() {
 		//empty constructor
 	}
 	
 	public void sendMessage(Message message) {
 		DBConnection connection = new DBConnection();
 		int bool = 0;
 		if (message.getRead()) bool = 1;
 		try {
 				connection.insert ("INSERT INTO messages (friendFromID, friendToID, message, readMark) " +
 						"values(" + message.getFromID() + " , " + message.getToID() + " , '" + message.getMessage() + "' , " + bool + " )");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Message getMessage(int id) {
 		DBConnection connection = new DBConnection();
 		ResultSet rs = connection.performQuery("SELECT * FROM messages WHERE id = " + id);
 		try {
 			if (rs.next()) {
 				Message message = new Message();
 				message.setFromID(rs.getInt("friendFromID"));
 				message.setToID(rs.getInt("friendToID"));
 				message.setMessage(rs.getString("message"));
 				message.setRead(rs.getBoolean("readMark"));
 				message.setID(rs.getInt("id"));
 				return message;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public ArrayList<Message> getSent(int id) {
		System.out.print(id + '\n');
 		return getMessages(id, "SELECT * FROM messages WHERE friendFromID = ");
 	}
 	
 	public ArrayList<Message> getInbox(int id) {
 		return getMessages(id, "SELECT * FROM messages WHERE friendToID = ");
 	}
 	
 	public ArrayList<Message> getMessages(int id, String query) {
 		ArrayList<Message> messages = new ArrayList<Message>();
 		DBConnection connection = new DBConnection();
 		ResultSet rs = connection.performQuery(query + id);
 		try {
 			while (rs.next()) {
 				Message message = new Message();
 				message.setFromID(rs.getInt("friendFromID"));
 				message.setToID(rs.getInt("friendToID"));
 				message.setMessage(rs.getString("message"));
 				message.setRead(rs.getBoolean("readMark"));
 				message.setID(rs.getInt("id"));
 				messages.add(message);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return messages;
 	}
 	
 }
