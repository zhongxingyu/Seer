 package org.dhappy.habits;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 
 import org.dhappy.android.widget.HeaderItem;
 import org.dhappy.android.widget.HeaderedListAdapter;
 import org.dhappy.android.widget.ListItem;
 import org.dhappy.android.widget.TextTimeItem;
 import org.dhappy.android.widget.Timer;
 import org.dhappy.habits.R;
 import org.dhappy.habits.contentprovider.HabitContentProvider;
 import org.dhappy.habits.database.EventTable;
 import org.dhappy.habits.database.GoalTable;
 import org.dhappy.habits.database.HabitTable;
 import org.dhappy.habits.database.ReadingTable;
 
 
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.Contacts;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 
 
 public class EventListFragment extends ListFragment
         implements LoaderManager.LoaderCallbacks<Cursor> {
 	private static final int MENU_DELETE = Menu.FIRST + 3;
 	public static final String FRAGMENT_KEY = "org.dhappy.habits.event.list";
	public static final int LOADER_KEY = 10;
 	
     List<ListItem> items = new ArrayList<ListItem>();
     HeaderedListAdapter mAdapter;
     
     @Override public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         // Give some text to display if there is no data.
         setEmptyText(getString(R.string.no_events));
 
         setHasOptionsMenu(true);
         
         registerForContextMenu(getListView());
 
         mAdapter = new HeaderedListAdapter(getActivity(), items);
         setListAdapter(mAdapter);
         
         // Start out with a progress indicator.
         setListShown(false);
 
         // Prepare the loader.  Either re-connect with an existing one,
         // or start a new one.
        getLoaderManager().initLoader(10, null, this);
         
         MainActivity activity = (MainActivity) getActivity();
         getFragmentManager().putFragment(activity.getSharedBundle(), FRAGMENT_KEY, this);
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         MenuItem item = menu.add(R.string.menu_new);
         item.setIcon(android.R.drawable.ic_input_add);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         item.setIntent(new Intent(getActivity(), EventDetailActivity.class));
     }
 
     @Override  
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
     	super.onCreateContextMenu(menu, v, menuInfo);  
     	menu.setHeaderTitle(R.string.event_options_header);
     	menu.add(ContextMenu.NONE, MENU_DELETE, ContextMenu.NONE, R.string.menu_delete);
     }
 
     @Override  
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_DELETE:
           AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
           int id = ((TextTimeItem) items.remove((int) info.position)).id;
           Uri uri = Uri.parse(HabitContentProvider.EVENTS_URI + "/" + id);
 
           getActivity().getContentResolver().delete(uri, null, null);
 
           mAdapter.notifyDataSetChanged();
           
           return true;
         }
         return super.onContextItemSelected(item);
     }
     
     @Override public void onListItemClick(ListView l, View v, int position, long listId) {
     	super.onListItemClick(l, v, position, listId);
         Intent intent = new Intent(getActivity(), EventDetailActivity.class);
         int id = ((TextTimeItem) items.get((int) position)).id;
         Uri eventUri = Uri.parse(HabitContentProvider.EVENTS_URI + "/" + id);
         intent.putExtra(HabitContentProvider.EVENT_CONTENT_ITEM_TYPE, eventUri);
 
         startActivity(intent);
     }
 
     // These are the rows that we will retrieve.
     static final String[] EVENTS_PROJECTION = new String[] {
     	EventTable.TABLE_EVENT + "." + EventTable.COLUMN_ID + " AS " + EventTable.COLUMN_ID,
         HabitTable.COLUMN_NAME,
         HabitTable.COLUMN_COLOR,
         ReadingTable.COLUMN_WEIGHT,
         EventTable.TABLE_EVENT + "." + EventTable.COLUMN_TIME + " AS " + EventTable.COLUMN_TIME
     };
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         // Now create and return a CursorLoader that will take care of
         // creating a Cursor for the data being displayed.
         return new CursorLoader(getActivity(), HabitContentProvider.EVENTS_URI, EVENTS_PROJECTION, null, null, EventTable.COLUMN_TIME + " DESC");
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         items.clear();
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("EE, d MMM y");
         
         TextTimeItem lastItem = null;
         if(cursor.moveToFirst()) {
         	do {
         		int id = cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID));
         		String name = cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_NAME));
         		long time = cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_TIME));
         		String color = cursor.getString(cursor.getColumnIndexOrThrow(HabitTable.COLUMN_COLOR));
         		TextTimeItem nextItem = new TextTimeItem(name, time, color, id);
         		
         		if(lastItem == null || nextItem.time.get(Calendar.DAY_OF_YEAR) != lastItem.time.get(Calendar.DAY_OF_YEAR)) {
         	        items.add(new HeaderItem(dateFormat.format(nextItem.time.getTime())));
         		}
         		items.add(nextItem);
         		lastItem = nextItem;
         	} while(cursor.moveToNext());
         }
 
         mAdapter.notifyDataSetChanged();
         
         if(isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         // This is called when the last Cursor provided to onLoadFinished()
         // above is about to be closed.  We need to make sure we are no
         // longer using it.
         //mAdapter.swapCursor(null);
     }
 }
