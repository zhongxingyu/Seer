 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
 
 /**
  * Connects to the database, sends queries and returns the results
  */
 
 public class Database {
 
 	private Connection conn;
 	/**
 	 * Builds up the database connection
 	 * @param url URL to the database
 	 * @param user name of database user
 	 * @param password password of database user
 	 */
 	public Database(String url, String user, String password){
 		try {
 			Class.forName("org.postgresql.Driver");
 		} catch (ClassNotFoundException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			conn = DriverManager.getConnection(url,user,password);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Puts a registered User into the database
 	 * @param user
 	 * @param password
 	 * @return
 	 */
 	public boolean register(String user, String password){
 		PreparedStatement st;
 		Integer rs;
 		String query;
 		query="INSERT INTO people(name,password) VALUES('"+user+"','"+password+"')";
 		try {
 			st = conn.prepareStatement(query);
 			rs = st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * checks if a User is in the database with the right password
 	 * @param user User who will be checked
 	 * @param password password of user
 	 * @return true if everything is okay false if user name or password wrong
 	 */
 	public boolean checkUser(String user, String password){
 		Statement st;
 		ResultSet rs;
 		String query;
 		
 		try {
 			st = conn.createStatement();
 			query="SELECT * from people WHERE name='"+user+"' AND password='"+password+"'";
 			rs = st.executeQuery(query);
 			if (rs.next()){
 				return true;
 			}
 			else{
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks if a relationship between buddies already exists
 	 * @param user actual user
 	 * @param friend new friend
 	 * @return true if friendship already exists, false otherwise
 	 */
 	private boolean checkFriendship(String user, String friend){
 		Statement st;
 		ResultSet rs;
 		String query;
 		
 		try {
 			st = conn.createStatement();
 			query="SELECT * from friends WHERE friend1='"+user+"' AND friend2='"+friend+"'";
 			rs = st.executeQuery(query);
 			if (rs.next()){
 				return true;
 			}
 			else{
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	
 	/**
 	 * Adds a new friendship in the database
 	 * @param user actual user
 	 * @param friend new friend
 	 * @return true if insertion was successful, false otherwise
 	 */
 	public boolean addBuddies(String user, String friend){
 		PreparedStatement st;
 		Integer rs;
 		String query;
 		if(!checkFriendship(user,friend)){
 			query="INSERT INTO friends(friend1,friend2) VALUES('"+user+"','"+friend+"')";
 			try {
 				st = conn.prepareStatement(query);
 				rs = st.executeUpdate();
 				st.close();
 			} catch (SQLException e) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Gets buddies from the database
 	 * @param user actual user
 	 * @return Arraylist of Buddies
 	 */
 	public ArrayList<String> getBuddies(String user){
 		ArrayList<String> buddies= new ArrayList<String>();
 		Statement st;
 		ResultSet rs;
 		String query;	
 		try {
 			st = conn.createStatement();
 			query="SELECT t.friend1 FROM"
 					+" (SELECT * FROM friends f where f.friend1='"+user+"' OR f.friend2 = '"+user+"')t"
 					+" JOIN friends w ON t.friend1=w.friend2 AND t.friend2=w.friend1 AND t.friend1!='"+user+"'";
 			rs = st.executeQuery(query);
 			while (rs.next()){
 				buddies.add(rs.getString(1));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return buddies;
 	}
 	
 	/**
 	 * Stores a message in the database
 	 * @param sender sending user
 	 * @param receiver receiving user
 	 * @param message message
 	 * @return
 	 */
 	public boolean storeMessage(String sender, String receiver, String message){
 		PreparedStatement st;
 		Integer rs;
 		String query;
 		query="INSERT INTO messages(sender,receiver,message) VALUES('"+sender+"','"+receiver+"','"+message+"')";
 		try {
 			st = conn.prepareStatement(query);
 			rs = st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		return true;
 	}
 	
 	/**
 	 * Gets all Messages for a Person o0ut of the database
 	 * @param receiver User whose messages should be read
 	 * @return Hashmap with sender and all their sended messages paired
 	 */
 	public HashMap<String,ArrayList<String>> getMessages(String receiver){
 		HashMap<String,ArrayList<String>> messages = new HashMap<String,ArrayList<String>>();
 		Statement st;
 		ResultSet rs;
 		String query;
 		ArrayList<String> temp = new ArrayList<String>();
 		String sender=null;
 		String oldsender;
 
 		try {
 			st = conn.createStatement();
 			query="SELECT sender,message FROM messages WHERE receiver='"+receiver+"' ORDER BY sender ASC" ;
 			rs = st.executeQuery(query);
 			while (rs.next()){
 				oldsender = sender;
 				sender=rs.getString(1).trim();
 				if(oldsender!=null && !oldsender.equals(sender)){
 					messages.put(oldsender, temp);
 					temp = new ArrayList<String>();
 				}
 				temp.add(rs.getString(2));
 			}
 			rs.close();
 			messages.put(sender, temp);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return messages;
 	}
 	/**
 	 * deletes Messages in the database
 	 * @param sender User who send 
 	 * @param receiver User who received
 	 * @param message
 	 * @return
 	 */
 	public boolean deleteMessage(String sender, String receiver, String message){
 		PreparedStatement st;
 		Integer rs;
 		String query;
 		query="DELETE FROM messages WHERE sender='"+sender+"' AND receiver='"+receiver+"' AND message='"+message+"'";
 		try {
 			st = conn.prepareStatement(query);
 			rs = st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @return Hashmap of users as keys and passwords as values
 	 */
 	public HashMap<String,String> getUsers(){
 		HashMap<String,String> users = new HashMap<String,String>();
 		Statement st;
 		ResultSet rs;
 		String query;
 		query = "SELECT * FROM people";
 		try{
 			st = conn.createStatement();
 			rs = st.executeQuery(query);
 			while (rs.next()){
 				users.put(rs.getString(1),rs.getString(2));
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 		
 	}
 	
	public HashMap<String,String> getFriendships(){
		HashMap<String,String> users = new HashMap<String,String>();
 		Statement st;
 		ResultSet rs;
 		String query;
 		query = "SELECT * FROM friends";
 		try{
 			st = conn.createStatement();
 			rs = st.executeQuery(query);
 			while (rs.next()){
				users.put(rs.getString(1),rs.getString(2));
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 		}
		return users;
 	}
 	
 	public void refresh(){
 		PreparedStatement st;
 		Integer rs;
 		String query;
		query="DELETE * FROM messages";
 		try {
 			st = conn.prepareStatement(query);
 			rs = st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
		query="DELETE * FROM friends";
 		try {
 			st = conn.prepareStatement(query);
 			rs = st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
		query="DELETE * FROM people";
 		try {
 			st = conn.prepareStatement(query);
 			rs = st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 }
