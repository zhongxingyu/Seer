 package ru.ezhoff.geolocation.geoexplorer;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 /**
  * @author e.ezhov
  * @version 1.0 05.06.13
  */
 public class LocationMonitor implements Monitor, LocationListener, Runnable {
 
     /**
      * Main template for module messaging.
      */
     private static final String MONITORING_MESSAGE = "LocationMonitor{Provider - %s}::%s";
 
     /**
      * Minimum time interval between location updates, in milliseconds.
      */
     private static final long MIN_TIME = 500;
 
     /**
      * Minimum distance between location updates, in meters.
      */
     private static final float MIN_DISTANCE = 1;
 
     /**
      * The logger.
      */
     private FileLogger logger = FileLogger.getInstance();
 
     /**
      * The location manager for defining current location.
      */
     private LocationManager locationManager;
 
     /**
      * The current location provider.
      */
     private String provider;
 
     /**
      * The period of checking location in milliseconds.
      * if equals <code>0</code> the periodically checks is switched off.
      */
     private long period;
 
     /**
      * The flag of monitoring is on.
      */
     private volatile boolean isStarted = false;
 
     /**
      * The thread for checking by period.
      */
     private Thread thread;

    /**
     * The current location
     */
     private Location location;
 
 
     @Override
     public void onLocationChanged(Location location) {
         logger.info(formatModuleMessage(String.format(
                 "Listener::Location changed - Latitude: %s, Longitude: %s, Altitude: %s, Accuracy: %s",
                 location.getLatitude(),
                 location.getLongitude(),
                 location.getAltitude(),
                 location.getAccuracy()
         )));
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
         logger.info(formatModuleMessage(
                 String.format("Listener::Status is changed - s: %s, i: %s", s, i)
         ));
     }
 
     @Override
     public void onProviderEnabled(String s) {
         logger.info(formatModuleMessage(
                 String.format("Listener::Provider '%s' is enabled", s)
         ));
     }
 
     @Override
     public void onProviderDisabled(String s) {
         logger.info(formatModuleMessage(
                 String.format("Listener::Provider '%s' is disabled", s)
         ));
     }
 
     @Override
     public void start() {
         if (locationManager == null || provider == null) {
             throw new IllegalMonitorStateException("Monitor is not initialized");
         }
         locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
         logger.info(formatModuleMessage(String.format("Location listener for provider '%s' is added.", provider)));
         if (period > 0) {
             isStarted = true;
             if (thread == null) {
                 thread = new Thread(this);
             }
             thread.start();
            logger.info(formatModuleMessage(String.format("Location monitor for provider '%s' is started.", provider)));
         }
     }
 
     @Override
     public void stop() {
         isStarted = false;
         logger.info(formatModuleMessage(String.format(
                "Signal to stopping for location monitor of provider '%s' is sent.",
                 provider
         )));
         locationManager.removeUpdates(this);
         logger.info(formatModuleMessage(String.format("Location listener for provider '%s' is removed.", provider)));
     }
 
     @Override
     public void run() {
         while (isStarted) {
             if (Thread.interrupted()) {
                 return;
             }
             Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
             if (lastKnownLocation != null && !lastKnownLocation.equals(location)) {
                 location = lastKnownLocation;
                 logger.info(formatModuleMessage(String.format(
                        "Monitor::Location changed - Latitude: %s, Longitude: %s, Altitude: %s, Accuracy: %s",
                         location.getLatitude(),
                         location.getLongitude(),
                         location.getAltitude(),
                         location.getAccuracy()
                 )));
             }
             try {
                 Thread.sleep(period);
             } catch (InterruptedException e) {
                 logger.error(formatModuleMessage(e.getMessage()));
             }
         }
        logger.info(formatModuleMessage(String.format("Location monitor for provider '%s' is stopped.", provider)));
     }
 
     /**
      * Sets Location Manager and returns self object for easy init.
      * @param locationManager   Location Manager
      * @return                  self object
      */
     public LocationMonitor setLocationManager(LocationManager locationManager) {
         this.locationManager = locationManager;
         return this;
     }
 
     /**
      * Sets provider name and returns self object for easy init.
      * @param provider          provider name
      * @return                  self object
      */
     public LocationMonitor setProvider(String provider) {
         this.provider = provider;
         return this;
     }
 
     /**
      * Sets period of checks and returns self object for easy init.
      * @param period            period, in milliseconds
      * @return                  self object
      */
     public LocationMonitor setPeriod(int period) {
         this.period = period;
         return this;
     }
 
     private String formatModuleMessage(String message) {
         return String.format(MONITORING_MESSAGE, provider, message);
     }
 }
