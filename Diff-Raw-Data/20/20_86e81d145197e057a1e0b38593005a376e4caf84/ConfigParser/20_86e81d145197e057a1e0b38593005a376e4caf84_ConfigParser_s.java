 package enduro.racer.configuration;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import enduro.MainClass;
 import enduro.racer.Log;
 
 /**
  * a basic config parser. It reads by default from config.conf in the CLASSPATH
  * (but can be configured otherwise)
  * 
  * It reads value pairs of the type A:B where B becomes the value and A the key.
  * default values for race, sorting, input, laps and minimumTime (case
  * sensative) exists, but older key value pairs gets deleted if new appears.
  * Comments ( // )and empty lines are ignored.
  * 
  * A parse error results in continued read. Errors can be polled by getError().
  */
 public class ConfigParser {
 	private HashMap<String, String> tmp = new HashMap<String, String>();
 	private StringBuilder errors = new StringBuilder();
 	private boolean fileNotFound = false;
 	
 	private ArrayList<String> nameList = new ArrayList<String>();
 	private ArrayList<String> startList = new ArrayList<String>();
 	private ArrayList<String> finishList = new ArrayList<String>();
 
 	private static ConfigParser parser = null;
 
 	private ConfigParser() {
 		this.handleInput("config.conf");
 	}
 
 	private ConfigParser(String fileLocation) {
 		this.handleInput(fileLocation);
 	}
 
 	/**
 	 * if the configparser singleton does not exists generates another one,
 	 * otherwise return previously defined singleton.
 	 * 
 	 * @return the singleton configparser
 	 */
 	public static ConfigParser getInstance() {
 		if (parser == null)
 			parser = new ConfigParser();
 		return parser;
 	}
 	
 	/**
 	 * overloads a key/value pair in the config file. should be used in debugging purposes or for other uses,
 	 * when a new config file is not needed / wished for.
 	 * @param key the configuration key
 	 * @param value the String value related to the key.
 	 */
 	public void overLoadValue(String key, String value) {
 		tmp.remove(key);
 		tmp.put(key, value);
 	}
 
 	/**
 	 * unconditionally creates a new singleton instance
 	 * 
 	 * @param fileLocation
 	 *            where the config file is
 	 * @return
 	 */
 	public static ConfigParser getInstance(String fileLocation) {
 		parser = new ConfigParser(fileLocation);
 		return parser;
 	}
 
 	private void handleInput(String fileLocation) {
 		tmp.put("race", "laps");
 		tmp.put("sorting", "number");
 		tmp.put("input", "file");
 		tmp.put("laps", "3");
 		tmp.put("stages", "3");
 		tmp.put("mintime", "01.00.00");
 		tmp.put("timelimit", "00.15.00");
 		tmp.put("sorted", "false");
 		
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileLocation));
 			int line = 1;
 			while (in.ready()) {
 				String temp = in.readLine();
 				if(!correctLine(temp,line))
 					continue;
 				
 				if(MainClass.debug)
 					System.out.println("config line:" + temp);
 				
 				
 				if(temp.startsWith("name")) {
 					if(MainClass.debug)
 						System.out.println("\tadding name:" + temp.substring(5));
 					this.nameList.add(temp.substring(5));
 					
 				} else if(temp.startsWith("finish")) {
 					if(MainClass.debug)
 						System.out.println("\tadding finish:" + temp.substring(7));
 					
 					this.finishList.add(temp.substring(7));
 					
 				} else if(temp.startsWith("start")) {
 					if(MainClass.debug)
 						System.out.println("\tadding start:" + temp.substring(6));
 					
 					this.startList.add(temp.substring(6));
 					
 				} else if (!temp.startsWith("//") && temp.length() > 0) {
 					if(MainClass.debug)
 						System.out.println("\tadding config" + temp);
 					try {
 						String[] res = temp.split(":");
 						tmp.remove(res[0]);
 						tmp.put(res[0], res[1]);
 					} catch (Exception E) {
 						errors.append("line " + line + ":: parse error::"
 								+ temp + "\n");
 					}
 				}
 				line++;
 			}
 		} catch (Exception e) {
 			fileNotFound = true;
 		}
 	}
 
 	/**
 	 * Returns the configuration value corresponding to a given key as an integer.
 	 * 
 	 * @param key
 	 *            The key which the value is mapped to
 	 * @return the value the key is mapped to as an integer, or <code>null</code> if there was
 	 *         no mapping for this key
 	 */
 	public int getIntConf(String key) {
 		return Integer.parseInt(tmp.get(key));
 	}
 
 	/**
 	 * Returns the configuration value corresponding to a given key as a String.
 	 * 
 	 * @param key
 	 *            The key which the value is mapped to
 	 * @return the value the key is mapped to as a String, or <code>null</code> if there was
 	 *         no mapping for this key
 	 */
 	public String getStringConf(String key) {
 		return tmp.get(key);
 	}
 
 	public String toString() {
 		return tmp.toString();
 	}
 
 	/**
 	 * a string containing all errors generated while parsing.
 	 * 
 	 * @return
 	 */
 	public String getError() {
 		return this.errors.toString();
 	}
 
 	/**
 	 * Returns <code>true</code> if the file given to the constructor wasn't
 	 * found.
 	 * 
 	 * @return <code>true</code> if the file given to the constructor wasn't
 	 *         found
 	 */
 	public boolean fileNotFound() {
 		return fileNotFound;
 	}
 	
 	/**
 	 * returns a list of the type A:B or possibly A depending on which format is used.
 	 * (A:B means stage:filename A means filename)
 	 * @return an array of name file information to be further parsed
 	 */
 	public String[] getNameFileList() {
 		return this.nameList.toArray(new String[this.nameList.size()]);
 	}
 	
 	/**
 	 * returns a list of the type A:B or possibly A depending on which format is used.
 	 * (A:B means stage:filename A means filename)
 	 * @return an array of start file information to be further parsed
 	 */
 	public String[] getStartFileList() {
 		return this.startList.toArray(new String[this.startList.size()]);
 	}
 	
 	/**
 	 * returns a list of the type A:B or possibly A depending on which format is used.
 	 * (A:B means stage:filename A means filename)
 	 * @return an array of finish file information to be further parsed
 	 */
 	public String[] getFinishFileList() {
 		return this.finishList.toArray(new String[this.finishList.size()]);
 	}
 	
 	/**
 	 * Deletes the singleton instance
 	 */
 	public static void delete(){
 		parser = null;
 	}
 	
 	private boolean correctLine(String lineText, int line) {
 		if(Pattern.matches("//.*", lineText)) {
 			return true;
 		} else if(Pattern.matches("(namn:.*)|(start:.*)|(finish:.*)",  lineText)) {
 			if(Pattern.matches("\\w+:(\\d+:)\\d+", "namn:12:1"))
 				return true;
 		} else if(Pattern.matches("[a-zA-Z]*:.*", lineText)) {
 			if(Pattern.matches("[a-zA-Z]*:\\w+", lineText))
 				return true;
 			else if(Pattern.matches("[a-zA-Z]*:\\d\\d\\.\\d\\d\\.\\d\\d", lineText))
 				return true;
 			else if(Pattern.matches("[a-zA-Z]*:[0-9a-zA-Z/._]+", lineText))
 				return true;
 		} else if(Pattern.matches("\\s", lineText)) {
 			return true;
 		} else if(lineText.length() == 0)
 			return true;
 		errors.append("in the config file on line " + line + ": incorrect line:\n\t" + lineText + "\n");
 		Log.log("in the config file on line " + line + ": incorrect line:\n\t" + lineText + "\n");
 		return false;
 	}
 	
 }
