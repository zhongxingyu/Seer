 package sql;
 
 /*hi*/
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 import javax.servlet.RequestDispatcher;
 
 import frontend.Achievement;
 import frontend.Announcement;
 import frontend.Challenge;
 import frontend.FillInBlank;
 import frontend.FriendRequest;
 import frontend.History;
 import frontend.Message;
 import frontend.MultipleChoice;
 import frontend.Picture;
 import frontend.Question;
 import frontend.QuestionResponse;
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
 	
 	private PreparedStatement getPreparedStatement(String query){
 		System.out.println(query);
 		try {
 			PreparedStatement pstmt = con.prepareStatement(query);
 			return pstmt;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static Connection getConnection() {
 		return con;
 	}
 	
 	// returns true if a quiz exists with the given name
 	public boolean isQuizAvailable(String quizName){
 		String query = "SELECT * FROM quizzes WHERE quiz_id = '" + quizName + "'";
 		ResultSet rs = getResult(query);
 		try {
 			if(rs.next()) return true;
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return false;
 	}
 	
 	/*Todo: implement this.*/
 	public ArrayList<Quiz> getPopularQuizzes(int limit){
 		String query = "select * from quizzes order by times_taken desc limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Quiz> popularQuizzes = new ArrayList<Quiz>();
 		try {
 			int q = rs.getRow();
 			while(rs.next()){
 				popularQuizzes.add(new Quiz(rs.getString("quiz_id"), rs.getString("creator_id"), rs.getString("date_created"), Boolean.parseBoolean(rs.getString("is_random")), Boolean.parseBoolean(rs.getString("is_one_page")), Boolean.parseBoolean(rs.getString("is_immediate")), rs.getString("image_url"), rs.getString("description")));
 			}
 			return popularQuizzes;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public ArrayList<Quiz> getQuizzes(){
 		ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
 		String query = "SELECT * FROM quizzes;";
 		ResultSet rs = getResult(query);
 		try {
 			while (rs.next()){
 				String quizId = rs.getString("quiz_id");
 				Quiz quiz = this.getQuiz(quizId);
 				quizzes.add(quiz);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return quizzes;
 	}
 	
 	public ArrayList<Quiz> getRecentQuizzes(int limit){
 		String query = "select * from quizzes order by date_created desc limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Quiz> recentQuizzes = new ArrayList<Quiz>();
 		try {
 			rs.beforeFirst();
 			while(rs.next()){
 				recentQuizzes.add(new Quiz(rs.getString("quiz_id"), rs.getString("creator_id"), rs.getString("date_created"), Boolean.parseBoolean(rs.getString("is_random")), Boolean.parseBoolean(rs.getString("is_one_page")), Boolean.parseBoolean(rs.getString("is_immediate")), rs.getString("image_url"), rs.getString("description")));
 			}
 			return recentQuizzes;
 		} catch (SQLException e) {
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
 	
 	public void addUser(String user, String hash, boolean isAdmin, String imageURL){
 		String query = "INSERT INTO users VALUES('" + user + "', " + "'" + hash + "', " + isAdmin
 				+ ", '" + imageURL + "');";
 		sqlUpdate(query);
 	}
 	
 	public void setUserImage(String userID, String image) {
 		String query = "UPDATE users SET image_url='" + image + "' WHERE id='" + userID + "'";
 		sqlUpdate(query);
 	}
 	
 	public String getUserImage(String userID) {
 		String query = "SELECT image_url FROM users WHERE id = '" + userID + "';";
 		ResultSet rs = getResult(query);
 		String image = "";
 		try {
 			rs.next();
 			image = rs.getString("image_url");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return image;
 	}
 	
 	private ResultSet getResult(String query){
 		PreparedStatement pstmt;
 		try {
 			pstmt = getPreparedStatement(query);
 			ResultSet rs = pstmt.executeQuery();
 			return rs;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private void sqlUpdate(String query){
 		PreparedStatement pstmt;
 		try {
 			pstmt = getPreparedStatement(query);
 			pstmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	// return null if user id does not exist
 	public User getUser(String id){
 		// execute query
 		String query = "SELECT * FROM users WHERE id = '" + id + "'";
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
 		
 		String image = "";
 		try {
 			image = rs.getString("image_url");
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return new User(id, hash, isAdmin, this, image);		
 	}
 	
 	// returns all Users whose id contains the given fragment
 	 // return null if there are no matches
 		public ArrayList<User> searchUsers(String fragment){
 			// execute query
			String query = "SELECT * FROM users WHERE id = '%" + fragment + "%";
 			System.out.println(query);
 			ResultSet rs = getResult(query);
 			
 			ArrayList<User> matches = new ArrayList<User>();
 			try {
 				while(rs.next()){
 					// add user to list
 					String id = null;					
 					try {
 						id = rs.getString("id");
 					} catch (SQLException e) {e.printStackTrace();}
 					
 					String hash = null;					
 					try {
 						hash = rs.getString("hash");
 					} catch (SQLException e) {e.printStackTrace();}
 					
 					boolean isAdmin = false;
 					try {
 						isAdmin = rs.getBoolean("isAdmin");
 					} catch (SQLException e) {e.printStackTrace();}
 					
 					String image = null;
 					try {
 						image = rs.getString("image_url");
 					} catch (SQLException e) {e.printStackTrace();}
 					
 					matches.add(new User(id, hash, isAdmin, this, image));
 				}
 			}catch (SQLException e) {e.printStackTrace();}			
 			
 			return matches;		
 		}
 	
 	public void addIsTaken(String quizID){
 		String query = "update users set times_taken = times_taken + 1 where quiz_id = " + quizID;
 		sqlUpdate(query);
 	}
 	
 	public ArrayList<String> getFriends(String userId){
 		String query = "SELECT id2 FROM friends WHERE id1 = '" + userId + "';";
 		ArrayList<String> list = new ArrayList<String>();
 		ResultSet rs = getResult(query);
 		
 		try {
 			while (rs.next()){
 				list.add(rs.getString("id2"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		query = "SELECT id1 FROM friends WHERE id2 = '" + userId + "';";
 		rs = getResult(query);
 		
 		try {
 			while (rs.next()){
 				list.add(rs.getString("id1"));
 			}
 		} catch (SQLException e) {
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
 		ResultSet rs = getResult(query);
 		
 		// add each result to the history
 		try{
 			while(rs.next()) {
 				String quiz = rs.getString("quiz");
 				int time = rs.getInt("time");
 				int questions = rs.getInt("questions");
 				int	correct = rs.getInt("correct");
 				String date = rs.getString("date");
 				Result r = new Result(quiz, userId, time, questions, correct, date);
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
 	}
 	
 	public boolean getIsAdmin(String userId){
 		String query = "SELECT isAdmin FROM users WHERE id = '" + userId + "';";
 		ResultSet rs = getResult(query);
 		boolean isAdmin = false;
 		try {
 			rs.absolute(1);
 			isAdmin = rs.getBoolean("isAdmin");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return isAdmin;
 	}
 	
 	public ArrayList<Achievement> getAchievements(String userId){
 		ArrayList<Achievement> list = new ArrayList<Achievement>();
 		
 		// get all entries in the achievements table for this user
 		String query = "SELECT * FROM achievements WHERE user = '" + userId + "'";
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
 		String query = "SELECT * FROM challenges WHERE dest ='" + userId + "'";
 		ArrayList<Challenge> challenges = new ArrayList<Challenge>();
 		ResultSet rs = getResult(query);
 
 		try {			
 			while(rs.next()) {
 				String src = rs.getString("source");
 				String body = rs.getString("text");
 				String quizId = rs.getString("quiz_id");
 				String time = rs.getString("time");
 				int score = rs.getInt("score");
 				Challenge c = new Challenge(src, userId, body, quizId, time, score);
 				challenges.add(c);
 			}
 		} catch (SQLException e) {e.printStackTrace();}
 		
 		return challenges;
 	}
 	
 	public ArrayList<Message> getNotes(String userId){
 		ArrayList<Message> returnList = new ArrayList<Message>();
 		String query = "select * from notes where dest = '" + userId  + "'";
 		ResultSet rs = getResult(query);
 		
 		
 		try {
 			while(rs.next()){
 				returnList.add(new Message(rs.getString("source"), rs.getString("dest"), rs.getString("text"), rs.getString("date")));
 			}
 		} catch (SQLException e) {e.printStackTrace();}
 		return returnList;
 		
 	}
 	
 	public void addFriend(String user1, String user2){
 		String query = "INSERT INTO friends VALUES('" + user1 + "', '" + user2 + "');";
 		sqlUpdate(query);
 		
 		query = "UPDATE requests SET isConfirmed = true WHERE source = '" + user2 + "' AND dest = '" + user1 + "';";
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
 		ResultSet rs = getResult(query);
 		
 		try {
 			while (rs.next()){
 				String source = rs.getString("source");
 				boolean isConfirmed = rs.getBoolean("isConfirmed");
 				String time = rs.getString("time");
 				String body = source + " has added you as a friend!";
 				FriendRequest fr = new FriendRequest(source, id, body, isConfirmed, time);
 				list.add(fr);
 			}
 		} catch (SQLException e) {
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
 		int stat = status == true ? 1 : 0;
 		String query = "UPDATE users SET isAdmin = '" + stat + "' WHERE id = '" + id + "'";
 		sqlUpdate(query);		
 	}
 
 	public void sendMessage(Message message) {
 		String src = message.getSrc();
 		String dest = message.getDest();
 		String body = message.getBody();
 		String time = message.getTime();
 		String query = null;
 		if(message instanceof Challenge){
 			String quizId = ((Challenge)message).getQuiz();
 			int score = (int)((Challenge)message).getScore();
 			query = "INSERT INTO challenges VALUES('" + src + "', '" + dest + "', '" + score + "', '" + time + "', '" + body + "', '" + quizId + "')";
 		} else {
 			query = "INSERT INTO notes VALUES('" + src + "', '" + dest + "', '" + body + "', '" + time + "')";
 		}
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
 	public void removeUser(String username){
 		String id = username;
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
 				createdQuizzes.add(new Quiz(rs.getString("quiz_id"), rs.getString("creator_id"), rs.getString("date_created"), Boolean.parseBoolean(rs.getString("is_random")), Boolean.parseBoolean(rs.getString("is_one_page")), Boolean.parseBoolean(rs.getString("is_immediate")), rs.getString("image_url"), rs.getString("description")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return createdQuizzes;
 	}
 	
 	/*gets the entire number of results for a user*/
 	public ArrayList<Result> getResults(String userID, int limit){
 		String query = "select * from results where user = '" + userID + "'limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Result> results = new ArrayList<Result>();
 		try {
 			rs.beforeFirst();
 			while(rs.next()){
 				results.add(new Result(rs.getString("quiz"), rs.getString("user"), Integer.parseInt(rs.getString("time")), Integer.parseInt(rs.getString("questions")), Integer.parseInt(rs.getString("correct")), rs.getString("date")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return results;
 	}
 	
 	// gets the specific result set for a quiz
 	public ArrayList<Result> getQuizResults(String quizId, String userId){
 		String query = "select * from results where user = '" + userId + "' AND quiz = '" + quizId + "'";
 		ResultSet rs = getResult(query);
 		ArrayList<Result> results = new ArrayList<Result>();
 		
 		try {
 			while(rs.next()){
 				results.add(new Result(rs.getString("quiz"), rs.getString("user"), Integer.parseInt(rs.getString("time")), Integer.parseInt(rs.getString("questions")), Integer.parseInt(rs.getString("correct")), rs.getString("date")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return results;
 	}
 	
 	/*Gets the top n results for a quiz*/
 	public ArrayList<Result> getTopResults(String quizID, int limit){
 		String query = "select * from results where quiz = '" + quizID + "' order by correct desc, time asc limit " + limit;
 		ResultSet rs = getResult(query);
 		ArrayList<Result> results = new ArrayList<Result>();
 		try {
 			rs.beforeFirst();
 			while(rs.next()){
 				results.add(new Result(rs.getString("quiz"), rs.getString("user"), Integer.parseInt(rs.getString("time")), Integer.parseInt(rs.getString("questions")), Integer.parseInt(rs.getString("correct")), rs.getString("date")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return results;
 	}
 	
 	public void postAnnouncement(String message){
 		String query = "INSERT INTO announcements VALUES('" + message + "', '" + new Date().toString() + "');";
 		sqlUpdate(query);
 	}
 	
 	public void removeAnnouncement(String message){
 		String query = "DELETE FROM announcements WHERE text = '" + message + "';";
 		sqlUpdate(query);
 	}
 	
 	public ArrayList<Announcement> getAnnouncements(){
 		ArrayList<Announcement> list = new ArrayList<Announcement>();
 		String query = "select * from announcements";		
 		ResultSet rs = getResult(query);
 		
 		try {
 			while(rs.next()){
 				list.add(new Announcement(rs.getString("text"), rs.getString("date")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return list;
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
 		String imageURL = quiz.getImageURL();
 		String description = quiz.getDescription();
 		String query = "INSERT INTO quizzes VALUES('" + id + "', '" + date + "', '" + creatorId + "', " 
 				+ numQuestions + ", " + isRandom + ", " + isOnePage + ", " + isImmediate + ", " 
 				+ numTimesTaken + ", '" + imageURL + "', '" + description + "');";
 		sqlUpdate(query);
 		
 		// TODO!!!! add each question as well
 		ArrayList<Question> questions = quiz.getQuestions();
 		for(Question q : questions){
 			int questionNum = q.getNumber();
 			/*ArrayList<String> answers = q.getAnswers();
 			for (String a : answers){
 				query = "INSERT INTO answers VALUES('" + id + "', '" + questionNum + "', '" + a + "');";
 			}*/
 			if(q instanceof MultipleChoice){
 				ArrayList<String> answers = q.getAnswers();
 				query = "INSERT INTO multiple_choice VALUES('" + id + "', '" + questionNum + "', '" + q.getQuestion() + "', '" + answers.get(0) + "', '" + answers.get(1) + "', '" + answers.get(2) + "', '" + answers.get(3) + "')";
 				sqlUpdate(query);
 				query = "INSERT INTO answers VALUES('" + id + "', " + questionNum + ", '" + ((MultipleChoice) q).getCorrectAnswer() + "');";
 				sqlUpdate(query);
 			} else if(q instanceof QuestionResponse){
 				query = "INSERT INTO question_response VALUES('" + id + "', '" + questionNum + "', '" + q.getQuestion() + "')";
 				sqlUpdate(query);
 				ArrayList<String> answers = q.getAnswers();
 				for (String a : answers){
 					query = "INSERT INTO answers VALUES('" + id + "', '" + questionNum + "', '" + a + "');";
 					sqlUpdate(query);
 				}
 			} else if(q instanceof Picture){
 				query = "INSERT INTO picture VALUES('" + id + "', '" + questionNum + "', '" + q.getQuestion() + "', '" + ((Picture) q).getUrl() + "')";
 				sqlUpdate(query);
 				ArrayList<String> answers = q.getAnswers();
 				for (String a : answers){
 					query = "INSERT INTO answers VALUES('" + id + "', '" + questionNum + "', '" + a + "');";
 					sqlUpdate(query);
 				}
 			} else if(q instanceof FillInBlank){
 				ArrayList<String> questionsArray = ((FillInBlank) q).getQuestions();
 				query = "INSERT INTO fill_in_the_blank VALUES('" + id + "', '" + questionNum + "', '" + questionsArray.get(0) + "', '" + questionsArray.get(1) + "')";
 				sqlUpdate(query);
 				ArrayList<String> answers = q.getAnswers();
 				for (String a : answers){
 					query = "INSERT INTO answers VALUES('" + id + "', '" + questionNum + "', '" + a + "');";
 					sqlUpdate(query);
 				}
 			} 
 		}
 	}
 	
 	public Quiz getQuiz(String quizId){
 		
 		//extracts quiz info from quizzes table
 		String query = "SELECT * FROM quizzes WHERE quiz_id = '" + quizId + "';";
 		ResultSet rs = getResult(query);
 		String creatorId = "";
 		String dateCreated = "";
 		boolean isRandom = false;
 		boolean isOnePage = false;
 		boolean isImmediate = false;
 		String imageURL = "";
 		String description = "";
 		try {
 			rs.first();
 			creatorId = rs.getString("creator_id");
 			dateCreated = rs.getString("date_created");
 			isRandom = rs.getBoolean("is_random");
 			isOnePage = rs.getBoolean("is_one_page");
 			isImmediate = rs.getBoolean("is_immediate");
 			imageURL = rs.getString("image_url");
 			description = rs.getString("description");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Quiz quiz = new Quiz(quizId, creatorId, dateCreated, isRandom, isOnePage, isImmediate, imageURL, description);
 		
 		//extracts info from fill_in_the_blank table, building each question with info from 
 		//answers as well
 		query = "SELECT * FROM fill_in_the_blank WHERE quiz_id = '" + quizId + "';";
 		rs = getResult(query);
 		try {
 			rs.beforeFirst();
 			while (rs.next()){
 				ArrayList<String> questions = new ArrayList<String>();
 				int questionNum = rs.getInt("question_num");
 				questions.add(0, rs.getString("question_one"));
 				questions.add(1, rs.getString("question_two"));
 				String questionQuery = "SELECT * FROM answers WHERE quiz_id = '" + quizId + "' AND question_num = " + questionNum + ";";
 				ResultSet questionRS = getResult(questionQuery);
 				ArrayList<String> answers = new ArrayList<String>();
 				questionRS.beforeFirst();
 				while (questionRS.next()){
 					answers.add(questionRS.getString("answer"));
 				}
 				FillInBlank FIB = new FillInBlank(questions, answers, questionNum);
 				quiz.addQuestion(FIB);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		//extracts info from question_response table, building each question with info from 
 		//answers as well
 		query = "SELECT * FROM question_response WHERE quiz_id = '" + quizId + "';";
 		rs = getResult(query);
 		try {
 			rs.beforeFirst();
 			while (rs.next()){
 				int questionNum = rs.getInt("question_num");
 				String question = rs.getString("question");
 				String questionQuery = "SELECT * FROM answers WHERE quiz_id = '" + quizId + "' AND question_num = " + questionNum + ";";
 				ResultSet questionRS = getResult(questionQuery);
 				ArrayList<String> answers = new ArrayList<String>();
 				questionRS.beforeFirst();
 				while (questionRS.next()){
 					answers.add(questionRS.getString("answer"));
 				}
 				QuestionResponse QR = new QuestionResponse(question, answers, questionNum);
 				quiz.addQuestion(QR);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		//extracts info from picture table, building each question with info from 
 		//answers as well
 		query = "SELECT * FROM picture WHERE quiz_id = '" + quizId + "';";
 		rs = getResult(query);
 		try {
 			rs.beforeFirst();
 			while (rs.next()){
 				int questionNum = rs.getInt("question_num");
 				String question = rs.getString("question");
 				String url = rs.getString("url");
 				String questionQuery = "SELECT * FROM answers WHERE quiz_id = '" + quizId + "' AND question_num = " + questionNum + ";";
 				ResultSet questionRS = getResult(questionQuery);
 				ArrayList<String> answers = new ArrayList<String>();
 				questionRS.beforeFirst();
 				while (questionRS.next()){
 					answers.add(questionRS.getString("answer"));
 				}
 				Picture P = new Picture(url, question, answers, questionNum);
 				quiz.addQuestion(P);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		//extracts info from multiple_choice table, building each question with info from 
 		//answers as well
 		query = "SELECT * FROM multiple_choice WHERE quiz_id = '" + quizId + "';";
 		rs = getResult(query);
 		try {
 			rs.beforeFirst();
 			while (rs.next()){
 				int questionNum = rs.getInt("question_num");
 				String question = rs.getString("question");
 				ArrayList<String> answers = new ArrayList<String>();
 				answers.add(0, rs.getString("a"));
 				answers.add(1, rs.getString("b"));
 				answers.add(2, rs.getString("c"));
 				answers.add(3, rs.getString("d"));
 				String questionQuery = "SELECT * FROM answers WHERE quiz_id = '" + quizId + "' AND question_num = " + questionNum + ";";
 				ResultSet questionRS = getResult(questionQuery);
 				questionRS.first();
 				String correctAnswer = questionRS.getString("answer");
 				
 				MultipleChoice MC = new MultipleChoice(question, answers, correctAnswer, questionNum);
 				quiz.addQuestion(MC);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	return quiz;
 	}
 	
 	/*public void incrementQuizTimesTaken(Quiz quiz){
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
 	}*/
 	
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
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 	
 }
