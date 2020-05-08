 package dk.itu.restconnection;
 
 import java.io.BufferedReader;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class RestClient {
 	
 	static final String BUILDINGINFO_URL = "http://gsd.itu.dk/api/user/building/entry/description/3/?format=json";
 	static final String MEASURE_URL = "http://gsd.itu.dk/api/user/measurement/?bid=3&limit=%25s&order_by=-timestamp&format=json&uuid=%s";
 	static final String SET_URL = "http://gsd.itu.dk/api/user/building/entry/set/3/%s/%s/?format=json";
 	BufferedReader br = null;
 	BuildingPlan building;
 	
 	public BuildingPlan getBuildingPlan() {
 		Gson gson = new GsonBuilder()
 		.registerTypeAdapter(BuildingPlan.class, new BuildingPlanDeserializer())
 		.create();
 		
		String json = getJsonString(BUILDINGINFO_URL, "GET");
 		return gson.fromJson(json, BuildingPlan.class);	
 	}
 	
 	public String setGain(String uuid, String value)
 	{
 		Gson gson = new GsonBuilder().create();
 		
		String json = getJsonString(SET_URL, uuid, value);
 		
 		return gson.fromJson(json, String.class);
 	}
 	
 	public Measurement[] getMeasurements(String uuid, int numOfMinutes) {
 		//Measurements are taken every 15 seconds. Request limit is calculated.
 		String limit = Integer.toString(numOfMinutes * 4 + 1);
 		
 		Gson gson = new GsonBuilder()
 		.registerTypeAdapter(Measurement[].class, new MeasurementDeserializer())
 		.create();
 		
		String json = getJsonString(MEASURE_URL, limit, uuid);
 		return gson.fromJson(json, Measurement[].class);
 	}
 	
	private String getJsonString(String url, String... params ) {
 
 		try {
 			//Inject parameters into URL.
 			url = String.format(url, params);
 			url = url.replaceAll("\\s","");
 			// connecting to restAPI
 			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
 			conn.setRequestProperty("Accept", "application/json");
 
 			// If connection successful
 			if (conn.getResponseCode() != 200) {
 				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
 			}
 			InputStreamReader input = new InputStreamReader((conn.getInputStream()));
 			
 			br = new BufferedReader(input);
 			String line = null;
 			StringBuffer json = new StringBuffer();
 			while((line=br.readLine())!=null){
 			   json.append(line+"\n");
 			}
 			br.close();
 			conn.disconnect();
 			
 			return json.toString();
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 }
