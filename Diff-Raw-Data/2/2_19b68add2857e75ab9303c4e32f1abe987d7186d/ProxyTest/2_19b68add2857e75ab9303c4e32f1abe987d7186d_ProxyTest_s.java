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
 
 import android.net.Proxy;
 import android.test.AndroidTestCase;
 
 @TestTargetClass(Proxy.class)
 public class ProxyTest extends AndroidTestCase {
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "Proxy",
         args = {}
     )
     public void testConstructor() {
         new Proxy();
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getDefaultPort",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getDefaultHost",
             args = {}
        ),
     })
     public void testAccessProperties() {
         final int minValidPort = 0;
         final int maxValidPort = 65535;
         int defaultPort = Proxy.getDefaultPort();
         if(null == Proxy.getDefaultHost()) {
             assertEquals(-1, defaultPort);
         } else {
             assertTrue(defaultPort >= minValidPort && defaultPort <= maxValidPort);
         }
     }
 }
