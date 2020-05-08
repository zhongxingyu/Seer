 package com.feedme.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.model.Baby;
 
 public class HomeActivity extends Activity {
 
     private static String TAG = "Feed-Me";
 
     /**
      * Called when the activity is first created.
      * @param savedInstanceState If the activity is being re-initialized after
      * previously being shut down then this Bundle contains the data it most 
      * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i(TAG, "onCreate");
         setContentView(R.layout.main);
 
         showBabies();
 
         //Add Settings Button
         Button addSettingsButton = (Button) findViewById(R.id.settings);
         addSettingsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                 startActivityForResult(intent, 2);
             }
         });
 
         // button listener for add child screen button
         Button addChildScreen = (Button)findViewById(R.id.addChildScreen);
         addChildScreen.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startActivity(new Intent(HomeActivity.this,
                         AddChildActivity.class));
             }
         });
 
        // button listener for add entry screen button
        Button addEntryScreen = (Button)findViewById(R.id.addEntryScreen);
        addEntryScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,
                        ViewEntriesActivity.class));
            }
        });
     }
 
 
     public void showBabies() {
         final BabyDao babyDao = new BabyDao(getApplicationContext());
         Baby[] myList = babyDao.getAllBabiesAsStringArray();
 
 
         LinearLayout ll = (LinearLayout)findViewById(R.id.babyScroll);
 
         int j = 0;
         while (j < myList.length) {
             TableLayout tl = new TableLayout(this);
             TableRow tr = new TableRow(this);  //create new row
             TableRow tr2 = new TableRow(this);  //create new row
             TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT);
             tableRowParams.setMargins(5,5,5,5);
             tl.setLayoutParams(tableRowParams);
             tr.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
             tr2.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             final Button b = new Button(this);                    //create button for child
 
             if (myList[j].getSex().equals("Male")) {     //change bg color of row and button
                  b.setBackgroundColor(0xFF7ED0FF);
             } else {
                  b.setBackgroundColor(0xFFFF99CC);
             }
 
             b.setText(myList[j].getName());                     //put child's name on button
             b.setTextColor(0xFFFFFFFF);
             b.setLayoutParams(new TableRow.LayoutParams(        //set params of button
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
             b.setWidth(100);
             b.setHeight(35);
 
             final ImageView babyImage = new ImageView(this);
 
             if (myList[j].getPicturePath() != null && !myList[j].getPicturePath().equals("")) {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inSampleSize = 12;
                 Bitmap bmImg = BitmapFactory.decodeFile(myList[j].getPicturePath(), options);
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
 
             //button listener for each baby
             b.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                     intent.putExtra("babyName", b.getText());
                     startActivityForResult(intent, 3);
                 }
             });
 
             /* Add table to linearlayout */
             ll.addView(tl);
 
             /* Add rows to table */
             tr.addView(b);
             tr2.addView(babyImage);
 
             /* Add row to TableLayout. */
             tl.addView(tr,new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
             tl.addView(tr2,new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++; //iterator
         }
     }
 
     private Bitmap getResizedBitmap(Bitmap bitMap, int newHeight, int newWidth, int rotateInDegrees) {
 
         int width = bitMap.getWidth();
         int height = bitMap.getHeight();
         float scaleWidth = ((float) newWidth) / width;
         float scaleHeight = ((float) newHeight) / height;
 
         // create a matrix for the manipulation
         Matrix matrix = new Matrix();
 
         // resize the bit map
         matrix.postScale(scaleWidth, scaleHeight);
         matrix.postRotate(rotateInDegrees);
 
         // recreate the new Bitmap
         Bitmap resizedBitmap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, false);
 
         return resizedBitmap;
     }
     
     @Override
     public void onBackPressed() {
         new AlertDialog.Builder(this)
                 .setTitle("Exit?")
                 .setMessage("Are you sure you want to exit?")
                 .setNegativeButton(android.R.string.no, null)
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 
                     public void onClick(DialogInterface arg0, int arg1) {
                         HomeActivity.super.onBackPressed();
                     }
                 }).create().show();
     }
 }
