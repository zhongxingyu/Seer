 package com.jperla.badge;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.jperla.badge.VCard;
 
 public class VCardActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
         String card = intent.getStringExtra("card");
 
         
         TextView textView = new TextView(this);
         textView.setText(card);
         textView.setTextSize(15);
         setContentView(textView);
     }
 }
