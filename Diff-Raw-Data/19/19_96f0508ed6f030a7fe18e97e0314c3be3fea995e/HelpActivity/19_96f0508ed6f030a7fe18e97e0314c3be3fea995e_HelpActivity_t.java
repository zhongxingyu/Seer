 package se.chalmers.krogkollen.help;
 
import se.chalmers.krogkollen.R;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 
 /**
  * Activity for the help screen
  * 
  * Author: Johan Backman
  * Date: 2013-10-02
  */
 public class HelpActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_help);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.help, menu);
 
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setIcon(R.drawable.transparent_spacer);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
