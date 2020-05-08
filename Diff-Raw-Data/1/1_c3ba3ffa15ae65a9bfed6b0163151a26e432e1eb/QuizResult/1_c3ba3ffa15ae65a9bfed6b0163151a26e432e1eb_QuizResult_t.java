 package site;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 
 
 
 /** QuizResult is a class that handles all SQL access to the results database
  * Which is the table of all quiz results. 
  * 
  * */
 public class QuizResult {
 	/***/
 	private static final String RESULT_DATABASE = "results";	
 
 	private static Connection con;
 	private static Statement stmt; 
 
 	public QuizResult(){
 		con = MyDB.getConnection();
 		try {
 			stmt = con.createStatement();
 		} catch (SQLException e) {
 
 		}
 	}
 	/**Adds a result to the SQL results database, either by passing in a bunch of parameters
 	 * Or first generating a Result and passing it in. 
 	 * 
 	 * The first type takes care of the timestamp and other things by itself
 	 * 
 	 * How do you add dates to an sql?
 	 * These are not functional yet, because I don't know how dates work yet
 	 * 
 	 * */
 	public static void addResult(int quizTakerId, int quizId, int pointsScored, int maxPossiblePoints, long duration ){
 		String execution = "INSERT INTO " + RESULT_DATABASE + " VALUES('"+quizTakerId+
 				"', '"+quizId+"', '"+pointsScored+"', '"+maxPossiblePoints+"', '"+duration;  
 		try {
 			stmt.executeUpdate(execution);
 		} catch (SQLException e) {
 		}
 	}
 
 	public static void addResult(Result r){
 		String execution = "INSERT INTO " + RESULT_DATABASE + " VALUES('"+r.userId+"', '"+r.resultId+
 				"', '"+r.quizId+"', '"+r.pointsScored+"', '"+r.maxPossiblePoints+"', '"+r.durationOfQuiz+"', '"+r.timeStamp+"'";  
 		try {
 			stmt.executeUpdate(execution);
 		} catch (SQLException e) {
 		}
 	}
 
 	/**Call to see if the quizresult database is empty
 	 * True if empty, false if not
 	 * */
 	public static boolean isEmpty(){
 		try {
 			ResultSet set = stmt.executeQuery("SELECT * FROM " + RESULT_DATABASE);
 			if (!set.next()){
 				return true;
 			}
 			return false;
 		} catch (SQLException e) {
 		}
 		return true;
 	}
 
 	/**Generates a Result from a given ResultSet set to a given row
 	 * Remember that ResultSets start at index 1
 	 * */
 	private static Result generateResult(ResultSet set, int row){
 		Result result = null;
 
 		try{
 			set.absolute(row);
 			int taker = set.getInt(1);
 			int rsId = set.getInt(2);
 			int quiz = set.getInt(3);
 			int score = set.getInt(4);
 			int mxScore = set.getInt(5); 
 			long dur =set.getLong(6);
 			Timestamp dt = set.getTimestamp(7);
 			result = new Result(taker, rsId, quiz, score, mxScore, dt, dur);
 		} catch (SQLException ignored){
 
 		}
 		return result;
 	}
 
 	/**Give it a sql string and this will return an ArrayList*/
 	private static ArrayList<Result> generateList(String execution){
 		ArrayList<Result> results = new ArrayList<Result>();
 		ResultSet set;
 		try {
 			set = stmt.executeQuery(execution);
 			while(set.next()){
 				results.add(generateResult(set, set.getRow()));
 			}
 		} catch (SQLException e) {}
 		return results;
 	}
 
 	/**Returns the most recent quiz a user has taken. Hopefully used for 
 	 * the Quiz Results Page
 	 * 
 	 * @param userID int
 	 * @param quizID int
 	 * */
 	public static Result getLastQuiz(int userID, int quizID){
 		Result rs = null;
 		return rs;
 	}
 
 	/**Returns a Result object given an ID
 	 * 
 	 * @param resultID integer ID of interest
 	 * */
 	public static Result getResultFromID(int resultID){
 		Result rs = null;
 		String ID = Integer.toString(resultID);
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE result_id= '" +ID+ "'";  
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			int taker = set.getInt(1);
 			int rsId = set.getInt(2);
 			int quiz = set.getInt(3);
 			int score = set.getInt(4);
 			int mxScore = set.getInt(5); 
 			long dur =set.getLong(6);
 			Timestamp dt = set.getTimestamp(7);
 			rs = new Result(taker, rsId, quiz, score, mxScore, dt, dur);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rs;
 	}
 
 	/** Returns an ordered set of Results/Strings containing past results that the 
 	 *  user has gotten on all quizzes If an order is provided, the list will be sorted
 	 *  by one of the fields. Returns newest first, highest first, longest first
 	 *  
 	 *  Quiz summary asks for: date, percent correct, by amount of time the quiz took
 	 *  @param userId
 	 *  @param quizID
 	 *  @param order - "BY_DATE" - "BY_SCORE" - "BY_DURATION". If none of the above then orders by date 
 	 */
 	public static ArrayList<Result> getUserPerformances(int userId, String order){
 		ArrayList<Result> results = new ArrayList<Result>();
 		String selectedOrder = "created_timestamp";
 		if (order.equals("BY_SCORE")){
 			String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE user_id = " + userId;
 			results = generateList(execution);
 			Collections.sort(results, new SortByBestScore());
 			return results;
 			//System.out.println(results.toString());
 		}
 		//if (order.equals("BY_DATE")) selectedOrder = "created_timestamp";
 		if (order.equals("BY_DURATION")) selectedOrder = "duration";
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE user_id = " + userId +
 				"  ORDER BY "+ selectedOrder + " DESC";
 		results = generateList(execution);
 		return results;
 	}
 	
 	/** Returns an ordered set of Results/Strings containing past results that the 
 	 *  user has gotten on a quiz. If an order is provided, the list will be sorted
 	 *  by one of the fields. Returns newest first, highest first, longest first
 	 *  
 	 *  Quiz summary asks for: date, percent correct, by amount of time the quiz took
 	 *  @param userId
 	 *  @param quizID
 	 *  @param order - "BY_DATE" - "BY_SCORE" - "BY_DURATION". If none of the above then orders by date 
 	 */
 	public static ArrayList<Result> getUserPerformanceOnQuiz(int userId, int quizID){
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE user_id = " + userId + " AND quiz_id = " + quizID +
 				"  ORDER BY created_timestamp DESC";
 		ArrayList<Result> results = generateList(execution);
 		//		System.out.println(results.toString());
 		return results;
 	}
 
 
 	public static ArrayList<Result> getUserPerformanceOnQuiz(int userId, int quizId, String order){
 		ArrayList<Result> results = new ArrayList<Result>();
 		String selectedOrder = "created_timestamp";
 		if (order.equals("BY_SCORE")){
 			String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE user_id = " + userId + " AND quiz_id = " + quizId;
 			results = generateList(execution);
 			Collections.sort(results, new SortByBestScore());
 			return results;
 			//System.out.println(results.toString());
 		}
 		//if (order.equals("BY_DATE")) selectedOrder = "created_timestamp";
 		if (order.equals("BY_DURATION")) selectedOrder = "duration";
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE user_id = " + userId + " AND quiz_id = " + quizId +
 				"  ORDER BY "+ selectedOrder + " DESC";
 		results = generateList(execution);
 		return results;
 	}
 
 	/**Sort by score first looks at higher score, then shorter duration*/
 	private static class SortByBestScore implements Comparator<Result>{
 		public int compare(Result a, Result b){
 			double scoreA = a.pointsScored / (double) a.maxPossiblePoints;
 			double scoreB = b.pointsScored / (double) b.maxPossiblePoints;
 			if (scoreA > scoreB) return -1;
 			if (scoreA < scoreB) return 1;
 
 			long durA = a.durationOfQuiz;
 			long durB = b.durationOfQuiz;
 			if (durA < durB) return -1;
 			if (durA > durB) return 1;
 			return 0;
 		}
 	}
 	/**Sort by lowest score, then longest duration*/
 	private static class SortByWorstScore implements Comparator<Result>{
 		public int compare(Result a, Result b){
 			double scoreA = a.pointsScored / (double) a.maxPossiblePoints;
 			double scoreB = b.pointsScored / (double) b.maxPossiblePoints;
 			if (scoreA > scoreB) return 1;
 			if (scoreA < scoreB) return -1;
 			long durA = a.durationOfQuiz;
 			long durB = b.durationOfQuiz;
 			if (durA < durB) return 1;
 			if (durA > durB) return -1;
 			return 0;
 		}
 	}
 
 	private static ArrayList<Result> sublist(int indexStart, int indexEnd, ArrayList<Result> result){
 		ArrayList<Result> results = new ArrayList<Result>();
 		for(int i = indexStart; i < indexEnd; i ++){
 			results.add(result.get(i));
 		}
 		return results;
 	}
 
 	/**Because no generics*/
 	private static ArrayList<Quiz> subQlist(int indexStart, int indexEnd, ArrayList<Quiz> result){
 		ArrayList<Quiz> results = new ArrayList<Quiz>();
 		if (indexEnd > results.size()) indexEnd = results.size() - 1;
 		for(int i = indexStart; i < indexEnd; i ++){
 			results.add(result.get(i));
 		}
 		return results;
 	}
 
 	/** Returns a sorted ArrayList of Results for the highest scores for a quiz
 	 * @param quizID integer ID number of quiz
 	 * @param numUsers length of quiz, if zero, return all 
 	 * */
 	public static ArrayList<Result> getBestQuizTakers(int quizID, int numUsers){
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE quiz_id = " + quizID;
 		ArrayList<Result> results = generateList(execution);
 		Collections.sort(results, new SortByBestScore());
 		if (numUsers > 0){
 			return sublist(0, numUsers, results);
 		}
 		//System.out.println(results.toString());
 		return results;
 	}
 
 	/** Returns a sorted ArrayList of Results for the lowers scores for a quiz
 	 * @param quizID integer ID number of quiz
 	 * @param numUsers length of quiz, if zero, return all 
 	 * */
 	public static ArrayList<Result> getWorstQuizTakers(int quizID, int numUsers){
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE quiz_id = " + quizID;
 		ArrayList<Result> results = generateList(execution);
 		Collections.sort(results, new SortByWorstScore());
 		if (numUsers > 0){
 			return sublist(0, numUsers, results);
 		}
 		//System.out.println(results.toString());
 		return results;
 	}
 
 	/** Returns a ArrayList of Results for a quiz in the last day WITH no repeated users.
 	 *  Sorted by date
 	 *  TODO test
 	 * @param quizID integer ID number of quiz
 	 * @param numUsers length of quiz, if zero, return all  
 	 * */
 	public static ArrayList<Result> getRecentTakers(int quizID, int numUsers){
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE quiz_id = " + quizID
 				+ " AND created_timestamp > DATE_SUB(NOW(), INTERVAL 24 HOUR) ORDER BY created_timestamp";
 		ArrayList<Result> results = generateList(execution);
 		if (numUsers > 0){
 			return sublist(0, numUsers, results);
 		}
 		return results;
 	}
 
 	/** Returns a sorted ArrayList of Results for the highest scores for a quiz
 	 * in the last day, WITH repeated users. Sorted by score
 	 * TODO test
 	 * @param quizID integer ID number of quiz
 	 * @param numUsers length of quiz, if zero, return all  
 	 * */
 	public static ArrayList<Result> getRecentHighScores(int quizID, int numUsers){
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE quiz_id = " + quizID +
 				" AND created_timestamp > DATE_SUB(NOW(), INTERVAL 24 HOUR)";
 		ArrayList<Result> results = generateList(execution);
 		Collections.sort(results, new SortByBestScore());
 		if (numUsers > 0){
 			return sublist(0, numUsers, results);
 		}
 		return results;
 	}
 
 	/** Returns a sorted ArrayList of Results for the highest scores for a quiz
 	 * ever 
 	 * TODO test
 	 * @param quizID integer ID number of quiz
 	 * @param numUsers length of quiz, if zero, return all  
 	 * */
 	public static ArrayList<Result> getAllTimeBest(int quizID, int numUsers){
 		String execution = "SELECT * FROM " + RESULT_DATABASE + " WHERE quiz_id = " + quizID;
 		ArrayList<Result> results = generateList(execution);
 		Collections.sort(results, new SortByBestScore());
 		if (numUsers > 0){
 			return sublist(0, numUsers, results);
 		}
 		return results;
 	}
 
 
 	/**Give it a sql string and this will return an ArrayList
 	 * TODO test*/
 	private static ArrayList<Quiz> generateQuizList(String execution) {
 		ArrayList<Quiz> results = new ArrayList<Quiz>();
 		ResultSet set;
 		try {
 			set = stmt.executeQuery(execution);
 			
 			while(set.next()){
 				results.add((new QuizManager()).getQuizByQuizId(set.getInt("quiz_id")));
 			}
 		} catch (SQLException e) {}
 		return results;
 	}
 
 	/**Returns a sorted ArrayList of Quizzes for the most popular quizzes
 	 * TODO test
 	 * @param numQuizzes number of quizzes asked for, if zero, return all 
 	 * */
 	public static ArrayList<Quiz> getPopularQuizzes(int numQuizzes){
 		String execution = "SELECT quiz_id, COUNT(*) from results GROUP by quiz_id" +
 				" ORDER BY COUNT(*) DESC";
 		ArrayList<Quiz> quizzes = generateQuizList(execution);
 		if (numQuizzes > 0){
 			return subQlist(0, numQuizzes, quizzes);
 		}
 		return quizzes;
 	}
 
 
 	/**Returns an ArrayList of quizzes sorted by time taken by a given user
 	 * TODO test
 	 * @param userId id of user
 	 * @param numQuizzes Number of quizzes asked for, if zero, return all
 	 * */
 	public static ArrayList<Quiz> getRecentQuizTakers(int userId, int numQuizzes){
 		String execution = "SELECT * FROM results WHERE user_id = " + userId + " ORDER BY created_timestamp ";
 		ArrayList<Quiz> quizzes = generateQuizList(execution);
 		if (numQuizzes > 0){
 			return subQlist(0, numQuizzes, quizzes);
 		}
 		return quizzes;
 	}
 
 
 	/**Returns an ArrayList of quizzes sorted by time created by a given user
 	 * TODO implement these somewhere that matters
 	 * TODO test
 	 * @param userId id of user
 	 * @param numQuizzes Number of quizzes asked for, if zero, return all
 	 * */
 	public static ArrayList<Quiz> getQuizzesCreatedByUser(int userId, int numQuizzes){
 		return null;
 	}
 
 	/**Returns an ArrayList of quizzes created by anybody, sorted by time 
 	 * TODO move these to somewhere that makes more sense
 	 * TODO test 
 	 * @param numQuizzes Number of quizzes asked for, if zero, return all
 	 * */
 	public static ArrayList<Quiz> getRecentlyCreated(int numQuizzes){
 		return null;
 	}
 
 	/**Returns an ArrayList of quizzes taken by friends of a given user, sorted by time 
 	 * TODO Implement these once friends works
 	 * @param numQuizzes Number of quizzes asked for, if zero, return all
 	 * */
 	public static ArrayList<Quiz> getFriendQuizzes(int userId, int numQuizzes){
 
 		return null;
 	}
 
 	/**Returns the number of quizzes that a given user has taken
 	 * Returns -1 on failure
 	 * TODO test
 	 * @param userId integer ID
 	 * */
 	public static int numTaken(int userId){
 		String execution = "SELECT COUNT(*) from results WHERE user_id = " + userId;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return set.getInt("Count(*)");
 		} catch (SQLException e) {}
 		return -1;
 	}
 
 	public static boolean isBestScoreOnQuiz(int userId, int quizId){
 		ArrayList<Result> results = getBestQuizTakers(quizId, 0);
 		if (results.get(0).userId == userId) return true;
 		return false; 
 	}
 
 	public static boolean isBestScoreOnAnyQuiz(int userId){
 		String execution = "SELECT DISTINCT quiz_id from results";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				int quizId = set.getInt("quiz_id");	
 				ArrayList<Result> results = getBestQuizTakers(quizId, 0);
 				if (results.get(0).userId == userId) return true;
 			}
 			return false; 
 		} catch (SQLException e) {}
 		return false;
 	}
 
 	public static final int NUM_USERS = 0;
 	public static final int NUM_TIMES = 1;
 	public static final int AVG_PERCENT = 2;
 	public static final int AVG_TIME = 3;
 	public static final int NUM_DAY_PLAYS = 4;
 	
 	/** Returns an ArrayList of doubles, with each double representing a different
 	 * statistic 
 	 * 
 	 * Relevant Statistics by Index:
 	 * 
 	 * 0 - Number of users who have taken this quiz
 	 * 1 - Number of times this quiz has been taken
 	 * 2 - Average Percent Correct
 	 * 3 - Average time taken
 	 * 4 - Number of plays within the last day
 	 * 
 	 * @param quizID integer number of quiz
 	 * @return DoubleList of statistics
 	 * */
 	public static ArrayList<Double> getNumericStatistics(int quizID){
 		ArrayList<Double> stats = new ArrayList<Double>();
 		stats.add(numUsersTaken(quizID));
 		stats.add(numTimesTaken(quizID));
 		stats.add(averageCorrect(quizID));
 		stats.add(averageDuration(quizID));
 		stats.add(numPlayInDay(quizID));
 		return stats;
 	}
 
 	private static double numUsersTaken(int quizId){
 		String execution = "SELECT COUNT(*) from results GROUP BY user_id";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.last();
 			return (double) set.getRow();
 		} catch (SQLException e) {}
 		return -1;
 	}
 
 	private static double numTimesTaken(int quizId){
 		String execution = "SELECT COUNT(*) from results WHERE quiz_id = " + quizId;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return set.getDouble("COUNT(*)");
 		} catch (SQLException ignored) {}
 		return -1;
 	}
 
 	private static double averageCorrect(int quizId){
 		String execution = "SELECT AVG(user_score/max_score) FROM results WHERE quiz_id = "+quizId;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return set.getDouble(1);
 		} catch (SQLException ignored) {}		
 		return -1;
 	}
 
 	private static double averageDuration(int quizId){
 		String execution = "SELECT AVG(duration) FROM results WHERE quiz_id = " + quizId;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return (double) set.getLong(1);
 		} catch (SQLException ignored) {}		
 		return -1;
 	}
 
 	private static double numPlayInDay(int quizId){
 		String execution = "SELECT * FROM results WHERE created_timestamp > DATE_SUB(NOW(), INTERVAL 24 HOUR) AND quiz_id = " + quizId;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.last();
 			return (double) set.getRow();
 		} catch (SQLException ignored) {}		
 		return -1;
 	}
 
 	public static final int BEST_SCORE = 0;
 	public static final int WORST_SCORE = 1;
 	public static final int LONGEST_TIME = 2;
 	public static final int SHORTEST_TIME = 3;
 	public static final int RECENT_PLAY = 4;
 	public static final int FIRST_PLAY = 5;
 
 	
 	/** Returns an ArrayList of Results, with each Result representing a different
 	 * statistic 
 	 * 
 	 * Relevant Statistics by Index:
 	 * 0 - Best percent score
 	 * 1 - Worst percent score
 	 * 2 - Longest time taken
 	 * 3 - Shortest time taken
 	 * 4 - Most recent play
 	 * 5 - First date played
 	 * 
 	 * @param quizID integer number of quiz
 	 * @return ResultList of statistics
 	 * */
 	
 	public static ArrayList<Result> getResultStatistics(int quizId){
 		ArrayList<Result> stats = new ArrayList<Result>();
 		stats.add(bestScoreResult(quizId));
 		stats.add(worstScoreResult(quizId));
 		stats.add(longestTimeResult(quizId));
 		stats.add(shortestTimeResult(quizId));
 		stats.add(recentPlayResult(quizId));
 		stats.add(firstPlayResult(quizId));
		if (stats.get(1) == null) return null;
 		return stats;
 	}
 
 	private static Result worstScoreResult(int quizId){
 		String execution = "SELECT * FROM results WHERE quiz_id = " + quizId+ " ORDER BY user_score / max_score";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return generateResult(set, 1);
 		} catch (SQLException ignored) {}
 		return null;
 	}
 
 	private static Result bestScoreResult(int quizId){
 		String execution = "SELECT * FROM results WHERE quiz_id = " + quizId+ " ORDER BY user_score / max_score DESC";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return generateResult(set, 1);
 		} catch (SQLException ignored) {}
 		return null;
 	}
 
 	private static Result longestTimeResult(int quizId){
 		String execution = "SELECT * FROM results WHERE quiz_id = " + quizId+ " ORDER BY duration DESC";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return generateResult(set, 1);
 		} catch (SQLException ignored) {}
 		return null;
 	}
 
 	private static Result shortestTimeResult(int quizId){
 		String execution = "SELECT * FROM results WHERE quiz_id = " + quizId+ " ORDER BY duration";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return generateResult(set, 1);
 		} catch (SQLException ignored) {}
 		return null;
 	}
 
 	private static Result firstPlayResult(int quizId){
 		String execution = "SELECT * FROM results WHERE quiz_id = " + quizId+ " ORDER BY created_timestamp";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return generateResult(set, 1);
 		} catch (SQLException ignored) {}
 		return null;
 	}
 
 	private static Result recentPlayResult(int quizId){
 		String execution = "SELECT * FROM results WHERE quiz_id = " + quizId+ " ORDER BY created_timestamp DESC";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return generateResult(set, 1);
 		} catch (SQLException ignored) {}
 		return null;
 	}
 
 }
