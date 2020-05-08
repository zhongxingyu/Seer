 package utils;
 
 import java.lang.reflect.Type;
 
 import org.joda.time.DateTime;
 
 import play.Play;
 
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 public class GsonFactory {
 
 	public static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
 
 	public static GsonBuilder gsonBuilder() {
 		GsonBuilder gson = new GsonBuilder();
 		gson.setDateFormat(ISO8601_DATE_FORMAT);
 		if (Boolean.parseBoolean(Play.configuration.getProperty("json.prettyprinting"))) {
 			gson.setPrettyPrinting();
 		}
 		gson.registerTypeAdapter(DateTime.class, new GsonFactory.DateTimeSerializer());
 		gson.registerTypeAdapter(DateTime.class, new GsonFactory.DateTimeDeserializer());
 		return gson;
 	}
 
 	public static class DateTimeSerializer implements JsonSerializer<DateTime> {
 		public JsonElement serialize(DateTime src, Type typeOfSrc,
 				JsonSerializationContext context) {
 			return new JsonPrimitive(src.toString());
 		}
 	}
 
 	private static class DateTimeDeserializer implements JsonDeserializer<DateTime> {
 		public DateTime deserialize(JsonElement json, Type typeOfT,
 				JsonDeserializationContext context) throws JsonParseException {
			return new DateTime(json.getAsJsonPrimitive().getAsString());
 		}
 	}
 }
