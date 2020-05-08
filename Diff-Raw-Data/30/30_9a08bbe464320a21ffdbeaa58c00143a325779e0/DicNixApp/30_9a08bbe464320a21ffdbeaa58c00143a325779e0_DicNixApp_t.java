 /*
  *      This program is free software; you can redistribute it and/or modify
  *      it under the terms of the GNU General Public License as published by
  *      the Free Software Foundation; either version 2 of the License, or
  *      (at your option) any later version.
  *      
  *      This program is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU General Public License for more details.
  *      
  *      You should have received a copy of the GNU General Public License
  *      along with this program; if not, write to the Free Software
  *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *      MA 02110-1301, USA.
  */
 package se.ltu.dicnix;
 
 import java.util.List;
 
 import ssc.SenseSmartCity;
 import ssc.Sensor;
 import ssc.SnowPressure;
 import android.app.Application;
 import android.util.Log;
 
 /**
  * @author Jim Gunnarsson, di98jgu
  *
  */
 public class DicNixApp extends Application {
    
    private static String TAG = DicNixApp.class.getSimpleName();
    
    public SenseSmartCity ssc = null;
 
    /**
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
       super.onCreate();
       
       getSSC();
       Log.i(TAG, "Start of application");
       
    }
    
    public SenseSmartCity getSSC() {
       
       if (this.ssc == null) {
          
          // Need preferences to load username and password
          this.ssc = new SenseSmartCity("", "");
       }
       
       return this.ssc;
       
    }
    
    public List<Sensor> getSensors() {
       
       List<Sensor> sensors = ssc.getSensors();
       
       if (sensors == null || sensors.isEmpty()) {
          
          Log.d(TAG, "Faild to fetched sensors!");
       }
       
       Log.d(TAG, "Fetched a list of sensors");
       
       return sensors;
       
    }
    
    public List<SnowPressure> getReadings(Sensor sensor) {
       
       List<SnowPressure> readings = ssc.getSnowPressure(sensor);
       
       if (readings == null || readings.isEmpty()) {
          
          Log.d(TAG, "No readings for this sensor: " + sensor.getSerial());
       }
       
       Log.d(TAG, "Fetched readings for this sensor: " + sensor.getSerial());
       
       return readings;
       
    }
    
 
 }
