 package com.mike.comicreeder.activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.view.View;
 
 import com.mike.comicreeder.R;
 import com.mike.comicreeder.components.FloatingLabelEditText;
 import com.parse.ParseObject;
 
 public class AddComicActivity extends Activity {
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_add_comic);
     // Show the Up button in the action bar.
     setupActionBar();
   }
 
   public void addComic(View view) {
 
     FloatingLabelEditText comicNameText = (FloatingLabelEditText) findViewById(R.id.comicName);
     FloatingLabelEditText writerText = (FloatingLabelEditText) findViewById(R.id.writer);
     FloatingLabelEditText issueNumberText = (FloatingLabelEditText) findViewById(R.id.issueNum);
     FloatingLabelEditText publisherText = (FloatingLabelEditText) findViewById(R.id.publisher);
 
     ParseObject comic = new ParseObject("Comics");
     comic.put("comicName", comicNameText.getTextFieldValue().toString());
     comic.put("writer", writerText.getTextFieldValue().toString());
     comic.put("issue", Integer.parseInt(issueNumberText.getTextFieldValue().toString()));
     comic.put("publisher", publisherText.getTextFieldValue().toString());
 
     comic.saveInBackground();
 
     Intent intent = new Intent(this, ComicReederActivity.class);
     startActivity(intent);
   }
 
   /**
    * Set up the {@link android.app.ActionBar}, if the API is available.
    */
   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   private void setupActionBar() {
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
       getActionBar().setDisplayHomeAsUpEnabled(true);
     }
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.add_comic, menu);
     return true;
   }
 
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
       case android.R.id.home:
         // This ID represents the Home or Up button. In the case of this
         // activity, the Up button is shown. Use NavUtils to allow users
         // to navigate up one level in the application structure. For
         // more details, see the Navigation pattern on Android Design:
         //
         // http://developer.android.com/design/patterns/navigation.html#up-vs-back
         //
         NavUtils.navigateUpFromSameTask(this);
         return true;
     }
     return super.onOptionsItemSelected(item);
   }
 
 }
