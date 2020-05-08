 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package android.location.cts;
 
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 import dalvik.annotation.ToBeFixed;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Criteria;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.location.GpsStatus.Listener;
 import android.os.Bundle;
 import android.os.Looper;
 import android.provider.Settings;
 import android.test.InstrumentationTestCase;
 import android.util.Log;
 
 import java.util.List;
 
 /**
  * There is only a fake GPS location provider on emulator.
  *
  * Requires the permissions
  * android.permission.ACCESS_MOCK_LOCATION to mock provider
  * android.permission.ACCESS_COARSE_LOCATION to access network provider
  * android.permission.ACCESS_FINE_LOCATION to access GPS provider
  * android.permission.ACCESS_LOCATION_EXTRA_COMMANDS to send extra commands to GPS provider
  */
 @TestTargetClass(LocationManager.class)
 public class LocationManagerTest extends InstrumentationTestCase {
     private static final int UPDATE_LOCATION_WAIT_TIME = 1000;
 
     private static final int PROXIMITY_WAIT_TIME = 2000;
 
     private static final String LOG_TAG = "LocationProximityTest";
 
     // use network provider as mock location provider, because:
     //  - proximity alert is hardcoded to listen to only network or gps
     //  - 'network' provider is not installed in emulator, so can mock it
     //    using test provider APIs
     private static final String PROVIDER_NAME =  LocationManager.NETWORK_PROVIDER;
 
     private LocationManager mManager;
 
     private Context mContext;
 
     private PendingIntent mPendingIntent;
 
     private TestIntentReceiver mIntentReceiver;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mContext = getInstrumentation().getTargetContext();
 
         mManager = (LocationManager) mContext.
                 getSystemService(Context.LOCATION_SERVICE);
 
         // test that mock locations are allowed so a more descriptive error message can be logged
         if (Settings.Secure.getInt(mContext.getContentResolver(),
                 Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0) {
             fail("Mock locations are currently disabled in Settings - this test requires "
                     + "mock locations");
         }
 
         checkProviderAlreadyExist(PROVIDER_NAME);
 
         mManager.addTestProvider(PROVIDER_NAME, true, //requiresNetwork,
                 false, // requiresSatellite,
                 true, // requiresCell,
                 false, // hasMonetaryCost,
                 false, // supportsAltitude,
                 false, // supportsSpeed, s
                 false, // upportsBearing,
                 Criteria.POWER_MEDIUM, // powerRequirement
                 Criteria.ACCURACY_FINE); // accuracy
         mManager.setTestProviderEnabled(PROVIDER_NAME, true);
     }
 
     @Override
     protected void tearDown() throws Exception {
         mManager.removeTestProvider(PROVIDER_NAME);
 
         if (mPendingIntent != null) {
             mManager.removeProximityAlert(mPendingIntent);
         }
         if (mIntentReceiver != null) {
             mContext.unregisterReceiver(mIntentReceiver);
         }
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getAllProviders",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getProviders",
             args = {boolean.class}
         )
     })
     public void testGetProviders() {
         List<String> providers = mManager.getAllProviders();
         assertEquals(2, providers.size());
         assertEquals(LocationManager.GPS_PROVIDER, providers.get(0));
         assertEquals(PROVIDER_NAME, providers.get(1));
 
         providers = mManager.getProviders(false);
         assertEquals(2, providers.size());
         assertEquals(LocationManager.GPS_PROVIDER, providers.get(0));
         assertEquals(PROVIDER_NAME, providers.get(1));
 
         providers = mManager.getProviders(true);
         assertEquals(1, providers.size());
         assertEquals(PROVIDER_NAME, providers.get(0));
 
         mManager.setTestProviderEnabled(PROVIDER_NAME, false);
         providers = mManager.getProviders(true);
         assertEquals(0, providers.size());
 
         providers = mManager.getProviders(false);
         assertEquals(2, providers.size());
         assertEquals(LocationManager.GPS_PROVIDER, providers.get(0));
         assertEquals(PROVIDER_NAME, providers.get(1));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getProvider",
         args = {String.class}
     )
     public void testGetProvider() {
         LocationProvider p = mManager.getProvider(PROVIDER_NAME);
         assertNotNull(p);
 
         p = mManager.getProvider(LocationManager.GPS_PROVIDER);
         assertNotNull(p);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getProviders",
             args = {Criteria.class, boolean.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getBestProvider",
             args = {Criteria.class, boolean.class}
         )
     })
     public void testGetProvidersWithCriteria() {
         Criteria criteria = new Criteria();
         List<String> providers = mManager.getProviders(criteria, true);
         assertEquals(1, providers.size());
         assertEquals(PROVIDER_NAME, providers.get(0));
 
         // add some criteria
         criteria.setSpeedRequired(true);
         providers = mManager.getProviders(criteria, false);
         assertEquals(1, providers.size());
         assertEquals(LocationManager.GPS_PROVIDER, providers.get(0));
 
         criteria = new Criteria();
         criteria.setPowerRequirement(Criteria.POWER_HIGH);
         String p = mManager.getBestProvider(criteria, true);
         assertEquals(PROVIDER_NAME, p);
 
         criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
         p = mManager.getBestProvider(criteria, false);
         assertEquals(PROVIDER_NAME, p);
 
         criteria.setPowerRequirement(Criteria.POWER_LOW);
         p = mManager.getBestProvider(criteria, true);
         assertEquals(PROVIDER_NAME, p);
 
         criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
         p = mManager.getBestProvider(criteria, false);
         assertEquals(PROVIDER_NAME, p);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "requestLocationUpdates",
             args = {String.class, long.class, float.class, LocationListener.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "removeUpdates",
             args = {LocationListener.class}
         )
     })
     @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
             + "because there is no simulation of GPS events on the emulator")
     public void testLocationUpdatesWithLocationListener() throws InterruptedException {
         final MockLocationListener listener = new MockLocationListener();
 
         mManager.requestLocationUpdates(PROVIDER_NAME, 0, 0, listener);
         // can not simulator location change on the emulator
         mManager.removeUpdates(listener);
 
         try {
             mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                     (LocationListener) null);
             fail("Should throw IllegalArgumentException if param listener is null!");
         } catch (IllegalArgumentException e) {
             // expected
         }
 
         try {
             mManager.requestLocationUpdates(null, 0, 0, listener);
             fail("Should throw IllegalArgumentException if param provider is null!");
         } catch (IllegalArgumentException e) {
             // expected
         }
 
         Thread t = new Thread() {
             @Override
             public void run() {
                 assertNull(Looper.myLooper());
                 try {
                     mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
                     fail("Should throw RuntimeException if the calling thread has no looper!");
                 } catch (RuntimeException e) {
                     // expected
                 }
             }
         };
         t.start();
         t.join();
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "requestLocationUpdates",
             args = {String.class, long.class, float.class, LocationListener.class, Looper.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "removeUpdates",
             args = {LocationListener.class}
         )
     })
     @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
             + "because there is no simulation of GPS events on the emulator")
     public void testLocationUpdatesWithLocationListenerAndLooper() throws InterruptedException {
         final MockLocationListener listener = new MockLocationListener();
 
         mManager.requestLocationUpdates(PROVIDER_NAME, 0, 0, listener,
                 Looper.myLooper());
         // can not simulator location change on the emulator
         mManager.removeUpdates(listener);
 
         try {
             mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                     (LocationListener) null, Looper.myLooper());
             fail("Should throw IllegalArgumentException if param listener is null!");
         } catch (IllegalArgumentException e) {
             // expected
         }
 
         try {
             mManager.requestLocationUpdates(null, 0, 0, listener, Looper.myLooper());
             fail("Should throw IllegalArgumentException if param provider is null!");
         } catch (IllegalArgumentException e) {
             // expected
         }
 
         Thread t = new Thread() {
             @Override
             public void run() {
                 Looper.prepare();
                 assertNotNull(Looper.myLooper());
                 mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener,
                         Looper.myLooper());
                 mManager.removeUpdates(listener);
                 Looper.myLooper().quit();
                 Looper.loop();
             }
         };
         t.start();
         t.join();
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "requestLocationUpdates",
             args = {String.class, long.class, float.class, PendingIntent.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "removeUpdates",
             args = {PendingIntent.class}
         )
     })
     @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
             + "because there is no simulation of GPS events on the emulator")
     public void testLocationUpdatesWithPendingIntent() {
         Intent i = new Intent();
         i.setAction("android.location.cts.TEST_LOCATION_UPDATES_ACTION");
         PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);
 
         mManager.requestLocationUpdates(PROVIDER_NAME, 0, 0, pi);
         // can not simulator location change on the emulator
         mManager.removeUpdates(pi);
 
         try {
             mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                     (PendingIntent) null);
             fail("Should throw IllegalArgumentException if param intent is null!");
         } catch (IllegalArgumentException e) {
             // expected
         }
 
         try {
             mManager.requestLocationUpdates(null, 0, 0, pi);
             fail("Should throw IllegalArgumentException if param provider is null!");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "addProximityAlert",
             args = {double.class, double.class, float.class, long.class, PendingIntent.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "removeProximityAlert",
             args = {PendingIntent.class}
         )
     })
     public void testAddProximityAlert() {
         Intent i = new Intent();
         i.setAction("android.location.cts.TEST_GET_GPS_STATUS_ACTION");
         PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);
 
         mManager.addProximityAlert(0, 0, 0, 5000, pi);
         mManager.removeProximityAlert(pi);
 
         mManager.addProximityAlert(0, 0, 0, 5000, null);
         mManager.removeProximityAlert(null);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isProviderEnabled",
         args = {String.class}
     )
     public void testIsProviderEnabled() {
          assertNotNull(mManager.getProvider(LocationManager.GPS_PROVIDER));
          assertFalse(mManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
 
          assertNotNull(mManager.getProvider(PROVIDER_NAME));
          assertTrue(mManager.isProviderEnabled(PROVIDER_NAME));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getLastKnownLocation",
         args = {String.class}
     )
     @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
             + "because there is no simulation of GPS events on the emulator")
     public void testGetLastKnownLocation() {
         // can not simulation location changes
         assertNotNull(mManager.getProvider(LocationManager.GPS_PROVIDER));
         assertNull(mManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "addGpsStatusListener",
             args = {GpsStatus.Listener.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "removeGpsStatusListener",
             args = {GpsStatus.Listener.class}
         )
     })
     @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
             + "because there is no simulation of GPS events on the emulator")
     public void testGpsStatusListener() {
         /*
          * The callback of GpsStatusListener can not be tested because
          * there is no simulation of GPS events on the emulator.
          */
         MockGpsStatusListener listener = new MockGpsStatusListener();
         mManager.addGpsStatusListener(listener);
         mManager.removeGpsStatusListener(listener);
 
         mManager.addGpsStatusListener(null);
         mManager.removeGpsStatusListener(null);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getGpsStatus",
         args = {GpsStatus.class}
     )
     @ToBeFixed(bug = "", explanation = "The callbacks of LocationListener can not be tested "
             + "because there is no simulation of GPS events on the emulator")
     public void testGetGpsStatus() {
         /*
          * The callback of GpsStatusListener can not be tested because
          * there is no simulation of GPS events on the emulator.
          */
         assertNotNull(mManager.getGpsStatus(null));
 
         GpsStatus status = mManager.getGpsStatus(null);
         assertSame(status, mManager.getGpsStatus(status));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "The only useful extra command is \"delete_aiding_data\" for GpsLocationProvider. "
                 + "And it requires permission android.permission.ACCESS_LOCATION_EXTRA_COMMANDS",
         method = "sendExtraCommand",
         args = {String.class, String.class, Bundle.class}
     )
     public void testSendExtraCommand() {
         assertNotNull(mManager.getProvider(LocationManager.GPS_PROVIDER));
         // Unknown command
         assertFalse(mManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "unknown",
                 new Bundle()));
 
         // delete all
         assertTrue(mManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data",
                 null));
 
         Bundle bundle = new Bundle();
         // with blank bundle
         assertFalse(mManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data",
                 bundle));
         // delete specific
         bundle.putBoolean("ephemeris", true);
         assertTrue(mManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data",
                 bundle));
 
         // non-exist provider
         String dummyProvider = "dummy";
         assertNull(mManager.getProvider(dummyProvider));
         try {
             mManager.sendExtraCommand(dummyProvider, "delete_aiding_data",
                     bundle);
             fail("Should throw NullPointerException if the provider does not exist!");
         } catch (NullPointerException e) {
             // expected
         }
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "addTestProvider",
         args = {String.class, boolean.class, boolean.class, boolean.class, boolean.class,
                 boolean.class, boolean.class, boolean.class, int.class, int.class}
     )
     public void testAddTestProvider() {
         // it is only used for mock test providers
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "setTestProviderEnabled",
         args = {String.class, boolean.class}
     )
     public void testSetTestProviderEnabled() {
         // it is only used for mock test providers
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "setTestProviderStatus",
         args = {String.class, int.class, Bundle.class, long.class}
     )
     public void testSetTestProviderStatus() {
         // it is only used for mock test providers
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "setTestProviderLocation",
         args = {String.class, Location.class}
     )
     public void testSetTestProviderLocation() {
         // it is only used for mock test providers
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "clearTestProviderEnabled",
         args = {String.class}
     )
     public void testClearTestProviderEnabled() {
         // it is only used for mock test providers
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "clearTestProviderStatus",
         args = {String.class}
     )
     public void testClearTestProviderStatus() {
         // it is only used for mock test providers
     }
 
     @TestTargetNew(
         level = TestLevel.NOT_NECESSARY,
         method = "clearTestProviderLocation",
         args = {String.class}
     )
     public void testClearTestProviderLocation() {
         // it is only used for mock test providers
     }
 
     /**
      * Tests basic proximity alert when entering proximity
      */
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "addProximityAlert",
         args = {double.class, double.class, float.class, long.class, PendingIntent.class}
     )
     public void testEnterProximity() throws Exception {
         doTestEnterProximity(10000);
     }
 
     /**
      * Tests proximity alert when entering proximity, with no expiration
      */
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "addProximityAlert",
         args = {double.class, double.class, float.class, long.class, PendingIntent.class}
     )
     public void testEnterProximity_noexpire() throws Exception {
         doTestEnterProximity(-1);
     }
 
     /**
      * Tests basic proximity alert when exiting proximity
      */
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "addProximityAlert",
         args = {double.class, double.class, float.class, long.class, PendingIntent.class}
     )
     public void testExitProximity() throws Exception {
         // first do enter proximity scenario
         doTestEnterProximity(-1);
 
         // now update to trigger exit proximity proximity
         mIntentReceiver.clearReceivedIntents();
         sendLocation(20, 20);
         waitForAlert();
         assertProximityType(false);
     }
 
     private void checkProviderAlreadyExist(String name) {
         if (mManager.getProvider(name) != null) {
             mManager.removeTestProvider(name);
         }
     }
 
     /**
      * Helper variant for testing enter proximity scenario
      * TODO: add additional parameters as more scenarios are added
      *
      * @param expiration - expiry of proximity alert
      */
     private void doTestEnterProximity(long expiration) throws Exception {
         // update location to outside proximity range
         synchronousSendLocation(30, 30);
         registerProximityListener(0, 0, 1000, expiration);
         sendLocation(0, 0);
         waitForAlert();
         assertProximityType(true);
     }
 
 
     /**
      * Registers the proximity intent receiver
      */
     private void registerProximityListener(double latitude, double longitude, float radius,
             long expiration) {
         String intentKey = "testProximity";
         Intent proximityIntent = new Intent(intentKey);
         mPendingIntent = PendingIntent.getBroadcast(mContext, 0, proximityIntent,
                 PendingIntent.FLAG_CANCEL_CURRENT);
         mIntentReceiver = new TestIntentReceiver(intentKey);
 
         mManager.addProximityAlert(latitude, longitude, radius, expiration, mPendingIntent);
 
         mContext.registerReceiver(mIntentReceiver, mIntentReceiver.getFilter());
     }
 
     /**
      * Blocks until proximity intent notification is received
      *
      * @throws InterruptedException
      */
     private void waitForAlert() throws InterruptedException {
         Log.d(LOG_TAG, "Waiting for proximity update");
         synchronized (mIntentReceiver) {
             mIntentReceiver.wait(PROXIMITY_WAIT_TIME);
         }
 
         assertNotNull("Did not receive proximity alert", mIntentReceiver.getLastReceivedIntent());
     }
 
     /**
      * Asserts that the received intent had the enter proximity property set as
      * expected
      *
      * @param expectedEnterProximity - true if enter proximity expected, false
      *            if exit expected
      */
     private void assertProximityType(boolean expectedEnterProximity) throws Exception {
         boolean proximityTest = mIntentReceiver.getLastReceivedIntent().getBooleanExtra(
                 LocationManager.KEY_PROXIMITY_ENTERING, !expectedEnterProximity);
         assertEquals("proximity alert not set to expected enter proximity value",
                 expectedEnterProximity, proximityTest);
     }
 
     /**
      * Synchronous variant of sendLocation
      */
     private void synchronousSendLocation(final double latitude, final double longitude)
             throws InterruptedException {
         sendLocation(latitude, longitude, this);
         // wait for location to be set
         synchronized (this) {
             wait(UPDATE_LOCATION_WAIT_TIME);
         }
     }
 
     /**
      * Asynchronously update the mock location provider without notification
      */
     private void sendLocation(final double latitude, final double longitude) {
         sendLocation(latitude, longitude, null);
     }
 
     /**
      * Asynchronously update the mock location provider with given latitude and
      * longitude
      *
      * @param latitude - update location
      * @param longitude - update location
      * @param observer - optionally, object to notify when update is sent.If
      *            null, no update will be sent
      */
     private void sendLocation(final double latitude, final double longitude,
             final Object observer) {
         Thread locationUpdater = new Thread() {
             @Override
             public void run() {
                 Location loc = new Location(PROVIDER_NAME);
                 loc.setLatitude(latitude);
                 loc.setLongitude(longitude);
 
                 loc.setTime(java.lang.System.currentTimeMillis());
                 Log.d(LOG_TAG, "Sending update for " + PROVIDER_NAME);
                 mManager.setTestProviderLocation(PROVIDER_NAME, loc);
                 if (observer != null) {
                     synchronized (observer) {
                         observer.notify();
                     }
                 }
             }
         };
         locationUpdater.start();
     }
 
     /**
      * Helper class that receives a proximity intent and notifies the main class
      * when received
      */
     private static class TestIntentReceiver extends BroadcastReceiver {
         private String mExpectedAction;
 
         private Intent mLastReceivedIntent;
 
         public TestIntentReceiver(String expectedAction) {
             mExpectedAction = expectedAction;
             mLastReceivedIntent = null;
         }
 
         public IntentFilter getFilter() {
             IntentFilter filter = new IntentFilter(mExpectedAction);
             return filter;
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent != null && mExpectedAction.equals(intent.getAction())) {
                 Log.d(LOG_TAG, "Intent Received: " + intent.toString());
                 mLastReceivedIntent = intent;
                 synchronized (this) {
                     notify();
                 }
             }
         }
 
         public Intent getLastReceivedIntent() {
             return mLastReceivedIntent;
         }
 
         public void clearReceivedIntents() {
             mLastReceivedIntent = null;
         }
     }
 
     private static class MockLocationListener implements LocationListener {
         private boolean mHasCalledOnLocationChanged;
 
         private boolean mHasCalledOnProviderDisabled;
 
         private boolean mHasCalledOnProviderEnabled;
 
         private boolean mHasCalledOnStatusChanged;
 
         public void reset(){
             mHasCalledOnLocationChanged = false;
             mHasCalledOnProviderDisabled = false;
             mHasCalledOnProviderEnabled = false;
             mHasCalledOnStatusChanged = false;
         }
 
         public boolean hasCalledOnLocationChanged() {
             return mHasCalledOnLocationChanged;
         }
 
         public boolean hasCalledOnProviderDisabled() {
             return mHasCalledOnProviderDisabled;
         }
 
         public boolean hasCalledOnProviderEnabled() {
             return mHasCalledOnProviderEnabled;
         }
 
         public boolean hasCalledOnStatusChanged() {
             return mHasCalledOnStatusChanged;
         }
 
         public void onLocationChanged(Location location) {
             mHasCalledOnLocationChanged = true;
         }
 
         public void onProviderDisabled(String provider) {
             mHasCalledOnProviderDisabled = true;
         }
 
         public void onProviderEnabled(String provider) {
             mHasCalledOnProviderEnabled = true;
         }
 
         public void onStatusChanged(String provider, int status, Bundle extras) {
             mHasCalledOnStatusChanged = true;
         }
     }
 
     private static class MockGpsStatusListener implements Listener {
         private boolean mHasCallOnGpsStatusChanged;
 
         public boolean hasCallOnGpsStatusChanged() {
             return mHasCallOnGpsStatusChanged;
         }
 
         public void reset(){
             mHasCallOnGpsStatusChanged = false;
         }
 
         public void onGpsStatusChanged(int event) {
             mHasCallOnGpsStatusChanged = true;
         }
     }
 }
