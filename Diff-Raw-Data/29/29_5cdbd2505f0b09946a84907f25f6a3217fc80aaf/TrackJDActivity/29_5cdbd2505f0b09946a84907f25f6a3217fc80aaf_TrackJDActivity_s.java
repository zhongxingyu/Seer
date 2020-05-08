 package org.thereflectproject.trackjd;
 
 import org.thereflectproject.trackjd.R;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * The app's main activity. It's currently very basic: just some monitoring
  * and a few options. 
  */
 public class TrackJDActivity extends Activity {
   /**
   * Assume we're on a LAN.
   */
  private static final String DEFAULT_SERVER_NAME = "localhost:3666";

  /**
    * On a scale of 0 to 1. The intention is to save some battery life if the
    * device is doing nothing but logging.
    */
   private static final float DIMMED_SCREEN_BRIGHTNESS = 0.1f;
 
   /**
    * Reference to the background service, if it's running, or null, if we've
    * lost the connection.
    */
   private TrackJDService service;
 
   /**
    * Connection to the background service. We have to keep a reference to this
    * in order to unbind when the activity is destroyed.
    */
   private ServiceConnection serviceConnection;
 
   /**
    * Called when the user clicks the Stop button. This stops the background
    * service and closes the app.
    * 
    * @param view
    */
   public void clickStop(View view) {
     ensureServiceConnectionUnbound();
     TrackJDService.stopBackgroundService(this);
     finish();
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.main);
 
     final SharedPreferences prefs = getSharedPreferences(
         Constants.PREFS_FILE_NAME, MODE_PRIVATE);
 
     final Handler handler = new Handler();
 
     final EditText serverName = (EditText) findViewById(R.id.server_name);
     serverName.setOnKeyListener(new View.OnKeyListener() {
       public boolean onKey(View v, int keyCode, KeyEvent event) {
         // set the server name when the user presses enter
         if ((event.getAction() == KeyEvent.ACTION_DOWN)
             && (keyCode == KeyEvent.KEYCODE_ENTER)) {
           SharedPreferences.Editor prefsEditor = prefs.edit();
           prefsEditor.putString(Constants.PREF_SERVER_NAME, serverName
               .getText().toString());
           prefsEditor.commit();
           return true;
         }
         return false;
       }
     });
     serverName.setText(prefs.getString(Constants.PREF_SERVER_NAME,
        DEFAULT_SERVER_NAME));
 
     final CheckBox dimScreen = (CheckBox) findViewById(R.id.dim_screen);
     dimScreen
         .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           public void onCheckedChanged(CompoundButton buttonView,
               boolean isChecked) {
             WindowManager.LayoutParams layoutParams = getWindow()
                 .getAttributes();
             if (isChecked) {
               layoutParams.screenBrightness = DIMMED_SCREEN_BRIGHTNESS;
             } else {
               layoutParams.screenBrightness = 1.0f;
             }
             getWindow().setAttributes(layoutParams);
             SharedPreferences.Editor prefsEditor = prefs.edit();
             prefsEditor.putBoolean(Constants.PREF_DIM_SCREEN, isChecked);
             prefsEditor.commit();
           }
         });
     dimScreen.setChecked(prefs.getBoolean(Constants.PREF_DIM_SCREEN, false));
 
     final TextView pointsOnDevice = (TextView) findViewById(R.id.points_on_device);
     final int POINTS_ON_DEVICE_UPDATE_DELAY = 1000; // ms
     handler.post(new Runnable() {
       public void run() {
         if (service == null) {
           pointsOnDevice.setText("(unknown)");
         } else {
           pointsOnDevice.setText(Integer.toString(service.getDataLogger()
               .countPoints()));
         }
         handler.postDelayed(this, POINTS_ON_DEVICE_UPDATE_DELAY);
       }
     });
 
     TrackJDService.startBackgroundService(this);
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onStart()
    */
   @Override
   protected void onStart() {
     super.onStart();
     
     serviceConnection = new ServiceConnection() {
       public void onServiceConnected(ComponentName name, IBinder service) {
         Log.i("TrackJDActivity", "background service connected");
         TrackJDService.LocalBinder binder = (TrackJDService.LocalBinder) service;
         TrackJDActivity.this.service = binder.getService();
       }
 
       public void onServiceDisconnected(ComponentName name) {
         Log.i("TrackJDActivity", "background service disconnected");
         TrackJDActivity.this.service = null;
       }
     };
     
     this.bindService(new Intent(this, TrackJDService.class), serviceConnection,
         0);
   }
 
   /* (non-Javadoc)
    * @see android.app.Activity#onStop()
    */
   @Override
   protected void onStop() {
     super.onStop();
     ensureServiceConnectionUnbound();
   }
   
   private void ensureServiceConnectionUnbound() {
     if (service != null) {
       unbindService(serviceConnection);
       service = null;
       serviceConnection = null;
     }
   }
 }
 
   //
   // This should work according to the docs, but the timeout actually remains
   // at two minutes, at least on my HTC Sensation Z710e.
   // http://developer.android.com/guide/topics/wireless/bluetooth.html
   //
   // There seem to be some long-standing bugs with this part of the API:
   // http://stackoverflow.com/questions/10906737/extend-android-bluetooth-discoverability
   //
   // Fortunately, setting it in the Bluetooth settings screen seems to work, so
   // I am removing this.
   //
   // public void clickMakeDiscoverable(View view) {
   // Intent discoverableIntent = new Intent(
   // BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
   // discoverableIntent.putExtra(
   // BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
   // startActivity(discoverableIntent);
   // }
