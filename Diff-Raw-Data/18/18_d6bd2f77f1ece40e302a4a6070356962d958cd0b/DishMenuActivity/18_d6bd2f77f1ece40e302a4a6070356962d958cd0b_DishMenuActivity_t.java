 package com.gourmet6;
 
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DishMenuActivity extends Activity {
 	
 	private Gourmet g;
 	private Restaurant current  = null;
 	private ExpandableListView dishes;
 	private DishMenuAdapter dishad;
 	private ArrayAdapter<String> adapter;
 	private ArrayList<String> types;
 	private ArrayList<String> subtypes;
 	private ArrayList<String> allergens;
 	private ArrayList<String> filters;
 	private ArrayList<Dish> listdish;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dish_menu);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		setTitle(R.string.activity_menu_title);
 		
 		g = (Gourmet)getApplicationContext();
 		current = g.getRest();
 		
 		//Reaction du bouton de commande
 		Button order = (Button) findViewById(R.id.button1);
		
 		if(g.getClient() == null){
 			order.setEnabled(false);
 		}else{
 			order.setEnabled(true);
 		}
		
 		order.setOnClickListener(new View.OnClickListener() {
 					
 			@Override
 			public void onClick(View v) {
 				Intent commande = new Intent(DishMenuActivity.this, OrderActivity.class);
 				commande.putExtra("from", true);
 				startActivity(commande);			
 			}
 		});
 		
 		updateLists(current.getListDishes());
 		
 		dishes = (ExpandableListView) findViewById(R.id.dish_menu);
 		dishad = new DishMenuAdapter(this,current,listdish, false);
 		dishes.setAdapter(dishad);
 		dishes.setOnChildClickListener(new OnChildClickListener() {
 			
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
 				
 				TextView txt = (TextView)v.findViewById(R.id.dishname);
 				
 				if(dishad.isChildSelectable(groupPosition, childPosition)){
 					Intent display = new Intent(DishMenuActivity.this, DishDisplayActivity.class);
 					display.putExtra("the_dish", txt.getText().toString());
 					startActivity(display);
 				}
 				return true;
 			}
 		});
 		
 		Spinner sort = (Spinner) findViewById(R.id.spinner1);
 		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, filters);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		sort.setAdapter(adapter);
 		sort.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				String str =(String)arg0.getSelectedItem();
 				if(str == "All"){
 					
 					updateLists(current.getListDishes());
 					dishes.setAdapter(new DishMenuAdapter(DishMenuActivity.this,current,listdish, false));
 					
 				}
 				else if(str == "Price"){
 					updateLists(current.sortDishesPrice(listdish));
 					dishes.setAdapter(new DishMenuAdapter(DishMenuActivity.this,current,listdish, false));
 				}
 				else if(types.contains(str)){
 					
 					updateLists(current.filterDishesType(str, listdish));
 					dishes.setAdapter(new DishMenuAdapter(DishMenuActivity.this,current,listdish, false));
 				}
 				else if(subtypes.contains(str)){
 					
 					updateLists(current.filterDishesSubtype(str, listdish));
 					dishes.setAdapter(new DishMenuAdapter(DishMenuActivity.this,current,listdish, false));
 				}
 				else if(allergens.contains(str)){
 					
 					updateLists(current.filterDishesAllergen(str, listdish));
 					dishes.setAdapter(new DishMenuAdapter(DishMenuActivity.this,current,listdish, false));
 				}
 				else{
 					Toast.makeText(DishMenuActivity.this,"Current list does not contain any dish from "+str, Toast.LENGTH_LONG) .show();
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 			}	
 		});
 	}
 	
 	private void updateLists(ArrayList<Dish> filtered){
 		this.listdish = filtered;
 		this.filters = current.getFilters(filtered);
 		this.types = current.getDishesTypes(filtered);
 		this.subtypes = current.getDishesSubtypes(filtered);
 		this.allergens = current.getAllergensForFilter(filtered);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.dish_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
