 package net.buddat.mineserv.options;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import net.buddat.mineserv.net.packet.handlers.PingHandler;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to manage all Options, also includes default options.
  * 
  * @author Budda
  */
 public class Options {
 	
 	/** The default logger for this class. */
 	private final Logger logger = LoggerFactory.getLogger(PingHandler.class);
 	
 	/** The character to use to denote comments in the config file. */
 	public static final String COMMENT_CHAR = "#";
 	
 	/** The String to denote delimiters between keys and values in the config file. */
 	public static final String DELIMITER = "=";
 	
 	public static final String LIST_DELIMITER = ",";
 	
 	/** Map of all options. */
 	private HashMap<String, Option> optionMap = new HashMap<String, Option>();
 
 	/**
 	 * Instantiates a new options.
 	 */
 	public Options() { }
 	
 	/**
 	 * Instantiates a new options.
 	 *
 	 * @param file config file to load
 	 */
 	public Options(String file) {
 		load(file);
 	}
 	
 	/**
 	 * Gets option corresponding to key
 	 *
 	 * @param key the key
 	 * @return the option - null if the option doesn't exist
 	 */
 	public Option getOption(String key) {
 		if (optionMap.containsKey(key))
 			return optionMap.get(key);
 		
 		return null;
 	}
 	
 	/**
 	 * Adds the option.
 	 *
 	 * @param key the key
 	 * @param value the option to add
 	 */
 	public void addOption(String key, Option value) {
 		if (optionMap.containsKey(key)) {
 			optionMap.get(key).setValue(value.getValue());
 			optionMap.get(key).setComment(value.getComment());
 		} else {
 			optionMap.put(key, value);
 		}
 			
 	}
 
 	/**
 	 * Loads the config file specified.
 	 * Initially just replaced values of default options, but also
 	 * loads non-default options if they are in the file.
 	 *
 	 * @param file the config file
 	 */
 	private void load(String file) {
 		File f = new File(file);
 		if (!f.exists()) {
 			save(file);
 			return;
 		}
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			String line = "", previousLine = "";
 			
 			while ((line = br.readLine()) != null) {
 				if (!line.startsWith(COMMENT_CHAR)) {
 					if (line.contains(DELIMITER)) {
 						String parts[] = line.split(DELIMITER);
 						
 						if (getOption(parts[0]) != null)
 							getOption(parts[0]).setValue(parts[1]);
 						else {
 							try {
 								int value = Integer.parseInt(parts[1]);
 								
 								if (previousLine.startsWith(COMMENT_CHAR))
 									addOption(parts[0], new IntegerOption(parts[0], value, previousLine));
 								else
 									addOption(parts[0], new IntegerOption(parts[0], value));
 								
 							} catch (NumberFormatException e) {
 								if (parts[1].equalsIgnoreCase("true") || parts[1].equalsIgnoreCase("false")) {
 									boolean value = Boolean.parseBoolean(parts[1]);
 									
 									if (previousLine.startsWith(COMMENT_CHAR))
 										addOption(parts[0], new BooleanOption(parts[0], value, previousLine));
 									else
 										addOption(parts[0], new BooleanOption(parts[0], value)); 
 								} else {
 									String value = parts[1];
 									
 									if (previousLine.startsWith(COMMENT_CHAR))
 										addOption(parts[0], new StringOption(parts[0], value, previousLine));
 									else
 										addOption(parts[0], new StringOption(parts[0], value));
 								}
 							}
 						}
 					}
 				}
 				
 				previousLine = line;
 			}
 			
 			logger.info("Loaded config file from {}", file);
 		} catch (IOException e) {
 			logger.error("Unable to load config file from {}", file);
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Saves all options to config file with comments where possible.
 	 *
 	 * @param file the config file
 	 */
 	private void save(String file) {
 		File f = new File(file);
 		try {
 			f.createNewFile();
 			
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 			
 			for (String key : optionMap.keySet()) {
 				Option o = optionMap.get(key);
 				bw.write(o.toString());
				bw.newLine();
				bw.newLine();
 			}
 			
			bw.flush();
			bw.close();
			
 			logger.info("Saved config file to {}", file);
 		} catch (IOException e) {
 			logger.error("Unable to save config file to {}", file);
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Defines the default options and adds them to the optionMap.
 	 */
 	{
 		Option defaultOptions[] = { 
 				new StringOption(NAME, "MineServ"),
 				new IntegerOption(PORT, 25565),
 				new IntegerOption(MAX_PLAYERS, 20),
 				new StringOption("server.motd", "A MineServ Server"),
 				new BooleanOption("server.creative", false, "Sets whether the server is in creative mode."),
 				new BooleanOption("server.online", true, "Sets whether the server connects to the central Minecraft.net servers for authentication."),
 				new BooleanOption("server.whitelist", false),
 				new IntegerOption("server.difficulty", 1, "0: Peaceful, 1: Easy, 2: Normal, 3: Hard"),
 				new StringOption("world.default", "world", "Default world to load on startup.")
 			};
 		
 		for (Option o : defaultOptions)
 			addOption(o.getKey(), o);
 	}
 	
 	public static final String NAME = "server.name", 
 			PORT = "server.port",
 			MAX_PLAYERS = "server.maxplayers",
 			MOTD = "server.motd";
 	
 }
