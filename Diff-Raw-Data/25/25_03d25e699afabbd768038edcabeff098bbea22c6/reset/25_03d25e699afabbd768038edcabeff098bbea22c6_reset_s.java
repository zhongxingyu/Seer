 package de.team06.psychoapp;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * Created by Kevin on 06.07.13.
  */
 public class reset extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.reset);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.reset, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId())
         {
             case R.id.reset : reset(); break;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     public void click (View view) {
         reset();
     }
 
     public void reset() {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
         SharedPreferences.Editor editor = preferences.edit();
         editor.clear();
         editor.commit();
         Toast.makeText(this.getApplicationContext(), "alles gelöscht", Toast.LENGTH_SHORT).show();
         finish();
     }
 }
