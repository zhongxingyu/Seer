 package com.datakom;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import com.datakom.POIObjects.HaggleConnector;
 
 public class CameraMain extends Activity {
 	private static final String TAG = "Take photo";
 	Camera camera;
 	CameraPreview preview;
 	Button buttonClick;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		preview = new CameraPreview(this);
 		((FrameLayout) findViewById(R.id.preview)).addView(preview);
 
 		buttonClick = (Button) findViewById(R.id.buttonClick);
 		buttonClick.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback,      
 						jpegCallback);
 			}
 		});
 
 		Log.d(TAG, "onCreate'd");
 	}
 
 	ShutterCallback shutterCallback = new ShutterCallback() {
 		public void onShutter() {
 			Log.d(TAG, "onShutter'd");
 		}
 	};
 
 	/** Handles data for raw picture */
 	PictureCallback rawCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			Log.d(TAG, "onPictureTaken - raw");
 		}
 	};
 
 	/** Handles data for jpeg picture */
 	PictureCallback jpegCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			FileOutputStream outStream = null;
 			try {
 				// write to local sandbox file system
 				// outStream =
 				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
 				// System.currentTimeMillis()), 0);
 				// Or write to sdcard
 				
 				outStream = new FileOutputStream(String.format(
 						HaggleConnector.STORAGE_PATH+"/%d.jpg", System.currentTimeMillis()));
 				outStream.write(data);
 				outStream.close();
 				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 			}
 			Log.d(TAG, "onPictureTaken - jpeg");
 		}
 	};
 
 }
