 package com.vanderbilt.people.finder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 
 public class RootActivity extends Activity 
 {
 	private static final String TAG = "RootActivity";
 	private static final int INIT_TAG = 1;
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		SharedPreferences settings = getSharedPreferences("UserData", MODE_PRIVATE);
 	    boolean presentInitialization = settings.getBoolean("needsInitialization", true);
	    Log.v(TAG, "needsInitialization: " + presentInitialization);
 	    if (presentInitialization)
 	    {
	    	Log.v(TAG, "starting StartupActivity");
 	    	startActivityForResult( new Intent(this, StartupActivity.class), INIT_TAG);
 	    }
 	    else
 	    {
	    	Log.v(TAG, "starting MainActivity");
 	    	startActivity(new Intent(this, MainActivity.class));
 	    	finish();
 	    }
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	if (requestCode == INIT_TAG)
     	{
     		if (resultCode == RESULT_OK)
     		{
    			Log.v(TAG, "starting MainActivity");
     			startActivity(new Intent(this, MainActivity.class));
     			finish();
     		}
     	}
     	
//    	Log.e(TAG, "Main activity couldn't be started!");
     }
 }
