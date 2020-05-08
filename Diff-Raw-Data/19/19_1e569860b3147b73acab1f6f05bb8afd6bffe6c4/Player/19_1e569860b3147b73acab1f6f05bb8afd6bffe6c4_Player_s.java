 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 
 /** @file Player.java
 * Class file for Player class
 * @author Joel Tanzi
 * @date 11 October 2011
 * @version 1.0 
 */
 
 /** @class Player
 * The Player class defines all objects and methods used for the 
 * Player within the RoboCup match.  The Player establishes a connection
 * to the server, initializes itself on the team, and 
 * performs all actions related to a RoboCup soccer player such as
 * (but not limited to) kicking, dashing, dribbling, passing and scoring. 
 * The Player class has a Memory for storing the current RoboCup worldstate.
 * It reacts to stimuli based on strategies provided by the Brain (TBD). 
 */
 public class Player {
 
 	protected RoboClient rc = new RoboClient();
 	private Memory m = new Memory();
 	private ObjInfo i = new ObjInfo();
 	private Parser p = new Parser();
 	private Brain b = new Brain();
<<<<<<< HEAD
 	private Action a = new Action(m, rc, p);
=======
	private Action a = new Action(m, rc);
>>>>>>> 1bbb2ad4d3dae0e42c891a5cc61d5c0d6e86ef60
 	private int time = 0;
 	
 	
 	public Player() {
 		
 	}
 	
 
 	/**
 	 * @param rc
 	 * @param m
 	 * @param i
 	 * @param p
 	 * @param b
 	 * @param time
 	 */
 	public Player(RoboClient rc, Memory m, ObjInfo i, Parser p, Brain b,
 			int time) {
 		super();
 		this.rc = rc;
 		this.m = m;
 		this.i = i;
 		this.p = p;
 		this.b = b;
 		this.time = time;
 	}
 
 	/**
 	 * @return the b
 	 */
 	public Brain getBrain() {
 		return b;
 	}
 
 	/**
 	 * @param b the b to set
 	 */
 	public void setBrain(Brain b) {
 		this.b = b;
 	}
 
 	public Action getAction() {
 		return a;
 	}
 	
 	public void setAction(Action a) {
 		this.a = a;
 	}
 	
 	/**
 	 * @return The RoboClient object for this Player.
 	 */
 	public RoboClient getRoboClient() {
 		return rc;
 	}
 
 	/**
 	 * @param rc The RoboClient to set.
 	 */
 	public void setRoboclient(RoboClient rc) {
 		this.rc = rc;
 	}
 
 	/**
 	 * @return The Memory for this Player.
 	 */
 	public Memory getMem() {
 		return m;
 	}
 
 	/**
 	 * @param m The Memory to set.
 	 */
 	public void setMem(Memory m) {
 		this.m = m;
 	}
 
 	/**
 	 * @return The ObjInfo for this Player.
 	 */
 	public ObjInfo getObjInfo() {
 		return i;
 	}
 
 	/**
 	 * @param i The ObjInfo to set.
 	 */
 	public void setObjInfo(ObjInfo i) {
 		this.i = i;
 	}
 
 	/**
 	 * @return The Parser for this Player.
 	 */
 	public Parser getParser() {
 		return p;
 	}
 
 	/**
 	 * @param p The Parser to set.
 	 */
 	public void setParser(Parser p) {
 		this.p = p;
 	}
 
 	/**
 	 * @return the time
 	 */
 	public int getTime() {
 		return time;
 	}
 
 
 	/**
 	 * @param time the time to set
 	 */
 	public void setTime(int time) {
 		this.time = time;
 	}
 
 
 	/**
 	 * Initializes the Player with the RoboCup server.
 	 * @pre A RoboCup server is available.
 	 * @post The Player has been initialized to the correct team.
 	 */
 	public void initPlayer() throws SocketException, UnknownHostException {
 		
 		rc.dsock = new DatagramSocket();
 		rc.init(getParser(), getMem());
 	}
 	
 	
 	 /**
 	 * Receives worldstate data from the RoboCup server.
 	 * @pre A RoboCup server is available.
 	 * @post The current worldstate has been stored in the Memory.
 	 */
 	public void receiveInput() throws InterruptedException  {		
 		p.Parse(rc.receive(), m);
 	}
 	
 	 /**
 	 * Teleports the Player to the specified coordinates.
 	 * @param x x-coordinate of the point to move the player to.
 	 * @param y y-coordinate of the point to move the player to.
 	 * @throws InterruptedException 
 	 * @pre Playmode is before-kickoff, goal-scored, free-kick.
 	 * @post The Player has been moved to the correct position.
 	 */
 	public void move(int x, int y) throws UnknownHostException, InterruptedException {
 		rc.move(x, y);
 	}
 	
 	 /**
 	 * Causes Player to kick the ball.
 	 * @param dir The direction in which to kick the ball in the form of a decimal value. 
 	 * representing the angle in degrees in relation go the player.
 	 * @param power The power with which to kick the ball in the form of a decimal value.
 	 * @throws InterruptedException 
 	 * @pre Playmode is play_on, ball is in kickable range.
 	 * @post The ball has been kicked in the specified direction and power.
 	 */
 	public void kick(double power, double dir) throws UnknownHostException, InterruptedException {
 		rc.kick(power, dir);
 	}
 	
 	public void dash(double power) throws Exception {
 		rc.dash(power);
 	}
 	
 	 /**
 	 * Causes Player to turn according to a specified turn moment.
 	 * @param moment The turn angle in degrees. 
 	 * @throws InterruptedException 
 	 * @pre Playmode is play_on, ball is in kickable range.
 	 * @post The ball has been kicked in the specified direction and power.
 	 */
 	public void turn(double moment) throws UnknownHostException, InterruptedException {
 		rc.turn(moment);
 	}
 	
 	 /**
 	 * Causes Player to say the given message.  It has a limitation of 512 characters by default.
 	 * @param message The string to be spoken by the player. 
 	 * @throws InterruptedException 
 	 * @pre None
 	 * @post The player has spoken the message.
 	 */	
 	public void say(String message) throws UnknownHostException, InterruptedException {
 		rc.say(message);
 	}
 	
 	
 	public void markOpponent(String team, String number) {
 		b.setMarked_team(team);
 		b.setMarked_unum(number);
 	}
 	
 	public void runDefense() {
 		b.setDefensive();
 		if (m.isObjVisible("player")) {
 			markOpponent(m.getPlayer().getTeam(), Integer.toString(m.getPlayer().getuNum()));
 			System.out.println("Marked Player " + b.getMarked_team() + " " + b.getMarked_unum());
 		}		
 	}
 	
 }
