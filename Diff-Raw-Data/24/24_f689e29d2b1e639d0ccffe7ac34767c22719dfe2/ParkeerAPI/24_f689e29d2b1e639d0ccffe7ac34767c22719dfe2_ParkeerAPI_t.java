 package nl.miraclethings.parkeerbeter.api;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 
 import nl.miraclethings.parkeerbeter.R;
 import nl.miraclethings.parkeerbeter.map.ZoneMarker;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.conn.ssl.X509HostnameVerifier;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Base64;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 
 public class ParkeerAPI {
 	private static final String BASE_URL = "https://sas.smsparking.nl/";
 	
 	private static final String LOCATION_URL = "https://i.smsparking.nl/1/zots";
 	
 	private SharedPreferences pref;
 
 	public ParkeerAPI(SharedPreferences pref) {
 		this.pref = pref;
 	}
 	
 	public String getUsername() {
 		return pref.getString("username", "");
 	}
 
 	public String getPassword() {
 		return pref.getString("password", "");
 	}
 
 	public String getCookie() {
 		return pref.getString("cookie", "");
 	}
 
 	public String getCookieName() {
 		return pref.getString("cookie_name", "");
 	}
 	
 	public void storeCredentials(String username, String password) {
 		pref.edit()
 			.putString("username", username)
 			.putString("password", password)
 			.commit();
 	}
 	
 	public void storeCookie(String name, String value) {
 		pref.edit()
 			.putString("cookie_name", name)
 			.putString("cookie", value)
 			.commit();
 	}
 
 	public DefaultHttpClient getClient() {
 	    HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
 	
 	    DefaultHttpClient client = new DefaultHttpClient();
 	
 	    SchemeRegistry registry = new SchemeRegistry();
 	    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
 	    socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
 	    registry.register(new Scheme("https", socketFactory, 443));
 	    SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
 	
 	    // Set verifier      
 	    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
 
 	    return new DefaultHttpClient(mgr, client.getParams());
 	}	
 	
 	public int checkLogin()
 	{
 		if (getUsername().length() == 0) {
 			Log.v("ParkeerAPI", "No credentials");
 			return R.string.login_needcredentials;
 		}
  
 		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();		
 		params.add(new BasicNameValuePair("username", getUsername()));
 		params.add(new BasicNameValuePair("password", getPassword()));
 		params.add(new BasicNameValuePair("lang", "nl_NL"));
 		
 		HttpPost request = new HttpPost(BASE_URL + "logmein.pl");
 		
         request.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
         request.getParams().setParameter("http.protocol.handle-redirects",false);
 
 		try {
 			request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			Log.e("ParkeerAPI", e.getMessage());
 			return R.string.login_unexpected;
 		}
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 
 		try {
 			response = client.execute(request);
 		} catch (Exception e) {
 			Log.e("ParkeerAPI", e.getMessage());
 			return R.string.login_unexpected;
 		}
   
 		if (response.getStatusLine().getStatusCode() != 302) {
 			Log.e("ParkeerAPI", "Login failure, HTTP " + response.getStatusLine().getStatusCode());
 			return R.string.login_fail;
 		}
 		
 		// All OK, store the cookie for later requests.
 		List<Cookie> cookies = client.getCookieStore().getCookies();
 		storeCookie(cookies.get(0).getName(), cookies.get(0).getValue());
 		
 		return 0;
 	}
 	
 	private String getRequest(String urlpart) throws ParkeerAPIException {
 		return getRequest(urlpart, false);
 	}
 	
 	private String getRequest(String urlpart, boolean retry) throws ParkeerAPIException {
 		String body = "";
 		
 		try {
 
 			HttpGet request = new HttpGet(BASE_URL + urlpart);
 			
 	        request.getParams().setParameter("http.protocol.handle-redirects",false);
 	        request.setHeader("Cookie", getCookieName()+"="+getCookie());
 
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpResponse response = client.execute(request);
 
 			body = EntityUtils.toString(response.getEntity());
 		} catch (Throwable e) {
 			e.printStackTrace();
 			throw new ParkeerAPIException("Error: " + e.getMessage());
 		}
 		if (!retry && body.contains("onload=\"sessionend")) {
 			// renew
 			if (checkLogin() != 0) {
 				throw new ParkeerAPIException("Authorization failed");
 			}
 			return getRequest(urlpart, true);
 		}
 		
 		return body;
 	}
 	
 	public List<ParkeerAPIEntry> getCurrentParkings() throws ParkeerAPIException
 	{
 		List<ParkeerAPIEntry> results = new ArrayList<ParkeerAPIEntry>();
 
 		Document doc = Jsoup.parse(getRequest("myparkings.pl"));
 		
 		Elements emptyCheck = doc.select("#main tr h2");
 		if (emptyCheck.size() == 1) {
 			Log.v("ParkeerAPI", "Niets actief");
 			return results;
 		}
 		
 		Elements trs = doc.select("#main tr");
 		Log.v("ParkeerAPI", "Nr of TRs: " + trs.size());
 		
 		Elements datas = doc.select("#main tr h1.acpark");
 		Log.v("ParkeerAPI", "Nr of H1s: " + datas.size());
 		int j = 0;
		for (int i=0; i<datas.size(); i+= 9) {
 			Elements els = doc.select("#parkid" + (j+2));
 			Log.v("ParkeerAPI","#parkid" + (j+2) + " " + els.size());
 
 			Log.v("ParkeerAPI", els.toString());
 			String id = els.attr("value");
			Log.v("ParkeerAPI", "Entry id: " + id + " " + datas.get(i+2).text());
 			ParkeerAPIEntry entry = new ParkeerAPIEntry(
 					id,
 					datas.get(i+0).text(),
 					datas.get(i+1).text(),
 					datas.get(i+2).text(),
 					datas.get(i+5).text(),
					datas.get(i+6).text(),
					datas.get(i+7).text(),
					datas.get(i+8).text()
 					);
 			results.add(entry);
 			j++;
 		}
 		
 		Log.v("ParkeerAPI", "Nr of entries: " + results.size());
 		return results;
 	}
 	
 	public String stop(ParkeerAPIEntry entry) throws ParkeerAPIException {
 		String s = getRequest("myparkings.pl?fname=endpark&args=" + entry.id);
 		int start = s.indexOf("onload=\"alert('")+18;
 		int end = s.length()-5;
 		Log.v("ParkeerAPI", s.substring(start, end));
 		return s.substring(start, end);
 	}
 	
 	public String start(String zonecode, String kenteken) throws ParkeerAPIException {
 		String s = getRequest("startpark.pl?fname=startpark" +
 				"&args=" + zonecode +
 				"&zone=" + zonecode +
 				"&args=" + kenteken +
 				"&vrn=" + kenteken);
 		
 		Log.v("ParkeerAPI", s);
 		Document doc = Jsoup.parse(s);
 		return doc.select("h1.status").text();
 	}
 
 	public List<ZoneMarker> queryLocations(Context context, GeoPoint center) {
 		try {
 			String url = LOCATION_URL+"?lat=" + Double.toString(center.getLatitudeE6()/1e6) + "&lgt=" + Double.toString(center.getLongitudeE6()/1e6);
 			Log.v("ParkeerAPI", url);
 			
 			HttpGet request = new HttpGet(url);
 			String auth = "Basic " + Base64.encodeToString(
 					new String(getUsername() + ":" + getPassword()).getBytes(), Base64.DEFAULT);
 			request.addHeader("Authorization", auth.trim());
 
 			DefaultHttpClient client = new HttpsClient(context);
 			
 			Log.v("ParkeerAPI", request.getRequestLine().toString());
 			Log.v("ParkeerAPI", auth);
 			
 			client.getParams().setBooleanParameter("http.protocol.expect-continue", false);
 			HttpResponse response = client.execute(request);
 	
 			String body = EntityUtils.toString(response.getEntity());
 			//Log.v("ParkeerAPI", body);
 
 			JSONArray l = new JSONArray(body);
 
 			List<ZoneMarker> locations = new ArrayList<ZoneMarker>();
 			Log.v("ParkeerAPI", "Num: " + l.length());
 			for (int i=0; i<l.length(); i++) {
 				
 				JSONObject o = null;
 				try { o = (JSONObject)l.get(i); } catch (JSONException e) { continue; }
 				if (o == null || !o.has("zonecode") || o.getString("zonecode") == null) continue;
 				
 				Log.v("XX", o.toString());
 
 				ZoneMarker marker = new ZoneMarker(o.getString("zonecode"),
 											  o.getString("name"),
 											  Double.parseDouble(o.getString("lat")),
 											  Double.parseDouble(o.getString("lgt")));
 				locations.add(marker);
 			}
 			
 			return locations;
 			
 		} catch (Throwable e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 }
