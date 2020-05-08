 package ptg;
 
 import java.util.HashMap;
 
 /**
  * Keeps track of any per-run or global configuration information.  Currently, all configuration is
  * expected to be done on each program run.  This class makes no attempt to persist configuration past runtime.
  * It also does not provide any mechanism for setting default configuration values.
  * 
  * Configuration key names are case insensitive, and must be unique.  Behavior is undefined if there are multiple
  * configuration keys with identical names.
  *
  */
 public class ConfigMgr {
 	private HashMap<String,String> config;
 
 	/**
 	 * Load all the arguments so they can be accessed later.  Arguments should be in the form:
 	 * 		argname=argvalue
 	 * Each argument should be in a separate item in the String array.
 	 * @param args Arguments (from command line or otherwise) to load
 	 */
 	public ConfigMgr(String args[]) {
 		config = new HashMap<String,String>();
 		for (String s: args)
 		{
			String info[] = s.split("=",2);
 			if (info.length == 2)
 			{
 				config.put(info[0].toLowerCase(),info[1].trim());
 			}
 			else
 			{
 				config.put(info[0].toLowerCase(), "");
 			}
 		}
 	}
 	
 	/**
 	 * Retrieve a configuration option based on the key name
 	 * @param name Name of the configuration option to retrieve.  Case insensitive
 	 * @return String value of the configuration option, or null if it was not set
 	 */
 	public String getConfig(String name)
 	{
 		return config.get(name.toLowerCase());
 	}
 
 }
