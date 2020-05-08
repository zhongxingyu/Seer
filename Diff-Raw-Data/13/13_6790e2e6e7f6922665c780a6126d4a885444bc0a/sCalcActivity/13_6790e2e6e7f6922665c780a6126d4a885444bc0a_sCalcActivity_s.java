 package com.shumz.scalc;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.app.Activity;
 
public class sCalcActivity extends Activity {
 
 	// Declaring Elements of Activity...
 
 	TextView resultString;
 
 	Button ClearButton, OffButton;
 
 	Button Button_1, Button_2, Button_3, Button_4, Button_5, Button_6,
 			Button_7, Button_8, Button_9, Button_0;
 
 	Button Button_Add, Button_Sub, Button_Mul, Button_Div, Button_Floater,
 			Button_Equal;
 
 	Float result, operand_1, operand_2;
 
 	Boolean DotIsPressed = false;
 	Boolean EqualIsPressed = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_scalc_activity);
 
 		initializeDigits();
 		initializeFuncKeys();
 		initializePanelKeys();
 
 		// resultString.setText("0");
 
 		// Clear results
 		ClearButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				resultString.setText("0");
 				result = 0F;
 				operand_1 = 0F;
 				operand_2 = 0F;
 			}
 		});
 
 		// Exit Application
 		OffButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				finish();
 				System.exit(0);
 			}
 		});
 	}
 
 	private void initializePanelKeys() {
 		// TODO Auto-generated method stub
 		OffButton = (Button) findViewById(R.id.buttonOff);
 		ClearButton = (Button) findViewById(R.id.buttonClear);
 		resultString = (TextView) findViewById(R.id.tvResult);

 	}
 
 	private void initializeFuncKeys() {
 		// TODO Auto-generated method stub
 
 		Button_Add = (Button) findViewById(R.id.button_Add);
 		Button_Sub = (Button) findViewById(R.id.button_Sub);
 		Button_Mul = (Button) findViewById(R.id.button_Mul);
 		Button_Div = (Button) findViewById(R.id.button_Div);
 		Button_Floater = (Button) findViewById(R.id.button_Dot);
 		Button_Equal = (Button) findViewById(R.id.button_Eql);

 	}
 
 	private void initializeDigits() {
 		// TODO Auto-generated method stub
 		Button_1 = (Button) findViewById(R.id.button_1);
 		Button_2 = (Button) findViewById(R.id.button_2);
 		Button_3 = (Button) findViewById(R.id.button_3);
 		Button_4 = (Button) findViewById(R.id.button_4);
 		Button_5 = (Button) findViewById(R.id.button_5);
 		Button_6 = (Button) findViewById(R.id.button_6);
 		Button_7 = (Button) findViewById(R.id.button_7);
 		Button_8 = (Button) findViewById(R.id.button_8);
 		Button_9 = (Button) findViewById(R.id.button_9);
 		Button_0 = (Button) findViewById(R.id.button_0);
 
 		// , Button_2, Button_3, Button_4, Button_5,
 		// Button_6, Button_7, Button_8, Button9, Button_0;
 	}
 
 }
 
 // @Override
 // public boolean onCreateOptionsMenu(Menu menu) {
 // // Inflate the menu; this adds items to the action bar if it is present.
 // getMenuInflater().inflate(R.menu.main, menu);
 // return true;
 // }
