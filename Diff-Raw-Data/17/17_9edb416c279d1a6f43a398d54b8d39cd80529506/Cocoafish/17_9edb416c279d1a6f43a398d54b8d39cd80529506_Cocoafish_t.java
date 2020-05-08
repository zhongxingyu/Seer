 package com.appcelerator.cloud.sdk;
 
 import android.Manifest;
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.webkit.CookieSyncManager;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
 import oauth.signpost.signature.HmacSha1MessageSigner;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.appcelerator.cloud.sdk.oauth2.CocoafishDialog;
 import com.appcelerator.cloud.sdk.oauth2.DialogError;
 import com.appcelerator.cloud.sdk.oauth2.DialogListener;
 import com.appcelerator.cloud.sdk.oauth2.DlgCustomizer;
 import com.appcelerator.cloud.sdk.oauth2.Util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.FileNameMap;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.zip.GZIPInputStream;
 
 public class Cocoafish {
 
     // Strings used in the authorization flow
 	public static final String REDIRECT_URI = "acsconnect://success";
     public static final String CANCEL_URI = "acsconnect://cancel";
     public static final String ACCESS_TOKEN = "access_token";
     public static final String ACCESS_TOKEN_EXPIRES_IN = "expires_in";
     public static final String ACTION_LOGIN = "oauth";
     public static final String ACTION_SIGNUP = "signup";
 
 	private String hostname = null;
     private String authHost = CCConstants.DEFAULT_AUTH_HOST;
 	private String appKey = null;
 	private String oauthKey = null;
 	private OAuthConsumer consumer = null;
 	
 	private HttpClient httpClient = null;
 	private CookieStore cookieStore = null;
 	private Context curApplicationContext = null;
 
     private String accessToken = null;
     private int accessExpiresIn = 0;
     private long accessExpires = 0;
     private DialogListener customDialogListener;
     private DlgCustomizer dlgCustomizer; 
 
  	protected CCUser currentUser = null;
 
  	private boolean threeLegged = false;
  	
 	public Cocoafish(String key) {
 		this(key, (Context) null);
 	}
 
 	public Cocoafish(String consumerKey, String consumerSecret) {
 		this(consumerKey, consumerSecret, (Context) null);
 	}
 
 	public Cocoafish(String key, Context context) {
 		this(key, context, null);
 	}
 
 	public Cocoafish(String key, Context context, String hostname) {
 		this.appKey = key;
 		curApplicationContext = context;
 		if (hostname != null && hostname.trim().length() > 0)
 			this.hostname = hostname;
 		else
 			this.hostname = CCConstants.DEFAULT_HOSTNAME;
 
 		initializeSDK();
 	}
 
 	public Cocoafish(String consumerKey, String consumerSecret, Context context) {
 		this(consumerKey, consumerSecret, context, null);
 	}
 
 	public Cocoafish(String consumerKey, String consumerSecret, Context context, String hostname) {
 		this.oauthKey = consumerKey;
 		consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
 		consumer.setMessageSigner(new HmacSha1MessageSigner());
 		consumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
 		curApplicationContext = context;
 		if (hostname != null && hostname.trim().length() > 0)
 			this.hostname = hostname;
 		else
 			this.hostname = CCConstants.DEFAULT_HOSTNAME;
 
 		initializeSDK();
 	}
 
 	
 	public void useThreeLegged(boolean threeLegged) {
 		this.threeLegged = threeLegged;
 		//if using 3-legged OAuth the passed in 'key' should be OAuth key
 		//this happens when consumerSecret isn't provided
 		if(threeLegged && this.oauthKey == null) {
 			this.oauthKey = this.appKey;
 			this.appKey = null;
 		}
 	}
 	
 	public boolean isThreeLegged() {
 		return this.threeLegged;
 	}
 	
 	
 	private void initializeSDK() {
 		SchemeRegistry registry = new SchemeRegistry();
 		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
 		sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
 		registry.register(new Scheme("https", sslSocketFactory, 443));
 		HttpParams parameters = new BasicHttpParams();
		// 2 mins for timeout. Comment out when needed
    // HttpConnectionParams.setConnectionTimeout(parameters, 120 * 1000);
 		ClientConnectionManager manager = new ThreadSafeClientConnManager(parameters, registry);
 		httpClient = new DefaultHttpClient(manager, parameters);
 
 		cookieStore = new BasicCookieStore();
 		try {
 			loadSessionInfo();
 		} catch (CocoafishError e) {
 			e.printStackTrace();
 		}
 	}
 
 	public CCResponse sendRequest(String url, CCRequestMethod method, Map<String, Object> data) throws CocoafishError, IOException {
 		return sendRequest(url, method, data, true);
 	}
 
 	/**
 	 * 
 	 * @param url
 	 *           The last fragment of request url
 	 * @param method
 	 *           It only can be one of CCRequestMthod.GET, CCRequestMthod.POST,
 	 *           CCRequestMthod.PUT, CCRequestMthod.DELETE.
 	 * @param data
 	 *           The name-value pairs which is ready to be sent to server, the
 	 *           value only can be String type or java.io.File type
 	 * @param useSecure
 	 *           Decide whether use http or https protocol.
 	 * @return
 	 * @throws IOException
 	 *            If there is network problem, the method will throw this type of
 	 *            exception.
 	 * @throws CocoafishError
 	 *            If other problems cause the request cannot be fulfilled, the
 	 *            CocoafishError will be threw.
 	 */
 	public CCResponse sendRequest(String url, CCRequestMethod method, Map<String, Object> data, boolean useSecure)
 			throws CocoafishError, IOException {
 		CCResponse response = null;
 
 		// parameters
 		List<NameValuePair> paramsPairs = null; // store all request
 		Map<String, File> fileMap = null; // store the requested file and its
 														// parameter name
 
 		if (data != null && !data.isEmpty()) {
 			Iterator<String> it = data.keySet().iterator();
 			while (it.hasNext()) {
 				String name = it.next();
 				Object value = data.get(name);
 
 				if (value instanceof String) {
 					if (paramsPairs == null)
 						paramsPairs = new ArrayList<NameValuePair>();
 					paramsPairs.add(new BasicNameValuePair(name, (String) value));
 				} else if (value instanceof File) {
 					if (fileMap == null)
 						fileMap = new HashMap<String, File>();
 					fileMap.put(name, (File) value);
 				}
 			}
 		}
 
 		StringBuffer requestUrl = null;
 		if (useSecure) {
 			requestUrl = new StringBuffer(CCConstants.HTTPS_HEAD + hostname);
 		} else {
 			requestUrl = new StringBuffer(CCConstants.HTTP_HEAD + hostname);
 		}
 		requestUrl.append(url);
 
 		if (appKey != null && consumer == null && !threeLegged) {
 			requestUrl.append(CCConstants.KEY);
 			requestUrl.append(appKey);
 		} 
 		
 		if(this.threeLegged) {
 			if (this.accessToken != null) {
 				if (requestUrl.toString().indexOf("?") > 0) {
 					requestUrl.append("&");
 				} else {
 					requestUrl.append("?");
 				}
 				requestUrl.append(CCConstants.ACCESS_TOKEN);
 				requestUrl.append("=");
 				requestUrl.append(this.accessToken);
 			}
 		}
 
 		if (method == CCRequestMethod.GET || method == CCRequestMethod.DELETE) {
 			if (paramsPairs != null && !paramsPairs.isEmpty()) {
 				String queryString = URLEncodedUtils.format(paramsPairs, CCConstants.ENCODING_UTF8);
 				if (requestUrl.toString().indexOf("?") > 0) {
 					requestUrl.append("&");
 					requestUrl.append(queryString);
 				} else {
 					requestUrl.append("?");
 					requestUrl.append(queryString);
 				}
 			}
 		}
 
 		URI reqUri;
 		try {
 			reqUri = new URL(requestUrl.toString()).toURI();
 		} catch (URISyntaxException e1) {
 			throw new CocoafishError("Incorrect API host: " + e1.getLocalizedMessage());
 		}
 
 		HttpUriRequest request = null;
 		if (method == CCRequestMethod.GET) {
 			request = new HttpGet(reqUri);
 		}
 		if (method == CCRequestMethod.POST) {
 			request = new HttpPost(reqUri);
 		}
 		if (method == CCRequestMethod.PUT) {
 			request = new HttpPut(reqUri);
 		}
 		if (method == CCRequestMethod.DELETE) {
 			request = new HttpDelete(reqUri);
 		}
 
 		if (request == null)
 			throw new CocoafishError("The request method is invalid.");
 
 		request.addHeader(CCConstants.ACCEPT_ENCODING_HEADER, CCConstants.GZIP);
 
 		if (fileMap != null && fileMap.size() > 0) {
 			CCMultipartEntity entity = new CCMultipartEntity();
 
 			// Append the nameValuePairs to request's url string
 			if (paramsPairs != null && !paramsPairs.isEmpty()) {
 				for (NameValuePair pair : paramsPairs) {
 					entity.addPart(URLEncoder.encode(pair.getName()), URLEncoder.encode(pair.getValue()));
 				}
 			}
 
 			// Add up the file to request's entity.
 			Set<String> nameSet = fileMap.keySet();
 			Iterator<String> it = nameSet.iterator();
 			if (it.hasNext()) {
 				String name = it.next();
 				File file = fileMap.get(name);
 
 				FileNameMap fileNameMap = URLConnection.getFileNameMap();
 				String mimeType = fileNameMap.getContentTypeFor(file.getName());
 				// Assume there is only one file in the map.
 				entity.addPart(name, new FileBody(file, mimeType));
 			}
 			if (request instanceof HttpEntityEnclosingRequestBase) {
 				((HttpEntityEnclosingRequestBase) request).setEntity(entity);
 			}
 		} else {
 			if (paramsPairs != null && !paramsPairs.isEmpty()) {
 				if (request instanceof HttpEntityEnclosingRequestBase) {
					((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(paramsPairs, "UTF-8"));
 				}
 			}
 		}
 
 		if (consumer != null) {
 			try {
 				consumer.sign(request);
 			} catch (OAuthMessageSignerException e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			} catch (OAuthExpectationFailedException e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			} catch (OAuthCommunicationException e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			}
 		} else {
 	    	if(this.threeLegged) {
 	    		OAuthConsumer tmpConsumer = new CommonsHttpOAuthConsumer(this.oauthKey, "12345678901234567890123456789012");
 	    		try {
 					tmpConsumer.sign(request);
 					Header authHeader = request.getFirstHeader("Authorization");
 					StringTokenizer st = new StringTokenizer(authHeader.getValue(), ",");
 					StringBuffer sb = new StringBuffer();
 					boolean first = true;
 					while(st.hasMoreElements()) {
 						String token = st.nextToken();
 						if(token.contains("oauth_signature") || token.contains("oauth_timestamp"))
 							continue;
 						if(first) {
 							first = false;
 						} else {
 							sb.append(",");
 						}
 						sb.append(token);
 					}
 					request.setHeader("Authorization", sb.toString());
 	    		} catch (Exception e) {
 					throw new CocoafishError(e.getLocalizedMessage());
 				} 
 	    	}
 
 		}
 
 		HttpContext localContext = new BasicHttpContext();
 		localContext.setAttribute(ClientContext.COOKIE_STORE, getCookieStore());
 
 		HttpResponse httpResponse = httpClient.execute(request, localContext);
 		HttpEntity entity = httpResponse.getEntity();
 
 		if (entity != null) {
 			// A Simple JSON Response Read
 			InputStream instream = entity.getContent();
 			if (entity.getContentEncoding() != null && entity.getContentEncoding().getValue().contains(CCConstants.GZIP)) {
 				instream = new GZIPInputStream(instream);
 			}
 
 			String result = convertStreamToString(instream);
 
 			// A Simple JSONObject Creation
 			JSONObject jsonObject;
 			try {
 				jsonObject = new JSONObject(result);
 			} catch (JSONException e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			}
 			response = new CCResponse(jsonObject);
 			updateSessionInfo(response);
 		}
 
 		return response;
 	}
 
 	/*
 	 * To convert the InputStream to String we use the BufferedReader.readLine()
 	 * method. We iterate until the BufferedReader return null which means
 	 * there's no more data to read. Each line will appended to a StringBuilder
 	 * and returned as String.
 	 */
 	private String convertStreamToString(InputStream is) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 
 	private void updateSessionInfo(CCResponse response) throws CocoafishError {
 		if (response != null && response.getMeta() != null) {
 			CCMeta meta = response.getMeta();
 			if (meta.getCode() == CCConstants.SUCCESS_CODE) {
 				if (CCConstants.LOGIN_METHOD.equals(meta.getMethod()) || CCConstants.CREATE_METHOD.equals(meta.getMethod())) {
 					try {
 						if (response.getResponseData() != null) {
 							JSONArray array = response.getResponseData().getJSONArray(CCConstants.USERS);
 							if (array != null && array.length() > 0) {
 								currentUser = new CCUser(array.getJSONObject(0));
 								saveSessionInfo();
 							}
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				} else if (CCConstants.LOGOUT_METHOD.equals(meta.getMethod())) {
 					clearSessionInfo();
 				}
 			}
 		}
 	}
 
 	private CookieStore getCookieStore() {
 		return cookieStore;
 	}
 
 	private void loadSessionInfo() throws CocoafishError {
 		if (curApplicationContext != null) {
 			try {
 				// read the user cookies
 				File cookiesFile = curApplicationContext.getFileStreamPath(CCConstants.COOKIES_FILE);
 				if (cookiesFile.exists()) {
 					FileInputStream fis = curApplicationContext.openFileInput(CCConstants.COOKIES_FILE);
 					ObjectInputStream in = new ObjectInputStream(fis);
 					currentUser = (CCUser) in.readObject();
 					int size = in.readInt();
 					for (int i = 0; i < size; i++) {
 						SerializableCookie cookie = (SerializableCookie) in.readObject();
 						cookieStore.addCookie(cookie);
 					}
 				}
 			} catch (Exception e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	protected void saveSessionInfo() throws CocoafishError {
 		if (curApplicationContext != null) {
 			try {
 				// save the cookies
 				List<Cookie> cookies = cookieStore.getCookies();
 				if (cookies.isEmpty()) {
 					// should not happen when we have a current user but no cookies
 					return;
 				} else {
 					final List<Cookie> serialisableCookies = new ArrayList<Cookie>(cookies.size());
 					for (Cookie cookie : cookies) {
 						serialisableCookies.add(new SerializableCookie(cookie));
 					}
 					try {
 						FileOutputStream fos = curApplicationContext.openFileOutput(CCConstants.COOKIES_FILE, Context.MODE_PRIVATE);
 						ObjectOutputStream out = new ObjectOutputStream(fos);
 						out.writeObject(currentUser);
 						out.writeInt(cookies.size());
 						for (Cookie cookie : serialisableCookies) {
 							out.writeObject(cookie);
 						}
 						out.flush();
 						out.close();
 					} catch (IOException ex) {
 						throw new CocoafishError(ex.getLocalizedMessage());
 					}
 				}
 			} catch (Exception e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	protected void clearSessionInfo() {
 		if (curApplicationContext != null) {
 			curApplicationContext.getFileStreamPath(CCConstants.COOKIES_FILE).delete();
 		}
 		currentUser = null;
 	}
 
 	
 	
 	
     /**
      * Default authorize method. Grants only basic permissions.
      * See authorize() below for @params.
      * @throws CocoafishError 
      */
     public void authorize(Activity activity, String action, final DialogListener listener) throws CocoafishError {
         authorize(activity, action, new String[] {}, listener, false);
     }
     
     /**
      * Default authorize method. Grants only basic permissions.
      * See authorize() below for @params.
      * @throws CocoafishError 
      */
     public void authorize(Activity activity, String action, final DialogListener listener, boolean useSecure) throws CocoafishError {
         authorize(activity, action, new String[] {}, listener, useSecure);
     }
 
     /**
      * 
      * @param activity
      * @param permissions
      * @param listener
      * @throws CocoafishError 
      */
     public void authorize(Activity activity, String action, String[] permissions, final DialogListener listener, boolean useSecure) throws CocoafishError {
     	if(!this.threeLegged)
     		throw new CocoafishError("Cocoafish.authorize should be used with 3-legged OAuth only");
     	if(this.oauthKey == null)
     		throw new CocoafishError("OAuth consumer key isn't set.");
     	if(activity == null)
     		throw new CocoafishError("Parameter activity can not be null.");
     	customDialogListener = listener;
         startDialogAuth(activity, action, permissions, useSecure);
     }
     
 	private void startDialogAuth(Activity activity, String action, String[] permissions, boolean useSecure) throws CocoafishError {
 		final String method = "Cocoafish.startDialogAuth";
 		
         Bundle params = new Bundle();
         if (permissions.length > 0) {
             params.putString("scope", TextUtils.join(",", permissions));
         }
         
         CookieSyncManager.createInstance(activity);
         
         dialog(activity, action, params, new DialogListener() {
 
             public void onComplete(Bundle values) {
                 // ensure any cookies set by the dialog are saved
                 CookieSyncManager.getInstance().sync();
                 setAccessToken(values.getString(ACCESS_TOKEN));
                 setAccessExpiresIn(Integer.parseInt(values.getString(ACCESS_TOKEN_EXPIRES_IN)));
                 if (isSessionValid()) {
                     Log.d(method, "Login Success! access_token=" + getAccessToken() + " expires=" + getAccessExpires());
                     //made a call to get user information to satisfy Cocoafish
     				try {
 						CCResponse response = sendRequest("users/show/me.json", CCRequestMethod.GET, null, false);
 						updateSessionInfo2(response);
 					} catch (Throwable e) {
 						Log.d(method, "Failed to get user information: " + e.getMessage());
 						if (customDialogListener != null)
 							customDialogListener.onCocoafishError(new CocoafishError(e.getLocalizedMessage()));
 					} 
 					if (customDialogListener != null)
 						customDialogListener.onComplete(values);
                 } else {
 					if (customDialogListener != null)
 						customDialogListener.onCocoafishError(new CocoafishError("Failed to receive access token."));
                 }
             }
 
             public void onError(DialogError error) {
                 Log.d(method, "Dialog onError: " + error);
 				if (customDialogListener != null)
 					customDialogListener.onError(error);
             }
 
             public void onCocoafishError(CocoafishError error) {
                 Log.d(method, "Dialog onCocoafishError: " + error);
 				if (customDialogListener != null)
 					customDialogListener.onCocoafishError(error);
             }
 
             public void onCancel() {
                 Log.d(method, "Dialog onCancel");
 				if (customDialogListener != null)
 					customDialogListener.onCancel();
             }
         }, useSecure);
     }
 
 	private void updateSessionInfo2(CCResponse response) throws CocoafishError {
 		if (response != null && response.getMeta() != null) {
 			CCMeta meta = response.getMeta();
 			if (meta.getCode() == CCConstants.SUCCESS_CODE) {
 				try {
 					if (response.getResponseData() != null) {
 						JSONArray array = response.getResponseData().getJSONArray(CCConstants.USERS);
 						if (array != null && array.length() > 0) {
 							currentUser = new CCUser(array.getJSONObject(0));
 							saveSessionInfo();
 						}
 					}
 				} catch (JSONException e) {
 					throw new CocoafishError(e.getLocalizedMessage());
 				}
 			}
 		}
 	}
 	
 
     private void dialog(Context context, String action, DialogListener listener, boolean useSecure) throws CocoafishError {
         dialog(context, action, new Bundle(), listener, useSecure);
     }
 
     /**
      * Show dialog for authentication
      * @param context
      * @param action
      * @param parameters
      * @param listener
      * @throws CocoafishError 
      */
     private void dialog(Context context, String action, Bundle parameters, final DialogListener listener, boolean useSecure) throws CocoafishError {
 
     	StringBuffer endpoint = null;
 		if (useSecure) {
 			endpoint = new StringBuffer(CCConstants.HTTPS_HEAD);
 		} else {
 			endpoint = new StringBuffer(CCConstants.HTTP_HEAD);
 		}
 		
 		if(this.authHost == null || this.authHost.length() == 0) {
 			listener.onCocoafishError(new CocoafishError("Authorization Server isn't set."));
 			return;
 		}
     	endpoint.append(this.authHost);
         parameters.putString("client_id", this.oauthKey);
         parameters.putString("redirect_uri", REDIRECT_URI);
 
         if(ACTION_LOGIN.equals(action)) {
 	    	endpoint.append("/oauth/authorize");
 	        parameters.putString("response_type", "token");
 		} else if(ACTION_SIGNUP.equals(action)) {
 			endpoint.append("/users/sign_up");
 		} else {
 			throw new CocoafishError("Action not supported: " + action);
 		}
 
         if (isSessionValid()) {
             parameters.putString(ACCESS_TOKEN, getAccessToken());
         }
         endpoint.append("?");
         endpoint.append(Util.encodeUrl(parameters));
         
 		try {
 			URI reqUri = new URL(endpoint.toString()).toURI();
 		} catch (MalformedURLException e1) {
 			throw new CocoafishError("Incorrect Auth Host: " + e1.getLocalizedMessage());
 		} catch (URISyntaxException e2) {
 			throw new CocoafishError("Incorrect Auth Host: " + e2.getLocalizedMessage());
 		}
         
         if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
             Util.showAlert(context, "Error", "Application requires permission to access the Internet");
         } else {
             new CocoafishDialog(context, endpoint.toString(), listener, this.dlgCustomizer).show();
         }
     }
 
     /**
      * @return boolean - whether this object has an non-expired session token
      */
     private boolean isSessionValid() {
         return (getAccessToken() != null) &&
                 ((getAccessExpires() == 0) ||
                         (System.currentTimeMillis() < getAccessExpires()));
     }
 
 	
 
     //getters and setters
 	public CCUser getCurrentUser() {
 		return currentUser;
 	}
 
 	public String getHostname() {
 		return hostname;
 	}
 
 	public void setHostname(String hostname) {
 		this.hostname = hostname;
 	}
 	
     public void setAppKey(String appKey) {
     	this.appKey = appKey;
     }
     
     public String getAppKey() {
     	return this.appKey;
     }
 
     /**
      * Retrieve the OAuth 2.0 access token for API access.
      * @return String - access token
      */
     public String getAccessToken() {
         return accessToken;
     }
 
     /**
      * Retrieve the current session's expiration time (in milliseconds since Unix epoch).
      * @return long - session expiration time
      */
     public long getAccessExpires() {
         return accessExpires;
     }
 
     /**
      * Set the OAuth 2.0 access token for API access.
      * @param token - access token
      */
     public void setAccessToken(String token) {
         accessToken = token;
     }
 
     /**
      * Set the current session's expiration time (in milliseconds since Unix epoch).
      * @param time - timestamp in milliseconds
      */
     private void setAccessExpires(long time) {
         accessExpires = time;
     }
 
     /**
      * Set the current session's duration (in seconds since issued).
      * @param expiresIn - duration in seconds
      */
     private void setAccessExpiresIn(int expiresIn) {
         if (expiresIn != 0) {
         	this.accessExpiresIn = expiresIn;
             setAccessExpires(System.currentTimeMillis() + expiresIn * 1000);
         }
     }
     
     /**
      * Get the current session's duration (in seconds since issued).
      * @return duration in seconds
      */
     public int getAccessExpiresIn() {
     	return this.accessExpiresIn;
     }
 
 
     public String getAuthHost() {
 		return authHost;
 	}
 
 	public void setAuthHost(String authHost) {
 		this.authHost = authHost;
 	}
 
     public DlgCustomizer getDlgCustomizer() {
 		return dlgCustomizer;
 	}
 
 	public void setDlgCustomizer(DlgCustomizer dlgCustomizer) {
 		this.dlgCustomizer = dlgCustomizer;
 	}
 
 }
