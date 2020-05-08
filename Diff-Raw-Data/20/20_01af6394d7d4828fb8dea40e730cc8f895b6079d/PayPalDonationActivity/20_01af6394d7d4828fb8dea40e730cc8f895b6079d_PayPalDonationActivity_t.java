 package com.codingspezis.android.metalonly.player;
 
 import android.content.*;
 import android.os.*;
 import android.support.v4.app.*;
 
 import com.actionbarsherlock.app.*;
 import com.actionbarsherlock.view.*;
 
 /**
  * 
  * with this activity users can make paypal donations to metal only
  * 
  */
 public class PayPalDonationActivity extends SherlockFragmentActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getSupportActionBar().setHomeButtonEnabled(true);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_donation);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			navigateUp();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 
 		}
 	}
 
 	private void navigateUp() {
 		Intent upIntent = new Intent(this, MainActivity.class);
 		NavUtils.navigateUpTo(this, upIntent);
 	}
 
 }
