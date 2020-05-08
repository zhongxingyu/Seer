 package com.object.disoriented;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class PurchaseActivity extends Activity {
 	private ArrayList<String> qrContents;
 	private final String TAG = "purchase-activity";
 	private TextView txtPrice;
 	private TextView txtItemName;
 	private TextView txtStoreName;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.purchase_screen);
 		
 		Intent intent = getIntent();
		qrContents = intent.getStringArrayListExtra("qr_contents");
 		
 		Log.v(TAG,""+qrContents.size());
 		for(int i = 0; i<qrContents.size();i++){
			String out = i+qrContents.get(i);
 			Log.v(TAG,out);
 		}
 		
 //		Log.v(TAG,qrContents.get(1));
 		
 		txtPrice = (TextView)findViewById(R.id.txtPrice);
 		txtItemName = (TextView)findViewById(R.id.txtItemName);
		txtStoreName = (TextView)findViewById(R.id.txtPrice);
 		
 		txtPrice.setText(qrContents.get(1));
 		txtItemName.setText(qrContents.get(2));
 		txtStoreName.setText(qrContents.get(0));
 	}
 }
