 package quizweb;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.*;
 
 import quizweb.database.DBConnection;
 import quizweb.question.*;
 import quizweb.quiz.QuizSummary;
 import quizweb.record.*;
 
 public class Quiz {
 	public int quizID;
 	public String name;
 	public String quizURL;	
 	public String description;
 	public String category;
 	public User creator; 
 	
 	public boolean isRandom;
 	public boolean opFeedback;
 	public boolean opPractice;	
 	
 	// Statistics
 	public int raterNumber;
 	public double totalRating;
 	
 	public static final String DBTable = "quiz";
 	
 	// Creation constructor
 	public Quiz(String name, String quizURL, String description, String category,
 			int userID, boolean isRandom, boolean opFeedback, boolean opPractice) {
 		this.name= name;
 		this.quizURL = quizURL;
 		this.description = description;
 		this.category = category;
 		this.creator = User.getUserByUserID(userID);
 		this.isRandom = isRandom;
 		this.opFeedback = opFeedback;
 		this.opPractice = opPractice;
 		this.raterNumber = 0;
 		this.totalRating = 0;
 		addQuizToDB();
 	}
 	
 	// Reference constructor
 	public Quiz(int quizID, String name, String quizURL, String description, String category,
 			int userID, boolean isRandom, boolean opFeedback, boolean opPractice, 
 			int raterNumber, double totalRating) {
 		this.quizID = quizID;
 		this.name= name;
 		this.quizURL = quizURL;
 		this.description = description;
 		this.category = category;
 		this.creator = User.getUserByUserID(userID);
 		this.isRandom = isRandom;
 		this.opFeedback = opFeedback;
 		this.opPractice = opPractice;
 		this.raterNumber = raterNumber;
 		this.totalRating = totalRating;		
 	}
 	
 	public void addQuizToDB() {
 		try {
 			String statement = new String("INSERT INTO " + DBTable 
 					+ " (name, url, description, category, userid, israndom, opfeedback, oppractice, raternumber, rating)" 
 					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement, new String[] {"qid"});
 			stmt.setString(1, name);
 			stmt.setString(2, quizURL);
 			stmt.setString(3, description);
 			stmt.setString(4, category);
 			stmt.setInt(5, creator.userID);
 			stmt.setBoolean(6, isRandom);
 			stmt.setBoolean(7, opFeedback);
 			stmt.setBoolean(8, opPractice);
 			stmt.setInt(9, raterNumber);
 			stmt.setDouble(10, totalRating);			
 			stmt.executeUpdate();
 			ResultSet rs = stmt.getGeneratedKeys();
 			rs.next();
			quizID = rs.getInt("qid");
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public static Quiz getQuizByQuizID(int quizID) {
 		String statement = new String("SELECT * FROM " + DBTable + " WHERE qid = ?");
 		PreparedStatement stmt;
 		try {
 			stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, quizID);
 			ResultSet rs = stmt.executeQuery();
 			rs.next();
 			Quiz quiz = new Quiz(rs.getInt("qid"), rs.getString("name"), rs.getString("url"), 
 					rs.getString("description"), rs.getString("category"), rs.getInt("userid"), 
 					rs.getBoolean("israndom"), rs.getBoolean("opfeedback"), rs.getBoolean("oppractice"), 
 					rs.getInt("raternumber"), rs.getDouble("rating"));
 			rs.close();
 			return quiz;			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	// Update current quiz record in database;
 	public void updateCurrentQuiz() {
 		try {
 			String statement = new String("UPDATE " + DBTable + " SET "
 					+ "name=?, url=?, description=?, category=?, userid=?, israndom=?, opfeedback=?, oppractice=?, raternumber=?, rating=?"
 					+ " WHERE qid=?");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setString(1, name);
 			stmt.setString(2, quizURL);
 			stmt.setString(3, description);
 			stmt.setString(4, category);
 			stmt.setInt(5, creator.userID);
 			stmt.setBoolean(6, isRandom);
 			stmt.setBoolean(7, opFeedback);
 			stmt.setBoolean(8, opPractice);
 			stmt.setInt(9, raterNumber);
 			stmt.setDouble(10, totalRating);	
 			stmt.setInt(11, quizID);
 			stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	public ArrayList<Question> getQuestions() {
 		return Question.getQuestionsByQuizID(quizID);
 	}
 	
 	public ArrayList<QuizTakenRecord> getAllHistory() {
 		ArrayList<QuizTakenRecord> records = QuizTakenRecord.getQuizHistoryByQuizID(quizID, new Timestamp(0));
 		Collections.sort(records, new QuizTakenRecordSortByTime());
 		return records;
 	}
 	
 	public ArrayList<QuizTakenRecord> getUserHistory(User user) {
 		ArrayList<QuizTakenRecord> records = QuizTakenRecord.getQuizHistoryByQuizIDUserID(quizID, user.userID);
 		Collections.sort(records, new QuizTakenRecordSortByTime());
 		return records;
 	}
 	
 
 	public ArrayList<QuizTakenRecord> getAllTopRecord() {
 		ArrayList<QuizTakenRecord> records = QuizTakenRecord.getQuizHistoryByQuizID(quizID, new Timestamp(0));
 		Collections.sort(records, new QuizTakenRecordSortByQuality());
 		return records;
 	}
 	
 	public ArrayList<QuizTakenRecord> getAllTopRecord(Timestamp time) {
 		ArrayList<QuizTakenRecord> records = QuizTakenRecord.getQuizHistoryByQuizID(quizID, time);
 		Collections.sort(records, new QuizTakenRecordSortByQuality());
 		return records;
 	}
 	
 	public ArrayList<QuizTakenRecord> getUserTopRecord(User user) {
 		ArrayList<QuizTakenRecord> records = QuizTakenRecord.getQuizHistoryByQuizIDUserID(quizID, user.userID);
 		Collections.sort(records, new QuizTakenRecordSortByQuality());
 		return records;
 	}	
 	
 	
 	// Summary statistics
 	public QuizSummary computeSummaryStats() {
 		return new QuizSummary(this);
 	}
 
 	// Score
 	public double getTotalScore() {
 		double sum = 0;
 		ArrayList<Question> questions = getQuestions();
 		for (int i = 0; i < questions.size(); i++) {
 			sum += questions.get(i).score;
 		}
 		return sum;
 	}
 	
 	/**
 	 * Compute the score of the quiz
 	 * @return score
 	 */
 	public double getScore(ArrayList<Object> userAnswers) {
 		double sum = 0;
 		ArrayList<Question> questions = getQuestions();
 		for (int i = 0; i < questions.size(); i++) {
 			sum += questions.get(i).getScore(userAnswers.get(i));
 		}
 		return sum;
 	}	
 	
 	public double getBestScore() {
 		ArrayList<QuizTakenRecord> records = getAllTopRecord();
 		if (!records.isEmpty()) 
 			return records.get(0).score;
 		else 
 			return 0;
 	}
 	
 	/**
 	 * Get best score for a given user
 	 * @param user
 	 * @return
 	 */
 	public double getUserBestScore(User user) {
 		ArrayList<QuizTakenRecord> records = getUserTopRecord(user);
 		double bestVal = 0;
 		for (int i = 0; i < records.size(); i++) 
 			if (bestVal < records.get(i).score)
 				bestVal = records.get(i).score;
 		return bestVal;
 	}
 	
 	// Rating
 	public void addQuizRating(double rating) {
 		try {
 			String statement = new String("UPDATE " + DBTable + " SET "
 					+ "raternumber=raternumber+1, rating=rating+?"
 					+ " WHERE qid=?");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setDouble(1, rating);
 			stmt.setInt(2, quizID);
 			stmt.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}			
 	}
 	
 	public double getQuizRating() {
 		String statement = new String("SELECT * FROM " + DBTable + " WHERE qid = ?");
 		PreparedStatement stmt;
 		try {
 			stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, quizID);
 			ResultSet rs = stmt.executeQuery();
 			rs.next();
 			double averageRating = rs.getDouble("rating") / rs.getInt("raternumber");
 			rs.close();
 			return averageRating;		
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 }
