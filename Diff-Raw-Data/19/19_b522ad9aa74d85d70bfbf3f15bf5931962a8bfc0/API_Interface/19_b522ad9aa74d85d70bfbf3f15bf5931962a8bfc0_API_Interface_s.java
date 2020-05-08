 package api;
 
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 public interface API_Interface {
 	
 
 	/*
 	 *****************************
 	 * DATABASE / USER FUNCTIONS *
 	 *****************************
 	 */
 	
 	/**
 	 * Checks a username / password combination against a database or external system.<br>
 	 * The implementing class should store the User's identifier for later use. (Persistent user) 
 	 * @param user_name login name
 	 * @param password login password (preferably the password has already been encrypted)
 	 * @return will be a User ID if the user is valid, zero otherwise.
 	 */
 	public int authenticate_user(String user_name, String password);
 	
 	/**
 	 * Returns the type of the user with given user id
 	 * @param UserID and user id as an integer
 	 * @return user type as an int (teacher = 1, student = 2)<br>
 	 * 	Zero will be returned if the user id is invalid.
 	 */
 	public int getUserType(int UserID);
 	
 	/**
 	 * Returns a associative array for a single user's progress.<br>
 	 * The map will be in the format:<br>
 	 * 	<pre>{@code
 	 *["id" :           UserID, [int]
 	 * "name" :         user name, [String]
 	 * "chapter":       highest chapter completed, [int]
 	 * "challenge":     highest challenge completed, [int]
 	 * "avgchapter":    average chapter completed by the class, [double]
 	 * "avgchallenge":  average challenge completed by the class [double]
 	 *]}</pre>
 	 *	To access these values use '.get("field_name")', where field_name is one of the above fields.<br>
 	 *	Casting will be required for any integer or double values. eg: (int)x.get("chapter");
 	 * 
 	 * @param UserID a valid user id
 	 * @return a {@code Map<String, String>} object. Casting will be required for integer values.
 	 */
 	public Map<String, String> getUserProgress(int UserID);
 	
 	/**
 	 * Returns a map of associative arrays for all users' progress.<br>
 	 * The array will be in the format:<br>
 	 * <pre>{@code
 	 * [ UserID : [ "name" :       user name, [String]
 	 *             "chapter":     highest chapter completed, [int]
 	 *             "challenge":   highest challenge completed, [int]
 	 *           ]
 	 *]}</pre>
 	 * To access these values use '.get(UserID).get("field_name")', where UserID is the user's id and field_name is one of the above fields.<br>
 	 * Casting will be required for any integer or double values. eg: (int)x.get("chapter");<br><br>
 	 *
 	 * The information at userID 0 will contain the average chapter and challenge completed by the class in place of the highest completed.<br><br>
 	 * 
 	 * Use in conjunction with getAllUserIDs to iterate over the map.<br>
 	 * eg:
 	 * <pre>{@code
 	 * Map<Integer, Map<String, String>> allUsers = getAllUserProgress();
 	 *Vector<Integer> userIDs = getAllUserIDs();
 	 *for(int id : userIDs)
 	 *     m.get(id).get("name");...etc
 	 * }<br>
 	 * @param UserID a valid user id
 	 * @return a {@code Map<Integer ,Map<String, String>>} object. Casting will be required for integer values.
 	 */
 	public Map<Integer, Map<String, String>> getAllUserProgress();
 	
 	/**
 	 * Returns a Vector of all the UserIDs.<br>
 	 * This vector will not include the average id (0)
 	 * @return Vector of user ids.
 	 */
 	public Vector<Integer> getAllUserIDs();
 	
 	
 	/**
 	 * Returns an ImageIcon object for the specified chapter and lesson.
 	 * @param Chapter
 	 * @param Lesson
 	 * @return the ImageIcon object
 	 */
 	public ImageIcon getLesson(int Chapter, int Lesson);
 	
 	/**
 	 * Sets the chapter progress for the user with specified UserID.
 	 * @param progress the new progress number
 	 */
 	public void setUserChapter(int UserID, int progress);
 	
 	/**
 	 * Sets the challenge progress for the user with specified UserID.
 	 * @param progress the new progress number
 	 */
 	public void setUserChallenge(int UserID, int progress);
 	
 	
 	
 	/*
 	 *****************************
 	 * ROBOT FUNCTIONS           *
 	 *****************************
 	 */
 	
 	/**
 	 * Loads the code found at 'filepath' to the robot.
 	 * @param filepath
 	 */
 	public String loadToRobot(String filepath);
 	
 	/**
 	 * Loads the code found at 'filepath' to the simulator.
 	 * @param filepath
 	 */
 	public String loadToSimulator(String filepath);
 	
 	/**
 	 * Translates the code found at 'filepath', then loads it into the robot.
 	 * @param filepath
 	 * @return String containing any error messages related to the translation. Null if translation was successful.
 	 */
 	public String translateLoadToRobot(String filepath);
 	
 	/**
 	 * Translates the code found at 'filepath', then loads it into the simulator.
 	 * @param filepath
 	 * @return String containing any error messages related to the translation. Null if translation was successful.
 	 */
 	public String translateLoadToSimulator(String filepath);
 	
 	/**
 	 * Loads the robot control interface, and returns it's instance
 	 * @return a RobotControl instance
 	 */
 	public RobotControl loadRobotController();
 	
 	/**
 	 * Loads the simulator control interface, and returns it's instance
 	 * @return a RobotControl instance
 	 */
 	public RobotControl loadSimulatorController();
 	
 }
