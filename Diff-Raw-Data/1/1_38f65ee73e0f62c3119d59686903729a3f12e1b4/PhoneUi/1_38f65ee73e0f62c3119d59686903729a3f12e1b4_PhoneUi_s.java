 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.browser;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.FrameLayout;
 
 /**
  * Ui for regular phone screen sizes
  */
 public class PhoneUi extends BaseUi {
 
     private static final String LOGTAG = "PhoneUi";
     private static final float NAV_TAB_SCALE = 0.75f;
 
     private TitleBarPhone mTitleBar;
     private ActiveTabsPage mActiveTabsPage;
     private boolean mUseQuickControls;
     private PieControl mPieControl;
     private NavScreen mNavScreen;
 
     boolean mExtendedMenuOpen;
     boolean mOptionsMenuOpen;
     boolean mAnimating;
 
     /**
      * @param browser
      * @param controller
      */
     public PhoneUi(Activity browser, UiController controller) {
         super(browser, controller);
         mTitleBar = new TitleBarPhone(mActivity, mUiController, this,
                 mContentView);
         // mTitleBar will be always be shown in the fully loaded mode on
         // phone
         mTitleBar.setProgress(100);
         mActivity.getActionBar().hide();
         setUseQuickControls(BrowserSettings.getInstance().useQuickControls());
     }
 
     @Override
     public void hideComboView() {
         super.hideComboView();
         mActivity.getActionBar().hide();
     }
 
     // lifecycle
 
     @Override
     public void onPause() {
         // FIXME: This removes the active tabs page and resets the menu to
         // MAIN_MENU.  A better solution might be to do this work in onNewIntent
         // but then we would need to save it in onSaveInstanceState and restore
         // it in onCreate/onRestoreInstanceState
         if (mActiveTabsPage != null) {
             mUiController.removeActiveTabsPage(true);
         }
         super.onPause();
     }
 
     @Override
     public void onDestroy() {
         hideTitleBar();
     }
 
     @Override
     public void editUrl(boolean clearInput) {
         if (mUseQuickControls) {
             getTitleBar().setShowProgressOnly(false);
         }
         super.editUrl(clearInput);
     }
 
     @Override
     public boolean onBackKey() {
         if (mActiveTabsPage != null) {
             // if tab page is showing, hide it
             mUiController.removeActiveTabsPage(true);
             return true;
         } else if (mNavScreen != null) {
             mNavScreen.close();
             return true;
         }
         return super.onBackKey();
     }
 
     @Override
     public boolean onMenuKey() {
         if (!isComboViewShowing()) {
             if (mNavScreen == null) {
                 showNavScreen();
             } else {
                 mNavScreen.close();
             }
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean dispatchKey(int code, KeyEvent event) {
         if (!isComboViewShowing()) {
         }
         return false;
     }
 
     @Override
     public void onProgressChanged(Tab tab) {
         if (tab.inForeground()) {
             int progress = tab.getLoadProgress();
             mTitleBar.setProgress(progress);
             if (progress == 100) {
                 if (!mOptionsMenuOpen || !mExtendedMenuOpen) {
                     hideTitleBar();
                     if (mUseQuickControls) {
                         mTitleBar.setShowProgressOnly(false);
                     }
                 }
             } else {
                 if (!mOptionsMenuOpen || mExtendedMenuOpen) {
                     if (mUseQuickControls && !mTitleBar.isEditingUrl()) {
                         mTitleBar.setShowProgressOnly(true);
                         setTitleGravity(Gravity.TOP);
                     }
                     showTitleBar();
                 }
             }
         }
     }
 
     @Override
     public void setActiveTab(final Tab tab) {
         captureTab(mActiveTab);
         super.setActiveTab(tab, true);
         setActiveTab(tab, true);
     }
 
     @Override
     void setActiveTab(Tab tab, boolean needsAttaching) {
         BrowserWebView view = (BrowserWebView) tab.getWebView();
         // TabControl.setCurrentTab has been called before this,
         // so the tab is guaranteed to have a webview
         if (view == null) {
             Log.e(LOGTAG, "active tab with no webview detected");
             return;
         }
         // Request focus on the top window.
         if (mUseQuickControls) {
             mPieControl.forceToTop(mContentView);
             view.setScrollListener(null);
         } else {
             // check if title bar is already attached by animation
             if (mTitleBar.getParent() == null) {
                 view.setEmbeddedTitleBar(mTitleBar);
             }
         }
         if (tab.isInVoiceSearchMode()) {
             showVoiceTitleBar(tab.getVoiceDisplayTitle(), tab.getVoiceSearchResults());
         } else {
             revertVoiceTitleBar(tab);
         }
         updateLockIconToLatest(tab);
         tab.getTopWindow().requestFocus();
     }
 
     @Override
     protected TitleBarBase getTitleBar() {
         return mTitleBar;
     }
 
     // active tabs page
 
     @Override
     public void showActiveTabsPage() {
         captureTab(mActiveTab);
         mActiveTabsPage = new ActiveTabsPage(mActivity, mUiController);
         mTitleBar.setVisibility(View.GONE);
         hideTitleBar();
         mContentView.addView(mActiveTabsPage, COVER_SCREEN_PARAMS);
         mActiveTabsPage.requestFocus();
     }
 
     /**
      * Remove the active tabs page.
      */
     @Override
     public void removeActiveTabsPage() {
         mContentView.removeView(mActiveTabsPage);
         mTitleBar.setVisibility(View.VISIBLE);
         mActiveTabsPage = null;
     }
 
     @Override
     public void showComboView(boolean startWithHistory, Bundle extras) {
         if (mNavScreen != null) {
             hideNavScreen(false);
         }
         super.showComboView(startWithHistory, extras);
     }
 
     @Override
     public boolean showsWeb() {
         return super.showsWeb() && mActiveTabsPage == null;
     }
 
     // menu handling callbacks
 
     @Override
     public void onContextMenuCreated(Menu menu) {
         hideTitleBar();
     }
 
     @Override
     public void onContextMenuClosed(Menu menu, boolean inLoad) {
         if (inLoad) {
             showTitleBar();
         }
     }
 
     // action mode callbacks
 
     @Override
     public void onActionModeStarted(ActionMode mode) {
         hideTitleBar();
     }
 
     @Override
     public void onActionModeFinished(boolean inLoad) {
         if (inLoad) {
             if (mUseQuickControls) {
                 mTitleBar.setShowProgressOnly(true);
             }
             showTitleBar();
         }
         mActivity.getActionBar().hide();
     }
 
     @Override
     protected void setTitleGravity(int gravity) {
         if (mUseQuickControls) {
             FrameLayout.LayoutParams lp =
                     (FrameLayout.LayoutParams) getTitleBar().getLayoutParams();
             lp.gravity = gravity;
             getTitleBar().setLayoutParams(lp);
         } else {
             super.setTitleGravity(gravity);
         }
     }
 
     private void setUseQuickControls(boolean useQuickControls) {
         mUseQuickControls = useQuickControls;
         getTitleBar().setUseQuickControls(mUseQuickControls);
         if (useQuickControls) {
             mPieControl = new PieControl(mActivity, mUiController, this);
             mPieControl.attachToContainer(mContentView);
             WebView web = getWebView();
             if (web != null) {
                 web.setEmbeddedTitleBar(null);
             }
         } else {
             if (mPieControl != null) {
                 mPieControl.removeFromContainer(mContentView);
             }
             WebView web = getWebView();
             if (web != null) {
                 web.setEmbeddedTitleBar(mTitleBar);
             }
             setTitleGravity(Gravity.NO_GRAVITY);
         }
     }
 
     @Override
     protected void captureTab(final Tab tab) {
         if (tab == null) return;
         BrowserWebView web = (BrowserWebView) tab.getWebView();
         if (web != null) {
             tab.setScreenshot(web.capture());
         }
     }
 
     void showNavScreen() {
         detachTab(mActiveTab);
         mNavScreen = new NavScreen(mActivity, mUiController, this);
         // Add the custom view to its container.
         mCustomViewContainer.addView(mNavScreen, COVER_SCREEN_PARAMS);
         mContentView.setVisibility(View.GONE);
         mCustomViewContainer.setVisibility(View.VISIBLE);
         mCustomViewContainer.bringToFront();
     }
 
     void hideNavScreen(boolean animate) {
         Tab tab = mNavScreen.getSelectedTab();
         mCustomViewContainer.removeView(mNavScreen);
         mNavScreen = null;
         mCustomViewContainer.setVisibility(View.GONE);
         mUiController.setActiveTab(tab);
         // Show the content view.
         mContentView.setVisibility(View.VISIBLE);
     }
 
 }
