 package com.happykrappy.instacram;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.TextView;
 import android.util.Log;
 
 public class EditDeckActivity extends Activity {
 	
 	TextView name_tv;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_edit_deck);
         
         name_tv = (TextView) findViewById(R.id.deck_name);
         int DeckId;
         if (savedInstanceState == null) {
             if(getIntent().getExtras() == null) {
             	DeckId= 0;
             } else {
             	DeckId = Integer.parseInt(getIntent().getExtras().getString("DeckId"));
             }
         } else {
         	DeckId= Integer.parseInt((String) savedInstanceState.getSerializable("DeckId"));
         }
         
         Log.d(MainActivity.TAG, "viewDeck-DeckId: " + DeckId);
        name_tv.setText(DeckId);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_view_deck, menu);
         return true;
     }
 }
