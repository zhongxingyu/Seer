 package edu.ucla.cens.budburst;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import edu.ucla.cens.budburst.data.Row;
 import edu.ucla.cens.budburst.models.PlantRow;
 
 public class PlantList extends ListActivity {
 
 	private SimpleAdapter adapter;
 	private static final String TAG = "PlantList";
 
 	private static final String ITEM_COMMON_NAME = "name";
 	private static final String ITEM_SPECIES_NAME = "description";
 	private static final String ITEM_IMG = "icon";
 
 	private static final int MENU_ADD = 0;
 	private static final int MENU_ONE = 1;
 	private static final int MENU_SETTINGS = 2;
 	private static final int MENU_SYNC = 3;
 
 	protected static final int CONTEXT_REMOVE = 0;
 	private static final int LOGIN_FINISHED = 0;
 
 	Button button1;
 	Button button2;
 	Button button3;
 	private BudburstDatabaseManager databaseManager;
 	private ArrayList<HashMap<String, String>> data;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.plant_list);
 
 		if (!PreferencesManager.isUserIn(this))
 			startActivityForResult(new Intent(this, LoginScreen.class), LOGIN_FINISHED);
 
 		databaseManager = Budburst.getDatabaseManager();
 
 		button1 = (Button) this.findViewById(R.id.button1);
 		button1.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// currentTask = new
 				// GrabCampaigns().execute("http://t5l-kullect.appspot.com/list?query=featured");
 			}
 		});
 
 		button2 = (Button) this.findViewById(R.id.button2);
 		button2.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// currentTask = new
 				// GrabCampaigns().execute("http://t5l-kullect.appspot.com/list?query=new");
 			}
 		});
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		ArrayList<Row> plants = databaseManager.getDatabase("plant").all();
 		data = new ArrayList<HashMap<String, String>>();
 		for (Iterator<Row> i = plants.iterator(); i.hasNext();) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			PlantRow current = (PlantRow) i.next();
 			map.put("name", current.species().common_name);
 			map.put("description", current.species().species_name);
 			map.put("icon", current.species().getImagePath());
 			map.put("_id", current._id.toString());
 			data.add(map);
 		}
 
 		adapter = new SimpleAdapter(this, data, R.layout.list_item, new String[] { ITEM_COMMON_NAME, ITEM_SPECIES_NAME, ITEM_IMG }, new int[] { R.id.name,
 				R.id.description, R.id.icon });
 		setListAdapter(this.adapter);
 
 		// set for long clicks
 		getListView().setOnCreateContextMenuListener(ContextMenuListener);
 
 		showUserName();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case LOGIN_FINISHED:
 			if (!PreferencesManager.isUserIn(this))
 				finish();
 			break;
 		}
 
 	}
 
 	private final OnCreateContextMenuListener ContextMenuListener = new OnCreateContextMenuListener() {
 
 		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 			menu.setHeaderTitle("Plant Actions");
 			// menu.add(0, CONTEXT_REMOVE, 0, "Remove");
 			// menu.add(0, CONTEXT_SHOW_ON_MAP, 1, "Show on map");
 			// menu.add(0, CONTEXT_VIEW, 2, "View tag");
 		}
 	};
 
 	// @Override
 	// public boolean onContextItemSelected(MenuItem item) {
 	// AdapterView.AdapterContextMenuInfo menuInfo =
 	// (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 	//
 	// switch (item.getItemId()) {
 	// case CONTEXT_REMOVE:
 	// databaseManager.getDatabase("plant").find(item.id).remove();
 	// this.adapter.notifyDataSetChanged();
 	//			
 	// break;
 	// }
 	//
 	// return super.onContextItemSelected(item);
 	// }
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 
 		Intent launchPlantIntent = new Intent(this, PlantInfo.class);
 		launchPlantIntent.putExtra("PlantID", Long.parseLong(data.get(position).get("_id")));
 		launchPlantIntent.putExtra("phase", "leaves");
 		startActivity(launchPlantIntent);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_ADD, 0, "Add Plant").setIcon(android.R.drawable.ic_menu_add);
 		menu.add(0, MENU_SETTINGS, 2, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
 		menu.add(0, MENU_SYNC, 3, "Sync").setIcon(android.R.drawable.ic_menu_rotate);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = null;
 
 		switch (item.getItemId()) {
 		case MENU_ADD:
 			intent = new Intent(this, AddPlant.class);
 			this.startActivity(intent);
 
 			break;
 		case MENU_SYNC:
 			intent = new Intent(this, SyncDatabases.class);
 			this.startActivity(intent);
 			break;
 		// case MENU_SETTINGS:
 		// intent = new Intent(this, settingsScreen.class);
 		// this.startActivity(intent);
 		//
 		// break;
 		case MENU_ONE:
 			// intent = new Intent(this, Help.class);
 			// this.startActivity(intent);
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	// added by EG to try to learn how to do this... add name after "Hello"
 	protected void showUserName() {
 		// Display user name at top of screen
 		TextView textView = (TextView) this.findViewById(R.id.TextView01);
 
 		String username_string = new String(PreferencesManager.currentUser(this));
 		textView.setText("Hello " + username_string + "!");
 
 	}
 }
