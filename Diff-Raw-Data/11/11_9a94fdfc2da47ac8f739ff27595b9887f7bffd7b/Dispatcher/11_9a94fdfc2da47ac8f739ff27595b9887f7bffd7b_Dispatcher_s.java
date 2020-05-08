 /*
  * 18-649 Fall 2013
  * Group 22
  * David Chow davidcho
  * Brody Anderson bcanders
  * Yang Liu yangliu2
  * Jeffrey Lau jalau
  * Dispatcher.java
  * @author: Jeff Lau (jalau)
  */
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.elevatormodules.CarLevelPositionCanPayloadTranslator;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 
 /**
  * 
  * @author Jeff Lau
  *
  */
 public class Dispatcher extends Controller{
 
 	/************************
 	 * Declarations
 	 ***********************/
 	//Network interface
 	//Outputs: mDesiredFloor 
 	private WriteableCanMailbox networkDesiredFloor;
 
 
 	//Respective translators for output messages
 	private DesiredFloorCanPayloadTranslator mDesiredFloor;
 
 
 	/*Inputs:mAtFloor, mDoorClosed, mHallCall,
 	 * mCarCall, mCarWeight
 	 */
 	private ReadableCanMailbox[] networkAtFloor;
 	private ReadableCanMailbox[] networkDoorClosed;
 	private ReadableCanMailbox[] networkHallCall;
 	private ReadableCanMailbox[] networkCarCall;
 	private ReadableCanMailbox networkCarWeight;
 	private ReadableCanMailbox networkCarLevelPosition;
 	private ReadableCanMailbox networkDriveSpeed;
 
 	//Respective translators for input messages
 	private AtFloorCanPayloadTranslator[] mAtFloor;
 	private DoorClosedCanPayloadTranslator[] mDoorClosed;
 	private HallCallCanPayloadTranslator[] mHallCall;
 	private CarCallCanPayloadTranslator[] mCarCall;
 	private CarWeightCanPayloadTranslator mCarWeight;
 	private CarLevelPositionCanPayloadTranslator mCarLevelPosition;
 	private DriveSpeedCanPayloadTranslator mDriveSpeed;
 
 	private int numFloors;
 	private int floor;
 	private int curr_f;
 	private int curr_pos;
 	private Hallway curr_h;
 	private Hallway hallway;
 	private Direction direction;
     private Direction curr_d;
 
     
 
 	//Store he period for the controller
 	private SimTime period;
 
 	//Enumerate states
 	private enum State{
 		STATE_DOORSCLOSED,
 		STATE_EMERGENCY,
 		STATE_DOORSOPEN,
 	}
 
 	//State variable initialized at ATFLOOR
 	private State state = State.STATE_DOORSOPEN;
 
 	public Dispatcher(int numFloors, SimTime period, boolean verbose){
 		super("Dispatcher", verbose);
 
 		this.period = period;
 		this.numFloors = numFloors;
 		curr_f = 1;
 		curr_pos = 0;
 		curr_h = Hallway.BOTH;
         curr_d = Direction.STOP;
 		floor = 1;
 		hallway = Hallway.BOTH;
 		//represents the next direction
 		direction = Direction.STOP;
 		
 		log("Created Dispatcher with period = ", period);
 
 		//Initialize network interface
 		//Outputs
 		networkDesiredFloor = CanMailbox.getWriteableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
 		mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
 
 
 		canInterface.sendTimeTriggered(networkDesiredFloor,period);
 
 		//Inputs
 		//CarWeight
 		networkCarWeight = CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_WEIGHT_CAN_ID);
 		mCarWeight = new CarWeightCanPayloadTranslator(networkCarWeight);
 		canInterface.registerTimeTriggered(networkCarWeight);
 
 		//AtFloor
 		networkAtFloor = new CanMailbox.ReadableCanMailbox[Elevator.numFloors*2];
 		mAtFloor = new AtFloorCanPayloadTranslator[Elevator.numFloors *2];
 		for (int floors = 1; floors <= Elevator.numFloors; floors++) {
 			for (Hallway h : Hallway.replicationValues) {
 				int index = ReplicationComputer.computeReplicationId(floors, h);
 				networkAtFloor[index] = CanMailbox.getReadableCanMailbox(
 						MessageDictionary.AT_FLOOR_BASE_CAN_ID + 
 						ReplicationComputer.computeReplicationId(floors, h));
 				mAtFloor[index] = new AtFloorCanPayloadTranslator(networkAtFloor[index], floors, h);
 				canInterface.registerTimeTriggered(networkAtFloor[index]);
 			}
 		}
 
 		//DoorClosed
 		networkDoorClosed = new CanMailbox.ReadableCanMailbox[4];
 		mDoorClosed = new DoorClosedCanPayloadTranslator[4];
 		for (Hallway h : Hallway.replicationValues) {
 			for (Side s : Side.values()) {
 				int index = ReplicationComputer.computeReplicationId(h, s);
 				networkDoorClosed[index] = CanMailbox.getReadableCanMailbox(
 						MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + 
 						ReplicationComputer.computeReplicationId(h, s));
 				mDoorClosed[index] = new DoorClosedCanPayloadTranslator(networkDoorClosed[index],h,s);
 				canInterface.registerTimeTriggered(networkDoorClosed[index]);
 			}
 		}
 
 		//HallCall
 		networkHallCall = new CanMailbox.ReadableCanMailbox[Elevator.numFloors*4];
 		mHallCall = new HallCallCanPayloadTranslator[Elevator.numFloors*4];
 		for(int floors = 1; floors <= Elevator.numFloors; floors++) {
 			for (Hallway h : Hallway.replicationValues) {
 				for (Direction d : Direction.replicationValues) {
 					int index = ReplicationComputer.computeReplicationId(floors,h,d);
 					networkHallCall[index] = CanMailbox.getReadableCanMailbox(
 							MessageDictionary.HALL_CALL_BASE_CAN_ID + 
 							ReplicationComputer.computeReplicationId(floors,h,d));
 					mHallCall[index] = new HallCallCanPayloadTranslator(networkHallCall[index],floors,h,d);
 					canInterface.registerTimeTriggered(networkHallCall[index]);
 				}
 			}
 		}
 
 		//CarCall
 		networkCarCall = new CanMailbox.ReadableCanMailbox[Elevator.numFloors*2];
 		mCarCall = new CarCallCanPayloadTranslator[Elevator.numFloors*2];
 		for(int floors = 1; floors <= Elevator.numFloors; floors++) {
 			for (Hallway h : Hallway.replicationValues) {
 				int index = ReplicationComputer.computeReplicationId(floors,h);
 				networkCarCall[index] = CanMailbox.getReadableCanMailbox(
 						MessageDictionary.CAR_CALL_BASE_CAN_ID + 
 						ReplicationComputer.computeReplicationId(floors,h));
 				mCarCall[index] = new CarCallCanPayloadTranslator(networkCarCall[index],floors,h);
 				canInterface.registerTimeTriggered(networkCarCall[index]);
 			}
 		}
 		
 		//Car Level Position
     	networkCarLevelPosition = CanMailbox.getReadableCanMailbox(
     								MessageDictionary.CAR_LEVEL_POSITION_CAN_ID);
     	mCarLevelPosition = new CarLevelPositionCanPayloadTranslator(networkCarLevelPosition);
     	canInterface.registerTimeTriggered(networkCarLevelPosition);
     	
     	//Drive Speed
     	networkDriveSpeed = CanMailbox.getReadableCanMailbox(
     								MessageDictionary.DRIVE_SPEED_CAN_ID);
     	mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeed);
     	canInterface.registerTimeTriggered(networkDriveSpeed);
     	
 		//initially set target to lobby.
 
 		mDesiredFloor.set(floor,hallway,direction);
 		
 		timer.start(period);	    
 	}
 
 	@Override
 	public void timerExpired(Object callbackData) {
 		State newState = state;
 		curr_pos = mCarLevelPosition.getPosition();
 		curr_f = curr_pos/5000 + 1;
 		//log("curr_pos is ", curr_pos, " and curr_f is ", curr_f);
 		if(Elevator.hasLanding(curr_f, Hallway.FRONT)){
 			if(Elevator.hasLanding(curr_f, Hallway.BACK))
 				curr_h = Hallway.BOTH;
 			else
 				curr_h = Hallway.FRONT;
 		}
 		else
 			curr_h = Hallway.BACK;
 		//State Machine
 		switch(state){
 		//#state State 2: Doors Open
 		case STATE_DOORSOPEN:
 			//State actions for 'DOORSOPEN'
 			
 			if(hallway == Hallway.BOTH) {
 				//If either side of doors open and we're not at the floor, emergency!
 				//#transition 11.T.2
 				if ((!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() ||
 						!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()) && 
 						!mAtFloor[ReplicationComputer.computeReplicationId(floor,Hallway.FRONT)].getValue()) {
 					newState = State.STATE_EMERGENCY;
 				}
 				//#transition 11.T.3 when doors closed.
 				else if((mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() &&
 						mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()))
 					newState = State.STATE_DOORSCLOSED;
 			}
 			//if it is not at ANY floor and ANY doors are open, jump to emergency state
 			//#transition 11.T.2
 			else if ((!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() ||
 					!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()) && 
 					!mAtFloor[ReplicationComputer.computeReplicationId(floor,hallway)].getValue()) {
 				newState = State.STATE_EMERGENCY;
 			}
 			//if no issues, move to the next state when doors closed
 			//#transition 11.T.3
 			else if((mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() &&
 					mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()))
 				newState = State.STATE_DOORSCLOSED;
 			else
 				newState = state;
 			break;
 
 			//State actions for 'Doors Closed'
 			//#state State 1: Doors Closed
 			case STATE_DOORSCLOSED:
 				
 				// Set next Direction and  next target Floor
 				nextTarget(mDriveSpeed.getSpeed(), mCarLevelPosition.getPosition(), curr_d);
 
 				//Decide which hallway is valid. If front and back, then both, else either front or back.
 				if (Elevator.hasLanding(floor, Hallway.FRONT))	{
 					if (Elevator.hasLanding(floor, Hallway.BACK))
 						hallway = Hallway.BOTH;
 					else
 						hallway = Hallway.FRONT;
 				}
 				else 
 					hallway = Hallway.BACK;
 				log("floor is ", floor, " next direction is ", direction, " current direction is", curr_d);
 				//Now set the next target.
 				mDesiredFloor.set(floor, direction, hallway);
 				
 				//Check both sides for closed doors if that is the case.
 				if(hallway == Hallway.BOTH) {
 					//If we're at the next target floor AND doors are opening, jump to DOORSOPEN
 					//#transition 11.T.1
 					if(mAtFloor[ReplicationComputer.computeReplicationId(floor,Hallway.FRONT)].getValue() &&
 							((!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() ||
 							!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()))){
 						newState = State.STATE_DOORSOPEN;
 						//curr_d updated within nextTarget() but also updated when we open doors.
 						curr_d = direction;
 					}
 					//If either side of doors open and we're not at any floor, emergency!
 					//#transition 11.T.4
 					else if ((!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() ||
 						!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()) && 
 						!mAtFloor[ReplicationComputer.computeReplicationId(curr_f,curr_h)].getValue() &&
 						!mAtFloor[ReplicationComputer.computeReplicationId(floor,Hallway.FRONT)].getValue()) {
 						newState = State.STATE_EMERGENCY;
 					}
 					else
 						newState = state;
 				}
 				//If we're at the next target floor AND doors are opening, jump to DOORSOPEN
 				//#transition 11.T.3
 				else if(mAtFloor[ReplicationComputer.computeReplicationId(floor,hallway)].getValue() && 
 						((!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() ||
 						!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()))){
 					newState = State.STATE_DOORSOPEN;
 					curr_d = direction;
 				}
 
 				//If either side of doors open and we're not at any floor, emergency!
 				//#transition 11.T.4
 				else if ((!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT)].getValue() ||
 						!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT)].getValue()) && 
 						!mAtFloor[ReplicationComputer.computeReplicationId(curr_f,curr_h)].getValue() &&
 						!mAtFloor[ReplicationComputer.computeReplicationId(floor,hallway)].getValue()) {
 					newState = State.STATE_EMERGENCY;
 				}
 				else
 					newState = state;
 				break;
 
 				//State actions for 'EMERGENCY'
 				//#state State 3: Emergency
 			case STATE_EMERGENCY:
 				floor = 1;
 				hallway = Hallway.BOTH;
 				direction = Direction.STOP;
 				//Set the desired floor back down to the lobby!
 				mDesiredFloor.set(floor, hallway, direction);
 				
 				//Once the doors have opened at the lobby, we can return to normal operation.
 				//#transition 11.T.5
 				if(!mDoorClosed[ReplicationComputer.computeReplicationId(Hallway.FRONT,Side.LEFT)].getValue() &&
 					mAtFloor[ReplicationComputer.computeReplicationId(1,Hallway.FRONT)].getValue())
 					newState = State.STATE_DOORSOPEN;
 				else
 					newState = state;
 				break;
 				
 			default:
 				throw new RuntimeException("State " + state + " was not recognized.");
 		}
 
 		/*log the results of this iteration
 		if (state == newState) {
 			log("remains in state: ",state);
 		} else {
 			log("Transition:",state,"->",newState);
 		}
 		*/
 		//update the state variable
 		state = newState;
 
 		//report the current state
 		setState(STATE_KEY,newState.toString());
 
 		//schedule the next iteration of the controller
 		//you must do this at the end of the timer callback in order to restart
 		//the timer
 		timer.start(period);
 	}
 
 	/**
 	 * This function will take in mCarLevelPosition and mDriveSpeed to determine the next
 	 * target. The algorithm works as follows in the example below:
 	 *  ______
 	 * 6|	 |
 	 * 5|	 |
 	 * 4|	 |
 	 * 3|car | (mDesiredFloor.d = UP)
 	 * 2|	 |
 	 * 1|	 |
 	 * 
 	 * 1) Check for car calls above car.
 	 * 2) Check for hall calls above car that are also going UP
 	 * 3) Check for hall calls above car that are going DOWN
 	 * 4) Check for car calls below car.
 	 * 5) Check for hall calls below car that are going DOWN
 	 * 6) Check for hall calls below car that are going UP
 	 * 
 	 *  *****************************************************
 	 *  The default checking scheme will be going down.
 	 *  Thus, if mDesiredFloor.d = DOWN or STOP, check next target
 	 *  as if going DOWN.
 	 *  *****************************************************
 	 *  
 	 * @param car_position 
 	 * @param speed 
 	 * @param current_d 
 	 */
 	private void nextTarget(double speed, int car_position, Direction current_d) {
 		int target = 0;
 		int nextTarget = 0;
 	//	int current_f = 0;
 		boolean targetFound = false;
 		boolean nextTargetFound = false;
 		Direction nextHallCall = Direction.STOP;
 	/*	if(curr_f == 1)
 			current_f = 2;
 		else
 			current_f = curr_f;*/
 		
 		//Step ONE and TWO, checking for car and hall calls in desired direction.
 		if(current_d == Direction.UP){
 			outerloop:
 				for(int f = curr_f; f <= Elevator.numFloors; f++){
 					innerloop:
 						for (Hallway h : Hallway.replicationValues) {
 							int index = ReplicationComputer.computeReplicationId(f,h);
 							int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.UP);
 							//If there is a car call or hall call at the given floor, try and set as target.
 							if(mCarCall[index].getValue() || mHallCall[hallIndex].getValue()){
 								//Make sure target hasn't already been set!
 								if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 									target = f;
 									targetFound = true;
 									if(mHallCall[hallIndex].getValue()){
 										nextHallCall = Direction.UP;
 										nextTargetFound = true;
 										break outerloop;
 									}
 									break innerloop;
 								}
 								//If the next target has been set as well, break out of both loops.
 								else if (!nextTargetFound){
 									nextTargetFound = true;
 									nextTarget = f;
									break outerloop;
 								}
 							}
 						}	
 				}
 			//Step THREE: Checking hall calls in desired direction, going in opposite direction.
 			if((nextHallCall == Direction.STOP) && (!targetFound || !nextTargetFound)){
 				outerloop:
 					for(int f = curr_f; f <= Elevator.numFloors; f++){
 							for (Hallway h : Hallway.replicationValues) {
 								int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.DOWN);
 								//If there is a car call or hall call at the given floor, try and set as target.
 								if(mHallCall[hallIndex].getValue()){
 									//Make sure target hasn't already been set!
 									if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 										nextHallCall = Direction.DOWN;
 										target = f;
 										targetFound = true;
 										nextTargetFound = true;
 										break outerloop;
 									}
 									//If the next target has been set as well, break out of both loops.
 									else if (!nextTargetFound){
 										nextTargetFound = true;
 										nextTarget = f;
 										break outerloop;
 									}
 								}
 							}	
 					}
 			}
 			//Step FOUR and FIVE: Checking car and hall calls opposite of desired direction.
 			if((nextHallCall == Direction.STOP) && (!targetFound || !nextTargetFound)){
 				outerloop:
 					for(int f = curr_f; f >= 1; f--){
 						innerloop:
 							for (Hallway h : Hallway.replicationValues) {
 								int index = ReplicationComputer.computeReplicationId(f,h);
 								int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.DOWN);
 								//If there is a car call or hall call at the given floor, try and set as target.
 								if(mCarCall[index].getValue() || mHallCall[hallIndex].getValue()){
 									//Make sure target hasn't already been set!
 									if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 										target = f;
 										targetFound = true;
 										if(mHallCall[hallIndex].getValue()){
 											nextHallCall = Direction.DOWN;
 											nextTargetFound = true;
 											break outerloop;
 										}
 										break innerloop;
 									}
 									//If the next target has been set as well, break out of both loops.
 									else if (!nextTargetFound){
 										nextTargetFound = true;
 										nextTarget = f;
										break outerloop;
 									}
 								}
 							}	
 					}
 			}
 			//Step SIX: Checking hall calls of desired direction, 
 			// going in current desired direction.
 			if((nextHallCall == Direction.STOP) && (!targetFound || !nextTargetFound)){
 				outerloop:
 					for(int f = curr_f; f >= 1; f--){
 							for (Hallway h : Hallway.replicationValues) {
 								int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.UP);
 								//If there is a car call or hall call at the given floor, try and set as target.
 								if(mHallCall[hallIndex].getValue()){
 									//Make sure target hasn't already been set!
 									if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 										nextHallCall = Direction.UP;
 										target = f;
 										targetFound = true;
 										nextTargetFound = true;
 										break outerloop;
 									}
 									//If the next target has been set as well, break out of both loops.
 									else if (!nextTargetFound){
 										nextTargetFound = true;
 										nextTarget = f;
 										break outerloop;
 									}
 								}
 							}	
 					}
 			}
 		}
 		//Otherwise, if DOWN or STOP, assume DOWN
 		else{
 			//Step ONE and TWO
 			outerloop:
 				for(int f = curr_f; f >= 1; f--){
 					innerloop:
 						for (Hallway h : Hallway.replicationValues) {
 							int index = ReplicationComputer.computeReplicationId(f,h);
 							int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.DOWN);
 							//If there is a car call or hall call at the given floor, try and set as target.
 							if(mCarCall[index].getValue()==true || mHallCall[hallIndex].getValue()==true){
 								//Make sure target hasn't already been set!
 								if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 									target = f;
 									targetFound = true;
 									if(mHallCall[hallIndex].getValue()==true){
 										nextHallCall = Direction.DOWN;
 										nextTargetFound = true;
 										break outerloop;
 									}
 									break innerloop;
 								}
 								//If the next target has been set as well, break out of both loops.
 								else if (!nextTargetFound){
 									nextTargetFound = true;
 									nextTarget = f;
									break outerloop;
 								}
 							}
 						}	
 				}
 			//Step THREE: Checking hall calls in desired direction, going in opposite direction.
 			if((nextHallCall == Direction.STOP) && (!targetFound || !nextTargetFound)){
 				outerloop:
 					for(int f = curr_f; f >= 1; f--){
 						for (Hallway h : Hallway.replicationValues) {
 								int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.UP);
 								//If there is a car call or hall call at the given floor, try and set as target.
 								if(mHallCall[hallIndex].getValue()){
 									//Make sure target hasn't already been set!
 									if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 										nextHallCall = Direction.UP;
 										target = f;
 										targetFound = true;
 										nextTargetFound = true;
 										break outerloop;
 									}
 									//If the next target has been set as well, break out of both loops.
 									else if (!nextTargetFound){
 										nextTargetFound = true;
 										nextTarget = f;
 										break outerloop;
 									}
 								}
 							}
 					}
 			}
 			//Step FOUR and FIVE: Checking car and hall calls opposite of desired direction.
 			if((nextHallCall == Direction.STOP) && (!targetFound || !nextTargetFound)){
 				outerloop:
 					for(int f = curr_f; f <= Elevator.numFloors; f++){
 						innerloop:
 							for (Hallway h : Hallway.replicationValues) {
 								int index = ReplicationComputer.computeReplicationId(f,h);
 								int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.UP);
 								//If there is a car call or hall call at the given floor, try and set as target.
 								if(mCarCall[index].getValue() || mHallCall[hallIndex].getValue()){
 									//Make sure target hasn't already been set!
 									if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 										target = f;
 										targetFound = true;
 										if(mHallCall[hallIndex].getValue()){
 											nextHallCall = Direction.UP;
 											nextTargetFound = true;
 											break outerloop;
 										}
 										break innerloop;
 									}
 									//If the next target has been set as well, break out of both loops.
 									else if (!nextTargetFound){
 										nextTargetFound = true;
 										nextTarget = f;
										break outerloop;
 									}
 								}
 							}	
 					}
 			}
 			//Step SIX: Checking hall calls opposite of desired direction, 
 			// going in current desired direction.
 			if((nextHallCall == Direction.STOP) && (!targetFound || !nextTargetFound)){
 				outerloop:
 					for(int f = curr_f; f <= Elevator.numFloors; f++){
 							for (Hallway h : Hallway.replicationValues) {
 								int hallIndex = ReplicationComputer.computeReplicationId(f,h,Direction.DOWN);
 								//If there is a car call or hall call at the given floor, try and set as target.
 								if(mHallCall[hallIndex].getValue()){
 									//Make sure target hasn't already been set!
 									if(!targetFound && commitPoint(f, current_d, car_position, speed)){
 										nextHallCall = Direction.DOWN;
 										target = f;
 										targetFound = true;
 										nextTargetFound = true;
 										break outerloop;
 									}
 									//If the next target has been set as well, break out of both loops.
 									else if (!nextTargetFound){
 										nextTargetFound = true;
 										nextTarget = f;
 										break outerloop;
 									}
 								}
 							}	
 					}
 			}
 		}
 		
 		log("target: ", target, " nextTarget: ", nextTarget, " hall call: ", nextHallCall , "current floor ", curr_f);
 		//If target was found, change desired floor.
 		if(targetFound){
 			floor = target;			
 			//If next target found, set desired Direction.
 			if(nextHallCall != Direction.STOP)
 				direction = nextHallCall;
 			else if(nextTargetFound){
 				if(target - nextTarget > 0)
 					direction = Direction.DOWN;
 				//It should not be possible for the nextTarget to equal the target
 				else if(target - nextTarget < 0)
 					direction = Direction.UP;
 			}
 			else
 				direction = Direction.STOP;
 		}
 		else if (!targetFound && nextTargetFound){
 			log("entered the arena ", (floor-nextTarget));
 			floor = nextTarget;
 			if(Elevator.hasLanding(floor, Hallway.FRONT)){
 				if (mHallCall[ReplicationComputer.computeReplicationId(floor, Hallway.FRONT, Direction.UP)].getValue())
 					direction = Direction.UP;
 				else if (mHallCall[ReplicationComputer.computeReplicationId(floor, Hallway.FRONT, Direction.DOWN)].getValue())
 					direction = Direction.DOWN;
 			}
 			else {
 				if (mHallCall[ReplicationComputer.computeReplicationId(floor, Hallway.BACK, Direction.UP)].getValue())
 					direction = Direction.UP;
 				else if (mHallCall[ReplicationComputer.computeReplicationId(floor, Hallway.FRONT, Direction.DOWN)].getValue())
 					direction = Direction.DOWN;
 			}
 		}
 		//Set the current direction based on the target.
 		if(floor - curr_f > 0)
 			curr_d = Direction.UP;
 		else if(floor - curr_f < 0)
 			curr_d = Direction.DOWN;
 		else
 			curr_d = Direction.STOP;
 	
 	}
 
 	/**
 	 * Performs a check to see whether a requested floor is viable to stop at or not.
 	 * 
 	 * @param f The requested floor
 	 * @param d The current direction the car is traveling in
 	 * @param car_position  The current car position
 	 * @param speed 
 	 * @return true if commit point not reached.
 	 * 		   false if commit point past.
 	 */
 	private boolean commitPoint(int f, Direction d, int car_position, double speed) {
 		if(d == Direction.UP){
 			return (((double)f - 1.0) * 5.0 - ((speed * speed) / 2.0 + 0.5))*1000 > car_position;
 		}
 		else if(d == Direction.DOWN){
 			return (((double)f - 1.0) * 5.0 + ((speed * speed) / 2.0 + 0.5))*1000 < car_position;
 		}
 		//If stopped, then we've definitely not reached the commit point.
 		else
 			return true;
 	}
 }
 
 
