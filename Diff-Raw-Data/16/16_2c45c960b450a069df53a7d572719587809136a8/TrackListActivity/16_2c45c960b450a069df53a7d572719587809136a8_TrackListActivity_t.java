 package com.aboveware.abovetracker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.*;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class TrackListActivity extends ListActivity {
 
 	private class Checked {
 		private String date;
 		private int id;
 
 		public Checked(String date, int id) {
 			this.setDate(date);
 			this.setId(id);
 		}
 
 		public String getDate() {
 			return date;
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public void setDate(String date) {
 			this.date = date;
 		}
 
 		public void setId(int id) {
 			this.id = id;
 		}
 	}
 
 	public class TrackSimpleCursorAdapter extends SimpleCursorAdapter {
 
 		public TrackSimpleCursorAdapter(Context context, int layout, Cursor c,
 		    String[] from, int[] to) {
 			super(context, layout, c, from, to);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.widget.CursorAdapter#getView(int, android.view.View,
 		 * android.view.ViewGroup)
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = super.getView(position, convertView, parent);
 			CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
 			if (null == checkBox)
 				return view;
 			if (null != restoreCheckBoxState) {
 				for (long i : restoreCheckBoxState) {
 					if ((int) i != position)
 						continue;
 					checkBox.setChecked(true);
 					slideButtonBar(true);
 					break;
 				}
 			}
 			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				public void onCheckedChanged(CompoundButton buttonView,
 				    boolean isChecked) {
 					slideButtonBar(!getChecked().isEmpty());
 				}
 			});
 			return view;
 		}
 	}
 
 	private static final String SELECTION = "SELECTION";
 
 	public final static String TRACK_LIST_VIEW = "TRACK_LIST_VIEW";
 
 	private static final int RENAME_ITEM_ID = 0;
 
 	private static final int DELETE_ITEM_ID = 1;
 
 	private TrackDbAdapter mDbHelper;
 
 	private boolean mExternalStorageAvailable = false;
 
 	private boolean mExternalStorageWriteable = false;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 
 	private long[] restoreCheckBoxState = null;
 
 	private void checkForSdCard() {
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			mExternalStorageAvailable = mExternalStorageWriteable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			// We can only read the media
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		} else {
 			// Something else is wrong. It may be one of many other states, but
 			// all we need to know is we can neither read nor write
 			mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 	}
 
 	public void clearButtonCLick(View v) {
 		final ListView listView = getListView();
 		for (int i = 0; i < getListAdapter().getCount(); ++i) {
 			final View view = listView.getChildAt(i);
 			if (null == view)
 				continue;
 			CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
 			if (null != checkBox && checkBox.isChecked()) {
 				checkBox.setChecked(false);
 			}
 		}
 	}
 
 	public void deleteButtonCLick(View view) {
 		ProgressDialog progressDialog = ProgressDialog.show(this,
 		    getString(R.string.deleting_),
 		    getString(R.string.please_wait_until_all_records_have_been_deleted),
 		    true);
 
 		TrackDbAdapter dbHelper = new TrackDbAdapter(this);
 		dbHelper.open(onTrackDbUpdate());
 		for (Checked date : getChecked()) {
 			dbHelper.delete(date.getDate());
 		}
 		dbHelper.close();
 		progressDialog.dismiss();
 	}
 
 	public void exportButtonCLick(View view) {
 		ProgressDialog progressDialog = ProgressDialog.show(this,
 		    getString(R.string.exporting_),
 		    getString(R.string.please_wait_until_all_records_have_been_exported),
 		    true);
 		for (Checked date : getChecked()) {
 			mDbHelper.export(this, date.getDate());
 		}
 		progressDialog.dismiss();
 	}
 
 	private List<Checked> getChecked() {
 		List<Checked> checked = new ArrayList<Checked>();
 		final ListView listView = getListView();
 		for (int i = 0; i < getListAdapter().getCount(); ++i) {
 			final View view = listView.getChildAt(i);
 			if (null == view)
 				continue;
 			CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
 			if (null != checkBox && checkBox.isChecked()) {
 				Cursor cursor = (Cursor) listView.getItemAtPosition(i);
 				if (null != cursor) {
 					checked.add(new Checked(cursor.getString(cursor
 					    .getColumnIndex(TrackDbAdapter.DATE)), i));
 				}
 			}
 		}
 		return checked;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.track_list);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		restoreCheckBoxState = state.getLongArray(SELECTION);
 		super.onRestoreInstanceState(state);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		List<Checked> itemsChecked = getChecked();
 		int size = itemsChecked.size();
 		long[] items = new long[size];
 		for (int i = 0; i < size; ++i) {
 			items[i] = itemsChecked.get(i).getId();
 		}
 		outState.putLongArray(SELECTION, items);
 		super.onSaveInstanceState(outState);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		mDbHelper = new TrackDbAdapter(this);
 		mDbHelper.open();
 
 		// Get all of the rows from the database and create the item list
 		final Cursor mTracksCursor = mDbHelper.selectTracks();
 		startManagingCursor(mTracksCursor);
 		ListAdapter adapter = new TrackSimpleCursorAdapter(this,
 		    R.layout.track_list_item, mTracksCursor, new String[] {
 		        TrackDbAdapter.TRACK, TrackDbAdapter.DATE }, new int[] {
 		        android.R.id.text1, android.R.id.text2 });
 
 		setListAdapter(adapter);
 
 		final ListView listView = getListView();
 		listView.setItemsCanFocus(false);
 		registerForContextMenu(listView);
 		mDbHelper.close();
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	    ContextMenuInfo menuInfo) {
 		if (v.getId() == android.R.id.list) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 			Cursor cursor = (Cursor) getListView().getItemAtPosition(info.position);
 			if (null != cursor) {
 				menu.setHeaderTitle(cursor.getString(cursor
 				    .getColumnIndex(TrackDbAdapter.TRACK)));
 			}
 			menu.add(info.position, RENAME_ITEM_ID, 0, R.string.rename);
 			menu.add(info.position, DELETE_ITEM_ID, 1, R.string.delete);
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		Cursor cursor = (Cursor)getListView().getItemAtPosition(item.getGroupId());
 		if (null != cursor) {
 			final String date = cursor.getString(cursor.getColumnIndex(TrackDbAdapter.DATE));
 			final String track = cursor.getString(cursor.getColumnIndex(TrackDbAdapter.TRACK));
 			final String id = cursor.getString(cursor.getColumnIndex(TrackDbAdapter.ID));
 			TrackDbAdapter dbHelper = new TrackDbAdapter(this);
 			dbHelper.open(onTrackDbUpdate());
 			switch(item.getItemId()){
 			case DELETE_ITEM_ID:
 				dbHelper.delete(date);
 				break;
 			case RENAME_ITEM_ID:
				dbHelper.rename(track, id, onTrackDbUpdate());
 				break;
 			}
 			dbHelper.close();
 		}
 	  return true;
 	}
 
 	private TrackDbAdapterListener onTrackDbUpdate() {
 	  return new TrackDbAdapterListener() {
 	  	@Override
 	  	public void Updated(Context context) {
 	  		finish();
 	  		// TODO - make sure the active item still is visible... sort of
 	  		startActivity(new Intent(context, TrackListActivity.class));
 	  	}
 	  };
   }
 
 	private void slideButtonBar(boolean anyChecked) {
 		View buttonBar = findViewById(R.id.buttonBar);
 		if (anyChecked && View.GONE == buttonBar.getVisibility()) {
 			Button button = (Button) buttonBar.findViewById(R.id.exportButton);
 			if (null != button) {
 				checkForSdCard();
 				button.setVisibility(mExternalStorageWriteable ? Button.VISIBLE
 				    : Button.GONE);
 			}
 			Animation animation = AnimationUtils.loadAnimation(
 			    buttonBar.getContext(), R.anim.slide_up);
 			buttonBar.startAnimation(animation);
 		}
 		if (!anyChecked && View.VISIBLE == buttonBar.getVisibility()) {
 			Animation animation = AnimationUtils.loadAnimation(
 			    buttonBar.getContext(), R.anim.slide_down);
 			buttonBar.startAnimation(animation);
 		}
 		buttonBar.setVisibility(anyChecked ? View.VISIBLE : View.GONE);
 	}
 
 	public void viewButtonCLick(View view) {
 		List<CharSequence> dates = new ArrayList<CharSequence>();
 		for (Checked date : getChecked()) {
 			dates.add(date.getDate());
 		}
 		if (!dates.isEmpty()) {
 			Intent intent = new Intent(this, Map.class);
 			intent.putExtra(TRACK_LIST_VIEW, dates.toArray());
 			startActivity(intent);
 		}
 	}
 }
