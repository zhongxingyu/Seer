 package org.esupportail.twitter.services;
 
 
 import org.apache.log4j.Logger;
 import org.esupportail.twitter.beans.OAuthTwitterConfig;
 import org.json.JSONObject;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.crypto.codec.Base64;
 import org.springframework.social.RejectedAuthorizationException;
 import org.springframework.stereotype.Service;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 /**   		
  * Anonymous connection is no more possible (even just to display only a user public timeline) 
  * 
  * So we HAVE TO authenticate the app itself :/
  * @see https://dev.twitter.com/docs/api/1.1/overview#Authentication_required_on_all_endpoints
  * 
  * For the code here, this blog post was very helpful, we reuse some of its code :		 
  * @see http://www.coderslexicon.com/demo-of-twitter-application-only-oauth-authentication-using-java/
  * 
  * Look at also to https://dev.twitter.com/docs/auth/application-only-auth
  * 
  */
 @Service
 public class OAuthTwitterApplicationOnlyService implements InitializingBean {
 	
 	private static Logger log = Logger.getLogger(OAuthTwitterApplicationOnlyService.class);
 	
 	final static String URL_TWITTER_OAUTH2_TOKEN = "https://api.twitter.com/oauth2/token";
 	
 	final static String ESUPTWITTER_USERAGENT = "EsupTwitter";
 	
     @Autowired
     protected OAuthTwitterConfig oAuthTwitterConfig;
     
     protected String applicationOnlyBearerToken;
      
     
 	public String getApplicationOnlyBearerToken() {
 		return applicationOnlyBearerToken;
 	}
 
 	// Encodes the consumer key and secret to create the basic authorization key
     private String encodeKeys(String consumerKey, String consumerSecret) {
     	try {
     		String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
     		String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
     		
     		String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
     		byte[] encodedBytes = Base64.encode(fullKey.getBytes());
     		return new String(encodedBytes);  
     	}
     	catch (UnsupportedEncodingException e) {
     		return new String();
     	}
     }
     
     // Constructs the request for requesting a bearer token and returns that token as a string
     
     public void afterPropertiesSet() throws Exception {		 
     	HttpURLConnection connection = null;
     	String encodedCredentials = encodeKeys(oAuthTwitterConfig.getConsumerKey(), oAuthTwitterConfig.getConsumerSecret());
     		
     	try {
     		URL url = new URL(URL_TWITTER_OAUTH2_TOKEN); 
     		connection =  (HttpURLConnection) url.openConnection();           
     		connection.setDoOutput(true);
     		connection.setDoInput(true); 
     		connection.setRequestMethod("POST"); 
     		connection.setRequestProperty("Host", "api.twitter.com");
     		connection.setRequestProperty("User-Agent", ESUPTWITTER_USERAGENT);
     		connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
     		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); 
     		connection.setRequestProperty("Content-Length", "29");
     		connection.setUseCaches(false);
     			
     		writeRequest(connection, "grant_type=client_credentials");
     		String jsonResponse = readResponse(connection);
    		log.debug("jsonResponse of the bearer oauth request : " + jsonResponse);
             if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                 log.error("HTTP 403 (Forbidden) returned from Twitter API call for bearer token. Check values of Consumer Key and Consumer Secret in tokens.properties");
                 throw new RejectedAuthorizationException("HTTP 403 (Forbidden) returned attempting to get Twitter API bearer token");
             }
     		
     		// Parse the JSON response into a JSON mapped object to fetch fields from.
     		JSONObject obj = new JSONObject(jsonResponse);
     			
     		if (obj != null) {
     			applicationOnlyBearerToken = (String)obj.get("access_token");
     		}
     	}
     	catch (MalformedURLException e) {
     		throw new IOException("Invalid endpoint URL specified.", e);
     	}
     	finally {
     		if (connection != null) {
     			connection.disconnect();
     		}
     	}
     }
     
     
  // Writes a request to a connection
     private static boolean writeRequest(HttpURLConnection connection, String textBody) {
     	try {
     		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
     		wr.write(textBody);
     		wr.flush();
     		wr.close();
     			
     		return true;
     	}
     	catch (IOException e) { 
     		return false; 
     	}
     }
     	
     	
     // Reads a response for a given connection and returns it as a string.
     private static String readResponse(HttpURLConnection connection) {
     	try {
     		StringBuilder str = new StringBuilder();
     			
     		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
     		String line = "";
     		while((line = br.readLine()) != null) {
     			str.append(line + System.getProperty("line.separator"));
     		}
     		return str.toString();
     	}
     	catch (IOException e) { return new String(); }
     }
 }
