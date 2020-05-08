 package com.dev.campus.event;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.R;
 import com.dev.campus.SettingsActivity;
 import com.dev.campus.event.Feed.FeedType;
 import com.dev.campus.util.FilterDialog;
 
 public class EventsActivity extends ListActivity implements OnItemClickListener {
 
 	public static final String EXTRA_CATEGORY = "com.dev.campus.CATEGORY";
 	public static final String EXTRA_EVENTS = "com.dev.campus.EVENTS";
 	public static final String EXTRA_EVENTS_INDEX = "com.dev.campus.EVENTS_POSITION";
 	
 	private final int PICK_CATEGORY = 10;
 	private final int updateFrequency = 60000; // Update frequency (ms)
 	
 	private ArrayList<Event> mEvents;
 	private ArrayList<Event> mSortedEvents;
 	private Category mCategory;
 
 	private ActionBar mActionBar;
 	private Resources mResources;
 	private Handler mHandler;
 	
 	private FilterDialog mFilterDialog;
 	private EventAdapter mEventAdapter;
 	private EventParser mEventParser;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mActionBar = getActionBar();
 		mActionBar.setDisplayHomeAsUpEnabled(true);
 		mResources = getResources();
 
 		mHandler = new Handler();
 		mFilterDialog = new FilterDialog(this);
 		mCategory = Category.MAIN_EVENTS;
 
 		try {
 			mEventParser = new EventParser(this);
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 		}
 
 		ListView listView = getListView();
 		View header = (View)getLayoutInflater().inflate(R.layout.event_list_header, listView, false);
 		listView.addHeaderView(header, null, true);
 		listView.setOnItemClickListener(this);
 
 		mEventAdapter = new EventAdapter(this, new ArrayList<Event>());
 		listView.setAdapter(mEventAdapter);
 		update();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		//startUpdateTimer();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		//pauseUpdateTimer();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.with_actionbar_refresh, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(EventsActivity.this, SettingsActivity.class));
 			return true;
 		case R.id.menu_filters:
 			mFilterDialog.showDialog();
 			return true;
 		case R.id.menu_refresh:
 			update();
 			return true;
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/*private Runnable mUpdateRunnable = new Runnable() {
 		@Override 
 		public void run() {
 			if (CampusUB1App.persistence.isWifiConnected())
 				update();
 			mHandler.postDelayed(mUpdateRunnable, updateFrequency);
 		}
 	};
 
 	private void startUpdateTimer() {
 		mUpdateRunnable.run();
 	}
 
 	private void pauseUpdateTimer() {
 		mHandler.removeCallbacks(mUpdateRunnable);
 	}*/
 
 	public void reloadEvents() {
 		ArrayList<Event> sortedEvents = new ArrayList<Event>();
 		TextView headerView = (TextView) findViewById(R.id.event_list_header);
 		headerView.setText(mCategory.toString());
 		mEventAdapter.clear();
 
 		if (mEvents != null) {
 			for (Event event : mEvents) {
 				if ((event.getSource().equals(FeedType.UB1_FEED) && CampusUB1App.persistence.isFilteredUB1())
 				 || (event.getSource().equals(FeedType.LABRI_FEED) && CampusUB1App.persistence.isFilteredLabri()))
 					sortedEvents.add(event);
 			}
 		}
 		Collections.sort(sortedEvents, new Event.EventComparator());
 		mSortedEvents = sortedEvents;
 		mEventAdapter.addAll(mSortedEvents);
 		mEventAdapter.notifyDataSetChanged();
 	}
 	
 	public String getHistoryPath() {
		int sub1 = CampusUB1App.persistence.isSubscribedUB1()? 1 : 0;
		int sub2 = CampusUB1App.persistence.isSubscribedLabri()? 1 : 0;
		return getFilesDir() + "/history_" + mCategory.toString().replace(" ", "") + sub1 + sub2 + ".dat";
 	}
 	
 	@SuppressWarnings("unchecked")
 	private SimpleEntry<ArrayList<Event>, ArrayList<Date>> readEventsHistory() {
 		File file = new File(getHistoryPath());
 		SimpleEntry<ArrayList<Event>, ArrayList<Date>> feedsEntry = null;
 
 		if (file.exists()) {
 			try {
 				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
 				feedsEntry = (SimpleEntry<ArrayList<Event>, ArrayList<Date>>) ois.readObject();
 				ois.close();
 			} catch (Exception e) { 
 				e.printStackTrace();
 			}
 		}
 		return feedsEntry;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void update() {
 		SimpleEntry<ArrayList<Event>, ArrayList<Date>> feedsEntry = readEventsHistory();
 		
 		if (feedsEntry != null) {
 			if (CampusUB1App.persistence.isOnline()) {
 				new UpdateFeedsTask().execute(feedsEntry);
 			} else {
 				mEvents = feedsEntry.getKey();
 				reloadEvents();
 				Toast.makeText(this, mResources.getString(R.string.showing_history), Toast.LENGTH_SHORT).show();
 			}
 		}
 		else {
 			if (CampusUB1App.persistence.isOnline()) {
 				new UpdateFeedsTask().execute();
 				Toast.makeText(this, mResources.getString(R.string.update_complete), Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(this, mResources.getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		if (position == 0) {
 			//Categories index
 			startActivityForResult(new Intent(EventsActivity.this, CategoryActivity.class), PICK_CATEGORY);
 		} else {
 			Intent intent = new Intent(EventsActivity.this, EventViewActivity.class);
 			intent.putExtra(EXTRA_EVENTS, mSortedEvents);
 			intent.putExtra(EXTRA_EVENTS_INDEX, position-1);	
 			//substract 1 so as to have a correct 
 			//reference relative to the list of events 
 			//(i.e. without the category as first item)
 			startActivity(intent);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == PICK_CATEGORY) {
 			if (resultCode == RESULT_OK) {
 				Category category = (Category) data.getSerializableExtra(EXTRA_CATEGORY);
 				if (category != null) {
 					mCategory = category;
 					update();
 				}
 			}
 		}
 	}
 
 	private class UpdateFeedsTask extends AsyncTask<SimpleEntry<ArrayList<Event>, ArrayList<Date>>, Void, Void> {
 
 		private ProgressDialog progressDialog = new ProgressDialog(EventsActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			progressDialog.setTitle(mResources.getString(R.string.events_loading));
 			progressDialog.setMessage(mResources.getString(R.string.please_wait));
 			progressDialog.setIndeterminate(true);
 			progressDialog.setCancelable(false);
 			progressDialog.show();
 		}
 
 		@Override
 		protected Void doInBackground(SimpleEntry<ArrayList<Event>, ArrayList<Date>>... entries) {
 			try {
 				//check if latest version, and update only if needed
 				//otherwise, just load history
 				if (entries.length > 0) {
 					if (mEventParser.isLatestVersion(mCategory, entries[0].getValue())) {
 						mEvents = entries[0].getKey();
 						return null;
 					}
 				}
 				mEventParser.parseEvents(mCategory);
 				mEventParser.saveEvents();
 				mEvents = mEventParser.getEvents();
 			} catch (Exception e) {
 				CampusUB1App.LogD(e.toString());
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			reloadEvents();
 			progressDialog.dismiss();
 		}
 
 		@Override
 		protected void onCancelled() {
 			progressDialog.dismiss();
 			super.onCancelled();
 		}
 	}
 	
 }
