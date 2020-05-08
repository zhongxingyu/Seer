 package edu.gatech.oad.fullhouse.findmystuff.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import edu.gatech.oad.fullhouse.findmystuff.*;
import edu.gatech.oad.fullhouse.findmystuff.view.HelloAndroidActivity;
 
 public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {
 
     public HelloAndroidActivityTest() {
         super(HelloAndroidActivity.class); 
     }
 
     public void testActivity() {
         HelloAndroidActivity activity = getActivity();
         assertNotNull(activity);
     }
 }
 
