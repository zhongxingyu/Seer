 package com.ted.listeners;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

 /**
  * Session and context listener. Session : Sets the MAX_INACTIVE_INTERVAL for
  * the session (in seconds) after which the session times out. The session
  * listener is intended to save the number of sessions as a context attribute -
  * but this does not work too well with Tomcat's hot redeployContext :
  * Initializes a ServletContext static variable and prints out when the context
  * is initialized and is about to be destroyed.
  */
 @WebListener
 public class SessionListener implements ServletContextListener,
 		HttpSessionListener {
 
 	private static final int MAX_INACTIVE_INTERVAL = 1000;
 	// static AtomicInteger numOfSessions;
 	// singleton ? static ?
 	static int numOfSessions;
 	static ServletContext context;
 
 	@Override
 	public void sessionCreated(HttpSessionEvent se) {
 		HttpSession session = se.getSession();
 		session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
 		Map<String, String> messages = new HashMap<>();
 		session.setAttribute("messages", messages);
 		System.out.println("Source : " + se.getSource());
 		increase();
 	}
 
 	@Override
 	public void sessionDestroyed(HttpSessionEvent se) {
 		decrease();
 	}
 
 	private synchronized void increase() {
 		synchronized (this) {
 			++numOfSessions;
 			context.setAttribute("numberOfSessions", numOfSessions);
 			System.out
 					.println("SessionListener - increase - numberOfSessions = "
 							+ numOfSessions);
 		}
 	}
 
 	private synchronized void decrease() {
 		synchronized (this) {
 			--numOfSessions;
 			context.setAttribute("numberOfSessions", numOfSessions);
 			System.out
 					.println("SessionListener - decrease - numberOfSessions = "
 							+ numOfSessions);
 		}
 	}
 
 	@Override
 	public void contextDestroyed(ServletContextEvent sce) {
 		// TODO freeDS()
		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (InterruptedException e) {
			System.out.println("SessionListener.contextDestroyed()");
			LoggerFactory.getLogger(this.getClass()).debug(
					"Failed to cleanup JDBC.", e);
		}
 		System.out.println("SessionListener - contextDestroyed");
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent sce) {
 		SessionListener.context = sce.getServletContext();
 		System.out.println("SessionListener - contextInitialized : " + context);
 	}
 }
