 package com.blastic.adapters;
 
 import java.lang.reflect.Field;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.util.Log;

public class JsonAdapter {
	public static <T> T bindJsonToClass (Class<T> type, JSONObject jsonObject){
 		try {
 			T object = (T) type.newInstance();
 			Field[] fields = type.getDeclaredFields();
 	        for (Field field : fields) {
 	        	Object valueField=null;
 	        	//Verify if exists the field with Camel notation;
 	        	try{
 	        		valueField = jsonObject.getString(field.getName());
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 	        	//If not, verify if exist the field with Pascal notation;
 	        	if(valueField == null){
 	        		String camelFieldName = ConvertToCamelNotation(field.getName());
 	        		try{
 	        			valueField = jsonObject.get(camelFieldName);
 	        		} catch (JSONException e) {
 	        			e.printStackTrace();
 	        		}
 	        	}
 	        	//Assign the value of the jsonObject
 				if(!field.isAccessible()) {
 					field.setAccessible(true);
 				}
 	        	field.set(object, valueField);	        	
 	        	
 	        } 
 			return object;
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	static String ConvertToCamelNotation(String s) {
 	    return s.substring(0, 1).toUpperCase() +
 	               s.substring(1);
 	}
 }
