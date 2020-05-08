 package AP2DX.specializedMessages;
 
 import AP2DX.*;
 
 //import org.json.simple.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONException;
 
 /**
 * Specialized message for sensor data
 *
 * An example of a USAR sonar message:
 * SEN {Time 395.7716} {Type Sonar} {Name F1 Range 4.5798} {Name F2 Range 2.1461} {Name F3 Range 1.7450} {Name F4 Range 1.5893} {Name F5 Range 0.6239} {Name F6 Range 0.7805} {Name F7 Range 1.2004} {Name F8 Range 2.0657}
 *
 * Sonars' positions are: (USER sim manual)
 * { X(mm), Y(mm), Theta(deg) } = 
 * { 155, -130, -90}
 * { 155, -115, -50}
 * { 190, -805, -30}
 * { 210, -25, -10}
 * { 210, 25, 10}
 * { 190, 80, 30}
 * { 155, 115, 50}
 * { 115, 130, 90}
 *
 * @author Maarten Inja
 */
 public class SonarSensorMessage extends SpecializedMessage 
 {
     private double[] rangeArray;
     private double time;
 
 	public SonarSensorMessage(Module sourceId, Module destinationId)
 	{
 		super(Message.MessageType.AP2DX_SENSOR_SONAR, sourceId, destinationId);
 	}
 
     /** Creates a specialized message from a standard AP2DXMessage.
     * This constructor could be used to clone an AP2DXMessage. */
     public SonarSensorMessage(AP2DXMessage message)
     {
         super(message);
     }
 
     public SonarSensorMessage(AP2DXMessage message, Module sourceId, Module destinationId)
     {
         super(message, sourceId, destinationId);
     }
 
     public void specializedParseMessage()
     {
         time = Double.parseDouble(values.get("time").toString());
 
         rangeArray = new double[7];
 
 		try 
         { 
             // this is, if I'm correct, also in the values map, which 
             // can normally be used to extract the variables, were it not
             // for arrays ...
             JSONObject jsonObject = new JSONObject(messageString);
             JSONArray jsonArray = jsonObject.getJSONArray("rangeArray");
             for (int i = 0; i < jsonArray.length(); i ++)
                 rangeArray[i] = jsonArray.getDouble(i);
         }
         catch (JSONException e)
         {
             System.out.println("Error in AP2DX.specializedMessages.SonarSensorMessage.specializedParseMessage()... things went south!");
             e.printStackTrace();
         }
     }
 
     // setters and getters {{{
 
     public void setRangeArray(double[] value)
     {
         rangeArray = value;
         values.put("rangeArray", value);
     }
 
     public void setTime(double value)
     {
         time = value;
         values.put("time", value);
     }
 
     /** The array of Sonar data.*/
     public double[] getRangeArray()
     {
         return rangeArray;
     }
 
     public double getTime()
     {
         return time;
     }
 
     // }}}
 }
