 package servlet;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 
 import quizweb.accountmanagement.AccountManager;
 
 /**
  * Application Lifecycle Listener implementation class Setup
  *
  */
 @WebListener
 public class Setup implements ServletContextListener {
 
     /**
      * Default constructor. 
      */
     public Setup() {
         // TODO Auto-generated constructor stub
     }
 
 	/**
      * @see ServletContextListener#contextInitialized(ServletContextEvent)
      */
     public void contextInitialized(ServletContextEvent arg0) {
     	AccountManager am = new AccountManager();
     	ServletContext sc = arg0.getServletContext();
    	sc.setAttribute("account manager", am);
     }
 
 	/**
      * @see ServletContextListener#contextDestroyed(ServletContextEvent)
      */
     public void contextDestroyed(ServletContextEvent arg0) {
     }
 }
