 package data;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Random;
 
 import json.simple.JSONArray;
 import json.simple.JSONObject;
 import json.simple.parser.ParseException;
 import admin.Utils;
 import data.bonus.Bonus;
 
 /**
  * GameData is the class that will be used to keep track of the important game
  * information, including the number of weeks passed, the lists of all/active
  * contestants, and the number of weeks remaining.
  * 
  * @author Graem Littleton, Ramesh Raj, Justin McDonald, Jonathan Demelo, Kevin
  *         Brightwell
  */
 
 public class GameData extends Observable {
 
 	private int weeksRem, weeksPassed; // keep track of weeks remaining/weeks
 										// passed
 	private int numInitialContestants;
 	private int betAmount;
 
 	private boolean seasonStarted = false;
 	private boolean elimExists = false;
 
 	private String[] tribeNames = new String[2]; // string array storing both
 													// tribe names
 
 	private List<Contestant> allContestants;
 
 	private List<User> allUsers;
 
 	// store the current running version
 	private static GameData currentGame = null;
 	// store contestant who was cast off
 	private Contestant elimCont;
 
 	private Field updatedComponent;
 	
 	/**
 	 * JSON Keys
 	 */
 	public enum Field {
 		START_SEASON, ADVANCE_WEEK, SET_TRIBE_NAMES, 
 		ADD_CONTESTANT, REMOVE_CONTESTANT, 
 		ADD_USER, REMOVE_USER, 
 		END_GAME, ALLOCATE_POINTS,
 	}
 	
 	private static final String KEY_CONTESTANTS = "cons";
 	private static final String KEY_NUM_CONTEST = "cons_num";
 
 	private static final String KEY_USERS = "users";
 
 	private static final String KEY_WEEKS_REMAIN = "weeks_rem";
 	private static final String KEY_WEEKS_PASSED = "weeks_pass";
 
 	private static final String KEY_TRIBES = "tribes_arr";
 
 	private static final String KEY_SEASON_STARTED = "season_started";
 	
 	public static String pathGame = "res/data/Settings.dat";
 	public static String pathBonus = "res/data/bonus.dat";
 
 	/**
 	 * Constructor method that takes a set number of contestants. Will not
 	 * proceed if numInitialContestants is NOT between 6 and 15, inclusive. Sets
 	 * number of weeks passed to 0 and weeks remaining to number of contestants
 	 * - 3.
 	 * 
 	 * @param numInitialContestants
 	 *            number of contestants to be in game
 	 */
 	// TODO: Make throw exception, its not enough to return, the object is still
 	// created.
 	public GameData(int numInitialContestants) {
 		// check if within parameters
 		if (numInitialContestants > 15 || numInitialContestants < 6)
 			return; // if not, do not create GameData item
 
 		weeksRem = numInitialContestants - 3;
 		weeksPassed = 0;
 		setBetAmount(0);
 		this.numInitialContestants = numInitialContestants;
 		allContestants = new ArrayList<Contestant>(numInitialContestants);
 
 		allUsers = new ArrayList<User>(5);
 
 		currentGame = this;
 	}
 
 	// ----------------- ACCESSOR METHODS -----------------//
 
 	// ~~~~~~~~~~~~~~~~ CONTESTANT METHODS ~~~~~~~~~~~~~~~ //
 
 	/**
 	 * getActiveContestants returns an array (list) of the contestants that are
 	 * still competing in the game.
 	 * 
 	 * @return The contestants active
 	 */
 	public List<Contestant> getActiveContestants() {
 
 		List<Contestant> active = new ArrayList<Contestant>(
 				allContestants.size());
 
 		for (Contestant c : allContestants) {
 			if ((c != null) && !c.isCastOff()) {
 				active.add(c);
 			}
 		}
 
 		return active;
 	}
 
 	/**
 	 * getInitialContestants returns an integer of the number of initial
 	 * contestants that are in the game.
 	 * 
 	 * @return The current amount of contestants
 	 */
 	public int getInitialContestants() {
 		return numInitialContestants;
 	}
 
 	/**
 	 * getNumCurrentContestants returns an integer of the number of contestants
 	 * that are in the game.
 	 * 
 	 * @return The current amount of contestants
 	 */
 	public int getNumCurrentContestants() {
 		return allContestants.size();
 	}
 
 	/**
 	 * getAllContestants returns a list of all current and former contestants
 	 * that are/have been involved with the game.
 	 * 
 	 * @return this.allContestants
 	 */
 	public List<Contestant> getAllContestants() {
 		return allContestants;
 	}
 
 	/**
 	 * getContestant takes the first and last name of a contestant as input and
 	 * searches the array of current contestants for him/her. Returns
 	 * information found in the Contestant class to the caller.
 	 * 
 	 * @param first
 	 *            First name
 	 * @param last
 	 *            Last name
 	 * @return contestant or string object
 	 */
 	public Contestant getContestant(String first, String last) {
 		// loop through array
 		for (Contestant j : allContestants) {
 			if (first.equals(j.getFirstName()) && last.equals(j.getLastName())) { // ensure
 																					// //
 																					// match
 				// return info on player
 				return j;
 			}
 		}
 
 		// otherwise return message saying contestant is no longer/is not in the
 		// game
 		return null;
 	}
 
 	/**
 	 * Get contestant based on unique id
 	 * 
 	 * @param id
 	 *            an unique id
 	 * @return the Contestant that matches id or null
 	 */
 	public Contestant getContestant(String id) {
 		int i = getContestantIndexID(id);
 
 		if (i >= 0)
 			return allContestants.get(i);
 		else
 			return null;
 	}
 
 	/**
 	 * Adds a new contestant into the Game, this will maintain the list of
 	 * contestants as sorted by ID.
 	 * 
 	 * @param c
 	 *            New contestant, will not add if ID of contestant is null.
 	 */
 	public void addContestant(Contestant c) throws InvalidFieldException {
 		if (allContestants.size() == numInitialContestants) {
 			System.out.println("Too many contestants.");
 			return;
 		}
 
 		if (isContestantIDInUse(c.getID())) {
 			throw new InvalidFieldException(
 					InvalidFieldException.Field.CONT_ID_DUP,
 					"Contestant ID invald (in use)");
 		}
 
 		allContestants.add(c);
 
 		updatedComponent = Field.ADD_CONTESTANT;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * removeContestant takes a Contestant object as input and attempts to
 	 * remove it from the array of active contestants. Maintains order of data
 	 * 
 	 * @param target
 	 *            Contestant to remove
 	 */
 	public void removeContestant(Contestant target) {
 		// is the contestant there?
 		int i = Utils.BinSearchSafe(allContestants, (Contestant) target,
 				Utils.CompType.CONTNT_ID);
 
 		if (i < 0) {
 			// i < 0 implies not found.
 			return;
 		}
 
 		allContestants.remove(i);
 		updateSortAllContestants(Utils.CompType.CONTNT_ID);
 
 		updatedComponent = Field.REMOVE_CONTESTANT;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~ USER METHODS ~~~~~~~~~~~~~~~~~~ //
 
 	/**
 	 * Gets the vector of all users.
 	 * 
 	 * @return Vector containing all users.
 	 */
 	public List<User> getAllUsers() {
 		return allUsers;
 	}
 
 	/**
 	 * Adds a user to the list of users.
 	 * 
 	 * @param u
 	 *            New user to add.
 	 * @throws InvalidFieldException
 	 *             Thrown if ID already in use.
 	 */
 	public void addUser(User u) throws InvalidFieldException {
 		if (isUserIDInUse(u.getID())) {
 			throw new InvalidFieldException(
 					InvalidFieldException.Field.CONT_ID_DUP,
 					"Contestant ID invald (in use)");
 		}
 
 		allUsers.add(u);
 
 		updatedComponent = Field.ADD_USER;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * Removes a user from the list.
 	 * 
 	 * @param u
 	 *            User to remove.
 	 */
 	public void removeUser(User u) {
 		for (User arrU : allUsers) {
 			if (u.getID().equals(arrU.getID())) {
 				allUsers.remove(arrU);
 				return;
 			}
 		}
 
 		updatedComponent = Field.REMOVE_USER;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * Gets a user from the stored users by ID.
 	 * 
 	 * @param ID
 	 *            User ID of the User to get from the stored data.
 	 * @return User if ID found, null otherwise.
 	 */
 	public User getUser(String ID) {
 		for (User u : allUsers) {
 			if (u.getID().equalsIgnoreCase(ID)) {
 				return u;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Iterates through all users on the list. Allocates points based off of
 	 * weekly elimination pick.
 	 * 
 	 * @param c
 	 *            Contestant that was cast off
 	 */
 
 	public void allocatePoints(Contestant c) {
 		Iterator<User> itr = allUsers.iterator();
 		User u;
 		while (itr.hasNext()) {
 			u = itr.next();
 			if (u.getWeeklyPick().equals(c)) {
 				if (!this.isSeasonEnded()){ // normal week
 				u.addPoints(20);
 				System.out.println("Added 20 points to " + u);
 				} else { // last week
 					u.addPoints(40);
 					System.out.println("Added 40 points to " + u);
 				}
 			}
 			// if the end of the game and the person gets the right ultimate pick
 			if (u.getUltimatePick().equals(c) && this.isSeasonEnded()){
 				u.addPoints(u.getUltimatePoints());
 			}
 		}
 
 		updatedComponent = Field.ALLOCATE_POINTS;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * getTribeName returns a String array with two entries: the name of the
 	 * first tribe, and the name of the second tribe.
 	 * 
 	 * @return String array tribe names
 	 */
 
 	public String[] getTribeNames() {
 		return tribeNames;
 	}
 
 	/**
 	 * weeksLeft returns the number of weeks remaining before the game ends.
 	 * 
 	 * @return this.weeksRem
 	 */
 	public int weeksLeft() {
 		return weeksRem;
 	}
 
 	/**
 	 * Get the current week in play. Starts from Week 1.
 	 * 
 	 * @return Current week
 	 */
 	public int getCurrentWeek() {
 		return weeksPassed + 1;
 	}
 
 	/**
 	 * Checks if a season has been started
 	 * 
 	 * @see startGame to set to true.
 	 * @return true if a season has started(different from created)
 	 */
 
 	public boolean isSeasonStarted() {
 		return seasonStarted;
 	}
 
 	/**
 	 * Checks if there are any more weeks remaining
 	 * 
 	 * @return true if weeks remaining = 0
 	 */
 	public boolean isSeasonEnded() {
 		return weeksRem == 0;
 	}
 
 	// ----------------- MUTATOR METHODS ------------------//
 
 	/**
 	 * TOOD:
 	 * 
 	 * @param isActive
 	 * @return
 	 */
 	public Contestant randomContestant(boolean isActive) {
 		List<Contestant> list = null;
 		if (isActive) {
 			list = getActiveContestants();
 		} else {
 			list = getAllContestants();
 		}
 		Random r = new Random();
 		int index = r.nextInt(list.size());
 		return list.get(index);
 	}
 
 	/**
 	 * advanceWeek sets the number of weeksPassed to weeksPassed + 1.
 	 */
 	public void advanceWeek() {
 		if (elimExists == false)
 			return;
 
 		/* Fill weekly NULLs */
 		for (User u : allUsers) {
 			if (u.getWeeklyPick().isNull()) {
 				try {
 					u.setWeeklyPick(randomContestant(true));
 				} catch (InvalidFieldException e) {
 				} // wont happen
 			}
 
 			/* Fill ultimate NULLs */
 			if (u.getUltimatePick().isNull()) {
 				try {
 					u.setUltimatePick(randomContestant(true));
 				} catch (InvalidFieldException e) {
 				} // wont happen
 			}
 		}
 
 		allocatePoints(elimCont);
 		elimCont.castOff();
 
 		/* clear all weekly picks */
 		for (User u : allUsers) {
 			try {
 				u.setWeeklyPick(null);
 			} catch (InvalidFieldException e) {
 			} // wont happen
 
 			/* clear defunct ult picks */
 			if (u.getUltimatePick().getID().equals(elimCont.getID())) {
 				try {
 					u.setUltimatePick(null);
 				} catch (InvalidFieldException e) {
 				} // wont happen
 			}
 		}
 
 		weeksRem -= 1; // reduce num of weeks remaining
 		weeksPassed += 1; // increment number of weeks passed
 
 		updatedComponent = Field.ADVANCE_WEEK;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * startGame sets gameStarted to true, not allowing the admin to add any
 	 * more players/Contestants to the pool/game.
 	 */
 
 	public void startSeason(int bet) {
 		this.setBetAmount(bet);
 		seasonStarted = true;
 		
 		updatedComponent = Field.START_SEASON;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * setTribeNames sets both tribe names accordingly and stores them in the
 	 * tribeNames string array. Updates all contestants accordingly
 	 * 
 	 * @param tribeOne
 	 *            name of tribe one
 	 * @param tribeTwo
 	 *            name of tribe two
 	 */
 	public void setTribeNames(String tribeOne, String tribeTwo) {
 		// temp tribe vars.
 		String oldT1 = tribeNames[0];
 		String oldT2 = tribeNames[1];
 
 		// set the new tribes (Contestant requires this)
 		tribeNames[0] = Utils.strCapitalize(tribeOne.toLowerCase().trim());
 		tribeNames[1] = Utils.strCapitalize(tribeTwo.toLowerCase().trim());
 
 		// update all tribes first..
 		for (Contestant c : allContestants) {
 			if (c.getTribe().equalsIgnoreCase(oldT1)) {
 				try {
 					c.setTribe(tribeOne);
 				} catch (InvalidFieldException e) {
 				}
 			} else if (c.getTribe().equalsIgnoreCase(oldT2)) {
 				try {
 					c.setTribe(tribeTwo);
 				} catch (InvalidFieldException e) {
 				}
 			}
 		}
 
 		updatedComponent = Field.SET_TRIBE_NAMES;
 		setChanged();
 		notifyObservers(updatedComponent);
 	}
 
 	/**
 	 * TODO:
 	 * 
 	 * @return
 	 */
 	public boolean doesElimExist() {
 		return elimExists;
 	}
 
 	/**
 	 * TODO:
 	 * 
 	 * @param elimExists
 	 */
 	protected void setElimExists(boolean elimExists) {
 		this.elimExists = elimExists;
 	}
 
 	/**
 	 * TODO:
 	 * 
 	 * @return
 	 */
 	protected Contestant getElimCont() {
 		return elimCont;
 	}
 
 	/**
 	 * TODO:
 	 * 
 	 * @param elimCont
 	 */
 	protected void setElimCont(Contestant elimCont) {
 		this.elimCont = elimCont;
 	}
 
 	// ----------------- HELPER METHODS ----------------- //
 
 	/**
 	 * Sorts all the data as appropriate.
 	 * 
 	 * @param compFactID
 	 */
 	private void updateSortAllContestants(Utils.CompType compFactID) {
 		Collections.sort(allContestants, Utils.getContComparator(compFactID));
 	}
 
 	/**
 	 * Helper method to get the index of a contestant ID in the
 	 * activeContestants array
 	 * 
 	 * @param searchID
 	 *            Search Contestant ID
 	 * @return Index in activeContestants where ID is stored, else < 0.
 	 */
 	protected int getContestantIndexID(String searchID) {
 		return Utils.BinIDSearchSafe(allContestants, searchID);
 	}
 
 	/**
 	 * Tells whether a Contestant ID is in use.
 	 * 
 	 * @param id
 	 *            The Contestant ID is in use.
 	 * @return True if in use.
 	 */
 	public boolean isContestantIDInUse(String id) {
 		return (getContestantIndexID(id) >= 0);
 	}
 
 	/**
 	 * Tells whether a User ID is in use.
 	 * 
 	 * @param id
 	 *            The Contestant ID is in use.
 	 * @return True if in use.
 	 */
 	public boolean isUserIDInUse(String id) {
 		return (getUserIndexID(id) >= 0);
 	}
 
 	/**
 	 * Gets a Users index in the stored list by ID. Uses a binary search for
 	 * speed.
 	 * 
 	 * @param searchID
 	 * @return Index in the list, index <0 if not found.
 	 */
 	protected int getUserIndexID(String searchID) {
 		return Utils.BinIDSearchSafe(allUsers, searchID);
 	}
 
 	public void setBetAmount(int betAmount) {
 		this.betAmount = betAmount;
 	}
 
 	public int getBetAmount() {
 		return betAmount;
 	}
 
 	/**
 	 * Returns the currently stored Game, this removed need to reference the
 	 * game data all the time. But also allows objects to read data, cleanly.
 	 * 
 	 * @return Currently started game, null if none present.
 	 */
 	public static GameData getCurrentGame() {
 		return GameData.currentGame;
 	}
 
 	/**
 	 * Nulls the current game stored, allows a new game to start.
 	 */
 	public void endCurrentGame() {
 		GameData.currentGame = null;
 
 		updatedComponent = Field.END_GAME;
 		notifyObservers(updatedComponent);
 
 		JSONUtils.resetSeason();
 	}
 
 	/**
 	 * toString returns a string of the contestant's information in JSON format.
 	 */
 	public String toString() {
 		return new String("GameData<WR:\"" + weeksRem + "\"" + ", WP:\""
 				+ weeksPassed + "\"" + ", #C:\"" + numInitialContestants + "\""
 				+ ", SS: " + "\"" + seasonStarted + "\"" + ", TN: {" + "\""
 				+ tribeNames[0] + "\", \"" + tribeNames[1] + "\"}>");
 	}
 
 	/**
 	 * Convert GameData to a JSON object
 	 * 
 	 * @return a JSONObject with all the relevant data
 	 * @throws JSONException
 	 */
 	public JSONObject toJSONObject() throws ParseException {
 		JSONObject obj = new JSONObject();
 
 		obj.put(KEY_NUM_CONTEST, new Integer(numInitialContestants));
 		JSONArray cons = new JSONArray();
 		for (Object o : allContestants) {
 			if (o != null)
 				cons.add(((Contestant) o).toJSONObject());
 		}
 
 		JSONArray users = new JSONArray();
 		for (Object o : allUsers) {
 			if (o != null)
 				users.add(((User) o).toJSONObject());
 		}
 
 		JSONArray ts = new JSONArray();
 		ts.add(tribeNames[0]);
 		ts.add(tribeNames[1]);
 
 		obj.put(KEY_CONTESTANTS, cons);
 		obj.put(KEY_USERS, users);
 		obj.put(KEY_TRIBES, ts);
 		obj.put(KEY_WEEKS_REMAIN, weeksRem);
 		obj.put(KEY_WEEKS_PASSED, weeksPassed);
 		obj.put(KEY_SEASON_STARTED, seasonStarted);
 
 		return obj;
 	}
 
 	/**
 	 * Update GameData with values from JSONObject
 	 * 
 	 * @param obj
 	 *            a JSONObject that contains all the values
 	 * @throws JSONException
 	 */
 	public void fromJSONObject(JSONObject obj) throws ParseException {
 		numInitialContestants = ((Number) obj.get(KEY_NUM_CONTEST)).intValue();
 
 		// tribes
 		JSONArray ts = (JSONArray) obj.get(KEY_TRIBES);
 		setTribeNames((String) ts.get(0), (String) ts.get(1));
 
 		// week info:
 		weeksRem = Utils.numToInt(obj.get(KEY_WEEKS_REMAIN));
 		weeksPassed = Utils.numToInt(obj.get(KEY_WEEKS_PASSED));
 
 		seasonStarted = (Boolean) obj.get(KEY_SEASON_STARTED);
 
 		// Contestants must be loaded before users, but after others!
 		allContestants = new ArrayList<Contestant>(numInitialContestants);
 
 		// load the contestant array.
 		JSONArray cons = (JSONArray) obj.get(KEY_CONTESTANTS);
 		for (int i = 0; i < cons.size(); i++) {
 			Contestant c = new Contestant();
 			c.fromJSONObject((JSONObject)cons.get(i));
 			try {
 				addContestant(c);
 			} catch (InvalidFieldException ie) {
 			}
 		}
 
 		// users:
 		JSONArray users = (JSONArray) obj.get(KEY_USERS);
 		allUsers = new ArrayList<User>(users.size());
 		for (int i = 0; i < users.size(); i++) {
 			User u = new User();
 			u.fromJSONObject((JSONObject)users.get(i));
 			try {
 				addUser(u);
 			} catch (InvalidFieldException ie) {
 			}
 		}
 
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * Used by SeasonCreate to create a new season.
 	 * 
 	 * @param num
 	 */
 	public static void initSeason(int num) {
 		currentGame = new GameData(num);
 	}
 
 	/**
 	 * intGameData reads in a data file and builds a GameData object out of it,
 	 * returning it to the user.
 	 * 
 	 * @param inputFile
 	 *            file to be read in
 	 * @return GameData object made out of file or null if season not created
 	 * 
 	 */
 	public static GameData initGameData() {
 		JSONObject json;
 		try {
 			json = JSONUtils.readFile(pathGame);
 		} catch (FileNotFoundException e) {
 			return currentGame;
 		}
 
 		currentGame = new GameData(
 				Utils.numToInt(json.get(KEY_NUM_CONTEST)));
 		// TODO: Combine?
 		try {
 			GameData.getCurrentGame().fromJSONObject(json);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		Bonus.initBonus();
 		return (GameData) currentGame;
 	}
 
 	/**
 	 * Write all DATA into file
 	 */
 	public void writeData() {
 
 		try {
 			JSONUtils.writeJSON(pathGame, this.toJSONObject());
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		GameData g = new GameData(6);
 
 		String[] tribes = new String[] { "banana", "apple" };
 
 		g.setTribeNames(tribes[0], tribes[1]);
 
 		Contestant c1 = null, c2 = null;
 		try {
 			c1 = new Contestant("a2", "Al", "Sd", tribes[1]);
 			c2 = new Contestant("as", "John", "Silver", tribes[0]);
 		} catch (InvalidFieldException e) {
 			// wont happen.
 		}
 
 		try {
 			g.addContestant(c1);
 			g.addContestant(c2);
 		} catch (InvalidFieldException ie) {
 		}
 		;
 
 		g.startSeason(5);
 		User u1;
 		try {
 			u1 = new User("First", "last", "flast");
 			User u2 = new User("Firsto", "lasto", "flasto");
 			g.addUser(u1);
 			g.addUser(u2);
 			u1.setPoints(10);
 			u2.setPoints(1);
 		} catch (InvalidFieldException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 
 		try {
 			System.out.println(g.toJSONObject().toString());
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 }
