 package com.lsg.app;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Calendar;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TimeTable extends Activity implements SelectedCallback {
 	public static class TimetableAdapter extends CursorAdapter {
 		private SQLiteDatabase myDB;
 
 		class TimetableItem {
 			public LinearLayout lay;
 			public TextView timetable_day;
 			public TextView timetable_hour;
 			public TextView timetable_subject;
 			public TextView timetable_teacher;
 			public TextView timetable_room;
 		}
 
 		public TimetableAdapter(Context context, Cursor c) {
 			super(context, c, false);
 			myDB = context.openOrCreateDatabase(Functions.DB_NAME,
 					Context.MODE_PRIVATE, null);
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater
 					.inflate(R.layout.timetable_item, null, true);
 			TimetableItem holder = new TimetableItem();
 			holder.lay = (LinearLayout) rowView
 					.findViewById(R.id.timetable_lay);
 			holder.timetable_day = (TextView) rowView
 					.findViewById(R.id.timetable_day);
 			holder.timetable_hour = (TextView) rowView
 					.findViewById(R.id.timetable_hour);
 			holder.timetable_subject = (TextView) rowView
 					.findViewById(R.id.timetable_subject);
 			holder.timetable_teacher = (TextView) rowView
 					.findViewById(R.id.timetable_teacher);
 			holder.timetable_room = (TextView) rowView
 					.findViewById(R.id.timetable_room);
 			if (Functions.getSDK() < 11)
 				holder.timetable_hour
 						.setBackgroundResource(R.layout.divider_gradient);
 			rowView.setTag(holder);
 			return rowView;
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			TimetableItem holder = (TimetableItem) view.getTag();
 			/*
 			 * int position = cursor.getPosition(); if(position == 0) {
 			 * holder.timetable_day.setVisibility(View.VISIBLE);
 			 * holder.timetable_day
 			 * .setText(context.getResources().getStringArray
 			 * (R.array.days)[cursor
 			 * .getInt(cursor.getColumnIndex(Functions.DB_DAY))]); } else
 			 */
 			holder.timetable_day.setVisibility(View.GONE);
 			int hour = cursor.getInt(cursor.getColumnIndex(Functions.DB_HOUR)) + 1;
 			String when = Integer.valueOf(hour).toString();
 			int i = 1;
 			int length = cursor.getInt(cursor
 					.getColumnIndex(Functions.DB_LENGTH));
 			while (i < length) {
 				when += ", " + Integer.valueOf(hour + i).toString();
 				i++;
 			}
 			String rawfach = cursor.getString(cursor
 					.getColumnIndex(Functions.DB_RAW_FACH));
 			String lehrer = cursor.getString(cursor
 					.getColumnIndex(Functions.DB_LEHRER));
 			Cursor c = myDB.query(Functions.DB_VPLAN_TABLE, new String[] {},
 					Functions.DB_STUNDE + "=? AND " + Functions.DB_RAW_FACH
 							+ "=? AND " + Functions.DB_RAW_LEHRER + "=?",
 					new String[] { Integer.valueOf(hour).toString(), rawfach,
 							lehrer }, null, null, null);
 			if (c.getCount() > 0)
 				holder.lay.setBackgroundResource(R.layout.background_info);
 			else
 				holder.lay.setBackgroundResource(R.layout.background);
 
 			holder.timetable_hour.setText(when + ". "
 					+ context.getString(R.string.hour));
 			holder.timetable_subject.setText(cursor.getString(cursor
 					.getColumnIndex(Functions.DB_FACH)));
 			holder.timetable_teacher.setText(cursor.getString(cursor
 					.getColumnIndex(Functions.DB_LEHRER)));
 			holder.timetable_room
 					.setText(context.getString(R.string.room)
 							+ " "
 							+ cursor.getString(cursor
 									.getColumnIndex(Functions.DB_RAUM)));
 		}
 
 		public void close() {
 			myDB.close();
 		}
 	}
 
 	public class TimeTableViewPagerAdapter extends PagerAdapter implements
 			PagerTitles {
 		private String[] exclude_subjects = new String[6];
 		private final SQLiteDatabase myDB;
 		public Cursor timetable_monday;
 		public Cursor timetable_tuesday;
 		public Cursor timetable_wednesday;
 		public Cursor timetable_thursday;
 		public Cursor timetable_friday;
 		private TimetableAdapter timetableadap_monday;
 		private TimetableAdapter timetableadap_tuesday;
 		private TimetableAdapter timetableadap_wednesday;
 		private TimetableAdapter timetableadap_thursday;
 		private TimetableAdapter timetableadap_friday;
 		private final Context context;
 		private final SharedPreferences prefs;
 		private String[] titles = new String[5];
 		private String klasse;
 		private boolean ownClass;
 
 		public TimeTableViewPagerAdapter(TimeTable act) {
 			prefs = PreferenceManager.getDefaultSharedPreferences(act);
 			setClass("", true);
 			context = (Context) act;
 			titles = getResources().getStringArray(R.array.days);
 
 			myDB = context.openOrCreateDatabase(Functions.DB_NAME,
 					Context.MODE_PRIVATE, null);
 			timetableadap_monday = new TimetableAdapter(context,
 					timetable_monday);
 			timetableadap_tuesday = new TimetableAdapter(context,
 					timetable_tuesday);
 			timetableadap_wednesday = new TimetableAdapter(context,
 					timetable_wednesday);
 			timetableadap_thursday = new TimetableAdapter(context,
 					timetable_thursday);
 			timetableadap_friday = new TimetableAdapter(context,
 					timetable_friday);
 
 			SQLiteStatement num_rows = myDB
 					.compileStatement("SELECT COUNT(*) FROM "
 							+ Functions.DB_TIME_TABLE);
 			long count = num_rows.simpleQueryForLong();
 			if (count == 0)
 				act.updateTimeTable();
 			num_rows.close();
 			updateCursor();
 		}
 
 		@Override
 		public int getCount() {
 			return 5;
 		}
 
 		@Override
 		public String getTitle(int pos) {
 			return titles[pos];
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return titles[position];
 		}
 
 		@Override
 		public Object instantiateItem(View pager, int position) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list,
 					null);
 			ListView lv = (ListView) lay.findViewById(android.R.id.list);
 			switch (position) {
 			case 0:
 				lv.setAdapter(timetableadap_monday);
 				break;
 			case 1:
 				lv.setAdapter(timetableadap_tuesday);
 				break;
 			case 2:
 				lv.setAdapter(timetableadap_wednesday);
 				break;
 			case 3:
 				lv.setAdapter(timetableadap_thursday);
 				break;
 			case 4:
 				lv.setAdapter(timetableadap_friday);
 				break;
 			default:
 				lv.setAdapter(timetableadap_monday);
 				break;
 			}
 			lv.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					Cursor c = myDB
 							.query(Functions.DB_TIME_TABLE, new String[] {
 									Functions.DB_RAW_FACH, Functions.DB_HOUR,
 									Functions.DB_LEHRER }, Functions.DB_ROWID
 									+ "=?", new String[] { Long.valueOf(id)
 									.toString() }, null, null, null);
 					c.moveToFirst();
 					String hour = Integer.valueOf(
 							c.getInt(c.getColumnIndex(Functions.DB_HOUR)) + 1)
 							.toString();
 					Cursor d = myDB.query(
 							Functions.DB_VPLAN_TABLE,
 							new String[] { Functions.DB_KLASSE,
 									Functions.DB_STUNDE,
 									Functions.DB_VERTRETUNGSTEXT,
 									Functions.DB_LEHRER, Functions.DB_FACH,
 									Functions.DB_ART },
 							Functions.DB_RAW_FACH + "=? AND "
 									+ Functions.DB_STUNDE + "=? AND "
 									+ Functions.DB_RAW_LEHRER + "=?",
 							new String[] {
 									c.getString(c
 											.getColumnIndex(Functions.DB_RAW_FACH)),
 									hour,
 									c.getString(c
 											.getColumnIndex(Functions.DB_LEHRER)) },
 							null, null, null);
 					d.moveToFirst();
 					if (d.getCount() > 0) {
 						String vtext = (!(d.getString(d
 								.getColumnIndex(Functions.DB_VERTRETUNGSTEXT)))
 								.equals("null")) ? d.getString(d
 								.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))
 								+ "\n" : "";
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								context);
 						builder.setTitle(
 								d.getString(d
 										.getColumnIndex(Functions.DB_KLASSE)))
 								.setMessage(
 										d.getString(d
 												.getColumnIndex(Functions.DB_FACH))
 												+ " / "
 												+ d.getString(d
 														.getColumnIndex(Functions.DB_STUNDE))
 												+ ". "
 												+ context
 														.getString(R.string.hour)
 												+ "\n"
 												+ vtext
 												+ d.getString(d
 														.getColumnIndex(Functions.DB_ART))
 												+ " "
 												+ context
 														.getString(R.string.at)
 												+ " "
 												+ d.getString(d
 														.getColumnIndex(Functions.DB_LEHRER)))
 								.setCancelable(true)
 								.setNeutralButton(
 										context.getString(R.string.ok),
 										new DialogInterface.OnClickListener() {
 											public void onClick(
 													DialogInterface dialog,
 													int id) {
 												dialog.cancel();
 											}
 										});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}
 				}
 			});
 			((ViewPager) pager).addView(lay, 0);
 			return lay;
 		}
 
 		public void updateExclude() {
 			if (ownClass) {
 				exclude_subjects[1] = (prefs.getString(Functions.GENDER, "")
 						.equals("m")) ? "Sw" : "Sm";
 				if (prefs.getString(Functions.RELIGION, "").equals(
 						Functions.KATHOLISCH)) {
 					exclude_subjects[2] = Functions.EVANGELISCH;
 					exclude_subjects[3] = Functions.ETHIK;
 				} else if (prefs.getString(Functions.RELIGION, "").equals(
 						Functions.EVANGELISCH)) {
 					exclude_subjects[2] = Functions.KATHOLISCH;
 					exclude_subjects[3] = Functions.ETHIK;
 				} else {
 					exclude_subjects[2] = Functions.KATHOLISCH;
 					exclude_subjects[3] = Functions.EVANGELISCH;
 				}
 			}
 			else {
 				exclude_subjects[1] = "%";
 				exclude_subjects[2] = "%";
 				exclude_subjects[3] = "%";
 			}
 		}
 
 		public void setClass(String klasse, boolean ownClass) {
 			this.klasse = klasse;
 			this.ownClass = ownClass;
 			if(ownClass)
 				this.klasse = prefs.getString(Functions.FULL_CLASS, "null");
 			updateExclude();
 		}
 		public void updateCursor() {
 			exclude_subjects[0] = "0";
 			exclude_subjects[4] = (ownClass) ? "1" : "%";
 			if (ownClass)
 				exclude_subjects[5] = "%" + klasse.substring(0, 2) + "%"
 						+ klasse.substring(2, 3) + "%";
 			else
 				exclude_subjects[5] = klasse;
 			String wherecond = Functions.DB_DAY + "=? AND  "
 					+ Functions.DB_RAW_FACH + " != ? AND "
 					+ Functions.DB_RAW_FACH + " != ? AND "
 					+ Functions.DB_RAW_FACH + " != ? AND "
 					+ Functions.DB_DISABLED + " != ? AND " + Functions.DB_CLASS
 					+ " LIKE ?";
 			timetable_monday = myDB.query(Functions.DB_TIME_TABLE,
 					new String[] { Functions.DB_ROWID, Functions.DB_LEHRER,
 							Functions.DB_FACH, Functions.DB_RAUM,
 							Functions.DB_LENGTH, Functions.DB_HOUR,
 							Functions.DB_DAY, Functions.DB_RAW_FACH },
 					wherecond, exclude_subjects, null, null, null);
 			timetableadap_monday.changeCursor(timetable_monday);
 			exclude_subjects[0] = "1";
 			timetable_tuesday = myDB.query(Functions.DB_TIME_TABLE,
 					new String[] { Functions.DB_ROWID, Functions.DB_LEHRER,
 							Functions.DB_FACH, Functions.DB_RAUM,
 							Functions.DB_LENGTH, Functions.DB_HOUR,
 							Functions.DB_DAY, Functions.DB_RAW_FACH },
 					wherecond, exclude_subjects, null, null, null);
 			timetableadap_tuesday.changeCursor(timetable_tuesday);
 
 			exclude_subjects[0] = "2";
 			timetable_wednesday = myDB.query(Functions.DB_TIME_TABLE,
 					new String[] { Functions.DB_ROWID, Functions.DB_LEHRER,
 							Functions.DB_FACH, Functions.DB_RAUM,
 							Functions.DB_LENGTH, Functions.DB_HOUR,
 							Functions.DB_DAY, Functions.DB_RAW_FACH },
 					wherecond, exclude_subjects, null, null, null);
 			timetableadap_wednesday.changeCursor(timetable_wednesday);
 
 			exclude_subjects[0] = "3";
 			timetable_thursday = myDB.query(Functions.DB_TIME_TABLE,
 					new String[] { Functions.DB_ROWID, Functions.DB_LEHRER,
 							Functions.DB_FACH, Functions.DB_RAUM,
 							Functions.DB_LENGTH, Functions.DB_HOUR,
 							Functions.DB_DAY, Functions.DB_RAW_FACH },
 					wherecond, exclude_subjects, null, null, null);
 			timetableadap_thursday.changeCursor(timetable_thursday);
 
 			exclude_subjects[0] = "4";
 			timetable_friday = myDB.query(Functions.DB_TIME_TABLE,
 					new String[] { Functions.DB_ROWID, Functions.DB_LEHRER,
 							Functions.DB_FACH, Functions.DB_RAUM,
 							Functions.DB_LENGTH, Functions.DB_HOUR,
 							Functions.DB_DAY, Functions.DB_RAW_FACH },
 					wherecond, exclude_subjects, null, null, Functions.DB_HOUR);
 			timetableadap_friday.changeCursor(timetable_friday);
 		}
 
 		public void closeCursorsDB() {
 			timetable_monday.close();
 			timetable_tuesday.close();
 			timetable_wednesday.close();
 			timetable_thursday.close();
 			timetable_friday.close();
 			myDB.close();
 		}
 
 		@Override
 		public void destroyItem(View pager, int position, Object view) {
 			((ViewPager) pager).removeView((View) view);
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object) {
 			return view.equals(object);
 		}
 
 		@Override
 		public void finishUpdate(View view) {
 		}
 
 		@Override
 		public void restoreState(Parcelable p, ClassLoader c) {
 		}
 
 		@Override
 		public Parcelable saveState() {
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View view) {
 		}
 	}
 
 	public static class TimeTableUpdater {
 		private Context context;
 
 		TimeTableUpdater(Context c) {
 			context = c;
 		}
 
 		public String[] update() {
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(context);
 			String add = "";
 			try {
 				add = "&"
 						+ URLEncoder.encode("date", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString("timetable_date", ""), "UTF-8")
 						+ "&"
 						+ URLEncoder.encode("time", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString("timetable_time", ""), "UTF-8")
 						+ "&"
 						+ URLEncoder.encode("class", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString(Functions.FULL_CLASS, ""),
 								"UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				Log.d("encoding", e.getMessage());
 			}
 			String get = Functions.getData(Functions.TIMETABLE_URL, context,
 					true, add);
 			if (!get.equals("networkerror") && !get.equals("loginerror")
 					&& !get.equals("noact")) {
 				try {
 					JSONArray classes = new JSONArray(get);
 					SQLiteDatabase myDB = context.openOrCreateDatabase(
 							Functions.DB_NAME, Context.MODE_PRIVATE, null);
 					// clear timetable
 					myDB.delete(Functions.DB_TIME_TABLE, null, null);
 					// clear headers
 					myDB.delete(Functions.DB_TIME_TABLE_HEADERS, null, null);
 					for (int i = 0; i < classes.length(); i++) {
 						JSONArray one_class = classes.getJSONArray(i);
 						JSONObject class_info = one_class.getJSONObject(0);
 						String date = class_info.getString("date");
 						String time = class_info.getString("time");
 						String one = class_info.getString("one");
 						String two = class_info.getString("two");
 						String klasse = class_info.getString("klasse");
 						SharedPreferences.Editor edit = prefs.edit();
 						edit.putString("timetable_date", date);
 						edit.putString("timetable_time", time);
 						ContentValues headerval = new ContentValues();
 						headerval.put(Functions.DB_TEACHER, one);
 						headerval.put(Functions.DB_SECOND_TEACHER, two);
 						headerval.put(Functions.DB_KLASSE, klasse);
 						myDB.insert(Functions.DB_TIME_TABLE_HEADERS, null, headerval);
 						edit.putString("timetable_one", one);
 						edit.putString("timetable_two", two);
 						edit.putString("timetable_klasse", klasse);
 						edit.commit();
 						for(int ii = 1; ii < one_class.length(); ii++) {
 							JSONObject jObject = one_class.getJSONObject(ii);
 							//Log.d("json", jObject.toString());
 							ContentValues values = new ContentValues();
 							values.put(Functions.DB_LEHRER,
 									jObject.getString("teacher"));
 							values.put(Functions.DB_FACH,
 									jObject.getString("subject"));
 							values.put(Functions.DB_RAW_FACH,
 									jObject.getString("rawsubject"));
 							values.put(Functions.DB_RAUM,
 									jObject.getString("room"));
 							values.put(Functions.DB_LENGTH,
 									jObject.getInt("length"));
 							values.put(Functions.DB_DAY, jObject.getInt("day"));
 							values.put(Functions.DB_HOUR,
 									jObject.getInt("hour"));
 							values.put(Functions.DB_CLASS, jObject.getString("class"));
 							values.put(Functions.DB_RAW_LEHRER, jObject.getString("rawteacher"));
 							Cursor c = myDB.query(Functions.EXCLUDE_TABLE,
 									new String[] { Functions.DB_ROWID },
 									Functions.DB_TEACHER + "=? AND "
 											+ Functions.DB_RAW_FACH
 											+ "=? AND "
 											+ Functions.DB_HOUR
 											+ "=? AND "
 											+ Functions.DB_DAY + "=?",
 									new String[] {
 											values.getAsString(Functions.DB_RAW_LEHRER),
 											values.getAsString(Functions.DB_RAW_FACH),
 											values.getAsString(Functions.DB_HOUR),
 											values.getAsString(Functions.DB_DAY) },
 									null, null, null);
 					c.moveToFirst();
 					if(c.getCount() > 0) {
 						values.put(Functions.DB_DISABLED, 1);
 					} else
 						values.put(Functions.DB_DISABLED, 2);
 							myDB.insert(Functions.DB_TIME_TABLE, null, values);
 						}
 					}
 					myDB.close();
 				} catch (JSONException e) {
 					Log.d("json", e.getMessage());
 					return new String[] { "json",
 							context.getString(R.string.jsonerror) };
 				}
 			} else if (get.equals("networkerror")) {
 				return new String[] { "networkerror",
 						context.getString(R.string.networkerror) };
 			} else if (get.equals("loginerror"))
 				return new String[] { "loginerror",
 						context.getString(R.string.loginerror) };
 			return new String[] { "success", "" };
 		}
 	}
 
 	public class TimeTableUpdateTask extends AsyncTask<Void, Void, String[]> {
 		protected void onPreExecute() {
 			super.onPreExecute();
 			Functions.lockRotation(TimeTable.this);
 			loading = ProgressDialog.show(TimeTable.this, "",
 					getString(R.string.loading_timetable));
 		}
 
 		@Override
 		protected String[] doInBackground(Void... params) {
 			TimeTableUpdater upd = new TimeTableUpdater(TimeTable.this);
 			return upd.update();
 		}
 
 		protected void onPostExecute(String[] res) {
 			loading.cancel();
 			if (!res[0].equals("success"))
 				Toast.makeText(TimeTable.this, res[1], Toast.LENGTH_LONG)
 						.show();
 			if (res[0].equals("loginerror")) {
 				Intent intent;
 				if (Functions.getSDK() >= 11)
 					intent = new Intent(TimeTable.this, SettingsAdvanced.class);
 				else
 					intent = new Intent(TimeTable.this, Settings.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 				TimeTable.this.startActivity(intent);
 			}
 			viewpageradap.updateCursor();
 			Functions.unlockRotation(TimeTable.this);
 		}
 	}
 
 	private ProgressDialog loading;
 	private TimeTableViewPagerAdapter viewpageradap;
 	private ViewPager pager;
 	private SlideMenu slidemenu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Functions.setupDB(this);
 		super.onCreate(savedInstanceState);
 		if(!((SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this)).getBoolean(Functions.IS_LOGGED_IN, false)) {
 			if(!((SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this)).getString("username", "null").equals("null")) {
 				Toast.makeText(this, getString(R.string.setup_assistant_opening), Toast.LENGTH_LONG).show();
 				startActivity(new Intent(TimeTable.this, SetupAssistant.class));
 			} else {
 				Toast.makeText(this, getString(R.string.run_setup_assistant),
 						Toast.LENGTH_LONG).show();
 				startActivity(new Intent(TimeTable.this, Events.class));
 			}
 			Functions.init(this);
 			this.finish();
 			return;
 		}
 		setTitle(R.string.timetable);
 		Functions.setTheme(false, true, this);
 		getWindow().setBackgroundDrawableResource(R.layout.background);
 		setContentView(R.layout.viewpager);
 		viewpageradap = new TimeTableViewPagerAdapter(this);
 		pager = (ViewPager) findViewById(R.id.viewpager);
 		pager.setAdapter(viewpageradap);
 		// pager.setOnPageChangeListener(this);
 		pager.setPageMargin(Functions.dpToPx(40, this));
 		pager.setPageMarginDrawable(R.layout.viewpager_margin);
 
 		// get current day
 		Calendar cal = Calendar.getInstance();
 		int day;
 		switch (cal.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.MONDAY:
 			day = 0;
 			break;
 		case Calendar.TUESDAY:
 			day = 1;
 			break;
 		case Calendar.WEDNESDAY:
 			day = 2;
 			break;
 		case Calendar.THURSDAY:
 			day = 3;
 			break;
 		case Calendar.FRIDAY:
 			day = 4;
 			break;
 		default:
 			day = 0;
 			break;
 		}
 		pager.setCurrentItem(day, true);
 		slidemenu = new SlideMenu(this, TimeTable.class);
 		slidemenu.checkEnabled();
 		if(Functions.getSDK() >= 11) {
 			AdvancedWrapper adv = new AdvancedWrapper();
 			adv.dropDownNav(this, R.array.timetable_actions, this, 0);
 		}
 	}
 	public void showMine() {
 		viewpageradap.setClass("", true);
		viewpageradap.updateCursor();
 	}
 	public void showClasses() {
 		SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
 		final Cursor c = myDB.query(Functions.DB_TIME_TABLE_HEADERS, new String[] {Functions.DB_ROWID, Functions.DB_KLASSE}, null, null, null, null, null);
 		AlertDialog.Builder builder = new AlertDialog.Builder(TimeTable.this);
 		builder.setTitle(R.string.select_class);
 		builder.setCursor(c, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int item) {
 		    	c.moveToPosition(item);
 		        viewpageradap.setClass(c.getString(c.getColumnIndex(Functions.DB_KLASSE)), false);
 		        ((TextView) findViewById(R.id.footer_text)).setText(c.getString(c.getColumnIndex(Functions.DB_KLASSE)));
 		        viewpageradap.updateCursor();
 		    }}, Functions.DB_KLASSE);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.timetable, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			updateTimeTable();
 			return true;
 		case android.R.id.home:
 			slidemenu.show();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void updateTimeTable() {
 		TimeTableUpdateTask upd = new TimeTableUpdateTask();
 		upd.execute();
 	}
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		try {
 			viewpageradap.closeCursorsDB();
 		} catch(NullPointerException e) {
 			//viewpager not yet initialized
 		}
 	}
 	private int selPos;
 	private boolean suppressSelect = false;
 	@Override
 	public boolean selected(int position, long id) {
 		selPos = position;
 		if(suppressSelect) {
 			suppressSelect = false;
 			return true;
 		}
 		switch(position) {
 		case 0:
 			((TextView) findViewById(R.id.footer_text)).setVisibility(View.GONE);
 			showMine();
 			break;
 		case 1:
 			((TextView) findViewById(R.id.footer_text)).setVisibility(View.VISIBLE);
 			showClasses();
 			break;
 			default:
 				showMine();
 				break;
 		}
 		return false;
 	}
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 	  super.onSaveInstanceState(savedInstanceState);
 	  savedInstanceState.putInt("navlistselected", selPos);
 	  savedInstanceState.putBoolean("ownclass", viewpageradap.ownClass);
 	  savedInstanceState.putString("selclass", viewpageradap.klasse);
 	  Log.d("save", "onsaveinstancestate");
 	}
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		viewpageradap.setClass((String) savedInstanceState.getString("selclass"), savedInstanceState.getBoolean("ownclass", true));
 		viewpageradap.updateCursor();
 		if(Functions.getSDK() >= 11) {
 			suppressSelect = true;
 			AdvancedWrapper adv = new AdvancedWrapper();
 			adv.setSelectedItem(savedInstanceState.getInt("navlistselected"), this);
 		}
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 }
