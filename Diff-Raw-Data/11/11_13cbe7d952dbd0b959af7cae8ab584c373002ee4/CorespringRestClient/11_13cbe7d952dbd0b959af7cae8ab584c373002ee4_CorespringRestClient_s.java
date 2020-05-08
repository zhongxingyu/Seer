 package org.corespring;
 
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.http.*;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.*;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.bson.types.ObjectId;
 import org.corespring.resource.CorespringResource;
 import org.corespring.resource.Organization;
 import org.corespring.resource.Quiz;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class CorespringRestClient {
 
   /** Version of the API to target */
   public static final String API_VESRION = "v1";
 
   /** Version of corespring-java client */
   public static final String VERSION = "1.0-SNAPSHOT";
 
   /** Default timeout in milliseconds */
   private static final int CONNECTION_TIMEOUT = 10000;
 
   /** The endpoint. */
   private String endpoint = "http://localhost:9000/api";
 
   private final String accessToken;
 
   private HttpClient httpClient;
 
   public CorespringRestClient(String accessToken) {
     validateAccessToken(accessToken);
     this.accessToken = accessToken;
 
     ThreadSafeClientConnManager clientConnectionManager = new ThreadSafeClientConnManager();
     clientConnectionManager.setDefaultMaxPerRoute(10);
     this.httpClient = new DefaultHttpClient(clientConnectionManager);
 
     httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
     httpClient.getParams().setParameter("http.socket.timeout", new Integer(CONNECTION_TIMEOUT));
     httpClient.getParams().setParameter("http.connection.timeout", new Integer(CONNECTION_TIMEOUT));
     httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
   }
 
   public Collection<Organization> getOrganizations() {
     CorespringRestResponse response = get(Organization.getResourceRoute(this));
     return response.getAll(Organization.class);
   }
 
   public Collection<Quiz> getQuizzes(Organization organization) {
     NameValuePair organizationId = new BasicNameValuePair("organization_id", organization.getId());
     List<NameValuePair> params = new ArrayList<NameValuePair>();
     params.add(organizationId);
 
     CorespringRestResponse response = get(Quiz.getResourcesRoute(this), params);
     return response.getAll(Quiz.class);
   }
 
   public Quiz getQuizById(String id) {
     CorespringRestResponse response = get(Quiz.getResourceRoute(this, id));
     return response.get(Quiz.class);
   }
 
   public Quiz create(Quiz quiz) {
     CorespringRestResponse response = post(Quiz.getResourcesRoute(this), quiz);
     return response.get(Quiz.class);
   }
 
   public Quiz update(Quiz quiz) {
     CorespringRestResponse response = put(Quiz.getResourceRoute(this, quiz.getId()), quiz);
     return response.get(Quiz.class);
   }
 
   public Quiz delete(Quiz quiz) {
     System.err.println(Quiz.getResourceRoute(this, quiz.getId()));
     CorespringRestResponse response = delete(Quiz.getResourceRoute(this, quiz.getId()), quiz);
     if (response.getHttpStatus() != 200) {
      // throw custom exception
       return quiz;
     } else {
       return null;
     }
   }
 
   public StringBuilder baseUrl() {
     return new StringBuilder(this.getEndpoint()).append("/").append(CorespringRestClient.API_VESRION).append("/");
   }
 
   private void validateAccessToken(String accessToken) {
     if (!accessToken.equals("demo_token") && !ObjectId.isValid(accessToken)) {
       throw new IllegalArgumentException(
           new StringBuilder("Access token ").append(accessToken).append(" is not valid").toString());
     }
   }
 
   private CorespringRestResponse put(String path, CorespringResource entity) {
     return doRequest(path, "PUT", new ArrayList<NameValuePair>(), entity);
   }
 
   private CorespringRestResponse post(String path, CorespringResource entity) {
     return doRequest(path, "POST", new ArrayList<NameValuePair>(), entity);
   }
 
   private CorespringRestResponse delete(String path, CorespringResource entity) {
     return doRequest(path, "DELETE", new ArrayList<NameValuePair>(), entity);
   }
 
   public CorespringRestResponse doRequest(String path, String method, List<NameValuePair> paramList,
                                           CorespringResource entity) {
     paramList.add(new BasicNameValuePair("access_token", this.accessToken));
     HttpUriRequest request = setupRequest(path, method, paramList, entity);
     HttpResponse response;
 
     try {
       response = httpClient.execute(request);
       HttpEntity responseEntity = response.getEntity();
 
       String responseBody = "";
 
       if (entity != null) {
         responseBody = EntityUtils.toString(responseEntity);
       }
 
       StatusLine status = response.getStatusLine();
       int statusCode = status.getStatusCode();
 
       CorespringRestResponse restResponse =
           new CorespringRestResponse(request.getURI().toString(), responseBody, statusCode);
 
       return restResponse;
 
     } catch (ClientProtocolException e1) {
       throw new RuntimeException(e1);
     } catch (IOException e1) {
       throw new RuntimeException(e1);
     }
   }
 
   public CorespringRestResponse get(String path) {
     return get(path, new ArrayList<NameValuePair>());
   }
 
   private CorespringRestResponse get(String path, List<NameValuePair> paramList) {
     paramList.add(new BasicNameValuePair("access_token", this.accessToken));
     HttpUriRequest request = setupRequest(path, "GET", paramList, null);
 
     HttpResponse response;
     try {
       response = httpClient.execute(request);
       HttpEntity entity = response.getEntity();
 
       String responseBody = "";
 
       if (entity != null) {
         responseBody = EntityUtils.toString(entity);
       }
 
       StatusLine status = response.getStatusLine();
       int statusCode = status.getStatusCode();
 
       CorespringRestResponse restResponse =
           new CorespringRestResponse(request.getURI().toString(), responseBody, statusCode);
 
       return restResponse;
 
     } catch (ClientProtocolException e1) {
       throw new RuntimeException(e1);
     } catch (IOException e1) {
       throw new RuntimeException(e1);
     }
   }
 
   private HttpUriRequest setupRequest(String path, String method, List<NameValuePair> parameters, CorespringResource entity) {
     HttpUriRequest request = (entity == null) ?
         buildMethod(method, path, parameters) :
         buildMethod(method, path, parameters, entity);
 
     request.addHeader(new BasicHeader("User-Agent", "corespring-java/" + VERSION));
     request.addHeader(new BasicHeader("Accept", "application/json"));
     request.addHeader(new BasicHeader("Accept-Charset", "utf-8"));
 
     if (method.equals("PUT") || method.equals("POST")) {
       request.addHeader(new BasicHeader("Content-Type", "application/json"));
     }
 
     return request;
   }
 
   private HttpUriRequest buildMethod(String method, String path, List<NameValuePair> parameters) {
     if (method.equalsIgnoreCase("GET")) {
       return generateGetRequest(path, parameters);
     } else {
       throw new IllegalArgumentException("Unknown Method: " + method);
     }
   }
 
   private HttpUriRequest buildMethod(String method, String path, List<NameValuePair> parameters, CorespringResource entity) {
     if (method.equalsIgnoreCase("POST")) {
       return generatePostRequest(path, parameters, entity);
     } else if (method.equalsIgnoreCase("PUT")) {
       return generatePutRequest(path, parameters, entity);
     } else if (method.equalsIgnoreCase("DELETE")) {
       return generateDeleteRequest(path, parameters, entity);
     } else {
       throw new IllegalArgumentException("Unknown Method " + method);
     }
   }
 
   private HttpPost generatePostRequest(String path, List<NameValuePair> parameters, CorespringResource entity) {
     URI uri = buildUri(path, parameters);
     try {
       StringEntity jsonEntity = buildJsonEntity(entity);
       HttpPost post = new HttpPost(uri);
       post.setEntity(jsonEntity);
       return post;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   private HttpPut generatePutRequest(String path, List<NameValuePair> parameters, CorespringResource entity) {
     URI uri = buildUri(path, parameters);
     try {
       StringEntity jsonEntity = buildJsonEntity(entity);
       HttpPut put = new HttpPut(uri);
       put.setEntity(jsonEntity);
       return put;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   private HttpDelete generateDeleteRequest(String path, List<NameValuePair> parameters, CorespringResource entity) {
     URI uri = buildUri(path, parameters);
     return new HttpDelete(uri);
   }
 
   private StringEntity buildJsonEntity(Object entity) throws JsonProcessingException, UnsupportedEncodingException {
     ObjectMapper objectMapper = new ObjectMapper();
     objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
     String json = objectMapper.writeValueAsString(entity);
     return new StringEntity(json);
   }
 
   private HttpGet generateGetRequest(String path, List<NameValuePair> parameters) {
     URI uri = buildUri(path, parameters);
     return new HttpGet(uri);
   }
 
   private URI buildUri(String path, List<NameValuePair> parameters) {
     StringBuilder sb = new StringBuilder();
     sb.append(path);
 
     if (parameters != null && parameters.size() > 0) {
       sb.append("?");
       sb.append(URLEncodedUtils.format(parameters, "UTF-8"));
     }
 
     URI uri;
     try {
       uri = new URI(sb.toString());
     } catch (URISyntaxException e) {
       throw new IllegalStateException("Invalid uri", e);
     }
 
     return uri;
   }
 
   public String getEndpoint() {
     return this.endpoint;
   }
 
   public void setEndpoint(String endpoint) {
     this.endpoint = endpoint;
   }
 
 
 }
