 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal)
  * Rajeev Sharma (rdsharma) 
  * Collin Buchan (cbuchan)
  * Jessica Tiu   (jtiu)
  */
 
 package simulator.elevatorcontrol;
 
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
 public class CarPositionIndicatorCanPayloadTranslator extends CanPayloadTranslator {
 
     /**
      * Constructor for WriteableCanMailbox.  You should always implement both a
      * Writeable and Readable constructor so the same translator can be used for
      * both objects
      *
      * @param payload
      */
     public CarPositionIndicatorCanPayloadTranslator(WriteableCanMailbox payload) {
         super(payload, 1, MessageDictionary.CAR_POSITION_CAN_ID);
     }
 
     /**
      * Constructor for ReadableCanMailbox.  You should always implement both a
      * Writeable and Readable constructor so the same translator can be used for
      * both objects
      *
      * @param payload
      */
     public CarPositionIndicatorCanPayloadTranslator(ReadableCanMailbox payload) {
         super(payload, 1, MessageDictionary.CAR_POSITION_CAN_ID);
     }
 
     /**
      * This method is required for setting values by reflection in the
      * MessageInjector.  The order of parameters in .mf files should match the
      * signature of this method.
      * All translators must have a set() method with the signature that contains
      * all the parameter values.
      *
      * @param floor
      */
     public void set(int floor) {
         setFloor(floor);
     }
 
     /**
      * Set the floor for mDesiredFloor into the lowest 3 bits of the payload
      *
      * @param floor
      */
     public void setFloor(int floor) {
         BitSet b = getMessagePayload();
        addIntToBitset(b, floor - 1, 0, 4);
         setMessagePayload(b, getByteSize());
     }
 
     /**
      * @return the floor value from the can message payload
      */
     public int getFloor() {
        return getIntFromBitset(getMessagePayload(), 0, 4) + 1;
     }
 
     /**
      * Implement a printing method for the translator.
      *
      * @return
      */
     @Override
     public String payloadToString() {
         return "CarPositionIndicator = " + getFloor();
     }
 }
