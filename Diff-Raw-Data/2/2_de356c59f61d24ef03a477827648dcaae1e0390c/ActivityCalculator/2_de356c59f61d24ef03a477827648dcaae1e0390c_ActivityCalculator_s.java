 package com.pk.personalcalculator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ActivityCalculator extends Activity
 {
 	
 	// Fonts used to Google Now theme.
 	Typeface robotoThin;
 	Typeface robotoBoldCondensed;
 	
 	// Buttons...
 	Button btnClear;
 	Button btn0;
 	Button btn1;
 	Button btn2;
 	Button btn3;
 	Button btn4;
 	Button btn5;
 	Button btn6;
 	Button btn7;
 	Button btn8;
 	Button btn9;
 	
 	// The EditText field
 	EditText text;
 	
 	MenuItem mItemDebug;
 	
 	// Called when activity starts
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calculator);
 		
 		initializeUI();
		lockdown();
 	}
 	
 	// Create ActionBar menu options
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.action_menu, menu);
 		
 		this.mItemDebug = menu.findItem(R.id.action_debug);
 		
 		// If on debug mode, let us debug!
 		if(ActivityMain.DebugMode)
 			mItemDebug.setVisible(true);
 		else
 			mItemDebug.setVisible(false);
 		
 		return true;
 	}
 	
 	// Select what to do once ActionBar option is touched.
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.action_manage:
 				Intent manageIntent = new Intent(ActivityCalculator.this, ActivityManage.class);
 				startActivity(manageIntent);
 				return true;
 			case R.id.action_settings:
 				Intent settingsIntent = new Intent(ActivityCalculator.this, ActivitySettings.class);
 				startActivity(settingsIntent);
 				return true;
 			case R.id.action_debug:
 				startActivity(new Intent(ActivityCalculator.this, ActivityDebug.class));
 			default:
 				
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	// Initialize all UI objects and set theme if needed
 	public void initializeUI()
 	{
 		robotoThin = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
 		robotoBoldCondensed = Typeface.createFromAsset(getAssets(), "Roboto-BoldCondensed.ttf");
 		
 		btnClear = (Button) findViewById(R.id.btnClear);
 		btn0 = (Button) findViewById(R.id.btn0);
 		btn1 = (Button) findViewById(R.id.btn1);
 		btn2 = (Button) findViewById(R.id.btn2);
 		btn3 = (Button) findViewById(R.id.btn3);
 		btn4 = (Button) findViewById(R.id.btn4);
 		btn5 = (Button) findViewById(R.id.btn5);
 		btn6 = (Button) findViewById(R.id.btn6);
 		btn7 = (Button) findViewById(R.id.btn7);
 		btn8 = (Button) findViewById(R.id.btn8);
 		btn9 = (Button) findViewById(R.id.btn9);
 		text = (EditText) findViewById(R.id.Input);
 		
 		btnClear.setTypeface(robotoBoldCondensed);
 		btn0.setTypeface(robotoThin);
 		btn1.setTypeface(robotoThin);
 		btn2.setTypeface(robotoThin);
 		btn3.setTypeface(robotoThin);
 		btn4.setTypeface(robotoThin);
 		btn5.setTypeface(robotoThin);
 		btn6.setTypeface(robotoThin);
 		btn7.setTypeface(robotoThin);
 		btn8.setTypeface(robotoThin);
 		btn9.setTypeface(robotoThin);
 		text.setTypeface(robotoThin);
 	}
 	
 	// Lock down the system
 	public void lockdown()
 	{
 		View disableStatusBar = new View(ActivityCalculator.this);
 		
 		WindowManager.LayoutParams handleParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, 50,
 		// This allows the view to be displayed over the status bar
 		WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
 		// this is to keep button presses going to the background window
 		WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
 		// this is to enable the notification to receive touch events
 		WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
 		// Draws over status bar
 		WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
 		
 		handleParams.gravity = Gravity.TOP;
 		getWindow().addContentView(disableStatusBar, handleParams);
 	}
 	
 	// This method will determine what will happen when a button is pressed.
 	public void buttonClickHandler(View v)
 	{
 		switch (v.getId())
 		{
 			case R.id.btn0:
 				text.append("0");
 				break;
 			case R.id.btn1:
 				text.append("1");
 				break;
 			case R.id.btn2:
 				text.append("2");
 				break;
 			case R.id.btn3:
 				text.append("3");
 				break;
 			case R.id.btn4:
 				text.append("4");
 				break;
 			case R.id.btn5:
 				text.append("5");
 				break;
 			case R.id.btn6:
 				text.append("6");
 				break;
 			case R.id.btn7:
 				text.append("7");
 				break;
 			case R.id.btn8:
 				text.append("8");
 				break;
 			case R.id.btn9:
 				text.append("9");
 				break;
 			case R.id.btnPlus:
 				text.append("+");
 				break;
 			case R.id.btnMinus:
 				text.append("-");
 				break;
 			case R.id.btnMultiply:
 				text.append("*");
 				break;
 			case R.id.btnDivide:
 				text.append("/");
 				break;
 			case R.id.btnLeftP:
 				text.append("(");
 				break;
 			case R.id.btnRightP:
 				text.append(")");
 				break;
 			case R.id.btnClear:
 				text.setText("");
 				break;
 			case R.id.btnEqual:
 				solve(); // Stub method; incomplete but skeletally functional.
 				break;
 			case R.id.btnSwitch: // Code for the additive inverse of a number goes
 									// here.;
 				break;
 			case R.id.btnDot:
 				if (!text.getText().toString().contains("."))
 				{
 					text.append(".");
 				}
 				break;
 		}
 	}
 	
 	// The method will take the text view "Input", analyze it, and construct an
 	// equation that the machine will solve;
 	// The resulting solution will be set in the text view afterwards.
 	public void solve()
 	{
 		// Code for solve goes here
 	}
 }
