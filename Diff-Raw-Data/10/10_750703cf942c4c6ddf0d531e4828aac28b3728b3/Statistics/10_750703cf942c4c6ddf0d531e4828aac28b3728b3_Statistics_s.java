 package storage;
 
 //import java.util.ArrayList;
 
 /**
  * Creates Statistics objects representing a stored player's stats with getters
  * and setters for all variables.
  * @author JackGivesBack
  *
  */
 public class Statistics {
 	private String username;
 	private String password;
 	private String realName;
 	private int age;
 	private int numChips;
 	private int totalChipsWon;
 	private int winStreak;
 	private int maxWinStreak;
 	private int chipsWinStreak;
 	private int maxChipsWinStreak;
 	private int lossStreak;
 	private int maxLossStreak;
 	private int chipsLossStreak;
 	private int maxChipsLossStreak;
 
 	// Constructors
 	Statistics(String username, String password) {
 		this(username, password, "noName", -1, 0, 0, 0, 0, 0, 0);
 	}	
 	Statistics(String username, String password, String realName, int age) {
 		this(username, password, realName, age, 0, 0, 0, 0, 0, 0);
 	}
 	Statistics(String username, String password,	String realName, int age,
 			int numChips, int totalMoneyWon, int maxWinStreak,
 			int maxChipsWinStreak, int maxLossStreak, int maxChipsLossStreak){
 		// Check if the username contains invalid characters
 		for (char c : username.toCharArray()) {
 			if (!Character.isLetter(c)) {
 				throw new IllegalArgumentException("Username may only contain letters");
 			}
 		}
 		// Check if the password contains commas
 		if (password.contains(",")) {
 				throw new IllegalArgumentException("Password contains a comma");
 		}
 		this.username = username;
 		this.password = password;
 		this.realName = realName;
 		this.age = age;
 		this.numChips = numChips;
 		this.totalChipsWon = totalMoneyWon;
 		this.maxWinStreak = maxWinStreak;
 		this.maxChipsWinStreak = maxChipsWinStreak;
 		this.maxLossStreak = maxLossStreak;
 		this.maxChipsLossStreak = maxChipsLossStreak;
 		this.winStreak = 0;
 		this.chipsWinStreak = 0;
 		this.lossStreak = 0;
 		this.chipsLossStreak = 0;
 	}
 	/**
 	 * Retrieves a stored player's username
 	 * @return String of the player's username
 	 */
 	public String getUsername() {
 		return username;
 	}
 	/**
 	 * Retrieves a stored player's password
 	 * @return String of the player's password
 	 */
 	public String getPassword() {
 		return password;
 	}
 	/**
 	 * Checks if the input password matches the player's profile password
 	 * @param inputPassword - String of the user's password
 	 * @return true if the password is valid, false otherwise
 	 */
 	public boolean validatePassword(String inputPassword){
 		if (inputPassword.equals(this.password)) {
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Sets a new password to replace a stored player's old password
 	 * @param password - String of the password entered by user
 	 */
 	public void changePassword(String password) {
 		if (!password.contains(",")) {
 			this.password = password;
 		} else {
 			throw new IllegalArgumentException("Real name contains a comma");
 		}
 	}
 	/**
 	 * Sets a stored player's real name
	 * @return
 	 */
 	public void setRealName(String realName) throws IllegalArgumentException{
 		if (!realName.contains(",")) {
 			this.realName = realName;
 		} else {
 			throw new IllegalArgumentException("Real name contains a comma");
 		}
 	}
 	/**
 	 * Retrieves a stored player's real name
 	 * @return String of the user's real name
 	 */
 	public String getRealName() {
 		return realName;
 	}
 	/**
 	 * Sets a stored player's age
 	 * @param age - Integer of the player's age
 	 */
 	public void setAge(int age) throws IllegalArgumentException{
 		if (age >= -1 && age <= 130) {
 			this.age = age;
 		} else {
 			throw new IllegalArgumentException("Invalid Age");
 		}
 	}
 	/**
 	 * Retrieves a stored player's age
 	 * @return An integer of the player's age
 	 */
 	public int getAge() {
 		return age;
 	}
 	/**
 	 * Sets a stored player's current chip count
 	 * @param numChips - Integer of the new value to set as current chip count
 	 */
 	public void setChips(int numChips) throws IllegalArgumentException{
 		if (numChips >= 0) {
 			this.numChips = numChips;
 			Storage.savePlayer(this);
 		} else {
 			throw new IllegalArgumentException("Number of chips is negative");
 		}
 	}
 	
 	public boolean addChips(int numChips) throws IllegalArgumentException {
 		boolean successful;
 		
 		if (numChips < 0 && -numChips > this.numChips) {
 			successful = false;
 			throw new IllegalArgumentException("Insufficient chips");
 		} else {
 			this.numChips += numChips;
 			Storage.savePlayer(this);
 			successful = true;
 		}
 		
 		return successful;
 	}
 	
 	/**
 	 * Retrieves a stored player's current chip count
 	 * @return An integer of the player's current chip count
 	 */
 	public int getChips() {
 		return numChips;
 	}
 	/**
 	 * Retrieves a stored player's total cumulative chips won
 	 * @return An integer of a player's cumulative chip wins
 	 */
 	public int getTotalChipsWon() {
 		return totalChipsWon;
 	}
 	/**
 	 * Adds a win to a player's statistics. Increments win streaks and breaks a loss streak.
 	 * @param betValue - Value of the player's bet that gets added to a player's chips
 	 */
 	public void addWin(int betValue) {
 		winStreak++;
 		
 		numChips += betValue;
 		chipsWinStreak += betValue;
 		totalChipsWon += betValue;
 		
 		if (winStreak > maxWinStreak){
 			maxWinStreak = winStreak;
 		}
 		if (chipsWinStreak > maxChipsWinStreak) {
 			maxChipsWinStreak = chipsWinStreak;
 		}
 		lossStreak = 0;
 		chipsLossStreak = 0;
 		
 		Storage.savePlayer(this);
 	}
 	
 	/**
 	 * Adds a loss to a player's statistics. Increments a loss streak and breaks a win streak.
 	 * @param betValue - Value of the player's bet that gets removed from a player's chips
 	 */
 	public void addLoss(int betValue) {
 		lossStreak++;
 		
 		numChips -= betValue;
 		chipsLossStreak += betValue;
 		
 		if (lossStreak > maxLossStreak){
 			maxLossStreak = lossStreak;
 		}
 		if (chipsLossStreak > maxChipsLossStreak) {
 			maxChipsLossStreak = chipsLossStreak;
 		}
 		winStreak = 0;
 		chipsWinStreak = 0;
 		
 		Storage.savePlayer(this);
 	}
 	/**
 	 * Retrieves a player's max win streak
 	 * @return An integer of a player's max win streak
 	 */
 	public int getMaxWinStreak() {
 		return maxWinStreak;
 	}
 	/**
 	 * Retrieves a player's max chip win streak
 	 * @return An integer of a player's max chip win streak
 	 */
 	public int getMaxChipsWinStreak() {
 		return maxChipsWinStreak;
 	}
 	/**
 	 * Retrieves a player's max loss streak
 	 * @return An integer of a player's max loss streak
 	 */
 	public int getMaxLossStreak() {
 		return maxLossStreak;
 	}
 	/**
 	 * Retrieves a player's max chip loss streak
 	 * @return An integer of a player's max chip loss streak
 	 */
 	public int getMaxChipsLossStreak() {
 		return maxChipsLossStreak;
 	}
 
 	/**
 	 * Converts the user's statistics into a string, to be displayed to screen
 	 * @return A String of the player's statistics, formatted to be displayed
 	 */
 	public String toString() {
 		String string = "Username: " + this.username + "\n";
 		string += "Total chips won: " + this.totalChipsWon + "\n";
 		string += "Biggest Winning Streak: \n";
 		string += "\tNumber of games: " + this.maxWinStreak + "\n";
 		string += "\tChips won: " + maxChipsWinStreak + "\n";
 		string += "Biggest Losing Streak: \n";
 		string += "\tNumber of games: " + this.maxLossStreak + "\n";
 		string += "\tChips lost: " + this.maxChipsLossStreak + "\n";
 		
 		return string;
 	}
 	/**
 	 * Prints the user's statistics to screen along with the Hall of Fame display
 	 */
 	public void displayStatistics() {
 		System.out.println(this.toString() + "\n\n");
 		HallOfFame.display();
 	}
 	
 }
