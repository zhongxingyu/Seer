 package com.example.reptilexpress.transportapi;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.location.Location;
 import android.net.Uri;
 import android.text.format.Time;
 import android.util.Log;
 
 public class BusInformation {
 
 		public static List<Stop> getClosestStops(final Location location) {
 			try {
 				return sharedInstance.closestStops(location);
 			} catch (Exception e) {
 				throw new RuntimeException("Failed getting closest stops", e);
 			}
 		}
 		
 		public static List<Arrival> getStopTimetable(final Stop stop) {
 			try {
 				return sharedInstance.stopTimetable(stop);
 			} catch (Exception e) {
 				throw new RuntimeException("Failed getting stop timetable", e);
 			}
 		}
 		
 		public static List<Arrival> getBusTimetable(final Arrival arrival) {
 			try {
 				return sharedInstance.busTimetable(arrival);
 			} catch (Exception e) {
 				throw new RuntimeException("Failed getting bus timetable", e);
 			}
 		}
 	
 		/* Unsafe singleton,
 		 *   but it doesn't matter if we accidentally (or deliberately)
 		 *   create more than one instance. */
 		private static BusInformation sharedInstance =
 				new BusInformation("56189ff394e20f89ed61a4bf32f1c065", "525bfde7");
 		
 		private final String apiKey;
 		private final String appId;
 		private final HttpClient http;
 		
 		public BusInformation(final String apiKey, final String appId) {
 			this.apiKey = apiKey; this.appId = appId;
 			this.http = new DefaultHttpClient();
 		}
 		
 		private static final int numberOfClosestStops = 5;
 		
 		public List<Stop> closestStops(final Location location) throws Exception {
 			final Uri url = Uri.parse("http://transportapi.com").buildUpon()
 					.path("v3/uk/bus/stops/near.json")
 					.appendQueryParameter("api_key", apiKey)
 					.appendQueryParameter("app_id", appId)
 					.appendQueryParameter("lat", String.valueOf(location.getLatitude()))
 					.appendQueryParameter("lon", String.valueOf(location.getLongitude()))
 					.appendQueryParameter("rpp", String.valueOf(numberOfClosestStops))
 					.appendQueryParameter("page", String.valueOf(1))
 					.build();
 			Log.d("JSON API", String.format("Requesting %s", url));
 			
 			
 			final HttpResponse response =
 					http.execute(new HttpGet(url.toString()));
 			final StatusLine status = response.getStatusLine();
 			
 			if (status.getStatusCode() != HttpStatus.SC_OK) {
 				response.getEntity().getContent().close();
 				throw new IOException(status.getReasonPhrase());
 			}
 			
 			final JSONArray stops;
 			{
 				try {
 					stops = new JSONObject(EntityUtils.toString(response.getEntity()))
 						.getJSONArray("stops");
 				} catch (JSONException e) {
 					throw new Exception("Request returned invalid JSON", e);
 				}
 				if (stops == null) {
 					throw new Exception("Request returned invalid JSON (data missing)");
 				}
 			}
 			
 			final ArrayList<Stop> result = new ArrayList<Stop>(stops.length());
 			for (int i = 0; i < stops.length(); ++i) {
 				final JSONObject stop = stops.getJSONObject(i);
 				result.add(new Stop(
 						stop.getString("atcocode"), stop.getString("name")));
 			}
 			
 			return result;
 		}
 		
 		public List<Arrival> stopTimetable(final Stop stop) throws Exception {
 			final Calendar now = Calendar.getInstance(Locale.UK);
 			
 			final Uri url = Uri.parse("http://transportapi.com").buildUpon()
 					.path(String.format("v3/uk/bus/stop/%s/%s/%s/timetable.json",
 							stop.atcocode,
 							dateFormat.format(now.getTime()),
 							timeFormat.format(now.getTime())))
 					.appendQueryParameter("api_key", apiKey)
 					.appendQueryParameter("app_id", appId)
 					.appendQueryParameter("group", "no")
 					.build();
 			Log.d("JSON API", String.format("Requesting %s", url));
 			
 			final HttpResponse response =
 					http.execute(new HttpGet(url.toString()));
 			final StatusLine status = response.getStatusLine();
 			
 			if (status.getStatusCode() != HttpStatus.SC_OK) {
 				response.getEntity().getContent().close();
 				throw new IOException(status.getReasonPhrase());
 			}
 			
 			final JSONArray arrivals;
 			{
 				try {
 					JSONObject root = new JSONObject(EntityUtils.toString(response.getEntity()));
 					root = root.getJSONObject("departures");
 					if (root == null)
 						throw new Exception("Request returned invalid JSON (data missing)");
 					
 					arrivals = root.getJSONArray("all");
 				} catch (JSONException e) {
 					throw new Exception("Request returned invalid JSON", e);
 				}
 				if (arrivals == null) {
 					throw new Exception("Request returned invalid JSON (data missing)");
 				}
 			}
 			
 			final ArrayList<Arrival> result =
 					new ArrayList<Arrival>(arrivals.length());
 			for (int i = 0; i < arrivals.length(); ++i) {
 				final JSONObject arrival = arrivals.getJSONObject(i);
 				result.add(new Arrival(
 						new Bus(arrival.getString("line"), arrival.getString("operator"), arrival.getString("direction")),
						stop, parseSimpleTime(arrival.getString("aimed_departure_time"))));		
 			}
 			
 			return result;
 		}
 		
 		public List<Arrival> busTimetable(final Arrival arrival) throws Exception {
 			final Calendar now = Calendar.getInstance(Locale.UK);
 			
 			final Uri url = Uri.parse("http://transportapi.com").buildUpon()
 					.path(String.format("v3/uk/bus/route/%s/%s/inbound/%s/%s/%s/timetable",
 							arrival.bus.operator, arrival.bus.route,
 							arrival.stop.atcocode,
 							dateFormat.format(now.getTime()),
 							timeFormat.format(now.getTime())))
 					.appendQueryParameter("api_key", apiKey)
 					.appendQueryParameter("app_id", appId)
 					.appendQueryParameter("group", "no")
 					.build();
 			Log.d("JSON API", String.format("Requesting %s", url));
 			
 			final HttpResponse response =
 					http.execute(new HttpGet(url.toString()));
 			final StatusLine status = response.getStatusLine();
 			
 			if (status.getStatusCode() != HttpStatus.SC_OK) {
 				response.getEntity().getContent().close();
 				throw new IOException(status.getReasonPhrase());
 			}
 			
 			final Document doc = Jsoup.parse(
 					EntityUtils.toString(response.getEntity()), url.toString());
 			final Element stopList = doc.getElementById("busroutelist");
 			final Elements stopListItems = stopList.getElementsByTag("li");
 			
 			ArrayList<Arrival> result = new ArrayList<Arrival>();
 			for (Element stopListItem : stopListItems) {
 				String destcode;
 				String destname;
 				Time desttime;
 				
 				Element timeElement = stopListItem.getElementsByClass("routelist-time").first();
 				desttime = parseSimpleTime(timeElement.text().substring(0, 5));
 				
 				Element destElement = stopListItem.getElementsByClass("routelist-destination").first();
 				String href = destElement.getElementsByTag("a").first().attr("href");
 				destcode = href;
 				if (destcode.startsWith("/v3/uk/bus/stop/")) {
 					destcode = destcode.substring("/v3/uk/bus/stop/".length());
 				}
 				if (destcode.indexOf('/') > 0) {
 					destcode = destcode.substring(0, destcode.indexOf('/'));
 				}
 			
 				destname = destElement.text();
 				
 				result.add(new Arrival(
 						arrival.bus, new Stop(destcode, destname), desttime));
 			}
 			return result;
 		}
 		
 		private static final SimpleDateFormat dateFormat =
 				new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
 		private static final SimpleDateFormat timeFormat =
 				new SimpleDateFormat("HH:mm", Locale.UK);
 		
 		private static Time parseSimpleTime(final String time) throws ParseException {
 			Date raw = timeFormat.parse(time);
 			Calendar now = Calendar.getInstance(Locale.UK);
 			Calendar calendar = Calendar.getInstance(Locale.UK);
 			calendar.setTime(raw);
 			
 			while (calendar.before(now))
 				calendar.add(Calendar.DATE, 1);
 			
 			Time result = new Time();
 			result.set(calendar.getTimeInMillis());
 			return result;
 		}
 }
