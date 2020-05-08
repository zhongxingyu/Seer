 package it.giacomos.android.osmer.pro.service;
 
 import it.giacomos.android.osmer.R;
 import it.giacomos.android.osmer.pro.OsmerActivity;
 import it.giacomos.android.osmer.pro.locationUtils.LocationService;
 import it.giacomos.android.osmer.pro.network.state.Urls;
 import it.giacomos.android.osmer.pro.service.sharedData.NotificationData;
 import it.giacomos.android.osmer.pro.service.sharedData.ReportRequestNotification;
 import it.giacomos.android.osmer.pro.service.sharedData.ServiceSharedData;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.provider.Settings.Secure;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.widget.Toast;
 
 public class ReportDataService extends Service 
 implements ServiceLocationUpdateListener, FetchRequestsTaskListener, Runnable
 {
 	public static String REPORT_REQUEST_NOTIFICATION_TAG = "ReportRequestNotification";
 	public static String REPORT_RECEIVED_NOTIFICATION_TAG = "ReportReceivedNotification";
 
 	private Location mLocation;
 	private Handler mHandler;
 	private ReportDataServiceLocationService mLocationService;
 	private FetchRequestsDataTask mServiceDataTask;
 	private final long mSleepInterval;
 
 	public ReportDataService() 
 	{
 		super();
 		mLocationService = null;
 		mHandler = new Handler();
 		mLocation = null;
 		mSleepInterval = 120000; /* 2 mins */
 		mServiceDataTask = null;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) 
 	{
 
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 
 		Log.e(">>>> ReportDataService <<<<<< ", "onStartCommand");
 		mLocationService = new ReportDataServiceLocationService(this.getApplicationContext(), this);
 		boolean success = mLocationService.connect();
 		log("\nReportDataService.onStartCommand(): connected to location service: success: " + success);
 
 
 		boolean ret = mHandler.postDelayed(this, 3000);
 		Log.e("ReportDataService", "post delayed returned " + ret);
 		return Service.START_STICKY;
 	}
 
 	@Override
 	public void onLocationChanged(Location l) 
 	{
		if(!l.equals(mLocation))
			Toast.makeText(this, 
"Meteo.FVG Service: location changed..." + l.getLatitude() + ", " 
 + l.getLongitude(), Toast.LENGTH_LONG).show();
 		mLocation = l;
		
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		/* clean tasks, stop scheduled repetition of data task, disconnect from 
 		 * location service.
 		 */
 		if(mServiceDataTask != null)
 			mServiceDataTask.cancel(false);
 		if(mHandler != null)
 			mHandler.removeCallbacks(this);
 		if(mLocationService != null)
 			mLocationService.disconnect(); /* we disconnected in onLocationChanged, by the way */
 
 		Log.e(">>>> ReportDataService.onDestroy <<<<<<", "--> disconnecting from location service");
 		super.onDestroy();
 		log("ReportDataService.onDestroy " );
 	}
 
 	/** stop the service */
 	@Override
 	public void onServiceDataTaskComplete(boolean error, String dataAsString) 
 	{
 		log("ReportDataService.onServiceDataTaskComplete: error " + error + ", data: " + dataAsString);
 		Log.e(">>>> ReportDataService.onServiceDataTaskComplete", "data: " + dataAsString + " error " + error);
 
 		ServiceSharedData sharedData = ServiceSharedData.Instance();
 		NotificationManager mNotificationManager =
 				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		/* a request has been withdrawn, remove notification, if present */
 		if(dataAsString.isEmpty())
 		{
 			/* remove notification, if present */
 			NotificationData currentNotification = sharedData.getNotificationData(NotificationData.TYPE_REQUEST);
 			if(currentNotification != null) /* a notification is present */
 			{
 				Log.e("ReportDataService.onServiceDataTaskComplete", " removing notification with id " + currentNotification.makeId());
 				mNotificationManager.cancel(REPORT_REQUEST_NOTIFICATION_TAG, currentNotification.makeId());
 				
 				/* mark as consumed. The currentNotification is not removed from sharedData because sharedData
 				 * keeps it there in order not to bother us with possibly new notifications incoming in a near
 				 * future. currentNotification thus needs to be stored in order to be used by 
 				 * canBeConsideredNew() sharedData method.
 				 * On the other hand, the map view tests this variable in order to show or not a marker.
 				 */
 				currentNotification.setConsumed(true);
 			}
 		}
 		
 		ReportRequestNotification notificationData = new ReportRequestNotification(dataAsString);
 		if(notificationData.isValid())
 		{
 			if(sharedData.canBeConsideredNew(notificationData, this))
 			{
 				/* replace the previous notification data (if any) with the new one.
 				 * This updates the sharedData timestamp of the last notification
 				 */
 				sharedData.setWasNotified(notificationData);
 				/* and notify */
 				String message = getResources().getString(R.string.notificatonNewReportRequest) + " " + notificationData.username;
 				if(notificationData.locality.length() > 0)
 					message += " - " + notificationData.locality;
 
 				NotificationCompat.Builder notificationBuilder =
 						new NotificationCompat.Builder(this)
 				.setSmallIcon(R.drawable.ic_launcher)
 				.setContentTitle(getResources().getString(R.string.app_name))
 				.setContentText(message);
 
 				// Creates an explicit intent for an Activity in your app
 				Intent resultIntent = new Intent(this, OsmerActivity.class);
 				// The stack builder object will contain an artificial back stack for the
 				// started Activity.
 				// This ensures that navigating backward from the Activity leads out of
 				// your application to the Home screen.
 				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 				// Adds the back stack for the Intent (but not the Intent itself)
 				stackBuilder.addParentStack(OsmerActivity.class);
 				// Adds the Intent that starts the Activity to the top of the stack
 				stackBuilder.addNextIntent(resultIntent);
 
 				PendingIntent resultPendingIntent =
 						stackBuilder.getPendingIntent(
 								0,
 								PendingIntent.FLAG_UPDATE_CURRENT
 								);
 				notificationBuilder.setContentIntent(resultPendingIntent);
 				notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
 				// mId allows you to update the notification later on.
 
 				mNotificationManager.notify(ReportDataService.REPORT_REQUEST_NOTIFICATION_TAG, notificationData.makeId(),  notificationBuilder.build());
 			}
 			/* just update the shared data notification data with the most up to date 
 			 * values of latitude, longitude, username...
 			 */
 			sharedData.updateCurrentRequest(notificationData);
 		}
 
 		/* schedule next update */
 
 		boolean ret = mHandler.postDelayed(this, mSleepInterval);
 		Log.e("ReportDataService", "post delayed returned " + ret);
 
 	}
 
 	private void log(String message)
 	{
 		File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 
 		PrintWriter out;
 		try {
 			out = new PrintWriter(new BufferedWriter(new FileWriter(f.getAbsolutePath() + "/Meteo.FVG.Service.log", true)));
 			out.append(message + ": " + Calendar.getInstance().getTime().toLocaleString() + "\n");
 			out.close();
 		} catch (FileNotFoundException e1) 
 		{
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	/** This method is executed when mSleepInterval time interval has elapsed 
 	 * 
 	 */
 	public void run() 
 	{
 		Log.e("ReportDataService.run()", "run, entering");
    	Toast.makeText(this.getApplicationContext(), "Meteo.FVG: updating reports...", Toast.LENGTH_LONG).show();
		boolean taskStarted = false;
    	if(mLocation != null)
 		{
 			final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			final NetworkInfo netinfo = connMgr.getActiveNetworkInfo();
 			if(netinfo != null && netinfo.isConnected())
 			{
 				String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
 				mServiceDataTask = new FetchRequestsDataTask(this, deviceId, mLocation.getLatitude(), mLocation.getLongitude());
 
 				Log.e("ReportDataService.run()", "dataTask.execute() entering");
 				mServiceDataTask.execute(new Urls().getRequestsUrl());
				taskStarted = true;
 			}
 			else
 				Log.e("Meteo.FVG.ReportDataService.run", "connection is unavailable");
 		}
 		else
 			Log.e("Meteo.FVG.ReportDataService.run", "location information is null");
 
 		/* next update is scheduled inside onServiceDataTaskComplete (i.e. when the download task
 		 * is complete) to ensure that download tasks never ovelap.
		 * But if there is no location available here and/or no connectivity,
		 * we must schedule an update here.
 		 */
    	if(!taskStarted)
    		mHandler.postDelayed(this, mSleepInterval);
 	}
 
 
 }
