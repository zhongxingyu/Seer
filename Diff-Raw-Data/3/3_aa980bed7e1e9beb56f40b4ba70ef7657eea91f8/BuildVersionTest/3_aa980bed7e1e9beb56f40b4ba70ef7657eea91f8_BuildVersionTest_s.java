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
 
 package android.os.cts;
 
 import dalvik.annotation.TestTargetClass;
 
 import android.os.Build;
 import android.util.Log;
 
 import junit.framework.TestCase;
 
 @TestTargetClass(Build.VERSION.class)
 public class BuildVersionTest extends TestCase {
 
     private static final String LOG_TAG = "BuildVersionTest";
     private static final String EXPECTED_RELEASE = "1.6";
     private static final String EXPECTED_SDK = "4";
 
     public void testReleaseVersion() {
         // Applications may rely on the exact release version
         assertEquals(EXPECTED_RELEASE, Build.VERSION.RELEASE);
         assertEquals(EXPECTED_SDK, Build.VERSION.SDK);
     }
 
     /**
      * Verifies {@link Build.FINGERPRINT} follows expected format:
      * <p/>
      * <code>
      * (BRAND)/(PRODUCT)/(DEVICE)/(BOARD):(VERSION.RELEASE)/(BUILD_ID)/
      * (BUILD_NUMBER):(BUILD_VARIANT)/(TAGS)
      * </code>
      */
     public void testBuildFingerprint() {
         final String fingerprint = Build.FINGERPRINT;
         Log.i(LOG_TAG, String.format("Testing fingerprint %s", fingerprint));
 
         assertEquals("Build fingerprint must not include whitespace", -1,
                 fingerprint.indexOf(' '));
         final String[] fingerprintSegs = fingerprint.split("/");
         assertEquals("Build fingerprint does not match expected format", 7, fingerprintSegs.length);
         assertEquals(Build.BRAND, fingerprintSegs[0]);
         assertEquals(Build.PRODUCT, fingerprintSegs[1]);
         assertEquals(Build.DEVICE, fingerprintSegs[2]);
         // parse BOARD:VERSION_RELEASE
         String[] bootloaderPlat = fingerprintSegs[3].split(":");
         assertEquals(Build.BOARD, bootloaderPlat[0]);
         assertEquals(Build.VERSION.RELEASE, bootloaderPlat[1]);
         assertEquals(Build.ID, fingerprintSegs[4]);
         // no requirements for BUILD_NUMBER and BUILD_VARIANT
         assertTrue(fingerprintSegs[5].contains(":"));
        assertEquals(Build.TAGS, fingerprintSegs[6]);
     }
 }
