 package quizweb;
 
 import java.sql.*;
 import java.util.*;
 
 import quizweb.database.*;
 import quizweb.question.*;
 import quizweb.quiz.*;
 import quizweb.record.*;
 
 public class Quiz {
 	public int quizID;
 	public String name;
 	public String quizURL;	
 	public String description;
 	public String category;
 	public User creator; 
 	
 	public boolean isRandom;
 	public boolean isOnepage;
 	public boolean opFeedback;
 	public boolean opPractice;	
 	
 	// Statistics
 	public int raterNumber;
 	public double totalRating;
 	
 	public static final String DBTable = "quiz";
 	
 	// Creation constructor
 	public Quiz(String name, String quizURL, String description, String category,
 			int userID, boolean isRandom, boolean isOnepage, boolean opFeedback, boolean opPractice) {
 		this.name= name;
 		this.quizURL = quizURL;
 		this.description = description;
 		this.category = category;
 		this.creator = User.getUserByUserID(userID);
 		this.isRandom = isRandom;
 		this.isOnepage = isOnepage;
 		this.opFeedback = opFeedback;
 		this.opPractice = opPractice;
 		this.raterNumber = 0;
 		this.totalRating = 0;
 	}
 	
 	// Reference constructor
 	public Quiz(int quizID, String name, String quizURL, String description, String category,
 			int userID, boolean isRandom, boolean isOnepage, boolean opFeedback, boolean opPractice, 
 			int raterNumber, double totalRating) {
 		this.quizID = quizID;
 		this.name= name;
 		this.quizURL = quizURL;
 		this.description = description;
 		this.category = category;
 		this.creator = User.getUserByUserID(userID);
 		this.isRandom = isRandom;
 		this.isOnepage = isOnepage;
 		this.opFeedback = opFeedback;
 		this.opPractice = opPractice;
 		this.raterNumber = raterNumber;
 		this.totalRating = totalRating;		
 	}
 	
 	public void addQuizToDB() {
 		try {
 			String statement = new String("INSERT INTO " + DBTable 
 					+ " (name, url, description, category, userid, israndom, isonepage, opfeedback, oppractice, raternumber, rating)" 
 					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement, new String[] {"qid"});
 			stmt.setString(1, name);
 			stmt.setString(2, quizURL);
 			stmt.setString(3, description);
 			stmt.setString(4, category);
 			stmt.setInt(5, creator.userID);
 			stmt.setBoolean(6, isRandom);
 			stmt.setBoolean(7, isOnepage);
 			stmt.setBoolean(8, opFeedback);
 			stmt.setBoolean(9, opPractice);
 			stmt.setInt(10, raterNumber);
 			stmt.setDouble(11, totalRating);			
 			stmt.executeUpdate();
 			ResultSet rs = stmt.getGeneratedKeys();
 			rs.next();
 			quizID = rs.getInt("GENERATED_KEY");
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
 			if (!rs.next()) {
 				System.out.println("Quiz " + quizID + " NOT FOUND");
 				return null;
 			}
 			Quiz quiz = new Quiz(rs.getInt("qid"), rs.getString("name"), rs.getString("url"), 
 					rs.getString("description"), rs.getString("category"), rs.getInt("userid"), 
 					rs.getBoolean("israndom"), rs.getBoolean("isonepage"), rs.getBoolean("opfeedback"), rs.getBoolean("oppractice"), 
 					rs.getInt("raternumber"), rs.getDouble("rating"));
 			rs.close();
 			return quiz;			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static Quiz getQuizByQuizName(String quizName) {
 		quizName = quizName.trim();
 		String statement = new String("SELECT * FROM " + DBTable + " WHERE name = ?");
 		PreparedStatement stmt;
 		try {
 			stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setString(1, quizName);
 			ResultSet rs = stmt.executeQuery();
 			if (!rs.next()) {
 				System.out.println("Quiz " + quizName + " NOT FOUND");
 				return null;
 			}
 			Quiz quiz = new Quiz(rs.getInt("qid"), rs.getString("name"), rs.getString("url"), 
 					rs.getString("description"), rs.getString("category"), rs.getInt("userid"), 
 					rs.getBoolean("israndom"), rs.getBoolean("isonepage"), rs.getBoolean("opfeedback"), rs.getBoolean("oppractice"), 
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
 					+ "name=?, url=?, description=?, category=?, userid=?, israndom=?, isonepage=?, opfeedback=?, oppractice=?, raternumber=?, rating=?"
 					+ " WHERE qid=?");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setString(1, name);
 			stmt.setString(2, quizURL);
 			stmt.setString(3, description);
 			stmt.setString(4, category);
 			stmt.setInt(5, creator.userID);
 			stmt.setBoolean(6, isRandom);
 			stmt.setBoolean(7, isOnepage);
 			stmt.setBoolean(8, opFeedback);
 			stmt.setBoolean(9, opPractice);
 			stmt.setInt(10, raterNumber);
 			stmt.setDouble(11, totalRating);	
 			stmt.setInt(12, quizID);
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
 	
 	public static ArrayList<Quiz> getRecentQuiz() {
 		ArrayList<QuizCreatedRecord> records = QuizCreatedRecord.getRecentQuizCreatedRecord();
 		ArrayList<Quiz> quizList = new ArrayList<Quiz>();
 		for (int i = 0; i < records.size(); i++) {
 			quizList.add(records.get(i).quiz);
 		}
 		return quizList;
 	}
 	
 	public static ArrayList<Quiz> getPopularQuiz() {
 		ArrayList<QuizCreatedRecord> records = QuizCreatedRecord.getRecentQuizCreatedRecord();
 		ArrayList<Quiz> quizList = new ArrayList<Quiz>();
 		for (int i = 0; i < records.size(); i++) {
 			quizList.add(records.get(i).quiz);
 		}
 		Collections.sort(quizList, new QuizSortByRating());
 		return quizList;		
 	}
 	
 	
 	// Summary statistics
 	public QuizSummary getQuizSummary() {
 		return new QuizSummary(this);
 	}
 	
 	public QuizSummary getQuizSummary(User user) {
 		return new QuizSummary(this, user);
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
 		raterNumber++;
 		totalRating += rating;
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
 			double averageRating;
 			if(rs.getInt("raternumber")==0){
 				averageRating = 0;
 			}else{
 				averageRating = rs.getDouble("rating") / rs.getInt("raternumber");
 			}
 			rs.close();
 			return averageRating;		
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	public String getQuizStringWithURL() {
 		StringBuilder sb = new StringBuilder();
		sb.append("<a class=\"link-style-dominant\" href=\"quiz_summary.jsp?id=");
 		sb.append(quizID + "\">" + name + "</a>");
 		return sb.toString();
 	}
 
 
 	public static Quiz getQuizByXMLElem(XMLElement root, ArrayList<XMLElement> questionElems) {
 		String name = new String("Quiz Name Missing");
 		String description = new String("Quiz Description Missing");
 		String category = new String("Uncategorized");
 		int userID = -1;
 		boolean isRandom = false;
 		boolean isOnepage = false;
 		boolean opFeedback = false;
 		boolean opPractice = false;
 		if (root.attributeMap.containsKey("random") && root.attributeMap.get("random").equals("true")) 
 			isRandom = true;
 		if (root.attributeMap.containsKey("one-page") && root.attributeMap.get("one-page").equals("true")) 
 			isOnepage = true;	
 		if (root.attributeMap.containsKey("feedback-mode") && root.attributeMap.get("feedback-mode").equals("true")) 
 			opFeedback = true;				
 		if (root.attributeMap.containsKey("practice-mode") && root.attributeMap.get("practice-mode").equals("true")) 
 			opPractice = true;
 			
 		for (int i = 0; i < root.childList.size(); i++) {
 			XMLElement elem = root.childList.get(i);
 			if (elem.name.equals("title")) {
 				name = elem.content;
 			} else if (elem.name.equals("description")) {
 				description = elem.content;
 			} else if (elem.name.equals("category")) {
 				category = elem.content;
 			} else if (elem.name.equals("author")) {
 				User user = User.getUserByUsername(elem.content);
 				if (user != null)
 					userID = user.userID;
 			} else if (elem.name.equals("question")) {
 				questionElems.add(elem);
 			} else {
 				System.out.println("Unknown Quiz XML field " + elem.name);
 			}
 		}
 		String quizURL = new String("URL NOT IMPLEMENTED"); // TODO fill in the quizURL
 		if (userID == -1) {
 			System.out.println("Author for quiz " + name + " must be specified!");
 		}
 		Quiz quiz = new Quiz(name, quizURL, description, category, userID, isRandom, isOnepage, opFeedback, opPractice);		
 		return quiz;
 	}
 
 
 
 }
