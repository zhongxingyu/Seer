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
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 @SuppressLint("InlinedApi")
 @SuppressWarnings("unused")
 public class calendar extends ListActivity {
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
 		mDialog = new ProgressDialog(calendar.this);
 		super.onCreate(savedInstanceState);
 		new connection().execute();
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
 			if (CheckInternet() == true) {
 				Database databaseHelper = new Database(getBaseContext());
 				SQLiteDatabase db = databaseHelper.getWritableDatabase();
 				ContentValues nowdb = new ContentValues();
 				nowdb.put("calendardate", "2012-02-20 15:00:00");
 				long samerow = db.update("lstchk", nowdb, null, null);
 				db.close();
 				MainActivity.nointernet = "false";
 				new connection().execute();
 			} else {
				Toast.makeText(this, R.string.noconnectionupdatecal,
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
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public static final String ICAL = "ical";
 	public String[] titolim;
 	public String[] descrizionim;
 	ProgressDialog mDialog;
 	public String idical = null;
 
 	@SuppressLint("SimpleDateFormat")
 	public class eventparser extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... params) {
 			String ical = "http://www.messedaglia.it/caltoxml.php?id=" + idical;
 			XMLParser parser = new XMLParser();
 			String xml = parser.getXmlFromUrl(ical);
 			if (xml == "UnknownHostException") {
 			} else {
 				Document doc = parser.getDomElement(xml);
 				NodeList nl = doc.getElementsByTagName("VEVENT");
 
 				String[] dati = { "", "", "", "", "" };
 				Element e = (Element) nl.item(0);
 				dati[0] = parser.getValue(e, "SUMMARY");
 				int l = parser.getValue(e, "DESCRIPTION").length();
 				if (l == 0) {
 					dati[1] = "Nessuna descrizione";
 				} else {
 					dati[1] = parser.getValue(e, "DESCRIPTION").substring(4,
 							l - 3);
 				}
 				dati[2] = parser.getValue(e, "LOCATION");
 				dati[3] = parser.getValue(e, "DTSTART");
 				dati[4] = parser.getValue(e, "DTEND");
 				SimpleDateFormat dateFormat = new SimpleDateFormat(
 						"yyyyMMdd'T'HHmmss");
 				Date fine = null;
 				Date inizio = null;
 				try {
 					fine = dateFormat.parse(dati[4].toString());
 					inizio = dateFormat.parse(dati[3].toString());
 					Intent intent = new Intent(Intent.ACTION_INSERT)
 							.setType("vnd.android.cursor.item/event")
 							.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
 									inizio.getTime())
 							.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
 									fine.getTime())
 							.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,
 									false)
 							.putExtra(Events.TITLE, dati[0])
 							.putExtra(Events.DESCRIPTION, dati[1])
 							.putExtra(Events.EVENT_LOCATION,
 									dati[2] + " A. Messedaglia");
 					startActivity(intent);
 				} catch (java.text.ParseException e1) {
 					e1.printStackTrace();
 				} catch (Exception e2) {
 					e2.printStackTrace();
 					Toast.makeText(calendar.this, R.string.noapilevel,
 							Toast.LENGTH_LONG).show();
 				}
 			}
 			return null;
 		}
 
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.ical:
 			if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 14) {
 				Toast.makeText(calendar.this, R.string.noapilevel,
 						Toast.LENGTH_LONG).show();
 			} else {
 				idical = icalarr.get(info.position).toString();
 				new eventparser().execute();
 			}
 
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	public ArrayList<Spanned> icalarr = new ArrayList<Spanned>();
 
 	public class connection extends
 			AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
 
 		Boolean unknhost = false;
 
 		protected void onCancelled() {
 			Intent main = new Intent(calendar.this, MainActivity.class);
 			main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(main);
 			Toast.makeText(calendar.this, R.string.canceledcalendar,
 					Toast.LENGTH_LONG).show();
 		}
 
 		public void onPreExecute() {
 			if (MainActivity.nointernet == "true") {
 				mDialog = ProgressDialog.show(calendar.this, "Recuperando",
 						"Sto recuperando gli eventi dal database", true, true,
 						new DialogInterface.OnCancelListener() {
 							public void onCancel(DialogInterface dialog) {
 								connection.this.cancel(true);
 							}
 						});
 
 			} else {
 				mDialog = ProgressDialog.show(calendar.this, "Scaricando",
 						"Sto scaricando gli eventi", true, true,
 						new DialogInterface.OnCancelListener() {
 							public void onCancel(DialogInterface dialog) {
 								connection.this.cancel(true);
 							}
 						});
 			}
 			mDialog.show();
 		}
 
 		@SuppressLint("SimpleDateFormat")
 		public HashMap<String, ArrayList<Spanned>> doInBackground(
 				Void... params) {
 			Database databaseHelper = new Database(getBaseContext());
 			db = databaseHelper.getWritableDatabase();
 			HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
 			ArrayList<Spanned> titoli = new ArrayList<Spanned>();
 			ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
 			ArrayList<Spanned> titolib = new ArrayList<Spanned>();
 			final String URL = "http://www.messedaglia.it/index.php?option=com_jevents&task=modlatest.rss&format=feed&type=rss&Itemid=127&modid=162";
 			String URLE = "http://www.messedaglia.it/caltoxml.php?id=";
 			final String ITEM = "item";
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e, e2 = null;
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
 			if (l / 10800000 >= 3 && MainActivity.nointernet != "true") {
 				XMLParser parser = new XMLParser();
 				String xml = parser.getXmlFromUrl(URL);
 				if (xml == "UnknownHostException") {
 					unknhost = true;
 					db.close();
 					return temhashmap;
 				} else {
 					Document doc = parser.getDomElement(xml);
 					NodeList nl;
 					nl = doc.getElementsByTagName(ITEM);
 					ContentValues values = new ContentValues();
 					Boolean ok = false;
 					HashMap<String, Integer> doppioni = new HashMap<String, Integer>();
 					for (int i = 1; i < nl.getLength(); i++) {
 						HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 						e = (Element) nl.item(i);
 						e2 = (Element) nl.item(i - 1);
 						String idnp = parser.getValue(e, "link");
 						String idnp2 = parser.getValue(e2, "link");
 						char[] idnpa = idnp.toCharArray();
 						char[] idnpa2 = idnp2.toCharArray();
 						String icalr = "";
 						String icalr2 = "";
 						int cnt = 0;
 						int lnt = idnp.length();
 						for (int j = lnt - 1; j > 0; j--) {
 							if (idnpa[j] == '/') {
 								cnt++;
 							}
 							if (cnt == 2) {
 								icalr += idnpa[j - 1];
 								icalr2 += idnpa2[j - 1];
 							}
 							if (cnt > 2) {
 								j = 0;
 							}
 						}
 						char[] icalar = icalr.toCharArray();
 						char[] icalar2 = icalr2.toCharArray();
 						String ical = "";
 						String ical2 = "";
 						for (int k = icalr.length() - 2; k > -1; k--) {
 							ical += icalar[k];
 							ical2 += icalar2[k];
 						}
 						values.put("ical", ical);
 						values.put("_id", i);
 						map.put("ical", Html.fromHtml(ical));
 						if (doppioni.containsKey(ical)) {
 							int d = doppioni.get(ical);
 							doppioni.remove(ical);
 							d++;
 							doppioni.put(ical, d++);
 						} else {
 							doppioni.put(ical, 0);
 						}
 						String tito = parser.getValue(e, TITLE);
 						int n = tito.charAt(0);
 						int n2 = tito.charAt(1);
 						StringBuffer buf = new StringBuffer(tito);
 
 						switch (tito.charAt(3) + tito.charAt(4)
 								+ tito.charAt(5)) {
 						case 282:// GEN
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 59)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 11));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'F');
 									buf.setCharAt(4, 'e');
 									buf.setCharAt(5, 'b');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 1));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'F');
 										buf.setCharAt(4, 'e');
 										buf.setCharAt(5, 'b');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 269: // FEB
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 50
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 66)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 18));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'M');
 									buf.setCharAt(4, 'a');
 									buf.setCharAt(5, 'r');
 								}
 							} else {
 								if (n == 50) {
 									if (n2 + doppioni.get(ical) <= 56) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 8));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'M');
 										buf.setCharAt(4, 'a');
 										buf.setCharAt(5, 'r');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 288: // Mar
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 59)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 11));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'A');
 									buf.setCharAt(4, 'p');
 									buf.setCharAt(5, 'r');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 1));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'A');
 										buf.setCharAt(4, 'p');
 										buf.setCharAt(5, 'r');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 291: // Apr
 							if (n2 + doppioni.get(ical) >= 58) {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical) - 10));
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) == 58)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 								} else {
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'M');
 									buf.setCharAt(4, 'a');
 									buf.setCharAt(5, 'g');
 								}
 							} else {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical)));
 							}
 							break;
 						case 277: // Mag
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 59)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 11));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'G');
 									buf.setCharAt(4, 'i');
 									buf.setCharAt(5, 'u');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 1));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'G');
 										buf.setCharAt(4, 'i');
 										buf.setCharAt(5, 'u');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 293: // Giu
 							if (n2 + doppioni.get(ical) >= 58) {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical) - 10));
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) == 58)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 								} else {
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'L');
 									buf.setCharAt(4, 'u');
 									buf.setCharAt(5, 'g');
 								}
 							} else {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical)));
 							}
 							break;
 						case 296: // Lug
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 59)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 11));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'A');
 									buf.setCharAt(4, 'g');
 									buf.setCharAt(5, 'g');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 1));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'A');
 										buf.setCharAt(4, 'g');
 										buf.setCharAt(5, 'o');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 279: // Ago
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 59)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 11));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'S');
 									buf.setCharAt(4, 'e');
 									buf.setCharAt(5, 't');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 1));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'S');
 										buf.setCharAt(4, 'e');
 										buf.setCharAt(5, 't');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 300: // Set
 							if (n2 + doppioni.get(ical) >= 58) {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical) - 10));
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) == 58)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 								} else {
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'O');
 									buf.setCharAt(4, 't');
 									buf.setCharAt(5, 't');
 								}
 							} else {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical)));
 							}
 							break;
 						case 311: // Ott
 							if (n2 + doppioni.get(ical) == 58) {
 								if (n + 1 <= 51) {
 									buf.setCharAt(0, (char) (n + 1));
 									buf.setCharAt(1, '0');
 								} else {
 									buf.setCharAt(1, '1');
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'N');
 									buf.setCharAt(4, 'o');
 									buf.setCharAt(5, 'v');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(1, '1');
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'N');
 										buf.setCharAt(4, 'o');
 										buf.setCharAt(5, 'v');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						case 307: // Nov
 							if (n2 + doppioni.get(ical) >= 58) {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical) - 10));
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) == 58)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 								} else {
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'D');
 									buf.setCharAt(4, 'i');
 									buf.setCharAt(5, 'c');
 								}
 							} else {
 								buf.setCharAt(1,
 										(char) (n2 + doppioni.get(ical)));
 							}
 							break;
 						case 272: // Dic
 							if (n2 + doppioni.get(ical) >= 58) {
 								if (n + 1 < 51
 										|| (n + 1 == 51 && n2
 												+ doppioni.get(ical) <= 59)) {
 									buf.setCharAt(0, (char) (n + 1));
 
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 10));
 
 								} else {
 									buf.setCharAt(
 											1,
 											(char) (n2 + doppioni.get(ical) - 11));
 									buf.setCharAt(0, '0');
 									buf.setCharAt(3, 'G');
 									buf.setCharAt(4, 'e');
 									buf.setCharAt(5, 'n');
 								}
 							} else {
 								if (n == 51) {
 									if (n2 + doppioni.get(ical) <= 49) {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical)));
 									} else {
 										buf.setCharAt(
 												1,
 												(char) (n2 + doppioni.get(ical) - 1));
 										buf.setCharAt(0, '0');
 										buf.setCharAt(3, 'G');
 										buf.setCharAt(4, 'e');
 										buf.setCharAt(5, 'n');
 									}
 								} else {
 									buf.setCharAt(1,
 											(char) (n2 + doppioni.get(ical)));
 								}
 							}
 							break;
 						}
 
 						tito = buf.toString();
 						values.put(TITLE, tito);
 						map.put(TITLE, Html.fromHtml(tito));
 						titoli.add(Html.fromHtml(tito));
 						titolib.add(Html.fromHtml("<b>" + tito + "</b>"));
 
 						values.put(DESC, parser.getValue(e, DESC));
 						values.put("titleb", "<b>" + tito + "</b>");
 						map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
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
 					temhashmap.put("titolib", titolib);
 					return temhashmap;
 
 				}
 
 			} else {
 				String[] clmndata = { "title", "description", "titleb", "ical" };
 				String sortOrder = "_id";
 
 				data = db.query("calendar", // The table to query
 						clmndata,
 						// The columns to return
 						null, // The columns for the WHERE clause
 						null, // The values for the WHERE clause
 						null, // don't group the rows
 						null, // don't filter by row groups
 						sortOrder // The sortorder
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
 					titolib.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("titleb"))));
 					menuItems.add(map);
 
 				}
 				data.close();
 				db.close();
 				temhashmap.put("titoli", titoli);
 				temhashmap.put("descrizioni", descrizioni);
 				temhashmap.put("ical", icalarr);
 				temhashmap.put("titolib", titolib);
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
 					final ArrayList<Spanned> titolib = resultmap.get("titolib");
 					final ArrayList<Spanned> icalarr = resultmap.get("ical");
 					ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
 							calendar.this, android.R.layout.simple_list_item_1,
 							titolib);
 					setContentView(R.layout.list_item);
 					ListView listView = (ListView) calendar.this
 							.findViewById(android.R.id.list);
 					listView.setAdapter(adapter);
 
 					registerForContextMenu(findViewById(android.R.id.list));
 
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
 								intent.putExtra(ICAL,
 										Html.toHtml(icalarr.get(position)));
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
