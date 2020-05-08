 package net.wlanlj.meshapp;
 
 import android.R.drawable;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
     /**
      * A simple class which controls the OLSR Daemon.
      */
 
 public class MainActivity extends Activity {
    
     /** Useful for debugging. */
     public static final String MSG_TAG = "[[MeshApp]]::[MainActivity] -> ";
 
    OlsrNative olsrNative = new OlsrNative();
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.olsr_main);
 	Log.i(MSG_TAG, "Call to onCreate()");
 
 	// Prepare for button clicks.
 	Button button = (Button)findViewById(R.id.start);
         button.setOnClickListener(olsrStartListener);
         button = (Button)findViewById(R.id.stop);
         button.setOnClickListener(olsrStopListener);
     }
 
     private OnClickListener olsrStartListener = new OnClickListener() {
 	    public void onClick(View v) {
 		Log.i(MSG_TAG, "olsrStartListener activated");
 		Log.d(MSG_TAG, "Attempting to start olsrd...");
		if(olsrNative.startOlsr("start") == 0) 
		    Log.i(MSG_TAG, "Call to startOlsr() successful!");
 		else
		    Log.d(MSG_TAG, "Call to startOlsr() failed...");
 	    }
 	};
     
     private OnClickListener olsrStopListener = new OnClickListener() {
 	    public void onClick(View v) {
 		Log.i(MSG_TAG, "olsrStopListener activated");
 		Log.i(MSG_TAG, "Attempting to stop olsrd...");
		if(olsrNative.stopOlsr("stop") == 1)
		    Log.i(MSG_TAG, "Call to stopOlsr() successful!");
 		else
		    Log.d(MSG_TAG, "Call to stopOlsR() failed...");
 	    }
 	};
 }    
