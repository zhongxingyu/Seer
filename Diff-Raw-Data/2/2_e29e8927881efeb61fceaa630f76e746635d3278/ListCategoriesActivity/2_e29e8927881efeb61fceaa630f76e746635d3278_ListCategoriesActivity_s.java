 package com.dpbsoft.app;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
  
 public class ListCategoriesActivity extends ListActivity {
  
 	static final String[] CATEGORIES = new String[] { "Categorie 1", "Categorie 2", "Categorie 3",
 			"Categorie 4", "Categorie 5", "Categorie 6", "Categorie 7", "Categorie 8",
 			"Categorie 9", "Categorie 10", "Categorie 11", "Categorie 12", "Categorie 13" };
  
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
  
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.activity_list_categories,CATEGORIES));
  
 		ListView listView = getListView();
 		listView.setTextFilterEnabled(true);
  
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent(ListCategoriesActivity.this, CategorieActivity.class);
 		        startActivity(intent);
 			}
 		});
  
 	}
  
 }
