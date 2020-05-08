 package uk.org.smithfamily.mslogger.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.GPSLocationManager;
 import uk.org.smithfamily.mslogger.MSLoggerApplication;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.log.DatalogManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.log.EmailManager;
 import uk.org.smithfamily.mslogger.log.FRDLogManager;
 import uk.org.smithfamily.mslogger.widgets.BarGraph;
 import uk.org.smithfamily.mslogger.widgets.Gauge;
 import uk.org.smithfamily.mslogger.widgets.GaugeDetails;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegister;
 import uk.org.smithfamily.mslogger.widgets.Histogram;
 import uk.org.smithfamily.mslogger.widgets.Indicator;
 import uk.org.smithfamily.mslogger.widgets.IndicatorManager;
 import uk.org.smithfamily.mslogger.widgets.NumericIndicator;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Main activity class where the main window (gauges) are and where the bottom menu is handled 
  */
 public class MSLoggerActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener
 {
     private BroadcastReceiver      updateReceiver        = new Reciever();
     private IndicatorManager       indicatorManager;
     private TextView               messages;
     private TextView               rps;
     static private Boolean         ready                 = null;
     
     private Indicator[]            indicators = new Indicator[5];
 
     private GestureDetector        gestureDetector;
     private boolean                gaugeEditEnabled;
     boolean                        scrolling;
     private LinearLayout           layout;
     private static final int       SHOW_PREFS            = 124230;
 
     private boolean                registered;
 	
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         checkSDCard();
         
         DebugLogManager.INSTANCE.log(getPackageName(),Log.ASSERT);
         try
         {
             String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
             DebugLogManager.INSTANCE.log(app_ver,Log.ASSERT);
         }
         catch (NameNotFoundException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }
         dumpPreferences();
         setContentView(R.layout.displaygauge);
         
         
         messages = (TextView) findViewById(R.id.messages);
         rps = (TextView) findViewById(R.id.RPS);
         
         /* 
          * Get status message from saved instance, for example when
          * switching from landscape to portrait mode
         */
         if (savedInstanceState != null) 
         {
             if (!savedInstanceState.getString("status_message").equals(""))
             {
                 messages.setText(savedInstanceState.getString("status_message"));
             }
             
             if (!savedInstanceState.getString("rps_message").equals(""))
             {
                 rps.setText(savedInstanceState.getString("rps_message"));
             }
         }
 
         findGauges();
         SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
         prefsManager.registerOnSharedPreferenceChangeListener(MSLoggerActivity.this);
 
         indicatorManager = IndicatorManager.INSTANCE;
         indicatorManager.setDisabled(true);
 
         ApplicationSettings.INSTANCE.setDefaultAdapter(BluetoothAdapter.getDefaultAdapter());
         GPSLocationManager.INSTANCE.start();
         ApplicationSettings.INSTANCE.setAutoConnectOverride(null);
 
         registerMessages();
         Intent serverIntent = new Intent(this, StartupActivity.class);
         startActivityForResult(serverIntent, MSLoggerApplication.PROBE_ECU);
     }
     
     /**
      * Save the bottom text views content so they can keep their state while device is rotated
      */
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         
         outState.putString("status_message", messages.getText().toString());
         outState.putString("rps_message", rps.getText().toString());
     }
     
     /**
      * Dump the user preference into the log file for easier debugging
      */
     private void dumpPreferences()
     {
         SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
         Map<String, ?> prefs = prefsManager.getAll();
         for(Entry<String, ?> entry : prefs.entrySet())
         {
             DebugLogManager.INSTANCE.log("Preference:"+entry.getKey()+":"+entry.getValue(), Log.ASSERT);
         }
     }
 
     /**
      * Complete the initialisation and load/init the gauges
      */
     private void completeCreate()
     {
         if (ready == null)
         {
             new InitTask().execute((Void) null);
         }
         else
         {
             finaliseInit();
         }
     }
 
     /**
      * Finilise the initialisation of the application by initialising the gauges, checking bluetooth, SD card and starting connection to the Megasquirt
      */
     private void finaliseInit()
     {
         initGauges();
 
         checkBTDeviceSet();
         checkSDCard();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         if (ecu != null)
         {
             ecu.start();
         }
 
     }
 
     /**
      * Check if the SD card is present
      */
     private void checkSDCard()
     {
         boolean cardOK = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
         ApplicationSettings.INSTANCE.setWritable(cardOK);
         if (!cardOK)
         {
             showDialog(2);
         }
     }
 
     /**
      * Clean up messages and GPS when the app is stopped
      */
     @Override
     protected void onDestroy()
     {
         deRegisterMessages();
         GPSLocationManager.INSTANCE.stop();
         super.onDestroy();
     }
 
     /**
      * Save the gauges when the app is stopped
      */
     @Override
     public void onStop()
     {
         super.onStop();
         saveGauges();
     }
 
     /**
      * Ask to select a Bluetooth device is none is selected
      */
     private void checkBTDeviceSet()
     {
         if (!ApplicationSettings.INSTANCE.btDeviceSelected())
         {
             messages.setText(R.string.please_select);
         }
     }
 
     /**
      * Save current selected gauges in preferences
      */
     private void saveGauges()
     {
         if (!(indicators[0] != null && indicators[1] != null && indicators[2] != null && indicators[3] != null && indicators[4] != null))
         {
             findGauges();
         }
         if (indicators[0] != null && indicators[1] != null && indicators[2] != null && indicators[3] != null && indicators[4] != null)
         {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             Editor editor = prefs.edit();
             
             if (!indicators[0].getName().equals(Gauge.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge1", indicators[0].getName());
             }
             if (!indicators[1].getName().equals(Gauge.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge2", indicators[1].getName());
             }
             if (!indicators[2].getName().equals(Gauge.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge3", indicators[2].getName());
             }
             if (!indicators[3].getName().equals(Gauge.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge4", indicators[3].getName());
             }
             if (!indicators[4].getName().equals(Gauge.DEAD_GAUGE_NAME))
             {
                 editor.putString("gauge5", indicators[4].getName());
             }
             
             editor.commit();
         }
     }
 
     /**
      * Load the gauges
      */
     private void loadGauges()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         GaugeRegister.INSTANCE.flush();
         ecu.initGauges();
     }
 
     /**
      * Init the gauges with the proper gauge saved in preference, default to firmware defined gauge if preference are empty
      */
     private void initGauges()
     {
         layout = (LinearLayout) (findViewById(R.id.layout));
         findGauges();
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         String[] defaultGauges = ecu.defaultGauges();
         
         indicators[0].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge1", defaultGauges[0]));
         indicators[1].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge2", defaultGauges[1]));
         indicators[2].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge3", defaultGauges[2]));
         indicators[3].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge4", defaultGauges[3]));
         
         if (indicators[4] != null)
         {
             indicators[4].initFromName(ApplicationSettings.INSTANCE.getOrSetPref("gauge5", defaultGauges[4]));
         }
        
         // Look at all indicators and make sure they are the right type
         for (int i = 0; i < indicators.length; i++)
         {
             if (indicators[i] != null)
             {
                 boolean wasWrongType = false;
                 String name = indicators[i].getName();
                 int id = indicators[i].getId();
 
                 GaugeDetails gd = GaugeRegister.INSTANCE.getGaugeDetails(name);
                 
                 if (gd.getType().equals(getString(R.string.gauge)) && !(indicators[i] instanceof Gauge))
                 {
                     indicators[i] = new Gauge(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.bargraph)) && !(indicators[i] instanceof BarGraph))
                 {
                     indicators[i] = new BarGraph(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.numeric_indicator)) && !(indicators[i] instanceof NumericIndicator))
                 {
                     indicators[i] = new NumericIndicator(this);
                     wasWrongType = true;
                 }
                 else if (gd.getType().equals(getString(R.string.histogram)) && !(indicators[i] instanceof Histogram))
                 {
                     indicators[i] = new Histogram(this);
                     wasWrongType = true;
                 }
                 
                 if (wasWrongType)
                 {
                     View indicator = findViewById(id);
                     
                     // Remove indicator with wrong type
                     ViewGroup parentView = (ViewGroup) indicator.getParent();
                     int index = parentView.indexOfChild(indicator);
                     parentView.removeView(indicator);
 
                     // Add new indicator back in place
                     parentView.addView(indicators[i], index);    
                     
                     indicators[i].setId(id);
                     indicators[i].initFromName(name);
                     
                     LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f);
                     indicators[i].setLayoutParams(params);
                     
                     indicators[i].setFocusable(true);
                     indicators[i].setFocusableInTouchMode(true);
                 }
             }
         }
         
         if (gaugeEditEnabled)
         {
             indicators[0].setOnTouchListener(new GaugeTouchListener(indicators[0]));
             indicators[1].setOnTouchListener(new GaugeTouchListener(indicators[1]));
             indicators[3].setOnTouchListener(new GaugeTouchListener(indicators[3]));
             
             if (indicators[4] != null)
             {
                 indicators[4].setOnTouchListener(new GaugeTouchListener(indicators[4]));
             }
             
             initIndicator3Events();
         }
         else
         {
             MarkListener l = new MarkListener(layout);
             setTouchListeners(l);
         }
         
         indicators[0].invalidate();
         indicators[1].invalidate();
         indicators[2].invalidate();
         indicators[3].invalidate();
         
         if (indicators[4] != null)
             indicators[4].invalidate();
 
     }
 
     /**
      * 
      */
     public void initIndicator3Events()
     {
         gestureDetector = new GestureDetector(new GaugeRotationDetector(MSLoggerActivity.this, indicators[2]));
         
         OnTouchListener gestureListener = new View.OnTouchListener()
         {
             public boolean onTouch(View v, MotionEvent event)
             {
                 if (gestureDetector.onTouchEvent(event))
                 {
                     return true;
                 }
 
                 if (event.getAction() == MotionEvent.ACTION_UP)
                 {
                     if (scrolling)
                     {
                         scrolling = false;
                         GaugeDetails gd = indicators[2].getDetails();
                         GaugeRegister.INSTANCE.persistDetails(gd);
                     }
                 }
 
                 return false;
             }
         };
         
         indicators[2].setOnClickListener(MSLoggerActivity.this);
         indicators[2].setOnTouchListener(gestureListener);
     }
     
     /**
      * Set all the gauges variable with their view
      */
     private void findGauges()
     {
         indicators[0] = (Indicator) findViewById(R.id.g1);
         indicators[1] = (Indicator) findViewById(R.id.g2);
         indicators[2] = (Indicator) findViewById(R.id.g3);
         indicators[3] = (Indicator) findViewById(R.id.g4);
         indicators[4] = (Indicator) findViewById(R.id.g5);
     }
 
     /**
      * Set the touch listener on the gauge
      * 
      * @param l
      */
     private void setTouchListeners(MarkListener l)
     {
         indicators[0].setOnTouchListener(l);
         indicators[1].setOnTouchListener(l);
         indicators[2].setOnTouchListener(l);
         indicators[3].setOnTouchListener(l);
         
         if (indicators[4] != null)
             indicators[4].setOnTouchListener(l);
     }
 
     /**
      * Register the receiver with the message to receive from the Megasquirt connection
      */
     private void registerMessages()
     {
         IntentFilter connectedFilter = new IntentFilter(Megasquirt.CONNECTED);
         registerReceiver(updateReceiver, connectedFilter);
         
         IntentFilter disconnectedFilter = new IntentFilter(Megasquirt.DISCONNECTED);
         registerReceiver(updateReceiver, disconnectedFilter);
         
         IntentFilter dataFilter = new IntentFilter(Megasquirt.NEW_DATA);
         registerReceiver(updateReceiver, dataFilter);
         
         IntentFilter msgFilter = new IntentFilter(ApplicationSettings.GENERAL_MESSAGE);
         registerReceiver(updateReceiver, msgFilter);
         
         IntentFilter rpsFilter = new IntentFilter(ApplicationSettings.RPS_MESSAGE);
         registerReceiver(updateReceiver, rpsFilter);
         
         registered = true;
     }
 
     /**
      * Unregister receiver
      */
     private void deRegisterMessages()
     {
         if (registered)
         {
             unregisterReceiver(updateReceiver);
         }
     }
 
     /**
      * Process data got from Megasquirt and update the gauges with it
      */
     protected void processData()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         List<Indicator> indicators;
         if ((indicators = indicatorManager.getIndicators()) != null)
         {
             indicatorManager.setDisabled(false);
             for (Indicator i : indicators)
             {
                 String channelName = i.getChannel();
                 if (channelName != null)
                 {
                     double value = ecu.getField(channelName);
                     i.setValue(value);
                 }
                 else
                 {
                     i.setValue(0);
                 }
             }
         }
     }
 
  
     /**
      * 
      * @param menu
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     /**
      * Triggered just before the bottom menu are displayed, used to update the state of some menu items
      * 
      * @param menu
      */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem editItem = menu.findItem(R.id.gaugeEditing);
         Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         editItem.setEnabled(ecuDefinition != null);
         if (gaugeEditEnabled)
         {
             editItem.setIcon(R.drawable.ic_menu_disable_gauge_editing);
             editItem.setTitle(R.string.disable_gauge_edit);
         }
         else
         {
             editItem.setIcon(R.drawable.ic_menu_enable_gauge_editing);
             editItem.setTitle(R.string.enable_gauge_edit);
         }
         MenuItem connectionItem = menu.findItem(R.id.forceConnection);
         if (ecuDefinition != null && ecuDefinition.isRunning())
         {
             connectionItem.setIcon(R.drawable.ic_menu_disconnect);
             connectionItem.setTitle(R.string.disconnect);
         }
         else
         {
             connectionItem.setIcon(R.drawable.ic_menu_connect);
             connectionItem.setTitle(R.string.connect);
         }
 
         MenuItem loggingItem = menu.findItem(R.id.forceLogging);
         loggingItem.setEnabled(ecuDefinition != null);
                 
         if (ecuDefinition != null && ecuDefinition.isLogging())
         {
             loggingItem.setIcon(R.drawable.ic_menu_stop_logging);
             loggingItem.setTitle(R.string.stop_logging);
         }
         else
         {
             loggingItem.setIcon(R.drawable.ic_menu_start_logging);
             loggingItem.setTitle(R.string.start_logging);
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     /**
      * Triggered when a bottom menu item is clicked
      * 
      * @param item  The clicked menu item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         checkBTDeviceSet();
         int itemId = item.getItemId();
         if (itemId == R.id.forceConnection)
         {
             toggleConnection();
             return true;
         }
         else if (itemId == R.id.gaugeEditing)
         {
             toggleEditing();
             return true;
         }
         else if (itemId == R.id.forceLogging)
         {
             toggleLogging();
             return true;
         }
         else if (itemId == R.id.manageDatalogs)
         {
             openManageDatalogs();
             return true;
         }
         else if (itemId == R.id.preferences)
         {
             openPreferences();
             return true;
         }        
         else if (itemId == R.id.resetGauges)
         {
             resetGuages();
             return true;
         }
         else if (itemId == R.id.calibrate)
         {
             openCalibrateTPS();
             return true;
         }
         else if (itemId == R.id.about)
         {
             showAbout();
             return true;
         }
         else if (itemId == R.id.quit)
         {
             quit();
             return true;
         }
         else
         {
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Used by edit gauge to change the instance of gauge3
      */
     public void setIndicator3(Indicator indicator)
     {
         indicators[2] = indicator;
         if (gaugeEditEnabled)
         {
             indicators[2].setOnTouchListener(new GaugeTouchListener(indicators[2]));
         }
     }
     
     /**
      * Start the background task to reset the gauges
      */
     public void resetGuages()
     {
         new ResetGaugesTask().execute((Void) null);
     }
 
     /**
      * Toggle the gauge editing
      */
     private void toggleEditing()
     {
         // Gauge editing is enabled
         if (gaugeEditEnabled)
         {
             saveGauges();
         }
         // Gauge editing is not enabled
         else
         {
             for (int i = 0; i < 2; i++)
             {
                 Toast.makeText(getApplicationContext(), R.string.edit_gauges_instructions, Toast.LENGTH_LONG).show();
             }
         }
         
         gaugeEditEnabled = !gaugeEditEnabled;
         initGauges();
     }
 
     /**
      * Toggle data logging of the Megasquirt
      */
     private void toggleLogging()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null) {
             if (ecu.isLogging())
             {
                 ecu.stopLogging();
                 sendLogs();
             }
             else
             {
                 ecu.startLogging();
             }
         }
     }
 
     /**
      * If the user opted in to get logs sent to his email, we prompt him
      */
     private void sendLogs()
     {
         if (ApplicationSettings.INSTANCE.emailEnabled())
         {
             DatalogManager.INSTANCE.close();
             FRDLogManager.INSTANCE.close();
             List<String> paths = new ArrayList<String>();
             paths.add(DatalogManager.INSTANCE.getAbsolutePath());
             paths.add(FRDLogManager.INSTANCE.getAbsolutePath());
             paths.add(DebugLogManager.INSTANCE.getAbsolutePath());
             String emailText = getString(R.string.email_body);
 
             String subject = String.format(getString(R.string.email_subject), System.currentTimeMillis());
             EmailManager.email(this, ApplicationSettings.INSTANCE.getEmailDestination(), null, subject, emailText, paths);
         }
 
     }
 
     /**
      * Quit the application cleanly by stopping the connection to the Megasquirt if it exists
      */
     private void quit()
     {
         ApplicationSettings.INSTANCE.setAutoConnectOverride(false);
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null)
         {
             ecu.stop();
         }
 
         sendLogs();
 
         if (ecu != null)
         {
             ecu.reset();
         }
 
         this.finish();
     }
 
     /**
      * Toggle the connection to the Megasquirt
      */
     private void toggleConnection()
     {
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ecu != null)
         {
             ecu.toggleConnection();
         }
     }
 
     /**
      * Open an about dialog
      */
     private void showAbout()
     {
         Dialog dialog = new Dialog(this);
 
         dialog.setContentView(R.layout.about);
 
         TextView text = (TextView) dialog.findViewById(R.id.text);
         String title = "";
         try
         {
             PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
             ApplicationInfo ai;
             ai = pInfo.applicationInfo;
             final String applicationName = (String) (ai != null ? getPackageManager().getApplicationLabel(ai) : "(unknown)");
             title = applicationName + " " + pInfo.versionName;
         }
         catch (NameNotFoundException e)
         {
             e.printStackTrace();
         }
         dialog.setTitle(title);
 
         text.setText(R.string.about_text);
         ImageView image = (ImageView) dialog.findViewById(R.id.image);
         image.setImageResource(R.drawable.icon);
 
         dialog.show();
     }
 
     /**
      * Open the calibrate TPS activity
      */
     private void openCalibrateTPS()
     {
         Intent launchCalibrate = new Intent(this, CalibrateActivity.class);
         startActivity(launchCalibrate);
     }
 
     /**
      * Open the manage datalogs activity
      */
     private void openManageDatalogs()
     {
         Intent lauchManageDatalogs = new Intent(this, ManageDatalogsActivity.class);
         startActivity(lauchManageDatalogs);        
     }
     
     /**
      * Open the preferences activity
      */
     private void openPreferences()
     {
         Intent launchPrefs = new Intent(this, PreferencesActivity.class);
         startActivityForResult(launchPrefs, SHOW_PREFS);
     }
 
     /**
      * 
      * @param requestCode
      * @param resultCode
      * @param data
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK)
         {
             if (requestCode == MSLoggerApplication.PROBE_ECU)
             {
                 completeCreate();
             }
             if (requestCode == SHOW_PREFS)
             {
                 Boolean dirty = (Boolean) data.getExtras().get(PreferencesActivity.DIRTY);
                 if (dirty)
                 {
                     Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
                     if (ecuDefinition != null)
                     {
                         ecuDefinition.refreshFlags();
                         GaugeRegister.INSTANCE.flush();
                         ecuDefinition.initGauges();
                         initGauges();
                     }
                 }
             }
         }
     }
 
     /**
      * Called when a preference change
      * 
      * @param prefs
      * @param key
      */
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
     {
         if (ready == null || !ready)
         {
             return;
         }
         if (key.startsWith("gauge"))
         {
             initGauges();
         }
         Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
 
         if (ApplicationSettings.INSTANCE.btDeviceSelected() && ecuDefinition != null)
         {
         	ecuDefinition.refreshFlags();
         }
     }
 
     /**
      * 
      * @param v
      */
     @Override
     public void onClick(View v)
     {
     }
 
      /**
      * Background task user to reset gauge to firmware default
      */
     private class ResetGaugesTask extends AsyncTask<Void, Void, Void>
     {
         private ProgressDialog dialog;
 
         /**
          * 
          */
         @Override
         protected void onPreExecute()
         {
             dialog = new ProgressDialog(MSLoggerActivity.this);
             dialog.setMessage(getString(R.string.reset_gauges));
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
         }
 
         /**
          * 
          * @param arg0
          */
         @Override
         protected Void doInBackground(Void... arg0)
         {
             GaugeRegister.INSTANCE.resetAll();
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MSLoggerActivity.this);
             Editor editor = prefs.edit();
             editor.remove("gauge1");
             editor.remove("gauge2");
             editor.remove("gauge3");
             editor.remove("gauge4");
             editor.remove("gauge5");
             editor.commit();
             return null;
         }
 
         /**
          * 
          * @param unused
          */
         @Override
         protected void onPostExecute(Void unused)
         {
             dialog.dismiss();
             
             Megasquirt ecuDefinition = ApplicationSettings.INSTANCE.getEcuDefinition();
             if (ecuDefinition != null)
             {
                 initGauges();
             }
         }
     }
 
     /**
      * Background task that load the pages
      */
     private class InitTask extends AsyncTask<Void, Void, Void>
     {
 
         /**
          * 
          * @param result
          */
         @Override
         protected void onPostExecute(Void result)
         {
             super.onPostExecute(result);
             finaliseInit();
             ready = true;
         }
 
         /**
          * 
          * @param params
          */
         @Override
         protected Void doInBackground(Void... params)
         {
             if (ready != null)
             {
                 return null;
             }
             ready = false;
 
             loadGauges();
 
             return null;
         }
     }
 
  
     /**
      * Listener that monitor touch action on the gauge.
      */
     public class GaugeTouchListener implements OnTouchListener
     {
 
         private Indicator gauge;
 
         /**
          * 
          * @param gauge
          */
         public GaugeTouchListener(Indicator gauge)
         {
             this.gauge = gauge;
         }
 
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
             if (event.getAction() == MotionEvent.ACTION_DOWN)
             {
                 String middleIndicatorType = indicators[2].getType();
                 
                 // indicator[2] is the middle gauge
                 String g3name = indicators[2].getName();
                 String gname = gauge.getName();
 
                 int indexIndicator = 0;
                 
                 // Find touched gauge index     
                 for (int i = 0; i < indicators.length; i++)
                 {
                    if (indicators[i] != null && v.getId() == indicators[i].getId())
                     {
                         indexIndicator = i;
                     }
                 };
                 
                 // Indicator are the same type
                 if (gauge.getType().equals(middleIndicatorType))
                 {
                     indicators[2].initFromName(gauge.getName());
 
                     indicators[indexIndicator] = gauge;
                     indicators[indexIndicator].initFromName(g3name);
                 }
                 // Indicator were not the same type, we need to rebuild them with the right class
                 else
                 {                
                     // Swap touched indicator with indicator 3 (middle one)
                     View indicator3View = findViewById(R.id.g3);
                     
                     // Remove old indicator 3
                     ViewGroup parentIndicator3View = (ViewGroup) indicator3View.getParent();
                     int index = parentIndicator3View.indexOfChild(indicator3View);
                     parentIndicator3View.removeView(indicator3View);
 
                     // Remove old touched indicator
                     ViewGroup parentTouchedIndicatorView = (ViewGroup) v.getParent();
                     index = parentTouchedIndicatorView.indexOfChild(v);
                     parentTouchedIndicatorView.removeView(v);
                     
                     // Add touched indicator in place of indicator 3
                     parentIndicator3View.addView(v);           
                     
                     // Add indicator 3 in place of touched indicator
                     parentTouchedIndicatorView.addView(indicator3View, index);        
                     
                     // Swap objects
                     Indicator tmpIndicator = indicators[indexIndicator];
                     indicators[indexIndicator] = indicators[2];
                     indicators[2] = tmpIndicator;
                     
                     // Init the indicator with their new gauge details
                     indicators[2].initFromName(gname);
                     indicators[indexIndicator].initFromName(g3name);
                 }                
                 
                 // Put their ID back in place
                 indicators[indexIndicator].setId(v.getId());
                 indicators[2].setId(R.id.g3);
                 
                 // Rebind touch event to new indicator 3
                 initIndicator3Events();
                 
                 // Rebind regular indicator event
                 indicators[indexIndicator].setOnTouchListener(new GaugeTouchListener(indicators[indexIndicator]));
 
                 return true;
             }
             
             return false;
         }
 
     }
 
     /**
      * Receiver that get events from other activities about Megasquirt status and activities
      */
     private final class Reciever extends BroadcastReceiver
     {
         /**
          * When an event is received
          * 
          * @param context
          * @param intent
          */
         @Override
         public void onReceive(Context context, Intent intent)
         {
             String action = intent.getAction();
             Log.i(ApplicationSettings.TAG, "Received :" + action);
             boolean autoLoggingEnabled = ApplicationSettings.INSTANCE.getAutoLogging();
 
             if (action.equals(Megasquirt.CONNECTED))
             {
                 Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
                 DebugLogManager.INSTANCE.log(action,Log.INFO);
                 indicatorManager.setDisabled(false);
                 if (autoLoggingEnabled && ecu != null)
                 {
                     ecu.startLogging();
                 }
             }
             else if (action.equals(Megasquirt.DISCONNECTED))
             {
                 DebugLogManager.INSTANCE.log(action,Log.INFO);
                 indicatorManager.setDisabled(true);
                 if (autoLoggingEnabled)
                 {
                     DatalogManager.INSTANCE.mark("Connection lost");
                 }
                 
                 messages.setText(R.string.disconnected_from_ms);    
                 rps.setText("");
                 
                 Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
                 if (ecu != null && ecu.isRunning())
                 {
                     ecu.stop();
                 }
             } 
             else if (action.equals(Megasquirt.NEW_DATA))
             {
                 processData();
             }
             else if (action.equals(ApplicationSettings.GENERAL_MESSAGE))
             {
                 String msg = intent.getStringExtra(ApplicationSettings.MESSAGE);
 
                 messages.setText(msg);
                 DebugLogManager.INSTANCE.log("Message : " + msg,Log.INFO);
             }
             else if (action.equals(ApplicationSettings.RPS_MESSAGE))
             {
                 String RPS = intent.getStringExtra(ApplicationSettings.RPS);
                 
                 rps.setText(RPS + " reads / second");
             }  
         }
     }
 
     /**
      * Listener used when the user touch the screen to mark the datalog
      */
     private static class MarkListener implements OnTouchListener
     {
         private LinearLayout layout;
 
         /**
          * Constructor
          * 
          * @param layout    The layout that will change background then the screen is touch
          */
         public MarkListener(LinearLayout layout)
         {
             this.layout = layout;
         }
 
         /**
          * On touch event
          * 
          * @param v         The view that triggered the event
          * @param event     Information about the event
          */
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
             Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
 
             if (ecu != null && ecu.isLogging() && event.getAction() == MotionEvent.ACTION_DOWN)
             {
                 layout.setBackgroundColor(Color.BLUE);
                 layout.invalidate();
                 return true;
             }
             if (event.getAction() == MotionEvent.ACTION_UP)
             {
                 layout.setBackgroundColor(Color.BLACK);
                 layout.invalidate();
                 DatalogManager.INSTANCE.mark("Manual");
                 return true;
             }
             return false;
         }
 
     }
 
 }
