 package com.chariot.simpleutil;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class AppRunner {
 	private static Logger logger = LoggerFactory.getLogger(AppRunner.class);
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		logger.debug("Starting App");
 		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
 				"/META-INF/spring/app-context.xml");
 
 		Service service = applicationContext.getBean(Service.class);
 
		logger.debug("Yeah I got your service bean... right here: {}", service);
 
 		// Your Code here!
 
 	}
 
 }
