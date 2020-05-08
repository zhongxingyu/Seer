 package edu.uw.cs.cse461.sp12.OS;
 
 import java.io.FileInputStream;
 import java.util.HashMap;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 import edu.uw.cs.cse461.sp12.util.Log;
 
 /**
  * A static class (only one instance, existing statically) implementing OS-like
  * functionality.  It is initialized by calling boot() and then startServices().
  * It's shut down by calling shutdown().  Its major responsibility is to bring up
  * services, like rpc, and make them available to apps (via getService()).
  * 
  * @author zahorjan
  *
  */
 public class OS {
 	private static final String TAG = "OS";
 	
 	private static boolean mAmShutdown = true;
 	
 	//---------------------------------------------------------------------------------------------------
 	// CSE461
 	// This array controls service creation.  An instance of each class is created, in the order given.
 	// If you create a new service, add the **name of its class** to this list (almost certainly at the end).
 	// (Don't confuse this with the value returned by its appname() method.)
 	public static final String[] rpcServiceClasses = { "edu.uw.cs.cse461.sp12.OS.RPCService",
 													   "edu.uw.cs.cse461.sp12.OS.EchoService"
 	                                                 };
 	public static final String[] ddnsServiceClasses = { "edu.uw.cs.cse461.sp12.OS.RPCService",
 														"edu.uw.cs.cse461.sp12.OS.DDNSService",
 														"edu.uw.cs.cse461.sp12.OS.DDNSResolverService"
 			  									      };
 	public static final String[] allServiceClasses = {"edu.uw.cs.cse461.sp12.OS.RPCService",
 		   											  "edu.uw.cs.cse461.sp12.OS.EchoService",
 		   											  "edu.uw.cs.cse461.sp12.OS.DDNSService",
 													  "edu.uw.cs.cse461.sp12.OS.DDNSResolverService",
 		   											  "edu.uw.cs.cse461.sp12.OS.HTTPDService"};
 	public static final String[] androidServiceClasses = {"edu.uw.cs.cse461.sp12.OS.RPCService",
 														  "edu.uw.cs.cse461.sp12.OS.EchoService",
 														  "edu.uw.cs.cse461.sp12.OS.DDNSResolverService"};
 	//---------------------------------------------------------------------------------------------------
 
 	// used to keep track of started services.  The String key is the name returned by the
 	// service's servicename() method.
 	private static HashMap<String, RPCCallable> serviceMap = new HashMap<String, RPCCallable>(); 
 
 	private static Properties mConfig;
 	private static String mHostname;
 	
 	
 	/**
 	 * Brings up the OS.  Doesn't start any services.  Call startServices() for that.
 	 * 
 	 * @param config  Configuration settings read by caller from a config file.
 	 * @throws Exception
 	 */
 	public static synchronized void boot(Properties config) throws Exception {
 		// sanity check
 		if ( !mAmShutdown ) throw new RuntimeException("Call to OS.boot() but an OS is already running!");
 		
 		if ( config == null ) throw new RuntimeException("OS.boot() called with arg config=null");
 		mConfig = config;
 
 		// code uses the host name a lot, so cache it in it's typical form (no trailing '.')
 		mHostname = mConfig.getProperty("host.name");
 		if ( mHostname == null ) throw new RuntimeException("OS: no hostname in config file");
 		if ( mHostname.equals(".") ) mHostname = "";
 		else if ( mHostname.endsWith(".") ) mHostname = mHostname.substring(0,mHostname.length()-1);
 		
 		mAmShutdown = false; // at this point, we're up, but with no services running
 	}
 
 
 	/**
 	 * Starts "OS resident" services.  The argument is an array of class names.
 	 * Two useful lists are included as static OS class variables: rpcServiceClasses
 	 * and ddnsServiceClasses.  (The latter isn't useful until Project 4.)
 	 * @param serviceClassList  Entries are fully qualified class names.
 	 */
 	public static synchronized void startServices(String[] serviceClassList) {
 		String startingService = null;  // for debugging output in catch block
 		try {
 			for ( String serviceClassname : serviceClassList ) {
 				Log.v(TAG, "Starting " + serviceClassname);
 				startingService = serviceClassname;
 				// Get the Java Class object
 				@SuppressWarnings("unchecked")
 				Class<RPCCallable> serviceClass = (Class<RPCCallable>)Class.forName(serviceClassname);
 				// Create an instance of the class
 				RPCCallable service = serviceClass.newInstance();
 				// Record the instance in a Map, keyed by the service's self-proclaimed name
 				serviceMap.put(service.servicename(), service);
 				Log.i(TAG, serviceClassname + " started");
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Error while starting service " + startingService + ": " + e.getMessage());
 			shutdown();
 		}
 	}
 
 	/**
 	 * Shutdown associated services and the OS.  The main point is to terminate threads, so that app
 	 * can terminate.  If you don't shut down various services, the odds are the app won't terminate
 	 * even if the main thread exits.
 	 */
 	public static synchronized void shutdown() {
		DDNSFullName thisName = new DDNSFullName(OS.config().getProperty("host.name"));
 		if ( mAmShutdown ) return;
 		try {
 			for ( String serviceName : serviceMap.keySet() ) {
 				RPCCallable service = serviceMap.get(serviceName);
 				service.shutdown();
 			}
 			// We can't remove items from the HashMap while iterating
 			serviceMap.clear();
 		} catch (Exception e) {
 			Log.e(TAG, "Error shutting down services: " + e.getMessage());
 			throw new RuntimeException(e.getMessage());
 		}
 		
 		mAmShutdown = true;
 		
 	}
 	
 	/**
 	 * Helper function that simply makes sure the OS is running when calls to it are made.
 	 * @param method
 	 */
 	private static void check(String method) {
 		if ( mAmShutdown ) throw new RuntimeException("OS." + method + " called when OS isn't running");
 	}
 	
 	/**
 	 * Get access to the configuration properties read from the config file specified at launch.
 	 * @return
 	 */
 	public static Properties config() {
 		check("getConfig");
 		return mConfig;
 	}
 	
 	/**
 	 * Returns this host's name, if it has one, otherwise null. (This isn't
 	 * useful until Project 4.)
 	 * @return
 	 */
 	public static String hostname() {
 		check("hostname");
 		return mHostname;
 	}
 	
 	/**
 	 * Takes the value returned by then servicename() method of the thing
 	 * you're looking for, and returns that thing.  (E.g., call with arg
 	 * "rpc" to get the RPC service.)
 	 * @param servicename
 	 * @return
 	 */
 	public static RPCCallable getService(String servicename) {
 		check("getService(" + servicename + ")");
 		return serviceMap.get(servicename);
 	}
 	
 	/**
 	 * A simple test driver that fires up the OS.  You probably don't want to run this.
 	 * Use the AppManager instead.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		final String TAG="OS.main";
 		String configFilename = "config.ini";  // default: may not exist
 		try {
 			// This code deals with command line options
 			Options options = new Options();
 			options.addOption("f", "configfile", true, "Config file name (Default: " + configFilename + ")");
 			options.addOption("h", "help", false, "Print this message");
 
 			CommandLineParser parser = new PosixParser();
 
 			CommandLine line = parser.parse(options, args);
 			if ( line.hasOption("help") ) {
 				HelpFormatter formatter = new HelpFormatter();
 				formatter.printHelp(OS.class.getName(), options );
 				return;
 			}
 			if ( line.hasOption("configfile") ) configFilename = line.getOptionValue("configfile");
 
 			// read config file data
 			Properties config = new Properties();
 			config.load(new FileInputStream(configFilename));
 			
 			String showDebug = config.getProperty("debug.enable");
 			Log.setShowLog( showDebug == null || (!showDebug.isEmpty() && !showDebug.equals("0")) );
 			String debugLevel = config.getProperty("debug.level");
 			if ( debugLevel != null ) {
 				try {
 					 Log.setLevel(Integer.parseInt(debugLevel));
 				} catch (Exception e) {
 					Log.e(TAG, "debug.level entry in " + configFilename + " has invalid value.  (Should be 0 or 1.)");
 				}
 			}
 
 			OS.boot(config);
 			OS.startServices(rpcServiceClasses);
 			// not used until Project 4
 			OS.startServices(ddnsServiceClasses);
 
 		} catch (Exception e) {
 			Log.e(TAG, "Caught exception: " + e.getMessage());
 		}
 	}
 	
 }
