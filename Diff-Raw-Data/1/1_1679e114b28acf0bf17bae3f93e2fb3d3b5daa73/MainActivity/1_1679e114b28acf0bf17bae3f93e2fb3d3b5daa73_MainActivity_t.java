 package com.bettername.thepokemonone;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity
 {
     Context appContext = this;
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Button continueButton = (Button)findViewById(R.id.continue_button);
         continueButton.setOnClickListener(new OnClickListener(){
 
             @Override
             public void onClick(View v)
             {
                 Intent continueIntent = new Intent(appContext, CreaturePickActivity.class);
                 appContext.startActivity(continueIntent);
             }
         });
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("About")
                 .setMessage(
                         "It's an app! Developed by:\n"
                                 + "Kristian\n"
                                 + "Nolan\n"
                                 + "Robert\n" 
                                 + "Will"
                                 + "Holly"
                                 + "Kyle")
                 .setNeutralButton("OK",
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog,
                                     int which) {
                                 Intent continueIntent = new Intent(appContext, MapActivity.class);
                                 appContext.startActivity(continueIntent);
                             }
                         });
         builder.create().show();
         return true;
     }
 
     @Override
     protected void onPause()
     {
         // TODO Auto-generated method stub
         super.onPause();
     }
 
     @Override
     protected void onResume()
     {
         // TODO Auto-generated method stub
         super.onResume();
     }
 
     @Override
     protected void onStop()
     {
         // TODO Auto-generated method stub
         super.onStop();
     }
 }
