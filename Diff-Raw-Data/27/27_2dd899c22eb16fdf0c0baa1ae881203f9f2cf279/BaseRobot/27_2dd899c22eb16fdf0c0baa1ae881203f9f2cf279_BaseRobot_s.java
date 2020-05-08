 package current;
 
 import battlecode.common.*;
 
 public class BaseRobot
 {
 	// Subsystems
 	public final RobotController rc;
 	public final Utilities util;
 	public final Communicator comm;
	public final Navigator nav;
 	public final Combat combat;
 	
 	// State data
 	public final int maph, mapw;
 	
 	// Robot data
 	public final int id;
 	public final int spawnRound;
 	public final RobotType myType;
 	
 	// Team constants
 	public final Team myTeam;
 	public final Team enemyTeam;
 	
 	// State variables
 	public int curRound;
 	public MapLocation curLoc;
 
 	BaseRobot(RobotController myRC){
 		// Initialize subsystems
 		this.rc = myRC;
 		this.util = new Utilities(this);
 		this.comm = new Communicator(this);
		this.nav = new Navigator(this);
 		this.combat = new Combat(this);
 		
 		// Initialize data
 		this.id = rc.getRobot().getID();
 		this.spawnRound = Clock.getRoundNum();
 		this.myType = rc.getType();
 		
 		// Initialize team objects
 		this.myTeam = rc.getTeam();
 		this.enemyTeam = myTeam.opponent();
 		
 		// Initialize state variables
 		this.maph = rc.getMapHeight();
 		this.mapw = rc.getMapWidth();
 	}
 	
 	public void run() throws GameActionException{
 		this.curLoc = rc.getLocation();
 	}
 
 }
