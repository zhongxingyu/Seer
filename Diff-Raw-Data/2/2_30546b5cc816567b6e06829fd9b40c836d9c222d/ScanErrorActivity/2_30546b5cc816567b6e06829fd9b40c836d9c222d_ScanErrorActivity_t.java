 package edu.harvad.law.librarylab.wtwba;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 public class ScanErrorActivity extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_scan_error);
 	}
 
 	/** Called when the user selects the "key in new item" button */
 	public void keyItem(View view) {
 
		Intent intent = new Intent(this, TypeBarcodeActivity.class);
 		startActivity(intent);
 
 	}
 
 	/** Called when the user selects the "scan in new item" button */
 	public void scanItem(View view) {
 
 		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 		// intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		startActivityForResult(intent, 0);
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		// When our zxing intent returns, we call this method
 
 		String message = "no message";
 
 		if (requestCode == 0) {
 			if (resultCode == RESULT_OK) {
 				String contents = intent.getStringExtra("SCAN_RESULT");
 				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
 				// Handle successful scan
 
 				message = contents;
 
 				Intent display_intent = new Intent(this,
 						DisplayItemDetailsActivity.class);
 				display_intent.putExtra("barcode", message);
 				startActivity(display_intent);
 
 			} else if (resultCode == RESULT_CANCELED) {
 				// Handle cancel
 				message = "canceled";
 			}
 		} else {
 
 			message = "requestcode not 0";
 		}
 
 	}
 
 }
