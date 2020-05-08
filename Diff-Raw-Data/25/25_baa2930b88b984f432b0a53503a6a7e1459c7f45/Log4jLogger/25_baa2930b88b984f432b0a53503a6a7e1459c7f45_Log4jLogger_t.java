 package log.impl;
 
 import org.apache.log4j.Logger;
 
 public class Log4jLogger implements log.Logger {

 	static {
 		Logger.getLogger(Log4jLogger.class).trace("Using the Log4j 1.2 logging framework");
 	}

 	private final Logger logger;

 	public Log4jLogger(Class<?> clazz) {
 		logger = Logger.getLogger(clazz);
 	}
 
 	@Override
 	public void catching(Throwable t) {
 		logger.error(t);
 	}
 
 	@Override
 	public void trace(String s, Object... params) {
 		if (logger.isTraceEnabled()) {
 			logger.trace(String.format(s, params));
 		}
 	}
 
 	@Override
 	public void debug(String s, Object... params) {
 		if (logger.isDebugEnabled()) {
 			logger.debug(String.format(s, params));
 		}
 	}
 
 	@Override
 	public void info(String s, Object... params) {
 		if (logger.isInfoEnabled()) {
 			logger.info(String.format(s, params));
 		}
 	}
 
 	@Override
 	public void warn(String s, Object... params) {
		logger.warn(String.format(s, params));
 	}
 
 	@Override
 	public void error(String s, Object... params) {
		logger.error(String.format(s, params));
 	}
 
 	@Override
 	public void fatal(String s, Object... params) {
		logger.fatal(String.format(s, params));
 	}
 
 }
