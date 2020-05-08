 package at.roadrunner.android.controller;
 
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Context;
 import at.roadrunner.android.couchdb.CouchDBException;
 import at.roadrunner.android.couchdb.HttpExecutor;
 import at.roadrunner.android.couchdb.RequestWorker;
 import at.roadrunner.android.sensor.SensorData;
 
 public class MonitoringController {
 	private Context _context;
 	private RequestWorker _requestWorker;
 	
 	public MonitoringController(Context context) {
 		_context = context;
 		_requestWorker = new RequestWorker(_context);
 	}
 	
 	public void readSensorData()  {
 		readTemperaturSensor();
 	}
 	
 	private void readTemperaturSensor()  {
 		JSONArray sensors = _requestWorker.getSensorsForSelectedContainer();
 		float temperature = 0;
 		int count = 0;
		
		if (sensors != null) {
			for (int i=0; i<sensors.length(); i++)  {
				try {
					String url = sensors.getString(i);
					String temp = HttpExecutor.getInstance().executeForResponse(new HttpGet(url));
					temperature = Float.parseFloat(temp);
					count++;
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (CouchDBException e) {
					e.printStackTrace();
				}
 			}
			SensorData.setTemperature(temperature/count);
 		}
 	}
 }
