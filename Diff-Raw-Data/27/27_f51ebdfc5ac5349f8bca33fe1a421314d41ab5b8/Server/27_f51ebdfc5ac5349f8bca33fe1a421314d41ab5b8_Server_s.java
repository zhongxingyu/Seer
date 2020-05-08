 package org.hackystat.sensorbase.server;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.hackystat.sensorbase.db.DbManager;
 import org.hackystat.sensorbase.mailer.Mailer;
 import org.hackystat.sensorbase.resource.db.CompressResource;
 import org.hackystat.sensorbase.resource.db.IndexResource;
 import org.hackystat.sensorbase.resource.db.RowCountResource;
 import org.hackystat.sensorbase.resource.ping.PingResource;
 import org.hackystat.sensorbase.resource.projects.ProjectManager;
 import org.hackystat.sensorbase.resource.projects.ProjectsResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectInvitationResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectRenameResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectSensorDataResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectSnapshotResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectSummaryResource;
 import org.hackystat.sensorbase.resource.projects.UserProjectsResource;
 import org.hackystat.sensorbase.resource.registration.HomePageResource;
 import org.hackystat.sensorbase.resource.registration.RegistrationResource;
 import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
 import org.hackystat.sensorbase.resource.sensordata.SensorDataResource;
 import org.hackystat.sensorbase.resource.sensordata.UserSensorDataResource;
 import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
 import org.hackystat.sensorbase.resource.sensordatatypes.SensorDataTypeResource;
 import org.hackystat.sensorbase.resource.sensordatatypes.SensorDataTypesResource;
 import org.hackystat.sensorbase.resource.users.UserManager;
 import org.hackystat.sensorbase.resource.users.UserResource;
 import org.hackystat.sensorbase.resource.users.UsersResource;
 import org.hackystat.utilities.logger.HackystatLogger;
 import org.hackystat.utilities.logger.RestletLoggerUtil;
 import org.restlet.Application;
 import org.restlet.Component;
 import org.restlet.Guard;
 import org.restlet.Restlet;
 import org.restlet.Router;
 import org.restlet.data.Protocol;
 
 import static org.hackystat.sensorbase.server.ServerProperties.HOSTNAME_KEY;
 import static org.hackystat.sensorbase.server.ServerProperties.PORT_KEY;
 import static org.hackystat.sensorbase.server.ServerProperties.CONTEXT_ROOT_KEY;
 import static org.hackystat.sensorbase.server.ServerProperties.LOGGING_LEVEL_KEY;
 
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
   
   /** Holds the HackystatLogger for the sensorbase. */
   private Logger logger; 
   
   /** Holds the ServerProperties instance associated with this sensorbase. */
   private ServerProperties serverProperties;
   
   /**
    * Creates a new instance of a SensorBase HTTP server, listening on the supplied port.
    * SensorBase properties are initialized from the User's sensorbase.properties file.  
    * @return The Server instance created. 
    * @throws Exception If problems occur starting up this server. 
    */
   public static Server newInstance() throws Exception {
     return newInstance(new ServerProperties());
   }
   
   /**
    * Creates a new instance of a SensorBase HTTP server suitable for unit testing. 
    * SensorBase properties are initialized from the User's sensorbase.properties file, 
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
    * Creates a new instance of a SensorBase HTTP server, listening on the supplied port.
    * @param  serverProperties The ServerProperties used to initialize this server.
    * @return The Server instance created. 
    * @throws Exception If problems occur starting up this server. 
    */
   public static Server newInstance(ServerProperties serverProperties) throws Exception {
     Server server = new Server();
     server.logger = HackystatLogger.getLogger("org.hackystat.sensorbase", "sensorbase");
     server.serverProperties = serverProperties;
     server.hostName = "http://" +
                       server.serverProperties.get(HOSTNAME_KEY) + 
                       ":" + 
                       server.serverProperties.get(PORT_KEY) + 
                       "/" +
                       server.serverProperties.get(CONTEXT_ROOT_KEY) +
                       "/";
     int port = Integer.valueOf(server.serverProperties.get(PORT_KEY));
     server.component = new Component();
     server.component.getServers().add(Protocol.HTTP, port);
     server.component.getDefaultHost()
       .attach("/" + server.serverProperties.get(CONTEXT_ROOT_KEY), server);
 
     // Set up logging.
     RestletLoggerUtil.useFileHandler("sensorbase");
     HackystatLogger.setLoggingLevel(server.logger, server.serverProperties.get(LOGGING_LEVEL_KEY));
     server.logger.warning("Starting sensorbase.");
     server.logger.warning("Host: " + server.hostName);
     server.logger.info(server.serverProperties.echoProperties());
 
     try {
       Mailer.getInstance();
     }
     catch (Throwable e) {
       String msg = "ERROR: JavaMail not installed correctly! Mail services will fail!";
       server.logger.warning(msg);
     }
 
     // Now create all of the Resource Managers and store them in the Context.
     // Ordering constraints: 
     // - DbManager must precede all resource managers so it can initialize the tables
     //   before the resource managers add data to them.
     // - UserManager must be initialized before ProjectManager, since ProjectManager needs
     //   to know about the Users. 
    Map<String, Object> attributes = server.getContext().getAttributes();
     DbManager dbManager = new DbManager(server);  // we need this later in this method.
     attributes.put("DbManager", dbManager);
     attributes.put("SdtManager", new SdtManager(server));
     attributes.put("UserManager", new UserManager(server));
     attributes.put("ProjectManager", new ProjectManager(server));
     attributes.put("SensorDataManager", new SensorDataManager(server));
     attributes.put("SensorBaseServer", server);
     attributes.put("ServerProperties", server.serverProperties);
     
     // Now let's open for business. 
     server.logger.info("Maximum Java heap size (MB): " + 
         (Runtime.getRuntime().maxMemory() / 1000000.0));
     server.logger.info("Table counts: " + getTableCounts(dbManager));
     server.component.start();
     server.logger.warning("SensorBase (Version " + getVersion() + ") now running.");
     return server;
   }
   
   /**
    * Returns a string with the counts of rows in the various tables. 
    * @param dbManager The dbManager. 
    * @return A string with info on row counts. 
    */
   private static String getTableCounts (DbManager dbManager) {
     Set<String> tableNames = dbManager.getTableNames();
     StringBuffer buff = new StringBuffer();
     for (String tableName : tableNames) {
       int tableRows = dbManager.getRowCount(tableName);
       buff.append(tableName).append(':').append(tableRows).append(' ');
     }
     return buff.toString();
   }
   
  
   /**
    * Starts up the SensorBase web service using the properties specified in sensor.properties.  
    * Control-c to exit. 
    * @param args Ignored. 
    * @throws Exception if problems occur.
    */
   public static void main(final String[] args) throws Exception {
     Server.newInstance();
   }
 
   /**
    * Dispatch to the Projects, SensorData, SensorDataTypes, or Users Resource depending on the URL.
    * We will authenticate all requests except for registration (users?email={email}).
    * @return The router Restlet.
    */
   @Override
   public Restlet createRoot() {
     // First, create a Router that will have a Guard placed in front of it so that this Router's
     // requests will require authentication.
     Router authRouter = new Router(getContext());
     
     // SENSORDATATYPES 
     authRouter.attach("/sensordatatypes", 
         SensorDataTypesResource.class);
     authRouter.attach("/sensordatatypes/{sensordatatypename}", 
         SensorDataTypeResource.class);
     
     // USERS
     authRouter.attach("/users", 
         UsersResource.class);
     authRouter.attach("/users/{user}", 
         UserResource.class);
     
     // SENSORDATA 
     authRouter.attach("/sensordata", 
         SensorDataResource.class);
     authRouter.attach("/sensordata/{user}", 
         UserSensorDataResource.class);
     authRouter.attach("/sensordata/{user}?sdt={sensordatatype}", 
         UserSensorDataResource.class);
     authRouter.attach(
         "/sensordata/{user}?lastModStartTime={lastModStartTime}&lastModEndTime={lastModEndTime}", 
         UserSensorDataResource.class);
     authRouter.attach("/sensordata/{user}/{timestamp}", 
         UserSensorDataResource.class);
     
     // PROJECTS
     authRouter.attach("/projects", 
         ProjectsResource.class);
     authRouter.attach("/projects/{user}", 
         UserProjectsResource.class);
     String projectUri = "/projects/{user}/{projectname}";
     authRouter.attach(projectUri,
         UserProjectResource.class);
     
     // PROJECTS SNAPSHOT
     authRouter.attach(projectUri + "/snapshot" + 
         "?startTime={startTime}&endTime={endTime}&sdt={sdt}&tool={tool}", 
         UserProjectSnapshotResource.class);
     authRouter.attach(projectUri + "/snapshot" + 
         "?startTime={startTime}&endTime={endTime}&sdt={sdt}",
         UserProjectSnapshotResource.class);
     
     // PROJECTS SUMMARY 
     authRouter.attach(projectUri + "/summary" +  
         "?startTime={startTime}&endTime={endTime}", 
         UserProjectSummaryResource.class);
     authRouter.attach(projectUri + "/summary" +  
         "?startTime={startTime}&numDays={numDays}", 
         UserProjectSummaryResource.class);
     
     // PROJECTS SENSORDATA
     String projectSensorDataUri = projectUri + "/sensordata";
     authRouter.attach(projectSensorDataUri, 
         UserProjectSensorDataResource.class);
     authRouter.attach(projectSensorDataUri +
      "?startTime={startTime}&endTime={endTime}&startIndex={startIndex}&maxInstances={maxInstances}",
        UserProjectSensorDataResource.class);
     authRouter.attach(projectSensorDataUri +  
         "?startTime={startTime}&endTime={endTime}", 
         UserProjectSensorDataResource.class);
     authRouter.attach(projectSensorDataUri +  
         "?sdt={sdt}&startTime={startTime}&endTime={endTime}&tool={tool}",  
         UserProjectSensorDataResource.class);
     authRouter.attach(projectSensorDataUri +  
         "?sdt={sdt}&startTime={startTime}&endTime={endTime}",  
         UserProjectSensorDataResource.class);
     
     // PROJECTS INVITATION 
     authRouter.attach(projectUri + "/invitation/{rsvp}", 
         UserProjectInvitationResource.class);
     
     // PROJECTS RENAME
     authRouter.attach(projectUri + "/rename/{newprojectname}", 
         UserProjectRenameResource.class);
     
     // DB Commands
     authRouter.attach("/db/table/compress", CompressResource.class);
     authRouter.attach("/db/table/index", IndexResource.class);
     authRouter.attach("/db/table/{table}/rowcount", RowCountResource.class);
     
     // Here's the Guard that we will place in front of authRouter.
     authRouter.attach("", HomePageResource.class);
     Guard guard = new Authenticator(getContext());
     guard.setNext(authRouter);
     
     // Now create our "top-level" router which will allow the registration URI to proceed without
     // authentication, but all other URI patterns will go to the guarded Router. 
     Router router = new Router(getContext());
     router.attach("/register", RegistrationResource.class);
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
       Package.getPackage("org.hackystat.sensorbase.server").getImplementationVersion();
     return (version == null) ? "Development" : version; 
   }
   
   /**
    * Returns the host name associated with this server. 
    * Example: "http://localhost:9876/sensorbase/"
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
     return this.serverProperties;
   }
   
   /**
    * Returns the logger for the SensorBase. 
    * @return The logger.
    */
   @Override
   public Logger getLogger() {
     return this.logger;
   }
 }
 
