 package com.qut.spc;
 
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
         
         String[] list = new String[] {
         		"Panels",
         		"Iinverters",
         		"Batteries",
         };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.id.spComponent, list);
//        spComponent.setAdapter(adapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void onSearchClick(View v) {
     	int postcode = 0;
     	int priceMin = 0;
     	int priceMax = 0;
     	int capacityMin = 0;
     	int capacityMax = 0;
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
    	String component = "panel";
     	
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
