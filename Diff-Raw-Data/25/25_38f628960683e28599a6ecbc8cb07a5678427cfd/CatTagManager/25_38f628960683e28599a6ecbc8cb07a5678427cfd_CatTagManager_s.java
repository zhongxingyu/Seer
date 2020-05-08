 package site;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /*
 CREATE TABLE categories (
 	category_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 	quiz_id INT,	
 	string TEXT
 );
 
 DROP TABLE IF EXISTS tags;
 
 CREATE TABLE tags (
 	tag_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 	quiz_id INT
 	string TEXT
 );*/
 
 //TODO implement removequiz function
 
 /**CatTag Control manages database structures that relate to categories and tags relating to quizzes
  * Remember to call new CatTagManager() at the beginning of any .jsp using code
  * 
  * Tags should be added by giving a string of tags separated by commas
  * Tags can have whitespace, so can Categories
  * */
 public class CatTagManager {
 	private static Connection con;
 	private static Statement stmt;
 
 	private static Pattern tagPattern;
 
 	public CatTagManager(){
 		con = MyDB.getConnection();
 		try {
 			stmt = con.createStatement();
 			tagPattern = Pattern.compile("(([^,]*)|,([^,]*)|([^,]*),)+");
 		} catch (SQLException e) {
 		}		
 	}
 
 	/**Removes all the categories and tags associated with a quiz*/
 	public static boolean removeQuizCatsTags(int quizId){
 		String deleteTags = "DELETE FROM tags where quiz_id = " + quizId;
 		String deleteCats = "DELETE FROM categories where quiz_id = " + quizId;
 		try {
 			stmt.executeUpdate(deleteTags);
 			stmt.executeUpdate(deleteCats);
 			return true;
 		} catch (SQLException e) {
 			return false;
 		}
 	}
 	
 	/**Associates a bunch of tags with a quiz in the database
 	 * You can repeat tags and only one of them will be added
 	 * 
 	 * @param quiz_id Id of the quiz
 	 * @param tags String of format: tag1, tag2, tag3... 
 	 * 
 	 * @return false on bad format or failed tagging, returns true on positive
 	 * */
 	public static boolean addStringOfTagsToQuiz(int quiz_id, String tags){
 		if (!correctTagFormat(tags)) return false;
 		for (String tag: parseTags(tags)){
 			addTagToQuiz(quiz_id, tag);
 		}
 		return true;
 	}
 
 	/**Checks if the tag is properly formatted. Given that any character goes, and commas split, this hardly ever fails*/
 	public static boolean correctTagFormat(String tags){
 		Matcher matcher = tagPattern.matcher(tags);
 		if (matcher.matches()) return true;
 		return false;
 	}
 
 	/**Adds a tag to the database*/
 	private static boolean addTagToQuiz(int quiz_id, String tag){
 		if (tag.equals("")) return true;
 		String execution = "INSERT INTO tags VALUES(NULL, " + quiz_id + ", '" + tag + "' )";
 		try {
 			stmt.executeUpdate(execution);
 			return true;
 		} catch (SQLException e) {
 			return false;
 		}
 	}
 	/**Takes a string and splits it on commas. Everything else goes*/
 	public static LinkedHashSet<String> parseTags(String tags){
 		Matcher matcher = tagPattern.matcher(tags);
 		LinkedHashSet<String> tagSet = new LinkedHashSet<String>();
 		while(matcher.find()){
 			String unStripped = matcher.group();
 			unStripped = unStripped.replaceAll("[ ]+$", "");
 			unStripped = unStripped.replaceAll("^[ ]+", "");
 			if (!unStripped.equals("")) tagSet.add(unStripped);
 		}
 		return tagSet;
 	}
 
 
 	/**Returns a string of all the tags with the quiz id */
 	public static ArrayList<String> getTagsFromQuiz(int quiz_id){
 		ArrayList<String> tags = new ArrayList<String>();
 		String execution = "SELECT * FROM tags WHERE quiz_id = " + quiz_id;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				tags.add(set.getString("string"));
 			}
 			return tags;
 		} catch (SQLException e) {
 		}
 		return null;
 	}
 
 	/**Get a list of Quiz objects from the database given a string tag*/
 	public static ArrayList<Quiz> getQuizzesFromTag(String tag){
 		ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
 		String execution = "SELECT DISTINCT quiz_id FROM tags WHERE string = '" + tag + "'";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				int quizNum = set.getInt("quiz_id");
 				Quiz newQuiz = (new QuizManager()).getQuizByQuizId(quizNum); 
 				quizzes.add(newQuiz);
 			}
 			return quizzes;
 		} catch (SQLException e) {}
 		return null;
 	}
 
 
 	/**Links a quiz to a category in the database. Doesn't do checking for valid categories
 	 * Returns false on some kind of sql failure
 	 * */
 
 	public static boolean categorizeQuiz(int quiz_id, String cat){
 		if (cat.equals("")) return false;
 		String execution = "INSERT INTO categories VALUES(NULL," + quiz_id + ", '" + cat + "')";
 		try {
 			stmt.executeUpdate(execution);
 			return true;
 		} catch (SQLException e) {}
 		return false;
 	}
 
 	/**Gets all the valid categories to tag a quiz with*/
 	public static ArrayList<String> getCategories(){
 		ArrayList<String> cats = new ArrayList<String>();
 		String execution = "SELECT DISTINCT string from categories WHERE quiz_id = -1";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				cats.add(set.getString("string"));
 			}
 			return cats;
 		} catch (SQLException e) {
 		}
 		return null;
 	}
 
 	/**Adds a category to the category database, making it a valid category
 	 * for users to add to the database. Only admin's should be able to call this
 	 * No adding the empty string to categories. 
 	 */
 	public static boolean createCategory(String cat){
 		if (cat.equals("")) return false;
 		//Adds a quiz with value -1 into database. Retrieval functions don't process quiz_ids of -1
 		String execution = "INSERT INTO categories VALUES(NULL, -1 , '" + cat + "')";
 		try {
 			stmt.executeUpdate(execution);
 			return true;
 		} catch (SQLException e) {}
 		return false;
 	}
 
 	/**Deletes a category from the database
 	 */
 	public static boolean deleteCategory(String cat){
 		if (cat.equals("")) return false;
 		//Adds a quiz with value -1 into database. Retrieval functions don't process quiz_ids of -1
 		String execution = "DELETE FROM categories WHERE string = '" + cat + "'";
 		try {
 			stmt.executeUpdate(execution);
 			return true;
 		} catch (SQLException e) {}
 		return false;
 	}
 	
 	
 	/**Gets the category associated with specific quiz*/
 	public static String getCategoryFromQuiz(int quiz_id){
 		String execution = "SELECT * from categories where quiz_id = " + quiz_id + " LIMIT 1";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return set.getString("string");
 		} catch (SQLException e) {
 		}
 		return "FailedExtractionOfCategory";
 	}
 	
 	/**Get a list of Quiz objects from the database given a string category*/
 	public static ArrayList<Quiz> getQuizzesFromCategory(String cat){
 		ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
		String execution = "SELECT DISTINCT quiz_id FROM categories WHERE string = '" + cat + "' where quiz_id > 0 ";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				int quizNum = set.getInt("quiz_id");
 				if (quizNum != -1){
 					Quiz newQuiz = (new QuizManager()).getQuizByQuizId(quizNum); 
 					quizzes.add(newQuiz);
 				}
 			}
 			return quizzes;
 		} catch (SQLException e) {}
 		return null;
 	}
 
 
 
 }
