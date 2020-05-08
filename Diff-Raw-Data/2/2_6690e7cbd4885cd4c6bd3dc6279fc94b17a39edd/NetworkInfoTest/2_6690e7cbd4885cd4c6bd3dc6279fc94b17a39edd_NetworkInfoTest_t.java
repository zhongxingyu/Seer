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
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.DetailedState;
 import android.net.NetworkInfo.State;
 import android.test.AndroidTestCase;
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 @TestTargetClass(NetworkInfo.class)
 public class NetworkInfoTest extends AndroidTestCase {
 
     public static final int TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
     public static final int TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
    public static final String MOBILE_TYPE_NAME = "mobile";
     public static final String WIFI_TYPE_NAME = "WIFI";
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isConnectedOrConnecting",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setFailover",
             args = {boolean.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isFailover",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isRoaming",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getType",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getSubtype",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getTypeName",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getSubtypeName",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setIsAvailable",
             args = {boolean.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isAvailable",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "isConnected",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getDetailedState",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getState",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getReason",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getExtraInfo",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "toString",
             args = {}
         )
     })
     public void testAccessNetworkInfoProperties() {
         ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(
                 Context.CONNECTIVITY_SERVICE);
 
         NetworkInfo[] ni = cm.getAllNetworkInfo();
         assertTrue(ni.length >= 2);
 
         assertFalse(ni[TYPE_MOBILE].isFailover());
         assertFalse(ni[TYPE_WIFI].isFailover());
 
         // test environment:connect as TYPE_MOBILE, and connect to internet.
         assertEquals(TYPE_MOBILE, ni[TYPE_MOBILE].getType());
         assertEquals(TYPE_WIFI, ni[TYPE_WIFI].getType());
 
         // don't know the return value
         ni[TYPE_MOBILE].getSubtype();
         ni[TYPE_WIFI].getSubtype();
 
         assertEquals(MOBILE_TYPE_NAME, ni[TYPE_MOBILE].getTypeName());
         assertEquals(WIFI_TYPE_NAME, ni[TYPE_WIFI].getTypeName());
 
         // don't know the return value
         ni[TYPE_MOBILE].getSubtypeName();
         ni[TYPE_WIFI].getSubtypeName();
 
         if(ni[TYPE_MOBILE].isConnectedOrConnecting()) {
             assertTrue(ni[TYPE_MOBILE].isAvailable());
             assertTrue(ni[TYPE_MOBILE].isConnected());
             assertEquals(State.CONNECTED, ni[TYPE_MOBILE].getState());
             assertEquals(DetailedState.CONNECTED, ni[TYPE_MOBILE].getDetailedState());
             ni[TYPE_MOBILE].getReason();
             ni[TYPE_MOBILE].getExtraInfo();
         }
 
         if(ni[TYPE_WIFI].isConnectedOrConnecting()) {
             assertTrue(ni[TYPE_WIFI].isAvailable());
             assertTrue(ni[TYPE_WIFI].isConnected());
             assertEquals(State.CONNECTED, ni[TYPE_WIFI].getState());
             assertEquals(DetailedState.CONNECTED, ni[TYPE_WIFI].getDetailedState());
             ni[TYPE_WIFI].getReason();
             ni[TYPE_WIFI].getExtraInfo();
         }
 
         assertFalse(ni[TYPE_MOBILE].isRoaming());
         assertFalse(ni[TYPE_WIFI].isRoaming());
 
         assertNotNull(ni[TYPE_MOBILE].toString());
         assertNotNull(ni[TYPE_WIFI].toString());
     }
 }
