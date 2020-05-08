 import java.util.ArrayList;
 
 /**
  * @file Parser.java
  * 
  * The server message parser.
  * 
  *@author Grant Hays
  *@date 10/1/11
  *@version 2
  */
 
 
 /**
  * @class Parser
  *
  * This class takes in the the messages sent by the parser and parses them into
  * information that can be stored in Memory and used by Players.
  * 
  *
  */
 public class Parser {
 
 	/**
 	 * Default constructor
 	 */
 	public Parser() {
 	
 	}
 
 	/**
 	 * This parses the (init) message, the first message sent by the server, directly
 	 * after a new Player is initialized.
 	 * 
 	 * @param inputPacket The init message from the server
 	 * @param mem the player's memory
 	 * @pre A memory must be created for the information to be stored in, and 
 	 * this must be called directly after an (init) is sent to the server.
 	 * @post Vital information about the Player will be saved, such as the
 	 * side of the field the player starts on, the Player's uniform number
 	 * and the play mode, which is "before_kickoff."
 	 */
 	public void initParse(String inputPacket, Memory mem) {
 		// Remove outer parantheses
 		inputPacket = inputPacket.substring(1, inputPacket.length() - 1);
 		//split into tokens by spaces
 		String[] splitPacket = (inputPacket.split(" "));
 		
 		// make sure this is, in fact, the init message from the server
 		if(splitPacket[0].compareTo("init") == 0) {
 			mem.side = splitPacket[1];
			mem.uNum = Double.valueOf(splitPacket[2]);
 			mem.playMode = splitPacket[3];
 			mem.setField(mem.side);
 		}
 	}
 	
 	/**
 	 * The actual message Parsing method
 	 * 
 	 * @param inputPacket the incoming String message from the server
 	 * @param InfoMem the Memory to store all the information in
 	 * @pre A Memory must be created and passed in, along with the message
 	 * from the server
 	 * @post The message will be parsed and stored either as SenseInfos from the
 	 * (sense_body) message, or ObjInfos from the (see) message, or the playMode from the
 	 * referee (hear) message
 	 */
 	public void Parse(String inputPacket, Memory InfoMem) {
 		// Remove outer parentheses
 		inputPacket = inputPacket.substring(1, inputPacket.length() - 1);
 		
 		// Split inputPacket into tokens by "(" and ")" delimiters
 		String[] splitPacket = (inputPacket.split("[()]"));
 		// Parse the first element into packet type and time
 		String[] packetType = (splitPacket[0].split(" "));
 		
 		// We only proceed if it is a (see), (sense_body), or (hear) message
 		if((packetType[0].compareTo("see") == 0) || (packetType[0].compareTo("sense_body") == 0) || (packetType[0].compareTo("hear") == 0)) {
 			// the time from the message
 			int time = Integer.parseInt(packetType[1]);
 		
 			// Call parse method based on packet type in position [0] (either see, sense_body, or hear)
 			if(packetType[0].compareTo("see") == 0) { 
 				ArrayList<ObjInfo> seeArray = new ArrayList<ObjInfo>();
 				seeParse(seeArray, splitPacket);
 				ObjMemory newObjMem = new ObjMemory(seeArray, time);
 				InfoMem.ObjMem = newObjMem;
 			}
 			else if(packetType[0].compareTo("sense_body") == 0) {
 				
 				SenseMemory newSenMem = new SenseMemory(time);
 				senseParse(newSenMem, splitPacket);
 				InfoMem.SenMem = newSenMem;
 			}
 			else if(packetType[0].compareTo("hear") == 0) {
 				hearParse(InfoMem, splitPacket);
 			}
 			
 		}
 		
 	}
 	
 	
 	/**
 	 * This parses the play mode out of the referee hear message
 	 * @param InfoMem the Memory to store the play mode in
 	 * @param splitPacket the split up message from the server
 	 */
 	private void hearParse(Memory InfoMem, String[] splitPacket) {
 		String splitInfo[] = (splitPacket[0].split(" "));
 		
 		if(splitInfo[2].compareTo("referee") == 0) {
 			InfoMem.playMode = splitInfo[3];
 		}
 	}
 
 
 
 	/**
 	 * This parses the information from the (see) server message
 	 * 
 	 * @param seeArray The ArrayList that stores all the ObjInfos from the message. This
 	 * will be saved in the Memory
 	 * @param splitPacket The message String split up by parentheses
 	 * @post The (see) message will be parsed and the ObjInfos will be stored in an ArrayList
 	 */
 	private void seeParse(ArrayList<ObjInfo> seeArray, String[] splitPacket) {
 		
 		for(int i = 2; i < splitPacket.length; i += 4)
 		{
 			
 			// Split up the ObjName
 			String[] splitName = (splitPacket[i].split(" "));
 			String[] splitInfo = (splitPacket[i+1].split(" "));
 			
 			// Determine type of object:
 			// - Flag -
 			if(splitName[0].compareTo("f") == 0) {
 				ObjFlag newFlag = new ObjFlag(splitPacket[i].replaceAll(" ", ""));
 				seeFlagParse(splitName, splitInfo, newFlag);
 				seeArray.add(newFlag);
 			}
 			// - Ball -
 			else if(splitName[0].compareTo("b") == 0) {
 				ObjBall newBall = new ObjBall();
 				seeBallParse(splitInfo, newBall);
 				seeArray.add(newBall);
 			}
 			
 			// - Player -
 			else if(splitName[0].compareTo("p") == 0) {
 				ObjPlayer newPlayer = new ObjPlayer();
 				seePlayerParse(splitName, splitInfo, newPlayer);
 				seeArray.add(newPlayer);
 			}
 			
 			// - Goal -
 			else if(splitName[0].compareTo("g") == 0) {
 				ObjGoal newGoal = new ObjGoal();
 				seeGoalParse(splitName, splitInfo, newGoal);
 				seeArray.add(newGoal);
 			}
 			
 			// - Line -
 			else if(splitName[0].compareTo("l") == 0) {
 				ObjLine newLine = new ObjLine();
 				seeLineParse(splitName, splitInfo, newLine);
 				seeArray.add(newLine);
 			}
 		}
 		
 	}
 	
 	
 
 
 	/**
 	 * This parses the ObjLine info from the (see) message
 	 * 
 	 * @param splitName this is the split up ObjName and side
 	 * @param splitInfo The rest of the information to store in the ObjLine
 	 * @param newLine The ObjLine to store the data in
 	 * @post A new ObjLine will be created and stored in the ObjInfo ArrayList
 	 */
 	private void seeLineParse(String[] splitName, String[] splitInfo, ObjLine newLine) {
 		// Set line's side of field
 		newLine.setSide(splitName[1]);
 		
 		// Set line's Distance, Direction, DistChng, and DirChng
 		if(splitInfo.length == 3) {
 			newLine.setDistance(Double.valueOf(splitInfo[1]));
 			newLine.setDirection(Double.valueOf(splitInfo[2]));
 		}
 		else {
 			newLine.setDistance(Double.valueOf(splitInfo[1]));
 			newLine.setDirection(Double.valueOf(splitInfo[2]));
 			newLine.setDistChng(Double.valueOf(splitInfo[3]));
 			newLine.setDirChng(Double.valueOf(splitInfo[4]));
 		}
 		
 	}
 
 	/**
 	 * This parses the ObjFlag info from the (see) message
 	 * 
 	 * @param splitName this is the split up ObjName with flagName, flagType, side, x_pos, y_pos, and yard
 	 * @param splitInfo The rest of the information to store in the ObjLine
 	 * @param newFlag The ObjFlag to store the data in
 	 * @post A new ObjFlag will be created and stored in the ObjInfo ArrayList
 	 */
 	private void seeFlagParse(String[] splitName, String[] splitInfo, ObjFlag newFlag) {
 		
 		// The center flags
 		if(splitName[1].compareTo("c") == 0) {
 			newFlag.setFlagType("c");
 			newFlag.setX_pos("c");
 			if(splitName.length == 3)
 				newFlag.setY_pos(splitName[2]);
 			else
 				newFlag.setY_pos("c");
 		}
 		// Penalty box flags
 		else if(splitName[1].compareTo("p") == 0) {
 			newFlag.setFlagType("p");
 			newFlag.setX_pos(splitName[2]);
 			newFlag.setY_pos(splitName[3]);
 		}
 		// Goal post flags
 		else if(splitName[1].compareTo("g") == 0) {
 			newFlag.setFlagType("g");
 			newFlag.setX_pos(splitName[2]);
 			newFlag.setY_pos(splitName[3]);
 		}
 		// Line flags
 		else if((splitName.length == 3) && (splitName[2].compareTo("0") != 0)) {
 			newFlag.setFlagType("l");
 			newFlag.setX_pos(splitName[1]);
 			newFlag.setY_pos(splitName[2]);
 		}
 		//
 		else if((splitName.length == 3) && (splitName[2].compareTo("0") == 0)) {
 			newFlag.setFlagType("b");
 			
 			if((splitName[1].compareTo("l") == 0) || (splitName[1].compareTo("r") == 0)) {
 				newFlag.setX_pos(splitName[1]);
 				newFlag.setY_pos("c");
 				newFlag.setYard(splitName[2]);
 			}
 			else {
 				newFlag.setX_pos(splitName[1]);
 				newFlag.setY_pos("c");
 				newFlag.setYard(splitName[2]);
 			}
 		}
 		// Boundary flags
 		else {
 			newFlag.setFlagType("b");
 			
 			if((splitName[1].compareTo("l") == 0) || (splitName[1].compareTo("r") == 0)) {
 				newFlag.setX_pos(splitName[1]);
 				newFlag.setY_pos(splitName[2]);
 				newFlag.setYard(splitName[3]);
 			}
 			else {
 				newFlag.setY_pos(splitName[1]);
 				newFlag.setX_pos(splitName[2]);
 				newFlag.setYard(splitName[3]);
 			}
 		}
 		
 		// Input info by determining how much info is available
 			if(splitInfo.length == 3) {
 				newFlag.setDistance(Double.valueOf(splitInfo[1]));
 				newFlag.setDirection(Double.valueOf(splitInfo[2]));
 			}
 			else {
 				newFlag.setDistance(Double.valueOf(splitInfo[1]));
 				newFlag.setDirection(Double.valueOf(splitInfo[2]));
 				newFlag.setDistChng(Double.valueOf(splitInfo[3]));
 				newFlag.setDirChng(Double.valueOf(splitInfo[4]));
 			}
 	}
 	
 	/**
 	 * This parses the ObjBall info from the (see) message
 	 * 
 	 * @param splitName this is the split up ObjName and side
 	 * @param splitInfo The rest of the information to store in the ObjBall
 	 * @param newBall The ObjBall to store the data in
 	 * @post A new ObjBall will be created and stored in the ObjInfo ArrayList
 	 */
 	private void seeBallParse(String[] splitInfo, ObjBall newBall) {
 		
 		// Input info by determining how much info is available
 		if(splitInfo.length == 3) {
 			newBall.setDistance(Double.valueOf(splitInfo[1]));
 			newBall.setDirection(Double.valueOf(splitInfo[2]));
 		}
 		else {
 			newBall.setDistance(Double.valueOf(splitInfo[1]));
 			newBall.setDirection(Double.valueOf(splitInfo[2]));
 			newBall.setDistChng(Double.valueOf(splitInfo[3]));
 			newBall.setDirChng(Double.valueOf(splitInfo[4]));
 		}
 	}
 
 	/**
 	 * This parses the ObjPlayer info from the (see) message
 	 * 
 	 * @param splitName this is the split up ObjName, team name, uniform number (if visible),
 	 * and goalie boolean (if visible).
 	 * @param splitInfo The rest of the information to store in the ObjPlayer
 	 * @param newPlayer The ObjPlayer to store the data in
 	 * @post A new ObjPlayer will be created and stored in the ObjInfo ArrayList
 	 */
 	private void seePlayerParse(String[] splitName, String[] splitInfo, ObjPlayer newPlayer) {
 		
 		if(splitName.length == 2) {
 			newPlayer.setTeam(splitName[1]);
 		}
 		else if(splitName.length == 3) {
 			newPlayer.setTeam(splitName[1]);
 			newPlayer.setuNum(Integer.parseInt(splitName[2]));
 		}
 		else if(splitName.length == 4) {
 			newPlayer.setTeam(splitName[1]);
 			newPlayer.setuNum(Integer.parseInt(splitName[2]));
 			newPlayer.setGoalie(true);
 		}
 		
 		if(splitInfo.length == 3) {
 			newPlayer.setDistance(Double.valueOf(splitInfo[1]));
 			newPlayer.setDirection(Double.valueOf(splitInfo[2]));
 		}
 		else if(splitInfo.length == 5) {
 			newPlayer.setDistance(Double.valueOf(splitInfo[1]));
 			newPlayer.setDirection(Double.valueOf(splitInfo[2]));
 			newPlayer.setDistChng(Double.valueOf(splitInfo[3]));
 			newPlayer.setDirChng(Double.valueOf(splitInfo[4]));
 		}
 		else if(splitInfo.length == 7) {
 			newPlayer.setDistance(Double.valueOf(splitInfo[1]));
 			newPlayer.setDirection(Double.valueOf(splitInfo[2]));
 			newPlayer.setDistChng(Double.valueOf(splitInfo[3]));
 			newPlayer.setDirChng(Double.valueOf(splitInfo[4]));
 			newPlayer.setHeadDir((Double.valueOf(splitInfo[5])));
 			newPlayer.setBodyDir((Double.valueOf(splitInfo[6])));
 		}
 		
 	}
 
 	/**
 	 * This parses the ObjGoal info from the (see) message
 	 * 
 	 * @param splitName this is the split up ObjName and side
 	 * @param splitInfo The rest of the information to store in the ObjGoal
 	 * @param newGoal The ObjGoal to store the data in
 	 * @post A new ObjGoal will be created and stored in the ObjInfo ArrayList
 	 */
 	private void seeGoalParse(String[] splitName, String[] splitInfo, ObjGoal newGoal) {
 		
 		// Set goal's side of field
 		newGoal.setSide(splitName[1]);
 		
 		// Set goal's Distance, Direction, DistChng, and DirChng
 		if(splitInfo.length == 3) {
 			newGoal.setDistance(Double.valueOf(splitInfo[1]));
 			newGoal.setDirection(Double.valueOf(splitInfo[2]));
 		}
 		else {
 			newGoal.setDistance(Double.valueOf(splitInfo[1]));
 			newGoal.setDirection(Double.valueOf(splitInfo[2]));
 			newGoal.setDistChng(Double.valueOf(splitInfo[3]));
 			newGoal.setDirChng(Double.valueOf(splitInfo[4]));
 		}
 		
 	}
 	
 	/**
 	 * This parses the information from the (sense_body) server message
 	 * 
 	 * @param newSenMem this is the SenseMemory to store all the information in
 	 * @param splitPacket The message String split up by parentheses
 	 * @post The (sense_body) message will be parsed and the information will be
 	 * stored in the SenseMemory of the Memory
 	 */
 	private void senseParse(SenseMemory newSenMem, String[] splitPacket) {
 		
 		// The split up Strings of useful information
 		String[] splitStamina = splitPacket[3].split(" ");
 		String[] splitSpeed = splitPacket[5].split(" ");
 		String[] splitHeadAngle = splitPacket[7].split(" ");
 		
 		//the parsed Stamina information
 		newSenMem.stamina = Double.valueOf(splitStamina[1]);
 		newSenMem.effort = Double.valueOf(splitStamina[2]);
 		newSenMem.recovery = Double.valueOf(splitStamina[3]);
 		
 		// the parsed Speed information
 		newSenMem.amountOfSpeed = Double.valueOf(splitSpeed[1]);
 		newSenMem.directionOfSpeed = Double.valueOf(splitSpeed[2]);
 		
 		// the parsed head direction angle
 		newSenMem.headDirection = Double.valueOf(splitHeadAngle[1]);
 		
 	}
 	
 	/**
 	 * The String of the incoming message
 	 */
 	public String input;
 	
 }
