 package de.ovgu.swe_projekt.socialdoc;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.text.format.Time;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * The main activity of this project.
  * This activity uses different views to implement all screens used in the app.
  */
 public class MainActivity extends Activity {
 
     /**
      * Control class handling the app's data.
      */
     protected Control _control;
     private AlertDialog dialog;
 
     /**
      * Create control class when app is created.
      * @param savedInstanceState standard android parameter
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         // set up control
         _control = new Control(getSharedPreferences("PsyAppPreferences", 0));
         super.onCreate(savedInstanceState);
         initAlert();
     }
 
     /**
      * Set the alarm and decide on which screen to show.
      * First run alarm
      * Then check if the file probandencode.csv exists
      * If it does: show the main menu
      * If it does not: have the user enter their Probandencode and create a new file for them
      * Update Alarm if we have to
      */
     @Override
     protected void onStart(){
         super.onStart();
 
         try
         {
             Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
             Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
             r.play();
             Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
             // Vibrate for 500 milliseconds
             v.vibrate(500);
         }catch (Exception e) {}
 
         // check if the probandencode.csv exists and create it (and a new user) if it doesn't
         if( _control.createCSV() ) {
             if(_control.probandenCodeIsEmpty()) {
                 setContentView(R.layout.proband_code);
             } else {
                 setContentView(R.layout.mainmenu);
                 Helper.setButtonDisabled((ImageButton)findViewById(R.id.goto_question_button),
                                          _control.wasLastQuestionAnswered());
             }
         } else {
             setContentView(R.layout.proband_code);
         }
 
         // only call this if the app was opened by an alarm!
         if( _control.isAlarmTime(SystemClock.elapsedRealtime()) )
             setAlarm(Helper.getTimeDifferenceInMillisecs(_control), true);
     }
 
     /**
      * Called when user presses smart phone's back button; Ignore it.
      */
     @Override
     public void onBackPressed() {
         // do nothing.
     }
 
     /**
      * Create error message.
      */
     protected void initAlert(){
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                 builder.setCancelable(true);
                 builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         // User clicked OK button
                     }
                 });
 
                 dialog = builder.create();
     }
 
     /**
      * Standard method
      * @param menu standard android parameter
      * @return always true
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
     /**
      * Set layout to question screen and set the correct question text.
      * @param view standard android parameter
      */
     public void button_menu_to_quest(View view){
         setContentView(R.layout.question);
         Helper.setQuestion(_control.generateQuestionText(), (TextView)findViewById(R.id.question));
     }
 
     /**
      * Set layout to time selection and make sure the spinners display the current user times.
      * @param view standard android parameter
      */
     public void button_menu_to_time(View view){
         setContentView(R.layout.set_time);
         // set the spinners to the currently chosen user times (if there are any)
         int[] times = _control.getUserTimes();
         Helper.setSpinnerToTime((Spinner)findViewById(R.id.spinner_termin1), times[0]);
         Helper.setSpinnerToTime((Spinner)findViewById(R.id.spinner_termin2), times[1]);
         Helper.setSpinnerToTime((Spinner)findViewById(R.id.spinner_termin3), times[2]);
         Helper.setSpinnerToTime((Spinner)findViewById(R.id.spinner_termin4), times[3]);
     }
 
     /**
      * Check question input, save input if it passes the test, set layout to main menu.
      * @param view standard android parameter
      */
     public void button_question_ok(View view){
         String numContacts, numHours, numMinutes;
         numContacts = Helper.getEditText((EditText)findViewById(R.id.editText_contacts));
         numHours = Helper.getEditText((EditText)findViewById(R.id.editText_hours));
         numMinutes = Helper.getEditText((EditText)findViewById(R.id.editText_minutes));
 
         // only 60 minutes per hour
        if( Integer.parseInt(numMinutes) > 59 )
             return;
         if( !_control.isQuestionInputOK(numHours+":"+numMinutes) ){
            // todo: display popup with warning
             return;
         }
         try{
             _control.saveUserInputToCSV(false, numContacts, numHours, numMinutes);
             _control.saveUserData();
 
             setContentView(R.layout.mainmenu);
             Helper.setButtonDisabled((ImageButton)findViewById(R.id.goto_question_button),
                                      _control.wasLastQuestionAnswered());
         } catch(IllegalStateException ex){
             // do nothing, wait for user to click on the button again
         }
     }
 
     /**
      * Get the new user times and save them, set alarm at next possible time, set next view.
      * Case 1 - User already answered at least one question: Set view to main menu.
      * Case 2 - This is the first time the user is choosing times, set the view to question.
      * @param view standard android parameter
      */
     public void button_time_ok(View view){
         // get times from spinners
         int[] times = new int[4];
         times[0] = Helper.getTimeFromSpinner((Spinner)findViewById(R.id.spinner_termin1));
         times[1] = Helper.getTimeFromSpinner((Spinner)findViewById(R.id.spinner_termin2));
         times[2] = Helper.getTimeFromSpinner((Spinner)findViewById(R.id.spinner_termin3));
         times[3] = Helper.getTimeFromSpinner((Spinner)findViewById(R.id.spinner_termin4));
         // and update the user data
         _control.updateUserTimes(times);
         // set the next alarm to the next possible time
         setAlarm(Helper.getTimeDifferenceInMillisecs(_control), false);
         _control.saveUserData();
         // then change to main menu if user did already answer at least one question, else to question
         if( _control.userAnsweredAQuestion() ) {
             setContentView(R.layout.mainmenu);
             Helper.setButtonDisabled((ImageButton)findViewById(R.id.goto_question_button),
                                      _control.wasLastQuestionAnswered());
         }
         else {
             setContentView(R.layout.question);
             Helper.setQuestion(_control.generateQuestionText(), (TextView)findViewById(R.id.question));
         }
     }
 
     /**
      * Cancel user time selection.
      * @param view standard android parameter
      */
     public void button_time_back(View view){
         setContentView(R.layout.mainmenu);
         Helper.setButtonDisabled((ImageButton)findViewById(R.id.goto_question_button),
                                  _control.wasLastQuestionAnswered());
     }
 
     /**
      * Check input for user's Probandencode for correctness, create new user using it if it passes.
      * @param view standard android parameter
      */
     public void button_proband_ok(View view){
         // create probandencode from different parts
         String codePart1 = Helper.getEditText((EditText)findViewById(R.id.pc_1));
         String codePart2 = Helper.getEditText((EditText)findViewById(R.id.pc_2));
         String codePart3 = Helper.getEditText((EditText)findViewById(R.id.pc_3));
         String codePart4 = Helper.getEditText((EditText)findViewById(R.id.pc_4));
         String codePart5 = Helper.getEditText((EditText)findViewById(R.id.pc_5));
        // ToDo: check if all are actually characters
         // (and not numbers/other symbols)
         String code = codePart1+codePart2+codePart3+codePart4+codePart5;
         char[] chars = code.toCharArray();
         boolean letters = true;
         for (char c : chars) 
             if(!Character.isLetter(c)) 
                 letters = false;            
         
         if(code.length()==5 && letters){
 	        _control.newUser( codePart1+codePart2+codePart3+codePart4+codePart5 );
 	        setContentView(R.layout.set_time);
         }else
             dialog.show();
     }
 
     /**
      * Sets a new alarm and takes care of affiliated tasks.
      * 1) Check if the last question was answered or if the user skipped it
      * 2) If the user did skip the last question, write the appropriate line to Probandencode.csv
      * 3) Set a new alarm to the time specified by timeDifference
      * @param timeDifference Time to next alarm (in milliseconds)
      * @param triggeredByAlarm Has this call been caused by an alarm?
      */
     private void setAlarm(long timeDifference, boolean triggeredByAlarm){
         if(triggeredByAlarm) {
             if (!_control.wasLastQuestionAnswered())
                 _control.saveUserInputToCSV(true,"","","");
             Time now = new Time(Time.getCurrentTimezone());
             now.setToNow();
             _control.changeLastAlarm(now);
         }
         Intent nextActivityIntent = new Intent(this, MainActivity.class);
 
         PendingIntent next = PendingIntent.getActivity(getApplicationContext(), 0, nextActivityIntent, 0);
 
         // alarmanager setzen
         AlarmManager alarmManager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
         alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeDifference, next);
         _control.setNextAlarmAt(SystemClock.elapsedRealtime() + timeDifference);
     }
 }
