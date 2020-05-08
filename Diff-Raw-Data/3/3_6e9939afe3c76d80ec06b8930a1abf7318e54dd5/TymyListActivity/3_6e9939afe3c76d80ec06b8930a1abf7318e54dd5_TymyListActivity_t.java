 package com.ph.tymyreader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.ph.tymyreader.model.TymyPref;
 
 public class TymyListActivity extends ListActivity {
 	//	private static final String TAG = TymyReader.TAG;
 	private static final int EDIT_TYMY_ACTIVITY = 1;
 	private static final int DS_LIST_ACTIVITY = 2;
 	private String[] from = new String[] {TymyPref.ONE, TymyPref.TWO};
 	private int[] to = new int[] {R.id.text1, R.id.text2};
 	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
 	private List<TymyPref> tymyPrefList = new ArrayList<TymyPref>();
 	private SimpleAdapter adapter;
 	private TymyListUtil tlu;
 	ListView lv;
 	private TymyReader app; 
 	private List<LoginAndUpdateTymy> loginAndUpdateTymy = new ArrayList<TymyListActivity.LoginAndUpdateTymy>();
 	private UpdateNewItemsTymy updateNewItemsTymy;
 	private ProgressBar pb;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tymy_list);
 		app = (TymyReader) getApplication();
 		tlu = new TymyListUtil();
		pb = (ProgressBar) findViewById(R.id.progress_bar);
 		
 		TymyListActivity oldState = (TymyListActivity) getLastNonConfigurationInstance();
 		if (oldState == null) {
 			// activity was started => load configuration
 			app.loadTymyCfg();
 			tymyPrefList = app.getTymyPrefList();
 			adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
 			
 			refreshListView();
 			//refresh discussions from web
 //			reloadTymyDsList(); // reload i seznamu diskusi, muze byt pomaljesi
 			reloadTymyNewItems(); // reload pouze poctu novych prispevku
 		} else {
 			// Configuration was changed, reload data
 			tymyList = oldState.tymyList;
 			tymyPrefList = oldState.tymyPrefList;
 			updateNewItemsTymy = oldState.updateNewItemsTymy;
 //			pb = oldState.pb;
 //			pb.setVisibility(oldState.pb.getVisibility());
 			adapter = oldState.adapter;
 			refreshListView();
 		}
 		// Set-up adapter for tymyList
 		lv = getListView();
 		lv.setAdapter(adapter);
 
 		registerForContextMenu(getListView());
 		if (!app.isOnline()) {
 			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (isFinishing()) {
 			cancelBackgroundTasks();
 		}
 	}
 
 	@Override
 	protected void onDestroy () {
 		super.onDestroy();
 		//save configuration
 		app.saveTymyCfg(tymyPrefList);
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return this;
 	}
 
 	// **************  Activity Option menu  ************** //
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_tymy_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			showSettings();
 			return true;
 		case R.id.menu_add_tymy:
 			showAddTymy();
 			return true;
 		case R.id.menu_refresh:
 //			refreshTymyPrefList();
 			reloadTymyNewItems();
 			return true;
 //		case R.id.menu_send_report:
 //			ACRA.getErrorReporter().handleException(new Exception("Manual report"));
 //			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get(position).get(TymyPref.ONE));
 		if ((index == -1) || (tymyPrefList.size() == 0) || tymyPrefList.get(index).noDs()) {
 			Toast.makeText(this, getString(R.string.no_discussion), Toast.LENGTH_LONG).show();
 			return;
 		}
 		cancelBackgroundTasks();	
 		Bundle bundle = new Bundle();
 		bundle.putInt("index", index);
 		Intent intent = new Intent(this, DiscussionListActivity.class);
 		intent.putExtras(bundle);
 		startActivityForResult(intent, DS_LIST_ACTIVITY);				
 	}
 
 	// **************  Context menu  ************** //
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		MenuInflater inflater = getMenuInflater();
 		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get(info.position).get(TymyPref.ONE));
 		if (index != -1) {
 			menu.setHeaderTitle(tymyPrefList.get(index).getUrl());
 			inflater.inflate(R.menu.tymy_list_context_menu, menu);
 		}
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get((int) info.id).get(TymyPref.ONE));
 		switch(item.getItemId()) {
 		case R.id.menu_context_edit:
 			showAddTymy(index);
 			return true;
 		case R.id.menu_context_delete:
 			deleteTymy(index);
 			return true;
 		case R.id.menu_context_web:
 			goToWeb(index);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	private void goToWeb(int index) {
 		String attr = new String();
 		if (tymyPrefList.get(index).getHttpContext() == null) {
 			// TODO Doresit spravnou skladbu parametru v URL aby nebylo nutne prihlasovani
 			attr = TymyLoader.getURLLoginAttr(tymyPrefList.get(index).getHttpContext());
 		}
 		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + tymyPrefList.get(index).getUrl() + attr));
 		startActivity(browserIntent);
 	}
 
 	// *****************  Setting  ******************** //
 	private void showSettings() {
 		Intent intent = new Intent(this, GeneralSettingsActivity.class);
 		startActivity(intent);		
 	}
 
 	private void showAddTymy() {
 		showAddTymy(-1);
 	}
 
 	private void showAddTymy(int position) {
 		Bundle bundle = new Bundle();
 		bundle.putInt("position", position);
 		Intent intent = new Intent(this, EditTymyActivity.class);
 		intent.putExtras(bundle);
 		startActivityForResult(intent, EDIT_TYMY_ACTIVITY);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case EDIT_TYMY_ACTIVITY:
 			if (resultCode == RESULT_OK) {
 				int index = data.getIntExtra("index", -1);
 				reloadTymyDsList(index);
 				refreshListView();
 			}
 			break;
 		case DS_LIST_ACTIVITY:
 			if (resultCode == RESULT_OK) {
 				int index = data.getIntExtra("index", -1);
 				reloadTymyNewItems(index);
 			}
 			break;	
 		}
 	}
 
 	private void deleteTymy(int position) {
 		app.deleteTymyCfg(tymyPrefList.get(position).getUrl());
 		tlu.removeTymyPref(tymyPrefList, position);
 		app.setTymyPrefList(tymyPrefList);
 		refreshListView();
 	}
 
 	private void cancelBackgroundTasks() {
 		//cancel background threads
 		for (LoginAndUpdateTymy loader : loginAndUpdateTymy) {
 			if (loader != null) {
 				loader.cancel(true);
 			}
 		}
 		if (updateNewItemsTymy != null) {
 			updateNewItemsTymy.cancel(true);
 		}
 	}
 
 	// TODO tyhle methody by meli byt v samostatny tride
 	private void reloadTymyDsList(int index) {
 		if (app.isOnline()) {
 			if (index == -1) reloadTymyDsList();
 			int i = 0;
 			i = loginAndUpdateTymy.size();
 			loginAndUpdateTymy.add(i, (LoginAndUpdateTymy) new LoginAndUpdateTymy());
 			loginAndUpdateTymy.get(i).execute(tymyPrefList.get(index));
 			app.setTymyPrefList(tymyPrefList);
 			app.saveTymyCfg(tymyPrefList);
 		}
 	}
 
 
 	private void reloadTymyDsList() {
 		if (app.isOnline()) {
 			// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
 			ArrayList<TymyPref> copy_tymyPrefList = new ArrayList<TymyPref>();
 			for (TymyPref tP : tymyPrefList) {
 				copy_tymyPrefList.add(tP);
 			}
 			int i = 0;
 			for(TymyPref tP : copy_tymyPrefList) {
 				i = loginAndUpdateTymy.size();
 				int index = copy_tymyPrefList.indexOf(tP);
 				loginAndUpdateTymy.add(i, (LoginAndUpdateTymy) new LoginAndUpdateTymy());
 				loginAndUpdateTymy.get(i).execute(tP);
 				tymyPrefList.remove(index);
 				tymyPrefList.add(index, tP);
 				app.setTymyPrefList(tymyPrefList);
 				//This maybe could cause problems when due to lost connectivity the download data will be corrupted,
 				//but next update should fix it (or refresh UI functionality)
 				app.saveTymyCfg(tymyPrefList);
 			}
 		}
 	}
 
 	private void reloadTymyNewItems(int index) {
 		if (app.isOnline()) {
 			if (index == -1) reloadTymyNewItems();
 			// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
 			updateNewItemsTymy = new UpdateNewItemsTymy();
 			updateNewItemsTymy.execute(tymyPrefList.get(index));
 			app.setTymyPrefList(tymyPrefList);
 			refreshListView();
 		}
 	}
 
 	private void reloadTymyNewItems() {
 		if (app.isOnline()) {
 			if (updateNewItemsTymy != null) {
 				updateNewItemsTymy.cancel(true);
 			}
 			updateNewItemsTymy = new UpdateNewItemsTymy();
 			updateNewItemsTymy.execute(tymyPrefList.toArray(new TymyPref[tymyPrefList.size()]));
 		}
 	}
 
 	private void refreshListView() {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		String noNewItems = pref.getString(getString(R.string.no_new_items_key), getString(R.string.no_new_items_default));
 //		tymyPrefList = app.getTymyPrefList();
 		if (tymyPrefList.isEmpty()) {
 			tlu.addMapToList(true, getString(R.string.no_tymy), getString(R.string.no_tymy_hint), tymyList);						
 		} else {
 			tlu.updateTymyList(tymyPrefList, noNewItems, tymyList);
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	//**************************************************************//
 	//*******************  AsyncTasks  *****************************//
 	private class LoginAndUpdateTymy extends AsyncTask<TymyPref, Integer, TymyPref> {
 
 		@Override
 		protected TymyPref doInBackground(TymyPref... tymyPref) {			
 			return tlu.updateTymDs(tymyPref);
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			setProgress(progress[0]);
 		}
 
 		// onPostExecute displays the results of the AsyncTask.
 		@Override
 		protected void onPostExecute(TymyPref tymyPref) {
 			//			Toast.makeText(getApplicationContext(), "discussions list " + tymyPref.getUrl() + " updated" , Toast.LENGTH_SHORT).show();
 			refreshListView();
 			//save configuration
 			app.saveTymyCfg(tymyPrefList);
 		}
 
 	}
 
 	private class UpdateNewItemsTymy extends AsyncTask<TymyPref, Integer, Void> {
 
 		@Override
 		protected void onPreExecute() {
 			pb.setVisibility(View.VISIBLE);	
 		}
 		
 		@Override
 		protected Void doInBackground(TymyPref... tymyPref) {			
 			for (TymyPref tp : tymyPref) {
 				tlu.updateNewItems(tp);
 				publishProgress(0);
 				if (isCancelled()) break;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			setProgress(progress[0]);
 			refreshListView();
 		}
 
 		// onPostExecute displays the results of the AsyncTask.
 		@Override
 		protected void onPostExecute(Void v) {
 			pb.setVisibility(View.GONE);
 		}
 		@Override
 		protected void onCancelled() {
 			pb.setVisibility(View.GONE);			
 		}
 	}
 }
