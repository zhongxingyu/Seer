 /*
  * Copyright 2011 Google Inc.
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
 
 package com.imaginea.android.sugarcrm.util;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 /**
  * An extension of {@link ActivityHelper} that provides Android 3.0-specific functionality for
  * Honeycomb tablets. It thus requires API level 11.
  */
 public class ActivityHelperHoneycomb extends ActivityHelper {
     private Menu mOptionsMenu;
 
     protected ActivityHelperHoneycomb(Activity activity) {
         super(activity);
     }
 
     @Override
     public void onPostCreate(Bundle savedInstanceState) {
         // Do nothing in onPostCreate. ActivityHelper creates the old action bar, we don't
         // need to for Honeycomb.
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         mOptionsMenu = menu;
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             // Handle the HOME / UP affordance. Since the app is only two levels deep
             // hierarchically, UP always just goes home.
             goHome();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /** {@inheritDoc} */
     @Override
     public void setupHomeActivity() {
         super.setupHomeActivity();
         // NOTE: there needs to be a content view set before this is called, so this method
         // should be called in onPostCreate.
         if (ViewUtil.isTablet(mActivity)) {
             mActivity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME
                                             | ActionBar.DISPLAY_SHOW_TITLE);
         } else {
            //TODO
           /// mActivity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_USE_LOGO
            //                                | ActionBar.DISPLAY_SHOW_TITLE);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void setupSubActivity() {
         super.setupSubActivity();
         // NOTE: there needs to be a content view set before this is called, so this method
         // should be called in onPostCreate.
         ActionBar actionBar = mActivity.getActionBar();
         if (ViewUtil.isTablet(mActivity)) {
 
             if (actionBar != null)
                 actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
                                                 | ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_HOME_AS_UP
                                                 | ActionBar.DISPLAY_USE_LOGO);
         } else {
             // add check for null explicitly as this can change
             if (actionBar != null)
                 actionBar.setDisplayOptions(0, ActionBar.DISPLAY_HOME_AS_UP
                                                 | ActionBar.DISPLAY_USE_LOGO);
         }
     }
 }
