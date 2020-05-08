 package site;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.HashSet;
 
 public class AccountManager {
 
 	private Connection con;
 	
 	public AccountManager() {
 		
 		con = MyDB.getConnection();
 	}
 	
 	public boolean isExistingAccountById(String user_id) { 
 		int resCount = 0;
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE user_id='"+user_id+"'");
 			while(rs.next()) {
 				resCount++;
 			}
 		}
 		catch(Exception e) {}
 		if(resCount == 0) {
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean isExistingAccount(String username) { 
 		int resCount = 0;
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='"+username+"'");
 			while(rs.next()) {
 				resCount++;
 			}
 		}
 		catch(Exception e) {}
 		if(resCount == 0) {
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean isPasswordCorrect(String username, String password) { 
 		if(!isExistingAccount(username)) return false;
 		
 		String hashedPassword = hashPassword(password);
 		String realPassword = null;
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='"+username+"'");
 			while(rs.next()) {
 				realPassword = rs.getString("password");
 			}
 		}
 		catch(Exception e) {} 
 		
 		if(hashedPassword.equals(realPassword))
 			return true;
 		return false; 
 	}
 	
 	public void updateAchievements(int user_id) {
 		int resCount = 0;
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM quizzes WHERE user_id="+user_id);
 			while(rs.next()) {
 				resCount++;
 			}
 		}
 		catch(Exception e) { System.out.println(e); }
 		
 		int userCount = 0;
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM achievements WHERE user_id="+user_id);
 			while(rs.next()) {
 				userCount++;
 			}
 			if(userCount == 0) {
 				stmt = con.createStatement();
 				stmt.executeUpdate("INSERT INTO achievements (user_id) VALUES("+user_id+")");
 			}
 		}
 		catch(Exception e) { System.out.println(e); }
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			if(resCount > 0) {
 				stmt.executeUpdate("UPDATE achievements SET amateur_author=true WHERE user_id="+user_id);
 			}
 			if(resCount > 4) {
 				stmt.executeUpdate("UPDATE achievements SET prolific_author=true WHERE user_id="+user_id);
 			}
 			if(resCount > 9) {
 				stmt.executeUpdate("UPDATE achievements SET prodigious_author=true WHERE user_id="+user_id);
 			}
 			int numTaken = QuizResult.numTaken(user_id);
 			if(numTaken > 9) {
 				stmt.executeUpdate("UPDATE achievements SET quiz_machine=true WHERE user_id="+user_id);
 			}
 			
 			//i am the greatest
 			//practice makes perfect
 		}
 		catch(Exception e) { System.out.println(e); } 
 	}
 	
 	public Achievements getAchievements(int user_id) {
 		Achievements achievements = new Achievements();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM achievements WHERE user_id="+user_id);
 			while(rs.next()) {
 				achievements.setAmateurAuthor(rs.getBoolean("amateur_author"));
 				achievements.setProlificAuthor(rs.getBoolean("prolific_author"));
 				achievements.setProdigiousAuthor(rs.getBoolean("prodigious_author"));
 				achievements.setQuizMachine(rs.getBoolean("quiz_machine"));
 				achievements.setiAmTheGreatest(rs.getBoolean("i_am_greatest"));
 				achievements.setPracticeMakesPerfect(rs.getBoolean("practice_perfect"));
 				
 			}
 		}
 		catch(Exception e) {System.out.println(e);}
 		
 		return achievements;
 	}
 	
 	public HashSet<Integer> getFriends(int user_id) {
 		
 		HashSet<Integer> xFriends = new HashSet<Integer>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM friends WHERE x_id="+user_id);
 			while(rs.next()) {
 				xFriends.add(rs.getInt("y_id"));
 			}
 		}
 		catch(Exception e) {}
 		
 		HashSet<Integer> yFriends = new HashSet<Integer>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM friends WHERE y_id="+user_id);
 			while(rs.next()) {
 				yFriends.add(rs.getInt("x_id"));
 			}
 		}
 		catch(Exception e) {}
 		
 		xFriends.retainAll(yFriends);
 		
 		return xFriends;
 	}
 	
 	public boolean areFriends(int x_id, int y_id) {
 		boolean areFriends = false;
 		
 		int resCount = 0;
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM friends WHERE x_id="+x_id+" AND y_id="+y_id);
 			while(rs.next()) {
 				resCount++;
 			}
 			if(resCount > 0) resCount = 1;
 		}
 		catch(Exception e) {}
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM friends WHERE x_id="+y_id+" AND y_id="+x_id);
 			while(rs.next()) {
 				resCount++;
 			}
 			if(resCount > 1) resCount = 2;
 		}
 		catch(Exception e) {}
 		
 		if(resCount == 2) areFriends = true;
 		return areFriends;
 	}
 	
 	public void removeFriend(int from_id, int to_id) {
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			String query = "DELETE FROM friends WHERE x_id="+from_id+" AND y_id="+to_id;
 			stmt.executeUpdate(query);
 		}
 		catch(Exception e) {} 
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			String query = "DELETE FROM friends WHERE x_id="+to_id+" AND y_id="+from_id;
 			stmt.executeUpdate(query);
 		}
 		catch(Exception e) {} 
 	}
 	
 	public void addFriend(int from_id, int to_id) {
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			String query = "INSERT INTO friends (x_id, y_id)" +
 						   " VALUES("+from_id+", "+to_id+")";
 			stmt.executeUpdate(query);
 		}
 		catch(Exception e) {} 
 	}
 	
 	public void sendChallenge(String challengerScore, String quizId, String from_id, String to_id) {
 		User fromUser = this.getAccountById(from_id);
 		User toUser = this.getAccountById(to_id);
 
 		int messageType = 2; //challenge
 		String content = fromUser.getUsername()+" has challenged you to a quiz. " +
 				"<form action=\\'quiz_summary_page.jsp\\' method=\\'POST\\'>" +
 				"<input type=\\'hidden\\' name=\\'challenger_best_score\\' value=\\'"+challengerScore+"\\' />" +
 				"<input type=\\'hidden\\' name=\\'challenger_id\\' value=\\'"+fromUser.getId()+"\\' />" +
 				"<input type=\\'hidden\\' name=\\'quiz_id\\' value=\\'"+quizId+"\\' />" +
 				"<input type=\\'submit\\' value=\\'Take the quiz!\\' /></form>";
 		
 		TextMessage message = new TextMessage(fromUser,toUser,messageType,content);
 		Inbox.sendTextMessage(message);
 	}
 	
 	public void sendFriendRequest(String from_id, String to_id) {
 		User fromUser = this.getAccountById(from_id);
 		User toUser = this.getAccountById(to_id);
 		
 		int messageType = 1; //friend request
		String content = fromUser.getUsername()+" wants to be friends. " +
				"<form action=\\'add_friend.jsp\\' method=\\'POST\\'>" +
				"<input type=\\'hidden\\' name=\\'confirmation\\' value=\\'true\\' />" +
				"<input type=\\'hidden\\' name=\\'x_id\\' value=\\'"+toUser.getId()+"\\' />" +
				"<input type=\\'hidden\\' name=\\'y_id\\' value=\\'"+fromUser.getId()+"\\' />" +
				"<input type=\\'submit\\' value=\\'Add as friend\\' /></form>";
 		
 		TextMessage message = new TextMessage(fromUser,toUser,messageType,content);
 		Inbox.sendTextMessage(message);
 	}
 	
 	public User getAccountById(String user_id) {
 		if(!isExistingAccountById(user_id)) return null;
 		
 		int id=0;
 		String username="";
 		String password="";
 		boolean isAdmin=false;
 		int loginCount=0;
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE user_id='"+user_id+"'");
 			while(rs.next()) {
 				id = rs.getInt("user_id");
 				username = rs.getString("username");
 				password = rs.getString("password");
 				isAdmin = rs.getBoolean("is_admin");
 				loginCount = rs.getInt("login_count");
 			}
 			User user = new User(id, username, password, isAdmin, loginCount);
 			return user;
 		}
 		catch(Exception e) {}
 		return null;
 	}
 	
 	public User getAccount(String username) {
 		if(!isExistingAccount(username)) return null;
 		
 		int id=0;
 		String password="";
 		boolean isAdmin=false;
 		int loginCount=0;
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='"+username+"'");
 			while(rs.next()) {
 				id = rs.getInt("user_id");
 				password = rs.getString("password");
 				isAdmin = rs.getBoolean("is_admin");
 				loginCount = rs.getInt("login_count");
 			}
 			User user = new User(id, username, password, isAdmin, loginCount);
 			return user;
 		}
 		catch(Exception e) {}
 		return null;
 	}
 	
 	public User createNewAccount(String username, String password, boolean isAdmin) {
 		if(isExistingAccount(username)) return null;
 		int loginCount = 0;
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='"+username+"'");
 			while(rs.next()) {
 				loginCount = Integer.parseInt(rs.getString("login_count"));
 			}
 		}
 		catch(Exception e) {}
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			String query = "INSERT INTO users (username, password, is_admin, login_count, created_timestamp, last_login_timestamp)" +
 						   " VALUES('"+username+"', '"+hashPassword(password)+"', "+isAdmin+", "+loginCount+", NULL, NULL)";
 			stmt.executeUpdate(query);
 		}
 		catch(Exception e) { return null; } 
 		
 		int id = 0;
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username='"+username+"'");
 			while(rs.next()) {
 				id = Integer.parseInt(rs.getString("user_id"));
 			}
 			User user = new User(id, username, hashPassword(password), isAdmin, loginCount);
 			return user;
 		}
 		catch(Exception e) {}
 		
 		return null;
 		
 	}
 	
 	/*
 	 Given a byte[] array, produces a hex String,
 	 such as "234a6f". with 2 chars for each byte in the array.
 	 (provided code)
 	*/
 	public static String hexToString(byte[] bytes) {
 		StringBuffer buff = new StringBuffer();
 		for (int i=0; i<bytes.length; i++) {
 			int val = bytes[i];
 			val = val & 0xff;  // remove higher bits, sign
 			if (val<16) buff.append('0'); // leading 0
 			buff.append(Integer.toString(val, 16));
 		}
 		return buff.toString();
 	}
 	
 	public String hashPassword(String input) {
 		byte[] bytes = input.getBytes();
 		
 		MessageDigest hash = null; //SHA hash
 		try {
 			hash = MessageDigest.getInstance("SHA");
 		} catch (NoSuchAlgorithmException e1) {
 			e1.printStackTrace();
 		}
 		
 		String res = null;
 		try {
 			hash.update(bytes);
 			byte[] output = hash.digest();
 			res = hexToString(output);
 		}
 		catch(Exception e) {
 			
 		}
 		
 		return res;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
