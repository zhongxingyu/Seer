 package AP2DX.specializedMessages;
 
 import AP2DX.*;
import java.util.HashMap;
 
 /**
 * Specialized message for sensor data, actually only a constructor to construct
 * a specialized message from an AP2DXMessage. It makes sure I don't have to
 * type this over and over again for each SpecializedMessage. It's not even
 * abstract, but I do not want anyone to create an instance of this bad boy. 
 *
 * PLEASE read this if you are going to create a specialized AP2DX message!
 *
 * It is important. NEVER set values without using the setters. Otherwise
 * messages can be compiled (be)for(e) sending without the variables added!
 * The setters should add (or change) these variables in the values map. This
 * to make sure every variable is compiled in the JSON message string send
 * to other modules. 
 *
 * The abstract method 'specializedParseMessage' is called from this overriden
 * parseMessage and should be implemented to parse the variables from 
 * the values map and set them in as variables (for the getters), now, obviously,
 * setters should not be used. Sometimes parsing values from the map is tricky
 * and one can prefer to 're-parse' the JSON message string. This is allllright. 
 * 
 * Examples of 'specializedParseMessage' to parse an array can be found
 * in SonarSensorMessage. An example to parse values from the map can be 
 * found in ActionMotorMessage.
 *
 * @author Maarten Inja
 */
 public abstract class SpecializedMessage extends AP2DXMessage
 {
 	/** We do not have an "in" value when we construct a specialize message. 
      * This is only the case when a message is received over the network from
      * another module.
 	 * 
 	 * @param sourceId
 	 * @param destinationId
 	 */
 	public SpecializedMessage(Message.MessageType type, Module sourceId, Module destinationId)
 	{
 		super(type, sourceId, destinationId);
        values = new HashMap ();
 	}
 
     /** 
      * Creates a specialized message from a standard AP2DXMessage.
      */
     public SpecializedMessage(AP2DXMessage message)
     {
         this(message, message.getSourceModuleId(), message.getDestinationModuleId());
     }
 
     /** Creates a specialized message form an AP2DX message with a different source and destionation ID */
     public SpecializedMessage(AP2DXMessage message, Module sourceId, Module destinationId)
     {
         super(message.getMessageString(), sourceId, destinationId);
         messageString = message.getMessageString();
         values = message.getValues();
     }
 
     public void parseMessage()
     {
         super.parseMessage();
         specializedParseMessage();
     }
 
     /** Parsing the fields of 'values' that are specific for a concrete 
     * specialized message. */
     abstract public void specializedParseMessage();
 }
