 package ru.shutoff.caralarm;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v7.app.ActionBarActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Date;
 import java.util.Vector;
 
 public class EventsActivity extends ActionBarActivity {
 
     final static String EVENTS = "http://api.car-online.ru/v2?get=events&skey=$1&begin=$2&end=$3&content=json";
 
     CaldroidFragment dialogCaldroidFragment;
     LocalDate current;
     ListView lvEvents;
     TextView tvNoEvents;
     View progress;
 
     SharedPreferences preferences;
     String api_key;
 
     Vector<Event> events;
     Vector<Event> filtered;
 
     int filter;
     boolean error;
 
     static final String FILTER = "filter";
 
     static class EventType {
         EventType(int type_, int string_, int icon_) {
             type = type_;
             string = string_;
             icon = icon_;
             filter = 4;
         }
 
         EventType(int type_, int string_, int icon_, int filter_) {
             type = type_;
             string = string_;
             icon = icon_;
             filter = filter_;
         }
 
         int type;
         int string;
         int icon;
         int filter;
     }
 
     ;
 
     static EventType[] event_types = {
             new EventType(24, R.string.guard_on, R.drawable.guard_on, 1),
             new EventType(25, R.string.guard_off, R.drawable.guard_off, 1),
             new EventType(91, R.string.end_move, R.drawable.system),
             new EventType(111, R.string.lock_off1, R.drawable.lockclose01, 1),
             new EventType(112, R.string.lock_off2, R.drawable.lockclose02, 1),
             new EventType(113, R.string.lock_off3, R.drawable.lockclose03, 1),
             new EventType(114, R.string.lock_off4, R.drawable.lockclose04, 1),
             new EventType(115, R.string.lock_off5, R.drawable.lockclose05, 1),
             new EventType(121, R.string.lock_on1, R.drawable.lockcopen01, 1),
             new EventType(122, R.string.lock_on2, R.drawable.lockcopen02, 1),
             new EventType(123, R.string.lock_on3, R.drawable.lockcopen03, 1),
             new EventType(124, R.string.lock_on4, R.drawable.lockcopen04, 1),
             new EventType(125, R.string.lock_on5, R.drawable.lockcopen05, 1),
             new EventType(42, R.string.user_call, R.drawable.user_call, 1),
             new EventType(46, R.string.motor_start, R.drawable.motor_start, 1),
             new EventType(89, R.string.request_photo, R.drawable.request_photo, 1),
             new EventType(110, R.string.valet_on, R.drawable.valet_on, 1),
             new EventType(120, R.string.valet_off, R.drawable.valet_off, 1),
             new EventType(5, R.string.trunk_open, R.drawable.boot_open, 2),
             new EventType(6, R.string.hood_open, R.drawable.e_hood_open, 2),
             new EventType(7, R.string.door_open, R.drawable.door_open, 2),
             new EventType(9, R.string.ignition_on, R.drawable.ignition_on, 2),
             new EventType(10, R.string.access_on, R.drawable.access_on, 2),
             new EventType(11, R.string.input1_on, R.drawable.input1_on, 2),
             new EventType(12, R.string.input2_on, R.drawable.input2_on, 2),
             new EventType(13, R.string.input3_on, R.drawable.input3_on, 2),
             new EventType(14, R.string.input4_on, R.drawable.input4_on, 2),
             new EventType(15, R.string.trunk_close, R.drawable.boot_close, 2),
             new EventType(16, R.string.hood_close, R.drawable.e_hood_close, 2),
             new EventType(17, R.string.door_close, R.drawable.door_close, 2),
             new EventType(18, R.string.ignition_off, R.drawable.ignition_off, 2),
             new EventType(19, R.string.access_off, R.drawable.access_off, 2),
             new EventType(20, R.string.input1_off, R.drawable.input1_off, 2),
             new EventType(21, R.string.input2_off, R.drawable.input2_off, 2),
             new EventType(22, R.string.input3_off, R.drawable.input3_off, 2),
             new EventType(23, R.string.input4_off, R.drawable.input4_off, 2),
             new EventType(59, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(60, R.string.gprs_on, R.drawable.gprs_on),
             new EventType(61, R.string.gprs_off, R.drawable.gprs_off),
             new EventType(65, R.string.reset, R.drawable.reset),
             new EventType(37, R.string.trace_start, R.drawable.trace_start),
             new EventType(38, R.string.trace_stop, R.drawable.trace_stop),
             new EventType(31, R.string.gsm_fail, R.drawable.gsm_fail),
             new EventType(32, R.string.gsm_recover, R.drawable.gsm_recover),
             new EventType(34, R.string.gps_fail, R.drawable.gps_fail),
             new EventType(35, R.string.gps_recover, R.drawable.gps_recover),
             new EventType(90, R.string.till_start, R.drawable.till_start),
             new EventType(105, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(106, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(107, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(108, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(26, R.string.reset, R.drawable.reset),
             new EventType(100, R.string.reset, R.drawable.reset),
             new EventType(101, R.string.reset, R.drawable.reset),
             new EventType(72, R.string.net_error, R.drawable.system),
             new EventType(68, R.string.net_error, R.drawable.system),
             new EventType(75, R.string.net_error, R.drawable.system),
             new EventType(76, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(77, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(78, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(79, R.string.reset_modem, R.drawable.reset_modem),
             new EventType(1, R.string.light_shock, R.drawable.light_shock, 0),
             new EventType(2, R.string.ext_zone, R.drawable.ext_zone, 0),
             new EventType(3, R.string.heavy_shock, R.drawable.heavy_shock, 0),
             new EventType(4, R.string.inner_zone, R.drawable.inner_zone, 0),
             new EventType(8, R.string.tilt, R.drawable.tilt, 0),
             new EventType(27, R.string.main_power_on, R.drawable.main_power_off, 0),
             new EventType(28, R.string.main_power_off, R.drawable.main_power_off, 0),
             new EventType(29, R.string.reserve_power_on, R.drawable.reserve_power_off, 0),
             new EventType(30, R.string.reserve_power_off, R.drawable.reserve_power_off, 0),
             new EventType(43, R.string.rogue, R.drawable.rogue, 0),
             new EventType(44, R.string.rogue_off, R.drawable.rogue, 0),
             new EventType(49, R.string.alarm_boot, R.drawable.alarm_boot, 0),
             new EventType(50, R.string.alarm_hood, R.drawable.alarm_hood, 0),
             new EventType(51, R.string.alarm_door, R.drawable.alarm_door, 0),
             new EventType(52, R.string.ignition_lock, R.drawable.ignition_lock, 0),
             new EventType(53, R.string.alarm_accessories, R.drawable.alarm_accessories, 0),
             new EventType(54, R.string.alarm_input1, R.drawable.alarm_input1, 0),
             new EventType(55, R.string.alarm_input2, R.drawable.alarm_input2, 0),
             new EventType(56, R.string.alarm_input3, R.drawable.alarm_input3, 0),
             new EventType(57, R.string.alarm_input4, R.drawable.alarm_input4, 0),
             new EventType(85, R.string.sos, R.drawable.sos, 0),
             new EventType(293, R.string.sos, R.drawable.sos, 0),
             new EventType(88, R.string.incomming_sms, R.drawable.user_sms, 1),
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.events);
         lvEvents = (ListView) findViewById(R.id.events);
         tvNoEvents = (TextView) findViewById(R.id.no_events);
         progress = findViewById(R.id.progress);
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
         api_key = preferences.getString(Names.KEY, "");
         events = new Vector<Event>();
         filtered = new Vector<Event>();
         filter = preferences.getInt(FILTER, 3);
         setupButton(R.id.actions, 1);
         setupButton(R.id.contacts, 2);
         setupButton(R.id.system, 4);
         current = new LocalDate();
         if (savedInstanceState != null)
             current = new LocalDate(savedInstanceState.getLong(Names.TRACK_DATE));
         DataFetcher fetcher = new DataFetcher();
         fetcher.update(current);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (current != null)
             outState.putLong(Names.TRACK_DATE, current.toDate().getTime());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.tracks, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.day: {
                 dialogCaldroidFragment = new CaldroidFragment() {
 
                     @Override
                     public void onAttach(Activity activity) {
                         super.onAttach(activity);
                         CaldroidListener listener = new CaldroidListener() {
 
                             @Override
                             public void onSelectDate(Date date, View view) {
                                 changeDate(date);
                             }
                         };
 
                         dialogCaldroidFragment = this;
                         setCaldroidListener(listener);
                     }
 
                 };
                 Bundle args = new Bundle();
                 args.putString(CaldroidFragment.DIALOG_TITLE, getString(R.string.day));
                 args.putInt(CaldroidFragment.MONTH, current.getMonthOfYear());
                 args.putInt(CaldroidFragment.YEAR, current.getYear());
                 args.putInt(CaldroidFragment.START_DAY_OF_WEEK, 1);
                 dialogCaldroidFragment.setArguments(args);
                 LocalDateTime now = new LocalDateTime();
                 dialogCaldroidFragment.setMaxDate(now.toDate());
                 dialogCaldroidFragment.show(getSupportFragmentManager(), "TAG");
                 break;
             }
         }
         return false;
     }
 
     void changeDate(Date date) {
         LocalDate d = new LocalDate(date);
         dialogCaldroidFragment.dismiss();
         dialogCaldroidFragment = null;
         progress.setVisibility(View.VISIBLE);
         lvEvents.setVisibility(View.GONE);
         tvNoEvents.setVisibility(View.GONE);
         DataFetcher fetcher = new DataFetcher();
         fetcher.update(d);
     }
 
     void showError() {
         this.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 tvNoEvents.setText(getString(R.string.error_load));
                 tvNoEvents.setVisibility(View.VISIBLE);
                 progress.setVisibility(View.GONE);
                 error = true;
             }
         });
     }
 
     class DataFetcher extends HttpTask {
 
         LocalDate date;
 
         @Override
         void result(JSONObject data) throws JSONException {
             if (!current.equals(date))
                 return;
             JSONArray res = data.getJSONArray("events");
             for (int i = 0; i < res.length(); i++) {
                 JSONObject event = res.getJSONObject(i);
                 int type = event.getInt("eventType");
                 if ((type == 94) || (type == 98) || (type == 41) || (type == 33) || (type == 39))
                     continue;
                 long time = event.getLong("eventTime");
                 long id = event.getLong("eventId");
                 Event e = new Event();
                 e.type = type;
                 e.time = time;
                 e.id = id;
                 events.add(e);
             }
             filterEvents();
             progress.setVisibility(View.GONE);
         }
 
         @Override
         void error() {
             showError();
         }
 
         void update(LocalDate d) {
            setTitle(d.toString("d MMMM"));
             date = d;
             current = d;
             DateTime start = date.toDateTime(new LocalTime(0, 0));
             LocalDate next = date.plusDays(1);
             DateTime finish = next.toDateTime(new LocalTime(0, 0));
             events.clear();
             error = false;
             execute(EVENTS,
                     api_key,
                     start.toDate().getTime() + "",
                     finish.toDate().getTime() + "");
         }
     }
 
     class EventsAdapter extends BaseAdapter {
 
         @Override
         public int getCount() {
             return filtered.size();
         }
 
         @Override
         public Object getItem(int position) {
             return filtered.get(position);
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater inflater = (LayoutInflater) getBaseContext()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = inflater.inflate(R.layout.event_item, null);
             }
             Event e = filtered.get(position);
             TextView tvName = (TextView) v.findViewById(R.id.name);
             TextView tvTime = (TextView) v.findViewById(R.id.time);
             ImageView icon = (ImageView) v.findViewById(R.id.icon);
             LocalTime time = new LocalTime(e.time);
             tvTime.setText(time.toString("H:mm:ss"));
             boolean found = false;
             for (EventType et : event_types) {
                 if (et.type == e.type) {
                     found = true;
                     tvName.setText(getString(et.string));
                     icon.setVisibility(View.VISIBLE);
                     icon.setImageResource(et.icon);
                 }
             }
             if (!found) {
                 tvName.setText(getString(R.string.event) + " #" + e.type);
                 icon.setVisibility(View.GONE);
             }
             return v;
         }
     }
 
     ;
 
     void setupButton(int id, int mask) {
         Button btn = (Button) findViewById(id);
         if ((mask & filter) != 0)
             btn.setBackgroundResource(R.drawable.pressed);
         btn.setTag(mask);
         btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 toggleButton(v);
             }
         });
     }
 
     void toggleButton(View v) {
         Button btn = (Button) v;
         int mask = (Integer) btn.getTag();
         if ((filter & mask) == 0) {
             filter |= mask;
             btn.setBackgroundResource(R.drawable.pressed);
         } else {
             filter &= ~mask;
             btn.setBackgroundResource(R.drawable.button);
         }
         SharedPreferences.Editor ed = preferences.edit();
         ed.putInt(FILTER, filter);
         ed.commit();
         if (!error)
             filterEvents();
     }
 
     void filterEvents() {
         filtered.clear();
         for (Event e : events) {
             if (isShow(e.type))
                 filtered.add(e);
         }
         if (filtered.size() > 0) {
             lvEvents.setAdapter(new EventsAdapter());
             lvEvents.setVisibility(View.VISIBLE);
             tvNoEvents.setVisibility(View.GONE);
         } else {
             tvNoEvents.setText(getString(R.string.no_events));
             tvNoEvents.setVisibility(View.VISIBLE);
             lvEvents.setVisibility(View.GONE);
         }
     }
 
     boolean isShow(int type) {
         for (EventType et : event_types) {
             if (et.type == type) {
                 if (et.filter == 0)
                     return true;
                 return (et.filter & filter) != 0;
             }
         }
         return (filter & 4) != 0;
     }
 
     static class Event {
         int type;
         long time;
         long id;
     }
 
     ;
 }
