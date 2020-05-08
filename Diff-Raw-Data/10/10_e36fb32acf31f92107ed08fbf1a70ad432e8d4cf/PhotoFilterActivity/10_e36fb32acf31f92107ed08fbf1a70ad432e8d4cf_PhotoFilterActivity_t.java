 package org.openparallel.photofilter;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.hardware.Camera;
 
 public class PhotoFilterActivity extends Activity {
     /** Called when the activity is first created. */
	
	static {
        System.loadLibrary("libjni_mosaic");
        System.loadLibrary("opencv");
    }

	protected Camera mCameraDevice;

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 }
