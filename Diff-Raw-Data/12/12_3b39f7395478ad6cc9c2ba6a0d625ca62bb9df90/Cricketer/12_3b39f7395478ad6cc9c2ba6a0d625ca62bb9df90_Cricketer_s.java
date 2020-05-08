 package cricketsim.model;
 
 import java.util.Date;
 
 /**
 * @author Ryan Gouldsmith (ryangouldsmith@gmail.com)
 * @author Josh Tumath (josh@joshtumath.me.uk)
 */
 public class Cricketer {
 	/** Full name */
 	private String name;
 	
 	/** Gender */
 	private char gender;
 	
 	/** Date of birth */
 	private Date dateOfBirth;
 	
 	/** Position on the field */
 	private Position position;
 	
 	/** The number of times this player has played a game */
 	private int appearances;
 	
 	/** ??? */
 	private int wickets;
 	
 	/** The total number of runs this player has made */
 	private int totalRuns;
 	
 	/**
 	 * Constructs a new cricket player.
 	 * @param name
 	 * @param gender
 	 * @param dateOfBirth
 	 * @param position
 	 */
 	public Cricketer(String name, char gender, Date dateOfBirth, Position position) {
 		this.name = name; 
 		this.gender = gender;
		this.dateOfBirth= dateOfBirth; 
 		this.position = position;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public char getGenger() {
 		return gender;
 	}
 	
 	public Date getDateOfBirth() {
 		return dateOfBirth;
 	}
 	
 	public Position getPosition() {
 		return position;
 	}
 	
 	public int getRuns() {
 		return totalRuns;
 	}
 	
 	public void setRuns(int runs) {
 		this.totalRuns = runs;
 	}
 	
 	public int getAppearances() {
 		return appearances;
 	}
 	
	public void setAppearances(int app) {
		this.appearances = app;
 	}
 	
 	public int getWickets() {
 		return wickets;
 	}
 	
	public void setWickets(int wick) {
		this.wickets = wick;
 	}
 	
 	public int getAverageRuns() {
 		return totalRuns / appearances;
 	}
 }
