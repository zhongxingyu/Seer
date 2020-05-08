 package com.cloud2bubble.ptsense.database;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.cloud2bubble.ptsense.list.ReviewItem;
 
 public class TripData implements ServerObject {
 
 	private static final String TYPE = "trip_data";
 
 	private ReviewItem trip;
 	private List<Calendar> timestamps = new ArrayList<Calendar>();
 	private Map<String, ArrayList<Float>> data = new HashMap<String, ArrayList<Float>>();
 
 	public TripData(ReviewItem trip, ArrayList<Calendar> timestamps,
 			Map<String, ArrayList<Float>> data) {
 		this.trip = trip;
 		this.timestamps = timestamps;
 		this.data = data;
 	}
 
 	public ReviewItem getTrip() {
 		return trip;
 	}
 
 	public List<Calendar> getTimestamps() {
 		return timestamps;
 	}
 
 	public Map<String, ArrayList<Float>> getData() {
 		return data;
 	}
 
 	public JSONObject toJSON() {
 		JSONObject jsonTripData = new JSONObject();
 		try {
 			jsonTripData.put("type", TYPE);
 
 			// trip info
 			JSONObject jsonTrip = new JSONObject();
 			jsonTrip.put("database_id", trip.getId());
 			jsonTrip.put("reviewed", trip.isReviewed());
 			jsonTrip.put("line", trip.line);
 			jsonTrip.put("service", trip.service);
 			jsonTrip.put("origin", trip.origin);
 			jsonTrip.put("destination", trip.destination);
 
 			SimpleDateFormat sdfDate = new SimpleDateFormat(
 					"yyyy-MM-dd HH:mm:ss");
 
 			jsonTrip.put("start_time", sdfDate.format(trip.startTime.getTime()));
 			jsonTrip.put("end_time", sdfDate.format(trip.endTime.getTime()));
 			jsonTripData.put("trip_info", jsonTrip);
 
			// sensor data
			JSONObject jsonSensorData = new JSONObject();

 			// timestamps
 			JSONArray jsonTimestamps = new JSONArray();
 			for (Calendar t : timestamps) {
 				jsonTimestamps.put(sdfDate.format(t.getTime()));
 			}
			jsonSensorData.put("timestamps", jsonTimestamps);
 
 			Iterator<Entry<String, ArrayList<Float>>> it = data.entrySet()
 					.iterator();
 			while (it.hasNext()) {
 				Map.Entry<String, ArrayList<Float>> pairs = (Map.Entry<String, ArrayList<Float>>) it
 						.next();
 				// populate array with sensor values
 				JSONArray jsonValues = new JSONArray();
 				for (Float f : pairs.getValue()) {
 					if (f.isNaN())
 						jsonValues.put(JSONObject.NULL);
 					else
 						jsonValues.put(f);
 				}
 				jsonSensorData.put(pairs.getKey(), jsonValues);
 			}
 			jsonTripData.put("sensor_data", jsonSensorData);
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		return jsonTripData;
 	}
 
 }
