 package util;
 
 import models.Entry;
 
 import com.example.rater.R;
 
 import org.json.*;
 
 /**
  * Parses and builds JSON strings.
  * 
  * JSON format:
  * [
  *     { 
  *     	 "content_id" : 3218432,
  * 		 "name" : Cheng,
  * 		 "title" : Jibberish,
  * 		 "content": Lorem Ipsum,
  * 		 "average": 0.1,
  * 		 "ratings": [ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 ]
  * 	   }
  * 	   ......
  * ]
  * 
  * @author cheng
  *
  */
 public class JSONTranslator {
 	
	public static final String ID = "content_id";
 
 	/**
 	 * Parses a list of entries received from the server.
 	 * 
 	 * @param data The JSON data received from server
 	 * @return
 	 * @throws JSONException
 	 */
 	public static Entry[] parse(String data) throws JSONException {
 		JSONArray array = new JSONArray(data);
 		Entry[] entries = new Entry[array.length()];
 		for (int i = 0; i < array.length(); i++) {
 			JSONObject e = array.getJSONObject(i);
 			int id = e.getInt(ID);
			String n = e.getString("name");
			String t = e.getString("title");
			String c = e.getString("content");
 			double a = e.getDouble("average");
 			JSONArray ratings = e.getJSONArray("ratings");
 			int[] r = new int[ratings.length()];
 			for (int j = 0; j < r.length; j++)
 				r[j] = ratings.getInt(j);
 			
 			entries[i] = new Entry(id, n, t, c, a, r);
 		}
 		
 		return entries;
 	}
 	
 	/**
 	 * Builds a JSON string that represents entry data.
 	 * @param data
 	 * @return
 	 * @throws JSONException
 	 */
 	public static String build(Entry data) throws JSONException {
 		JSONObject o = new JSONObject();
		o.put("content_id", )
 	}
 	
 }
