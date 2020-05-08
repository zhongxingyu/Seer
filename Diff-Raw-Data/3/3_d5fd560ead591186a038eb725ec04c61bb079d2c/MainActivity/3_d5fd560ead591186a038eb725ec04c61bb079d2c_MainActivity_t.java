 package webdad.apps.verbshaker;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.HapticFeedbackConstants;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements SensorEventListener{
 
 	public DataBaseHelper db;
 	public TextView txt_m;
 	private ProgressDialog pd;
 	
 	private Boolean sync_onstart;
 	private String language;
 	
 	private SensorManager mSensorManager;
 	private Sensor mSensor;
 	private static final int SHAKE_THRESHOLD = 800;
 	private long lastUpdate=0;
 	float x, y, z, last_x=0.0f, last_y=0.0f, last_z = 0.0f, gravity_x=0.0f, gravity_y=0.0f, gravity_z=0.0f;
 	final float alpha = (float) 0.8;
 	
 	private Vibrator vib;
 	
 	
 
 	//IN
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.i("App", "Starting...");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
		
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		sync_onstart = sharedPref.getBoolean("pref_sync_onstart",sharedPref.getBoolean("pref_sync_onstart_default", true));
 		language = sharedPref.getString("pref_language",sharedPref.getString("pref_language_default", "de"));
 		
 		db = new DataBaseHelper(getApplicationContext());
 		
 		
 		
 		Log.i("App", "Create DB if needed...");
 		db.CreateMe();
		
 		txt_m = (TextView)findViewById(R.id.txt_mixed);
 		getApplicationContext();
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
 		    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		  }
 		vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 		
 		
 		Log.i("App", "Ready!");
 	}
 	
 	//created
 	
 	@Override
 	protected void onStart(){
 		super.onStart();
 		if(sync_onstart){
 			sync();
 		}
 	}
 	
 	//started(visible)
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		 mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	
 	//resumed (visible)
 	
 	@Override
 	protected void onPause(){
 		super.onPause();
 		mSensorManager.unregisterListener(this);
 	}
 	
 	//paused (maybe visible)
 	
 	@Override
 	protected void onStop(){
 		super.onStop();
 	}
 	
 	//stopped
 	
 	@Override
 	protected void onDestroy(){
 		db.close();
 		super.onDestroy();
 	}
 	
 	//OUT
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		try{
 			getMenuInflater().inflate(R.menu.activity_main, menu);
 		}
 		catch(Exception e){
 			Log.e("App",e.getMessage());
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.menu_settings:
 	        	try
 	        	{
 	        	Intent in = new Intent(MainActivity.this, SettingsActivity.class);
 	        	MainActivity.this.startActivity(in);
 	        	}
 	        	catch(Exception e)
 	        	{
 	        	Log.e("Menu", e.getMessage());
 	        	}
 	        	
 	            return true;
 	        case R.id.menu_sync:
 	        	try
 	        	{
 	        		sync();
 	        	}
 	        	catch(Exception e)
 	        	{
 	        		Log.e("Menu", e.getMessage());
 	        	}
 	            return true;
 	        case R.id.menu_share:
 	        	try
 	        	{
 	        		Intent sendIntent = new Intent();
 		        	sendIntent.setAction(Intent.ACTION_SEND);
 		        	sendIntent.putExtra(Intent.EXTRA_TEXT, txt_m.getText()+"\n\n\nMixUp presented by VerbShaker\nAn App brought to you by <a href=\"http://www.webdad.eu\">WebDaD.eu</a>");
 		        	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "VerbShaker Mix!" );
 		        	sendIntent.setType("text/plain");
 		        	startActivity(sendIntent);
 	        	}
 	        	catch(Exception e)
 	        	{
 	        		Log.e("Menu", e.getMessage());
 	        	}
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	public void btn_getNewMix_onclick(View view) {
 	     getMix();
 	     view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
 	 }
 	
 	public void getMix(){
 		txt_m.setText(db.getProVerb());
 	}
 	
 	private void sync(){
 		pd = ProgressDialog.show(MainActivity.this, "", "Syncing with Online DB", true, false);
 		SyncThread st = new SyncThread();
 	    st.start();
 	}
 	
 	private class SyncThread extends Thread {
         public SyncThread() {
         }
 
         @Override
         public void run() {         
             db.Sync();
             handler.sendEmptyMessage(0);
         }
 
 		@SuppressLint("HandlerLeak")
 		private Handler handler = new Handler() {
 
             @Override
             public void handleMessage(Message msg) {
                 pd.dismiss();
             }
         };
     }
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Do Something for an Acc change
 		
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		Log.i("Sensor",event.toString());
 
 		long curTime = System.currentTimeMillis();
 	    // only allow one update every 100ms.
 	    if ((curTime - lastUpdate) > 100) {
 	      long diffTime = (curTime - lastUpdate);
 	      lastUpdate = curTime;
 
 	      
 
 	      // Isolate the force of gravity with the low-pass filter.
 	      gravity_x = alpha * gravity_x + (1 - alpha) * event.values[0];
 	      gravity_y = alpha * gravity_y + (1 - alpha) * event.values[1];
 	      gravity_z = alpha * gravity_z + (1 - alpha) * event.values[2];
 
 	      // Remove the gravity contribution with the high-pass filter.
 	      x = event.values[0] - gravity_x;
 	      y = event.values[1] - gravity_y;
 	      z = event.values[2] - gravity_z;
 
 
 	      float speed = Math.abs(x+y+z - (last_x + last_y + last_z)) / diffTime * 10000;
 
 	      if (speed > SHAKE_THRESHOLD) {
 	        Log.d("sensor", "shake detected w/ speed: " + speed);
 	        getMix();
 	        vib.vibrate(300);
 	      }
 	      last_x = x;
 	      last_y = y;
 	      last_z = z;
 	    }
 		
 	}
 }
