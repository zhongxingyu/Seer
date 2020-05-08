 package com.bananity.util.serialization;
 
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * @author Andreu Correa Casablanca
  */
 public class JsonSerializer {
 
 	public static String ObjectToJsonString (Object jsonObject) {
 		if (jsonObject == null) {
 
 			return "null";
 
 		} if (jsonObject instanceof Boolean) {
 
 			return ((Boolean)jsonObject).toString();
 
 		} if (jsonObject instanceof Integer) {
 
 			return ((Integer)jsonObject).toString();
 
 		} if (jsonObject instanceof Long) {
 
 			return ((Long)jsonObject).toString();
 
 		} if (jsonObject instanceof Float) {
 
 			return ((Float)jsonObject).toString();
 
 		} if (jsonObject instanceof Double) {
 
 			return ((Double)jsonObject).toString();
 
 		} if (jsonObject instanceof String) {
 
 			return StringToJsonString((String)jsonObject);
 
 		} if (jsonObject instanceof Collection) {
 
 			return CollectionToJsonString((Collection)jsonObject);
 
 		} if (jsonObject instanceof Object[]) {
 
 			return ArrayToJsonString((Object[])jsonObject);
 
 		} if (jsonObject instanceof Map) {
 
 			return MapToJsonString((Map)jsonObject);
 
 		} if (jsonObject instanceof IJsonSerializable) {
 
 			return ((IJsonSerializable)jsonObject).toJsonStr();
 
 		} else {
 			return ""; // TODO : throw an Exception
 		}
 	}
 
 	public static String StringToJsonString (String str) {
		return new StringBuilder().append("\"").append(str).append("\"").toString();
 	}
 
 	public static String CollectionToJsonString (Collection jsonCollection) {
 		StringBuilder result = new StringBuilder("[");
 
 		boolean isStart = true;
 		for (Object item : jsonCollection) {
 			if (isStart) {
 				isStart = false;
 			} else {
 				result.append(",");
 			}
 
 			result.append(ObjectToJsonString(item));
 		}
 
 		return result.append("]").toString();
 	}
 
 	public static String ArrayToJsonString (Object[] jsonArray) {
 		StringBuilder result = new StringBuilder("[");
 
 		boolean isStart = true;
 		for (Object item : jsonArray) {
 			if (isStart) {
 				isStart = false;
 			} else {
 				result.append(",");
 			}
 
 			result.append(ObjectToJsonString(item));
 		}
 
 		return result.append("]").toString();
 	}
 
 	public static String MapToJsonString (Map<String, Object> jsonMap) {
 		StringBuilder result = new StringBuilder("{");
 
 		boolean isStart = true;
 		for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
 			if (isStart) {
 				isStart = false;
 			} else {
 				result.append(",");
 			}
 
 			result.append("\"").append(entry.getKey()).append("\":").append(ObjectToJsonString(entry.getValue()));
 		}
 
 		return result.append("}").toString();
 	}
 }
