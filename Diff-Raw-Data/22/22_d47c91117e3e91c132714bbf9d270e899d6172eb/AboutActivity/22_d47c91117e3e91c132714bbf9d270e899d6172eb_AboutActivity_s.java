 package eu.pinnoo.garbagecalendar.ui.preferences;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.SpannableString;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.view.MenuItem;
 import android.widget.TextView;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import eu.pinnoo.garbagecalendar.R;
 import eu.pinnoo.garbagecalendar.data.LocalConstants;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class AboutActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.about);
 
         if (LocalConstants.DEBUG) {
             GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getApplicationContext());
             googleAnalytics.setAppOptOut(true);
         }
 
         final SpannableString msg = new SpannableString(getString(R.string.aboutMessageAuthor));
         Linkify.addLinks(msg, Linkify.ALL);
         TextView v = (TextView) findViewById(R.id.aboutTextAuthor);
         v.setMovementMethod(LinkMovementMethod.getInstance());
         v.setText(msg);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
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
