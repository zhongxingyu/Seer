 package com.gunayorbay.android.touchscroll;
 
 import android.app.Activity;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class TouchScrollActivity extends Activity {
     /** Called when the activity is first created. */
     
     //private SensorManager mSensorManager;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	//mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
     	this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
  		GLSurfaceView view = new OpenGLRenderer(this);
    		setContentView(view);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_item_quit) {
 			finish();
 			return true;
 		}
 		return false; 
     }
 }
