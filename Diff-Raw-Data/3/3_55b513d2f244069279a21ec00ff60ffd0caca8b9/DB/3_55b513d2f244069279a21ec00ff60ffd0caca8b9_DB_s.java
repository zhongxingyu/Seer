 package sql;
 
 /*hi*/
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 
 import frontend.Challenge;
 import frontend.FriendRequest;
 import frontend.History;
 import frontend.Message;
 import frontend.Result;
 
 public class DB {
 	
 	private static final String MYSQL_USERNAME = "ccs108kolyyu22";
 	private static final String MYSQL_PASSWORD = "shooneon";
 	private static final String MYSQL_DATABASE_SERVER = "mysql-user.stanford.edu";
 	private static final String MYSQL_DATABASE_NAME = "c_cs108_kolyyu22";
 	private static Connection con;
 	
 	static {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			String url = "jdbc:mysql://" + MYSQL_DATABASE_SERVER + "/" + MYSQL_DATABASE_NAME;
 			con = DriverManager.getConnection(url, MYSQL_USERNAME, MYSQL_PASSWORD);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("CS108 student: Update the MySQL constants to correct values!");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			System.err.println("CS108 student: Add the MySQL jar file to your build path!");
 		}
 	}
 	
 	public static Connection getConnection() {
 		return con;
 	}
 	
 	public static void close() {
 		try {
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addUser(String user, String hash, boolean isAdmin){
 		String query = "INSERT INTO users VALUES('" + user + "', " + "'" + hash + "', " + isAdmin + ");";
 		System.out.println(query);
 		sqlUpdate(query);
 	}
 	
 	private ResultSet getResult(String query){
 		Statement stmt;
 		try {
 			stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 			return rs;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private void sqlUpdate(String query){
 		Statement stmt;
 		try {
 			stmt = con.createStatement();
 			stmt.executeUpdate(query);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	
 	public ArrayList<String> getFriends(String userId){
 		String query = "SELECT id2 FROM friends WHERE id1 = '" + userId + "';";
 		System.out.println(query);
 		ArrayList<String> list = new ArrayList<String>();
 		try {
 			ResultSet rs = getResult(query);
 			rs.beforeFirst();
 			while (rs.next()){
 				list.add(rs.getString("id2"));
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return list;
 	}
 	
 	public ArrayList<Message> getMessages(String userId){
 		ArrayList<Message> returnList = new ArrayList<Message>();
 		String query = "select * from notes where dest = '" + userId  + "'";
 		ResultSet rs = getResult(query);
 		try {
 			while(rs.next()){
 				returnList.add(new Message(rs.getString("src"), rs.getString("dest"), rs.getString("body"), rs.getString("time")));
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ArrayList<Message>();
 		
 		
 	}
 	
 	public History getHistory(String userId){
 		return null;
 	}
 	
 	public ArrayList<String> getAchievements(String userId){
 		return null;
 	}
 	
 	public boolean getIsAdmin(String userId){
 		return false;
 	}
 	
 	public void addAchievement(String userId, String achievement){
 		
 	}
 	
 	public ArrayList<Challenge> getChallenges(String userId){
 		return null;
 	}
 	
 	public void addFriend(String user1, String user2){
 		String query = "INSERT INTO friends VALUES('" + user1 + "', '" + user2 + "');";
 		sqlUpdate(query);
 		System.out.println(query);
 	}
 
 	public void removeFriend(String id, String id2) {
 		String query = "DELETE FROM friends WHERE id1 = '" + id + "' AND id2 = '" + id2 + "';";
 		sqlUpdate(query);
 		System.out.println(query);
 	}
 
 	public ArrayList<FriendRequest> getFriendRequests(String id) {
 		String query = "SELECT * FROM requests WHERE dest = '" + id + "' and isConfirmed = false;";
 		System.out.println(query);
 		ArrayList<FriendRequest> list = new ArrayList<FriendRequest>();
 		try {
 			ResultSet rs = getResult(query);
 			rs.beforeFirst();
 			while (rs.next()){
 				String source = rs.getString("source");
 				boolean isConfirmed = rs.getBoolean("isConfirmed");
 				String time = rs.getString("time");
				FriendRequest fr = new FriendRequest(source, id, source + " has added you as a friend!", isConfirmed);
 				list.add(fr);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return list;
 	}
 
 	public void addResult(String id, Result result) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setAdminStatus(String id, boolean status) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void sendMessage(Message message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
