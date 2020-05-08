 package com.androidmontreal.gesture.commander;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 import android.gesture.Prediction;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 import com.androidmontreal.arduino.commander.R;
 
 public class GestureVoiceCommander extends Activity implements
 		OnGesturePerformedListener {
 	private GestureLibrary gestureLib;
 	 // Debugging
     private static final String TAG = "RoogleCommander";
     private static final boolean D = true;
 
 	/** Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
 		View inflate = getLayoutInflater().inflate(R.layout.commander, null);
 		gestureOverlayView.addView(inflate);
 		gestureOverlayView.addOnGesturePerformedListener(this);
 		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
 		if (!gestureLib.load()) {
 			finish();
 		}
 		setContentView(gestureOverlayView);
 	}
 
 	@Override
 	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
 		ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
 		for (Prediction prediction : predictions) {
 			if (prediction.score > 3.0) {
 				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT)
 						.show();
 				Log.d(TAG, "Detected this gesture "+prediction.name+" with a score of "+prediction.score);
 			}
 		}
 	}
 }
