 package com.pk.personalcalculator;
 
 import org.javia.arity.Complex;
 import org.javia.arity.Symbols;
 import org.javia.arity.SyntaxException;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.PixelFormat;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.format.DateUtils;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ActivityCalculator extends Activity
 {
 	private SharedPreferences prefs;
 	private int selectedTheme;
 	ActionBar actionBar;
 	Intent intent;
 	
 	// Fonts used to Google Now theme.
 	Typeface robotoThin;
 	Typeface robotoBoldCondensed;
 	
 	// Buttons...
 	ImageButton btnExpand;
 	Button btnClear;
 	Button btnDelete;
 	Button btnEqual;
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
 	Button btnLeftP;
 	Button btnRightP;
 	Button btnDivide;
 	Button btnMultiply;
 	Button btnMinus;
 	Button btnPlus;
 	Button btnDot;
 	Button btnSwitch;
 	
 	// The text view form and container
 	TextView textInput;
 	RelativeLayout inputContainer;
 	
 	// Lockdown Stuff
 	LinearLayout infoLockdown;
 	TextView textLock;
 	int Hours;
 	int Minutes;
 	CountDownTimer mCountDownTimer;
 	long mInitialTime;
 	
 	//String builder to build the equation
 	StringBuilder textString = new StringBuilder();
 	
 	//Resource strings
 	String plusSign;
 	String minusSign;
 	String multiplySign;
 	String divideSign;
 	String leftParSign;
 	String rightParSign;
 	
 	MenuItem mItemDebug;
 	
 	// Called when activity starts
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		intent = getIntent();
 		firstLock();
 		setContentView(R.layout.activity_calculator);
 		
 		prefs = getSharedPreferences("PersonalCalculatorPreferences", 0);
 		selectedTheme = prefs.getInt("Theme", 0);
 		actionBar = getActionBar();
 		
 		initializeUI();
 		initializeSigns();
 		
 		if (intent.hasExtra("Lockdown"))
 		{
 			Hours = intent.getIntExtra("Hours", 0);
 			Minutes = intent.getIntExtra("Minutes", 0);
 			
 			mInitialTime = DateUtils.HOUR_IN_MILLIS * Hours + DateUtils.MINUTE_IN_MILLIS * Minutes;
 			mCountDownTimer = new CountDownTimer(mInitialTime, 1000)
 			{
 				StringBuilder time = new StringBuilder();
 				
 				@Override
 				public void onFinish()
 				{
 					textLock.setText(DateUtils.formatElapsedTime(0));
 					// RESTORE SYSTEM HERE... later because I'm too lazy
 				}
 				
 				@Override
 				public void onTick(long millisUntilFinished)
 				{
 					time.setLength(0);
 					if (millisUntilFinished > DateUtils.HOUR_IN_MILLIS)
 					{
 						long count = millisUntilFinished / DateUtils.HOUR_IN_MILLIS;
 						if (count > 1)
 							time.append(count).append(" hours ");
 						else
 							time.append(count).append(" hour ");
 						
 						millisUntilFinished %= DateUtils.DAY_IN_MILLIS;
 					}
 					if (millisUntilFinished > DateUtils.MINUTE_IN_MILLIS)
 					{
 						long count = millisUntilFinished / DateUtils.MINUTE_IN_MILLIS % 60;
 						if (count > 1)
 							time.append(count).append(" minutes ");
 						else
 							time.append(count).append(" minutes ");
 						
 						millisUntilFinished %= DateUtils.MINUTE_IN_MILLIS;
 					}
 					if (millisUntilFinished > DateUtils.SECOND_IN_MILLIS)
 					{
 						long count = millisUntilFinished / DateUtils.SECOND_IN_MILLIS;
 						if (count > 1)
 							time.append(count).append(" seconds ");
 						else
 							time.append(count).append(" second ");
 						
 						millisUntilFinished %= DateUtils.SECOND_IN_MILLIS;
 					}
 					
 					textLock.setText(time.toString());
 				}
 			}.start();
 			
 			if (intent.getBooleanExtra("Lockdown", false))
 				lockdown();
 		}
 	}
 	
 	@Override
 	protected void onRestart()
 	{
 		super.onRestart();
 		
 		selectedTheme = prefs.getInt("Theme", 0);
 		setCalculatorTheme(selectedTheme);
 	}
 	
 	// Create ActionBar menu options
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.action_menu, menu);
 		
 		this.mItemDebug = menu.findItem(R.id.action_debug);
 		
 		// If on debug mode, let us debug!
 		if (ActivityMain.DebugMode)
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
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		// Override Back Button
 		if (keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		// Override Menu Button
 		else if (keyCode == KeyEvent.KEYCODE_MENU)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		// Override Menu Button
 		else if (keyCode == KeyEvent.KEYCODE_HOME)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	/*
 	 * @Override public void onAttachedToWindow() { // Override home button
 	 * this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
 	 * super.onAttachedToWindow();
 	 * 
 	 * if(!(intent.hasExtra("Lockdown")) || !(intent.getBooleanExtra("Lockdown",
 	 * false))) { // If not in lockdown, act like normal moveTaskToBack(true); }
 	 * else Toast.makeText(ActivityCalculator.this,
 	 * "You can't go home during lockdown mode. Wait for the timer to end.",
 	 * Toast.LENGTH_LONG).show(); }
 	 */
 	
 	@Override
 	public boolean onKeyLongPress(int keyCode, KeyEvent event)
 	{
 		// Override Back Button
 		if (keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		// Override Menu Button
 		else if (keyCode == KeyEvent.KEYCODE_MENU)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		// Override Menu Button
 		else if (keyCode == KeyEvent.KEYCODE_HOME)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		return super.onKeyLongPress(keyCode, event);
 	}
 	
 	@Override
 	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event)
 	{
 		if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 		{
 			Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 			return true;
 		}
 		else
 			return super.onKeyMultiple(keyCode, repeatCount, event);
 	}
 	
 	@Override
 	public boolean onKeyShortcut(int keyCode, KeyEvent event)
 	{
 		if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 		{
 			Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 			return true;
 		}
 		else
 			return super.onKeyShortcut(keyCode, event);
 	}
 	
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event)
 	{
 		// Override Back Button
 		if (keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		// Override Menu Button
 		else if (keyCode == KeyEvent.KEYCODE_MENU)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		// Override Menu Button
 		else if (keyCode == KeyEvent.KEYCODE_HOME)
 		{
 			if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 			{
 				Toast.makeText(ActivityCalculator.this, "You can't go back during lockdown mode. Wait for the timer to end.", Toast.LENGTH_LONG).show();
 				return true;
 			}
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 	
 	// Initialize all UI objects and set theme if needed
 	public void initializeUI()
 	{
 		robotoThin = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
 		robotoBoldCondensed = Typeface.createFromAsset(getAssets(), "Roboto-BoldCondensed.ttf");
 		
 		btnExpand = (ImageButton) findViewById(R.id.btnExpand);
 		btnClear = (Button) findViewById(R.id.btnClear);
 		btnDelete = (Button) findViewById(R.id.btnDelete);
 		btnEqual = (Button) findViewById(R.id.btnEqual);
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
 		btnLeftP = (Button) findViewById(R.id.btnLeftP);
 		btnRightP = (Button) findViewById(R.id.btnRightP);
 		btnDivide = (Button) findViewById(R.id.btnDivide);
 		btnMultiply = (Button) findViewById(R.id.btnMultiply);
 		btnMinus = (Button) findViewById(R.id.btnMinus);
 		btnPlus = (Button) findViewById(R.id.btnPlus);
 		btnDot = (Button) findViewById(R.id.btnDot);
 		btnSwitch = (Button) findViewById(R.id.btnSwitch);
 		textInput = (TextView) findViewById(R.id.Input);
 		inputContainer = (RelativeLayout) findViewById(R.id.inputContainer);
 		
 		textInput.setText("");
 		
		//setCalculatorTheme(selectedTheme);
 	}
 
 	
 	// Set calculator theme
 	public void setCalculatorTheme(int theme)
 	{
 
 		if (theme == 1)
 		{
 			// Google Now theme
 			textInput.setTextSize(R.dimen.theme2_input);
 		}
 		if (theme == 0)
 		{
 			// Default Light Theme
 			textInput.setTextColor(getResources().getColor(R.color.black_light));
 			inputContainer.setBackgroundColor(getResources().getColor(R.color.transparent));
 			
 			setTheme(R.style.AppTheme);
 			textInput.setTextColor(getResources().getColor(R.color.black_light));
 			inputContainer.setBackgroundColor(getResources().getColor(R.color.transparent));
 			infoLockdown.setBackgroundColor(getResources().getColor(R.color.transparent));
 			
 			btnExpand.setImageResource(R.drawable.expand);
 			btnDelete.setTextColor(getResources().getColor(R.color.black));
 			btnEqual.setTextColor(getResources().getColor(R.color.black));
 			btnClear.setTextColor(getResources().getColor(R.color.black));
 			btnLeftP.setTextColor(getResources().getColor(R.color.black));
 			btnRightP.setTextColor(getResources().getColor(R.color.black));
 			btnDivide.setTextColor(getResources().getColor(R.color.black));
 			btnMultiply.setTextColor(getResources().getColor(R.color.black));
 			btnMinus.setTextColor(getResources().getColor(R.color.black));
 			btnPlus.setTextColor(getResources().getColor(R.color.black));
 			btnDot.setTextColor(getResources().getColor(R.color.black));
 			btnSwitch.setTextColor(getResources().getColor(R.color.black));
 			btn0.setTextColor(getResources().getColor(R.color.black));
 			btn1.setTextColor(getResources().getColor(R.color.black));
 			btn2.setTextColor(getResources().getColor(R.color.black));
 			btn3.setTextColor(getResources().getColor(R.color.black));
 			btn4.setTextColor(getResources().getColor(R.color.black));
 			btn5.setTextColor(getResources().getColor(R.color.black));
 			btn6.setTextColor(getResources().getColor(R.color.black));
 			btn7.setTextColor(getResources().getColor(R.color.black));
 			btn8.setTextColor(getResources().getColor(R.color.black));
 			btn9.setTextColor(getResources().getColor(R.color.black));
 		}
 		else if (theme == 1)
 		{
 			// Default Dark Theme
 			
 			textInput.setTextColor(getResources().getColor(R.color.white));
 			inputContainer.setBackgroundColor(getResources().getColor(R.color.black));
 			
 			setTheme(R.style.ActionBar_Dark);
 			textInput.setTextColor(getResources().getColor(R.color.white));
 			inputContainer.setBackgroundColor(getResources().getColor(R.color.black));
 			infoLockdown.setBackgroundColor(getResources().getColor(R.color.black));
 			
 			btnExpand.setBackgroundResource(R.drawable.border_light_selector);
 			btnDelete.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnEqual.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnClear.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnLeftP.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnRightP.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnDivide.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnMultiply.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnMinus.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnPlus.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnSwitch.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btnDot.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn0.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn1.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn2.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn3.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn4.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn5.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn6.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn7.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn8.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			btn9.setBackgroundResource(R.drawable.button_holodark_selector_light);
 			
 			btnExpand.setImageResource(R.drawable.expand_light);
 			btnDelete.setTextColor(getResources().getColor(R.color.white));
 			btnEqual.setTextColor(getResources().getColor(R.color.white));
 			btnSwitch.setTextColor(getResources().getColor(R.color.white));
 			btnClear.setTextColor(getResources().getColor(R.color.white));
 			btnLeftP.setTextColor(getResources().getColor(R.color.white));
 			btnRightP.setTextColor(getResources().getColor(R.color.white));
 			btnDivide.setTextColor(getResources().getColor(R.color.white));
 			btnMultiply.setTextColor(getResources().getColor(R.color.white));
 			btnMinus.setTextColor(getResources().getColor(R.color.white));
 			btnPlus.setTextColor(getResources().getColor(R.color.white));
 			btnDot.setTextColor(getResources().getColor(R.color.white));
 			btn0.setTextColor(getResources().getColor(R.color.white));
 			btn1.setTextColor(getResources().getColor(R.color.white));
 			btn2.setTextColor(getResources().getColor(R.color.white));
 			btn3.setTextColor(getResources().getColor(R.color.white));
 			btn4.setTextColor(getResources().getColor(R.color.white));
 			btn5.setTextColor(getResources().getColor(R.color.white));
 			btn6.setTextColor(getResources().getColor(R.color.white));
 			btn7.setTextColor(getResources().getColor(R.color.white));
 			btn8.setTextColor(getResources().getColor(R.color.white));
 			btn9.setTextColor(getResources().getColor(R.color.white));
 			
 		}
 		else if (theme == 2)
 		{
 			// Google Now Theme
 			setTheme(R.style.AppTheme);
 			textInput.setTextSize(35);
 			
 			textInput.setTextColor(getResources().getColor(R.color.black_light));
 			inputContainer.setBackgroundResource(getResources().getColor(R.color.transparent));
 			
 			textInput.setTextColor(getResources().getColor(R.color.black_light));
 			inputContainer.setBackgroundResource(getResources().getColor(R.color.transparent));
 			infoLockdown.setBackgroundColor(getResources().getColor(R.color.transparent));
 			
 			btnExpand.setBackgroundResource(R.drawable.border_selector);
 			btnDelete.setBackgroundResource(R.drawable.item_selector);
 			btnEqual.setBackgroundResource(R.drawable.item_selector);
 			btnClear.setBackgroundResource(R.drawable.item_selector);
 			btnLeftP.setBackgroundResource(R.drawable.item_selector);
 			btnRightP.setBackgroundResource(R.drawable.item_selector);
 			btnDivide.setBackgroundResource(R.drawable.item_selector);
 			btnMultiply.setBackgroundResource(R.drawable.item_selector);
 			btnMinus.setBackgroundResource(R.drawable.item_selector);
 			btnPlus.setBackgroundResource(R.drawable.item_selector);
 			btnDot.setBackgroundResource(R.drawable.item_selector);
 			btnSwitch.setBackgroundResource(R.drawable.item_selector);
 			btn0.setBackgroundResource(R.drawable.item_selector);
 			btn1.setBackgroundResource(R.drawable.item_selector);
 			btn2.setBackgroundResource(R.drawable.item_selector);
 			btn3.setBackgroundResource(R.drawable.item_selector);
 			btn4.setBackgroundResource(R.drawable.item_selector);
 			btn5.setBackgroundResource(R.drawable.item_selector);
 			btn6.setBackgroundResource(R.drawable.item_selector);
 			btn7.setBackgroundResource(R.drawable.item_selector);
 			btn8.setBackgroundResource(R.drawable.item_selector);
 			btn9.setBackgroundResource(R.drawable.item_selector);
 			
 			btnExpand.setImageResource(R.drawable.expand);
 			btnDelete.setTextColor(getResources().getColor(R.color.black));
 			btnEqual.setTextColor(getResources().getColor(R.color.black));
 			btnClear.setTextColor(getResources().getColor(R.color.black));
 			btnLeftP.setTextColor(getResources().getColor(R.color.black));
 			btnRightP.setTextColor(getResources().getColor(R.color.black));
 			btnDivide.setTextColor(getResources().getColor(R.color.black));
 			btnMultiply.setTextColor(getResources().getColor(R.color.black));
 			btnMinus.setTextColor(getResources().getColor(R.color.black));
 			btnPlus.setTextColor(getResources().getColor(R.color.black));
 			btnDot.setTextColor(getResources().getColor(R.color.black));
 			btnSwitch.setTextColor(getResources().getColor(R.color.black));
 			btn0.setTextColor(getResources().getColor(R.color.black));
 			btn1.setTextColor(getResources().getColor(R.color.black));
 			btn2.setTextColor(getResources().getColor(R.color.black));
 			btn3.setTextColor(getResources().getColor(R.color.black));
 			btn4.setTextColor(getResources().getColor(R.color.black));
 			btn5.setTextColor(getResources().getColor(R.color.black));
 			btn6.setTextColor(getResources().getColor(R.color.black));
 			btn7.setTextColor(getResources().getColor(R.color.black));
 			btn8.setTextColor(getResources().getColor(R.color.black));
 			btn9.setTextColor(getResources().getColor(R.color.black));
 			
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
 			textInput.setTypeface(robotoThin);
 		}
 	}
 	
 	// I have to do this before setting content
 	public void firstLock()
 	{
 		if (intent.hasExtra("Lockdown") && intent.getBooleanExtra("Lockdown", false))
 		{
 			// Hide the status bar
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 	}
 	
 	// Lock down the system
 	public void lockdown()
 	{
 		// Show lockdown info
 		infoLockdown.setVisibility(View.VISIBLE);
 		
 		// Dim system bar
 		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 		
 		// Hide system bar
 		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
 		
 		// Disable the status bar
 		View disableStatusBar = new View(ActivityCalculator.this);
 		WindowManager.LayoutParams handleParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, 50, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
 		handleParams.gravity = Gravity.TOP;
 		getWindow().addContentView(disableStatusBar, handleParams);
 	}
 
 	
 	// Obtains the string resources for use with buttonClick
 	public void initializeSigns()
 	{
 		plusSign = getResources().getString(R.string.plus);
 		minusSign = getResources().getString(R.string.minus);
 		multiplySign = getResources().getString(R.string.multiply);
 		divideSign = getResources().getString(R.string.divide);
 		leftParSign = getResources().getString(R.string.leftPar);
 		rightParSign = getResources().getString(R.string.rightPar);
 	}
 	
 	public boolean isASign(Character c)
 	{
 		if (c.toString().equals(plusSign) || c.toString().equals(minusSign) || c.toString().equals(multiplySign) || c.toString().equals(divideSign))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 		
 	}
 	
 	public int lastCharIndex()
 	{
 		int index;
 		
 		if (textString.toString().lastIndexOf(plusSign) != -1)
 		{
 			index = textString.toString().lastIndexOf(plusSign);
 		}
 		else if (textString.toString().lastIndexOf(minusSign) != -1)
 		{
 			index = textString.toString().lastIndexOf(minusSign);
 		}
 		else if (textString.toString().lastIndexOf(multiplySign) != -1)
 		{
 			index = textString.toString().lastIndexOf(multiplySign);
 		}
 		else if (textString.toString().lastIndexOf(divideSign) != -1)
 		{
 			index = textString.toString().lastIndexOf(divideSign);
 		}
 		else if (textString.toString().lastIndexOf(leftParSign) != -1)
 		{
 			index = textString.toString().lastIndexOf(leftParSign);
 		}
 		else
 		{
 			index = 0;
 		}
 		
 		return index;
 	}
 	
 	// This method will determine what will happen when a button is pressed.
 	public void buttonClick(View v)
 	{
 		switch (v.getId())
 		{
 			case R.id.btn0:
 				textString.append(0);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn1:
 				textString.append(1);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn2:
 				textString.append(2);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn3:
 				textString.append(3);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn4:
 				textString.append(4);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn5:
 				textString.append(5);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn6:
 				textString.append(6);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn7:
 				textString.append(7);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn8:
 				textString.append(8);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btn9:
 				textString.append(9);
 				textInput.setText(textString.toString());
 				break;
 			case R.id.btnPlus:
 			{
 				if (!(textInput.getText().toString().isEmpty()))
 				{
 					if (!(textInput.getText().toString().isEmpty()))
 					{
 						Character lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 						if (lastChar.toString().equals(leftParSign) || isASign(lastChar))
 						{
 							textString.deleteCharAt(textString.length() - 1);
 							
 						}
 						
 					}
 					
 					textString.append(plusSign);
 					textInput.setText(textString.toString());
 					
 				}
 				break;
 			}
 			case R.id.btnMinus:
 			{
 				
 				if (!(textInput.getText().toString().isEmpty()))
 				{
 					
 					if (!(textInput.getText().toString().isEmpty()))
 					{
 						
 						Character lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 						if (lastChar.toString().equals(leftParSign) || isASign(lastChar))
 						{
 							textString.deleteCharAt(textString.length() - 1);
 						}
 						textString.append(minusSign);
 						textInput.setText(textString.toString());
 					}
 					
 				}
 				break;
 			}
 			case R.id.btnMultiply:
 			{
 				
 				if (!(textInput.getText().toString().isEmpty()))
 				{
 					
 					if (!(textInput.getText().toString().isEmpty()))
 					{
 						
 						Character lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 						if (lastChar.toString().equals(leftParSign) || isASign(lastChar))
 						{
 							textString.deleteCharAt(textString.length() - 1);
 						}
 						textString.append(multiplySign);
 						textInput.setText(textString.toString());
 					}
 				}
 				break;
 			}
 			case R.id.btnDivide:
 			{
 				
 				if (!(textInput.getText().toString().isEmpty()))
 				{
 					
 					if (!(textInput.getText().toString().isEmpty()))
 					{
 						
 						Character lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 						if (lastChar.toString().equals(leftParSign) || isASign(lastChar))
 						{
 							textString.deleteCharAt(textString.length() - 1);
 						}
 						textString.append(divideSign);
 						textInput.setText(textString.toString());
 					}
 				}
 				break;
 			}
 			
 			case R.id.btnLeftP:
 			{
 				Character lastChar = null;
 				if (!(textString.toString().isEmpty()))
 				{
 					lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 					
 					if (isASign(lastChar))
 					{
 						textString.append(getResources().getString(R.string.leftPar));
 						textInput.setText(textString.toString());
 					}
 					break;
 				}
 				else
 				{
 					textString.append(getResources().getString(R.string.leftPar));
 					textInput.setText(textString.toString());
 				}
 			}
 			case R.id.btnRightP:
 			{
 				if ((textString.toString().contains("(")))
 				{
 					Character lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 					if (!(lastChar.toString().equals("(")))
 					{
 						if (isASign(lastChar))
 						{
 							textString.deleteCharAt(textString.length() - 1);
 						}
 						textString.append(rightParSign);
 						textInput.setText(textString.toString());
 					}
 				}
 				break;
 			}
 			case R.id.btnClear:
 				textString.setLength(0);
 				textInput.setText("");
 				break;
 			case R.id.btnDelete:
 				if (!(textInput.getText().toString().isEmpty()))
 				{
 					textString.deleteCharAt(textString.length() - 1);
 					textInput.setText(textString.toString());
 				}
 				break;
 			case R.id.btnEqual:
 			{
 				
 				String solutionText;
 				try
 				{
 					solutionText = solve(textString.toString()); // Has some function at the moment;
 					textString.setLength(0);
 					textString.append(solutionText);
 				}
 				catch (SyntaxException e)
 				{
 					textString.setLength(0);
 					textString.append("Syntax Error: try again.");
 				}
 				
 				textInput.setText(textString.toString());
 			}
 				
 				break;
 			case R.id.btnSwitch:
 			{
 				if (lastCharIndex() != 0)
 				{
 					// Code for the additive inverse of a number goes
 				}
 			}
 									// here.;
 				break;
 			case R.id.btnDot:
 			{
 				if (!textString.toString().isEmpty())
 				{
 					int subStart = lastCharIndex();
 					String subString = textString.toString().substring(subStart);
 					if (!subString.contains("."))
 					{
 						textString.append(".");
 						textInput.setText(textString.toString());
 					}	
 					break;
 				}
 				else
 				{
 					textString.append(".");
 					textInput.setText(textString.toString());
 				}
 			}
 		}
 	}
 	
 	
 	public String solve(String equation) throws SyntaxException
 	{
 		Symbols eSymbols = new Symbols();
 		
 		Character lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 		
 		if (isASign(lastChar))
 		{
 			
 			textString.deleteCharAt(textString.length() - 1);
 			lastChar = Character.valueOf(textString.charAt(textInput.length() - 1));
 			equation = textString.toString();
 		}
 		
 		equation.replace('\u2212', '-');
 		equation.replace('\u00d7', '*');
 		equation.replace('\u00f7', '/');
 		equation.replace('\u0028', '(');
 		equation.replace('\u0029', ')');
 		
 		Complex value = eSymbols.evalComplex(equation);
 		
 		double solution = value.re;
 		
 		String stringSolution = "" + solution;
 		return stringSolution;
 	}
 }
