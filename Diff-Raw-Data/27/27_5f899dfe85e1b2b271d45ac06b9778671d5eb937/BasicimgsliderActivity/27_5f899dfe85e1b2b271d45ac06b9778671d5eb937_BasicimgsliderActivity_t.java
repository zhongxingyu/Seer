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
 
public class BasicimgsliderActivity extends Activity {
 	private GestureLibrary gestureLib;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     }

 }
