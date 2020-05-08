 package com.cs2340.spacetrader;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.support.v4.app.NavUtils;
 
 public class PlanetView extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_planet);
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
     
     public void startTradeView(View view){
     	Intent intent = new Intent(this, TradeView.class);
     	startActivity(intent);
     }
 
 }
