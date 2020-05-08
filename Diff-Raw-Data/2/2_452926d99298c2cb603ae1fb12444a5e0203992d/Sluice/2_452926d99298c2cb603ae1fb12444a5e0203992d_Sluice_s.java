 package com.dashlabs.sluice;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.util.resource.Resource;
 
 import java.io.File;
 import java.util.Arrays;
 
 /**
  * User: blangel
  * Date: 1/15/13
  * Time: 8:35 AM
  *
  * Runs a jetty server to serve static content.
  */
 public final class Sluice {
 
     public static void main(String[] args) throws Exception {
         if ((args == null) || (args.length < 1)) {
             System.out.printf("^error^ sluice dir [port] [welcome files...]");
             System.exit(1);
         }
         String baseDir = args[0];
         int port = 8080;
        if (args.length == 2) {
             try {
                 port = Integer.valueOf(args[1]);
             } catch (NumberFormatException nfe) {
                 System.out.printf("^error^ Could not parse port %s%n", args[1]);
             }
         }
         Server server = new Server(port);
         ResourceHandler resourceHandler = new ResourceHandler();
         if (args.length > 2) {
             resourceHandler.setWelcomeFiles(Arrays.copyOfRange(args, 2, args.length));
         } else {
             resourceHandler.setWelcomeFiles(new String[] { "index.html" });
         }
         Resource base = Resource.newResource(new File(baseDir));
         resourceHandler.setBaseResource(base);
         server.setHandler(resourceHandler);
         server.start();
         server.join();
     }
 
     private Sluice() { }
 
 }
