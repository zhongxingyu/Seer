 package ws.wiklund.vinguiden.activities;
 
 import java.io.File;
 
 import ws.wiklund.guides.activities.CustomListActivity;
 import ws.wiklund.guides.list.BeverageListCursorAdapter;
 import ws.wiklund.guides.model.BaseModel;
 import ws.wiklund.guides.util.ExportDatabaseCSVTask;
 import ws.wiklund.guides.util.GetBeverageFromCursorTask;
 import ws.wiklund.guides.util.Selectable;
 import ws.wiklund.guides.util.ViewHelper;
 import ws.wiklund.vinguiden.R;
 import ws.wiklund.vinguiden.db.WineDatabaseHelper;
 import ws.wiklund.vinguiden.util.BootStrapHandler;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class WineListActivity extends CustomListActivity {
 	private static final String PRIVATE_PREF = "vinguiden";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		BootStrapHandler.init(this, PRIVATE_PREF);
 
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			startActivityForResult(new Intent(getApplicationContext(), WineFlowActivity.class), 0);
 		} else {		
 			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 			setContentView(R.layout.list);
 			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
 	
 			// Now create a new list adapter bound to the cursor.
 			cursorAdapter = new BeverageListCursorAdapter(this, getNewCursor(currentSortColumn));
 	
 			startManagingCursor(cursorAdapter.getCursor());
 
 			// Bind to our new adapter.
 			setListAdapter(cursorAdapter);
 	
 			ListView list = getListView();
 			list.setOnItemLongClickListener(new OnItemLongClickListener() {
 				@Override
 				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
 					handleLongClick(position);
 					return true;
 				}
 			});
 		}
 	}
 
 	protected Cursor getNewCursor(String sortColumn) {
 		if(db == null || !db.isOpen()) {		
 			db = new WineDatabaseHelper(this).getReadableDatabase();
 		}
 		
 		return db.rawQuery(WineDatabaseHelper.SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR + " ORDER BY " + sortColumn, null);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		new GetBeverageFromCursorTask(this, WineActivity.class).execute((Cursor) WineListActivity.this.getListAdapter().getItem(position));
 	}
 
 	@Override
 	public void notifyDataSetChanged() {
 		super.notifyDataSetChanged();
 		
 		if (cursorAdapter != null) {
 			WineDatabaseHelper helper = new WineDatabaseHelper(this);
 			int bottles = helper.getNoBottlesInCellar();
 			// Update title with no wines in cellar
 			if (bottles > 0) {
 				TextView view = (TextView) WineListActivity.this
 						.findViewById(R.id.title);
 
 				String text = view.getText().toString();
 				if (text.contains("(")) {
 					text = text.substring(0, text.indexOf("(") - 1);
 				}
 
 				view.setText(text + " (" + bottles + ")");
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.beverage_list_menu, menu);
 
 		return true;
 	}
 		
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.findItem(R.id.menuStats).setEnabled(hasSomeStats());
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuStats:
 			startActivityForResult(new Intent(WineListActivity.this.getApplicationContext(),StatsActivity.class), 0);
 			break;
 		case R.id.menuExport:
 			final AlertDialog alertDialog = new AlertDialog.Builder(WineListActivity.this).create();
 			alertDialog.setTitle(getString(R.string.export));
 			
 			final File exportFile = new File(ViewHelper.getRoot(), "export_guide.csv");
 			alertDialog.setMessage(String.format(getString(R.string.export_message), new Object[]{exportFile.getAbsolutePath()}));
 			
			alertDialog.setButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 			       new ExportDatabaseCSVTask(WineListActivity.this, new WineDatabaseHelper(WineListActivity.this), exportFile, WineListActivity.this.getListAdapter().getCount()).execute();
 				} 
 			});
 			
			alertDialog.setButton2(getString(android.R.string.no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					alertDialog.cancel();
 				} 
 			});
 
 			alertDialog.setCancelable(true);
 			alertDialog.setIcon(R.drawable.export);
 			alertDialog.show();
 			break;
 		case R.id.menuAbout:
 			startActivityForResult(new Intent(WineListActivity.this.getApplicationContext(), AboutActivity.class), 0);
 			break;
 		}
 
 		return true;
 	}
 	
 	@Override
 	public void addBeverage(View view) {
     	Intent intent = new Intent(view.getContext(), AddWineActivity.class);
     	startActivityForResult(intent, 0);
 	}
 
 	@Override
 	protected void select(Selectable selectable, int position) {
 		ListView listView = WineListActivity.this.getListView();
 		final Cursor c = (Cursor) listView.getItemAtPosition(position);
 
 		selectable.select(this, new WineDatabaseHelper(this), new BaseModel(c.getInt(0), c.getString(1)));
 	}
 	
 	@Override
 	protected void addSelectables() {
 		selectableAdapter.add(new Selectable(getString(R.string.addToCellar), R.drawable.icon, Selectable.ADD_ACTION));
 		selectableAdapter.add(new Selectable(getString(R.string.removeFromCellar), R.drawable.from_cellar, Selectable.REMOVE_ACTION));
 		selectableAdapter.add(new Selectable(getString(R.string.deleteTitle), R.drawable.trash, Selectable.DELETE_ACTION));
 	}
 	
 }
