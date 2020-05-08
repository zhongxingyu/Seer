 package com.example.accsel;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements SensorEventListener {
 	private SensorManager sensorManager;
 
 	TextView xCoor; // declare X axis object
 	TextView yCoor; // declare Y axis object
 	TextView zCoor; // declare Z axis object
 	TextView recNum;
 	File dataDir;
 	File saveFile;
 	PrintWriter csvWriter;
 	Time now = new Time();
 
 	public void startListen(View view) {
 		Log.v("Axel Listener", "Starting listener");

 		now.setToNow();
 		String TimeStampDB = now.format("%F_%H-%M-%S");
 		
 		int FileNum = dataDir.list().length + 1;
 		recNum.setText("Record # : " + FileNum);
 		if(recNum.getVisibility()!=android.view.View.VISIBLE)
 			recNum.setVisibility(android.view.View.VISIBLE);
 		saveFile = new File(dataDir.getAbsolutePath() + "/" + FileNum + "_"
 				+ TimeStampDB + ".csv");
 		if (!saveFile.exists())
 			try {
 				saveFile.createNewFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		try {
 			csvWriter = new PrintWriter(new FileWriter(saveFile, true));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		sensorManager.registerListener(this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
 				SensorManager.SENSOR_DELAY_FASTEST);
 	}
 
 	public void stopListen(View view) {
 		Log.v("Axel Listener", "Stopping listener");
 		sensorManager.unregisterListener(this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
 		csvWriter.close();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {

 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		xCoor = (TextView) findViewById(R.id.xcoor); // create X axis object
 		yCoor = (TextView) findViewById(R.id.ycoor); // create Y axis object
 		zCoor = (TextView) findViewById(R.id.zcoor); // create Z axis object
 		recNum = (TextView) findViewById(R.id.recnum);
 
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		String extStorageDirectory = Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + "/axel";
 		dataDir = new File(extStorageDirectory);
 		if (!dataDir.exists())
 			dataDir.mkdir();
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 
 		// check sensor type
 		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
 
 			// assign directions
 			float x = event.values[0];
 			float y = event.values[1];
 			float z = event.values[2];
 
 			try {
 				writeToLog(x, y, z,event.timestamp);
 			} catch (IOException e) {
 				e.printStackTrace();
 				stopListen(null);
 			}
 
 			xCoor.setText("X: " + x);
 			yCoor.setText("Y: " + y);
 			zCoor.setText("Z: " + z);
 		}
 	}
 
 	public void writeToLog(float x, float y, float z,long nanos) throws IOException {
 
 		try {
 			csvWriter.print(x + ";" + y + ";" + z + ";" + nanos	+ "\n");
 
 		} catch (Exception er) {
 			er.printStackTrace();
 			stopListen(null);
 		}
 
 	}
 }
