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
 
 public class LoginActivity extends FragmentActivity
                            implements LoaderManager.LoaderCallbacks<LoginAuthLoader.Result> {
 
     private ViewGroup mRootGroup;
     private ViewGroup mUsernameGroup;
     private ViewGroup mProgressBarGroup;
 
     private ImageView mLogoImage;
     private Button mSyncButton;
     private CheckBox mSyncCheckbox;
     private EditText mPasswordEditText;
     private EditText mUsernameEditText;
     private WebView mDebugWebview;
 
     private static final String LOGIN_IN_PROGRESS = "mLoginInProgress";
     private boolean mLoginInProgress;
 
     private boolean mIsSyncRequest;
     private static final String IS_SYNC_REQUEST = "mIsSyncRequest";
 
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
 
         LoaderManager manager = getLoaderManager();
         if (manager.getLoader(0) != null) {
             manager.initLoader(0, null, this);
         }
 
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
                 mLoginInProgress = true;
                 disableLoginInput();
 
                 getLoaderManager().initLoader(0, null, LoginActivity.this);
             }
         });
 
         mSyncCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 mIsSyncRequest = b;
                 b = b && !mLoginInProgress;
                 int visibility = b ? View.VISIBLE : View.GONE;
                 int stringId = b ? R.string.sync : R.string.login;
                 mSyncButton.setText(getString(stringId));
                 mPasswordEditText.setVisibility(visibility);
                 mPasswordEditText.setText("");
             }
         });
 
         // Disable UI controls if currently logging in from orientation change
         mLoginInProgress = (savedInstanceState != null) && savedInstanceState.getBoolean(LOGIN_IN_PROGRESS);
         mIsSyncRequest = (savedInstanceState != null) && savedInstanceState.getBoolean(IS_SYNC_REQUEST);
         if (mLoginInProgress) {
             disableLoginInput();
         }
 
     }
 
     public void disableLoginInput() {
         mProgressBarGroup.setVisibility(View.VISIBLE);
         mUsernameGroup.setVisibility(View.GONE);
         mPasswordEditText.setVisibility(View.GONE);
         mSyncButton.setVisibility(View.GONE);
         mLoginInProgress = true;
 
         // Closes soft keyboard if open
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mRootGroup.getWindowToken(), 0);
     }
 
     public void enableLoginInput() {
         mProgressBarGroup.setVisibility(View.INVISIBLE);
         mUsernameGroup.setVisibility(View.VISIBLE);
         mPasswordEditText.setVisibility(mIsSyncRequest ? View.VISIBLE : View.GONE);
         mSyncButton.setVisibility(View.VISIBLE);
         mLoginInProgress = false;
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putBoolean(LOGIN_IN_PROGRESS, mLoginInProgress);
         outState.putBoolean(IS_SYNC_REQUEST, mIsSyncRequest);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.login, menu);
         return true;
     }
 
     @Override
     public Loader<LoginAuthLoader.Result> onCreateLoader(int i, Bundle bundle) {
         String username = mUsernameEditText.getText().toString();
         String password = mPasswordEditText.getText().toString();
 
         Loader<LoginAuthLoader.Result> loader = new LoginAuthLoader(this, username, password);
         loader.forceLoad();
         return loader;
     }
 
     @Override
     public void onLoadFinished(Loader<LoginAuthLoader.Result> loginResponseLoader, LoginAuthLoader.Result result) {
         LoginAuthenticator.Response response = result.getResponse();
         if (response == LoginAuthenticator.Response.OK) {
            mDebugWebview.setVisibility(View.VISIBLE);
            mDebugWebview.loadData(result.getCookieValue(), "text/html", "UTF-8");
             mProgressBarGroup.setVisibility(View.GONE);
         } else {
             enableLoginInput();
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(response.getStringResId())
                    .setTitle(R.string.login_dialog_title)
                    .setPositiveButton(R.string.ok, null)
                    .setCancelable(true);
             builder.create().show();
         }
     }
 
     @Override
     public void onLoaderReset(Loader<LoginAuthLoader.Result> loginResponseLoader) {  }
 
     /*    private ServiceConnection mServiceConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
             LoginService.LocalLoginBinder loginBinder = (LoginService.LocalLoginBinder) iBinder;
             mLoginService = loginBinder.getService();
             mIsBounded = true;
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
             LoginAuthenticator.Response loginResponse = (LoginAuthenticator.Response) intent.getSerializableExtra(LoginService.PARAM_OUT_RESPONSE);
             LoginActivity loginActivity = mLoginActivity.get();
 
             Log.d(LoginService.class.getSimpleName(), "SENDING SERVICE RECEIVE");
 
             if (loginActivity != null) {
                 loginActivity.enableLoginInput();
                 loginActivity.mLoginInProgress = false;
 
                 loginActivity.mDebugWebview.setVisibility(View.VISIBLE);
                 if (loginActivity.mIsBounded) {
                     String cookieData;
                     if (loginResponse == LoginAuthenticator.Response.OK) {
                         cookieData = intent.getStringExtra(LoginService.PARAM_OUT_COOKIE);
                     } else {
                         cookieData = "Username or password invalid";
                     }
 
                     loginActivity.mDebugWebview.loadData(cookieData, "text/html", "UTF-8");
                 }
             }
         }
     } */
     
 }
