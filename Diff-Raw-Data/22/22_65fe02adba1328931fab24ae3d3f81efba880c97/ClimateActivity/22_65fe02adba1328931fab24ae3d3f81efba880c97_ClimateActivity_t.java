 package com.morris.scott.autohome;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class ClimateActivity extends Activity {
 
 	/*
 	 * Widget Variables
 	 */
 	TextView setTempText, currentTempText;
 	Button upTempButton, downTempButton;
 	ImageView furnaceStatus, acStatus;
 	ToggleButton furnaceEnable, acEnable;
 	
 	/*
 	 * Other Variables
 	 */
 	boolean	furnaceState, acState, furnaceOn = false, acOn = false, tempBeingSet = false;
 	int setTemp = 20, currentTemp = 20;
 	
 	Handler handler = new Handler();
 	String tempSuffix = "°C";
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.climate);
 		
 		/*
 		 * Runnables for the Handler 
 		 */
 		final Runnable resetTempPosition = new Runnable() {
 			
 			public void run() {
 				currentTempText.setText(currentTemp+tempSuffix);
 				setTempText.setText(setTemp+tempSuffix);
 				tempBeingSet = false;
 			};
 		};
 		final Runnable emulateSysOperation = new Runnable() {
 			
 			public void run() {
				if((currentTemp > setTemp + 3) && acState) {
 					acOn = true;
 					currentTemp -= 1;
 				} else if((currentTemp > setTemp - 3) && furnaceState) {
 					furnaceOn = true;
 					currentTemp += 1;
				} else if (((currentTemp < setTemp - 2) && acOn) || ((currentTemp > setTemp + 2) && furnaceOn)){
 					acOn = false;
 					furnaceOn = false;
 				} else if (acOn)
 					currentTemp--;
 				else if (furnaceOn)
 					currentTemp++;
 				else
 					currentTemp++;
 				if (!tempBeingSet)
 					currentTempText.setText(currentTemp+tempSuffix);
 				else
 					setTempText.setText(currentTemp+tempSuffix);
 				handler.postDelayed(this, 1000);
 			};
 		};
 		
 		handler.postDelayed(emulateSysOperation, 1000);
 		
 		setTempText = (TextView)findViewById(R.id.climate_setTemp);
		setTempText.setText(setTemp+tempSuffix);
 		currentTempText = (TextView)findViewById(R.id.climate_currentTemp);
 		currentTempText.setText(currentTemp+tempSuffix);
 		
 		upTempButton = (Button)findViewById(R.id.climate_upTemp);
 		upTempButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				handler.removeCallbacks(resetTempPosition);
 				setTempText.setText(currentTemp+tempSuffix);
 				setTemp += 1;
 				currentTempText.setText(setTemp+tempSuffix);
 				tempBeingSet = true;
 				handler.postDelayed(resetTempPosition, 3000);
 			}
 		});
 		downTempButton = (Button)findViewById(R.id.climate_downTemp);
 		downTempButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				handler.removeCallbacks(resetTempPosition);
 				setTempText.setText(currentTemp+tempSuffix);
 				setTemp -= 1;
 				currentTempText.setText(setTemp+tempSuffix);
 				tempBeingSet = true;
 				handler.postDelayed(resetTempPosition, 3000);
 			}
 		});
 		
 		furnaceStatus = (ImageView)findViewById(R.id.climate_furnaceStateIcon);
		acStatus = (ImageView)findViewById(R.id.climate_acStateIcon);
		
		furnaceEnable = (ToggleButton)findViewById(R.id.climate_enableFurnace);
		furnaceEnable.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				furnaceState = furnaceEnable.isChecked();
 			}
 		});
		acEnable = (ToggleButton)findViewById(R.id.climate_enableAC);
		acEnable.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				acState = acEnable.isChecked();
 			}
 		});
 		
 		
 	}
 	
 
 }
