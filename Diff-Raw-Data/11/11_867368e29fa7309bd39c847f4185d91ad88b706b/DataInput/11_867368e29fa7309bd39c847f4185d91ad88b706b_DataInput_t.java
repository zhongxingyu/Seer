 package com.placella.bmi;
 
 import com.placella.bmi.R;
 
 import android.app.*;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 
 public class DataInput extends Activity {
 	private DataInput self = this;
 	private int height = 0;
 	private int weight = 0;
 	private boolean imperial = false; 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_input);
         
         imperial = (boolean) getIntent().getBooleanExtra("imperial", false);
         height = getIntent().getIntExtra("height", 0);
         weight = (int) getIntent().getIntExtra("weight", 0);
         
         EditText[] inputs = {
             (EditText) findViewById(R.id.height),
             (EditText) findViewById(R.id.weight)
     	};
         inputs[0].setText(Integer.toString(height));
         inputs[1].setText(Integer.toString(weight));
         
         inputs[0].selectAll();
         
         if (! imperial) {
         	TextView t = (TextView) findViewById(R.id.height_text);
             t.setText(R.string.metric_your_height);
         	t = (TextView) findViewById(R.id.weight_text);
             t.setText(R.string.metric_your_weight);
         }
         Intent returnIntent = new Intent();
     	returnIntent.putExtra("height", height);
     	returnIntent.putExtra("weight", weight);
     	returnIntent.putExtra("imperial", imperial);
     	setResult(0, returnIntent);
         
         Button button = (Button) findViewById(R.id.calculate);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
         		int[] values = getValues(); 
         		height = values[0];
         		weight = values[1];
 	            TextView[] labels = {
 		            (TextView) findViewById(R.id.height_text),
 		            (TextView) findViewById(R.id.weight_text)
 	            };
 	            int colors[] = {
             		getResources().getColor(R.color.normal),
             		getResources().getColor(R.color.error)
 	            };
         		labels[0].setTextColor(colors[0]);
         		labels[1].setTextColor(colors[0]);
             	if (height <= 0 || weight <= 0) {
                 	new MessageDialog(self, R.string.invalid).show();
             		if (height <= 0) {
             			labels[0].setTextColor(colors[1]);
             		}
             		if (weight <= 0) {
             			labels[1].setTextColor(colors[1]);
             		}
             	} else {
                 	Bundle b = new Bundle();
                 	b.putBoolean("imperial", imperial);
                 	if (! imperial) {
                     	b.putDouble("height", (double)height / 100);
                 	} else {
                 		b.putDouble("height", (double)height);
                 	}
                 	b.putInt("weight", weight);
                 	Intent i = new Intent(self, Output.class);
                 	i.putExtras(b);
                 	startActivity(i);
             	}
             	update(height, weight);
             }
         });
     }
  
    @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
 		int[] values = getValues(); 
 		update(values[0], values[1]);
     	return super.onKeyUp(keyCode, event);
     }
     
     private void update(int h, int w) {
     	Intent returnIntent = new Intent();
     	returnIntent.putExtra("height", h);
     	returnIntent.putExtra("weight", w);
     	returnIntent.putExtra("imperial", imperial);
     	setResult(0, returnIntent);   
     }
     
     @Override
     public void onBackPressed() {
     	super.onBackPressed();
     	finish();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	MenuHandler.onOptionsItemSelected(item, this);
 		return super.onOptionsItemSelected(item);
     }
     
     private int[] getValues() {
         EditText[] inputs = {
             (EditText) findViewById(R.id.height),
             (EditText) findViewById(R.id.weight)
     	};
         int[] retval = new int[2];
 		try {
 			retval[0] = Integer.parseInt(inputs[0].getText().toString());
 		} catch (Exception e) {}
 		try {
 			retval[1] = Integer.parseInt(inputs[1].getText().toString());
 		} catch (Exception e) {}
 		return retval; // [ height, weight]
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
 	    super.onSaveInstanceState(outState);		
 	    outState.putInt("height", height);
 	    outState.putInt("weight", weight);
 	    outState.putBoolean("imperial", imperial);
     }
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
 	    super.onRestoreInstanceState(savedInstanceState);
 	    height = savedInstanceState.getInt("height");
 	    weight = savedInstanceState.getInt("weight");
 	    imperial = savedInstanceState.getBoolean("imperial");
     }
 }
