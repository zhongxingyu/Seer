 package madisonmay.sensordebugger;
 
 /**
  * Created by mmay on 9/21/13.
  */
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class SensorSnapshotActivity extends Activity implements SensorEventListener {
     private SensorManager sensorManager;
     private HashMap<String, String> d;
     private ArrayList<String> sensors;
     private Button snapshot;
     private TextView status;
     private List<String> files;
     private SnapshotListAdapter aa;
     private ListView snapshots;
     public boolean started;
 
 
     /** Called when the activity is first created. */
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.take_snapshot);
 
         started = false;
         snapshot = (Button) findViewById(R.id.snapshot);
         snapshot.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 started = true;
                 status = (TextView) findViewById(R.id.status);
                 status.setText("Processing...");
             }
         });
 
         sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         sensors = new ArrayList<String>() {{
             add("Accelerometer");
             add("Gravity");
             add("Light");
             add("Magnetic Field");
             add("Gyroscope");
         }};
 
         d = new HashMap<String, String>() {{
             for (String s: sensors) {
                 put(s, "None");
             }
         }};
 
         files = new ArrayList<String>(Arrays.asList(fileList()));
 
         aa = new SnapshotListAdapter(this, android.R.layout.simple_list_item_1, files);
 
         snapshots = (ListView) findViewById(R.id.listView);
         snapshots.setAdapter(aa);
     }
 
     private boolean completed() {
         for (String v : d.values()) {
             if (v == "None") {
                 return false;
             }
         }
         return true;
     }
 
     private void saveData() {
         started = false;
         SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         Date now = new Date();
        String title = "  " + sdfDate.format(now);
         String fulltext = "";
         status.setText("Complete!");
         for (Map.Entry<String, String> entry : d.entrySet()) {
             String k = entry.getKey();
             String v = entry.getValue();
             String text = "  " + k + ": " + v + "\n";
             fulltext += text;
         }
 
         if (title != null && fulltext != null){
             try{
                 FileOutputStream fos = openFileOutput(title, Context.MODE_PRIVATE);
                 fos.write(fulltext.getBytes());
                 fos.close();
                 aa.insert(title,0);
                 aa.notifyDataSetChanged();
             }catch (IOException e){
                 Log.e("IOException", e.getMessage());
             }
         }
 
         snapshots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 final TextView title = (TextView) view.findViewById(R.id.titleTextView);
                 String fileName = title.getText().toString();
                 Intent in = new Intent(getApplicationContext(), SnapshotDetailActivity.class);
                 in.putExtra("file", fileName);
                 startActivity(in);
             }
         });
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         if (started) {
             if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                 getAccelerometer(event);
             }
             else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                 getGravity(event);
             }
             else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                 getLight(event);
             }
             else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                 getMagneticField(event);
             }
             else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                 getGyroscope(event);
             }
 
             if (completed()) {
                 saveData();
             }
         }
     }
 
     private void getAccelerometer(SensorEvent event) {
         float[] values = event.values;
         // Movement
         float x = values[0];
         float y = values[1];
         float z = values[2];
         String text = String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z);
         d.put("Accelerometer", text);
     }
 
     private void getGravity(SensorEvent event) {
         float[] values = event.values;
         // Movement
         float x = values[0];
         float y = values[1];
         float z = values[2];
         String text = String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z);
         d.put("Gravity", text);
     }
 
     private void getLight(SensorEvent event) {
         float[] values = event.values;
 
         float intensity = values[0];
         String text = String.valueOf(intensity);
         d.put("Light", text);
     }
 
     private void getMagneticField(SensorEvent event) {
         float[] values = event.values;
 
         float intensity = values[0];
         String text = String.valueOf(intensity);
         d.put("Magnetic Field", text);
     }
 
     private void getGyroscope(SensorEvent event) {
         float[] values = event.values;
 
         float x = values[0];
         String text = String.valueOf(x);
         d.put("Gyroscope", text);
     }
 
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         //register all listeners
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                 SensorManager.SENSOR_DELAY_NORMAL);
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                 SensorManager.SENSOR_DELAY_NORMAL);
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                 SensorManager.SENSOR_DELAY_NORMAL);
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                 SensorManager.SENSOR_DELAY_NORMAL);
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                 SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     @Override
     protected void onPause() {
         // unregister listener
         super.onPause();
         sensorManager.unregisterListener(this);
     }
 }
