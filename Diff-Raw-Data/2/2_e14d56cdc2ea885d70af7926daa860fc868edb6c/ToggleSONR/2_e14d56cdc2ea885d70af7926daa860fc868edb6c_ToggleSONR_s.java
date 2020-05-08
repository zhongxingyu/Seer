 package com.sonrlabs.test.sonr;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 //import org.acra.ErrorReporter;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.os.Build;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 import com.sonrlabs.test.sonr.common.Common;
 import com.sonrlabs.test.sonr.signal.AudioProcessor;
 
 public class ToggleSONR
       extends Service {
 
    private static final String TAG = ToggleSONR.class.getSimpleName();
 //   public static final String INTENT_UPDATE_ICON = "INTENT_UPDATE_ICON";
    private static final String INTENT_USER_TOGGLE_REQUEST = "INTENT_TOGGLE_HEADSET";
 
    /*
    * Constants determined from AudioSystem source
    */
    private static final int DEVICE_IN_WIRED_HEADSET = 0x400000;
    private static final int DEVICE_OUT_EARPIECE = 0x1;
    private static final int DEVICE_OUT_WIRED_HEADSET = 0x4;
    private static final int DEVICE_STATE_UNAVAILABLE = 0;
    private static final int DEVICE_STATE_AVAILABLE = 1;
    
    public static boolean SERVICE_ON = false;
 
    @Override
    public IBinder onBind(Intent arg0) {
       return null;
    }
 
    private static HeadphoneReciever headsetReceiver = null;
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       Notification notification = new Notification();
 
       try {
          // LogFile.MakeLog("ToggleSONR triggered");
          Log.d(TAG, "onStart");
 
          String action = "";
          if (intent != null && intent.getAction() != null) {
             Log.d(TAG, "Received " + intent.getAction());
             action = intent.getAction();
          }
          
          SERVICE_ON = true;
 
          if (headsetReceiver == null) {
             /**
              * Since HEADSET_PLUG uses FLAG_RECIEVER_REGISTERED_ONLY we need to
              * register and unregister the broadcast receiver in the service
              */
             headsetReceiver = new HeadphoneReciever();
             IntentFilter plugIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
             registerReceiver(headsetReceiver, plugIntentFilter);
 
             IntentFilter powerConnectedFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
             registerReceiver(headsetReceiver, powerConnectedFilter);
 
             IntentFilter powerDisconnectedFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
             registerReceiver(headsetReceiver, powerDisconnectedFilter);
          }
 
          if (intent != null && !action.equals("")) {
 
             if (INTENT_USER_TOGGLE_REQUEST.equals(action)) {
 
                Intent i = new Intent(this, SONR.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
 
             } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getExtras().getInt("state");
 
                Log.d(TAG, "Headset plug intent recieved, state " + Integer.toString(state));
                /**
                 * Found by log and source code examine - state 2 is the state on
                 * the multi-function adapter where the 3.5mm audio jack is
                 * plugged in
                 */
 
                if (state != 0) {
                   route_headset(this);
 
                   // LogFile.MakeLog("ToggleSONR routing headset");
 
                   if (isRoutingHeadset(this)) {
                      // LogFile.MakeLog("ToggleSONR headset has mic");
                      if (SONR.neverStarted()) { // if
                         /*
                          * running the app already, don't do autostart, that
                          * would be a mess
                          */
                         SONRClient theclient =
                               new SONRClient(this, AudioProcessor.findAudioRecord(), (AudioManager) this.getSystemService(Context.AUDIO_SERVICE));
                         theclient.createListener();
                         
                         /*
                          * XXX Really call searchSignal again? The createListener call above just did that.
                          */
                         theclient.searchSignal();
                         
                         boolean found = theclient.foundDock();
                         Log.d(TAG, "made it past search signal");
                         if (found) {
                            // LogFile.MakeLog(SONR.DOCK_FOUND);
                            Log.d(TAG, SONR.DOCK_FOUND);
                            SONR.setOn(true);
 
                            if (Common.get(this, SONR.DEFAULT_PLAYER_SELECTED, false)) {
                               Log.d(TAG, "DEFAULT MEDIA PLAYER FOUND");
                               theclient.startListener();
                               SONR.startSonr(this, true);
                            } else {
                               Log.d(TAG, "NO DEFAULT MEDIA PLAYER");
                               Intent i = new Intent(this, SONR.class);
                               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               this.startActivity(i);
                            }
 
                            updateIconON();
                         } else { // dock not found, probably headphones
                            // LogFile.MakeLog("DOCK NOT FOUND");
                            Log.d(TAG, SONR.DOCK_NOT_FOUND);
                            theclient.destroy();
                            startForeground(1337, notification);
 
                           return START_FLAG_REDELIVERY; //START_STICKY;
                         }
                         theclient.destroy();
                      } // end if sonr main screen
                   }
                } else { // end if state != 0
                   unroute_headset(this);
                   // LogFile.MakeLog("ToggleSONR unrouting headset");
                   if (SONR.isOn()) {
                      SONR.setOn(false);
                      Intent stopintent = new Intent(this, StopSONR.class);
                      stopintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(stopintent);
                   }
                }
 
                updateIcon();
             } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                /**
                 * Do nothing - but this intent should wake the service up and
                 * allow us to catch HEADSET_PLUG
                 */
                Log.d(TAG, "Caught POWER_CONNECTED_INTENT");
             } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                /**
                 * Do nothing - but this intent should wake the service up and
                 * allow us to refresh the icon if we were previously asleep
                 */
                Log.d(TAG, "Caught POWER_DISCONNECTED_INTENT");
             }
          }
       } catch (RuntimeException e) {
          e.printStackTrace();
       }
       
       startForeground(1337, notification);
       return START_FLAG_REDELIVERY; //START_STICKY;
    } // end onstart
 
    /**
     * Called when the service is destroyed (low memory conditions). We may miss
     * notification of headset plug
     */
    @Override
    public void onDestroy() {
       try {
          SERVICE_ON = false;
          Log.i(TAG, "onDestroy");
          if (headsetReceiver != null) {
             unregisterReceiver(headsetReceiver);
          }
       } catch (RuntimeException e) {
          e.printStackTrace();
          //ErrorReporter.getInstance().handleException(e);
       }
    }
 
    private static void route_headset(Context ctx) {
       Log.d(TAG, "route to headset");
       AudioManager manager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.DONUT) {
          /*
           * see AudioService.setRouting Use MODE_INVALID to force headset
           * routing change
           */
          manager.setRouting(AudioManager.MODE_INVALID, AudioManager.ROUTE_HEADSET, AudioManager.ROUTE_HEADSET);
       } else {
          setDeviceConnectionState(DEVICE_IN_WIRED_HEADSET, DEVICE_STATE_AVAILABLE, "");
          setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_AVAILABLE, "");
       }
    }
 
    private static void unroute_headset(Context ctx) {
       Log.d(TAG, "unroute headset");
       AudioManager manager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
       
       //Restore notification volume
       SharedPreferences sharedPrefs = ctx.getSharedPreferences(SONR.SHARED_PREFERENCES, 0);
       int savedNotificationVolume = sharedPrefs.getInt("sharedNotificationVolume", 10);
       manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, savedNotificationVolume, AudioManager.FLAG_VIBRATE);
       
       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.DONUT) {
          /*
           * see AudioService.setRouting Use MODE_INVALID to force headset
           * routing change
           */
          manager.setRouting(AudioManager.MODE_INVALID, 0, AudioManager.ROUTE_HEADSET);
       } else {
          setDeviceConnectionState(DEVICE_IN_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "");
          setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "");
          setDeviceConnectionState(DEVICE_OUT_EARPIECE, DEVICE_STATE_AVAILABLE, "");
       }
    }
 
    /**
     * Toggles the current headset setting. If currently routed headset, routes
     * to speaker. If currently routed to speaker routes to headset
     */
 //   public static void toggleHeadset(Context ctx) {
 //      AudioManager manager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
 //      Log.d(TAG, "toggleHeadset");
 //      if (isRoutingHeadset(ctx)) {
 //         Log.d(TAG, "route to earpiece");
 //         if (Build.VERSION.SDK_INT == Build.VERSION_CODES.DONUT) {
 //            /*
 //             * see AudioService.setRouting Use MODE_INVALID to force headset
 //             * routing change
 //             */
 //            manager.setRouting(AudioManager.MODE_INVALID, 0, AudioManager.ROUTE_HEADSET);
 //         } else {
 //            setDeviceConnectionState(DEVICE_IN_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "");
 //            setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_UNAVAILABLE, "");
 //            setDeviceConnectionState(DEVICE_OUT_EARPIECE, DEVICE_STATE_AVAILABLE, "");
 //         }
 //      } else {
 //         Log.d(TAG, "route to headset");
 //         if (Build.VERSION.SDK_INT == Build.VERSION_CODES.DONUT) {
 //            /*
 //             * see AudioService.setRouting Use MODE_INVALID to force headset
 //             * routing change
 //             */
 //            manager.setRouting(AudioManager.MODE_INVALID, AudioManager.ROUTE_HEADSET, AudioManager.ROUTE_HEADSET);
 //         } else {
 //            setDeviceConnectionState(DEVICE_IN_WIRED_HEADSET, DEVICE_STATE_AVAILABLE, "");
 //            setDeviceConnectionState(DEVICE_OUT_WIRED_HEADSET, DEVICE_STATE_AVAILABLE, "");
 //         }
 //      }
 //   }
 
    /**
     * Checks whether we are currently routing to headset
     * 
     * @return true if routing to headset, false if routing somewhere else
     */
    private static boolean isRoutingHeadset(Context ctx) {
       boolean isRoutingHeadset = false;
 
       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.DONUT) {
          /*
           * The code that works and is tested for Donut...
           */
          AudioManager manager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
 
          int routing = manager.getRouting(AudioManager.MODE_NORMAL);
          Log.d(TAG, "getRouting returns " + routing);
          isRoutingHeadset = (routing & AudioManager.ROUTE_HEADSET) != 0;
       } else {
          /*
           * Code for Android 2.1, 2.2, 2.3, maybe others... Thanks Adam King!
           */
          try {
             /**
              * Use reflection to get headset routing
              */
             Class<?> audioSystem = Class.forName("android.media.AudioSystem");
             Method getDeviceConnectionState = audioSystem.getMethod("getDeviceConnectionState", int.class, String.class);
 
             int retVal = (Integer) getDeviceConnectionState.invoke(audioSystem, DEVICE_IN_WIRED_HEADSET, "");
 
             isRoutingHeadset = retVal == 1;
             Log.d(TAG, "getDeviceConnectionState " + retVal);
 
          } catch (RuntimeException e) {
             Log.e(TAG, "Could not determine status in isRoutingHeadset(): " + e);
          } catch (ClassNotFoundException e) {
             Log.e(TAG, "Could not determine status in isRoutingHeadset(): " + e);
          } catch (NoSuchMethodException e) {
             Log.e(TAG, "Could not determine status in isRoutingHeadset(): " + e);
          } catch (IllegalAccessException e) {
             Log.e(TAG, "Could not determine status in isRoutingHeadset(): " + e);
          } catch (InvocationTargetException e) {
             Log.e(TAG, "Could not determine status in isRoutingHeadset(): " + e);
          }
       }
       return isRoutingHeadset;
    }
 
    /**
     * Updates the icon of the appwidget based on the current status of headphone
     * routing
     */
    void updateIconON() {
       RemoteViews view = new RemoteViews(this.getPackageName(), R.layout.toggle_apwidget);
       view.setImageViewResource(R.id.Icon, R.drawable.sonr_on);
 
       // Create an Intent to launch toggle headset
       Intent toggleIntent = new Intent(this, ToggleSONR.class);
       toggleIntent.setAction(INTENT_USER_TOGGLE_REQUEST);
       PendingIntent pendingIntent = PendingIntent.getService(this, 0, toggleIntent, 0);
 
       // Get the layout for the App Widget and attach an on-click listener to
       // the icon
       view.setOnClickPendingIntent(R.id.Icon, pendingIntent);
 
       ComponentName thisWidget = new ComponentName(this, SonrWidget.class);
       AppWidgetManager manager = AppWidgetManager.getInstance(this);
       manager.updateAppWidget(thisWidget, view);
    }
 
 //   public void updateIconOFF() {
 //      RemoteViews view = new RemoteViews(this.getPackageName(), R.layout.toggle_apwidget);
 //      view.setImageViewResource(R.id.Icon, R.drawable.sonr_off);
 //
 //      // Create an Intent to launch toggle headset
 //      Intent toggleIntent = new Intent(this, ToggleSONR.class);
 //      toggleIntent.setAction(INTENT_USER_TOGGLE_REQUEST);
 //      PendingIntent pendingIntent = PendingIntent.getService(this, 0, toggleIntent, 0);
 //
 //      // Get the layout for the App Widget and attach an on-click listener to
 //      // the icon
 //      view.setOnClickPendingIntent(R.id.Icon, pendingIntent);
 //
 //      ComponentName thisWidget = new ComponentName(this, SonrWidget.class);
 //      AppWidgetManager manager = AppWidgetManager.getInstance(this);
 //      manager.updateAppWidget(thisWidget, view);
 //   }
 
    void updateIcon() {
       Log.d(TAG, "updateIcon");
 
       RemoteViews view = new RemoteViews(this.getPackageName(), R.layout.toggle_apwidget);
 
       if (isRoutingHeadset(this)) {
          Log.d(TAG, "Headset is routed");
          view.setImageViewResource(R.id.Icon, R.drawable.sonr_on);
       } else {
          Log.d(TAG, "Headset not routed");
          view.setImageViewResource(R.id.Icon, R.drawable.sonr_off);
       }
 
       // Create an Intent to launch toggle headset
       Intent toggleIntent = new Intent(this, ToggleSONR.class);
       toggleIntent.setAction(ToggleSONR.INTENT_USER_TOGGLE_REQUEST);
       PendingIntent pendingIntent = PendingIntent.getService(this, 0, toggleIntent, 0);
 
       // Get the layout for the App Widget and attach an on-click listener to
       // the icon
       view.setOnClickPendingIntent(R.id.Icon, pendingIntent);
 
       // Push update for this widget to the home screen
       ComponentName thisWidget = new ComponentName(this, SonrWidget.class);
       AppWidgetManager manager = AppWidgetManager.getInstance(this);
       manager.updateAppWidget(thisWidget, view);
    }
 
    /**
     * set device connection state through reflection for Android 2.1, 2.2, 2.3,
     * maybe others. Thanks Adam King!
     * 
     * @param device
     * @param state
     * @param address
     */
    private static void setDeviceConnectionState(final int device, final int state, final String address) {
       try {
          Class<?> audioSystem = Class.forName("android.media.AudioSystem");
          Method setDeviceConnectionState = audioSystem.getMethod("setDeviceConnectionState", int.class, int.class, String.class);
 
          setDeviceConnectionState.invoke(audioSystem, device, state, address);
       } catch (RuntimeException e) {
          Log.e(TAG, "setDeviceConnectionState failed: " + e);
       } catch (ClassNotFoundException e) {
          Log.e(TAG, "setDeviceConnectionState failed: " + e);
       } catch (NoSuchMethodException e) {
          Log.e(TAG, "setDeviceConnectionState failed: " + e);
       } catch (IllegalAccessException e) {
          Log.e(TAG, "setDeviceConnectionState failed: " + e);
       } catch (InvocationTargetException e) {
          Log.e(TAG, "setDeviceConnectionState failed: " + e);
       }
    }
 }
