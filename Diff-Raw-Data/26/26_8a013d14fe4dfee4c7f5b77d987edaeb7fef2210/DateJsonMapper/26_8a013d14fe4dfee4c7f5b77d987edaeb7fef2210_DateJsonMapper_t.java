 package com.example.snms;
 
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 import java.lang.reflect.Type;
 
 import android.util.Log;
 
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 
 
 
 public class DateJsonMapper implements JsonDeserializer<Date> {
 
     public Date deserialize(JsonElement json, Type typeOfT,
             JsonDeserializationContext context) throws JsonParseException {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         try {
 			return dateFormat.parse(json.getAsString());
 		
 		} catch (ParseException e) {
 			e.printStackTrace();
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			try {
				return dateFormat.parse("2013-10-27T00:00:33:00");
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return new Date(); 
 		}
     }
 }
