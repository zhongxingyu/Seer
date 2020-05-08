 import java.util.*;
 /**
 * @author Grant Hays
 * The Memory class stores instances of ObjMemory and SenseMemory and supplies
 * methods to access their innards.
 */
 public class Memory {
 	/**
 	* The default constructor for the Memory.
 	*
 	* This creates new, empty ArrayList for the ObjMemory and SenseMemory, initiates
 	* the time at 0 for both, and creates an ObjMemory and SenseMemory with the new
 	* ArrayLists and time as parameters.
 	*/
 	public Memory() {
 		ArrayList<ObjInfo> newObjArray = new ArrayList<ObjInfo>();
 		int time = 0;
 		ObjMem = new ObjMemory(newObjArray, time);
 		SenMem = new SenseMemory();
 	}
 	
 	public void setField(String side) {
 		f = new Field(side);
 		if(side.compareTo("l") == 0) {
 			oppGoal = getFlagPos("gr");
 			oppSide = "r";
 		}
 		else {
 			oppGoal = getFlagPos("gl");
 			oppSide = "l";
 		}
 	}
 	
 	
 	/**
 	* The ObjInfo getter
 	*
 	* This fetches the ObjInfo at index i of the ArrayList ObjArray in ObjMemory,
 	* and returns it as an ObjInfo.
 	*
 	* @param i the index number of the location of the desired ObjInfo in ObjArray
 	* @pre An index needs to be supplied when calling this
 	* @post A basic ObjInfo will be given.
 	* @return ObjInfo the ObjInfo at location i of the ObjArray
 	*/
 	// Get Object
 	public ObjInfo getObj(int i) {
 		return ObjMem.getObj(i);
 	}
 	
 	/**
 	* The ObjMemory size
 	*
 	* A getter to quickly retrieve the number of ObjInfo in ObjMemory
 	*
 	* @return size of ObjMemory
 	*/
 	public int getObjMemorySize() {
 		return ObjMem.getSize();
 	}
 	
 	/**
 	* Is this ObjInfo visible?
 	*
 	* @param name the ObjName of the ObjInfo we're detecting visibility of
 	* @return true if the ball is in the ObjMemory, false if it is not or
 	* if the the ObjMemory is empty
 	*/
 	public boolean isObjVisible(String name) {
 		if(ObjMem.getSize() == 0)
 			return false;
 		else {
 			for(int i = 0; i < ObjMem.getSize(); i++) {
 				if(getObj(i).getObjName().compareTo(name) == 0)
 					return true;
 			}
 			return false;
 		}
 	}
 	
 	/**
 	* The Ball Getter
 	*
 	* @pre Make sure you either check visibility first
 	* @post If the ball is in the Memory, it will be returned. Otherwise
 	* a Null ObjBall will be sent.
 	* @return ObjBall containing the ball
 	*/
 	public ObjBall getBall() {
 		for(int i = 0; i < ObjMem.getSize(); i++) {
 			if(getObj(i).getObjName().compareTo("ball") == 0)
 				return (ObjBall) getObj(i);
 		}
 		return null;
 	}
 	
 	/**
 	* The Flag Getter
 	*
 	* If you're looking for a specific flag, this is you're guy. You need to
 	* pass in the FlagName (i.e. flb30) into it, and out pops the ObjFlag
 	* with that FlagName attached to it.
 	*
 	* @pre Make sure you either check visibility first
 	* @post If the flag is in the Memory, it will be returned. Otherwise
 	* a Null ObjFlag will be sent.
 	* @return ObjFlag containing the flag with specified name
 	*/
 	public ObjFlag getFlag(String name) {
 		ObjFlag newFlag = new ObjFlag();
 		for(int i = 0; i < ObjMem.getSize(); i++) {
 			if(getObj(i).getObjName().compareTo("flag") == 0)
 				newFlag = (ObjFlag) getObj(i);
 			if(newFlag.getFlagName().compareTo(name) == 0)
 				return newFlag;
 		}
 		return null;
 	}
 	
 	/**
 	* The Goal Getter
 	*
 	* This will get the ObjGoal in your field of vision.
 	*
 	* @post If you're facing a goal, an ObjGoal with it's information will
 	* be returned. Otherwise a null ObjGoal will be sent
 	* @return ObjGoal containing the goal in your vision
 	*/
 	public ObjGoal getOppGoal() {
 		for(int i = 0; i < ObjMem.getSize(); i++) {
 			if((getObj(i).getObjName().compareTo("goal") == 0) && (getObj(i).getSide().compareTo(oppSide) == 0))
 				return (ObjGoal) getObj(i);
 		}
 		return null;
 	}
 	
 	public Pos getOppGoalPos() {
 		if(side.compareTo("l") == 0) 
 			return(getFlagPos("gr"));
 		else
 			return(getFlagPos("gl"));
 	}
 	
 	public ObjGoal getOwnGoal() {
 		for(int i = 0; i < ObjMem.getSize(); i++) {
 			if((getObj(i).getObjName().compareTo("goal") == 0) && (getObj(i).getSide().compareTo(side) == 0))
 				return (ObjGoal) getObj(i);
 		}
 		return null;
 	}
 	
 	public Pos getOwnGoalPos() {
 		if(side.compareTo("l") == 0) 
 			return(getFlagPos("gl"));
 		else
 			return(getFlagPos("gr"));
 	}
 	
 	/**
 	* The Player Getter
 	*
 	* This will get the ObjPlayer of the first player you see.
 	*
 	* @return ObjPlayer
 	*/
 	public ObjPlayer getPlayer() {
 		for(int i = 0; i < ObjMem.getSize(); i++) {
 			if(getObj(i).getObjName().compareTo("player") == 0)
 				return (ObjPlayer) getObj(i);
 		}
 		return null;
 	}
 	
 	/**
 	* The Line getter
 	* This will get the ObjLine of the first line you see.
 	*
 	* @return ObjLine
 	*/
 	public ObjLine getLine() {
 		for(int i = 0; i < ObjMem.getSize(); i++) {
 			if(getObj(i).getObjName().compareTo("line") == 0)
 				return (ObjLine) getObj(i);
 		}
 		return null;
 	}
 	
 	/**
 	* This will test a players local time against the ObjMemory's time. This
 	* can be used to ensure that more than one action will not be attempted
 	* during a single simulation step.
 	*
 	* @param t the Player's local time
 	* @pre A player's local time must be initialized and passed in
 	* @post The player's local time needs to be set to the Memory's time after
 	* a true is returned.
 	* @return true if the newly parsed Memory's time is greater than the players
 	* local time. False if the memory time is <= the local time.
 	*/
 	public boolean timeCheck(int t) {
 		if(t < ObjMem.getTime())
 			return true;
 		else
 			return false;
 	}
 	
 	
 	
 	public ArrayList<ObjPlayer> getPlayers() {
 		ArrayList<ObjPlayer> players = new ArrayList<ObjPlayer>();
 		for(int i = 0; i< ObjMem.getSize(); i++) {
 			if(getObj(i).getObjName().compareTo("player") == 0) {
 				players.add((ObjPlayer) getObj(i));
 			}				
 		}
 		return players;
 	}
 	
 	
 	
 	public ObjLine getClosestLine() {
 		ObjLine line = new ObjLine();
 		ObjLine closestLine = null;
 		double dist = 100.0;
 		
 		for(int i = 0; i < getObjMemorySize(); i++) {
 			if(getObj(i).getObjName().compareTo("line") == 0) {
 				line = (ObjLine) getObj(i);
 				if(line.getDistance() < dist) {
 					closestLine = line;
 				}
 			}
 		}
 		
 		return closestLine;
 	}
 	
 	public void setLocation(double x, double y) {
 		this.pos.x = x;
 		this.pos.y = y;
 	}
 	
 	public ObjFlag getClosestFlag() {
 		
 		ObjFlag flag = new ObjFlag();
 		ObjFlag closestFlag = null;
 		
 		double dist = 100.0;
 		
 		for(int i = 0; i < getObjMemorySize(); i++) {
 			
 			if(getObj(i).getObjName().compareTo("flag") == 0) {
 				flag = (ObjFlag) getObj(i);
 				if(flag.getDistance() < dist) {
 					closestFlag = flag;
 					dist = flag.getDistance();
 				}
 			}
 		}
 		
 		return closestFlag;
 	}
 	
 	public ObjFlag getClosestBoundary() {
 		
 		ObjFlag flag = new ObjFlag();
 		ObjFlag closestFlag = null;
 		
 		double dist = 100.0;
 		
 		for(int i = 0; i < getObjMemorySize(); i++) {
 			
 			if(getObj(i).getObjName().compareTo("flag") == 0) {
 				flag = (ObjFlag) getObj(i);
 				if((flag.getFlagType().compareTo("b") == 0) && (flag.getDistance() < dist)) {
 					closestFlag = flag;
 					dist = flag.getDistance();
 				}
 			}
 		}
 		
 		return closestFlag;
 	}
 	
 	public ObjFlag getClosestPenaltyFlag() {
 		
 		ObjFlag flag = new ObjFlag();
 		ObjFlag closestFlag = null;
 		
 		double dist = 100.0;
 		
 		for(int i = 0; i < getObjMemorySize(); i++) {
 			
 			if(getObj(i).getObjName().compareTo("flag") == 0) {
 				flag = (ObjFlag) getObj(i);
 				if((flag.getFlagType().compareTo("p") == 0) && (flag.getDistance() < dist)) {
 					closestFlag = flag;
 					dist = flag.getDistance();
 				}
 			}
 			
 		}
 		
 		return closestFlag;
 	}
 	
 	public Pos getFlagPos(String flagName) {
 		for(int i = 0; i < f.posList.size(); i++) {
 			if(f.posList.get(i).name.compareTo(flagName) == 0)
 				return f.posList.get(i);
 		}
 		
 		return null;
 		
 	}
 	
 	public double getDirection() {
 		ObjLine line = getClosestLine();
 		
 		if(line.getSide().compareTo("t") == 0) {
 			if(line.getDirection() > 0)
 				return(-1 * line.getDirection());
 			else
 				return(-180 - line.getDirection());
 		}
 		
 		else if(line.getSide().compareTo("b") == 0) {
 			if(line.getDirection() < 0)
 				return(-1 * line.getDirection());
 			else
 				return(180 - line.getDirection());
 		}		
 		
 		else if(line.getSide().compareTo(side) == 0) {
 			if(Math.abs(line.getDirection()) == 90.0)
 				return(180.0);
 			else if(line.getDirection() > 0)
 				return(-90 - line.getDirection());
 			else if(line.getDirection() < 0)
 				return(90 - line.getDirection());
 		}
 		
 		else if(line.getSide().compareTo(oppSide) == 0){
 			if(Math.abs(line.getDirection()) == 90.0)
 				return(0.0);
 			else if(line.getDirection() > 0)
 				return(90 - line.getDirection());
 			else if(line.getDirection() < 0)
 				return(-90 - line.getDirection());
 		}
 		
 		return(0.0);
 	}
 	
 	public Pos getPosition() {
 			
 			ObjFlag flag = getClosestFlag();
 			
 			
 			//System.out.println("getPosition flag: (" + flag.getDistance() + ", " + flag.getDirection() + ")");
 			{
 				
 				Pos flagCoord = getFlagPos(flag.getFlagName());
 				Pos toFlag = m.getPos(flag.getDistance(), getDirection() + flag.getDirection());
 				Pos self = m.vSub(flagCoord, toFlag);
 				/*
 				System.out.println("****************************************");
 				System.out.println("Penalty Flag (" + flag.getFlagName() + "): (" + flagCoord.x + ", " + flagCoord.y + ")");
 				System.out.println("Flag Polar: (" + flag.getDistance() + ", " + flag.getDirection() + ")");
 				System.out.println("DirectionOfSpeed: " + getDirection());
 				System.out.println("My Position: (" + self.x + ", " + self.y + ")");
 				*/
 				
 				return(self);
 				
 			}
 			
 	}
 	
 	
 	// ******************* SenseMemory *******************
 	/**
 	* The getter for the Player's stamina
 	*/
 	public double getStamina(){
 		return SenMem.stamina;
 	}
 	
 	/**
 	* The getter for the Player's stamina recovery
 	*/
 	public double getRecovery(){
 		return SenMem.recovery;
 	}
 	
 	/**
 	* The getter for the Player's stamina effort
 	*/
 	public double getEffort() {
 		return SenMem.effort;
 	}
 	
 	/**
 	* The getter for the magnitude of the Player's velocity
 	*/
 	public double getAmountOfSpeed() {
 		return SenMem.amountOfSpeed;
 	}
 	
 	/**
 	* The getter for the direction of the Player's velocity
 	*/
 	public double getDirectionOfSpeed() {
 		if(SenMem.directionOfSpeed == 0)
 			return 0.0;
 		else
 			return (-1 * SenMem.directionOfSpeed);
 	}
 	
 	/**
 	* The getter for the angle of the Player's head relative to
 	* the orientation of the Player's positive y-axis (up-field)
 	*/
 	public double getHeadDirection() {
 		return SenMem.headDirection;
 	}
 	
 	// ****************** Hear Memory ********************
 	
 	/**
 	* The getter for the game's current play mode
 	*/
 	public String getPlayMode() {
 		return playMode;
 	}
 	
 	// ***************** Class Variables *****************
 	
 	public Field f;
 	public Pos pos;
 	
 	/**
 	* The memory that stores all parsed ObjInfo
 	*/
 	public ObjMemory ObjMem;
 	/**
 	* The memory that stores all parsed SenseInfo
 	*/
 	public SenseMemory SenMem;
 	/**
 	* The play mode as told by the referee
 	*/
 	public String playMode;
 	
 	public String oppSide;
 	public String side;
 	public Double uNum;
 	public Pos oppGoal;
 
 }
