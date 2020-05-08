 package org.hackystat.dailyprojectdata.server;
 
 import static org.hackystat.dailyprojectdata.server.ServerProperties.CONTEXT_ROOT_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.HOSTNAME_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.LOGGING_LEVEL_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.PORT_KEY;
 import static org.hackystat.dailyprojectdata.server.ServerProperties.SENSORBASE_FULLHOST_KEY;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBContext;
 
 import org.hackystat.dailyprojectdata.resource.build.BuildResource;
 import org.hackystat.dailyprojectdata.resource.codeissue.CodeIssueResource;
 import org.hackystat.dailyprojectdata.resource.commit.CommitResource;
 import org.hackystat.dailyprojectdata.resource.coverage.CoverageResource;
 import org.hackystat.dailyprojectdata.resource.devtime.DevTimeResource;
 import org.hackystat.dailyprojectdata.resource.filemetric.FileMetricResource;
 import org.hackystat.dailyprojectdata.resource.ping.PingResource;
 import org.hackystat.dailyprojectdata.resource.unittest.UnitTestResource;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.utilities.logger.HackystatLogger;
 import org.hackystat.utilities.logger.RestletLoggerUtil;
 import org.restlet.Application;
 import org.restlet.Component;
 import org.restlet.Guard;
 import org.restlet.Restlet;
 import org.restlet.Router;
 import org.restlet.data.Protocol;
 
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
   
   /** Holds the ServerProperties instance for this Service. */
   private ServerProperties properties;
 
   /**
    * Creates a new instance of a DailyProjectData HTTP server, listening on the supplied port.
    * @return The Server instance created. 
    * @throws Exception If problems occur starting up this server. 
    */
   public static Server newInstance() throws Exception {
     return newInstance(new ServerProperties());
   }
   
   /**
    * Creates a new instance of a DailyProjectData HTTP server suitable for unit testing. 
    * DPD properties are initialized from the User's dailyprojectdata.properties file, 
    * then set to their "testing" versions.   
    * @return The Server instance created. 
    * @throws Exception If problems occur starting up this server. 
    */
   public static Server newTestInstance() throws Exception {
     ServerProperties properties = new ServerProperties();
     properties.setTestProperties();
     return newInstance(properties);
   }
   
   /**
    * Creates a new instance of a DailyProjectData HTTP server, listening on the supplied port.
    * @param properties The ServerProperties instance used to initialize this server.  
    * @return The Server instance created. 
    * @throws Exception If problems occur starting up this server. 
    */
   public static Server newInstance(ServerProperties properties) throws Exception {
     Server server = new Server();
    server.logger = HackystatLogger.getLogger("org.hackystat.dailyprojectdata");
     server.properties = properties;
     server.hostName = "http://" +
                       server.properties.get(HOSTNAME_KEY) + 
                       ":" + 
                       server.properties.get(PORT_KEY) + 
                       "/" +
                       server.properties.get(CONTEXT_ROOT_KEY) +
                       "/";
     int port = Integer.valueOf(server.properties.get(PORT_KEY));
     server.component = new Component();
     server.component.getServers().add(Protocol.HTTP, port);
     server.component.getDefaultHost()
       .attach("/" + server.properties.get(CONTEXT_ROOT_KEY), server);
     
     // Create and store the JAXBContext instances on the server context.
     // They are supposed to be thread safe. 
     Map<String, Object> attributes = server.getContext().getAttributes();
     
     JAXBContext devTimeJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.devtime.jaxb.ObjectFactory.class);
     attributes.put("DevTimeJAXB", devTimeJAXB);
     JAXBContext fileMetricJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.filemetric.jaxb.ObjectFactory.class);
     attributes.put("FileMetricJAXB", fileMetricJAXB);
 
     
     JAXBContext unitTestJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.unittest.jaxb.ObjectFactory.class);
     attributes.put("UnitTestJAXB", unitTestJAXB);
     
     JAXBContext codeIssueJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.codeissue.jaxb.ObjectFactory.class);
     attributes.put("CodeIssueJAXB", codeIssueJAXB);
     JAXBContext coverageJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.coverage.jaxb.ObjectFactory.class);
     attributes.put("CoverageJAXB", coverageJAXB);
     JAXBContext buildJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.build.jaxb.ObjectFactory.class);
     attributes.put("BuildJAXB", buildJAXB);
     JAXBContext commitJAXB = JAXBContext.newInstance(
         org.hackystat.dailyprojectdata.resource.commit.jaxb.ObjectFactory.class);
     attributes.put("CommitJAXB", commitJAXB);
     
     // Provide a pointer to this server in the Context so that Resources can get at this server.
     attributes.put("DailyProjectDataServer", server);
     
     // Move Restlet Logging into a file. 
     RestletLoggerUtil.useFileHandler("dailyprojectdata");
     
     // Now let's open for business. 
     server.logger.warning("Host: " + server.hostName);
     HackystatLogger.setLoggingLevel(server.logger, server.properties.get(LOGGING_LEVEL_KEY));
     server.logger.info(server.properties.echoProperties());
     String sensorBaseHost = server.properties.get(SENSORBASE_FULLHOST_KEY);
     boolean sensorBaseOK = SensorBaseClient.isHost(sensorBaseHost);
     server.logger.warning("SensorBase " + sensorBaseHost + 
         ((sensorBaseOK) ? " was contacted successfully." : 
           " NOT AVAILABLE. This service will not run correctly."));
     server.logger.warning("DailyProjectData (Version " + getVersion() + ") now running.");
     server.component.start();
 
     return server;
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
     authRouter.attach("/devtime/{user}/{project}/{timestamp}", DevTimeResource.class);
     authRouter.attach("/filemetric/{user}/{project}/{timestamp}/{sizemetric}", 
         FileMetricResource.class);
     authRouter.attach("/unittest/{user}/{project}/{timestamp}", UnitTestResource.class);
     authRouter.attach("/codeissue/{user}/{project}/{timestamp}", CodeIssueResource.class);
     authRouter.attach("/codeissue/{user}/{project}/{timestamp}?Tool={Tool}&Type={Type}", 
         CodeIssueResource.class);
     authRouter.attach("/codeissue/{user}/{project}/{timestamp}?Tool={Tool}", 
         CodeIssueResource.class);
     authRouter.attach("/codeissue/{user}/{project}/{timestamp}?Type={Type}", 
         CodeIssueResource.class);
     authRouter.attach("/coverage/{user}/{project}/{timestamp}/{granularity}", 
         CoverageResource.class);
     authRouter.attach("/build/{user}/{project}/{timestamp}", BuildResource.class);
     authRouter.attach("/build/{user}/{project}/{timestamp}?Type={Type}", BuildResource.class);
     authRouter.attach("/commit/{user}/{project}/{timestamp}", CommitResource.class);
 
     // Here's the Guard that we will place in front of authRouter.
     Guard guard = new Authenticator(getContext(), 
         this.getServerProperties().get(SENSORBASE_FULLHOST_KEY));
     guard.setNext(authRouter);
     
     // Now create our "top-level" router which will allow the Ping URI to proceed without
     // authentication, but all other URI patterns will go to the guarded Router.
     Router router = new Router(getContext());
     router.attach("/ping", PingResource.class);
     router.attach("/ping?user={user}&password={password}", PingResource.class);
     router.attachDefault(guard);
     return router;
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
    * Returns the ServerProperties instance associated with this server. 
    * @return The server properties.
    */
   public ServerProperties getServerProperties() {
     return this.properties;
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
 
