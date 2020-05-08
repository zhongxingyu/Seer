 package com.emiexpert;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private EditText textPrinciple, textInterest, textDuration;
 	private TextView textResult;
 	public static Loan mLoan;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		initializeGlobals();
 		//add text change listener
 		textInterest.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// TODO Auto-generated method stub
 				if (count%2==0)
 				{
 					
 				}
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 
 	private void initializeGlobals() {
 		textResult = (TextView) findViewById(R.id.text_result);
 		textPrinciple = (EditText) findViewById(R.id.text_principle);
 		textInterest = (EditText) findViewById(R.id.text_interest);
 		textDuration = (EditText) findViewById(R.id.text_duration);
 	}
 
 	public void calculateInterest(View v) {
 		if (textDuration.getText().toString().length() > 0
 				&& textInterest.getText().toString().length() > 0
 				&& textPrinciple.getText().toString().length() > 0) {
 			int duration = Integer.parseInt(textDuration.getText().toString());
 			long principle = Long.parseLong(textPrinciple.getText().toString());
 			double interest = Double.parseDouble(textInterest.getText()
 					.toString());
 			mLoan = new Loan(principle, interest, duration);
 
			String text = "On completion of your home loan, you pay a total interest of <b><font color=\"#E20F0F\"> Rs. "
					+ String.valueOf(mLoan.getTotalInterest()) + "</font></b>";
 			textResult.setText(Html.fromHtml(text),
 					TextView.BufferType.SPANNABLE);
 			// textResult
 			// .setText("On completion of your home loan, you pay a total interest of Rs. "
 			// + String.valueOf(mLoan.getTotalInterest()));
 		} else {
 			Toast.makeText(MainActivity.this, "Please fill all data first",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void reset(View v) {
 		mLoan = null;
 		textResult.setText(getResources().getString(R.string.interestamt));
 		textDuration.setText("");
 		textInterest.setText("");
 		textPrinciple.setText("");
 	}
 
 	public void showInfo(View v) {
 		Intent intent = new Intent(this, InfoActivity.class);
 		startActivity(intent);
 	}
 
 	public void manageInterest(View v) {
 
 		if (mLoan != null) {
 
 			Intent myIntent = new Intent(this, ManageInterestActivity.class);
 			startActivity(myIntent);
 		} else {
 			Toast.makeText(MainActivity.this, "Please calculate loan first",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
