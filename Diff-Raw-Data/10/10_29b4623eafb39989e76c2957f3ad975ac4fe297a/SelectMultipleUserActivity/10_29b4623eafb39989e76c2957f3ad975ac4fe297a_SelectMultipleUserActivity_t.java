 package de.thm.ateam.memory;
 
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import de.thm.ateam.memory.engine.MemoryPlayerDAO;
 import de.thm.ateam.memory.engine.type.Player;
 import de.thm.ateam.memory.game.GameActivity;
 import de.thm.ateam.memory.game.PlayerList;
 
 /**
  * 
  * Class to pick multiple users
  * 
  */
 public class SelectMultipleUserActivity extends ListActivity {
 	
 	@SuppressWarnings("unused")
 	private final String TAG = this.getClass().getSimpleName();
 
 	private ListView listView;
 
 	final static int GAME_HAS_FINISHED = 42;
 
 
 	private BaseAdapter adapter;
 
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
 		setContentView(R.layout.selectmultipleuser);
 
 		PlayerList.getInstance().session.clear(); 
 
 		
 		
 		adapter = new ArrayAdapter<Player>(this,
 				android.R.layout.simple_list_item_multiple_choice, PlayerList.getInstance().players);
 
 
 		listView = getListView();
 		listView.setAdapter(adapter);
 		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		
 		registerForContextMenu(getListView());
 	}
 
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.contextdelete, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 			case R.id.delete_user:
 				Player selected = (Player)adapter.getItem(info.position);
 			   /*
 				* PlayerList.getInstance().session.remove(selected);
 				* MemoryPlayerDAO.getInstance(this).removePlayer(selected);
 				* 
 				* is this a joke? see SelectMultipleUser Activity at the same spot. 
 				*/
 				if(!selected.remove(adapter))Log.i(TAG, "Could not delete " + selected.nick + "!");
				getListView().setItemChecked(info.position, false);
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 		}
 	}
 
 	/**
 	 * Load the adapter again when the activity is resumed
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		adapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * 
 	 * Function to create the optionsmenu
 	 * 
 	 * @param menu
 	 * @return boolean
 	 * 
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.selectmultipleusermenu, menu);
 		return true;
 	}
 
 
 	/**
 	 * 
 	 * Function called when user clicks on an entry
 	 * 
 	 * @param ListView l
 	 * @param View v
 	 * @param int position
 	 * @param long id
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		if(!PlayerList.getInstance().session.remove((Player)l.getAdapter().getItem(position))){
 			Toast.makeText(this, "Adding One", Toast.LENGTH_SHORT).show();
 			PlayerList.getInstance().session.add((Player)l.getAdapter().getItem(position));
 		}else{
 			Toast.makeText(this, "Removin One", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * 
 	 * Function which is called when a user clicks on a menu button
 	 * 
 	 * @param item
 	 * @return boolean
 	 * 
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {	
 		Intent intent;
 		switch (item.getItemId()) {
 		// Create new user
 		case R.id.adduser:
 			intent = new Intent(this, CreateUserActivity.class);
 			startActivity(intent);
 			break;
 			// Start the game
 		case R.id.start:
 			if(PlayerList.getInstance().session.size()>1 && PlayerList.getInstance().session.size()<7){  // 2 - 6 Players
 				intent = new Intent(this, GameActivity.class);
 				startActivityForResult(intent, GAME_HAS_FINISHED);
 			}
 			break;
 		}
 		return true;
 	}
 	
 	public void createUser(View view){
 		Intent intent = new Intent(this, CreateUserActivity.class);
 		startActivity(intent);
 	}
 	
 	public void start(View view){
 		if(PlayerList.getInstance().session.size()>1 && PlayerList.getInstance().session.size()<7){  // 2 - 6 Players
 			Intent intent = new Intent(this, GameActivity.class);
 			startActivityForResult(intent, GAME_HAS_FINISHED);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if(resultCode == RESULT_OK){
 			if(requestCode == GAME_HAS_FINISHED){
 				DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						switch(which){
 						case DialogInterface.BUTTON_POSITIVE:
 							Intent i = new Intent(SelectMultipleUserActivity.this, GameActivity.class);
 							SelectMultipleUserActivity.this.startActivityForResult(i, GAME_HAS_FINISHED);
 							break;
 						}
 					}
 				};
 				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 				dialog.setMessage(data.getStringExtra("msg") +" Start a new game?").setPositiveButton("Yes", clickListener).setNegativeButton("No", clickListener).show();
 			}
 		}
 	}
 
 }
