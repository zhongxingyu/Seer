 /**
  * 
  */
 package config;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  * @author Peter Vollmer
  *
  */
 public class ConfigManager {
 
 	private static ConfigManager instance = null;
 	
 	/**
 	 * 	Parses the config file in JSON format.
 	 */
 	private final JSONParser parser = new JSONParser();
 	
 	
 	/**
 	 * Storage of configuration entries
 	 */
 	private Map<String, Object> confMap = null;
 	
 	
 	/**
 	 * Construct the ConfigManager for centralize all configurations of the server
 	 * 
 	 * @param configPath
 	 * @throws Exception
 	 */
 	private ConfigManager(String configPath) throws Exception {
 		BufferedReader reader = null;
 		reader = new BufferedReader(new InputStreamReader(
 					new FileInputStream(configPath), Charset.forName("UTF-8")));
 		JSONObject requestJSON = (JSONObject) parser.parse(reader);
 
 		this.confMap = requestJSON;
 		
 	}		
 
 	/**
 	 * Initialize the config management and construct the configManager.
 	 * To do before getInstance
 	 * 
 	 * @param configPath
 	 * 			
 	 * @throws Exception
 	 */
 	public static void Init (String configPath) throws Exception {
 		instance = new ConfigManager(configPath);
 	}
 	
 	
 	/**
 	 * Return the value of a given key or the given defaultValue
 	 * 
 	 * @param key
 	 * @param defaultValue
 	 * @return value 
 	 *			 of a given key or the given defaultValue
 	 */
 	public String getEntry(String key, String defaultValue){
 		String value = null;
 		if (confMap.containsKey(key)){
			value = (String)confMap.get(key);
 		} else {
 			value = defaultValue;
 		}
 		return value;
 	}
 	
 	/**
 	 * Returns the value of the given key
 	 * 
 	 * @param key
 	 * 			to give the 
 	 * @return value (default value is null)
 	 */
 	public String getEntry (String key){
 		String value = null;
 		if (confMap.containsKey(key)){
 			value = (String)confMap.get(key);
 		}
 		return value;
 	}
 	
 	
 	/**
 	 * Return an instance of a constructed ConfigManager. Need Init before.
 	 * 
 	 * 
 	 * @return instance of ConfigManager
 	 */
 	public static ConfigManager getInstance()	{
 		return instance;
 	}
 	
 }
