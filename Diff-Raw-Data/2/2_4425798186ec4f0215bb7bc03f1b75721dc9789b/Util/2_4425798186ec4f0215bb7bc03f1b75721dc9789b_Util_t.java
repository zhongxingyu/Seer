 package btwmods;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 public class Util {
 	public static String getStackTrace() {
		return getStackTrace(new Throwable(""));
 	}
 	
 	/**
 	 * @see <a href="http://stackoverflow.com/a/1069342">Original source</a>
 	 */
 	@SuppressWarnings("javadoc")
 	public static String getStackTrace(Throwable e) {
 		StringWriter sw = new StringWriter();
 		printStackTrace(new PrintWriter(sw));
 		return sw.toString();
 	}
 	
 	public static void printStackTrace(PrintWriter writer) {
 		printStackTrace(writer, new Throwable(""));
 	}
 	
 	public static void printStackTrace(PrintWriter writer, Throwable e) {
 		e.printStackTrace(writer);
 	}
 
 	public static int getWorldIndexFromDimension(int dimension) {
 		if (dimension == -1) return 1;
 		else if (dimension == 1) return 2;
 		return 0;
 	}
 }
