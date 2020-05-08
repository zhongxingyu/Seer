 /**
  * @file ObjInfo.java
  * 
  * The ObjInfo container
  * 
  * @author Grant Hays
  * @date 09/01/11
  * @version 1
  */
 
 import java.net.*;
 import java.util.*;
 import java.io.*;
 
 /**
  * @class ObjInfo
  * 
  * A container for items in the Player's vision
  */
 class ObjInfo {
 	
 	/**
 	 * The Default constructor
 	 */
 	public ObjInfo() {
 	}
 	
 	/**
 	 * The ObjInfo constructor
 	 * 
 	 * This initializes all the variables to 0.0 and sets the name
 	 * 
 	 * @param name The type of ObjInfo, either ball, player, goal, line, or flag
 	 */
 	public ObjInfo(String name) {
 		setObjName(name);
 		setDistance(0.0);
 		setDirection(0.0);
 		setDistChng(0.0);
 		setDirChng(0.0);
 	}
 	
 	/**
 	 * The ObjName getter
 	 */
 	public String getObjName() {
 		return ObjName;
 	}
 	
 	/**
 	 * The ObjName setter
 	 */
 	public void setObjName(String name) {
 		ObjName = name;
 	}
 
 	/**
 	 * The side getter
 	 */
 	public String getSide() {
 		return side;
 	}
 	
 	/**
 	 * The side setter
 	 */
 	public void setSide(String objSide) {
 		this.side = objSide;
 	}
 
 
 	/**
 	 * The distance getter
 	 * 
 	 * @return the approximate distance to the object
 	 */
 	public double getDistance() {
 		return distance;
 	}
 
 	/**
 	 * The distance setter
 	 */
 	public void setDistance(double distance) {
 		this.distance = distance;
 	}
 
 	/**
 	 * The direction getter
 	 * 
 	 * @return the approximate direction of ObjInfo 
 	 */
 	public double getDirection() {
 		return direction;
 	}
 
 	/**
 	 * The direction setter
 	 */
 	public void setDirection(double direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * The distance change getter
 	 * 
 	 * @return the approximate distance change (magnitude of
 	 * velocity) of ObjInfo 
 	 */
 	public double getDistChng() {
 		return distChng;
 	}
 
 	/**
 	 * The distance change setter 
 	 */
 	public void setDistChng(double distChng) {
 		this.distChng = distChng;
 	}
 
 	/**
 	 * The direction change getter
 	 * 
 	 * @return the approximate direction change (direction of
 	 * velocity) of ObjInfo 
 	 */
 	public double getDirChng() {
 		return dirChng;
 	}
 
 	/**
 	 * The distance change setter 
 	 */
 	public void setDirChng(double dirChng) {
 		this.dirChng = dirChng;
 	}
 
 
 	private String ObjName;
 	private String side;
 	private double distance;
 	private double direction;
 	private double distChng;
 	private double dirChng;
 
 }
 
 /**
  * @class ObjBall
  * 
  * container for the ball ObjInfo,
  * 
  */
 class ObjBall extends ObjInfo {
 
 	public ObjBall() {
 		super("ball");
 	}
 }
 
 /**
  * @class ObjGoal
  * 
  * container for the goal ObjInfo,
  * 
  */
 class ObjGoal extends ObjInfo {
 
 	public ObjGoal() {
 		super("goal");
 	}
 }
 
 /**
  * @class ObjBall
  * 
  * container for the flag ObjInfo,
  * 
  */
 class ObjFlag extends ObjInfo {
 	
 	public ObjFlag() {
 		super("flag");
 	}
 
 	/**
 	 * Constructor of flag with flag name
 	 */
 	public ObjFlag(String name) {
 		super("flag");
 		flagName = name;
 	}
 	
 	/**
 	 * The Flag Type getter
 	 * 
 	 * @return The type of flag depending on it's location:
 	 * 				"b" - outer boundary
 	 * 				"g" - goal post
 	 * 				"p" - penalty box
 	 * 				"c" - center of field
 	 * 				"l" - border line
 	 */
 	public String getFlagType() {
 		return flagType;
 	}
 	
 	/**
 	 * The Flag Type setter
 	 */
 	public void setFlagType(String flagType) {
 		this.flagType = flagType;
 	}
 
 	/**
 	 * The Flag Name getter
 	 * 
 	 * @return The name of the flag, as given by the server but with no
 	 * spaces (e.g. flt20 for boundary flag left, top, 20 yard line)
 	 */
 	public String getFlagName() {
 		return flagName;
 	}
 	
 	/**
 	 * The Flag Name setter
 	 */
 	public void setFlagName(String name) {
 		this.flagType = name;
 	}
 	
 	/**
 	 * The X position getter
 	 * 
 	 * @return Either "l" for left, "r" for right, or "c" for center
 	 */
 	public String getX_pos() {
 		return x_pos;
 	}
 
 	/**
 	 * The X position setter
 	 */
 	public void setX_pos(String x_pos) {
 		this.x_pos = x_pos;
 	}
 
 	/**
 	 * The Y position getter
 	 * 
 	 * @return Either "t" for top, "b" for bottom, or "c" for center
 	 */
 	public String getY_pos() {
 		return y_pos;
 	}
 
 	/**
 	 * The Y position setter
 	 */
 	public void setY_pos(String y_pos) {
 		this.y_pos = y_pos;
 	}
 
 	/**
 	 * The yard getter
 	 * 
 	 * @return the yard is a String of a number for boundaries
 	 */
 	public String getYard() {
 		return yard;
 	}
 
 	/**
 	 * The yard setter
 	 */
 	public void setYard(String yard) {
 		this.yard = yard;
 	}
 
 	private String flagName;
 	private String flagType;
 	private String x_pos;
 	private String y_pos;
 	private String yard;
 }
 
 /**
  * @class ObjPlayer
  * 
  * container for player ObjInfo
  *
  */
 class ObjPlayer extends ObjInfo {
 
 	public ObjPlayer() {
		super("player");
 	}
 	
 	/**
 	 * The Team Name getter
 	 * 
 	 * @return the name of the team the player is on, if they're
 	 * close enough to see the team
 	 * 
 	 */
 	public String getTeam() {
 		return team;
 	}
 	
 	/**
 	 * The Team Name setter
 	 */
 	public void setTeam(String team) {
 		this.team = team;
 	}
 
 	/**
 	 * The Uniform Number getter
 	 * 
 	 * @return the Uniform Number on the player's shirt, if they're
 	 * close enough to see it
 	 * 
 	 */
 	public int getuNum() {
 		return uNum;
 	}
 
 	/**
 	 * The Uniform Number getter
 	 */
 	public void setuNum(int uNum) {
 		this.uNum = uNum;
 	}
 
 	/**
 	 * A check to see if the player is a goalie or field player
 	 * 
 	 * @return true if the player is the goalie, false if s/he is not
 	 */
 	public boolean isGoalie() {
 		return goalie;
 	}
 
 	/**
 	 * The goalie check setter
 	 */
 	public void setGoalie(boolean goalie) {
 		this.goalie = goalie;
 	}
 
 	/**
 	 * A getter for the player's head direction
 	 * 
 	 * @return a double of the angle, in degrees, of the direction of
 	 * the player's head relative to your own. The angle is 0 if they 
 	 * are both facing each other.
 	 */
 	public double getHeadDir() {
 		return headDir;
 	}
 
 	/**
 	 * The head direction setter
 	 */
 	public void setHeadDir(double headDir) {
 		this.headDir = headDir;
 	}
 
 	/**
 	 * A getter for the player's body direction
 	 * 
 	 * @return a double of the angle, in degrees, of the direction of
 	 * the player's body relative to your own. The angle is 0 if their
 	 * bodies are both facing each other.
 	 */
 	public double getBodyDir() {
 		return bodyDir;
 	}
 
 	/**
 	 * The body direction setter
 	 */
 	public void setBodyDir(double bodyDir) {
 		this.bodyDir = bodyDir;
 	}
 
 	private String team;
 	private int uNum;
 	private boolean goalie;
 	private double headDir;
 	private double bodyDir;
 	
 }
 
 /**
  * @class ObjLine
  * 
  * container for line ObjInfo
  *
  */
 class ObjLine extends ObjInfo {
 	
 	public ObjLine() {
 		super("line");
 	}
 }
