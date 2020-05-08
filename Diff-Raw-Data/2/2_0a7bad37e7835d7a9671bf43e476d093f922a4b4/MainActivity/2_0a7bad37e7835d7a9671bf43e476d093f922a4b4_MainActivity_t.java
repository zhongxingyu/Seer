 package com.qut.spc;
 
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class MainActivity extends Activity {
 	final String SPC_URL = "http://solarpowercalc.appspot.com";
 
 	final String[] componentTypes = {
 			"Panels",
			"Inverters",
 			"Batteries",
 	};
 
 	private Spinner spComponent;
 	private EditText etPostcode, etPriceMin, etPriceMax, etCapacityMin, etCapacityMax;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		spComponent = (Spinner)findViewById(R.id.spComponent);
 		etPostcode = (EditText)findViewById(R.id.postcode);
 		etPriceMin = (EditText)findViewById(R.id.priceMin);
 		etPriceMax = (EditText)findViewById(R.id.priceMax);
 		etCapacityMin = (EditText)findViewById(R.id.capacityMin);
 		etCapacityMax = (EditText)findViewById(R.id.capacityMax);
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_dropdown_item, componentTypes);
 		spComponent.setAdapter(adapter);
 
 		restoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 
 		state.putString("type", spComponent.getSelectedItem().toString());
 		state.putString("postcode", etPostcode.getText().toString());
 		state.putString("priceMin", etPriceMin.getText().toString());
 		state.putString("priceMax", etPriceMax.getText().toString());
 		state.putString("capacityMin", etCapacityMin.getText().toString());
 		state.putString("capacityMax", etCapacityMax.getText().toString());
 	}
 	
 	@Override
 	// onRestoreInstanceState is only called when the program is terminated
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		restoreInstanceState(savedInstanceState);
 	}
 
 	private void restoreInstanceState(Bundle state) {
 		if (state == null) {
 			return;
 		}
 		String str;
 		str = state.getString("type");
 		if (str.length() > 0) {
 			int pos = Arrays.asList(componentTypes).indexOf(str);
 			spComponent.setSelection(pos);
 		}
 		str = state.getString("postcode");
 		if (str.length() > 0) {
 			etPostcode.setText(str);
 		}
 		str = state.getString("priceMin");
 		if (str.length() > 0) {
 			etPriceMin.setText(str);
 		}
 		str = state.getString("priceMax");
 		if (str.length() > 0) {
 			etPriceMax.setText(str);
 		}
 		str = state.getString("capacityMin");
 		if (str.length() > 0) {
 			etCapacityMin.setText(str);
 		}
 		str = state.getString("capacityMax");
 		if (str.length() > 0) {
 			etCapacityMax.setText(str);
 		}
 	}
 
     public void onSearchClick(View v) {
     	int postcode = 0;
     	int priceMin = 0;
     	int priceMax = 0;
     	int capacityMin = 0;
     	int capacityMax = 0;
     	String component = "panel";
     	String str;
     	
     	str = etPostcode.getText().toString();
     	if (str.length() > 0) {
     		postcode = Integer.parseInt(str);
     	}
     	str = etPriceMin.getText().toString();
     	if (str.length() > 0) {
     		priceMin = Integer.parseInt(str);
     	}
     	str = etPriceMax.getText().toString();
     	if (str.length() > 0) {
     		priceMax = Integer.parseInt(str);
     	}
     	str = etCapacityMin.getText().toString();
     	if (str.length() > 0) {
     		capacityMin = Integer.parseInt(str);
     	}
     	str = etCapacityMax.getText().toString();
     	if (str.length() > 0) {
     		capacityMax = Integer.parseInt(str);
     	}
     	str = spComponent.getSelectedItem().toString();
     	if (str.equals("Panels")) {
     		component = "panel";
     	} else if (str.equals("Inverters")) {
     		component = "inverter";
     	} else if (str.equals("Batteries")) {
     		component = "battery";
     	}  
     	
     	String query = "";
     	if (postcode > 0) {
     		query += "postcode=" + postcode + "&";
     	}
     	if (priceMin > 0) {
     		query += "priceMin=" + priceMin + "&";
     	}
     	if (priceMax > 0) {
     		query += "priceMax=" + priceMax + "&";
     	}
     	if (capacityMin > 0) {
     		query += "capacityMin=" + capacityMin + "&";
     	}
     	if (capacityMax > 0) {
     		query += "capacityMax=" + capacityMax + "&";
     	}
     	search(SPC_URL + "/" + component + "?" + query, component);
     }
     
     private void search(String url, String component) {
     	Intent i = new Intent(this, SearchResultActivity.class);
     	i.putExtra("url", url);
     	i.putExtra("component", component);
     	startActivity(i);
     }
 }
