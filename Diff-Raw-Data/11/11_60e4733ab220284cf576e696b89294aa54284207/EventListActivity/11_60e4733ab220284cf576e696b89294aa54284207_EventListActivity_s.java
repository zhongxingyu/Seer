 package com.dzebsu.acctrip;
 
 import java.util.List;
import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
import android.content.res.Configuration;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 import com.dzebsu.acctrip.adapters.EventListViewAdapter;
 import com.dzebsu.acctrip.db.EventAccDbHelper;
 import com.dzebsu.acctrip.db.datasources.EventDataSource;
 import com.dzebsu.acctrip.models.Event;
 import com.dzebsu.acctrip.settings.SettingsActivity;
 
 public class EventListActivity extends Activity {
 
 	// Anonymous class wanted this adapter inside itself
 	private EventListViewAdapter adapterZ = null;
 
 	// for restoring list scroll position
 	private static final String LIST_STATE = "listState";
 
 	private Parcelable mListState = null;
 
 	private boolean dataChanged = false;
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		mListState = state.getParcelable(LIST_STATE);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		String languageToLoad = "ru"; // your language
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources()
				.updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
 
 		// to initialize default settings for the first and only time
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		setContentView(R.layout.activity_event_list);
 		// getListView().addHeaderView(new SearchView(this));
 		fillEventList();
 		ListView listView = (ListView) findViewById(R.id.event_list);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				onSelectEvent(id);
 			}
 		});
 		// add filter_Event_Edittext for events names
 		SearchView eventsFilter = (SearchView) findViewById(R.id.event_SearchView);
 		eventsFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				return false;
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				EventListActivity.this.adapterZ.getFilter().filter(newText);
 				return true;
 			}
 		});
 		// end
 		Intent intent = getIntent();
 		if (intent.hasExtra("toast"))
 			Toast.makeText(getApplicationContext(), intent.getIntExtra("toast", R.string.not_message),
 					Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.open_dictionaries:
 				onOpenDictionaries(item.getActionView());
 				return true;
 			case R.id.recreate_all_tables:
 				// TODO deprecated. delete!
 				// reCreateAllTables(item.getActionView());
 				return true;
 			case R.id.settings:
 				startActivity(new Intent(this, SettingsActivity.class));
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void reCreateAllTables(View view) {
 
 		EventAccDbHelper dbHelper = new EventAccDbHelper(this);
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		dbHelper.reCreateAllTables(db);
 		db.close();
 	}
 
 	public void onNewEvent(View view) {
 		Intent intent = new Intent(this, EditEventActivity.class);
 		String name = ((SearchView) findViewById(R.id.event_SearchView)).getQuery().toString();
 		intent.putExtra("eventName", name);
 		// intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 		startActivity(intent);
 	}
 
 	public void onOpenDictionaries(View view) {
 		Intent intent = new Intent(this, DictionaryActivity.class);
 		startActivity(intent);
 	}
 
 	public void onSelectEvent(long eventId) {
 		Intent intent = new Intent(this, OperationListActivity.class);
 		intent.putExtra("eventId", eventId);
 		startActivity(intent);
 	}
 
 	private void fillEventList() {
 		if (adapterZ == null || dataChanged) {
 			dataChanged = false;
 			EventDataSource dataSource = new EventDataSource(this);
 			List<Event> events = dataSource.getEventList();
 			adapterZ = new EventListViewAdapter(this, events);
 		}
 		ListAdapter adapter = adapterZ;
 		ListView listView = (ListView) findViewById(R.id.event_list);
 		// trigger filter to it being applied on resume
 		listView.setAdapter(adapter);
 		EventListActivity.this.adapterZ.getFilter().filter(
 				((SearchView) findViewById(R.id.event_SearchView)).getQuery());
 
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		dataChanged = true;
 		// fillEventList();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// think itn't needed here
 		fillEventList();
 		if (mListState != null) ((ListView) findViewById(R.id.event_list)).onRestoreInstanceState(mListState);
 		mListState = null;
 	}
 
 	// scroll position saving
 	@Override
 	protected void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		mListState = ((ListView) findViewById(R.id.event_list)).onSaveInstanceState();
 		state.putParcelable(LIST_STATE, mListState);
 	}
 }
