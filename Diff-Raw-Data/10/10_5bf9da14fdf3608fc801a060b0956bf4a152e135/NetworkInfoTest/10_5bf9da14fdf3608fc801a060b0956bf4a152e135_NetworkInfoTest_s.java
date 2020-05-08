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
     public static final String MOBILE_TYPE_NAME = "MOBILE";
     public static final String WIFI_TYPE_NAME = "WIFI";
 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of NetworkInfo.",
        method = "NetworkInfo",
        args = {int.class}
    )
    public void testConstructor() {
        new NetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

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
             method = "getType",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getTypeName",
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
             notes = "Test isConnectedOrConnecting().",
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
         assertEquals(ni[TYPE_MOBILE].getType(), TYPE_MOBILE);
         assertEquals(ni[TYPE_WIFI].getType(), TYPE_WIFI);
 
         assertEquals(MOBILE_TYPE_NAME, ni[TYPE_MOBILE].getTypeName());
         assertEquals(WIFI_TYPE_NAME, ni[TYPE_WIFI].getTypeName());
 
         assertTrue(ni[TYPE_MOBILE].isConnectedOrConnecting());
         assertFalse(ni[TYPE_WIFI].isConnectedOrConnecting());
 
         assertTrue(ni[TYPE_MOBILE].isAvailable());
         assertFalse(ni[TYPE_WIFI].isAvailable());
 
         assertTrue(ni[TYPE_MOBILE].isConnected());
         assertFalse(ni[TYPE_WIFI].isConnected());
 
         assertEquals(State.CONNECTED, ni[TYPE_MOBILE].getState());
         assertEquals(State.UNKNOWN, ni[TYPE_WIFI].getState());
 
         assertEquals(DetailedState.CONNECTED, ni[TYPE_MOBILE].getDetailedState());
         assertEquals(DetailedState.IDLE, ni[TYPE_WIFI].getDetailedState());
 
         assertNotNull(ni[TYPE_MOBILE].getReason());
         assertNull(ni[TYPE_WIFI].getReason());
 
         assertNotNull(ni[TYPE_MOBILE].getExtraInfo());
         assertNull(ni[TYPE_WIFI].getExtraInfo());
 
         assertNotNull(ni[TYPE_MOBILE].toString());
         assertNotNull(ni[TYPE_WIFI].toString());
     }
 }
