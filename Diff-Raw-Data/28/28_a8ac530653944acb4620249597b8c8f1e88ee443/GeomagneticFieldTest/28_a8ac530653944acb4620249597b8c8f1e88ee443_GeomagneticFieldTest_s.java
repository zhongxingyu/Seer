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
 
 package android.hardware.cts;
 
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 import android.hardware.GeomagneticField;
 import android.test.AndroidTestCase;
 
 import java.util.GregorianCalendar;
 
 @TestTargetClass(GeomagneticField.class)
 public class GeomagneticFieldTest extends AndroidTestCase {
    private static final float LATITUDE_OF_CHENGDU = 104.06f;
    private static final float LONGITUDE_OF_CHENGDU = 30.67f;
     private static final float ALTITUDE_OF_CHENGDU = 500f;
    private static final float DELTA = 1.0f;
    private static final long TEST_TIME = new GregorianCalendar(2009, 5, 1).getTimeInMillis();
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "GeomagneticField",
             args = {float.class, float.class, float.class, long.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getDeclination",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getInclination",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getFieldStrength",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getHorizontalStrength",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getX",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getY",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getZ",
             args = {}
         )
     })
     public void testGeomagneticField() {
         GeomagneticField geomagneticField = new GeomagneticField(LATITUDE_OF_CHENGDU,
                 LONGITUDE_OF_CHENGDU, ALTITUDE_OF_CHENGDU, TEST_TIME);
        assertEquals(16.12f, geomagneticField.getDeclination(), DELTA);
        assertEquals(88.04f, geomagneticField.getInclination(), DELTA);
        assertEquals(56768.22f, geomagneticField.getFieldStrength(), DELTA);
        assertEquals(1943.81f, geomagneticField.getHorizontalStrength(), DELTA);
        assertEquals(1867.41f, geomagneticField.getX(), DELTA);
        assertEquals(539.61f, geomagneticField.getY(), DELTA);
        assertEquals(56734.93f, geomagneticField.getZ(), DELTA);
     }
 }
