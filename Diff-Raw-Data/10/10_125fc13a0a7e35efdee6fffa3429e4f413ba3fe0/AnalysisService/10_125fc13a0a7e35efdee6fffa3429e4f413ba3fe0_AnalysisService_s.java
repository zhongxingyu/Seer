 package org.iplantc.workflow.experiment;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.iplantc.persistence.dto.workspace.Workspace;
 import org.iplantc.workflow.WorkflowException;
 import org.iplantc.workflow.core.TransformationActivity;
 import org.iplantc.workflow.dao.DaoFactory;
 import org.iplantc.workflow.dao.hibernate.HibernateDaoFactory;
 
 
 /**
  * Services for retrieving and deleting analyses.
  */
 public class AnalysisService {
 
     /**
      * The default connection timeout.
      */
     private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
 
     /**
      * Used to log debugging messages.
      */
     private static Logger LOG = Logger.getLogger(AnalysisService.class);
 
     /**
      * Used to obtain Hibernate sessions.
      */
     private SessionFactory sessionFactory;
 
     /**
      * The base URL used to connect to the Object State Management system.
      */
     private String osmBaseUrl;
 
     /**
      * The name of the bucket to use in the Object State Management system.
      */
     private String osmBucket;
 
     /**
      * The timeout in milliseconds for HTTP connections.
      */
     private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
 
     /**
      * The key used to refer to the analysis ID in the job state information stored in the OSM.
      */
     private static final String ANALYSIS_ID_KEY = "analysis_id";
 
     /**
      * Maps the job state information fields used in the Object State Management system to the corresponding fields
      * expected by the user interface.
      */
     private static final HashMap<String, FieldInfo> FIELD_MAP = new HashMap<String, FieldInfo>();
 
     static {
         FIELD_MAP.put("id", new FieldInfo("uuid", null));
         FIELD_MAP.put("name", new FieldInfo("name", null));
         FIELD_MAP.put("startdate", new DateFieldInfo("submission_date", ""));
         FIELD_MAP.put("enddate", new DateFieldInfo("completion_date", ""));
         FIELD_MAP.put("analysis_id", new FieldInfo(ANALYSIS_ID_KEY, ""));
         FIELD_MAP.put("analysis_name", new FieldInfo("analysis_name", ""));
         FIELD_MAP.put("analysis_details", new FieldInfo("analysis_details", ""));
         FIELD_MAP.put("status", new FieldInfo("status", ""));
         FIELD_MAP.put("description", new FieldInfo("description", ""));
         FIELD_MAP.put("resultfolderid", new UriFieldInfo("output_dir", ""));
     }
 
     /**
      * Retrieves all experiments associated with the given workspace ID that haven't been deleted.
      * 
      * @param workspaceId the workspace identifier.
      * @return the string representation of a JSON object containing the list of analyses.
      * @throws Exception if the analyses can't be retrieved.
      */
     public String retrieveExperimentsByWorkspaceId(Long workspaceId) throws Exception {
         Session session = sessionFactory.openSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             DaoFactory daoFactory = new HibernateDaoFactory(session);
             String result = retrieveExperiments(daoFactory, workspaceId);
             tx.commit();
             return result;
         }
         catch (Exception e) {
             if (tx != null) {
                 tx.rollback();
             }
             throw e;
         }
         finally {
             session.close();
         }
     }
 
     /**
      * Retrieves a user's experiments.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param workspaceId the user's workspace identifier.
      * @return a JSON string representing the user's results.
      */
     private String retrieveExperiments(DaoFactory daoFactory, Long workspaceId) throws Exception {
         validateWorkspaceId(daoFactory, workspaceId);
         String responseBody = sendPostRequest(buildOsmQueryUrl(), buildWorkspaceIdQuery(workspaceId));
         String result = formatExperimentQueryResult(daoFactory, responseBody);
         logQueryResult(result);
         return result;
     }
 
     /**
      * Logs the result of the analysis query if debugging is enabled.
      * 
      * @param result the result of the analysis query.
      */
     private void logQueryResult(String result) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("result: " + result);
         }
     }
 
     /**
      * Formats the result of the experiment query.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param responseBody the body of the response received from the OSM.
      * @return the formatted result.
      */
     private String formatExperimentQueryResult(DaoFactory daoFactory, String responseBody) {
         JSONObject json = (JSONObject) JSONSerializer.toJSON(responseBody);
         JSONObject result = new JSONObject();
         result.put("analyses", analysesFromObjects(daoFactory, json.getJSONArray("objects")));
         return result.toString();
     }
 
     /**
      * Converts an array of matching objects from the OSM to an array of analyses.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param objects the array of matching objects.
      * @return an array of analyses.
      */
     private JSONArray analysesFromObjects(DaoFactory daoFactory, JSONArray objects) {
         JSONArray analyses = new JSONArray();
         for (int i = 0; i < objects.size(); i++) {
             JSONObject state = objects.getJSONObject(i).getJSONObject("state");
             JSONObject analysis = analysisFromJobState(daoFactory, state);
             analyses.add(analysis);
         }
         return analyses;
     }
 
     /**
      * Converts a job state information object to an analysis information object. This is done by using the field map
      * to map fields in the job state information object to their corresponding fields in the analysis object.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param state the job state information object.
      * @return the analysis information object.
      */
     private JSONObject analysisFromJobState(DaoFactory daoFactory, JSONObject state) {
         JSONObject execution = new JSONObject();
 
         for (String key : FIELD_MAP.keySet()) {
             FieldInfo fieldInfo = FIELD_MAP.get(key);
             execution.put(key, fieldInfo.extract(state));
         }
         String analysisId = state.optString(ANALYSIS_ID_KEY, "");
         addExtraAnalysisFields(daoFactory, analysisId, execution);
         return execution;
     }
 
     /**
      * Adds extra fields from the analysis that aren't persisted in the OSM.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param analysisId the analysis identifier.
      * @param execution the analysis information object.
      */
     private void addExtraAnalysisFields(DaoFactory daoFactory, String analysisId, JSONObject execution) {
         TransformationActivity analysis = daoFactory.getTransformationActivityDao().findById(analysisId);
         if (analysis != null) {
             execution.put("analysis_details", analysis.getDescription());
             execution.put("wiki_url", analysis.getWikiurl());
         }
         else {
             LOG.error("unable to add extra analysis fields: analysis ID " + analysisId + " not found");
         }
     }
 
     /**
      * Validates a workspace ID.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param workspaceId the workspace ID to validate.
      * @throws Exception if the workspace ID is not valid.
      */
     private void validateWorkspaceId(DaoFactory daoFactory, Long workspaceId) throws Exception {
         Workspace workspace = daoFactory.getWorkspaceDao().findById(workspaceId);
         if (workspace == null) {
             throw new WorkflowException("The requested workspace doesn't exist");
         }
     }
 
     /**
      * Deletes a set of analyses.
      * 
      * @param obj a JSON object containing information about which analyses to delete.
      * @throws Exception if one or more of the analyses can't be deleted.
      */
     public void deleteExecutionSet(JSONObject obj) throws Exception {
         Session session = sessionFactory.openSession();
         Transaction tx = null;
         try {
             tx = session.beginTransaction();
             DaoFactory daoFactory = new HibernateDaoFactory(session);
             deleteExecutions(daoFactory, obj);
             tx.commit();
         }
         catch (Exception e) {
             if (tx != null) {
                 tx.rollback();
             }
             throw e;
         }
         finally {
             session.close();
         }
     }
 
     /**
      * Deletes a set of analyses.
      * 
      * @param daoFactory used to obtain data access objects.
      * @param obj a JSON object containing information about which analyses to delete.
      * @throws Exception if one or more of the analyses can't be deleted.
      */
     private void deleteExecutions(DaoFactory daoFactory, JSONObject obj) throws Exception {
         validateWorkspaceId(daoFactory, obj.getLong("workspace_id"));
         JSONArray executions = obj.getJSONArray("executions");
         for (int i = 0; i < executions.size(); i++) {
             String jobUuid = executions.getString(i);
             String queryResponse = sendPostRequest(buildOsmQueryUrl(), buildUuidQuery(jobUuid));
             JSONObject queryResult = (JSONObject) JSONSerializer.toJSON(queryResponse);
             JSONObject execInfo = extractExecutionInfo(queryResult);
             if (execInfo != null) {
                 JSONObject state = execInfo.getJSONObject("state");
                 markAsDeleted(state);
                 String objectPersistenceUuid = execInfo.getString("object_persistence_uuid");
                 sendPostRequest(buildJobStatusUpdateUrl(objectPersistenceUuid), state.toString());
             }
             else {
                 LOG.warn("attempt to delete non-existent job " + jobUuid + " ignored.");
             }
         }
     }
 
     /**
      * Marks a job state information record as deleted.
      * 
      * @param state the job state information record.
      */
     private void markAsDeleted(JSONObject state) {
         state.remove("deleted");
         state.put("deleted", true);
     }
 
     /**
      * Extracts the execution information from the given execution object.
      * 
      * @param queryResult the result that was returned by the OSM.
      * @return the execution information.
      */
     private JSONObject extractExecutionInfo(JSONObject queryResult) throws Exception {
         JSONArray resultExecutions = queryResult.getJSONArray("objects");
         JSONObject execInfo = null;
         if (!resultExecutions.isEmpty()) {
             execInfo = resultExecutions.getJSONObject(0);
         }
         return execInfo;
     }
 
     /**
      * Sends a POST request to a remote server and retrieves the response.
      * 
      * @param url the URL to send the request to.
      * @param requestBody the body of the request.
      * @return the body of the response.
      * @throws IOException if an I/O error occurs or the server returns an error code.
      */
     private String sendPostRequest(String url, String requestBody) throws IOException {
         logRequestInfo(url, requestBody);
         HttpClient client = createHttpClient();
         HttpPost post = new HttpPost(url);
         try {
             post.setEntity(new StringEntity(requestBody, "application/json", "UTF-8"));
             HttpResponse response = client.execute(post);
             String responseBody = IOUtils.toString(response.getEntity().getContent());
             int responseCode = response.getStatusLine().getStatusCode();
             if (responseCode != HttpStatus.SC_OK) {
                 throw new IOException("server returned " + responseCode + " " + responseBody);
             }
             logResponseInfo(responseBody);
             return responseBody;
         }
         finally {
             client.getConnectionManager().shutdown();
         }
     }
 
     /**
      * Creates a new HTTP client.
      * 
      * @return the new HTTP client.
      */
     private HttpClient createHttpClient() {
         HttpClient client = new DefaultHttpClient();
         client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
         if (LOG.isDebugEnabled()) {
             LOG.debug("created an HTTP client with a connection timeout of " + connectionTimeout + " milliseconds");
         }
         return client;
     }
 
     /**
      * Logs response information from the server if debugging is enabled.
      * 
      * @param responseBody the response body.
      */
     private void logResponseInfo(String responseBody) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("response body: " + responseBody);
         }
     }
 
     /**
      * Logs request information if debugging is enabled.
      * 
      * @param url the URL that the request is being sent to.
      * @param requestBody the body of the request.
      */
     private void logRequestInfo(String url, String requestBody) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("request url: " + url);
             LOG.debug("request body: " + requestBody);
         }
     }
 
     /**
      * Builds the URL used to update job status information.
      * 
      * @param objectPersistenceUuid the UUID used by the OSM to reference the status information.
      * @return the URL as a string.
      */
     private String buildJobStatusUpdateUrl(String objectPersistenceUuid) {
         return buildOsmBucketBaseUrl() + "/" + objectPersistenceUuid;
     }
 
     /**
      * Builds the URL used to query the OSM.
      * 
      * @return the URL as a string.
      */
     private String buildOsmQueryUrl() {
         return buildOsmBucketBaseUrl() + "/" + "query";
     }
 
     /**
      * Builds the base URL used to access the bucket we're using in the OSM.
      * 
      * @return the base URL as a string.
      */
     private String buildOsmBucketBaseUrl() {
         return osmBaseUrl + "/" + osmBucket;
     }
 
     /**
      * Builds the query used to find jobs by workspace ID.
      * 
      * @param workspaceId the workspace identifier.
      * @return the query as a string.
      */
     private String buildWorkspaceIdQuery(Long workspaceId) {
         JSONObject json = new JSONObject();
         json.put("state.workspace_id", String.valueOf(workspaceId));
         addDeletionCheck(json, false);
         return json.toString();
     }
 
     /**
      * Builds the query used to find jobs by analysis UUID.
      * 
      * @param uuid the analysis UUID.
      * @return the query as a string.
      */
     private String buildUuidQuery(String uuid) {
         JSONObject json = new JSONObject();
         json.put("state.uuid", uuid);
         addDeletionCheck(json, false);
         return json.toString();
     }
 
     /**
      * Adds the check for deleted analyses to a JSON object representing an OSM query.
      * 
      * @param json the JSON object representing the OSM query.
      * @param deleted true if we want to look for deleted messages.
      */
     private void addDeletionCheck(JSONObject json, boolean deleted) {
         JSONObject existenceCheck = new JSONObject();
         existenceCheck.put("$exists", deleted);
         json.put("state.deleted", existenceCheck);
     }
 
     /**
      * @param sessionFactory the Hibernate session factory.
      */
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     /**
      * Gets the base URL used to connect to the Object State Management system.
      * 
      * @return the base URL as a string.
      */
     public String getOsmBaseUrl() {
         return osmBaseUrl;
     }
 
     /**
      * Sets the base URL used to connect to the Object State Management system.
      * 
      * @param osmBaseUrl the base URL as a string.
      */
     public void setOsmBaseUrl(String osmBaseUrl) {
         this.osmBaseUrl = osmBaseUrl;
     }
 
     /**
      * Gets the name of the bucket that we're using in the OSM.
      * 
      * @return the name of the OSM bucket.
      */
     public String getOsmBucket() {
         return osmBucket;
     }
 
     /**
      * Sets the name of the bucket that we're using in the OSM.
      * 
      * @param osmBucket the name of the OSM bucket.
      */
     public void setOsmBucket(String osmBucket) {
         this.osmBucket = osmBucket;
     }
 
     /**
      * Gets the timeout for HTTP connections in milliseconds.
      * 
      * @return the timeout.
      */
     public int getConnectionTimeout() {
         return connectionTimeout;
     }
 
     /**
      * Sets the timeout for HTTP connections in milliseconds.
      * 
      * @param connectionTimeout the new connection timeout.
      */
     public void setConnectionTimeout(int connectionTimeout) {
         this.connectionTimeout = connectionTimeout;
     }
 }
 /**
  * Provides a generalized way to extract fields from the current state object provided by the OSM.
  */
 class FieldInfo {
 
     /**
      * The name of the field in the OSM.
      */
     protected String osmName;
 
     /**
      * The default value to use if the field doesn't exist in the OSM. If the default value is null then the field is
      * considered to be required and an exception is thrown if the field doesn't exist in the OSM.
      */
     protected String defaultValue;
 
     /**
      * Initializes a new FieldInfo object with the given properties.
      * 
      * @param osmName the name of the field in the OSM.
      * @param defaultValue the default value to use if the field doesn't exist in the OSM.
      */
     public FieldInfo(String osmName, String defaultValue) {
         this.osmName = osmName;
         this.defaultValue = defaultValue;
     }
 
     /**
      * Extracts the field value from the current state object returned by the OSM.
      * 
      * @param state the current state object.
      * @return either the extracted value or the default value.
      */
     public String extract(JSONObject state) {
         String value = state.optString(osmName, defaultValue);
         if (value == null) {
             throw new WorkflowException("missing required field: " + osmName);
         }
         return value;
     }
 }
 
 /**
  * Provides a way to extract the values of fields containing URI information from the current state object provided by
  * the OSM.
  */
 class UriFieldInfo extends FieldInfo {
 
     /**
      * @param osmName the name of the field in the OSM.
      * @param defaultValue the default value to use if the field doesn't exist in the OSM.
      */
     public UriFieldInfo(String osmName, String defaultValue) {
         super(osmName, defaultValue);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String extract(JSONObject state) {
         String uri = super.extract(state);
         return StringUtils.isBlank(uri) ? "" : extractPathFromUri(uri);
     }
 
     /**
      * Extracts the path from the given URI.
      * 
      * @param uri the string representation of the URI.
      * @return the path.
      */
     private String extractPathFromUri(String uri) {
         String path = null;
         try {
             path = new URI(uri).getPath();
         }
         catch (URISyntaxException e) {
             throw new WorkflowException("Malformed url received for \"" + osmName + "\": " + uri, e);
         }
         return path;
     }
 }
 
 /**
  * Provides a way to extract the values of fields containing date information from the current state object provided
  * by the OSM.
  */
 class DateFieldInfo extends FieldInfo {
 
     /**
      * The regular expression to use when searching for time zone offsets.
      */
     private static final Pattern TIMEZONE_OFFSET_PATTERN = Pattern.compile("([+-]\\d{2})(\\d{2})");
 
     /**
      * @param osmName the name of the field in the OSM.
      * @param defaultValue the default value to use if the field doesn't exist in the OSM.
      */
     public DateFieldInfo(String osmName, String defaultValue) {
         super(osmName, defaultValue);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String extract(JSONObject state) {
         String dateString = super.extract(state);
        return StringUtils.isBlank(dateString) ? "" : convertDateString(dateString);
     }
 
     /**
      * Converts a date string from the format used by the JEX to the format used internally by Java (that is, the
      * number of milliseconds since the Unix epoch).
      *  
      * @param dateString the date string to convert.
      * @return the converted date string.
      */
     private String convertDateString(String dateString) {
         SimpleDateFormat parser = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z");
         try {
             return String.valueOf(parser.parse(fixTimeZoneOffset(dateString)).getTime());
         }
         catch (ParseException e) {
             throw new WorkflowException("unrecognized timestamp format: " + dateString, e);
         }
     }
 
     /**
      * Converts the time zone offset from the format that is used by the JEX to the format that is expected by
      * SimpleDateFormat.  There wasn't an easy way to fix the code that parses the time zone offset without
      * completely reimplementing SimpleDateFormat, so this will have to do for now.
      * 
      * @param dateString the string representing the date we're trying to parse.
      * @return the updated date string.
      */
     private String fixTimeZoneOffset(String dateString) {
         Matcher matcher = TIMEZONE_OFFSET_PATTERN.matcher(dateString);
         StringBuffer buffer = new StringBuffer();
         while (matcher.find()) {
             matcher.appendReplacement(buffer, matcher.group(1) + ":" + matcher.group(2));
         }
         matcher.appendTail(buffer);
         return buffer.toString();
     }
 }
