 package org.darksoft.android.randomgenerator;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private EditText minimum = null;
 	private EditText maxium = null;
 	private EditText quantity = null;
 	private TextView result = null;
 	private CheckBox enable_decimal = null;
 	private Button button = null;
 
 	@SuppressWarnings("unused")
 	private static double truncate(double x) {
 		if (x > 0)
 			return Math.floor(x * 100) / 100;
 		else
 			return Math.ceil(x * 100) / 100;
 	}
 
 	private double getRandomNumber() throws Exception {
 		double ran;
 		int max, min;
 		try {
 			max = Integer.parseInt(maxium.getText().toString());
 			min = Integer.parseInt(minimum.getText().toString());
 		} catch (NumberFormatException ex) {
 			showNumberFormatError();
 			throw new Exception("Value Error");
 		}
 		if (max < min) {
 			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 			alertDialog.setTitle(getString(R.string.dialog_title));
 			alertDialog.setMessage(getString(R.string.dialog_message));
 			alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
 					getString(android.R.string.ok),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							// here you can add functions
 						}
 					});
 			alertDialog.setIcon(R.drawable.ic_launcher);
 			alertDialog.show();
 			throw new Exception("Value Error");
 		}
 		ran = (Math.random() * (max - min)) + min;
 		return ran;
 	}
 
 	private void showNumberFormatError() {
 		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 		alertDialog.setTitle(getString(R.string.number_error_title));
 		alertDialog.setMessage(getString(R.string.number_error_message));
 		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
 				getString(android.R.string.ok),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// here you can add functions
 					}
 				});
 		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
 		alertDialog.show();
 	}
 
 	private OnClickListener buttonListener = new OnClickListener() {
 		public void onClick(View v) {
 			String text = "";
 			int i = 0;
 			try {
 				i = (Integer.parseInt(quantity.getText().toString()));
 			} catch (NumberFormatException ex) {
 				showNumberFormatError();
 				return;
 			}
 			double r = 0.0;
 			result.setText(text);
 			if (i == 1)
 				text += getString(R.string.random_number);
 			else
 				text += getString(R.string.random_numbers);
 			text += "\n";
 			for (; i > 0; i--) {
 				try {
 					r = getRandomNumber();
 				} catch (Exception e) {
 					return;
 				}
 				if (enable_decimal.isChecked())
 					text += Double.toString(r);
 				else
 					text += Integer.toString((int) r);
 				if (i != 1)
 					text += ", ";
 			}
 			result.setText(text);
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		minimum = (EditText) findViewById(R.id.minimum);
 		maxium = (EditText) findViewById(R.id.maxium);
 		quantity = (EditText) findViewById(R.id.quantity);
 		result = (TextView) findViewById(R.id.result);
 		enable_decimal = (CheckBox) findViewById(R.id.decimal_enable);
 		button = (Button) findViewById(R.id.generate);
 		button.setOnClickListener(buttonListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_about:
 			showProgramInfo();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void showProgramInfo() {
 		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 		alertDialog.setTitle(getString(R.string.about_title));
 		alertDialog.setMessage(getString(R.string.copyright));
 		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
 				getString(android.R.string.ok),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// here you can add functions
 					}
 				});
 		alertDialog.setIcon(android.R.drawable.ic_menu_help);
 		alertDialog.show();
 	}
 
 }
