 package com.cleverua.bb.gps;
 
 import java.util.Vector;
 
 import javax.microedition.location.Criteria;
 import javax.microedition.location.Location;
 import javax.microedition.location.LocationException;
 import javax.microedition.location.LocationListener;
 import javax.microedition.location.LocationProvider;
 
 /**
  * Wrapper over the Location Provider
  */
 public class GPSLocator {
     private static final String PROVIDER_AVAILABLE_MSG               = "Location provider is available.";
     private static final String LOCATION_EXCEPTOIN_MSG               = "All location providers are currently permanently unavailable!";
     private static final String PROVIDER_TEMPORARILY_UNAVAILABLE_MSG = "Location provider is temporarily unavailable!";
     private static final String PROVIDER_OUT_OF_SERVICE_MSG          = "The location provider is permanently unavailable!";
     private static final String PROVIDER_NULL_MSG                    = "Can not find the location provider that meets the defined criteria!";
     private static final String PROVIDER_UNDEFINED_STATE_MSG         = "Provider state is undefined";
     
     private Vector stateListeners;
     private Vector locationListeners;
     private LocationProvider provider;
     private int state;
 
     /**
      * Returns the locator's message for the location provider's state.
      */
     public static String getStateMessage(int state) {
         switch (state) {
             case LocationProvider.OUT_OF_SERVICE: 
                 return PROVIDER_OUT_OF_SERVICE_MSG;
             case LocationProvider.TEMPORARILY_UNAVAILABLE: 
                 return PROVIDER_TEMPORARILY_UNAVAILABLE_MSG;
             case LocationProvider.AVAILABLE: 
                 return PROVIDER_AVAILABLE_MSG;
             default: 
                 return PROVIDER_UNDEFINED_STATE_MSG;
         }
     }
     
     public GPSLocator() {
         stateListeners = new Vector();
         locationListeners = new Vector();
     }
     
     /**
      * Initialize the GPS locator.
      * @param criteria - GPS criteria for initialization.
      * @param interval - the interval in seconds. 
      * -1 is used for the default interval of this provider. 
      * 0 is used to indicate that the application wants to receive 
      * only provider status updates and not location updates at all.
      * @param timeout - timeout value in seconds, must be greater than 0. 
      * If the value is -1, the default timeout for this provider is used.
      * @param maxage - maximum age of the returned location in seconds, 
      * must be greater than 0 or equal to -1 to indicate that the default 
      * maximum age for this provider is used.
      * @throws GPSException if GPS provider is unavailable 
      * or there are no providers can meet the given criteria. 
      */
     public void init(Criteria criteria, int interval, int timeout, int maxage) throws GPSException {
         try {
             provider = LocationProvider.getInstance(criteria);
         } catch (LocationException e) {
             StringBuffer message = new StringBuffer();
             message.append(LOCATION_EXCEPTOIN_MSG).append(' ').append(e);
             throw new GPSException(message.toString());
         }
         
         if (provider == null) {
             throw new GPSException(PROVIDER_NULL_MSG);
         }
         
         final int providerState = provider.getState();
         if (providerState == LocationProvider.AVAILABLE) {
             provider.setLocationListener(locationListener, interval, timeout, maxage);
         } else {
             throw new GPSException(getStateMessage(providerState));
         }
     }
     
     /**
      * Resets the GPS locator
      */
     public void reset() {
         if (provider != null) {
             provider.reset();
             provider.setLocationListener(null, -1, -1, -1);
         }
     }
     
     /**
      * Returns the current state of the locator
      */
     public int getState() {
         if (provider != null) {
             return provider.getState();
         } else {
             return -1;
         }
     }
     
     /**
      * Retrieves a Location with the constraints given by the Criteria associated with this class.
      * Do not call this method from the event thread.
      * @param timeOut - a timeout value in seconds. 
      * -1 is used to indicate that the implementation should use its default timeout value for this provider.
      * @return Location object or null if no result could be retrieved.
      */
     public Location getLocation(int timeOut) {
         if (provider != null) {
             try {
                 return provider.getLocation(timeOut);
             } catch (Exception e) {
                 // suppose location to be null
             }
         }
         return null;
     }
     
     /**
      * Adds listener for location provider's state. 
      */
     public void addStateListener(GPSStateListener listener) {
         stateListeners.addElement(listener);
     }
     
     /**
      * Removes listener for location provider's state.
      */
     public void removeStateListener(GPSStateListener listener) {
         stateListeners.removeElement(listener);
     }
     
     /**
      * Adds listener for location updates. 
      */
     public void addLocationListener(GPSLocationListener listener) {
         locationListeners.addElement(listener);
     }
     
     /**
      * Removes listener for location updates.
      */
     public void removeLocationListener(GPSLocationListener listener) {
         locationListeners.removeElement(listener);
     }
     
     private void setState(int state) {
         this.state = state;
     }
     
     private LocationListener locationListener = new LocationListener() {
         
         public void providerStateChanged(LocationProvider provider, int state) {
             setState(state);
             updateStateListeners(state);
         }
 
         public void locationUpdated(LocationProvider provider, Location location) {
             final int newState = provider.getState();
             if (newState != state) {
                 providerStateChanged(provider, newState);
             }
             
             if ((newState == LocationProvider.AVAILABLE) && (location.isValid())) {
                 updateLocationListeners(location);
             } else {
                 updateLocationListeners(null);
             }
         }
         
         private void updateStateListeners(int state) {
             final int size = stateListeners.size();
             for (int i = 0;  i < size; i++) {
                 ((GPSStateListener) stateListeners.elementAt(i)).gpsStateChanged(state);
             }
         }
         
         private void updateLocationListeners(Location location) {
             final int size = locationListeners.size();
             for (int i = 0;  i < size; i++) {
                 ((GPSLocationListener) locationListeners.elementAt(i)).gpsLocationUpdated(location);
             }
         }
         
     };
     
 }
