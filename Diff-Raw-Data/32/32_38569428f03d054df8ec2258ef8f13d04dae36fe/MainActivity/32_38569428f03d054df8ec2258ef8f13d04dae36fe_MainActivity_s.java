 package com.example.rater;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.Entry;
 import net.Connection;
 import net.ConnectionFactory;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewParent;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class MainActivity extends Activity {
 	private static final int BLURB_LENGTH = 40;
 	private static final int GET_NUM_ENTRIES = 100;
 	private static final String TOP_LINE = "line1";
 	private static final String BOTTOM_LINE = "line2";
 
 	public static final String AVERAGE_RATING = "Average Rating";
 	public static final String CONTENT_ID = "Content Id";
 	public static final String RATE_TITLE = "Rate Title";
 	public static final String RATE_TEXT = "Rate Text";
 
 	public static final Connection connect;
 	private static final boolean CONNECTION = true;
 	private static final Entry[] entries;
 
 	List<Map<String, String>> toDisplay;
 	List<Entry> indexToEntry = new ArrayList<Entry>();
 	private BaseAdapter adapter;
 	private int lastId = -1;
 	private ListView list;
 
 	static {
 		if (CONNECTION) {
 			connect = ConnectionFactory.makeConnection();
			entries = connect.getContent(1, GET_NUM_ENTRIES);
 		} else {
 			Entry[] TEST_ENTRIES = new Entry[15];
 			connect = null;
 			int[] ratings = new int[10];
 			Arrays.fill(ratings, 5);
 			for (int i = 0; i < 15; ++i) {
 				Entry entry = new Entry(i, "Dummy", "Title", "Content", 15.0,
 						ratings);
 				TEST_ENTRIES[i] = entry;
 			}
 			entries = TEST_ENTRIES;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		toDisplay = entriesToDisplay(TOP_LINE, BOTTOM_LINE, entries);
 		lastId = lastId(entries);
 		adapter = new SimpleAdapter(this, toDisplay, R.layout.feed_item,
 				new String[] { "line1", "line2" }, new int[] {
 						R.id.content_title, R.id.blurb });
 		list = (ListView) findViewById(R.id.list);
 		list.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public boolean onOptionItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_add_content:
 			Intent i = new Intent(this, AddActivity.class);
 			startActivity(i);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Finds entry associated to button that was clicked. Adds this entry's
 	 * information to an intent and spawns a rate_activity.
 	 * 
 	 * @param view
 	 */
 	public void rateEntry(View view) {
		// TODO Add the id of the content
 		Entry entry = getEntry(view);
 		Intent intent = new Intent(this, Rate.class);
 		int contentId = entry.getId();
 		double averageRating = entry.getAverage();
 		String title = entry.getTitle();
 		String text = entry.getContent();
 		intent.putExtra(CONTENT_ID, contentId);
 		intent.putExtra(AVERAGE_RATING, averageRating);
 		intent.putExtra(RATE_TITLE, title);
 		intent.putExtra(RATE_TEXT, text);
 		startActivity(intent);
 	}
 
 	/**
 	 * From the button, we want to get to the entry that it's associated to. We
 	 * find the button's parent, the item, and find that item's index in the
 	 * list view. We take this index and find its corresponding entry
 	 * 
 	 * @param view
 	 * @return
 	 */
 	private Entry getEntry(View view) {
 		ViewParent parent = view.getParent();
 		int childIndex = list.indexOfChild((View) parent);
 		return indexToEntry.get(childIndex);
 	}
 
 	public List<Map<String, String>> entriesToDisplay(String name1,
 			String name2, Entry[] entries) {
 		List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
 		for (int i = 0; i < entries.length; i++) {
 			Map<String, String> map = new HashMap<String, String>();
 			String title = entries[i].getTitle();
 			String blurb = entries[i].blurb(BLURB_LENGTH);
 			map.put(name1, title);
 			map.put(name2, blurb);
 			listMap.add(map);
 		}
 		refreshIndexEntryMap(entries);
 		return listMap;
 	}
 
 	/**
 	 * Called when user hits the add button. Leads to new page
 	 */
 	public void addContent(View view) {
 		Intent intent = new Intent(this, AddActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Displays more content in the feed
 	 * 
 	 * @param view
 	 */
 	public void displayMoreContent(View view) {
		Entry[] entries = connect.getContent(lastId - 1, GET_NUM_ENTRIES);
		lastId = lastId(entries);
		displayEntries(entries);
 	}
 
 	/**
 	 * Displays entries to screen
 	 * 
 	 * @param entries
 	 */
 	private void displayEntries(Entry[] entries) {
 		toDisplay.addAll(entriesToDisplay(TOP_LINE, BOTTOM_LINE, entries));
 		adapter.notifyDataSetChanged();
 		refreshIndexEntryMap(entries);
 	}
 
 	/**
 	 * Adds the elements and their future corresponding ID to a map. This
 	 * establishes the connection between list element index and entry
 	 * 
 	 * @param entries
 	 */
 	private void refreshIndexEntryMap(Entry[] entries) {
 		for (Entry entry : entries)
 			indexToEntry.add(entry);
 	}
 
 	private static int lastId(Entry[] entries) {
		return entries[entries.length - 1].getId();
 	}
 
 }
