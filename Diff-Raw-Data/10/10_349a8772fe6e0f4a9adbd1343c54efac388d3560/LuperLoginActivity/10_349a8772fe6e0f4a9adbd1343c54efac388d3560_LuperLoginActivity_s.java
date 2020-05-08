 package com.teamluper.luper;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
<<<<<<< HEAD
=======
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.androidannotations.annotations.Background;
>>>>>>> 318cdbf64de99c3a1abaff1d57496cecef68d2db
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.UiThread;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 @EActivity
 public class LuperLoginActivity extends SherlockFragmentActivity {
   ViewPager mViewPager;
   TabsAdapter mTabsAdapter;
 
<<<<<<< HEAD
   /**
    * The default email to populate the email field with.
    */
   public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
   /**
    * Keep track of the login task to ensure we can cancel it if requested.
    */
   private UserLoginTask mAuthTask = null;
 
   // Values for email and password at the time of the login attempt.
   private String mEmail;
   private String mPassword;
 
   // UI references.
   private EditText mEmailView;
   private EditText mPasswordView;
   private View mLoginFormView;
   private View mLoginStatusView;
   private TextView mLoginStatusMessageView;
 
   //Instance of FacebookLoginFragment
   private FacebookLoginFragment facebookLoginFragment;
=======
   private SQLiteDataSource dataSource;
>>>>>>> 318cdbf64de99c3a1abaff1d57496cecef68d2db
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
     // enable tabs in the ActionBar
     final ActionBar bar = getSupportActionBar();
     bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     bar.setTitle(R.string.login_title);
     bar.setDisplayHomeAsUpEnabled(false);
 
     // set up the ViewPager and Tabs
     mViewPager = new ViewPager(this);
     mViewPager.setId(R.id.tabcontentpager);
     setContentView(mViewPager);
     mTabsAdapter = new TabsAdapter(this, mViewPager);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Log In"),
       TabLoginFragment_.class, null);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Register"),
       TabRegisterFragment_.class, null);
 
     // connect to the database
     dataSource = new SQLiteDataSource(this);
     dataSource.open();
   }
 
   @Override
   protected void onStop() {
     if(dataSource.isOpen()) dataSource.close();
     super.onStop();
   }
 
   @Override
   protected void onResume() {
     if(!dataSource.isOpen()) dataSource.open();
     super.onResume();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     getSupportMenuInflater().inflate(R.menu.luper_login, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if(item.getItemId() == R.id.action_forgot_password) {
       Intent intent = new Intent(this, LuperForgotPasswordActivity_.class);
       startActivity(intent);
     }
     return true;
   }
 
   public SQLiteDataSource getDataSource() {
     return dataSource;
   }
 
   public void skipLogin(View v) {
     // TODO actually sign in to a dummy account, so we can refuse to launch the main activity if truly logged out.
     Intent intent = new Intent(this, LuperMainActivity_.class);
     startActivity(intent);
   }
 
   public static String sha1(String input) throws NoSuchAlgorithmException {
     MessageDigest mDigest = MessageDigest.getInstance("SHA1");
     byte[] result = mDigest.digest(input.getBytes());
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < result.length; i++) {
       sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
     }
     return sb.toString();
   }
 }
