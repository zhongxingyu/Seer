 package com.seawolfsanctuary.tmt;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class FromActivity extends Activity {
 	TextView txt_FromSearch, txt_FromSummary;
 	DatePicker dp_FromDate;
 	TimePicker tp_FromTime;
 	AutoCompleteTextView actv_FromSearch;
 
 	private String[] read_csv(String filename) {
 		String[] array = {};
 
 		try {
 			InputStream input;
 			input = getAssets().open(filename);
 			int size = input.available();
 			byte[] buffer = new byte[size];
 
 			input.read(buffer);
 			input.close();
 			array = new String(buffer).split("\n");
 
 		} catch (Exception e) {
 			String error_msg = "Error reading station list!";
			actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
			actv_FromSearch.setText(error_msg);
			actv_FromSearch.setError(error_msg);
			actv_FromSearch.setEnabled(false);
 		}
 
 		return array;
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tab_from);
 
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		txt_FromSummary = (TextView) findViewById(R.id.txt_FromSummary);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		// Link array of completions
 		String[] completions = read_csv("stations.lst");
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 		actv_FromSearch.setAdapter(adapter);
 		actv_FromSearch.setThreshold(2);
 
 		actv_FromSearch
 				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						hideKeyboard();
 						updateText();
 					}
 				});
 	}
 
 	public void updateText() {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		txt_FromSummary = (TextView) findViewById(R.id.txt_FromSummary);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_FromSummary.setText("You selected: "
 				+ actv_FromSearch.getText().toString() + "\nOn: "
 				+ dp_FromDate.getDayOfMonth() + "/" + dp_FromDate.getMonth()
 				+ "/" + dp_FromDate.getYear() + "\nAt: "
 				+ tp_FromTime.getCurrentHour() + ":"
 				+ tp_FromTime.getCurrentMinute());
 
 	}
 
 	public void hideKeyboard() {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(actv_FromSearch.getWindowToken(), 0);
 	}
 }
