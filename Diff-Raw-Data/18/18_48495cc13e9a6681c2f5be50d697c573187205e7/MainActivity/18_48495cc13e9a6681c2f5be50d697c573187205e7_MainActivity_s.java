 package com.appliedmobileapps.TwoDto3d;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Set;
 
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements PictureCallback, SensorEventListener{
 
 	private final static String TAG = MainActivity.class.getSimpleName();
 	
 	private final static String MASTER_CSV_FILE = "master.csv";
 	
 //	private ImageView mainImage;
 	private Button takePictureBtn;
 	
 	private Camera mCamera = null;
 	private Preview mPreview;
 	private int cameraId = 0;
 	
 	//Set<String> imageFilesList;
 	
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	private Sensor magField;
 	
 	float yaw;
     float pitch;
     float roll;
     
     String yawPitchRollOfPicture = "";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		
 		// Create an instance of Camera
         mCamera = getCameraInstance();
 
         // Create our Preview view and set it as the content of our activity.
         mPreview = new Preview(this, mCamera);
         FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
         preview.addView(mPreview);
 
 		
 		takePictureBtn = (Button)findViewById(R.id.pictureBtn);
 		takePictureBtn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 
 				//Take Picture!
 				if(mCamera!=null)
 				{
					mCamera.takePicture(null, null, MainActivity.this);//new PhotoHandler(getApplicationContext()));
					yawPitchRollOfPicture = "Yaw_"+yaw+"_Pitch_"+pitch+"_Roll_"+roll;
 				}
 				
 			}
 			
 		});
 		
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		
 		
 		
 		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 		sensorManager.registerListener(this, magField, SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	
 	@Override
 	protected void onPause() 
 	{
 		super.onPause();
 		
 		if (mCamera != null) 
 		{
 			mCamera.release();
 			mCamera = null;
 		}
 		
 		sensorManager.unregisterListener(this);
 	}
 	
 	
 	/** A safe way to get an instance of the Camera object. */
 	public Camera getCameraInstance(){
 	    Camera c = null;
 
 	    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) 
 		{
 			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 			cameraId = findBackFacingCamera();
 			
 			if (cameraId < 0)
 			{
 				Toast.makeText(this, "No back facing camera found.", Toast.LENGTH_LONG).show();
 				
 				//try to get ANY camera if possible...
 				try {
 			        c = Camera.open(); // attempt to get a Camera instance
 			    }
 			    catch (Exception e){
 			        // Camera is not available (in use or does not exist)
 			    }
 			} 
 			else 
 			{
 				try {
 			        c = Camera.open(cameraId); // attempt to get a Camera instance
 			    }
 			    catch (Exception e){
 			        // Camera is not available (in use or does not exist)
 			    }
 			}
 		}
 
 	    return c; // returns null if camera is unavailable
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	
 	private int findBackFacingCamera() 
 	{
 		int cameraId = -1;
 		// Search for the back facing camera
 		int numberOfCameras = Camera.getNumberOfCameras();
 		for (int i = 0; i < numberOfCameras; i++) 
 		{
 			CameraInfo info = new CameraInfo();
 			Camera.getCameraInfo(i, info);
 			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
 				Log.d(TAG, "Camera found");
 				cameraId = i;
 				break;
 			}
 		}
 		return cameraId;
 	}
 
 	@Override
 	public void onPictureTaken(byte[] data, Camera camera) 
 	{
 
 		File pictureFileDir = getDir();
 
 		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) 
 		{
 			Log.d(TAG, "Can't create directory to save image.");
 			
 			Toast.makeText(this, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
 			return;
 
 		}
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss", Locale.US);
 		String date = dateFormat.format(new Date());
 		String photoFile = "Picture_" + date + "_"+yawPitchRollOfPicture+".jpg";
 
 		String filename = pictureFileDir.getPath() + File.separator + photoFile;
 
 		File pictureFile = new File(filename);
 
 		FileOutputStream fos = null;
 		
 		try {
 			fos = new FileOutputStream(pictureFile);
 			fos.write(data);
 			
 			//imageFilesList.add(filename);
 			
 			
 			
 			//updateMasterCsv(filename+yawPitchRollOfPicture+"\n");
 			
 			Toast.makeText(this, "New Image saved:" + photoFile, Toast.LENGTH_LONG).show();
 		} 
 		catch (Exception error) 
 		{
 			Log.d(TAG, "File" + filename + "not saved: " + error.getMessage());
 
 			Toast.makeText(this, "Image could not be saved.", Toast.LENGTH_LONG).show();
 		}
 		finally
 		{
 			if(fos!=null)
 			{
 				try {
 					fos.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 		}
 	}
 	
 	private File getDir() 
 	{
 		File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 		return new File(sdDir, "TwoDTo3D_Picts");
 	}
 	
 	
 	private void updateMasterCsv(String data)
 	{
 		File dataFile = new File(MASTER_CSV_FILE);
 
 		OutputStreamWriter outputStreamWriter = null;
 		try {
             outputStreamWriter = new OutputStreamWriter(openFileOutput(MASTER_CSV_FILE, Context.MODE_APPEND));
             
             outputStreamWriter.write(data);
                         
             outputStreamWriter.close();
         }
         catch (IOException e) {
             Log.e(TAG, "File write failed: " + e.toString());
             
         }
 		finally
 		{
 			if(outputStreamWriter!=null)
 			{
 				try {
 					outputStreamWriter.close();
 				} catch (IOException e) {
 					
 				}
 			}
 		}
 	}
 	
 	
 	/****************************************
 	 * Sensor stuffs
 	 */
 	
 	float []lastMagFields;		
     float []lastAccels;			
     private float[] rotationMatrix = new float[16];
     private float[] orientation = new float[4];
     
     
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) 
 	{
 		switch(event.sensor.getType())
 		{
 		case Sensor.TYPE_ACCELEROMETER:
 			getAccelerometer(event);
 			break;
 		case Sensor.TYPE_MAGNETIC_FIELD:
 			getMag(event);
 			break;
 		}
 		
 		
 	}
 	
 	private void getAccelerometer(SensorEvent event)
 	{
 				
 		if (lastAccels == null) {
             lastAccels = new float[3];
         }
  
         System.arraycopy(event.values, 0, lastAccels, 0, 3);
 	}
 	
 	private void getMag(SensorEvent event)
 	{
 		if (lastMagFields == null) {
             lastMagFields = new float[3];
         }
  
         System.arraycopy(event.values, 0, lastMagFields, 0, 3);
  
         if (lastAccels != null) {
             calcOrientation();
         }
 
 	}
 	
 	private void calcOrientation()
 	{
 		if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccels, lastMagFields)) 
 		{
 			SensorManager.getOrientation(rotationMatrix, orientation);
 			
 			//convert radians to degrees
 			yaw = orientation[0] * 57.2957795f;
 	        pitch = orientation[1] * 57.2957795f;
 	        roll = orientation[2] * 57.2957795f;
 	        
 	        ((TextView)findViewById(R.id.yaw)).setText("Yaw: "+yaw);
 	        ((TextView)findViewById(R.id.pitch)).setText("Pitch: "+pitch);
 	        ((TextView)findViewById(R.id.roll)).setText("Roll: "+roll);
 		}
 	}
 	
 	
 }
