 package constants;
 
 /**
  * Defines global constants.
  *
  * @author cmihail, radu-tutueanu
  */
 public class Constants {
 
 	public static final String WEBSERVICE_ADDRESS =
 			"http://localhost:8080/axis/services/WebService";
 	public static final String SERVER_IP = "127.0.0.1";
 	public static final int SERVER_PORT = 30000;
 
 	public static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DATABASE_IP = "localhost";
 	public static final String DATABASE_DB = "idp";
 	public static final String DATABASE_USER = "student";
 	public static final String DATABASE_PASS = "student";
 	public static final String DATABASE_URL =
 			"jdbc:mysql://"+ DATABASE_IP + ":3306/" + DATABASE_DB;
 
 	public static final String CONFIG_FILE_EXTENSION = ".cfg";
 
 	public static final String LOGGER_FILE_EXTENSION = ".log";
 	public static final String LOGGER_FOLDER = "log/";
 	public static final String SERVER_LOGGER_FILE = LOGGER_FOLDER + "server" +
 			LOGGER_FILE_EXTENSION;
 	public static final String LOGGER_PATTERN = "%-4r %-4L [%t] %-5p %c %x - %m%n";
 
 	public static final String SERVICE_FOLDER = "services/";
 	public static final int SERVICE_NETWORK_SEGMENT_SIZE = 1024;
 	public static final int SERVICE_BLOCK_SIZE = 1000;
 	public static final int SERVICE_MAXIMUM_SIZE = 1000 * SERVICE_BLOCK_SIZE;
 
 	public static boolean TEST_CANCEL = false; // TODO to delete
 }
