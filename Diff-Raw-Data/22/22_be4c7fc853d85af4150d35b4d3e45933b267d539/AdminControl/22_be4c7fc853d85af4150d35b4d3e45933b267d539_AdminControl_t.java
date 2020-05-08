 package site;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 
 /**Admin Control allows an admin to do bunch of things to people's stuff without them asking
  * Admin Control does not check if the user is an admin. The caller to AdminControl should check beforehand
  * 
  * */
 
 
 public class AdminControl {
 
 	private static Connection con;
 	private static Statement stmt;
 	private static Object AdminControl; 
 
 	public AdminControl(){
 		con = MyDB.getConnection();
 		try {
 			stmt = con.createStatement();
 		} catch (SQLException e) {
 
 		}
 	}
 
 	/**Returns true if the selected user is a admin. Returns false if they aren't an admin, or aren't actually an account*/
 	public static boolean isAdmin(int userId){
 		String execution = "SELECT is_admin FROM users where user_id = " + userId;
 		try {
 			ResultSet set =	stmt.executeQuery(execution);
 			if (!set.first()) return false;
 			return set.getBoolean("is_admin");
 		} catch (SQLException ignored) {}
 		return false;
 	}
 
 	/**Adds an announcement to the announcements list*/
 	public static void AddAnouncement(int userId, String text){
 		String execution = "INSERT INTO announcements value(NULL," + userId + ",'"+text+"',NOW())";
 		try {
 			stmt.executeUpdate(execution);
 		} catch (SQLException e) {
 		}
 	}
 
 
 
 	/**Returns an ArrayList of Announcements ordered by date created
 	 * Pass it a limit!
 	 * */
 
 	public static ArrayList<Announcement> getAnnouncements(int limit){
 		String execution = "SELECT * FROM announcements ORDER BY created_timestamp DESC LIMIT 0," + limit;
 		ArrayList<Announcement> result = new ArrayList<Announcement>();
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.last();
 			int numRows = set.getRow();
 			for (int i = 1; i <= numRows; i ++){
 				set.absolute(i);
 				result.add(generateAnnouncement(set));
 			}
 			return result;
 		} catch (SQLException e) {
 		}
 		return null;
 	}
 
 	private static Announcement generateAnnouncement(ResultSet set){
 		try {
 			int userId = set.getInt("user_id");
 			String text = set.getString("string");
 			Timestamp time = set.getTimestamp("created_timestamp");
 			return new Announcement(userId, text, time);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new Announcement(-1,"FailedRetrievalofAnnouncement",new Timestamp(0));
 	}
 
 
 	/**Removes an account and EVERYTHING associated with it. 
 	 * Removes all quizzes created by that user
 	 * Removes all results taken by those quizzes
 	 * Removes all questions and answers associated with that stuff
 	 * Returns true if successful and false if anything goes wrong. Also has a high chance of
 	 * quitting with the database in terrible condition
 	 * */
 
 	public static boolean removeAccount(int userId){	
 		/** for each quiz created by the user - call removequiz
 		 * 
 		 */
 
 		return false;
 	}
 
 	/**Remove quiz removes a quiz and all results and questions associated with it
 	 * Does nothing right now
 	 * */
 	public static boolean removeQuiz(int quizId){
 		/*For each result related to that quiz, 
 		 * 	remove all the user_answers related to that question
 		 *	remove the quiz result
 		 *Then for each question in the quiz, remove that question
 		 *
 		 *then finally remove that quiz
 		 * 
 		 * */
 
 
 		return false;
 	}
 
 	/**Clears all the results for a quiz and also removes all user_answers associated with that quiz
 	 * Returns true if it works
 	 * */
 	public static boolean clearQuizResults(int quizId){
 		String deleteResults = "DELETE FROM results WHERE quiz_id = " + quizId;
 		String selectResults = "SELECT * FROM results WHERE quiz_id = " + quizId;
 		String deleteAnswers = "DELETE FROM user_answers where result_id = ";
 		try {
 			//Getting all the results
 			ResultSet allResults = stmt.executeQuery(selectResults);
 //			System.out.println(selectResults);
 			ArrayList<String> toBeRemoved = new ArrayList<String>();
 			while(allResults.next()){
 				//For each result, delete user_answers with that quiz
 				String toRemove = Integer.toString(allResults.getInt("result_id"));
 				toBeRemoved.add(toRemove);
 			}
 			for (String toRemove: toBeRemoved){
 				stmt.executeUpdate(deleteAnswers + toRemove);
 //				System.out.println(deleteAnswers + toRemove);
 			}
 //			System.out.println(deleteResults);
 			stmt.executeUpdate(deleteResults);
 			return true;
 		} catch (SQLException e) {
 			return false;
 		}
 	}
 
 	/**Makes a user an admin and sends them a message telling them so
 	 * Returns true if successful, returns false if userId is not a valid Id
 	 * or something else goes wrong
 	 * 
 	 * */
 	public static boolean promoteToAdmin(int adminId, int userId){
 		String execution = "UPDATE users SET is_admin = TRUE WHERE user_id = " + userId;
 		try {
 			if (!isAccount(userId)) return false;
 			if (isAdmin(userId)) return false;
 			if (!isAdmin(adminId)) return false;
 			stmt.executeUpdate(execution);
 			AccountManager manager = new AccountManager();
 			String name = manager.getAccountById(Integer.toString(adminId)).getUsername();
 			String message = "Congratulations, you have been promoted to an admin by " + name + ".\n You can now modify quizzes, other user accounts and more!";
 			User admin = manager.getAccountById(Integer.toString(adminId));
 			User user = manager.getAccountById(Integer.toString(userId));
 			Inbox.sendTextMessage(new TextMessage(admin, user, 3, message));
 			return true;
 		} catch (SQLException ignored) {	}		
 		return false;
 	}
 
 	/**Demotes an admin to a normal user and sends them a message telling them so
 	 * Returns true if successful, returns false if userId is not a valid Id
 	 * or something else goes wrong
 	 * 
 	 * */
 	public static boolean demoteFromAdmin(int adminId, int userId){
 		String execution = "UPDATE users SET is_admin = FALSE WHERE user_id = " + userId;
 		try {
 			if (!isAccount(userId)) return false;
 			stmt.executeUpdate(execution);
 			AccountManager manager = new AccountManager();
 			String name = manager.getAccountById(Integer.toString(adminId)).getUsername();
 			String message = "You have been demoted from admin by " + name + "\n on account of poor behavior or just bad karma";
 			User admin = manager.getAccountById(Integer.toString(adminId));
 			User user = manager.getAccountById(Integer.toString(userId));
 			Inbox.sendTextMessage(new TextMessage(admin, user, 3, message));
 			return true;
 		} catch (SQLException ignored) {	}		
 		return false;
 	}
 
 	
 	
 	
 	/**Returns a bunch of stats in an arraylist of objects.
 	 * A Statistic has a stat.stat int field and stat.description String field
 	 * Most of them will be -1 on failure. 
 	 * 0 - Integer - number of users
 	 * 1 - Integer - number of quizzes
 	 * 2 - Integer - number of quizzes taken
 	 * 3 - User - User object of most something user
 	 * 
 	 * 
 	 * */
 
 	public static ArrayList<Statistic> getStatistics(){
 		ArrayList<Statistic> stats = new ArrayList<Statistic>();
 		stats.add(getUserCount());
 		stats.add(getQuizCount());
 		stats.add(getResultCount());
		return stats;
 	}
 	
 	private static Statistic getUserCount(){
 		String execution = "SELECT COUNT(*) FROM users";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return new Statistic("Number of Accounts",set.getInt(1));
 		} catch (SQLException ignored) {}
 		return new Statistic("Failed Query", -1);
 	}
 
 	private static Statistic getQuizCount(){
 		String execution = "SELECT COUNT(*) FROM quizzes";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return new Statistic("Number of Quizzes",set.getInt(1));
 		} catch (SQLException ignored) {}
 		return new Statistic("Failed Query", -1);
 	}
 
 	private static Statistic getResultCount(){
 		String execution = "SELECT COUNT(*) FROM results";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return new Statistic("Number of Quizzes Taken",set.getInt(1));
 		} catch (SQLException ignored) {}
 		return new Statistic("Failed Query", -1);
 	}
 
 	/***/
 	private static boolean isAccount(int userId){
 		String execution = "SELECT is_admin FROM users where user_id = " + userId;
 		try {
 			ResultSet set =	stmt.executeQuery(execution);
 			if (!set.first()) return false;
 			return true;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 }
 
 
