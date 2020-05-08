 package com.gydoc.galleon.servlet.listener;
 
 import com.gydoc.galleon.IdGenerator;
 import com.gydoc.galleon.SpringUtil;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class GalleonAppContextListener implements ServletContextListener {
 
     private static Logger log = LoggerFactory.getLogger(GalleonAppContextListener.class);
 
     public void contextInitialized(ServletContextEvent servletContextEvent) {
        String fileList = servletContextEvent.getServletContext().getInitParameter("GalleonSpringConfFiles");
         if (log.isInfoEnabled()) {
             log.info("Load spring conf files: " + fileList);
         }
         SpringUtil.init(fileList);
         SpringUtil.getSpringContext();
     }
 
     public void contextDestroyed(ServletContextEvent servletContextEvent) {
         
     }
     
 }
