 package com.droidorb;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.droidorb.observer.OnMissedCallListener;
 import com.droidorb.server.Client;
 import com.droidorb.server.Server;
 import com.droidorb.server.ServerListener;
 
 /**
  * Main activity for prototype. This will be replaced by Main once the project
  * is under way
  * 
  * @author toby
  */
 public class DroidOrbActivity extends Activity {
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
    }
 
    @Override
    protected void onStart() {
       super.onStart();
    }
 
    @Override
    protected void onStop() {
       super.onStop();
    }
 
    /**
     * Add a listener so that the LED's update as the seek bar is dragged
     */
    private void setupSeekbarListeners() {
       OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
          @Override
          public void onStopTrackingTouch(SeekBar arg0) {
          }
 
          @Override
          public void onStartTrackingTouch(SeekBar arg0) {
          }
 
          @Override
          public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
             onColoursClick(null);
          }
       };
 
       ((SeekBar) findViewById(R.id.red)).setOnSeekBarChangeListener(listener);
       ((SeekBar) findViewById(R.id.green)).setOnSeekBarChangeListener(listener);
       ((SeekBar) findViewById(R.id.blue)).setOnSeekBarChangeListener(listener);
       
       OnSeekBarChangeListener listener2 = new OnSeekBarChangeListener() {
          @Override
          public void onStopTrackingTouch(SeekBar arg0) {
          }
 
          @Override
          public void onStartTrackingTouch(SeekBar arg0) {
          }
 
          @Override
          public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
             try {
                byte[] data = new byte[4];
                data[0] = (byte) ((ProgressBar) findViewById(R.id.red_pulse)).getProgress();
                data[1] = (byte) ((ProgressBar) findViewById(R.id.green_pulse)).getProgress();
                data[2] = (byte) ((ProgressBar) findViewById(R.id.blue_pulse)).getProgress();
                data[3] = (byte) ((ProgressBar) findViewById(R.id.white_pulse)).getProgress();
 
               Main.mBoundService.sendCommand((byte) 0, (byte) 0, data);
             } catch (IOException e) {
                Toast.makeText(DroidOrbActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(Main.LOG_TAG, "Error sending command to accessory", e);
             }            
          }
       };
 
       ((SeekBar) findViewById(R.id.red_pulse)).setOnSeekBarChangeListener(listener2);
       ((SeekBar) findViewById(R.id.green_pulse)).setOnSeekBarChangeListener(listener2);
       ((SeekBar) findViewById(R.id.blue_pulse)).setOnSeekBarChangeListener(listener2);
       ((SeekBar) findViewById(R.id.white_pulse)).setOnSeekBarChangeListener(listener2);
    }
 
    public void onCommandClick(View v) {
       try {
          Main.mBoundService.sendCommand((byte) 0, (byte) 254, null);
       } catch (IOException e) {
          Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
          Log.e(Main.LOG_TAG, "Error sending command to accessory", e);
       }
    }
 
    public void onColoursClick(View v) {
       try {
          byte[] data = new byte[3];
          data[0] = (byte) ((ProgressBar) findViewById(R.id.red)).getProgress();
          data[1] = (byte) ((ProgressBar) findViewById(R.id.green)).getProgress();
          data[2] = (byte) ((ProgressBar) findViewById(R.id.blue)).getProgress();
 
          Main.mBoundService.sendCommand((byte) 0, (byte) 0, data);
       } catch (IOException e) {
          Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
          Log.e(Main.LOG_TAG, "Error sending command to accessory", e);
       }
    }
 
    public void onMissedCall(View v) {
       try {
         // light up blue when there is a missed call
          byte[] data = new byte[3];
         data[0] = (byte) 0;
          data[1] = (byte) 0;
         data[2] = (byte) 10;
 
          Main.mBoundService.sendCommand((byte) 0, (byte) 1, data);
       } catch (IOException e) {
          Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
          Log.e(Main.LOG_TAG, "Error sending command to accessory", e);
       }
    }
 
 }
