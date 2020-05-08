 package com.huskysoft.eduki.test;
 
 import android.preference.PreferenceManager;
 import android.test.ActivityInstrumentationTestCase2;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.CoursesListActivity;
 import com.huskysoft.eduki.MainActivity;
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * 
  * @author Rafael Vertido MainActivityTest tests the Main Activity
  */
 
 public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
     private Solo solo;
     private static final int TIMEOUT = 10000;
     
     /**
      * Constructs the tests
      */
     public MainActivityTest() {
         super(MainActivity.class);
     }
     
     /**
      * Initializes required variables
      */
     @Before
     public void setUp() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("authenticated", true).commit();
         solo = new Solo(getInstrumentation(), getActivity());
     }
     
     /**
      * Clear shared preferences
      */
     @After
     public void tearDown() throws Exception {
         PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
     }
     
     /**
      * This is a black box test, making sure the new activity starts after clicking the courses button
      */
     @Test(timeout=TIMEOUT)
     public void testAllCoursesClick() {
         solo.assertCurrentActivity("Wrong activity", MainActivity.class);
         solo.clickOnActionBarItem(com.huskysoft.eduki.R.id.action_courses);
         solo.assertCurrentActivity("Did not start the Course list Activity", CoursesListActivity.class);
     }
 }
