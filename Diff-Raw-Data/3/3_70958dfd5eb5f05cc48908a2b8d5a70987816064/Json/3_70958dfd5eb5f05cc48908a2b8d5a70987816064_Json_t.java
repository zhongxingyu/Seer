 package jewas.json;
 
 import com.google.gson.*;
 import jewas.lang.Objects;
 import jewas.lang.Strings;
 import jewas.reflection.Properties;
 import jewas.reflection.Property;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 
 /**
  * Utility class used for JSON from/to conversions.
  */
 public class Json {
 
     public static final String DATE_FORMAT = "yyyy-MM-dd";
     public static final String DATE_TIME_FORMAT = DATE_FORMAT + " HH:mm:ss";
     private static Json instance = null;
 
     private GsonBuilder gson = new GsonBuilder();
 
     private Json() {
         gson.registerTypeAdapter(DateTime.class, new DateTimeSerializer());
         gson.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
         gson.registerTypeAdapter(DateMidnight.class, new DateMidnightSerializer());
         gson.registerTypeAdapter(DateMidnight.class, new DateMidnightDeserializer());
     }
 
     public static Json instance() {
         if (instance == null) {
             instance = new Json();
         }
         return instance;
     }
 
     /**
      * Converts an object into a JSON object.
      *
      * @param sourceObject the Java object to convert in JSON object
      * @return a JSON string representation of the conversion.
      */
     public String toJsonString(Object sourceObject) {
 
         if (Objects.isNull(sourceObject)) {
             return "{}";
         } else {
              return gson.create().toJson(sourceObject);
         }
     }
 
     public Object fromJsonString(String jsonString, Class clazz) {
         if (Strings.isNullOrEmptyString(jsonString)) {
             return Objects.NULL;
         } else {
            //noinspection unchecked
             return gson.create().fromJson(jsonString, clazz);
         }
     }
 
     private class DateTimeSerializer implements JsonSerializer<DateTime> {
 
         public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
             return new JsonPrimitive(new SimpleDateFormat(DATE_TIME_FORMAT).format(src.toDate()));
         }
     }
 
     private class DateMidnightSerializer implements JsonSerializer<DateMidnight> {
 
         public JsonElement serialize(DateMidnight src, Type typeOfSrc, JsonSerializationContext context) {
             return new JsonPrimitive(new SimpleDateFormat(DATE_FORMAT).format(src.toDate()));
         }
     }
 
     private class DateTimeDeserializer implements JsonDeserializer<DateTime> {
 
         public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                 throws JsonParseException {
             try {
                 Date date = new SimpleDateFormat(DATE_TIME_FORMAT).parse(json.getAsJsonPrimitive().getAsString());
                 return new DateTime(date);
             } catch (ParseException e) {
                 throw new JsonParseException(e);
             }
         }
     }
 
       private class DateMidnightDeserializer implements JsonDeserializer<DateMidnight> {
 
         public DateMidnight deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                 throws JsonParseException {
             try {
                 Date date = new SimpleDateFormat(DATE_FORMAT).parse(json.getAsJsonPrimitive().getAsString());
                 return new DateMidnight(date);
             } catch (ParseException e) {
                 throw new JsonParseException(e);
             }
         }
     }
 }
