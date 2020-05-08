 ﻿package de.tum.in.tumcampus;
 
 import static de.tum.in.tumcampus.services.DownloadService.broadcast;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.SlidingDrawer;
 import android.widget.Toast;
 import de.tum.in.tumcampus.models.Feed;
 import de.tum.in.tumcampus.models.FeedItemManager;
 import de.tum.in.tumcampus.models.FeedManager;
 import de.tum.in.tumcampus.services.DownloadService;
 
 /**
  * Activity to RSS-feeds and their news items
  */
 public class Feeds extends Activity implements OnItemClickListener, ViewBinder,
 		OnItemLongClickListener, View.OnClickListener {
 
 	/**
 	 * Current selected feed (ID)
 	 */
 	private String feedId;
 
 	/**
 	 * Current selected feed (Name)
 	 */
 	private String feedName;
 
 	/**
 	 * Adapter for feed list
 	 */
 	private SimpleCursorAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.feeds);
 
 		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
 		sd.open();
 
 		// get all feeds
 		FeedManager fm = new FeedManager(this, Const.db);
 		Cursor c = fm.getAllFromDb();
 
 		adapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
 				new int[] { android.R.id.text1 });
 
 		// add a footer to the list for adding new feeds
 		View view = getLayoutInflater().inflate(R.layout.feeds_footer, null,
 				false);
 
 		ListView lv = (ListView) findViewById(R.id.listView);
 		lv.addFooterView(view);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 		lv.setOnItemLongClickListener(this);
 		fm.close();
 
 		Button save = (Button) view.findViewById(R.id.save);
 		save.setOnClickListener(this);
 
 		registerReceiver(DownloadService.receiver, new IntentFilter(broadcast));
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(DownloadService.receiver);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// refresh current selected feed on resume (finished download)
 		if (feedId != null) {
 			ListView lv = (ListView) findViewById(R.id.listView);
 			onItemClick(lv, lv, -1, 0);
 		}
 
 		// reset new items counter
 		FeedManager.lastInserted = 0;
 		FeedItemManager.lastInserted = 0;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
 
 		// click on feed item in list, open URL in browser
 		if (av.getId() == R.id.listView2) {
 			Cursor c = (Cursor) av.getAdapter().getItem(position);
 			String link = c.getString(c.getColumnIndex("link"));
 
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
 			startActivity(intent);
 			return;
 		}
 
 		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
 		if (sd.isOpened()) {
 			sd.animateClose();
 		}
 
 		// click on feed in list
 		if (position != -1) {
 			Cursor c = (Cursor) av.getAdapter().getItem(position);
 			feedId = c.getString(c.getColumnIndex("_id"));
 			feedName = c.getString(c.getColumnIndex("name"));
 		}
 
 		setTitle("Nachrichten: " + feedName);
 
 		// get all feed items for a feed
 		FeedItemManager fim = new FeedItemManager(this, Const.db);
 		Cursor c2 = fim.getAllFromDb(feedId);
 
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
 				R.layout.feeds_listview, c2, c2.getColumnNames(), new int[] {
 						R.id.icon, R.id.title, R.id.description });
 
 		adapter.setViewBinder(this);
 		ListView lv2 = (ListView) findViewById(R.id.listView2);
 		lv2.setAdapter(adapter);
 		lv2.setOnItemClickListener(this);
 		fim.close();
 	}
 
 	@Override
 	public boolean setViewValue(View view, Cursor cursor, int index) {
 		// hide empty view elements (e.g. missing image or description)
 		if (cursor.getString(index).length() == 0) {
 			view.setVisibility(View.GONE);
 
 			// no binding needed
 			return true;
 		} else {
 			view.setVisibility(View.VISIBLE);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> av, View v,
 			final int position, long id) {
		if (id == -1) {
			return false;
		}
 
 		// confirm delete
 		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 
 				// delete feed from list, refresh feed list
 				Cursor c = (Cursor) av.getAdapter().getItem(position);
 				int _id = c.getInt(c.getColumnIndex("_id"));
 
 				FeedManager fm = new FeedManager(av.getContext(), Const.db);
 				fm.deleteFromDb(_id);
 				adapter.changeCursor(fm.getAllFromDb());
 				fm.close();
 			}
 		};
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Wirklch löschen?");
 		builder.setPositiveButton("Ja", listener);
 		builder.setNegativeButton("Nein", null);
 		builder.show();
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, Menu.FIRST, 0, "Aktualisieren");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// download latest feed items
 		Intent service = new Intent(this, DownloadService.class);
 		service.putExtra("action", "feeds");
 		startService(service);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// add a new feed
 		EditText editName = (EditText) findViewById(R.id.name);
 		EditText editUrl = (EditText) findViewById(R.id.url);
 
 		String name = editName.getText().toString();
 		String url = editUrl.getText().toString();
 
 		// prepend http:// if needed
 		if (url.length() > 0 && !url.contains(":")) {
 			url = "http://" + url;
 		}
 		FeedManager fm = new FeedManager(this, Const.db);
 		try {
 			Feed feed = new Feed(name, url);
 			fm.insertUpdateIntoDb(feed);
 		} catch (Exception e) {
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 
 		// refresh feed list
 		adapter.changeCursor(fm.getAllFromDb());
 		fm.close();
 
 		// clear form
 		editName.setText("");
 		editUrl.setText("");
 	}
 }
