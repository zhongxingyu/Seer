 package fi.oulu.tol.group19project;
 
 import fi.oulu.tol.group19project.HomeControlService.HomeControlBinder;
 import fi.oulu.tol.group19project.model.AbstractDevice;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.app.ListActivity;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 public class DeviceListActivity extends ListActivity implements HomeControlServiceObserver {
 
 
 	private static final String TAG = "Group19HomeControl";
 	public static final int DEBUG = 3;
 	private HomeControlService homeControlService;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		DeviceAdapter adapter = DeviceAdapter.getInstance();
 		adapter.setInflater(getLayoutInflater());
 		this.setListAdapter(adapter);
 
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		Intent intent = new Intent(this, HomeControlService.class);
 		startService(intent);
 		
 		
 		
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		AbstractDevice device = (AbstractDevice)DeviceAdapter.getInstance().getItem(position);
 		if (null != device) {
 			Intent intent = new Intent(this, DeviceActivity.class);
 			intent.putExtra(DeviceActivity.KEY_DEVICE_ID, device.getId());
 			this.startActivity(intent);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.device_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected (MenuItem item) {
 		Intent intent = new Intent(this, SettingsActivity.class);
 		this.startActivity(intent);
 		
 		
 		switch (item.getItemId()) {
 	
 		   case R.id.server_connect: {
 		      connectToControlUnit();
 		      break;
 		   }
 		   case R.id.server_disconnect: {
 		      disconnectFromControlUnit(false);
 		      break;
 		   }
 		   case R.id.server_force_close: {
 		      disconnectFromControlUnit(true);
 		      break;
 		   }
 		   case R.id.server_refresh: {
 		   try {
 		      if (homeControlService != null) {
 		         Log.d(TAG, "Refreshing device data from server");
 		         homeControlService.getProtocol().getPath(null, "/");
 		      }
 		   } catch (InterruptedException e) {
 		      e.printStackTrace();
 		   }
 		   break;
 		   }}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Intent intent = new Intent(this, HomeControlService.class);
 		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
 	}
 
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		new CountDownTimer(20000, 1000) {
 
 			public void onTick(long millisUntilFinished) {
 
 			}
 
 			public void onFinish() {
 				NotificationCompat.Builder mBuilder =
 						new NotificationCompat.Builder(DeviceListActivity.this)
 				.setSmallIcon(R.drawable.ic_launcher)
 				.setContentTitle("Notification")
 				.setContentText("The application has stopped!");
 				// Creates an explicit intent for an Activity in your app
 				Intent resultIntent = new Intent(DeviceListActivity.this, DeviceListActivity.class);
 
 				// The stack builder object will contain an artificial back stack for the
 				// started Activity.
 				// This ensures that navigating backward from the Activity leads out of
 				// your application to the Home screen.
 				TaskStackBuilder stackBuilder = TaskStackBuilder.create(DeviceListActivity.this);
 				// Adds the back stack for the Intent (but not the Intent itself)
 				stackBuilder.addParentStack(DeviceListActivity.class);
 				// Adds the Intent that starts the Activity to the top of the stack
 				stackBuilder.addNextIntent(resultIntent);
 				PendingIntent resultPendingIntent =
 						stackBuilder.getPendingIntent(
 								0,
 								PendingIntent.FLAG_UPDATE_CURRENT
 								);
 				mBuilder.setContentIntent(resultPendingIntent);
 				NotificationManager mNotificationManager =
 						(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 				// mId allows you to update the notification later on.
 				mNotificationManager.notify(0, mBuilder.build());
 			}
 		}.start();
 		if (homeControlService != null) {
 			unbindService(mServiceConnection);
 			//homeControlService = null;
 		}
 	}
 
 	public void onRefreshButtonClick() {
 		Log.d(TAG, "Refresh Button Clicked");
 	}
 
 	private ServiceConnection mServiceConnection = new ServiceConnection() {
     @Override
     public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Service connected!");
        HomeControlBinder binder = (HomeControlBinder)service;
        homeControlService = binder.getService();
        homeControlService.setObserver(DeviceListActivity.this);
        if (checkAutoConnect()) {
     	   connectToControlUnit();
        } 
         DeviceAdapter.getInstance().setDevices(homeControlService.getDevices()); // Wiring done
     }
     @Override
     public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected!");
        DeviceAdapter.getInstance().setDevices(null);  // Unwiring done, adapter has no devices to show
        homeControlService = null;
      }
 
 };
 
 //<<< New method for the activity:
 private boolean checkAutoConnect() {
 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 // Check what you use as the first parameter in your setting!!!
 boolean doConnect = sharedPref.getBoolean(SettingsActivity.KEY_PREF_CONNECT_TO_SERVER_SETTING, false);
 return doConnect;
 }
 
 //<<< New method for the activity:
 private void connectToControlUnit() {
 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 // Again check which setting keys you use in your app and if your strings.xml has default address for server.
 String addr = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, getString(R.string.default_server_address));
 if (homeControlService != null) {
 	//thn addr tilalle http://....jne jos ei connect onnistu
   homeControlService.getProtocol().startSession("http://ohap.opimobi.com:18000");
 }
 }
 
 @Override
 public void modelUpdated() {
    if (homeControlService != null) {
       DeviceAdapter.getInstance().setDevices(homeControlService.getDevices());
       getListView().invalidateViews();
    }
 }
 
 //And a new method for activity:
 private void disconnectFromControlUnit(boolean doForceClose) {
 Log.d(TAG, "Disconnect from control unit");
 if (homeControlService != null) {
    homeControlService.getProtocol().endSession(doForceClose);
 }
 }
 
 
 }
