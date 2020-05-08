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
 
 package com.bigpupdev.synodroid.utils;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.ui.HomeActivity;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 /**
  * An extension of {@link ActivityHelper} that provides Android 3.0-specific functionality for
  * Honeycomb tablets. It thus requires API level 11.
  */
 public class ActivityHelperHoneycomb extends ActivityHelper {
     private Menu mOptionsMenu;
     private OnClickListener ocl = null;
 
     protected ActivityHelperHoneycomb(Activity activity) {
         super(activity);
     }
 
     @Override
     public void onPostCreate(Bundle savedInstanceState) {
         // Do nothing in onPostCreate. ActivityHelper creates the old action bar, we don't
         // need to for Honeycomb.
     }
     
     public void invalidateOptionMenu(){
     	if (UIUtils.isICS()){
 			mActivity.invalidateOptionsMenu();
 		}	
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
             	if (ocl != null){
             		ocl.onClick(new View(mActivity));
             	}
             	else{
             		goHome();
             	}
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Invoke "home" action, returning to {@link com.google.android.apps.iosched.ui.HomeActivity}.
      */
     public void goHome() {
         if (mActivity instanceof HomeActivity) {
             return;
         }
 
         final Intent intent = new Intent(mActivity, HomeActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         mActivity.startActivity(intent);
         
         if (!UIUtils.isHoneycomb()) {
             mActivity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
         }
     }
     
     /**
      * Sets up the action bar with the given title and accent color. If title is null, then
      * the app logo will be shown instead of a title. Otherwise, a home button and title are
      * visible. If color is null, then the default colorstrip is visible.
      */
     public void setupActionBar(CharSequence title, boolean is_home) {
         setActionBarTitle(title, false);
     }
     
     /** {@inheritDoc} */
     @Override
     public void setupHomeActivity() {
         super.setupHomeActivity();
         // NOTE: there needs to be a content view set before this is called, so this method
         // should be called in onPostCreate.
         if (UIUtils.isTablet(mActivity)) {
             mActivity.getActionBar().setDisplayOptions(
                     0,
                     ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
         } else {
             mActivity.getActionBar().setDisplayOptions(
                     ActionBar.DISPLAY_USE_LOGO,
                     ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
         }
         if (UIUtils.isICS())
         	mActivity.getActionBar().setHomeButtonEnabled(true);
         mActivity.getActionBar().setDisplayShowTitleEnabled(true);
     }
 
     /** {@inheritDoc} */
     @Override
     public void setupSubActivity() {
         super.setupSubActivity();
         // NOTE: there needs to be a content view set before this is called, so this method
         // should be called in onPostCreate.
         mActivity.getActionBar().setDisplayOptions(
                 ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO,
                 ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
     }
 
     /**
      * No-op on Honeycomb. The action bar title always remains the same.
      */
     @Override
     public void setActionBarTitle(CharSequence title, boolean is_secure) {
     	mActivity.getActionBar().setTitle(title);
 		if (UIUtils.isICS()){
     		if (is_secure){
         		mActivity.getActionBar().setLogo(R.drawable.title_logo_https);
         	}
     	}
     	else{
     		if (is_secure){
     			View v = mActivity.getLayoutInflater().inflate(R.layout.https_custom_view, null);
     			mActivity.getActionBar().setCustomView(v);
     			mActivity.getActionBar().setDisplayShowCustomEnabled(true);
     		}
         	else{
     			mActivity.getActionBar().setDisplayShowCustomEnabled(false);
     		}
     	}
     }
 
     /** {@inheritDoc} */
     @Override
     public void setRefreshActionButtonCompatState(boolean refreshing) {
         // On Honeycomb, we can set the state of the refresh button by giving it a custom
         // action view.
         if (mOptionsMenu == null) {
             return;
         }
 
         final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
         if (refreshItem != null) {
             if (refreshing) {
                 refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
             } else {
                 refreshItem.setActionView(null);
             }
         }
     }
     
     @Override
     public void setTitleOnClickListener(OnClickListener pOcl){
     	ocl = pOcl;
     }
 }
