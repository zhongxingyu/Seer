 package ws.wiklund.guides.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ws.wiklund.guides.R;
 import ws.wiklund.guides.util.Notifyable;
 import ws.wiklund.guides.util.Selectable;
 import ws.wiklund.guides.util.SelectableAdapter;
 import ws.wiklund.guides.util.Sortable;
 import ws.wiklund.guides.util.SortableAdapter;
 import ws.wiklund.guides.util.ViewHelper;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.SimpleCursorAdapter;
 
 public abstract class CustomListActivity extends ListActivity implements Notifyable {
 	private static final String SORT_COLUMN = "sort_column";
 
 	protected List<Sortable> sortableItems;
 
 	protected String currentSortColumn = "beverage.name asc";
 	
 	protected SimpleCursorAdapter cursorAdapter;
 	protected SortableAdapter sortableAdapter;
 	protected SelectableAdapter selectableAdapter;
 
 	protected SQLiteDatabase db;
 	
 	private int currentPosition;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		sortableItems = new ArrayList<Sortable>();
         
         sortableItems.add(new Sortable(
         		getString(R.string.sortOnName), 
         		getString(R.string.sortOnNameSub), 
         		R.drawable.descending, 
         		"beverage.name"));
 
         sortableItems.add(new Sortable(
         		getString(R.string.sortOnRank), 
         		getString(R.string.sortOnRankSub), 
         		R.drawable.rating, 
         		"beverage.rating"));
 
         sortableItems.add(new Sortable(
         		getString(R.string.sortOnType), 
         		getString(R.string.sortOnTypeSub), 
         		R.drawable.icon, 
         		"beverage_type.name"));
         
         sortableItems.add(new Sortable(
         		getString(R.string.sortOnAmountInCellar), 
         		getString(R.string.sortOnAmountInCellarSub), 
         		R.drawable.amount, 
         		"total"));
 
         sortableAdapter = new SortableAdapter(this, R.layout.spinner_row, sortableItems, getLayoutInflater());
 		
 		selectableAdapter = new SelectableAdapter(this, R.layout.spinner_row, getLayoutInflater()){
 			public boolean isAvailableInCellar() {
 				Cursor cursor = (Cursor) getListView().getItemAtPosition(currentPosition);
 				return ViewHelper.getBeverageFromCursor(cursor).getBottlesInCellar() > 0;
 			}
 		};
 		
         SharedPreferences prefs = getSharedPreferences(SORT_COLUMN, 0);
         currentSortColumn = prefs.getString("sortColumn", "beverage.name asc");
         
 		addSelectables();
 	}
 	
 	public void sortList(View view) {
 		AlertDialog.Builder sortingDialog = new AlertDialog.Builder(this); 
 		sortingDialog.setTitle(R.string.sort_list);
 		sortingDialog.setSingleChoiceItems( sortableAdapter, 0, new OnClickListener() { 
 	        @Override 
 	        public void onClick(DialogInterface dialog, int which) { 
                 dialog.dismiss();
                 sort(sortableItems.get(which));
             }
         }); 
 
 		sortingDialog.show(); 
 	}
 	
 	public void notifyDataSetChanged() {
 		if(db == null || cursorAdapter == null) {
 			return;
 		}
 		
 		if (!db.isOpen() || cursorAdapter.getCount() == 0) {
 			stopManagingCursor(cursorAdapter.getCursor());
 
 			cursorAdapter.changeCursor(getNewCursor(currentSortColumn));
 
 			startManagingCursor(cursorAdapter.getCursor());
 		}
 
 		cursorAdapter.getCursor().requery();
 	}
 	
 	@Override
 	protected void onStart() {
     	super.onStart();
 		ViewHelper.handleAds(this);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (cursorAdapter != null) {
 			Cursor c = cursorAdapter.getCursor();
 
 			if(c != null) {
 				stopManagingCursor(c);
 				c.close();
 			}
 		}
 		
 		if (db != null && db.isOpen()) {
 			db.close();
 		}		
 
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		notifyDataSetChanged();
 	}
 
 	@Override
 	protected void onRestart() {
 		notifyDataSetChanged();
 		super.onRestart();
 	}
 
 	@Override
 	protected void onResume() {
 		notifyDataSetChanged();
 		super.onResume();
 	}
 	
 	protected void handleLongClick(final int position) {
 		currentPosition = position;
 		
 		Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
 		
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		alertDialog.setTitle(cursor.getString(1));
 		
 		alertDialog.setSingleChoiceItems( selectableAdapter, 0, new OnClickListener() { 
             @Override 
             public void onClick(DialogInterface dialog, int which) { 
                 dialog.dismiss();
                 select(selectableAdapter.getItem(which), position);
             }
 		}); 
 
 		alertDialog.show(); 				
 	}	
 
 	protected boolean hasSomeStats() {
		return cursorAdapter != null && cursorAdapter.getCount() > 0;
 	}
 
 	protected void sort(Sortable sortable) {
 		currentSortColumn = sortable.getSortColumn();
 
 		cursorAdapter.changeCursor(getNewCursor(currentSortColumn));
 
 		cursorAdapter.getCursor().requery();
 		
 		//Persist sort column
         SharedPreferences prefs = getSharedPreferences(SORT_COLUMN, 0);
         SharedPreferences.Editor editor = prefs.edit();
             
         editor.putString("sortColumn", currentSortColumn);
         editor.commit();
 	}
 
 	protected abstract void select(Selectable selectable, int position);
 	protected abstract void addSelectables();
 	protected abstract Cursor getNewCursor(String sortColumn);
 	
 	public abstract void addBeverage(View view);
 	
 }
