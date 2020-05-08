 package se.chalmers.h_sektionen.utils;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import se.chalmers.h_sektionen.containers.Event;
 import se.chalmers.h_sektionen.containers.NewsItem;
 
 /**
  * Class for retrieving and parsing data for different feeds
  */
 public class LoadData {
 	
 	
 	/**
 	 * Retrieves and parses event posts
 	 * @return List containing events
 	 */
 	public static List<Event> loadEvents() throws JSONException {
 		
 		String data = getJSON(Constants.GOOGLEEVENTS);
 		
 		JSONObject json_obj = new JSONObject(data).getJSONObject("feed");
 		JSONArray json_arr = json_obj.getJSONArray("entry");
 			
 		List<Event> events = new ArrayList<Event>();
 			
 		//Collection the right content from JSON
 		for (int i = 0; i < json_arr.length(); i++){
 			String title = json_arr.getJSONObject(i).getJSONObject("title").optString("$t");
 			String description = json_arr.getJSONObject(i).getJSONObject("content").optString("$t");
 			String time = json_arr.getJSONObject(i).getJSONArray("gd$when").getJSONObject(0).optString("startTime");
 			String where = json_arr.getJSONObject(i).getJSONArray("gd$where").getJSONObject(0).optString("valueString");
 				
 			//If time is not an all day event
 			if(time.length() > "1967-09-03".length()){
 				time = fromDate(time);
 			}
 			
 			//Add events if it has i title
 			if (!title.equals("")){
 				events.add(new Event(title, description, where, time + ". "));
 			}
 		}	
 		return events;
 	}
 	
 	/**
 	 * Retrieves and parses pub events
 	 * @return List containing pub events
 	 */
 	public static List<Event> loadPubs() throws JSONException {
 		
 		String data = getJSON(Constants.PUBEVENTS);
 		
 		JSONObject json_obj = new JSONObject(data).getJSONObject("feed");
 		JSONArray json_arr = json_obj.getJSONArray("entry");
 			
 		List<Event> events = new ArrayList<Event>();
 			
 			
 		for (int i = 0; i < json_arr.length(); i++){
 			String title = json_arr.getJSONObject(i).getJSONObject("title").optString("$t");
 			String description = json_arr.getJSONObject(i).getJSONObject("content").optString("$t");
 			String time = json_arr.getJSONObject(i).getJSONArray("gd$when").getJSONObject(0).optString("startTime");
 			String where = json_arr.getJSONObject(i).getJSONArray("gd$where").getJSONObject(0).optString("valueString");
 			
 			//If time is not an all day event
 			if(time.length() > "1967-09-03".length()){
 				time = fromDate(time) + " - SENT. ";
 			}
 			
 
 			if (!title.equals("")){
 				events.add(new Event(title, description, where, time));
 			}
 		}
 			
 		return events;
 	}
 
 	/**
 	 * Retrieves and parses news feed posts
 	 * @return List containing news feed posts
 	 * @throws JSONException 
 	 */
 	public static ArrayList<NewsItem> loadNews(int descending) throws JSONException{
 		
 		//Get JSON string from server
 		String result = getJSON(Constants.NEWSFEED + descending); 
 		
 		List<NewsItem> posts = new ArrayList<NewsItem>();
 		
 		//Parse JSON string
 			JSONObject json_obj = new JSONObject(result);
 			JSONArray json_arr = json_obj.getJSONArray("data");
 			
 			for (int i = 0; i < json_arr.length(); i++){
 				//Get post message
 				String message = json_arr.getJSONObject(i).optString("message");
 				
 				//Get date
 				String[] date = json_arr.getJSONObject(i).optString("created_time").split("T");
 				
 				//Get image url
 				String image = json_arr.getJSONObject(i).optString("picture");
 				
 				if(!image.equals("")){
 					image = image.replace("s.jpg", "n.jpg");
 				}
 				
 				//Add to posts if valid content
 				if ((!message.equals("")) && (!date.equals(""))){
 					posts.add(new NewsItem(message, date[0], image));
 				}
 			}
 		
 		return (ArrayList<NewsItem>) posts;
 	}
 
 	/**
 	 * Retrieves data from url and returns it in a string
 	 * @param url The url to retrieve data from
 	 * @return String containing the retrieved data
 	 */
 	public static String getJSON(String url){
 		
 		StringBuilder builder = new StringBuilder();
 	    HttpClient client = new DefaultHttpClient();
 	    HttpGet httpGet = new HttpGet(url);
 	    try {
 	        HttpResponse response = client.execute(httpGet);
 	        HttpEntity entity = response.getEntity();
 	        InputStream content = entity.getContent();
 	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 	        
 	        String line;
 	        while ((line = reader.readLine()) != null) {
 	          builder.append(line);
 	        }
 	    } catch (Exception e) {}
 		
 		return builder.toString(); 
 	}
 	
 	/**
 	 * Convert date string to another format
 	 * @param s
 	 * @return String with date and time
 	 */
 	public static String fromDate(String s){
 		
 		String[] date = s.split("T");
 		String time = date[1].substring(0,5);
 		String[] dates = date[0].split("-"); 
 		String month = dates[1];
 		String day = dates[2];
 		
 		return day + "/" + month + ", kl: " + time;
 	}
 }
