 package org.nosreme.app.urlhelper;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import org.nosreme.app.urlhelper.UrlStore;
 
 public class URLHelperActivity extends ListActivity {
 	private final String[] colFields = { "url", "time" };
 
 	private final int REQ_CHOOSE_INTENT = 0;
 
 	private Cursor cursor;
 
 	private String intentOptionsStr = null;
 
 	/* Launch a URL using the configured browser. */
 	private void launchUrl(String urlString)
 	{
     	PackageManager pm = getPackageManager();
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         boolean expand = prefs.getBoolean("expandOnLaunch", false);
         
         if (expand)
         {
         	String expanded = expandUrl(urlString, true);
         	if (expanded != null)
         	{
                 urlString = expanded;
         	}
         }
     	Uri uri = Uri.parse(urlString);
     	Intent intent = new Intent();
     	intent.setComponent(null);
     	
     	intent.setData(uri);
     	intent.setAction(android.content.Intent.ACTION_VIEW);
     	
     	/* Ask the system for possible activities which will handle this URL.
     	 * This is likely to include us, so we want to filter it out.
     	 */
     	List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
     	String pkg = null;
     	String name = null;
     	Log.i("URLHelper", "Searching for handler for " + urlString);
     	
     	int activityCount = activities.size();
     	Parcelable[] activitylist = new Parcelable[activityCount];
     	// Build a list of the options as strings
     	String[] intentStrList = new String[activityCount];
     	int activitiesFound = 0;
     	for (ResolveInfo ri: activities)
     	{
     		ActivityInfo ai = ri.activityInfo;
     		Log.i("URLHelper", "Activity: " + ai.packageName + "/" + ai.applicationInfo.className 
     				+ "/" + ai.name);
     		if (!ai.name.startsWith("org.nosreme.app.urlhelper"))
     		{
     			/* It's an activity which isn't this one, so add it to the list. */
     			if (pkg == null)
     			{
     				/* If it's the first interesting one, save the details. */
     			    pkg = ai.packageName;
     			    name = ai.name;
     			}
     			Intent actIntent = new Intent();
     			actIntent.setClassName(ai.packageName, ai.name);
     			activitylist[activitiesFound] = actIntent;
     			intentStrList[activitiesFound] = ai.packageName + "/" + ai.name;
     			activitiesFound += 1;
     		}
     	}
     	
     	boolean chosen = false;
 		String combinedActivityList = null;
 		
     	if (activitiesFound > 1)
     	{
     		/* Build a single canonical string of the possible handlers,
     		 * which we can store and look up in the database.
     		 */
     		Arrays.sort(intentStrList, 0, activitiesFound);
     		
     		StringBuilder builder = new StringBuilder();
     		
     		for (int i=0; i<activitiesFound; ++i)
     		{
     			builder.append("@@");
     			builder.append(intentStrList[i]);
     		}
     		combinedActivityList = new String(builder);
     		
     		UrlStore urlstore = new UrlStore(getApplicationContext());
 
     	    Cursor cursor = urlstore.findHandlerSet(combinedActivityList);
     	    
     	    Log.v("handler", "combined str=" + combinedActivityList);
     	    
     	    cursor.moveToFirst();
     	    if (!cursor.isAfterLast())
     	    {
     	    	/* TODO: Use constants for column names!*/
     	    	pkg = cursor.getString(2);
     	    	name = cursor.getString(3);
     	    	Log.v("handler","Found: " + pkg + "/" + name);
     	    	chosen = true;
     	    }
     	    else
     	    {
     	    	Log.v("handler", "Nothing found");
     	    }
     	}
     	
     	/* If there's no single choice, then ask the user. */
     	if (!chosen)
     	{
     		Intent chooserIntent = new Intent();
     		chooserIntent.setAction(Intent.ACTION_PICK_ACTIVITY);
     		if (false) {
     			/* TODO:
     			 * This is a way to add activities to the chooser; but to
     			 * filter out there's not much I can do without re-implementing
     			 * the chooser.  Some useful references when I get around to doing
     			 * that:
     			 * 
     			 * http://pilcrowpipe.blogspot.co.uk/2012/01/creating-custom-android-intent-chooser.html
     			 * http://stackoverflow.com/questions/5734678/custom-filtering-of-intent-chooser-based-on-installed-android-package-name
     			 */
     		    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, activitylist);
     		}
 
     		chooserIntent.putExtra(Intent.EXTRA_INTENT, intent);
     		
     		/* This will create the system chooser, and return the result in onActivityResult
     		 * below (when we'll actually launch it).
     		 */
     		intentOptionsStr = combinedActivityList;
     		Log.v("handler", "starting chooser.  saved str:" + intentOptionsStr);
     		startActivityForResult(chooserIntent, REQ_CHOOSE_INTENT);    		
     	} else {
     		/* Only one, so use it directly. */
     	    intent.setClassName(pkg, name);
         	startActivity(intent);
     	}
 	}
 
 	private static final int MAX_URL_EXPAND = 5;
 
 	/*
 	 * Attempt to expand a shortened URL. Returns null if there is no change.
 	 * 
 	 * If iterate==true, then iterates until there is no further redirection. To
 	 * avoid loops, at most MAX_URL_EXPAND iterations.
 	 */
 	private static String expandUrl(String urlString, boolean iterate) {
 		URL url;
 		String result = null;
 		int iterations = iterate ? MAX_URL_EXPAND : 1;
 		do {
 			try {
 				url = new URL((result != null) ? result : urlString);
 				HttpURLConnection conn = (HttpURLConnection) url
 						.openConnection();
 				conn.setRequestMethod("HEAD");
 				conn.setInstanceFollowRedirects(false);
 				// See http://code.google.com/p/android/issues/detail?id=16227
 				// and http://code.google.com/p/android/issues/detail?id=24672
 				// for why the Accept-Encoding is required to disable gzip
 				conn.setRequestProperty("Accept-Encoding", "identity");
 				int resp = conn.getResponseCode();
 				if (resp == 301 || resp == 302) {
 					Map<String, List<String>> headers = conn.getHeaderFields();
 					List<String> hlist = headers.get("location");
 					if (hlist.size() != 1) {
 						return result;
 					}
 					result = hlist.get(0);
 				} else {
 					return result;
 				}
 			} catch (Exception e) {
 				return result;
 			}
 		} while (--iterations > 0);
 
 		return result;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/* Check whether we're in offline mode. */
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 
 		boolean offlineSetting = prefs.getBoolean("offline", true);
 
 		UrlStore urlstore = new UrlStore(getApplicationContext());
 
 		Intent intent = getIntent();
		
		/* If we're restarting, then reinitialise the UI and so on (and refresh it!), but don't
		 * launch anything or write anything to the database, as it's been done already.
		 */
		boolean restarting = savedInstanceState != null;
 		/*
 		 * If we've been launched with ACTION_VIEW (a URL) and we're in online
 		 * (passthrough) mode, then we won't have a UI.
 		 */
 		boolean haveUrl = intent.getAction().equals(
 				android.content.Intent.ACTION_VIEW);
		boolean willLaunch = haveUrl && !offlineSetting && !restarting;  /* We're online and have a new URL */
 
 		if (!willLaunch) {
 			/*
 			 * We're going to save the URL and show the list. TODO: A
 			 * "save but don't show" option might be good.
 			 */
			if (haveUrl && !restarting) {
 				urlstore.addUrl(intent.getDataString());
 			}
 			setContentView(R.layout.main);
 
 			ToggleButton button = (ToggleButton) findViewById(R.id.toggleOffline);
 			button.setChecked(offlineSetting);
 			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					SharedPreferences prefs = PreferenceManager
 							.getDefaultSharedPreferences(getApplicationContext());
 
 					Log.v("URLHandler", "on click value: " + isChecked);
 					prefs.edit().putBoolean("offline", isChecked).commit();
 					Log.v("URLHandler",
 							"committed (count = " + prefs.getInt("count", -1));
 				}
 
 			});
 		}
 
 		/* If online, simply relaunch it. */
 		if (willLaunch) {
 			launchUrl(intent.getDataString());
 			/* And exit - nothing else to do once we've launched the URL again. */
 			finish();
 		} else {
 			showList(urlstore);
 		}
 
 	}
 
 	private void showList(UrlStore urlstore) {
 		cursor = urlstore.getUrlCursor();
 
 		int[] to = { R.id.tv1, R.id.tv2 };
 
 		setListAdapter(new SimpleCursorAdapter(getApplicationContext(),
 				R.layout.urllist, cursor, colFields, to));
 
 		ListView lv = getListView();
 
 		registerForContextMenu(lv);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.urlpopup, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.remove: {
 			UrlStore urlstore = new UrlStore(getApplicationContext());
 
 			int items = urlstore.removeUrl(info.id);
 
 			// ((SimpleCursorAdapter) getListAdapter()).notifyDataSetChanged();
 			cursor.requery();
 			Toast t = Toast.makeText(getApplicationContext(),
 					Integer.toString(items) + " item(s) deleted.",
 					Toast.LENGTH_SHORT);
 			t.show();
 
 			return true;
 		}
 		case R.id.expand: {
 			UrlStore urlstore = new UrlStore(getApplicationContext());
 			String url = urlstore.getUrl(info.id);
 			String expanded = expandUrl(url, true);
 			if (expanded != null) {
 				urlstore.setUrl(info.id, expanded);
 				cursor.requery();
 				Toast t = Toast.makeText(getApplicationContext(),
 						"URL expanded", Toast.LENGTH_SHORT);
 				t.show();
 			} else {
 				Toast t = Toast.makeText(getApplicationContext(),
 						"URL expansion failed", Toast.LENGTH_SHORT);
 				t.show();
 			}
 			return true;
 		}
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	private void doRemoveAll() {
 		UrlStore urlstore = new UrlStore(getApplicationContext());
 
 		int items = urlstore.removeAllUrls();
 
 		cursor.requery();
 		Toast t = Toast.makeText(getApplicationContext(),
 				Integer.toString(items) + " item(s) deleted.",
 				Toast.LENGTH_SHORT);
 		t.show();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.expand_all: {
 			UrlStore urlstore = new UrlStore(getApplicationContext());
 			Cursor urls = urlstore.getUnexpanded();
 
 			urls.moveToFirst();
 
 			while (!urls.isAfterLast()) {
 				// TODO: use constants for field indices
 				String url = urls.getString(1);
 				int id = urls.getInt(0);
 
 				String expanded = expandUrl(url, true);
 				if (expanded != null) {
 					urlstore.setUrlExpansion(id, expanded);
 				}
 				urls.moveToNext();
 			}
 			urls.close();
 			cursor.requery();
 
 			return true;
 		}
 		case R.id.remove_all: {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Remove all URLs?")
 					.setCancelable(false)
 					.setPositiveButton("Yes",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									URLHelperActivity.this.doRemoveAll();
 								}
 							})
 					.setNegativeButton("No",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		}
 		case R.id.menu_settings: {
 			Intent settingsIntent = new Intent(this, SettingsActivity.class);
 			startActivity(settingsIntent);
 			return true;
 		}
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		UrlStore urlstore = new UrlStore(getApplicationContext());
 
 		String urlString = urlstore.getUrl(id);
 
 		launchUrl(urlString);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.v("handler", "in onActivityResult");
 		if (requestCode == REQ_CHOOSE_INTENT) {
 			Log.i("URLHandler", "Got activity result");
 			Log.v("handler", "got result, saved str=" + intentOptionsStr);
 			Log.v("handler", "got resultCode" + resultCode);
 			if (resultCode == RESULT_OK) {
 				/*
 				 * Save the handler preference. TODO: Need option to not make it
 				 * default!
 				 */
 				if (intentOptionsStr != null) {
 					Log.v("handler", "saved string=" + intentOptionsStr);
 					UrlStore urlstore = new UrlStore(getApplicationContext());
 					ComponentName comp = data.getComponent();
 					urlstore.setHandlerSet(intentOptionsStr,
 							comp.getPackageName(), comp.getClassName());
 					Log.v("handler", "saving as " + comp.getPackageName() + "/"
 							+ comp.getClassName());
 				}
 
 				startActivity(data);
 			}
 			intentOptionsStr = null;
 		}
 	}
 }
