 /* ----------------------------------------------------------------------------
  * Project Voodoo OC/UV Stability App
  *
  * CpuSpy.java
  *
  * Class for getting the time_in_state data from sysfs and eventually other
  * CPU-related info
  *
  * Originally adapated from  CPU Spy android app
  * <https://github.com/storm717/cpuspy>
  *
  * --------------------------------------------------------------------------*/
 
 package org.projectvoodoo.ocuvstabilitytest.metrics;
 
 // imports
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.util.List;
 import java.util.ArrayList;
 import android.util.Log;
 import android.os.SystemClock;
 
 /** Class used for getting live information about the CPU. Implements the
  * singleton module (use CpuSpy.getInstance() to maintain any state necesary
  */
 public class CpuSpy {
 
     //-------------------------------------------------------------------------
     // member data
     //-------------------------------------------------------------------------
 
     /** singleton */
     private static CpuSpy mSingleton = new CpuSpy();
 
     // various FS points we query
     public static final String TIME_IN_STATE_PATH =
         "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
     public static final String VERSION_PATH = "/proc/version";
 
     /** array of the states / duration */
     private List<CpuState> mStates = new ArrayList<CpuState>();
 
     //-------------------------------------------------------------------------
     // data types
     //-------------------------------------------------------------------------
 
     /** simple struct for states/time */
     public class CpuState {
         public CpuState(int a, int b) { freq = a; duration =b; }
         public int freq = 0;
         public int duration = 0;
     }
 
     //-------------------------------------------------------------------------
     // Access functions
     //-------------------------------------------------------------------------
 
     /** get singleton instance */
    public CpuSpy getInstance() {
         return mSingleton;
     }
 
     /** get time in states */
     public List<CpuState> getTimeInStates () {
         return mStates;
     }
 
     //-------------------------------------------------------------------------
     // Member functions
     //-------------------------------------------------------------------------
 
     /** update the class's state with the time_in_state info and return
      * the values*/
     public List<CpuState> updateTimeInStates () {
         try {
             // create a buffered reader to read in the time-in-states log
             InputStream is = new FileInputStream (TIME_IN_STATE_PATH);
             InputStreamReader ir = new InputStreamReader (is);
             BufferedReader br = new BufferedReader (ir);
 
             // clear out the array and read in the new state lines
             mStates.clear ();
             String line;
             while ( (line = br.readLine ()) != null ) {
                 // split open line and convert to Integers
                 String[] nums = line.split (" ");
                 mStates.add ( new CpuState  (
                 Integer.parseInt (nums[0]), Integer.parseInt (nums[1]) ) );
             }
 
             is.close ();
 
         } catch (Exception e) {
             Log.e ("cpuspy", e.getMessage() );
             return null;
         }
 
         // add in sleep state
         int sleepTime = (int)(SystemClock.elapsedRealtime() -
             SystemClock.uptimeMillis ()) / 10;
         mStates.add ( new CpuState (0, sleepTime));
 
         // made it
         return mStates;
     }
 }
