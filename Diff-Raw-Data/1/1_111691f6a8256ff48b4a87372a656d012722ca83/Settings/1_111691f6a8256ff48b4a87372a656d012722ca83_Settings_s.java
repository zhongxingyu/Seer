 package de.pribluda.android.accanalyzer;
 
 import android.app.Activity;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import de.pribluda.android.andject.InjectView;
 import de.pribluda.android.andject.ViewInjector;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * configuration of application settings
  *
  * @author Konstantin Pribluda
  */
 public class Settings extends Activity {
 
     @InjectView(id = R.id.sampleRate)
     private SeekBar sensorDelayBar;
     @InjectView(id = R.id.selectedSampleRateValue)
     private TextView sensorDelayLabel;
 
     @InjectView(id = R.id.windowSize)
     private SeekBar windowSizeBar;
     @InjectView(id = R.id.selectedWindowSizeValue)
     private TextView windowSizeLabel;
 
     @InjectView(id = R.id.updateRate)
     private SeekBar updateRateBar;
     @InjectView(id = R.id.selectedUpdateRateValue)
     private TextView updateRateLabel;
 
     private static final int[] windowSizesLookup = {16, 32, 64, 128, 256, 512, 1024};
     private final static Map<Integer, Integer> reverseWindowSizesLookup = new HashMap<Integer, Integer>() {{
         put(16, 0);
         put(32, 1);
         put(64, 2);
         put(128, 3);
         put(256, 4);
         put(512, 5);
         put(1024, 6);
     }};
 
     public static final int[] sensorDelayLookup = {SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_FASTEST};
     public static final Map<Integer, Integer> reverseSensorDelayLookup = new HashMap() {
         {
             put(SensorManager.SENSOR_DELAY_NORMAL, 0);
             put(SensorManager.SENSOR_DELAY_UI, 1);
             put(SensorManager.SENSOR_DELAY_GAME, 2);
             put(SensorManager.SENSOR_DELAY_FASTEST, 3);
         }
     };
     public static final int[] sampleRateTexts = {R.string.sampleRateNormal, R.string.sampleRateUI, R.string.sampleRateGame, R.string.sampleRateFastest};
 
     private Configuration configuration;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         configuration = Configuration.getInstance(this);
 
         setContentView(R.layout.settings);
 
         //  wire views
         ViewInjector.startActivity(this);
 
 
         sensorDelayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                 // update sample rate label
                 sensorDelayLabel.setText(sampleRateTexts[i]);
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
 
 
         updateRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                 updateRateLabel.setText("" +  (i + 1));
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
 
 
         windowSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                 windowSizeLabel.setText("" + windowSizesLookup[i]);
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
 
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
 
             }
         });
 
     }
 
     /**
      * set sliders from configuration
      */
     @Override
     protected void onResume() {
         super.onResume();
 
         //  set up sliders from  configuration
 
         // sensor delay
         sensorDelayBar.setProgress(reverseSensorDelayLookup.get(configuration.getSensorDelay()));
 
         // window size
         windowSizeBar.setProgress(reverseWindowSizesLookup.get(configuration.getWindowSize()));
 
         // update rate
         updateRateBar.setProgress(configuration.getUpdateRate() / 1000 - 1);
 
     }
 
     /**
      * set configuration values and save to preferences
      */
     @Override
     protected void onPause() {
         super.onPause();
 
         configuration.setSensorDelay(sensorDelayLookup[sensorDelayBar.getProgress()]);
         configuration.setWindowSize(windowSizesLookup[windowSizeBar.getProgress()]);
         configuration.setUpdateRate((updateRateBar.getProgress() + 1) * 1000);
 
         configuration.save(this);
 
     }
 
 }
