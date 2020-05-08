 package ru.ezhoff.geolocation.geoexplorer;
 
 import android.app.Activity;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class ExplorerActivity extends Activity {
 
     public static final int PERIOD = 500;
     private Button startStopButton;
     private Button markerButton;
     private TextView outputView;
     private FileLogger logger;
     private List<Monitor> monitors;
 
     private boolean active = false;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         startStopButton = (Button) findViewById(R.id.startStopButton);
         markerButton    = (Button) findViewById(R.id.markerButton);
         outputView      = (TextView) findViewById(R.id.outputText);
        //FileLogger.setOutputView(outputView);
         logger = FileLogger.getInstance();
         startStopButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 changeMonitoringState();
             }
         });
         markerButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 logger.warn("MARKER");
             }
         });
        initMonitors();
         logger.info("Loaded.");
     }
 
     private void changeMonitoringState() {
         active = !active;
         if (active) {
             activateMonitoring();
         } else {
             deactivateMonitoring();
         }
         startStopButton.setText(active ? R.string.stop : R.string.start);
     }
 
     private void activateMonitoring() {
         logger = FileLogger.getInstance();
         logger.info("Monitoring is activated.");
         for (Monitor monitor: monitors) {
             monitor.start();
         }
     }
 
     private void deactivateMonitoring() {
         for (Monitor monitor: monitors) {
             monitor.stop();
         }
         logger.info("Monitoring is deactivated.");
         logger.close();
     }
 
     private void initMonitors() {
         monitors = new LinkedList<Monitor>();
         LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
         for (String provider: locationManager.getProviders(true)) {
             monitors.add(
                     new LocationMonitor().setLocationManager(locationManager).setProvider(provider).setPeriod(PERIOD)
             );
         }
         monitors.add(new CellMonitor().setTelephonyManager(telephonyManager).setPeriod(PERIOD));
     }
 }
