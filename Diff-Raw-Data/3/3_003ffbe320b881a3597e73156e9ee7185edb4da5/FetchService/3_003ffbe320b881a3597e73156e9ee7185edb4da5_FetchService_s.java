 package ru.shutoff.caralarm;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FetchService extends Service {
 
     final long REPEAT_AFTER_ERROR = 20 * 1000;
     final long REPEAT_AFTER_500 = 3600 * 1000;
 
     BroadcastReceiver mReceiver;
     PendingIntent pi;
 
     SharedPreferences preferences;
     ConnectivityManager conMgr;
     PowerManager powerMgr;
     AlarmManager alarmMgr;
 
     static final String ACTION_UPDATE = "ru.shutoff.caralarm.UPDATE";
     static final String ACTION_NOUPDATE = "ru.shutoff.caralarm.NO_UPDATE";
     static final String ACTION_ERROR = "ru.shutoff.caralarm.ERROR";
     static final String ACTION_START = "ru.shutoff.caralarm.START";
     static final String ACTION_START_UPDATE = "ru.shutoff.caralarm.START_UPDATE";
 
     static final Pattern balancePattern = Pattern.compile("-?[0-9]+[\\.,][0-9][0-9]");
 
     static final String STATUS_URL = "http://api.car-online.ru/v2?get=lastinfo&skey=$1&content=json";
     static final String EVENTS_URL = "http://api.car-online.ru/v2?get=events&skey=$1&begin=$2&end=$3&content=json";
     static final String TEMP_URL = "http://api.car-online.ru/v2?get=temperaturelist&skey=$1&begin=$2&end=$3&content=json";
     static final String GPS_URL = "http://api.car-online.ru/v2?get=gps&skey=$1&id=$2&time=$3&content=json";
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
         alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         pi = PendingIntent.getService(this, 0, new Intent(this, FetchService.class), 0);
         mReceiver = new ScreenReceiver();
         requests = new HashMap<String, ServerRequest>();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (intent != null) {
             String car_id = intent.getStringExtra(Names.ID);
             if (car_id != null)
                 new StatusRequest(Preferences.getCar(preferences, car_id));
         }
         startRequest();
         return START_STICKY;
     }
 
     void startRequest() {
         for (Map.Entry<String, ServerRequest> entry : requests.entrySet()) {
             if (entry.getValue().started)
                 return;
         }
         for (Map.Entry<String, ServerRequest> entry : requests.entrySet()) {
             entry.getValue().start();
             return;
         }
     }
 
     Map<String, ServerRequest> requests;
 
     abstract class ServerRequest extends HttpTask {
 
         String key;
         String car_id;
         boolean started;
 
         ServerRequest(String type, String id) {
             key = type + id;
             if (requests.get(key) != null)
                 return;
             car_id = id;
             requests.put(key, this);
         }
 
         @Override
         void result(JSONObject res) throws JSONException {
             requests.remove(key);
             startRequest();
         }
 
         @Override
         void error() {
             requests.remove(key);
             new StatusRequest(car_id);
             long timeout = (error_text != null) ? REPEAT_AFTER_500 : REPEAT_AFTER_ERROR;
             alarmMgr.setInexactRepeating(AlarmManager.RTC,
                     System.currentTimeMillis() + timeout, timeout, pi);
             sendError(ACTION_ERROR, error_text, car_id);
         }
 
         void start() {
             if (started)
                 return;
             String api_key = preferences.getString(Names.CAR_KEY + car_id, "");
             if (api_key.length() == 0) {
                 requests.remove(key);
                 return;
             }
             final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
             if ((activeNetwork == null) || !activeNetwork.isConnected()) {
                 IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                 registerReceiver(mReceiver, filter);
                 return;
             }
             if (!powerMgr.isScreenOn()) {
                 IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                 registerReceiver(mReceiver, filter);
                 return;
             }
             try {
                 unregisterReceiver(mReceiver);
             } catch (Exception e) {
                 // ignore
             }
             alarmMgr.cancel(pi);
             started = true;
             exec(api_key);
         }
 
         abstract void exec(String api_key);
     }
 
 
     class StatusRequest extends ServerRequest {
 
         StatusRequest(String id) {
             super("S", id);
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             JSONObject event = res.getJSONObject("event");
             long eventId = event.getLong("eventId");
             if (eventId == preferences.getLong(Names.EVENT_ID + car_id, 0)) {
                 sendUpdate(ACTION_NOUPDATE, car_id);
                 return;
             }
             long eventTime = event.getLong("eventTime");
             SharedPreferences.Editor ed = preferences.edit();
             ed.putLong(Names.EVENT_ID + car_id, eventId);
             ed.putLong(Names.EVENT_TIME + car_id, eventTime);
 
             JSONObject voltage = res.getJSONObject("voltage");
             ed.putString(Names.VOLTAGE_MAIN + car_id, voltage.getString("main"));
             ed.putString(Names.VOLTAGE_RESERVED + car_id, voltage.getString("reserved"));
 
             JSONObject balance = res.getJSONObject("balance");
             Matcher m = balancePattern.matcher(balance.getString("source"));
             if (m.find())
                 ed.putString(Names.BALANCE + car_id, m.group(0).replaceAll(",", "."));
 
             JSONObject gps = res.getJSONObject("gps");
             ed.putString(Names.LATITUDE + car_id, gps.getString("latitude"));
             ed.putString(Names.LONGITUDE + car_id, gps.getString("longitude"));
             ed.putString(Names.SPEED + car_id, gps.getString("speed"));
 
             JSONObject contact = res.getJSONObject("contact");
             ed.putBoolean(Names.GUARD + car_id, contact.getBoolean("stGuard"));
             ed.putBoolean(Names.INPUT1 + car_id, contact.getBoolean("stInput1"));
             ed.putBoolean(Names.INPUT2 + car_id, contact.getBoolean("stInput2"));
             ed.putBoolean(Names.INPUT3 + car_id, contact.getBoolean("stInput3"));
             ed.putBoolean(Names.INPUT4 + car_id, contact.getBoolean("stInput4"));
             ed.putBoolean(Names.ZONE_DOOR + car_id, contact.getBoolean("stZoneDoor"));
             ed.putBoolean(Names.ZONE_HOOD + car_id, contact.getBoolean("stZoneHood"));
             ed.putBoolean(Names.ZONE_TRUNK + car_id, contact.getBoolean("stZoneTrunk"));
             ed.putBoolean(Names.ZONE_ACCESSORY + car_id, contact.getBoolean("stZoneAccessoryOn"));
             ed.putBoolean(Names.ZONE_IGNITION + car_id, contact.getBoolean("stZoneIgnitionOn"));
             if (contact.getBoolean("stGPS") && contact.getBoolean("stGPSValid"))
                 ed.putString(Names.COURSE + car_id, gps.getString("course"));
 
             ed.commit();
             sendUpdate(ACTION_UPDATE, car_id);
 
             new EventsRequest(car_id);
         }
 
 
         @Override
         void exec(String api_key) {
             execute(STATUS_URL, api_key);
         }
     }
 
     class EventsRequest extends ServerRequest {
 
         EventsRequest(String id) {
             super("E", id);
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             if (res == null)
                 return;
 
             JSONArray events = res.getJSONArray("events");
             if (events.length() > 0) {
                 boolean valet_state = preferences.getBoolean(Names.VALET + car_id, false);
                 boolean valet = valet_state;
                 boolean engine_state = preferences.getBoolean(Names.ENGINE + car_id, false);
                 boolean engine = engine_state;
                 long last_stand = preferences.getLong(Names.LAST_STAND, 0);
                 long stand = last_stand;
                 long event_id = 0;
                 for (int i = events.length() - 1; i >= 0; i--) {
                     JSONObject event = events.getJSONObject(i);
                     int type = event.getInt("eventType");
                     switch (type) {
                         case 120:
                             valet_state = true;
                             engine_state = false;
                             break;
                         case 110:
                         case 24:
                         case 25:
                             valet_state = false;
                             engine_state = false;
                             break;
                         case 45:
                         case 46:
                             engine_state = true;
                             break;
                         case 47:
                         case 48:
                             engine_state = false;
                             break;
                         case 37:
                             last_stand = -event.getLong("eventTime");
                             break;
                         case 38:
                             last_stand = event.getLong("eventTime");
                             event_id = event.getLong("eventId");
                             break;
                     }
                 }
                 if (event_id > 0)
                     new GPSRequest(car_id, event_id, last_stand);
                 boolean changed = false;
                 SharedPreferences.Editor ed = preferences.edit();
                 ed.putLong(Names.LAST_EVENT + car_id, eventTime);
                 if (valet_state != valet) {
                     ed.putBoolean(Names.VALET + car_id, valet_state);
                     changed = true;
                 }
                 if (engine_state != engine) {
                     ed.putBoolean(Names.ENGINE + car_id, engine_state);
                     changed = true;
                 }
                 if (last_stand != stand) {
                     ed.putLong(Names.LAST_STAND + car_id, last_stand);
                     changed = true;
                 }
                 ed.commit();
                 if (changed)
                     sendUpdate(ACTION_UPDATE, car_id);
             }
 
             new TemperatureRequest(car_id);
         }
 
         @Override
         void exec(String api_key) {
             eventTime = preferences.getLong(Names.EVENT_TIME + car_id, 0);
             long begin = preferences.getLong(Names.LAST_EVENT + car_id, 0);
             long bound = eventTime - 2 * 24 * 60 * 60 * 1000;
             if (begin < bound)
                 begin = bound;
             execute(EVENTS_URL, api_key, begin + "", eventTime + "");
         }
 
         long eventTime;
 
     }
 
     class GPSRequest extends ServerRequest {
 
         String event_id;
         String event_time;
 
         GPSRequest(String id, long eventId, long eventTime) {
             super("G", id);
             event_id = eventId + "";
             event_time = eventTime + "";
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             if (res == null)
                 return;
             SharedPreferences.Editor ed = preferences.edit();
             ed.putString(Names.COURSE + car_id, res.getString("course"));
             ed.commit();
         }
 
         @Override
         void exec(String api_key) {
            long eventTime = preferences.getLong(Names.LAST_EVENT + car_id, 0);
             execute(GPS_URL, api_key, event_id, event_time);
         }
     }
 
     class TemperatureRequest extends ServerRequest {
 
         TemperatureRequest(String id) {
             super("T", id);
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             if (res == null)
                 return;
             JSONArray arr = res.getJSONArray("temperatureList");
             if (arr.length() == 0)
                 return;
             JSONObject value = arr.getJSONObject(0);
             String temp = value.getString("value");
             if (temp.equals(preferences.getString(Names.TEMPERATURE + car_id, "")))
                 return;
             SharedPreferences.Editor ed = preferences.edit();
             ed.putString(Names.TEMPERATURE + car_id, temp);
             ed.commit();
             sendUpdate(ACTION_UPDATE, car_id);
         }
 
         @Override
         void exec(String api_key) {
             long eventTime = preferences.getLong(Names.LAST_EVENT + car_id, 0);
             execute(TEMP_URL, api_key,
                     (eventTime - 24 * 60 * 60 * 1000) + "",
                     eventTime + "");
         }
     }
 
     void sendUpdate(String action, String car_id) {
         try {
             Intent intent = new Intent(action);
             intent.putExtra(Names.ID, car_id);
             sendBroadcast(intent);
         } catch (Exception e) {
             // ignore
         }
     }
 
 
     void sendError(String action, String error, String car_id) {
         try {
             Intent intent = new Intent(action);
             intent.putExtra(Names.ERROR, error);
             intent.putExtra(Names.ID, car_id);
             sendBroadcast(intent);
         } catch (Exception e) {
             // ignore
         }
     }
 }
