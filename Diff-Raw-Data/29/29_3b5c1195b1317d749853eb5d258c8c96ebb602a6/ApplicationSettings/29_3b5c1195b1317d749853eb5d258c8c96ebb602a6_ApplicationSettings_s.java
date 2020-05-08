 package uk.org.smithfamily.mslogger;
 
 import java.io.File;
import java.util.*;
import java.util.Map.Entry;
 
import uk.org.smithfamily.mslogger.ecuDef.*;
 import uk.org.smithfamily.mslogger.ecuDef.gen.ECURegistry;
import uk.org.smithfamily.mslogger.ecuDef.gen.Msns_extra29y;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.bluetooth.BluetoothAdapter;
import android.content.*;
 import android.content.SharedPreferences.Editor;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * This class take care of the settings of the application. All these preferences can be found in the app
  * in the "Preferences" section (bottom left menu icon)
  */
 public enum ApplicationSettings implements SharedPreferences.OnSharedPreferenceChangeListener
 {
     INSTANCE;
 
     public static final String      MISSING_VALUE       = "f741d5b0-fbee-11e0-be50-0800200c9a66";
     public static final String      GENERAL_MESSAGE     = "uk.org.smithfamily.mslogger.GENERAL_MESSAGE";
     public static final String      MESSAGE             = "uk.org.smithfamily.mslogger.MESSAGE";
     public static final String      ECU_CHANGED         = "uk.org.smithfamily.mslogger.ECU_CHANGED";
     public static final String      TAG                 = "uk.org.smithfamily.mslogger";
     private Context                 context;
     private File                    dataDir;
     private int                     hertz;
     private SharedPreferences       prefs;
     private Megasquirt              ecuDefinition;
     private String                  bluetoothMac;
     private Boolean                 autoConnectOverride = null;
 
     private Map<String, Boolean>    settings            = new HashMap<String, Boolean>();
     private BluetoothAdapter        defaultAdapter;
     private boolean                 writable;
     
     /**
      * Initialise all preferences
      * Also load all ECU definitions currently supported
      * 
      * @param context
      */
     public void initialise(Context context)
     {
         this.context = context;
         PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
         prefs = PreferenceManager.getDefaultSharedPreferences(context);
         prefs.registerOnSharedPreferenceChangeListener(this);
         
         dataDir = new File(Environment.getExternalStorageDirectory(), prefs.getString("DataDir", context.getString(R.string.app_name)));
         dataDir.mkdirs();
         this.hertz = prefs.getInt(context.getString(R.string.hertz), 20);
     }
 
     
     /**
      * Get the data directory for the application
      */
     public File getDataDir()
     {
         return dataDir;
     }
 
     /**
      * Get the context where the application settings are used
      */
     public Context getContext()
     {
         return context;
     }
 
     /**
      * Get the hertz of the Android device
      */
     public int getHertz()
     {
         return hertz;
     }
 
     /**
      * Get the current ECU definition
      */
     public synchronized Megasquirt getEcuDefinition()
     {
         return ecuDefinition;
     }
 
     /**
      * Get the MAC address of the Bluetooth device
      */
     public synchronized String getBluetoothMac()
     {
         bluetoothMac = prefs.getString("bluetooth_mac", MISSING_VALUE);
         return bluetoothMac;
     }
 
     /**
      * @param Set the MAC address of the current Bluetooth device
      */
     public synchronized void setBluetoothMac(String m)
     {
         Editor editor = prefs.edit();
         editor.putString("bluetooth_mac", m);
         editor.commit();
     }
 
     /**
      * This method mimics the C style preprocessor of INI files
      *  
      *  @param name Name of the settings to look for
      */
     public boolean isSet(String name)
     {
         Boolean result = settings.get(name);
         if (result != null)
             return result;
         boolean val = false;
 
         String tempType = prefs.getString("temptype", MISSING_VALUE);
         if (!val && tempType.equals(name))
             val = true;
 
         if (!val && prefs.getString("mstype", MISSING_VALUE).equals(name))
             val = true;
 
         if (!val && prefs.getString("maptype", MISSING_VALUE).equals(name))
             val = true;
 
         if (!val && prefs.getString("egotype", MISSING_VALUE).equals(name))
             val = true;
 
         settings.put(name, val);
         return val;
     }
     
     /**
      * Triggered whenever the prefenrece change to reset MAC address and settings
      * 
      * @param sharedPreferences
      * @param key
      */
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
     {
         bluetoothMac = null;
         settings = new HashMap<String, Boolean>();
     }
 
     /**
      * If bluetooth device is not the default one, a bluetooth device has been selected
      */
     public boolean btDeviceSelected()
     {
         return !MISSING_VALUE.equals(getBluetoothMac());
     }
 
     /**
      * Return true if preference that automatically send email with datalog is enabled, false otherwise
      */
     public boolean emailEnabled()
     {
         return prefs.getBoolean("autoemail_enabled", false);
     }
 
     /**
      * @return The current email to send datalog to 
      */
     public String getEmailDestination()
     {
         return prefs.getString("autoemail_target", "");
     }
 
     
     /**
      * Return true if the preferences is enabled to automatically connect to the Megasquirt on startup
      */
     public boolean autoConnectable()
     {
         boolean autoconnectpref = prefs.getBoolean("autoconnect", true);
         boolean btDeviceSelected = btDeviceSelected();
         return (autoConnectOverride == null || autoConnectOverride == true) && btDeviceSelected && autoconnectpref;
     }
 
     /**
      * Get a specific application preference by name
      * 
      * @param name The name of the preference
      */
     public String getPref(String name)
     {
     	String value = prefs.getString(name, MISSING_VALUE);
         if (value.equals(MISSING_VALUE))
         {
         	value = null;
         }
         return value;
     }
     
     /**
      * Helper function to set or get a preference
      * 
      * @param name  The name of the preference
      * @param def   The value of the preference
      * @return      Return the current value in preference
      */
     public String getOrSetPref(String name, String def)
     {
         String value = prefs.getString(name, MISSING_VALUE);
         if (value.equals(MISSING_VALUE))
         {
             setPref(name,def);
             value = def;
         }
         return value;
     }
     
     /**
      * Helper function to save a string in preference
      * 
      * @param name
      * @param value
      */
     public void setPref(String name,String value)
     {
         Editor editor = prefs.edit();
         editor.putString(name, value);
         editor.commit();
     }
 
     /**
      * 
      * @param gaugeName
      * @param title
      * @param var
      * @param def
      * @return
      */
     public double getGaugeSetting(String gaugeName, String title, String var, double def)
     {
         return def;
     }
 
     /**
      * 
      * @return
      */
     public boolean isAutoConnectOverride()
     {
         return autoConnectOverride;
     }
 
     /**
      * 
      * @param autoConnectOverride
      */
     public void setAutoConnectOverride(Boolean autoConnectOverride)
     {
         this.autoConnectOverride = autoConnectOverride;
     }
 
     /**
      * 
      * @return
      */
     public boolean getAutoLogging()
     {
         return prefs.getBoolean("autolog", true);
     }
     
     /**
      * 
      * @return
      */
     public BluetoothAdapter getDefaultAdapter()
     {
         return defaultAdapter;
     }
 
     /**
      * 
      * @param defaultAdapter
      */
     public void setDefaultAdapter(BluetoothAdapter defaultAdapter)
     {
         this.defaultAdapter = defaultAdapter;
     }
 
     /**
      * 
      * @param cardOK
      */
     public void setWritable(boolean cardOK)
     {
         this.writable = cardOK;
     }
 
     /**
      * 
      *
      */
     public boolean isWritable()
     {
         return this.writable;
     }
 
     /**
      * Return the ECU file for the specified Megasquirt signature
      * 
      * @param sig
      * @return
      */
     public Megasquirt getEcuForSig(String sig)
     {
         return ECURegistry.INSTANCE.getECU(sig, context);
     }
 
     /**
      * 
      * @param ecu
      */
     public void setEcu(Megasquirt ecu)
     {
         ecuDefinition = ecu;
 
         Intent broadcast = new Intent();
         broadcast.setAction(ApplicationSettings.ECU_CHANGED);
         context.sendBroadcast(broadcast);
     }
 
     /**
      * 
      */
     public void refreshFlags()
     {
         settings.clear();
     }
     
     /**
      * Return true if Bluetooth work around is activated, false otherwise
      * 
      * @return
      */
     public boolean isBTWorkaround()
     {
     	return prefs.getBoolean("workaround", false);
     }
     
     /**
      * Set the state for the Bluetooth work around
      * 
      * @param state
      */
     public void setBTWorkaround(boolean state)
     {
         Editor editor = prefs.edit();
         editor.putBoolean("workaround", state);
         editor.commit();
         DebugLogManager.INSTANCE.log("set BTWorkaround to "+state,Log.ASSERT);
     }
 
     /**
      * Return the logging level of the application
      * Used for application debugging
      * 
      * @return
      */
     public int getLoggingLevel()
     {
         String value = prefs.getString("loglevel", null);
         return value == null ? 6 : Integer.valueOf(value);
     }
     
     /**
      * @return Datalog fields used in the datalog viewer
      */
     public String[] getDatalogFields()
     {
         String datalogFields = prefs.getString("datalogfields","Time\tRPM\tMAP\tAFR\tMAT\tCLT\tBatt\tIACstep");
         
         return datalogFields.split("\t");
     }
     
     /**
      * @param datalogFields Datalog fields to be view in the datalog viewer
      */
     public void setDatalogFields(String[] datalogFields)
     {
         String output = "";
         
         if (datalogFields.length > 0) {
             StringBuilder sb = new StringBuilder();
             sb.append(datalogFields[0]);
             
             for (int i = 1; i < datalogFields.length; i++) {
                 sb.append("\t");
                 sb.append(datalogFields[i]);
             }
             
             output = sb.toString();
         }
             
         setPref("datalogfields",output);
     }
 }
