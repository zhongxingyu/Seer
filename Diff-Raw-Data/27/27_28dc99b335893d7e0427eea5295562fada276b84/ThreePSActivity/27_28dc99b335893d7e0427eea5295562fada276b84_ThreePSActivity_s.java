 package com.object.disoriented;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class ThreePSActivity extends Activity {
 	/** Called when the activity is first created. */
 	private Button btnBuy;
 	private Button btnReceipt;
 	private String TAG = "3PS Buyer Screen";
 	private ArrayList<String> qrContents;
 	@Override
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.buyer_start);
 
 		qrContents = new ArrayList<String>();
 		
 		btnBuy = (Button) findViewById(R.id.btnBuy);
 		btnBuy.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Log.v(TAG,"Buy button pressed");
 				Toast.makeText(ThreePSActivity.this, "Buy Button clicked!", Toast.LENGTH_SHORT).show();
 
 				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 				startActivityForResult(intent, 0);
 			}
 		});
 
 		btnReceipt = (Button) findViewById(R.id.btnReceipt);
 		btnReceipt.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 
 				Log.v(TAG,"Receipt button pressed");
 				for(int i = 0;i<qrContents.size();i++){
 					Log.v(TAG,qrContents.get(i));
 				}
 				Toast.makeText(ThreePSActivity.this, "Receipt Button clicked!", Toast.LENGTH_SHORT).show();
 			}
 		});
 
 	}
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == 0) {
 			if (resultCode == RESULT_OK) {
 				String contents = intent.getStringExtra("SCAN_RESULT");
 				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
 				
				StringTokenizer st = new StringTokenizer(contents);
 				
 				while(st.hasMoreTokens()){
					qrContents.add(contents);
 				}
 				
 				
 				Log.v(TAG,contents);
 				// Handle successful scan
 				
 				Intent i = new Intent(this,PurchaseActivity.class);
 				i.putStringArrayListExtra("qr_contents", qrContents);
 				
 				startActivity(i);
 			} else if (resultCode == RESULT_CANCELED) {
 				// Handle cancel
 			}
 		}
 	}
 
 
 }
