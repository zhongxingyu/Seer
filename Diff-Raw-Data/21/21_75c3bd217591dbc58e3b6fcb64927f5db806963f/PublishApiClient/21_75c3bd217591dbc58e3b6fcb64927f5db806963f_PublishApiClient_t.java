 package com.lulu.publish.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import javax.net.ssl.HttpsURLConnection;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.BasicHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.lulu.publish.model.Conversion;
 import com.lulu.publish.model.ConversionManifest;
 import com.lulu.publish.model.FileContext;
 import com.lulu.publish.model.Project;
 import com.lulu.publish.response.ApiResponse;
 import com.lulu.publish.response.AuthenticationResponse;
 import com.lulu.publish.response.BaseCostResponse;
 import com.lulu.publish.response.CreateResponse;
 import com.lulu.publish.response.ErrorResponse;
 import com.lulu.publish.response.ListResponse;
 import com.lulu.publish.response.ProjectResponse;
 import com.lulu.publish.response.UploadResponse;
 import com.lulu.publish.response.UploadTokenResponse;
 import com.lulu.publish.util.JsonMapper;
 import com.lulu.publish.util.JsonMapperException;
 import com.lulu.publish.util.StreamUtils;
 import com.lulu.publish.web.ProtocolRegistrar;
 
 /**
  * Provides a client interface to the Lulu publish API.
  */
 public class PublishApiClient {
 
     private static final Logger LOG = LoggerFactory.getLogger(PublishApiClient.class); // NOPMD
 
     private static final int TRANSFER_BLOCK_SIZE = 1 << 24; // For downloads, arbitrary block size
 
     private String apiKey;
     private String email;
     private String password;
    private String authenticationEndpoint = "https://www.lulu.com/account/endpoints/authenticator.php";
    private String apiUrlTemplate = "https://apps.lulu.com/api/publish/v1/%s";
    private String apiUploadUrl = "https://pubapp.lulu.com/api/publish/v1/upload";
     private String authenticationToken;
 
     private ErrorResponse error;
     private HttpClient httpClient;
 
     /**
      * Construct the client with the default configuration file.
      * <p/>
      * This file is "publishapi.properties" in the user's home directory.
      *
      * @throws IOException if SSL context could not be initialized
      */
     public PublishApiClient() throws IOException {
         this(new PublishApiConfiguration());
     }
 
     /**
      * Construct with the specified configuration file.
      *
      * @param configFile properties file
      * @throws IOException if the properties file could not be located or parsed or if SSL context could not be initialized
      */
     public PublishApiClient(String configFile) throws IOException {
         this(new PublishApiConfiguration(configFile));
     }
 
     /**
      * Construct with the given configuration.
      * <p/>
      * Also sets up SSL to accept <b>all</b> certificates, even self-signed. This is not recommended for production use.
      *
      * @param configuration holder for configuration values
      * @throws IOException if SSL context could not be initialized
      */
     public PublishApiClient(PublishApiConfiguration configuration) throws IOException {
         LOG.info(configuration.toString());
         apiKey = configuration.getApiKey();
         email = configuration.getEmail();
         password = configuration.getPassword();
 
         httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
         ProtocolRegistrar.registerTrustAllSslContextWithHttpClient(httpClient);
     }
 
     /**
      * Authenticate using the credentials found in the properties file with which client was constructed.
      *
      * @return whether authentication was successful
      * @throws PublishApiException when an unexpected error occurs
      */
     public boolean login() throws PublishApiException {
         return login(email, password);
     }
 
     /**
      * Authenticate with the provided credentials.
      *
      * @param email    lulu account email address
      * @param password lulu account password
      * @return whether authentication was successful
      * @throws PublishApiException when an unexpected error occurs
      */
     public boolean login(String email, String password) throws PublishApiException {
         ApiResponse response = performApiCall(authenticationEndpoint,
                 generateParameters("username", email, "password", password, "responseType", "json"),
                 AuthenticationResponse.class);
 
         if (!response.isError()) {
             AuthenticationResponse payload = (AuthenticationResponse) response.getPayload();
             LOG.info(payload.toString());
             if (payload.isAuthenticated()) {
                 authenticationToken = payload.getAuthToken();
                 return true;
             }
         }
         return false;
     }
 
     public ErrorResponse getError() {
         return error;
     }
 
     /**
      * Create a new project using the provided data.
      *
      * @param project project to create
      * @return the project with content id set
      * @throws NotAuthenticatedException if not logged in
      * @throws PublishApiException       when an unexpected error occurs
      */
     public Project create(Project project) throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "create"),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken, "project", serializeProject(project)),
                 CreateResponse.class);
 
         if (!response.isError()) {
             project.setContentId(((CreateResponse) response.getPayload()).getContentId());
             return project;
         }
         error = (ErrorResponse) response.getPayload();
         throw new PublishApiException("Failed to create the project:  " + error.getErrorValue());
     }
 
     /**
      * Update the provided project.
      * <p/>
      * Can perform delta updates. At a minimum, the content id must be supplied.
      *
      * @param project project to update
      * @return the project after update
      * @throws NotAuthenticatedException if not logged in
      * @throws PublishApiException       when an unexpected error occurs
      */
     public Project update(Project project) throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "update"),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken, "project", serializeProject(project)),
                 ProjectResponse.class);
 
         if (!response.isError()) {
             return ((ProjectResponse) response.getPayload()).getProject();
         }
         error = (ErrorResponse) response.getPayload();
         throw new PublishApiException("Failed to update the project.");
     }
 
     private String serializeProject(Project project) throws PublishApiException {
         String projectJson;
         try {
             projectJson = JsonMapper.toJson(project);
         } catch (JsonMapperException e) {
             throw new PublishApiException("Could not serialize project.", e);
         }
         return projectJson;
     }
 
     /**
      * Get the base cost for a project with the given specifications.
      *
      * @param project   project specification - needs physical attributes (books) or drm (ebooks)
      * @param pageCount number of pages (books)
      * @return base cost response
      * @throws NotAuthenticatedException if not logged in
      * @throws PublishApiException       when an unexpected error occurs
      */
     public BaseCostResponse baseCost(Project project, int pageCount) throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "base_cost"),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken,
                         "project", serializeProject(project), "page_count", Integer.toString(pageCount)),
                 BaseCostResponse.class);
 
         if (!response.isError()) {
             return (BaseCostResponse) response.getPayload();
         }
         error = (ErrorResponse) response.getPayload();
         throw new PublishApiException("Failed to get base cost.");
     }
 
     /**
      * Returns the project with the provided id.
      *
      * @param contentId project identifier
      * @return project
      * @throws NotAuthenticatedException if not logged in
      * @throws PublishApiException       when an unexpected error occurs
      */
     public Project read(int contentId) throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "read/id/").concat(Integer.toString(contentId)),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken),
                 ProjectResponse.class);
 
         if (!response.isError()) {
             return ((ProjectResponse) response.getPayload()).getProject();
         }
         error = (ErrorResponse) response.getPayload();
         throw new PublishApiException("Failed to read the project.");
     }
 
     /**
      * Returns a list of the projects owned by the logged in user.
      *
      * @return list of project identifiers
      * @throws NotAuthenticatedException if not logged in
      * @throws PublishApiException       when unexpected error occurs
      */
     public List<Integer> list() throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "list"),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken),
                 ListResponse.class);
 
         if (response.isError()) {
             error = (ErrorResponse) response.getPayload();
             return new ArrayList<Integer>();
         } else {
             return ((ListResponse) response.getPayload()).getContentIds();
         }
     }
 
     /**
      * Deletes the specified project.
      *
      * @param contentId project identifier
      * @throws NotAuthenticatedException if not logged in
      * @throws PublishApiException       when unexpected error occurs
      */
     public void delete(int contentId) throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "delete/id/").concat(Integer.toString(contentId)),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken),
                 List.class);
 
         if (response.isError()) {
             error = (ErrorResponse) response.getPayload();
             throw new PublishApiException("Failed to delete the project.");
         }
     }
 
     /**
      * Upload the provided files for use in a project.
      *
      * @param files collection of files
      * @throws FileNotFoundException if attempt is made to upload a non-existent file
      * @throws PublishApiException   if an unexpected error occurs
      * @return uploaded files
      */
     public UploadResponse upload(Collection<File> files) throws PublishApiException, FileNotFoundException {
         String uploadToken = requestUploadToken();
         try {
             return performUpload(files, uploadToken);
         } catch (FileNotFoundException e) {
             throw e;
         } catch (IOException e) {
             throw new PublishApiException("Upload failed unexpectedly.", e);
         } catch (JsonMapperException e) {
             throw new PublishApiException("Upload failed unexpectedly.", e);
         }
     }
 
     private UploadResponse performUpload(Collection<File> files, String uploadToken) throws IOException, JsonMapperException {
         FileUploader uploader = new FileUploader(uploadToken, authenticationToken);
         String response = uploader.upload(apiUploadUrl, files.toArray(new File[files.size()]));
         return JsonMapper.fromJson(response, UploadResponse.class);
     }
 
     private String requestUploadToken() throws PublishApiException {
         assertAuthenticated();
 
         ApiResponse response = performApiCall(String.format(apiUrlTemplate, "request_upload_token"),
                 generateParameters("api_key", apiKey, "auth_token", authenticationToken),
                 UploadTokenResponse.class);
 
         if (response.isError()) {
             error = (ErrorResponse) response.getPayload();
             throw new PublishApiException("Failed to obtain file upload token.");
         }
         return ((UploadTokenResponse) response.getPayload()).getToken();
     }
 
     /**
      * Download the indicated project file.
      *
      * @param contentId      project identifier
      * @param fileContext    which file to download
      * @param outputLocation where to put the file
      * @throws PublishApiException if an unexpected error occurs
      */
     public void download(int contentId, FileContext fileContext, File outputLocation) throws PublishApiException {
         assertAuthenticated();
 
         String downloadUrl = String.format(apiUrlTemplate, "download/id/" + Integer.toString(contentId) + "/what/" + fileContext.toString())
                 + "?" + URLEncodedUtils.format(generateParameters("api_key", apiKey, "auth_token", authenticationToken), "UTF-8");
         if (LOG.isInfoEnabled()) {
             LOG.info("API call to <{}>", downloadUrl);
         }
 
         try {
             HttpGet httpGet = new HttpGet(downloadUrl);
             HttpResponse httpResponse = httpClient.execute(httpGet);
 
             int statusCode = httpResponse.getStatusLine().getStatusCode();
             if (statusCode == HttpStatus.SC_OK) {
                 ReadableByteChannel rbc = Channels.newChannel(httpResponse.getEntity().getContent());
                 FileOutputStream fos = new FileOutputStream(outputLocation);
                 fos.getChannel().transferFrom(rbc, 0, TRANSFER_BLOCK_SIZE);
             } else {
                 throw new PublishApiException("Bad response code returned attempting to download file: " + statusCode);
             }
         } catch (IOException e) {
             throw new PublishApiException("Unable to download indicated file.", e);
         }
     }
 
     /**
      * Converts source files
      *
      * @param manifest the manifest for conversion
      * @return the id of conversion
      * @throws PublishApiException if an unexpected error occurs
      */
     public Long convert(ConversionManifest manifest) throws PublishApiException {
         assertAuthenticated();
 
        String call = String.format(apiUrlTemplate, "conversion")
                 + "?" + URLEncodedUtils.format(generateParameters("api_key", apiKey, "auth_token", authenticationToken), "UTF-8");
         if (LOG.isInfoEnabled()) {
             LOG.info("API call to <{}>", call);
         }
 
         HttpPost httpPost = new HttpPost(call);
         httpPost.addHeader("Content-Type", "application/json");
         httpPost.addHeader("Accept", "application/json");
         httpPost.addHeader("Chunked", "false");
 
         String output = "";
         Conversion conversion;
         try {
             BasicHttpEntity httpEntity = new BasicHttpEntity();
             httpEntity.setContent(new ByteArrayInputStream(serializeManifest(manifest).getBytes("UTF-8")));
             httpPost.setEntity(httpEntity);
             HttpResponse httpResponse = httpClient.execute(httpPost);
             int returnCode = httpResponse.getStatusLine().getStatusCode();
             output = StreamUtils.inputStreamToString(httpResponse.getEntity().getContent());
             if (LOG.isInfoEnabled()) {
                 LOG.info("API call response: " + output);
             }
             if (returnCode == HttpsURLConnection.HTTP_OK) {
                 conversion = JsonMapper.fromJson(output, Conversion.class);
                 return conversion.getConversionId();
             } else {
                 throw new PublishApiException("Failed to convert the files of the project:  " + output);
             }
 
         } catch (IOException e) {
             throw new PublishApiException("Unexpected error calling API endpoint.", e);
         } catch (JsonMapperException e) {
             throw new PublishApiException("API endpoint returned unparseable response: " + output, e);
         }
     }
 
     private String serializeManifest(ConversionManifest manifest) throws PublishApiException {
         String manifestJson;
         try {
             manifestJson = JsonMapper.toJson(manifest);
         } catch (JsonMapperException e) {
             throw new PublishApiException("Could not serialize manifest.", e);
         }
         return manifestJson;
     }
 
     /**
      * Determines status of conversion
      *
      * @param jobId the id of conversion
      * @return status
      * @throws PublishApiException if an unexpected error occurs
      */
     public Conversion convertStatus(Long jobId) throws PublishApiException {
 
        String call = String.format(apiUrlTemplate, String.format("conversion/%d", jobId))
                 + "?" + URLEncodedUtils.format(generateParameters("api_key", apiKey, "auth_token", authenticationToken), "UTF-8");
 
         if (LOG.isInfoEnabled()) {
             LOG.info("API call to <{}>", call);
         }
 
         HttpGet getMethod = new HttpGet(call);
         getMethod.addHeader("Accept", "application/json");
         String output = "";
         Conversion conversion;
         try {
             HttpResponse httpResponse = httpClient.execute(getMethod);
             int returnCode = httpResponse.getStatusLine().getStatusCode();
             output = StreamUtils.inputStreamToString(httpResponse.getEntity().getContent());
             if (LOG.isInfoEnabled()) {
                 LOG.info("API call response: " + output);
             }
             conversion = JsonMapper.fromJson(output, Conversion.class);
 
             if (returnCode == HttpsURLConnection.HTTP_OK) {
                 return conversion;
             } else {
                 throw new PublishApiException("Failed to get the convertation status:  " + output);
             }
         } catch (IOException e) {
             throw new PublishApiException("Unexpected error calling API endpoint.", e);
         } catch (JsonMapperException e) {
             throw new PublishApiException("API endpoint returned unparseable response: " + output, e);
         }
     }
 
     private void assertAuthenticated() throws NotAuthenticatedException {
         if (authenticationToken == null || authenticationToken.length() == 0) {
             throw new NotAuthenticatedException("Attempted perform action without authenticating.");
         }
     }
 
     private List<NameValuePair> generateParameters(String... keysAndValues) {
         List<NameValuePair> parameters = new ArrayList<NameValuePair>();
         for (int index = 0; index < keysAndValues.length; index += 2) {
             parameters.add(new BasicNameValuePair(keysAndValues[index], keysAndValues[index + 1]));
         }
         return parameters;
     }
 
     private <T> ApiResponse performApiCall(String call, List<NameValuePair> urlParameters, Class<T> clazz) throws PublishApiException {
         if (LOG.isInfoEnabled()) {
             LOG.info("API call to <{}>", call);
         }
 
         HttpPost postMethod = new HttpPost(call);
         ApiResponse response;
         String output = "";
         try {
             postMethod.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
             HttpResponse httpResponse = httpClient.execute(postMethod);
             output = StreamUtils.inputStreamToString(httpResponse.getEntity().getContent());
             if (LOG.isInfoEnabled()) {
                 LOG.info("API call response: " + output);
             }
 
             int returnCode = httpResponse.getStatusLine().getStatusCode();
             if (returnCode == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                 response = new ApiResponse<ErrorResponse>(returnCode, JsonMapper.fromJson(output, ErrorResponse.class));
             } else {
                 response = new ApiResponse<T>(returnCode, JsonMapper.fromJson(output, clazz));
             }
             httpResponse.getEntity().consumeContent();
             return response;
         } catch (IOException e) {
             throw new PublishApiException("Unexpected error calling API endpoint.", e);
         } catch (JsonMapperException e) {
             throw new PublishApiException("API endpoint returned unparseable response: " + output, e);
         }
     }
 
 }
