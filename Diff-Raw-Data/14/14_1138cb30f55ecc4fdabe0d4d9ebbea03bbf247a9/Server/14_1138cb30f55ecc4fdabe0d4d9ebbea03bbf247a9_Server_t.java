 package org.hackystat.dailyprojectdata.server;
 
 import java.util.Enumeration;
 import java.util.Map;
 
 import org.hackystat.dailyprojectdata.resource.devtime.DevTimeManager;
 import org.hackystat.dailyprojectdata.resource.devtime.DevTimeResource;
 import org.hackystat.utilities.logger.HackystatLogger;
 import org.restlet.Application;
 import org.restlet.Component;
 import org.restlet.Guard;
 import org.restlet.Restlet;
 import org.restlet.Router;
 import org.restlet.data.Protocol;
 
 import static org.hackystat.dailyprojectdata.server.ServerProperties.HOSTNAME_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.PORT_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.CONTEXT_ROOT_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.LOGGING_LEVEL_KEY;
 
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 /**
  * Sets up the HTTP Server process and dispatching to the associated resources. 
  * @author Philip Johnson
  */
 public class Server extends Application { 
 
   /** Holds the Restlet Component associated with this Server. */
   private Component component; 
   
   /** Holds the host name associated with this Server. */
   private String hostName;
   
   /** Holds the HackystatLogger for this Service. */
   private Logger logger; 
   
   /**
    * Creates a new instance of a DailyProjectData HTTP server, listening on the supplied port.  
    * @return The Server instance created. 
    * @throws Exception If problems occur starting up this server. 
    */
   public static Server newInstance() throws Exception {
     Server server = new Server();
     server.logger = HackystatLogger.getLogger("org.hackystat.dailyprojectdata");
     ServerProperties.initializeProperties();
     server.hostName = "http://" +
                       ServerProperties.get(HOSTNAME_KEY) + 
                       ":" + 
                       ServerProperties.get(PORT_KEY) + 
                       "/" +
                       ServerProperties.get(CONTEXT_ROOT_KEY) +
                       "/";
     int port = Integer.valueOf(ServerProperties.get(PORT_KEY));
     server.component = new Component();
     server.component.getServers().add(Protocol.HTTP, port);
     server.component.getDefaultHost()
       .attach("/" + ServerProperties.get(CONTEXT_ROOT_KEY), server);
  
     
     // Now create all of the Resource Managers and store them in the Context.
     Map<String, Object> attributes = server.getContext().getAttributes();
     attributes.put("DevTimeManager", new DevTimeManager(server));
    
    // Provide a pointer to this server in the Context as well. 
     attributes.put("DailyProjectDataServer", server);
     
     // Now let's open for business. 
     server.logger.warning("Host: " + server.hostName);
     HackystatLogger.setLoggingLevel(server.logger, ServerProperties.get(LOGGING_LEVEL_KEY));
     ServerProperties.echoProperties(server);
     server.logger.warning("DailyProjectData (Version " + getVersion() + ") now running.");
     server.component.start();
     disableRestletLogging();
     return server;
   }
 
   /**
    * Disable all loggers from com.noelios and org.restlet. 
    */
   private static void disableRestletLogging() {
     LogManager logManager = LogManager.getLogManager();
     for (Enumeration e = logManager.getLoggerNames(); e.hasMoreElements() ;) {
       String logName = e.nextElement().toString();
       if (logName.startsWith("com.noelios") ||
           logName.startsWith("org.restlet")) {
         logManager.getLogger(logName).setLevel(Level.OFF);
       }
     }
   }
   
   /**
    * Starts up the web service.  Control-c to exit. 
    * @param args Ignored. 
    * @throws Exception if problems occur.
    */
   public static void main(final String[] args) throws Exception {
     Server.newInstance();
   }
 
   /**
    * Dispatch to the specific DailyProjectData resource based upon the URI.
    * We will authenticate all requests.
    * @return The router Restlet.
    */
   @Override
   public Restlet createRoot() {
     // First, create a Router that will have a Guard placed in front of it so that this Router's
     // requests will require authentication.
     Router authRouter = new Router(getContext());
     authRouter.attach("/devtime", DevTimeResource.class);
     // Here's the Guard that we will place in front of authRouter.
     Guard guard = new Authenticator(getContext());
     guard.setNext(authRouter);
     return guard;
   }
 
 
   /**
    * Returns the version associated with this Package, if available from the jar file manifest.
    * If not being run from a jar file, then returns "Development". 
    * @return The version.
    */
   public static String getVersion() {
     String version = 
       Package.getPackage("org.hackystat.dailyprojectdata.server").getImplementationVersion();
     return (version == null) ? "Development" : version; 
   }
   
   /**
    * Returns the host name associated with this server. 
    * Example: "http://localhost:9877/dailyprojectdata"
    * @return The host name. 
    */
   public String getHostName() {
     return this.hostName;
   }
   
   /**
    * Returns the logger for this service.
    * @return The logger.
    */
   @Override
   public Logger getLogger() {
     return this.logger;
   }
 }
 
