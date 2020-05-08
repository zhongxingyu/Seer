 /* 
  * Course and Semester : 18-649 Fall 2013
  * Group No: 16
  * Group Members : Jiangtian Nie(jnie) , Yue Chen(yuechen),
  *                 Sally Stevenson(ststeven) , Sri Harsha Koppaka(skoppaka)
  * Author : Jiangtian Nie
  * AndrewID : jnie
  */
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 
 import java.util.HashMap;
 
 import simulator.elevatormodules.*;
 import simulator.framework.*;
 import simulator.payloads.*;
 import simulator.payloads.translators.IntegerCanPayloadTranslator;
 import simulator.payloads.DrivePayload.WriteableDrivePayload;
 import simulator.payloads.DriveSpeedPayload.ReadableDriveSpeedPayload;
 
 public class DriveControl extends Controller {
 
     SimTime simtime = MessageDictionary.DRIVE_CONTROL_PERIOD;
     WriteableDrivePayload localDrive;
     ReadableDriveSpeedPayload localDriveSpeed;
     DriveCommandCanPayloadTranslator mDriveCommand;
     DriveSpeedCanPayloadTranslator mDriveSpeed;
     DesiredFloorCanPayloadTranslator mDesiredFloor;
     CarLevelPositionCanPayloadTranslator mCarLevelPosition;
     HashMap<Integer, DoorClosedCanPayloadTranslator> mDoorClosedArray;
     HashMap<Integer, DoorMotorCommandCanPayloadTranslator> mDoorMotorArray;
     SafetySensorCanPayloadTranslator mSafety;
     HashMap<Integer, HoistwayLimitSensorCanPayloadTranslator> mHoistwayLimitArray;
     LevelingCanPayloadTranslator mLevelSensorArray[];
     CarWeightCanPayloadTranslator mCarWeight;
     Utility.AtFloorArray mAtFloorArray;
 
     private enum State {
         STATE_STOP,
         STATE_LEVEL_UP,
         STATE_LEVEL_DOWN,
         STATE_SLOW_UP,
         STATE_SLOW_DOWN,
         STATE_EMERGENCY;	
     }
 
     private State state = State.STATE_STOP;
 
     public DriveControl(boolean flag) {
         super("DriveControl", flag);
         
         // Initializing physical interface
         localDrive = DrivePayload.getWriteablePayload();
         physicalInterface.sendTimeTriggered(localDrive, simtime);
         localDriveSpeed = DriveSpeedPayload.getReadablePayload();
         physicalInterface.registerTimeTriggered(localDriveSpeed);
         
         // Initializing network messages
         simulator.payloads.CanMailbox.WriteableCanMailbox writeablecanmailbox =
                 CanMailbox.getWriteableCanMailbox(
                         MessageDictionary.DRIVE_COMMAND_CAN_ID);
         mDriveCommand = new DriveCommandCanPayloadTranslator(
                 writeablecanmailbox);
         canInterface.sendTimeTriggered(writeablecanmailbox, simtime);
         writeablecanmailbox = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DRIVE_SPEED_CAN_ID);
         mDriveSpeed = new DriveSpeedCanPayloadTranslator(writeablecanmailbox);
         canInterface.sendTimeTriggered(writeablecanmailbox, simtime);
         simulator.payloads.CanMailbox.ReadableCanMailbox readablecanmailbox =
                 CanMailbox.getReadableCanMailbox(
                         MessageDictionary.DESIRED_FLOOR_CAN_ID);
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(readablecanmailbox);
         canInterface.registerTimeTriggered(readablecanmailbox);
         readablecanmailbox = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.CAR_LEVEL_POSITION_CAN_ID);
         mCarLevelPosition = new CarLevelPositionCanPayloadTranslator(
                 readablecanmailbox);
         canInterface.registerTimeTriggered(readablecanmailbox);
         readablecanmailbox = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.CAR_WEIGHT_CAN_ID);
         mCarWeight = new CarWeightCanPayloadTranslator(readablecanmailbox);
         canInterface.registerTimeTriggered(readablecanmailbox);
         readablecanmailbox = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.EMERGENCY_BRAKE_CAN_ID);
         mSafety = new SafetySensorCanPayloadTranslator(readablecanmailbox);
         canInterface.registerTimeTriggered(readablecanmailbox);
         
         mDoorClosedArray = 
                 new HashMap<Integer, DoorClosedCanPayloadTranslator>();
         mDoorMotorArray =
                 new HashMap<Integer, DoorMotorCommandCanPayloadTranslator>();
 
         Object hallways[] = Hallway.replicationValues;
         int hallwayCount = hallways.length;
         Object directions[] = Direction.replicationValues;
        numDirections = directions.length;
         Side sides[] = Side.values();
        int numSides = sideList.length;
         
         // Setting up mailboxes so we can check the status of each of the doors
         for (int i = 0; i < hallwayCount; i++) {
             Hallway hallway = (Hallway) hallways[i];
             for (int j = 0; j < numSides; j++) {
                 Side side = sides[j];
                 int doorId = ReplicationComputer.computeReplicationId(hallway,
                                                                   side);
                 simulator.payloads.CanMailbox.ReadableCanMailbox
                     doorClosedMailbox =
                         CanMailbox.getReadableCanMailbox(
                             MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + 
                             doorId);
                mDoorClosedArray.put(id, new DoorClosedCanPayloadTranslator(
                         doorClosedMailbox, hallway, side));
                 canInterface.registerTimeTriggered(doorClosedMailbox);
                 simulator.payloads.CanMailbox.ReadableCanMailbox
                     doorMotorMailbox = CanMailbox.getReadableCanMailbox(
                         MessageDictionary.DOOR_MOTOR_COMMAND_BASE_CAN_ID +
                         doorId);
                 mDoorMotorArray.put(doorId,
                         new DoorMotorCommandCanPayloadTranslator(
                                 doorMotorMailbox, hallway, side));
                 canInterface.registerTimeTriggered(doorMotorMailbox);
             }
 
         }
 
         // Setting up the network messages for the top and bottom hoistway
         // limits
         mHoistwayLimitArray = 
                 new HashMap<Integer, HoistwayLimitSensorCanPayloadTranslator>();
         for (int i = 0; i < numDirections; i++) {
             Direction direction = (Direction) directions[i];
             int directionId = ReplicationComputer.computeReplicationId(direction);
             simulator.payloads.CanMailbox.ReadableCanMailbox
                 hoistwayLimitMailbox =
                     CanMailbox.getReadableCanMailbox(
                             MessageDictionary.HOISTWAY_LIMIT_BASE_CAN_ID +
                             directionId);
             mHoistwayLimitArray.put(directionId,
                     new HoistwayLimitSensorCanPayloadTranslator(
                             hoistwayLimitMailbox, direction));
            canInterface.registerTimeTriggered(readablecanmailbox2);
         }
 
         // Setting up network messages for the level sensors
         mLevelSensorArray = new LevelingCanPayloadTranslator[2];
         for (int i = 0; i < numDirections; i++) {
             Direction direction = (Direction) directions[i];
             simulator.payloads.CanMailbox.ReadableCanMailbox
                 levelSensorMailbox = CanMailbox.getReadableCanMailbox(
                         MessageDictionary.LEVELING_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(direction));
             mLevelSensorArray[ReplicationComputer.computeReplicationId(direction)] =
                     new LevelingCanPayloadTranslator(levelSensorMailbox,
                                                      direction);
             canInterface.registerTimeTriggered(levelSensorMailbox);
         }
 
         mAtFloorArray = new Utility.AtFloorArray(canInterface);
         timer.start(simtime);
     }
 
     private boolean isAnyDoorOpen() {
         Hallway hallways[] = Hallway.replicationValues;
         int numHallways = hallways.length;
         for (int i = 0; i < numHallways; i++) {
             Hallway hallway = hallways[i];
             Side sides[] = Side.values();
             int numSides = sides.length;
             for (int j = 0; j < numSides; j++) {
                 Side side = sides[j];
                 int doorId = ReplicationComputer.computeReplicationId(hallway,
                                                                       side);
                 if (!mDoorClosedArray.get(doorId).getValue() ||
                         (mDoorMotorArray.get(doorId).getDoorCommand() == DoorCommand.OPEN))
                     return true;
             }
 
         }
         return false;
     }
 
     private boolean isSafetyViolation() {
         if (mSafety.getValue())
             return true;
         Direction directions[] = Direction.replicationValues;
         int numDirections = directions.length;
         for (int i = 0; i < numDirections; i++) {
             Direction direction = directions[i];
             if ((mHoistwayLimitArray.get(ReplicationComputer.computeReplicationId(direction))).getValue())
                 return true;
         }
         return false;
     }
 
     public void timerExpired(Object callbackData) {
         State newstate = state;
         int desiredFloor = mDesiredFloor.getFloor();
         int currentFloor = mAtFloorArray.getCurrentFloor();
         boolean isOverweight = mCarWeight.getWeight() >= Elevator.MaxCarCapacity;
         boolean isLevel =
                 mLevelSensorArray[ReplicationComputer.computeReplicationId(
                         Direction.DOWN)].getValue() &&
                         mLevelSensorArray[ReplicationComputer .computeReplicationId(Direction.UP)].getValue();
         
         switch (state) {
         case STATE_STOP:
             localDrive.set(Speed.STOP, Direction.STOP);
             
             //#transition 'T6.5.1'
             if (isSafetyViolation()) {
                 newstate = State.STATE_EMERGENCY;
                 break;
             }
             //#transition 'T6.12'
            if (isOverweight()) {
                 newstate=State.STATE_STOP;
                 break;
             }
             if ((desiredFloor != currentFloor) && !isAnyDoorOpen() && !isOverweight) {
                 if (desiredFloor - currentFloor > 0) {
                     //#transition 'T6.1'
                     newstate = State.STATE_SLOW_UP;
                 }
                 else {
                     //#transition 'T6.4'
                     newstate = State.STATE_SLOW_DOWN;
                 }
                 break;
             }
             if (isLevel || !isAnyDoorOpen()) {
                 break;
             }
             if (mLevelSensorArray[ReplicationComputer.computeReplicationId(Direction.DOWN)].getValue()) {
                 //#transition 'T6.8'
                 newstate = State.STATE_LEVEL_UP;
             }
             else if (mLevelSensorArray[ReplicationComputer.computeReplicationId(Direction.UP)].getValue()) {
                 //#transition 'T6.9'
                 newstate = State.STATE_LEVEL_DOWN;
             }
             break;
         case STATE_LEVEL_UP:
             localDrive.set(Speed.LEVEL, Direction.UP);
             if (isSafetyViolation()) {
                 //#transition 'T6.5.*'
                 newstate = State.STATE_EMERGENCY;
             }
             else if (isLevel) {
                 //#transition 'T6.7'
                 newstate = State.STATE_STOP;
             }
             break;
         case STATE_LEVEL_DOWN:
             localDrive.set(Speed.LEVEL, Direction.DOWN);
             if (isSafetyViolation()) {
                 //#transition 'T6.5.*'
                 newstate = State.STATE_EMERGENCY;
             }
             else if (isLevel) {
                 //#transition 'T6.10'
                 newstate = State.STATE_STOP;
             }
             break;
         case STATE_SLOW_UP:
             localDrive.set(Speed.SLOW, Direction.UP);
             if (isSafetyViolation()) {
                 //#transition 'T6.5.2'
                 newstate = State.STATE_EMERGENCY;
             }
             else if (currentFloor == desiredFloor){
                 if (isLevel){
                     //#transition 'T6.3'
                     newstate = State.STATE_STOP;
                 }
                 else {
                     //#transition 'T6.6'
                     newstate = State.STATE_LEVEL_UP;
                 }
             }
             break;
         case STATE_SLOW_DOWN:
             localDrive.set(Speed.SLOW, Direction.DOWN);
             if (isSafetyViolation()) {
                 //#transition 'T6.5.3'
                 newstate = State.STATE_EMERGENCY;
                 break;
             }
             else if (currentFloor == desiredFloor){
                 if (isLevel) {
                     //#transition 'T6.2'
                     newstate = State.STATE_STOP;
                 }
                 else {
                     //#transition 'T6.11'
                     newstate = State.STATE_LEVEL_DOWN;
                 }
                 break;
             }
             break;
         case STATE_EMERGENCY:
             localDrive.set(Speed.STOP, Direction.STOP);
             break;
         default:
             throw new RuntimeException("State " + state + " was not recognized.");
         }
 
         mDriveSpeed.set(localDriveSpeed.speed(),localDriveSpeed.direction());
         mDriveCommand.setSpeed(localDrive.speed());
         mDriveCommand.setDirection(localDrive.direction());
         
         if (state != newstate)
             log(new Object[] { "Transition from ", state, " to ", newstate });
         setState("STATE", state.toString());
         state = newstate;
         timer.start(simtime);
     }
 }
 
 
