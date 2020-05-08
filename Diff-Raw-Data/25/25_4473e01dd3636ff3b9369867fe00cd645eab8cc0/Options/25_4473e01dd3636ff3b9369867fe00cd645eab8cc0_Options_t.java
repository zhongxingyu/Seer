 package com.example.zootypers.ui;
 
 
 
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RelativeLayout;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import com.example.zootypers.R;
 import com.example.zootypers.core.MultiLeaderBoardModel;
 import com.example.zootypers.core.SingleLeaderBoardModel;
 import com.example.zootypers.util.InternetConnectionException;
 import com.parse.Parse;
 import com.parse.ParseUser;
 
 /**
  *
  * UI / Activity for options screen.
  * @author cdallas
  *
  */
 public class Options extends Activity {
 
   LoginPopup lp;
   ParseUser currentUser;
   private int useTestDB;
 
   @Override
   protected final void onCreate(final Bundle savedInstanceState) {
 	  super.onCreate(savedInstanceState);
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
       setContentView(R.layout.activity_options);
       lp = new LoginPopup(currentUser);
 
       //Vibration listener
       Switch mySwitch = (Switch) findViewById(R.id.vibrate);
       setCorrectPosition(mySwitch, "vibrate.txt");
       //attach a listener to check for changes in state
       mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
               if(!isChecked){
                   deleteFile("vibrate.txt");
               }else{
                   try {
                       FileOutputStream fos = openFileOutput("vibrate.txt", Context.MODE_PRIVATE);
                       fos.write(0);
                       fos.close();
                   } catch (IOException e){
                       Log.e("Options.java", "vibrate.txt", e);
                   }
               }
           }
       });
       
       //BGM listener
       mySwitch = (Switch) findViewById(R.id.bgm);
       setCorrectPosition(mySwitch, "bgm.txt");
       //attach a listener to check for changes in state
       mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
               if(!isChecked){
                   deleteFile("bgm.txt");
               }else{
                   try {
                       FileOutputStream fos = openFileOutput("bgm.txt", Context.MODE_PRIVATE);
                       fos.write(0);
                       fos.close();
                   } catch (IOException e){
                       Log.e("Options.java", "bgm.txt", e);
                   }
               }
           }
       });
 
   }
 
   @Override
   public final boolean onCreateOptionsMenu(final Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.options, menu);
     return true;
   }
 
   /**
    * Clears the single player leaderboard.
    * @param view The button clicked.
    */
   public final void clearSingle(final View view) {
     SingleLeaderBoardModel sl = new SingleLeaderBoardModel(this.getApplicationContext());
     sl.clearLeaderboard();
     final String title = "Cleared Leaderboard";
     final String message = "The single player leaderboard has been successfully cleared.";
     buildAlertDialog(title, message);
   }
 
   /**
    * Clears the multiplayer leaderboard.
    */
   public final void clearMulti(final View view) {
       useTestDB = getIntent().getIntExtra("Testing", 0);
       Log.e("Extra", "INTENT " + useTestDB);
       // Initialize the database
       if (useTestDB == 1) {
           Parse.initialize(this, "E8hfMLlgnEWvPw1auMOvGVsrTp1C6eSoqW1s6roq",
                   "hzPRfP284H5GuRzIFDhVxX6iR9sgTwg4tJU08Bez"); 
       } else {
           Parse.initialize(this, "Iy4JZxlewoSxswYgOEa6vhOSRgJkGIfDJ8wj8FtM",
                   "SVlq5dqYQ4FemgUfA7zdQvdIHOmKBkc5bXoI7y0C"); 
       }
       currentUser = ParseUser.getCurrentUser();
       if (currentUser == null) {
           buildPopup(false);
           Log.i("Options", "user begins logging in");
       } else {
           MultiLeaderBoardModel ml;
           try {
               ml = new MultiLeaderBoardModel();
               ml.setPlayer(currentUser.getString("username"));
           } catch (InternetConnectionException e) {
               Log.i("Leaderboard", "triggering internet connection error screen");
               Intent intent = new Intent(this, ErrorScreen.class);
              intent.putExtra("error", R.layout.activity_connection_error_lb);
               startActivity(intent);
               return;
           }
           Log.i("Options", "begin clearing multi player leader board");
           ml.clearLeaderboard();
           final String title = "Cleared Leaderboard";
           final String message = "Your multiplayer scores have been successfully cleared.";
           buildAlertDialog(title, message);
       }
   }
 
   /**
    * Called when the user clicks the "Main Menu" button.
    * @param view The button clicked
    */
   public final void goToTitlePage(final View view) {
     Intent intent = new Intent(this, TitlePage.class);
     startActivity(intent);
   }
 
   private void buildPopup(boolean dismisspsw) {
     // set up the layout inflater to inflate the popup layout
     LayoutInflater layoutInflater =
     (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
 
     // the parent layout to put the layout in
     ViewGroup parentLayout = (ViewGroup) findViewById(R.id.options_layout);
 
     // inflate either the login layout
     lp.buildLoginPopup(layoutInflater, parentLayout, dismisspsw);
   }
   
   /**
    * Handles what happens when user clicks the "Forgot your password" link
    * @param view Button that is pressed
    */
   public final void forgotPassword(View view) {
 	  Log.i("Options", "user has forgotten password");
     // set up the layout inflater to inflate the popup layout
     LayoutInflater layoutInflater =
     (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
 
     // the parent layout to put the layout in
     ViewGroup parentLayout = (ViewGroup) findViewById(R.id.options_layout);
 
     // inflate the password layout
     lp.buildResetPopup(layoutInflater, parentLayout);
   }
   
   /**
    * Handles what happens when user clicks the login button
    * @param view Button that is pressed
    */
   public void loginButton(final View view) {
     // Try to login
     String usernameString;
 	try {
 		usernameString = lp.loginButton();
 	} catch (InternetConnectionException e) {
         Log.i("Options", "triggering internet connection error screen");
 		Intent intent = new Intent(this, ErrorScreen.class);
		intent.putExtra("error", R.layout.activity_connection_error_lb);
 		startActivity(intent);
 		return;
 	}
     // If login was successful, go to the multiplayer game
     if (!usernameString.equals("")) {
       exitLoginPopup(view);
       clearMulti(view);
       Log.i("Options", "user has logged in");
     }
   }
 
   /**
    * Exits the login popup window
    * @param view the button clicked
    */
   public void exitLoginPopup(View view) {
     Log.i("Options", "user exits log in");
     lp.exitLoginPopup();
   }
 
   /**
    * Exits the password popup window
    * @param view the button clicked
    */
   public void exitPasswordPopup(View view) {
 	Log.i("Options", "user exits password popup");
     buildPopup(true);
   }
   
   /**
    * Logs out the current user
    * @param view the button clicked
    */
   public void logoutUser(View view) {
 	Log.i("Options", "user has logged out");
     // Log the user out
     ParseUser.logOut();
     currentUser = ParseUser.getCurrentUser();
     
     // Display appropriate message / etc
     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
     lp.logoutUser(alertDialogBuilder);
     makeViewsInvisible();
   }
 
   /**
    * Handles what happens when user wants to reset password.
    * @param view the button clicked
    */
   public void resetPassword(View view) {
     Log.i("Options", "user resets password");
     // Sort through the reset info
     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
     lp.resetPassword(alertDialogBuilder);   
     // Go back to the login popup
     buildPopup(true);
   }
 
   //Set the switch to the correct position
   private final void setCorrectPosition(Switch mySwitch, String fileName){
       try{
           FileInputStream fis = openFileInput(fileName);
           mySwitch.setChecked(true);
       } catch (IOException e){
           mySwitch.setChecked(false);
       }
   }
   
   /**
    * Goes to the Registration page
    * @param view the button clicked
    */
   public void goToRegister(View view) {
     Intent registerIntent = new Intent(this, RegisterPage.class);
     startActivity(registerIntent);
   }
 
   /**
    * helper method that sets the logged in status views to invisible
    */
   private void makeViewsInvisible() {
     // get all the logged in related views
     RelativeLayout loginBox = (RelativeLayout) findViewById(R.id.title_log_info);
     TextView loggedInText = (TextView) findViewById(R.id.loggedin_text);
     TextView currentUserText = (TextView) findViewById(R.id.current_user_text);
     Button logoutButton = (Button) findViewById(R.id.logout_button);
 
     // set the views to be invisible
     loginBox.setVisibility(View.INVISIBLE);
     loggedInText.setVisibility(View.INVISIBLE);
     currentUserText.setVisibility(View.INVISIBLE);
     logoutButton.setVisibility(View.INVISIBLE);
   }
   
   
 
 
   // TODO remove repetition from title page / post game
   /**
    * builds an AlertDialog popup with the given title and message
    * @param title String representing title of the AlertDialog popup
    * @param message String representing the message of the AlertDialog
    * popup
    */
   private void buildAlertDialog(String title, String message) {
     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
     // set title
     alertDialogBuilder.setTitle(title);
 
     // set dialog message
     alertDialogBuilder
     .setMessage(message)
     .setCancelable(false)
     .setPositiveButton("Close", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
         // if this button is clicked, close the dialog box
         dialog.cancel();
       }
     });
 
     // create alert dialog
     AlertDialog alertDialog = alertDialogBuilder.create();
 
     // show the message
     alertDialog.show();
   }
 
 }
