 package fi.testbed2.robotium;
 
 import android.test.ActivityInstrumentationTestCase2;
 import com.jayway.android.robotium.solo.Solo;
 import fi.testbed2.R;
 import fi.testbed2.activity.AnimationActivity;
 import fi.testbed2.activity.MainActivity;
 import fi.testbed2.activity.TestbedPreferenceActivity;
 
 /**
  * Robotium test for the main activity.
  *
  * Prerequisite for the test are:
  *  - Number of map images to be shown: 10
  *
  */
 public class MainActivityRobotiumTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
     private Solo solo;
 
     public MainActivityRobotiumTest() {
         super(MainActivity.class);
     }
 
     @Override
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
 
     @Override
     public void tearDown() throws Exception {
         solo.finishOpenedActivities();
     }
 
     public void testAboutBoxShouldBeShown() throws Exception {
 
         solo.assertCurrentActivity("Should be MainActivity", MainActivity.class);
         solo.sendKey(Solo.MENU);
         solo.clickOnText(solo.getString(R.string.main_menu_about));
         assertTrue(solo.searchText(solo.getString(R.string.about_visit_website)));
         solo.assertCurrentActivity("Should be MainActivity", MainActivity.class);
         solo.clickOnText(getActivity().getResources().getString(R.string.close_button));
 
     }
 
     public void testPreferencesShouldBeShown() throws Exception {
 
         solo.sendKey(Solo.MENU);
         solo.clickOnText(solo.getString(R.string.main_menu_preferences));
 
         solo.waitForText(solo.getString(R.string.preference_category_maps));
         solo.assertCurrentActivity("Should be TestbedPreferenceActivity", TestbedPreferenceActivity.class);
 
         solo.scrollDown();
 
         /*
          * Note, there is a bug in robotium when checking the status of checkboxes:
          * http://code.google.com/p/robotium/issues/detail?id=106
          * Therefore, the checkboxes is not tested here so thoroughly
          */
 
         solo.clickOnText(getActivity().getResources().getString(R.string.preference_location_municipalities_title));
         solo.clickOnText("Askola");
         solo.clickOnText("Espoo");
         solo.clickOnText("OK");
 
         solo.goBack();
         solo.assertCurrentActivity("Should be MainActivity", MainActivity.class);
 
     }
 
     public void testRefreshButtonSelected() throws Exception {
 
         solo.clickOnImageButton(0);
 
         solo.waitForText("@");
         solo.assertCurrentActivity("Should be AnimationActivity", AnimationActivity.class);
         solo.waitForText("/10");
         assertTrue(solo.searchText("/10"));
 
     }
 
     public void testDownloadCancelledWhenRefreshFromMainActivity() throws Exception {
 
        setMapType(4);

         solo.sendKey(Solo.MENU);
         solo.clickOnText(solo.getString(R.string.main_menu_refresh));
         solo.goBack();
 
         solo.waitForText(solo.getString(R.string.notice_cancelled));
         solo.assertCurrentActivity("Should be MainActivity", MainActivity.class);
 
     }
 
     public void testDownloadCancelledWhenRefreshFromAnimationActivity() throws Exception {
 
        setMapType(1);

         solo.clickOnImageButton(0);
 
         solo.waitForText("@");
         solo.assertCurrentActivity("Should be AnimationActivity", AnimationActivity.class);
         solo.waitForText("/10");
         assertTrue(solo.searchText("/10"));
 
        setMapType(2);

         solo.sendKey(Solo.MENU);
         solo.clickOnText(solo.getString(R.string.main_menu_refresh));
         solo.waitForText(solo.getString(R.string.progress_parsing));
         solo.goBack();
 
         solo.waitForText(solo.getString(R.string.notice_cancelled));
         solo.assertCurrentActivity("Should be MainActivity", MainActivity.class);
 
     }
 
     public void testDownloadCancelledWhenBackPressedInAnimationActivity() throws Exception {
 
         setMapType(1);
 
         solo.clickOnImageButton(0);
 
         solo.waitForText("@");
         solo.assertCurrentActivity("Should be AnimationActivity", AnimationActivity.class);
         solo.waitForText("/10");
         assertTrue(solo.searchText("/10"));
 
         // Change map type which clears the cache and we download images again
         setMapType(2);
 
         solo.sendKey(Solo.MENU);
         solo.clickOnText(solo.getString(R.string.main_menu_refresh));
         solo.waitForText("@");
         solo.goBack();
 
         solo.waitForText(solo.getString(R.string.notice_cancelled));
         solo.assertCurrentActivity("Should be MainActivity", MainActivity.class);
 
     }
 
     private void setMapType(int index) {
 
         solo.sendKey(Solo.MENU);
         solo.clickOnText(solo.getString(R.string.main_menu_preferences));
         solo.waitForText(solo.getString(R.string.preference_category_maps));
         solo.assertCurrentActivity("Should be TestbedPreferenceActivity", TestbedPreferenceActivity.class);
         solo.clickOnText(solo.getString(R.string.preference_map_type_title));
         solo.clickInList(index);
         solo.goBack();
 
     }
 
 
 }
