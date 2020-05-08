 package com.hyperactivity.android_app.activities;
 
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.ScrollPicker;
 import com.hyperactivity.android_app.forum.ForumThread;
 
 public class MainActivity extends FragmentActivity {
     private ScrollPicker scrollPicker;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         View view = findViewById(R.id.forum_categories_surface_view);
         scrollPicker = (ScrollPicker) view;
         scrollPicker.getThread().setState(
                 ScrollPicker.ScrollPickerThread.STATE_READY);
 
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inScaled = false;
         options.inDither = false;
         options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_contact, options), "Kontakt", Color.BLACK);
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_creativity, options), "Krativitet", Color.BLACK);
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_general, options), "Generellt", Color.BLACK, true);
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_hobby, options), "Hobby", Color.BLACK);
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_medicine, options), "Medicin", Color.BLACK);
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_school, options), "Skola", Color.BLACK);
         scrollPicker.getItemManager().addItem(BitmapFactory.decodeResource(getResources(), R.drawable.c_tips, options), "Tips", Color.BLACK);
 
        scrollPicker.getItemManager().recalculateItems();
 
         ArrayList<ForumThread> forumList = new ArrayList<ForumThread>();
         forumList.add(new ForumThread(null, "test1", "test12"));
         forumList.add(new ForumThread(null, "test2", "test22"));
         forumList.add(new ForumThread(null, "test3", "test32"));
 
         ThreadListFragment threadListFragment = (ThreadListFragment) getSupportFragmentManager().findFragmentById(R.id.thread_list);
 
         threadListFragment.updateThreadList(forumList);
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
             // Settings has been clicked, check the android version to decide if
             // to use fragmented settings or not
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
