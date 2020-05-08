 /*
  * Copyright (C) 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.questmaster.tudmensa;
 
 import java.util.Calendar;
 
 import de.questmaster.tudmensa.R;
 
 import android.app.ExpandableListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.SimpleCursorTreeAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MensaMeals extends ExpandableListActivity {
 
 	public static final int UPDATE_ID = Menu.FIRST;
 	public static final int SETTINGS_ID = Menu.FIRST + 1;
 	public static final int CLEAR_DB_ID = Menu.FIRST + 2;
 
 	public static final int ON_SETTINGS_CHANGE = 0;
 
 	private MensaMealsSettings.Settings mSettings = new MensaMealsSettings.Settings();
 	protected MealsDbAdapter mDbHelper;
 
 	private ProgressDialog mPDialog = null;
 	private boolean mRestart = false;
 	protected Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			mPDialog.dismiss();
 			// updateView after update. Don't do it for an update after startup
 			mRestart = true;
 			fillData();
 		}
 	};
 
 	private Calendar mToday = Calendar.getInstance();
 	protected Context mContext = this;
 
 	public class CustomCursorTreeAdapter extends SimpleCursorTreeAdapter {
 
 		public CustomCursorTreeAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom,
 				int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
 			super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
 		}
 
 		@Override
 		protected Cursor getChildrenCursor(Cursor groupCursor) {
 			String location = groupCursor.getString(groupCursor.getColumnIndex(MealsDbAdapter.KEY_LOCATION));
 			String date = groupCursor.getString(groupCursor.getColumnIndex(MealsDbAdapter.KEY_DATE));
 			String counter = groupCursor.getString(groupCursor.getColumnIndex(MealsDbAdapter.KEY_COUNTER));
 
 			Cursor c = new MealCursorWrapper(mDbHelper.fetchMealsOfGroupDay(location, date, counter), mContext);
 
 			startManagingCursor(c);
 			return c;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// Read settings
 		mSettings.ReadSettings(this);
 
 		// Setup Theme
 		if (mSettings.m_sThemes.equals("dark")) {
 			setTheme(R.style.myTheme);
 		} else if (mSettings.m_sThemes.equals("light")) {
 			setTheme(R.style.myThemeLight);
 		}
 		
 		// Set Content
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.meals_list);
 		
 		// Init Database
 		mDbHelper = new MealsDbAdapter(this);
 		mDbHelper.open();
 
 		// Setup date
 		mToday = Calendar.getInstance();
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		// Capture our buttons from layout and set them up
 		Button buttonPrev = (Button) findViewById(R.id.btn_prev);
 		Button buttonNext = (Button) findViewById(R.id.btn_next);
 		buttonPrev.setBackgroundResource(R.drawable.ic_menu_back);
 		buttonNext.setBackgroundResource(R.drawable.ic_menu_forward);
 		updateButtonText();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		MenuItem mItem = null;
 
 		mItem = menu.add(0, UPDATE_ID, 0, R.string.menu_update);
 		mItem.setIcon(R.drawable.ic_menu_refresh);
 
 		mItem = menu.add(0, SETTINGS_ID, 1, R.string.menu_settings);
 		mItem.setIcon(android.R.drawable.ic_menu_preferences);
 
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case UPDATE_ID:
 			getData();
 			fillData();
 			break;
 
 		case SETTINGS_ID:
 			Intent iSettings = new Intent();
 			iSettings.setClass(this, MensaMealsSettings.class);
 			startActivityForResult(iSettings, ON_SETTINGS_CHANGE);
 
 			// To be able to check for new data if mensa changed. 
 			mRestart = false;
 
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ON_SETTINGS_CHANGE:
 			mSettings.ReadSettings(this);
 
			// WORKAROUND: restart activity
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 			
 			// Reread data and display it
 			updateButtonText();
 			fillData();
 
 			break;
 		}
 	}
 
 	@Override
 	public void onGroupCollapse(int groupPosition) {
 		// keep the Groups expanded
 		getExpandableListView().expandGroup(groupPosition);
 	}
 
 	public void onClickNextButton(View v) {
 		// Setup date
 		mToday.add(Calendar.DAY_OF_YEAR, 1);
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 1);
 		}
 		updateButtonText();
 
 		fillData();
 	}
 
 	public void onClickPrevButton(View v) {
 		// Setup date
 		mToday.add(Calendar.DAY_OF_YEAR, -1);
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, -1);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, -2);
 		}
 		updateButtonText();
 
 		fillData();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		// close database
 		mDbHelper.close();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// expand groups
 		fillData();
 	}
 
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 		// show Type and legend information
 		Cursor c = mDbHelper.fetchMeal(id);
 		startManagingCursor(c);
 
 		// get info date
 		String info = c.getString(c.getColumnIndex(MealsDbAdapter.KEY_INFO));
 
 		if (!info.equals(""))
 			// Display Toast message
 			Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
 
 		return false;
 	}
 
 //	 @Override
 //	 // TODO Wechsele Tag mit links/rechts wisch.
 //	 public boolean onTouchEvent(MotionEvent evt) {
 //	 // switch (evt.getAction()) {
 //	 // case MotionEvent.ACTION_MOVE:
 //	 switch (evt.getEdgeFlags()) {
 //	 case MotionEvent.EDGE_LEFT:
 //	 System.err.printf("Left wisch.");
 //	
 //	 return true;
 //	 case MotionEvent.EDGE_RIGHT:
 //	 System.err.printf("Right wisch.");
 //	
 //	 return true;
 //	 }
 //	 // break;
 //	 // }
 //	
 //	 return false;
 //	 }
 
 	private void updateButtonText() {
 		// Prepare times
 		Calendar cPrev = (Calendar) mToday.clone();
 		Calendar cNext = (Calendar) mToday.clone();
 		cPrev.add(Calendar.DAY_OF_YEAR, -1);
 		cNext.add(Calendar.DAY_OF_YEAR, 1);
 
 		// check weekends
 		if (cPrev.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			cPrev.add(Calendar.DAY_OF_YEAR, -1);
 		} else if (cPrev.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			cPrev.add(Calendar.DAY_OF_YEAR, -2);
 		}
 		if (cNext.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			cNext.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (cNext.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			cNext.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		// Update Text Prev
 		Button buttonPrev = (Button) findViewById(R.id.btn_prev);
 		String textPrev = DateFormat.getDateFormat(this).format(cPrev.getTime());
 		buttonPrev.setText(textPrev.substring(0, textPrev.length() - 5));
 
 		// Update Text Next
 		Button buttonNext = (Button) findViewById(R.id.btn_next);
 		String textNext = DateFormat.getDateFormat(this).format(cNext.getTime());
 		buttonNext.setText(textNext.substring(0, textNext.length() - 5));
 
 		// Set new title
 		int pos = 0;
 		for (String s : getResources().getStringArray(R.array.MensaLocationsValues)) {
 			if (s.equals(mSettings.m_sMensaLocation)) {
 				break;
 			} else
 				pos++;
 		}
 
 		// Update label
 		TextView labelDay = (TextView) findViewById(R.id.txt_date);
 		labelDay.setText(getResources().getStringArray(R.array.MensaLocations)[pos] + "\n"
 				+ DateFormat.format("EEEE", mToday.getTime()) + ", "
 				+ DateFormat.getDateFormat(this).format(mToday.getTime()));
 	}
 
 	private void fillData() {
 		// prepare date string
 		String date = (String) DateFormat.format("yyyyMMdd", mToday);
 
 		// Get all of the notes from the database and create the item list
 		Cursor c = mDbHelper.fetchGroupsOfDay(mSettings.m_sMensaLocation, date);
 		// if none found start a new query automatically
 		if (mSettings.m_bAutoUpdate && c.getCount() == 0 && !mRestart) {
 			mRestart = true;
 			getData();
 			return;
 		}
 		startManagingCursor(c);
 
 		String[] group_from = new String[] { MealsDbAdapter.KEY_COUNTER };
 		int[] group_to = new int[] { R.id.counter };
 		String[] child_from = new String[] { MealsDbAdapter.KEY_NAME, MealsDbAdapter.KEY_PRICE, MealsDbAdapter.KEY_TYPE };
 		int[] child_to = new int[] { R.id.meal, R.id.price, R.id.meal_type };
 
 		// Now create an array adapter and set it to display using our row
 		CustomCursorTreeAdapter meals = new CustomCursorTreeAdapter(this, c, R.layout.simple_expandable_list_item_1,
 				group_from, group_to, R.layout.simple_expandable_list_item_2, child_from, child_to);
 		setListAdapter(meals);
 
 		// expand all items
 		for (int i = 0; i < c.getCount(); i++) {
 			getExpandableListView().expandGroup(i);
 		}
 	}
 
 	private void getData() {
 		mPDialog = ProgressDialog.show(this, null, getResources().getString(R.string.dialog_updating_text), true, true);
 
 		// get data
 		DataExtractor de = new DataExtractor(this, mSettings.m_sMensaLocation);
 		Thread t = new Thread(de);
 		t.start();
 	}
 }
