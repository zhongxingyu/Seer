 package com.vast.util;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.eclipse.jetty.xml.XmlConfiguration;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import java.io.File;
 import java.net.URL;
 import java.security.ProtectionDomain;
 
 /**
  * @author David Pratt (dpratt@vast.com)
  */
 public class EmbeddedJettyServer {
 
     public static void main(String[] args) throws Exception {
 
         if(args.length > 1 && args[0].equals("-?")) {
             usage();
             System.exit(0);
         }
 
         Args parsedArgs = parseArgs(args);
 
         //probably want to find a better place to put this.
         // Optionally remove existing handlers attached to j.u.l root logger
         SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)
 
         // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
         // the initialization phase of your application
         SLF4JBridgeHandler.install();
 
         Server server = new Server();
 
         if(parsedArgs.jettyConfig != null) {
             //if there's an explicit jetty.xml, use it
             XmlConfiguration configuration = new XmlConfiguration(new File(parsedArgs.jettyConfig).toURL());
             configuration.configure(server);
         } else {
             //otherwise just go with reasonable defaults
             SelectChannelConnector connector = new SelectChannelConnector();
             connector.setPort(8080);
             server.setConnectors(new Connector[]{connector});
         }
 
         String contextRoot = parsedArgs.contextRoot;
         if(!contextRoot.startsWith("/")) {
             contextRoot = "/" + contextRoot;
         }
 
         WebAppContext context = new WebAppContext();
         context.setServer(server);
         context.setContextPath(contextRoot);
 
         //prevent webapps from loading any logging classes
         context.addSystemClass("org.apache.log4j.");
         context.addSystemClass("org.slf4j.");
         context.addSystemClass("org.apache.commons.logging.");
 
         ProtectionDomain protectionDomain = EmbeddedJettyServer.class.getProtectionDomain();
         URL location = protectionDomain.getCodeSource().getLocation();
         context.setWar(location.toExternalForm());
 
         server.setHandler(context);
         try {
             server.start();
             server.join();
         } catch (Exception e) {
             e.printStackTrace();
            System.exit(100);
         }
     }
 
     private static void usage() {
         System.out.println("==================================================");
         System.out.println("   Usage ---  ");
         System.out.println(" java -jar your_war_file ");
         System.out.println("    --jetty-config=   Path to an optional Jetty XML config file.");
         System.out.println("    --context-root=   The prefix to use as the servlet context root for this executable war.");
         System.out.println("==================================================");
     }
 
     //not making this a JavaBean - getters and setters are stupid for a private static class
     //If you're looking at this and thinking WTF, take a step back and think about *why* you
     //make everything a bean pattern when it doesn't get you anything. Then, go learn Scala.
     private static class Args {
         public String contextRoot = "/";
         public String jettyConfig = null;
     }
 
     private static final String JETTY_CONFIG = "--jetty-config=";
     private static final String CONTEXT_ROOT = "--context-root=";
 
     private static Args parseArgs(String[] args) {
 
         Args parsed = new Args();
 
         for(int i=0;i<args.length;i++) {
             if(args[i].startsWith(JETTY_CONFIG)) {
                 parsed.jettyConfig = args[i].substring(JETTY_CONFIG.length());
             } else if(args[i].startsWith(CONTEXT_ROOT)) {
                 parsed.contextRoot = args[i].substring(CONTEXT_ROOT.length());
             }
         }
 
         return parsed;
     }
 
 
 
 }
