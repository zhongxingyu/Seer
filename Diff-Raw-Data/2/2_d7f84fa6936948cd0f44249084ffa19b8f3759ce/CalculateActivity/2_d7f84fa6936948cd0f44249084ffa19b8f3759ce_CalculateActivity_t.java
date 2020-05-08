 package com.gris.ege.activity;
 
 import com.gris.ege.R;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ViewFlipper;
 import android.app.Activity;
 import android.content.Intent;
 
 public class CalculateActivity extends Activity
 {
     private static final String TAG="CalculateActivity";
 
     public static final String TASK_ID="task_id";
 
     private ViewFlipper mTasksFlipper;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_calculate);
 
         mTasksFlipper=(ViewFlipper)findViewById(R.id.tasksFlipper);
 
         mTasksFlipper.addView(new View(this));
         mTasksFlipper.addView(new View(this));
 
         Intent aIntent=getIntent();
         Bundle aExtras=aIntent.getExtras();
 
         if (aExtras.containsKey(TASK_ID))
         {
             int aTaskId=aExtras.getInt(TASK_ID);
            Log.v(TAG, "Start calculation for task: "+String.valueOf(aTaskId));
         }
     }
 }
