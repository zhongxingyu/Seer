 package uk.org.smithfamily.mslogger;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.org.smithfamily.mslogger.ecuDef.*;
 import uk.org.smithfamily.mslogger.widgets.GaugeRegister;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
 public enum ApplicationSettings implements SharedPreferences.OnSharedPreferenceChangeListener
 {
     INSTANCE;
 
    public static final String MISSING_VALUE = "f741d5b0-fbee-11e0-be50-0800200c9a66";
     public static final String  GENERAL_MESSAGE = "uk.org.smithfamily.mslogger.GENERAL_MESSAGE";
     public static final String  MESSAGE         = "uk.org.smithfamily.mslogger.MESSAGE";
     public static final String  TAG             = "uk.org.smithfamily.mslogger";
     private Context             context;
     private File                dataDir;
     private int                 hertz;
     private SharedPreferences   prefs;
     private Megasquirt          ecuDefinition;
     private String              bluetoothMac;
 
     public void initialise(Context context)
     {
         this.context = context;
         prefs = PreferenceManager.getDefaultSharedPreferences(context);
         prefs.registerOnSharedPreferenceChangeListener(this);
         dataDir = new File(Environment.getExternalStorageDirectory(), prefs.getString("DataDir",
                 context.getString(R.string.app_name)));
         dataDir.mkdirs();
         this.hertz = prefs.getInt(context.getString(R.string.hertz), 20);
 
 
     }
 
     public File getDataDir()
     {
         return dataDir;
     }
 
     public Context getContext()
     {
         return context;
     }
 
     public int getHertz()
     {
         return hertz;
     }
 
     public synchronized Megasquirt getEcuDefinition()
     {
         if (ecuDefinition != null)
         {
             return ecuDefinition;
         }
         String ecuName = prefs.getString("mstype", "MS1Extra");
         if (ecuName.equals("MS1Extra"))
         {
             ecuDefinition = new MS1Extra29y(context);
         }
         else if(ecuName.equals("MS2Extra21"))
         {
             ecuDefinition = new MS2Extra210(context);
         }
         else
         {
         	ecuDefinition=new MS2Extra310(context);
         }
         GaugeRegister.INSTANCE.flush();
         ecuDefinition.initGauges();
         return ecuDefinition;
     }
     public synchronized String getBluetoothMac()
     {
         if (bluetoothMac != null)
         {
             return bluetoothMac;
         }
        bluetoothMac = prefs.getString("bluetooth_mac", MISSING_VALUE);
        if (MISSING_VALUE.equals(bluetoothMac))
         {
             Toast.makeText(context, "Please select your serial Bluetooth dongle", Toast.LENGTH_LONG).show();
         }
         return bluetoothMac;
     }
 
     private Map<String,Boolean> settings = new HashMap<String,Boolean>();
     // This method mimics the C style preprocessor of INI files
     public boolean isSet(String name)
     {
         Boolean result = settings.get(name);
         if(result != null)
             return result;
         boolean val = false;
         
         if(!val && prefs.getString("temptype", MISSING_VALUE).equals(name))
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
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
     {
         bluetoothMac = null;
         if (ecuDefinition != null)
         {
             ecuDefinition.stop();
             ecuDefinition = null;
         }
         settings = new HashMap<String,Boolean>();
     }
 
     public boolean btDeviceSelected()
     {
        return !MISSING_VALUE.equals(getBluetoothMac());
     }
 
     public boolean emailEnabled()
     {
     	return prefs.getBoolean("autoemail_enabled", false);
     }
     public String getEmailDestination()
     {
     	return prefs.getString("autoemail_target", "");
     }
 
     
     public String getOrSetPref(String name, String def)
     {
         String value=prefs.getString(name, MISSING_VALUE);
         if(value.equals(MISSING_VALUE))
         {
             Editor editor = prefs.edit();
             editor.putString(name, def);
             editor.commit();
             value = def;
         }
         return value;
     }
 
     public double getGaugeSetting(String gaugeName, String title, String var, double def)
     {
         return def;
     }
 
 }
