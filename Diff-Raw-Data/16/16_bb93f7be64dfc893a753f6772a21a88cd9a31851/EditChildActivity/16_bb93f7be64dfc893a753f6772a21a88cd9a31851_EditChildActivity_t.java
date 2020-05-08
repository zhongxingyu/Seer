 package com.feedme.activity;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.dao.JournalDao;
 import com.feedme.dao.NapDao;
 import com.feedme.model.Baby;
 
 import java.util.Calendar;
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:27 PM
  */
 public class EditChildActivity extends ChildActivity
 {
     private Button babyDob;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add_child);
 
         googleAnalyticsTracker.startNewSession(TRACKING_ID, this);
         googleAnalyticsTracker.trackPageView("/Edit-Child");
 
         final BabyDao babyDao = new BabyDao(getApplicationContext());
 
         final Baby editBaby = (Baby) getIntent().getSerializableExtra("baby");
 
         Log.d("BABY:EDIT:", editBaby.dump());
 
         final EditText babyName = (EditText) findViewById(R.id.babyName);
         final Spinner babySex = (Spinner) findViewById(R.id.babySex);
         final EditText babyHeight = (EditText) findViewById(R.id.babyHeight);
         final EditText babyWeight = (EditText) findViewById(R.id.babyWeight);
         final Button deleteBaby = (Button) findViewById(R.id.deleteBaby);
         babyDob = (Button) findViewById(R.id.babyDob);
 
         final ImageView babyImage = (ImageView) findViewById(R.id.babyPicture);

        /* Clear out the ImageView object reference in the event a User backed out of the Gallery or Camera Intents */
        if(editBaby.getPicturePath() != null && editBaby.getPicturePath().contains("android.widget.ImageView")){
            editBaby.setPicturePath("");
        }

         if (editBaby.getPicturePath() != null && !editBaby.getPicturePath().equals("")) {
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = 12;
             Bitmap bmImg = BitmapFactory.decodeFile(editBaby.getPicturePath(), options);
             babyImage.setImageBitmap(getResizedBitmap(bmImg, 75, 75, 90));
             babyImage.setMaxWidth(100);
             babyImage.setMaxHeight(100);
             babyImage.setMinimumWidth(100);
             babyImage.setMinimumHeight(100);
         } else {
             babyImage.setImageResource(R.drawable.babyicon);
             babyImage.setMaxWidth(100);
             babyImage.setMaxHeight(100);
             babyImage.setMinimumWidth(100);
             babyImage.setMinimumHeight(100);
         }
 
         styleActivity(editBaby.getSex());
 
         TextView addChild = (TextView) findViewById(R.id.addChild);
         addChild.setText("Edit Baby");
 
         deleteBaby.setVisibility(View.VISIBLE);
 
         Button addChildButton = (Button) findViewById(R.id.addChildButton);
         Button takePicture = (Button) findViewById(R.id.takePicture);
         Button selectPicture = (Button) findViewById(R.id.pickPicture);
 
         babyName.setText(editBaby.getName());
         babyHeight.setText(editBaby.getHeight());
         babyWeight.setText(editBaby.getWeight());
         babyDob.setText(editBaby.getDob());
 
         // add a click listener to the button
         babyDob.setOnClickListener(showDateDialog());
 
         // get the current date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
 
         // display the current date
         //updateDateDisplay();
 
         //populate male/female spinner
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.babySex, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         Spinner s = (Spinner) findViewById(R.id.babySex);
         s.setAdapter(adapter);
 
         //Set Spinner Value for Baby Sex
         if (editBaby.getSex().equals("Male")) {
             babySex.setSelection(0);
         } else {
             babySex.setSelection(1);
         }
         
         if (getIntent().getStringExtra("picturePath") != null)
         {
             editBaby.setPicturePath(getIntent().getStringExtra("picturePath"));
             babyName.setText(editBaby.getName());
             babyHeight.setText(editBaby.getHeight());
             babyWeight.setText(editBaby.getWeight());
             babyDob.setText(editBaby.getDob());
 
             //Set Spinner Value for Baby Sex
             if (editBaby.getSex().equals("Male"))
             {
                 babySex.setSelection(0);
             }
             else
             {
                 babySex.setSelection(1);
             }
 
         }
 
         //Take Picture Button
         takePicture.setOnClickListener(takePictureListener(editBaby.getId(), EDIT_CHILD_ACTIVITY_ID));
 
         //Select Picture Button
         selectPicture.setOnClickListener(selectPictureListener(editBaby.getId(), EDIT_CHILD_ACTIVITY_ID));
 
         //declare alert dialog
         final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 
         //Delete Button`
         Button deleteButton = (Button) findViewById(R.id.deleteBaby);
         deleteButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 deleteBaby(editBaby.getId(), editBaby.getName());
             }
         });
 
         // button listener for add child button
         addChildButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 //if name, weight, and height aren't filled out, throw an alert
                 if (babyName.getText().toString().equals("") || babyHeight.getText().toString().equals("") ||
                         babyWeight.getText().toString().equals(""))
                 {
                     alertDialog.setTitle("Oops!");
                     alertDialog.setMessage("Please fill out the form completely.");
                     alertDialog.setButton("OK", new DialogInterface.OnClickListener()
                     {
                         public void onClick(DialogInterface dialog, int which)
                         {
                             dialog.dismiss();
                         }
                     });
                     alertDialog.show();
                 }
                 else
                 {      
                     Baby updateBaby = new Baby(editBaby.getId(),
                             babyName.getText().toString(),
                             babySex.getSelectedItem().toString(),
                             babyHeight.getText().toString(),
                             babyWeight.getText().toString(),
                             babyDob.getText().toString(),
                             editBaby.getPicturePath());
 
                     Log.d("BABY:UPDATE: ", updateBaby.dump());
 
                     babyDao.updateBaby(updateBaby, editBaby.getId());
 
                     Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
 
                     Bundle b = new Bundle();
                     b.putSerializable("baby", updateBaby);
                     intent.putExtras(b);
 
                     startActivityForResult(intent, 3);
                 }
 
             }
         });
     }
 
     @Override
     protected Dialog onCreateDialog(int id)
     {
         switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                         mDateSetListener,
                         mYear, mMonth, mDay);
         }
         return null;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 
         switch (item.getItemId()) {
             case R.id.home:
                 startActivity(new Intent(EditChildActivity.this,
                         HomeActivity.class));
                 break;
             case R.id.settings:
                 startActivity(new Intent(EditChildActivity.this,
                         SettingsActivity.class));
                 break;
             case R.id.report:
                 startActivity(new Intent(EditChildActivity.this,
                         ReportBugActivity.class));
                 break;
         }
         return true;
     }
 
     private void deleteBaby(final String babyID, final String babyName) {
 
         AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(EditChildActivity.this);
         myAlertDialog.setTitle("Delete \"" + babyName + "\"?");
         myAlertDialog.setMessage("Are you sure?");
         myAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface arg0, int arg1) {
                 BabyDao babyDao = new BabyDao(getApplicationContext());
                 Baby baby = babyDao.getBaby(babyID);
                 babyDao.deleteBaby(baby, babyID);
                 JournalDao journalDao = new JournalDao(getApplicationContext());
                 journalDao.deleteEntry(babyID);
                 NapDao napDao = new NapDao(getApplicationContext());
                 napDao.deleteNap(babyID);
                 startActivity(new Intent(EditChildActivity.this,
                         HomeActivity.class));
             }
         });
         myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface arg0, int arg1) {
 
             }
         });
 
         myAlertDialog.show();
     }
 
     // updates the date we display in the TextView
     private void updateDateDisplay()
     {
         babyDob.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(mMonth + 1).append("-")
                         .append(mDay).append("-")
                         .append(mYear).append(" "));
     }
 
     // the callback received when the user "sets" the date in the dialog
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener()
             {
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth)
                 {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     updateDateDisplay();
                 }
             };
 
 }
 
