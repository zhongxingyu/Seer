 package com.alvarosantisteban.pathos.preferences;
 
import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
import android.os.Build;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.alvarosantisteban.pathos.R;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 /**
  * Displays the information about the GooglePlay attributions.
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class LegalNoticesActivity extends Activity {
 	
 	TextView googlePlayAttributions;
 	TextView websites;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		// Enable the app's icon to act as home
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
 		setContentView(R.layout.activity_legal_notices);
 		websites = (TextView)findViewById(R.id.used_websites_attribution);
 		websites.setText(Html.fromHtml("<a href=\"http://www.iheartberlin.de/\">I Heart Berlin</a> <br>" +
 				"<a href=\"http://berlinmetal.lima-city.de/index.php/index.php?id=start\">Metal Concerts</a> <br>" +
 				"<a href=\"http://www.whitetrashfastfood.com/events/\">White Trash</a> <br>" +
 				"<a href=\"http://www.goth-city-radio.com/dsb/dates.php\">Goth Datum</a> <br>" +
 				"<a href=\"http://stressfaktor.squat.net/termine.php\">Stress Faktor</a> <br>" +
 				"<a href=\"http://www.indexberlin.de/openings-and-events\">Index</a> <br>"));
 		websites.setMovementMethod (LinkMovementMethod.getInstance());
 		googlePlayAttributions = (TextView) findViewById(R.id.googleplay_attribution);
 		googlePlayAttributions.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
 		return true;
 	}
 	
 	/**
 	 * Checks which item from the menu has been clicked
 	 */
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
 			// app icon in action bar clicked; go to Settings
 			Intent intent = new Intent(this, SettingsActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		}
         return true;
     }
 }
