 package com.tojc.logging.android.multiprocessapploggersample;
 
 import com.tojc.logging.android.Log;
 import com.tojc.logging.android.Logger;
 import com.tojc.logging.android.internal.model.LoggerModelType;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class MainActivity extends Activity
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
		Logger.settings().initialize(this, com.tojc.logging.android.multiprocessapploggersample.LoggerProvider.class);
 		Logger.settings().setPackageName("com.tojc.logging.android.multiprocessapploggersample");
 		Logger.settings().setLogcatEnabled(true);
 		Logger.settings().setLoggerEnabled(true);
 		Logger.settings().setLoggerModelType(LoggerModelType.DelayedContentProvider);
 
 		Logger.verbose().print("test88");
 		Log.v("test49");
 
 		for(int i = 0; i <= 50 ; i++)
 		{
 			Logger.verbose().print("test: %d", i);
 		}
 		String test = Logger.output();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
