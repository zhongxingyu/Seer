 package com.test.basicimgslider;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 import android.gesture.Prediction;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
public class BasicimgsliderActivity extends Activity implements OnGesturePerformedListener {
 	private GestureLibrary gestureLib;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.main,null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.addOnGesturePerformedListener(this);
        gestureOverlayView.setGestureColor(Color.rgb(0,255,0));
        gestureOverlayView.setGestureVisible(true);
        gestureLib = GestureLibraries.fromRawResource(this,R.raw.gestures);
        if(!gestureLib.load()){
        	finish();
        }
        setContentView(gestureOverlayView);
     }
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions =gestureLib.recognize(gesture);
		for(Prediction prediction:predictions){
			if(prediction.score > 1.0){
				Toast.makeText(this,prediction.name,Toast.LENGTH_SHORT).show();
			}
		}
		
	}
 }
