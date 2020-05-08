 package sk.gista.medobs;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 
 import android.util.Log;
 
 public class Client {
 	private static final String TAG = "HttpClient";
 	
 	private String serverUrl;
 	protected HttpClient client;
 	private HttpUriRequest currentRequest;
 	
 	public Client(String serverUrl) {
 		this.serverUrl = serverUrl;
 		BasicHttpParams params = new BasicHttpParams();
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
 		client = new DefaultHttpClient(cm, params);
 		//client = new DefaultHttpClient();
 	}
 
 	public boolean login(String username, String password) {
 		HttpPost loginRequest = new HttpPost(serverUrl+"/android/login/");
 		
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("username", username));
 		params.add(new BasicNameValuePair("password", password));
 		HttpResponse response = null;
 		try {
 			loginRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
 			response = client.execute(loginRequest);
 			Log.i(TAG, "status: "+response.getStatusLine().getStatusCode());
 			return response.getStatusLine().getStatusCode() == 200;
 		} catch (UnsupportedEncodingException e) {
 			Log.e(TAG, "login into Gisplan failed", e);
 		} catch (Exception e) {
 			Log.e(TAG, "login into Gisplan failed", e);
 		} finally {
 			closeResponse(response);
 		}
 		return false;
 	}
 
 	public void logout() {
 		HttpResponse response = null;
 		try {
 			response = httpGet("/gisplandroid/logout/");
 		} catch (IOException e) {
 			Log.e(TAG, "logout from Gisplan failed", e);
 		} finally {
 			closeResponse(response);
 		}
 	}
 
 	public HttpResponse httpGet(String path) throws IOException {
 		String url = serverUrl+path;
 		//Log.i(TAG, url);
 		HttpGet request = new HttpGet(url);
 		return httpRequest(request);
 	}
 
 	public HttpResponse httpRequest(HttpUriRequest request) throws IOException {
 		currentRequest = request;
 		HttpResponse response = null;
 		try {
 			response = client.execute(request);
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "http request failed: "+request.getURI(), e);
 		}
 		return response;
 	}
 
 	public boolean isExecuting() {
 		return currentRequest != null;
 	}
 
 	public void cancelCurrentRequest() {
 		if (currentRequest != null) {
 			currentRequest.abort();
 			currentRequest = null;
 		}
 	}
 
 	public void closeResponse(HttpResponse response) {
 		if (response != null) {
 			try {
 				response.getEntity().consumeContent();
 			} catch (IOException e) {
 				Log.e(TAG, "closing connection failed", e);
 			}
 		}
 		currentRequest = null;
 	}
 }
