 package dk.itu.restconnection;
 
 import java.lang.reflect.Type;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 
 public class MeasurementDeserializer implements JsonDeserializer<Measurement[]>
 {
 	@Override
 	public Measurement[] deserialize(JsonElement json, Type type,
 			JsonDeserializationContext ctx) throws JsonParseException 
 	{
 		JsonArray array = json.getAsJsonObject().getAsJsonArray("objects");
 				
 		Measurement[] measurements = new Measurement[array.size()];
 		
 		Measurement temp;
 		JsonObject jsonMeasurement;
		int count = array.size();
		for(int i = 0; i < array.size(); i++)
 		{
 			temp = new Measurement();
 			jsonMeasurement = array.get(i).getAsJsonObject();
 			temp.setUuid(jsonMeasurement.getAsJsonPrimitive("uuid").getAsString());
 			temp.setValue(jsonMeasurement.getAsJsonPrimitive("val").getAsDouble());
 			temp.setTimestamp(jsonMeasurement.getAsJsonPrimitive("timestamp").getAsString());
			measurements[--count] = temp;
 		}
 		
 		return measurements;
 	}
 
 }
