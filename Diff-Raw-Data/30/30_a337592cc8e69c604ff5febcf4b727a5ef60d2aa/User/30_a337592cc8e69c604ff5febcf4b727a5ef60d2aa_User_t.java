 package data;
 
 import data.me.json.*;
 
 /**
  * The user class will be used to create an individual who will be participating
  * in the survivor pool.
  * 
  * @author Graem Littleton, Kevin Brightwell, Jonathan Demelo, Ramesh Raj,
  *         Justin McDonald
  */
 
 public abstract class User implements Person {
 
 	private String firstName, lastName, unID; // first and last names and unique
 												// ID (UWO ID format)
 	private int points; // point total, points user receives if that
 									// pick wins
 	protected int winPoints;
 	
 	// JSON Keys:
 	
 	protected static final String KEY_ID = "id";
 	protected static final String KEY_FIRST_NAME = "first";
 	protected static final String KEY_LAST_NAME = "last";
 	protected static final String KEY_POINTS = "curr_points";
 	protected static final String KEY_WIN_PICK_POINTS = "win_points";
 	protected static final String KEY_ULT_PICK_ID	= "ult_pick";
 	protected static final String KEY_WEEKLY_PICK_ID = "week_pick";
 
 	/**
 	 * Constructor method for the type User sets names, initializes points
 	 * 
 	 * @param first
 	 *            first name
 	 * @param last
 	 *            last name
 	 * @param id
 	 *            unique ID
 	 */
 	public User(String first, String last, String id) {
 		firstName = first;
 		lastName = last;
 		unID = id;
 		points = 0; // begin with 0 points
 
 	}
 
 	public User() {
 		// TODO Auto-generated constructor stub
 	}
 
 	// -------------------- ACCESSOR METHODS ------------------ //
 
 	/**
 	 * getFirstName returns the first name of the user
 	 * 
 	 * @return this.firstName
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 
 	/**
 	 * getLastName returns the users last name
 	 * 
 	 * @return this.lastName
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 
 	/**
 	 * getID returns the users unique ID
 	 * 
 	 * @return this.unID
 	 */
 	public String getID() {
 		return unID;
 	}
 
 	/**
 	 * getWeeklyPick returns the users pick for which contestant will be
 	 * eliminated @ return this.weeklyPick
 	 */
 	public abstract Contestant getWeeklyPick(); 
 	
 	public int getPoints() {
 		return points;
 	}
 	
 	public void setPoints(int points) {
 		this.points = points;
 	}

 
 	/**
 	 * getWinPick returns the users selection for which contestant will win the
 	 * game
 	 * 
 	 * @return this.winPick
 	 */
 	public abstract Contestant getWinPick();
 
 	// ---------------- MUTATOR METHODS ----------------- //
 
 	/**
 	 * addPoints add points to a users current total
 	 * 
 	 * @param newPoints
 	 *            points to be added
 	 */
 	public void addPoints(int newPoints) {
 		points += newPoints;
 	}
 
 	/**
 	 * setFirstName sets the users first name
 	 * 
 	 * @param first
 	 *            users first name
 	 */
 	public void setFirstName(String first) {
 		firstName = first;
 	}
 
 	/**
 	 * setFirstName sets the users last name
 	 * 
 	 * @param last
 	 *            users last name
 	 */
 	public void setLastName(String last) {
 		lastName = last;
 	}
 
 	/**
 	 * setWeeklyPick sets the users pick for which contestant will be eliminated
 	 * 
 	 * @param pick
 	 *            contestant choice
 	 */
 	public abstract void setWeeklyPick(Contestant pick);
 
 	/**
 	 * setWinPick sets the users choice for which contestant will win the
 	 * competition. Also determines how many points the user will receive if
 	 * that player wins
 	 * 
 	 * @param winner
 	 *            contestant choice
 	 */
 	public abstract void setWinPick(Contestant winner);
 	
 	// TODO: Doc
 	// just sets the same as prior without setting pts.
 	public abstract void setWinPickNoSetPts(Contestant winner);
 	
 	public int getWinPoints() {
 		return winPoints;
 	}
 	
 	public void setWinPoints(int winPts) {
 		winPoints = winPts;
 	}
 
 	public void setID(String id) {
 		id = id.toLowerCase();
		if (DataUtils.checkString(id,REGEX_CONTEST_ID))
 			unID = id;
 	}
 	
 	/**
 	 * toString returns a string of the contestant's information in JSON format.
 	 */
 	public String toString() {
 		return new String("User<FN: " + "\"" + firstName + "\"" + ", LN: " + "\"" + lastName + "\"" + 
 				", Points: " + "\"" + points + "\"" + ", ID: " + "\"" + unID + "\">");
 	}
 
 	// ----------------- JSON ----------------- //
 	
 	
 
 	// TODO: DOCS:
 	public abstract JSONObject toJSONObject() throws JSONException; 
 	
 	public abstract void fromJSONString(String json) throws JSONException;
 	
 	public String toJSONString() throws JSONException {
 		return toJSONObject().toString();
 	}
 
 }
