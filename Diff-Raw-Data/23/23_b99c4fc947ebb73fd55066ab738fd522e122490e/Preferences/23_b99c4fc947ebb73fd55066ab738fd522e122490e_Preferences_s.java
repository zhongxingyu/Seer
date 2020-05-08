 package de.bitowl.space;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.JsonReader;
 import com.badlogic.gdx.utils.JsonWriter;
 import com.badlogic.gdx.utils.OrderedMap;
 
 public class Preferences {
 	static HashMap<String,Object> preferences;
 	
 	public static void init(){
 		preferences=new HashMap<String, Object>();
 		if(!Gdx.files.local("space/preferences.json").exists()){return;} // we can not load any preferences
 		JsonReader reader=new JsonReader();
 	
 		// read all the preferences and save it in the hashmap TODO just use the OrderedMap from libgdx?
 		@SuppressWarnings("unchecked")
 		OrderedMap<String, Object> values=(OrderedMap<String,Object>)reader.parse(Gdx.files.local("space/preferences.json"));
 		for(String key : values.keys()){
 			preferences.put(key, values.get(key));
 		}
 	}
 	
 	public static String getString(String pKey,String pDefault){
 		if(preferences.containsKey(pKey)){
			return (String) preferences.get(pKey);
 		}else{
 			return pDefault;
 		}
 	}
 	public static int getInt(String pKey,int pDefault){
 		if(preferences.containsKey(pKey)){
			return Integer.parseInt((String) preferences.get(pKey));
 		}else{
 			return pDefault;
 		}
 	}
 	public static float getFloat(String pKey,float pDefault){
 		if(preferences.containsKey(pKey)){
			return Float.parseFloat((String) preferences.get(pKey));
 		}else{
 			return pDefault;
 		}
 	}
 	
 	
 	public static void putString(String pKey,String pValue){
 		preferences.put(pKey, pValue);
 	}
 	public static void putInt(String pKey,int pValue){
 		preferences.put(pKey, pValue);
 	}	
 	public static void putFloat(String pKey,float pValue){
 		preferences.put(pKey, pValue);
 	}
 
 	
 	/**
 	 * saves the preferences into the json file
 	 */
 	public static void flush() {	
 		try {
 			JsonWriter writer=new JsonWriter(Gdx.files.local("space/preferences.json").writer(false));
 			writer.object();
 			writer.set("test", 5);
 			for(String key : preferences.keySet()){
 					writer.set(key, preferences.get(key));			
 			}
 		
 			writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
