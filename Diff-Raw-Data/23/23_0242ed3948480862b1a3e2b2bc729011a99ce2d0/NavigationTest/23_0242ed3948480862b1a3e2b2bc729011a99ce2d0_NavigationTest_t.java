 package com.cloudbees.gasp.robotium;
 
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.cloudbees.gasp.R;
 import com.cloudbees.gasp.activity.ConsoleActivity;
 import com.cloudbees.gasp.activity.PlacesActivity;
 import com.cloudbees.gasp.activity.PlacesDetailActivity;
 import com.cloudbees.gasp.activity.RestaurantListActivity;
 import com.cloudbees.gasp.activity.ReviewListActivity;
 import com.cloudbees.gasp.activity.SetPreferencesActivity;
 import com.cloudbees.gasp.activity.TwitterStreamActivity;
 import com.cloudbees.gasp.activity.UserListActivity;
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * Copyright (c) 2013 Mark Prichard, CloudBees
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 public class NavigationTest extends ActivityInstrumentationTestCase2<ConsoleActivity> {
     private Solo solo;
 
     public NavigationTest() {
         super(ConsoleActivity.class);
     }
 
     @Override
     public void setUp() throws Exception {
         solo = new Solo(getInstrumentation(), getActivity());
     }
 
     /**
      * Start Activity from Action Bar
      * @param resId     Resource id for the Activity
      * @param to        Activity subclass to start
      */
     private void actionBarActivity(int resId, Class<? extends Activity> to) {
         solo.clickOnActionBarItem(resId);
         solo.waitForActivity(to, 5000);
         solo.assertCurrentActivity("Action Bar: " + to.getClass().getName(), to);
     }
 
     /**
      * Start Activity from Options Menu
     * @param text  Menu item text
      * @param to    Activity subclass to start
      */
     private void optionsMenuActivity(String text, Class<? extends Activity> to) {
         solo.clickOnMenuItem(text);
         solo.waitForActivity(to, 5000);
         solo.assertCurrentActivity("Action Bar: " + to.getClass().getName(), to);
     }
 
     /**
      * Simulate Action Bar Home click
      * @param to    Parent Activity
      */
     private void actionBarHome(Class<? extends Activity> to) {
         solo.clickOnActionBarHomeButton();
         solo.waitForActivity(to, 5000);
         solo.assertCurrentActivity("Action Bar Home", to);
     }
 
     public void testPlacesNavigation() {
         actionBarActivity(R.id.gasp_menu_places, PlacesActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testPlacesDetailNavigation() {
         actionBarActivity(R.id.gasp_menu_places, PlacesActivity.class);
 
         // Allow time for ListActivity to populate
         solo.sleep(10000);
         assertTrue(solo.waitForView(R.id.places_list, 1, 1000));
 
         // Select first item from list
         solo.clickInList(1);
         solo.waitForActivity(PlacesDetailActivity.class,5000);
         solo.assertCurrentActivity("Clicked", PlacesDetailActivity.class);
 
         actionBarHome(PlacesActivity.class);
 
         // Allow time for ListActivity to populate
         solo.sleep(10000);
         assertTrue(solo.waitForView(R.id.places_list, 1, 1000));
 
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testTwitterNavigation() {
         actionBarActivity(R.id.gasp_menu_twitter, TwitterStreamActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testSettingsNavigation() {
        optionsMenuActivity(solo.getString(R.string.gasp_settings), SetPreferencesActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testPlacesSettingsNavigation() {
         actionBarActivity(R.id.gasp_menu_places, PlacesActivity.class);
        optionsMenuActivity(solo.getString(R.string.gasp_settings), SetPreferencesActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testRestaurantsNavigation() {
        optionsMenuActivity(solo.getString(R.string.gasp_restaurants_data), RestaurantListActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testReviewsNavigation() {
        optionsMenuActivity(solo.getString(R.string.gasp_reviews_data), ReviewListActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     public void testUsersNavigation() {
        optionsMenuActivity(solo.getString(R.string.gasp_users_data), UserListActivity.class);
         actionBarHome(ConsoleActivity.class);
     }
 
     @Override
     public void tearDown() throws Exception {
         solo.finishOpenedActivities();
     }
 }
