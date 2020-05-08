 package com.net.rmopenmenu;
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class FavoritesActivity extends ListActivity {
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		final SharedPreferences.Editor editor = prefs.edit();
 				
 		final boolean menu = this.getIntent().getBooleanExtra("menu", true);
 		this.setTitle("OM Finder - Favorite " + (menu? "Menus" : "Restaurants"));
 
 		if (prefs.contains("result")) { 
 			Toast.makeText(getBaseContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
 			editor.remove("result");
 			
 			editor.commit();
 		}
 		final String favess = (menu? prefs.getString("favoritemenus", "") : prefs.getString("favoriterests", ""));
 		final String[] favorites = favess.split(",,,");
 
 		final ArrayList<String> faves = new ArrayList<String>();
 		for (int i = 0; i < favorites.length; i++) {
 			String str = favorites[i];
 			
 			if (!str.equals("") && !faves.contains(str)) {
 				faves.add(str);
 			}
 		}
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_item, faves);
 		
 		setListAdapter(adapter);
 		
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 
 		// Every item will launch the map
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				Intent myIntent = new Intent(getBaseContext(), SearchActivity.class);
             	String selectedItem = ((TextView) v).getText().toString();
 
 				myIntent.putExtra("query", selectedItem.split("\n")[0].trim());
 				myIntent.putExtra("menu", menu);
 				
 				startActivity(myIntent);
 			}
 		});
 		
 		// Every item will launch the map
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
 				final String selectedItem = ((TextView) v).getText().toString();
 
 				faves.remove(selectedItem);
 				
 				String newStr = "";
 				for (int i = 0; i < faves.size(); i++) {
 					newStr += faves.get(i);
 					if (i < faves.size() - 1) {
						newStr += ",,, ";
 					}
 				}
 				editor.putString((menu? "favoritemenus" : "favoriterests"), newStr);
 				editor.putString("result", "Removed from Favorites");
 				
 				editor.commit();
 				
 				startActivity(getIntent()); finish();
 				
 				return true;
 			}
 		});
 	}
 }
