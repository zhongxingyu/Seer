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
 
 /**
  * Handles the functionality of the Checkout xml file
  * 
  * @author Tsuehue Xiong, Johnathan Ly, Adam Bachmeier, Justin Springer
  * 
  */
 @SuppressLint("UseValueOf")
 public class CheckoutActivity extends Activity implements OnClickListener {
 
 	TextView total, punchValue_textView;
 	double totalPrice, entrePrice, sidePrice, drinkPrice, punchValue = 5.5,
 			totalPriceInstance, totalString, currentFlex, currentCash,
 			flexDoubleValueTemp, flexDoubleValue, cashDoubleValueTemp,
 			cashDoubleValue, tax = 1.06875, removeTax = .93125,
 			originalTotal = 0;
 	Bundle extras;
 	ArrayList<FoodItem> foodList;
 	String flexValue, flexValueTemp, cashValue, cashValueTemp, punchCounter,
 			punchCounterTemp;
 	int punchIntCounterTemp, punchIntCounter, flag;
 	protected Button homeButton, resetButton, checkoutButton, punchButton,
 			flexButton, cashButton, resetFlexButton, resetCashButton,
 			punchIncreaseButton, punchDecreaseButton;
 
 	/**
 	 * Generic onCreate method
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		String formattedString;
 
 		// set initial setup for punchCounter, currentFlex, currentCash, and
 		// punchIntCounter
 		setPunchIntCounter(0);
 		setCurrentFlex(0.00);
 		setCurrentCash(0.00);
 		setPunchIntCounterTemp(0);
 
 		// pulls variables from connecting pages (either activity_browse or
 		// activity_punch)
 		Intent myIntent = getIntent();
 		extras = myIntent.getExtras();
 		flag = myIntent.getIntExtra("flag", 0);
 
 		// sets the view to the current page to display the layout
 		setContentView(R.layout.activity_checkout);
 
 		// initial setup if the previous page that lead to this page is
 		// activity_punch
 		if (flag == 1) {
 			// obtain the passed foodList and display it into a listView
 			foodList = extras.getParcelableArrayList("foodList");
 			ListView listView1 = (ListView) findViewById(R.id.food_listView);
 			ArrayAdapter<FoodItem> adapter = new ArrayAdapter<FoodItem>(this,
 					android.R.layout.simple_list_item_1, foodList);
 			listView1.setAdapter(adapter);
 
 			// get the initial total of the items in foodList
 			totalPrice = extras.getDouble("totalString");
 
 			// convert the passed totalString value from a string to a double
 			// and set it as the totalPrice
 			formattedString = formatIntoDecimal().format(totalPrice);
 			total = (TextView) findViewById(R.id.amountRemaining_textView);
 			total.setText("$" + formattedString);
 			setTotalPrice(totalPrice);
 			originalTotal = totalPrice;
 		}
 		// initial setup if the previous page that lead to this page is
 		// activity_punch
 		else if (flag == 2) {
 			// obtain the passed foodList and display it into a listView
 			foodList = getIntent().getExtras().getParcelableArrayList(
 					"foodList");
 			ListView listView1 = (ListView) findViewById(R.id.food_listView);
 			ArrayAdapter<FoodItem> adapter = new ArrayAdapter<FoodItem>(this,
 					android.R.layout.simple_list_item_1, foodList);
 			listView1.setAdapter(adapter);
 
 			// get the initial total of the items in foodList
 			totalPrice = extras.getDouble("totalString");
 
 			// convert the passed totalString value from a string to a double
 			// and set it as the totalPrice
 			formattedString = formatIntoDecimal().format(totalPrice);
 			total = (TextView) findViewById(R.id.amountRemaining_textView);
 			total.setText("$" + formattedString);
 			setTotalPrice(totalPrice);
 			originalTotal = totalPrice;
 		}
 		// find buttons on the page and set them to an onClickListener to call a
 		// method when the button is clicked
 		punchButton = (Button) findViewById(R.id.punch_button);
 		punchButton.setOnClickListener(this);
 		punchDecreaseButton = (Button) findViewById(R.id.punchDecrease_button);
 		punchDecreaseButton.setOnClickListener(this);
 		flexButton = (Button) findViewById(R.id.flex_button);
 		flexButton.setOnClickListener(this);
 		resetFlexButton = (Button) findViewById(R.id.resetFlex_button);
 		resetFlexButton.setOnClickListener(this);
 		cashButton = (Button) findViewById(R.id.cash_button);
 		cashButton.setOnClickListener(this);
 		resetCashButton = (Button) findViewById(R.id.resetCash_button);
 		resetCashButton.setOnClickListener(this);
 		homeButton = (Button) findViewById(R.id.home_button);
 		homeButton.setOnClickListener(this);
 		resetButton = (Button) findViewById(R.id.reset_button);
 		resetButton.setOnClickListener(this);
 
 		// finds and set the visibility of checkout to invisible
 		checkoutButton = (Button) findViewById(R.id.checkout_button);
 		checkoutButton.setVisibility(View.INVISIBLE);
 	}
 
 	/**
 	 * Generic onCreateOptionsMenu method
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_checkout, menu);
 		return true;
 	}
 
 	/**
 	 * Handles the events when a button is pressed/clicked
 	 */
 	public void onClick(View v) {
 		Intent intent;
 		if (v == homeButton) {
 			intent = new Intent(this, HomeActivity.class);
 			startActivity(intent);
 		} else if (v == resetButton) {
 			restartActivity();
 		} else if (v == punchButton) {
 			punchActivity();
 		} else if (v == flexButton) {
 			flexActivity();
 		} else if (v == cashButton) {
 			cashActivity();
 		} else if (v == punchDecreaseButton) {
 			punchDecreaseActivity();
 		} else if (v == resetFlexButton) {
 			flexResetActivity();
 		} else if (v == resetCashButton) {
 			cashResetActivity();
 		}
 	}
 
 	/**
 	 * a convert that converts a string to a decimal with 2 decimal places
 	 * 
 	 * @return dFormat the decimal format to be used
 	 */
 	public DecimalFormat formatIntoDecimal() {
 		DecimalFormat dFormat = new DecimalFormat("0.00");
 		return dFormat;
 	}
 
 	/**
 	 * decrease the current value of punchIntCounter by 1
 	 */
 	public void punchDecreaseActivity() {
 		totalPriceInstance = getTotalPrice();
 		EditText edit = (EditText) findViewById(R.id.punchPay_editText);
 		punchCounterTemp = edit.getText().toString();
 		punchIntCounterTemp = Integer.parseInt(punchCounterTemp);
 		punchIntCounter = getPunchIntCounter();
 		// decrease the value of punchIntCounter if the previous page was
 		// PunchActivity
 		if (flag == 1) {
 			// if punchIntCounter is 1, decrease to value to 0 and set the value
 			// of totalPriceInstance back to its original price
 			if (punchIntCounter == 1) {
 				punchIntCounter = punchIntCounter - 1;
 				totalPriceInstance = totalPriceInstance + punchValue;
 			}
 			// punchIntCounter is not equal to 1, do nothing
 			else {
 				// do nothing because punch can't be negative or larger than 1
 			}
 		}
 		// decrease the value of punchIntCounter if the previous page was
 		// BrowseActivity
 		else if (flag == 2) {
 			// if punchIntCounter is larger than 0, decrease its value by 1 and
 			// set add punchValue(5.50) back into totalPriceInstance
 			if (punchIntCounter > 0) {
 				punchIntCounter = punchIntCounter - 1;
 				totalPriceInstance = totalPriceInstance + punchValue;
 			}
 			// if punchIntCounter is equal to 0 or smaller, do nothing
 			else {
 				// do nothing because punch can't be negative
 			}
 		}
 		setPunchIntCounter(punchIntCounter);
 		punchCounter = Integer.toString(punchIntCounter);
 		edit.setText(punchCounter);
 		setTotalPrice(totalPriceInstance);
 		setTotalText(totalPriceInstance);
 	}
 
 	/**
 	 * reset the value of flex back to 0.00
 	 */
 	public void flexResetActivity() {
 		// if the current flex value is 0.00, do nothing
 		if (getCurrentFlex() == 0.00) {
 		}
 
 		// if the current flex value is larger than 0.00, set flex to 0.00 and
 		// return the value of flex back to the total remaining
 		else if (getCurrentFlex() > 0.00) {
 			setTotalText(getCurrentFlex() + getTotalPrice());
 			setTotalPrice(getCurrentFlex() + getTotalPrice());
 			setFlexText(0.00);
 			setCurrentFlex(0.00);
 		}
 		// if the current flex value is negative, do nothing
 		else {
 			// current cash value is negative
 		}
 	}
 
 	/**
 	 * reset the value of cash back to 0.00 and return the value of cash back to
 	 * the total remaining
 	 */
 	public void cashResetActivity() {
 		// if the current cash value is 0.00, do nothing
 		if (getCurrentCash() == 0.00) {
 		}
 		// if the current cash value is larger than 0.00, set cash to 0.00 and
 		// return the value of cash back to the total remaining
 		else if (getCurrentCash() > 0.00) {
 			setTotalText(originalTotal
 					- (getPunchIntCounter() * punchValue + getCurrentFlex()));
 			setTotalPrice(originalTotal
 					- (getPunchIntCounter() * punchValue + getCurrentFlex()));
 			setCashText(0.00);
 			setCurrentCash(0.00);
 		}
 		// if the current cash value is negative, do nothing
 		else {
 			// current cash value is negative
 		}
 	}
 
 	/**
 	 * reset the page back to what it looked like when you first got to the page
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
 	 * Sets the total price
 	 * 
 	 * @param totalPrice
 	 *            price to be set
 	 */
 	public void setTotalPrice(double totalPrice) {
 		this.totalPrice = totalPrice;
 	}
 
 	/**
 	 * Sets the TextView to the totalPrice
 	 * 
 	 * @param totalPrice
 	 *            price to set the textview to
 	 */
 	public void setTotalText(double totalPrice) {
 		String formattedCurrentTotalString = formatIntoDecimal().format(
 				totalPrice);
 		total.setText("$" + formattedCurrentTotalString);
 	}
 
 	/**
 	 * Gets the TotalPrice
 	 * 
 	 * @return totalPrice the totalPrice
 	 */
 	public double getTotalPrice() {
 		return totalPrice;
 	}
 
 	/**
 	 * Sets the punchIntCounterTemp to the given variable
 	 * 
 	 * @param punchIntCounterTemp
 	 *            variable to set it to
 	 */
 	public void setPunchIntCounterTemp(int punchIntCounterTemp) {
 		this.punchIntCounterTemp = punchIntCounterTemp;
 	}
 
 	/**
 	 * Gets the punchIntCounterTemp
 	 * 
 	 * @return punchIntCounterTemp the variable being returned
 	 */
 	public int getPunchIntCounterTemp() {
 		return punchIntCounterTemp;
 	}
 
 	/**
 	 * Sets the punchIntCounter
 	 * 
 	 * @param punchIntCounter
 	 *            variable to set it to
 	 */
 	public void setPunchIntCounter(int punchIntCounter) {
 		this.punchIntCounter = punchIntCounter;
 	}
 
 	/**
 	 * Gets the punchIntCounter
 	 * 
 	 * @return punchIntCounter the variable being returned
 	 */
 	public int getPunchIntCounter() {
 		return punchIntCounter;
 	}
 
 	/**
 	 * Handles when punch is pressed and the functionalities of it
 	 */
 	public void punchActivity() {
 		totalPriceInstance = getTotalPrice();
 		EditText edit = (EditText) findViewById(R.id.punchPay_editText);
 		punchCounterTemp = edit.getText().toString();
 		punchIntCounterTemp = Integer.parseInt(punchCounterTemp);
 		punchIntCounter = getPunchIntCounter();
 		// this method is executed because the page prior to CheckoutActivity is
 		// PunchActivity
 		if (flag == 1) {
 			// punch can only be implemented if the current cash and flex is
 			// equal to 0.00
			if ((punchIntCounter == 0) && (punchIntCounterTemp == 0) && (getCurrentCash() == 0.00)
 					&& (getCurrentFlex() == 0.00)) {
 				totalPriceInstance = 0.00;
 				punchIntCounter = 1;
 			}
 			// nothing changes if the entered value of punchIntCounter is larger
 			// than 1 because you can only ;unch once
 			else if (punchIntCounterTemp > 1) {
 				totalPriceInstance = getTotalPrice();
 				punchIntCounter = getPunchIntCounter();
 			}
 			// increment punch by 1
 			else if ((punchIntCounterTemp == 1) && (getCurrentCash() == 0.00)
 					&& (getCurrentFlex() == 0.00)) {
 				totalPriceInstance = 0.00;
 				punchIntCounter = punchIntCounterTemp;
 			}
 			// the entered value must be 0 because it can't be a negative number
 			// 1 punch is taken away from checkout
 			else if (punchIntCounterTemp < 1) {
 				totalPriceInstance = punchValue;
 				punchIntCounter = 0;
 			}
 		}
 		// this method is executed because the page prior to CheckoutActivity is
 		// BrowseActivity
 		else if (flag == 2) {
 			int punchIntDifference;
 
 			// clicking the punchButton will increment punch by 1 only if
 			// totalPriceInstance is larger than $0.00
 			if ((punchIntCounterTemp == punchIntCounter)
 					&& (totalPriceInstance > 0.00)) {
 				totalPriceInstance = totalPriceInstance - punchValue;
 				punchIntCounter = getPunchIntCounter() + 1;
 			}
 			// if the input value of punch is larger than the current value,
 			// punch will account for the change of punchIntCounter
 			else if ((punchIntCounterTemp > punchIntCounter)
 					&& (totalPriceInstance > 0.00)) {
 				double totalPriceInstanceTemp;
 				punchIntDifference = punchIntCounterTemp - punchIntCounter;
 				totalPriceInstanceTemp = totalPriceInstance
 						- (punchValue * punchIntDifference);
 
 				// punch can only implement the change in punchIntCounterTemp if
 				// the value doesn't use necessary punch(es)
 				if (totalPriceInstanceTemp > (-1 * punchValue)) {
 					punchIntCounter = getPunchIntCounter() + punchIntDifference;
 					totalPriceInstance = totalPriceInstanceTemp;
 				}
 				// the user is inputting an excessive amount of punches
 				else {
 					// telling user that they are using excessive punches
 				}
 			}
 			// if the input value of punch is smaller than the current value,
 			// punch will account for the change of punchIntCounter
 			// setting the value of punchIntCounter and totalPriceInstance to
 			// their new values
 			else if (punchIntCounterTemp < punchIntCounter) {
 				punchIntDifference = punchIntCounter - punchIntCounterTemp;
 				totalPriceInstance = totalPriceInstance
 						+ (punchValue * punchIntDifference);
 
 				// if the input value for punch is negative, nothing will happen
 				if (punchIntCounterTemp < 0) {
 					// tell users that they can't input negative value for punch
 				}
 				// if the input value for punch is smaller than the current
 				// punch value, punch will account for the change in
 				// punchIntCounter
 				// setting the value of punchIntCounter and totalPriceInstance
 				// to their new values
 				else {
 					punchIntCounter = getPunchIntCounter() - punchIntDifference;
 				}
 			}
 		}
 		setTotalPrice(totalPriceInstance);
 		setTotalText(totalPriceInstance);
 		setPunchIntCounter(punchIntCounter);
 		punchCounter = Integer.toString(punchIntCounter);
 		edit.setText(punchCounter);
 	}
 
 	/**
 	 * Sets the currentFlex
 	 * 
 	 * @param currentFlex
 	 *            variable to set it to
 	 */
 	public void setCurrentFlex(double currentFlex) {
 		this.currentFlex = currentFlex;
 	}
 
 	/**
 	 * Gets the currentFlex
 	 * 
 	 * @return currentFlex the variable being returned
 	 */
 	public double getCurrentFlex() {
 		return currentFlex;
 	}
 
 	/**
 	 * Sets the TextView flexText to the currentFlex
 	 * 
 	 * @param currentFlex
 	 *            the variable to set the textView to
 	 */
 	public void setFlexText(double currentFlex) {
 		String formattedCurrentFlexString = formatIntoDecimal().format(
 				currentFlex);
 		EditText edit = (EditText) findViewById(R.id.flexPay_editText);
 		edit.setText(formattedCurrentFlexString);
 	}
 
 	/**
 	 * Handles when the flex button is pressed and all the functionalities of it
 	 */
 	public void flexActivity() {
 		double flexDoubleDifference, setNewTotal = 0.00, setNewFlex = 0.00;
 		totalPriceInstance = getTotalPrice();
 
 		EditText edit = (EditText) findViewById(R.id.flexPay_editText);
 		flexValueTemp = edit.getText().toString();
 		flexDoubleValueTemp = Double.parseDouble(flexValueTemp);
 		flexDoubleValue = getCurrentFlex();
 
 		// if the totalPriceInstance is equal to 0.00, continue with
 		// flexActivity
 		if (totalPriceInstance >= 0.00) {
 			// set the value of flexDoubleValue to totalPriceInstance and set
 			// the value of totalPriceInstance to 0.00 if the current value of
			// flexDoubleValueTemp and flexDoubleValue is equal to 0.00
			if ((flexDoubleValue == 0.00) && (flexDoubleValueTemp == 0.00)) {
 				setNewFlex = flexDoubleValue + totalPriceInstance;
 				setNewTotal = 0.00;
 			}
 			// if flexDoubleValueTemp isn't equal to 0.00, continue...
 			else {
 				// if there are no changes in the input value for
 				// flexDoubleValueTemp, nothing is calculated
 				if (flexDoubleValueTemp == flexDoubleValue) {
 					// do nothing since there is no changes in the entered value
 					// for flexDoubleValueTemp
 					setNewTotal = totalPriceInstance;
 					setNewFlex = flexDoubleValueTemp;
 				}
 				// if the new input value for flexDoubleValueTemp is larger than
 				// flexDoubleValue,calculate and set the changes for
 				// flexDoubleValue and totalPriceInstance
 				else if (flexDoubleValueTemp > flexDoubleValue) {
 					flexDoubleDifference = flexDoubleValueTemp
 							- flexDoubleValue;
 					totalPriceInstance = totalPriceInstance
 							- (flexDoubleDifference);
 
 					// if the new totalPriceInstance is larger than 0.00,
 					// continue with the calculation
 					if (totalPriceInstance >= 0.00) {
 						setNewFlex = getCurrentFlex() + flexDoubleDifference;
 						setNewTotal = totalPriceInstance;
 					}
 					// if the new totalPriceInstance is smaller than 0.00, reset
 					// the values for flexDoubleValue
 					else {
 						setNewFlex = flexDoubleValue;
 						setNewTotal = getTotalPrice();
 					}
 				}
 				// if the new input value for flexDoubleValueTemp is smaller
 				// than flexDoubleValue,calculate and set the changes for
 				// flexDoubleValue and totalPriceInstance
 				else if (flexDoubleValueTemp < flexDoubleValue) {
 					flexDoubleDifference = flexDoubleValue
 							- flexDoubleValueTemp;
 					setNewTotal = totalPriceInstance + (flexDoubleDifference);
 					setNewFlex = getCurrentFlex() - flexDoubleDifference;
 				}
 			}
 		}
 		// if the new input value for flexDoubleValueTemp is negative, don't
 		// calculate the new changes
 		else {
 			// do nothing because the current totalPriceInstance is a negative
 			// value
 		}
 		setTotalPrice(setNewTotal);
 		setTotalText(setNewTotal);
 		setCurrentFlex(setNewFlex);
 		setFlexText(setNewFlex);
 	}
 
 	/**
 	 * Sets the currentCash
 	 * 
 	 * @param currentCash
 	 *            the variable to set it to
 	 */
 	public void setCurrentCash(double currentCash) {
 		this.currentCash = currentCash;
 	}
 
 	/**
 	 * Gets the currentCash
 	 * 
 	 * @return currentCash the current cash total
 	 */
 	public double getCurrentCash() {
 		return currentCash;
 	}
 
 	/**
 	 * Sets the textView CashText to the current cash
 	 * 
 	 * @param currentCash
 	 *            the variable to set it to
 	 */
 	public void setCashText(double currentCash) {
 		String formattedCurrentCashString = formatIntoDecimal().format(
 				currentCash);
 		EditText edit = (EditText) findViewById(R.id.cashPay_editText);
 		edit.setText(formattedCurrentCashString);
 	}
 
 	/**
 	 * Handles when the Cash button is pressed and the functionalities of it
 	 */
 	public void cashActivity() {
 		double cashDoubleDifference, setNewTotal = 0.00, setNewCash = 0.00;
 		totalPriceInstance = getTotalPrice();
 		EditText edit = (EditText) findViewById(R.id.cashPay_editText);
 		cashValue = edit.getText().toString();
 		cashDoubleValueTemp = Double.parseDouble(cashValue);
 		cashDoubleValue = getCurrentCash();
 
 		// if the totalPriceInstance is equal to 0.00, continue with
 		// cashActivity
 		if (totalPriceInstance >= 0.00) {
 			// set the value of cashDoubleValue to totalPriceInstance and set
 			// the value of totalPriceInstance to 0.00 if the current value of
			// cashDoubleValueTemp and cashDoubleValue is equal to 0.00
			if ((cashDoubleValueTemp == 0.00) && (cashDoubleValue == 0.00)) {
 				setNewCash = ((cashDoubleValue + totalPriceInstance) * tax);
 				setNewTotal = 0.00;
 			}
 			// if cashDoubleValueTemp isn't equal to 0.00, continue...
 			else {
 				// if there are no changes in the input value for
 				// cashDoubleValueTemp, nothing is calculated
 				if (cashDoubleValueTemp == cashDoubleValue) {
 					setNewCash = cashDoubleValueTemp;
 					setNewTotal = totalPriceInstance;
 					// do nothing if there are no changes in the entered value
 				}
 				// if the new input value for cashDoubleValueTemp is larger than
 				// cashDoubleValue,calculate and set the changes for
 				// cashDoubleValue and totalPriceInstance
 				else if (cashDoubleValueTemp > cashDoubleValue) {
 					cashDoubleDifference = cashDoubleValueTemp
 							- cashDoubleValue;
 					totalPriceInstance = totalPriceInstance
 							- (cashDoubleDifference * removeTax);
 
 					// if the new totalPriceInstance is larger than 0.00,
 					// continue with the calculation
 					if (totalPriceInstance >= 0.00) {
 						setNewCash = getCurrentCash() + cashDoubleDifference;
 						setNewTotal = totalPriceInstance;
 					}
 					// if the new totalPriceInstance is smaller than 0.00, reset
 					// the values for flexDoubleValue
 					else {
 						// do nothing because the new totalPriceInstance is
 						// negative
 						setNewCash = flexDoubleValue;
 						setNewTotal = getTotalPrice();
 					}
 				}
 				// if the new input value for cashDoubleValueTemp is smaller
 				// than cashDoubleValue,calculate and set the changes for
 				// cashDoubleValue and totalPriceInstance
 				else if (cashDoubleValueTemp < cashDoubleValue) {
 					cashDoubleDifference = cashDoubleValue
 							- cashDoubleValueTemp;
 					setNewTotal = totalPriceInstance
 							+ (cashDoubleDifference * removeTax);
 					setNewCash = getCurrentCash() - cashDoubleDifference;
 				}
 			}
 		}
 		// if the new input value for flexDoubleValueTemp is negative, don't
 		// calculate the new changes
 		else {
 			// do nothing because the current totalPriceInstance is a negative
 			// value
 		}
 		setTotalPrice(setNewTotal);
 		setTotalText(setNewTotal);
 		setCurrentCash(setNewCash);
 		setCashText(setNewCash);
 	}
 
 }
