 package edu.chl.hajo.sshop;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 
 /**
  * Web application lifecycle listener.
  *
  * @author anno
  */
 @WebListener()
 public class ContextListener implements ServletContextListener {
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
         Logger.getAnonymousLogger().log(Level.INFO, "Putting Shop in application scope");
         sce.getServletContext().setAttribute(Keys.SHOP.toString(), Shop.INSTANCE);
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
