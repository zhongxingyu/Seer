 package name.shimobayashi.pip_boy3030;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
 
 import android.app.Activity;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.widget.TextView;
import android.util.Log;
 
 public class MainActivity extends Activity {
 	private final static int TIMER_INTERVAL = 1000;
	private final static int PUT_INTERVAL = 30 * 1000;
 
 	private Handler handler;
 	private Timer timer;
 	private Timer putTimer;
 	private SoundLevelMeter soundLevelMeter;
 	private TremorSensor tremorSensor;
 	private SensorsViaMicrobridge sensorViaMicrobridge;
 
 	private double tremor = -1;
 	private double db = -1;
 	private double temperature = -1;
 	private double humidity = -1;
 	private int pressure = -1;
 	private int responseCode = -1;
 
 	private double maxTremor = -1;
 	private double maxDb = -1;
 	private double maxTemperature = -1;
 	private double maxHumidity = -1;
 	private int maxPressure = -1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Setup TremorSensor
 		tremorSensor = new TremorSensor(
 				(SensorManager) getSystemService(SENSOR_SERVICE));
 		tremorSensor.setListener(new TremorSensor.TremorSensorListener() {
 			@Override
 			public void onMeasureTremor(double _tremor) {
 				tremor = _tremor;
 
 				maxTremor = Math.max(tremor, maxTremor);
 			}
 		});
 
 		// Setup SoundLevelMeter
 		soundLevelMeter = new SoundLevelMeter();
		soundLevelMeter.setBaseValue(12.0);
 		soundLevelMeter
 				.setListener(new SoundLevelMeter.SoundLevelMeterListener() {
 					@Override
 					public void onMeasureSoundLevel(double _db) {
 						db = _db;
 
 						maxDb = Math.max(db, maxDb);
 					}
 				});
 		(new Thread(soundLevelMeter)).start();
 
 		// Setup SensorViaMicrobridge
 		sensorViaMicrobridge = new SensorsViaMicrobridge();
 		sensorViaMicrobridge
 				.setListener(new SensorsViaMicrobridge.SensorsViaMicrobridgeListener() {
 					@Override
 					public void onMeasureViaMicrobridge(double _temperature,
 							double _humidity, int _pressure) {
 						temperature = _temperature;
 						humidity = _humidity;
 						pressure = _pressure;
 
 						maxTemperature = Math.max(temperature, maxTemperature);
 						maxHumidity = Math.max(humidity, maxHumidity);
 						maxPressure = Math.max(pressure, maxPressure);
 					}
 				});
 
 		// Call onTimer every TIMER_INTERVAL
 		handler = new Handler();
 		timer = new Timer(true);
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 						onTimer();
 					}
 				});
 			}
 		}, TIMER_INTERVAL, TIMER_INTERVAL);
 		putTimer = new Timer(true);
 		putTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 						onPutTimer();
 					}
 				});
 			}
 		}, PUT_INTERVAL, PUT_INTERVAL);
 	}
 
 	private void onTimer() {
 		TextView textView;
 
 		// Show tremor
 		textView = (TextView) findViewById(R.id.tremor_textview);
 		textView.setText("Tremor : " + tremor);
 
 		// Show sound level
 		textView = (TextView) findViewById(R.id.soundlevel_textview);
 		textView.setText("Sound Level : " + db + "(dB)");
 
 		// Show temperature
 		textView = (TextView) findViewById(R.id.temperature_textview);
 		textView.setText("Temperature : " + temperature + "(C)");
 
 		// Show humidity
 		textView = (TextView) findViewById(R.id.humidity_textview);
 		textView.setText("Humidity : " + humidity + "(%)");
 
 		// Show pressure
 		textView = (TextView) findViewById(R.id.pressure_textview);
 		textView.setText("Pressure : " + pressure + "(Pa)");
 
 		// Show responseCode
 		textView = (TextView) findViewById(R.id.responsecode_textview);
 		textView.setText("Response Code: " + responseCode);
 
 		// Show time
 		Date date = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
 		textView = (TextView) findViewById(R.id.datetime_textview);
 		textView.setText(sdf.format(date));
 	}
 
 	private void onPutTimer() {
 		responseCode = -1;
 
 		int feedId = 89487;
 		URI url = URI.create("http://api.cosm.com/v2/feeds/" + feedId);
 		HttpPut request = new HttpPut(url);
 		String apiKey = getString(R.string.cosm_api_key);
 		request.addHeader("X-ApiKey", apiKey);
 
 		StringEntity entity;
 		try {
 			entity = new StringEntity(
 					"{\"datastreams\":[{\"id\":\"temperature\",\"current_value\":\""
 							+ maxTemperature
 							+ "\"},{\"id\":\"bed-tremor\",\"current_value\":\""
 							+ maxTremor
 							+ "\"},{\"id\":\"bed-sound-level\",\"current_value\":\""
 							+ maxDb
 							+ "\"},{\"id\":\"humidity\",\"current_value\":\""
 							+ maxHumidity
 							+ "\"},{\"id\":\"pressure\",\"current_value\":\""
 							+ maxPressure + "\"}]}", "UTF-8");
 			entity.setContentType("application/x-www-form-urlencoded");
 			request.setEntity(entity);
 
 			// Do PUT
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpResponse response = httpClient.execute(request);
			//Log.d("entity", EntityUtils.toString(response.getEntity()));
 			responseCode = response.getStatusLine().getStatusCode();
 
 			// Reset
 			maxTremor = -1;
 			maxDb = -1;
 			maxTemperature = -1;
 			maxHumidity = -1;
 			maxPressure = -1;
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		tremorSensor.pause();
 		soundLevelMeter.pause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		tremorSensor.resume();
 		soundLevelMeter.resume();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		soundLevelMeter.stop();
 		sensorViaMicrobridge.stop();
 	}
 
 }
