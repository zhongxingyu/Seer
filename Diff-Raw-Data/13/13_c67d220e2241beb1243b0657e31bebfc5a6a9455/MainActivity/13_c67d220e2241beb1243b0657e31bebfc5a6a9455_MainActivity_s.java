 package io.github.maluta.libusbproject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ToggleButton;
 import io.github.maluta.libusbproject.R;
 
 public class MainActivity extends Activity {
 	private static final String TAG = null;
 	private NativeLib nativeLib = new NativeLib();
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
		//nativeLib.listDevices();
 
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 }
