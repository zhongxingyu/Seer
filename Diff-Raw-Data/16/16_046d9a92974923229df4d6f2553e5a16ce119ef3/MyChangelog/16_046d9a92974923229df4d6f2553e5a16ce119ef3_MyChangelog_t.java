 package de.thecamper.android.wkgvertretungsplan;
 
 import android.content.Intent;
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.MenuItem;
 
 import de.thecamper.android.androidtools.ChangelogActivity;
 
 
 /**
  * Shows the changelog located in /res/values/changelog.xml
  * as a String-Array
  * @author Daniel
  *
  */
 public class MyChangelog extends ChangelogActivity {
     
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         setTheme(PreferencesActivity.getTheme(this));
         
         super.onCreate(savedInstanceState);
         
         // set up ActionBar
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
 
         setChangelogText(R.array.changelog);
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, PreferencesActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
 }
