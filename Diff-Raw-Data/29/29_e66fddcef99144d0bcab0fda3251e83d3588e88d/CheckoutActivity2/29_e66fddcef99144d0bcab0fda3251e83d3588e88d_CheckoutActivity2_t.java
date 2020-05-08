 package com.lossboys.customerapp.dashboard;
 
 import com.lossboys.customerapp.R;
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.view.View;
 
 import android.widget.Button;
 import android.widget.EditText;
 
 import android.widget.TextView;
 
 public class CheckoutActivity2 extends Activity {
 	/** Called when the activity is first created. */
 
 	Button btn_submit = (Button) findViewById(R.id.btnSubmit);
 	EditText inputCardName;
 	EditText inputCardAddress;
 	TextView checkout2ErrorMsg;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.checkout2_layout);
 
 		inputCardName = (EditText) findViewById(R.id.cardName);
 		inputCardAddress = (EditText) findViewById(R.id.cardAddress);
 		btn_submit = (Button) findViewById(R.id.btnSubmit);
 		checkout2ErrorMsg = (TextView) findViewById(R.id.checkout2_error);
 
 		btn_submit.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 
 				String CardName = inputCardName.getText().toString();
 				String CardAddress = inputCardAddress.getText().toString();
 
 				checkout2ErrorMsg
 						.setText("Your order has been received. You will receive a confirmation email and a notification when your order is ready for pickup!");
 			}
 		});
 
 	}
 }
