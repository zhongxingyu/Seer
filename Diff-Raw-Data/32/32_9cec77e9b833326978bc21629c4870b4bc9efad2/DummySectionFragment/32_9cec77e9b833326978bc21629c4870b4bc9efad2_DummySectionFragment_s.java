 package com.bluemoonscience.whatscoolbyu;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.ContentValues;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 
 /**
  * A dummy fragment representing a section of the app, but that simply displays dummy text.
  */
 public class DummySectionFragment extends Fragment implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 	/**
 	 * The fragment argument representing the section number for this fragment.
 	 */
 	public static final String ARG_SECTION_NUMBER = "section_number";
 	private ListView dummyListView;
 	private TextView dummyEmptyTextView;
 	private ProgressBar dummyEmptyProgressBar;
 	private SimpleCursorAdapter adapter;
 	ArrayList<Entry> image_details = new ArrayList<Entry>();
 	ItemListBaseAdapter mAdapter;
 	Calendar dateLocalRecent = new GregorianCalendar(1988, 6, 26, 03, 03, 07);
 
 	public DummySectionFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.d("dummy", "onCreateView & refreshDisplay = " + MainActivity.refreshDisplay);
 		setRetainInstance(true);
 		View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
 		dummyListView = (ListView) rootView.findViewById(R.id.dummyList);
 		dummyEmptyTextView = (TextView) rootView.findViewById(R.id.dummyEmptyTextView);
 		dummyEmptyProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
 		dummyListView.setEmptyView(rootView.findViewById(R.id.relLayoutEmpty));
		fillData();
		// if (mAdapter == null)
		// mAdapter = new ItemListBaseAdapter(getActivity().getApplicationContext(),
		// image_details);
		// dummyListView.setAdapter(mAdapter);
 
 		Log.d("dummy", "onCreateView & dummyAdapter = "
 				+ dummyListView.getAdapter().toString());
 		return rootView;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.list_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// handle item selection
 		switch (item.getItemId()) {
 		case R.id.list_refresh_menuitem:
 			if (this != null) {
 				loadPage();
 			} else {
 				Log.d("main", "button pushed & dummyFrag == null");
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		((MainActivity) getActivity()).dummyFrag = this;
 		setHasOptionsMenu(true);
 	}
 
 	// Refreshes the display if the network connection and the
 	// pref settings allow it.
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.d("dummy", "onStart & refreshDisplay = " + MainActivity.refreshDisplay);
 		// Gets the user's network preference settings
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity().getApplicationContext());
 
 		// Retrieves a string value for the preferences. The second parameter
 		// is the default value to use if a preference value is not found.
 		MainActivity.sPref = sharedPrefs.getString("listPref", "Wi-Fi");
 
 		updateConnectedFlags();
 
 		// Only loads the page if refreshDisplay is true. Otherwise, keeps previous
 		// display. For example, if the user has set "Wi-Fi only" in prefs and the
 		// device loses its Wi-Fi connection midway through the user using the app,
 		// you don't want to refresh the display--this would force the display of
 		// an error page instead of stackoverflow.com content.
 		if (MainActivity.refreshDisplay || dummyEmptyTextView.getText().equals("Empty List")) {
 			loadPage();
 		}
 	}
 
 	// Checks the network connection and sets the wifiConnected and
 	// mobileConnected
 	// variables accordingly.
 	private void updateConnectedFlags() {
 		((MainActivity) getActivity()).updateInternetFlags();
 	}
 
 	// Uses AsyncTask subclass to download the XML feed from
 	// stackoverflow.com.
 	// This avoids UI lock up. To prevent network operations from
 	// causing a delay that results in a poor user experience, always
 	// perform
 	// network operations on a separate thread from the UI.
 	public void loadPage() {
 		Log.d("dummy", "loadPage & refreshDisplay = " + MainActivity.refreshDisplay);
 		// return;
 		if (((MainActivity.sPref.equals(MainActivity.ANY)) && (MainActivity.wifiConnected || MainActivity.mobileConnected))
 				|| ((MainActivity.sPref.equals(MainActivity.WIFI)) && (MainActivity.wifiConnected))) {
 			// AsyncTask subclass
 			Log.d("loadPage", "just before executing(URL)");
 			new DownloadXmlTask().execute(buildRecentDateURL());
 			dummyEmptyTextView.setText("Success!");
 		} else {
 			showErrorPage();
 		}
 	}
 
 	// Displays an error if the app is unable to load content.
 	private void showErrorPage() {
 		dummyEmptyTextView.setText(getResources().getString(R.string.connection_error));
 		dummyEmptyProgressBar.setAlpha(0);
 	}
 
 	// Implementation of AsyncTask used to download XML feed from
 	// stackoverflow.com.
 	private class DownloadXmlTask extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... urls) {
 			try {
 				Log.d("downloadXMLTask", "starting loadXmlFromNetwork & url = " + urls[0]);
 				return loadXmlFromNetwork(urls[0]);
 			} catch (IOException e) {
 				return getResources().getString(R.string.connection_error);
 			} catch (XmlPullParserException e) {
 				return getResources().getString(R.string.xml_error);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// TODO update local copy to match the results
 		}
 	}
 
 	// Uploads XML from stackoverflow.com, parses it, and combines it with
 	// HTML markup. Returns HTML string.
 	private String loadXmlFromNetwork(String urlString) throws XmlPullParserException,
 			IOException {
 		InputStream stream = null;
 		StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();
 		List<Entry> entries = null;
 
 		try {
 			Log.d("laodXML", "before downloadURL");
 			stream = downloadUrl(urlString);
 			Log.d("laodXML", "before parse");
 			entries = stackOverflowXmlParser.parse(stream);
 			Log.d("laodXML", "after parse");
 			// Makes sure that the InputStream is closed after the app is
 			// finished using it.
 		} finally {
 			if (stream != null) {
 				stream.close();
 			}
 		}
 		Log.d("loadXML", "number of entries = " + Integer.toString(entries.size()));
 
 		for (Entry entry : entries) {
 			Log.d("cloud entry", entry.toString());
 			String[] projection = { ByuTable.COLUMN_ID };
 			Cursor c = getActivity().getContentResolver().query(
 					MyByuContentProvider.CONTENT_URI, projection,
 					ByuTable.COLUMN_CLOUDID + " = " + entry.id, null, null);
 			ContentValues values = new ContentValues();
 			values.put(ByuTable.COLUMN_CLOUDID, entry.id);
 			values.put(ByuTable.COLUMN_SDESCRIPTION, entry.sDescription);
 			values.put(ByuTable.COLUMN_TIMESTAMP, entry.timestamp);
 			values.put(ByuTable.COLUMN_TITLE, entry.title);
 			values.put(ByuTable.COLUMN_AVGRATING, entry.avgRating);
 			values.put(ByuTable.COLUMN_LASTUPDATESHORT, entry.lastUpdateShort);
 			values.put(ByuTable.COLUMN_LAT, entry.lat);
 			values.put(ByuTable.COLUMN_LNG, entry.lng);
 			values.put(ByuTable.COLUMN_PICTUREURL, entry.pictureURL);
 			if (c.getCount() > 0) {
 				Log.d("query", "the query returned a result");
 				c.moveToFirst();
 				getActivity().getContentResolver().update(MyByuContentProvider.CONTENT_URI,
 						values, ByuTable.COLUMN_CLOUDID + " = " + entry.id, null);
 			} else {
 				Log.d("query", "the query returned NO results");
 				getActivity().getContentResolver().insert(MyByuContentProvider.CONTENT_URI,
 						values);
 			}
 			c.close();
 
 		}
 		return "done";
 	}
 
 	// Given a string representation of a URL, sets up a connection and gets
 	// an input stream.
 	private InputStream downloadUrl(String urlString) throws IOException {
 		URL url = new URL(urlString);
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.setReadTimeout(10000 /* milliseconds */);
 		conn.setConnectTimeout(15000 /* milliseconds */);
 		conn.setRequestMethod("GET");
 		conn.setDoInput(true);
 		// Starts the query
 		conn.connect();
 		InputStream stream = conn.getInputStream();
 		return stream;
 	}
 
 	// Creates a new loader after the initLoader () call
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		Log.d("dummy", "onCreateLoader");
 		// String[] projection = { ByuTable.COLUMN_ID, ByuTable.COLUMN_SDESCRIPTION,
 		// ByuTable.COLUMN_TITLE, ByuTable.COLUMN_TIMESTAMP };
 		CursorLoader cursorLoader = new CursorLoader(getActivity().getApplicationContext(),
 				MyByuContentProvider.CONTENT_URI, null, null, null, ByuTable.COLUMN_TIMESTAMP
 						+ " DESC");
 		return cursorLoader;
 	}
 
 	// Opens the second activity if an entry is clicked
 	// @Override
 	// protected void onListItemClick(ListView l, View v, int position, long id) {
 	// super.onListItemClick(l, v, position, id);
 	// Intent i = new Intent(getActivity().getApplicationContext(), ByuDetailActivity.class);
 	// Uri todoUri = Uri.parse(MyByuContentProvider.CONTENT_URI + "/" + id);
 	// i.putExtra(MyByuContentProvider.CONTENT_ITEM_TYPE, todoUri);
 	//
 	// startActivity(i);
 	// }
 
	private void fillData() {
		Log.d("dummy", "fillData");
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
 		String[] from = new String[] { ByuTable.COLUMN_TITLE, ByuTable.COLUMN_SDESCRIPTION };
 		// Fields on the UI to which we map
 		int[] to = new int[] { R.id.tvTitle, R.id.tvShortDesc };
 
 		getLoaderManager().initLoader(0, null, this);
 		adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
 				R.layout.list_entry, null, from, to, 0);

 		dummyListView.setAdapter(adapter);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		Log.d("dummy", "onLoadFinished");
 		adapter.swapCursor(data);
		dateLocalRecent = getMostRecentLocalTimestamp(data);
		Log.d("dummy", "most recent = " + dateLocalRecent.toString());
		new DownloadXmlTask().execute(buildRecentDateURL());
 	}
 
 	private String buildRecentDateURL() {
 		// http://aaronapps.bluemoonscience.com/getAfterDate.php?year=2013&month=06&day=26&hour=03&minute=03&second=07
 		String url = "http://aaronapps.bluemoonscience.com/getAfterDate.php";
 		url += "?year=" + dateLocalRecent.get(Calendar.YEAR);
 		url += "&month=" + dateLocalRecent.get(Calendar.MONTH);
 		url += "&day=" + dateLocalRecent.get(Calendar.DAY_OF_MONTH);
 		url += "&hour=" + dateLocalRecent.get(Calendar.HOUR);
 		url += "&minute=" + dateLocalRecent.get(Calendar.MINUTE);
 		url += "&second=" + dateLocalRecent.get(Calendar.SECOND);
 		Log.d("main", "buildDateURL = " + url);
 		return url;
 	}
 
 	private Calendar getMostRecentLocalTimestamp(Cursor data) {
 		Log.d("dummy", "getMostRecent count = " + data.getCount());
 		Calendar recent = dateLocalRecent;
 		while (data.moveToNext()) {
 			String preTime = data.getString(data.getColumnIndex(ByuTable.COLUMN_TIMESTAMP));
 			Integer year = Integer.parseInt(preTime.substring(0, 4));
 			Integer month = Integer.parseInt(preTime.substring(5, 7));
 			Integer day = Integer.parseInt(preTime.substring(8, 10));
 			Integer hour = Integer.parseInt(preTime.substring(11, 13));
 			Integer minute = Integer.parseInt(preTime.substring(14, 16));
 			Integer second = Integer.parseInt(preTime.substring(17, 19));
 			Log.d("dummy", "preTIme = " + preTime);
 			Log.d("dummy", String.format(
 					"year = %d,  month = %d, day = %d, hour = %d, minute = %d, second = %d",
 					year, month, day, hour, minute, second));
			Calendar thisDate = new GregorianCalendar(year, month, day, hour, minute, second);
 			if (thisDate.after(recent)) {
 				recent = thisDate;
 			}
 		}
 		return recent;
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		Log.d("dummy", "onLoaderReset");
 		// data is not available anymore, delete reference
 		adapter.swapCursor(null);
 	}
 }
