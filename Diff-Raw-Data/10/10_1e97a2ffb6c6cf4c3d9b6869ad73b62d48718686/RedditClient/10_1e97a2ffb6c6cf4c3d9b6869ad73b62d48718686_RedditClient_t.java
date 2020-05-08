 package net.acuttone.reddimg;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.util.Log;
 
 // TODO: encrypt communication, maybe not possible yet?
 public class RedditClient {
 
 	private static final String UH_KEY = "UH";
 
 	public final static String UPVOTE = "1";
 	public final static String DOWNVOTE = "-1";
 	public final static String NO_VOTE = "0";
 	
 	private HttpClient httpclient;
 	private String uh;
 	private HttpContext localContext;
 	private File cookiesCacheDir;
 	private boolean cookiesFound;
 
 	
 	public RedditClient(File cacheDir) {
 		httpclient = new DefaultHttpClient();
 		CookieStore cookieStore = new BasicCookieStore();
 		localContext = new BasicHttpContext();
 		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 		cookiesFound = false;
 		this.cookiesCacheDir = new File(cacheDir, "cookies");
 		if (cookiesCacheDir.exists()) {
 			for (File cookieFile : cookiesCacheDir.listFiles()) {
 				FileInputStream fis = null;
 				ObjectInputStream in = null;
 				try {
 					fis = new FileInputStream(cookieFile);
 					in = new ObjectInputStream(fis);
 					SerializableCookie sc = (SerializableCookie) in.readObject();
 					Cookie sessionCookie = SerializableCookie.toBasicClientCookie(sc);
 					cookieStore.addCookie(sessionCookie);
 					cookiesFound = true;
 					Log.d(ReddimgApp.APP_NAME, "session cookie read successfully");
 				} catch (Exception ex) {
 					Log.e(ReddimgApp.APP_NAME, "error loading cookie: " + ex.toString());
 				} finally {
 					try {
 						in.close();
 					} catch (IOException ex) {
 						Log.e(ReddimgApp.APP_NAME, "error loading cookie: " + ex.toString());
 					}
 				}
 			}
 		} else {
 			Log.d(ReddimgApp.APP_NAME, "no session cookies found");
 			cookiesCacheDir.mkdir();
 		}
 
 		uh = PreferenceManager.getDefaultSharedPreferences(ReddimgApp.instance()).getString(UH_KEY, "");
 		if (!"".equals(uh)) {
 			Log.d(ReddimgApp.APP_NAME, "UH found");
 		} else {
 			Log.d(ReddimgApp.APP_NAME, "UH not found");
 		}
 	}
 
 	public boolean doLogin(String username, String password) {
 		boolean success = true;
		HttpPost httppost = null;
		try {
			httppost = new HttpPost("https://ssl.reddit.com/api/login/" + username.trim());
		} catch(Exception e) {
			Log.e(ReddimgApp.APP_NAME, e.toString());
			return false;
		}
 		CookieStore cookieStore = (CookieStore) localContext.getAttribute(ClientContext.COOKIE_STORE);
 		cookieStore.clear();
 		try {
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
 			nameValuePairs.add(new BasicNameValuePair("user", username));
 			nameValuePairs.add(new BasicNameValuePair("passwd", password));
 			nameValuePairs.add(new BasicNameValuePair("api_type", "json"));
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			HttpResponse response = httpclient.execute(httppost, localContext);
 			HttpEntity entity = response.getEntity();
 			String result = EntityUtils.toString(entity);
 
 			JSONObject jsonObject = new JSONObject(result);
 			JSONObject jo = (JSONObject) jsonObject.get("json");
 			JSONArray errors = (JSONArray) jo.get("errors");
 			if (errors.length() == 0) {
 				JSONObject data = (JSONObject) jo.get("data");
 				uh = (String) data.get("modhash");
 				saveLoginInfo(cookieStore.getCookies());
 			} else {
 				success = false;
 			}
 		} catch (Exception exc) {
 			Log.e(ReddimgApp.APP_NAME, "error while logging in: " + exc.toString());
 			success = false;
 		}
 
 		return success;
 	}
 	
 	public String getLinks(List<RedditLink> newLinks, String subreddit, String lastT3) {
 		BufferedReader in = null;
 		try {
 			HttpGet request = new HttpGet();
             request.setURI(new URI("http://www.reddit.com/" + subreddit + "/.json" + "?after=t3_" + lastT3));
             HttpResponse response = httpclient.execute(request, localContext);
             in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			String inputLine;
 			StringBuilder sb = new StringBuilder();
 			while ((inputLine = in.readLine()) != null)
 				sb.append(inputLine);
 			in.close();
 
 			JSONObject jsonObject = new JSONObject(sb.toString());
 			JSONObject data = (JSONObject) jsonObject.get("data");
 			JSONArray children = (JSONArray) data.get("children");
 			for (int j = 0; j < children.length(); j++) {
 				JSONObject obj = (JSONObject) children.get(j);
 				JSONObject cData = (JSONObject) obj.get("data");
 				String url = (String) cData.get("url");
 				String commentUrl = "http://www.reddit.com" + cData.get("permalink");
 				String title = Html.fromHtml((String) cData.get("title")).toString();
 				String author = (String) cData.get("author");
 				String postedIn = (String) cData.get("subreddit");
 				int score = cData.getInt("score");								
 				Boolean voteStatus = null;
 				if(cData.isNull("likes") == false) {
 					voteStatus = cData.getBoolean("likes");
 				}
 				lastT3 = (String) cData.get("id");
 				if (isUrlValid(url)) {
 					RedditLink newRedditLink = new RedditLink(lastT3, url, commentUrl, title, author, postedIn, score, voteStatus);
 					newLinks.add(newRedditLink);
 					Log.d(ReddimgApp.APP_NAME, " [" + lastT3 + "] " + title + " (" + url + ")");
 				}
 			}
 		} catch (Exception e) {
 			Log.e(ReddimgApp.APP_NAME, e.toString());
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					Log.e(ReddimgApp.APP_NAME, e.toString());
 				}
 			}
 		}
 		return lastT3;
 	}
 	
 	private static boolean isUrlValid(String url) {
 		return url.matches(".*(gif|jpeg|jpg|png)$");
 	}
 	
 	private void saveLoginInfo(List<Cookie> cookies) {
 		// save uh
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ReddimgApp.instance());
 		Editor edit = sp.edit();
 		edit.putString(UH_KEY, uh);
 		edit.commit();
 
 		// save cookies
 		for (int i = 0; i < cookies.size(); i++) {
 			BasicClientCookie c = (BasicClientCookie) cookies.get(i);
 			File cookieFile = new File(cookiesCacheDir, "sessioncookie" + i);
 			FileOutputStream fos = null;
 			ObjectOutputStream out = null;
 			try {
 				fos = new FileOutputStream(cookieFile);
 				out = new ObjectOutputStream(fos);
 				out.writeObject(new SerializableCookie(c));
 				cookiesFound = true;
 			} catch (IOException ex) {
 				Log.e(ReddimgApp.APP_NAME, "Error while writing cookie: " + ex.toString());
 			} finally {
 				try {
 					out.close();
 				} catch (IOException ex) {
 					Log.e(ReddimgApp.APP_NAME, "Error while writing cookie: " + ex.toString());
 				}
 			}
 		}
 	}
 	
 	public void doLogout() {
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ReddimgApp.instance());
 		Editor edit = sharedPrefs.edit();
 		edit.remove(UH_KEY);
 		edit.commit();
 		uh = "";
 		CookieStore cookieStore = (CookieStore) localContext.getAttribute(ClientContext.COOKIE_STORE);
 		cookieStore.clear();
 		cookiesFound = false;
 		for (File cookieFile : cookiesCacheDir.listFiles()) {
 			cookieFile.delete();
 		}
 		Log.d(ReddimgApp.APP_NAME, "Logout completed");
 	}
 
 	public boolean vote(RedditLink currentLink, String vote) {
 		if(isLoggedIn() == false) {
 			Log.e(ReddimgApp.APP_NAME, "error on vote");
 			return false;
 		}
 		
 		String result = "";
 
 		try {
 			HttpPost httppost = new HttpPost("http://www.reddit.com/api/vote");
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
 			nameValuePairs.add(new BasicNameValuePair("id", "t3_" + currentLink.getId()));
 			nameValuePairs.add(new BasicNameValuePair("dir", vote));
 			nameValuePairs.add(new BasicNameValuePair("uh", uh));
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse response = httpclient.execute(httppost, localContext);
 			HttpEntity entity = response.getEntity();
 			result = EntityUtils.toString(entity);
 			boolean success = result.equals("{}");
 			if(success == false) {
 				Log.e(ReddimgApp.APP_NAME, "error on vote");
 			} else {
 				currentLink.setVoteStatus(vote);
 			}
 			return success;
 		} catch (Exception exc) {
 			Log.e(ReddimgApp.APP_NAME, "error on vote: " + exc.toString());
 			return false;
 		}
 	}
 
 	public boolean isLoggedIn() {
 		if (httpclient == null || localContext == null || "".equals(uh) || cookiesFound == false) {			
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	public List<String> getMySubreddits() {
 		List<String> subreddits = new ArrayList<String>();
 
 		if (isLoggedIn() == false) {
 			Log.e(ReddimgApp.APP_NAME, "You must be logged in to retrieve your subreddits");
 			return subreddits;
 		}
 
 		try {
 			HttpGet httpget = new HttpGet("http://www.reddit.com/reddits/mine.json");
 			HttpResponse response = httpclient.execute(httpget, localContext);
 			HttpEntity entity = response.getEntity();
 			String result = EntityUtils.toString(entity);
 			JSONObject jsonObject = new JSONObject(result);
 			JSONObject data = (JSONObject) jsonObject.get("data");
 			JSONArray children = (JSONArray) data.get("children");
 			for (int j = 0; j < children.length(); j++) {
 				JSONObject obj = (JSONObject) children.get(j);
 				JSONObject cData = (JSONObject) obj.get("data");
 				String url = (String) cData.get("url");	
 				subreddits.add(url);
 			}
 		} catch (Exception e) {
 			Log.e(ReddimgApp.APP_NAME, "Error while retrieving subreddits: " + e.toString());
 		}
 
 		return subreddits;
 	}
 
 	private static class SerializableCookie implements Serializable {
 
 		private static final String EXPIRES_KEY = "expires";
 
 		private static final long serialVersionUID = 1L;
 
 		private String domain;
 		private String value;
 		private String name;
 		private String comment;
 		private Date expiryDate;
 		private String path;
 
 		private String expiryDateString;
 
 		public SerializableCookie(BasicClientCookie cookie){
 			this.domain = cookie.getDomain();
 			this.value =  cookie.getValue();
 			this.name = cookie.getName();
 			this.comment = cookie.getComment();
 			this.expiryDate = cookie.getExpiryDate();
 			this.path = cookie.getPath();
 			this.expiryDateString = cookie.getAttribute(EXPIRES_KEY);
 		}
 
 		public static BasicClientCookie toBasicClientCookie(SerializableCookie serializable) {
 			BasicClientCookie basicClientCookie = new BasicClientCookie(serializable.getName(), serializable.getValue());
 			basicClientCookie.setDomain(serializable.getDomain());
 			basicClientCookie.setComment(serializable.getComment());
 			basicClientCookie.setExpiryDate(serializable.getExpiryDate());
 			basicClientCookie.setPath(serializable.getPath());
 			basicClientCookie.setAttribute(BasicClientCookie.DOMAIN_ATTR, serializable.getDomain());
 			basicClientCookie.setAttribute(BasicClientCookie.PATH_ATTR, serializable.getPath());
 			basicClientCookie.setAttribute(EXPIRES_KEY, serializable.getExpiryDateString());
 			return basicClientCookie;
 		}
 
 		public String getDomain() {
 			return domain;
 		}
 
 		public String getValue() {
 			return value;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public String getComment() {
 			return comment;
 		}
 
 		public Date getExpiryDate() {
 			return expiryDate;
 		}
 
 		public String getPath() {
 			return path;
 		}
 
 		public String getExpiryDateString() {
 			return expiryDateString;
 		}
 		
 	}
 
 }
