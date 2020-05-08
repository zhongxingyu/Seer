 /**
  * @source http://www.mkyong.com/servlet/what-is-listener-servletcontextlistener-example/
  */
 package com.homework.hw3;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import com.homework.hw3.hsql.DbAccess;
 
 public class AppServletContextListener implements ServletContextListener{
  
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		System.out.println("ServletContextListener destroyed");
 	}
  
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		System.out.println("ServletContextListener started");	
		AddServlet.contextHasRan = true;
 		DbAccess.setupDatabase();
 	}
 }
