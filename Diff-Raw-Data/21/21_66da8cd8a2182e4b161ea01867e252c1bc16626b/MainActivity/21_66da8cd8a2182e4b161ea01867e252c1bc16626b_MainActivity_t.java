 package com.chiller.bme.posture;
 
 
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.chiller.bme.posture.db.SessionDAO;
 import com.chiller.bme.posture.db.SessionRecord;
 import com.chiller.bme.posture.tasks.AsyncTaskPostStats;
import com.chiller.bme.posture.tasks.AsyncTaskPostStats.UploadStatsCompleteListener;
 
 
public class MainActivity extends Activity implements UploadStatsCompleteListener{
 	
 	public static float calibrated_angle;
 	public static String[] events = { 
 		"START",
 		"WARN",
 		"OK",
 		"STOP"};
 	
 	//This bool helps make sure the service isn't started twice
     public static boolean started;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState); 
 		calibrated_angle = 0;
 		
 		setContentView(R.layout.main);
 
 		//Button1: Calibrate button, launches Calibration Activity
 	    Button calibrateButton = (Button)findViewById(R.id.button1);
 	    calibrateButton.setOnClickListener(new View.OnClickListener() {
 	      @Override
 	      public void onClick(View view) {
 	        Intent intent = new Intent(MainActivity.this, CalibrationActivity.class);
 	        startActivity(intent);
 	      }
 	    });
 
 	    //Button2: Stop service button. Stops the running sensor service.
 	    Button serviceButton = (Button)findViewById(R.id.button2);
 	    serviceButton.setOnClickListener(new View.OnClickListener() {
 	      @Override
 	      public void onClick(View view) {	        	        
 	        Intent intent = new Intent(MainActivity.this, PostureService.class);
 	        getApplicationContext().stopService(intent);	
 	        MainActivity.started = false;
 	      }
 	    });
 	    
 	    //Button3: Button to trigger sync database to server
 	    Button syncButton = (Button)findViewById(R.id.button3);
 	    syncButton.setOnClickListener(new View.OnClickListener() {
 	      @Override
 	      public void onClick(View view) {	        
 	    	//Getting unsynced data to sync	    	
 	    	Log.i("PostureService", "Syncing!");
 			SessionDAO datasource = new SessionDAO(MainActivity.this);
 		    datasource.open();
 		    String data = datasource.getUnsyncedJson();
 		    datasource.close();
 	    	//Send data to backend via asynctask  
 	    	AsyncTaskPostStats getImagesTask =
       	    new AsyncTaskPostStats(MainActivity.this, MainActivity.this);
       		getImagesTask.execute(
       	    "http://posturehelper.appspot.com/",data); 	      
 	      }
 	    });
 	    
 	    //Button4: Button to show stats
 	    /*Button statsButton = (Button)findViewById(R.id.button4);
 	    statsButton.setOnClickListener(new View.OnClickListener() {
 
 	      @Override
 	      public void onClick(View view) {	        
 	    	//Getting unsynced data to sync	    	
 	    	SessionDAO datasource = new SessionDAO(MainActivity.this);
 		    datasource.open();
 		    List<Integer> data = datasource.getStatsForUser("Endre");
 		    datasource.close();		    
 	    	Toast.makeText(MainActivity.this, String.valueOf(data.get(0)), Toast.LENGTH_LONG).show(); 
 	    	Toast.makeText(MainActivity.this, String.valueOf(data.get(1)), Toast.LENGTH_LONG).show(); 	    		      
 	      }
 	    });	*/
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.mymenu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		if (item.getItemId() == R.id.itemPreferences)
 		{
 			//Set some preferences that influence calibration and alerts
 			Intent settingsActivity = new Intent(this,PreferencesActivity.class);
 	        startActivity(settingsActivity);
 		} else if (item.getItemId() == R.id.clearDatabase) {
 			//Clear sqlite database
 			Log.i("PostureService", "Clearing database!");
 			SessionDAO datasource = new SessionDAO(this);
 		    datasource.open();
 		    datasource.deleteAllRecords();
 		    datasource.close();
 			
 		} else if (item.getItemId() == R.id.dumpDatabase) {
 			//Dump database to logs
 			SessionDAO datasource = new SessionDAO(this);
 		    datasource.open();
 		    for (SessionRecord r: datasource.getAllRecords()){
 		    	Log.i("PostureService",r.toString());		    	
 		    } 
 		    datasource.close();			
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onTaskComplete(String aResult) {
 		// Handler for completed asynctask that sends database to backend
 		// On complete all records should be marked synced
 		// TODO: only mark those that have ben synced, new ones might have been added
 		Log.i("PostureService", "AsyncTask Finished: " + aResult);
 		SessionDAO datasource = new SessionDAO(this);
 	    datasource.open();
 	    datasource.markAllRecords();
 	    datasource.close();
 	    Toast.makeText(MainActivity.this, "Sync Complete!", Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onError(String aError) {
 		Log.i("PostureService", "AsyncTask Finished with Error: "+ aError);
 	}
 	
 	//This fragment is in charge of displaying statistics about user's posture
 	//It is a nicely reusable part of the application, easily plugable into another activity's layout
 	public static class StatsFragment extends Fragment {
 
 		private Timer autoUpdate;
 
 		@Override
 		public void onResume() {
 			super.onResume();
 			autoUpdate = new Timer();
 			autoUpdate.schedule(new TimerTask() {
 		    @Override
 		    public void run() {
 		    	StatsFragment.this.getActivity().runOnUiThread(new Runnable() {
 		    		public void run() {
 		    			updateStats();
 		    		}
 		    	});
 		    }
 		  }, 0, 1000); // updates every second
 		 }
 		
 		//This method fetches the posture statistics from database
 		 private void updateStats(){
 			//Log.i("Posture Service","Periodical Lol");
 			TextView status = (TextView)StatsFragment.this.getActivity().findViewById(R.id.statok);
 			TextView statuswarn = (TextView)StatsFragment.this.getActivity().findViewById(R.id.statwarn);
 			ProgressBar progress = (ProgressBar)StatsFragment.this.getActivity().findViewById(R.id.progressBar1);
 			
 			SessionDAO datasource = new SessionDAO(StatsFragment.this.getActivity());
 		    datasource.open();
 		    List<Integer> data = datasource.getStatsForUser("Endre");
 		    datasource.close();		    
 		    
 		   
 			status.setText( formatIntoHHMMSS(data.get(0)));
 			statuswarn.setText( formatIntoHHMMSS(data.get(1)));
 			float dataok = (float)(data.get(0));
 			float datawarn = (float)(data.get(1));
 			
 			progress.setProgress((int)(100*dataok/(dataok+datawarn)));
 			
 			}
 
 		 @Override
 		 public void onPause() {
 		  autoUpdate.cancel();
 		  super.onPause();
 		 }
 		
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 	        return inflater.inflate(R.layout.statsfrag, container, false);
 		}
 		
 		//Simple tool for converting seconds to HH MM SS format
 		static String formatIntoHHMMSS(int secsIn)
 		{
 
 			int hours = secsIn / 3600,
 			remainder = secsIn % 3600,
 			minutes = remainder / 60,
 			seconds = remainder % 60;
 	
 			return ( (hours < 10 ? "0" : "") + hours
 			+ ":" + (minutes < 10 ? "0" : "") + minutes
 			+ ":" + (seconds< 10 ? "0" : "") + seconds );
 
 		}
 
 	}
 }
