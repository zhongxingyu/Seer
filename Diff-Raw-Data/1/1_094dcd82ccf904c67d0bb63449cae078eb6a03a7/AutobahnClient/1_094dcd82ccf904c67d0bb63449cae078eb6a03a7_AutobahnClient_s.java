 package autobahn.android;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import com.example.autobahn.R;
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 import com.loopj.android.http.PersistentCookieStore;
 import net.geant.autobahn.android.ErrorType;
 import net.geant.autobahn.android.ReservationInfo;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class AutobahnClient {
 
 	static AutobahnClient instance = null;
 	private static final String LOGIN_URL = "/autobahn-gui/j_spring_security_check";
 	private static final String DOMAIN_URL = "/autobahn-gui/portal/secure/android/idms";
 	private static final String SERVICES_URL = "/autobahn-gui/portal/secure/android/services";
 	private static final String SERVICE_URL = "/autobahn-gui/portal/secure/android/service";
 	private static final String PORTS_URL = "/autobahn-gui/portal/secure/android/ports";
     private static final String SUBMIT_URL="/autobahn-gui/portal/secure/android/requestReservation";
     private static final String LOGOUT_URL = "/autobahn-gui/j_spring_security_logout";
 
 	private HttpClient httpclient;
 	private HttpGet httpget;
 	private String scheme;
 	private String host;
 	private int port;
	//boolean isLogIn;
 	private String userName;
 	private String password;
 	private HttpContext localContext;
 	private Context context = null;
 	private String TAG = "Autobahn2";
 
 	public AutobahnClient() {
 		httpclient = new DefaultHttpClient();
 		scheme = "http";
         host="62.217.125.174";
         port=8080;
 
 	}
 
 	public static AutobahnClient getInstance() {
 		if (instance == null)
 			instance = new AutobahnClient();
 
 		return instance;
 	}
 
 	public void setContext(Context context) {
 		this.context = context;
 		CookieStore cookieStore = new PersistentCookieStore(context);
 
 		localContext = new BasicHttpContext();
 		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 	}
 
 	public void setHost(String s) {
 		host = s;
 	}
 
 	public void setPort(int p) {
 		port = p;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String s) {
 		userName = s;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String pass) {
 		password = pass;
 	}
 
 	public boolean hasAuthenticate() {
 		CookieStore cookieStore = (CookieStore) localContext.getAttribute(ClientContext.COOKIE_STORE);
 		for (Cookie c : cookieStore.getCookies()) {
 			if (c.getName().equals("SPRING_SECURITY_REMEMBER_ME_COOKIE") && !c.isExpired(new Date())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
     public synchronized void logOut() throws AutobahnClientException {
         CookieStore cookieStore = (CookieStore) localContext.getAttribute(ClientContext.COOKIE_STORE);
         cookieStore.clear();
 
         NetCache.getInstance().clear();
 
         URI url;
         HttpPost httppost;
 
         try {
             url = new URI(scheme, null, host, port, LOGOUT_URL, null, null);
             httppost = new HttpPost(url);
 
         } catch (URISyntaxException e) {
             String error = e.getMessage();
             Log.d(TAG, error);
             throw new AutobahnClientException(error);
         }
 
         handlePostRequest(httppost);
 
     }
 
 	public synchronized void logIn() throws AutobahnClientException {
 
 		if (hasAuthenticate()) {
 			Log.d(TAG, "Autobahn client has already authenticate");
 			return;
 		}
 
 		retrieveLoginInfo();
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("j_username", userName));
 		params.add(new BasicNameValuePair("j_password", password));
 		params.add(new BasicNameValuePair("_spring_security_remember_me", "true"));
 		String query = URLEncodedUtils.format(params, "utf-8");
 
 		URI url;
 		HttpPost httppost;
 		HttpResponse response;
 		try {
 			url = new URI(scheme, null, host, port, LOGIN_URL, query, null);
 			httppost = new HttpPost(url);
 
 		} catch (URISyntaxException e) {
 			String error = e.getMessage();
 			Log.d(TAG, error);
 			throw new AutobahnClientException(error);
 		}
         handlePostRequest(httppost);
 
 
         if (!hasAuthenticate()) {
             String error = context.getString(R.string.login_failed);
             throw new AutobahnClientException(error);
         }
 
 	}
 
 	/**
 	 * Retrieves username, password and autobahn url
 	 * from local preferences, and sets them to autobahn variables
 	 */
 	private void retrieveLoginInfo() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
 		host = sharedPref.getString(PreferencesActivity.HOST_PREFERENCE_KEY, "");
 		port = 8080;
 
 		userName = sharedPref.getString(PreferencesActivity.USERNAME_PREFERENCE_KEY, "");
 		password = sharedPref.getString(PreferencesActivity.PASSWORD_PREFERENCE_KEY, "");
 	}
 
     private void handlePostRequest(HttpPost httppost) throws AutobahnClientException {
 
         HttpResponse response;
         try {
             response = httpclient.execute(httppost, localContext);
         } catch (ClientProtocolException e) {
             String error = e.getMessage();
             Log.d(TAG, error);
             throw new AutobahnClientException(error);
         } catch (IOException e) {
             String error = e.getMessage();
             Log.d(TAG, error);
             throw new AutobahnClientException(error);
         }
 
         int status = response.getStatusLine().getStatusCode();
 
 
         if (status == 200) {
             return ;
         } else if (status == 404) {
             String error = context.getString(R.string.error_404);
             throw new AutobahnClientException(error);
         } else if (status == 500) {
             String error = context.getString(R.string.error_500);
             throw new AutobahnClientException(error);
         } else {
             String error = context.getString(R.string.error);
             throw new AutobahnClientException(error + status);
         }
     }
 
 	private String handleGetRequest(URI url) throws AutobahnClientException {
 
 		httpget = new HttpGet(url);
         Log.d(TAG,url.toString());
 		HttpResponse response =null;
 		try {
 			response = httpclient.execute(httpget, localContext);
 
 		} catch (ClientProtocolException e) {
 			String error = context.getString(R.string.net_error);
             Log.d(TAG, e.getMessage());
 			throw new AutobahnClientException(error);
 		} catch (IOException e) {
             Log.d(TAG, e.getMessage());
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		String responceStr = null;
         String data=null;
 		int status = response.getStatusLine().getStatusCode();
         Log.d(TAG,"HTTP STATUS:"+status);
 		if (status == 200) {
 			try {
                 responceStr = EntityUtils.toString(response.getEntity());
 			} catch (IOException e) {
                 Log.d(TAG, e.getMessage());
 				String errorStr = context.getString(R.string.response_error);
 				throw new AutobahnClientException(errorStr);
 			}
 
 			Log.d(TAG, responceStr);
 
 			if (responceStr == null) {
 				String errorStr = context.getString(R.string.response_error);
 				throw new AutobahnClientException(errorStr);
 			}
 
             JSONObject json= null;
             int err=0;
             try {
                 json = new JSONObject(responceStr);
                 err=json.getInt("error");
             } catch (JSONException e) {
                 String errorStr = context.getString(R.string.response_error);
                 throw new AutobahnClientException(errorStr);
             }
 
             if(err ==  ErrorType.OK ) {
                 try {
                     data=json.getString("data");
                     Log.d(TAG, data);
                 } catch (JSONException e) {
                     String errorStr = context.getString(R.string.response_error);
                     throw new AutobahnClientException(errorStr);
                 }
             } else if(err == ErrorType.NO_DATA ) {
                 return new String("");
             }else {
                 try {
                     data=json.getString("message");
                     Log.d(TAG, data);
                 } catch (JSONException e) {
                     String errorStr = context.getString(R.string.response_error);
                     throw new AutobahnClientException(errorStr);
                 }
                 throw new AutobahnClientException(data);
             }
 
 		} else if (status == 404) {
 			throw new AutobahnClientException(context.getString(R.string.error_404) );
 		} else if (status == 500) {
 			throw new AutobahnClientException(context.getString(R.string.error_500) );
 		} else {
 			throw new AutobahnClientException(context.getString(R.string.http_error) );
 		}
 
          return data;
 	}
 
 	public synchronized void fetchTrackCircuit(String domain) throws AutobahnClientException {
 
 		ArrayList<String> circuits = new ArrayList<String>();
 		URI url;
 		url = null;
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("currentIdm", domain));
 		String query = URLEncodedUtils.format(params, "utf-8");
 
 		try {
 
 			url = new URI(scheme, null, host, port, SERVICES_URL, query, null);
 		} catch (URISyntaxException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		String json = handleGetRequest(url);
 
 		Gson gson = new Gson();
 		try {
 			circuits = gson.fromJson(json, circuits.getClass());
 		} catch (JsonParseException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 		NetCache.getInstance().setReservations(circuits, domain);
 	}
 
 	public synchronized void fetchIdms() throws AutobahnClientException {
 
         Log.d(TAG, "Fetching Domains...");
         URI url;
 		url = null;
 		try {
 			url = new URI(scheme, null, host, port, DOMAIN_URL, null, null);
 		} catch (URISyntaxException e) {
             Log.d(TAG,e.getMessage());
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		String json = handleGetRequest(url);
         Gson gson = new Gson();
 		ArrayList<String> l = new ArrayList<String>();
 		try {
 			l = gson.fromJson(json, l.getClass());
 		} catch (JsonParseException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		NetCache.getInstance().setIdms(l);
 	}
 
 	public synchronized void fetchPorts(String domain) throws AutobahnClientException {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 
 		params.add(new BasicNameValuePair("domain", domain));
 		String query = URLEncodedUtils.format(params, "utf-8");
 
 		URI url;
 		url = null;
 		try {
 			url = new URI(scheme, null, host, port, PORTS_URL, query, null);
 		} catch (URISyntaxException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		String json = handleGetRequest(url);
 		Gson gson = new Gson();
 		ArrayList<String> ports = new ArrayList<String>();
 		try {
 			ports = gson.fromJson(json, ports.getClass());
 		} catch (JsonParseException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		if (ports == null) {
 			Log.d(TAG, "received null ports");
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		Log.d(TAG, ports.toString());
 
 		NetCache.getInstance().addPorts(domain, ports);
 	}
 
 	public synchronized void fetchReservationInfo(String serviceID) throws AutobahnClientException {
 		ReservationInfo reservationInfo = new ReservationInfo();
 
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("serviceID", serviceID));
 		String query = URLEncodedUtils.format(params, "utf-8");
 		URI url;
 		try {
 			url = new URI(scheme, null, host, port, SERVICE_URL, query, null);
 		} catch (URISyntaxException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 		String json = handleGetRequest(url);
 		Gson gson = new Gson();
 		try {
 			reservationInfo = gson.fromJson(json, reservationInfo.getClass());
 		} catch (JsonParseException e) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		if (reservationInfo == null) {
 			String error = context.getString(R.string.net_error);
 			throw new AutobahnClientException(error);
 		}
 
 		NetCache.getInstance().setLastResInfo(serviceID, reservationInfo);
 	}
 
     public synchronized void submitReservation(ReservationInfo info) throws AutobahnClientException {
 
         URI url;
         HttpPost httppost;
         HttpResponse response;
         try {
             url = new URI(scheme, null, host, port, SUBMIT_URL, null, null);
             httppost = new HttpPost(url);
             Gson gson = new Gson();
             String json=gson.toJson(info);
             Log.d(TAG,json);
             StringEntity se = new StringEntity(json);
 
             httppost.addHeader("content-type", "application/json");
 
             se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
             httppost.setEntity(se);
 
         } catch (URISyntaxException e) {
             String error = e.getMessage();
             Log.d(TAG, error);
             throw new AutobahnClientException(error);
         } catch (IOException e) {
             String error = e.getMessage();
             Log.d(TAG, error);
             throw new AutobahnClientException(error);
         }
 
         handlePostRequest(httppost);
     }
 }
