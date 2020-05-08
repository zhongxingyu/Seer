 package executor.lightLogger.logger;
 
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Properties;
 
 import executor.lightLogger.level.Default;
 import executor.lightLogger.level.ILevel;
 
 public class ConsoleLogger extends AbstractLogger {
 
	private Writer out = new OutputStreamWriter(System.out);
	private Writer err = new OutputStreamWriter(System.err);
 
 	public ConsoleLogger() {
 		super();
 	}
 
 	public ConsoleLogger(String name) {
 		super(name);
 	}

	protected void finalize() throws Throwable {
		if (out != null)
			out.close();
		if (err != null)
			err.close();

		super.finalize();
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
