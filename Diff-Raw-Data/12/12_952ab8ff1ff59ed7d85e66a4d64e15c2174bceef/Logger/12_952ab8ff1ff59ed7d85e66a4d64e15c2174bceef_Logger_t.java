 package com.nsn.uwr.panio.logger;
 
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.util.Properties;
 
 public abstract class Logger {
 
 	private enum LoggerType {
 
 		CONSOLE(ConsoleLogger.class),FILE(FileLogger.class);
 
 		private final Class<? extends Logger> loggerClass;
 
 		private LoggerType(Class<? extends Logger> loggerClass) {
 			this.loggerClass = loggerClass;
 		}
 
 		public Class<? extends Logger> getLoggerClass() {
 			return loggerClass;
 		}
 	};
 
 	private static Logger instance;
 
 	public static Logger getInstance() {
 		if (instance == null) {
 
 			Properties properties = new Properties();
 			try {
 				properties.load(Logger.class
						.getResourceAsStream("logger.properties"));
 			String property = properties.getProperty("Logger.Type");
 
 			LoggerType loggerType = LoggerType.valueOf(property);
 
 			instance = loggerType.getLoggerClass().newInstance();
 			} catch (Exception e) {
 				instance = new ConsoleLogger();
				e.printStackTrace();
 				
 			} 
 		}
 		return instance;
 	}
 
 	protected abstract PrintStream getOutputStream();
 
 	public void logMessage(String message, ELogLevel loglevel) {
 
 		DateFormat datetimeFormatter = DateFormat.getDateTimeInstance();
 
 		java.util.Date date = new java.util.Date();
 		String dateString = datetimeFormatter.format(date);
 
 		this.getOutputStream().println(
 				loglevel + " [" + dateString + "] " + message);
 
 	};
 
 	public void logWarning(String message) {
 
 		logMessage(message, ELogLevel.WARNING);
 
 	};
 
 	public void logInfo(String message) {
 
 		logMessage(message, ELogLevel.INFO);
 
 	};
 
 	public void logError(String message) {
 
 		logMessage(message, ELogLevel.ERROR);
 
 	};
 
 }
