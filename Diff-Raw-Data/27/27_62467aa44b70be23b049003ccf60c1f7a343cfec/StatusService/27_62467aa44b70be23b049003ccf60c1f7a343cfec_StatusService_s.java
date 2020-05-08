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
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StatusService extends Service {
 
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
 
     HttpTask statusRequest;
     HttpTask temperatureRequest;
     HttpTask eventsRequest;
 
     String api_key;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
        State.setExceptionHandler();
         preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
         alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         pi = PendingIntent.getService(this, 0, new Intent(this, StatusService.class), 0);
         mReceiver = new ScreenReceiver();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         startRequest();
         return START_STICKY;
     }
 
     void startRequest() {
         if (statusRequest != null)
             return;
         api_key = preferences.getString(Names.KEY, "");
         if (api_key.length() == 0)
             return;
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
 
         sendUpdate(ACTION_START);
         statusRequest = new StatusRequest();
         statusRequest.execute(STATUS_URL, api_key);
     }
 
     class StatusRequest extends HttpTask {
 
         @Override
         void result(JSONObject res) throws JSONException {
             statusRequest = null;
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             JSONObject event = res.getJSONObject("event");
             long eventId = event.getLong("eventId");
             if (eventId == preferences.getLong(Names.EventId, 0)) {
                 sendUpdate(ACTION_NOUPDATE);
                 return;
             }
             long eventTime = event.getLong("eventTime");
             SharedPreferences.Editor ed = preferences.edit();
             ed.putLong(Names.EventId, eventId);
             ed.putLong(Names.EventTime, eventTime);
 
             JSONObject voltage = res.getJSONObject("voltage");
             ed.putString(Names.VoltageMain, voltage.getString("main"));
             ed.putString(Names.VoltageReserved, voltage.getString("reserved"));
 
             JSONObject balance = res.getJSONObject("balance");
             Matcher m = balancePattern.matcher(balance.getString("source"));
             if (m.find())
                 ed.putString(Names.Balance, m.group(0).replaceAll(",", "."));
 
             JSONObject gps = res.getJSONObject("gps");
             ed.putString(Names.Latitude, gps.getString("latitude"));
             ed.putString(Names.Longitude, gps.getString("longitude"));
             ed.putString(Names.Speed, gps.getString("speed"));
 
             JSONObject contact = res.getJSONObject("contact");
             ed.putBoolean(Names.Guard, contact.getBoolean("stGuard"));
             ed.putBoolean(Names.Input1, contact.getBoolean("stInput1"));
             ed.putBoolean(Names.Input2, contact.getBoolean("stInput2"));
             ed.putBoolean(Names.Input3, contact.getBoolean("stInput3"));
             ed.putBoolean(Names.Input4, contact.getBoolean("stInput4"));
             ed.putBoolean(Names.ZoneDoor, contact.getBoolean("stZoneDoor"));
             ed.putBoolean(Names.ZoneHood, contact.getBoolean("stZoneHood"));
             ed.putBoolean(Names.ZoneTrunk, contact.getBoolean("stZoneTrunk"));
             ed.putBoolean(Names.ZoneAccessory, contact.getBoolean("stZoneAccessoryOn"));
             ed.putBoolean(Names.ZoneIgnition, contact.getBoolean("stZoneIgnitionOn"));
 
             ed.commit();
             sendUpdate(ACTION_UPDATE);
 
             if (eventsRequest != null)
                 return;
 
             long begin = preferences.getLong(Names.LastEvent, 0);
             long bound = eventTime - 2 * 24 * 60 * 60 * 1000;
             if (begin < bound)
                 begin = bound;
 
             eventsRequest = new EventsRequest(eventTime);
             eventsRequest.execute(EVENTS_URL, api_key, begin + "", eventTime + "");
         }
 
         @Override
         void error() {
             statusRequest = null;
             long timeout = (error_text != null) ? REPEAT_AFTER_500 : REPEAT_AFTER_ERROR;
             alarmMgr.setInexactRepeating(AlarmManager.RTC,
                     System.currentTimeMillis() + timeout, timeout, pi);
             sendError(ACTION_ERROR, error_text);
         }
     }
 
     class EventsRequest extends HttpTask {
 
         EventsRequest(long time) {
             eventTime = time;
         }
 
         @Override
         void result(JSONObject res) throws JSONException {
             eventsRequest = null;
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             if (res == null)
                 return;
 
             JSONArray events = res.getJSONArray("events");
             if (events.length() > 0) {
                 boolean valet_state = preferences.getBoolean(Names.Valet, false);
                 boolean engine_state = preferences.getBoolean(Names.Engine, false);
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
                     }
                 }
                 boolean valet = preferences.getBoolean(Names.Valet, false);
                 boolean engine = preferences.getBoolean(Names.Engine, false);
                 SharedPreferences.Editor ed = preferences.edit();
                 ed.putLong(Names.LastEvent, eventTime);
                 if (valet_state != valet)
                     ed.putBoolean(Names.Valet, valet_state);
                 if (engine_state != engine)
                     ed.putBoolean(Names.Engine, engine_state);
                 ed.commit();
                 if ((valet_state != valet) || (engine_state != engine))
                     sendUpdate(ACTION_UPDATE);
             }
 
             if (temperatureRequest != null)
                 return;
 
             temperatureRequest = new TemperatureRequest();
             temperatureRequest.execute(TEMP_URL, api_key,
                     (eventTime - 24 * 60 * 60 * 1000) + "",
                     eventTime + "");
 
         }
 
         @Override
         void error() {
             eventsRequest = null;
         }
 
         long eventTime;
     }
 
     class TemperatureRequest extends HttpTask {
         @Override
         void result(JSONObject res) throws JSONException {
             temperatureRequest = null;
         }
 
         @Override
         void background(JSONObject res) throws JSONException {
             if (res == null)
                 return;
             JSONArray arr = res.getJSONArray("temperatureList");
             JSONObject value = arr.getJSONObject(0);
             String temp = value.getString("value");
             if (temp.equals(preferences.getString(Names.Temperature, "")))
                 return;
             SharedPreferences.Editor ed = preferences.edit();
             ed.putString(Names.Temperature, temp);
             ed.commit();
             sendUpdate(ACTION_UPDATE);
         }
 
         @Override
         void error() {
             temperatureRequest = null;
         }
     }
 
     void sendUpdate(String action) {
         try {
             Intent intent = new Intent(action);
             sendBroadcast(intent);
         } catch (Exception e) {
             // ignore
         }
     }
 
 
     void sendError(String action, String error) {
         try {
             Intent intent = new Intent(action);
             intent.putExtra(Names.ERROR, error);
             sendBroadcast(intent);
         } catch (Exception e) {
             // ignore
         }
     }
 }
