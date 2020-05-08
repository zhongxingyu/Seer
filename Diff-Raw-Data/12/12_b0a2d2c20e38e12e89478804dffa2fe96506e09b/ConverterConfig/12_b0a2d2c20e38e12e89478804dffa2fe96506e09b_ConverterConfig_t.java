 /**
  * ConverterConfig class. Only contains static variables for configuration of this conversion session.
  */
package CLInterface;
import java.io.PrintStream;
 
 /**
  * ConverterConfig class.
  * 
  * @author Chris Tandiono
  *
  */
 public class ConverterConfig {
 	
 	/**
 	 * Whether to attempt to ignore errors and force conversion
 	 */
 	public static boolean FORCE = false;
 	
 	/**
 	 * Whether we should spit out a lot of messages
 	 */
 	public static boolean VERBOSE = false;

	
	/* temporarily here until changes from branch can be incorporated */
	
	public static PrintStream OUTPUT;
	
	public static PrintStream DEBUG;
 	
 }
