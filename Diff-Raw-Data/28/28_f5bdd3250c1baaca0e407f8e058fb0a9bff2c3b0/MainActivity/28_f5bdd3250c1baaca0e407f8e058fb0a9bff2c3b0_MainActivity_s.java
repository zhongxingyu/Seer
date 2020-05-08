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
 import com.hyperactivity.android_app.core.Engine;
 
 public class MainActivity extends Activity {
 
 	private Boolean	isLocked;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		final TextView welcomeNameTextView = (TextView) findViewById(R.id.welcomeName);
 		welcomeNameTextView.setText(((Engine) getApplication()).getAccount().getUsername());
 
 		// Set up our lock button stuff
 		isLocked = true;
 
 		final View topBar = (View) findViewById(R.id.top_bar);
		final ImageView lockImageButton = (ImageView) findViewById(R.id.lockButton);
 		final TextView lockTextView = (TextView) findViewById(R.id.lockText);
 
		lockImageButton.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View arg0) {
 				if (isLocked) {
 					lockTextView.setText((String) getResources().getString(R.string.top_bar_private));
					lockImageButton.setImageResource(R.drawable.locked);
 				}
 				else {
 					lockTextView.setText((String) getResources().getString(R.string.top_bar_public));
					lockImageButton.setImageResource(R.drawable.unlocked);
 				}

				lockImageButton.invalidate();
				isLocked = !isLocked;
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	/**
 	 * Triggers when an item in the menu is clicked such as "Settings" or "Help"
 	 */
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
