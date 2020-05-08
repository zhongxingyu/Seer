 package uk.co.bssd.monitoring;
 
 import java.io.IOException;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.PatternLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
public class Slf4jValueLogger<T> {
 
 	private static final String APPENDER_NAME = "file";
 	
 	private final Logger logger;
 
 	public Slf4jValueLogger(String filename) {
 		this.logger = LoggerFactory.getLogger(Slf4jValueLogger.class);
 
 		if (log4jLogger().getAppender(APPENDER_NAME) == null) {
			PatternLayout layout = new PatternLayout("%d,%m%n");
 
 			try {
 				FileAppender fileAppender = new FileAppender(layout, filename);
 				fileAppender.setName(APPENDER_NAME);
 				log4jLogger().addAppender(fileAppender);
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 	}
 	
 	private org.apache.log4j.Logger log4jLogger() {
 		return org.apache.log4j.Logger.getLogger(Slf4jValueLogger.class);
 	}
 	
	public void value(T currentValue) {
		this.logger.info("{}", currentValue); 
 	}
 }
