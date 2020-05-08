 package jp.ac.tsukuba.cs.conclave.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 /**
  * This class holds a set of parameters, that can be used for a variety of projects.
  * 
  * Parameters are stored as values associated with unique key strings. Parameter values are 
  * stored and retrieved as strings. Key values are NOT case sensitive.
  * 
  * These parameters can be read from a text file with the following sintax:
  * * Lines starting with "#" are commentary, and are ignored.
  * * Any text past a # is treated as a commentary, and ignored.
  * * Parameters are on the form: "A = B", where A and B can be any string of characters.
  * 
  * 
  * @author caranha
  *
  */
 
 public class Parameter {
 	
 	protected static Logger log = null;
 	HashMap<String,String> values;
 	
 	public Parameter()
 	{
 		if (Parameter.log == null) 
 			Logger.getLogger(Parameter.class.getPackage().getName());
 		values = new HashMap<String,String>();
 	}
 	
 	/**
 	 * Loads a set of parameters from a text file. The format is 
 	 * "A = B", where A and B are strings not containing the character '='
 	 * Also, any character '#' causes the rest of the line to be ignored
 	 * 
 	 * This function returns the number of parameters read, or -1 if an 
 	 * error was encoutered.
 	 * 
 	 * This function does not clear the parameter instance. It adds 
 	 * to current parameters, and replaces duplicate values.
 	 * 
 	 * @param Filename
 	 * @return
 	 * @throws IOException 
 	 */
 	public int loadTextFile(String filename) throws IOException
 	{
     	BufferedReader reader;
     	String line = null;
     	 	
     	reader = new BufferedReader(new FileReader(new File(filename)));
 
     	while ((line = reader.readLine()) != null) 
     	{
     		int index = line.indexOf("#");
     		if (index != -1)
     			line = line.substring(0, index); // removing everything past the first "#"
     		
     		if (line.length() > 0) // is there anything left?
     		{	
     			String[] input = line.split("=");
 
     			if (input.length != 2) // this line is something different than "A = B"! skip it and log to error.
     			{
    				log.severe("Ignoring invalid parameter string in the parameter file '"+filename+"': "+line);
     				continue;
     			}
     				
     			input[0] = input[0].trim();
     			input[1] = input[1].trim();
     			input[0] = input[0].toLowerCase(Locale.ENGLISH); // parameters are not case sensitive
     				
     			this.addParameter(input[0], input[1]);
     		
     		}
     	}
     	
     	reader.close();
     	
     	return this.getNumberOfKeys();
		
 	}
 	
 	/**
 	 * Adds the key/value pair to the parameter database. If the key already exists, the 
 	 * value is replaced. if the value is "null", the key will be removed instead.
 	 * 
 	 * @param name Key to be added (case is ignored)
 	 * @param value Value to be added (will replace existing values)
 	 * @return The value added, for chaining.
 	 */
 	public String addParameter(String name, String value)
 	{
 		if (value == null)
 			values.remove(name.toLowerCase(Locale.ENGLISH));
 		else
 			values.put(name.toLowerCase(Locale.ENGLISH), value);
 		return value;
 	}
 	
 	/**
 	 * Removes a key and its associated value from the Parameter file.
 	 * returns the associated value for chaining.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public String delParameter(String name)
 	{
 		return values.remove(name.toLowerCase(Locale.ENGLISH));
 	}
 	
 	
 	/**
 	 * Gets the value for the parameter associated with the key "name". 
 	 * If no parameter is found, or if the associated parameter value is null, 
 	 * then "def value" is added to this key, and returned.
 	 * 
 	 * @param name the key to be used.
 	 * @param defvalue Default value, in case the key "name" is non existant or null.
 	 * @return
 	 */
 	public String getParameter(String name, String defvalue)
 	{
 		String lname = name.toLowerCase(Locale.ENGLISH);
 		String ret = values.get(lname);
 		
 		if (ret == null) // using the default value if ret is null;
 		{
 			ret = defvalue;
 			values.put(lname, ret);
 		}		
 		return ret;
 	}
 	
 	/**
 	 * Returns the total number of keys stored in this parameter instance
 	 * @return
 	 */
 	public int getNumberOfKeys()
 	{
 		return values.size();
 	}
 	
 	/**
 	 * Returns an array of strings with all the keys that are defined in this object
 	 * @return
 	 */
 	public String[] getKeys()
 	{
		return (String[]) values.keySet().toArray();
 	}
 	
 	/**
 	 * Returns an array with all the keys and values contained in this parameter instance.
 	 * If this parameter instance is empty, returns null.
 	 * @return
 	 */
 	public String[][] getKeysValues()
 	{
 		int size = this.getNumberOfKeys();
 		if (size == 0)
 			return null;		
 		
 		String[][] ret = new String[size][2];
 		
 		String keys[] = this.getKeys();
		String vals[] = (String[]) values.values().toArray();
 		
 		for (int i = 0; i < size; i++)
 		{
 			ret[i][0] = keys[i];
 			ret[i][1] = vals[i];
 		}		
 		return ret;
 	}
 	
 	@Override
 	public String toString()
 	{
 		String ret = "";
 		String[][] KV = this.getKeysValues();
 		int size = this.getNumberOfKeys();
 		
 		if (KV != null)
 			for (int i = 0; i < size; i++)
 			{
 				ret += KV[i][0]+" = "+KV[i][1]+"\n";
 			}
 		return ret;
 	}
 	
 	
 	/**
 	 * Clears all the stored parameters;
 	 */
 	public void clear()
 	{
 		values.clear();
 	}	
 }
