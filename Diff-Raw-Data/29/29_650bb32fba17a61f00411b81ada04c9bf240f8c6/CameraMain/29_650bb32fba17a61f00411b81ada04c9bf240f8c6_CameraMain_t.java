 package com.datakom;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import com.datakom.POIObjects.HaggleConnector;
 
 public class CameraMain extends Activity {
 	Camera camera;
 	CameraPreview preview;
 	Button buttonClick;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.camera);
 
 		preview = new CameraPreview(this);
 		((FrameLayout) findViewById(R.id.preview)).addView(preview);
 
 		buttonClick = (Button) findViewById(R.id.buttonClick);
 		buttonClick.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				preview.camera.takePicture(shutterCallback, rawCallback,      
 						jpegCallback);
 			}
 		});
 
 		Log.d(getClass().getSimpleName(), "onCreate'd");
 	}
 
 	ShutterCallback shutterCallback = new ShutterCallback() {
 		public void onShutter() {
 			Log.d(getClass().getSimpleName(), "onShutter'd");
 		}
 	};
 
 	/** Handles data for raw picture */
 	PictureCallback rawCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			Log.d(getClass().getSimpleName(), "onPictureTaken - raw");
 		}
 	};
 
 	/** Handles data for jpeg picture */
 	PictureCallback jpegCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			FileOutputStream outStream = null;
 			try {
 				File dir = new File(HaggleConnector.STORAGE_PATH);
 				dir.mkdirs(); //creating directory if missing
 				String filename = "HagglePOI-" + System.currentTimeMillis() + ".jpg";
 				String filepath = HaggleConnector.STORAGE_PATH + "/" + filename;
 				
 				outStream = new FileOutputStream(filepath);
 				outStream.write(data);
 				outStream.close();
 				Log.d(getClass().getSimpleName(), "onPictureTaken - wrote bytes: " + data.length);
 				
 				Intent i = new Intent();
 				i.putExtra("filename", filename);
 				setResult(1000, i); //returning value
 			} catch (FileNotFoundException e) {
 				Log.e(getClass().getSimpleName(), "file not found: " + e.getMessage());
 			} catch (IOException e) {
 				Log.e(getClass().getSimpleName(), "IO Exception: " + e.getMessage());
 			} 
 		}
 	};
 }
