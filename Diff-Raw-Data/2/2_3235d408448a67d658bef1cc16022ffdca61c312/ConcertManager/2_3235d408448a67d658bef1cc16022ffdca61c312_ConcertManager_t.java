 package hioa.mappe3.s171183;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  * This class gets concert information from SongKick.com
  * 
  * @author Kat
  */
 public class ConcertManager {
 	public static final String SONGKICK_KEY = "&apikey=AZlMMENPFsnnHtbQ";
 	public static final String GET_LOCATION = "http://api.songkick.com/api/3.0/search/locations.json?location=geo:";
 	private static final String GET_CONCERTS = "http://api.songkick.com/api/3.0/metro_areas/";
 	private static final String FORMAT = "/calendar.json?";
 	private static final String PAGE = "&page=";
 	private static final int NUMBER_OF_PAGES = 5;
 
 	public static TreeMap<String, String> getLocationSuggestions(
 			double latitude, double longitude) throws InterruptedException,
 			ExecutionException {
 		TreeMap<String, String> locations = new TreeMap<String, String>();
 		String result = new GetJSONObject().execute(
 				GET_LOCATION + latitude + "," + longitude + SONGKICK_KEY).get();
 		try {
 			JSONObject jObject = new JSONObject(result).getJSONObject(
 					"resultsPage").getJSONObject("results");
 			JSONArray locationArray = jObject.getJSONArray("location");
 
 			for (int i = 0; i < locationArray.length(); i++) {
 				JSONObject obj = locationArray.getJSONObject(i);
 
 				String metroId = obj.getJSONObject("metroArea").getString("id");
 				String city = obj.getJSONObject("city")
 						.getString("displayName");
 
 				locations.put(city, metroId);
 			}
 
 		} catch (JSONException e) {
 			Log.e("ERROR",
 					"JSON error when getting locations. " + e.getMessage());
 		}
 
 		return locations;
 	}
 
 	public static ArrayList<Concert> getConcertsInCity(String metroId)
 			throws InterruptedException, ExecutionException {
 		ArrayList<Concert> concerts = new ArrayList<Concert>();
 		
		for(int p = 1; p <= NUMBER_OF_PAGES; p++){
 			String result = new GetJSONObject().execute(
 					GET_CONCERTS + metroId + FORMAT + PAGE + p + SONGKICK_KEY).get();
 
 			try {
 				JSONObject jObject = new JSONObject(result).getJSONObject(
 						"resultsPage").getJSONObject("results");
 				JSONArray eventArray = jObject.getJSONArray("event");
 
 				for (int i = 0; i < eventArray.length(); i++) {
 					JSONObject eventObject = eventArray.getJSONObject(i);
 					if (eventObject.getString("type").equals("Concert")) {
 						JSONArray performances = eventObject
 								.getJSONArray("performance");
 
 						String venueId = eventObject.getJSONObject("venue")
 								.getJSONObject("metroArea").getString("id");
 						if (venueId.equals(metroId)) {
 
 							String venue = eventObject.getJSONObject("venue")
 									.getString("displayName");
 							String time = eventObject.getJSONObject("start")
 									.getString("time");
 							String date = eventObject.getJSONObject("start")
 									.getString("date");
 
 							for (int j = 0; j < performances.length(); j++) {
 								String artist = performances.getJSONObject(j)
 										.getJSONObject("artist")
 										.getString("displayName");
 								concerts.add(new Concert(venue, artist, time, date));
 							}
 						}
 					}
 				}
 
 			} catch (JSONException e) {
 				Log.e("ERROR",
 						"JSON error when getting concerts. " + e.getMessage());
 			}
 
 			
 		}
 		
 		return concerts;
 	}
 
 	public static ArrayList<Concert> getConcertsByArtist(String artistName, String metroId) throws InterruptedException, ExecutionException {
 		ArrayList<Concert> allConcerts = getConcertsInCity(metroId);
 		System.out.println("TOTAL CONCERTS " + allConcerts.size());
 		ArrayList<Concert> artistsConcerts = new ArrayList<Concert>();
 		
 		for(Concert concert : allConcerts){
 			System.out.println("checking concert in " + concert.getVenue() + " by " + concert.getArtist());
 			if(concert.getArtist().equals(artistName)){
 				artistsConcerts.add(concert);
 			}
 		}
 		
 		return artistsConcerts;
 
 	}
 
 	/*--- ASYNC TASKS --- */
 
 	private static class GetJSONObject extends
 			AsyncTask<String, String, String> {
 
 		@Override
 		protected String doInBackground(String... urls) {
 			URL url;
 			HttpURLConnection urlConnection;
 			try {
 				url = new URL(urls[0]);
 				urlConnection = (HttpURLConnection) url.openConnection();
 				InputStream stream = new BufferedInputStream(
 						urlConnection.getInputStream());
 				StringWriter writer = new StringWriter();
 				IOUtils.copy(stream, writer);
 				return writer.toString();
 
 			} catch (MalformedURLException e2) {
 				Log.e("ERROR", "urlexception: " + e2.getMessage());
 			} catch (IOException e1) {
 				Log.e("ERROR", "ioexception: " + e1.getMessage());
 			}
 
 			return "";
 		}
 	}
 
 }
