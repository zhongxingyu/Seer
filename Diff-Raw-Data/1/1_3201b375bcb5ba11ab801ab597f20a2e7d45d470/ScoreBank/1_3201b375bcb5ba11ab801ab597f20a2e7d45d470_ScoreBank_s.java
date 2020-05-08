 package database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import quiz.Quiz;
 import quiz.Score;
 
 import user.User;
 
 
 public class ScoreBank {
 
 	private DBConnection connection;    //Singleton DBConnection instance
 
 	
 	/*
 	 * Constructor for the ScoreBank class. Sets DBConncetion i-var
 	 * for later use.
 	 */
 	public ScoreBank() {
 		connection = DBConnection.getDBConnection();
 	}
 	
 	
 	/**
 	 * Adds a score to the scoreBank and underlying mysql database.
 	 * @param newScore Score object for new score to be added
 	 */
 	public void addScore(Score newScore) {
 		StringBuffer query = new StringBuffer("INSERT INTO score VALUES (0,");
 		query.append(newScore.getUserId() + ", ");
 		query.append("current_timestamp, ");
 		query.append(newScore.getQuizId() + ", ");
 		query.append(newScore.getNumCorrect() + ", ");
 		query.append(newScore.getScoreTime() + ");");
 		
 		connection.ExecuteUpdate(query.toString());
 	}
 	
 	/**
 	 * Returns top global scores of all time. Specify numDays=0 for absolute history
 	 * @param numScores - How many total scores to return
 	 * @param numDays - How many days to look behind, set 0 for unlimited
 	 * @return List of top scores
 	 */
 	public List<Score> getGlobalTopScores(int numScores, int numDays) {
 		StringBuffer query = new StringBuffer("SELECT * from score ");
 		if(numDays!=0) {
 			//Grab scores within [now - numDays, now]
 			query.append("WHERE date between date_sub(current_timestamp, interval " + numDays + " day)");
 			query.append(" and current_timestamp ");
 		}
 		
 		query.append("ORDER by value DESC, time ASC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());
 	}
 	
 	/**
 	 * Returns users's top scores. Specify numDays=0 for absolute history
 	 * 
 	 * @param userIdNum user's id number
 	 * @param numScores number of scores to retrieve
 	 * @param numDays lookback period
 	 * @return List of scores
 	 */
 	public List<Score> getUserTopScores(int userIdNum, int numScores, int numDays) {
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");
 		if(numDays!=0) {
 			//Grab scores within [now - numDays, now]
 			query.append("date between date_sub(current_timestamp, interval " + numDays + " day)");
 			query.append(" and current_timestamp and ");
 		}
 		
 		query.append("userid=" + userIdNum);
 		query.append(" ORDER by value DESC, time ASC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());
 	}
 	
 	/**
 	 * Returns top scores for a quiz. Set numDays=0 for complete history.  
 	 * 
 	  @param quizId quiz ID to get top scores for
 	 * @param numScores number of scores to retrieve
 	 * @param numDays lookback period
 	 * @return List of scores
 	 */
 	public List<Score> getQuizTopScores(int quizId, int numScores, int numDays) {
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");
 		if(numDays!=0) {
 			//Grab scores within [now - numDays, now]
 			query.append("date between date_sub(current_timestamp, interval " + numDays + " day)");
 			query.append(" and current_timestamp and ");
 		}
 		
 		query.append("quizid=" + quizId);
 		query.append(" ORDER by value DESC, time ASC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());	
 	}
 	
 	
 	/**
 	 * Returns recent scores for a quiz.  
 	 * 
 	 * @param quizId quiz ID to get recent scores for
 	 * @param numScores number of scores to retrieve
 	 * @return List of scores
 	 */
 	public List<Score> getQuizRecentScores(int quizId, int numScores) {
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");	
 		query.append("quizid=" + quizId);
 		query.append(" ORDER by date desc LIMIT " + numScores);
 		
 		return getResultSet(query.toString());	
 	}
 	
 	/**
 	 * Returns the most recent results for a users's friends
 	 * For now, just returns results by chronological order
 	 * One friend's scores could potentially barrage the result set
 	 * @param userId User ID for user to get friends scores
 	 * @param numScores number of scores to get
 	 * @param numDays lookback period
 	 * @return List of friend's scores
 	 */
 	public List<Score> getFriendTopScores(int userId, int numScores, int numDays) {
 		UserBank ub = new UserBank();
 		User user = ub.getUser(userId);
 		Set<Integer> friends = user.GetFriends();
 		if (friends.size() == 0) return new ArrayList<Score>();
 
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");
 		
 		if(numDays!=0) {
 			//Grab scores within [now - numDays, now]
 			query.append("date between date_sub(current_timestamp, interval " + numDays + " day)");
 			query.append(" and current_timestamp and ");
 		}
 		
 		//Select where it can be any of the user's friends
 		query.append("(");
 		Iterator<Integer> itr = friends.iterator();
 		while(itr.hasNext()) {
 			Integer friendId = itr.next();
 			query.append(" userid=" + friendId);
 			if(itr.hasNext()) query.append(" or ");
 			else query.append(")");
 		}
 
 		query.append(" ORDER by value DESC, time ASC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());	
 	}
 	
 	/**
 	 * Returns the most recent results for a users's friends
 	 * For now, just returns results by chronological order
 	 * One friend's scores could potentially barrage the result set
 	 * @param userId User ID for user to get friends scores
 	 * @param numScores number of scores to get
 	 * @param numDays lookback period
 	 * @return List of friend's scores
 	 */
 	public List<Score> getFriendRecentScores(int userId, int numScores) {
 		UserBank ub = new UserBank();
 		User user = ub.getUser(userId);
 		Set<Integer> friends = user.GetFriends();
 		if (friends.size() == 0) return new ArrayList<Score>();
 
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");
 		
 		//Select where it can be any of the user's friends
 		query.append("(");
 		Iterator<Integer> itr = friends.iterator();
 		while(itr.hasNext()) {
 			Integer friendId = itr.next();
 			query.append(" userid=" + friendId);
 			if(itr.hasNext()) query.append(" or ");
 			else query.append(")");
 		}
 
 		query.append(" ORDER by date DESC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());	
 	}
 	
 	
 	/**
 	 * Returns the top scores for a given quiz for a given user's group
 	 * of friends.
 	 * @param userId user id for user to get friends top scores
 	 * @param numScores num scores to return
 	 * @param numDays lookback period
 	 * @param quizID quiz id for quiz to get friends scores of
 	 * @return list of user's friends top scores on given quiz
 	 */
 	public List<Score> getFriendTopScoresForQuiz(int userId, int numScores, int numDays, int quizID) {
 		UserBank ub = new UserBank();
 		User user = ub.getUser(userId);
 		Set<Integer> friends = user.GetFriends();
 		if (friends.size() == 0) return new ArrayList<Score>();
 
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE quizid=" + quizID + " AND ");
 		
 		if(numDays!=0) {
 			//Grab scores within [now - numDays, now]
 			query.append("date between date_sub(current_timestamp, interval " + numDays + " day)");
 			query.append(" and current_timestamp and ");
 		}
 		
 		//Select where it can be any of the user's friends
 		query.append("(");
 		Iterator<Integer> itr = friends.iterator();
 		while(itr.hasNext()) {
 			Integer friendId = itr.next();
 			query.append(" userid=" + friendId);
 			if(itr.hasNext()) query.append(" or ");
 			else query.append(")");
 		}
 
 		query.append(" ORDER by value DESC, time ASC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());	
 	}
 	
 	
 	/**
 	 * Returns the rank of a given score against a user's friends historic scores.
 	 * @param userId userid whose score is being ranked
 	 * @param num_correct number of correct answers 
 	 * @param num_seconds quiz completion time
 	 * @param quizID quiz ID that score was made on
 	 * @return numeric rank of user's score versus their friends scores on the quiz
 	 */
 	public int getQuizScoreRankFriends(int userId, int num_correct, int num_seconds, int quizID) {
 		UserBank ub = new UserBank();
 		User user = ub.getUser(userId);
 		Set<Integer> friends = user.GetFriends();
 
 		StringBuffer query = new StringBuffer("SELECT COUNT(*) from score WHERE quizid=" + quizID + " AND ");
 		
 		//Select where it can be any of the user's friends
 		query.append("(");
 		Iterator<Integer> itr = friends.iterator();
 		while(itr.hasNext()) {
 			Integer friendId = itr.next();
 			query.append(" userid=" + friendId);
 			if(itr.hasNext()) query.append(" or ");
 			else query.append(")");
 		}
 		
 		query.append( " AND (value>" + num_correct + " OR (value=" + num_correct + " AND time<" +num_seconds + "))");
 				
 		System.out.println("Query " + query.toString());
 		int rank = 0;
 		try {
 			ResultSet rs = connection.ExecuteQuery(query.toString());
 			rs.next();
 			int num_scores_better = rs.getInt(1);
 			rank = num_scores_better + 1;
 
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return rank;
 	}
 	
 	/**
 	 * Returns a user's top scores for a specific quiz
 	 * Whoever calls this should check if topScores is empty
 	 *  and will have to edit the response accordingly
 	 *  (i.e. "You have not yet taken this quiz")
 	 * 
 	 * @param userId user to get top scores of
 	 * @param quizId quiz to get users top scores for
 	 * @param numScores num scores to return
 	 * @param numDays lookback period
 	 * @return list of user's top scores on quiz
 	 */
 	public List<Score> getUserQuizTopScores(int userId, int quizId, int numScores, int numDays) {
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");
 		if(numDays!=0) {
 			//Grab scores within [now - numDays, now]
 			query.append("date between date_sub(current_timestamp, interval " + numDays + " day)");
 			query.append(" and current_timestamp and ");
 		}
 		
 		query.append("quizid=" + quizId);
 		query.append(" and userid =" + userId);
 		query.append(" ORDER by value DESC, time ASC LIMIT " + numScores);
 		
 		return getResultSet(query.toString());		
 	}
 	
 	
 	/**
 	 * Returns all of a user's past scores for a given quiz
 	 * Default sorting by score descending
 	 * 
 	 * @param userId user to get top scores of
 	 * @param quizId quiz to get users top scores for
 	 * @return full history (list) of user's scores on quiz, sorted by best score
 	 */
 	public List<Score> getUserQuizAllScores(int userId, int quizId) {
 		StringBuffer query = new StringBuffer("SELECT * from score WHERE ");	
 		query.append("quizid=" + quizId);
 		query.append(" and userid =" + userId);
 		query.append(" ORDER BY value DESC, time ASC");
 		
 		return getResultSet(query.toString());		
 	}
 	
 	
 	/**
 	 * Returns a user's most recently created quizzes
 	 * 
 	 * @param userId user to get their recently created quizzes
 	 * @param num_quizzes number of recently created quizzes to obtain
 	 * @return recent list of recently created quizzes
 	 */
 	public List<Quiz> getUserRecentlyCreatedQuizzes(int userId, int num_quizzes) {
 		StringBuffer query = new StringBuffer("SELECT * from quiz WHERE ");	
 		query.append(" userid =" + userId);
 		query.append(" ORDER BY created DESC LIMIT " + num_quizzes);
 		List<Quiz> recentQuizzes = new ArrayList<Quiz>();
 		try {
 			ResultSet rs = connection.ExecuteQuery(query.toString());
 			while (rs.next()) {
 				int quiz_id = rs.getInt("id");
 				String name = rs.getString("name");
 				boolean random = false;
 				boolean multiPage = false;
 				boolean feedback = false;
 				String description = "";
 				int creator = 0;
 				Date created = rs.getDate("created");
 				Quiz quiz = new Quiz(quiz_id, name, creator,random, feedback, multiPage, description, created);
 				recentQuizzes.add(quiz);
 			}
 
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return recentQuizzes;
 	}
 	
 	/**
 	 * Returns a user's friends' most recently created quizzes
 	 * 
 	 * @param userId user to get their friends recently created quizzes
 	 * @param num_quizzes number of recently created quizzes to obtain
 	 * @return recent list of recently created quizzes
 	 */
 	public List<Quiz> getFriendsRecentlyCreatedQuizzes(int userId, int num_quizzes) {
 		UserBank ub = new UserBank();
 		User user = ub.getUser(userId);
 		Set<Integer> friends = user.GetFriends();
 		if (friends.size() == 0) return new ArrayList<Quiz>();
 		
 		StringBuffer query = new StringBuffer("SELECT * from quiz WHERE ");	
 		query.append("(");
 		Iterator<Integer> itr = friends.iterator();
 		while(itr.hasNext()) {
 			Integer friendId = itr.next();
 			query.append(" userid=" + friendId);
 			if(itr.hasNext()) query.append(" or ");
 			else query.append(")");
 		}
 		query.append(" ORDER BY created DESC LIMIT " + num_quizzes);
 		List<Quiz> recentQuizzes = new ArrayList<Quiz>();
 		try {
 			System.out.println(query.toString());
 			ResultSet rs = connection.ExecuteQuery(query.toString());
 			while (rs.next()) {
 				int quiz_id = rs.getInt("id");
 				String name = rs.getString("name");
 				boolean random = false;
 				boolean multiPage = false;
 				boolean feedback = false;
 				String description = "";
 				int creator = rs.getInt("userid");
 				Date created = rs.getDate("created");
 				Quiz quiz = new Quiz(quiz_id, name, creator,random, feedback, multiPage, description, created);
 				recentQuizzes.add(quiz);
 			}
 
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return recentQuizzes;
 	}
 	
 
 	/**
 	 * Returns the rank of a given score on a given quiz against 
 	 * all past quiz performance for that quiz
 	 * 
 	 * @param num_correct number correct from score
 	 * @param num_seconds number of seconds from score
 	 * @param quizId id of quiz
 	 * @return rank of score against all past quiz scores as an integer/numeric rank
 	 */
 	public int getQuizScoreRank(int num_correct, int num_seconds, int quizId) {
 		int rank = 0;
 		try {
 			ResultSet rs = connection.ExecuteQuery("SELECT COUNT(*) FROM score WHERE quizid=" + quizId + " AND (value>" + num_correct + " OR (value=" + num_correct + " AND time<" + num_seconds +"))");
 			rs.next();
 			int num_scores_better = rs.getInt(1);
 			rank = num_scores_better + 1;
 
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return rank;
 	}
 	
 	
 	/**
 	 * Returns the rank of a given score on a given quiz against 
 	 * all past quiz performance for that quiz
 	 * 
 	 * @param userID user to get score frank fo
 	 * @param num_correct number correct from score
 	 * @param num_seconds number of seconds from score
 	 * @param quizId id of quiz to get the rank of user's score for
 	 * @return rank of score against all past quiz scores for this user on this quiz
 	 */
 	public int getQuizScoreRankForUser(int userID, int num_correct, int num_seconds, int quizId) {
 		int rank = 0;
 		try {
 			System.out.println("query:" + "SELECT COUNT(*) FROM score WHERE userid=" + userID + " AND quizid=" + quizId + " AND (value>" + num_correct + " OR (value=" + num_correct + " AND time<" + num_seconds + "))");
 			ResultSet rs = connection.ExecuteQuery("SELECT COUNT(*) FROM score WHERE userid=" + userID + " AND quizid=" + quizId + " AND (value>" + num_correct + " OR (value=" + num_correct + " AND time<" + num_seconds + "))");
 			rs.next();
 			int num_scores_better = rs.getInt(1);
 			rank = num_scores_better + 1;
 
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return rank;
 	}
 	
 	
 	/**
 	 * Returns a user's most recent quiz scores
 	 * 
 	 * @param userId user to get recent scores of
 	 * @param num_scores number of recent scores to retrieve
 	 * @return recent list of user's scores
 	 */
 	public List<Score> getUserRecentScores(int userId, int num_scores) {
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM score where userid=" + userId + " ORDER by date DESC LIMIT " + num_scores);
 		List<Score> scores = new ArrayList<Score>();		
 		try {
 			while(rs.next()) {
 				int scoreId = rs.getInt("id");
 				int userID = rs.getInt("userid");
 				String scoreDate = rs.getString("date");
 				int quizId = rs.getInt("quizid");
 				int numCorrect = rs.getInt("value");
 				int scoreTime = rs.getInt("time");
 				Score tmpScore = new Score(scoreId,userID,scoreDate,quizId,numCorrect,scoreTime);
 	
 				scores.add(tmpScore);			
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return scores;
 	}
 	
 	
 	
 	/**
 	 * Private helper method, gets resultSet and returns it in 
 	 * the form of a List of Score objects.
 	 */
 	private List<Score> getResultSet(String query) {
 		List<Score> topScores = new ArrayList<Score>();
 		
 		try {
 			ResultSet rs = connection.ExecuteQuery(query);
 			while(rs.next()) {
 				int scoreId = rs.getInt("id");
 				int userId = rs.getInt("userid");
 				String scoreDate = rs.getString("date");
 				int quizId = rs.getInt("quizid");
 				int numCorrect = rs.getInt("value");
 				int scoreTime = rs.getInt("time");
 				Score tmpScore = new Score(scoreId,userId,scoreDate,quizId,numCorrect,scoreTime);
 				
 				topScores.add(tmpScore);			
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return topScores;
 		
 	}
 }
