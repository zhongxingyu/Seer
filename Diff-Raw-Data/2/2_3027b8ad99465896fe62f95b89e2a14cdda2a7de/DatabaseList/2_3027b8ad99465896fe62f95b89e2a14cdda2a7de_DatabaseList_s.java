 package com.example.tomatroid;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.example.tomatroid.sql.SQHelper;
 import com.example.tomatroid.util.NavigationBarManager;
 import com.example.tomatroid.util.Util;
 
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class DatabaseList extends ListActivity {
 
	final int ACTIVITYNUMBER = 2;
 
 	SQHelper sqHelper = new SQHelper(this);
 	LayoutInflater mInflater;
 	DatabaseCursorAdapter databaseCursorAdapter;
 	SparseArray<Boolean> needSeperator = new SparseArray<Boolean>();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_database_list);
 		mInflater = LayoutInflater.from(this);
 
 		NavigationBarManager navi = new NavigationBarManager(this,
 				ACTIVITYNUMBER);
 
 		Cursor cursor = sqHelper.getDatesCursor();
 		
 		if (cursor.moveToFirst()) {
 			int column_day = cursor.getColumnIndex(SQHelper.KEY_DATE_DAY);
 			int column_month = cursor.getColumnIndex(SQHelper.KEY_DATE_MONTH);
 			int column_year = cursor.getColumnIndex(SQHelper.KEY_DATE_YEAR);
 			int oldDayNumber = 0;
 			int oldMonthNumber = 0;
 			int oldYearNumber = 0;
 						
 			do{
 				if( (oldDayNumber != cursor.getInt(column_day)) || 
 						(oldMonthNumber != cursor.getInt(column_month)) || 
 						(oldYearNumber != cursor.getInt(column_year)) ){
 					
 					oldDayNumber = cursor.getInt(column_day);
 					oldMonthNumber = cursor.getInt(column_month);
 					oldYearNumber = cursor.getInt(column_year);
 					needSeperator.append(cursor.getPosition(), true);
 				}
 			} while(cursor.moveToNext());
 		}
 		
 		databaseCursorAdapter = new DatabaseCursorAdapter(this, cursor, 0);
 		setListAdapter(databaseCursorAdapter);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getActionBar().setSelectedNavigationItem(ACTIVITYNUMBER);
 	}
 
 	// @Override
 	// public boolean onCreateOptionsMenu(Menu menu) {
 	// getMenuInflater().inflate(R.menu.activity_database_list, menu);
 	// return true;
 	// }
 
 	class DatabaseCursorAdapter extends CursorAdapter {
 
 		Cursor c;
 		int startTime;
 		int day;
 		int month;
 		int year;
 		int startHour;
 		int startMinute;
 		int endHour;
 		int endMinute;
 		int theme;
 		int type;
 		int duration;
 		DateTimeFormatter fmt = DateTimeFormat.forPattern("EE dd.MM.yy");
 		
 		public DatabaseCursorAdapter(Context context, Cursor c, int flags) {
 			super(context, c, flags);
 			this.c = c;
 			startHour = c.getColumnIndex(SQHelper.KEY_DATE_START_HOUR);
 			startMinute = c.getColumnIndex(SQHelper.KEY_DATE_START_MINUTE);
 			endHour = c.getColumnIndex(SQHelper.KEY_DATE_END_HOUR);
 			endMinute = c.getColumnIndex(SQHelper.KEY_DATE_END_MINUTE);
 
 			startTime = c.getColumnIndex(SQHelper.KEY_STARTTIMEMILLIES); 
 			
 			day = c.getColumnIndex(SQHelper.KEY_DATE_DAY);
 			month = c.getColumnIndex(SQHelper.KEY_DATE_MONTH);
 			year = c.getColumnIndex(SQHelper.KEY_DATE_YEAR);
 
 			theme = c.getColumnIndex(SQHelper.KEY_THEME);
 			type = c.getColumnIndex(SQHelper.KEY_TYPE);
 			duration = c.getColumnIndex(SQHelper.KEY_DURATION);
 		}
 
 		@Override
 		public void bindView(View view, Context context, final Cursor c) {
 			final int rowId = c.getInt(c.getColumnIndex(SQHelper.KEY_ROWID));
 			final int typeId = c.getInt(c.getColumnIndex(SQHelper.KEY_TYPE));
 			final int themeId = c.getInt(c.getColumnIndex(SQHelper.KEY_THEME));
 			
 			ImageView image = (ImageView) view.findViewById(R.id.typeIcon);
 			switch (c.getInt(type)) {
 			case SQHelper.TYPE_POMODORO:
 				// image.setImageResource(android.R.drawable.ic_dialog_info);
 				image.setBackgroundColor(MainActivity.COLOR_POMODORO);
 				break;
 			case SQHelper.TYPE_LONGBREAK:
 				// image.setImageResource(android.R.drawable.ic_media_ff);
 				image.setBackgroundColor(MainActivity.COLOR_BREAK);
 				break;
 			case SQHelper.TYPE_SHORTBREAK:
 				// image.setImageResource(android.R.drawable.ic_delete);
 				image.setBackgroundColor(MainActivity.COLOR_BREAK);
 				break;
 			case SQHelper.TYPE_SLEEPING:
 				// image.setImageResource(android.R.drawable.ic_lock_idle_low_battery);
 				image.setBackgroundColor(MainActivity.COLOR_SLEEP);
 				break;
 			case SQHelper.TYPE_TRACKING:
 				// image.setImageResource(android.R.drawable.ic_input_add);
 				image.setBackgroundColor(MainActivity.COLOR_TRACKING);
 				break;
 			}
 
 			TextView tv1 = (TextView) view.findViewById(R.id.time);
 			TextView tv3 = (TextView) view.findViewById(R.id.duration);
 			TextView tv2 = (TextView) view.findViewById(R.id.theme);
 				
 			LinearLayout title = (LinearLayout) view.findViewById(R.id.title);
 			if(needSeperator.get(c.getPosition(), false)){
 				DateTime start = new DateTime(Long.parseLong(c.getString(startTime)));
 				
 				TextView date = (TextView) title.findViewById(R.id.date);
 				date.setText(start.toString(fmt));
 				title.setVisibility(View.VISIBLE);
 			} else {
 				title.setVisibility(View.GONE);
 			}
 			
 			tv1.setText(c.getInt(startHour) + ":" + c.getInt(startMinute) + " - " + c.getInt(endHour) + ":" + c.getInt(endMinute));
 
 			tv3.setText(Util.generateTimeText(c.getInt(duration)));
 			tv2.setText(sqHelper.getTheme(c.getInt(theme)));
 
 			view.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					showChangeEntryDialog(rowId, typeId, themeId);
 				}
 			});
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			return mInflater.inflate(R.layout.database_list_row, parent, false);
 		}
 		
 	}
 
 	private void showDeleteDialog(final int rowId) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Do you want to delete this entry?");
 		builder.setCancelable(true);
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				sqHelper.deleteEntry(rowId);
 				databaseCursorAdapter.getCursor().requery();
 				databaseCursorAdapter.notifyDataSetChanged();
 			}
 		});
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private void showChangeEntryDialog(final int rowId, final int type,
 			final int themeId) {
 		View dialogView = mInflater.inflate(R.layout.dialog_change_entry, null);
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setView(dialogView);
 		alertDialogBuilder.setTitle(getString(R.string.change));
 
 		// Type
 		ArrayList<String> typeList = new ArrayList<String>();
 		typeList.add(getString(R.string.pomodoro));
 		typeList.add(getString(R.string.shortbreak));
 		typeList.add(getString(R.string.longbreak));
 		typeList.add(getString(R.string.tracking));
 		typeList.add(getString(R.string.sleep));
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				R.layout.choose_theme_row, typeList);
 		final Spinner typeSpinner = (Spinner) dialogView
 				.findViewById(R.id.typespinner);
 		typeSpinner.setAdapter(adapter);
 		typeSpinner.setSelection(type);
 
 		// Theme
 		SimpleCursorAdapter themeListAdapter = new SimpleCursorAdapter(this,
 				R.layout.choose_theme_row, sqHelper.getThemeCursor(),
 				new String[] { SQHelper.KEY_NAME }, new int[] { R.id.name }, 0);
 		final Spinner themeSpinner = (Spinner) dialogView
 				.findViewById(R.id.themespinner);
 		themeSpinner.setAdapter(themeListAdapter);
 		themeSpinner.setSelection(getIndex(themeSpinner, themeId));
 
 		// set dialog message
 		alertDialogBuilder
 				.setPositiveButton(getString(R.string.change),
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// Select Type
 								int selectedType = typeSpinner
 										.getSelectedItemPosition();
 
 								// Select Theme
 								Cursor cc = (Cursor) themeSpinner
 										.getSelectedItem();
 								int selectedThemeId = cc.getInt(cc
 										.getColumnIndex(SQHelper.KEY_ROWID));
 
 								sqHelper.changeEntry(rowId, selectedType,
 										selectedThemeId);
 								databaseCursorAdapter.getCursor().requery();
 								databaseCursorAdapter.notifyDataSetChanged();
 							}
 						})
 				.setNegativeButton(getString(R.string.cancel),
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 							}
 						})
 				.setNeutralButton(getString(R.string.delete),
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								showDeleteDialog(rowId);
 							}
 						});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 
 	private int getIndex(Spinner spinner, int parentId) {
 		if (parentId == -1) {
 			return 0;
 		}
 		Cursor cc = sqHelper.getThemeCursor();
 		int column = cc.getColumnIndex(SQHelper.KEY_ROWID);
 		if (cc.moveToFirst()) {
 			while (cc.moveToNext()) {
 				if (cc.getInt(column) == parentId)
 					return cc.getPosition();
 			}
 		}
 		return 0;
 	}
 }
