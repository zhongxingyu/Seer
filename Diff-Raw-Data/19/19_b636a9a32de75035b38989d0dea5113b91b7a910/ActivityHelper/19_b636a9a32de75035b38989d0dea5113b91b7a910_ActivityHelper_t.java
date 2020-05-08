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
 import com.bigpupdev.synodroid.ui.DownloadFragment;
 import com.bigpupdev.synodroid.ui.HomeActivity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * A class that handles some common activity-related functionality in the app, such as setting up
  * the action bar. This class provides functioanlity useful for both phones and tablets, and does
  * not require any Android 3.0-specific features.
  */
 public class ActivityHelper {
     protected Activity mActivity;
     
     /**
      * Factory method for creating {@link ActivityHelper} objects for a given activity. Depending
      * on which device the app is running, either a basic helper or Honeycomb-specific helper will
      * be returned.
      */
     public static ActivityHelper createInstance(Activity activity) {
         return UIUtils.isHoneycomb() ?
                 new ActivityHelperHoneycomb(activity) :
                 new ActivityHelper(activity);
     }
 
     protected ActivityHelper(Activity activity) {
         mActivity = activity;
     }
 
     public void invalidateOptionMenu(){
     	
     }
     
     public void onPostCreate(Bundle savedInstanceState) {
         // Create the action bar
     	SimpleMenu menu = new SimpleMenu(mActivity);
         mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
         // TODO: call onPreparePanelMenu here as well
         for (int i = 0; i < menu.size(); i++) {
             MenuItem item = menu.getItem(i);
             if (item.getOrder() == 0) 
             	addActionButtonCompatFromMenuItem(item);
         }
         
         //ActionMode menus
         ViewGroup actionMode = (ViewGroup) mActivity.findViewById(R.id.actionmode_compat);
     	
        if (actionMode != null){
	        SimpleMenu actionMenu = new SimpleMenu(mActivity);
	        mActivity.getMenuInflater().inflate(R.menu.action_mode_menu, actionMenu);
	        for (int i = 0; i < actionMenu.size(); i++) {
	            MenuItem item = actionMenu.getItem(i);
	            addActionButtonCompatFromMenuItem(actionMode, item, null);
	        }
         }
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
     	return false;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_search:
                 goSearch();
                 return true;
         }
         return false;
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         return false;
     }
 
     /**
      * Method, to be called in <code>onPostCreate</code>, that sets up this activity as the
      * home activity for the app.
      */
     public void setupHomeActivity() {
     }
 
     /**
      * Method, to be called in <code>onPostCreate</code>, that sets up this activity as a
      * sub-activity in the app.
      */
     public void setupSubActivity() {
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
         
         //TODO: fix!?
         //if (!UIUtils.isHoneycomb()) {
         //    mActivity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
         //}
     }
 
     /**
      * Invoke "search" action, triggering a default search.
      */
     public void goSearch() {
         mActivity.startSearch(null, false, Bundle.EMPTY, false);
     }
 
     public void setTitleOnClickListener(OnClickListener ocl){
     	ViewGroup actionBar = getActionBarCompat();
         if (actionBar == null) {
             return;
         }
 
        ImageButton logo = (ImageButton) actionBar.findViewById(R.id.actionbar_compat_logo);
        if (logo != null) {
        	logo.setOnClickListener(ocl);
         }
     }
     
     /**
      * Sets up the action bar with the given title and accent color. If title is null, then
      * the app logo will be shown instead of a title. Otherwise, a home button and title are
      * visible. If color is null, then the default colorstrip is visible.
      */
     public void setupActionBar(CharSequence title, boolean is_home) {
         final ViewGroup actionBarCompat = getActionBarCompat();
         if (actionBarCompat == null) {
             return;
         }
 
         LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
         springLayoutParams.weight = 1;
         
         LinearLayout.LayoutParams secLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
         secLayoutParams.gravity = 0x10;
         secLayoutParams.leftMargin = 4;
         
         OnClickListener homeClickListener = new OnClickListener() {
             public void onClick(View view) {
                 goHome();
             }
         };
 
     	if (!is_home){
     		// Add Home button
     		addActionButtonCompat(R.drawable.ic_title_home, R.string.description_home,
     				homeClickListener, true);
     	}
     	else{
     		ImageButton logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
     		logo.setLayoutParams(secLayoutParams);
             actionBarCompat.addView(logo);
     	}
 
         // Add title text
         TextView titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
         titleText.setLayoutParams(springLayoutParams);
         titleText.setText(title);
         actionBarCompat.addView(titleText);
         
         //ACTION MODE STUFF
         ViewGroup actionMode = (ViewGroup) mActivity.findViewById(R.id.actionmode_compat);
         
         if (actionMode != null){        	
         	addActionButtonCompat(actionMode, R.attr.actionmodeCompatLogoStyle, R.drawable.actionmode_compat_logo, R.string.description_home,
         			null, true);
         	
             // Add title text
             TextView actionText = new TextView(mActivity, null, R.attr.actionmodeCompatTextStyle);
             actionText.setLayoutParams(springLayoutParams);
             actionText.setText("");
             actionMode.addView(actionText);
         }
     }
 
     /**
      * Sets the action bar title to the given string.
      */
     public void setActionBarTitle(CharSequence title, boolean is_secure) {
         ViewGroup actionBar = getActionBarCompat();
         if (actionBar == null || title.equals("")) {
             return;
         }
 
         TextView titleText = (TextView) actionBar.findViewById(R.id.actionbar_compat_text);
         if (titleText != null) {
             titleText.setText(title);
         }
         
         ImageButton sec = (ImageButton) actionBar.findViewById(R.id.actionbar_compat_logo);
         if (sec != null) {
         	sec.setImageResource(is_secure ? R.drawable.actionbar_compat_logo_https : R.drawable.actionbar_compat_logo);
         }
         
     }
 
     /**
      * Returns the {@link ViewGroup} for the action bar on phones (compatibility action bar).
      * Can return null, and will return null on Honeycomb.
      */
     public ViewGroup getActionBarCompat() {
         return (ViewGroup) mActivity.findViewById(R.id.actionbar_compat);
     }
 
     /**
      * Adds an action bar button to the compatibility action bar (on phones).
      */
     private View addActionButtonCompat(ViewGroup actionBar, int style, int iconResId, int textResId,
             OnClickListener clickListener, boolean separatorAfter) {
     	// Create the button
         ImageButton actionButton = new ImageButton(mActivity, null,
         		style);
         actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                 (int) mActivity.getResources().getDimension(R.dimen.actionbar_compat_height),
                 ViewGroup.LayoutParams.FILL_PARENT));
         actionButton.setImageResource(iconResId);
         actionButton.setScaleType(ImageView.ScaleType.CENTER);
         actionButton.setContentDescription(mActivity.getResources().getString(textResId));
         if (clickListener != null)
         	actionButton.setOnClickListener(clickListener);
 
         // Add separator and button to the action bar in the desired order
 
         actionBar.addView(actionButton);
 
         return actionButton;
     }
     private View addActionButtonCompat(int iconResId, int textResId,
             OnClickListener clickListener, boolean separatorAfter) {
         final ViewGroup actionBar = getActionBarCompat();
         if (actionBar == null) {
             return null;
         }
         return addActionButtonCompat(actionBar, R.attr.actionbarCompatButtonStyle, iconResId, textResId,
                 clickListener, separatorAfter);
     }
 
     /**
      * Adds an action button to the compatibility action bar, using menu information from a
      * {@link MenuItem}. If the menu item ID is <code>menu_refresh</code>, the menu item's state
      * can be changed to show a loading spinner using
      * {@link ActivityHelper#setRefreshActionButtonCompatState(boolean)}.
      */
     private View addActionButtonCompatFromMenuItem(final MenuItem item) {
     	final ViewGroup actionBar = getActionBarCompat();
         if (actionBar == null) {
             return null;
         }
         OnClickListener ocl = new View.OnClickListener() {
             public void onClick(View view) {
                 mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
             }
         };
         return addActionButtonCompatFromMenuItem(actionBar, item, ocl);
     }	
     
     private View addActionButtonCompatFromMenuItem(ViewGroup actionBar, final MenuItem item, OnClickListener l) {
              // Create the button
         ImageButton actionButton = new ImageButton(mActivity, null,
                 R.attr.actionbarCompatButtonStyle);
         actionButton.setId(item.getItemId());
         actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                 (int) mActivity.getResources().getDimension(R.dimen.actionbar_compat_height),
                 ViewGroup.LayoutParams.FILL_PARENT));
         actionButton.setImageDrawable(item.getIcon());
         actionButton.setScaleType(ImageView.ScaleType.CENTER);
         actionButton.setContentDescription(item.getTitle());
         if (l != null)
         	actionButton.setOnClickListener(l);
 
         actionBar.addView(actionButton);
 
         if (item.getItemId() == R.id.menu_refresh) {
             // Refresh buttons should be stateful, and allow for indeterminate progress indicators,
             // so add those.
             int buttonWidth = mActivity.getResources()
                     .getDimensionPixelSize(R.dimen.actionbar_compat_height);
             int buttonWidthDiv3 = buttonWidth / 3;
             ProgressBar indicator = new ProgressBar(mActivity, null,
                     R.attr.actionbarCompatProgressIndicatorStyle);
             LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(
                     buttonWidthDiv3, buttonWidthDiv3);
             indicatorLayoutParams.setMargins(buttonWidthDiv3-1, buttonWidthDiv3,
                     buttonWidth - 2 * buttonWidthDiv3, 0);
             indicator.setLayoutParams(indicatorLayoutParams);
             indicator.setVisibility(View.GONE);
             indicator.setId(R.id.menu_refresh_progress);
             actionBar.addView(indicator);
         }
 
         return actionButton;
     }
 
     /**
      * Sets the indeterminate loading state of a refresh button added with
      * {@link ActivityHelper#addActionButtonCompatFromMenuItem(android.view.MenuItem)}
      * (where the item ID was menu_refresh).
      */
     public void setRefreshActionButtonCompatState(boolean refreshing) {
         View refreshButton = mActivity.findViewById(R.id.menu_refresh);
         View refreshIndicator = mActivity.findViewById(R.id.menu_refresh_progress);
 
         if (refreshButton != null) {
             refreshButton.setVisibility(refreshing ? View.GONE : View.VISIBLE);
         }
         if (refreshIndicator != null) {
             refreshIndicator.setVisibility(refreshing ? View.VISIBLE : View.GONE);
         }
     }
     
     public void stopActionMode(){
     	View actionBar = mActivity.findViewById(R.id.actionbar_compat);
         View actionMode = mActivity.findViewById(R.id.actionmode_compat);
         
         if (actionBar != null){
         	actionBar.setVisibility(View.VISIBLE);
         }
 
         if (actionMode != null){
         	actionMode.setVisibility(View.GONE);
         }
     }
     
     public void startActionMode(DownloadFragment fragment, OnClickListener cancelClickListener,
     		OnClickListener clearClickListener, OnClickListener resumeClickListener,
     		OnClickListener pauseClickListener){
     	ViewGroup actionBar = (ViewGroup) mActivity.findViewById(R.id.actionbar_compat);
     	ViewGroup actionMode = (ViewGroup) mActivity.findViewById(R.id.actionmode_compat);
         
         if (actionBar != null){
         	actionBar.setVisibility(View.GONE);
         }
 
         if (actionMode != null){
         	actionMode.setVisibility(View.VISIBLE);
         	ImageButton logo = (ImageButton) actionMode.findViewById(R.id.actionmode_compat_logo);
         	logo.setOnClickListener(cancelClickListener);
         	
         	ImageButton clear = (ImageButton) actionMode.findViewById(R.id.menu_clear);
         	ImageButton pause = (ImageButton) actionMode.findViewById(R.id.menu_pause);
         	ImageButton resume = (ImageButton) actionMode.findViewById(R.id.menu_resume);
         	clear.setOnClickListener(clearClickListener);
         	pause.setOnClickListener(pauseClickListener);
         	resume.setOnClickListener(resumeClickListener);
         }
     }
     
     public void setActionModeTitle(String title){
     	ViewGroup actionMode = (ViewGroup) mActivity.findViewById(R.id.actionmode_compat);
     	
     	if (actionMode != null){
     		TextView titleText = (TextView) actionMode.findViewById(R.id.actionmode_compat_text);
     		titleText.setText(title);
     	}
     }
 }
