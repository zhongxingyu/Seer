 package com.example.sextoncalculator;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 @SuppressLint("UseValueOf")
 public class CheckoutActivity extends Activity implements OnClickListener {
 
 	TextView total;
 	double totalPrice;
 	Bundle extras;
 	double entrePrice;
 	double sidePrice;
 	double drinkPrice;
 	String punchCounter;
 	double punchValue =5.5;
 	ArrayList<FoodItem> foodList;
 	
 	String flexValue;
 	String cashValue;
 	double totalPriceInstance;
 	double totalString;
 
 	protected Button homeButton, resetButton, checkoutButton, punchButton,
 			flexButton, cashButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//Intent intent = getIntent();
 		//String[] foodListIntance = intent.getStringArrayExtra("foodListIntance");
 		setContentView(R.layout.activity_checkout);	
 		foodList = getIntent().getExtras().getParcelableArrayList("foodList");
 		ListView listView1 = (ListView) findViewById(R.id.food_listView);
 		ArrayAdapter<FoodItem> adapter = new ArrayAdapter<FoodItem>(this, android.R.layout.simple_list_item_1, foodList);
 		listView1.setAdapter(adapter);
 		extras = getIntent().getExtras();
 		totalPrice = extras.getDouble("totalString");
 		// entrePrice = extras.getDouble("entrePrice");
 		// sidePrice = extras.getDouble("sidePrice");
 		// drinkPrice = extras.getDouble("drinkPrice");
 		// totalPrice = entrePrice + sidePrice + drinkPrice;
 		DecimalFormat df = new DecimalFormat("#.##");
 		totalPrice = Double.parseDouble(df.format(totalPrice));
 		total = (TextView) findViewById(R.id.amountRemaining_textView);
 		total.setText("$" + String.valueOf(totalPrice));
 		setTotalPrice(totalPrice);
 		
 
 		homeButton = (Button) findViewById(R.id.home_button);
 		homeButton.setOnClickListener(this);
 		resetButton = (Button) findViewById(R.id.reset_button);
 		resetButton.setOnClickListener(this);
 		punchButton = (Button) findViewById(R.id.punch_button);
 		punchButton.setOnClickListener(this);
 		flexButton = (Button) findViewById(R.id.flex_button);
 		flexButton.setOnClickListener(this);
 		cashButton = (Button) findViewById(R.id.cash_button);
 		cashButton.setOnClickListener(this);
 
 		checkoutButton = (Button) findViewById(R.id.checkout_button);
 		checkoutButton.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_checkout, menu);
 		return true;
 	}
 
 	public void onClick(View v) {
 		Intent intent;
 		if (v == homeButton) {
 			intent = new Intent(this, HomeActivity.class);
 			startActivity(intent);
 		} else if (v == resetButton) {
 			restartActivity(this);
 		} else if (v == punchButton) {
 			punchActivity();
 		} else if (v == flexButton) {
 			flexActivity();
 		} else if (v == cashButton) {
			cashActivity();
 		}
 	}
 
 	public void restartActivity(Activity act) {
 		//Intent intent = new Intent();
 		//act.finish();
 		//intent.setClass(act, act.getClass());
 		//act.startActivity(intent);
 	}
 
 	public void setTotalPrice(double totalPrice) {
 		this.totalPrice = totalPrice;
 	}
 	
 	public double getTotalPrice() {
 		return totalPrice;
 	}
 
 	public void punchActivity() {
 		//final TextView newTotal = (TextView) findViewById(R.id.amountRemaining_textView);
 		// totalPrice = totalPrice - entrePrice - sidePrice - drinkPrice;
 		totalPriceInstance = getTotalPrice();
 		if (totalPriceInstance > 5.50) {
 			totalPriceInstance = totalPriceInstance - 5.50;
 			setTotalPrice(totalPriceInstance);
 			if (totalPriceInstance > 0.00) {
 				DecimalFormat df = new DecimalFormat("0.00");
 				String totalPriceString = df.format(totalPriceInstance);
 				total.setText("$" + totalPriceString);
 				if (totalPriceInstance == 0.00) {
 					total.setText("$0.00");
 				}
 
 				EditText edit = (EditText) findViewById(R.id.punchPay_editText);
 				punchCounter = edit.getText().toString();
 				int punchIntCounter = Integer.parseInt(punchCounter);
 				punchIntCounter = punchIntCounter + 1;
 				punchCounter = Integer.toString(punchIntCounter);
 				//punchCounter = Double.toString(getTotalPrice());
 				edit.setText(punchCounter);
 			}
 		}
 	}
 	
 	public void flexActivity() {
 		totalPriceInstance = getTotalPrice();
 		if (totalPriceInstance == 0.00) {
 			//do nothing
 		}
 		else if (totalPriceInstance > 0.00) {
 			EditText edit = (EditText) findViewById(R.id.flexPay_editText);
 			flexValue = edit.getText().toString();
 			double flexDoubleValue = Double.parseDouble(flexValue);
 			flexDoubleValue = flexDoubleValue + totalPriceInstance;
 			DecimalFormat df = new DecimalFormat("0.00");
 			String flexValue = df.format(flexDoubleValue);
 			edit.setText("$" +flexValue);
 			
 			setTotalPrice(0.00);
 			total.setText("$0.00");
 		}
 	}
 	
 	public void cashActivity() {
 		totalPriceInstance = getTotalPrice();
 		if (totalPriceInstance == 0.00) {
 			//do nothing
 		}
 		else if (totalPriceInstance > 0.00) {
 			EditText edit = (EditText) findViewById(R.id.cashPay_editText);
 			cashValue = edit.getText().toString();
 			double cashDoubleValue = Double.parseDouble(cashValue);
 			cashDoubleValue = cashDoubleValue + totalPriceInstance;
 			DecimalFormat df = new DecimalFormat("0.00");
 			String cashValue = df.format(cashDoubleValue);
 			edit.setText("$" +cashValue);
 			
 			setTotalPrice(0.00);
 			total.setText("$0.00");
 		}
 	}
 
 }
