 package org.dhappy.habits;
 
 import org.dhappy.android.widget.HeaderedListAdapter;
 import org.dhappy.android.widget.Timer;
 import org.dhappy.habits.R;
 import org.dhappy.habits.contentprovider.HabitContentProvider;
 import org.dhappy.habits.database.DescriptorTable;
 import org.dhappy.habits.database.EventTable;
 import org.dhappy.habits.database.HabitTable;
 import org.dhappy.habits.database.ReadingTable;
 
 
 import android.app.DialogFragment;
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.ContentValues;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 
 
 public class DescriptorListFragment extends ListFragment
         implements LoaderManager.LoaderCallbacks<Cursor>, DescriptorWeightDialog.DescriptorWeightDialogListener {
 	private static final int MENU_EDIT = Menu.FIRST + 1;
 	private static final int MENU_DELETE = Menu.FIRST + 2;
 	
 	String TAG = "DescriptorListFragment";
 	
     // This is the Adapter being used to display the list's data.
     SimpleCursorAdapter mAdapter;
 
     @Override public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         // Give some text to display if there is no data.  In a real
         // application this would come from a resource.
         setEmptyText(getString(R.string.no_moods));
 
         // We have a menu item to show in action bar.
         setHasOptionsMenu(true);
 
         registerForContextMenu(getListView());
 
         String[] from = new String[] { DescriptorTable.COLUMN_NAME, DescriptorTable.COLUMN_COLOR };
         int[] to = new int[] { R.id.label, R.id.color_block };
 
         mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.descriptor_row, null, from, to, 0);
 
         mAdapter.setViewBinder(new ViewBinder() {
     		@Override
     		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
     			if(columnIndex == 2) { // Color
     				view.setBackgroundColor(Color.parseColor(cursor.getString(columnIndex)));
     				return true;
     			}
 
     			if(columnIndex == 3) { // Time
 					Timer timer = (Timer) view;
     				if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_NULL) {
     					timer.setVisibility(View.GONE);
     				} else {
     					timer.setVisibility(View.VISIBLE);
     					long time = cursor.getInt(columnIndex);
     					timer.setStartingTime(time * 1000);
     				}
     				return true;
     			}
 
     			return false;
     		}
         });
 
         setListAdapter(mAdapter);
 
         // Start out with a progress indicator.
         setListShown(false);
 
         // Prepare the loader.  Either re-connect with an existing one,
         // or start a new one.
         getLoaderManager().initLoader(0, null, this);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         MenuItem item = menu.add("New");
         item.setIcon(android.R.drawable.ic_input_add);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         item.setIntent(new Intent(getActivity(), DescriptorDetailActivity.class));
     }
 
     @Override  
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
     	super.onCreateContextMenu(menu, v, menuInfo);  
     	menu.setHeaderTitle("Descriptor Options");
     	menu.add(ContextMenu.NONE, MENU_EDIT, ContextMenu.NONE, R.string.menu_edit);
     	menu.add(ContextMenu.NONE, MENU_DELETE, ContextMenu.NONE, R.string.menu_delete);
     }
 
     @Override  
     public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
         case MENU_EDIT:
             Intent intent = new Intent(getActivity(), DescriptorDetailActivity.class);
             Uri habitUri = Uri.parse(HabitContentProvider.DESCRIPTORS_URI + "/" + info.id);
             intent.putExtra(HabitContentProvider.DESCRIPTOR_CONTENT_ITEM_TYPE, habitUri);
             startActivity(intent);
             return true;
         case MENU_DELETE:
         	Uri uri = Uri.parse(HabitContentProvider.DESCRIPTORS_URI + "/" + info.id);
         	getActivity().getContentResolver().delete(uri, null, null);
         	return true;
         }
         return super.onContextItemSelected(item);
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         DescriptorWeightDialog dialog = new DescriptorWeightDialog();
         Bundle bundle = new Bundle();
         bundle.putInt(DescriptorWeightDialog.DESCRIPTOR_ID, (int) id);
         dialog.setArguments(bundle);
         dialog.setTargetFragment(this, 0);
         dialog.show(getFragmentManager(), "Weight");
     }
 
     // These are the rows that we will retrieve.
     static final String[] DESCRIPTORS_PROJECTION = new String[] {
     	DescriptorTable.COLUMN_ID,
     	DescriptorTable.COLUMN_NAME,
     	DescriptorTable.COLUMN_COLOR
     };
 
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new CursorLoader(getActivity(), HabitContentProvider.DESCRIPTORS_URI, DESCRIPTORS_PROJECTION, null, null, null);
     }
 
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         // Swap the new cursor in.  (The framework will take care of closing the
         // old cursor once we return.)
         mAdapter.swapCursor(data);
 
         // The list should now be shown.
         if (isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         // This is called when the last Cursor provided to onLoadFinished()
         // above is about to be closed.  We need to make sure we are no
         // longer using it.
         mAdapter.swapCursor(null);
     }
 
 	@Override
 	public void onRecordWeight(int descriptorId, double weight) {
         ContentValues values = new ContentValues();
         values.put(ReadingTable.COLUMN_DESCRIPTOR_ID, descriptorId);
         values.put(ReadingTable.COLUMN_WEIGHT, weight);
         values.put(ReadingTable.COLUMN_TIME, Math.floor(System.currentTimeMillis() / 1000));
 
         getActivity().getContentResolver().insert(HabitContentProvider.READINGS_URI, values);
         
         MainActivity activity = (MainActivity) getActivity();
         EventListFragment eventsList = ((EventListFragment) getFragmentManager().getFragment(activity.getSharedBundle(), EventListFragment.FRAGMENT_KEY));
         if(eventsList != null) {
          	getLoaderManager().restartLoader(EventListFragment.LOADER_KEY, null, eventsList);
         }
         
         activity.setActiveTab(2);
 	}
 }
