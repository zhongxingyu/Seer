 package com.loysen.MovieGuru.delegate;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class RottenTomatoesDelegate {
 
 	private static final String OPENING_SHOWINGS = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/opening.json";
 	private static final String CURRENT_SHOWINGS = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json";
 	private static final String DVD_RELEASES = "http://api.rottentomatoes.com/api/public/v1.0/lists/dvds/new_releases.json";
 	private static final String SEARCH = "http://api.rottentomatoes.com/api/public/v1.0/movies.json";
 	private static final String MOVIE_BASE = "http://api.rottentomatoes.com/api/public/v1.0/movies/";
 	
 	
 	private static final String RT_API_KEY = "e65q9n29mpqxsmq6dpj2qazc";
 	
 	public String getCurrentShowings() {
 		String json = "";
 		String request = CURRENT_SHOWINGS + "?apiKey=" + RT_API_KEY;// + "&page_limit=1";
 		
 		json = rottenRequest(request);
 		
 		return grabJsonArray(json, "movies");
 	}
 	
 	public String getOpeningShowings() {
 		String json = "";
 		String request = OPENING_SHOWINGS + "?apiKey=" + RT_API_KEY;
 		
 		json = rottenRequest(request);
 		
 		return grabJsonArray(json, "movies");
 	}
 	
 	public String getMovieInfo(long id) {
 		String request = MOVIE_BASE + id + ".json?apiKey=" + RT_API_KEY;
 		
 		return rottenRequest(request);
 	}
 	
 	public String getMovieReviews(long id) {
 		String json = "";
 		String request = MOVIE_BASE + id + "/reviews.json?apiKey=" + RT_API_KEY;
 		
 		json = rottenRequest(request);
 		
 		return grabJsonArray(json, "reviews");
 	}
 
 	public String getNewDVDList() {
 		String json = "";
 		String request = DVD_RELEASES + "?apiKey=" + RT_API_KEY;
 		
 		json = rottenRequest(request);
 		
 		return grabJsonArray(json, "movies");
 	}
 
 	public String search(String string) {
 		String json = "";
 		String request = SEARCH + "?apiKey=" + RT_API_KEY + "&q=" + URLEncoder.encode(string);
 		
 		json = rottenRequest(request);
 		
 		return grabJsonArray(json, "movies");
 	}
 	
 	private String rottenRequest(String request) {
 		String output = "";
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(request);
 		
 		try {
 			HttpResponse response = client.execute(httpGet);
 			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			String line = "";
 			while((line = in.readLine()) != null){
 				output += line;
 			}
 			
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return output;
 	}
 	
 	private String grabJsonArray(String rawJson, String key) {
 		JSONObject jsonObject = null;
 		
 		try {
 			jsonObject = new JSONObject(rawJson);
 			rawJson = jsonObject.getJSONArray(key).toString();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return rawJson;
 	}
 }
