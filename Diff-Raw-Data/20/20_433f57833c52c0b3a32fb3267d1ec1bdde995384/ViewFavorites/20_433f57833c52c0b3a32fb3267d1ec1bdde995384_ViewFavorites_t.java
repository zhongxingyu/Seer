 package edu.rit.se.agile.randominsultapp;
 
 import android.os.Bundle;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
import edu.rit.se.agile.data.FavoritesTemplate;
 
 public class ViewFavorites extends GenericActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_favorites);
 		
 		ListView list =  (ListView) findViewById(R.id.favorites_list);
 		list.setAdapter(
 				new SimpleCursorAdapter(this, 
 						R.id.favorites_list, 
 						favoritesDAO.getAllFavoritesCursor(), //FIX THIS 
 						new String[]{ FavoritesTemplate.COLUMN_ID, FavoritesTemplate.COLUMN_TEMPLATE }, 
 						new int[]{ R.id.category_list_entry }, 
 						SimpleCursorAdapter.FLAG_AUTO_REQUERY ));
 		}
 
 }
