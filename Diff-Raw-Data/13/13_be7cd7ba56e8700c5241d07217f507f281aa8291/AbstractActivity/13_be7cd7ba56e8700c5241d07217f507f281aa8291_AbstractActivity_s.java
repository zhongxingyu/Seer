 package com.ad.cow;
 
 import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
 
 public class AbstractActivity extends SherlockActivity {
 
 	/*private AdView adView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Create the adView
 		adView = new AdView(this, AdSize.BANNER, "a14fbaa462a5c66");
 
 		// Lookup your LinearLayout assuming it’s been given
 		// the attribute android:id="@+id/mainLayout"
 		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
 
 		// Add the adView to it
 		layout.addView(adView);
 
 		// Initiate a generic request to load it with an ad
 		adView.loadAd(new AdRequest());
 	}
 
 	@Override
 	public void onDestroy() {
 		if (adView != null) {
 			adView.destroy();
 		}
 		super.onDestroy();
 	}*/
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, R.id.ID_ACTION_HOME, Menu.NONE,
 				R.string.action_label_home)
 				.setIcon(R.drawable.ic_compose)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_ALWAYS
 								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
 		menu.add(Menu.NONE, R.id.ID_ACTION_STATUS, Menu.NONE,
 				R.string.action_label_status)
 				.setIcon(R.drawable.ic_search)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_ALWAYS
 								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
 		getSupportActionBar().setDisplayShowHomeEnabled(false);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.ID_ACTION_HOME:
 			intent = new Intent(this, HomeActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		case R.id.ID_ACTION_STATUS:
 			intent = new Intent(this, StatusActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
