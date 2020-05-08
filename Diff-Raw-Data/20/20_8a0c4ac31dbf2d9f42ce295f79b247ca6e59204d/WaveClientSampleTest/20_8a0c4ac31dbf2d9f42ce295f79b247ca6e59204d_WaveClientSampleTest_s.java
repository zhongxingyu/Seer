 package edu.berkeley.waveclientsample;
 
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 
 /**
  * This is a simple framework for a test of an Application.  See
  * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
  * how to write and extend Application tests.
  * <p/>
  * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class edu.berkeley.waveclientsample.WaveClientSampleTest \
 * edu.berkeley.waveclientsample.tests/android.test.InstrumentationTestRunner
  */
 public class WaveClientSampleTest extends ActivityInstrumentationTestCase2<WaveClientSample> {
 
     public WaveClientSampleTest() {
         super("edu.berkeley.waveclientsample", WaveClientSample.class);
     }
 
     /**
      * Verifies that activity under test can be launched.
      */
     public void testActivityTestCaseSetUpProperly() {
         Activity a = getActivity();
         assertNotNull("activity should be launched successfully", a);
     }
 }
