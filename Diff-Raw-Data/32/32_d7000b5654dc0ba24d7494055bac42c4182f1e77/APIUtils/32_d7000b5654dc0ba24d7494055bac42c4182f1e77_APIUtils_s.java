 package com.egeniq.utils.api;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 
 /**
  * Utility methods to deal with data retrieved from the API.
  */
 public class APIUtils {
     private final static SimpleDateFormat _dateFormat;
     static {
         // Interpret dates delivered by the API as en_US in the UTC time zone.
         _dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
         _dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     public static Integer getInt(JSONObject object, String key, Integer fallback) {
         try {
            return object.isNull(key) ? fallback : object.getInt(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static Long getLong(JSONObject object, String key, Long fallback) {
         try {
            return object.isNull(key) ? fallback : object.getLong(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
     
     public static Double getDouble(JSONObject object, String key, Double fallback) {
         try {
            return object.isNull(key) ? fallback : object.getDouble(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
     
     public static Float getFloat(JSONObject object, String key, Float fallback) {
         try {
            return object.isNull(key) ? fallback : Float.parseFloat(object.getString(key));
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static Boolean getBoolean(JSONObject object, String key, Boolean fallback) {
         try {
            return object.isNull(key) ? fallback : object.getBoolean(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static String getString(JSONObject object, String key, String fallback) {
         try {
            return object.isNull(key) ? fallback : object.getString(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static Date getDate(JSONObject object, String key, Date fallback) {
         return getDate(object, key, fallback, null);
     }
 
     public static Date getDate(JSONObject object, String key, Date fallback, SimpleDateFormat format) {
         try {
            return object.isNull(key) ? fallback : format == null ? _dateFormat.parse(object.getString(key)) : format.parse(object.getString(key));
         } catch (JSONException e) {
             return fallback;
         } catch (ParseException e) {
             return fallback;
         }
     }
 
     @SuppressLint("DefaultLocale")
     public static <T extends Enum<T>> T getEnum(JSONObject object, String key, Class<T> enumType, T fallback) {
         try {
             return Enum.valueOf(enumType, getString(object, key, "").toUpperCase());
         } catch (Exception e) {
             return fallback;
         }
     }
 
     public static JSONObject getObject(JSONObject object, String key, JSONObject fallback) {
         try {
            return object.isNull(key) ? fallback : object.getJSONObject(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static JSONArray getArray(JSONObject object, String key, JSONArray fallback) {
         try {
            return object.isNull(key) ? fallback : object.getJSONArray(key);
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static String[] getStringArray(JSONObject object, String key, String[] fallback) {
         try {
            if (object.isNull(key)) {
                 return fallback;
             }
 
             JSONArray array = object.getJSONArray(key);
             String[] result = new String[array.length()];
             for (int i = 0; i < array.length(); i++) {
                 result[i] = array.getString(i);
             }
 
             return result;
         } catch (JSONException e) {
             return fallback;
         }
     }
     
     public static int[] getIntArray(JSONObject object, String key, int[] fallback) {
         try {
            if (object.isNull(key)) {
                 return fallback;
             }
 
             JSONArray array = object.getJSONArray(key);
             int[] result = new int[array.length()];
             for (int i = 0; i < array.length(); i++) {
                 result[i] = array.getInt(i);
             }
 
             return result;
         } catch (JSONException e) {
             return fallback;
         }
     }
 
     public static Collection<Object> getCollection(JSONObject object, String key, Collection<Object> fallback) {
         JSONArray array = getArray(object, key, null);
         if (array == null) {
             return fallback;
         }
 
         try {
             ArrayList<Object> result = new ArrayList<Object>();
             for (int i = 0; i < array.length(); i++) {
                 Object value = array.get(i);
                 if (value instanceof JSONObject) {
                     result.add(getMap((JSONObject)value, null, null));
                 } else {
                     result.add(value);
                 }
             }
     
             return result;
         } catch (JSONException e) {
             return null;
         }            
     }
 
     public static Map<String, Object> getMap(JSONObject object, String key, Map<String, Object> fallback) {
         JSONObject data = key == null ? object : getObject(object, key, null);
         if (data == null) {
             return null;
         }
 
         try {
             HashMap<String, Object> result = new HashMap<String, Object>();
 
             Iterator<?> keyIterator = data.keys();
             while (keyIterator.hasNext()) {
                 String current = (String)keyIterator.next();
 
                 Object value = data.get(current);
                 if (value instanceof JSONObject) {
                     value = getMap(data, current, null);
                 } else if (value instanceof JSONArray) {
                     value = getCollection(data, current, null);
                 }
 
                 result.put(current, value);
             }
             return result;
         } catch (JSONException e) {
             return null;
         }
     }
 }
