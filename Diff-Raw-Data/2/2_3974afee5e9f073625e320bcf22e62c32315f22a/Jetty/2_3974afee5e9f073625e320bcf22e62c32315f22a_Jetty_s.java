 package com.wideplay.warp.widgets.acceptance;
 
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 /**
  * @author Dhanji R. Prasanna (dhanji@gmail.com)
  */
 public class Jetty {
     private final Server server = new Server(4040);
 
     private Jetty(WebAppContext webAppContext) {
         server.addHandler(webAppContext);
     }
 
     public static void main(String... args) throws Exception {
        new Jetty(new WebAppContext("out/exploded/Warp-mvcWeb", "/warp")).run();
     }
 
     private void run() throws Exception {
         server.start();
         server.join();
     }
 }
