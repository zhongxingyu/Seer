 package org.katta.activity;
 
 
 import org.katta.adapter.TripListEntryAdapter;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class GoDutchActivity extends ListActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		setListAdapter(new TripListEntryAdapter(this));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.add_trip);
 		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.delete_trip);
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		boolean result = super.onOptionsItemSelected(item);
		
		
 		Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
 		
 		startActivity(new Intent(this, TripDetailActivity.class));
 		
 		return result;
 	}
 
 }
