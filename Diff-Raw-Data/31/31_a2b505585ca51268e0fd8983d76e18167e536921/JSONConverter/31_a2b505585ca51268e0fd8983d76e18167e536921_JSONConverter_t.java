 package vision.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import vision.Config;
 
 public class JSONConverter {
 	
 	private static final Logger LOG = Logger.getLogger(JSONConverter.class.getName());
 
 	public JSONConverter() {
 		this.sensorList = new ArrayList<Sensor>();
 	}
 
 	private JSONObject json;
 	private Sensor sensor;
 	private List<Sensor> sensorList;
 
 	public String getUrl() {
 		return Config.SERVER_URL;
 	}
 
 	public JSONObject getJson() {
 		return this.json;
 	}
 
 	public String offlineStream() {
 		File file = new File("offlinestream");
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 			return br.readLine();
 		} catch (FileNotFoundException e) {
 			LOG.warning("File offlinestream not found!");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "Error.";
 
 	}
 
 	public String getJSONStream() {
 		try {
 			URL url = new URL(getUrl());
 			LOG.info("Connecting to " + Config.SERVER_URL + "...");
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			LOG.info("Connection successful.");
			conn.setReadTimeout(500);
 			BufferedReader br = new BufferedReader(new InputStreamReader(conn
 					.getInputStream()));
 			
 
 			String content = br.readLine();
 			return content;
 		} catch (MalformedURLException e) {
 			LOG.warning("Connection Error(1). Using offlinestream instead...");
 			return offlineStream();
 		} catch (IOException e) {
 			LOG.warning("Connection Error(2). Using offlinestream instead...");
 			return offlineStream();
 		}
 
 	}
 
 	public void convert() {
 		String stream = getJSONStream();
 		JSONObject jo;
 
 		try {
 			jo = new JSONObject(stream);
 			for (int sensorid = 0; sensorid < jo.length(); sensorid++) {
 
 				JSONObject lvl1 = jo
 						.getJSONObject(JSONObject.getNames(jo)[sensorid]);
 				sensor = new Sensor();
 
 				for (int type = 0; type < jo.getJSONObject(
 						JSONObject.getNames(jo)[sensorid])
 						.getJSONObject("data").length(); type++) {
 
 					JSONObject lvl2 = lvl1.getJSONObject("data");
 					JSONObject temp = jo.getJSONObject(
 							JSONObject.getNames(jo)[sensorid]).getJSONObject(
 							"data");
 					Sample sample = new Sample();
 
 					JSONObject lvl3 = lvl2.getJSONObject(JSONObject
 							.getNames(lvl2)[type]);
 
 					// sample.setSensor(sensor);
 
 					sample.setTyp(JSONObject.getNames(lvl2)[type]);
 					sample.setUnit(lvl3.getString("unit"));
 					sample.setUpdate(Long.parseLong(lvl3.getString("updated")));
 					sample.setValue(Float.parseFloat(lvl3.getString("value")));
 
 					sensor.addToSamples(sample);
 				}
 
 				JSONArray ja = lvl1.getJSONArray("tags");
 				for (int i = 0; i < ja.length(); i++) {
 					sensor.addToTags(ja.getString(i));
 				}
 				sensor.setUpdate(Long.parseLong(lvl1.getString("updated")));
 				sensor.setDescription(lvl1.getString("description"));
 				sensor.setPosition(new Position(0, 0, 0));
 				sensor.setId(JSONObject.getNames(jo)[sensorid]);
 
 				this.addSensorToList();
 
 			}
 
 		} catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 	
 	public Position calcLocalPos() {
 		return null;
 	}
 	
 	public void resetList() {
 		this.sensorList = null;
 	}
 
 	public void addSensorToList() {
 		this.sensorList.add(this.sensor);
 	}
 
 	public List<Sensor> getSensorList() {
 		return this.sensorList;
 	}
 
 }
