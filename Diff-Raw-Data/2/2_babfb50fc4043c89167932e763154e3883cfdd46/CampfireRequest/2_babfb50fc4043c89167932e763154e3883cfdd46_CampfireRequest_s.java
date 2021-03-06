 package com.github.klondike.java.campfire;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import lgpl.haustein.Base64Encoder;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class CampfireRequest {
 	private String format = ".json";
 	
 	private Campfire campfire;
 	private boolean followRedirects = true;
 	
 	public CampfireRequest(Campfire campfire) {
 		this.campfire = campfire;
 	}
 	
 	public CampfireRequest(Campfire campfire, boolean followRedirects) {
 		this.campfire = campfire;
 		this.followRedirects = followRedirects;
 	}
 	
 	public JSONObject getOne(String path, String key) throws CampfireException, JSONException {
 		return new JSONObject(responseBody(get(path))).getJSONObject(key);
 	}
 	
 	public JSONArray getList(String path, String key) throws CampfireException, JSONException {
 		return new JSONObject(responseBody(get(path))).getJSONArray(key);
 	}
 	
 	public HttpResponse get(String path) throws CampfireException {
         return makeRequest(new HttpGet(url(path)));
 	}
 	
 	public HttpResponse post(String path) throws CampfireException {
 		return post(path, null);
 	}
 	
 	public HttpResponse post(String path, String body) throws CampfireException {
 		HttpPost request = new HttpPost(url(path));
 		
 		if (body != null) {
 			try {
 				request.addHeader("Content-type", "application/json");
 				request.setEntity(new StringEntity(body));
 			} catch(UnsupportedEncodingException e) {
 				throw new CampfireException(e, "Unsupported encoding on posting to: " + path);
 			}
 		}
 		
 		return makeRequest(request);
 	}
         
     public HttpResponse makeRequest(HttpUriRequest request) throws CampfireException {
     	request.addHeader("User-Agent", Campfire.USER_AGENT);
     	
     	Credentials credentials = new UsernamePasswordCredentials(campfire.token, "X");
 		CredentialsProvider credsProvider = new BasicCredentialsProvider();
 		credsProvider.setCredentials(new AuthScope(domain(), (campfire.ssl ? 443 : 80)), credentials);
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		client.setCredentialsProvider(credsProvider);
 		if (!followRedirects)
 			client.setRedirectHandler(new NoRedirectHandler());
         
         try {
         	return client.execute(request);
 		} catch (ClientProtocolException e) {
 			throw new CampfireException(e, "ClientProtocolException while making request to: " + request.getURI().toString());
 		} catch (IOException e) {
 			throw new CampfireException(e, "Couldn't connect to the Internet. Check your network connection.");
 		}
 	}
     
     public static String responseBody(HttpResponse response) throws CampfireException {
 		int statusCode = response.getStatusLine().getStatusCode();
 		
 		try {
 	        if (statusCode >= 200 && statusCode < 300)
 	        	return EntityUtils.toString(response.getEntity());
 	        else
 	        	throw new CampfireException("Bad status code: " + statusCode);
 		} catch(IOException e) {
 			throw new CampfireException(e, "Error while reading body of HTTP response.");
 		}
 	}
 	
 	public String domain() {
 		return campfire.subdomain + ".campfirenow.com";
 	}
 	
 	public String url(String path) {
 		return url(path, this.format);
 	}
 	
 	// lets you override the format per-request (used only for file uploading, which has to be .xml)
 	public String url(String path, String format) {
 		return (campfire.ssl ? "https" : "http") + "://" + domain() + path + format;
 	}
 	
 	public void uploadFile(String path, InputStream stream, String filename, String mimeType) throws CampfireException {
         String lineEnd = "\r\n";
         String twoHyphens = "--";
         String boundary = "---------------------------XXX";
         
         try {
         	// Unlike other parts of the API, this must be posted to the .xml endpoint, not the .json
         	// This seems to be because .json endpoints require a Content-Type of application/json,
         	// and with a multipart post it must be multipart/form-data.
         	// I consider this a bug, since it is inconsistent with the rest of the API, and undocumented.
             URL connectURL = new URL(url(path, ".xml"));
             
             HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
             conn.setDoInput(true);
             conn.setDoOutput(true);
             conn.setUseCaches(false);
             conn.setRequestMethod("POST");
 
             // authentication
             String token = campfire.token + ":" + "X";
     		String encoding = Base64Encoder.encode(token);
     		conn.setRequestProperty("Authorization", "Basic " + encoding);
             
             conn.setRequestProperty("User-Agent", Campfire.USER_AGENT);
             conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
             
             DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
             
             // header for the file itself
             dos.writeBytes(twoHyphens + boundary + lineEnd);
             // OH MY GOD the space between the semicolon and "filename=" is ABSOLUTELY NECESSARY
             dos.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + filename + "\"" + lineEnd);
             dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
             dos.writeBytes("Content-Type: " + mimeType + lineEnd);
             dos.writeBytes(lineEnd);
 
             // insert file
             int bytesAvailable = stream.available();
             int maxBufferSize = 1024;
             int bufferSize = Math.min(bytesAvailable, maxBufferSize);
             byte[] buffer = new byte[bufferSize];
             int bytesRead = stream.read(buffer, 0, bufferSize);
             while (bytesRead > 0) {
                 dos.write(buffer, 0, bufferSize);
                 bytesAvailable = stream.available();
                 bufferSize = Math.min(bytesAvailable, maxBufferSize);
                 bytesRead = stream.read(buffer, 0, bufferSize);
             }
             
             // file closer
             dos.writeBytes(lineEnd);
             
             // end multipart request            
             dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 
             // close streams
             stream.close();
             dos.flush();
             dos.close();
 
             int responseCode = conn.getResponseCode();
             
             if (responseCode != HttpStatus.SC_CREATED)
             	throw new CampfireException("Could not upload file to Campfire.");
         } catch (IOException e) {
        	throw new CampfireException(e.getClass().getCanonicalName() + "\n" + e.getMessage());
         } 
 		
 	}
 }
 
 // Used for requests where we want to detect a redirect vs a 200 (i.e. login)
 class NoRedirectHandler extends DefaultRedirectHandler {
 	@Override
 	public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
 		return false;
 	}
 }
