 package com.cruzroja.creuroja;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 import com.google.android.gms.maps.model.LatLng;
 
 /**
  * Structure for the JSON: Array with repeatable objects with the following
  * content:
  * 
  * [{position:{lat:<double> latitud, lng:<double> longitud},icon:'<String>
  * name', <contenido>:"String html"}, {same as before, different values}, ...,
  * {}]
  */
 public class JSONParser {
 	private static final String FILE_NAME_FIJOS = "fijos";
 	private static final String FILE_NAME_VARIABLES = "variables";
 
 	public static final String sPos = "position";
 	public static final String sLat = "lat";
 	public static final String sLong = "lng";
 	public static final String sIcon = "icon";
 	public static final String sContent = "contenido";
 
 	public static final String sStatus = "status";
 	public static final String sRoutes = "routes";
 	public static final String sLegs = "legs";
 	public static final String sSteps = "steps";
 	public static final String sStartLocation = "start_location";
 	public static final String sEndLocation = "end_location";
 
 	public static final int STATUS_OK = 0;
 	public static final int STATUS_NOT_OK = 1;
 
 	public static ArrayList<Location> parseLocations(String data) {
 		try {
 			JSONArray array = new JSONArray(data);
 
 			ArrayList<Location> locationsList = new ArrayList<Location>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject jsonLocation = array.getJSONObject(i);
 				JSONObject jsonPosition = jsonLocation.getJSONObject(sPos);
 				Location location = new Location(jsonPosition.getDouble(sLat),
 						jsonPosition.getDouble(sLong),
 						jsonLocation.getString(sIcon),
 						jsonLocation.getString(sContent));
 				locationsList.add(location);
 			}
 			return locationsList;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static void saveToDisk(Context context, String data, boolean isFijo) {
 		PrintStream ps = null;
 		try {
 			if (isFijo) {
 				ps = new PrintStream(context.openFileOutput(FILE_NAME_FIJOS,
 						Context.MODE_PRIVATE));
 			} else {
 				ps = new PrintStream(context.openFileOutput(
 						FILE_NAME_VARIABLES, Context.MODE_PRIVATE));
 			}
 			ps.print(data);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			ps.close();
 		}
 	}
 
 	public static ArrayList<Location> getFromDisk(Context context) {
 		ArrayList<Location> locationList = null;
 
 		String data = getDataFromFile(context, FILE_NAME_FIJOS);
 		if (data.equals("")) {
 			return null;
 		}
 		locationList = parseLocations(data);
 		data = getDataFromFile(context, FILE_NAME_VARIABLES);
 		if (data.equals("")) {
 			return null;
 		}
 		locationList.addAll(parseLocations(data));
 
 		return locationList;
 	}
 
 	public static String getDataFromFile(Context context, String fileName) {
 		String data = "";
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					context.openFileInput(fileName)));
 			String line = "";
 
 			while ((line = reader.readLine()) != null) {
 				data = data + line;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return "";
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "";
 		}
 		return data;
 	}
 
 	public static ArrayList<LatLng> getPoints(String fullResponse) {
 		ArrayList<LatLng> points = new ArrayList<LatLng>();
 		try {
 			JSONObject response = new JSONObject(fullResponse);
 			if (parseStatus(response.getString(sStatus)) == STATUS_OK) {
 				JSONArray routes = response.getJSONArray(sRoutes);
 				for (int i = 0; i < routes.length(); i++) {
 					JSONObject route = routes.getJSONObject(i);
 					JSONArray legs = route.getJSONArray(sLegs);
 					for (int j = 0; j < legs.length(); j++) {
 						JSONObject leg = legs.getJSONObject(j);
 						JSONArray steps = leg.getJSONArray(sSteps);
 						for (int k = 0; k < steps.length(); k++) {
 							JSONObject step = steps.getJSONObject(k);
 							JSONObject startLocation = step
 									.getJSONObject(sStartLocation);
 							JSONObject endLocation = step
 									.getJSONObject(sEndLocation);
 							points.add(new LatLng(
 									startLocation.getDouble(sLat),
 									startLocation.getDouble(sLong)));
 							points.add(new LatLng(
 									endLocation.getDouble(sLat),
 									endLocation.getDouble(sLong)));
 						}
 					}
 				}
 			} else {
 				// Handle error
 				return null;
 			}
 		} catch (JSONException e) {
 			return null;
 		}
 		return points;
 	}
 
 	public static int parseStatus(String status) {
 		if (status.equals("OK") || status.equals("ZERO_RESULTS")) {
 			return STATUS_OK;
 		} else {
 			return STATUS_NOT_OK;
 		}
 	}
 }
