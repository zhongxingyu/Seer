 package fetcher.dal;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import sun.misc.BASE64Encoder;
 
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 
 @SuppressWarnings("restriction")
 public class TwitterClient implements ITwitterClient {
 	private static String TWITTER_CONSUMER_KEY = "CvOYJ2qiCZOmw4ZjII57A";
 	private static String TWITTER_CONSUMER_SECRET = "fTSpbBcm5pNJGJ935rblVsOvbBk8F55pnIhXF0mQ1Y";
 
 	private String accessTokenCache = null;
 
 	/**
 	 * Does a search with Twitter search api and returns the JSON
 	 * response.
 	 */
 	public String search(String keyword, long maxId) {
 		try {
 			HttpsURLConnection conn = createSearchConnection(getAccessToken(true), keyword, maxId);
 			if (conn.getResponseCode() == 401) {
 				conn = createSearchConnection(getAccessToken(false), keyword, maxId);
 			}
 
 			return slurpStream(conn.getInputStream());
 		} catch (Exception e) {
 			System.out.println("Could not query twitter.");
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private HttpsURLConnection createSearchConnection(String access_token, String keyword, long maxId)
 		throws MalformedURLException, UnsupportedEncodingException, IOException {
 		
 		URL url = new URL("https://api.twitter.com/1.1/search/tweets.json?q="
 						  + URLEncoder.encode(keyword, "utf-8") +
 						  "&count=100&include_entities=true&max_id=" + maxId);
 		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
 		conn.setDoInput(true);
 		conn.addRequestProperty("Authorization", "Bearer " + access_token);
 		return conn;
 	}
 
 
 	/**
 	 * Gets a access token for Twitter API.
 	 */
 	private String getAccessToken(boolean useCache) {
 		if (useCache && accessTokenCache != null) {
 			return accessTokenCache;
 		}
 
 		try {
 			URL url = new URL("https://api.twitter.com/oauth2/token");
 			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
 			String data = "grant_type=client_credentials";
 			conn.setDoOutput(true);
 			conn.setDoInput(true);
 			conn.setRequestMethod("POST");
 			conn.addRequestProperty(
 				"Authorization",
 				"Basic " + encode64(TWITTER_CONSUMER_KEY + ":" + TWITTER_CONSUMER_SECRET));
 			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
 			conn.addRequestProperty("Content-Length", ""+data.length());
 
 			DataOutputStream stream = new DataOutputStream(conn.getOutputStream());
 			stream.writeBytes(data);
 			stream.flush();
 			stream.close();
 			
 			if (conn.getResponseCode() != 200) {
 				return null;
 			}
 
 			String response = slurpStream(conn.getInputStream());
 			JsonObject token = new GsonBuilder().create().fromJson(response, JsonObject.class);
 			accessTokenCache = token.get("access_token").getAsString();
 			return accessTokenCache;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Helper method to encode to base 64.
 	 */
 	private static String encode64(String input) throws UnsupportedEncodingException {
 		try {
 			BASE64Encoder enc = new BASE64Encoder();
			return enc.encode(input.getBytes("UTF-8")).replace("\n", "").replace("\r", "");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Helper method to slurp a input stream and return as string.
 	 */
 	private static String slurpStream(InputStream in) {
 		try {
 			StringBuffer response = new StringBuffer();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				response.append(line);
 			}
 			reader.close();
 			return response.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 }
