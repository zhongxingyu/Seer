 package uk.co.andybarratt.chipcounter;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 public class MainActivity extends CustomActivity
 {	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         Log.d(TAG, "Checking number of chips");
         if(loadChipValues()==false)
         {
         	setContentView(R.layout.no_chips);
         	Log.d(TAG, "No Chips Set, showing welcome screen.");
         	final Button btnSetValues = (Button) findViewById(R.id.btnSetValues); //initialise set values button
         	btnSetValues.setOnClickListener(new OnClickListener() {
         		@Override
         		public void onClick(final View view) {
         			Log.d(TAG, "Set Chip Values button pressed.");
         			Intent myIntent = new Intent(view.getContext(), setValuesActivity.class);
 	                startActivityForResult(myIntent, 0);
         		}
         	});
         }
         else
         {
         	setContentView(R.layout.main);
         	Log.d(TAG, "Chips found, showing main layout.");
         	
         	
         	LinearLayout chipsLayout = (LinearLayout) findViewById(R.id.chips);
         	
         	final EditText [] numChips = new EditText[11];
         	
         	for(int i=0; i<11; i++)
         	{
        		int pixels = convertToPixels(50);
         		numChips[i] = new EditText(this);
 					numChips[i].setHint(getString(R.string.numChips));
					numChips[i].setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, pixels));
 					numChips[i].setInputType(InputType.TYPE_CLASS_NUMBER);
 					
         		if(chips[i].getValue()>0)
         		{
         			ImageView chipImage = new ImageView(this);
         				chipImage.setImageResource(chips[i].getDrawable());
         				chipImage.setAdjustViewBounds(true);
        				chipImage.setMaxWidth(pixels);
         			
 	        		LinearLayout chip = new LinearLayout(this);
 	        			chip.setPadding(10,10,10,10);
 	        			chip.addView(chipImage);
 	        			chip.addView(numChips[i]);
 	        		chipsLayout.addView(chip);
         		}
         	}
         	
         	final Button btnCalculate = (Button) findViewById(R.id.btnCalculate); //initialise set values button
         	btnCalculate.setOnClickListener(new OnClickListener() {
         		@Override
         		public void onClick(final View view) {
         			Log.d(TAG, "Calculate Button Pressed");
         			calculate(numChips);
         		}
         	});
         }
     }
     
     public void calculate(EditText [] numChips)
     {
     	Log.d(TAG, "Calculate Method Started");
     	
     	float total = 0;
     	
     	for(int i=0; i<11; i++)
     	{
     		String numString = numChips[i].getText().toString();
     		if(!numString.equals(""))
     		{
     			int numInt = Integer.parseInt(numString);
     			total += chips[i].getTotalValue(numInt);
     		}
     	}
     	
     	Log.d(TAG, "Total = " + total);
     	
     	String formattedTotal = dec.format(total);
     	
     	final AlertDialog alertDialog = new AlertDialog.Builder(this).create(); // make an alert dialogue		
 		alertDialog.setTitle(getString(R.string.total));
 		alertDialog.setMessage(formattedTotal);
 		
 		alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				alertDialog.dismiss();
 			}
 		});
 		
 		alertDialog.show();
     }
 }
