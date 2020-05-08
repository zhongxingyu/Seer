 /***********************************************************************
  * 
  * PROGRAM:	E-85 Calculator Android App
  * DESC:	My first android app, helps a user decide if E-85 or gas
  * 			is the better deal considering the lowered MPG.
  * FILE:	MainActivity.java - The starting point of the app.
  * AUTHOR: 	Ryan Boykin
  * DATE:	May 2013
  * 
  **********************************************************************/
 
 package com.theyapps.e85calc;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity 
 {
 	private EditText 	edt_e85MPG;
 	private EditText 	edt_gasMPG;
 	private EditText 	edt_e85Price;
 	private EditText 	edt_gasPrice;
 	private TextView 	txt_result;
 	private Button		btn_submit;
 	private Button		btn_reset;
 	private int			i_distUnit;
 	private int			i_fuelUnit;
 	private int			i_currency;
 	
 	static 	SharedPreferences 			settings;
 	static 	SharedPreferences.Editor 	editor;
 	
 	//private MenuItem	mi_About;
 	
 	/**
 	 * Actions for when MainActivity is first created.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
		edt_e85MPG 		= (EditText)findViewById(R.id.e85MPG);
		edt_gasMPG 		= (EditText)findViewById(R.id.gasMPG);
		edt_e85Price 	= (EditText)findViewById(R.id.e85Price);
		edt_gasPrice 	= (EditText)findViewById(R.id.gasPrice);
		txt_result 		= (TextView)findViewById(R.id.result);
 		btn_submit		= (Button)  findViewById(R.id.submit);
 		btn_reset		= (Button)	findViewById(R.id.reset);
 		
		settings 	= getSharedPreferences("user-pref", MODE_PRIVATE);
		//editor 		= settings.edit();
 		
 		// BUTTON LISTENERS
 		btn_submit.setOnClickListener(new OnClickListener() 
 	    {
 	        @Override
 			public void onClick(View v) 
 	        {
 	        	onCalcBtnPress();
 	        }
 	    });
 		
 		btn_reset.setOnClickListener(new OnClickListener() 
 	    {
 	        @Override
 			public void onClick(View v) 
 	        {
 	        	onResetBtnPress();
 	        }
 	    });
 	}
 	
 	/**
 	 * Items to be done when MainActivity is resumed.
 	 */
 	@Override
 	protected void onResume() 
 	{
 		super.onResume();
		i_distUnit  = settings.getInt("distUnit",  0);
		i_fuelUnit  = settings.getInt("fuelUnit",  0);
		i_currency 	= settings.getInt("currency",  0);
 		
 		edt_e85MPG.setHint("E-85 " + AppGlobal.distUnitString[i_distUnit] + "'s per " + AppGlobal.fuelUnitString[i_fuelUnit]);
 		edt_gasMPG.setHint("Gas " +  AppGlobal.distUnitString[i_distUnit] + "'s per " + AppGlobal.fuelUnitString[i_fuelUnit]);
 		edt_e85Price.setHint("E-85 Cost (" + AppGlobal.currencySymbol[i_currency] + ")");
 		edt_gasPrice.setHint("Gas Cost (" + AppGlobal.currencySymbol[i_currency] + ")");
 	}
 
 	/**
 	 * Janitorial clean up.
 	 */
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 		// Any clean up stuff...
 	}
 	
 	/**
 	 * Set up actions for the menu.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 	    // Handle item selection
 	    switch (item.getItemId()) 
 	    {
 	    case R.id.about:
 	    	startActivity(new Intent(this, AboutActivity.class));
 	        return true;
 	    case R.id.settings:
 	    	startActivity(new Intent(this, SettingsActivity.class));
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	
 	/**
 	 * Create the menu.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		// show menu
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/**
 	 * Actions to perform when the user presses "Calculate"	
 	 */
 	public void onCalcBtnPress()
 	{
 		String	s_e85MPG 	= edt_e85MPG.getText().toString();
 		String	s_gasMPG 	= edt_gasMPG.getText().toString();
 		String	s_e85Price 	= edt_e85Price.getText().toString();
 		String 	s_gasPrice 	= edt_gasPrice.getText().toString();
 		
 		double 	d_e85MPG;
 		double 	d_gasMPG;
 		double 	d_e85Price;
 		double 	d_gasPrice;
 		
 		// Lower the keyboard
 		InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
 		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		
 		if(!(s_e85MPG.matches("")||s_gasMPG.matches("")||s_e85Price.matches("")||s_gasPrice.matches("")))
 		{
 			d_e85MPG 	= Double.valueOf(s_e85MPG);
 			d_gasMPG 	= Double.valueOf(s_gasMPG);
 			d_e85Price 	= Double.valueOf(s_e85Price);
 			d_gasPrice 	= Double.valueOf(s_gasPrice);
 			if((d_e85Price / d_e85MPG) < (d_gasPrice / d_gasMPG))
 			{
 				txt_result.setText(getString(R.string.result_e85));
 			}
 			else if ((d_e85Price / d_e85MPG) > (d_gasPrice / d_gasMPG))
 			{
 				txt_result.setText(getString(R.string.result_gas));
 			}
 			else
 			{
 				txt_result.setText(getString(R.string.result_nodiff));
 			}
 			txt_result.append("\n\n" + "e-85 Costs: " + AppGlobal.currencySymbol[i_currency] + roundToDecimals(d_e85Price / d_e85MPG, 2) + " per " + AppGlobal.distUnitString[i_fuelUnit]);
 			txt_result.append("\n"   + "Gas Costs:  " + AppGlobal.currencySymbol[i_currency] + roundToDecimals(d_gasPrice / d_gasMPG, 2) + " per " + AppGlobal.distUnitString[i_fuelUnit]);
 		}
 		else
 		{
 			Toast.makeText(getApplicationContext(), getString(R.string.result_error), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	/**
 	 * Reset all fields to default of empty.
 	 */
 	public void onResetBtnPress()
 	{
 		edt_e85MPG.setText("");
 		edt_gasMPG.setText("");
 		edt_e85Price.setText("");
 		edt_gasPrice.setText("");
 		txt_result.setText(getString(R.string.result_initial));
 		edt_e85MPG.requestFocus();
 	}
 	
 	/**
 	 * Takes in a double and returns that double rounded 'int c' decimal places.
 	 * TODO: Change money to integer count of pennies
 	 */
 	public static double roundToDecimals(double d, int c) 
 	{
 		int temp=(int)((d*Math.pow(10,c)));
 		return ((temp)/Math.pow(10,c));
 	}
 }
