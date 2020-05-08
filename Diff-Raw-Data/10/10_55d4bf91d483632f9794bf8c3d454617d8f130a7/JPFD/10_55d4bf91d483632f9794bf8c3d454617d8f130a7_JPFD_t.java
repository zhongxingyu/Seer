 package jpfd;
 
 import org.apache.commons.cli.*;
 import org.apache.log4j.*;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import java.io.File;
 import java.util.*;
 import java.util.regex.*;
 
 public class JPFD implements Runnable
 {
 	private static Logger log = Logger.getLogger(JPFD.class);
 	private static final ObjectMapper mapper = new ObjectMapper();
 
 	private JPFDConfig config = null;
 	private String log4jConfig = null;
 	private boolean debug = false;
 
 	private List<TCPForwardServer> tcpForwarders = new ArrayList<TCPForwardServer>();
	private List<Thread> threads = new ArrayList<Thread>();
 
 	public static void main(String args[])
 	{
 		BasicConfigurator.configure();
 
 		try
 		{
 			log.debug("Starting Port Forwarding Daemon");
 			JPFD jpfd = new JPFD(args);
 			Thread jpfdThread = new Thread(jpfd, "JPFD Main");
 			jpfdThread.start();
 			log.debug("waiting for jpfd thread to join...");
 			jpfdThread.join();
 		}
 		catch (Exception e)
 		{
 			log.error(e);
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		System.exit(0);
 	}
 
 	/**
 	 * Constructor accepts configuration options as name=value pairs
 	 */
 	public JPFD(String[] args) throws Exception
 	{
 		BasicConfigurator.configure();
 
 		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
 
 		if (!parseOptions(args))
 		{
 			return;
 		}
 
 		//
 		// config is now populated, or should be
 		//
 		if (config == null)
 		{
 			throw new Exception("missing config argument");
 		}
 
 		//
 		// config is set here
 		//
 
 		String listenRegex = "^(.*?)://(.*?):(\\d+)$";
 		String destRegex   = "^(.*?):(\\d+)$";
 
 		Pattern listenPattern = Pattern.compile(listenRegex);
 		Pattern destPattern   = Pattern.compile(destRegex);
 
 		String listenHost;
 		int listenPort;
 		String destHost;
 		int destPort;
 
 		for (Map.Entry<String, String> e : config.forwards.entrySet())
 		{
 			log.debug("setting up a forwarder for " + e.getKey() + " to " + e.getValue());
 
 			//
 			// key is proto://host:port
 			//
 			// if proto is not tcp/i, throw exception
 			//
 
 			Matcher matcher = listenPattern.matcher(e.getKey());
 
 			if (!matcher.matches())
 			{
 				throw new IllegalArgumentException("listen pattern did not match: " + e.getKey());
 			}
 
 			String proto = matcher.group(1).toLowerCase();
 
 			if (!proto.equals("tcp"))
 			{
 				throw new IllegalArgumentException("we only support tcp, not " + proto);
 			}
 
 			listenHost = matcher.group(2);
 			listenPort = Integer.parseInt(matcher.group(3));
 			
 			matcher = destPattern.matcher(e.getValue());
 		
 			if (!matcher.matches())
 			{
 				throw new IllegalArgumentException("destination pattern did not match: " + e.getKey());
 			}
 
 			destHost = matcher.group(1);
 			destPort = Integer.parseInt(matcher.group(2));
 
 			TCPForwardServer s = new TCPForwardServer(listenHost, listenPort, destHost, destPort);
 			tcpForwarders.add(s);
 
 			Thread t = new Thread(s, "TCPForward: " + listenHost + ":" + listenPort + " => " + destHost + ":" + destPort);
 			t.start();
			threads.add(t);
 		}
 
 		log.debug("done starting threads");
 
 	}
 
 	public Options getOptions()
 	{
 		Options options = new Options();
 		options.addOption("D", false, "Enable debug level logging");
 		options.addOption("h", false, "Help");
 		options.addOption("c", true, "path to config file");
 		options.addOption("l", true, "path to log4j config file");
 		return options;
 	}
 
 	private boolean parseOptions(String[] args) throws Exception
 	{
 		if (args == null)
 		{
 			return true;
 		}
 
 		CommandLineParser parser = new GnuParser();
 		CommandLine cl = parser.parse(getOptions(), args);
 		Option[] options = cl.getOptions();
 
 		boolean result = true;
 
 		for (Option option : options)
 		{
 			if (option.getOpt().equals("D"))
 			{
 				debug = true;
 			}
 			else if (option.getOpt().equals("h"))
 			{
 				HelpFormatter formatter = new HelpFormatter();
 				formatter.printHelp("java -jar jpfd.jar", getOptions());
 				return false;
 			}
 			else if (option.getOpt().equals("c"))
 			{
 				config = mapper.readValue(new File(option.getValue()), JPFDConfig.class);
 			}
 			else if (option.getOpt().equals("l"))
 			{
 				log4jConfig = option.getValue();
 			}
 		}
 
 		setupLogging();
 
 		return true;
 	}
 
 	private void setupLogging() throws Exception
 	{
 		LogManager.resetConfiguration();
 
 		if (null == log4jConfig)
 		{
 			BasicConfigurator.configure();
 
 			if (debug)
 			{
 				Logger.getRootLogger().setLevel(Level.toLevel("DEBUG"));
 			}
 			return;
 		}
 
 		File log4jFile = new File(log4jConfig);
 
 		if (!log4jFile.exists())
 		{
 			throw new IllegalArgumentException("log4j config file does not exist: " + log4jConfig);
 		}
 
 		//
 		// load log4j config file
 		//
 		PropertyConfigurator.configure(log4jConfig);
 
 		if (debug)
 		{
 			Logger.getRootLogger().setLevel(Level.toLevel("DEBUG"));
 		}
 
 		log = Logger.getLogger(JPFD.class);
 	}
 
 	public void run()
 	{
 		while (true)
 		{
 			try
 			{
 				Thread.sleep(1000000000);
 			}
 			catch (InterruptedException e)
 			{
 			}
 		}
 	}
 }
