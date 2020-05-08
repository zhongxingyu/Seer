 package net.mindengine.oculus.experior.reporter.remote;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class RestClient {
 
     private static final int MAX_PER_ROUTE_DEFAULT = 100;
     private static final int CONN_TIMEOUT_DEFAULT = 10000;
     private static final String PREFIX = "oculus.experior.report.client.";
     private static final int MAX_TOTAL_CONNECTION_DEFAULT = 1000;
     private static final PoolingClientConnectionManager _connectionManager = new PoolingClientConnectionManager();
     private static final HttpClient httpClient;
     
     static {
         _connectionManager.setMaxTotal(getIntParameterFromEnv(PREFIX + "max.connection", MAX_TOTAL_CONNECTION_DEFAULT));
         _connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE_DEFAULT);
         HttpParams params = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(params, getIntParameterFromEnv(PREFIX + "connection.timeout", CONN_TIMEOUT_DEFAULT));
         HttpConnectionParams.setSoTimeout(params, getIntParameterFromEnv(PREFIX + "connection.timeout", CONN_TIMEOUT_DEFAULT));
         httpClient = new DefaultHttpClient(_connectionManager, params);
     }
 
     
     public static class ResponseReader {
         private String response;
         private ResponseReader(String response) {
             this.response = response;
             if ( this.response == null ) {
                 this.response = "";
             }
         }
         public String asString(){
             return response;
         }
         public long asLong() {
             try {
                 return Long.parseLong(response);
             }
             catch (Exception e) {
                 return 0L;
             }
         }
     }
     
     private static int getIntParameterFromEnv(String name, int defaultValue) {
         String envParameter = System.getenv(name);
         if ( envParameter != null ) {
             try {
                 return Integer.parseInt(envParameter);
             }
             catch (Exception e) {
             }
         }
         return defaultValue;
     }
 
     
     public ResponseReader post(String uri, String authToken, Object requestObject) {
         return request(new HttpPost(uri), authToken, requestObject);
     }
     
     public ResponseReader put(String uri, String authToken, Object requestObject) {
         return request(new HttpPut(uri), authToken, requestObject);
     }
     
     public ResponseReader put(String uri, String authToken) {
         return request(new HttpPut(uri), authToken);
     }
 
     private ResponseReader request(HttpEntityEnclosingRequestBase requestBase, String authToken, Object requestObject) {
         HttpResponse response;
         try {
            String strRequestBody = convertRequestObjectToJson(requestObject);
            StringEntity request = new StringEntity(strRequestBody);
             request.setContentType("application/json");
             request.setContentEncoding("UTF-8");
             requestBase.setEntity(request);
             
             if ( authToken != null ) {
                 requestBase.addHeader("Auth-Token", authToken);
             }
             
             response = httpClient.execute(requestBase);
             return new ResponseReader(EntityUtils.toString(response.getEntity()));
         } catch (Exception ex) {
             requestBase.abort();
             throw new RuntimeException(ex);
         }
     }
     
     
     private ResponseReader request(HttpEntityEnclosingRequestBase requestBase, String authToken) {
         HttpResponse response;
         try {
             if ( authToken != null ) {
                 requestBase.addHeader("Auth-Token", authToken);
             }
             response = httpClient.execute(requestBase);
             return new ResponseReader(EntityUtils.toString(response.getEntity()));
         } catch (Exception ex) {
             requestBase.abort();
             throw new RuntimeException(ex);
         }
     }
 
     private String convertRequestObjectToJson(Object requestObject) throws JsonGenerationException, JsonMappingException, IOException {
         if ( requestObject == null ) {
             return "null";
         }
         else {
             ObjectMapper mapper = new ObjectMapper();
             return mapper.writeValueAsString(requestObject);
         }
     }
     
     
 }
