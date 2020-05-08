 package com.messedagliavr.messeapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.ParseException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 @SuppressWarnings("unused")
 @SuppressLint("SimpleDateFormat")
 public class news extends ListActivity {
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public String[] titolim;
 	public String[] descrizionim;
 	public Boolean canceled = true;
 	ProgressDialog mDialog;
 	public Boolean unknhost = false;
 	public SQLiteDatabase db;
 	public Cursor data;
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance(this).activityStart(this); // Add this method.
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance(this).activityStop(this); // Add this method.
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		mDialog = new ProgressDialog(news.this);
 		super.onCreate(savedInstanceState);
 		new connection().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.list_item, menu);
 		return true;
 	}
 
 	public boolean CheckInternet() {
 		boolean connected = false;
 		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		android.net.NetworkInfo wifi = connec
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		android.net.NetworkInfo mobile = connec
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		if (wifi.isConnected()) {
 			connected = true;
 		} else {
 			try {
 				if (mobile.isConnected())
 					connected = true;
 			} catch (Exception e) {
 			}
 
 		}
 
 		return connected;
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			if (CheckInternet() == true) {
 				Database databaseHelper = new Database(getBaseContext());
 				SQLiteDatabase db = databaseHelper.getWritableDatabase();
 				ContentValues nowdb = new ContentValues();
 				nowdb.put("newsdate", "2012-02-20 15:00:00");
 				long samerow = db.update("lstchk", nowdb, null, null);
 				db.close();
 				MainActivity.nointernet = "false";
 				new connection().execute();
 			} else {
 				Toast.makeText(this, R.string.noconnectionupdate,
 						Toast.LENGTH_LONG).show();
 			}
 
 			break;
 		}
 		return true;
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	private Long getTimeDiff(String time, String curTime) throws ParseException {
 		Date curDate = null;
 		Date oldDate = null;
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		try {
 			curDate = (Date) formatter.parse(curTime);
 			oldDate = (Date) formatter.parse(time);
 		} catch (java.text.ParseException e) {
 			e.printStackTrace();
 		}
 		long oldMillis = oldDate.getTime();
 		long curMillis = curDate.getTime();
 		long diff = curMillis - oldMillis;
 		return diff;
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent main = new Intent(this, MainActivity.class);
 		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(main);
 	}
 
 	public class connection extends
 			AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
 		protected void onCancelled() {
 			Intent main = new Intent(news.this, MainActivity.class);
 			main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(main);
 			Toast.makeText(news.this, R.string.cancelednews, Toast.LENGTH_LONG)
 					.show();
 		}
 
 		public void onCanceled() {
 			mDialog.dismiss();
 		}
 
 		public void onPreExecute() {
 			if (MainActivity.nointernet == "true") {
				mDialog = ProgressDialog.show(news.this, getString(R.string.retrieving),
						getString(R.string.retrievingNews), true, true,
 						new DialogInterface.OnCancelListener() {
 							public void onCancel(DialogInterface dialog) {
 								connection.this.cancel(true);
 							}
 						});
 			} else {
				mDialog = ProgressDialog.show(news.this, getString(R.string.downloading),
						getString(R.string.downloadNews), true, true,
 						new DialogInterface.OnCancelListener() {
 							public void onCancel(DialogInterface dialog) {
 								connection.this.cancel(true);
 							}
 						});
 			}
 			mDialog.show();
 		}
 
 		public HashMap<String, ArrayList<Spanned>> doInBackground(
 				Void... params) {
 			Database databaseHelper = new Database(getBaseContext());
 			db = databaseHelper.getWritableDatabase();
 			HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
 			ArrayList<Spanned> titoli = new ArrayList<Spanned>();
 			ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
 			ArrayList<Spanned> titolib = new ArrayList<Spanned>();
 			// All static variables
 			final String URL = "http://www.messedaglia.it/index.php?option=com_ninjarsssyndicator&feed_id=1&format=raw";
 			// XML node keys
 			final String ITEM = "item"; // parent node
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e = null;
 			ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
 			String[] outdated = { "newsdate", "calendardate" };
 			Calendar c = Calendar.getInstance();
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			String now = df.format(c.getTime());
 			Cursor date = db.query("lstchk", // The table to query
 					outdated, // The columns to return
 					null, // The columns for the WHERE clause
 					null, // The values for the WHERE clause
 					null, // don't group the rows
 					null, // don't filter by row groups
 					null // The sort order
 					);
 			date.moveToFirst();
 			String past = date.getString(date.getColumnIndex("newsdate"));
 			date.close();
 			long l = getTimeDiff(past, now);
 			if (l / 10800000 >= 3 && MainActivity.nointernet != "true") {
 				XMLParser parser = new XMLParser();
 				String xml = parser.getXmlFromUrl(URL);
 				if (xml == "UnknownHostException") {
 					unknhost = true;
 					db.close();
 					return temhashmap;
 				} else {
 					Document doc = parser.getDomElement(xml);
 					NodeList nl = doc.getElementsByTagName(ITEM);
 					ContentValues values = new ContentValues();
 					for (int i = 0; i < nl.getLength(); i++) {
 						HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 						e = (Element) nl.item(i);
 						values.put("_id", i);
 						values.put(TITLE, parser.getValue(e, TITLE));
 						values.put(DESC, parser.getValue(e, DESC));
 						values.put("titleb", "<b>" + parser.getValue(e, TITLE)
 								+ "</b>");
 						map.put(TITLE, Html.fromHtml(parser.getValue(e, TITLE)));
 						map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
 
 						titoli.add(Html.fromHtml(parser.getValue(e, TITLE)));
 						descrizioni
 								.add(Html.fromHtml(parser.getValue(e, DESC)));
 						titolib.add(Html.fromHtml("<b>"
 								+ parser.getValue(e, TITLE) + "</b>"));
 						// adding HashList to ArrayList
 						menuItems.add(map);
 						long newRowId = db.insertWithOnConflict("news", null,
 								values, SQLiteDatabase.CONFLICT_REPLACE);
 					}
 					ContentValues nowdb = new ContentValues();
 					nowdb.put("newsdate", now);
 					long samerow = db.update("lstchk", nowdb, null, null);
 					temhashmap.put("titoli", titoli);
 					temhashmap.put("descrizioni", descrizioni);
 					temhashmap.put("titolib", titolib);
 					return temhashmap;
 
 				}
 			} else {
 				String[] clmndata = { "title", "description", "titleb" };
 				String sortOrder = "_id";
 
 				data = db.query("news", // The table to query
 						clmndata, // The columns to return
 						null, // The columns for the WHERE clause
 						null, // The values for the WHERE clause
 						null, // don't group the rows
 						null, // don't filter by row groups
 						sortOrder // The sort order
 						);
 				for (data.move(0); data.moveToNext(); data.isAfterLast()) {
 					HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 					map.put(TITLE, Html.fromHtml(data.getString(data
 							.getColumnIndex("title"))));
 					map.put(DESC, Html.fromHtml(data.getString(data
 							.getColumnIndex("description"))));
 
 					titoli.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("title"))));
 					descrizioni.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("description"))));
 					titolib.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("titleb"))));
 					// adding HashList to ArrayList
 					menuItems.add(map);
 
 				}
 				data.close();
 				db.close();
 				temhashmap.put("titoli", titoli);
 				temhashmap.put("descrizioni", descrizioni);
 				temhashmap.put("titolib", titolib);
 				return temhashmap;
 
 			}
 
 		}
 
 		public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
 			if (unknhost == true) {
 				mDialog.dismiss();
 				Toast.makeText(news.this, R.string.connerr, Toast.LENGTH_LONG)
 						.show();
 				Intent main = new Intent(news.this, MainActivity.class);
 				main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(main);
 			} else {
 				if (resultmap.size() > 0) {
 					final ArrayList<Spanned> titoli = resultmap.get("titoli");
 					final ArrayList<Spanned> descrizioni = resultmap
 							.get("descrizioni");
 					final ArrayList<Spanned> titolib = resultmap.get("titolib");
 
 					ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
 							news.this, android.R.layout.simple_list_item_1,
 							titolib);
 					setContentView(R.layout.list_item);
 					ListView listView = (ListView) news.this
 							.findViewById(android.R.id.list);
 					listView.setAdapter(adapter);
 					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 						public void onItemClick(AdapterView<?> parentView,
 								View childView, int position, long id) {
 							Intent intent = new Intent(news.this,
 									ListItemSelectedNews.class);
 							intent.putExtra(TITLE,
 									Html.toHtml(titoli.get(position)));
 							intent.putExtra(DESC,
 									Html.toHtml(descrizioni.get(position)));
 							startActivity(intent);
 						}
 					});
 				}
 				mDialog.dismiss();
 			}
 		}
 	}
 }
