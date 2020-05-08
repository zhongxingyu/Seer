 package edu.incense.android.ui;
 
 import java.util.UUID;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.incense.android.R;
 import edu.incense.android.session.SessionService;
 import edu.incense.android.survey.SurveyService;
 
 /**
  * Activity where the user can start a recording session by pressing the start
  * button
  * 
  * @author Moises Perez (mxpxgx@gmail.com)
  * @since 2011/04/28?
  * @version 1.7 2011/05/31
  */
 
 public class RecordActivity extends MainMenuActivity {
 
     // UI elements
     private ProgressDialog progressDialog = null;
     private TextView statusTextView;
     private TextView usernameTextView;
     private Button startButton;
 
     private String username = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.record);
 
         // Get UI elements
         usernameTextView = (TextView) findViewById(R.id.textview_username);
         statusTextView = (TextView) findViewById(R.id.textview_status);
         startButton = (Button) findViewById(R.id.button_start);
 
         // Initialize username and usernameEditText according to the
         // SharedPreferences
         updateUsernameFromPrefs();
 
         // Set the instructions text
         statusTextView.setText(getResources().getText(
                 R.string.record_instructions));
 
         // Add click listener in START button
         startButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 // Verify is a valid username
                 if (username == null || !(username.length() > 0)) {
                     // Visible notice to the user
                     Toast.makeText(
                             RecordActivity.this,
                             getResources()
                                     .getText(R.string.no_username_message),
                             Toast.LENGTH_LONG).show();
 
                 } else {
                     /*** START RECORDING SESSION ***/
                     startSession();
                 }
             }
         });
 
     }
 
     /**
      * Get the username from SharedPreferences
      */
     private String getUsernameFromPrefs() {
         SharedPreferences sp = PreferenceManager
                 .getDefaultSharedPreferences(getApplicationContext());
         return sp.getString("editTextUsername", "Unknown");
     }
 
     /**
      * Update EditText with the username from SharedPreferences (if necessary)
      */
     private void updateUsernameFromPrefs() {
         this.username = getUsernameFromPrefs();
         usernameTextView.setText(this.username);
     }
 
     /*** Overridden methods from Activity ***/
 
     // Called at the start of the visible lifetime.
     @Override
     public void onStart() {
         super.onStart();
         // Apply any required UI change now that the Activity is visible.
         updateUsernameFromPrefs();
     }
 
     // Called at the end of the active lifetime.
     @Override
     public void onPause() {
         // Suspend UI updates, threads, or CPU intensive processes
         // that don't need to be updated when the Activity isn't
         // the active foreground activity.
         super.onPause();
         unregisterReceiver(sessionCompleteReceiver);
         suspendRecordingSession();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         resetUI();
         
         IntentFilter filter = new IntentFilter();
         filter.addAction(SessionService.SESSION_USER_ACTION_COMPLETE);
         registerReceiver(sessionCompleteReceiver, filter);
     }
 
     /*** BROADCAST_RECEIVER ***/
     private BroadcastReceiver sessionCompleteReceiver = new BroadcastReceiver() {
         
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().compareTo(
                     SessionService.SESSION_USER_ACTION_COMPLETE) == 0) {
                 if (actionId == intent.getLongExtra(
                         SessionService.ACTION_ID_FIELDNAME, 0)) {
                     suspendRecordingSession();
                     Toast.makeText(RecordActivity.this,
                             getString(R.string.session_completed_message),
                             Toast.LENGTH_LONG).show();
                     startResultsActivity();
                 }
             }
         }
 
     };
 
     /*** RECORDING SESSION ***/
     private long actionId;
 
     /**
      * Start recording session and the thread from this class, show the progress
      * dialog
      */
     private void startSession() {
         startButton.setEnabled(false);
 
         // Show progress dialog
 //        Resources res = getResources();
 //        progressDialog = ProgressDialog.show(this,
 //                res.getText(R.string.session_title),
 //                res.getText(R.string.session_active_message));
 //        // Start service for it to run the recording session
 //        Intent sessionServiceIntent = new Intent(this, SessionService.class);
 //        // Point out this action was triggered by a user
 //        sessionServiceIntent.setAction(SessionService.SESSION_ACTION);
 //        // Send unique id for this action
 //        actionId = UUID.randomUUID().getLeastSignificantBits();
 //        sessionServiceIntent.putExtra(SessionService.ACTION_ID_FIELDNAME,
 //                actionId);
 //        startService(sessionServiceIntent);
         
         // Start service for it to run the recording session
         Intent surveyIntent = new Intent(this, SurveyService.class);
         // Point out this action was triggered by a user
         surveyIntent.setAction(SurveyService.SURVEY_ACTION);
         // Send unique id for this action
         actionId = UUID.randomUUID().getLeastSignificantBits();
         surveyIntent.putExtra(SurveyService.ACTION_ID_FIELDNAME,
                 actionId);
         this.startService(surveyIntent);
     }
 
     /**
      * Suspend recording session and the thread from this class, dismiss the
      * progress dialog
      */
     private void suspendRecordingSession() {
         resetUI();
     }
 
     /**
      * Remove progress dialog and enable the start button again
      */
     private void resetUI() {
         if (progressDialog != null) {
             if (progressDialog.isShowing())
                 progressDialog.dismiss();
             progressDialog = null;
         }
 
         if (!startButton.isEnabled())
             startButton.setEnabled(true);
     }
     
     private void startResultsActivity(){
         Intent intent = new Intent(this, ResultsListActivity.class);
         startActivity(intent);
     }
 }
