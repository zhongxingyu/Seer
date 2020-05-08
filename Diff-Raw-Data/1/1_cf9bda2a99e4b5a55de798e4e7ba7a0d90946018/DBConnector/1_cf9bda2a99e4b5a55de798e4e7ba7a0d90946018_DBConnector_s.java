 package ge.edu.freeuni.restaurant.logic;
 /**
  * The class that connects to the database. It's a synchronized singleton class. It has several methods that update or select
  * information from the database.
  */
 
 import java.sql.*;
 import java.util.Arrays;
 
 public class DBConnector{
 	private static Object lock  = new Object();
 	static String server = "localhost";
 	static String password = ""; //<---------
 	static String account = "root";
 	static String database = "test"; //<--------- 
 	private static  Connection con;
 	private static DBConnector db;
 	static Statement stmt;
 	
 	/**
 	 * This method returns the DBManager object, initialized AT MOST once.
 	 */
 	public static DBConnector getInstance(){
 		synchronized(lock){
 			if(db==null) db = new DBConnector();
 			return db;
 		}
 	}
 	
 	/**
 	 * The constructor of this class.
 	 */
 	private DBConnector(){
 		try {
 			 Class.forName("com.mysql.jdbc.Driver");
 			 con = DriverManager.getConnection("jdbc:mysql://" + server, account, password);
 			 stmt = con.createStatement();
 			 stmt.executeQuery("USE " + database);
 		}
 		catch (ClassNotFoundException e) {
 			  e.printStackTrace();
 		}
 		catch (SQLException e) {
 			 e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * @return the information about all the tables the restaurant from the database. (Note: It's not a SQL table,
 	 * but ordinary one :) Ex: I am a table.).
 	 * @throws SQLException
 	 */
 	public ResultSet getTables() throws SQLException{
 		ResultSet rset;
 		rset = stmt.executeQuery("select * from tables");
 		return rset;
 
 	}
 	
 	/**
 	 * @param table_id the id of a table we are interested in.
 	 * @return the information about the reservation times of the table.
 	 */
 	public ResultSet getReservedInfo(int table_id) throws SQLException{
 		ResultSet rs;
 		rs = stmt.executeQuery("select * from ReservedTables where id = "+table_id);
 		if(rs.next()){
 			return rs;
 		}
 		return null;
 	}
 	
 	/**
 	 * @param table_id the id of a table we are interested in.
 	 * @param timeIndex index of the time, it's bit string with length = 30; first 0-14 bits are for the first day reservation
 	 * and 15 - 29 are for second day. the restaurant works from 9:00 t 0 24:00 so 9:00 is index 0, 10:00 is 1 and so on.
 	 * @return true if the table reserved successfully and false if it is allready reserved.
 	 */
 	
 	public boolean reserveTable(int table_id, int timeIndex) throws SQLException{
 		ResultSet rset;
 		rset = stmt.executeQuery("select * from ReservedTables where id = "+table_id);
 
 		if(rset.next()){
 			String str =  rset.getString("reserveInfo");
 			if(str.charAt(timeIndex) == '1') return false;
 			char[] arr = str.toCharArray();
 			arr[timeIndex] = '1';
 			String newOne = new String(arr);
 			stmt.executeUpdate("update ReservedTables set reserveInfo = '" + newOne + "' where id =" +table_id);
 		}
 		return true;
 	}
 	
 	public boolean reserveTable(int table_id, String timeIndex) throws SQLException{
 			char[] arr = timeIndex.toCharArray();
 			for (int i = 0; i < timeIndex.length(); i++) {
 				if(timeIndex.charAt(i)=='2')arr[i]='1';
 			}
 			String newOne = new String(arr);
 			stmt.executeUpdate("update ReservedTables set reserveInfo = '" + newOne + "' where id =" +table_id);
 		return true;
 	}
 	
 	public void reserveForUser(String userId, int table_id, String timeIndex) throws SQLException{
 		char[] arr = timeIndex.toCharArray();
 		for (int i = 0; i < timeIndex.length(); i++) {
 			if(timeIndex.charAt(i)=='2')arr[i]='2';
 			else arr[i] = '1';
 		}
 		String newOne = new String(arr);
 		ResultSet rset;
 		rset = stmt.executeQuery("select * from user_table where username = '"+userId+"' and id="+table_id);
 
 		if(rset.next()){
 			stmt.executeUpdate("update user_table set reserveInfo = '" + newOne + "' where username='"+userId+"' and id =" +table_id);
 		}else{
 			stmt.executeUpdate("insert into user_table values('"+userId+"',"+table_id+","+newOne+")");
 		}
 	}
 	
 	public boolean isCorrectUsernameAndPassword(String username, String password){
 		try {
 			ResultSet rset;
 			rset = stmt.executeQuery("select * from User where username = '"+username + "' and password = '" + password + "'");
 			rset.last();
 			return rset.getRow() > 0;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * writes user in database. returns true if user is not in database, false otherwise
 	 * @param user
 	 * @return
 	 */
 	public boolean registerNewUser(User user){
 		if(isUsernameInUse(user.getUsername())){
 			return false;
 		}
 		try {
 			stmt.executeUpdate("insert into User values('"+user.getUsername()+"', '" +
 							user.getPassword() + "', '" + user.getName() +"', '" +user.getSurname()+"', '"+
 							user.getInfo() + "'," + false + ")");	
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private boolean isUsernameInUse(String username){
 		try {
 			ResultSet rset;
 			rset = stmt.executeQuery("select * from User where username = '"+username + "'");
 			rset.last();
 			return rset.getRow() > 0;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * retrieve admin column from user table and check for admin flag
 	 * @param username
 	 * @return true if the user is admin
 	 */
 	public boolean isAdmin(String username){
 		ResultSet rset;
 		boolean res = false;
 		try {
 			rset = stmt.executeQuery("select admin from User where username = '"+username + "'");
 			if(rset.next()){
 				res = rset.getBoolean(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return res;
 	}
 	
 	public User getUser(String username){
 		ResultSet rs;
 		User user = null;
 		try {
 			rs = stmt.executeQuery("select * from User where username = \""+username+"\"");
 			if(rs.next()){
 				String pass = rs.getString(2);
 				String name = rs.getString(3);
 				String surname = rs.getString(4);
 				String info = rs.getString(5);
 				boolean admin = rs.getBoolean(6);
 				user = new User(username, pass, name, surname, info, admin);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return user;
 	}
 	
 	
 	// es ubralod ragaca ro gadmoecemodes mainc, dasatestad . . .
 	// roca daceren cxrils mere unda shecvalon . ..s
 	public ResultSet getOccupiedBy(int id){
 		//gadmoecema magidis id da unda daaselectos occupation cxrilidan am id-ze mjdomi
 		ResultSet rs = null;
 		
 		try {
 			rs = stmt.executeQuery("select * from Occupation where table_id = \""+id+"\"");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	//unda chaamatos occupation cxrilshi...
 	public void addIntoOccupation(int id, String name){
 		
 	}
 	//unda washalos occupation cxrilidan mocemuli id
 	public void removeFromOccupation(int id){
 		
 	}
 	
 	public ResultSet TableJoinOccupation(){
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from tables inner join occupation");
			if(rs.next()){}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 	}
 }
