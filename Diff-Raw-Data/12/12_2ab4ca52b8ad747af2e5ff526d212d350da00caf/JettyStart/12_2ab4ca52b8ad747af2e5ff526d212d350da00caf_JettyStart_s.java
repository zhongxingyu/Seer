 package com.abudko.reseller.huuto;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.ServerConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class JettyStart {
     public static void main(String[] args) throws Exception {
         int timeout = 3600000;
 
         Server server = new Server();
         ServerConnector connector = new ServerConnector(server);
 
         // Set some timeout options to make debugging easier.
         connector.setStopTimeout(timeout);
         connector.setSoLingerTime(-1);
        connector.setPort(8088);
         server.setConnectors(new Connector[] { connector });
 
         WebAppContext bb = new WebAppContext();
         bb.setServer(server);
         bb.setContextPath("/");
         bb.setWar("src/main/webapp");
 
         // START JMX SERVER
         // MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
         // MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
         // server.getContainer().addEventListener(mBeanContainer);
         // mBeanContainer.start();
 
         server.setHandler(bb);
 
         try {
             System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
             server.start();
             System.in.read();
             System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
             server.stop();
             server.join();
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 }
