 package org.hackystat.dailyprojectdata.client;
 
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBContext;
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
   /** DevTime JAXBContext */
   private JAXBContext devTimeJAXB;
   /** FileMetric JAXBContext */
   private JAXBContext fileMetricJAXB;
   /** UnitTest JAXBContext */
   private JAXBContext unitTestJAXB;
   /** CodeIssue JAXBContext */
   private JAXBContext codeIssueJAXB;
   /** CodeIssue JAXBContext */
   private JAXBContext coverageJAXB;
   /** Commit JAXBContext */
   private JAXBContext commitJAXB;
   /** Build JAXB Context. */
   private JAXBContext buildJAXB;
   /** Complexity JAXB Context. */
   private JAXBContext complexityJAXB;
   /** Coupling JAXB Context. */
   private JAXBContext couplingJAXB;
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
       devTime = (DevTimeDailyProjectData) this.uriCache.getFromGroup(uri, "devtime");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "devtime", devTime);
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
       unitDPD = (UnitTestDailyProjectData) this.uriCache.getFromGroup(uri, "unittest");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "unittest", unitDPD);
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
       fileMetric = (FileMetricDailyProjectData) this.uriCache.getFromGroup(uri, "filemetric");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "filemetric", fileMetric);
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
       complexity = (ComplexityDailyProjectData) this.uriCache.getFromGroup(uri, "complexity");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "complexity", complexity);
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
       coupling = (CouplingDailyProjectData) this.uriCache.getFromGroup(uri, "coupling");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "coupling", coupling);
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
       codeIssue = (CodeIssueDailyProjectData) this.uriCache.getFromGroup(uri, "codeissue");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "codeissue", codeIssue);
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
       coverage = (CoverageDailyProjectData) this.uriCache.getFromGroup(uri, "coverage");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "coverage", coverage);
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
       commit = (CommitDailyProjectData) this.uriCache.getFromGroup(uri, "commit");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
        this.uriCache.putInGroup(uri, "coverage", commit);
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
       build = (BuildDailyProjectData) this.uriCache.getFromGroup(uri, "build");
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
       if (this.isCacheEnabled && !isToday(timestamp)) {
         this.uriCache.putInGroup(uri, "build", build);
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
   public synchronized void clearCache() {
     if (this.uriCache != null) {
       this.uriCache.clearAll();
     }
   }
 
   /**
    * Removes all cache entries associated with the passed dpdType, such as "build", "commit",
    * "unittest", "codeissue", "filemetric", etc.
    * If this DPDClient does not have caching enabled, then this has no effect.
    * 
    * @param dpdType The DPD type.
    */
   public synchronized void clearCache(String dpdType) {
     if (this.uriCache != null) {
       this.uriCache.clearGroup(dpdType);
     }
   }
 
   /**
    * Returns a newly generated Set containing the Uris in the cache for this DPDCclient of the given
    * dpdType.
    * If this DPDClient does not have caching enabled, then an empty set is returned.
    * 
    * @param dpdType The dpdtype of interest.
    * @return The keys.
    */
   public synchronized Set<String> getCacheKeys(String dpdType) {
     Set<String> cacheKeys = new HashSet<String>();
     if (this.uriCache != null) {
       for (Object key : this.uriCache.getGroupKeys(dpdType)) {
         cacheKeys.add((String) key);
       }
     }
     return cacheKeys;
   }
 
   /**
    * Removes the DPD instance of type dpdType with timestamp indicated by tstamp. Uses a somewhat
    * slow, expensive, and heuristic approach of simply checking to see if the tstamp string appears
    * anywhere in the URI. This could potentially result in more than one cached dpd instance being
    * deleted, although that should not happen in practice.
    * 
    * If this DPDClient does not have caching enabled, then this has no effect.
    * 
    * @param dpdType The dpd type, such as "filemetric", "codeissue", etc.
    * @param tstamp The tstamp passed when creating this dpd instance.
    * @return True if the dpd instance was found and deleted, false if not found.
    */
   public synchronized boolean clearCache(String dpdType, String tstamp) {
     boolean success = false;
     if (this.uriCache != null) {
       for (Serializable key : this.uriCache.getGroupKeys(dpdType)) {
         if (((String) key).contains(tstamp)) {
           this.uriCache.removeFromGroup(key, dpdType);
           success = true;
           // Don't return from the loop right away, because it is possible we haven't
           // yet deleted the right dpd instance from the cache. Unfortunately this makes
           // this loop a bit more expensive. Fortunately, caching clearing doesn't have to be cheap.
         }
       }
     }
     return success;
   }
 
   /**
    * Returns the number of cached entries of the given DPD type, such as "filemetric", "build", etc.
    * If this DPDClient does not have caching enabled, then returns 0.
    * 
    * @param dpdType The type of DPD instance.
    * @return The number of entries in the cache of that type.
    */
   public synchronized int cacheSize(String dpdType) {
     int size = 0;
     if (this.uriCache != null) {
       size = this.uriCache.getGroupSize(dpdType);
     }
     return size;
   }
 
   /**
    * Clears the SensorData cache associated with the specified user on the DailyProjectData server
    * to which this DailyProjectDataClient instance is connected.
    * 
    * @param user The user whose SensorData cache on the DPD server is to be cleared.
    * @return True if the command succeeded.
    * @throws DailyProjectDataClientException If problems occur.
    */
   public synchronized boolean clearServerCache(String user) throws DailyProjectDataClientException {
     Date startTime = new Date();
     String uri = "cache/" + user;
     Response response = makeRequest(Method.DELETE, uri, null);
     if (!response.getStatus().isSuccess()) {
       logElapsedTime(uri, startTime);
       System.err.println("cache/" + user);
       throw new DailyProjectDataClientException(response.getStatus());
     }
     logElapsedTime(uri, startTime);
     return true;
   }
 
   /**
    * Returns true if the passed timestamp indicates today's date.
    * 
    * @param timestamp The timestamp of interest.
    * @return True if it's today.
    */
   private boolean isToday(XMLGregorianCalendar timestamp) {
     XMLGregorianCalendar today = Tstamp.makeTimestamp();
     return (today.getYear() == timestamp.getYear()) && (today.getMonth() == timestamp.getMonth())
         && (today.getDay() == timestamp.getDay());
   }
 }
