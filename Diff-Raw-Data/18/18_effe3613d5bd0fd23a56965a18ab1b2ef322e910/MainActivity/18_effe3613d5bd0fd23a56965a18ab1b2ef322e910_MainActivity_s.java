 package com.application.takeacoffee;
 
 import com.dm.zbar.android.scanner.ZBarConstants;
 import com.dm.zbar.android.scanner.ZBarScannerActivity;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	public static final String TAG ="MainActivity";
 	protected static final int ZBAR_SCANNER_REQUEST = 0;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.coffe_machine_layout);
 		
 
 		
 		//STATIC BUTTON to try out scanCodeReader
 		LinearLayout QRCodeScanButton = (LinearLayout)findViewById(R.id.findCoffeeMachineLayoutId);
 		QRCodeScanButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(MainActivity.this, ZBarScannerActivity.class);
 				startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
 			}
 		});
 		
 		Context context = this.getApplicationContext();
 		
 		initView(context);
 	}
 
	
 	
 	public void initView(Context context){
 		//get data from JSON
 		CoffeMachine[] coffeMachineArray = RetrieveDataFromServer.getCoffeMachineData();
 
 		if(coffeMachineArray.length != 0) {
 			for(CoffeMachine coffeMachineObj : coffeMachineArray){
 				Log.e(TAG,"coffeMachineData - " + coffeMachineObj.address + coffeMachineObj.name);
 				
 				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				LinearLayout layoutContainer = (LinearLayout)findViewById(R.id.coffeeMachineContainerLayoutId);
				layoutContainer.addView(inflater.inflate(R.layout.coffe_machine_template, null));  		
				
			}
		}
 
 		Button  myReviewsButton= (Button)findViewById(R.id.myReviewsButtonId);
 		myReviewsButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(MainActivity.this, MyReviewsActivity.class);
 				startActivity(intent);
 			}
 		});
 
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{    
 	    if (resultCode == RESULT_OK) 
 	    {
 	        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
 	        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
 	        Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
 	        Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
 	        // The value of type indicates one of the symbols listed in Advanced Options below.
 	    } else if(resultCode == RESULT_CANCELED) {
 	        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
 	    }
 	}
 }
