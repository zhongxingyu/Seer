 package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.preference.PreferenceManager;
 import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.R;
 import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorActivity;
 import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorManagerService;
 import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TimerTask;
 
 /**
  * @author Jonathan Nain
  * This class presents a minimalist service which use the SensApp android API to log the sensors.
  * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
  */
 public abstract class AbstractSensorLoggerTask extends TimerTask{
 
     static SensorManager sensorManager = null;
     static public List<AbstractSensor> sensors;
     protected AbstractSensor sensor = null;
     static Context context;
 
     AbstractSensorLoggerTask(AbstractSensor as, Context c){
         sensor = as;
         context = c;
         if(sensorManager == null){
             initSensorManager(c);
         }
     }
 
     @Override
     abstract public void run();
 
     protected void registerAndListenSensor(){
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         sensor.registerInSensApp(context, R.drawable.ic_launcher, sp.getString(context.getString(R.string.pref_description_key), "No description"));
     }
 
     static public void addSensor(AbstractSensor as){
         if(!sensors.contains(as))
             sensors.add(as);
     }
 
     static public void initSensorArray(){
         if(sensors == null)
             sensors = new ArrayList<AbstractSensor>();
     }
 
     static public List<AbstractSensor> getSensors(){
         return sensors;
     }
 
     public AbstractSensor getSensor(){
         return this.sensor;
     }
 
     static public AbstractSensor getSensorByName(String name){
         for(AbstractSensor s: sensors){
             if(s.getName().equals(name))
                 return s;
         }
         return null;
     }
 
     static public void initSensorManager(Context c){
         Intent startService = new Intent(c, SensorManagerService.class);
         PendingIntent pendingIntent = PendingIntent.getService(c, 0, startService, PendingIntent.FLAG_ONE_SHOT);
         ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
     }
 
     static public void setUpSensors(Context c, SensorManager manager, BluetoothAdapter bt){
         sensorManager = manager;
 
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
         String compositeName = sp.getString(c.getString(R.string.pref_compositename_key), SensorActivity.compositeName);
 
         if(sensors == null)
             initSensorArray();
 
         for(Sensor s: sensorManager.getSensorList(Sensor.TYPE_ALL)){
             AndroidSensor as = new AndroidSensor(s, compositeName);
             setUpSensor(as, sp, c);
         }
 
         //Add Battery sensor
         BatterySensor bs = new BatterySensor(compositeName);
         setUpSensor(bs, sp, c);
 
         //Add the Free Memory percentage
         FreeMemorySensor fms = new FreeMemorySensor(compositeName);
         setUpSensor(fms, sp, c);
 
         //Add the Bluetooth Light sensor
        BluetoothSensor bts = new BluetoothSensor(compositeName, "Kestrel", bt, "K4000");
         setUpSensor(bts, sp, c);
     }
 
     static private void setUpSensor(AbstractSensor as, SharedPreferences sp, Context c){
         as.setRefreshRate(sp.getInt(as.getName(), as.getDefaultRate()));
         as.setListened(sp.getBoolean(as.getFullName(), false));
         if(as instanceof AndroidSensor)
             ((AndroidSensor) as).setBenchmarkAvg(sp.getFloat(as.getName()+"_avg", Float.NaN));
         if(!sensors.contains(as))
             addSensor(as);
         if(as.isListened())
             SensorManagerService.setLog(c, as);
     }
 }
