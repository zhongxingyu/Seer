 package com.mvanveggel.gordijnapp;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class SelectBonnr extends Activity implements OnItemClickListener, OnItemLongClickListener {
 
 	Order order;
 	ArrayList<String> listValues, listFooters;
 	ListView list;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.selectbonnr);
 		order = OrderOverview.getOrder();
 		list = (ListView) findViewById(R.id.lvMenuBonnr);
 		list.setOnItemLongClickListener(this);
 		list.setOnItemClickListener(this);
 		refreshLijst();
 	}
 
 	public void refreshLijst() {
 		listValues = new ArrayList<String>();
 		listFooters = new ArrayList<String>();
 
 		// Laat 'Nieuw gordijn' op eerste positie staan
 		listValues.add("Nieuw gordijn");
 		listFooters.add("Voeg een nieuw gordijn toe");
 
 		// Set de rest van de lijst naarmate de aantal gordijnen en vul met Type
		for (int i = 0; i < order.getAantalGordijnen(); i++) {
 			listValues.add(order.getTitel(i));
 			listFooters.add(order.getGordijnFooter(i));
 		}
 		MenuListAdapter adapter = new MenuListAdapter(this, listValues, listFooters, true);
 		list.setAdapter(adapter);
 
 		for (int i = 0; i < order.getAantalGordijnen(); i++) {
 			order.setGordijnIngevoerd(i, false);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshLijst();
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 		if (position > 0) {
 			int gordijnid = position - 1;
 			try {
 				Gordijn gordijnKopie = (Gordijn) order.getGordijn(gordijnid).clone();
 				gordijnKopie.setVertrek(gordijnKopie.getVertrek() + " kopie");
 				order.addGordijn(gordijnKopie);
 				refreshLijst();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		} else
 			return false;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 		if (position == 0) {
 			try {
 				Intent selectGordijnTypeIntent = new Intent(SelectBonnr.this, SelectTypeGordijn.class);
 				startActivity(selectGordijnTypeIntent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				position--;
 				Intent gordijnInvoerenIntent = new Intent(SelectBonnr.this, GordijnInvoeren.class);
 				gordijnInvoerenIntent.putExtra("toevoegen", false);
 				gordijnInvoerenIntent.putExtra("gordijnid", position);
 				startActivity(gordijnInvoerenIntent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
