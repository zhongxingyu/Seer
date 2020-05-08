 /*
  * ***********************************************************************************************************
  * Copyright (C) 2010 Sense Observation Systems, Rotterdam, the Netherlands. All rights reserved. *
  * **
  * ************************************************************************************************
  * *********
  */
 package nl.sense_os.service;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Locale;
 
 import nl.sense_os.service.provider.LocalStorage;
 import nl.sense_os.service.provider.SensorData.DataPoint;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 
 public class MsgHandler extends Service {
 
     /**
      * Inner class that handles the creation of the SQLite3 database with the desired tables and
      * columns.
      * 
      * To view the Sqlite3 database in a terminal: $ adb shell # sqlite3
      * /data/data/nl.sense_os.dji/databases/data.sqlite3 sqlite> .headers ON sqlite> select * from
      * testTbl;
      */
     private static class DbHelper extends SQLiteOpenHelper {
 
         protected static final String COL_ACTIVE = "active";
         protected static final String COL_JSON = "json";
         protected static final String COL_SENSOR = "sensor";
         protected static final String COL_ROWID = "_id";
         protected static final String DATABASE_NAME = "tx_buffer.sqlite3";
         protected static final int DATABASE_VERSION = 4;
         protected static final String TABLE_NAME = "sensor_data";
 
         DbHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             final StringBuilder sb = new StringBuilder("CREATE TABLE " + TABLE_NAME + "(");
             sb.append(COL_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT");
             sb.append(", " + COL_JSON + " STRING");
             sb.append(", " + COL_SENSOR + " STRING");
             sb.append(", " + COL_ACTIVE + " INTEGER");
             sb.append(");");
             db.execSQL(sb.toString());
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVers, int newVers) {
             Log.w(TAG, "Upgrading database from version " + oldVers + " to " + newVers
                     + ", which will destroy all old data");
 
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
             onCreate(db);
         }
     }
 
     private class SendDataThread extends Handler {
 
         private final String cookie;
         private final String sensorName;
         private final String deviceType;
         private final String dataType;
         private final Context context;
         private final JSONObject data;
         private WakeLock wakeLock;
 
         public SendDataThread(String cookie, JSONObject data, String sensorName, String dataType,
                 String deviceType, Context context, Looper looper) {
             super(looper);
             this.cookie = cookie;
             this.data = data;
             this.sensorName = sensorName;
             this.dataType = dataType;
             this.deviceType = deviceType != null ? deviceType : sensorName;
             this.context = context;
         }
 
         private String getSensorUrl() {
             String url = null;
             try {
                 String sensorValue = (String) ((JSONObject) ((JSONArray) data.get("data")).get(0))
                         .get("value");
                 url = MsgHandler.this.getSensorUrl(context, sensorName, sensorValue, dataType,
                         deviceType);
             } catch (Exception e) {
                 Log.e(TAG, "Exception retrieving sensor URL from API", e);
             }
             return url;
         }
 
         @Override
         public void handleMessage(Message msg) {
 
             try {
                 // make sure the device stays awake while transmitting
                 this.wakeLock = getWakeLock();
                 this.wakeLock.acquire();
 
                 // get sensor URL at CommonSense
                 String url = getSensorUrl();
 
                 if (url == null) {
                     Log.w(TAG, "Received invalid sensor URL for '" + sensorName
                             + "': requeue the message.");
                     bufferMessage(sensorName, data, dataType, deviceType);
                     return;
                 }
 
                 HashMap<String, String> response = SenseApi.sendJson(context, new URL(url), data,
                         "POST", cookie);
                 // Error when sending
                 if (response == null
                         || response.get("http response code").compareToIgnoreCase("201") != 0) {
 
                     // if un-authorized: relogin
                     if (response != null
                             && response.get("http response code").compareToIgnoreCase("403") == 0) {
                         final Intent serviceIntent = new Intent(ISenseService.class.getName());
                         serviceIntent.putExtra(SenseService.ACTION_RELOGIN, true);
                         context.startService(serviceIntent);
                     }
 
                     // Show the HTTP response Code
                     if (response != null) {
                         Log.w(TAG, "Failed to send '" + sensorName + "' data. Response code:"
                                 + response.get("http response code") + ", Response content: '"
                                 + response.get("content") + "'\nMessage will be requeued");
                     } else {
                         Log.w(TAG, "Failed to send '" + sensorName
                                 + "' data.\nMessage will be requeued.");
                     }
 
                     // connection error put all the messages back in the queue
                     bufferMessage(sensorName, data, dataType, deviceType);
                 }
 
                 // Data sent successfully
                 else {
                     int bytes = data.toString().getBytes().length;
                     Log.i(TAG, "Sent '" + sensorName + "' data! Raw data size: " + bytes + " bytes");
                 }
                 // Log.d(TAG, "  data: " + data);
 
             } catch (Exception e) {
                 if (null != e.getMessage()) {
                     Log.e(TAG, "Exception sending '" + sensorName
                             + "' data, message will be requeued: " + e.getMessage());
                 } else {
                     Log.e(TAG, "Exception sending '" + sensorName
                             + "' data, message will be requeued.", e);
                 }
                 bufferMessage(sensorName, data, dataType, deviceType);
 
             } finally {
                 stopAndCleanup();
             }
         }
 
         private void stopAndCleanup() {
             --nrOfSendMessageThreads;
             this.wakeLock.release();
             getLooper().quit();
         }
     }
 
     private class SendFileThread extends Handler {
 
         private final String cookie;
         private final JSONObject data;
         private final String sensorName;
         private final String dataType;
         private final String deviceType;
         private final Context context;
         private WakeLock wakeLock;
 
         public SendFileThread(String cookie, JSONObject data, String sensorName, String dataType,
                 String deviceType, Context context, Looper looper) {
             super(looper);
             this.cookie = cookie;
             this.data = data;
             this.sensorName = sensorName;
             this.dataType = dataType;
             this.deviceType = deviceType;
             this.context = context;
         }
 
         private String getSensorUrl() {
             String url = null;
             try {
                 final String f_deviceType = deviceType != null ? deviceType : sensorName;
                 String dataStructure = (String) ((JSONObject) ((JSONArray) data.get("data")).get(0))
                         .get("value");
                 url = SenseApi.getSensorUrl(context, sensorName, dataStructure, dataType,
                         f_deviceType);
             } catch (Exception e) {
                 Log.e(TAG, "Exception retrieving sensor URL from API", e);
             }
             return url;
         }
 
         @Override
         public void handleMessage(Message message) {
 
             try {
                 // make sure the device stays awake while transmitting
                 this.wakeLock = getWakeLock();
                 this.wakeLock.acquire();
 
                 // get sensor URL from CommonSense
                 String urlStr = getSensorUrl();
 
                 if (urlStr == null) {
                     Log.w(TAG, "Received invalid sensor URL for '" + sensorName + "'. Data lost.");
                     return;
                 }
 
                 // submit each file separately
                 JSONArray data = (JSONArray) this.data.get("data");
                 for (int i = 0; i < data.length(); i++) {
                     JSONObject object = (JSONObject) data.get(i);
                     String fileName = (String) object.get("value");
 
                     HttpURLConnection conn = null;
 
                     DataOutputStream dos = null;
 
                     // OutputStream os = null;
                     // boolean ret = false;
 
                     String lineEnd = "\r\n";
                     String twoHyphens = "--";
                     String boundary = "----FormBoundary6bYQOdhfGEj4oCSv";
 
                     int bytesRead, bytesAvailable, bufferSize;
 
                     byte[] buffer;
 
                     int maxBufferSize = 1 * 1024 * 1024;
 
                     // ------------------ CLIENT REQUEST
 
                     FileInputStream fileInputStream = new FileInputStream(new File(fileName));
 
                     // open a URL connection to the Servlet
 
                     URL url = new URL(urlStr);
 
                     // Open a HTTP connection to the URL
 
                     conn = (HttpURLConnection) url.openConnection();
 
                     // Allow Inputs
                     conn.setDoInput(true);
 
                     // Allow Outputs
                     conn.setDoOutput(true);
 
                     // Don't use a cached copy.
                     conn.setUseCaches(false);
 
                     // Use a post method.
                     conn.setRequestMethod("POST");
                     conn.setRequestProperty("Cookie", cookie);
                     conn.setRequestProperty("Connection", "Keep-Alive");
 
                     conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                             + boundary);
 
                     dos = new DataOutputStream(conn.getOutputStream());
 
                     dos.writeBytes(twoHyphens + boundary + lineEnd);
                     dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                             + fileName + "\"" + lineEnd);
                     dos.writeBytes(lineEnd);
                     // create a buffer of maximum size
                     bytesAvailable = fileInputStream.available();
                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     buffer = new byte[bufferSize];
 
                     // read file and write it into form...
 
                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);
 
                     while (bytesRead > 0) {
                         dos.write(buffer, 0, bufferSize);
                         bytesAvailable = fileInputStream.available();
                         bufferSize = Math.min(bytesAvailable, maxBufferSize);
                         bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                     }
 
                     // send multipart form data necesssary after file data...
 
                     dos.writeBytes(lineEnd);
                     dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 
                     // close streams
 
                     fileInputStream.close();
                     dos.flush();
                     dos.close();
 
                     if (conn.getResponseCode() != 201) {
                         Log.e(TAG,
                                 "Sending '" + sensorName
                                         + "' sensor file failed. Data lost. Response code:"
                                         + conn.getResponseCode());
                     } else {
                         Log.i(TAG, "Sent '" + sensorName + "' sensor value file OK!");
                     }
                 }
             } catch (Exception e) {
                 Log.e(TAG, "Sending '" + sensorName + "' sensor file failed. Data lost.", e);
             } finally {
                 stopAndCleanup();
             }
         }
 
         private void stopAndCleanup() {
             --nrOfSendMessageThreads;
             this.wakeLock.release();
             getLooper().quit();
         }
     }
 
     private static final String TAG = "Sense MsgHandler";
     public static final String ACTION_NEW_MSG = "nl.sense_os.app.MsgHandler.NEW_MSG";
     public static final String ACTION_NEW_FILE = "nl.sense_os.app.MsgHandler.NEW_FILE";
     public static final String ACTION_SEND_DATA = "nl.sense_os.app.MsgHandler.SEND_DATA";
     public static final String KEY_DATA_TYPE = "data_type";
     public static final String KEY_SENSOR_DEVICE = "sensor_device";
     public static final String KEY_SENSOR_NAME = "sensor_name";
     public static final String KEY_TIMESTAMP = "timestamp";
     public static final String KEY_VALUE = "value";
     private static final int MAX_BUFFER = 1048500; // 1mb in bytes
     private static final int MAX_NR_OF_SEND_MSG_THREADS = 50;
     private static final int MAX_POST_DATA = 100;
     private static final int MAX_POST_DATA_TIME_SERIE = 10;
     private JSONObject buffer;
     private int bufferCount;
     private SQLiteDatabase db;
     private DbHelper dbHelper;
     private boolean isDbOpen;
     private int nrOfSendMessageThreads = 0;
     private WakeLock wakeLock;
 
     /**
      * Buffers a data point in the memory, for scheduled transmission later on.
      * 
      * @param sensorName
      *            Sensor name.
      * @param sensorValue
      *            Sensor value, stored as a String.
      * @param timeInSecs
      *            Timestamp of the data point, in seconds and properly formatted.
      * @param dataType
      *            Data type of the point.
      * @param deviceType
      *            Sensor device type.
      */
     private void bufferDataPoint(String sensorName, String sensorValue, String timeInSecs,
             String dataType, String deviceType) {
         // Log.d(TAG, "Buffer new sensor data");
 
         try {
             // create JSON object for buffering
             JSONObject json = new JSONObject();
             json.put("name", sensorName);
             json.put("time", timeInSecs);
             json.put("type", dataType);
             json.put("device", deviceType);
             json.put("val", sensorValue);
 
             int jsonBytes = json.toString().length();
 
             // check if there is room in the buffer
             if (bufferCount + jsonBytes >= MAX_BUFFER) {
                 // empty buffer into database
                 // Log.v(TAG, "Buffer overflow! Emptying buffer to database");
                 emptyBufferToDb();
             }
 
             // put data in buffer
             String sensorKey = sensorName + "_" + deviceType;
             JSONArray dataArray = buffer.optJSONArray(sensorKey);
             if (dataArray == null) {
                 dataArray = new JSONArray();
             }
             dataArray.put(json);
             buffer.put(sensorKey, dataArray);
             bufferCount += jsonBytes;
 
         } catch (Exception e) {
             Log.e(TAG, "Error in buffering data:" + e.getMessage());
         }
     }
 
     /**
      * Puts a failed sensor data message back in the buffer, for transmission later on.
      * 
      * @param sensorName
      *            Sensor name.
      * @param messageData
      *            JSON object with array of sensor data points that have to be buffered again.
      * @param dataType
      *            Sensor data type.
      * @param deviceType
      *            Sensor device type.
      */
     private void bufferMessage(String sensorName, JSONObject messageData, String dataType,
             String deviceType) {
         // Log.v(TAG, "Buffer sensor data from failed transmission...");
 
         try {
             JSONArray dataArray = messageData.getJSONArray("data");
 
             // put each data point in the buffer individually
             for (int index = 0; index < dataArray.length(); index++) {
                 JSONObject mysteryJson = dataArray.getJSONObject(index);
                 String value = mysteryJson.getString("value");
                 String date = mysteryJson.getString("date");
                 bufferDataPoint(sensorName, value, date, dataType, deviceType);
                 // Log.v(TAG, sensorName + " data buffered.");
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Error in buffering failed message:", e);
         }
     }
 
     private WakeLock getWakeLock() {
         if (null == wakeLock) {
             PowerManager powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
             wakeLock = powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
         }
         return wakeLock;
     }
 
     private void closeDb() {
         if (true == isDbOpen) {
             dbHelper.close();
             isDbOpen = false;
         }
     }
 
     /**
      * Puts data from the buffer in the flash database for long-term storage
      */
     private void emptyBufferToDb() {
         // Log.v(TAG, "Emptying buffer to persistant database...");
 
         try {
             openDb();
             JSONArray names = buffer.names();
             if (names == null) {
                 return;
             }
             for (int i = 0; i < names.length(); i++) {
                 JSONArray sensorArray = buffer.getJSONArray(names.getString(i));
 
                 for (int x = 0; x < sensorArray.length(); x++) {
                     ContentValues values = new ContentValues();
                     values.put(DbHelper.COL_JSON, ((JSONObject) sensorArray.get(x)).toString());
                     values.put(DbHelper.COL_SENSOR, names.getString(i));
                     values.put(DbHelper.COL_ACTIVE, false);
                     db.insert(DbHelper.TABLE_NAME, null, values);
                 }
             }
             // reset buffer
             bufferCount = 0;
             buffer = new JSONObject();
         } catch (Exception e) {
             Log.e(TAG, "Error storing buffer in persistant database!", e);
         } finally {
             closeDb();
         }
     }
 
     /**
      * Calls through to the Sense API class. This method is synchronized to make sure that multiple
      * thread do not create multiple sensors at the same time.
      * 
      * @return The URL of the sensor at CommonSense, or null if an error occurred.
      */
     private synchronized String getSensorUrl(Context context, String sensorName,
             String sensorValue, String dataType, String deviceType) {
         String url = SenseApi.getSensorUrl(context, sensorName, sensorValue, dataType, deviceType);
         return url;
     }
 
     /**
      * Handles an incoming Intent that started the service by checking if it wants to store a new
      * message or if it wants to send data to CommonSense.
      */
     private void handleIntent(Intent intent, int flags, int startId) {
 
         final String action = intent.getAction();
 
         if (action != null && action.equals(ACTION_NEW_MSG)) {
             handleNewMsgIntent(intent);
         } else if (action != null && action.equals(ACTION_SEND_DATA)) {
             handleSendIntent(intent);
         } else {
             Log.e(TAG, "Unexpected intent action: " + action);
         }
     }
 
     private void handleNewMsgIntent(Intent intent) {
         // Log.d(TAG, "handleNewMsgIntent");
 
         try {
             DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
             NumberFormat formatter = new DecimalFormat("##########.##", otherSymbols);
 
             // get data point details from Intent
             String sensorName = intent.getStringExtra(KEY_SENSOR_NAME);
             String dataType = intent.getStringExtra(KEY_DATA_TYPE);
             String timeInSecs = formatter.format(intent.getLongExtra(KEY_TIMESTAMP,
                     System.currentTimeMillis()) / 1000.0d);
             String deviceType = intent.getStringExtra(KEY_SENSOR_DEVICE);
             deviceType = deviceType != null ? deviceType : sensorName;
 
             // convert sensor value to String
             String sensorValue = "";
             if (dataType.equals(Constants.SENSOR_DATA_TYPE_BOOL)) {
                 sensorValue += intent.getBooleanExtra(KEY_VALUE, false);
             } else if (dataType.equals(Constants.SENSOR_DATA_TYPE_FLOAT)) {
                 sensorValue += intent.getFloatExtra(KEY_VALUE, Float.MIN_VALUE);
             } else if (dataType.equals(Constants.SENSOR_DATA_TYPE_INT)) {
                 sensorValue += intent.getIntExtra(KEY_VALUE, Integer.MIN_VALUE);
             } else if (dataType.equals(Constants.SENSOR_DATA_TYPE_JSON)
                     || dataType.equals(Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE)) {
                 sensorValue += new JSONObject(intent.getStringExtra(KEY_VALUE)).toString();
             } else if (dataType.equals(Constants.SENSOR_DATA_TYPE_STRING)
                     || dataType.equals(Constants.SENSOR_DATA_TYPE_FILE)) {
                 sensorValue += intent.getStringExtra(KEY_VALUE);
             }
 
             // check if we can send the data point immediately
             final SharedPreferences mainPrefs = getSharedPreferences(Constants.MAIN_PREFS,
                     MODE_PRIVATE);
             final int rate = Integer.parseInt(mainPrefs.getString(Constants.PREF_SYNC_RATE, "0"));
             boolean isMaxThreads = nrOfSendMessageThreads >= MAX_NR_OF_SEND_MSG_THREADS - 5;
             boolean isRealTimeMode = rate == -2;
             if (isOnline() && isRealTimeMode && !isMaxThreads) {
                 /* send immediately */
 
                 // create sensor data JSON object with only 1 data point
                 JSONObject sensorData = new JSONObject();
                 JSONArray dataArray = new JSONArray();
                 JSONObject data = new JSONObject();
                 data.put("value", sensorValue);
                 data.put("date", timeInSecs);
                 dataArray.put(data);
                 sensorData.put("data", dataArray);
 
                 sendSensorData(sensorName, sensorData, dataType, deviceType);
 
             } else {
                 /* buffer data if there is no connectivity */
 
                 bufferDataPoint(sensorName, sensorValue, timeInSecs, dataType, deviceType);
             }
 
             // put the data point in the local storage
             if (mainPrefs.getBoolean(Constants.PREF_LOCAL_STORAGE, false)) {
                 insertToLocalStorage(sensorName, deviceType, dataType,
                         intent.getLongExtra(KEY_TIMESTAMP, System.currentTimeMillis()), sensorValue);
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Failed to handle new data point!", e);
         }
     }
 
     /**
      * Inserts a data point as new row in the local storage. Removal of old points is done
      * automatically.
      * 
      * @param sensorName
      * @param sensorDescription
      * @param dataType
      * @param timestamp
      * @param value
      */
     private void insertToLocalStorage(String sensorName, String sensorDescription, String dataType,
             long timestamp, String value) {
 
        Uri url = Uri.parse("content://" + LocalStorage.AUTHORITY + "/recent_values");
 
         // new value
         ContentValues values = new ContentValues();
         values.put(DataPoint.SENSOR_NAME, sensorName);
         values.put(DataPoint.SENSOR_DESCRIPTION, sensorDescription);
         values.put(DataPoint.DATA_TYPE, dataType);
         values.put(DataPoint.TIMESTAMP, timestamp);
         values.put(DataPoint.VALUE, value);
 
         getContentResolver().insert(url, values);
     }
 
     private void handleSendIntent(Intent intent) {
         if (isOnline()) {
             sendDataFromDb();
             sendDataFromBuffer();
         }
     }
 
     /**
      * @return <code>true</code> if the phone has network connectivity.
      */
     private boolean isOnline() {
         final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         final NetworkInfo info = cm.getActiveNetworkInfo();
         return null != info && info.isConnected();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         // you cannot bind to this service
         return null;
     }
 
     @Override
     public void onCreate() {
         // Log.v(TAG, "onCreate");
         super.onCreate();
         buffer = new JSONObject();
         bufferCount = 0;
     }
 
     @Override
     public void onDestroy() {
         // Log.v(TAG, "onDestroy");
         emptyBufferToDb();
         super.onDestroy();
     }
 
     /**
      * Deprecated method for starting the service, used in 1.6 and older.
      */
     @Override
     public void onStart(Intent intent, int startid) {
         handleIntent(intent, 0, startid);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         handleIntent(intent, flags, startId);
 
         // this service is not sticky, it will get an intent to restart it if necessary
         return START_NOT_STICKY;
     }
 
     private void openDb() {
         if (false == isDbOpen) {
             dbHelper = new DbHelper(this);
             db = dbHelper.getWritableDatabase();
             isDbOpen = true;
         }
     }
 
     /**
      * Puts a message with sensor data in the queue for the MsgHandler again, for immediate
      * retrying.
      * 
      * @param sensorName
      *            Name of the sensor.
      * @param data
      *            JSON sensor data, with multiple data points.
      * @param dataType
      *            Sensor data type.
      * @param deviceType
      *            Sensor device type.
      * @param context
      *            Application context, used to call the MsgHandler.
      */
     private void requeueMessage(String sensorName, JSONObject data, String dataType,
             String deviceType, Context context) {
         try {
             JSONArray dataArray = data.getJSONArray("data");
             for (int index = 0; index < dataArray.length(); index++) {
                 Intent i = new Intent(MsgHandler.ACTION_NEW_MSG);
                 i.putExtra(MsgHandler.KEY_SENSOR_NAME, sensorName);
                 i.putExtra(MsgHandler.KEY_SENSOR_DEVICE, deviceType);
                 i.putExtra(MsgHandler.KEY_DATA_TYPE, dataType);
                 i.putExtra(MsgHandler.KEY_TIMESTAMP, (long) ((float) Double.parseDouble(dataArray
                         .getJSONObject(index).getString("date")) * 1000f));
                 String value = dataArray.getJSONObject(index).getString("value");
                 if (dataType.equals(Constants.SENSOR_DATA_TYPE_BOOL)) {
                     i.putExtra(MsgHandler.KEY_VALUE, Boolean.getBoolean(value));
                 } else if (dataType.equals(Constants.SENSOR_DATA_TYPE_FLOAT)) {
                     i.putExtra(MsgHandler.KEY_VALUE, Float.parseFloat(value));
                 } else if (dataType.equals(Constants.SENSOR_DATA_TYPE_INT)) {
                     i.putExtra(MsgHandler.KEY_VALUE, Integer.parseInt(value));
                 } else {
                     i.putExtra(MsgHandler.KEY_VALUE, value);
                 }
                 context.startService(i);
                 // Log.v(TAG, sensorName + " data requeued.");
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Error in sending sensor data:", e);
         }
     }
 
     private boolean sendDataFromBuffer() {
 
         if (bufferCount > 0) {
             // Log.v(TAG, "Sending " + bufferCount + " bytes from local buffer to CommonSense");
             try {
                 int sentCount = 0;
                 int sentIndex = 0;
                 JSONArray names = buffer.names();
                 int max_post_sensor_data = MAX_POST_DATA;
                 for (int i = 0; i < names.length(); i++) {
                     JSONArray sensorArray = buffer.getJSONArray(names.getString(i));
                     JSONObject sensorData = new JSONObject();
                     JSONArray dataArray = new JSONArray();
                     if (((JSONObject) sensorArray.get(0)).getString("type").equalsIgnoreCase(
                             Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE)) {
                         max_post_sensor_data = MAX_POST_DATA_TIME_SERIE;
                     }
                     for (int x = sentIndex; x < sensorArray.length()
                             && sentCount - sentIndex < max_post_sensor_data; x++) {
                         JSONObject data = new JSONObject();
                         JSONObject sensor = (JSONObject) sensorArray.get(x);
                         data.put("value", sensor.getString("val"));
                         data.put("date", sensor.get("time"));
                         dataArray.put(data);
                         ++sentCount;
                     }
 
                     sensorData.put("data", dataArray);
                     JSONObject sensor = (JSONObject) sensorArray.get(0);
                     sendSensorData(sensor.getString("name"), sensorData, sensor.getString("type"),
                             sensor.getString("device"));
 
                     // if MAX_POST_DATA reached but their are still some items left, then do the
                     // rest --i;
                     if (sentCount != sensorArray.length()) {
                         --i;
                         sentIndex = sentCount;
                     } else {
                         sentIndex = sentCount = 0;
                     }
                 }
                 // Log.d(TAG, "Buffered sensor values sent OK");
                 buffer = new JSONObject();
                 bufferCount = 0;
             } catch (Exception e) {
                 Log.e(TAG, "Error sending data from buffer:" + e.getMessage());
             }
 
         } else {
             // TODO smart transmission scaling
         }
 
         return true;
     }
 
     private boolean sendDataFromDb() {
         Cursor c = null;
         boolean emptyDataBase = false;
         String limit = "90";
 
         try {
             // query the database
             openDb();
             String[] cols = { DbHelper.COL_ROWID, DbHelper.COL_JSON, DbHelper.COL_SENSOR };
             String sel = DbHelper.COL_ACTIVE + "!=\'true\'";
             while (!emptyDataBase) {
                 c = db.query(DbHelper.TABLE_NAME, cols, sel, null, null, null, DbHelper.COL_SENSOR,
                         limit);
 
                 if (c.getCount() > 0) {
                     // Log.v(TAG, "Sending " + c.getCount() + " values from DB to CommonSense");
 
                     // Send Data from each sensor
                     int sentCount = 0;
                     String sensorKey = "";
                     JSONObject sensorData = new JSONObject();
                     JSONArray dataArray = new JSONArray();
                     String sensorName = "";
                     String sensorType = "";
                     String sensorDevice = "";
                     c.moveToFirst();
                     int max_post_sensor_data = MAX_POST_DATA;
                     while (false == c.isAfterLast()) {
                         if (sensorType.equalsIgnoreCase(Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE)) {
                             max_post_sensor_data = MAX_POST_DATA_TIME_SERIE;
                         }
                         if (c.getString(2).compareToIgnoreCase(sensorKey) != 0
                                 || sentCount >= max_post_sensor_data) {
                             // send the in the previous rounds collected data
                             if (sensorKey.length() > 0) {
                                 sensorData.put("data", dataArray);
                                 sendSensorData(sensorName, sensorData, sensorType, sensorDevice);
                                 sensorData = new JSONObject();
                                 dataArray = new JSONArray();
                             }
                         }
 
                         JSONObject sensor = new JSONObject(c.getString(1));
                         JSONObject data = new JSONObject();
                         data.put("value", sensor.getString("val"));
                         data.put("date", sensor.get("time"));
                         if (dataArray.length() == 0) {
                             sensorName = sensor.getString("name");
                             sensorType = sensor.getString("type");
                             sensorDevice = sensor.getString("device");
                         }
                         dataArray.put(data);
                         sensorKey = c.getString(2);
                         // if last, then send
                         if (c.isLast()) {
                             sensorData.put("data", dataArray);
                             sendSensorData(sensorName, sensorData, sensorType, sensorDevice);
                         }
                         sentCount++;
                         c.moveToNext();
                     }
 
                     // Log.d(TAG, "Sensor values from database sent OK!");
 
                     // remove data from database
                     c.moveToFirst();
                     while (false == c.isAfterLast()) {
                         int id = c.getInt(c.getColumnIndex(DbHelper.COL_ROWID));
                         String where = DbHelper.COL_ROWID + "=?";
                         String[] whereArgs = { "" + id };
                         db.delete(DbHelper.TABLE_NAME, where, whereArgs);
                         c.moveToNext();
                     }
                     c.close();
                     c = null;
                 } else {
                     emptyDataBase = true;
                     // TODO smart transmission scaling
                 }
             }
         } catch (Exception e) {
             Log.e(TAG, "Error in sending data from database!", e);
             return false;
         } finally {
             if (c != null) {
                 c.close();
             }
             closeDb();
         }
         return true;
     }
 
     private void sendSensorData(final String sensorName, final JSONObject sensorData,
             final String dataType, final String deviceType) {
 
         try {
             if (nrOfSendMessageThreads >= MAX_NR_OF_SEND_MSG_THREADS) {
                 requeueMessage(sensorName, sensorData, dataType, deviceType, this);
 
             } else {
                 final SharedPreferences prefs = getSharedPreferences(Constants.AUTH_PREFS,
                         Context.MODE_PRIVATE);
                 String cookie = prefs.getString(Constants.PREF_LOGIN_COOKIE, "");
 
                 // check for sending a file
                 if (dataType.equals(Constants.SENSOR_DATA_TYPE_FILE)) {
                     if (nrOfSendMessageThreads < MAX_NR_OF_SEND_MSG_THREADS) {
                         ++nrOfSendMessageThreads;
 
                         // create handlerthread and run task on there
                         HandlerThread ht = new HandlerThread("sendFileThread");
                         ht.start();
                         new SendFileThread(cookie, sensorData, sensorName, dataType, deviceType,
                                 this, ht.getLooper()).sendEmptyMessage(0);
 
                     } else {
                         Log.w(TAG, "Maximum nr of send msg threads reached.");
                         requeueMessage(sensorName, sensorData, dataType, deviceType, this);
                     }
 
                 } else {
                     // start send thread
                     if (nrOfSendMessageThreads < MAX_NR_OF_SEND_MSG_THREADS) {
                         ++nrOfSendMessageThreads;
 
                         // create handlerthread and run task on there
                         HandlerThread ht = new HandlerThread("sendDataThread");
                         ht.start();
                         new SendDataThread(cookie, sensorData, sensorName, dataType, deviceType,
                                 this, ht.getLooper()).sendEmptyMessage(0);
 
                     } else {
                         Log.w(TAG, "Maximum nr of send msg threads reached");
                         requeueMessage(sensorName, sensorData, dataType, deviceType, this);
                     }
                 }
             }
 
         } catch (Exception e) {
             Log.e(TAG, "Error in sending sensor data:", e);
         }
     }
 }
