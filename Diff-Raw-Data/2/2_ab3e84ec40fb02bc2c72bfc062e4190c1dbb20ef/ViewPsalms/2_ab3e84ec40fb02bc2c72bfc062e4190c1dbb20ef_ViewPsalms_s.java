 package com.smouring.android.psalterapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 
 import static com.smouring.android.psalterapp.Constants.*;
 
 /**
  * @author Stephen Mouring
  */
 public class ViewPsalms extends Activity {
 
   private int selectedPsalm;
 
   private boolean restore;
 
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.viewpsalms);
 
     System.setProperty("log.tag.1650ForAndroid", "INFO");
 
    int restoredSelectedPsalm = 150;
 
     if (savedInstanceState == null) {
       Log.i("1650ForAndroid", "No saved instance state.");
     } else {
       Log.i("1650ForAndroid", "Restoring saved instance state.");
       if (savedInstanceState.containsKey(SELECTED_PSALM_KEY)) {
         restoredSelectedPsalm = savedInstanceState.getInt(SELECTED_PSALM_KEY);
         Log.i("1650ForAndroid", "Found key: " + SELECTED_PSALM_KEY + " - " + restoredSelectedPsalm);
       } else {
         Log.e("1650ForAndroid", "No valid key stored in saved instance state!");
       }
     }
 
     restore = true;
 
     final int bookIndex = getBookForPsalm(restoredSelectedPsalm) - 1;
     Log.i("1650ForAndroid", "bookIndex - " + bookIndex);
     final int psalmIndex = (bookIndex == 0 ? (restoredSelectedPsalm - 1) : (restoredSelectedPsalm - 1) - BOOKS[bookIndex -1]);
     Log.i("1650ForAndroid", "psalmIndex - " + psalmIndex);
 
     Spinner chooseBook = (Spinner) findViewById(R.id.choosebook);
     ArrayAdapter<String> bookNameAdapter = new ArrayAdapter<String>(this, R.layout.psalmselector, BOOK_NAMES);
     bookNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     chooseBook.setAdapter(bookNameAdapter);
     chooseBook.setSelection(bookIndex);
     chooseBook.setOnItemSelectedListener(new OnItemSelectedListenerAdapter() {
       public void onItemSelected(AdapterView parent, View view, int pos, long id) {
         Log.i("1650ForAndroid", "chooseBook onItemSelectedListener fired.");
 
         int bookIndex = Integer.parseInt(parent.getItemAtPosition(pos).toString().split(" ")[1]) - 1;
 
         Spinner choosePsalm = (Spinner) findViewById(R.id.choosepsalm);
 
         ArrayAdapter<String> psalmNameAdapter = new ArrayAdapter<String>(ViewPsalms.this, R.layout.psalmselector, PSALM_NAMES[bookIndex]);
         psalmNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         choosePsalm.setAdapter(psalmNameAdapter);
         if (restore) {
           choosePsalm.setSelection(psalmIndex);
           restore = false;
         }
       }
     });
 
     Spinner choosePsalm = (Spinner) findViewById(R.id.choosepsalm);
     choosePsalm.setOnItemSelectedListener(new OnItemSelectedListenerAdapter() {
       public void onItemSelected(AdapterView parent, View view, int pos, long id) {
         Log.i("1650ForAndroid", "choosePsalm onItemSelectedListener fired.");
         selectedPsalm = Integer.parseInt(parent.getItemAtPosition(pos).toString().replace("Psalm ", ""));
         Log.i("1650ForAndroid", "selectedPsalm set to: [" + selectedPsalm + "]");
       }
     });
 
     Button selectButton = (Button) findViewById(R.id.select);
     selectButton.setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         Log.i("1650ForAndroid", "Launching intent for ViewPsalm activity.");
 
         Intent i = new Intent(ViewPsalms.this, ViewPsalm.class);
         i.putExtra(SELECTED_PSALM_KEY, selectedPsalm);
         startActivity(i);
       }
     });
   }
 
   public void onSaveInstanceState(Bundle savedInstanceState) {
     Log.i("1650ForAndroid", "onSaveInstanceState listener fired.");
     savedInstanceState.putInt(SELECTED_PSALM_KEY, selectedPsalm);
   }
 
   private int getBookForPsalm(int selectedPsalm) {
     for (int i = 0; i < BOOKS.length; ++i) {
       if (selectedPsalm <= BOOKS[i]) {
         return i+1;
       }
     }
     Log.e("1650ForAndroid", "Invalid selectedPsalm parameter!");
     throw new RuntimeException("Invalid selectedPsalm parameter!");
   }
 
   public class OnItemSelectedListenerAdapter implements OnItemSelectedListener {
     public void onItemSelected(AdapterView parent, View view, int pos, long id) {
     }
 
     public void onNothingSelected(AdapterView parent) {
     }
   }
 }
