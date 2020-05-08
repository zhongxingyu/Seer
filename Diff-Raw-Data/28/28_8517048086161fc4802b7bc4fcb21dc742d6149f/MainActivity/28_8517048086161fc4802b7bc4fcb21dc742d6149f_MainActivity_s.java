 package de.ovgu.swe_projekt.socialdoc;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.*;
 
 /* known problem:
  * if the app crashes/is terminated and reopened several alarms later, it will NOT make
  * entries for those alarms in the probandencode.csv
  * we are probably not going to fix that problem
  * missing entries can be seen by the "Antwortzeit" and "Datum" entries anyway...
  * (yes we should fix it, but... ehhhh)
  */
 
 public class MainActivity extends Activity {
 
     protected Control _control;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // set up control
         _control = new Control(getSharedPreferences("PsyAppPreferences", 0));
         // check if a the probandencode.csv exists and create it (and a new user) if it doesn't
         // TODO: also check if user exists on show, not only on create!!
         if( _control.createCSV() ) {
             setContentView(R.layout.mainmenu);
             setButtonDisabled(R.id.goto_question_button, _control.wasLastQuestionAnswered());
             // todo: set alarm to next possible user time
         } else {
             _control.saveUserData();
             setContentView(R.layout.proband_code);
         }
     }
 
     @Override
     public void onBackPressed() {
         // do nothing.
     }
     @Override       //Fehlermeldungen
     protected Dialog onCreateDialog(int id){
         switch(id){
             case 10:
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                 builder.setCancelable(true);
                 builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         // User clicked OK button
                     }
                 });
 
                 AlertDialog dialog = builder.create();
                 dialog.show();
 
 
         }
 
 
         return super.onCreateDialog(id);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     /* todo: on alarm:
      * alarm has to be set so that it will trigger on next alarm time
      * write to csv, if lastWasAnswered is false
      * (saveUserInputToCSV(true, "-77", "-77", "-77"))
      * set lastWasAnswered to false (if it isn't already)
      */
 
     public void button_menu_to_quest(View view){
         setContentView(R.layout.question);
         // todo: generate the correct question text using information taken from UserData and display it
         // that means:
         // find out if the last answer was given yesterday or x days ago
         // find out what time the last answer was given at
         setQuestion("heute schon");
     }
     public void button_menu_to_time(View view){
         setContentView(R.layout.set_time);
         // set the spinners to the currently chosen user times (if there are any)
         int[] times = _control.getUserTimes();
         setSpinnerToTime(R.id.spinner_termin1, times[0]);
         setSpinnerToTime(R.id.spinner_termin2, times[1]);
         setSpinnerToTime(R.id.spinner_termin3, times[2]);
         setSpinnerToTime(R.id.spinner_termin4, times[3]);
     }
     public void button_question_ok(View view){
         String numContacts, numHours, numMinutes;
         numContacts = getEditText(R.id.editText_contacts);
         numHours = getEditText(R.id.editText_hours);
         numMinutes = getEditText(R.id.editText_minutes);
 
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
             setButtonDisabled(R.id.goto_question_button, _control.wasLastQuestionAnswered());
         } catch(IllegalStateException ex){
             // do nothing, wait for user to click on the button again
         }
     }
     public void button_time_ok(View view){
         // get times from spinners
         int[] times = new int[4];
         times[0] = getTimeFromSpinner(R.id.spinner_termin1);
         times[1] = getTimeFromSpinner(R.id.spinner_termin2);
         times[2] = getTimeFromSpinner(R.id.spinner_termin3);
         times[3] = getTimeFromSpinner(R.id.spinner_termin4);
         // and update the user data
         _control.updateUserTimes(times);
         _control.saveUserData();
         // then change to main menu
         setContentView(R.layout.mainmenu);
         setButtonDisabled(R.id.goto_question_button, _control.wasLastQuestionAnswered());
     }
     public void button_time_back(View view){
         setContentView(R.layout.mainmenu);
         setButtonDisabled(R.id.goto_question_button, _control.wasLastQuestionAnswered());
     }
     public void button_proband_ok(View view){
        String code = getEditText(R.id.editText1);
         // create probandencode from different parts
         // ToDo: check if all are actually characters
         // (and not numbers/other symbols)
        _control.newUser( code );
         setContentView(R.layout.set_time);
     }
 
     private String getEditText(int id){
         String returnText;
         try{
             EditText editText = (EditText) findViewById(id);
             returnText = editText.getText().toString();
         } catch(NullPointerException ex) {
             returnText = "";
         }
         return returnText;
     }
     private int getTimeFromSpinner(int id){
         String selected = "";
         try{
             Spinner spinner = (Spinner) findViewById(id);
             selected = spinner.getSelectedItem().toString();
         } catch( NullPointerException ex ){
             selected = "-77";
         }
         // parse the hour selected (we don't care about the minutes, those are 00 anyway)
         return Integer.parseInt(selected.split(":")[0]);
     }
 
     private void setButtonDisabled(int id, boolean disabled){
         try{
             ImageButton button = (ImageButton)findViewById(id);
             button.setEnabled(!disabled);
         } catch(NullPointerException ex){}
     }
     private void setSpinnerToTime(int id, int time){
         try{
             Spinner spinner = (Spinner) findViewById(id);
             int spinnerPosition = 0;
 
             if( time != -77 ){
                 try{
                     // get the position of the element we want to set to
                     ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
                     spinnerPosition = adapter.getPosition(time+":00");
                 } catch(NullPointerException ex){}
             }
             spinner.setSelection(spinnerPosition);
         } catch(NullPointerException ex){}
     }
 
     private void setQuestion(String time){
         String quest = "Mit wie vielen Interaktionspartnern hatten Sie ";
         quest+=time;
         quest+=" Kontakt? Schätzen Sie bitte zusätzlich die Gesamtdauer der Kontakte ein.";
 
 
         TextView text = (TextView) findViewById(R.id.question);
         text.setText(quest);
     }
 }
