 package com.gloria.offlineexperiments.proxy;
 
 import java.io.UnsupportedEncodingException;
 import java.security.KeyStore;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 import android.util.Log;
 
 import com.gloria.offlineexperiments.Base64;
 
 public class GloriaApiProxy {
 
 	// private static final String SERVER_URL =
 	// "https://venus.datsi.fi.upm.es:8443/GLORIAAPI/";
 	private static final String SERVER_URL = "https://ws.users.gloria-project.eu:8443/";
 	private static final String EXPERIMENTS = "GLORIAAPI/experiments/";
 
 	public static final String OP_EXPERIMENT_LIST = SERVER_URL + EXPERIMENTS
 			+ "offline/list";
 	public static final String OP_ACTIVE_EXPERIMENTS = SERVER_URL + EXPERIMENTS
 			+ "active";
 	public static final String OP_APPLY_FOR_WOLF = SERVER_URL + EXPERIMENTS
 			+ "offline/apply?experiment=WOLF";
 	public static final String OP_SET_DATE = SERVER_URL + EXPERIMENTS
 			+ "context/%s/parameters/date";
 	public static final String OP_LOAD_URL = SERVER_URL + EXPERIMENTS
 			+ "context/%s/execute/load";
 	public static final String OP_GET_URL = SERVER_URL + EXPERIMENTS
 			+ "context/%s/parameters/image";
 	public static final String OP_SET_MARKERS = SERVER_URL + EXPERIMENTS
 			+ "context/%s/parameters/markers";
 	public static final String OP_SAVE = SERVER_URL + EXPERIMENTS
 			+ "context/%s/execute/save";
 	
 	private HttpClient lastHttpClient = null;
 	private int contextId = -1;
 
 	public int getContextId() {
 		return contextId;
 	}
 
 	public void setContextId(int contextId) {
 		this.contextId = contextId;
 	}
 
 	public HttpClient getHttpClient() {
 		shutdown();
 		try {
 			KeyStore trustStore = KeyStore.getInstance(KeyStore
 					.getDefaultType());
 			trustStore.load(null, null);
 
 			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
 			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
 			HttpParams params = new BasicHttpParams();
 			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 
 			SchemeRegistry registry = new SchemeRegistry();
 			registry.register(new Scheme("http", PlainSocketFactory
 					.getSocketFactory(), 80));
 			registry.register(new Scheme("https", sf, 443));
 
 			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
 					params, registry);
 
 			lastHttpClient = new DefaultHttpClient(ccm, params);
 		} catch (Exception e) {
 			lastHttpClient = new DefaultHttpClient();
 		}
 		return lastHttpClient;
 	}
 
 	public void shutdown() {
 		if (lastHttpClient != null) {
 			lastHttpClient.getConnectionManager().shutdown();
 			lastHttpClient = null;
 		}
 	}
 
	public HttpGet getHttpGetRequest(String operation, String authUsername,
			String authPassword) {
		String authToken = getAuthorizationTokenFromUserPassword(authUsername,
				authPassword);
		return authToken != null ? getHttpGetRequest(operation, authToken)
				: null;
	}

 	public HttpGet getHttpGetRequest(String operation, String authToken) {
 		HttpGet getRequest = new HttpGet(String.format(operation, contextId));
 		setHeaders(getRequest, authToken);
 		return getRequest;
 	}
 
	public HttpPost getHttpPostRequest(String operation, String authUsername,
			String authPassword, HttpEntity entity) {
		String authToken = getAuthorizationTokenFromUserPassword(authUsername,
				authPassword);
		return authToken != null ? getHttpPostRequest(operation, authToken,
				entity) : null;
	}

 	public HttpPost getHttpPostRequest(String operation, String authToken,
 			HttpEntity entity) {
 		HttpPost postRequest = new HttpPost(String.format(operation, contextId));
 		setHeaders(postRequest, authToken);
 		postRequest.setEntity(entity);
 		return postRequest;
 	}
 
	private String getAuthorizationTokenFromUserPassword(String username,
 			String password) {
 		String accessToken = username + ":" + password;
 		byte[] accessTokenBytes;
 		try {
 			accessTokenBytes = accessToken.getBytes("iso-8859-1");
 		} catch (UnsupportedEncodingException e) {
 			Log.e("GLORIA API", "Cannot encode authorization token!");
 			return null;
 		}
 		return Base64.encodeBase64String(accessTokenBytes);
 	}
 
 	private void setHeaders(HttpRequestBase request, String authToken) {
 		request.setHeader("content-type", "application/json");
 		request.setHeader("Authorization", "Basic " + authToken);
 	}
 }
