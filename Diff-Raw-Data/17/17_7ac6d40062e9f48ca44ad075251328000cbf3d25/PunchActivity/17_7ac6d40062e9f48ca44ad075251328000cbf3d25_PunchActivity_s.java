 package com.example.sextoncalculator;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class PunchActivity extends Activity implements OnClickListener {
 	protected Spinner entreeSpinner, sideSpinner, drinkSpinner;
 	protected FoodData foodData;
 	protected Button homeButton, checkoutButton, resetButton;
 	ArrayList<FoodItem> foodList;
 	String punchValue = "5.50";
 	String totalString = punchValue;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_punch);
 		
 		homeButton = (Button) findViewById(R.id.home_button);
 		homeButton.setOnClickListener(this);
 		checkoutButton = (Button) findViewById(R.id.checkout_button);
 		checkoutButton.setOnClickListener(this);
 		resetButton = (Button) findViewById(R.id.reset_button);
 		resetButton.setOnClickListener(this);
 		
 		initializeDB();
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_punch, menu);
 		return true;
 	}
 
 	@SuppressWarnings("deprecation")
 	public void initializeDB(){
 
         foodData = new FoodData(this);
 		entreeSpinner = (Spinner) findViewById(R.id.spinner1);
 		sideSpinner = (Spinner) findViewById(R.id.spinner2);
 		drinkSpinner = (Spinner) findViewById(R.id.spinner3);
 		if(foodData.count() == 0){
 			foodData.load();
 		}
 		
 		String[] from = new String[] { FoodData.NAME, FoodData.CALORIES};
 		int[] to =new int[] { R.id.spinner_textView1, R.id.spinner_textView2};
 		
 		Cursor entreeCursor = foodData.cat1(this);
 		startManagingCursor(entreeCursor);
 		Cursor sidesCursor = foodData.cat2(this);
 		startManagingCursor(entreeCursor);
 		Cursor drinksCursor = foodData.cat3(this);
 		startManagingCursor(entreeCursor);
 		
 		SimpleCursorAdapter entreeCursorAdapter = new SimpleCursorAdapter(this, R.layout.activity_punch_row, entreeCursor, from, to);
 		entreeSpinner.setAdapter(entreeCursorAdapter);
 		SimpleCursorAdapter sidesCursorAdapter = new SimpleCursorAdapter(this, R.layout.activity_punch_row, sidesCursor, from, to);
 		sideSpinner.setAdapter(sidesCursorAdapter);
 		SimpleCursorAdapter drinksCursorAdapter = new SimpleCursorAdapter(this, R.layout.activity_punch_row, drinksCursor, from, to);
 		drinkSpinner.setAdapter(drinksCursorAdapter);
 		
 		//foodCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	}
 
 	public void restartActivity(Activity act) {
 		Intent intent = new Intent();
 		act.finish();
 		intent.setClass(act, act.getClass());
 		act.startActivity(intent);
 	}	
 	
 	public double getTotalString() {
 		return Double.parseDouble(this.totalString);
 	}
 	
 	public ArrayList<FoodItem> generateFoodList() {
 		foodList = new ArrayList<FoodItem>();
 		TextView itemName;
 		String name;
 		double price;
 		int quantity;
 		itemName = (TextView) findViewById(R.id.spinner_textView1);
 		name = itemName.getText().toString();
 		price = 0.00;
 		quantity = 1;
 		foodList.add(new FoodItem(name, price, quantity));
 		return foodList;
 	}
 
 	public void onClick(View v) {
 		Intent intent;
 		if (v == homeButton) {
 			intent = new Intent(this, HomeActivity.class);
 			startActivity(intent);
 		} else if (v == checkoutButton) {
 			intent = new Intent(this, CheckoutActivity.class);
 			intent.putParcelableArrayListExtra("foodList", generateFoodList());
 			intent.putExtra("totalString", getTotalString());
 			startActivity(intent);
 		} else if (v == resetButton) {
 			restartActivity(this);
 		}
 	}
 
 }
