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
 
 package org.crazythink.TimeTrackerv1;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class Entries extends ListActivity {
 
     private Long mTaskId;
     private DbAdapter mDbHelper;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mDbHelper = new DbAdapter(this);
         mDbHelper.open();
        setContentView(R.layout.main_entries);
 
         mTaskId = (savedInstanceState == null) ? null :
         	(Long) savedInstanceState.getSerializable(DbAdapter.KEY_TASK_ID);
         if (mTaskId == null) {
         	Bundle extras = getIntent().getExtras();
             mTaskId = extras != null ? extras.getLong(DbAdapter.KEY_TASK_ID)
             						: null;
         }
         if(mTaskId != null) {
         	Cursor task = mDbHelper.fetchTask(mTaskId);
             setTitle(task.getString(task.getColumnIndex(DbAdapter.KEY_NAME))
             		+ " " + getString(R.string.entries));
             task.close();
         }
         else
         	setTitle(R.string.all_task_entries);
         
         
         fillData();
         
     }
     
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		mDbHelper.close();
 	}
     
     private void fillData() {
     	Cursor c;
     	Log.d("DEBUG","entries fillData on task " + mTaskId);
     	
     	if (mTaskId != null) {
     		 c = mDbHelper.fetchEntriesByTask(mTaskId);
     	}
     	else {
     		c = mDbHelper.fetchAllEntries();
     	}
     	startManagingCursor(c);
     	
     	String[] from = new String[]{DbAdapter.KEY_START_TIME_HHMM, DbAdapter.KEY_DESCRIPTION, DbAdapter.KEY_MINUTES_FORMATTED, DbAdapter.KEY_START_DATE_MMDD};
         int[] to = new int[]{R.id.timeTextView, R.id.descriptionTextView, R.id.minutesTextView, R.id.dateHeaderTextView};
         SimpleCursorAdapter adapter = 
         	new SimpleCursorAdapter(this, R.layout.entry, c, from, to);
         
         final int nStartDateIndex = c.getColumnIndex(DbAdapter.KEY_START_DATE_MMDD);
         
         adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder()
         {
         	public boolean setViewValue(View view, Cursor cursor, int columnIndex)
         	{
         		if (columnIndex == nStartDateIndex) {
         			// prepare for displaying project header if needed
         			Log.v("DEBUG:", "header column: cursor is at position " + cursor.getPosition() + " of " + cursor.getCount());
         			if (isHeaderVisible(cursor)) {
         				view.setVisibility(View.VISIBLE);
         				String headerName = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_START_DATE_MMDD));
         				((TextView) view).setText(headerName);
         			} else {
         				((TextView) view).setText("");
         				view.setVisibility(View.GONE);
         			}
         			return true;
 				}
         		return false;
         	}
         });
         
         
         setListAdapter(adapter);
     }
     
     protected boolean isHeaderVisible(Cursor cursor) {
     	int startDateColumn = cursor.getColumnIndex(DbAdapter.KEY_START_DATE_MMDD);
     	if (cursor.moveToPrevious()) {
     		String prevHeaderId = cursor.getString(startDateColumn);
     		cursor.moveToNext();
     		String currHeaderId = cursor.getString(startDateColumn);
     		if (currHeaderId.equals(prevHeaderId))
     			return false;
     	}
     	else
     		cursor.moveToFirst();
     	return true;
 	}
     
 }
