 package com.singh.servemeclient;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.singh.servemeclient.helper.IntentIntegrator;
 import com.singh.servemeclient.helper.IntentResult;
 
 public class LocationActivity extends Activity {
 	// Debugging
 	private static final String TAG = "LocationActivity";
 	private static final boolean D = true;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (D)
 			Log.e(TAG, "+++ ON CREATE +++");
 		setContentView(R.layout.location_activity);
 
 		IntentIntegrator integrator = new IntentIntegrator(this);
 		integrator.initiateScan();
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(
 				requestCode, resultCode, intent);
 		if (scanResult != null) {
			Log.i("IntentResult",scanResult.toString());

		}else {
		Toast.makeText(getApplicationContext(),
				"Invalid Barcode. Please Try Again.", Toast.LENGTH_LONG)
				.show();
		Intent myIntent = new Intent(LocationActivity.this,
				LogInActivity.class);
		startActivityForResult(myIntent, 0);
 		}
 	}
	
 	protected void onResume() {
 		super.onResume();
 		if (D)
 			Log.e(TAG, "+++ ON RESUME +++");
		
 	}
	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (D)
 			Log.e(TAG, "--- ON DESTROY ---");
 
 	}
 
 }
