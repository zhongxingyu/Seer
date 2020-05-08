 package dossee.droids.rest;
 
 import android.util.Log;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 
 /**
  * @author Erik Telepovsky
  *
  * RestClient class provides the communication 
  * between application (android / web) and server
  */
 public class RestClient{
 
     private static RestClient instance;
    private static String server_address = "http://api.openarms.dk";
     private static int server_port = 80;
     private String response;
     private final String tag = "RestClient";
 
     /**
      * Constructor
      * @param address is address of server
      * @param port the server is running on
      */
     private RestClient(String address, int port) {
         server_address = address;
         server_port = port;
     }
     
     
     /**
      * getInstance of RestClient Singleton
      * @return
      */
     public static RestClient getInstance() {
     	if(instance == null) 
     		instance = new RestClient(server_address, server_port);
     	return instance;
     }
 
     public void setServerURL(String serverURL) {
     	server_address = serverURL;
     }
     
     public void setServerPort(Integer port) {
     	server_port = port;
     }
     
     public String getServerURL() {
     	return server_address;
     }
     
     public Integer getServerPort() {
     	return server_port;
     }
     
     /**
      * This method provide hard assembled process to connect to server
      * @param service
      * @param JSON as string
      */
     private void executeRequest(HttpRequestBase method, String service, String stringJSON) {
         DefaultHttpClient client = new DefaultHttpClient();
         StringBuilder sb = new StringBuilder();
         
         try {
         	String url = server_address + ":" + Integer.toString(server_port) + "/" + service;
         	URI u = new URI(url);
             method.setURI(u);
             Log.i(this.tag, url);
             
             if ((method instanceof HttpPost) && (stringJSON.length() != 0)) {
                 ByteArrayEntity bae = new ByteArrayEntity(stringJSON.getBytes());
                 bae.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                 ((HttpPost) method).setEntity(bae);
             }
 
             HttpResponse hr = client.execute(method);       
             HttpEntity entity = hr.getEntity();
             
             if (entity != null) {
                 InputStream is = entity.getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is));
                 String line;
                 while ((line = br.readLine()) != null) {
                     sb.append(line);
                 }
                 response = sb.toString();
                 is.close();
             }
         } catch (URISyntaxException ex) {
             Log.e("ERROR 1", ex.getMessage(),ex);
         } catch (IOException ex) {
             Log.e("ERROR 2", ex.getMessage(),ex);
         } finally {
             client.getConnectionManager().shutdown();
         }
     }
 
     /**
      * Encapsulate http response from server to String
      * @param service
      * @return JSONObject
      */
     @SuppressWarnings("finally")
 	private String getService(String service) {
         HttpRequestBase hrb = new HttpGet();
         String ret = "ERROR";
         try {
         	this.executeRequest(hrb, service, null);
         	Log.i("get_service", this.response);
         	ret = this.response;
         } finally {
             return ret;
         }
     }
 
     /**
      * Send json to the server in the request
      * @param service
      * @param json
      * @return
      */
     @SuppressWarnings("finally")
 	private String postService(String service, String stringJSON) {
         HttpRequestBase hrb = new HttpPost();
         String ret = "ERROR";
         try {
         	this.executeRequest(hrb, service, stringJSON);
             ret = this.response;
         } finally {
             return ret;
         }
     }
     
     /**
      * Returns the response of the HTTP request
      * @return
      */
     public String getResponse() {
         return this.response;
     }
     
     /**
      * Handle method
      * @return get poll question from server
      */
     public String getPoll(String pollID) {
         return this.getService(StaticQuery.get_poll + pollID);
     }
     
     /**
      * Handle method
      * @return send vote to server
      */
     public String sendVote(long pollID, String voteJSON) {
     	return this.postService(StaticQuery.vote + pollID, voteJSON);
     }
     
     /**
      * Handle method
      * @return get poll results from server
      */
     public String getResults(long pollID) {
         return this.getService(StaticQuery.get_results + pollID);
     }
     
     /**
      * Private class for identifying base URL for services
      */
     private class StaticQuery {
     	public final static String get_poll = "";
     	public final static String vote = "vote/";
     	public final static String get_results = "getResults/";
     }
 	
 }
