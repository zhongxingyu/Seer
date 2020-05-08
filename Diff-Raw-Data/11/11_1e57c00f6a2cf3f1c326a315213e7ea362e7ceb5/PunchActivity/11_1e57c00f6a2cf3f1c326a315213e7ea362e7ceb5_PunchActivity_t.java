 package com.example.sextoncalculator;
 
 import java.util.ArrayList;
 import java.util.Random;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * Handles the functionality of the Punch xml file
  * 
  * @author Justin Springer, Adam Bachmeier, Johnathan Ly, Tsuehue Xiong
  * 
  */
 public class PunchActivity extends Activity implements OnClickListener,
 		OnItemSelectedListener {
 	protected Spinner entreeSpinner, sideSpinner, drinkSpinner;
 	protected FoodData foodData;
 	protected Button homeButton, checkoutButton, resetButton, randomButton;
 	ArrayList<FoodItem> foodList;
 	String punchValue = "5.50";
 	String totalString = punchValue;
 	TextView calories;
 
 	/**
 	 * Generic onCreate method
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_punch);
 
 		// find and set on click listeners to buttons
 		homeButton = (Button) findViewById(R.id.home_button);
 		homeButton.setOnClickListener(this);
 		checkoutButton = (Button) findViewById(R.id.checkout_button);
 		checkoutButton.setOnClickListener(this);
 		resetButton = (Button) findViewById(R.id.reset_button);
 		resetButton.setOnClickListener(this);
 		randomButton = (Button) findViewById(R.id.random_button);
 		randomButton.setOnClickListener(this);
 
 		// initialize database
 		initializeDB();
 	}
 
 	/**
 	 * Generic onCreateOptionsMenu method
 	 * 
 	 * @param menu
 	 *            this object does that
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_punch, menu);
 		return true;
 	}
 
 	/**
 	 * Creates the FoodData database to be used on this page
 	 */
 	@SuppressWarnings("deprecation")
 	public void initializeDB() {
 		// set on item selected listener to spinners
 		foodData = new FoodData(this);
 		entreeSpinner = (Spinner) findViewById(R.id.spinner1);
 		entreeSpinner.setOnItemSelectedListener(this);
 		sideSpinner = (Spinner) findViewById(R.id.spinner2);
 		sideSpinner.setOnItemSelectedListener(this);
 		drinkSpinner = (Spinner) findViewById(R.id.spinner3);
 		drinkSpinner.setOnItemSelectedListener(this);
 
 		// load database if empty
 		if (foodData.count() == 0) {
 			foodData.load();
 		}
 		// bind data to spinner view
 		String[] from = new String[] { FoodData.NAME, FoodData.CATEGORY,
 				FoodData.CALORIES };
 		int[] to = new int[] { R.id.spinner_textView1, R.id.spinner_category,
 				R.id.spinner_textView2 };
 
 		// display appropriate category data in each spinner
 		Cursor entreeCursor = foodData.cat1(this);
 		startManagingCursor(entreeCursor);
 		Cursor sidesCursor = foodData.cat2(this);
 		startManagingCursor(entreeCursor);
 		Cursor drinksCursor = foodData.cat3(this);
 		startManagingCursor(entreeCursor);
 
 		// set adapter for each spinner
 		SimpleCursorAdapter entreeCursorAdapter = new SimpleCursorAdapter(this,
 				R.layout.activity_punch_row, entreeCursor, from, to);
 		entreeSpinner.setAdapter(entreeCursorAdapter);
 		SimpleCursorAdapter sidesCursorAdapter = new SimpleCursorAdapter(this,
 				R.layout.activity_punch_row, sidesCursor, from, to);
 		sideSpinner.setAdapter(sidesCursorAdapter);
 		SimpleCursorAdapter drinksCursorAdapter = new SimpleCursorAdapter(this,
 				R.layout.activity_punch_row, drinksCursor, from, to);
 		drinkSpinner.setAdapter(drinksCursorAdapter);
 	}
 
 	/**
 	 * Restarts this page
 	 */
 	public void restartActivity() {
 		Intent intent = getIntent();
 		overridePendingTransition(0, 0);
 		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 		finish();
 		overridePendingTransition(0, 0);
 		startActivity(intent);
 	}
 
 	/**
 	 * Randomly selects items in each of the spinners
 	 */
 	public void randomActivity() {
 		// generator generates a random number to be used to select an item in
 		// the spinner
 		// generator uses count to get the number of items in each spinner
 		Random generator = new Random();
 		int count = entreeSpinner.getCount();
 		int random = generator.nextInt(count);
 		entreeSpinner.setSelection(random, true);
 		count = sideSpinner.getCount();
 		random = generator.nextInt(count);
 		sideSpinner.setSelection(random, true);
 		count = drinkSpinner.getCount();
 		random = generator.nextInt(count);
 		drinkSpinner.setSelection(random, true);
 	}
 
 	/**
 	 * Updates the total calories each time a new item is selected in one of the
 	 * spinners
 	 */
 	public void calorieUpdate() {
 		// grabs each calorie from the spinner locations and adds them together
 		calories = (TextView) findViewById(R.id.calorieView);
		int calTotal = 0, eCal, sCal, dCal;
 		TextView eView, sView, dView, itemCat;
 		eView = (TextView) entreeSpinner.findViewById(R.id.spinner_textView2);
		eCal = Integer.parseInt(eView.getText().toString());
 		calTotal = calTotal + eCal;
 
 		// the following code checks if entree is category 4 in order to ignore
 		// the side calories
 		itemCat = (TextView) entreeSpinner.findViewById(R.id.spinner_category);
 		String cat = itemCat.getText().toString();
 		if (!cat.equals("4")) {
 			sView = (TextView) sideSpinner.findViewById(R.id.spinner_textView2);
			sCal = Integer.parseInt(sView.getText().toString());
 			calTotal = calTotal + sCal;
 		}
 		dView = (TextView) drinkSpinner.findViewById(R.id.spinner_textView2);
		dCal = Integer.parseInt(dView.getText().toString());
 		calTotal = calTotal + dCal;
 		calories.setText("Total Calories = " + calTotal);
 	}
 
 	/**
 	 * Returns the TotalString variable
 	 * 
 	 * @return totalString
 	 */
 	public double getTotalString() {
 		return Double.parseDouble(this.totalString);
 	}
 
 	/**
 	 * Returns a foodList to be passed on to the checkout page to be displayed
 	 * 
 	 * @return foodList ArrayList of FoodItems
 	 */
 	public ArrayList<FoodItem> generateFoodList() {
 		// adds each spinner info to the foodlist to be passed on to checkout
 		foodList = new ArrayList<FoodItem>();
 		TextView itemName, itemCal, itemCat;
 		String name;
 		int calories, counter = 0;
 		int quantity = 1;
 		itemName = (TextView) entreeSpinner
 				.findViewById(R.id.spinner_textView1);
 		itemCal = (TextView) entreeSpinner.findViewById(R.id.spinner_textView2);
 		name = itemName.getText().toString();
 		calories = Integer.parseInt(itemCal.getText().toString());
 		counter = counter + calories;
 		FoodItem entreeFoodItem = new FoodItem();
 		entreeFoodItem.setName(name);
 		entreeFoodItem.setCalories(calories);
 		foodList.add(entreeFoodItem);
 
 		// the following code checks if entree is category 4 in order to ignore
 		// the side info
 		itemCat = (TextView) entreeSpinner.findViewById(R.id.spinner_category);
 		String cat = itemCat.getText().toString();
 		if (!cat.equals("4")) {
 			itemName = (TextView) sideSpinner
 					.findViewById(R.id.spinner_textView1);
 			itemCal = (TextView) sideSpinner
 					.findViewById(R.id.spinner_textView2);
 			name = itemName.getText().toString();
 			calories = Integer.parseInt(itemCal.getText().toString());
 			counter = counter + calories;
 			FoodItem sideFoodItem = new FoodItem();
 			sideFoodItem.setName(name);
 			sideFoodItem.setCalories(calories);
 			foodList.add(sideFoodItem);
 		}
 		itemName = (TextView) drinkSpinner.findViewById(R.id.spinner_textView1);
 		itemCal = (TextView) drinkSpinner.findViewById(R.id.spinner_textView2);
 		name = itemName.getText().toString();
 		calories = Integer.parseInt(itemCal.getText().toString());
 		counter = counter + calories;
 		FoodItem drinkFoodItem = new FoodItem();
 		drinkFoodItem.setName(name);
 		drinkFoodItem.setCalories(calories);
 		foodList.add(drinkFoodItem);
 		name = "Total ";
 		calories = counter;
 		FoodItem newFoodItem = new FoodItem();
 		newFoodItem.setName(name);
 		newFoodItem.setCalories(calories);
 		foodList.add(newFoodItem);
 		return foodList;
 	}
 
 	/**
 	 * onClick method to activate events when certain buttons are pressed
 	 * 
 	 * @param v
 	 *            the current View associated with the buttons
 	 */
 	public void onClick(View v) {
 		Intent intent;
 		if (v == homeButton) {
 			intent = new Intent(this, HomeActivity.class);
 			startActivity(intent);
 		} else if (v == checkoutButton) {
 			intent = new Intent(this, CheckoutActivity.class);
 			intent.putParcelableArrayListExtra("foodList", generateFoodList());
 			intent.putExtra("totalString", getTotalString());
 			intent.putExtra("flag", 1);
 			startActivity(intent);
 		} else if (v == resetButton) {
 			restartActivity();
 		} else if (v == randomButton) {
 			randomActivity();
 		}
 	}
 
 	/**
 	 * onItemSelected method to activate events when an item is selected in any
 	 * of the spinners
 	 * 
 	 * @param parentView
 	 *            needed for interface implementation
 	 * @param selectedItemView
 	 *            needed for interface implementation
 	 * @param position
 	 *            needed for interface implementation
 	 * @param id
 	 *            needed for interface implementation
 	 */
 	public void onItemSelected(AdapterView<?> parentView,
 			View selectedItemView, int position, long id) {
 		TextView category = (TextView) entreeSpinner
 				.findViewById(R.id.spinner_category);
 		String cat = category.getText().toString();
 		if (cat.equals("4")) {
 			sideSpinner.setVisibility(View.INVISIBLE);
 		} else {
 			sideSpinner.setVisibility(View.VISIBLE);
 		}
 		calorieUpdate();
 	}
 
 	/**
 	 * onNothingSelected does nothing
 	 * 
 	 * @param parentView
 	 *            needed for interface implementation
 	 */
 	public void onNothingSelected(AdapterView<?> parentView) {
 		// Do Nothing
 	}
 
 }
