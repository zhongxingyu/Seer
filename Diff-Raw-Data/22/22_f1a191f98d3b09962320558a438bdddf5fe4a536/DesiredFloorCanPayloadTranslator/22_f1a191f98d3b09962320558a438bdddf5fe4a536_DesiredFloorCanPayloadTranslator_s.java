 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal)
  * Rajeev Sharma (rdsharma) 
  * Collin Buchan (cbuchan)
  * Jessica Tiu   (jtiu)
  */
 
 package simulator.elevatorcontrol;
 
 import simulator.framework.Direction;
 import simulator.framework.Hallway;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.translators.CanPayloadTranslator;
 
 import java.util.BitSet;
 
 /**
  * This is an example CAN payload translator for desired floor messages.  It
  * takes three data fields (floor, hall, direction) and packages them into
  * a bit-level representation of the message.
  * <p/>
  * CanPayloadTranslator provides a lot of utility classes.  See the javadoc for
  * that class for more details.
  *
  * @author Justin Ray
  */
 public class DesiredFloorCanPayloadTranslator extends CanPayloadTranslator {
 
     /**
      * Constructor for WriteableCanMailbox.  You should always implement both a
      * Writeable and Readable constructor so the same translator can be used for
      * both objects
      *
      * @param payload
      */
     public DesiredFloorCanPayloadTranslator(WriteableCanMailbox payload) {
         super(payload, 1, MessageDictionary.DESIRED_FLOOR_CAN_ID);
     }
 
     /**
      * Constructor for ReadableCanMailbox.  You should always implement both a
      * Writeable and Readable constructor so the same translator can be used for
      * both objects
      *
      * @param payload
      */
     public DesiredFloorCanPayloadTranslator(ReadableCanMailbox payload) {
         super(payload, 1, MessageDictionary.DESIRED_FLOOR_CAN_ID);
     }
 
     /**
      * This method is required for setting values by reflection in the
      * MessageInjector.  The order of parameters in .mf files should match the
      * signature of this method.
      * All translators must have a set() method with the signature that contains
      * all the parameter values.
      *
      * @param floor
      * @param dir
      * @param hallway
      */
     public void set(int floor, Direction dir, Hallway hallway) {
         setFloor(floor);
         setDirection(dir);
         setHallway(hallway);
     }
 
     /**
      * Similar to the other set method, but the Hallway/Dir field order reversed.
      *
      * @param floor
      * @param hallway
      * @param dir
      */
     public void set(int floor, Hallway hallway, Direction dir) {
         setFloor(floor);
         setDirection(dir);
         setHallway(hallway);
     }
 
 
     /**
      * Set the floor for mDesiredFloor into the lowest 3 bits of the payload
      *
      * @param floor
      */
     public void setFloor(int floor) {
         BitSet b = getMessagePayload();
        addIntToBitset(b, floor, 0, 4);
         setMessagePayload(b, getByteSize());
     }
 
     /**
      * @return the floor value from the can message payload
      */
     public int getFloor() {
        return getIntFromBitset(getMessagePayload(), 0, 4);
     }
 
     /**
      * Set the direction for mDesiredFloor in bits 3-5 of the can payload
      *
      * @param dir
      */
     public void setDirection(Direction dir) {
         BitSet b = getMessagePayload();
         addIntToBitset(b, dir.ordinal() - 2, 4, 2);
         setMessagePayload(b, getByteSize());
     }
 
     /**
      * @return the direction value from the can payload
      */
     public Direction getDirection() {
         int val = getIntFromBitset(getMessagePayload(), 4, 2) + 2;
         for (Direction d : Direction.values()) {
             if (d.ordinal() == val) {
                 return d;
             }
         }
         throw new RuntimeException("Unrecognized Direction Value " + val);
     }
 
     /**
      * Set the hallway for mDesiredFloor in bits 5-7 of the can payload
      *
      * @param hallway
      */
     public void setHallway(Hallway hallway) {
         BitSet b = getMessagePayload();
         addIntToBitset(b, hallway.ordinal() - 2, 6, 2);
         setMessagePayload(b, getByteSize());
     }
 
     /**
      * @return the hallway value from the CAN payload.
      */
     public Hallway getHallway() {
         int val = getIntFromBitset(getMessagePayload(), 6, 2) + 2;
         for (Hallway h : Hallway.values()) {
             if (h.ordinal() == val) {
                 return h;
             }
         }
         throw new RuntimeException("Unrecognized Hallway Value " + val);
     }
 
     /**
      * Implement a printing method for the translator.
      *
      * @return
      */
     @Override
     public String payloadToString() {
         return "DesiredFloor = " + getFloor() + ", DesiredDirection = " + getDirection() + ", DesiredHallway = " + getHallway();
     }
 }
