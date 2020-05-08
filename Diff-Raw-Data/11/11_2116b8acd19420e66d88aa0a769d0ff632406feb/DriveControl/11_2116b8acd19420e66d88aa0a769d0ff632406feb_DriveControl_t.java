 /*
  * 18-649 Fall 2013
  * Group 22
  * David Chow davidcho
  * Brody Anderson bcanders
  * Yang Liu yangliu2
  * Jeffrey Lau jalau
  * DriveControl.java
  */
 package simulator.elevatorcontrol;
  
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.elevatormodules.CarLevelPositionCanPayloadTranslator;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.elevatormodules.HoistwayLimitSensorCanPayloadTranslator;
 import simulator.elevatormodules.LevelingCanPayloadTranslator;
 import simulator.elevatormodules.SafetySensorCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.framework.Speed;
 import simulator.payloads.AtFloorPayload;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.DoorClosedPayload;
 import simulator.payloads.DoorMotorPayload;
 import simulator.payloads.DoorReversalPayload;
 import simulator.payloads.HoistwayLimitPayload;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.DrivePayload;
 import simulator.payloads.DrivePayload.WriteableDrivePayload;
 import simulator.payloads.DriveSpeedPayload;
 import simulator.payloads.DriveSpeedPayload.ReadableDriveSpeedPayload;
 import simulator.payloads.HoistwayLimitPayload.ReadableHoistwayLimitPayload;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 import jSimPack.SimTime;
 
 /**
  * 
  * @author Yang Liu
  *
  */
 public class DriveControl extends Controller{
 
 	/************************
 	 * Declarations
 	 ************************/
 	// load physical input (Drivespeed) and output(Drive)
 	private ReadableDriveSpeedPayload localDriveSpeed;
 	private WriteableDrivePayload localDrive;
 	
 	//Network interface
 	//Outputs: mDrivespeed
 	private WriteableCanMailbox networkDriveSpeed;
 	
 	//Respective translators for output messages
 	private DriveSpeedCanPayloadTranslator mDriveSpeed;
 	
 	/*Inputs: mAtFloor, mLevel, mCarLevelPosition, 
 	* mDoorClosed (4 doors), mDoorMotor(4 motors),
 	* mEmergencyBrake, mDesiredFloor, mHoistwayLimit,
 	* mCarweight
 	*/
 	private ReadableCanMailbox[] networkAtFloor;
 	private ReadableCanMailbox[] networkLevel;
 	private ReadableCanMailbox networkCarLevelPosition;
 	private ReadableCanMailbox networkEmergencyBrake;
 	private ReadableCanMailbox networkDesiredFloor;
 	private ReadableCanMailbox[] networkHoistwayLimit;
 	private ReadableCanMailbox networkCarWeight;
 	private ReadableCanMailbox[] networkDoorClosed;
 	
 	//Respective translators for input messages
 	private AtFloorCanPayloadTranslator[] mAtFloor;
 	private LevelingCanPayloadTranslator[] mLevel;
 	private SafetySensorCanPayloadTranslator mEmergencyBrake;
 	private HoistwayLimitSensorCanPayloadTranslator[] mHoistwayLimit;
 	private DoorClosedCanPayloadTranslator[] mDoorClosed;
 	private CarLevelPositionCanPayloadTranslator mCarLevelPosition;
 	private CarWeightCanPayloadTranslator mCarWeight;
 	private DesiredFloorCanPayloadTranslator mDesiredFloor;
 	
 	//Variables to keep track of instance/state
 	private Speed s;
 	private double speed;
 	//also desired direction
 	private Direction direction;
 	private int floor;
 	private Hallway hallway;
 	private int index_direction;
 	private int index_desired;
 	//Store the period for the controller
     private SimTime period;
     private boolean door_open;
     //Store variable to tell when overweight alarm goes off
     private boolean weight_flag;
     //To tell Commit Point reached or not
     private boolean commitPointReached;
     //Additional internal state variables
     private SimTime counter = SimTime.ZERO;
     private int CurrentFloor;
     private AtFloorArray floorArray;
 
     //enumerate states
     private enum State {
     	STATE_STOP,
     	STATE_SLOW,
     	STATE_LEVEL,
     	STATE_FAST
     }
     
     //state variable initialized at STOP
     private State state = State.STATE_STOP;
     
     public DriveControl(SimTime period, boolean verbose){
     	//call to the Controller superclass constructor is required
         super("DriveControl", verbose);
         
         //stored arguments in internal state
         this.direction = Direction.STOP;
         this.floor = 1;
         this.hallway = Hallway.BOTH;
         this.period = period;
         this.index_desired = 0;
         this.index_direction = 0;
         this.door_open = true;
         this.weight_flag = false;
         //Initialize physical state and create payload objects
         //while also registering the payload with the physical
         //interface.
         localDrive = DrivePayload.getWriteablePayload();
         localDriveSpeed = DriveSpeedPayload.getReadablePayload();
         //Register both payloads to update periodically.
         physicalInterface.registerTimeTriggered(localDriveSpeed);
         physicalInterface.sendTimeTriggered(localDrive, period);
         
         //Initialize commit point reached to be false.
         commitPointReached = false;
         //Initialize network interface
         //Outputted network messages
         networkDriveSpeed = CanMailbox.getWriteableCanMailbox(
         					MessageDictionary.DRIVE_SPEED_CAN_ID);
         //Register each mailbox with its translator
     	mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeed);
     	//Register each mailbox to send or receive messages on a timely basis
     	canInterface.sendTimeTriggered(networkDriveSpeed,period);
         
         //Inputed network messages
     	
     	//Car Level Position
     	networkCarLevelPosition = CanMailbox.getReadableCanMailbox(
     								MessageDictionary.CAR_LEVEL_POSITION_CAN_ID);
     	mCarLevelPosition = new CarLevelPositionCanPayloadTranslator(networkCarLevelPosition);
     	canInterface.registerTimeTriggered(networkCarLevelPosition);
     	
     	//Emergency Brake
     	networkEmergencyBrake = CanMailbox.getReadableCanMailbox(
     								MessageDictionary.EMERGENCY_BRAKE_CAN_ID);
     	mEmergencyBrake = new SafetySensorCanPayloadTranslator(networkEmergencyBrake);
     	canInterface.registerTimeTriggered(networkEmergencyBrake);
     	
     	//Desired Floor
     	networkDesiredFloor = CanMailbox.getReadableCanMailbox(
 								MessageDictionary.DESIRED_FLOOR_CAN_ID);
     	mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
     	canInterface.registerTimeTriggered(networkDesiredFloor);
     	
     	//Car Weight
     	networkCarWeight = CanMailbox.getReadableCanMailbox(
 							MessageDictionary.CAR_WEIGHT_CAN_ID);
     	mCarWeight = new CarWeightCanPayloadTranslator(networkCarWeight);
     	canInterface.registerTimeTriggered(networkCarWeight);
     	
     	//hoistway limit 
     	mHoistwayLimit = new HoistwayLimitSensorCanPayloadTranslator[2];
     	networkHoistwayLimit = new CanMailbox.ReadableCanMailbox[2];
         for (Direction d : Direction.replicationValues) {
             int index = ReplicationComputer.computeReplicationId(d);
             networkHoistwayLimit[index] = CanMailbox.getReadableCanMailbox(
 													MessageDictionary.HOISTWAY_LIMIT_BASE_CAN_ID +
 													ReplicationComputer.computeReplicationId(d));
             mHoistwayLimit[index] = new HoistwayLimitSensorCanPayloadTranslator(networkHoistwayLimit[index], d);
             canInterface.registerTimeTriggered(networkHoistwayLimit[index]);
         }
     	
     	//The ID of AtFloor will changed based on the current floor and hallway.
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
     	
         //The ID of Level will change based on current direction.
         networkLevel = new CanMailbox.ReadableCanMailbox[Elevator.numFloors*2];
         mLevel = new LevelingCanPayloadTranslator[Elevator.numFloors *2];
         for (Direction d : Direction.replicationValues) {
             int index = ReplicationComputer.computeReplicationId(d);
             networkLevel[index] = CanMailbox.getReadableCanMailbox(
 										MessageDictionary.LEVELING_BASE_CAN_ID +
 										ReplicationComputer.computeReplicationId(d));
             mLevel[index] = new LevelingCanPayloadTranslator(networkLevel[index], d);
             canInterface.registerTimeTriggered(networkLevel[index]);
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
         
     	//Issuing the time start
     	timer.start(period);
     }
 
 	@Override
 	public void timerExpired(Object callbackData) {
 		State newState = state;
 		//State Machine
 		switch(state){
 		case STATE_STOP:
 			door_open = false;
 			//State actions for 'STOPPED'
 			direction = Direction.STOP;
 			localDrive.set(Speed.STOP, direction);
 			//DriveSpeed should be stopped
 		
 			mDriveSpeed.set(localDriveSpeed.speed(),localDriveSpeed.direction());
 			
 			//Determine if any doors are open:
 			for (Hallway h : Hallway.replicationValues) {
 	            for (Side s : Side.values()) {
 	                if(!mDoorClosed[ReplicationComputer.computeReplicationId(h, s)].getValue())
 	                	door_open = true;
 	            }
 			}
 			//transitions:
 			//#transition 6.T.1
 			//If we are off level at all, level again.
 			//#transition 6.T.6
 			if(mCarWeight.getValue() >= (Elevator.MaxCarCapacity) &&
 				!mLevel[ReplicationComputer.computeReplicationId(Direction.UP)].getValue()){
 				//Set weight_flag
 				weight_flag = true;
 				//Cable slips require the elevator to come back UP
 				direction = Direction.UP;
 				newState = State.STATE_LEVEL;
 			}
 			else if(mEmergencyBrake.getValue()) {
 					newState = state;
 					door_open = false;
 			}
 			//#transition 6.T.2
 			else if (!door_open && mDesiredFloor.hasReadablePayload()){
 				if (mDesiredFloor.getFloor() - floor == 0){
 					direction = Direction.STOP;
 					newState = state;
 				}
 				else if ((mDesiredFloor.getFloor() - floor) > 0){
 					direction = Direction.UP;
 					newState=State.STATE_SLOW;
 				}
 				else if ((mDesiredFloor.getFloor() - floor) < 0){
 					direction = Direction.DOWN;
 					newState=State.STATE_SLOW;
 				}
 			}
 			else{
 				direction = Direction.STOP;
 				newState = state;
 				//reset door_open
 				door_open = false;
 				}
 			break;
 		case STATE_SLOW:
 			//State actions for 'SpeedSlow'
 			localDrive.set(Speed.SLOW, direction);
 			mDriveSpeed.set(localDriveSpeed.speed(),localDriveSpeed.direction());
 			
 			//if false, then we can go faster!
 			if(direction == Direction.UP){
 				commitPointReached = (((double)mDesiredFloor.getFloor() -1.0) * 5.0 - 
 					(localDriveSpeed.speed() * localDriveSpeed.speed() / 2.0 + 0.5)) * 1000 < 
					mCarLevelPosition.getPosition();
 			}
 			else if(direction == Direction.DOWN){
 				commitPointReached = (((double)mDesiredFloor.getFloor() -1.0) * 5.0 + 
 					(localDriveSpeed.speed() * localDriveSpeed.speed() / 2.0 + 0.5))*1000 > 
					mCarLevelPosition.getPosition();
 			}
 			else;
 
 			//transitions:
 			//#transition 6.T.3
 			if(mEmergencyBrake.getValue())
 				newState = State.STATE_STOP;
 			//#transition 6.T.4
 			else if(mDesiredFloor.getHallway() == Hallway.BOTH){
 				if(mAtFloor[ReplicationComputer.computeReplicationId(
 						mDesiredFloor.getFloor(),
 						Hallway.FRONT)].getValue())
 					newState = State.STATE_LEVEL;
 			} else if(mAtFloor[ReplicationComputer.computeReplicationId(
 					mDesiredFloor.getFloor(),
 					mDesiredFloor.getHallway())].getValue())
 				newState = State.STATE_LEVEL;
 			//#transition 6.T.7
 			else if(commitPointReached == false && localDriveSpeed.speed() == 0.25){
 				newState = State.STATE_FAST;
 			}
 			else
 				newState = state;
 			break;
 		case STATE_LEVEL:
 			//State actions for 'SpeedLevel'
 			localDrive.set(Speed.LEVEL, direction);
 			mDriveSpeed.set(localDriveSpeed.speed(),localDriveSpeed.direction());
 			
 			//transitions:
 			//#transition 6.T.5
 			//if level sensor triggers, stop the drive.
 			if(mLevel[ReplicationComputer.computeReplicationId(direction)].getValue()){
 				// If overweight, don't change the floor, and reset weight_flag
 				if (weight_flag) {
 					weight_flag = false;
 				}
 				//if we were leveling not because of weight, change the floor.
 				else if (!weight_flag) {
 					floor = mDesiredFloor.getFloor();
 				}
 				hallway = mDesiredFloor.getHallway();
 				direction = mDesiredFloor.getDirection();
 				newState = State.STATE_STOP;
 				door_open = false;
 			}else
 				newState = state;
 			break;
 		case STATE_FAST:
 			//State action for 'SpeedFast'
 			localDrive.set(Speed.FAST, direction);
 			mDriveSpeed.set(localDriveSpeed.speed(),localDriveSpeed.direction());
 
 	
 			if(direction == Direction.UP){
 				commitPointReached = (((double)mDesiredFloor.getFloor() -1.0) * 5.0 - 
 					(localDriveSpeed.speed() * localDriveSpeed.speed() / 2.0 + 0.5))*1000 < 
					mCarLevelPosition.getPosition();
 			}
 			else if(direction == Direction.DOWN){
 				commitPointReached = (((double)mDesiredFloor.getFloor() -1.0) * 5.0 + 
 					(localDriveSpeed.speed() * localDriveSpeed.speed() / 2.0 + 0.5))*1000 > 
					mCarLevelPosition.getPosition();
 			}
 			else;
 
 			//transitions:
 			//#transition 6.T.8
 			if(commitPointReached == true && localDriveSpeed.speed() > 0.25){
 				newState = State.STATE_SLOW;
 			}
 			//#transition 6.T.9
 			else if(mEmergencyBrake.getValue())
 				newState = State.STATE_STOP;
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
 }
