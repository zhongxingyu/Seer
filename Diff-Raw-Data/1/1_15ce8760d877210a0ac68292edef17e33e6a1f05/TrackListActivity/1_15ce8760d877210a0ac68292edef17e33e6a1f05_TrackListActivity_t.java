 package com.aboveware.abovetracker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.aboveware.abovetracker.R;
 import com.aboveware.abovetracker.R.anim;
 import com.aboveware.abovetracker.R.id;
 import com.aboveware.abovetracker.R.layout;
 import com.aboveware.abovetracker.R.string;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
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
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class TrackListActivity extends ListActivity {
 
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
 			checkBox.setTag(position);
 			checkBox.setChecked(checkBoxStates[position]);
 			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				public void onCheckedChanged(CompoundButton buttonView,
 				    boolean isChecked) {
 					int position = (Integer) buttonView.getTag();
 					checkBoxStates[position] = isChecked;
 					isChecked = anyChecked();
 					slideButtonBar(isChecked);
 				}
 			});
 			return view;
 		}
 	}
 
 	private static final int DELETE_ITEM_ID = 1;
 
 	private static final int RENAME_ITEM_ID = 0;
 
 	private static final String SELECTION = "SELECTION";
 
 	public final static String TRACK_LIST_VIEW = "TRACK_LIST_VIEW";
 
 	private boolean[] checkBoxStates = null;
 
 	@SuppressWarnings("unused")
 	private boolean mExternalStorageAvailable = false;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 
 	private boolean mExternalStorageWriteable = false;
 
 	private Cursor TracksCursor;
 
 	private boolean anyChecked() {
 		boolean isChecked = false;
 		if (null != checkBoxStates) {
 			for (Boolean checked : checkBoxStates) {
 				isChecked = checked;
 				if (checked)
 					break;
 			}
 		}
 
 		return isChecked;
 	}
 
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
 			// Something else is wrong. It may be one of many other stareqtes, but
 			// all we need to know is we can neither read nor write
 			mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 	}
 
   public void clearButtonClick(View v) {
 		checkBoxStates = new boolean[getListAdapter().getCount()];
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
 		slideButtonBar(false);
 	}
 
 	public void deleteButtonClick(View view) {
 		slideButtonBar(false);
 		ProgressDialog progressDialog = ProgressDialog.show(this,
 		    getString(R.string.deleting_),
 		    getString(R.string.please_wait_until_all_records_have_been_deleted),
 		    true);
 
 		TrackDbAdapter dbHelper = new TrackDbAdapter(this);
 		dbHelper.open();
 		for (String date : getChecked()) {
 			dbHelper.delete(date);
 		}
 		dbHelper.close();
 		progressDialog.dismiss();
 		fillListView();
 	}
 
 	public void exportButtonClick(View view) {
 		slideButtonBar(false);
 		ProgressDialog progressDialog = ProgressDialog.show(this,
 		    getString(R.string.exporting_),
 		    getString(R.string.please_wait_until_all_records_have_been_exported),
 		    true);
 		TrackDbAdapter dbHelper = new TrackDbAdapter(this);
 		for (String date : getChecked()) {
 			dbHelper.export(this, date);
 		}
 		clearButtonClick(view);
 		progressDialog.dismiss();
 	}
 
 	private void fillListView() {
 		TrackDbAdapter dbHelper = new TrackDbAdapter(this);
 		dbHelper.open();
 
 		TracksCursor = dbHelper.selectTracks();
 		checkBoxStates = new boolean[TracksCursor.getCount()];
 		startManagingCursor(TracksCursor);
 		TrackSimpleCursorAdapter adapter = new TrackSimpleCursorAdapter(this,
 		    R.layout.track_list_item, TracksCursor, new String[] {
 		        TrackDbAdapter.TRACK, TrackDbAdapter.DATE }, new int[] {
 		        android.R.id.text1, android.R.id.text2 });
 
 		setListAdapter(adapter);
     dbHelper.close();
 
 		final ListView listView = getListView();
 		listView.setItemsCanFocus(false);
 		registerForContextMenu(listView);
 	}
 
 	private List<String> getChecked() {
 		List<String> checked = new ArrayList<String>();
 		for (int i = 0; i < checkBoxStates.length; ++i) {
 			if (!checkBoxStates[i])
 				continue;
 			Cursor cursor = (Cursor) getListAdapter().getItem(i);
 			if (null == cursor)
 				continue;
 			checked.add(cursor.getString(cursor.getColumnIndex(TrackDbAdapter.DATE)));
 		}
 		return checked;
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		Cursor cursor = (Cursor) getListView().getItemAtPosition(item.getGroupId());
 		if (null != cursor) {
 			TrackDbAdapter dbHelper = new TrackDbAdapter(this);
       dbHelper.open();
 			switch (item.getItemId()) {
 			case DELETE_ITEM_ID:
 	      final String date = cursor.getString(cursor
 	          .getColumnIndex(TrackDbAdapter.DATE));
 				dbHelper.delete(date);
 				break;
 			case RENAME_ITEM_ID:
 	      final String track = cursor.getString(cursor
 	          .getColumnIndex(TrackDbAdapter.TRACK));
 	      final String id = cursor.getString(cursor
 	          .getColumnIndex(TrackDbAdapter.ID));
 				dbHelper.rename(track, id, new TrackDbAdapterListener() {
           public void Updated(Context context) {
             fillListView();
           }
         });
 				break;
 			}
 			dbHelper.close();
 			fillListView();
 		}
 		return true;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.track_list);
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
   protected void onListItemClick(ListView l, View v, int position, long id) {
     super.onListItemClick(l, v, position, id);
     Cursor cursor = (Cursor)getListView().getItemAtPosition(position);
     if (null != cursor){
       List<String> checked = new ArrayList<String>();
       checked.add(cursor.getString(cursor.getColumnIndex(TrackDbAdapter.DATE)));
       view(checked);
     }
   }
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		View buttonBar = findViewById(R.id.buttonBar);
 		slideButtonBar(buttonBar.getVisibility() == View.GONE);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		checkBoxStates = state.getBooleanArray(SELECTION);
 		super.onRestoreInstanceState(state);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		slideButtonBar(false);
 		fillListView();
 	}
 
 	/*
 	 * checkBox.setTag(position);
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putBooleanArray(SELECTION, checkBoxStates);
 		super.onSaveInstanceState(outState);
 	}
 
 	private void slideButtonBar(boolean show) {
 
 		View buttonBar = findViewById(R.id.buttonBar);
 
 		Boolean isAnyChecked = anyChecked();
 		if (show){
 		View button = buttonBar.findViewById(R.id.exportButton);
 		if (null != button)
 			button.setEnabled(isAnyChecked);
 		button = buttonBar.findViewById(R.id.viewButton);
 		if (null != button)
 			button.setEnabled(isAnyChecked);
 		button = buttonBar.findViewById(R.id.clearButton);
 		if (null != button)
 			button.setEnabled(isAnyChecked);
 		button = buttonBar.findViewById(R.id.deleteButton);
 		if (null != button)
 			button.setEnabled(isAnyChecked);
 		}
 		if (show && View.GONE == buttonBar.getVisibility()) {
 			View button = buttonBar.findViewById(R.id.exportButton);
 			if (null != button) {
 				checkForSdCard();
 				button.setVisibility(mExternalStorageWriteable ? Button.VISIBLE
 				    : Button.GONE);
 			}
 			Animation animation = AnimationUtils.loadAnimation(
 			    buttonBar.getContext(), R.anim.slide_up);
 			buttonBar.startAnimation(animation);
 		}
 		if (!show && View.VISIBLE == buttonBar.getVisibility()) {
 			Animation animation = AnimationUtils.loadAnimation(
 			    buttonBar.getContext(), R.anim.slide_down);
 			buttonBar.startAnimation(animation);
 		}
 		buttonBar.setVisibility(show ? View.VISIBLE : View.GONE);
 	}
 
 	public void viewButtonClick(View view) {
 		List<String> dates = new ArrayList<String>();
 		for (String date : getChecked()) {
 			dates.add(date);
 		}
 		view(dates);
 	}
 
   private void view(List<String> dates) {
     if (!dates.isEmpty()) {
 			Dashboard.ShowLoadingDialog(this);
 			Intent intent = new Intent(this, MapViewActivity.class);
 			intent.putExtra(TRACK_LIST_VIEW, (ArrayList<String>) dates);
	    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			startActivity(intent);
 		}
   }
 }
