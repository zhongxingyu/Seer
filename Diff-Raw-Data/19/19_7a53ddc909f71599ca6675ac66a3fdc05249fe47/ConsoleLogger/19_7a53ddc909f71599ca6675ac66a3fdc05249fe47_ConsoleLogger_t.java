 package executor.lightLogger.logger;
 
import java.io.BufferedWriter;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Properties;
 
 import executor.lightLogger.level.Default;
 import executor.lightLogger.level.ILevel;
 
 public class ConsoleLogger extends AbstractLogger {
 
	// NOTE: Do not close writer which contain System.out/System.err, otherwise all syso's won't work!
	private Writer out = new BufferedWriter(new OutputStreamWriter(System.out));
	private Writer err = new BufferedWriter(new OutputStreamWriter(System.err));
 
 	public ConsoleLogger() {
 		super();
 	}
 
 	public ConsoleLogger(String name) {
 		super(name);
 	}
	
 	@Override
 	public boolean loadProperties(Properties properties) {
 		return false;
 	}
 
 	@Override
 	public void log(ILevel level, String message) {
 		if (Default.ERROR.getValue() == level.getValue() || Default.FATAL.getValue() == level.getValue())
 			super.log(err, level, message);
 		else
 			super.log(out, level, message);
 	}
 
 	@Override
 	public void fatal(String message) {
 		log(Default.FATAL, message);
 	}
 
 	@Override
 	public void error(String message) {
 		log(Default.ERROR, message);
 	}
 
 	@Override
 	public void warn(String message) {
 		log(Default.WARN, message);
 	}
 
 	@Override
 	public void info(String message) {
 		log(Default.INFO, message);
 	}
 
 	@Override
 	public void debug(String message) {
 		log(Default.DEBUG, message);
 	}
 
 	@Override
 	public void trace(String message) {
 		log(Default.TRACE, message);
 	}
 
 }
