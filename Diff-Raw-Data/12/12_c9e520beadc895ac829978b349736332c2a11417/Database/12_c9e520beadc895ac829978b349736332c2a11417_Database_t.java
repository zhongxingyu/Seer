 package mainPackage;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Scanner;
 import java.util.TreeSet;
 
 import clientPackage.Info;
 import clientPackage.Statistics;
 
 
 public class Database {
 	private static final String URL = "jdbc:mysql://localhost/wot";
 	private static final String USERNAME = "root";
	private static final String PASSWORD = "ly6adywe";
 
 	
 	/**
 	 * First register Driver. This statement registers the MySQL
 	 * driver classes with the Java program, making it possible
 	 * for this program to manipulate data on the MySQL server.
 	 * Create a connection to the database.
 	 * @throws ClassNotFoundException 
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 */
 	private static Connection createConnection() throws Exception{
 		return DriverManager.getConnection(URL, USERNAME, PASSWORD);
 	}	
 		
 	public static Info loadInfo(String username){
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			ResultSet result = statement.executeQuery("SELECT * FROM clients WHERE username='" + username + "'");
 			if (result.next()){
 				String password = result.getString("password");
 				String email = result.getString("email");
 				String nationality = result.getString("nationality");
 				String date = result.getString("date");
 				Info info = new Info(username, password, email, nationality, date);
 				result.close();
 				con.close();
 				return info;
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 		
 	public static boolean registerInfo(Info info){
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			statement.executeUpdate("INSERT INTO clients (username, password, email, nationality, last_login) VALUES ('" 
 					+ info.getUsername() + "','" + info.getPassword() + "','" + info.getEmail() + "','"+ info.getNationality() +"','')");
 			return true;
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static Statistics loadStatistics(String username){
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			ResultSet result = statement.executeQuery("SELECT * FROM statistics WHERE username='" + username + "'");
 			if (result.next()){
 				int elo = result.getInt("elo");
 				int ttl_games = result.getInt("ttl_games");
 				int ttl_wins = result.getInt("ttl_wins");
 				int ttl_leaves = result.getInt("ttl_leaves");
 				int teamwork = result.getInt("teamwork");
 				double speed = result.getDouble("speed");
 				int ttl_small = result.getInt("small");
 				int small_successful = result.getInt("small_successful");
 				int ttl_large = result.getInt("large");
 				int large_successful = result.getInt("large_successful");
 				int ttl_bombs = result.getInt("ttl_bombs");
 				Statistics statistics = new Statistics(username, elo, ttl_games, ttl_wins, ttl_leaves, teamwork, speed, ttl_small, small_successful, ttl_large, large_successful, ttl_bombs);
 				result.close();
 				con.close();
 				return statistics;
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static boolean registerStatisctics(Statistics stat){
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			statement.executeUpdate("INSERT INTO statistics (username,  ttl_games, ttl_wins, ttl_leaves, teamwork, speed, small, small_successful, large, large_successful, ttl_bombs) VALUES ('" + 
 									 stat.getUsername() + "','" + stat.getTtl_games() + "','" + stat.getTtl_wins() + "','" + 
 									 stat.getTtl_leaves() + "','" + stat.getTeamwork() + "','" + stat.getSpeed() + "','" + stat.getTtl_small() + "','" +
 									 stat.getSmall_successful() + "','" + stat.getTtl_large() + "','" + stat.getLarge_successful() + "','" + 
 									 stat.getTtl_bombs() + "')");
 			return true;
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static boolean addInvitation(String username1, String username2){
 		try{
 			if(!hasInvitation(username1, username2)){
 				Connection con = createConnection();
 				Statement statement = con.createStatement();
 				statement.executeUpdate("INSERT INTO invitations (username1, username2) VALUES ('" + 
 						username1+  "','" +  username2 + "')");
 				return true;
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	
 	public static boolean hasInvitation(String username1, String username2){
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			ResultSet result = statement.executeQuery("SELECT * FROM invitations WHERE username1='" + username1 + "'" +" AND username2='" + username2 + "'");
 			if (result.next()){
 				return true;
 			}
 
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static boolean deleteInvitation(String username1, String username2){
 		try{
 			if(hasInvitation(username1, username2)){
 				Connection con = createConnection();
 				Statement statement = con.createStatement();
 				statement.executeUpdate("DELETE FROM invitations WHERE username1='" + username1 + "'" +" AND username2='" + username2 + "'");
 			}
 			return true;
 		}catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return false;
 	}
 	
 	public static boolean addFriendship(String username1, String username2){
 		try{
 			if(!hasFriendship(username1, username2)){
 				System.err.println(username1 + " is now friend with " + username2);
 				Connection con = createConnection();
 				Statement statement = con.createStatement();
 				statement.executeUpdate("INSERT INTO friendship (username1, username2) VALUES ('" + 
 										 username1+  "','" +  username2 + "')");
 				return true;
 			}else{
 				System.err.println(username1 + " is already friend with " + username2);
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	
 	public static boolean hasFriendship(String username1, String username2) {
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			ResultSet result = statement.executeQuery("SELECT * FROM friendship WHERE (username1='" + username1 + "'" +" AND username2='" + username2 + "') OR (username1='" + username2 + "'" +" AND username2='"+username1+ "')");
 			if (result.next()){
 				return true;
 			}
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static boolean deleteFriendship(String username1, String username2){
 		try{
 			if(hasFriendship(username1, username2)){
 				Connection con = createConnection();
 				Statement statement = con.createStatement();
 				statement.executeUpdate("DELETE FROM friendship WHERE (username1='" + username1 + "'" +" AND username2='" + username2 + "') OR (username1='" + username2 + "'" +" AND username2='"+username1+ "')");
 				return true;
 			}
 			
 		}catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return false;
 	}
 	
 	public static TreeSet<String> getInvites(String username){
 		TreeSet<String> invites = new TreeSet<String>();
 		
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 
 			
 			ResultSet result = statement.executeQuery("SELECT username1 FROM invitations WHERE username2='" + username + "'");
 			while(result.next()){
 				invites.add(result.getString("username1"));
 			}
 
 			//contacts is a TreeSet Structure and hence it is sorted.
 			return invites;
 
 		}catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return null;
 	}	
 	
 	public static TreeSet<String> getContacts(String username){
 		TreeSet<String> contacts = new TreeSet<String>();
 		
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();			
 
 			ResultSet result = statement.executeQuery("SELECT username2 FROM friendship WHERE username1='" + username + "'");	
 			while(result.next()){
 				contacts.add(result.getString("username2"));
 			}
 						
 			result = statement.executeQuery("SELECT username1 FROM friendship WHERE username2='" + username + "'");				
 			while(result.next()){
 				contacts.add(result.getString("username1"));
 			}			
 			//contacts is a TreeSet Structure and hence it is sorted.
 			return contacts;
 
 		}catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return null;
 	}
 	
 
 	
 	public static void initDatabaseForTesting(){		
 		Info filotas = new Info("filotas", "123456", "filotas@gmail.com", "Ston Kosmo Mou", "01012011");
 		Info okan = new Info("okan", "123456", "okan@gmail.com", "Apo To Pouthena", "01012011");
 		Info gio = new Info("gio", "123456", "gio@gmail.com", "Apo Allo Simpan", "01012011");
 		Info tasos = new Info("tasos", "123456", "tasos@gmail.com", "Apo To Mellon", "01012011");
 		
 		registerInfo(filotas);
 		registerInfo(okan);
 		registerInfo(gio);
 		registerInfo(tasos);
 		
 		addFriendship("filotas", "tasos");
 		addFriendship("filotas", "gio");	
 		System.out.println("Database Initialized for Testing.");
 	}
 	
 	public static void emptyTheDatabase(){
 		emptyTheTable("clients");
 		emptyTheTable("statistics");
 		emptyTheTable("friendship");
 		emptyTheTable("invitations");
 		System.out.println("Database is now Empty.");
 	}
 	
 	private static void emptyTheTable(String tablename){
 		try{
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			statement.executeUpdate("TRUNCATE TABLE " + tablename);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void printUsernames(){
 		try{
 			System.out.println("All existing usernames: ");
 			Connection con = createConnection();
 			Statement statement = con.createStatement();
 			ResultSet result = statement.executeQuery("SELECT username FROM clients");
 			
 			while(result.next()){
 				System.out.println(result.getString("username"));
 			}
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void registerStats(String dateStr, String stats){
 		Scanner scanner = new Scanner(stats.toLowerCase());
 		scanner.useDelimiter("~");
 
 		String fields = "date_str";
 		String values = dateStr;
 				
 		Connection con = null;
 		Statement statement = null;
 		try{
 			con = createConnection();
 			statement = con.createStatement();
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (con != null && statement != null){
 			while (scanner.hasNext()){
 				String fieldName = scanner.next();
 				String value = scanner.next();
 				String subSQL = "SHOW COLUMNS FROM serverstat LIKE '" + fieldName + "'";
 	
 				fields += ", " + fieldName;
 				values += ", " + value;
 				
 				try {				
 					ResultSet resultSet = statement.executeQuery(subSQL);
 					resultSet.last();
 					if (resultSet.getRow() == 0)
 						statement.executeUpdate("ALTER TABLE serverstat ADD " + fieldName + " INT");
 					
 					resultSet.close();
 				}catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			String sql = "INSERT INTO serverstat (" + fields + ") VALUES (" + values + ")";
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 				statement.close();
 				con.close();
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
