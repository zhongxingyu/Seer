 package rvmiller.gravity;
 
 import java.util.List;
 import java.math.*;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements SensorEventListener {
 	
 	private String TAG = "GravityApp";
 	private SensorManager sensorManager;
 	private Sensor accel;
 	private ImageView arrowImage;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         arrowImage = (ImageView) this.findViewById(R.id.imageView1);
         sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         
         accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         if (accel == null)	{
         	Log.e(TAG, "No accelerometer found");
         	Toast.makeText(this, "No accelerometer found", Toast.LENGTH_LONG);
         }
     }
     
     @Override
     public void onResume()	{
     	super.onResume();
         sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
     }
     
     @Override
     public void onPause()	{
     	super.onPause();
     	sensorManager.unregisterListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		float x, y, z;
 		x = event.values[0];
 		y = event.values[1];
 		z = event.values[2];
 		Log.d(TAG, "x: " + x + ", y: " + y + ", z: " + z);
 		double theta = Math.atan2(y, x);
 		double degree = ((theta * -180.0) / 3.14159) + 180;
 		Log.d(TAG, "Degree: " + degree);
 		
 		float rotateDegree = (float) ((degree + 270.0) % 360.0);
 		
 		Bitmap myImg = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
 
 		Matrix matrix = new Matrix();
 		matrix.setRotate(rotateDegree);
 
 		Bitmap rotated = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(),
 		        matrix, true);
 
 		arrowImage.setImageBitmap(rotated);
 	}
 }
