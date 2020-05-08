 package quiz;
 
 import java.sql.*;
 import java.util.Date;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import user.User;
 
 public class QuizSummary {
 	
 	private Quiz quiz;
 	private List<QuizResult> usersResults; //leave as ability to be null?
 	private List<String> topPerformers;
 	private List<String> recentTopPerformers;
 	private List<QuizResult> recentResults;
 	private double mean;
 	private int median;
 	
 	public QuizSummary(Quiz quiz, User usr, Connection conn) {
 		this.quiz = quiz;
 		
 		recentResults(conn);
 		recentUsernames(conn);
 		topAllTime(conn);
 		usersResults(conn, usr);
 		mean(conn);
 		median(conn);
 		
 	}
 	
 	private void usersResults(Connection conn, User user) {
 		usersResults = new ArrayList<QuizResult>();
 		if (user == null) return;
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			String query = "SELECT * from Quiz_Result where quiz_id=" + quiz.getID() + " AND username='" + user.getName() + "' order by id DESC;";
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs != null) {
 				while (rs.next()) {
 					usersResults.add(new QuizResult(rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5), quiz));
 				}
 			}	
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	
 	private void recentResults(Connection conn) {
 		recentResults = new ArrayList<QuizResult>();
 		
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			String query = "SELECT * from Quiz_Result where quiz_id=" + quiz.getID() + " order by id DESC;";
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs != null) {
 				int count = 0;
 				while (rs.next() && count < QuizConstants.N_TOP_SCORERS) {
 					recentResults.add(new QuizResult(rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5), quiz));
 					count++;
 				}
 			}	
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	private void recentUsernames(Connection conn) { //check this query
 		recentTopPerformers = new ArrayList<String>();
 		Statement stmt;
 		long time = System.currentTimeMillis();
 		Date date = new Date(time - QuizConstants.RECENT_INTERVAL);
 		SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
 		String earliest = formatter.format(date);
 		
 		try {
 			stmt = conn.createStatement();
 			String query = "SELECT * from Quiz_Result where quiz_id=" + quiz.getID() + " AND time_taken >= '" + earliest + "' order by score DESC;";
 			ResultSet rs = stmt.executeQuery(query);
 
 			int count = 0;
 			if (rs != null) {
 				while (rs.next() && count < QuizConstants.N_TOP_SCORERS) {
 					topPerformers.add("<a href=\"UserProfileServlet?username=" + rs.getString("username") + "\">" +
 										rs.getString("username") + "</a> got " + rs.getString("score") + " points.\n" +
 										"Taken at " + rs.getString("time_taken") + "\n");
 					count++;
 				}
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	private void topAllTime(Connection conn) {
 		topPerformers = new ArrayList<String>();
 
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			String query = "SELECT * from Quiz_Result where quiz_id=" + quiz.getID() + " order by score DESC;";
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs != null) {
 				int count = 0;
 				int prevScore = 0;
 				while (rs.next()) {
 					int score = rs.getInt("score");
 					if (count >= QuizConstants.N_TOP_SCORERS && prevScore != score) break;
 					topPerformers.add("<a href=\"UserProfileServlet?username=" + rs.getString("username") + "\">" +
 										rs.getString("username") + "</a> got " + rs.getString("score") + " points.\n" +
 									  "Taken at " + rs.getString("time_taken") + "\n");
 					prevScore = score;
 					count++;
 				}
 			}	
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 	
 	private void mean(Connection conn) {
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			String query = "Select avg(score) from Quiz_Result WHERE quiz_id=" + quiz.getID() + ";";
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs.next()) {
 				mean = rs.getDouble(1);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private void median(Connection conn) {
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			String query = "Select score from Quiz_Result WHERE quiz_id=" + quiz.getID() + " ORDER BY score asc;";
 			ResultSet rs = stmt.executeQuery(query);
 			int numEntries;
 			if (rs.last()) {
 				numEntries = rs.getRow();
				rs.First();
 				rs.relative(numEntries / 2);
 				median = rs.getInt("score");
 			} 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public int getMedian() {
 		return median;
 	}
 	
 	public double getMean() {
 		return mean;
 	}
 	
 	public List<String> getTopAll() {
 		return topPerformers;
 	}
 	
 	public List<String> getRecentTop() {
 		return recentTopPerformers;
 	}
 	
 	public List<QuizResult> getUserResults() {
 		return usersResults;
 	}
 	
 	public List<QuizResult> getRecentResults() {
 		return recentResults;
 	}
 
 	public Quiz getQuiz() {
 		return quiz;
 	}
 }
