 package qvdev.examples.travisforandroid.tests;
 
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.qvdev.apps.twitflick.View.MainView;
 
 /**
  * Created by dirkwilmer on 7/3/13.
  */
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainView> {
 
     Activity mainActivity = null;
 
     public MainActivityTest() {
         super(MainView.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mainActivity = getActivity();
 
     }
 
     public void testActivityCreation() {
        assertNull("Activity is null, check creating of activity", mainActivity);
     }
 
 }
