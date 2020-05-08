 package com.squareward.peachtree;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.support.GenericXmlApplicationContext;
 
 import com.squareward.peachtree.grizzly.GrizzlyServer;
 import com.squareward.peachtree.util.JUL2SLF4JBridge;
 
 /**
  * 
  * @author wangzijian
  * 
  */
 public class Launcher {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
 	
 	private Launcher() {
 	}
 	
 	public static void main(String[] args) throws Throwable {
 		GrizzlyServer server = null;
 		GenericXmlApplicationContext applicationContext = null;
 		try {
 			JUL2SLF4JBridge.disableJULPrinting();
 			applicationContext = newApplicationContext();
 			server = applicationContext.getBean(GrizzlyServer.class);
 			server.start();
 			synchronized (server) {
 				server.wait();
 			}
 		} catch (Throwable e) {
 			LOGGER.error("Failed to start server", e);
 			throw e;
 		} finally {
 			destroyQuietly(applicationContext);
 			stopQuietly(server);
 		}
 	}
 	
 	private static GenericXmlApplicationContext newApplicationContext() {
 		return new GenericXmlApplicationContext(
				"classpath:application.hibernate.xml",
				"classpath:application.datasource.xml",
 				"classpath:application.system.xml",
 				"classpath:application.component.xml",
 				"classpath:application.server.xml",
 				"classpath:application.mock.xml");
 	}
 	
 	private static void stopQuietly(GrizzlyServer server) {
 		if (server != null) {
 			try {
 				server.stop();
 			} catch (Exception e) {
 				LOGGER.error("Failed to stop server", e);
 			}
 		}
 	}
 
 	private static void destroyQuietly(GenericXmlApplicationContext applicationContext) {
 		if (applicationContext != null) {
 			try {
 				applicationContext.destroy();
 			} catch (Exception e) {
 				LOGGER.error("Failed to destroy ApplicationContext", e);
 			}
 		}
 	}
 }
