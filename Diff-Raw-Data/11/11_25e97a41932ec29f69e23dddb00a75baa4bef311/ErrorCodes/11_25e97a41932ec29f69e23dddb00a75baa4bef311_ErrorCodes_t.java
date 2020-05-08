 /**
  * 
  */
 package rezend.musix.constants;
 
 /**
 * This class holds the warning and error messages of the application. It should
 * centralized the output messages of the system. However, for more complex
 * applications, use Apache log4j instead.
 * 
  * @author Rafael Rezende
  * 
  */
 public final class ErrorCodes {
 
 	// Warning messages
 	public final static String MULTIPLE_MATCH = "Warning! Multiple matches for the current regex. Please provide a more specific regex.";
 
	// Error messages
	public final static String DB_CONN_FAILED = "ERROR! Error connecting to MySQL database!";
	public final static String NO_CONN_AVAILABLE = "ERROR! There is no open connection to database!";
	public final static String ADD_PRODUCT_FAILURE = "ERROR! Failure adding to database!";
	public final static String SELECT_PRODUCT_FAILURE = "ERROR! Failure retrieving data from database!";

 }
