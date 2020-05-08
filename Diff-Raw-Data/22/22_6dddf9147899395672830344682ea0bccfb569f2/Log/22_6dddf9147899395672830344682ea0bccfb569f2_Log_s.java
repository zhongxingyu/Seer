 package ms.server.log;
 
 import java.io.IOException;
 
 import ms.server.utils.Constants;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.DailyRollingFileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.WriterAppender;
 
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

 /**
  * Apache log4j Logger
  * three different logtypes:
  * - show in console
  * - write to a file (daily logging)
  * - put log information to a outputstream
  * 
  * @author david
  *
  */
 public class Log {
 	
 	private static Logger logger = Logger.getLogger(Log.class);
	private static ByteOutputStream bos = new ByteOutputStream();
 	static {
 		new Log();
 	}
 	private Log() {
 		logger.setLevel(Level.ALL);
 		String pattern = "%d{ISO8601}: %m %n";
 		PatternLayout layout = new PatternLayout(pattern);
 		consoleLogger(layout);
 		fileLogger(layout);
 		writeLogger(layout);
 	}
 
 	private void writeLogger(PatternLayout layout) {
 		WriterAppender writeAppender = new WriterAppender(layout, bos);
 		logger.addAppender(writeAppender);
 	}
 
 	private void consoleLogger(PatternLayout layout) {
 		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
 		consoleAppender.setFollow(true);
 		logger.addAppender(consoleAppender);
 	}
 
 	private void fileLogger(PatternLayout layout) {
 		DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
 		try {
 			fileAppender = new DailyRollingFileAppender(layout, Constants.LOGFILE, "'_'yyyy-MM-dd");
 			fileAppender.setAppend(true);
 		} catch (IOException e) {
 			e.printStackTrace();
 			logger.info("Can't write Logfile");
 		}
 		logger.addAppender(fileAppender);
 	}
 
 	/**
 	 * get logger
 	 * 
 	 * @return Logger
 	 */
 	public static Logger getLogger() {
 		return logger;
 	}
 	
 	/**
 	 * get OutputStream with logged information
 	 * 
 	 * @return ByteOutputStream
 	 */
	public static ByteOutputStream getOutputStream() {
 		return bos;
 	}
 }
