 package com.elections;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class Main {
     
     public static void main(String[] args) throws Exception{
    	String webappDirLocation = "src/main/webapp/scndpt";
    	String webappDirLocation2 = "src/main/webapp/elections";
         
 //The port that we should run on can be set into an environment variable
         //Look for that variable and default to 8080 if it isn't there.
         String webPort = System.getenv("PORT");
         if(webPort == null || webPort.isEmpty()) {
             webPort = "80";
         }
         
         
     	Server server = new Server(Integer.valueOf(webPort));
 
     	WebAppContext root = new WebAppContext();
         root.setContextPath("/");
        root.setDescriptor(webappDirLocation+"/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
         
         
         //Parent loader priority is a class loader setting that Jetty accepts.
         //By default Jetty will behave like most web containers in that it will
         //allow your application to replace non-server libraries that are part of the
         //container. Setting parent loader priority to true changes this behavior.
         //Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
         root.setParentLoaderPriority(true);
         
         WebAppContext scndpt = new WebAppContext();
         scndpt.setContextPath("/");
        scndpt.setDescriptor(webappDirLocation2+"/WEB-INF/web.xml");
        scndpt.setResourceBase(webappDirLocation2);
         scndpt.setAliases(true);
         String[] hosts = {"scnd.pt", "www.scnd.pt"};
         scndpt.setVirtualHosts(hosts);
         
         //Parent loader priority is a class loader setting that Jetty accepts.
         //By default Jetty will behave like most web containers in that it will
         //allow your application to replace non-server libraries that are part of the
         //container. Setting parent loader priority to true changes this behavior.
         //Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
         scndpt.setParentLoaderPriority(true);
        
         server.setHandler(root);
         server.setHandler(scndpt);
         server.start();
         server.join();   
     }
 
 }
