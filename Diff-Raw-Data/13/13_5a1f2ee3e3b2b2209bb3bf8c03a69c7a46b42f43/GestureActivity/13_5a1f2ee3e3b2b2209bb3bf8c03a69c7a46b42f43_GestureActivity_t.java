 package com.example.famtreeapp;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.GestureOverlayView.OnGestureListener;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 import android.gesture.Prediction;
 import android.graphics.Color;
import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class GestureActivity extends Activity implements OnGesturePerformedListener{
 	GestureLibrary   gestureLib;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		
 		super.onCreate(savedInstanceState);
		
 		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
 		View inflate = getLayoutInflater().inflate(R.layout.activity_gesture, null);
 		gestureOverlayView.setGestureColor(Color.rgb(0, 255, 0));
 		gestureOverlayView.setGestureVisible(true);
 		gestureOverlayView.setUncertainGestureColor(Color.rgb(255, 0, 0));
 		gestureOverlayView.addView(inflate);
 		gestureOverlayView.addOnGesturePerformedListener(this);
 		
 	    gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
	    if(!gestureLib.load()){
	    	Log.i("debug", "GestureLib not loaded");
	    }
	    else
	    	Log.i("debug", "GestureLib loaded");
	    setContentView(gestureOverlayView);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.gesture, menu);
 		return true;
 	}
 
 	@Override
 	public void onGesturePerformed(GestureOverlayView arg0, Gesture g) {
 		ArrayList<Prediction> predictions = gestureLib.recognize(g); 
 		if(predictions.size() >0 ){
 			Prediction prediction = predictions.get(0);
 			if(prediction.score >1){
 				String s = prediction.name;
 				if(s.equals("UP") || s.equals("up"))
 					Toast.makeText(getApplicationContext(), "UP", 1).show();
 				else if(s.equals("DOWN") || s.equals("down"))
 					Toast.makeText(getApplicationContext(), "DOWN", 1).show();
 			}
 		}
 		
 	}
 
 }
