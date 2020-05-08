 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.telefonica.tcloud.db_collectd2fqn;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 /**
  *
  * @author jomar
  */
 class WebServerThread extends Thread {
     private Server server = null;
     private boolean shutdown = false;
     public static final String warPath=
            "/opt/monitoring/shared/registerfqn4monitoring_ws-1.0-SNAPSHOT.war";
     public static final String warContext="/registerfqn4monitoring_ws";
     public WebServerThread(int port)  {
         server = new Server(port);
         
         WebAppContext webapp = new WebAppContext();
         webapp.setWar(warPath);
         webapp.setContextPath(warContext);
         server.setHandler(webapp);
         try {
             server.start();
         } catch (Exception ex) {
             Logger.getLogger(Collectd2FQNMapDB.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void shutdown() {
         shutdown = true;
         this.interrupt();
     }
 
     @Override
     public void run() {
         while (!shutdown) {
             try {
                 server.join();
             } catch (InterruptedException ex) {
             }
         }
     }
     
 }
