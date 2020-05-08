 package ch.bfh.evoting.voterapp;
 
 
 import org.apache.log4j.Level;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Application;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.WindowManager;
 import android.widget.Toast;
 import ch.bfh.evoting.voterapp.network.AllJoynNetworkInterface;
 import ch.bfh.evoting.voterapp.network.NetworkInterface;
 import ch.bfh.evoting.voterapp.network.NetworkMonitor;
 import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
 import ch.bfh.evoting.voterapp.util.JavaSerialization;
 import ch.bfh.evoting.voterapp.util.SerializationUtil;
 import ch.bfh.evoting.voterapp.util.Utility;
 
 /**
  * Class representing the application. This class is used to do some initializations and to share data.
  * @author Philémon von Bergen
  *
  */
 public class AndroidApplication extends Application {
 
 	public static final String PREFS_NAME = "network_preferences";
 	public static final Level LEVEL = Level.DEBUG;
 
 	private static AndroidApplication instance;
 	private SerializationUtil su;
 	private NetworkInterface ni;
 	private Activity currentActivity = null;
 	private boolean isAdmin = false;
 	private boolean voteRunning;
 
 
 	private AlertDialog dialogNetworkLost;
	private AlertDialog dialogWrongKey;
 	private NetworkMonitor networkMonitor;
 
 	/**
 	 * Return the single instance of this class
 	 * @return the single instance of this class
 	 */
 	public static AndroidApplication getInstance() {
 		return instance;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		//TODO remove when not used anymore
 //				SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
 //				settings.edit().putBoolean("first_run_ReviewPollVoterActivity", true).commit();
 //				settings.edit().putBoolean("first_run_NetworkConfigActivity", true).commit();
 //				settings.edit().putBoolean("first_run", true).commit();
 
 
 		WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
 		if(!wm.isWifiEnabled()){
 			wm.setWifiEnabled(true);
 		}
 
 		instance = this;
 		instance.initializeInstance();
 		Utility.initialiseLogging();
 
 		//wifi event listener
 		IntentFilter filters = new IntentFilter();
 		filters.addAction("android.net.wifi.STATE_CHANGED");
 		filters.addAction("android.net.conn.CONNECTIVITY_CHANGE");
 		networkMonitor = new NetworkMonitor(this);
 		this.registerReceiver(networkMonitor, filters);
 		registerActivityLifecycleCallbacks(new AndroidApplicationActivityLifecycleCallbacks());
 
 
 		LocalBroadcastManager.getInstance(this).registerReceiver(mGroupEventReceiver, new IntentFilter(BroadcastIntentTypes.networkGroupDestroyedEvent));
 		LocalBroadcastManager.getInstance(this).registerReceiver(mAttackDetecter, new IntentFilter(BroadcastIntentTypes.attackDetected));
 		LocalBroadcastManager.getInstance(this).registerReceiver(startPollReceiver, new IntentFilter(BroadcastIntentTypes.electorate));
 		LocalBroadcastManager.getInstance(this).registerReceiver(wrongDecryptionKeyReceiver, new IntentFilter(BroadcastIntentTypes.probablyWrongDecryptionKeyUsed));
 	}
 
 	@Override
 	public void onTerminate() {
 		if(this.ni!=null)
 			this.ni.disconnect();
 		this.unregisterReceiver(networkMonitor);
 		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notificationManager.cancel("mobivote", 1);
 		super.onTerminate();
 	}
 
 
 
 	/*--------------------------------------------------------------------------------------------
 	 * Helper Methods
 	--------------------------------------------------------------------------------------------*/
 
 	/**
 	 * Initialize the Serialization method and the Network Component to use
 	 */
 	private void initializeInstance() {
 
 		//initialize ICE for AllJoyn
 		//This must be done on the main thread
         org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(this); 
 
 		new AsyncTask<Object, Object, Object>() {
 
 			@Override
 			protected Object doInBackground(Object... params) {
 
 				su = new SerializationUtil(new JavaSerialization());
 				ni = new AllJoynNetworkInterface(AndroidApplication.this.getApplicationContext());///* new InstaCircleNetworkInterface(this.getApplicationContext());*/new SimulatedNetworkInterface(AndroidApplication.this.getApplicationContext());
 
 				return null;
 			}
 		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 
 	}
 
 
 	
 
 	/*--------------------------------------------------------------------------------------------
 	 * Getters/Setters
 	--------------------------------------------------------------------------------------------*/
 
 	/**
 	 * Get the serialization helper
 	 * @return the serialization helper
 	 */
 	public SerializationUtil getSerializationUtil(){
 		return su;
 	}
 
 	/**
 	 * Get the network component
 	 * @return the network component
 	 */
 	public NetworkInterface getNetworkInterface(){
 		return ni;
 	}
 
 	/**
 	 * Get the network monitor receiving wifi events
 	 * @return the network monitor receiving wifi events
 	 */
 	public NetworkMonitor getNetworkMonitor(){
 		return this.networkMonitor;
 	}
 
 	/**
 	 * Get the activity that is currently running
 	 * @return the activity that is currently running, null if none is running
 	 */
 	public Activity getCurrentActivity(){
 		return currentActivity;
 	}
 
 	/**
 	 * Set the activity that is currently running
 	 * @param currentActivity the activity that is currently running
 	 */
 	public void setCurrentActivity(Activity currentActivity){
 		this.currentActivity = currentActivity;
 
 		if(isVoteRunning()){
 			// Create a pending intent which will be invoked after tapping on the
 			// Android notification
 			Intent notificationIntent = new Intent(this,
 					currentActivity.getClass());
 			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			PendingIntent pendingNotificationIntent = PendingIntent.getActivity(
 					this, 0, notificationIntent, 0);
 
 			// Setting up the notification which is being displayed
 			Notification.Builder notificationBuilder = new Notification.Builder(
 					this);
 			notificationBuilder.setContentTitle(getResources().getString(
 					R.string.voter_app_app_name));
 			notificationBuilder
 			.setContentText(getResources().getString(R.string.notification));
 			notificationBuilder
 			.setSmallIcon(R.drawable.ic_launcher);
 			notificationBuilder.setContentIntent(pendingNotificationIntent);
 			notificationBuilder.setOngoing(true);
 			@SuppressWarnings("deprecation")
 			Notification notification = notificationBuilder.getNotification();
 
 			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 			notificationManager.notify("mobivote", 1, notification);
 		} else {
 			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 			notificationManager.cancel("mobivote", 1);
 		}
 	}
 
 	/**
 	 * Set a flag indicating that a vote session is running
 	 * @param running true if a vote session is running, false otherwise
 	 */
 	public void setVoteRunning(boolean running){
 		this.voteRunning = running;
 	}
 
 	/**
 	 * Indicate if a vote session is running
 	 * @return true if yes, false otherwise
 	 */
 	public boolean isVoteRunning(){
 		return voteRunning;
 	}
 
 	/**
 	 * Indicate if this user is the administrator of the vote
 	 * @return true if yes, false otherwise
 	 */
 	public boolean isAdmin() {
 		return isAdmin;
 	}
 
 	/**
 	 * Set if this user is the administrator of the vote 
 	 * @param isAdmin true if this user is the administrator of the vote, false otherwise
 	 */
 	public void setIsAdmin(boolean isAdmin) {
 		this.isAdmin = isAdmin;
 	}
 
 
 
 
 	/*--------------------------------------------------------------------------------------------
 	 * Broadcast receivers
 	--------------------------------------------------------------------------------------------*/
 
 	/**
 	 * this broadcast receiver listens for information about the network group destruction
 	 */
 	private BroadcastReceiver mGroupEventReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(ni==null) return;
 			if(currentActivity!=null && ni.getNetworkName()!=null){
 				if(voteRunning){
 					AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
 					// Add the buttons
 					builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							Intent i = new Intent(AndroidApplication.this, MainActivity.class);
 							currentActivity.startActivity(i);
 						}
 					});
 
 					builder.setTitle(R.string.dialog_title_network_lost);
 					builder.setMessage(R.string.dialog_network_lost);
 
 
 					dialogNetworkLost = builder.create();
 					dialogNetworkLost.setOnShowListener(new DialogInterface.OnShowListener() {
 						@Override
 						public void onShow(DialogInterface dialog) {
 							Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
 							dialogNetworkLost.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundResource(
 									R.drawable.selectable_background_votebartheme);
 						}
 					});
 
 					// Create the AlertDialog
 					dialogNetworkLost.show();
 				} else {
 					for(int i=0; i < 2; i++)
 						Toast.makeText(currentActivity, getResources().getString(R.string.toast_network_lost), Toast.LENGTH_SHORT).show();
 				}
 				ni.disconnect();
 			}
 		}
 	};
 
 	/**
 	 * this broadcast receiver listens for information about an attack
 	 */
 	private BroadcastReceiver mAttackDetecter = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(currentActivity!=null && voteRunning){
 				AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
 				// Add the buttons
 				builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Intent i = new Intent(AndroidApplication.this, MainActivity.class);
 						currentActivity.startActivity(i);
 					}
 				});
 
 				builder.setTitle(R.string.dialog_title_attack_detected);
 				if(intent.getIntExtra("type", 0)==1){
 					builder.setMessage(R.string.dialog_attack_impersonalization);
 				} else if(intent.getIntExtra("type", 0)==2){
 					builder.setMessage(R.string.dialog_attack_different_senders);
 				} else if(intent.getIntExtra("type", 0)==3){
 					builder.setMessage(R.string.dialog_attack_different_salts);
 				}
 
 				// Create the AlertDialog
 				builder.create().show();
 			}
 		}
 	};
 
 	/**
 	 * this broadcast receiver listen for broadcasts containing the electorate. So, if the user is member
 	 * of a session, when the admin sends the electorate, the user is redirected to the correct activity, wherever
 	 * he is.
 	 */
 	private BroadcastReceiver startPollReceiver = new BroadcastReceiver(){
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(!isAdmin && !(currentActivity instanceof CheckElectorateActivity) && currentActivity!=null){
 				Intent i = new Intent(AndroidApplication.this, CheckElectorateActivity.class);
 				i.putExtra("participants", intent.getSerializableExtra("participants"));
 				currentActivity.startActivity(i);
 			}
 		}
 	};
 	
 	/**
 	 * this broadcast receiver listens for messages indicating that many decryptions failed
 	 */
 	private BroadcastReceiver wrongDecryptionKeyReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if(currentActivity!=null){
 				AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
				
 				// Add the buttons
 				builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				});
 
 				builder.setTitle(R.string.dialog_title_wrong_key);
 				builder.setMessage(R.string.dialog_wrong_key_pwd);
 				
 				// Create the AlertDialog
				dialogWrongKey = builder.create(); 
				
				dialogWrongKey.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
						dialogWrongKey.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundResource(
								R.drawable.selectable_background_votebartheme);
					}
				});
				dialogWrongKey.show();
 			}
 		}
 	};
 
 
 	private class AndroidApplicationActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
 
 		private String connectedSSID = "";
 
 		public void onActivityCreated(Activity activity, Bundle bundle) {
 			if(isVoteRunning()){
 				activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
 				activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 			}
 		}
 
 		public void onActivityDestroyed(Activity activity) {
 		}
 
 		public void onActivityPaused(Activity activity) {
 			this.connectedSSID = networkMonitor.getConnectedSSID();
 		}
 
 		public void onActivityResumed(Activity activity) {
 			if(this.connectedSSID == null) return;
 			if(!this.connectedSSID.equals(networkMonitor.getConnectedSSID())){
 				Intent intent = new Intent(BroadcastIntentTypes.networkGroupDestroyedEvent);
 				LocalBroadcastManager.getInstance(AndroidApplication.this).sendBroadcast(intent);
 			}
 		}
 
 		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
 		}
 
 		public void onActivityStarted(Activity activity) {
 		}
 
 		public void onActivityStopped(Activity activity) {
 		}
 	}
 
 }
