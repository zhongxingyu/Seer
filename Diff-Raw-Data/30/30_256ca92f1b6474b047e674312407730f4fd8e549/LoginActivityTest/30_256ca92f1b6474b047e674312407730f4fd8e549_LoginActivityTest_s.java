 package com.huskysoft.eduki.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.CoursesListActivity;
 import com.huskysoft.eduki.LoginActivity;
 import com.huskysoft.eduki.MainActivity;
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * 
  * @author Cody Thomas LoginActivityTest tests the login activity
  *
  */
 public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {
 
     private Solo solo;
    private static final int TIMEOUT = 10000;
     
     /**
      * Construct the tests
      */
     public LoginActivityTest() {
         super(LoginActivity.class);
     }
     
     /**
      * Initializes required variables
      */
     @Before
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
     
     /**
      * Test that the login activity exits the activity upon successful login
      */
     @Test(timeout=TIMEOUT)
     public void testLoginRenavigates() {        
         EditText user = solo.getEditText(0);
         EditText pass = solo.getEditText(1);
         solo.typeText(user, "test@eduki.com");
         solo.typeText(pass, "password");
         solo.clickOnButton(solo.getString(com.huskysoft.eduki.R.string.login));
         // Need to sleep to allow the activity to finish
        solo.sleep(2000);
         solo.assertCurrentActivity("Wrong activity", MainActivity.class);
     }
     
     @Override
     public void tearDown() throws Exception {
         getActivity().logout();
         solo.finishOpenedActivities();
         super.tearDown();
     }
    
     /**
      * Test that the browse courses image leads to the courses list activity 
      */
     @Test(timeout=TIMEOUT)
     public void testCourseBrowsing() {
         solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
         solo.clickOnImageButton(0);
         solo.assertCurrentActivity("Wrong activity", CoursesListActivity.class);
     }
 }
