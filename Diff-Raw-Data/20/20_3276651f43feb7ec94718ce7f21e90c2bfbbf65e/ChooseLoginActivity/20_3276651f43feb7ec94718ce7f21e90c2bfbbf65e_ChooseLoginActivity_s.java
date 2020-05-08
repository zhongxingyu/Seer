 package com.lollapaloozer.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.lollapaloozer.R;
 import com.lollapaloozer.auth.client.AuthDemoModel;
 import com.lollapaloozer.auth.client.AuthProvider;
 import com.ratethisfest.shared.Constants;
 
 public class ChooseLoginActivity extends Activity implements OnClickListener {
 
   private boolean _debugMode = false;
 
   // Framework
   private TextView _loginStatusText;
   private TextView _accountNameText;
   private TextView _tokenIdText;
 
   private Button _buttonLoginFacebookWeb;
   private Button _buttonInvalidateTokens;
   private Button _buttonDismissActivity;
 
   // App
   private AuthDemoModel _model;
   private boolean _firstStart = true;
 
   private static final int SWIPE_MIN_DISTANCE = 120;
   private static final int SWIPE_MAX_OFF_PATH = 250;
   private static final int SWIPE_THRESHOLD_VELOCITY = 200;
   private GestureDetector _gestureDetector;
   private View.OnTouchListener _gestureListener;
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.auth_choose_login);
     System.out.println("OnCreate starting");
 
     // Framework
     _loginStatusText = (TextView) this.findViewById(R.id.text_login_status);
     _accountNameText = (TextView) this.findViewById(R.id.text_account_name);
     _tokenIdText = (TextView) this.findViewById(R.id.text_token_id);
 
     findViewById(R.id.btn_login_twitter).setOnClickListener(this);
     findViewById(R.id.btn_login_google).setOnClickListener(this);
 
     // Need references to these buttons so they can disappear
     findViewById(R.id.btn_login_facebook).setOnClickListener(this);
 
     _buttonLoginFacebookWeb = (Button) findViewById(R.id.btn_login_facebook_browser);
     _buttonLoginFacebookWeb.setOnClickListener(this);
 
     _buttonInvalidateTokens = (Button) findViewById(R.id.btn_invalidate_tokens);
     _buttonInvalidateTokens.setOnClickListener(this);
     _buttonDismissActivity = (Button) findViewById(R.id.btn_dismiss_activity);
     _buttonDismissActivity.setOnClickListener(this);
 
     _gestureDetector = new GestureDetector(new MyGestureDetector());
     _gestureListener = new View.OnTouchListener() {
       public boolean onTouch(View v, MotionEvent event) {
         return _gestureDetector.onTouchEvent(event);
       }
     };
 
     findViewById(android.R.id.content).setOnClickListener(ChooseLoginActivity.this);
     findViewById(android.R.id.content).setOnTouchListener(_gestureListener);
 
     // App
     _model = new AuthDemoModel(this);
     _model.checkAccounts();
     System.out.println("OnCreate complete");
 
   }
 
   @Override
   protected void onResume() {
     super.onResume();
 
     AuthProvider currentProvider = _model.getCurrentAuthProvider();
     if (currentProvider != null) {
       currentProvider.extendAccess();
     }
 
     if (_firstStart) {
       System.out.println("ChooseLoginActivity First Launch, invalidating all logins");
       _model.invalidateTokens();
       _firstStart = false;
     }
 
     _updateUI();
   }
 
   private void _returnToMainActivity() {
     Intent returnIntent = getIntent();
 
    returnIntent.putExtra(Constants.INTENT_EXTRA_LOGIN_TYPE, _model.getCurrentAuthProviderType());
 
    returnIntent.putExtra(Constants.INTENT_EXTRA_ACCOUNT_IDENTIFIER, _model
        .getCurrentAuthProvider().getVerifiedAccountIdentifier());
 
    returnIntent.putExtra(Constants.INTENT_EXTRA_LOGIN_TOKEN, _model.getCurrentAuthProvider()
        .getAuthToken());
 
    int result;
    if (_model.isLoggedIn()) {
      result = RESULT_OK;
     } else {
       result = RESULT_CANCELED;
     }
 
     setResult(result, returnIntent);
     finish();
   }
 
   private void _updateUI() {
     if (_debugMode) {
       _accountNameText.setVisibility(View.VISIBLE);
       _tokenIdText.setVisibility(View.VISIBLE);
       _buttonInvalidateTokens.setVisibility(View.VISIBLE);
       _buttonDismissActivity.setVisibility(View.VISIBLE);
 
       if (_model.isLoggedIn()) {
         AuthProvider currentProvider = _model.getCurrentAuthProvider();
         _setLoginStatus(currentProvider.getAccountType());
         _accountNameText.setText("Account: " + currentProvider.getLocalAccountName() + "\r\n"
             + currentProvider.getAccountType() + " confirms you are the owner of "
             + currentProvider.getVerifiedAccountIdentifier());
         _tokenIdText.setText("Token ID: " + currentProvider.getAuthToken());
       } else {
         _setLoginStatus("Not Logged In");
         _accountNameText.setText("-");
         _tokenIdText.setText("-");
       }
 
       if (this.findViewById(R.layout.auth_choose_login) != null) {
         this.findViewById(R.layout.auth_choose_login).invalidate();
       }
 
     } else {
       _loginStatusText.setText("");
       // _loginStatusText.setVisibility(View.GONE);
 
       _accountNameText.setVisibility(View.GONE);
       _tokenIdText.setVisibility(View.GONE);
       _buttonInvalidateTokens.setVisibility(View.GONE);
       _buttonDismissActivity.setVisibility(View.GONE);
       if (_model.isLoggedIn()) {
         _returnToMainActivity();
       }
 
     }
 
   }
 
   private void _setLoginStatus(String status) {
     _loginStatusText.setText(Constants.UI_STR_LOGIN_STATUS + " " + status);
   }
 
   // Default is For Facebook
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
 
     System.out.println("ChooseLoginActivity.onActivityResult(requestCode=[" + requestCode
         + "], resultCode=[" + resultCode + "] with data: " + data);
 
     switch (requestCode) {
     case Constants.INTENT_REQ_TWITTER_LOGIN:
       if (resultCode == Activity.RESULT_OK) {
         _model.twitterAuthCallback(requestCode, resultCode, data);
         break;
       }
     default:
 
       System.out.println("onACtivityResult called with unknown values: " + requestCode + ","
           + resultCode);
       _model.getFacebookObject().authorizeCallback(requestCode, resultCode, data);
       break;
     }
   }
 
   public void modelChanged() {
     // Run method in main UI thread
     View v = findViewById(android.R.id.content);
     v.post(new Runnable() {
       public void run() {
         _updateUI();
       }
     });
 
     // _updateUI();
   }
 
   public void showErrorDialog(String title, String problem, String details) {
     String errorString = problem + "\r\n\r\nDetails:\r\n" + details;
     System.out.println(errorString);
 
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setTitle(title);
     builder.setMessage(errorString).setCancelable(true)
         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
           }
         })
     // todo .setIcon(R.)
 
     // .setNegativeButton("No", new DialogInterface.OnClickListener() {
     // public void onClick(DialogInterface dialog, int id) {
     // }
     // })
     ;
     AlertDialog alert = builder.create();
     alert.show();
   }
 
   @Override
   public void onClick(View arg0) {
     String buttonClickedName = this.getResources().getResourceEntryName(arg0.getId());
     System.out.println("Button Click: " + buttonClickedName);
 
     if (buttonClickedName.equals(this.getResources().getResourceEntryName(R.id.btn_login_google))) {
       _model.loginToGoogle(); // UI Update done on callback
     }
     if (buttonClickedName.equals(this.getResources().getResourceEntryName(R.id.btn_login_twitter))) {
       _model.loginToTwitter();
 
     }
     if (buttonClickedName.equals(this.getResources().getResourceEntryName(R.id.btn_login_facebook))) {
       _model.loginToFacebook();
 
     }
 
     if (buttonClickedName.equals(this.getResources().getResourceEntryName(
         R.id.btn_login_facebook_browser))) {
       _model.loginToFacebookBrowser();
     }
 
     if (buttonClickedName.equals(this.getResources().getResourceEntryName(
         R.id.btn_invalidate_tokens))) {
       _model.invalidateTokens();
       _updateUI();
     }
 
     if (buttonClickedName.equals(this.getResources()
         .getResourceEntryName(R.id.btn_dismiss_activity))) {
       _returnToMainActivity();
     }
   }
 
   class MyGestureDetector extends SimpleOnGestureListener {
     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
       try {
         if (_debugMode == true) {
           return true;
         }
 
         if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
           return false;
         }
 
         if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
             && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {// Left Swipe
           _loginStatusText.setText(_loginStatusText.getText() + "L");
         } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
             && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {// Right Swipe
           _loginStatusText.setText(_loginStatusText.getText() + "R");
         }
 
       } catch (Exception e) {
         // nothing
       }
 
       if (_loginStatusText.getText().equals("LRLR")) {
         _debugMode = true;
         _updateUI();
       }
 
       return true;
     }
 
   }
 
 }
