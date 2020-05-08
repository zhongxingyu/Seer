 package com.feedme.activity;
 
 import android.app.*;
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
 import com.feedme.model.Baby;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:27 PM
  */
 public class AddChildActivity extends ChildActivity
 {
     private Button babyDob;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add_child);
 
         googleAnalyticsTracker.startNewSession(TRACKING_ID, this);
         googleAnalyticsTracker.trackPageView("/Add-Child");
 
         final BabyDao babyDao = new BabyDao(getApplicationContext());
 
         final Baby baby = (Baby) getIntent().getSerializableExtra("baby");
 
         // button listener for add child button
         final EditText babyName = (EditText) findViewById(R.id.babyName);
         final Spinner babySex = (Spinner) findViewById(R.id.babySex);
         final EditText babyHeight = (EditText) findViewById(R.id.babyHeight);
         final EditText babyWeight = (EditText) findViewById(R.id.babyWeight);
 
         final ImageView babyImage = (ImageView) findViewById(R.id.babyPicture);
 
         if (baby != null)
         {
             if (baby.getPicturePath() != null && !baby.getPicturePath().equals(""))
             {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inSampleSize = 12;
                 Bitmap bmImg = BitmapFactory.decodeFile(baby.getPicturePath(), options);
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
         }
 
         Button addChildButton = (Button) findViewById(R.id.addChildButton);
         Button takePicture = (Button) findViewById(R.id.takePicture);
         Button selectPicture = (Button) findViewById(R.id.pickPicture);
 
         babyDob = (Button) findViewById(R.id.babyDob);
 
         // add a click listener to the button
         babyDob.setOnClickListener(showDateDialog());
 
         // get the current date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
 
         // display the current date
         updateDateDisplay();
 
         //populate male/female spinner
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.babySex, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         Spinner s = (Spinner) findViewById(R.id.babySex);
         s.setAdapter(adapter);
 
         //In the even that the user clicked Take Picture or Select Picture and fired off a new Intent from the Add
         // Child screen.
         if (getIntent().getStringExtra("picturePath") != null)
         {
             baby.setPicturePath(getIntent().getStringExtra("picturePath"));
             babyName.setText(baby.getName());
             babyHeight.setText(baby.getHeight());
             babyWeight.setText(baby.getWeight());
             babyDob.setText(baby.getDob());
 
             //Set Spinner Value for Baby Sex
            if (baby.getDob().equals("Male"))
             {
                 babySex.setSelection(0);
             }
             else
             {
                 babySex.setSelection(1);
             }
 
         }
 
         //Take Picture Button
         takePicture.setOnClickListener(takePictureListener(0, ADD_CHILD_ACTIVITY_ID));
 
         //Select Picture Button
         selectPicture.setOnClickListener(selectPictureListener(0, ADD_CHILD_ACTIVITY_ID));
 
         //declare alert dialog
         final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 
         //Add Child Button
         addChildButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
 
                 //if name, weight, and height aren't filled out, throw an alert
                 if (babyName.getText().toString().equals("") || babyHeight.getText().toString().equals("") ||
                         babyWeight.getText().toString().equals("")) {
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
                     Baby addBaby = new Baby(babyName.getText().toString(),
                                             babySex.getSelectedItem().toString(),
                                             babyHeight.getText().toString(),
                                             babyWeight.getText().toString(),
                                             babyDob.getText().toString(),
                                             baby.getPicturePath());
                     // Inserting baby
                     Log.d("Insert: ", "Inserting ..");
                     babyDao.addBaby(addBaby);
 
                     Log.d("BABY:ADD: ", addBaby.dump());
 
                     // Reading all babies
                     Log.d("Reading: ", "Reading all babies..");
                     List<Baby> babies = babyDao.getAllBabies();
 
                     for (Baby baby : babies) {
                         String log = "Id: " + baby.getId() + " ,Name: " + baby.getName() + " ,Sex: " + baby.getSex()
                                 + " ,Height: " + baby.getHeight() + " ,Weight: " + baby.getWeight() + " ," +
                                 "DOB: " + baby.getDob();
 
                         // Writing babies to log
                         Log.d("Name: ", log);
                     }
 
                     babyName.setText("");
                     babyHeight.setText("");
                     babyWeight.setText("");
                     babyDob.setText("");
 
                     Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                     startActivityForResult(intent, ADD_CHILD_ACTIVITY_ID);
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
                 startActivity(new Intent(AddChildActivity.this,
                         HomeActivity.class));
                 break;
             case R.id.settings:
                 startActivity(new Intent(AddChildActivity.this,
                         SettingsActivity.class));
                 break;
             case R.id.report:
                 startActivity(new Intent(AddChildActivity.this,
                         ReportBugActivity.class));
                 break;
         }
         return true;
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
 
