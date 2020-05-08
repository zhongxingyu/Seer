 package jmbs.server;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import jmbs.common.Message;
 import jmbs.common.User;
 
 public class MessageDAO extends DAO {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7014521799755756037L;
 
 	public MessageDAO(Connection c) {
 		super(c);
 	}
 
 	/**
 	 * @param id
 	 * @return
 	 */
 	public Message getMessage(int id) {
 		Message m = null;
 		ResultSet res = send("SELECT * FROM message WHERE idmessage=" + id + ";");
 		UserDAO uDAO = new UserDAO(this.getConnection());
 
 		try {
 
 			m = new Message(res.getInt("idmessage"), uDAO.getUser(res.getInt("iduser")), res.getString("content"), res.getDate("time"));
 		} catch (SQLException e) {
 			System.out.println("No messages for id=" + id + " !");
 		}
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 
 		return m;
 	}
 	
 	/*
 	 * @return null if there a no message by the user u, else return the message.
 	 */
 	public Message getLastMessage(int iduser) // TODO javadoc.
 	{
 		Message m = null;
 		ResultSet res = send("SELECT * FROM message WHERE iduser=" + iduser + " ORDER BY idmessage DESC;"); //TODO: test it ! 
 		UserDAO uDAO = new UserDAO(this.getConnection());
 		
 		try {
 			m = new Message(res.getInt("idmessage"), uDAO.getUser(res.getInt("iduser")), res.getString("content"), res.getDate("time"));
 		} catch (SQLException e) {
 			System.err.println("No messages by " + uDAO.getUser(iduser).getFname() + " !"); // :) 
 		}
 		
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}	
 		
 		return m;
 		
 	}
 
 	/**
 	 * add message to the DB
 	 * 
 	 * @param m
 	 *            the message
 	 * @return true if message m is added successful to the DB
 	 */
 	public int addMessage(Message m) {
		int messageId = 0;
 		String query = "INSERT INTO message(content, \"time\", iduser) VALUES ('" + m.getMessage() + "', '" + m.getDatetime() + "', " + m.getOwner().getId() + ");";
 		ResultSet res = send(query);
 		
 		if (res != null)
 			messageId = getLastMessage(m.getOwner().getId()).getId(); // id of the last message sent to database by the user. (here, this message's id)
 
 		return messageId;
 	}
 
 	/**
 	 * @param iduser
 	 * @param idlastmessage
 	 * @return list of messages
 	 */
 	public ArrayList<Message> getMessages(int iduser, int idlastmessage) {
 		Connection con = new Connect().getConnection();
 		ArrayList<Message> msgList = new ArrayList<Message>();
 		String query = "SELECT idmessage, content, \"time\", iduser FROM message,follows WHERE (((follows.followed = message.iduser AND follows.follower = " + iduser + ") OR message.iduser=" + iduser + ") AND idmessage>" + idlastmessage + ") GROUP BY idmessage ORDER BY idmessage;";
 		ResultSet res = send(query);
 		UserDAO udao = new UserDAO(con);
 		
 		try {
 			msgList.add(new Message(res.getInt("idmessage"), udao.getUser(res.getInt("iduser")), res.getString("content"), res.getDate("time")));
 			while (!res.isLast()) {
 				res.next();
 				msgList.add(new Message(res.getInt("idmessage"), udao.getUser(res.getInt("iduser")), res.getString("content"), res.getDate("time")));
 			}
 		} catch (SQLException e) {
 			System.out.println("There are no new messages !");
 		}
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 		return msgList;
 	}
 
 }
