 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package android.net.cts;
 
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 import dalvik.annotation.ToBeFixed;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.DetailedState;
 import android.net.NetworkInfo.State;
 import android.test.AndroidTestCase;
 
 @TestTargetClass(ConnectivityManager.class)
 public class ConnectivityManagerTest extends AndroidTestCase {
 
     public static final int TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
     public static final int TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
     private static final int HOST_ADDRESS = 0x7f000001;// represent ip 127.0.0.1
     private ConnectivityManager mCm;
    // must include both mobile data + wifi
    private static final int MIN_NUM_NETWORK_TYPES = 2;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mCm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getNetworkInfo",
         args = {int.class}
     )
     public void testGetNetworkInfo() {
 
         // this test assumes that there are at least two network types.
        assertTrue(mCm.getAllNetworkInfo().length >= MIN_NUM_NETWORK_TYPES);
         NetworkInfo ni = mCm.getNetworkInfo(1);
         State state = ni.getState();
         assertTrue(State.UNKNOWN.ordinal() >= state.ordinal()
                 && state.ordinal() >= State.CONNECTING.ordinal());
         DetailedState ds = ni.getDetailedState();
         assertTrue(DetailedState.FAILED.ordinal() >= ds.ordinal()
                 && ds.ordinal() >= DetailedState.IDLE.ordinal());
 
         ni = mCm.getNetworkInfo(0);
         state = ni.getState();
         assertTrue(State.UNKNOWN.ordinal() >= state.ordinal()
                 && state.ordinal() >= State.CONNECTING.ordinal());
         ds = ni.getDetailedState();
         assertTrue(DetailedState.FAILED.ordinal() >= ds.ordinal()
                 && ds.ordinal() >= DetailedState.IDLE.ordinal());
 
         ni = mCm.getNetworkInfo(-1);
         assertNull(ni);
 
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isNetworkTypeValid",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getAllNetworkInfo",
             args = {}
         )
     })
     public void testIsNetworkTypeValid() {
 
         NetworkInfo[] ni = mCm.getAllNetworkInfo();
 
         for (NetworkInfo n : ni) {
             assertTrue(ConnectivityManager.isNetworkTypeValid(n.getType()));
         }
         assertFalse(ConnectivityManager.isNetworkTypeValid(-1));
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getNetworkPreference",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.SUFFICIENT,
             method = "setNetworkPreference",
             args = {int.class}
         )
     })
     public void testAccessNetworkPreference() {
         int initialSetting = mCm.getNetworkPreference();
 
         // Changing the network preference requires android.permission.WRITE_SECURE_SETTINGS,
         // which is only available to signed or system applications.
 
         // Setting the same preference that is already set is a no-op and does not throw
         // a SecurityException.
         mCm.setNetworkPreference(initialSetting);
         assertEquals(initialSetting, mCm.getNetworkPreference());
 
         // find a valid setting that is different from the initial setting
         int validSetting = -1;
         NetworkInfo[] ni = mCm.getAllNetworkInfo();
         for (NetworkInfo n : ni) {
             int type = n.getType();
             if (type != initialSetting) {
                 validSetting = type;
                 break;
             }
         }
         if (validSetting >= 0) {
             try {
                 mCm.setNetworkPreference(validSetting);
                 fail("Trying to change the network preference should throw SecurityException");
             } catch (SecurityException expected) {
                 // expected
             }
         }
 
         // find an invalid setting
         int invalidSetting = -1;
         for (int i = 0; i < 10; i++) {
             if (!ConnectivityManager.isNetworkTypeValid(i)) {
                 invalidSetting = i;
                 break;
             }
         }
         if (invalidSetting >= 0) {
             // illegal setting should be ignored
             mCm.setNetworkPreference(invalidSetting);
             assertEquals(initialSetting, mCm.getNetworkPreference());
         }
 
         // illegal setting should be ignored
         mCm.setNetworkPreference(-1);
         assertEquals(initialSetting, mCm.getNetworkPreference());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test getAllNetworkInfo().",
         method = "getAllNetworkInfo",
         args = {}
     )
     public void testGetAllNetworkInfo() {
         NetworkInfo[] ni = mCm.getAllNetworkInfo();
        assertTrue(ni.length >= MIN_NUM_NETWORK_TYPES);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "startUsingNetworkFeature",
             args = {int.class, java.lang.String.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "stopUsingNetworkFeature",
             args = {int.class, java.lang.String.class}
         )
     })
     public void testStartUsingNetworkFeature() {
 
         final String invalidateFeature = "invalidateFeature";
         final String mmsFeature = "enableMMS";
         final int failureCode = -1;
 
         assertEquals(failureCode, mCm.startUsingNetworkFeature(TYPE_MOBILE, invalidateFeature));
         assertEquals(failureCode, mCm.stopUsingNetworkFeature(TYPE_MOBILE, invalidateFeature));
 
         // Should return failure(-1) because MMS is not supported on WIFI.
         assertEquals(failureCode, mCm.startUsingNetworkFeature(TYPE_WIFI, mmsFeature));
         assertEquals(failureCode, mCm.stopUsingNetworkFeature(TYPE_WIFI, mmsFeature));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "requestRouteToHost",
         args = {int.class, int.class}
     )
     public void testRequestRouteToHost() {
 
         NetworkInfo[] ni = mCm.getAllNetworkInfo();
         for (NetworkInfo n : ni) {
            // make sure network is up
            if (n.isConnected()) {
                assertTrue(mCm.requestRouteToHost(n.getType(), HOST_ADDRESS));
            }
         }
 
         assertFalse(mCm.requestRouteToHost(-1, HOST_ADDRESS));
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getActiveNetworkInfo",
         args = {}
     )
     @ToBeFixed(bug="1695243", explanation="No Javadoc")
     public void testGetActiveNetworkInfo() {
         NetworkInfo ni = mCm.getActiveNetworkInfo();
 
         if (ni != null) {
             assertTrue(ni.getType() >= 0);
         }
     }
 
     @TestTargetNew(
         level = TestLevel.SUFFICIENT,
         method = "getBackgroundDataSetting",
         args = {}
     )
     public void testTest() {
         mCm.getBackgroundDataSetting();
     }
 }
