 package com.cmolls.thefactsoflife;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class TodaysFact extends Activity {
 
     //todo: prevent incrementation on rotation
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("TheFactsOfLife", MODE_PRIVATE);
        int factNumber = sharedPreferences.getInt("factNumber", 0);
 
        setContentView(R.layout.activity_todaysfact);
        String[] listOfFacts = getResources().getStringArray(R.array.factlist);
        TextView fact = (TextView) findViewById(R.id.tv_list_of_facts);
         if(factNumber >= listOfFacts.length) factNumber = 0;
         fact.setText(listOfFacts[factNumber]);
         factNumber = factNumber + 1;
         //chose to use apply, but commit could be more appropriate
         sharedPreferences.edit().putInt("factNumber", factNumber).apply();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.todays_fact, menu);
         return true;
     }
     
 }
