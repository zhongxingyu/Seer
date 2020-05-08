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
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class APIHandler {
 	private Preferences prefs;
 
 	public APIHandler(Context ctx) {
 		prefs = new Preferences(ctx);
 	}
 
 	public int verifyLogin() {
 		String username = prefs.getUsername();
 		String password = prefs.getPassword();
 		if (username == "" || password == "")
 			return 0;
 		
 		String key = getWebPage("https://ucheck.nl/api/login.php?user=" + URLEncoder.encode(username) + "&pass=" + URLEncoder.encode(password));
 		if(key.length() >= 3 && !key.substring(0, 3).equalsIgnoreCase("err")) {
			//Probably some null character at the end of 'key' which is messing up requests etc.
			prefs.edit().putString("key", key.substring(0, key.length() - 1));
 			return (prefs.edit().commit()) ? 1 : 0;
 		} else if (key.equals("")) {
 			return -1;
 		} else {
 			return 0;
 		}
 	}
	
	public String getProgress() {
		String username = prefs.getUsername();
		String key = prefs.getKey();
		return getWebPage("https://ucheck.nl/api/voortgang.php?user=" + username + "&pass=" + key);		
	}
 
 	private String getWebPage(String page) {
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpContext localContext = new BasicHttpContext();
 		HttpGet httpGet = new HttpGet(page);
 		HttpResponse response;
 		try {
 			response = httpClient.execute(httpGet, localContext);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return "";
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "";
 		}
 		String result = "";
 
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(response
 					.getEntity().getContent()));
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			return "";
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "";
 		}
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				result += line + "\n";
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "";
 		}
 		return result;
 	}
 }
