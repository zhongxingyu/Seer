 package com.example.lagerlogger;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListActivity;

 
 public class BeerList extends SherlockListActivity {
 	
 	
 	static final String[] BEER_LIST_TEST = new String[] { "East India Pale Ale", "Flower Power", "Flounder Pounder"
 															,"Lagunitas IPA", "Lil' Sumpin' Sumpin' Ale", "Arrogant Bastard"
 															,"Sublimely Self-Righteous"};
 	
 	
 
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 	}
 	
 }
