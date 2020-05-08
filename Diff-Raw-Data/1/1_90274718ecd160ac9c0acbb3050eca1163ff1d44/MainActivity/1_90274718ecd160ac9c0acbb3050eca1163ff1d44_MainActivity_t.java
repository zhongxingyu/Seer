 package com.officerhalf.dxdice;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.NumberPicker;
 import android.widget.TextView;
 
//Everything happens here
 public class MainActivity extends Activity {
 
 	protected NumberPicker dicePicker;
 	protected NumberPicker sidesPicker;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Set up number pickers
 		dicePicker = (NumberPicker) findViewById(R.id.dicePicker);
 		sidesPicker = (NumberPicker) findViewById(R.id.sidesPicker);
 		dicePicker.setMaxValue(100);
 		dicePicker.setMinValue(1);
 		sidesPicker.setMaxValue(100);
 		sidesPicker.setMinValue(1);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public void roll_button (View view) {
 		
 		//Get sides, number of dice
 		int dice = dicePicker.getValue();
 		int sides = sidesPicker.getValue();
 		
 		int[] rolls = new int[dice];	
 		
 		//Get Rolls
 		Random r = new Random();
 		for(int i = 0; i < dice; i++)
 			rolls[i] = r.nextInt(sides) + 1;
 		
 		//Display Rolls
 		TextView output = (TextView) findViewById(R.id.roll_output);
 		String outputText = Integer.toString(dice) + "d" + Integer.toString(sides) + ":  ";
 		for(int i = 0; i < dice; i++)
 		{
 			outputText = outputText + rolls[i];
 			if(i != dice - 1)
 			{
 				outputText = outputText + "  +  ";
 			}
 		}
 		int sum = 0;
 		for(int i = 0; i < dice; i++)
 			sum += rolls[i];
 		outputText = outputText + "  =  " + Integer.toString(sum);
 		output.setText(outputText);
 	}
 	
 }
