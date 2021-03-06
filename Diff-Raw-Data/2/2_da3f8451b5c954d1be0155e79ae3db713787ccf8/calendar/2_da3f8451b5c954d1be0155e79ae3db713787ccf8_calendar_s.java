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
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ParseException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 @SuppressWarnings("unused")
 @SuppressLint("SimpleDateFormat")
 public class calendar extends ListActivity {
 	public SQLiteDatabase db;
 	public Cursor data;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		mDialog = new ProgressDialog(calendar.this);
 		super.onCreate(savedInstanceState);
 		new connection().execute();
 	}
 
 	@Override
 	public void onBackPressed() {
 		db.close();
 		data.close();
 		Intent main = new Intent(this, MainActivity.class);
 		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.list_item, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			Database databaseHelper = new Database(getBaseContext());
 			SQLiteDatabase db = databaseHelper.getWritableDatabase();
 			ContentValues nowdb = new ContentValues();
 			nowdb.put("calendardate", "2012-02-20 15:00:00");
 			long samerow = db.update("lstchk", nowdb, null, null);
 			db.close();
 			new connection().execute();
 			break;
 		}
 		return true;
 	}
 
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
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public static final String ICAL = "ical";
 	public String[] titolim;
 	public String[] descrizionim;
 	ProgressDialog mDialog;
 	public Boolean unknhost = false;
 	public String idical = null;
 
 	public class eventparser extends AsyncTask<Void, Void, String[]> {
 		@Override
 		protected String[] doInBackground(Void... params) {
 			String ical = "http://lookedpath.altervista.org/test.php?id="
 					+ idical;
 			System.out.println("Prima parser  " + idical);
 			XMLParser parser = new XMLParser();
 			String xml = parser.getXmlFromUrl(ical);
 			System.out.println("Dopo get xml");
 			Document doc = parser.getDomElement(xml);
 			System.out.println("Dopo dom");
 			NodeList nl = doc.getElementsByTagName("VEVENT");
 			System.out.println("Dopo elements");
			String[] dati = {};
 			Element e = (Element) nl.item(0);
 			System.out.println("Prima array");
 			dati[0] = parser.getValue(e, "SUMMARY");
 			dati[1] = parser.getValue(e, "DESCRIPTION");
 			dati[2] = parser.getValue(e, "LOCATION");
 			dati[3] = parser.getValue(e, "DTSTART");
 			dati[4] = parser.getValue(e, "DTEND");
 			System.out.println("Dopo array");
 			return dati;
 		}
 
 		public Void onPostExecute(String[]... dati) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyyyMMdd'T'HHmmss");
 			Date fine = null;
 			Date inizio = null;
 			try {
 				fine = dateFormat.parse(dati[4].toString());
 				inizio = dateFormat.parse(dati[3].toString());
 			} catch (java.text.ParseException e1) {
 				e1.printStackTrace();
 			}
 			try {
 				Intent intent = new Intent(Intent.ACTION_INSERT)
 						.setType("vnd.android.cursor.item/event")
 						.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
 								inizio.getTime())
 						.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
 								fine.getTime())
 						.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
 						.putExtra(Events.TITLE, dati[0])
 						.putExtra(Events.DESCRIPTION, dati[1])
 						.putExtra(Events.EVENT_LOCATION,
 								dati[2] + " A. Messedaglia");
 				startActivity(intent);
 			} catch (Exception e) {
 				Toast.makeText(calendar.this, R.string.noapilevel,
 						Toast.LENGTH_LONG).show();
 			}
 			return null;
 		}
 
 	}
 
 	public class connection extends
 			AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
 
 		protected void onCancelled() {
 			Intent main = new Intent(calendar.this, MainActivity.class);
 			main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(main);
 			Toast.makeText(calendar.this, R.string.canceledcalendar,
 					Toast.LENGTH_LONG).show();
 		}
 
 		public void onPreExecute() {
 			mDialog = ProgressDialog.show(calendar.this, "Scaricando",
 					"Sto scaricando gli eventi", true, true,
 					new DialogInterface.OnCancelListener() {
 						public void onCancel(DialogInterface dialog) {
 							connection.this.cancel(true);
 						}
 					});
 			mDialog.show();
 		}
 
 		public boolean checkForTables() {
 			Boolean hasTables = null;
 			Database databaseHelper = new Database(getBaseContext());
 			db = databaseHelper.getWritableDatabase();
 			Cursor cursor = db.rawQuery("SELECT * FROM calendar", null);
 			if (cursor.getCount() == 0) {
 				hasTables = false;
 				if (cursor.getCount() > 0) {
 					hasTables = true;
 				}
 			}
 			db.close();
 			cursor.close();
 			return hasTables;
 		}
 
 		public HashMap<String, ArrayList<Spanned>> doInBackground(
 				Void... params) {
 			Database databaseHelper = new Database(getBaseContext());
 			db = databaseHelper.getWritableDatabase();
 			HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
 			ArrayList<Spanned> titoli = new ArrayList<Spanned>();
 			ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
 			ArrayList<Spanned> icalarr = new ArrayList<Spanned>();
 			final String URL = "http://www.messedaglia.it/index.php?option=com_jevents&task=modlatest.rss&format=feed&type=rss&Itemid=127&modid=162";
 			String URLE = "http://lookedpath.altervista.org/test.php?id=";
 			final String ITEM = "item";
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
 			String past = date.getString(date.getColumnIndex("calendardate"));
 			date.close();
 			long l = getTimeDiff(past, now);
 			if (l / 10800000 >= 3) {
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
 						String idnp = parser.getValue(e, "link");
 						char[] idnpa = idnp.toCharArray();
 						String icalr = "";
 						int cnt = 0;
 						int lnt = idnp.length();
 						for (int j = lnt - 1; j > 0; j--) {
 							if (idnpa[j] == '/') {
 								cnt++;
 							}
 							if (cnt == 2) {
 								icalr += idnpa[j - 1];
 							}
 							if (cnt > 2) {
 								j = 0;
 							}
 						}
 						// segnalibro
 						char[] icalar = icalr.toCharArray();
 						String ical = "";
 						for (int k = icalr.length() - 2; k > -1; k--) {
 							ical += icalar[k];
 						}
 						values.put("ical", ical);
 						values.put("_id", i);
 						values.put(TITLE, parser.getValue(e, TITLE));
 						values.put(DESC, parser.getValue(e, DESC));
 						map.put("ical", Html.fromHtml(ical));
 						map.put(TITLE, Html.fromHtml(parser.getValue(e, TITLE)));
 						map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
 						titoli.add(Html.fromHtml(parser.getValue(e, TITLE)));
 						descrizioni
 								.add(Html.fromHtml(parser.getValue(e, DESC)));
 						icalarr.add(Html.fromHtml(ical));
 						menuItems.add(map);
 						long newRowId = db.insertWithOnConflict("calendar",
 								null, values, SQLiteDatabase.CONFLICT_REPLACE);
 
 					}
 
 					ContentValues nowdb = new ContentValues();
 					nowdb.put("calendardate", now);
 					long samerow = db.update("lstchk", nowdb, null, null);
 					temhashmap.put("titoli", titoli);
 					temhashmap.put("descrizioni", descrizioni);
 					temhashmap.put("ical", icalarr);
 					return temhashmap;
 
 				}
 			} else {
 				String[] clmndata = { "title", "description", "ical" };
 				String sortOrder = "_id";
 
 				data = db.query("calendar", // The table to query
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
 					map.put("ical", Html.fromHtml(data.getString(data
 							.getColumnIndex("ical"))));
 
 					titoli.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("title"))));
 					descrizioni.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("description"))));
 					icalarr.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("ical"))));
 					// adding HashList to ArrayList
 					menuItems.add(map);
 
 				}
 				data.close();
 				db.close();
 				temhashmap.put("titoli", titoli);
 				temhashmap.put("descrizioni", descrizioni);
 				temhashmap.put("ical", icalarr);
 				return temhashmap;
 
 			}
 
 		}
 
 		public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
 			if (unknhost == true) {
 				mDialog.dismiss();
 				Toast.makeText(calendar.this, R.string.connerr,
 						Toast.LENGTH_LONG).show();
 				Intent main = new Intent(calendar.this, MainActivity.class);
 				main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(main);
 			} else {
 				if (resultmap.size() > 0) {
 					final ArrayList<Spanned> titoli = resultmap.get("titoli");
 					final ArrayList<Spanned> descrizioni = resultmap
 							.get("descrizioni");
 					final ArrayList<Spanned> icalarr = resultmap.get("ical");
 					ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
 							calendar.this, android.R.layout.simple_list_item_1,
 							titoli);
 					setContentView(R.layout.list_item);
 					ListView listView = (ListView) calendar.this
 							.findViewById(android.R.id.list);
 					listView.setAdapter(adapter);
 					listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 						public boolean onItemLongClick(AdapterView<?> parent,
 								View view, int position, long id) {
 							idical = Html.toHtml(icalarr.get(position));
 							int l = idical.length()-5;
 							idical = idical.substring(3, l);
 							new eventparser().execute();
 							return true;
 						}
 					});
 					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 						public void onItemClick(AdapterView<?> parentView,
 								View childView, int position, long id) {
 							if (Html.toHtml(descrizioni.get(position)) != "") {
 								Intent intent = new Intent(calendar.this,
 										ListItemSelectedCalendar.class);
 								intent.putExtra(TITLE,
 										Html.toHtml(titoli.get(position)));
 								intent.putExtra(DESC,
 										Html.toHtml(descrizioni.get(position)));
 								intent.putExtra(ICAL, icalarr.get(position));
 								startActivity(intent);
 							} else {
 								Toast.makeText(calendar.this,
 										R.string.nodescription,
 										Toast.LENGTH_LONG).show();
 							}
 						}
 					});
 				}
 
 				mDialog.dismiss();
 			}
 		}
 	}
 }
