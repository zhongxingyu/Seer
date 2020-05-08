 package com.messedagliavr.messeapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.messedagliavr.messeapp.news.connection;
 
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
 import android.text.Html;
 import android.text.Spanned;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class calendar extends ListActivity {
 
 	@Override
 	public void onBackPressed() {
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
			nowdb.put("newscalendar", "2012-02-20 15:00:00");
 			long samerow= db.update("lstchk", nowdb, null,null);
 			new connection().execute();
 			break;
 		}
 		return true;
 	}
 	
 	private Long getTimeDiff(String time,String curTime) throws ParseException
 	{ 
 	    Date curDate = null ;
 	    Date oldDate = null ; 
 	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    try {
 			curDate =(Date)formatter.parse(curTime);
 			oldDate = (Date)formatter.parse(time);
 		} catch (java.text.ParseException e) {
 			e.printStackTrace();
 		}
 	    long oldMillis=oldDate.getTime();
 	    long curMillis=curDate.getTime();
 	    long diff=curMillis-oldMillis;
 	    return diff;
 	}
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public String[] titolim;
 	public String[] descrizionim;
 	ProgressDialog mDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		mDialog = new ProgressDialog(calendar.this);
 		super.onCreate(savedInstanceState);
 		new connection().execute();
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
 
 		public HashMap<String, ArrayList<Spanned>> doInBackground(
 				Void... params) {
 			Database databaseHelper = new Database(getBaseContext());
 			SQLiteDatabase db = databaseHelper.getWritableDatabase();
 			HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
 			ArrayList<Spanned> titoli = new ArrayList<Spanned>();
 			ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
 			final String URL = "http://www.messedaglia.it/index.php?option=com_jevents&task=modlatest.rss&format=feed&type=rss&Itemid=127&modid=162";
 			final String ITEM = "item";
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e = null;
 			ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
 			String[] outdated = { "newsdate" , "calendardate"};
 	        Calendar c = Calendar.getInstance();
 	        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	        String now = df.format(c.getTime());
 	        Cursor date = db.query(
 	        		"lstchk", // The table to query
 					outdated, // The columns to return
 					null, // The columns for the WHERE clause
 					null, // The values for the WHERE clause
 					null, // don't group the rows
 					null, // don't filter by row groups
 					null // The sort order
 					);
 	        date.moveToFirst();
 	        String past = date.getString(date.getColumnIndex("calendardate"));
 	        long l = getTimeDiff(past,now);
 	        if (l/10800000>=3){
 				XMLParser parser = new XMLParser();
 				String xml = parser.getXmlFromUrl(URL); // getting XML
 				Document doc = parser.getDomElement(xml); // getting DOM element
 				NodeList nl = doc.getElementsByTagName(ITEM);
 				ContentValues values = new ContentValues();
 				for (int i = 0; i < nl.getLength(); i++) {
 					HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 					e = (Element) nl.item(i);
 					values.put("id", i);
 					values.put(TITLE, parser.getValue(e, TITLE));
 					values.put(DESC, parser.getValue(e, DESC));
 					map.put(TITLE, Html.fromHtml(parser.getValue(e, TITLE)));
 					map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
 
 					titoli.add(Html.fromHtml(parser.getValue(e, TITLE)));
 					descrizioni.add(Html.fromHtml(parser.getValue(e, DESC)));
 					// adding HashList to ArrayList
 					menuItems.add(map);
 					long newRowId = db.insert("calendar", null, values);
 
 				}
 				ContentValues nowdb = new ContentValues();
 				nowdb.put("calendardate", now);
 				long samerow= db.update("lstchk", nowdb, null,null);
 				temhashmap.put("titoli", titoli);
 				temhashmap.put("descrizioni", descrizioni);
 				return temhashmap;
 	        	
 	        } else {
 	        	String[] clmndata = { "title", "description" };
 				String sortOrder = "id";
 
 				Cursor data = db.query("calendar", // The table to query
 						clmndata, // The columns to return
 						null, // The columns for the WHERE clause
 						null, // The values for the WHERE clause
 						null, // don't group the rows
 						null, // don't filter by row groups
 						sortOrder // The sort order
 						);
 				
 				for (data.move(0); data.moveToNext(); data.isAfterLast()) {
 					HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 					map.put(TITLE, Html.fromHtml(data.getString(data.getColumnIndex("title"))));
 					map.put(DESC, Html.fromHtml(data.getString(data.getColumnIndex("description"))));
 
 					titoli.add(Html.fromHtml(data.getString(data.getColumnIndex("title"))));
 					descrizioni.add(Html.fromHtml(data.getString(data.getColumnIndex("description"))));
 					// adding HashList to ArrayList
 					menuItems.add(map);
 
 				}
 				temhashmap.put("titoli", titoli);
 				temhashmap.put("descrizioni", descrizioni);
 				return temhashmap;
 	        	
 	        }
 		}
 
 		public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
 
 			if (resultmap.size() > 0) {
 				final ArrayList<Spanned> titoli = resultmap.get("titoli");
 				final ArrayList<Spanned> descrizioni = resultmap
 						.get("descrizioni");
 				ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
 						calendar.this, android.R.layout.simple_list_item_1, titoli);
 				setContentView(R.layout.list_item);
 				ListView listView = (ListView) calendar.this
 						.findViewById(android.R.id.list);
 				listView.setAdapter(adapter);
 				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					public void onItemClick(AdapterView<?> parentView,
 							View childView, int position, long id) {
 						if (Html.toHtml(descrizioni.get(position))!="") {
 							Intent intent = new Intent(calendar.this,
 									ListItemSelectedCalendar.class);
 							intent.putExtra(TITLE,
 									Html.toHtml(titoli.get(position)));
 							intent.putExtra(DESC,
 									Html.toHtml(descrizioni.get(position)));
 						startActivity(intent);
 						} else {
 							Toast.makeText(calendar.this, R.string.nodescription,
 									Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 			}
 			mDialog.dismiss();
 		}
 	}
 }
