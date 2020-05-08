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
 
 package com.amgems.uwschedule;
 
 import android.content.*;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.*;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.*;
 import com.amgems.uwschedule.services.LoginService;
 
 import java.lang.Override;
 import java.lang.ref.WeakReference;
 
 public class LoginActivity extends FragmentActivity {
 
     private ViewGroup mRootGroup;
     private ViewGroup mUsernameGroup;
     private ViewGroup mProgressBarGroup;
 
     private ImageView mLogoImage;
     private Button mSyncButton;
     private CheckBox mSyncCheckbox;
     private EditText mPasswordEditText;
     private EditText mUsernameEditText;
     private WebView mDebugWebview;
 
     private LoginResponseReceiver mResponseReceiver;
     private LocalBroadcastManager mBroadcastManager;
     private static final String LOGIN_IN_PROGRESS = "mIsInProgress";
     private boolean mIsInProgress;
 
     private boolean mIsSyncRequest;
     private static final String IS_SYNC_REQUEST = "mIsSyncRequest";
 
     private LoginService mLoginService;
     private boolean mIsBounded;
 
     private RelativeLayout.LayoutParams mLogoParamsInputGone;
     private RelativeLayout.LayoutParams mLogoParamsInputVisible;
 
     private static final int MINIMUM_SCREEN_SIZE_CHANGE = 100;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.login);
 
         mRootGroup = (ViewGroup) findViewById(R.id.login_root);
         mLogoImage = (ImageView) findViewById(R.id.husky_logo);
         mUsernameGroup = (ViewGroup) findViewById(R.id.username_group);
         mSyncCheckbox = (CheckBox) findViewById(R.id.sync_checkbox);
         mPasswordEditText = (EditText) findViewById(R.id.password);
         mProgressBarGroup = (ViewGroup) findViewById(R.id.login_progress_group);
         mUsernameEditText = (EditText) findViewById(R.id.username);
         mDebugWebview = (WebView) findViewById(R.id.login_debug_webview);
 
         int logoPixelSizeSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 225,
                 getResources().getDisplayMetrics());
         mLogoParamsInputGone = new RelativeLayout.LayoutParams(logoPixelSizeSmall, logoPixelSizeSmall);
         mLogoParamsInputGone.addRule(RelativeLayout.CENTER_HORIZONTAL);
 
         int logoPixelSizeLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 125,
                 getResources().getDisplayMetrics());
         mLogoParamsInputVisible = new RelativeLayout.LayoutParams(logoPixelSizeLarge, logoPixelSizeLarge);
         mLogoParamsInputVisible.addRule(RelativeLayout.CENTER_HORIZONTAL);
 
         // Register login service broadcast receiver
         mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mResponseReceiver = new LoginResponseReceiver(this);
 
         // Account for keyboard taking up screen space
         mRootGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 int heightDiff = mRootGroup.getRootView().getHeight() - mRootGroup.getHeight();
                 if (heightDiff > MINIMUM_SCREEN_SIZE_CHANGE ||
                     getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) { // Keyboard appeared
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
                 mIsInProgress = true;
                 String username = mUsernameEditText.getText().toString();
                 String password = mPasswordEditText.getText().toString();
                 disableLoginInput();
 
                 Intent loginServiceIntent = new Intent(LoginActivity.this, LoginService.class);
                 loginServiceIntent.putExtra(LoginService.PARAM_IN_USERNAME, username);
                 loginServiceIntent.putExtra(LoginService.PARAM_IN_PASSWORD, password);
                 startService(loginServiceIntent);
             }
         });
 
         mSyncCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 mIsSyncRequest = b;
                 b = b && !mIsInProgress;
                 int visibility = b ? View.VISIBLE : View.GONE;
                 int stringId = b ? R.string.sync : R.string.login;
                 mSyncButton.setText(getString(stringId));
                 mPasswordEditText.setVisibility(visibility);
                 mPasswordEditText.setText("");
             }
         });
 
         // Disable UI controls if currently logging in from orientation change
         mIsInProgress = (savedInstanceState != null) && savedInstanceState.getBoolean(LOGIN_IN_PROGRESS);
         mIsSyncRequest = (savedInstanceState != null) && savedInstanceState.getBoolean(IS_SYNC_REQUEST);
         if (mIsInProgress) {
             disableLoginInput();
         }
 
     }
 
     public void disableLoginInput() {
         mProgressBarGroup.setVisibility(View.VISIBLE);
         mUsernameGroup.setVisibility(View.GONE);
         mPasswordEditText.setVisibility(View.GONE);
         mSyncButton.setVisibility(View.GONE);
 
         // Closes soft keyboard if open
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mRootGroup.getWindowToken(), 0);
     }
 
     public void enableLoginInput() {
         mProgressBarGroup.setVisibility(View.INVISIBLE);
         mUsernameGroup.setVisibility(View.VISIBLE);
         mPasswordEditText.setVisibility(mIsSyncRequest ? View.VISIBLE : View.GONE);
         mSyncButton.setVisibility(View.VISIBLE);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         IntentFilter intentFilter = new IntentFilter(LoginService.ACTION_RESPONSE);
         intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
         mBroadcastManager.registerReceiver(mResponseReceiver, intentFilter);
     }
 
     @Override
     protected void onPause() {
         if (mResponseReceiver != null) {
             mBroadcastManager.unregisterReceiver(mResponseReceiver);
             mResponseReceiver = null;
         }
 
         super.onPause();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         Intent loginBindIntent = new Intent(this, LoginService.class);
         bindService(loginBindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         if (mIsBounded) {
             unbindService(mServiceConnection);
             mIsBounded = false;
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putBoolean(LOGIN_IN_PROGRESS, mIsInProgress);
         outState.putBoolean(IS_SYNC_REQUEST, mIsSyncRequest);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.login, menu);
         return true;
     }
 
     private ServiceConnection mServiceConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
             LoginService.LocalLoginBinder loginBinder = (LoginService.LocalLoginBinder) iBinder;
             mLoginService = loginBinder.getService();
             mIsBounded = true;
             if (mLoginService.pollForCookie()) {
                 enableLoginInput();
                 mIsInProgress = false;
 
                 mDebugWebview.setVisibility(View.VISIBLE);
                 mDebugWebview.loadData(mLoginService.getCookie().toString(), "text/html", "UTF-8");
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName componentName) {
             mIsBounded = false;
         }
     };
 
     private static class LoginResponseReceiver extends BroadcastReceiver {
 
         private WeakReference<LoginActivity> mLoginActivity;
 
         public LoginResponseReceiver(LoginActivity callingActivity) {
             mLoginActivity = new WeakReference<LoginActivity>(callingActivity);
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             String loginResponse = intent.getStringExtra(LoginService.PARAM_OUT);
             LoginActivity loginActivity = mLoginActivity.get();
 
             Log.d(LoginService.class.getSimpleName(), "SENDING SERVICE RECEIVE");
 
             if (loginActivity != null && loginResponse.equals("OK")) {
                 loginActivity.enableLoginInput();
                 loginActivity.mIsInProgress = false;
 
                 loginActivity.mDebugWebview.setVisibility(View.VISIBLE);
                 if (loginActivity.mIsBounded) {
                     loginActivity.mDebugWebview.loadData(loginActivity.mLoginService.getCookie().toString(),
                                                          "text/html", "UTF-8");
                 }
             }
         }
     }
     
 }
