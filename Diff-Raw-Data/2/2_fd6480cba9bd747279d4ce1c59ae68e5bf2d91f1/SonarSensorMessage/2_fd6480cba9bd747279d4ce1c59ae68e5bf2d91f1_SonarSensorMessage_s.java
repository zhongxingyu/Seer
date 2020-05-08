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
 * @author Maarten Inja
 */
 public class SonarSensorMessage extends SpecializedMessage 
 {
     private double[] rangeArray;
     private double time;
 
 	public SonarSensorMessage(Module sourceId, Module destinationId)
 	{
		super(sourceId, destinationId);
 	}
 
     /** Creates a specialized message from a standard AP2DXMessage.
     * This constructor could be used to clone an AP2DXMessage. */
     public SonarSensorMessage(AP2DXMessage message)
     {
         super(message);
     }
 
 
     public void parseMessage()
     {
         super.parseMessage();
 
         time = Double.parseDouble(values.get("time").toString());
 
         rangeArray = new double[7];
 
 		try 
         {
             JSONObject jsonObject = new JSONObject(messageString);
             JSONArray jsonArray = jsonObject.getJSONArray("rangeArray");
             for (int i = 0; i < jsonArray.length(); i ++)
                 rangeArray[i] = jsonArray.getDouble(i);
         }
         catch (JSONException e)
         {
             System.out.println("Error in AP2DX.specializedMessages.SonarSensorMessage.parseMessage()... things went south!");
             e.printStackTrace();
         }
     }
 
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
 
 }
