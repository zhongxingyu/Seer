 
 import java.io.BufferedReader;	//for configuration file functionality
 import java.io.File;			//for configuration file functionality
 import java.io.FileInputStream;	//for configuration file functionality
 import java.io.FileReader;		//for configuration file functionality
 import java.io.IOException;		//for configuration file functionality
 import java.io.InputStream;		//for configuration file functionality
 import java.util.Properties;	//for configuration file functionality
 import java.util.logging.*;		//for logger functionality
 import org.apache.commons.cli.*;	//for command line options
 
 public class Gio {
 
 	
 	private static Logger logger = Logger.getLogger("");		//create logger object
     	private FileHandler fh; ;					//creates filehandler for logging
 	private String genConfig;					//location of general configuration file
 	private String wConfig;						//location of weights configuration file, if specified.
 
 	/**
 	 * Constructor fo gio class. There should only be one. Consider this a singleton instance to call I/O messages on.
 	 * Constructs and parses command line arguements as well.
 	 * 
 	 *  @author ngerstle
 	 */
 	public Gio(String[] args) 
 	{
 		// create Options object
 		Options options = new Options();
 	
 		// add t option
 		options.addOption("c", true, "general configuration file location");
 		options.addOption("w", true, "weights configuration file location");
 
 
 		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
 
 
 		genConfig = cmd.getOptionValue("c");
		if genConfig == null
 		{
 			genConfig = "./PrivacyAdviser.cfg";
 		}
 		wConfig = cmd.getOptionValue("w"); //don't need to check for null as it is assumed to be in the general config file loaded later
 
 
 	}
 
 	/**
 	 * Loads the general config file from either commandline location or default of './PrivacyAdviser.cfg'
 	 * 
 	 * @author ngerstle
 	 * @return properties object corresponding to given configuration file
 	 */
 	public Properties loadGeneral()
 	{
 		return loadGeneral(genConfig);
 	}
 	
 	/**
 	 * Loads the general config, either from provided string, or default location (./PrivacyAdviser.cfg)
 	 * 
 	 * @author ngerstle
 	 * @param location of configuration file
 	 * @return properties object corresponding to given configuration file
 	 */
 	public Properties loadGeneral(String fileLoc)
 	{
 		Properties configFile = new Properties();
 
 		try {
 			File localConfig = new File(fileLoc);
 			InputStream is = null;
 			if(localConfig.exists())
 			{
 				is = new FileInputStream(localConfig);
 			}
 			else
 			{
 				System.out.println("No configuration file at "+fileLoc+ ". Please place one in the working directory.");
 				System.exit(3);
 			}
 			configFile.load(is);
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			System.out.println("IOException reading first configuration file. Exiting...\n");
 			System.exit(1);
 		}	
 		return configFile;
 	}
 	
 	/**
 	 * Loads the weights configuration file, from the provided location
 	 * 
 	 * @author ngerstle
 	 * @param location of configuration file
 	 * @return properties object corresponding to given configuration file
 	 */
 	
 	Properties loadWeights(String fileLoc)
 	{
 		if(wConfig != null)
 		{
 			fileLoc = wConfig;
 		}
 		Properties configFile = new Properties();
 
 		try 
 		{
 			File localConfig = new File(fileLoc);
 			InputStream is = null;
 			if(localConfig.exists())
 			{
 				is = new FileInputStream(localConfig);
 			}
 			else
 			{
 				System.out.println("No weights file is available at "+fileLoc+" . Please place one in the working directory.");
 				System.exit(3);
 			}
 			configFile.load(is);
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			System.out.println("IOException reading the weights configuration file. Exiting...\n");
 			System.exit(1);
 		}	
 		return configFile;
 	}
 	
 	
 	/**
 	 * startLogger initializes and returns a file at logLoc with the results of logging at level logLevel. 
 	 * @param logLoc	location of the output log file- a string
 	 * @param logLevel	logging level (is parsed by level.parse())
 	 * @return	Logger object to log to.
 	 */
 	
 	Logger startLogger(String logLoc, String logLevel)
 	{
 		try 
 		{
 			fh = new FileHandler(logLoc);		//sets output log file at logLoc
 		}
 		catch (SecurityException e) 
 		{
 			e.printStackTrace();
 			System.out.println("SecurityException establishing logger. Exiting...\n");
 			System.exit(1);
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			System.out.println("IOException establishing logger. Exiting...\n");
 			System.exit(1);
 		}			
 		fh.setFormatter(new SimpleFormatter()); 	//format of log is 'human-readable' simpleformat
 		logger.addHandler(fh);						//attach formatter to logger
 		logger.setLevel(Level.parse(logLevel));		//set log level
 		return logger;
 	}
 	
 	
 	/**
 	 * Loads the case history into cache.
 	 * 
 	 * @author ngerstle
 	 * 
 	 */
 	void loadDB()
 	{
 	}
 }
