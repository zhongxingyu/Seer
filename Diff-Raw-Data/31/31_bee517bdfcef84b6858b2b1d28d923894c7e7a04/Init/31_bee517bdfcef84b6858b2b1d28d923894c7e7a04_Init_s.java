 package monopoly.web.servlet;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 
 /**
  * Application Lifecycle Listener implementation class Init
  *
  */
 @WebListener
 public class Init implements ServletContextListener {
 
 	
 	private Thread worker;
 	
    /**
     * Default constructor. 
     */
    public Init() {
        // TODO Auto-generated constructor stub
    }
 
 	/**
      * @see ServletContextListener#contextInitialized(ServletContextEvent)
      */
     public void contextInitialized(ServletContextEvent arg0) {
     	worker = new Thread(new GameRunner(arg0.getServletContext()));
         worker.start();
     }
 
 	/**
      * @see ServletContextListener#contextDestroyed(ServletContextEvent)
      */
     public void contextDestroyed(ServletContextEvent arg0) {
         worker.interrupt();
     }
 	
 }
