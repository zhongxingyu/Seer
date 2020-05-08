 package com.karhatsu.suosikkipysakit.ui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import com.karhatsu.suosikkipysakit.R;
 import com.karhatsu.suosikkipysakit.db.StopCollectionDao;
 import com.karhatsu.suosikkipysakit.db.StopDao;
 import com.karhatsu.suosikkipysakit.domain.Stop;
 import com.karhatsu.suosikkipysakit.domain.StopCollection;
 import com.karhatsu.suosikkipysakit.util.AccountInformation;
 
 public class MainActivity extends Activity implements OnStopEditCancel {
 
 	private StopDao stopDao;
 
 	private SaveStopDialog renameStopDialog;
 	private AddToCollectionDialog addToCollectionDialog;
 
 	private RenameStopId stopToBeRenamedId;
 	private AddToCollectionId stopToBeAddedToCollection;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		AccountInformation.initialize(this);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setupStopListView();
 		resumeToPreviousState();
 	}
 
 	protected void resumeToPreviousState() {
 		Object retained = getLastNonConfigurationInstance();
 		if (retained instanceof RenameStopId) {
 			stopToBeRenamedId = (RenameStopId) retained;
 			showStopRenameDialog();
 		} else if (retained instanceof AddToCollectionId) {
 			stopToBeAddedToCollection = (AddToCollectionId) retained;
 			showAddToCollectionDialog();
 		} else {
 			redirectToDeparturesIfRequested();
 		}
 	}
 
 	private void redirectToDeparturesIfRequested() {
 		Stop stop = getIntent().getParcelableExtra(Stop.STOP_KEY);
 		String stopCode = getIntent().getStringExtra(Stop.CODE_KEY);
 		if (stop != null) {
 			showDepartures(stop, null);
 		} else if (stopCode != null) {
 			showDepartures(null, stopCode);
 		}
 	}
 
 	private void setupStopListView() {
 		final ListView stopListView = getStopListView();
 		final ListAdapter stopListAdapter = createStopListAdapter();
 		stopListView.setAdapter(stopListAdapter);
 		stopListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String selectedStopCode = getSelectedStopCode(stopListAdapter,
 						position);
 				if (selectedStopCode != null) {
 					showDepartures(null, selectedStopCode);
 				} else {
 					long selectedCollectionId = getSelectedCollectionId(stopListAdapter, position);
 					if (new StopCollectionDao(MainActivity.this).containsStops(selectedCollectionId)) {
 						showCollectionDepartures(selectedCollectionId);
 					} else {
 						ToastHelper.showToast(MainActivity.this, R.string.activity_main_no_stops_in_collection);
 					}
 				}
 			}
 		});
 		registerForContextMenu(stopListView);
 	}
 
 	private void showDepartures(Stop stop, String stopCode) {
 		Intent intent = new Intent(MainActivity.this, DeparturesActivity.class);
 		if (stop != null) {
 			intent.putExtra(Stop.STOP_KEY, stop);
 		} else {
 			intent.putExtra(Stop.CODE_KEY, stopCode);
 		}
 		startActivity(intent);
 	}
 
 	private void showCollectionDepartures(long id) {
 		Intent intent = new Intent(MainActivity.this, DeparturesActivity.class);
 		intent.putExtra(StopCollection.COLLECTION_ID_KEY, id);
 		startActivity(intent);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshStopList();
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		if (stopToBeRenamedId != null) {
 			return stopToBeRenamedId;
 		}
 		return null;
 	}
 
 	private ListAdapter createStopListAdapter() {
 		stopDao = new StopDao(this);
 		Cursor cursor = stopDao.findAllStopsAndCollections();
 		return new StopListAdapter(this, cursor);
 	}
 
 	private String getSelectedStopCode(final ListAdapter stopListAdapter,
 			int position) {
 		Cursor cursor = (Cursor) stopListAdapter.getItem(position);
 		cursor.moveToPosition(position);
 		return cursor.getString(1);
 	}
 
 	private long getSelectedCollectionId(final ListAdapter stopListAdapter,
 									   int position) {
 		Cursor cursor = (Cursor) stopListAdapter.getItem(position);
 		cursor.moveToPosition(position);
 		return cursor.getLong(0);
 	}
 
 	public void addStop(View button) {
 		Intent intent = new Intent(this, AddStopActivity.class);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, view, menuInfo);
 		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		Cursor cursor = (Cursor) getStopListAdapter().getItem(info.position);
 		String columnCode = cursor.getString(1);
 		MenuInflater menuInflater = getMenuInflater();
 		if (columnCode != null) {
 			menuInflater.inflate(R.menu.menu_stop_item, menu);
 		} else {
 			menuInflater.inflate(R.menu.menu_collection_item, menu);
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.menu_stop_item_rename:
 			stopToBeRenamedId = new RenameStopId(info.id);
 			showStopRenameDialog();
 			refreshStopList();
 			return true;
 		case R.id.menu_stop_item_add_to_collection:
 			stopToBeAddedToCollection = new AddToCollectionId(info.id);
 			showAddToCollectionDialog();
 			refreshStopList();
 			return true;
 		case R.id.menu_stop_item_delete:
 			new StopDao(this).delete(info.id);
 			refreshStopList();
 			return true;
 		case R.id.menu_collection_item_delete:
 			new StopCollectionDao(this).delete(info.id);
 			refreshStopList();
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	private void showStopRenameDialog() {
 		renameStopDialog = new RenameStopDialog(this, this, stopToBeRenamedId.id);
 		renameStopDialog.show();
 	}
 
 	private void showAddToCollectionDialog() {
 		addToCollectionDialog = new AddToCollectionDialog(this, this, stopToBeAddedToCollection.id);
 		addToCollectionDialog.show();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (renameStopDialog != null) {
 			renameStopDialog.dismiss();
 		}
 		if (addToCollectionDialog != null) {
 			addToCollectionDialog.dismiss();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		closeDbConnections();
 	}
 
 	public void refreshStopList() {
 		closeDbConnections();
 		stopDao = new StopDao(this);
 		Cursor cursor = stopDao.findAllStopsAndCollections();
 		getStopListAdapter().changeCursor(cursor);
 	}
 
 	private void closeDbConnections() {
 		StopListAdapter adapter = getStopListAdapter();
 		if (adapter != null && adapter.getCursor() != null) {
 			adapter.getCursor().close();
 		}
 		if (stopDao != null) {
 			stopDao.close();
 		}
 	}
 
 	private ListView getStopListView() {
 		return (ListView) findViewById(R.id.stop_list);
 	}
 
 	private StopListAdapter getStopListAdapter() {
 		ListView stopListView = getStopListView();
 		return (StopListAdapter) stopListView.getAdapter();
 	}
 
 	@Override
 	public void stopEditCancelled() {
 		stopToBeRenamedId = null;
 		stopToBeAddedToCollection = null;
 	}
 
 	private class RenameStopId {
 		private final long id;
 		private RenameStopId(long id) {
 			this.id = id;
 		}
 	}
 
 	private class AddToCollectionId {
 		private final long id;
 		private AddToCollectionId(long id) {
 			this.id = id;
 		}
 	}
 }
