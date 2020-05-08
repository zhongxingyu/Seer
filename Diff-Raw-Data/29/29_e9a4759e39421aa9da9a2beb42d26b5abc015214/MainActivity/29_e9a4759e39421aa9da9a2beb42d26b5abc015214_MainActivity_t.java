 package com.hyperactivity.android_app.activities;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.ScrollPicker;
 
 public class MainActivity extends Activity {
     private ScrollPicker scrollPicker;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
        View view = findViewById(R.id.forum_categories_surface_view);
         scrollPicker = (ScrollPicker) view;
         scrollPicker.getThread().setState(ScrollPicker.ScrollPickerThread.STATE_READY);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     /**
      * Triggers when an item in the menu is clicked such as "Settings" or "Help"
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_settings:
                 // Settings has been clicked, check the android version to decide if to use fragmented settings or not
                 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                     startActivity(new Intent(this, SettingsActivity.class));
                 } else {
                     startActivity(new Intent(this, SettingsActivityHoneycomb.class));
                 }
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
