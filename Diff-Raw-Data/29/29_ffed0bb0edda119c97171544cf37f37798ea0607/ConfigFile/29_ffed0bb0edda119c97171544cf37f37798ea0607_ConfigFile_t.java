 /* file name  : ConfigFile.java
  * authors    : Nacho Barrientos Arias <chipi@criptonita.com>
  * created    : 
  * copyright  : GPL
  *
  * modifications:
  *
  */
 package es.uniovi.aic.miex.config;
 
 import java.util.Properties;
 import java.util.HashMap;
 import java.io.*;
 import java.util.*;
 import java.lang.Exception;
 
 /** 
  * Configuration files handler 
  * 
  * @author Nacho Barrientos Arias <chipi@criptonita.com>
  * @version 0.1
  */
 public class ConfigFile
 {
 
 	/** 
 	 * Creates the object, reads the configuration file and checks
 	 * if it's in a good shape. 
 	 * 
 	 * @param fileName Path to the configuration file to read 
 	 */
 	public ConfigFile(String fileName)
 	{
 		properties = new Properties();
 
 		try
 		{
 			properties.load(new FileInputStream(fileName));
 			this.checkFile();
 		}
 		catch(Exception e)
 		{
 			System.err.println("ERROR: " + e.getMessage());
 			System.exit(-1);
 		}
 
 	}
 
 	/** 
 	 * Checks configuration file consistency 
 	 * 
 	 * @throws Exception If something weird happens, i.e.:
    *				    - Missing mandatory fields
    *            - Boolean fields set as non-boolean 
 	 */
 	private void checkFile() throws Exception
 	{
 	
 		// Looking for missing required config fields	
 		for(int i=0; i < configFileFields.length; i++)
 			try
 			{
 				getRealName(configFileFields[i]);
 			}
 			catch(Exception e)
 			{
 				System.err.println("Missing required field " + configFileFields[i] + " in config file.");
 				System.exit(-1);
 			}
 		
 		// Looking for wrong set boolean fields
 		for(int i=0; i < configFileBooleanFields.length; i++)
 			try
 			{
 				String real = getRealName(configFileBooleanFields[i]);
 
 				if( !(properties.getProperty(real).toLowerCase().equals("yes"))
 						&&
 						!(properties.getProperty(real).toLowerCase().equals("no")))
 				throw new Exception("Boolean keys must only contain \"Yes\" or \"No\" values");
 			}
 			// If a realname couldn't be found it means that it's commented out, so don't checkin' its value.
 			catch(Exception e)
 			{
 				continue;
 			}
 
 	}
 
 	/** 
 	 * Observer method for non-boolean settings
 	 * 
 	 * @param stt The property to fetch
 	 * @return The value 
 	 */
 	public String getStringSetting(String stt)
 	{
 		String real = null;
 
 		try
 		{
 			real = getRealName(stt);
 		}
 		catch(Exception e)
 		{
			return defaultValuesString.get(stt.toLowerCase());
 		}
 
 		return properties.getProperty(real);
 	}
 
 	/** 
 	 * Observer method for boolean settings 
 	 * 
 	 * @param stt The property to fetch 
 	 * @return The value
 	 */
 	public boolean getBooleanSetting(String stt)
 	{
     String real = null;
 
     try
     {
       real = getRealName(stt);
       //System.out.println("Real name for " + stt + " is " + real);
     }
     catch(Exception e) // If real field name is requested and not found, using default value
     {
       //System.err.println("\n\tW: Using default bahaviour for field" + stt);
 			return defaultValuesBoolean.get(stt.toLowerCase()).booleanValue();
     }
 		
 		// Exists
 		if(properties.getProperty(real).toLowerCase().equals("yes"))
 				return true;
 			else
 				return false;
 	}
 
 	private String getRealName(String dumbprop) throws Exception
 	{
 		String str;
 
 		// Searching among all the fields the realname of dumpprop
 		for(Enumeration e = properties.propertyNames(); e.hasMoreElements(); )
 		{
 			str = (String)e.nextElement(); 
 
 			if(str.toString().toLowerCase().equals(dumbprop.toLowerCase()))
 				return str;
 		}
 		throw new Exception("\'" + dumbprop + " not found.");
 	}
 
 	/** 
 	 * Storage for all settings after reading the configuration file 
 	 */
 	private Properties properties;
 
 	/** 
 	 * Self-explaining 
 	 */
 	private static String[] configFileFields, configFileBooleanFields; 
 	
 	/** 
 	 * Self-explaining 
 	 */
 	private static HashMap<String,String> defaultValuesString;
 	
 	/** 
 	 * Self-explaining 
 	 */
 	private static HashMap<String,Boolean> defaultValuesBoolean;
 
   static
   {
     configFileFields = new String[]{"BDHostname", "BDUser", "BDPassword", "BDName"};
 
 		configFileBooleanFields = new String[]{"Validate", "CreateDB", "Dump", "Normalize"};
 		
 		defaultValuesString = new HashMap<String,String>();
 		defaultValuesBoolean = new HashMap<String,Boolean>();
 
 		// Loading default values to boolean settings
 		defaultValuesBoolean.put("validate", new Boolean(false));
 		defaultValuesBoolean.put("dump", new Boolean(false));
 		defaultValuesBoolean.put("createdb", new Boolean(false));
 		defaultValuesBoolean.put("normalize", new Boolean(false));
 
 		// Loading default values to string settings
 		defaultValuesString.put("xmlschemauri", "/usr/share/miex/schemas/default.xsd");
		defaultValuesString.put("grammaruri", "/usr/share/miex/grammars/englishPCFG.ser.gz");
     defaultValuesString.put("dumpdir", "/tmp");
     defaultValuesString.put("sqlskeleton", "/usr/share/miex/sql/skeleton.sql");
 
   }
 
 }
