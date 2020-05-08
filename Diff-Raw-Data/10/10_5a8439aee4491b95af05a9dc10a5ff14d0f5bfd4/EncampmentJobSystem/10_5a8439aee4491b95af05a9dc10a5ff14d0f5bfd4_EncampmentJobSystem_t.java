 package base;
 
 import battlecode.common.Clock;
 import battlecode.common.GameActionException;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 
 /**
  * TODO: make the HQ update every cycle call, but have a modified update
  * where we check the previous cycle's channels to see if any jobs weren't taken
  * 
  * if any jobs were not taken, then copy the message over to the new channels
  * 
  * Also check whether or not this repair fixes the other issue with the 
  * completion channel half of this problem. Previous solution was to update every time it spawns.
  * @author asdfryan
  *
  */
 public class EncampmentJobSystem {
 	public static BaseRobot robot;
 	public static RobotController rc;
 	public static MapLocation goalLoc;
 	
 	public static ChannelType[] encampmentJobChannelList = 
 		{ChannelType.ENC1,
 		ChannelType.ENC2,
 		ChannelType.ENC3,
 		ChannelType.ENC4,
 		ChannelType.ENC5}; // list of encampment channels
 
 	public static ChannelType[] encampmentCompletionChannelList=
 		{ChannelType.COMP1,
 		ChannelType.COMP2,
 		ChannelType.COMP3,
 		ChannelType.COMP4,
 		ChannelType.COMP5}; // list of encampment completion channels
 	
 	public static int maxEncampmentJobs = encampmentJobChannelList.length;
 	public static int maxMessage = Constants.MAX_MESSAGE; //24-bit message all 1s
 	
 	
 	// these two arrays must have bijective correspondence
 	// the job associated with encampmentJobs[i] must always write to channelList[i] for each i
 	public static MapLocation[] encampmentJobs = new MapLocation[EncampmentJobSystem.maxEncampmentJobs]; // length is constant
 	public static ChannelType[] encampmentChannels = new ChannelType[EncampmentJobSystem.maxEncampmentJobs]; // length is constant
 
 	public static int numEncampmentsNeeded; // must be less than encampmentJobChannelList.length
 	
 	public static MapLocation[] unreachableEncampments;
 	public static int numUnreachableEncampments;
 	
 	public static MapLocation HQLocation;
 	public static MapLocation EnemyHQLocation;
 	
 	public static RobotType assignedRobotType;
 	public static ChannelType assignedChannel;
 	
 	public static int encampmentRadius;
 	
 	public static int supCount;
 	public static int genCount;
 	
 	/**
 	 * Initializes BroadcastSystem by setting rc
 	 * @param myRobot
 	 */
 	public static void init(BaseRobot myRobot) {
 		robot = myRobot;
 		rc = robot.rc;
 	}
 	
 	public static void initializeConstants(MapLocation hqloc, MapLocation enemyloc) throws GameActionException {
 		HQLocation = hqloc;
 		EnemyHQLocation = enemyloc;
 		numEncampmentsNeeded = Constants.INITIAL_NUM_ENCAMPMENTS_NEEDED; 
 		numUnreachableEncampments = 0;
 		unreachableEncampments = new MapLocation[100];
 		int rushDist = hqloc.distanceSquaredTo(enemyloc);
 		encampmentRadius = (int) (0.6 * rushDist);
 		supCount = 0;
 		genCount = 0;
 		
 		MapLocation[] allEncampments = rc.senseEncampmentSquares(hqloc, encampmentRadius, Team.NEUTRAL);
		if (allEncampments.length == 0) {
			numEncampmentsNeeded = 0;
		} else if (allEncampments.length < 10) {
 			numEncampmentsNeeded = 1;
 		} else if (allEncampments.length < 30) {
 			numEncampmentsNeeded = 2;
 		} else {
 			numEncampmentsNeeded = 3;
 		}
 
 		MapLocation[] closestEncampments = getClosestMapLocations(HQLocation, allEncampments, numEncampmentsNeeded);
 
 		for (int i=0; i<numEncampmentsNeeded; i++) {
 			// save in list of jobs
 			encampmentJobs[i] = closestEncampments[i];
 
 			// broadcast job opening
 			encampmentChannels[i] = encampmentJobChannelList[i];
			
			postJob(EncampmentJobSystem.encampmentChannels[i], encampmentJobs[i], getRobotTypeToBuild());
 		}
 	}
 	
 	/**
 	 * HQ uses this to post a new job, and increments genCount and supCount
 	 * @param channel
 	 * @param loc
 	 */
 	public static void postJob(ChannelType channel, MapLocation loc, int robType) {
 		BroadcastSystem.write(channel, createMessage(loc, false, true, robType));
 		if (robType == 1){
 			genCount++;
 		} else if (robType == 0){
 			supCount++;
 		}
 	}
 	
 	/**
 	 * same as above, except without incrementing
 	 * @param channel
 	 * @param loc
 	 */
 	public static void postJobWithoutIncrementing(ChannelType channel, MapLocation loc, int robType) {
 		BroadcastSystem.write(channel, createMessage(loc, false, true, robType));
 	}
 	
 	/**
 	 * Soldiers use this to update the job to claim that it's taken
 	 * They also use this every turn to ping back to others
 	 * @param channel
 	 */
 	public static void updateJobTaken() {
 		int robotTypeInt = 0;
 		if (assignedRobotType == RobotType.GENERATOR){
 			robotTypeInt = 1;
 		}
 		BroadcastSystem.write(assignedChannel, createMessage(goalLoc, true, true, robotTypeInt));
 	}
 	
 	/**
 	 * Soldiers use this to find an untaken job or a taken job that has been inactive
 	 * @return ChannelType channel
 	 * @throws GameActionException 
 	 */
 	public static ChannelType findJob() throws GameActionException {
 		int currentRoundNum = Clock.getRoundNum();
 		for (ChannelType channel: encampmentJobChannelList) {
 //			System.out.println("findChannel: " + channel);
 			Message message = BroadcastSystem.read(channel);
 			System.out.println("findJobChannel: " + channel.toString());
 			System.out.println("channelIsValid: " + message.isValid);
 			System.out.println("channelBody: " + message.body);
 
 			if (message.isValid && message.body != maxMessage) {
 //				rc.setIndicatorString(0, Integer.toString(message.body));
 				int onOrOff = parseOnOrOff(message.body);
 				int isTaken = parseTaken(message.body);
 				if (onOrOff == 1 && isTaken == 0) { //if job is on and untaken
 					goalLoc = parseLocation(message.body);
 					if (rc.canSenseSquare(goalLoc)) {
 						GameObject robotOnSquare = rc.senseObjectAtLocation(goalLoc);
 						if (robotOnSquare == null || !robotOnSquare.getTeam().equals(rc.getTeam())) {
 							assignedRobotType = parseRobotType(message.body);
 							assignedChannel = channel;
 							return channel;
 						}
 					} else {
 						assignedRobotType = parseRobotType(message.body);
 						assignedChannel = channel;
 						return channel;
 					}
 				} else if (onOrOff == 1) {
 					int postedRoundNum = parseRoundNum(message.body);
 					if ((16+currentRoundNum - postedRoundNum)%16 >= 4) { // it hasn't been written on for 5 turns
 						goalLoc = parseLocation(message.body);
 //						System.out.println("posted: " + postedRoundNum);
 //						System.out.println("current: " + currentRoundNum % 16);
 //
 						if (rc.canSenseSquare(goalLoc)) {
 							GameObject robotOnSquare = rc.senseObjectAtLocation(goalLoc);
 							if (robotOnSquare == null || !robotOnSquare.getTeam().equals(rc.getTeam())) {
 								assignedRobotType = parseRobotType(message.body);
 								assignedChannel = channel;
 								return channel;
 							}
 						} else {
 							assignedRobotType = parseRobotType(message.body);
 							assignedChannel = channel;
 							return channel;
 						}
 
 					}
 				}
 
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Encampments use this at its birth to indicate to the HQ
 	 * that the encampment is finished
 	 * @param currLoc
 	 * @return ChannelType
 	 */
 	public static void postCompletionMessage(MapLocation currLoc) {
 		forloop: for (ChannelType channel: encampmentCompletionChannelList) {
 			Message message = BroadcastSystem.read(channel);
 			if (message.isUnwritten || (message.isValid && message.body == maxMessage)){
 				int newmsg = (currLoc.x << 8) + currLoc.y;
 				BroadcastSystem.write(channel, newmsg);
 				break forloop; 
 			}
 		}
 	}
 	
 	public static void postUnreachableMessage(MapLocation goalLoc) {
 		forloop: for (ChannelType channel: encampmentCompletionChannelList) {
 			Message message = BroadcastSystem.read(channel);
 			if (message.isUnwritten || (message.isValid && message.body == maxMessage)){
 				int newmsg = (1 << 16) + (goalLoc.x << 8) + goalLoc.y;
 //				System.out.println("unreachable msg: " + newmsg);
 				BroadcastSystem.write(channel, newmsg);
 				break forloop; 
 			}
 		}
 	}
 	
 	/**
 	 * In the round after its birth (or 5 after), the encampment "cleans up after itself"
 	 * and posts 0 in the channel
 	 * @param channel
 	 */
 	public static void postCleanUp(ChannelType channel) {
 		BroadcastSystem.writeMaxMessage(channel);
 	}
 	
 	/**
 	 * HQ uses this to see if a certain channel contains a maplocation
 	 * of a certain completed encampment. Returns null if 
 	 * @param channel
 	 * @return
 	 */
 	public static EncampmentJobMessageType checkCompletion(ChannelType channel) {
 		Message message = BroadcastSystem.read(channel);
 		if (message.isValid && message.body != maxMessage) {
 			
 			int locY = message.body & 0xFF;
 			int locX = (message.body >> 8) & 0xFF;
 			int unreachableBit = message.body >> 16;
 			postCleanUp(channel); // cleanup
 //			System.out.println("locy: " + locY + " locx: " + locX);
 			if (unreachableBit == 1) { // if unreachable
 //				System.out.println("unreachable!!!");
 				unreachableEncampments[numUnreachableEncampments] = new MapLocation(locY, locX);
 				numUnreachableEncampments++;
 				return EncampmentJobMessageType.FAILURE;
 			} else { // it's a completion message
 				return EncampmentJobMessageType.COMPLETION;
 			}
 		}
 		return EncampmentJobMessageType.EMPTY;
 	}
 	
 	/**
 	 * HQ uses this to see if a certain channel contains a maplocation
 	 * of a certain completed encampment. Returns null if 
 	 * @param channel
 	 * @return
 	 */
 	public static EncampmentJobMessageType checkCompletionOnCycle(ChannelType channel) {
 		Message message = BroadcastSystem.readLastCycle(channel);
 		if (message.isValid && message.body != maxMessage) {
 			
 			int locY = message.body & 0xFF;
 			int locX = (message.body >> 8) & 0xFF;
 			int unreachableBit = message.body >> 16;
 //			System.out.println("locy: " + locY + " locx: " + locX);
 			if (unreachableBit == 1) { // if unreachable
 				unreachableEncampments[numUnreachableEncampments] = new MapLocation(locY, locX);
 				numUnreachableEncampments++;
 				return EncampmentJobMessageType.FAILURE;
 			} else { // it's a completion message
 				return EncampmentJobMessageType.COMPLETION;
 			}
 		}
 		return EncampmentJobMessageType.EMPTY;
 	}
 	
 	
 	/** 
 	 * Returns a boolean: true if one of the channels has a 
 	 * completion or a failure message, false otherwise
 	 * @return
 	 */
 	public static boolean checkAllCompletion() {
 		for (ChannelType channel: encampmentCompletionChannelList) {
 			EncampmentJobMessageType msgType = checkCompletion(channel);
 			if (msgType != EncampmentJobMessageType.EMPTY) { // if a completion or a failure message
 				return true;
 			} 
 		}
 
 		return false;
 	}
 	
 	public static boolean checkAllCompletionOnCycle() {
 		for (ChannelType channel: encampmentCompletionChannelList) {
 			EncampmentJobMessageType msgType = checkCompletionOnCycle(channel);
 			if (msgType != EncampmentJobMessageType.EMPTY) { // if a completion or a failure message
 				return true;
 			} 
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * The HQ calls this at the beginning of every broadcast cycle
 	 * to persist old job messages that were untaken
 	 */
 	public static void persistChannelsOnCycle() {
 		for (ChannelType channel: encampmentChannels) {
 			Message msgLastCycle = BroadcastSystem.readLastCycle(channel);
 			System.out.println("isValid: " + msgLastCycle.isValid);
 			System.out.println("body: " + msgLastCycle.body);
 
 			if (msgLastCycle.isValid && msgLastCycle.body != maxMessage) {
 				// check if the job was on and was untaken
 				if (parseOnOrOff(msgLastCycle.body) == 1 && parseTaken(msgLastCycle.body) == 0) {
 					System.out.println("hello!!!!!");
 					System.out.println("channel: " + channel.toString());
 					int robotTypeToBuild = getRobotTypeToBuild();
 					postJobWithoutIncrementing(channel, parseLocation(msgLastCycle.body), robotTypeToBuild);
 				} else if (parseOnOrOff(msgLastCycle.body) == 1 && parseTaken(msgLastCycle.body) == 1) {
 					int postedRoundNum = parseRoundNum(msgLastCycle.body);
 					if ((16+Clock.getRoundNum() - postedRoundNum)%16 >= 4) { // it hasn't been written on for 5 turns
 						System.out.println("hello!!!!!");
 						System.out.println("channel: " + channel.toString());
 						int robotTypeToBuild = getRobotTypeToBuild();
 						postJobWithoutIncrementing(channel, parseLocation(msgLastCycle.body), robotTypeToBuild);
 					}
 				}
 
 			}
 		}
 	}
 	/**
 	 * returns new list of channels corresponding to the new jobs
 	 * that will maintain the same channels for the old jobs
 	 * but find new channels for the new ones
 	 * @param newJobsList
 	 * @param oldJobsList
 	 * @param oldChannelsList
 	 * @return
 	 */
 	public static ChannelType[] assignChannels(MapLocation[] newJobsList, MapLocation[] oldJobsList, ChannelType[] oldChannelsList) {
 		ChannelType[] channelList = new ChannelType[maxEncampmentJobs];
 
 		int arrayIndices[] = new int[newJobsList.length];
 
 		for (int i=0; i<newJobsList.length; i++) {
 			arrayIndices[i] = arrayIndex(newJobsList[i], oldJobsList);
 		}
 
 
 		int channelIndex = -1;
 		// keep old channels for non-new jobs
 		for (int i=0; i<newJobsList.length; i++) {
 			if (arrayIndices[i] != -1 && newJobsList[i] != null) { // if the job is not new, keep the same channel
 				channelList[i] = oldChannelsList[arrayIndices[i]];
 			}
 		}
 		int alliedNumEncampments = rc.senseAlliedEncampmentSquares().length;
 		// allocate unused channels for new jobs
 		for (int i=0; i<newJobsList.length; i++) {
 			if (arrayIndices[i] == -1 && newJobsList[i] != null) {
 				channelLoop: for (ChannelType channel: encampmentJobChannelList) {
 					channelIndex = arrayIndex(channel, channelList);
 					if (channelIndex == -1) { // if not already used, use it and post job
 						channelList[i] = channel;
 						int robotTypeToBuild = getRobotTypeToBuild();
 						EncampmentJobSystem.postJob(channel, newJobsList[i], robotTypeToBuild);
 						break channelLoop;
 					}
 				}
 			}
 		}
 		return channelList;
 	}
 	
 	/** 
 	 * returns index of element e in array
 	 * @param e
 	 * @param array
 	 * @return
 	 */
 	public static int arrayIndex(Object e, Object[] array) {
 		if (e == null) {
 			return -1;
 		}
 
 		for (int i = 0; i<array.length; i++) {
 			if (array[i] != null) {
 				if (array[i].equals(e)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * checks nearby encampments to see if jobs need to be changed
 	 * @throws GameActionException
 	 */
 	public static void updateJobs() throws GameActionException {
 		
 		System.out.println("numEncampmentsNeeded: " + numEncampmentsNeeded);
 		System.out.println("numUnreachableEncampments: " + numUnreachableEncampments);
 		
 //		System.out.println("Before update: " + Clock.getBytecodeNum());
 		MapLocation[] neutralEncampments = rc.senseEncampmentSquares(HQLocation,encampmentRadius, Team.NEUTRAL);
 		if (numEncampmentsNeeded > neutralEncampments.length){
 			numEncampmentsNeeded = neutralEncampments.length;
 		}
 		
 		if (numEncampmentsNeeded != 0) {
 			MapLocation[] newJobsList = getClosestMapLocations(HQLocation, neutralEncampments, numEncampmentsNeeded);
 //			System.out.println("new jobs list: " + Clock.getBytecodeNum());
 
 			ChannelType[] channelList = EncampmentJobSystem.assignChannels(newJobsList, EncampmentJobSystem.encampmentJobs, EncampmentJobSystem.encampmentChannels);
 
 			for (int i=0; i<EncampmentJobSystem.numEncampmentsNeeded; i++) { // update lists
 				EncampmentJobSystem.encampmentJobs[i] = newJobsList[i];
 				System.out.println("encampmentJobs.x: " + encampmentJobs[i].x);
 				System.out.println("encampmentJobs.y: " + encampmentJobs[i].y);
 
 				EncampmentJobSystem.encampmentChannels[i] = channelList[i];
 			}
 
 //			System.out.println("update lists: " + Clock.getBytecodeNum());
 
 			// clear unused channels
 			for (ChannelType channel: EncampmentJobSystem.encampmentJobChannelList) {
 				if (arrayIndex(channel, EncampmentJobSystem.encampmentChannels) == -1) { // if unused
 					System.out.println("channel overwrite: " + channel.toString());
 					BroadcastSystem.writeMaxMessage(channel); // reset the channel
 				}
 			}
 		}
 
 //		System.out.println("After update: " + Clock.getRoundNum());
 	}
 	/**
 	 * HQ uses this to check if any jobs are completed, and then updates new jobs
 	 * @throws GameActionException
 	 */
 	public static void updateJobsAfterChecking() throws GameActionException {
 		if (EncampmentJobSystem.checkAllCompletion()) { // if it contains non-null elements
 			updateJobs();
 		}
 
 	}
 	
 	public static void updateJobsOnCycle() throws GameActionException {
 		System.out.println("callpersist");
 		persistChannelsOnCycle();
 		checkAllCompletionOnCycle();
 		updateJobs();
 	}
 	
 	/**
 	 * Finds array of closest MapLocations to rc.getLocation()
 	 * in decreasing order
 	 * 
 	 * Specification: k must be <= allLoc.length
 	 * Specification: array must not contain origin
 	 * @param origin
 	 * @param allLoc
 	 * @param k
 	 * @return
 	 * 
 	 * This still costs lots of bytecodes
 	 */
 	public static MapLocation[] getClosestMapLocations(MapLocation origin, MapLocation[] allLoc, int k) {
 		MapLocation[] currentTopLocations = new MapLocation[k];
 		
 		// make arrays of 0s and 1s corresponding to allLoc where 1 if unreachable
 		int[] unreachableCheckArray = new int[allLoc.length];
 		for (int j=0; j<numUnreachableEncampments; j++) {
 			for (int i=0; i<allLoc.length; i++) {
 				if (allLoc[i].equals(unreachableEncampments[j])) {
 					unreachableCheckArray[i] = 1;
 				}
 			}
 		}
 
 		int[] allDistances = new int[allLoc.length];
 		for (int i=0; i<allLoc.length; i++) {
 			allDistances[i] = origin.distanceSquaredTo(allLoc[i]);
 		}
 
 		int[] allLocIndex = new int[allLoc.length];
 
 		int runningDist = 1000000;
 		MapLocation runningLoc = null;
 		int runningIndex = 0;
 		for (int j = 0; j < k; j++) {
 			runningDist = 1000000;
 			for (int i=0; i<allLoc.length; i++) {
 				if (allDistances[i] < runningDist && allLocIndex[i] == 0 && unreachableCheckArray[i] == 0) {
 					runningDist = allDistances[i];
 					runningLoc = allLoc[i];
 					runningIndex = i;
 				}
 			}
 			currentTopLocations[j] = runningLoc;
 			allLocIndex[runningIndex] = 1;
 		}
 
 		return currentTopLocations;
 	}
 	
 	
 	public static int getRobotTypeToBuild() {
 		if (supCount == 0 && genCount == 0) {
 			return 0;
 		}
 		double supRatio = (supCount*1.0)/(supCount*1.0 + genCount*1.0);
 		if (supRatio > 0.8) {
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 	
 	/**
 	 * Creates a 24-bit job message to send from the goal location
 	 * @param goalLoc
 	 * @return
 	 */
 	public static int createMessage(MapLocation goalLoc, boolean isTaken, boolean onOrOff, int robType) {
 		int msg = robType << 22;
 		
 		msg += (goalLoc.x << 14);
 		msg += (goalLoc.y << 6);
 		if (isTaken) {
 			msg += 0x20;
 		}
 		if (onOrOff) {
 			msg += 0x10;
 		}
 		msg += Clock.getRoundNum() % 16;
 		
 		return msg;
 	}
 	
 	/**
 	 * Parses a 24-bit job-message
 	 * @param msgBody
 	 * @return an array containing round mod 16, onOrOff, job taken or not, y-coord, x-coord, and encampment type
 	 */
 	public static int[] parseEntireJobMessage(int msgBody) {
 		int[] output = new int[6];
 		output[0] = msgBody & 0xF; // round # mod 16
 		output[1] = (msgBody >> 4) & 0x1; // on or off
 		output[2] = (msgBody >> 5) & 0x1; // job taken or not
 		output[3] = (msgBody >> 6) & 0xFF; // y-coord of location
 		output[4] = (msgBody >> 14) & 0xFF; // x-coord of location
 		output[5] = (msgBody >> 22);
 		
 		return output;
 	}
 	
 	public static int parseRoundNum(int msgBody) { // round number mod 16
 		return msgBody & 0xF;
 	}
 	
 	public static int parseOnOrOff(int msgBody) { // job on or off
 		return (msgBody >> 4) & 0x1;
 	}
 	
 	public static int parseTaken(int msgBody) { // job taken or not
 		return (msgBody >> 5) & 0x1; 
 	}
 	
 	public static MapLocation parseLocation(int msgBody) {
 		int y = (msgBody >> 6) & 0xFF; // y-coord of location
 		int x = (msgBody >> 14) & 0xFF; // x-coord of location
 		return new MapLocation(x,y);
 	}
 	
 	public static RobotType parseRobotType(int msgBody) {
 		int robotTypeInt = msgBody >> 22;
 		if (robotTypeInt == 0) {
 			return RobotType.SUPPLIER;
 		} else {
 			return RobotType.GENERATOR;
 		}
 	}
 	
 //	public static void main(String[] arg0) {
 //		int[] output = parseEntireJobMessage(0b0000111100001110011111);
 //		System.out.println("parsed: " + output[0]);
 //		System.out.println("parsed: " + output[1]);
 //		System.out.println("parsed: " + output[2]);
 //		System.out.println("parsed: " + output[3]);
 //		System.out.println("parsed: " + output[4]);
 //		System.out.println("parsed: " + output[5]);
 //
 //		
 //		System.out.println("message: " + createMessage(new MapLocation(15,14), false, true, 0));
 //	}
 }
