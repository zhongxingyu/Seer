 package com.aaasen.smsvis.util;
 
 import java.lang.reflect.Type;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 public class SMSSerializer implements JsonSerializer<SMS> {
 	
 	@Override
 	public JsonElement serialize(SMS message, Type type, JsonSerializationContext context) {
 		JsonObject result = new JsonObject();
 		result.add("address", new JsonPrimitive(message.getAddress()));
 		result.add("time", new JsonPrimitive(message.getDate().getTime() / 1000));
 		result.add("sent", new JsonPrimitive(message.isSent()));
 
 		return result;
 	}
 }
