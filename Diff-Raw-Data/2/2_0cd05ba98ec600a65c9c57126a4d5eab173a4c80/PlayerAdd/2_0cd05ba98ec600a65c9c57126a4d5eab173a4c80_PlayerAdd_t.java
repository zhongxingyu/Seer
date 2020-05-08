 package com.github.joeljoly.tournament;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 
 /**
  * Created with IntelliJ IDEA.
  * User: joel
  * Date: 6/3/13
  * Time: 13:42 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PlayerAdd extends Activity {
     private EditText firstNameEdit;
     private EditText lastNameEdit;
     private EditText idEdit;
     private EditText pointsEdit;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.player_add);
         firstNameEdit = (EditText) findViewById(R.id.firstNameEdit);
         lastNameEdit = (EditText) findViewById(R.id.lastNameEdit);
         idEdit = (EditText) findViewById(R.id.idEdit);
         pointsEdit = (EditText) findViewById(R.id.pointEdit);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.player_add_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.player_add_validate:
                 Integer licenceNumber;
                 try
                 {
                     licenceNumber = Integer.valueOf(idEdit.getText().toString());
                 }
                 catch(NumberFormatException e)
                 {
                     displayError(R.string.invalid_player_licence_message);
                     idEdit.requestFocus();
                     return true;
                 }
                 String  firstName;
                 firstName = firstNameEdit.getText().toString();
                 if (firstName.isEmpty())
                 {
                     displayError(R.string.invalid_player_first_name_message);
                     firstNameEdit.requestFocus();
                     return true;
                 }
                 String  lastName;
                 lastName = lastNameEdit.getText().toString();
                 if (lastName.isEmpty())
                 {
                     displayError(R.string.invalid_player_last_name_message);
                     lastNameEdit.requestFocus();
                     return true;
                 }
                 Integer points;
                 String pointsAsString = pointsEdit.getText().toString();
                 if (pointsAsString.isEmpty())
                 {
                     pointsAsString = pointsEdit.getHint().toString();
                 }
                 try
                 {
                     points = Integer.valueOf(pointsAsString);
                 }
                 catch(NumberFormatException e)
                 {
                     displayError(R.string.invalid_player_points_message);
                     pointsEdit.requestFocus();
                     return true;
                 }
                 Player newPlayer;
                 newPlayer = new Player(licenceNumber, firstName, lastName, points);
                 TournamentDataDbHelper database;
                 database = new TournamentDataDbHelper(this);
                 if (database.addPlayer(newPlayer) >= 0)
                 {
                     Intent returnIntent;
                     returnIntent = new Intent();
                     returnIntent.putExtra("result", newPlayer);
                     setResult(RESULT_OK,returnIntent);
                     this.finish();
                 }
                 else
                 {
                     displayError(R.string.duplicate_player_message);
                 }
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void displayError(int resId)
     {
         displayError(this.getString(resId));
     }
     private void displayError(String message)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(message).setTitle(R.string.duplicate_player_title);
        // display an "OK" button with nothing to do on click
        builder.setPositiveButton(android.R.string.ok, null);
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 }
