 package com.stonepeak.monkey;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import com.stonepeak.monkey.data.GlobalConfig;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 
 public class Main {
 	
 	private static final String WEB_SERVICE_ROOT_PATH = "/rest";
 	private static final String JAR_WEB_CONTENT_DIRECTORY = "html";
	private static final String JAR_WEB_CONTENT_STARTPAGE = "index.html";
 	
     public static void main(String[] args) throws IOException 
     {
     	// process command line args and set global app config
     	if (args.length > 0)
     	{
     		GlobalConfig.setGtestAppName(args[0]);
     	} else {
     		System.out.println("Please specify a gtest native executable (eg java -jar testmonkey.jar [native exe name])");
     		System.exit(-1);
     	}
     	
     	suppressFrameworkLogging();
     		
     	// Create the web server
         Server server = new Server(8080);
         
         // set up servlet handler for rest calls
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         
         context.setContextPath(WEB_SERVICE_ROOT_PATH);
         
         ServletHolder h = new ServletHolder(new ServletContainer());
         h.setInitParameter("com.sun.jersey.config.property.packages", "com.stonepeak.monkey.resources");
 		h.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
         context.addServlet(h, "/*");
         
         // set up a resource handler to serve up static content from jar html dir
         String webDir = server.getClass().getClassLoader().getResource(JAR_WEB_CONTENT_DIRECTORY).toExternalForm();
         
         ResourceHandler resource_handler = new ResourceHandler();
         resource_handler.setDirectoriesListed(false);
         resource_handler.setWelcomeFiles(new String[]{ JAR_WEB_CONTENT_STARTPAGE });
         resource_handler.setResourceBase(webDir);

         HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, context });
         
         server.setHandler(handlers);
         
         System.out.println("Starting app...\n");
         
         try {
             server.start();
             System.out.println("Started OK! Point your browser to http://localhost:8080. Press Ctrl-c to exit.\n\n");
             server.join();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Prevent jersey and jetty logging
      */
     public static void suppressFrameworkLogging()
     {
     	// Turn off jetty logging to stderr
     	System.setProperty( "org.eclipse.jetty.util.log.class", "com.stonepeak.monkey.HushLogger");
     	org.eclipse.jetty.util.log.Log.setLog(new HushLogger());
     	// Turn off jersey logging
     	java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger("com.sun.jersey");
     	jerseyLogger.setLevel(Level.OFF);
     }
 }
