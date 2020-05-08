 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ws.ip4u.mediadaemon;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import ws.ip4u.mediadaemon.Config.ConfigOptions;
 import ws.ip4u.mediadaemon.Config.ConfigurationValidator.ValidationException;
 
 /**
  *
  * @author jalsk
  */
 public class Config
 {
 	private Log log = LogFactory.getLog(Config.class);
 	private static final String DEFAULT_API_KEY = "8495C6D0B9081C3C";
 	private static final String DEFAULT_SEARCH_PATH = "/Users/jalsk/Movies/mediaTest/";
 	private static final String DEFAULT_TORRENT_PATH = "/Users/jalsk/torrents/torrents/";
 
 	//<editor-fold defaultstate="collapsed" desc="ConfigOptions enum">
 	protected enum ConfigOptions
 	{
 		CONFIG("config", "c", true, "path to the config file", false, "config"),
 		SEARCH_PATH("searchPath", "sp", true, "base path for final media files", true, "search path"),
 		HELP("help", "h", false, "display this message", false, "help"),
 		TORRENT_PATH("torrentPath", "tp", true, "path to finished torrent directory", true, "torrent path"),
 		API_KEY("apiKey", "a", true, "API key for thetvdb.com", true, "api key"),
 		SHOW_FORMAT("showFormat", "sf", false, "show the format for the config file", false, "show format"),
 		PRETEND("pretend", "p", false, "don't actually do anything, just tell what would happen", false, "pretend"),
 		TEST_CONFIG("testConfig", "td", false, "test the config for the specified config file", false, "test config"),
 		SCAN_FREQUENCY("scanFrequency", "sf", true, "how frequently we should scan the directories for changes", true, "scan frequency"),
 		DAEMON("daemon", "d", false, "whether we are running in daemon mode", true, "daemon");
 		private String name;
 		private String shortName;
 		private boolean requiredParam;
 		private String description;
 		private boolean inConfig;
 		private String friendlyName;
 
 		ConfigOptions(String name, String shortName, boolean requiredParam, String description, boolean inConfig, String friendlyName)
 		{
 			this.name = name;
 			this.shortName = shortName;
 			this.requiredParam = requiredParam;
 			this.description = description;
 			this.inConfig = inConfig;
 			this.friendlyName = friendlyName;
 		}
 
 		public String getName()
 		{
 			return this.name;
 		}
 
 		public String getShortName()
 		{
 			return this.shortName;
 		}
 
 		public boolean getRequiredParam()
 		{
 			return this.requiredParam;
 		}
 
 		public String getDescription()
 		{
 			return this.description;
 		}
 
 		public boolean getInConfig()
 		{
 			return this.inConfig;
 		}
 
 		public String getFriendlyName()
 		{
 			return this.friendlyName;
 		}
 	};
 	//</editor-fold>
 	String searchPath = null, torrentPath = null, apiKey = null;
 	boolean pretend = false, daemon = false;
 	int scanFrequency;
 
 	public Config(String[] args) throws ConfigException, ConfigTestingException
 	{
 		BufferedReader br = null;
 		try
 		{
 			Options options = new Options();
 			for(ConfigOptions co : ConfigOptions.values())
 			{
 				options.addOption(co.shortName, co.name, co.requiredParam, co.description);
 			}
 			CommandLineParser parser = new GnuParser();
 			CommandLine cmd = parser.parse(options, args);
 
 			if(args.length == 0 || cmd.hasOption(ConfigOptions.HELP.shortName))
 			{
 				HelpFormatter formatter = new HelpFormatter();
 				formatter.printHelp("java -jar MediaDaemon", options);
				throw new ConfigException("Showing help");
 			}
 
 			if(cmd.hasOption(ConfigOptions.TEST_CONFIG.shortName))
 			{
 				String fileToTest = cmd.getOptionValue(ConfigOptions.TEST_CONFIG.shortName);
 
 				int lineNo = 1;
 				try
 				{
 					ConfigurationValidator cv = new ConfigurationValidator();
 					br = new BufferedReader(new FileReader(fileToTest));
 					String line;
 					while((line = br.readLine()) != null)
 					{
 						cv.validate(line);
 						lineNo++;
 					}
 				}
 				catch(ValidationException e)
 				{
 					log.fatal("Error on line " + lineNo + ":\n" + e.getMessage());
 				}
 				throw new ConfigTestingException("Finished testing the config, exiting");
 			}
 
 			if(cmd.hasOption(ConfigOptions.CONFIG.shortName))
 			{
 				// Config file format:
 				// key="value"
 				// read in the config file
 				String configFile = cmd.getOptionValue(ConfigOptions.TEST_CONFIG.shortName);
 				br = new BufferedReader(new FileReader(configFile));
 				String line;
 				while((line = br.readLine()) != null)
 				{
 					String[] valPair = parseString(line);
 					if(valPair[0].equals(ConfigOptions.SEARCH_PATH.name))
 					{
 						searchPath = valPair[1];
 					}
 					else if(valPair[0].equals(ConfigOptions.SEARCH_PATH.name))
 					{
 						searchPath = valPair[1];
 					}
 					else if(valPair[0].equals(ConfigOptions.API_KEY.name))
 					{
 						apiKey = valPair[1];
 					}
 				}
 			}
 			if(cmd.hasOption(ConfigOptions.API_KEY.shortName))
 			{
 				apiKey = cmd.getOptionValue(ConfigOptions.API_KEY.shortName);
 			}
 			else
 			{
 				apiKey = DEFAULT_API_KEY;
 			}
 
 			if(cmd.hasOption(ConfigOptions.SEARCH_PATH.shortName))
 			{
 				searchPath = cmd.getOptionValue(ConfigOptions.SEARCH_PATH.shortName);
 			}
 			else
 			{
 				searchPath = DEFAULT_SEARCH_PATH;
 			}
 
 			if(cmd.hasOption(ConfigOptions.TORRENT_PATH.shortName))
 			{
 				torrentPath = cmd.getOptionValue(ConfigOptions.TORRENT_PATH.shortName);
 			}
 			else
 			{
 				torrentPath = DEFAULT_TORRENT_PATH;
 			}
 
 			if(cmd.hasOption(ConfigOptions.PRETEND.shortName))
 			{
 				pretend = true;
 			}
 
 			if(cmd.hasOption(ConfigOptions.SCAN_FREQUENCY.shortName))
 			{
 				scanFrequency = Integer.parseInt(cmd.getOptionValue(ConfigOptions.SCAN_FREQUENCY.shortName));
 			}
 
 			if(cmd.hasOption(ConfigOptions.DAEMON.shortName))
 			{
 				daemon = true;
 			}
 		}
 		catch(IOException e)
 		{
 			throw new ConfigException("Error reading from the input file", e);
 		}
 		catch(ParseException e)
 		{
 			throw new ConfigException("Error parsing the command line arguments", e);
 		}
 		finally
 		{
 			IOUtils.closeQuietly(br);
 		}
 	}
 
 	public boolean isPretend()
 	{
 		return pretend;
 	}
 
 	public boolean isDaemon()
 	{
 		return daemon;
 	}
 
 	public String getTorrentPath()
 	{
 		return torrentPath;
 	}
 
 	public String getShowPath()
 	{
 		return searchPath;
 	}
 
 	public String getApiKey()
 	{
 		return apiKey;
 	}
 
 	public int getScanFrequency()
 	{
 		return scanFrequency;
 	}
 
 	private static String[] parseString(String input)
 	{
 		String[] ret = new String[2];
 
 		ret[0] = input.substring(0, input.indexOf("="));
 
 		ret[1] = input.substring(input.indexOf("=") + 1).substring(1); // cut off the leading quote
 		ret[1] = ret[1].substring(0, ret[0].length() - 2); // cut off the trailing quote
 
 		return ret;
 	}
 
 	public class ConfigException extends Exception
 	{
 		public ConfigException(String message)
 		{
 			super(message);
 		}
 
 		public ConfigException(String message, Exception e)
 		{
 			super(message, e);
 		}
 	}
 
 	public class ConfigTestingException extends Exception
 	{
 		public ConfigTestingException(String message)
 		{
 			super(message);
 		}
 
 		public ConfigTestingException(String message, Exception e)
 		{
 			super(message, e);
 		}
 	}
 
 	//<editor-fold defaultstate="collapsed" desc="Configuration Validator">
 	class ConfigurationValidator
 	{
 		public void validate(String input) throws ValidationException
 		{
 			// if the line is empty, disregard it
 			if(input.isEmpty())
 				return;
 
 			if(input.indexOf("=") < 0)
 				throw new ValidationException("Configuration file must contain key-value pairs separated by '='");
 
 			String key = input.substring(0, input.indexOf("="));
 			String value = input.substring(input.indexOf("=") + 1);
 
 			if(key.isEmpty())
 				throw new ValidationException("Cannot have a blank key value");
 
 			boolean foundValue = false;
 			ConfigOptions option = null;
 			for(ConfigOptions co : ConfigOptions.values())
 			{
 				if(key.equals(co.getName()) && co.getInConfig())
 				{
 					option = co;
 					foundValue = true;
 				}
 			}
 
 			if(!foundValue)
 				throw new ValidationException("Key value: " + key + " is not known.\nValid values are:\n" + getValidKeyValues());
 
 			if(!value.startsWith("\"") || !value.endsWith("\""))
 				throw new ValidationException("Value associated with key " + key + " does not begin and end with quotation marks");
 
 			value = value.substring(0, value.length() - 2);
 
 			if(option == ConfigOptions.SEARCH_PATH || option == ConfigOptions.TORRENT_PATH)
 			{
 				File f = new File(value);
 				if(!f.exists())
 					throw new ValidationException("Specified " + option.getFriendlyName() + " is not a valid path name.\n"
 												  + "Tested path: " + value);
 				if(!f.canRead())
 					throw new ValidationException("Specified " + option.getFriendlyName() + " cannot be read.\n"
 												  + "Tested path: " + value);
 
 				if(!f.isDirectory())
 					throw new ValidationException("Specified " + option.getFriendlyName() + " is not a directory.\n"
 												  + "Tested path: " + value);
 			}
 		}
 
 		private String getValidKeyValues()
 		{
 			StringBuilder sb = new StringBuilder();
 
 			for(ConfigOptions co : ConfigOptions.values())
 			{
 				if(co.getInConfig())
 					sb.append(co.getName()).append(", ");
 			}
 
 			sb.deleteCharAt(sb.length() - 1); // get rid of the last two characters (the comma and space)
 			sb.deleteCharAt(sb.length() - 1);
 
 			return sb.toString();
 		}
 
 		public class ValidationException extends Exception
 		{
 			public ValidationException(String message)
 			{
 				super(message);
 			}
 		}
 	}
 //</editor-fold>
 }
