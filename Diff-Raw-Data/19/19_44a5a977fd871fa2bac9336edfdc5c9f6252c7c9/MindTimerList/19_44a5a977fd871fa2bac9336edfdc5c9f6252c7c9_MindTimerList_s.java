 package com.futilities.mindtimer;
 
import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
import com.futilities.mindtimer.HourGlass.TimerState;

 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 
 public class MindTimerList extends ListActivity {
 	protected final static int ACTIVITY_CREATE = 1;
 	protected final static int ACTIVITY_EDIT = 0;
 	private static final String TAG = "MINDTIMERLIST";
 	private final int INSERT_ID = 1;
 	private TimersDbAdapter mDbAdapter;
 	private MindTimerCursorAdapter mCursorAdapter;
 	private Timer mTimer;
 	private ElapsationTask mElapsationTask;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.timers_list);
 
 		initDbAdapter();
 
 		Cursor cursor = mDbAdapter.fetchAll();
 
 		mCursorAdapter = new MindTimerCursorAdapter(this, cursor);
 
 		setListAdapter(mCursorAdapter);
 
 		initElapsationTask();
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		Log.i("mindtimer", "in onPause");
 
 		stopElapsationTask();
 
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		Log.i("MindTimer", "in onStop");
 
 		mDbAdapter.close();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		mTimer = null;
 		initElapsationTask();
 		startElapsationTask();
 		
 		initDbAdapter();
 
 	}
 
 	private void initDbAdapter() {
 		mDbAdapter = new TimersDbAdapter(this);
 		mDbAdapter.open();		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case INSERT_ID:
 			createTimer();
 			return true;
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	private void createTimer() {
 		Intent i = new Intent(this, TimerEdit.class);
 		startActivityForResult(i, ACTIVITY_CREATE);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == RESULT_OK) {
 			switch (requestCode) {
 			case ACTIVITY_EDIT:
 			case ACTIVITY_CREATE:
 				Log.i(TAG, "About to call requery");
 				
 				// For some reason onCreate is not called before this;
 				// so the db adapter isn't open
 				mDbAdapter.open();
 
 				requery();
 
 				break;
 			}
 		}
 	}
 
 	void requery() {
 		CursorAdapter adapter = (CursorAdapter) getListAdapter();
 		
 		Cursor cursor = mDbAdapter.fetchAll();
 		adapter.changeCursor(cursor);
 		
 		adapter.notifyDataSetChanged();
 	}
 
 	private void initElapsationTask() {
 		// foreground UI updates depend on this timer thread
 		mTimer = new Timer();
 		mElapsationTask = new ElapsationTask();
 
 	}
 
 	private void startElapsationTask() {
 		mTimer.schedule(mElapsationTask, 1000, 1000);
 
 	}
 
 	private void stopElapsationTask() {
 		mTimer.cancel();
 		mTimer.purge();
 
 	}
 
 	private class ElapsationTask extends TimerTask {
 		public void run() {
 			mUiUpdateHandler.sendMessage(mUiUpdateHandler.obtainMessage(0));
 
 		}
 	}
 
 	private Handler mUiUpdateHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
 			// Update all visible views.
 			ListView view = getListView();
 
 			int first = view.getFirstVisiblePosition();
 			int count = view.getChildCount();
 
 			for (int i = 0; i < count; i++) {
 				MindTimerListItemView itemView = (MindTimerListItemView) view
 						.getChildAt(first + i);
 
 				if(itemView != null){
 					itemView.updateProgress();
 				}
 					
 			}
 
 		}
 	};
 
 
 }
