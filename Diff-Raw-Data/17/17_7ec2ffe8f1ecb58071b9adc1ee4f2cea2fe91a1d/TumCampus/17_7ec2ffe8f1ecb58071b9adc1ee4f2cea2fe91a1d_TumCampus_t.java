 ﻿package de.tum.in.newtumcampus;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.tum.in.newtumcampus.common.Utils;
 import de.tum.in.newtumcampus.models.CafeteriaManager;
 import de.tum.in.newtumcampus.models.CafeteriaMenuManager;
 import de.tum.in.newtumcampus.models.EventManager;
 import de.tum.in.newtumcampus.models.FeedItemManager;
 import de.tum.in.newtumcampus.models.FeedManager;
 import de.tum.in.newtumcampus.models.GalleryManager;
 import de.tum.in.newtumcampus.models.LectureItemManager;
 import de.tum.in.newtumcampus.models.LinkManager;
 import de.tum.in.newtumcampus.models.NewsManager;
 import de.tum.in.newtumcampus.models.SyncManager;
 import de.tum.in.newtumcampus.services.DownloadService;
 import de.tum.in.newtumcampus.services.ImportService;
 import de.tum.in.newtumcampus.services.SilenceService;
 
 /**
  * Main activity to show main menu, logo and refresh button
  */
 public class TumCampus extends Activity implements OnItemClickListener, View.OnClickListener {
 
 	static boolean syncing = false;	
 
 	/**
 	 * Returns network connection type if available or can be available soon
 	 * 
 	 * @return empty String if not available or connection type if available
 	 */
 	public String getConnection() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			String connection = "";
 			if (netInfo.getSubtypeName().length() > 0) {
 				connection += netInfo.getSubtypeName();
 			} else {
 				connection += netInfo.getTypeName();
 			}
 			if (netInfo.isRoaming()) {
 				connection += " roaming";
 			}
 			return connection;
 		}
 		return "";
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
 
 		// adjust logo width to screen width
 		ImageView iv = (ImageView) findViewById(R.id.logo);
 		iv.getLayoutParams().width = getWindowManager().getDefaultDisplay().getWidth();
 
 		// bind download buttons
 		Button b = (Button) findViewById(R.id.refresh);
 		b.setOnClickListener(this);
 
 		b = (Button) findViewById(R.id.initial);
 		b.setOnClickListener(this);
 
 		// show initial download button if feed items are empty
 		FeedItemManager fim = new FeedItemManager(this);
 		if (fim.empty()) {
 			b.setVisibility(View.VISIBLE);
 		} else {
 			b.setVisibility(View.GONE);
 		}
 
 		// register receiver for download and import
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction(ImportService.broadcast);
 		intentFilter.addAction(DownloadService.broadcast);
 		registerReceiver(receiver, intentFilter);
 
 		// import default values into database
 		Intent service = new Intent(this, ImportService.class);
 		service.putExtra(Const.ACTION_EXTRA, Const.DEFAULTS);
 		startService(service);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(receiver);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// build main menu
 		SimpleAdapter adapter = new SimpleAdapter(this, buildMenu(), R.layout.main_listview, new String[] { "icon",
 				"name", "icon2" }, new int[] { R.id.icon, R.id.name, R.id.icon2 });
 		ListView lv = (ListView) findViewById(R.id.menu);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 		
 		try {
 			Utils.ensureImagesAreNotIndexed();
 		} catch (Exception e) {
 			Log.e("CampusApp", "Exception", e);
 			e.printStackTrace();
 		}
 
 		String conn = getConnection();
 		Button b = (Button) findViewById(R.id.refresh);
 		// hello world text
 		//TextView tv = (TextView) findViewById(R.id.status);
 
 		/**
 		 * <pre>
 		 * disable download button if offline
 		 * show cancel button if currently syncing
 		 * else show download button
 		 * </pre>
 		 */
 		// TODO CLEANUP plz Review Vasyl
 		// 4 recognition from v1
 		if (conn.length() > 0) {
 			//b.setVisibility(android.view.View.VISIBLE);
 			if (!syncing) {
 				b.setText(getString(R.string.update) + " (" + conn + ")");
 				b.setEnabled(true);
 				/* TODO delete this content
 				// hide text if offline message is still there
 				//if (tv.getTag() != null) {
 				//	tv.setVisibility(View.GONE);
 				//	tv.setTag(null);
 				} */
 			} else {
 				b.setText(getString(R.string.cancel));
 
 				// hide initial download button when syncing
 				b = (Button) findViewById(R.id.initial);
 				b.setVisibility(View.GONE);
 			}
 		} else {
 			/* TODO delete this content
 			b.setVisibility(android.view.View.GONE);
 
 			// show hello world line when offline
 			tv.setVisibility(View.VISIBLE);
 			tv.setText("Offline.");
 			tv.setTag("offline");
 
 			// hide initial download button if no connection
 			b = (Button) findViewById(R.id.initial);
 			b.setVisibility(View.GONE);
 			*/
 			b.setText("offline.");
 			b.setEnabled(false);
 		}
 
 		// initialize import buttons
 		setImportButtons(true);
 
 		// start silence service
 		Intent service = new Intent(this, SilenceService.class);
 		startService(service);
 	}
 
 	/**
 	 * Return main menu item list
 	 * 
 	 * @return item list of Map[] (icon, name, icon2, intent)
 	 */
 	public List<Map<String, Object>> buildMenu() {
 		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
 		/*
 		 * build list, intent = start activity on click TODO Review Vasyl
 		 * Florian Schulz: Changed ifs with external Strings + "kurz notiert"
 		 * also view settings.xml
 		 */
 		if (Utils.getSettingBool(this, getString(R.string.lectures))) {
 			addItem(list, R.drawable.vorlesung, getString(R.string.lectures), LectureItemManager.lastInserted > 0,
 					new Intent(this, Lectures.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.person_search))) {
 			addItem(list, R.drawable.personnel, getString(R.string.person_search), false, new Intent(this, Staff.class));
 		}
 		/*if (Utils.getSettingBool(this, getString(R.string.organisations))) {
 			addItem(list, R.drawable.organisationen, getString(R.string.organisations), false, new Intent(this,
 					Organisation.class));
 		}*/
 		if (Utils.getSettingBool(this, getString(R.string.grades))) {
 			addItem(list, R.drawable.grade, getString(R.string.grades), false, new Intent(this, Grades.class));
 		}
 //		Removed due to security concerns from TUM official.
 //		if (Utils.getSettingBool(this, getString(R.string.documents))) {
 //			addItem(list, R.drawable.documents, getString(R.string.documents), false, new Intent(this, Documents.class));
 //		}
 		if (Utils.getSettingBool(this, getString(R.string.study_plans))) {
 			addItem(list, R.drawable.curricula, getString(R.string.study_plans), false, new Intent(this,
 					Curricula.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.tuition_fees))) {
 			addItem(list, R.drawable.euro, getString(R.string.tuition_fees), false, new Intent(this, TuitionFees.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.menues))) {
 			addItem(list, R.drawable.essen, getString(R.string.menues), CafeteriaMenuManager.lastInserted > 0,
 					new Intent(this, Cafeterias.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.mvv))) {
 			addItem(list, R.drawable.zug, getString(R.string.mvv), false, new Intent(this, Transports.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.rss_feeds))) {
 			addItem(list, R.drawable.rss, getString(R.string.rss_feeds), FeedItemManager.lastInserted
 					+ FeedManager.lastInserted > 0, new Intent(this, Feeds.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.events))) {
 			addItem(list, R.drawable.party, getString(R.string.events), EventManager.lastInserted > 0, new Intent(this,
 					Events.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.gallery))) {
 			addItem(list, R.drawable.gallery, getString(R.string.gallery), GalleryManager.lastInserted > 0, new Intent(this, Gallery.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.area_maps))) {
 			addItem(list, R.drawable.kompass, getString(R.string.area_maps), false, new Intent(this, Plans.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.roomfinder))) {
 			addItem(list, R.drawable.roomfinder, getString(R.string.roomfinder), false,
 					createRoomfinderAppIntent());
 		}
 		if (Utils.getSettingBool(this, getString(R.string.opening_hours))) {
 			addItem(list, R.drawable.hours, getString(R.string.opening_hours), false, new Intent(this, Hours.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.news))) {
 			addItem(list, R.drawable.globus, getString(R.string.news), NewsManager.lastInserted > 0, new Intent(this,
 					News.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.links))) {
 			addItem(list, R.drawable.www, getString(R.string.links), LinkManager.lastInserted > 0, new Intent(this,
 					Links.class));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.facebook))) {
 			addItem(list, R.drawable.fb, getString(R.string.facebook), false,
 					new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link))));
 		}
 		if (Utils.getSettingBool(this, getString(R.string.app_info))){
 			addItem(list, R.drawable.info, getString(R.string.app_info), false, new Intent(this, AppInfo.class));
 		}
 		if (Utils.getSettingBool(this, Const.Settings.debug)) {
 			addItem(list, R.drawable.icon, getString(R.string.debug), false, new Intent(this, Debug.class));
 		}
 		return list;
 	}
 
 	/**
 	 * Add menu item to list
 	 * 
 	 * <pre>
 	 * @param list List to append new item to
 	 * @param icon Icon ID
 	 * @param name Menu item name
 	 * @param changed Menu item was changed recently
 	 * @param intent Activity to start on click
 	 * </pre>
 	 */
 	public void addItem(List<Map<String, Object>> list, int icon, String name, boolean changed, Intent intent) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("icon", icon);
 		map.put("name", name);
 		int icon2 = android.R.color.transparent;
 		if (changed) {
 			icon2 = android.R.drawable.star_off;
 		}
 		map.put("icon2", icon2);
 		map.put("intent", intent);
 		list.add(map);
 	}
 
 	/**
 	 * @Author Vasyl Malinskyi
 	 * @review Florian schulz - after checking works - added externalisation
 	 */
 
 	private Intent createRoomfinderAppIntent() {
 		Intent resultIntent = null;
 //		try {
 			final Intent intent = new Intent();
 			intent.setAction("android.intent.action.SEARCH");
 			intent.addCategory("de.tum.event");
 			resultIntent = intent;
 		return resultIntent;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> av, View view, int position, long id) {
 
 		// start activity on main menu item click
 		@SuppressWarnings("unchecked")
 		Map<String, Object> map = (Map<String, Object>) av.getAdapter().getItem(position);
 
 		Intent intent = (Intent) map.get("intent");
 		try{
 		startActivity(intent);
 		
 		}catch (ActivityNotFoundException e) {
 			if(intent.hasCategory("de.tum.event")&intent.getAction().equals("android.intent.action.SEARCH")){
 			Toast.makeText(this, this.getString(R.string.roomfinder_install), Toast.LENGTH_LONG).show();
 
 			final Intent marketIntent = new Intent();
 			marketIntent.setAction("android.intent.action.VIEW");
 			marketIntent.setData(Uri.parse("market://details?id=de.tum.roomfinder"));
 			startActivity(marketIntent); //Consider catching this, too
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 // TODO Flo/Vasyl: consider to add the APPinfo here!
 		
 		/* example for this
 		 * MenuItem m = menu.add(0, Menu.FIRST, 0, "App-Info");
 		 * m.setIcon(android.R.drawable.ic_menu_info_details);
 		 */
 		MenuItem m = menu.add(0, Menu.FIRST, 0, getString(R.string.settings));
 		m.setIcon(android.R.drawable.ic_menu_preferences);
 
 		m = menu.add(0, Menu.FIRST + 1, 0, getString(R.string.manual));
 		m.setIcon(android.R.drawable.ic_menu_agenda);
 
 		m = menu.add(0, Menu.FIRST + 2, 0, getString(R.string.empty_cache));
 		m.setIcon(android.R.drawable.ic_menu_delete);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		// open settings activity, clear cache (database tables, sd-card)
 		switch (item.getItemId()) {
 		case Menu.FIRST:
 			Intent intent = new Intent(this, Settings.class);
 			startActivity(intent);
 			return true;
 
 		case Menu.FIRST + 1:
 			try {
 				// copy pdf manual from assets to sd-card
 				String target = Utils.getCacheDir("cache") + getString(R.string.manual);
 				InputStream in = getAssets().open(this.getString(R.string.manualpdf));
 				OutputStream out = new FileOutputStream(target);
 
 				byte[] buffer = new byte[8192];
 				int read;
 				while ((read = in.read(buffer)) != -1) {
 					out.write(buffer, 0, read);
 				}
 				in.close();
 				out.close();
 
 				// open pdf manual
 				Uri uri = Uri.fromFile(new File(target));
 				Intent intent2 = new Intent(Intent.ACTION_VIEW);
 				intent2.setDataAndType(uri, "application/pdf");
 				intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent2);
 			} catch (Exception e) {
 				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 			}
 			return true;
 
 		case Menu.FIRST + 2:
 			clearCache();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Clears the cache (database tables, sd-card)
 	 */
 	public void clearCache() {
 		try {
 			Utils.getCacheDir("");
 		} catch (Exception e) {
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		CafeteriaManager cm = new CafeteriaManager(this);
 		cm.removeCache();
 
 		CafeteriaMenuManager cmm = new CafeteriaMenuManager(this);
 		cmm.removeCache();
 
 		FeedItemManager fim = new FeedItemManager(this);
 		fim.removeCache();
 
 		EventManager em = new EventManager(this);
 		em.removeCache();
 
 		GalleryManager gm = new GalleryManager(this);
 		gm.removeCache();
 
 		LinkManager lm = new LinkManager(this);
 		lm.removeCache();
 
 		NewsManager nm = new NewsManager(this);
 		nm.removeCache();
 
 		// table of all download events
 		SyncManager sm = new SyncManager(this);
 		sm.deleteFromDb();
 
 		// show initial download button
 		Button b = (Button) findViewById(R.id.initial);
 		b.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public void onClick(View v) {
 		// Click on download/cancel button, start/stop download service
 		if (v.getId() == R.id.refresh || v.getId() == R.id.initial) {
 			Intent service = new Intent(this, DownloadService.class);
 			if (syncing) {
 				stopService(service);
 				syncing = false;
 			} else {
 				startService(service);
 				syncing = true;
 			}
 			onResume();
 		}
 
 		// Click on import lectures, start import service
 		if (v.getId() == R.id.importLectures) {
 			Intent service = new Intent(this, ImportService.class);
 			service.putExtra(Const.ACTION_EXTRA, Const.LECTURES);
 			startService(service);
 			setImportButtons(false);
 		}
 
 		// added by Daniel G. Mayr
 		// Click on import lectures from TUMOnline, start import service
 		if (v.getId() == R.id.importLecturesTUMOnline) {
			if(!Utils.isAccessTokenValid(Utils.getSetting(getBaseContext(), Const.ACCESS_TOKEN))){
				Intent tumonlinesettings = new Intent(this, TUMOnlineSettings.class);
				startActivity(tumonlinesettings);
				return;
			}
 			Intent service = new Intent(this, ImportService.class);
 			service.putExtra(Const.ACTION_EXTRA, Const.LECTURES_TUM_ONLINE);
 			startService(service);
 			setImportButtons(false);
 		}
 
 		// Click on import links, start import service
 		if (v.getId() == R.id.importLinks) {
 			Intent service = new Intent(this, ImportService.class);
 			service.putExtra(Const.ACTION_EXTRA, Const.LINKS);
 			startService(service);
 			setImportButtons(false);
 		}
 
 		// Click on import links, start import service
 		if (v.getId() == R.id.importFeeds) {
 			Intent service = new Intent(this, ImportService.class);
 			service.putExtra(Const.ACTION_EXTRA, Const.FEEDS);
 			startService(service);
 			setImportButtons(false);
 		}
 
 		// Daniel G. Mayr added TUMOnline Settings Button
 		if (v.getId() == R.id.btnTUMOnlineSettings) {
 			Intent tumonlinesettings = new Intent(this, TUMOnlineSettings.class);
 			startActivity(tumonlinesettings);
 		}
 	}
 
 	/**
 	 * Initialize import buttons
 	 * 
 	 * <pre>
 	 * @param enabled True to enable buttons, False to disable buttons
 	 * </pre>
 	 */
 	public void setImportButtons(boolean enabled) {
 		Button b = (Button) findViewById(R.id.importLectures);
 		b.setOnClickListener(this);
 		b.setEnabled(enabled);
 
 		b = (Button) findViewById(R.id.importFeeds);
 		b.setOnClickListener(this);
 		b.setEnabled(enabled);
 
 		b = (Button) findViewById(R.id.importLinks);
 		b.setOnClickListener(this);
 		b.setEnabled(enabled);
 
 		// Daniel G. Mayr added btnTUMOnlineSettings button
 		b = (Button) findViewById(R.id.btnTUMOnlineSettings);
 		b.setOnClickListener(this);
 		b.setEnabled(enabled);
 
 		// Daniel G. Mayr added for TUMonline Lecture import button
 		b = (Button) findViewById(R.id.importLecturesTUMOnline);
 		b.setOnClickListener(this);
 		b.setEnabled(enabled);
 	}
 
 	/**
 	 * Receiver for Download and Import services
 	 */
 	private final BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			// show message from download service, refresh main menu
 			if (intent.getAction().equals(DownloadService.broadcast)) {
 				String message = intent.getStringExtra(Const.MESSAGE_EXTRA);
 				String action = intent.getStringExtra(Const.ACTION_EXTRA);
 
 				if (action.equals(Const.COMPLETED)) {
 					syncing = false;
 				}
 				if (message.length() > 0) {
 					TextView tv = (TextView) findViewById(R.id.status);
 					tv.setVisibility(View.VISIBLE);
 					tv.setText(message);
 					// make (long) Toast to every Error Message
 					if (action.compareTo(Const.ERROR) == 0) {
 						Toast.makeText(TumCampus.this, message, Toast.LENGTH_LONG).show();
 					}
 				}
 				onResume();
 			}
 
 			// show message from import service, refresh main menu
 			if (intent.getAction().equals(ImportService.broadcast)) {
 				String message = intent.getStringExtra(Const.MESSAGE_EXTRA);
 				String action = intent.getStringExtra(Const.ACTION_EXTRA);
 
 				if (action.length() != 0) {
 					Toast.makeText(context, message, Toast.LENGTH_LONG).show();
 					setImportButtons(true);
 
 					SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slider);
 					sd.animateClose();
 
 					onResume();
 				}
 			}
 		}
 	};
 }
