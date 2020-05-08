 package com.simple.calculator;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class EquationActivity extends Activity {
 	public EditText first;
 	public EditText second;
 	public EditText third;
 	public TextView res;
 	
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.equation);
 		first = (EditText) findViewById(R.id.editText1);
 		second = (EditText) findViewById(R.id.editText2);
 		third = (EditText) findViewById(R.id.editText3);
 		res = (TextView) findViewById(R.id.textView4);
 		
 		third.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					try {
 						ArrayList<String> a = Calculate.equation(Double.parseDouble(first.getText().toString()), Double.parseDouble(second.getText().toString()), Double.parseDouble(third.getText().toString()));
 						if (a.get(0).equals("NaN")) res.setText("No Real Roots");
 						else{
 							String pr = "";
 							for (String s : a)
								pr += s + "\n";
 							res.setText(pr);
 						}
 					}
 					catch (Exception ex){
 						first.requestFocus();
 					}
 				}
 				return false;
 			}
 			
 		});
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.second, menu);
 		return true;
 	}
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    	case R.id.back_calulation:
 	    		this.finish();
 	    		return true;
 	    	default:
 	    		return true;
 	    }
 	}
 
 }
 
