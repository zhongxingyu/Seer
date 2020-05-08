 package de.thm.ateam.memory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.thm.ateam.memory.engine.MemoryPlayerDAO;
 import de.thm.ateam.memory.engine.type.Player;
 import de.thm.ateam.memory.game.GameActivity;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 
 /**
  * 
  * List activity to select the user
  * 
  */
 public class SelectUserActivity extends ListActivity {
 
 	List<Player> players = new ArrayList<Player>();
 
 	/**
 	 * 
 	 * Function called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 * 
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/*users = new ArrayList<String>();
 		users.add("Quallenmann");
 		users.add("Quallenmann2");
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);
 		setListAdapter(adapter);*/
 		
 		players = MemoryPlayerDAO.getInstance(this).getAllPlayers();
 		
 
 		setListAdapter(new ArrayAdapter<Player>(this, android.R.layout.simple_list_item_1, players));
 	}
 	
 	/**
 	 * 
 	 * Load the adapter again when the activity is resumed
 	 */
 	@Override
   protected void onResume() {
     super.onResume();
     BaseAdapter base = (BaseAdapter) getListAdapter();
     Log.i("info", base.getCount() + "");
     if(base != null){
      players = MemoryPlayerDAO.getInstance(this).getAllPlayers();
       base.notifyDataSetChanged();
     }
   }
 
 	/**
 	 * 
 	 * Function to create the optionmenu
 	 * 
 	 * @param menu
 	 * @return boolean
 	 * 
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.selectusermenu, menu);
 		return true;
 	}
 
 	/**
 	 * 
 	 * Function which is called when a user clicks on a option menu button
 	 * 
 	 * @param item
 	 * @return boolean
 	 * 
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.adduser:
 			Intent intent = new Intent(getApplicationContext(),
 			    CreateUserActivity.class);
 			startActivity(intent);
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * Function when user clicked on a user
 	 * 
 	 * @param l
 	 * @param v
 	 * @param position
 	 * @param id
 	 * 
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		final CharSequence[] items = { "Network", "Local" };
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose a game");
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				Intent intent;
 				switch (item) {
 
 				// Network Game
 				case 0:
 					/*
 					 * TestWeise Multiple User
 					 */
 
 					intent = new Intent(getApplicationContext(),
 					    SelectMultipleUserActivity.class);
 					startActivity(intent);
 
 					break;
 				// Local Game
 				case 1:
 					intent = new Intent(getApplicationContext(), GameActivity.class);
 					startActivity(intent);
 
 					break;
 				default:
 					break;
 				}
 
 			}
 		});
 
 		builder.create().show();
 	}
 }
