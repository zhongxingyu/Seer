 package org.droidstack.activity;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import net.sf.stackwrap4j.StackWrapper;
 import net.sf.stackwrap4j.entities.Stats;
 import net.sf.stackwrap4j.http.HttpClient;
 
 import org.droidstack.R;
 import org.droidstack.adapter.SitesCursorAdapter;
 import org.droidstack.service.NotificationsService;
 import org.droidstack.util.Const;
 import org.droidstack.util.SitesDatabase;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.NetworkInfo.State;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class SitesActivity extends ListActivity {
 	
 	private final static int REQUEST_PICK_USER = 1;
 	private final static int REQUEST_PICK_SITES = 2;
 	
 	private SitesDatabase mSitesDatabase;
 	private Cursor mSites;
 	private SitesCursorAdapter mAdapter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sites);
 
         if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         	externalMediaError();
         }
         
         final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         final NetworkInfo info = cm.getActiveNetworkInfo();
         if (info == null || info.getState() != State.CONNECTED) {
         	networkError();
         }
         
         HttpClient.setTimeout(Const.NET_TIMEOUT);
         mSitesDatabase = new SitesDatabase(this);
         mSites = mSitesDatabase.getSites();
         startManagingCursor(mSites);
         
         if (Const.getIconsDir() == null) externalMediaError();
 
         mAdapter = new SitesCursorAdapter(this, mSites);
         setListAdapter(mAdapter);
         registerForContextMenu(getListView());
         
         // check for missing icons
         mSites.moveToFirst();
         ArrayList<String> endpoints = new ArrayList<String>();
         while (!mSites.isAfterLast()) {
         	String endpoint = SitesDatabase.getEndpoint(mSites);
         	String host = Uri.parse(endpoint).getHost();
         	File f = new File(Const.getIconsDir(), host);
         	if (!f.exists()) {
         		endpoints.add(endpoint);
         	}
         	mSites.moveToNext();
         }
         if (endpoints.size() > 0) {
         	new GetIcons(endpoints).execute();
         }
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	int minutes = 0;
     	try {
     		minutes = Integer.parseInt(prefs.getString(Const.PREF_NOTIF_INTERVAL, Const.DEF_NOTIF_INTERVAL));
     	}
     	catch (NumberFormatException e) { }
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		Intent i = new Intent(this, NotificationsService.class);
 		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
 		am.cancel(pi);
     	if (minutes > 0) {
     		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
     				SystemClock.elapsedRealtime() + minutes*60*1000,
     				minutes*60*1000, pi);
     		Log.d(Const.TAG, "AlarmManager set");
     	}
     }
     
     private class GetIcons extends AsyncTask<Void, Void, Void> {
     	private final HashMap<String, String> icons;
     	private final List<String> endpoints;
     	public GetIcons(HashMap<String, String> icons) {
     		this.icons = icons;
     		this.endpoints = null;
     	}
     	public GetIcons(List<String> endpoints) {
     		this.endpoints = endpoints;
     		this.icons = new HashMap<String, String>();
     	}
     	@Override
     	protected Void doInBackground(Void... params) {
     		if (endpoints != null) {
     			for (String endpoint: endpoints) {
     				StackWrapper api = new StackWrapper(endpoint, Const.APIKEY);
     				try {
     					Stats stats = api.getStats();
     					icons.put(stats.getSite().getIconUrl(), Uri.parse(endpoint).getHost());
     				}
     				catch (Exception e) {
     					Log.e(Const.TAG, "Could not get site stats for " + endpoint, e);
     				}
     			}
     		}
     		for (Entry<String, String> icon: icons.entrySet()) {
     			try {
 	    			File f = new File(Const.getIconsDir(), icon.getValue());
 	    			InputStream in = new URL(icon.getKey()).openStream();
 	    			OutputStream out = new FileOutputStream(f);
 	    			byte[] buf = new byte[8192];
 	    			int len;
 	    			while ((len = in.read(buf)) > 0) {
 	    				out.write(buf, 0, len);
 	    			}
 	    			in.close();
 	    			out.close();
     			}
     			catch (Exception e) {
     				Log.e(Const.TAG, "Could not fetch icon " + icon.getKey() + " for " + icon.getValue(), e);
     			}
     			publishProgress();
     		}
     		return null;
     	}
     	@Override
     	protected void onProgressUpdate(Void... values) {
     		mAdapter.notifyDataSetChanged();
     	}
     	@Override
     	protected void onPostExecute(Void result) {
     		if (isFinishing()) return;
     		mAdapter.notifyDataSetChanged();
     	}
     }
     
     private void externalMediaError() {
     	new AlertDialog.Builder(this)
 		.setTitle(R.string.title_error)
 		.setCancelable(false)
 		.setMessage(R.string.no_sd_error)
 		.setNeutralButton(android.R.string.ok, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			}
 		})
 		.create().show();
     }
     
     private void networkError() {
     	new AlertDialog.Builder(this)
 		.setTitle(R.string.title_error)
 		.setCancelable(false)
 		.setMessage(R.string.no_network_error)
 		.setNeutralButton(android.R.string.ok, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			}
 		})
 		.create().show();
     }
 
     @Override
     protected void onListItemClick(ListView parent, View view, int position, long id) {
     	Object item = mAdapter.getItem(position);
 		if (item instanceof Integer) return;
 		Cursor site = (Cursor) item;
 		String endpoint = site.getString(site.getColumnIndex(SitesDatabase.KEY_ENDPOINT));
 		String name = site.getString(site.getColumnIndex(SitesDatabase.KEY_NAME));
 		int uid = site.getInt(site.getColumnIndex(SitesDatabase.KEY_UID));
 		String uname = site.getString(site.getColumnIndex(SitesDatabase.KEY_UNAME));
 		Intent i = new Intent(SitesActivity.this, SiteActivity.class);
 		String uri = "droidstack://site" +
 			"?endpoint=" + Uri.encode(endpoint) +
 			"&name=" + Uri.encode(name) +
 			"&uid=" + String.valueOf(uid) +
 			"&uname=" + Uri.encode(uname);
 		i.setData(Uri.parse(uri));
 		startActivity(i);
     }
     
     @Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
     	if (v.getId() == android.R.id.list) {
     		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
     		Object item = mAdapter.getItem(info.position);
     		if (item instanceof Integer) return;
     		Cursor site = (Cursor) item;
     		String name = site.getString(site.getColumnIndex(SitesDatabase.KEY_NAME));
     		menu.setHeaderTitle(name);
     		getMenuInflater().inflate(R.menu.site_context, menu);
     	}
 	}
     
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		mSites.moveToPosition(info.position);
 		final String endpoint = SitesDatabase.getEndpoint(mSites);
 		switch(item.getItemId()) {
 		case R.id.menu_set_user:
 			Intent i = new Intent(this, UsersActivity.class);
 			i.setAction(Intent.ACTION_PICK);
 			String uri = "droidstack://users?endpoint=" + Uri.encode(endpoint);
 			i.setData(Uri.parse(uri));
 			startActivityForResult(i, REQUEST_PICK_USER);
 			return true;
 		case R.id.menu_remove:
 			mSitesDatabase.removeSite(endpoint);
 			mSites.requery();
 			mAdapter.notifyDataSetChanged();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Bundle extras;
 		if (data != null) extras = data.getExtras();
 		else extras = null;
 		switch(requestCode) {
 		case REQUEST_PICK_USER:
 			if (resultCode == RESULT_OK) {
 				String endpoint = extras.getString("endpoint");
 				int uid = extras.getInt("uid");
 				String name = extras.getString("name");
 				mSitesDatabase.setUser(endpoint, uid, name);
 				mSites.requery();
 				mAdapter.notifyDataSetChanged();
 			}
 			break;
 		case REQUEST_PICK_SITES:
 			if (resultCode == RESULT_OK) {
 				String[] endpoints = (String[]) extras.getSerializable("endpoints");
 				String[] names = (String[]) extras.getSerializable("names");
 				String[] icons = (String[]) extras.getSerializable("icons");
 				// remove unchecked sites
 				mSites.moveToFirst();
 				while (!mSites.isAfterLast()) {
 					String endpoint = SitesDatabase.getEndpoint(mSites);
 					boolean flag = false;
 					for (String e: endpoints) {
 						if (e.equals(endpoint))
 							flag = true;
 					}
 					if (!flag) mSitesDatabase.removeSite(endpoint);
 					mSites.moveToNext();
 				}
 				// add checked sites
 				HashMap<String, String> iconsToGet = new HashMap<String, String>();
 				for (int i=0; i < endpoints.length; i++) {
 					if (mSitesDatabase.exists(endpoints[i])) continue;
 					mSitesDatabase.addSite(endpoints[i], names[i], 0, null);
 					String host = Uri.parse(endpoints[i]).getHost();
 					File icon = new File(Const.getIconsDir(), host);
 					if (!icon.exists())
 						iconsToGet.put(icons[i], host);
 				}
 				if (iconsToGet.size() > 0) new GetIcons(iconsToGet).execute();
 				mSites.requery();
 				mAdapter.notifyDataSetChanged();
 			}
 			break;
 		default:
 			super.onActivityResult(requestCode, resultCode, data);
 			break;
 		}
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.sites, menu);
     	return true;
     }
 	
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent i;
     	switch (item.getItemId()) {
     	case R.id.menu_settings:
     		i = new Intent(this, PreferencesActivity.class);
     		startActivity(i);
     		return true;
     	case R.id.menu_pick_sites:
     		i = new Intent(this, SitePickerActivity.class);
     		String[] checked = new String[mSites.getCount()];
     		mSites.moveToFirst();
     		while (!mSites.isAfterLast()) {
     			String endpoint = mSites.getString(mSites.getColumnIndex(SitesDatabase.KEY_ENDPOINT));
     			checked[mSites.getPosition()] = endpoint;
     			mSites.moveToNext();
     		}
     		i.putExtra("checked", checked);
     		startActivityForResult(i, REQUEST_PICK_SITES);
     		return true;
     	}
     	return false;
     }
 
 	public void openChat(View target) {
 		int position = (Integer) target.getTag();
 		mSites.moveToPosition(position);
 		String endpoint = SitesDatabase.getEndpoint(mSites);
 		String name = SitesDatabase.getName(mSites);
 		Intent i = new Intent(this, ChatActivity.class);
 		String uri = "droidstack://chat" +
 			"?endpoint=" + Uri.encode(endpoint) +
 			"&name=" + Uri.encode(name);
 		i.setData(Uri.parse(uri));
 		startActivity(i);
 	}
     
 }
