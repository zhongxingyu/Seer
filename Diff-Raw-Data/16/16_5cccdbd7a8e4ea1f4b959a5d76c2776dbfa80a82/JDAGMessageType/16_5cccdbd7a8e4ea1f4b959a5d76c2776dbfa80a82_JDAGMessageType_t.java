package org.jdryad.communicator.messages;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jdryad.commmunicator.MessageType;
 
 /**
  * A simple class that represents the <code>MessageType</code> as an enum.
  *
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public enum JDAGMessageType implements MessageType
 {
     UP_AND_ALIVE_MESSAGE(1),
     EXECUTE_VERTEX_MESSAGE(2),
     EXECUTE_VERTEX_STATUS_MESSAGE(3);
 
     private static final Map<Integer, JDAGMessageType> myInt2TypeMap =
         new HashMap<Integer, JDAGMessageType>();
 
     private final int myIntegerRepresentation;
 
     /**
      * CTOR
      */
     private JDAGMessageType(int integerRepresentation)
     {
         myIntegerRepresentation = integerRepresentation;
     }
 
     /**
      * Returns the value as an integer
      */
     public int asInteger()
     {
         return myIntegerRepresentation;
     }
 
     /**
      * Returns the message type corresponding to the given int representation
      */
     public static JDAGMessageType fromInteger(int val)
     {
         if (myInt2TypeMap.isEmpty()) {
             for (JDAGMessageType type : values()) {
                 myInt2TypeMap.put(type.asInteger(), type);
             }
         }
         return myInt2TypeMap.get(Integer.valueOf(val));
     }
 }
