 package edu.bonn.mobilegaming.geoquest;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GameItem;
 
 public class GameListActivity extends GeoQuestListActivity {
 
     ListAdapter gameListAdapter;
     CharSequence repoName;
 
     private final static boolean enable_long_click_auto_game = false;
 
     private GameListActivity getInstance() {
 	return this;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 
 	setContentView(R.layout.gamelist);
 
 	this.repoName = getIntent()
 		.getCharSequenceExtra("edu.bonn.mobilegaming.geoquest.REPO");
 
 	String headerText = getText(R.string.start_gameList_headerPart).toString()
 		+ " \"" + this.repoName + "\":";
 	((TextView) findViewById(R.id.gamelistHeader)).setText(headerText);
 
 //		// Init data adapter - moved this code to onResume():
 //		gameListAdapter = new ArrayAdapter<String>(this, R.layout.game_item,
 //				GeoQuestApp.getGameNamesForRepository(repoName.toString()));
 //		setListAdapter(gameListAdapter);
 
 	if (enable_long_click_auto_game) { // remove this if-clause if you want
 					   // a context menu to be shown under
 					   // any circumstances
 	    registerForContextMenu(getListView());
 	}
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu,
 				    View v,
 				    ContextMenuInfo menuInfo) {
 	if (enable_long_click_auto_game) {
 	    menu.add(R.string.context_auto_start_game);
 	}
 	super.onCreateContextMenu(menu,
 				  v,
 				  menuInfo);
     }
 
     @Override
     public boolean onContextItemSelected(final MenuItem item) {
 	if (item.getTitle().equals(getString(R.string.context_auto_start_game))) {
 	    // create and show password dialog
 	    LinearLayout layout = new LinearLayout(this);
 	    layout.setOrientation(LinearLayout.VERTICAL);
 	    layout.setGravity(Gravity.CENTER_HORIZONTAL);
 	    final EditText input = new EditText(this);
 	    input.setTransformationMethod(android.text.method.PasswordTransformationMethod
 		    .getInstance());
 	    layout.setPadding(10,
 			      0,
 			      10,
 			      0);
 	    layout.addView(input);
 
 	    new AlertDialog.Builder(this)
 		    .setTitle(R.string.pw_dialog_title)
 		    .setMessage(R.string.pw_dialog_descr)
 		    .setView(layout)
 		    .setPositiveButton(R.string.ok,
 				       new DialogInterface.OnClickListener() {
 					   public
 						   void
 						   onClick(DialogInterface dialog,
 							   int whichButton) {
 					       String pw = input.getText()
 						       .toString();
 					       if (pw != null
 						       && pw.equals(PreferenceManager
 							       .getDefaultSharedPreferences(getInstance())
 							       .getString("pref_password",
 									  null))) {
 						   AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
 							   .getMenuInfo();
 						   String quest = (String) getListAdapter()
 							   .getItem(menuInfo.position);
 						   SharedPreferences mainPrefs = PreferenceManager
 							   .getDefaultSharedPreferences(getInstance());
 						   mainPrefs
 							   .edit()
 							   .putBoolean(Preferences.PREF_KEY_AUTO_START_GAME_CHECK,
 								       true)
 							   .commit();
 						   mainPrefs
 							   .edit()
 							   .putString(Preferences.PREF_KEY_AUTO_START_REPO,
 								      repoName.toString())
 							   .commit();
 						   mainPrefs
 							   .edit()
 							   .putString(Preferences.PREF_KEY_AUTO_START_GAME,
 								      quest)
 							   .commit();
 						   Toast.makeText(getInstance(),
 								  R.string.context_auto_start_game_toast,
 								  Toast.LENGTH_SHORT)
 							   .show();
 					       } else {
 						   new AlertDialog.Builder(
 							   getInstance())
 							   .setMessage(R.string.pw_dialog_wrong_pw)
 							   .show();
 					       }
 					   }
 				       })
 		    .setNegativeButton(R.string.cancel,
 				       new DialogInterface.OnClickListener() {
 					   public
 						   void
 						   onClick(DialogInterface dialog,
 							   int whichButton) {
 					   }
 				       }).show();
 	}
 	return super.onContextItemSelected(item);
     }
 
     @Override
     protected void onListItemClick(ListView l,
 				   View v,
 				   int position,
 				   long id) {
 	super.onListItemClick(l,
 			      v,
 			      position,
 			      id);
 	final String gameName = (String) gameListAdapter.getItem(position);
 	final GameItem gameItem = GeoQuestApp.getGameItem(repoName,
 							  gameName);
 	final String selectedRepo = repoName.toString();
 
 	startGame(gameItem,
 		  selectedRepo);
     }
    
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem sortItem = menu.add(R.string.game_list_sort);
 		sortItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			
 			public boolean onMenuItemClick(MenuItem item) {
 				Intent i = new Intent(GeoQuestApp.getContext(), edu.bonn.mobilegaming.geoquest.SortingOptionsActivity.class);
 				startActivity(i);
 				return false;
			}
		});
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Init data adapter
		gameListAdapter = new ArrayAdapter<String>(this, R.layout.game_item,
				GeoQuestApp.getGameNamesForRepository(repoName.toString()));
		setListAdapter(gameListAdapter);
	}
}
