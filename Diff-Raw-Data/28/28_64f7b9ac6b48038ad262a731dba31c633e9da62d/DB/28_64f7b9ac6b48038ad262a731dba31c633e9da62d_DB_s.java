 package sql;
 
 /*hi*/
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 
 import frontend.Achievement;
 import frontend.Challenge;
 import frontend.FriendRequest;
 import frontend.History;
 import frontend.Message;
 import frontend.Quiz;
 import frontend.Result;
 import frontend.User;
 
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
 	
 	/*Todo: implement this.*/
 	public ArrayList<Quiz> getPopularQuizzes(int limit){
 		String query = "select * from quizzes order by times_taken desc limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Quiz> popularQuizzes = new ArrayList<Quiz>();
 		try {
 			rs.beforeFirst();
 			while(rs.next()){
 				popularQuizzes.add(new Quiz(rs.getString("quiz_id"), rs.getString("creator_id"), rs.getString("date_created"), Boolean.parseBoolean(rs.getString("is_random")), Boolean.parseBoolean(rs.getString("is_one_page")), Boolean.parseBoolean(rs.getString("is_immediate"))));
 			}
 			return popularQuizzes;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public ArrayList<Quiz> getRecentQuizzes(int limit){
 		String query = "select * from quizzes order by date_created desc limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Quiz> recentQuizzes = new ArrayList<Quiz>();
 		try {
 			rs.beforeFirst();
 			while(rs.next()){
 				recentQuizzes.add(new Quiz(rs.getString("quiz_id"), rs.getString("creator_id"), rs.getString("date_created"), Boolean.parseBoolean(rs.getString("is_random")), Boolean.parseBoolean(rs.getString("is_one_page")), Boolean.parseBoolean(rs.getString("is_immediate"))));
 			}
 			return recentQuizzes;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
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
 	
 	// return null if user id does not exist
 	public User getUser(String id){
 		// execute query
 		String query = "SELECT * FROM users WHERE id = '" + id + "'";
 		System.out.println(query);
 		ResultSet rs = getResult(query);
 		
 		// check if result is empty
 		try {
 			if(rs.next() == false) return null;
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		// otherwise create user with query result
 		String hash = null;
 		boolean isAdmin = false;
 		try {
 			hash = rs.getString("hash");
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		try {
 			isAdmin = rs.getBoolean("isAdmin");
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return new User(id, hash, isAdmin, this);		
 	}
 	
 	public void addIsTaken(String quizID){
 		String query = "update users set times_taken = times_taken + 1 where quiz_id = " + quizID;
 		sqlUpdate(query);
 	}
 	
 	public ArrayList<String> getFriends(String userId){
 		String query = "SELECT id2 FROM friends WHERE id1 = '" + userId + "';";
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
 	
 	
 	/**/
 	
 	public void addResult(String id, Result result) {		
 		String query = "INSERT INTO results VALUES('" + id + "', " + "'" + result.getQuiz() + "', " 
 				+ result.getTimeUsed() +  ", " 
 				+ result.getNumQuestions() + ", " 
 				+ result.getNumCorrect() + ", '"
 				+ result.getDateTaken() + "'"
 				+ ")";
 		System.out.println(query);
 		sqlUpdate(query);
 	}
 	
 	public History getHistory(String userId, int limit){
 		History history = new History(userId);
 		
 		// get all entries in the results table for this user
 		String query = "SELECT * FROM results WHERE user = '" + userId + "' order by date desc limit " + limit;
 		System.out.println(query);
 		ResultSet rs = getResult(query);
 		
 		// add each result to the history
 		try{
 			while(rs.next()) {
 				String quiz = rs.getString("quiz");
 				int time = rs.getInt("time");
 				int questions = rs.getInt("questions");
 				int	correct = rs.getInt("correct");
 				String date = rs.getString("date");
 				Result r = new Result(quiz, time, questions, correct, date);
 				history.addResult(r);
 			}
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return history;
 	}
 	
 	public void addAchievement(String userId, Achievement achievement){
 		String query = "INSERT INTO achievements VALUES('" + userId + "', '" + achievement.getName() + "', '"
 				+ achievement.getDescription() + "', '"
 				+ achievement.getURL()
 				+ "')";
 		sqlUpdate(query);
 		System.out.println(query);
 	}
 	
 	public boolean getIsAdmin(String userId){
 		String query = "SELECT isAdmin FROM users WHERE id = '" + userId + "';";
 		ResultSet rs = getResult(query);
 		boolean isAdmin = false;
 		try {
 			rs.absolute(1);
 			isAdmin = rs.getBoolean("isAdmin");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(isAdmin);
 		return isAdmin;
 	}
 	
 	public ArrayList<Achievement> getAchievements(String userId){
 		ArrayList<Achievement> list = new ArrayList<Achievement>();
 		
 		// get all entries in the results table for this user
 		String query = "SELECT * FROM achievements WHERE user = '" + userId + "'";
 		System.out.println(query);
 		ResultSet rs = getResult(query);
 		
 		// add each achievement to the list
 		try{
 			while(rs.next()) {
 				String achievement = rs.getString("achievement");
 				String description = rs.getString("description");
 				String url = rs.getString("url");
 				list.add(new Achievement(achievement, description, url));				
 			}
 			return list;
 		} catch (SQLException e) {e.printStackTrace();
 		}
 		
 		return list;
 	}
 	
 	
 	public ArrayList<Challenge> getChallenges(String userId){
 		String query = "SELECT * FROM challenges WHERE destination='" + userId + "'";
 		ArrayList<Challenge> challenges = new ArrayList<Challenge>();
		try {
			ResultSet rs = getResult(query);
 			rs.beforeFirst();
 			while(rs.next()) {
 				String src = rs.getString("src");
 				String body = rs.getString("body");
 				String url = rs.getString("url");
 				String time = rs.getString("time");
 				Challenge c = new Challenge(src, userId, body, url, time);
 				challenges.add(c);
 			}
 			return challenges;
		} catch (SQLException e) {
			e.printStackTrace();
		}
 		
 		return challenges;
 	}
 	
 	public ArrayList<Message> getNotes(String userId){
 		ArrayList<Message> returnList = new ArrayList<Message>();
 		String query = "select * from notes where dest = '" + userId  + "'";
 		ResultSet rs = getResult(query);
 		try {
 			while(rs.next()){
 				returnList.add(new Message(rs.getString("src"), rs.getString("dest"), rs.getString("body"), rs.getString("time")));
 			}
			return returnList;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Message>();
 		
 	}
 	
 	public void addFriend(String user1, String user2){
 		String query = "INSERT INTO friends VALUES('" + user1 + "', '" + user2 + "');";
 		sqlUpdate(query);
 		query = "UPDATE requests SET isConfirmed = true WHERE source = '" + user1 + "' AND dest = '" + user2 + "';";
 		sqlUpdate(query);
 	}
 
 	public void removeFriend(String id, String id2) {
 		String query = "DELETE FROM friends WHERE id1 = '" + id + "' AND id2 = '" + id2 + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM friends WHERE id1 = '" + id2 + "' AND id2 = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM requests WHERE source = '" + id + "' AND dest = '" + id2 + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM requests WHERE source = '" + id2 + "' AND dest = '" + id + "';";
 		sqlUpdate(query);
 	}
 
 	public ArrayList<FriendRequest> getFriendRequests(String id) {
 		String query = "SELECT * FROM requests WHERE dest = '" + id + "';";
 		ArrayList<FriendRequest> list = new ArrayList<FriendRequest>();
 		try {
 			ResultSet rs = getResult(query);
 			rs.beforeFirst();
 			while (rs.next()){
 				String source = rs.getString("source");
 				boolean isConfirmed = rs.getBoolean("isConfirmed");
 				String time = rs.getString("time");
 				String body = source + " has added you as a friend!";
 				FriendRequest fr = new FriendRequest(source, id, body, isConfirmed, time);
 				list.add(fr);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return list;
 	}
 	
 	public void sendFriendRequest(String id1, String id2){
 		Date date = new Date();
 		String dateAsStr = date.toString();
 		String query = "INSERT INTO requests VALUES('" + id1 + "', '" + id2 + "', false, '" + dateAsStr + "');";
 		sqlUpdate(query);
 	}
 
 	
 
 	public void setAdminStatus(String id, boolean status) {
 		String query = "UPDATE users SET isAdmin = '" + status + "' WHERE id = '" + id + "'";
 		System.out.println(query);
 		sqlUpdate(query);		
 	}
 
 	public void sendMessage(Message message) {
 		String src = message.getSrc();
 		String dest = message.getDest();
 		String body = message.getBody();
 		String time = message.getTime();
 		String query = "INSERT INTO notes VALUES('" + src + "', '" + dest + "', '" + body + "', '" + time + "');";
 		sqlUpdate(query);
 	}
 
 	/**
 	 * deletes user from users, achievements, 
 	 * and friends list, and deletes
 	 * messages of any type sent to or from the user
 	 * Leaves references to the user in the history
 	 * and any quizzes created by the user remain
 	 * @param user
 	 */
 	public void removeUser(User user){
 		String id = user.getID();
 		String query = "DELETE FROM users WHERE id = '"+ id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM friends WHERE id1 = id OR id2 = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM achievements WHERE user = id;";
 		sqlUpdate(query);
 		
 		query = "DELETE FROM notes WHERE source = '" + id + "' OR dest = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM challenges WHERE source = '" + id + "' OR dest = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM requests WHERE source = '" + id + "' OR dest = '" + id + "';";
 		sqlUpdate(query);
 	}
 	
 	public ArrayList<Quiz> getCreatedQuizzes(String userID, int limit){
 		String query = "select * from quizzes where creator_id = '" + userID + "' order by date_created desc limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Quiz> createdQuizzes = new ArrayList<Quiz>();
 		try {
 			rs.beforeFirst();
 			while(rs.next()){
 				createdQuizzes.add(new Quiz(rs.getString("quiz_id"), rs.getString("creator_id"), rs.getString("date_created"), Boolean.parseBoolean(rs.getString("is_random")), Boolean.parseBoolean(rs.getString("is_one_page")), Boolean.parseBoolean(rs.getString("is_immediate"))));
 			}
 			return createdQuizzes;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void postAnnouncement(String message){
 		Date date = new Date();
 		String dateAsStr = date.toString();
 		String query = "INSERT INTO announcements VALUES('" + message + "', '" + dateAsStr + "');";
 		sqlUpdate(query);
 	}
 	
 	/**
 	 * adds a quiz to quizzes with number of times taken set to 0
 	 * to update times_taken, use incrementQuizTimesTaken
 	 * @param quiz
 	 */
 	public void addQuiz(Quiz quiz){
 		String id = quiz.getQuizId();
 		String date = quiz.getDateCreated();
 		String creatorId = quiz.getCreatorId();
 		int numQuestions = quiz.getNumQuestions();
 		boolean isRandom = quiz.getIsRandom();
 		boolean isOnePage = quiz.getIsOnePage();
 		boolean isImmediate = quiz.getIsImmediate();
 		int numTimesTaken = 0;
 		String query = "INSERT INTO quizzes VALUES('" + id + "', '" + date + "', '" + creatorId + "', " + numQuestions + ", '" + isRandom + "', '" + isOnePage + "', '" + isImmediate + "', " + numTimesTaken + ");";
 		sqlUpdate(query);
 	}
 	
 	public void incrementQuizTimesTaken(Quiz quiz){
 		String query = "SELECT times_taken FROM quizzes WHERE quiz_id = '" + quiz.getQuizId() + "';";
 		ResultSet rs = getResult(query);
 		int timesTaken = 0;
 		try {
 			timesTaken = rs.getInt("times_taken");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		timesTaken++;
 		query = "UPDATE quizzes SET times_taken = " + timesTaken + ";";
 	}
 	
 	/**
 	 * erases any entries concerning this quiz from
 	 * question_response, picture, fill_in_the_blank,
 	 * multiple_choice, answers, and quizzes tables
 	 * references to it remain in users' histories
 	 * @param quiz
 	 */
 	public void removeQuiz(Quiz quiz){
 		String id = quiz.getQuizId();
 		String query = "DELETE FROM question_response WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM picture WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM fill_in_the_blank WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM multiple_choice WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM answers WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 		query = "DELETE FROM quizzes WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 	}
 	
 	/**
 	 * removes any references to this quiz in results,
 	 * erasing any records of high scores, best times, etc
 	 * @param quiz
 	 */
 	public void resetQuizStats(Quiz quiz){
 		String id = quiz.getQuizId();
 		String query = "DELETE FROM results WHERE quiz_id = '" + id + "';";
 		sqlUpdate(query);
 	}
 	
 	public int numQuizzes(){
 		String query = "SELECT * FROM quizzes;";
 		ResultSet rs = getResult(query);
 		try {
 			rs.afterLast();
 			rs.previous();
 			return rs.getRow();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 0;
 	}
 	
 	public int numUsers(){
 		String query = "SELECT * FROM users;";
 		ResultSet rs = getResult(query);
 		try {
 			rs.afterLast();
 			rs.previous();
 			return rs.getRow();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 0;
 	}
 	
 }
