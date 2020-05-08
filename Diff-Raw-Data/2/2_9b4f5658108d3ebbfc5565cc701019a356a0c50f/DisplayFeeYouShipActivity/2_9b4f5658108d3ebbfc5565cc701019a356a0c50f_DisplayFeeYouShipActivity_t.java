 package com.amazon.hackday.trms;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class DisplayFeeYouShipActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.display_fee_you_ship);
         
 		// start Over button
		ImageView startover = (ImageView)findViewById(R.id.youshipstartover);
 		startover.setOnClickListener(startOverClickListener);
 		
 		// create Listings Button
 		ImageView createListing = (ImageView)findViewById(R.id.youshipcreateListing);
 		createListing.setOnClickListener(createListingClickListener);
 		
 		// calculate 		
 		ImageView calculate = (ImageView)findViewById(R.id.youshipcalculate);
 		calculate.setOnClickListener(calculateClickListener);		
 		
 		// EditText
 		EditText itemPrice = (EditText)findViewById(R.id.ysEditItemPrice);
 		itemPrice.setOnFocusChangeListener(itemPriceFocusChangeListener);
 		
 		itemPrice.setText("200.00");
 		calculateFee();        
     }
     
     private void calculateFee() {
     	EditText text = (EditText)findViewById(R.id.ysEditItemPrice);
 		String value = text.getText().toString();
 		Double val = Double.parseDouble(value);
 		
     	EditText tShipCredit = (EditText)findViewById(R.id.ysEditShippingCredit);
 		String shipcredit = tShipCredit.getText().toString();
 		Double shipCreditVal = Double.parseDouble(shipcredit);		
 		
 		TextView referralText = (TextView) findViewById(R.id.ysEditReferralFee);
 		Double rFee = 0.08 * (val + shipCreditVal);
 		referralText.setText("($" + rFee.toString() + ")");	
 		
 		TextView netAmountText = (TextView) findViewById(R.id.ysEditNetAmount);
 		Double netFee = val - rFee;
 		netAmountText.setText("($" + netFee.toString() + ")");		
     }
     
     OnClickListener calculateClickListener = new OnClickListener() {
 		
 		public void onClick(View view) {
 			calculateFee();			
 		}
 	};    
     
     OnClickListener startOverClickListener = new OnClickListener() {
 		
 		public void onClick(View view) {
 			Context context = view.getContext();
 			Intent intent = new Intent(DisplayFeeYouShipActivity.this, FindAsinActivity.class);
 			try {
 				//Start next activity
 				context.startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 				// TODO: handle exception
 			}
 		}
 	};
 	
     OnFocusChangeListener itemPriceFocusChangeListener = new OnFocusChangeListener() {		
 		public void onFocusChange(View view, boolean arg1) {			
 			//calculateFee();
 		}
 	};	
 	
     OnClickListener createListingClickListener = new OnClickListener() {
 		
 		public void onClick(View view) {
 			Context context = view.getContext();
 			Intent intent = new Intent(DisplayFeeYouShipActivity.this, CreateListingActivity.class);
 			try {
 				//Start next activity
 				context.startActivity(intent);
 			} catch (Exception e) {
 				e.printStackTrace();
 				// TODO: handle exception
 			}
 		}
 	};      
 }
