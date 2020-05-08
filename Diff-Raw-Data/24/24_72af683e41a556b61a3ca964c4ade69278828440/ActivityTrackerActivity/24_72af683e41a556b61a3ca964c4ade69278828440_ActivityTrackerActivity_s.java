 package edu.oregonstate.biomed.actigps;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.AlertDialog;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Looper;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ActivityTrackerActivity extends Activity {
 	private DataUpdateReceiver dataUpdateRcvr;	
 	private Timer mBackgroundTimer;
 	
 	/* chart variables */
 	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
 	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
 	private XYSeriesRenderer mCurrentRenderer;
 	private String mDateFormat;
 	
 	private static final int[] SERIES_COLORS = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
 	private static final PointStyle[] SERIES_STYLES = { PointStyle.CIRCLE, PointStyle.TRIANGLE, PointStyle.SQUARE, PointStyle.DIAMOND };
 	
 	private String mUserId;
 	private List<String> mOtherUsers;
 	
 	private GraphicalView mChartView;
 	
 	private float calibrate_value;
 	
 	private static final long HOUR = 3600 * 1000;
 
 	private static final long DAY = HOUR * 24;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
  
         /* start the background service when the app is launched */
         startService();
         
         /* Set up tabbed view */
         setContentView(R.layout.main);
         
         TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
         tabHost.setup();
         
         TabSpec spec1=tabHost.newTabSpec("Tab 1");
         spec1.setIndicator("Visualization");
         spec1.setContent(R.id.chart);
         
         TabSpec spec2=tabHost.newTabSpec("Tab 2");
         spec2.setIndicator("Settings");
         spec2.setContent(R.id.settingsTab);
 
         tabHost.addTab(spec1);
         tabHost.addTab(spec2);
         tabHost.setCurrentTab(0);
         
         /* create chart stuff */
         setChartSettings("Activity Data", "Time", "Activity Level", new Date().getTime() - (7 * DAY), 
         		new Date().getTime() - (7 * DAY), -5, 30, Color.LTGRAY, Color.LTGRAY);
 
         XYSeries series = new XYSeries("My Data");
         mDataset.addSeries(series);
         
         XYSeriesRenderer renderer = new XYSeriesRenderer();
         renderer.setPointStyle(PointStyle.CIRCLE);
         renderer.setFillPoints(true);
         renderer.setColor(Color.BLUE);
 
         mRenderer.addSeriesRenderer(renderer);
         mRenderer.setZoomEnabled(true, false);
         mRenderer.setZoomButtonsVisible(false);
         mRenderer.setExternalZoomEnabled(true);
         mRenderer.setLabelsTextSize(20);
         mRenderer.setLegendTextSize(24);
         mRenderer.setAxisTitleTextSize(24);
         mRenderer.setChartTitleTextSize(30); 
         
         mCurrentRenderer = renderer;
 
         if (mChartView != null) {
           mChartView.repaint();
         }
         
         mOtherUsers = Collections.synchronizedList(new ArrayList<String>());
         
     	/* update the ui based on saved setting on start */
         updateSettings();
     }
     
     @Override
     protected void onRestoreInstanceState(Bundle savedState) {
       super.onRestoreInstanceState(savedState);
       mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
       mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
       mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
       mDateFormat = savedState.getString("date_format");
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
       super.onSaveInstanceState(outState);
       outState.putSerializable("dataset", mDataset);
       outState.putSerializable("renderer", mRenderer);
       outState.putSerializable("current_renderer", mCurrentRenderer);
       outState.putString("date_format", mDateFormat);
     }
     
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		/* stop getting HTTP data on pause */
 		mBackgroundTimer.cancel();
 		mBackgroundTimer = null;
 	}
 	
     
     @Override
     public void onResume() {
     	super.onResume();
     	
     	/* create chartview */
         if (mChartView == null) {
             LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
             mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "MM/dd H:mm");
             mRenderer.setClickEnabled(true);
             mRenderer.setSelectableBuffer(100);
             mChartView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) { }
             });
 
             layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
                 LayoutParams.FILL_PARENT));
           } else {
             mChartView.repaint();
           }
         
         if(mBackgroundTimer == null)
         {
 	        mBackgroundTimer = new Timer();
 	        
 	        mBackgroundTimer.scheduleAtFixedRate( new HttpGetTimerTask(), 0, 60*1000);
         }
     }
     
     
     /*
      * Service related things
      */
     
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		public void onServiceConnected(ComponentName className, IBinder binder) {}
 
 		public void onServiceDisconnected(ComponentName className) {}
 	};
 	
 	private void doBindService() {
 		bindService(new Intent(this, ActivityTrackerService.class), mConnection,
 				Context.BIND_AUTO_CREATE);
 	}
 	
 	
 	/**
 	 * Checks to see if a service is running by parsing the service list
 	 * @return True if the background service is running; False otherwise
 	 */
 	private boolean isMyServiceRunning() {
 	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	        if (ActivityTrackerService.class.getName().equals(service.service.getClassName())) {
 	            return true;
 	        }
 	    }
 	    return false;
 	}
 	
 	
 	private void startService()
 	{
 		/* only create a new service if one did not already exist */
 		if(isMyServiceRunning() == false)
 		{
 	        doBindService();
 			startService(new Intent(this, ActivityTrackerService.class));
 		}
 	}
 	
 	/* button handler */
 	public void onClickStopService(View v) {
 		stopService();
 		Toast.makeText(this, "Background Service Stopped.", Toast.LENGTH_SHORT).show();
 	}
 	
 	private void stopService()
 	{
 		if(isMyServiceRunning())
 		{
 			try {
 				unbindService(mConnection);
 	    	}
 	    	catch (IllegalArgumentException ex) {
 	    		// catch if the receiver is not registered
 	    	}
 	    	
 	    	/* even if we were not bound to service, it might still be running, so kill it. */
 			stopService(new Intent(this, ActivityTrackerService.class));
 		}
 	}
 	
 	
 	public void onClickAddUser(View v)
 	{
 		if( mOtherUsers.size() >= 3 )
 		{
 			Toast.makeText
 			(
 				this, 
 				"Sorry, cannot track > 3 users.  Clear tracked users first before adding more.", 
 				Toast.LENGTH_LONG
 			).show();
 			return;
 		}
 		
 		/* Set an EditText view to get user input */
 		final EditText input = new EditText(this);
 		
 		/* add a user to the list of tracked users */
 		new AlertDialog.Builder(this)
 		    .setTitle("Add User ID")
 		    .setMessage("Input the User ID of the user you wish to track.")
 		    .setView(input)
 		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int whichButton) {
 		            String value = input.getText().toString().trim();
 		            
 		            if( value.length() == 0)
 		            	return; /* user entered no string */
 		            	
 		            TextView others = (TextView)findViewById(R.id.text_Others);
 		            mOtherUsers.add(value);
 		            String newtext = "";
 		            for(String s : mOtherUsers)
 		            {
 		            	newtext += s + ", ";
 		            }
 		            others.setText(newtext.substring(0, newtext.length() - 2));
 		            
 		            /* save the new user to the settings file */
 		            saveSettings();
 		        }
 		    })
 		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int whichButton) {
 		            // Do nothing.
 		        }
 		    })
 		    .show();
 		
 	}
 	
 	public void onClickClearUsers(View v)
 	{
 		mOtherUsers.clear();
 		
 		/* save the setting of an empty string before setting to 'None' in textbox */
 	    ((TextView)findViewById(R.id.text_Others)).setText("");
 	    saveSettings();
 	    ((TextView)findViewById(R.id.text_Others)).setText("None");
 	    
 	    /* remove all datasets but the main one */
 	    for(int i = 1; i < mDataset.getSeriesCount(); i++)
 	    {
 	    	mDataset.removeSeries(i);
 	    }
 	}
 	
 	private ProgressDialog progDiag;
 	
     protected Dialog onCreateDialog(int id) {
         switch(id) {
         case 0:
         	progDiag = new ProgressDialog(ActivityTrackerActivity.this);
         	progDiag.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         	progDiag.setMessage("Calibrating. Please wait...");
             return progDiag;
         default:
             return null;
         }
     }
 	
 	/* button handler */
 	public void onClickCalibrate(View v) {
 		/* calibrate the accelerometer for this device */
 		calibrateAccelerometer();
 	}
     
 	public void calibrateAccelerometer()
 	{
 		/* start the service if it has not yet started */
 		startService();
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Place the device on a stable surface and press \"Ok\"")
 	       .setNeutralButton(R.string.OK, new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	        	   /* dismiss the old dialog before popping up progress dialog */
 	        	   dialog.dismiss();
 	        	   
 	        	   showDialog(0);
 	        	   
 	        	   CalibrateThread t = new CalibrateThread();
 	        	   t.start();
 	           }
 	       });
 		
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	/**
 	 * Nested thread class to handle accelerometer calibration procedure
 	 */
 	private class CalibrateThread extends Thread {       
 		CalibrateThread() { }
        
 		/* perform the calibration */
 		@Override
         public void run() {
      	   /* register for accelerometer updates */
 		   if(dataUpdateRcvr == null) dataUpdateRcvr = new DataUpdateReceiver();
      	   IntentFilter intentFilter = new IntentFilter("PHONE_ACCELEROMETER_VALUE");
            registerReceiver(dataUpdateRcvr, intentFilter);
      	   
            /* wait for 5 seconds of accelerometer data */
            try
 		   {
 				Thread.sleep(5000);
 		   } catch (InterruptedException e)
 		   {
 				/* Do nothing */
 		   }
      	   
 		   unregisterReceiver(dataUpdateRcvr);
 		   
 		   /* calibrated value is average at stationary, plus offset */
 		   calibrate_value = (float)(dataUpdateRcvr.getAvg() + 0.5);
 		   dismissDialog(0);
 		   
 		   /* commit the settings */
 		   saveSettings();
         }
     }
 	
 	private class DataUpdateReceiver extends BroadcastReceiver {
 	    ArrayList<Double> vals = new ArrayList<Double>();
 	    
 	    public DataUpdateReceiver()
 	    {
 	    	vals.clear();
 	    }
 	    
 		@Override
 		public void onReceive(Context ctx, Intent intent) {
 			if (intent.getAction().equals("PHONE_ACCELEROMETER_VALUE")) {
 				Bundle data = intent.getExtras();
 				vals.add(data.getDouble("val"));
 			}
 		}
 		
 		public double getAvg()
 		{
 			/* if we got no other values, 10 will be average */
 			double avg = 10;
 			int size = vals.size();
 			for(double val : vals)
 			{
 				avg += val;
 			}
 			
 			return( avg / ( size + 1 ));
 		}
 	}
 
 	
 	public void onClickCommitSettings(View v) {
 		saveSettings();
 		   
 		Toast.makeText(ActivityTrackerActivity.this, "Settings Saved", Toast.LENGTH_SHORT).show();
 	}
 	
 	private void saveSettings()
 	{
 		/* save service running state */
 		boolean wasRunning = isMyServiceRunning();
 		
 		/* stop the current service before applying settings */
 		stopService();
 		
 		boolean wifienabled = ((CheckBox)findViewById(R.id.chkbox_wifiscan)).isChecked();
 		boolean gpsenabled = ((CheckBox)findViewById(R.id.chkbox_gpsscan)).isChecked();
 		boolean accelenabled = ((CheckBox)findViewById(R.id.chkbox_accel)).isChecked();
 		boolean gyroenabled = ((CheckBox)findViewById(R.id.chkbox_gyro)).isChecked();
 		String otherUsers = ((TextView)findViewById(R.id.text_Others)).getText().toString();
 		
 		/* setup the new settings */
 	    SharedPreferences settings = getSharedPreferences(ActivityTrackerService.PREFS_NAME, 0);
 	    SharedPreferences.Editor editor = settings.edit();
 	  
 	    /* set the enabled values based on checkboxes */
     	editor.putBoolean(ActivityTrackerService.SETTINGS_WIFI_ENABLE_KEY, wifienabled);
     	editor.putBoolean(ActivityTrackerService.SETTINGS_GPS_ENABLE_KEY, gpsenabled);
     	editor.putBoolean(ActivityTrackerService.SETTINGS_ACCEL_ENABLE_KEY, accelenabled);
     	editor.putBoolean(ActivityTrackerService.SETTINGS_GYRO_ENABLE_KEY, gyroenabled);
     	
     	/* save the latest calibration value */
 	    editor.putFloat(ActivityTrackerService.SETTINGS_CALIBRATE_KEY, calibrate_value);
 	    editor.putString(ActivityTrackerService.SETTINGS_TRACKED_USERS_KEY, otherUsers);
 	    
 	    /* commit changes to settings */
 	    editor.commit();
 	    
 	    /* only start the service if it was running before */
 	    if( wasRunning == true )
 	    	startService();
 	}
 
 	private void updateSettings(){
 		SharedPreferences settings = getSharedPreferences(ActivityTrackerService.PREFS_NAME, 0);
 		boolean wifienabled = settings.getBoolean(ActivityTrackerService.SETTINGS_WIFI_ENABLE_KEY, true);
 		boolean gpsenabled = settings.getBoolean(ActivityTrackerService.SETTINGS_GPS_ENABLE_KEY, true);
 		boolean accelenabled = settings.getBoolean(ActivityTrackerService.SETTINGS_ACCEL_ENABLE_KEY, true);
 		boolean gyroenabled = settings.getBoolean(ActivityTrackerService.SETTINGS_GYRO_ENABLE_KEY, true);
 		String trackedUsers = settings.getString(ActivityTrackerService.SETTINGS_TRACKED_USERS_KEY, "");
 
 		String[] parsed = trackedUsers.split(", ");
 		for( String user : parsed)
 		{
 			if(user.length() > 0)
 				mOtherUsers.add(user);
 		}
 		if(trackedUsers.length() == 0)
 			trackedUsers = "None";
 		
 		
 		/* set these settings to default values if they are not present */
 	    SharedPreferences.Editor editor = settings.edit();
 	    
 	    /* check if the setting returns the default value, then perform calibration if it does */
 	    calibrate_value = settings.getFloat(ActivityTrackerService.SETTINGS_CALIBRATE_KEY, 0);
 	    if(calibrate_value == 0)
 	    {
 	    	calibrateAccelerometer();
 	    }
 	    
 		mUserId = settings.getString(ActivityTrackerService.SETTINGS_USER_ID_KEY, "");
 		if( mUserId.length() == 0 )
 		{
 			String uuid = UUID.randomUUID().toString().substring(0, 5);
 			System.out.println("uuid = " + uuid);
 			mUserId = uuid;
 			editor.putString(ActivityTrackerService.SETTINGS_USER_ID_KEY, mUserId);
 		}
 	    
 	    /* commit changes to settings */
 	    editor.commit();
 		
 		/* update the checked state of the checkboxes */
 		((CheckBox)findViewById(R.id.chkbox_wifiscan)).setChecked(wifienabled);
 		((CheckBox)findViewById(R.id.chkbox_gpsscan)).setChecked(gpsenabled);
 		((CheckBox)findViewById(R.id.chkbox_accel)).setChecked(accelenabled);
 		((CheckBox)findViewById(R.id.chkbox_gyro)).setChecked(gyroenabled);
 		
 		/* update textboxes */
 		((TextView)findViewById(R.id.text_UID)).setText(mUserId);
 		((TextView)findViewById(R.id.text_Others)).setText(trackedUsers);
 	}
 	
 	  /**
 	   * Sets a few of the series renderer settings.
 	   * 
 	   * @param renderer the renderer to set the properties to
 	   * @param title the chart title
 	   * @param xTitle the title for the X axis
 	   * @param yTitle the title for the Y axis
 	   * @param xMin the minimum value on the X axis
 	   * @param xMax the maximum value on the X axis
 	   * @param yMin the minimum value on the Y axis
 	   * @param yMax the maximum value on the Y axis
 	   * @param axesColor the axes color
 	   * @param labelsColor the labels color
 	   */
 	  protected void setChartSettings(String title, String xTitle,
 	      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
 	      int labelsColor) {
 		  
 			mRenderer.setApplyBackgroundColor(true);
 			mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
 			mRenderer.setAxisTitleTextSize(16);
 			mRenderer.setChartTitleTextSize(20);
 			mRenderer.setLabelsTextSize(15);
 			mRenderer.setLegendTextSize(15);
 			mRenderer.setMargins(new int[] { 20, 30, 15, 0 }); 
 			mRenderer.setZoomButtonsVisible(true);
 			mRenderer.setPointSize(10);
 			    
 			mRenderer.setChartTitle(title);
 			mRenderer.setXTitle(xTitle);
 			mRenderer.setYTitle(yTitle);
 			
 			setChartAxes(xMin, xMax, yMin, yMax, 5);
 
 			mRenderer.setAxesColor(axesColor);
 			mRenderer.setLabelsColor(labelsColor);
 			mRenderer.setShowGrid(true);
 	  }
 	  
 	  private void setChartAxes(double xMin, double xMax, double yMin, double yMax, int count)
 	  {
 		  mRenderer.setXAxisMin(xMin);
 		  mRenderer.setXAxisMax(xMax);
 	      mRenderer.setYAxisMin(yMin);
 		  mRenderer.setYAxisMax(yMax);
 		  
 		  mRenderer.setXLabels(count);
 		  mRenderer.setYLabels(count);
 		  mRenderer.setXLabelsAlign(Align.CENTER);
 		  mRenderer.setYLabelsAlign(Align.RIGHT);
 	  }
 	
 
 	  
 	/* 
 	 * HTTP related things 
 	 */
 	private class HttpGetTimerTask extends TimerTask {
     	private Handler mHandler = new Handler(Looper.getMainLooper());
 
         @Override
         public void run() {
            mHandler.post(new Runnable() {
               public void run() {
               	new HttpGetTask().execute(360);
               }
            });
          }
 	}
 	
 	/* create an async task to handle queuing of HTTP GETs on separate threads */
 	private class HttpGetTask extends AsyncTask<Integer, Integer, ArrayList<ArrayList<DataPoint>>> {
		protected ArrayList<ArrayList<DataPoint>> doInBackground(Integer... limit) {
			int count = limit.length;
			
			if(count == 0)
				return new ArrayList<ArrayList<DataPoint>>();

			return doHttpGetAccelData(limit[0]);
 		}
 		 
 		protected void onPostExecute(ArrayList<ArrayList<DataPoint>> result) 
 		{
 			double x;
 			double y;
 	
 			double minX = -1;
 			double maxX = 0;
 			double minY = -1;
 			double maxY = 0;
 	        
 			/* get the amount of data series that we are currently displaying */
 			int seriescount = mDataset.getSeriesCount();
 			
 			for(int i = 0; i < result.size(); i++)
 			{
 				ArrayList<DataPoint> list = result.get(i);
 				
 				/* only redraw if we have results */
 				if( list.size() > 0 )
 				{
 					XYSeries currentSeries;
 					
 					/* if there are less series being graphed then we have, create more */
 					if( seriescount <= i )
 					{
 						currentSeries = new XYSeries("Data " + (i+1));
 						mDataset.addSeries(currentSeries);
 						
 				        XYSeriesRenderer renderer = new XYSeriesRenderer();
 				        renderer.setPointStyle(SERIES_STYLES[i%4]);
 				        renderer.setColor(SERIES_COLORS[i%4]);
 				        renderer.setFillPoints(true);
 
 				        mRenderer.addSeriesRenderer(renderer);
 					}
 					else
 					{
 						currentSeries = mDataset.getSeriesAt(i);
 						currentSeries.clear();
 					}
 					
 					for(DataPoint val : list)
 					{
 						x = val.getX();
 						
 						/* keep track of min and max x for setting axis */
 						if( minX < 0)
 						{
 							minX = x;
 							maxX = x;
 						}
 						else if (x < minX)
 							minX = x;
 						else if (x > maxX)
 							maxX = x;
 						
 						y = val.getY();
 						
 						/* keep track of max and min y for setting axis */
 						if( minY < 0)
 						{
 							minY = y;
 							maxY = y;
 						}
 						else if (y < minY)
 							minY = y;
 						else if (y > maxY)
 							maxY = y;
 						
 						currentSeries.add(x, y);
 					}
 				}
 			}
 
 			/* set x axis to only display past half hour of data at most*/
 			setChartAxes(Math.max(maxX - (HOUR / 2), minX), maxX, minY - 1, maxY + 1, 5);
 	        
 	        if (mChartView != null) {
 	            mChartView.repaint();
 	        }
         
 		}
 	}
 	
 	
 	/**
 	 * Perform the HTTP Get routine and return 
 	 * @return
 	 */
	private ArrayList<ArrayList<DataPoint>> doHttpGetAccelData(int limit) 
 	{			
 		ArrayList<ArrayList<DataPoint>> retlist = new ArrayList<ArrayList<DataPoint>>();
 		/* first, do the get for our user id */
		retlist.add(getData(mUserId, limit));
 		for(String user : mOtherUsers )
 		{
 			/* then, do the get for other user IDs */
			retlist.add(getData(user, limit));
 		}
 		
 		return retlist;
 	}
 	
	private ArrayList<DataPoint> getData(String userid, int limit)
 	{
 		int datacount = 0;
 		int numhours = 1;
 		ArrayList<DataPoint> fetchedData = new ArrayList<DataPoint>();
 		
 		Log.i(ActivityTrackerService.TAG, "Trying to get accel data for " + userid);
 		
 		/* get latest 300 points */
 		while(datacount < 300)
 		{
 			/* reset datacount for this query and expand the sincetime */
 			datacount = 0;
 			numhours = numhours*2;
 			
 			/* if we are querying more than 8 hours in the past, just return with what we have */
 			if( numhours > 8 )
 			{
 				break;
 			}
 			else
 			{
 				fetchedData.clear();
 			}
 			
 			long sincetime = ((new Date()).getTime() - HOUR*numhours);
 			
 			String query = "user=" + ActivityTrackerService.UPLOAD_UID + "&channel=Activity_Data_" + 
				userid + "&limit=" + limit + "&since=" + sincetime;
 			
 			HttpGet httpget;
 			try
 			{
 				httpget = new HttpGet(new URI("http", ActivityTrackerService.DOWNLOAD_HOST, ActivityTrackerService.DOWNLOAD_PATH, query, null));
 				
 	
 			} catch (URISyntaxException e1)
 			{
 				return fetchedData;
 			}
 	
 			
 			try {
 		
 				HttpClient client = HttpClientFactory.getThreadSafeClient();
 				HttpResponse r = client.execute(httpget);
 				
 				if( r.getStatusLine().toString().contains("200 OK"))
 				{
 					BufferedReader in = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
 		            String line = "";
 		            String[] parsed;
 		            
 		            /* parse each line of response for timestamp:value tuples */
 		            while ((line = in.readLine()) != null) {
 		            	parsed = line.split(":");
 		            	fetchedData.add(new DataPoint(Long.parseLong(parsed[0]), Double.parseDouble(parsed[1])));
 		            	
 		            	/* we got a datapoint, increment count */
 		            	datacount++;
 		            }
 		            in.close();
 				}
 			}
 			catch (IOException e) 
 			{
 				Log.e(ActivityTrackerService.TAG, "Exception Occurred on HTTP post: " + e.toString());
 			}
 		} /* loop exits when query has at least 300 points */
 		
 		Log.i(ActivityTrackerService.TAG, "Accel data sucessfully retrieved for " + userid);
 		return fetchedData;
 	}
 }
