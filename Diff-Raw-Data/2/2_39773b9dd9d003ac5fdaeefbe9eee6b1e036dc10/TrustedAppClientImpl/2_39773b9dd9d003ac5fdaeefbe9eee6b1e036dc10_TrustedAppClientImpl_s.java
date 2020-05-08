 package edu.emory.cci.bindaas.trusted_app_client.core;
 
 import java.io.IOException;
 import java.net.URI;
 import java.security.MessageDigest;
 import java.util.UUID;
 
 import javax.xml.bind.DatatypeConverter;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class TrustedAppClientImpl implements ITrustedAppClient{
 	
 	private final static long ROUNDOFF_FACTOR = 100000;
 	private String baseUrl ;
 	private String applicationID;
 	private String applicationSecretKey;
 	private DefaultHttpClient httpClient;
 	private JsonParser jsonParser;
 	
 	public TrustedAppClientImpl(String baseUrl , String applicationID , String applicationSecretKey)
 	{
 		this.baseUrl = baseUrl;
 		this.applicationID = applicationID;
 		this.applicationSecretKey = applicationSecretKey;
 		this.httpClient = new DefaultHttpClient();
 		this.jsonParser = new JsonParser();
 	}
 	
 	
 	
 		public static String calculateDigestValue(String applicationID,
 			String applicationKey, String salt, String username)
 			throws Exception {
 		long roundoff = System.currentTimeMillis() / ROUNDOFF_FACTOR;
 		String prenonce = roundoff + "|" + applicationKey;
 		byte[] nonceBytes = MessageDigest.getInstance("SHA-1").digest(
 				prenonce.getBytes("UTF-8"));
 		String nonce = DatatypeConverter.printBase64Binary(nonceBytes);
 
 		String predigest = String.format("%s|%s|%s|%s", username, nonce,
				applicationKey, salt);
 		String digest = DatatypeConverter.printBase64Binary(MessageDigest
 				.getInstance("SHA-1").digest(predigest.getBytes("UTF-8")));
 		
 		System.out.println(String.format("Digest of (%s,%s,%s,%s,%s)=%s", applicationID , applicationKey , salt , username , roundoff + "" , digest));
 
 		return digest;
 	}
 		static String convertStreamToString(java.io.InputStream is) {
 		    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
 		    return s.hasNext() ? s.next() : "";
 		}
 
 
 		public APIKey getShortLivedAPIKey(String username, Integer lifetime) throws Exception {
 			String salt = UUID.randomUUID().toString();
 			String digest = calculateDigestValue(this.applicationID, this.applicationSecretKey, salt, username);
 			
 			URI baseUri = new URI(baseUrl);
 			URIBuilder uriBuilder = new URIBuilder(baseUri.toString() + "/issueShortLivedApiKey" );
 			uriBuilder.addParameter("lifetime", lifetime.toString());
 			HttpGet get = new HttpGet(uriBuilder.build());
 			get.addHeader("_username" , username);
 			get.addHeader("_salt" , salt);
 			get.addHeader("_digest" , digest);
 			get.addHeader("_applicationID" , this.applicationID);
 			HttpResponse response = httpClient.execute(get);
 			
 			StatusLine statusLine = response.getStatusLine();
 			if(statusLine.getStatusCode() == 200 && response.getEntity().getContent() !=null )
 			{
 				return serverResponseToAPIKey(response);
 			}
 			else
 			{
 				serverDump(response);
 				throw new Exception("Unable to fetch API Key");
 			}
 			
 			
 		}
 
 		private  APIKey serverResponseToAPIKey ( HttpResponse response) throws IllegalStateException, IOException
 		{
 			String content = convertStreamToString(response.getEntity().getContent());
 			JsonObject serverResponseJson = jsonParser.parse(content).getAsJsonObject();
 			APIKey apiKey = new APIKey();
 			apiKey.setValue(serverResponseJson.get("api_key").getAsString());
 			apiKey.setApplicationID(serverResponseJson.get("applicationID").getAsString());
 			apiKey.setApplicationName(serverResponseJson.get("applicationName").getAsString());
 			apiKey.setExpires(serverResponseJson.get("expires").getAsLong());
 			return apiKey;
 		}
 
 		private static void serverDump(HttpResponse response) throws IllegalStateException, IOException
 		{
 			StringBuffer serverDump = new StringBuffer();
 			serverDump.append("Server Response Dump\nHeaders");
 			for(Header h : response.getAllHeaders())
 			{
 				serverDump.append(h.toString()).append("\n");
 			}
 			
 			
 			if(response.getEntity()!=null && response.getEntity().getContent()!=null)
 			{
 				serverDump.append("\nBody:\n");
 				serverDump.append(convertStreamToString(response.getEntity().getContent()));
 			}
 				
 			System.out.println(serverDump.toString());
 		}
 
 		public APIKey authorizeNewUser(String username, Long epochTimeExpires,
 				String comments) throws Exception{
 			
 			String salt = UUID.randomUUID().toString();
 			String digest = calculateDigestValue(this.applicationID, this.applicationSecretKey, salt, username);
 			
 			URI baseUri = new URI(baseUrl);
 			URIBuilder uriBuilder = new URIBuilder(baseUri.toString() + "/authorizeUser" );
 			
 			if(comments!=null)
 				uriBuilder.addParameter("comments", comments);
 			
 			
 			uriBuilder.addParameter("expires", epochTimeExpires.toString());
 			
 			HttpGet get = new HttpGet(uriBuilder.build());
 			get.addHeader("_username" , username);
 			get.addHeader("_salt" , salt);
 			get.addHeader("_digest" , digest);
 			get.addHeader("_applicationID" , this.applicationID);
 			HttpResponse response = httpClient.execute(get);
 			
 			StatusLine statusLine = response.getStatusLine();
 			if(statusLine.getStatusCode() == 200 && response.getEntity().getContent() !=null )
 			{
 				return serverResponseToAPIKey(response);
 			}
 			else
 			{
 				serverDump(response);
 				throw new Exception("Unable to Authorize user [" + username + "]");
 			}
 		}
 
 
 
 		public String revokeAccess(String username, String comments) throws Exception {
 			String salt = UUID.randomUUID().toString();
 			String digest = calculateDigestValue(this.applicationID, this.applicationSecretKey, salt, username);
 			
 			URI baseUri = new URI(baseUrl);
 			URIBuilder uriBuilder = new URIBuilder(baseUri.toString() + "/revokeUser" );
 			
 			if(comments!=null)
 				uriBuilder.addParameter("comments", comments);
 			
 			
 			HttpDelete delete = new HttpDelete(uriBuilder.build());
 			delete.addHeader("_username" , username);
 			delete.addHeader("_salt" , salt);
 			delete.addHeader("_digest" , digest);
 			delete.addHeader("_applicationID" , this.applicationID);
 			HttpResponse response = httpClient.execute(delete);
 			
 			StatusLine statusLine = response.getStatusLine();
 			if(statusLine.getStatusCode() == 200 && response.getEntity().getContent() !=null )
 			{
 				return convertStreamToString(response.getEntity().getContent());
 			}
 			else
 			{
 				serverDump(response);
 				throw new Exception("Unable to Revoke user [" + username + "]");
 			}
 		}
 
 
 }
