 package com.haligali.PowerHangmanClient.Menus;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.LoginFilter;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Button;
 import android.widget.Toast;
 import android.widget.CompoundButton;
 import com.haligali.PowerHangmanClient.Networking.LoginTask;
 import com.haligali.PowerHangmanClient.Networking.MakeAccountTask;
 import com.haligali.PowerHangmanClient.Networking.Interfaces.OnLoginResponse;
 import com.haligali.PowerHangmanClient.Networking.ServerResponseCodes.ServerResponseCode;
 import com.haligali.PowerHangmanClient.PowerHangmanApp;
 import com.haligali.PowerHangmanClient.R;
 import junit.framework.Assert;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Paul
  * Date: 10/8/13
  * Time: 7:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SignInActivity extends Activity implements OnLoginResponse {
     // TODO make is so we don't lose our AsyncTasks
     // when the screen rotates, try headless fragments
 
     // View Handles
     /**
      * usernameEntry
      * The handle to the EditText on the screen
      * where user types in the username.
      */
     private EditText usernameEntry;
     /**
      * passwordEntry
      * The handle to the EditText on the screen
      * where user types in the password.
      */
     private EditText passwordEntry;
     /**
      * confirmPassword
      * The handle to the EditText on the screen
      * where user types in the password confirmation.
      */
     private EditText confirmPassword;
     /**
      *  singInButton
      *  The handle to the Button on the screen
      *  which user clicks to sign in.
      */
     private Button signInButton;
     /**
      *  needAccountCheckbox
      *  The handle to the Checkbox on the screen
      *  which user checks if the new account should be created.
      */
     private CheckBox needAccountCheckBox;
 
     /**
      * signInName
      * The initialization of the username.
      */
     private String signInName = "";
     /**
      * playerPrefs
      * The delcaration of the player preferences
      * which is to be set later.
      */
     private SharedPreferences playerPrefs;
 
     /**
      * pName
      * Load player's username.
      */
     private String pName = PowerHangmanApp.PLAYER_USERNAME_EKEY;
     /**
      * pId
      * Load player's id.
      */
     private String pId = PowerHangmanApp.PLAYER_ID_EKEY;
     /**
      * pref
      * Load player's preferences.
      */
     private String pref = PowerHangmanApp.PLAYER_PREFERENCES;
 
     @Override
     public final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // If the player ID is not stored as an empty string,
         // then we are already signed in and can
         // skip to the main menu.
 
         playerPrefs = getSharedPreferences(pref, Context.MODE_PRIVATE);
         if (playerPrefs.getString(pId, "").compareTo("") != 0) {
             goToMainMenu();
         }
 
         setContentView(R.layout.sign_in_layout);
 
         // Grab needed on screen View handles for later use.
         int uname = R.id.etx_UserNameEntry;
         int pw = R.id.etx_PasswordEntry;
         int cpw = R.id.etx_PasswordConfirm;
         int signIn = R.id.btn_SignAction;
         int check = R.id.chb_MakeAccount;
 
         usernameEntry = (EditText) findViewById(uname);
         passwordEntry = (EditText) findViewById(pw);
         confirmPassword = (EditText) findViewById(cpw);
         signInButton = (Button) findViewById(signIn);
         needAccountCheckBox = (CheckBox) findViewById(check);
 
         // Set input filters on username and password
         usernameEntry.setFilters(new InputFilter[]{
                 new LoginFilter.UsernameFilterGMail()});
         passwordEntry.setFilters(new InputFilter[]{
                 new LoginFilter.PasswordFilterGMail()});
        confirmPassword.setFilters(new InputFilter[]{
                new LoginFilter.PasswordFilterGMail()});
 
         // Set the logic for when someone selects
         // or deselects "I need an account"
         needAccountCheckBox.setOnCheckedChangeListener(
                 new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(final CompoundButton buttonView,
                                          final boolean isChecked) {
                 int makeAcc = R.string.SI_MakeAccountButtonLabel;
                 int signInBtn = R.string.SI_SignInButtonLabel;
 
                 if (isChecked) {
                     confirmPassword.setVisibility(View.VISIBLE);
                     signInButton.setText(makeAcc);
                 } else {
                     confirmPassword.setVisibility(View.GONE);
                     signInButton.setText(signInBtn);
                 }
             }
         });
     }
 
     /**
      * Logs in to the application when the checkbox is not checked.
      * When the checkbox is checked, creates a new account and logs in.
      * @param v The View input of LogIn button.
      */
     public final void onLogInClick(final View v) {
         signInName = usernameEntry.getText().toString();
         String password = passwordEntry.getText().toString();
 
 
         if (needAccountCheckBox.isChecked()) {
             // Making a new account
             String confPassword = confirmPassword.getText().toString();
 
             if (password.compareTo(confPassword) == 0) {
                 // Make sure the two passwords are the same...
                 new MakeAccountTask(this).execute(signInName, password);
                 lockDownScreen(true);
             } else {
                 // If the passwords are not the same..
                 int noMatch = R.string.SI_NoMatchPasswordsToast;
                 int length = Toast.LENGTH_SHORT;
                 Context app = getApplicationContext();
 
                 final Toast toast = Toast.makeText(app, noMatch, length);
                 toast.show();
 
                 // Clear the passwords for re-entry
                 passwordEntry.setText("");
                 confirmPassword.setText("");
             }
         } else {
             // We're just logging in
             new LoginTask(this).execute(signInName, password);
             lockDownScreen(true);
         }
     }
 
     @Override
     public final void receiveLoginResponse(final ServerResponseCode code,
                                      final String playerID) {
         int fail = R.string.SI_LogInFailedToast;
         int invalid = R.string.SI_UserNameInvalidToast;
         int length = Toast.LENGTH_SHORT;
         Context app = getApplicationContext();
 
         switch (code) {
             case LOG_IN_SUCCESSFUL:
             case ACCOUNT_CREATE_SUCCESSFUL:
                 final SharedPreferences.Editor newPlayerInfo =
                         playerPrefs.edit();
                 newPlayerInfo.putString(pId, playerID);
                 newPlayerInfo.putString(pName, signInName);
                 newPlayerInfo.commit();
 
                 goToMainMenu();
                 break;
 
             case LOG_IN_DENIED:
                 final Toast loginToast =
                         Toast.makeText(app, fail, length);
                 loginToast.show();
 
                 // Re-enable screen so user can try again.
                 lockDownScreen(false);
                 break;
 
             case ACCOUNT_CREATE_DENIED:
                 final Toast actCreateToast =
                         Toast.makeText(app, invalid, length);
                 actCreateToast.show();
 
                 // Re-enable screen so user can try again.
                 lockDownScreen(false);
                 break;
 
             case SERVER_TIMEOUT:
                 // Re-enable screen so user can try again.
                 lockDownScreen(false);
                 break;
 
             default:
                 Assert.fail("Received invalided LogInCode");
                 break;
         }
     }
 
     /**
      * Clears the username, password, and password confirmation entries.
      * @param enable Tells whether "clearing entries" is true.
      */
     private void lockDownScreen(final boolean enable) {
         final boolean enableViews = !enable;
 
         usernameEntry.setEnabled(enableViews);
         passwordEntry.setEnabled(enableViews);
         confirmPassword.setEnabled(enableViews);
         signInButton.setEnabled(enableViews);
         needAccountCheckBox.setEnabled(enableViews);
 
         if (enable) {
             // If we're enabling the views, clear them of any old user input
             usernameEntry.setText("");
             passwordEntry.setText("");
             confirmPassword.setText("");
         }
     }
 
     /**
      * Goes back to the Main Menu Screen.
      */
     private void goToMainMenu() {
         Context app = getApplicationContext();
         final Intent mainMenuIntent = new Intent(app, MainMenuActivity.class);
         startActivity(mainMenuIntent);
         finish();
     }
 }
