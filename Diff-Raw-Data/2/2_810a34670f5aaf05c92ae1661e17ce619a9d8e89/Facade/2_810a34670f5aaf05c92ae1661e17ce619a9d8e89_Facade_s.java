 package what;
 
 // Java imports
 import java.util.ArrayList;
 
 // JSON imports
 import org.json.JSONObject;
 
 // intern imports
 import what.sp_config.ConfigWrap;
 import what.sp_config.DimRow;
 import what.sp_parser.ParserMediator;
 import what.sp_chart_creation.ChartMediator;
 import what.sp_data_access.DataMediator;
 
 /**
  * This class Facade represents a facade.<br>
  * 
  * More precisely, it is the facade for the application tier, 
  * with the task to receive all requests or calls from the 
  * web server tier.
  * 
  * @author Jonathan, PSE Gruppe 14
  * @version 1.0
  *
  * @see ChartMediator
  * @see ParserMediator
  * @see ConfigWrap
  */
 public class Facade {
 	
 	// -- ATTRIBUTES -- ATTRIBUTES -- ATTRIBUTES -- ATTRIBUTES --
 	/**	Current Configuration on which all work is based with this Facade. */
 	private ConfigWrap currentConfig;
 	
 	/** ParserMediator to which work is directed from this Facade. */
 	private ParserMediator parsMedi;
 	
 	/** ChartMediator to which work is directed from this Facade. */
 	private ChartMediator chartMedi;
 	
 	/** DataMediator to which work is directed from this Facade. */
 	private DataMediator dataMedi;
 	
 	// -- SINGLETON PATTERN -- SINGLETON PATTERN -- SINGLETON PATTERN --
 	/** The singleton facade object */
 	private static Facade FACADE_OBJECT;
 	 
 	static {
 		// initialize the Facade singleton
 		FACADE_OBJECT = new Facade();
 		FACADE_OBJECT.init(getStandardPath());
 	}
 	
 	/**
 	 * Private constructor for this Facade class,
 	 * which should not be used, because this class
 	 * uses the singleton pattern.
 	 */
 	private Facade() {
 		// private constructor, should not be used, because this class works with the singleton pattern
 	}
 	
 	/**
 	 * Returns the singleton Facade object.
 	 * 
 	 * @return the singleton Facade object
 	 */
 	public static Facade getFacadeInstance() {
 		if (FACADE_OBJECT.isInitialized()) {
 			return FACADE_OBJECT;
 		} else {
 			FACADE_OBJECT.init(getStandardPath());
 			return FACADE_OBJECT;
 		}
 	}
 	
 	/**
 	 * Returns the standard path of the configuration for this what application.
 	 * 
 	 * @return the standard path of the configuration
 	 */
 	private static String getStandardPath() {
 		// get the default path
 	    String sourcePath = System.getProperty("user.dir");
 		String seperator = System.getProperty("file.separator");
		return sourcePath + seperator + "conf\\ConfigurationFile.json";
 	}
 
 	// -- INIT -- RESET -- INIT -- RESET -- INIT --
 	/**
 	 * Initializes this facade with a ConfigWrap, what is necessary
 	 * to post requests to it.
 	 * 
 	 * @param path of the configuration file (.json)
 	 * @return whether initialization was successful
 	 */
 	public boolean init(String path) {
 		if (path == null) {						 
 			throw new IllegalArgumentException();
 		}
 		
 		// tries to build a new ConfigWrap
 		ConfigWrap config = ConfigWrap.buildConfig(path);
 		if (config == null) {
 			Printer.pfail("Building ConfigWrap with the given path failed");
 			return false;
 		}
 		
 		// set all the attributes
 		currentConfig = config;
 		dataMedi = new DataMediator(config);
 		chartMedi = new ChartMediator(config, dataMedi);	
 		parsMedi = new ParserMediator(config, dataMedi);
 
 		// testing
 		Printer.ptest(config.toString());
 		
 		// create tables for the configuration if necessary
 		if (!(tablesAreCreated())) {
 			if (!(createDBTables())) {
 				Printer.pfail("Creating tables for this configuration");
 				return false;
 			} else {
 				Printer.psuccess("Creating tables for this configuration");
 			}
 			
 		}
 		
 		// pre-compute strings for the web page selection boxes
 		computeDimensionData();
 		
 		return true;
 	}
 	
 	/**
 	 * Returns whether the tables in the warehouse are created yet.
 	 * 
 	 * @return whether the tables in the warehouse are created yet
 	 */
 	private boolean tablesAreCreated() {
 		return dataMedi.areTablesCreated();
 	}
 
 	
 	/**
 	 * Checks whether everything is initialized.
 	 * 
 	 * @return whether everything is initialized
 	 */
 	private boolean isInitialized() {
 		return ((parsMedi != null) && (chartMedi != null)
 				&& (dataMedi != null) && (currentConfig != null));
 	}
 	
 	/**
 	 * Resets the facade.<br>
 	 * This clears the configuration and all mediators. 
 	 */
 	public void reset() {
 		currentConfig = null;
 		parsMedi = null;
 		chartMedi = null;
 		dataMedi = null;
 	}
 	
 	/**
 	 * Creates the warehouse tables for the configuration.
 	 * 
 	 * @return whether it was successful
 	 */
 	public boolean createDBTables() {
 		return dataMedi.createDBTables();
 	}
 	
 	// -- REQUESTS -- REQUESTS -- REQUESTS -- -- REQUESTS --
 	/**
 	 * Directs a parsing request to a ParserMediator.<br>
 	 * 
 	 * Referring to the given configuration (id), it directs the request of parsing
 	 * a given log-file (path) to a ParserMediator.
 	 * 
 	 * @param path path of the log file, which has to be parsed
 	 * @return whether parsing this log file was successful, to a certain point;
 	 * 			therefore {@linkplain ParserMediator}
 	 * @see ParserMediator 
 	 */
 	public boolean parseLogFile(String path) {
 		if (path == null) {						 
 			throw new IllegalArgumentException();
 		}
 		
 		//Printer.ptest("creating tables: " + createDBTables());
 		
 		// testing
 		Printer.ptest("Start parsing task.");
 		long start = System.currentTimeMillis();
 		
 		// checks whether a request is allowed
 		if (!isInitialized()) {
 			Printer.pproblem("Configurations & mediators not initalized!");
 			Printer.print("-> Parsing not possible!");
 			return false;
 		}
 		
 		// directs the request
 		if (!parsMedi.parseLogFile(path)) {
 			Printer.pfail("Parsing.");
 			return false;		
 		}  else {
 			Printer.psuccess("Parsing.");
 		}
 		
 		// testing
 		Printer.ptest("Completed parsing task. Time needed: " + (System.currentTimeMillis() - start));
 		
 
 		// precompute strings for the web page selection boxes after parsinng
 		computeDimensionData();
 			
 		return true;
 	}
 
 	/**
 	 * Directs a chart request to a ChartMediator.<br>
 	 * 
 	 * Referring to a given configuration (id), it directs a request
 	 * for a chart to a ChartMediator and returns the result.
 	 * 
 	 * @param json String path to a .json file where request information are stored
 	 * @return a json-object which contains all information about the requested chart
 	 * @see ChartMediator
 	 */
 	public JSONObject computeChart(JSONObject json) { 
 		if (json == null) { 
 			throw new IllegalArgumentException();
 		}
 		
 		// checks whether a request is allowed
 		if (!isInitialized()) {
 			Printer.pproblem("Configurations & mediators not initalized!");
 			Printer.print("-> Chart creation not possible!");
 			return null;
 		}
 		
 		// direct request and hopefully receive a JSON object
 		JSONObject chart = chartMedi.computeChart(json);	
 		
 		if (chart == null) {
 			Printer.pfail("Computing chart.");
 			return null;
 		} else {
 			Printer.psuccess("Computing chart.");
 		}
 		
 		return chart;
 	}
 	
 	/**
 	 * Request a old chart request from the history of a ChartMediator.<br>
 	 * 
 	 * Referring to a given configuration (id), it requests a chart from the history
 	 * of a ChartMediator. Thereby it requests the one, indicated by the given number.
 	 * E.g. 1 stands for the newest one, 6 for the 6 latest. 
 	 * 
 	 * @param number number of the latest computed chart, range from 1 (latest) to 10 (oldest)
 	 * @return the JSON object of the requested chart, referring to the id and the number
 	 */
 	public JSONObject historyChart(int number) {
 		// checks whether a request is allowed
 				if (!isInitialized()) {
 					Printer.pproblem("Configurations & mediators not initalized!");
 					Printer.print("-> Chart creation not possible!");
 					return null;
 				}
 		
 		// checks whether the parameters are legal
 		if ((number <= 0) || (getMaxSizeOfHistory() < number)) { 
 			throw new IllegalArgumentException();
 		
 		}
 		
 		// checks if enough charts are stored yet
 		if (number > getCurrentSizeOfHistory()) {
 			Printer.pproblem("Not so many charts stored yet.");
 			return null;
 		}
 		
 		// request the chart
 		JSONObject histo = chartMedi.getHistoryChart(number);	
 		if (histo == null) {
 			Printer.perror("No history for " + number + " found!");
 			return null;
 		}
 		
 		return histo;
 	}
 	
 	// -- GETTER -- GETTER -- GETTER -- GETTER -- GETTER -- 
 	/**
 	 * Returns the current ConfigWrap.
 	 * 
 	 * @return the current ConfigWrap
 	 */
 	public ConfigWrap getCurrentConfig() {
 		return currentConfig;
 	}
 	
 	/**
 	 * Returns the dimensions and rows.
 	 * 
 	 * @return the dimensions and rows
 	 */
 	public ArrayList<DimRow> getDimensions() {
 		// checks whether a request against configuration is allowed
 		if (!isInitialized()) {
 			Printer.pproblem("Configurations not initalized!");
 			Printer.print("-> Request on it possible!");
 			return null;
 		}
 		
 		return currentConfig.getDims(); 	
 	}	
 
 	/**
 	 * Returns the maximal number of charts possible to store in the history.
 	 * 
 	 * @return the maximal number of charts possible to store in the history
 	 */
 	public int getMaxSizeOfHistory() {
 		return chartMedi.getMaxSizeOfHistory();
 	}
 	
 	/**
 	 * Returns the current number of charts stored in the history.
 	 * 
 	 * @return the current number of charts stored in the history
 	 */
 	public int getCurrentSizeOfHistory() {
 		return  chartMedi.getCurrentSizeOfHistory();
 	}
 	
 	// -- PRIVATE HELPER -- PRIVATE HELPER -- PRIVATE HELPER --
 	/**
 	 * Precomputes the Strings in the dimensions for the
 	 * web page selection boxes. 
 	 */
 	private void computeDimensionData() {
 		assert (isInitialized());
 		
 		if (!(dataMedi.organizeData())) {
 			Printer.pfail("Precomputing strings for dimensions.");
 		}
 		Printer.psuccess("Precompunting strings for dimensions.");
 	}
 
 	// -- TESTING -- TESTING -- TESTING -- -- TESTING --
 	/**
 	 * Just for testing
 	 * @return the parser mediator
 	 */
 	public ParserMediator getParserMediator() {
 		return parsMedi;
 	}	
 
 }
