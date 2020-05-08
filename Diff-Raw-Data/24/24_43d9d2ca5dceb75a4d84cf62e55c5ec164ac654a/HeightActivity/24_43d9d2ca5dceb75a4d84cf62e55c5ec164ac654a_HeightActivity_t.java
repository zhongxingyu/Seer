 package com.github.mahmoudhossam.height;
 
 import java.text.NumberFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.EditText;
 
 public class HeightActivity extends Activity {
 
 	private EditText cm;
 	private EditText feet;
 	private EditText inches;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		init();
 	}
 
	private void init() {
 
 		cm = (EditText) findViewById(R.id.editText1);
 		feet = (EditText) findViewById(R.id.editText2);
 		inches = (EditText) findViewById(R.id.editText3);
 		setListeners(cm);
 		setListeners(feet);
 		setListeners(inches);
 	}
 
 	public void setListeners(View v) {
		
 		v.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER
 						&& event.getAction() == KeyEvent.ACTION_DOWN) {
 					if ((v == cm)) {
 						onToImperial(null);
 					} else if (v == inches){
 						onToMetric(null);
 					}
 					} else  {
 						inches.requestFocus();
 					}
 				return false;
 			}
 		});
 	}
 
 	public void onToImperial(View view) {
		
 		createResultDialog(getImperialResult(), getCurrentFocus()).show();
 	}
 
 	public void onToMetric(View view) {
		
 		createResultDialog(getMetricResult(), getCurrentFocus()).show();
 	}
 
	private String getMetricResult() {
		
 		int[] result = Backend.getFeetAndInches(parseInput(cm));
 		return result[0] + " feet, " + result[1] + " inches.";
 	}
 
 	private String getImperialResult() {
		
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMaximumFractionDigits(1);
 		double output = Backend.getCentimeters(parseInput(feet),
 				parseInput(inches));
 		return nf.format(output) + " centimeters";
 	}
 
 	private AlertDialog createResultDialog(String result, final View caller) {
		
 		Builder builder = new AlertDialog.Builder(this);
 		builder.setCancelable(false)
 				.setTitle(getResources().getString(R.string.result))
 				.setMessage(result)
 				.setPositiveButton("OK", new OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 						caller.requestFocus();
 					}
 				});
 		return builder.create();
 	}
 
	private double parseInput(EditText input) {
		
 		if (input.getText().length() > 0) {
 			String text = input.getText().toString();
 			double content = Double.parseDouble(text);
 			return content;
 		} else {
 			return 0;
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
 		cm.setText(savedInstanceState.getString("cm"));
 		feet.setText(savedInstanceState.getString("feet"));
 		inches.setText(savedInstanceState.getString("inches"));
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
		
 		outState.putString("cm", cm.getText().toString());
 		outState.putString("inches", inches.getText().toString());
 		outState.putString("feet", feet.getText().toString());
 		super.onSaveInstanceState(outState);
 	}
 }
