 package com.mvanveggel.gordijnapp;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SelectTypeStof extends ListActivity {
 
 	Order order;
 	String typeGordijnNaam;
 	String[] menuItems;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		order = OrderOverview.getOrder();
 
 		typeGordijnNaam = getIntent().getStringExtra("typegordijnnaam");
 		
 		DataManager sqlData = new DataManager(this);
 		sqlData.open();
 		String maakwijzeValues[][] = sqlData.getEntryMaakwijze(typeGordijnNaam);
 		sqlData.close();
 		GordijnData gordijnData = new GordijnData(null, maakwijzeValues);
 		
 		menuItems = gordijnData.getTypenStof();
 
 		setListAdapter(new ArrayAdapter<String>(SelectTypeStof.this, android.R.layout.simple_list_item_1, menuItems));
 		setTitle(typeGordijnNaam + " - Selecteer stof");
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		try {
 			Intent gordijnInvoerenIntent = new Intent(SelectTypeStof.this, GordijnInvoeren.class);
			gordijnInvoerenIntent.putExtra("typegordijnid", typeGordijnNaam);
 			gordijnInvoerenIntent.putExtra("typestofnaam", menuItems[position]);
 			startActivity(gordijnInvoerenIntent);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(order.getAantalGordijnen() > 0){
 			if(order.getGordijnIngevoerd(order.getAantalGordijnen()-1))
 				finish();
 		}
 	}
 }
