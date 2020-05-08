 package org.hackystat.dailyprojectdata.client;
 
 import java.io.StringReader;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.dailyprojectdata.resource.build.jaxb.BuildDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.codeissue.jaxb.CodeIssueDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.commit.jaxb.CommitDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.complexity.jaxb.ComplexityDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.coupling.jaxb.CouplingDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.coverage.jaxb.CoverageDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.devtime.jaxb.DevTimeDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.filemetric.jaxb.FileMetricDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.issue.jaxb.IssueDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.unittest.jaxb.UnitTestDailyProjectData;
 import org.hackystat.utilities.logger.HackystatLogger;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.utilities.uricache.UriCache;
 import org.restlet.Client;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Preference;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 
 /**
  * Provides a client to support access to the DailyProjectData service.
  * 
  * @author Philip Johnson
  */
 public class DailyProjectDataClient {
 
   /** Holds the userEmail to be associated with this client. */
   private String userEmail;
   /** Holds the password to be associated with this client. */
   private String password;
   /**
    * The DailyProjectData host, such as "http://localhost:9877/dailyprojectdata".
    */
   private String dailyProjectDataHost;
   /** The Restlet Client instance used to communicate with the server. */
   private Client client;
   /** DevTime JAXBContext. */
   private JAXBContext devTimeJAXB;
   /** FileMetric JAXBContext. */
   private JAXBContext fileMetricJAXB;
   /** UnitTest JAXBContext. */
   private JAXBContext unitTestJAXB;
   /** CodeIssue JAXBContext. */
   private JAXBContext codeIssueJAXB;
   /** CodeIssue JAXBContext. */
   private JAXBContext coverageJAXB;
   /** Commit JAXBContext. */
   private JAXBContext commitJAXB;
   /** Build JAXB Context. */
   private JAXBContext buildJAXB;
   /** Complexity JAXB Context. */
   private JAXBContext complexityJAXB;
   /** Coupling JAXB Context. */
   private JAXBContext couplingJAXB;
   /** Issue JAXB Context. */
   private JAXBContext issueJAXB;
   /** The http authentication approach. */
   private ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
   /** The preferred representation type. */
   private Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
   /** To facilitate debugging of problems using this system. */
   private boolean isTraceEnabled = false;
   /** For logging. */
   private Logger logger;
   /** The System property key used to retrieve the default timeout value in milliseconds. */
   public static final String DAILYPROJECTDATACLIENT_TIMEOUT_KEY = "dailyprojectdataclient.timeout";
 
 
   /** An associated UriCache to improve responsiveness. */
   private UriCache uriCache;
 
   /** Indicates whether or not cache is enabled. */
   private boolean isCacheEnabled = false;
 
   /**
    * Initializes a new DailyProjectDataClient, given the host, userEmail, and password. Note that
    * the userEmail and password refer to the underlying SensorBase client associated with this
    * DailyProjectData service. This service does not keep its own independent set of userEmails and
    * passwords. Authentication is not actually performed in this constructor. Use the authenticate()
    * method to explicitly check the authentication credentials.
    * 
    * @param host The host, such as 'http://localhost:9877/dailyprojectdata'.
    * @param email The user's email used for authentication.
    * @param password The password used for authentication.
    */
   public DailyProjectDataClient(String host, String email, String password) {
     this.logger = HackystatLogger.getLogger(
         "org.hackystat.dailyprojectdata.client.DailyProjectDataClient", "dailyprojectdata", false);
     this.logger.info("Instantiating client for: " + host + " " + email);
     validateArg(host);
     validateArg(email);
     validateArg(password);
     this.userEmail = email;
     this.password = password;
     this.dailyProjectDataHost = host;
     if (!this.dailyProjectDataHost.endsWith("/")) {
       this.dailyProjectDataHost = this.dailyProjectDataHost + "/";
     }
     if (this.isTraceEnabled) {
       System.out.println("DailyProjectDataClient Tracing: INITIALIZE " + "host='" + host
           + "', email='" + email + "', password='" + password + "'");
     }
     this.client = new Client(Protocol.HTTP);
     setTimeoutFromSystemProperty();
     try {
       this.devTimeJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.devtime.jaxb.ObjectFactory.class);
       this.unitTestJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.unittest.jaxb.ObjectFactory.class);
       this.fileMetricJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.filemetric.jaxb.ObjectFactory.class);
       this.codeIssueJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.codeissue.jaxb.ObjectFactory.class);
       this.coverageJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.coverage.jaxb.ObjectFactory.class);
       this.buildJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.build.jaxb.ObjectFactory.class);
       this.commitJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.commit.jaxb.ObjectFactory.class);
       this.complexityJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.complexity.jaxb.ObjectFactory.class);
       this.couplingJAXB = JAXBContext
           .newInstance(org.hackystat.dailyprojectdata.resource.coupling.jaxb.ObjectFactory.class);
       this.issueJAXB = JAXBContext
       .newInstance(org.hackystat.dailyprojectdata.resource.issue.jaxb.ObjectFactory.class);
     }
     catch (Exception e) {
       throw new RuntimeException("Couldn't create JAXB context instances.", e);
     }
   }
 
   /**
    * Throws an unchecked illegal argument exception if the arg is null or empty.
    * 
    * @param arg The String that must be non-null and non-empty.
    */
   private void validateArg(String arg) {
     if ((arg == null) || ("".equals(arg))) {
       throw new IllegalArgumentException(arg + " cannot be null or the empty string.");
     }
   }
 
   /**
    * Sets the timeout value for this client.
    * 
    * @param milliseconds The number of milliseconds to wait before timing out.
    */
   public final synchronized void setTimeout(int milliseconds) {
     client.getContext().getParameters().removeAll("connectTimeout");
     client.getContext().getParameters().add("connectTimeout", String.valueOf(milliseconds));
     // For the Apache Commons client.
     client.getContext().getParameters().removeAll("readTimeout");
     client.getContext().getParameters().add("readTimeout", String.valueOf(milliseconds));
     client.getContext().getParameters().removeAll("connectionManagerTimeout");
     client.getContext().getParameters().add("connectionManagerTimeout",
         String.valueOf(milliseconds));
   }
 
   /**
    * Sets the timeout for this client if the system property 
    * dailyprojectdataclient.timeout is set
    * and if it can be parsed to an integer.
    */
   private void setTimeoutFromSystemProperty() {
     String systemTimeout = System.getProperty(DAILYPROJECTDATACLIENT_TIMEOUT_KEY);
     // if not set, then return immediately.
     if (systemTimeout == null) {
       return;
     }
     // systemTimeout has a value, so set it if we can.
     try {
       int timeout = Integer.parseInt(systemTimeout);
       setTimeout(timeout);
       this.logger.info("DdpClient timeout set to: " + timeout + " milliseconds");
     }
     catch (Exception e) {
       this.logger.warning("dailyprojectdataclient.timeout has non integer value: " + systemTimeout);
     }
   }
 
   /**
    * Does the housekeeping for making HTTP requests to the SensorBase by a test or admin user.
    * 
    * @param method The type of Method.
    * @param requestString A string, such as "users". No preceding slash.
    * @param entity The representation to be sent with the request, or null if not needed.
    * @return The Response instance returned from the server.
    */
   private Response makeRequest(Method method, String requestString, Representation entity) {
     Reference reference = new Reference(this.dailyProjectDataHost + requestString);
     Request request = (entity == null) ? new Request(method, reference) : new Request(method,
         reference, entity);
     request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia);
     ChallengeResponse authentication = new ChallengeResponse(scheme, this.userEmail, this.password);
     request.setChallengeResponse(authentication);
     if (this.isTraceEnabled) {
       System.out.println("DailyProjectDataClient Tracing: " + method + " " + reference);
       if (entity != null) {
         try {
           System.out.println(entity.getText());
         }
         catch (Exception e) {
           System.out.println("  Problems with getText() on entity.");
         }
       }
     }
     Response response = this.client.handle(request);
     if (this.isTraceEnabled) {
       Status status = response.getStatus();
       System.out.println("  => " + status.getCode() + " " + status.getDescription());
     }
     return response;
   }
 
   /**
    * Takes a String encoding of a DevTimeDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a DevTimeDailyProjectData.
    * @return The corresponding DevTimeDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private DevTimeDailyProjectData makeDevTimeDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.devTimeJAXB.createUnmarshaller();
     return (DevTimeDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Authenticates this user and password with this DailyProjectData service, throwing a
    * DailyProjectDataException if the user and password associated with this instance are not valid
    * credentials. Note that authentication is performed by checking these credentials with the
    * SensorBase; this service does not keep its own independent set of usernames and passwords.
    * 
    * @return This DailyProjectDataClient instance.
    * @throws DailyProjectDataClientException If authentication is not successful.
    */
   public synchronized DailyProjectDataClient authenticate() throws DailyProjectDataClientException {
     // Performs authentication by invoking ping with user and password as form
     // params.
     String uri = "ping?user=" + this.userEmail + "&password=" + this.password;
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       throw new DailyProjectDataClientException(response.getStatus());
     }
     String responseString;
     try {
       responseString = response.getEntity().getText();
     }
     catch (Exception e) {
       throw new DailyProjectDataClientException("Bad response", e);
     }
     if (!"DailyProjectData authenticated".equals(responseString)) {
       throw new DailyProjectDataClientException("Authentication failed");
     }
     return this;
   }
 
   /**
    * Returns a DevTimeDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
    * @return A DevTimeDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized DevTimeDailyProjectData getDevTime(String user, String project,
       XMLGregorianCalendar timestamp) throws DailyProjectDataClientException {
     Date startTime = new Date();
     DevTimeDailyProjectData devTime;
     String uri = "devtime/" + user + "/" + project + "/" + timestamp;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       devTime = (DevTimeDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (devTime != null) {
         return devTime;
       }
     }
     // Otherwise get it from the DPD service.
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       devTime = makeDevTimeDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, devTime);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return devTime;
   }
 
   /**
    * Returns a UnitTestDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
    * @return A DevTimeDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized UnitTestDailyProjectData getUnitTest(String user, String project,
       XMLGregorianCalendar timestamp) throws DailyProjectDataClientException {
     Date startTime = new Date();
     String uri = "unittest/" + user + "/" + project + "/" + timestamp;
     UnitTestDailyProjectData unitDPD;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       unitDPD = (UnitTestDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (unitDPD != null) {
         return unitDPD;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       unitDPD = makeUnitTestDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, unitDPD);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return unitDPD;
   }
 
   /**
    * Takes a String encoding of a UnitTestDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a UnitTestDailyProjectData.
    * @return The corresponding UnitTestDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private UnitTestDailyProjectData makeUnitTestDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.unitTestJAXB.createUnmarshaller();
     return (UnitTestDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns true if the passed host is a DailyProjectData host.
    * 
    * @param host The URL of a DailyProjectData host, "http://localhost:9876/dailyprojectdata".
    * @return True if this URL responds as a DailyProjectData host.
    */
   public static boolean isHost(String host) {
     // All sensorbase hosts use the HTTP protocol.
     if (!host.startsWith("http://")) {
       return false;
     }
     // Create the host/register URL.
     try {
       String registerUri = host.endsWith("/") ? host + "ping" : host + "/ping";
       Request request = new Request();
       request.setResourceRef(registerUri);
       request.setMethod(Method.GET);
       Client client = new Client(Protocol.HTTP);
       Response response = client.handle(request);
       String pingText = response.getEntity().getText();
       return (response.getStatus().isSuccess() && "DailyProjectData".equals(pingText));
     }
     catch (Exception e) {
       return false;
     }
   }
 
   /**
    * Returns a FileMetricDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
    * @param sizeMetric The size metric to be retrieved.
    * @return A FileMetricDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized FileMetricDailyProjectData getFileMetric(String user, String project,
       XMLGregorianCalendar timestamp, String sizeMetric) throws DailyProjectDataClientException {
     return getFileMetric(user, project, timestamp, sizeMetric, null);
   }
 
   /**
    * Returns a FileMetricDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
    * @param sizeMetric The size metric to be retrieved.
    * @param tool The tool whose data is to be retrieved, or null for no tool.
    * @return A FileMetricDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized FileMetricDailyProjectData getFileMetric(String user, String project,
       XMLGregorianCalendar timestamp, String sizeMetric, String tool)
       throws DailyProjectDataClientException {
     Date startTime = new Date();
     FileMetricDailyProjectData fileMetric;
     String param = (tool == null) ? "" : "?Tool=" + tool;
     String uri = "filemetric/" + user + "/" + project + "/" + timestamp + "/" + sizeMetric + param;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       fileMetric = (FileMetricDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (fileMetric != null) {
         return fileMetric;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       System.err.println("filemetric/" + user + "/" + project + "/" + timestamp + "/" + sizeMetric);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       fileMetric = makeFileMetricDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, fileMetric);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return fileMetric;
   }
 
   /**
    * Returns a ComplexityDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
    * @param type The type of complexity, such as "Cyclometric".
    * @param tool The tool that provided the complexity data, such as "JavaNCSS".
    * @return A ComplexityDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized ComplexityDailyProjectData getComplexity(String user, String project,
       XMLGregorianCalendar timestamp, String type, String tool)
       throws DailyProjectDataClientException {
     Date startTime = new Date();
     ComplexityDailyProjectData complexity;
     String uri = "complexity/" + user + "/" + project + "/" + timestamp + "/" + type + "?Tool="
         + tool;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       complexity = (ComplexityDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (complexity != null) {
         return complexity;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       System.err.println("complexity/" + user + "/" + project + "/" + timestamp + "/" + type);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       complexity = makeComplexityDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, complexity);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return complexity;
   }
 
   /**
    * Returns a CouplingDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
    * @param type The type of coupling, such as "class".
    * @param tool The tool that provided the coupling data, such as "DependencyFinder".
    * @return A CouplingDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized CouplingDailyProjectData getCoupling(String user, String project,
       XMLGregorianCalendar timestamp, String type, String tool)
       throws DailyProjectDataClientException {
     Date startTime = new Date();
     CouplingDailyProjectData coupling;
     String uri = "coupling/" + user + "/" + project + "/" + timestamp + "/" + type + "?Tool="
         + tool;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       coupling = (CouplingDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (coupling != null) {
         return coupling;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       System.err.println("coupling/" + user + "/" + project + "/" + timestamp + "/" + type);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       coupling = makeCouplingDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, coupling);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return coupling;
   }
 
   /**
    * Takes a String encoding of a FileMetricDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a DevTimeDailyProjectData.
    * @return The corresponding DevTimeDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private FileMetricDailyProjectData makeFileMetricDailyProjectData(String xmlString)
       throws Exception {
     Unmarshaller unmarshaller = this.fileMetricJAXB.createUnmarshaller();
     return (FileMetricDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a ComplexityDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a ComplexityDailyProjectData.
    * @return The corresponding ComplexityDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private ComplexityDailyProjectData makeComplexityDailyProjectData(String xmlString)
       throws Exception {
     Unmarshaller unmarshaller = this.complexityJAXB.createUnmarshaller();
     return (ComplexityDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a CouplingDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a CouplingDailyProjectData.
    * @return The corresponding CouplingDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private CouplingDailyProjectData makeCouplingDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.couplingJAXB.createUnmarshaller();
     return (CouplingDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns a CodeIssueDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of CodeIssue data.
    * @param tool An optional tool for matching CodeIssue data.
    * @param type An optional type for matching CodeIssue types.
    * @return A CodeIssueDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized CodeIssueDailyProjectData getCodeIssue(String user, String project,
       XMLGregorianCalendar timestamp, String tool, String type)
       throws DailyProjectDataClientException {
     CodeIssueDailyProjectData codeIssue;
     Date startTime = new Date();
     StringBuilder requestStringBuilder = new StringBuilder("codeissue/");
     requestStringBuilder.append(user);
     requestStringBuilder.append("/");
     requestStringBuilder.append(project);
     requestStringBuilder.append("/");
     requestStringBuilder.append(timestamp);
 
     boolean questionMarkAppended = false;
     if (tool != null) {
       requestStringBuilder.append("?");
       requestStringBuilder.append("Tool=");
       requestStringBuilder.append(tool);
       questionMarkAppended = true;
     }
     if (type != null) {
       if (questionMarkAppended) {
         requestStringBuilder.append("&");
       }
       else {
         requestStringBuilder.append("?");
       }
       requestStringBuilder.append("Type=");
       requestStringBuilder.append(type);
     }
     String uri = requestStringBuilder.toString();
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       codeIssue = (CodeIssueDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (codeIssue != null) {
         return codeIssue;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
 
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       codeIssue = makeCodeIssueDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, codeIssue);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return codeIssue;
   }
 
   /**
    * Returns a CoverageDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of CodeIssue data.
    * @param granularity the granularity of the coverage data.
    * @return A CoverageDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized CoverageDailyProjectData getCoverage(String user, String project,
       XMLGregorianCalendar timestamp, String granularity) throws DailyProjectDataClientException {
     Date startTime = new Date();
     StringBuilder requestStringBuilder = new StringBuilder("coverage/");
     requestStringBuilder.append(user);
     requestStringBuilder.append("/");
     requestStringBuilder.append(project);
     requestStringBuilder.append("/");
     requestStringBuilder.append(timestamp);
 
     if (granularity != null) {
       requestStringBuilder.append("/");
       requestStringBuilder.append(granularity);
     }
 
     String uri = requestStringBuilder.toString();
     CoverageDailyProjectData coverage;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       coverage = (CoverageDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (coverage != null) {
         return coverage;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
 
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       coverage = makeCoverageDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, coverage);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return coverage;
   }
 
   /**
    * Takes a String encoding of a CoverageDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a CoverageDailyProjectData.
    * @return The corresponding CoverageDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private CoverageDailyProjectData makeCoverageDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.coverageJAXB.createUnmarshaller();
     return (CoverageDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns a CommitDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of CodeIssue data.
    * @return A CommitDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized CommitDailyProjectData getCommit(String user, String project,
       XMLGregorianCalendar timestamp) throws DailyProjectDataClientException {
     Date startTime = new Date();
     StringBuilder requestStringBuilder = new StringBuilder("commit/");
     requestStringBuilder.append(user);
     requestStringBuilder.append("/");
     requestStringBuilder.append(project);
     requestStringBuilder.append("/");
     requestStringBuilder.append(timestamp);
 
     String uri = requestStringBuilder.toString();
     CommitDailyProjectData commit;
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       commit = (CommitDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (commit != null) {
         return commit;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
 
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       commit = makeCommitDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       // Since CM sensors typically run on yesterday's data, don't cache unless 2 days or older.
       if (this.isCacheEnabled && !Tstamp.isYesterdayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, commit);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return commit;
   }
 
   /**
    * Takes a String encoding of a CommitDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a CommitDailyProjectData.
    * @return The corresponding CommitsDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private CommitDailyProjectData makeCommitDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.commitJAXB.createUnmarshaller();
     return (CommitDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a CodeIssueDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a CodeIssueDailyProjectData.
    * @return The corresponding CodeIssueDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private CodeIssueDailyProjectData makeCodeIssueDailyProjectData(String xmlString)
       throws Exception {
     Unmarshaller unmarshaller = this.codeIssueJAXB.createUnmarshaller();
     return (CodeIssueDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns a BuildDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of build data.
    * @param type The type of build to retrieve data for.
    * @return A BuildDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized BuildDailyProjectData getBuild(String user, String project,
       XMLGregorianCalendar timestamp, String type) throws DailyProjectDataClientException {
     Date startTime = new Date();
 
     StringBuilder requestStringBuilder = new StringBuilder("build/");
     requestStringBuilder.append(user);
     requestStringBuilder.append("/");
     requestStringBuilder.append(project);
     requestStringBuilder.append("/");
     requestStringBuilder.append(timestamp);
 
     if (type != null) {
       requestStringBuilder.append("?");
       requestStringBuilder.append("Type=");
       requestStringBuilder.append(type);
     }
 
     BuildDailyProjectData build;
     String uri = requestStringBuilder.toString();
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       build = (BuildDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (build != null) {
         return build;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
 
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       build = makeBuildDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, build);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return build;
   }
 
   /**
    * Returns a BuildDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * 
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of build data.
    * @return A BuildDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized BuildDailyProjectData getBuild(String user, String project,
       XMLGregorianCalendar timestamp) throws DailyProjectDataClientException {
     return getBuild(user, project, timestamp, null);
   }
 
   /**
    * Takes a String encoding of a BuildDailyProjectData in XML format and converts it.
    * 
    * @param xmlString The XML string representing a DevTimeDailyProjectData.
    * @return The corresponding BuildDailyProjectData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private BuildDailyProjectData makeBuildDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.buildJAXB.createUnmarshaller();
     return (BuildDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Returns a IssueDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of build data.
    * @param status The status of the issue, open or closed, 
   *          or a specified status such as "Accepted" or "Fixed"
    * @return A IssueDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized IssueDailyProjectData getIssue(String user, String project,
       XMLGregorianCalendar timestamp, String status) throws DailyProjectDataClientException {
     Date startTime = new Date();
 
     StringBuilder requestStringBuilder = new StringBuilder("issue/");
     requestStringBuilder.append(user);
     requestStringBuilder.append("/");
     requestStringBuilder.append(project);
     requestStringBuilder.append("/");
     requestStringBuilder.append(timestamp);
 
     if (status != null) {
       requestStringBuilder.append("?");
       requestStringBuilder.append("Status=");
       requestStringBuilder.append(status);
     }
     
     IssueDailyProjectData issue;
     String uri = requestStringBuilder.toString();
     // Check the cache, and return the instance from it if available.
     if (this.isCacheEnabled) {
       issue = (IssueDailyProjectData) this.uriCache.getFromGroup(uri, user + project);
       if (issue != null) {
         return issue;
       }
     }
     Response response = makeRequest(Method.GET, uri, null);
 
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       issue = makeIssueDailyProjectData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled && !Tstamp.isTodayOrLater(timestamp)) {
         this.uriCache.putInGroup(uri, user + project, issue);
       }
     }
     catch (Exception e) {
       logElapsedTime(uri, startTime, e);
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     logElapsedTime(uri, startTime);
     return issue;
   }
 
   /**
    * Returns a IssueDailyProjectData instance from this server, or throws a DailyProjectData
    * exception if problems occurred.
    * @param user The user that owns the project.
    * @param project The project owned by user.
    * @param timestamp The Timestamp indicating the start of the 24 hour period of build data.
    * @return A IssueDailyProjectData instance.
    * @throws DailyProjectDataClientException If the credentials associated with this instance are
    *         not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    *         of the supplied user, password, or timestamp is not valid.
    */
   public synchronized IssueDailyProjectData getIssue(String user, String project,
       XMLGregorianCalendar timestamp) throws DailyProjectDataClientException {
     return getIssue(user, project, timestamp, null);
   }
   /**
    * Takes a String encoding of a IssueDailyProjectData in XML format and converts it.
    * 
    * @param xmlData The XML string representing a DevTimeDailyProjectData.
    * @return The corresponding IssueDailyProjectData instance.
    * @throws JAXBException If problems occur during unmarshalling.
    */
   private IssueDailyProjectData makeIssueDailyProjectData(String xmlData) throws JAXBException {
     Unmarshaller unmarshaller = this.issueJAXB.createUnmarshaller();
     return (IssueDailyProjectData) unmarshaller.unmarshal(new StringReader(xmlData));
   }
 
   /**
    * Logs info to the logger about the elapsed time for this request.
    * 
    * @param uri The URI requested.
    * @param startTime The startTime of the call.
    * @param e The exception thrown, or null if no exception.
    */
   private void logElapsedTime(String uri, Date startTime, Exception e) {
     long millis = (new Date()).getTime() - startTime.getTime();
     String msg = millis + " millis: " + uri + ((e == null) ? "" : " " + e);
     this.logger.info(msg);
   }
 
   /**
    * Logs info to the logger about the elapsed time for this request.
    * 
    * @param uri The URI requested.
    * @param startTime The startTime of the call.
    */
   private void logElapsedTime(String uri, Date startTime) {
     logElapsedTime(uri, startTime, null);
   }
 
   /**
    * Enables caching in this client. We do not cache DPDs for the current day, since not all data
    * might be have been sent yet.
    * 
    * @param cacheName The name of the cache.
    * @param subDir The subdirectory in which the cache backend store is saved.
    * @param maxLife The default expiration time for cached objects in days.
    * @param capacity The maximum number of instances to be held in-memory.
    */
   public synchronized void enableCaching(String cacheName, String subDir, Double maxLife,
       Long capacity) {
     this.uriCache = new UriCache(cacheName, subDir, maxLife, capacity);
     this.isCacheEnabled = true;
   }
 
   /**
    * Delete all entries from the local cache of DailyProjectData instances associated with this
    * DailyProjectDataClient instance. All DPD-specific caches are cleared. If this DPDClient
    * instance does not have caching enabled, then this method has no effect.
    */
   public synchronized void clearLocalCache() {
     if (this.uriCache != null) {
       this.uriCache.clearAll();
     }
   }
 
   /**
    * Delete all cache entries associated with the specified project and its owner. 
    * If this DPDClient does not have caching enabled, then this has no effect.
    * 
    * @param user The user. 
    * @param project The project. 
    */
   public synchronized void clearLocalCache(String user, String project) {
     if (this.uriCache != null) {
       this.uriCache.clearGroup(user + project);
     }
   }
 
   /**
    * Returns the number of cached entries for the given project and its owner. 
    * If this DPDClient does not have caching enabled, then returns 0.
    * 
    * @param user The owner of this project. 
    * @param project The project.
    * @return The number of entries in the cache for that project. 
    */
   public synchronized int localCacheSize(String user, String project) {
     int size = 0;
     if (this.uriCache != null) {
       size = this.uriCache.getGroupSize(user + project);
     }
     return size;
   }
 
   /**
    * Clears the (front side) DPD cache associated with this user on the DailyProjectData server
    * to which this DailyProjectDataClient instance is connected.
    * 
    * @return True if the command succeeded.
    * @throws DailyProjectDataClientException If problems occur.
    */
   public synchronized boolean clearServerCache() throws DailyProjectDataClientException {
     Date startTime = new Date();
     String uri = "cache";
     Response response = makeRequest(Method.DELETE, uri, null);
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     logElapsedTime(uri, startTime);
     return true;
   }
   
   /**
    * Clears the (front side) DPD cache entries associated with the specified project and its owner 
    * on the DailyProjectData server to which this DailyProjectDataClient instance is connected.
    * 
    * @param owner The owner of the project whose entries are to be cleared.
    * @param project The project DPDs to be cleared on the server. 
    * @return True if the command succeeded.
    * @throws DailyProjectDataClientException If problems occur.
    */
   public synchronized boolean clearServerCache(String owner, String project) 
   throws DailyProjectDataClientException {
     Date startTime = new Date();
     String uri = String.format("cache/%s/%s", owner, project);
     Response response = makeRequest(Method.DELETE, uri, null);
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     logElapsedTime(uri, startTime);
     return true;
   }
 
 
 }
