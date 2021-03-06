 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *   UWSchedule student class and registration sharing interface
  *   Copyright (C) 2013 Sherman Pay, Jeremy Teo, Zachary Iqbal
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by`
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.amgems.uwschedule.ui;
 
 import android.app.Activity;
 import android.app.ActivityOptions;
 import android.app.AlertDialog;
 import android.app.LoaderManager;
 import android.content.*;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.TypedValue;
 import android.view.*;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.*;
 import com.amgems.uwschedule.R;
 import com.amgems.uwschedule.api.uw.LoginAuthenticator;
 import com.amgems.uwschedule.loaders.LoginAuthLoader;
 
 import java.lang.Override;
 
 public class LoginActivity extends Activity
                            implements LoaderManager.LoaderCallbacks<LoginAuthLoader.Result> {
 
     private ViewGroup mRootGroup;
     private ViewGroup mProgressBarGroup;
 
     private ImageView mLogoImage;
     private Button mSyncButton;
     private EditText mPasswordEditText;
     private EditText mUsernameEditText;
 
     private static final String LOGIN_IN_PROGRESS = "mLoginInProgress";
     private static final int LOGIN_LOADER_ID = 0;
     private boolean mLoginInProgress;
 
     private RelativeLayout.LayoutParams mLogoParamsInputGone;
     private RelativeLayout.LayoutParams mLogoParamsInputVisible;
 
     private static final int MINIMUM_SCREEN_SIZE_CHANGE = 100;
     private static final int LOGO_PIXSIZE_SMALL = 125;
     private static final int LOGO_PIXSIZE_LARGE = 225;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.login_activity);
 
         mRootGroup = (ViewGroup) findViewById(R.id.login_root);
         mLogoImage = (ImageView) findViewById(R.id.husky_logo);
         mPasswordEditText = (EditText) findViewById(R.id.password);
         mProgressBarGroup = (ViewGroup) findViewById(R.id.login_progress_group);
         mUsernameEditText = (EditText) findViewById(R.id.username);
 
         int logoPixelSizeSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LOGO_PIXSIZE_LARGE,
                 getResources().getDisplayMetrics());
         mLogoParamsInputGone = new RelativeLayout.LayoutParams(logoPixelSizeSmall, logoPixelSizeSmall);
         mLogoParamsInputGone.addRule(RelativeLayout.CENTER_HORIZONTAL);
 
         int logoPixelSizeLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LOGO_PIXSIZE_SMALL,
                 getResources().getDisplayMetrics());
         mLogoParamsInputVisible = new RelativeLayout.LayoutParams(logoPixelSizeLarge, logoPixelSizeLarge);
         mLogoParamsInputVisible.addRule(RelativeLayout.CENTER_HORIZONTAL);
 
         LoaderManager manager = getLoaderManager();
         if (manager.getLoader(LOGIN_LOADER_ID) != null) {
             manager.initLoader(LOGIN_LOADER_ID, null, this);
         }
 
         // Account for keyboard taking up screen space
         mRootGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 int heightDiff = mRootGroup.getRootView().getHeight() - mRootGroup.getHeight();
                 // Keyboard appeared or orientation is landscape
                 if (heightDiff > MINIMUM_SCREEN_SIZE_CHANGE ||
                     getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                     mLogoImage.setLayoutParams(mLogoParamsInputVisible);
                 } else {
                     mLogoImage.setLayoutParams(mLogoParamsInputGone);
                 }
             }
         });
 
         mSyncButton = (Button) findViewById(R.id.sync_button);
         mSyncButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mLoginInProgress = true;
                 disableLoginInput();
 
                 LoaderManager manager = getLoaderManager();
                 if (manager.getLoader(LOGIN_LOADER_ID) != null) {
                     manager.restartLoader(LOGIN_LOADER_ID, null, LoginActivity.this);
                 } else {
                     manager.initLoader(LOGIN_LOADER_ID, null, LoginActivity.this);
                 }
 
             }
         });
 
         // Disable UI controls if currently logging in from orientation change
         mLoginInProgress = (savedInstanceState != null) && savedInstanceState.getBoolean(LOGIN_IN_PROGRESS);
         if (mLoginInProgress) {
             disableLoginInput();
         }
 
     }
 
     public void disableLoginInput() {
 
         mProgressBarGroup.setVisibility(View.VISIBLE);
         mUsernameEditText.setVisibility(View.GONE);
         mPasswordEditText.setVisibility(View.GONE);
         mSyncButton.setVisibility(View.GONE);
 
         // Closes soft keyboard if open
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mRootGroup.getWindowToken(), 0);
     }
 
     public void enableLoginInput() {
         mProgressBarGroup.setVisibility(View.INVISIBLE);
         mUsernameEditText.setVisibility(View.VISIBLE);
         mPasswordEditText.setVisibility(View.VISIBLE);
         mSyncButton.setVisibility(View.VISIBLE);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putBoolean(LOGIN_IN_PROGRESS, mLoginInProgress);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public Loader<LoginAuthLoader.Result> onCreateLoader(int id, Bundle args) {
         String username = mUsernameEditText.getText().toString();
         String password = mPasswordEditText.getText().toString();
 
         Loader<LoginAuthLoader.Result> loader = new LoginAuthLoader(this, username, password);
         loader.forceLoad();
         return loader;
     }
 
     @Override
     public void onLoadFinished(Loader<LoginAuthLoader.Result> loginResponseLoader, LoginAuthLoader.Result result) {
         if (mLoginInProgress) {
             LoginAuthenticator.Response response = result.getResponse();
             if (response == LoginAuthenticator.Response.OK) {
                 Intent homeActivityIntent = new Intent(this, HomeActivity.class);
                 homeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
                 Bundle translationBundle =
                         ActivityOptions.makeCustomAnimation(this, R.anim.activity_transition_slide_right_in,
                                                             R.anim.activity_transition_slide_right_out).toBundle();
                 startActivity(homeActivityIntent, translationBundle);
                 finish();
 
             } else {
                 enableLoginInput();
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(response.getStringResId())
                         .setTitle(R.string.login_dialog_title)
                         .setPositiveButton(R.string.ok, null)
                         .setCancelable(true);
                 builder.create().show();
 
             }
             mLoginInProgress = false;
         }
     }
 
     @Override
     public void onLoaderReset(Loader<LoginAuthLoader.Result> loginResponseLoader) {  }
 
 }
