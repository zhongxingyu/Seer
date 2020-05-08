 package AP2DX.specializedMessages;
 
 import AP2DX.*;
 
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONException;
 
 /**
 * Specialized message send from the planner to the reflex and from the reflex to the motor.
 * The abstraction in this message allows for commands such as "left 30 degrees, forward 1 meter"
 * We could change thsi into an array to allow turns and stuff. For now, 1 message at a time.
 * 
 * @author Maarten Inja
 */
 public class ActionMotorMessage extends SpecializedMessage 
 {
 	public enum ActionType {
 		FORWARD, BACKWARD, LEFT, RIGHT, TURN, STOP
 	}
 	
 	private ActionType action;
 	
 	/** If the action is TURN than the integer is read as degree else it is read as meter */
 	private double value;	
 	
     /** Creates a specialized message from a standard AP2DXMessage.*/
     public ActionMotorMessage(AP2DXMessage message)
     {
         super(message);
     }
     
     public ActionMotorMessage(Module sourceId, Module destinationId, ActionType actionType, double value)
     {
     	super(Message.MessageType.AP2DX_MOTOR_ACTION, sourceId, destinationId);
         setActionType(actionType);
         setValue(value);
     }
 
     public void specializedParseMessage()
     {
         try
         {
             JSONObject jsonObject = new JSONObject(messageString);
            setValue((double)jsonObject.getInt("value"));
             setActionType(ActionType.valueOf(jsonObject.getString("actionType")));
         }
         catch (Exception e)
         {
 
             System.out.println("Error in AP2DX.specializedMessages.ActionMotorMessage.specializedParseMessage()... shit hitteted the van!");
             e.printStackTrace();
         }
     }   
  
     // setters and getters {{{
 
     public ActionType getActionType() 
     {
     	return action;
     }
 
     public ActionType setActionType(ActionType action) 
     {
     	this.action = action;
         values.put("actionType", action.toString());
     	return action;
     }
     
     public double setValue(double value) 
     {
     	this.value = value;
         values.put("value", value);
     	return value;
     }
 
     public double getValue() 
     {
     	return value;
     }
 
     // }}}
 }
