 package info.vanderkooy.ucheck;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.widget.Toast;
 
 public class APIHandler {
 	private Preferences prefs;
 	private Context ctx;
 	private Tracker tracker;
 
 	public APIHandler(Context ctx) {
 		this.ctx = ctx;
 		prefs = new Preferences(ctx);
 		tracker = GoogleAnalytics.getInstance(ctx).getDefaultTracker();
 	}
 	
 	public boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager 
 	          = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    if(activeNetworkInfo != null) {
 	    	tracker.trackEvent("APIHandler", "isNetworkAvailable", "Yes", (long) 0);
 	    } else {
 	    	tracker.trackEvent("APIHandler", "isNetworkAvailable", "No", (long) 0);
 	    }
 	    return activeNetworkInfo != null;
 	}
 	
 	public void noNetworkToast() {
 		Toast toast = Toast.makeText(ctx, R.string.noNetwork, Toast.LENGTH_LONG);
 		toast.show();
 	}
 
 	public int getKey(String username, String password) {
 		tracker.trackEvent("APIHandler", "getInfo", "Key", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		if (username == "" || password == "")
 			return 0;
 		
 		String key = getWebPage("https://ucheck.nl/api/login.php?user=" + URLEncoder.encode(username) + "&pass=" + URLEncoder.encode(password));
 		if(key.length() >= 3 && !key.substring(0, 3).equalsIgnoreCase("err")) {
 			//Probably some null character at the end of 'key' which is messing up requests etc.
 			prefs.edit().putString("key", key.substring(0, key.length() - 1));
 			prefs.setUsername(username);
 			return (prefs.edit().commit()) ? 1 : 0;
 		} else if (key.equals("")) {
 			return -1;
 		} else {
			return -2;
 		}
 	}
 	
 	public boolean verifyLogin(String password) {
 		tracker.trackEvent("APIHandler", "getInfo", "VerifyKey", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		String username = prefs.getUsername();
 		int response = getKey(username, password);
 		
 		if(response == 1)
 			return true;
 		else
 			return false;
 	}
 
 	public String getProgress() {
 		tracker.trackEvent("APIHandler", "getInfo", "Progress", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		String username = prefs.getUsername();
 		String key = prefs.getKey();
 		return getWebPage("https://ucheck.nl/api/voortgang.php?user=" + URLEncoder.encode(username) + "&pass=" + key);
 	}
 	
 	public JSONObject getGrades() {
 		tracker.trackEvent("APIHandler", "getInfo", "Grades", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		String username = prefs.getUsername();
 		String key = prefs.getKey();
 		String data = getWebPage("https://ucheck.nl/api/cijfers.php?user=" + URLEncoder.encode(username) + "&pass=" + key);
 		JSONObject obj;
 		try {
 			obj = new JSONObject(data);
 		} catch (JSONException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getGrades obj", (long) 0);
 			obj = null;
 			e.printStackTrace();
 		}
 		return obj;
 	}
 	
 	public JSONObject getClasses() {
 		tracker.trackEvent("APIHandler", "getInfo", "Classes", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		String username = prefs.getUsername();
 		String key = prefs.getKey();
 		String data = getWebPage("https://ucheck.nl/api/inschrijvingen.php?user=" + URLEncoder.encode(username) + "&pass=" + key);
 		JSONObject obj;
 		try {
 			obj = new JSONObject(data);
 		} catch (JSONException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getClasses obj", (long) 0);
 			obj = null;
 			e.printStackTrace();
 		}
 		return obj;
 	}
 
 	private String getWebPage(String page) {
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpContext localContext = new BasicHttpContext();
 		HttpGet httpGet = new HttpGet(page);
 		HttpResponse response;
 		try {
 			response = httpClient.execute(httpGet, localContext);
 		} catch (ClientProtocolException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getWebPage ClientProtocolException", (long) 0);
 			e.printStackTrace();
 			return "";
 		} catch (IOException e) {
 			tracker.trackEvent("Exception", "APIHandler", "IOException", (long) 0);
 			e.printStackTrace();
 			return "";
 		}
 		String result = "";
 
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(response
 					.getEntity().getContent()));
 		} catch (IllegalStateException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getWebPage IllegalStateException", (long) 0);
 			e.printStackTrace();
 			return "";
 		} catch (IOException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getWebPage IOException2", (long) 0);
 			e.printStackTrace();
 			return "";
 		}
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				result += line + "\n";
 			}
 		} catch (IOException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getWebPage IOException3", (long) 0);
 			e.printStackTrace();
 			return "";
 		}
 		return result;
 	}
 	
 	public String unenroll(String stopid) {
 		tracker.trackEvent("APIHandler", "unEnroll", "", (long) 0);
 		String response = "Fout in verbinding maken met uSis. Probeer het later nog een keer";
 		String username = prefs.getUsername();
 		String key = prefs.getKey();
 		response = getWebPage("https://ucheck.nl/api/uitschrijven?user=" + URLEncoder.encode(username) + "&pass=" + URLEncoder.encode(key) + "&q=" + stopid);
 		
 		return response;
 	}
 
 	public JSONObject getSubjects(String value) {
 		tracker.trackEvent("APIHandler", "getInfo", "Subjects", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		String data = getWebPage("https://ucheck.nl/api/vakken?vak=" + URLEncoder.encode(value) + "&year=12");
 		JSONObject obj;
 		try {
 			obj = new JSONObject(data);
 		} catch (JSONException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getSubjects obj", (long) 0);
 			obj = null;
 			e.printStackTrace();
 		}
 		return obj;
 	}
 	
 	public JSONObject getSubjectInfo(String q) {
 		tracker.trackEvent("APIHandler", "getInfo", "subject info", (long) 0);
 		GAServiceManager.getInstance().dispatch();
 		String username = prefs.getUsername();
 		String key = prefs.getKey();
 		String response = getWebPage("https://ucheck.nl/api/details?year=12&user=" + URLEncoder.encode(username) + "&pass=" + URLEncoder.encode(key) + "&q=" + URLEncoder.encode(q));
 		JSONObject obj;
 		try {
 			obj = new JSONObject(response);
 		} catch (JSONException e) {
 			tracker.trackEvent("Exception", "APIHandler", "getSubjects obj", (long) 0);
 			obj = null;
 			e.printStackTrace();
 		}
 		return obj;
 	}
 	
 	public String enroll(String q, String nummer) {
 		tracker.trackEvent("APIHandler", "Enroll", "", (long) 0);
 		String response = "Fout in verbinding maken met uSis. Probeer het later nog een keer";
 		String username = prefs.getUsername();
 		String key = prefs.getKey();
 		response = getWebPage("https://ucheck.nl/api/inschrijven?year=12&user="+ URLEncoder.encode(username) +"&pass=" + URLEncoder.encode(key) + "&q=" + URLEncoder.encode(q) + "&nummer=" + URLEncoder.encode(nummer));
 		return response;
 	}
 }
