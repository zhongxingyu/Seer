 package ge.edu.freeuni.restaurant.logic;
 /**
  * The class that connects to the database. It's a synchronized singleton class. It has several methods that update or select
  * information from the database.
  */
 
 import java.sql.*;
 import java.util.ArrayList;
 
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
 	 * Returns the current date.
 	 * @return the current date.
 	 */
 	public String getCurrentDate() throws SQLException {
 		ResultSet rset = stmt.executeQuery("select sysdate()");
 		rset.next();
 		String str = rset.getString(1);
 		return str;
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
 			System.out.println("done");
 
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
 	 * 
 	 * @param username the name of the user who is logged in
 	 * @return information about the the reserved table by user and reservation time
 	 * @throws SQLException
 	 */
 	public ResultSet getUsersReservedInfo(String username, int table_id) throws SQLException {
 		ResultSet rs;
 		System.out.println("select * from user_table where username = '"+username+"' and id = "+table_id);
 		rs = stmt.executeQuery("select * from user_table where username = '"+username+"' and id = "+table_id);
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
 							user.getPassword() + "', '" + user.getMail() +"', '" + user.getName() +"', '" +user.getSurname()+"', '"+
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
 	public ArrayList<String> getAllUsers() throws SQLException{
 		ResultSet set ;
 		ArrayList<String> users = new ArrayList<String>();
 		set =  stmt.executeQuery("Select username from user where admin = 0");
 		while(set.next()){
 			users.add(set.getString("username"));
 		}
 		return users;
 	}
 
 
 	
 	public User getUser(String username){
 		ResultSet rs;
 		User user = null;
 		try {
 			rs = stmt.executeQuery("select * from User where username = \""+username+"\"");
 			if(rs.next()){
 				String pass = rs.getString(2);
 				String mail = rs.getString(3);
 				String name = rs.getString(4);
 				String surname = rs.getString(5);
 				String info = rs.getString(6);
 				boolean admin = rs.getBoolean(7);
 				user = new User(username, pass, mail, name, surname, info, admin);
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
 		try {
 			String str = "'";
 			stmt.executeUpdate("insert into occupation values("+id+"," + str + name + str+")");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 	//unda washalos occupation cxrilidan mocemuli id
 	public void removeFromOccupation(int id){
 		try {
 			String str = "'";
 			stmt.executeUpdate("delete from occupation where table_id="+id);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	public ResultSet TableJoinOccupation(){
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from tables inner join occupation");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	/**
 	 * gadaecema useris saxeli an gvari da abrunebs
 	 * msgavsi saxeli/gvaris userebsis lists
 	 * @param name momxmareblis saxeli/gvari
 	 * @return bazidan amogebul shesabamisi usereebis listi
 	 */
 	public ArrayList<User> filterUsers(String name){
 		ArrayList<User> list = new ArrayList<User>();
 		ResultSet rs = null;
 		String filterBy = name;
 		
 		try {
 			Statement st = con.createStatement();
 			rs = st.executeQuery("select username from user where name like \"%" + filterBy + "%\"" +
 					" or surname like \"%" + filterBy + "%\"");
 			while(rs.next()){
 				String username = rs.getString(1);
 				list.add(getUser(username));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return list;
 	}
 	
 	public void deleteUser(String username){
 		try {
 			stmt.executeUpdate("delete from user where username = \""+username+"\"");
 			stmt.executeUpdate("delete from user_table where username = \""+username+"\"");
 			stmt.executeUpdate("delete from shekveta where username = \""+username+"\"");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//unda daabrunos mteli menu cxrili
 	public ResultSet selectFromMenu(){
 		ResultSet rset = null;
 		try {
 			rset = stmt.executeQuery("select * from menu");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return rset;
 	}
 	
 	public void updatePrice(int id, double price){
 		try {
 			stmt.executeUpdate("update menu set price = "+price+" where id = "+id);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	//unda daaselectos where name=name;
 	public ResultSet selectFromMenuByName(String name){
 		
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from menu where name ='" +name+"'");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 		
 	}
 	
 	//unda chaamatos menu-shi axali kerdzi. id tavisic unda izrdebodes chamatebisas menu cxrilshi
 	public void insertIntoMenu(String name, double price, String category){
 		try {
 			stmt.executeUpdate("insert into menu (name, price, foodtype) values('"+name+"',"+price+",'"+category+"')" );
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//menu xrilidan unda washalos mocemuli saxelis kerdzi
 	public void removeFromMenuByName(int id){
 		try {
 			stmt.executeUpdate("delete from menu where id = "+id);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//shekveta cxrilshi amatebs username_s kerdzis_ids da quantitys
 	public void insertIntoShekveta(String userName, int kerdzi_id, int quantity){
 		try {
 			//System.out.println("insert into shekveta (username, kerdzi_id, quantity) values('"+userName+"',"+kerdzi_id+"," + quantity +")");
 			stmt.executeUpdate("insert into shekveta (username, kerdzi_id, quantity) values('"+userName+"',"+kerdzi_id+"," + quantity +")" );
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	//aketebs selects shekveta cxrilidan username_is mixedvit
 	public ResultSet selectFromShekvetaByUserName(String userName){
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from shekveta where username ='" +userName+"'");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 		
 	}
 	
 	/**
 	 * shlis users shekveta cxrilidan useris mixedvit
 	 * @param username
 	 * @throws SQLException
 	 */
 	public void removeFromShekvetaByName(String username) throws SQLException{
 		stmt.executeUpdate("delete  from shekveta where username='"+username+"'");
 	}
 	
 	
 	/**
 	 * Returns all the users from the database about who we have statistics.
 	 * @return The list of all users.
 	 */
 	public ResultSet selectFromUserHistory(){
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from UserHistory ");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	
 	public ResultSet selectNameByIdFromMenu(int id){
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select name from menu where id='"+id+"'");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	public void updateResultsInUserHistory(String userName, int visits, int bookings, int notCome, double moneySpent){
 		try {
 			stmt.executeUpdate("update userhistory " +
 					" set visits = visits + "+visits+", bookings = bookings + "+bookings+", notcome = notcome + " +
 							" "+notCome+", totalmoney = totalmoney + "+moneySpent +" " +
 									" where username = '"+userName+"'");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * es metodi washlis yvela tables tu arsebobs da axlidan sheqmnis, mattvis vinc bazas testavs
 	 * @throws SQLException
 	 */
 	
 	public void setUpTables() throws SQLException{
 		
 		
 		stmt.execute("Drop Table IF EXISTS menu");
 		stmt.execute("Drop Table IF EXISTS occupation");
 		stmt.execute("Drop Table IF EXISTS shekveta");
 		stmt.execute("Drop Table IF EXISTS ReservedTables");
 		stmt.execute("Drop Table IF EXISTS Tables");
 		stmt.execute("Drop Table IF EXISTS USER_TABLE");
 		stmt.execute("Drop Table IF EXISTS USER");
 		
 		stmt.execute("CREATE TABLE MENU (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, price DOUBLE NOT NULL, foodtype VARCHAR(50) NOT NULL, PRIMARY KEY (id))");
 		stmt.execute("CREATE TABLE Occupation ( table_id INT NOT NULL, username varchar(50) NOT NULL)");
 		stmt.execute("CREATE TABLE shekveta (username varchar(50) NOT NULL,  kerdzi_id INT NOT NULL, quantity INT NOT NULL)");
 		stmt.execute("CREATE TABLE ReservedTables ( id INT, reserveInfo VARCHAR(100), PRIMARY KEY (id))");
 		stmt.execute("CREATE TABLE Tables ( id INT NOT NULL AUTO_INCREMENT, size int NOT NULL, description varchar(100), PRIMARY KEY (id))");
 		stmt.execute("CREATE TABLE USER_TABLE ( username varchar(50) NOT NULL, id INT NOT NULL, reserveInfo VARCHAR(100), PRIMARY KEY (username,id))");
 		stmt.execute("CREATE TABLE User (username varchar(50) NOT NULL, password varchar(50) NOT NULL, name varchar(50) NOT NULL,surname varchar(50) NOT NULL, info varchar(50) NOT NULL, admin boolean not null default 0, PRIMARY KEY (username))");
 		
 	}
 
 	/**
 	 * Returns true if this food exists in menu
 	 * else false.
 	 * @param foodID
 	 * @return
 	 */
 	public boolean existFood(int foodId) {
 		ResultSet rset;
 		try {
 			rset = stmt.executeQuery("select * from menu where id = " + foodId);
 			rset.last();
 			return rset.getRow() > 0;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Deletes reservation for the user and given table id with given time index.
 	 * @param table_id the id of the table
 	 * @param time_index the index of the time
 	 * @param username the username owning a reservation
 	 * @throws SQLException
 	 */
 	public void deleteReservation(int table_id, int time_index, String username) throws SQLException {
 		ResultSet rs = getUsersReservedInfo(username, table_id);
 		String str = rs.getString("reserveInfo");
 		char [] ch= str.toCharArray();
 		ch[time_index] = '1';
 		str = new String(ch);
 		stmt.executeUpdate("update USER_TABLE set reserveInfo =  '"+str+"' "+
 									" where username = '"+username+"' and id = "+table_id);
 		
 	}
 	
 	/**
 	 * Deletes the reservation info from reservedTables table.
 	 * @param table_id the id of the table
 	 * @param time_index the index of the time
 	 * @throws SQLException
 	 */
 	public void deleteReservationFromReservedTables(int table_id, int time_index) throws SQLException {
 		ResultSet rs = getReservedInfo(table_id);
 		String str = rs.getString("reserveInfo");
 		char[] ch = str.toCharArray();
 		ch[time_index] = '0';
 		str = new String(ch);
 		stmt.executeUpdate("update reservedtables set reserveInfo = '"+str+"' where id = "+table_id);
 	}
 	
 	/**
 	 * Returns the ResultSet object containing all the rows from the user_table table.
 	 * @return the resultSet of all the rows of user_table table
 	 * @throws SQLException
 	 */
 	public ResultSet getAllRowsFromUserTable() throws SQLException {
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from USER_TABLE ");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	public boolean isOccupiedTable(int table_id) throws SQLException{
		ResultSet rs = stmt.executeQuery("select * from occupation where id = "+table_id);
 		return rs.next();
 	}
 	
 }
