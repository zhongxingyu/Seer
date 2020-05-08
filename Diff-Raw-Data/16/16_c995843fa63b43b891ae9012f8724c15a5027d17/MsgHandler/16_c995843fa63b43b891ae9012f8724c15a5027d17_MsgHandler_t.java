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
 
 import org.json.JSONArray;
 import org.json.JSONException;
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
 import android.os.IBinder;
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
 
 	private class SendDataThread implements Runnable {
 		String cookie;
 		String url;
 		String sensorName;
 		String deviceType;
 		String dataType;
 		Context context;
 		JSONObject data;
 
 		public SendDataThread(String _url, String _cookie, JSONObject _data, String _sensorName, String _dataType, String _deviceType, Context _context) {
 			url = _url;
 			cookie = _cookie;
 			data = _data;
 			sensorName = _sensorName;
 			dataType = _dataType;
 			deviceType = _deviceType;
 			context = _context;
 		}
 
 		@Override
 		public void run() {
 
 			if (url == null) {
 				return;
 			}
 
 			// get sensor ID from the URL
 			int start = url.indexOf("/sensors/") + "/sensors/".length();
 			int end = url.indexOf("/data");
 			String sensorId = url.substring(start, end);
 
 			try {
 				HashMap<String, String> response = SenseApi.sendJson(new URL(url), data, "POST", cookie);
 				// Error when sending
 				if(response == null || response.get("http response code").compareToIgnoreCase("201") != 0 )
 				{
 					// if un-authorized relogin
 					if(response != null && response.get("http response code").compareToIgnoreCase("403") == 0)
 					{
 						final Intent serviceIntent = new Intent(ISenseService.class.getName());
 						serviceIntent.putExtra(SenseService.ACTION_RELOGIN, true);
 						context.startService(serviceIntent);
 					}	 
 					// connection error put all the messages back in the queue					   
 					requeueMessage(sensorName, data, dataType, deviceType, context);
 
 					// Show the HTTP response Code
 					if(response != null)
 						Log.e(TAG, "Sending sensor data failed. Sensor: " + sensorId
 								+ ", HTTP response code:" + response.get("http response code")
 								+ ",  content: \'" + response.get("content") + "\'\n message requeued");
 					else
 						Log.e(TAG, "Sending sensor data failed. Sensor: " + sensorId +" message requeued.");              
 				}     
 				// Data sent successfully
 				else 
 				{
 					int bytes = data.toString().getBytes().length;
 					Log.v(TAG, "Sent sensor data! Sensor ID: " + sensorId + ", raw data size: "
 							+ bytes + " bytes");
 				}
 				// Log.d(TAG, "  data: " + data);
 			} catch (Exception e) {
 				if (null != e.getMessage()) {
 					Log.e(TAG, "Exception sending sensor data: " + e.getMessage());
 				} else {
 					Log.e(TAG, "Exception sending sensor data!", e);
 				}
 			} finally {
 				--nrOfSendMessageThreads;
 			}
 		}
 	}
 
 	private class SendFileThread implements Runnable {
 		String cookie;
 		String urlStr;
 		String fileName;
 
 		public SendFileThread(String _url, String _cookie, String _fileName) {
 			urlStr = _url;
 			cookie = _cookie;
 			fileName = _fileName;
 		}
 
 		@Override
 		public void run() {
 			try {
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
 
 				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
 
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
 
 				if (conn.getResponseCode() != 201)
 					Log.e(TAG, "Sending file failed. Response code:" + conn.getResponseMessage());
 				else
 					Log.v(TAG, "Sent file data OK!");
 			} catch (Exception e) {
 				Log.e(TAG, "Sending sensor file failed:" + e.getMessage());
 			} finally {
 				--nrOfSendMessageThreads;
 			}
 		}
 	}
 
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
 	private static final int MAX_POST_DATA_TIME_SERIE = 3; 
 	private static final String TAG = "Sense MsgHandler";
 	private JSONObject buffer;
 	private int bufferCount;
 	private SQLiteDatabase db;
 	private DbHelper dbHelper;
 	private boolean isDbOpen;
 	private int nrOfSendMessageThreads = 0;
 	private ChildThread sendThread, getSensorUrlTread;
 
 	/**
 	 * Buffers data in the memory
 	 * 
 	 * @param json
 	 *            the data to buffer
 	 */
 	private void bufferData(JSONObject json) {
 
 		// Log.d(TAG, "Buffer new sensor data");
 		int jsonBytes = json.toString().length();
 		// check if there is room in the buffer
 		if (this.bufferCount+jsonBytes >= MAX_BUFFER) {
 			// empty buffer into database
 			Log.d(TAG, "Buffer overflow! Emptying buffer to database");
 			emptyBufferToDb();
 		}
 		try {
 			// put data in buffer
 			String sensorKey = json.getString("name") + "_"
 			+ json.optString("device", json.getString("name"));
 			JSONArray dataArray = buffer.optJSONArray(sensorKey);
 			if (dataArray == null)
 				dataArray = new JSONArray();
 			dataArray.put(json);
 			buffer.put(sensorKey, dataArray);
 			this.bufferCount += jsonBytes;
 		} catch (Exception e) {
 			Log.e(TAG, "Error in buffering data:" + e.getMessage());
 		}
 	}
 
 	private void closeDb() {
 		if (true == this.isDbOpen) {
 			this.dbHelper.close();
 			this.isDbOpen = false;
 		}
 	}
 
 	/**
 	 * Puts data from the buffer in the flash database for long-term storage
 	 */
 	private void emptyBufferToDb() {
 		Log.v(TAG, "Emptying buffer to persistant database...");
 
 		try {
 			openDb();
 			JSONArray names = buffer.names();
 			if(names == null)
 				return;
 			for (int i = 0; i < names.length(); i++) {
 				JSONArray sensorArray = buffer.getJSONArray(names.getString(i));
 
 				for (int x = 0; x < sensorArray.length(); x++) {
 					ContentValues values = new ContentValues();
 					values.put(DbHelper.COL_JSON, ((JSONObject) sensorArray.get(x)).toString());
 					values.put(DbHelper.COL_SENSOR, names.getString(i));
 					values.put(DbHelper.COL_ACTIVE, false);
 					this.db.insert(DbHelper.TABLE_NAME, null, values);
 				}
 			}
 			// reset buffer
 			this.bufferCount = 0;
 			this.buffer = new JSONObject();
 		} catch (Exception e) {
 			Log.e(TAG, "Error storing buffer in persistant database!", e);
 		} finally {
 			closeDb();
 		}
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
 
 		String name = intent.getStringExtra(KEY_SENSOR_NAME);
 		String type = intent.getStringExtra(KEY_DATA_TYPE);
 		long time = intent.getLongExtra(KEY_TIMESTAMP, System.currentTimeMillis());
 		String device = intent.getStringExtra(KEY_SENSOR_DEVICE);
 		JSONObject json = new JSONObject();
 
 		try {
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
 			NumberFormat formatter = new DecimalFormat("##########.##", otherSymbols);
 			json.put("name", name);
 			json.put("time", formatter.format(((double) time) / 1000.0d));
 			json.put("type", type);
 			json.put("device", (null == device) ? name : device);
 
 			if (type.equals(Constants.SENSOR_DATA_TYPE_BOOL)) {
 				json.put("val", intent.getBooleanExtra(KEY_VALUE, false));
 			} else if (type.equals(Constants.SENSOR_DATA_TYPE_FLOAT)) {
 				json.put("val", intent.getFloatExtra(KEY_VALUE, Float.MIN_VALUE));
 			} else if (type.equals(Constants.SENSOR_DATA_TYPE_INT)) {
 				json.put("val", intent.getIntExtra(KEY_VALUE, Integer.MIN_VALUE));
 			} else if (type.equals(Constants.SENSOR_DATA_TYPE_JSON)) {
 				json.put("val", new JSONObject(intent.getStringExtra(KEY_VALUE)));
 			} else if (type.equals(Constants.SENSOR_DATA_TYPE_STRING)) {
 				json.put("val", intent.getStringExtra(KEY_VALUE));
 			} else if (type.equals(Constants.SENSOR_DATA_TYPE_FILE)) {
 				json.put("val", intent.getStringExtra(KEY_VALUE));
 			} else if (type.equals(Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE)) {
 				json.put("val", new JSONObject(intent.getStringExtra(KEY_VALUE)));
 			} else {
 				Log.e(TAG, "Unexpected data type: " + type);
 				return;
 			}
 
 			// check if there is connectivity
 			if (isOnline()) 
 			{
 				// check if the app is in real-time sending mode
 				final SharedPreferences mainPrefs = getSharedPreferences(Constants.MAIN_PREFS,
 						MODE_WORLD_WRITEABLE);
 				final int rate = Integer.parseInt(mainPrefs.getString(Constants.PREF_SYNC_RATE, "0"));
 				if (rate == -2) 
 				{
 					// real time mode, send immediately
 					String value = "";
 					if (type.equals(Constants.SENSOR_DATA_TYPE_BOOL)) {
 						value += intent.getBooleanExtra(KEY_VALUE, false);
 					} else if (type.equals(Constants.SENSOR_DATA_TYPE_FLOAT)) {
 						value += intent.getFloatExtra(KEY_VALUE, Float.MIN_VALUE);
 					} else if (type.equals(Constants.SENSOR_DATA_TYPE_INT)) {
 						value += intent.getIntExtra(KEY_VALUE, Integer.MIN_VALUE);
 					} else if (type.equals(Constants.SENSOR_DATA_TYPE_JSON)	|| type.equals(Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE)) {
 						value += new JSONObject(intent.getStringExtra(KEY_VALUE)).toString();						
 					} else if (type.equals(Constants.SENSOR_DATA_TYPE_STRING)) {
 						value += intent.getStringExtra(KEY_VALUE);
 					} else if (type.equals(Constants.SENSOR_DATA_TYPE_FILE)) {
 						value += intent.getStringExtra(KEY_VALUE);
 					}
 					JSONObject sensorData = new JSONObject();
 					JSONArray dataArray = new JSONArray();
 					JSONObject data = new JSONObject();
 					data.put("value", value);
 					data.put("date", formatter.format(((double) time) / 1000.0d));
 					dataArray.put(data);
 					sensorData.put("data", dataArray);
 					sendSensorData(name, sensorData, type, device);                
 				} 
 				else 
 				{
 					// normal mode, buffer to memory
 					bufferData(json);
 				}
 			} 
 			else 
 			{
 				// buffer data if there is no connectivity
 				bufferData(json);
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "JSONException before putting message in buffer database", e);
 			return;
 		}
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
 		return ((null != info) && (info.isConnected()));
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// you cannot bind to this service
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d(TAG, "onCreate MsgHandler");
 		sendThread =  new ChildThread();
 		sendThread.start();
 		getSensorUrlTread = new ChildThread();
 		getSensorUrlTread.start();
 		this.buffer = new JSONObject();
 		this.bufferCount = 0;
 	}
 
 	@Override
 	public void onDestroy() {
 		// Log.d(TAG, "onDestroy");
 
 		emptyBufferToDb();
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
 		if (false == this.isDbOpen) {
 			this.dbHelper = new DbHelper(this);
 			this.db = this.dbHelper.getWritableDatabase();
 			this.isDbOpen = true;
 		}
 	}
 
 	private boolean sendDataFromBuffer() {
 
 		if (this.bufferCount > 0) {
 			Log.v(TAG, "Sending " + this.bufferCount + " bytes from local buffer to CommonSense");
 			try {
 				int sentCount = 0;
 				int sentIndex = 0;
 				JSONArray names = buffer.names();
 				int max_post_sensor_data = MAX_POST_DATA;
 				for (int i = 0; i < names.length(); i++) {
 					JSONArray sensorArray = buffer.getJSONArray(names.getString(i));
 					JSONObject sensorData = new JSONObject();
 					JSONArray dataArray = new JSONArray();
 					if(((JSONObject) sensorArray.get(0)).getString("type").equalsIgnoreCase(Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE))
 						max_post_sensor_data = MAX_POST_DATA_TIME_SERIE;
 					for (int x = sentIndex; x < sensorArray.length() && (sentCount - sentIndex) < max_post_sensor_data; x++) {
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
 					} else
 						sentIndex = sentCount = 0;
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
 
 	private boolean sendDataFromDb() 
 	{
 		Cursor c = null;
 		boolean emptyDataBase = false;
 		String limit = "90";
 
 		try {
 			// query the database
 			openDb();
 			String[] cols = { DbHelper.COL_ROWID, DbHelper.COL_JSON, DbHelper.COL_SENSOR };
 			String sel = DbHelper.COL_ACTIVE + "!=\'true\'";
 			while(!emptyDataBase)
 			{
 				c = this.db.query(DbHelper.TABLE_NAME, cols, sel, null, null, null,
 						DbHelper.COL_SENSOR, limit);
 
 				if (c.getCount() > 0) {
 					Log.v(TAG, "Sending " + c.getCount() + " values from database to CommonSense");
 
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
 					while (false == c.isAfterLast()) 
 					{
 						if(sensorType.equalsIgnoreCase(Constants.SENSOR_DATA_TYPE_JSON_TIME_SERIE))
 							max_post_sensor_data = MAX_POST_DATA_TIME_SERIE;
 						if (c.getString(2).compareToIgnoreCase(sensorKey) != 0  || sentCount >= max_post_sensor_data) {
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
 						this.db.delete(DbHelper.TABLE_NAME, where, whereArgs);
 						c.moveToNext();
 					}
 					c.close();
 					c = null;
 				} 
 				else {    			
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
 
 	public void sendSensorData(final String sensorName, final JSONObject sensorData, final String dataType, final String deviceType) {
 		try {
 			// double check device type
 			final Context context = this;
 			// Check if the ChildThread is available, should always be...
 			if(getSensorUrlTread == null)
 			{
 				getSensorUrlTread =  new ChildThread();
 				getSensorUrlTread.start();
 				Log.d(TAG, "Get Sensor URL thread is null");
 				while(getSensorUrlTread.handler == null)
 					Thread.sleep(100);
 			}
 			if (nrOfSendMessageThreads >= MAX_NR_OF_SEND_MSG_THREADS) 
 				requeueMessage(sensorName, sensorData, dataType, deviceType, context);
 			else
 			getSensorUrlTread.handler.post(new Runnable() {
 				@Override
 				public void run() {
 					++nrOfSendMessageThreads;
 					try{
 						final String f_deviceType = deviceType != null ? deviceType : sensorName;
 						String dataStructure = (String) ((JSONObject) ((JSONArray) sensorData.get("data")).get(0)).get("value");
 						String url = SenseApi.getSensorUrl(context, sensorName, dataStructure, dataType, f_deviceType);
 
 						final SharedPreferences prefs = getSharedPreferences(Constants.AUTH_PREFS,
 								Context.MODE_PRIVATE);
 						String cookie = prefs.getString(Constants.PREF_LOGIN_COOKIE, "");
 
 						// check for sending a file
 						if (dataType.equals(Constants.SENSOR_DATA_TYPE_FILE)) 
 						{
 							JSONArray data = ((JSONArray) sensorData.get("data"));
 							for (int i = 0; i < data.length(); i++) {
 								JSONObject object = (JSONObject) data.get(i);
 								// start send thread
 								if (nrOfSendMessageThreads < MAX_NR_OF_SEND_MSG_THREADS) {
 									++nrOfSendMessageThreads;
 									new Thread(new SendFileThread(url, cookie, (String) object.get("value")))
 									.start();
 								}
 								else
 								{
 									--i;
 									Log.e(TAG, "Maximum nr of send msg threads reached.");
 									Thread.sleep(1); // sleep is deadly remove!!
 								}
 								// TODO: wait until data can be send
 							}
 						} 
 						else 
 						{
 							// start send thread
 							if (nrOfSendMessageThreads < MAX_NR_OF_SEND_MSG_THREADS) 
 							{
 								++nrOfSendMessageThreads;
 								if(sendThread == null)
 								{
 									sendThread =  new ChildThread();
 									sendThread.start();
 									Log.d(TAG, "Send thread is null");
 								}
 								sendThread.handler.post(new SendDataThread(url, cookie, sensorData, sensorName, dataType, f_deviceType, context));
 							}
 							else
 							{                	
 								Log.e(TAG, "Maximum nr of send msg threads reached");    
 								requeueMessage(sensorName, sensorData, dataType, deviceType, context);
 							}
 						}
 					}
 					catch (Exception e) {
 						Log.e(TAG, "Error in sending sensor data:", e);						
 					}
 					--nrOfSendMessageThreads;
 				}
 			});
 		} catch (Exception e) {
 			Log.e(TAG, "Error in sending sensor data:", e);
 		}
 	}
 
 	private void requeueMessage(String sensorName, JSONObject data, String dataType, String deviceType,  Context context)
 	{
 		try
 		{
 			JSONArray dataArray = data.getJSONArray("data");                	
 			for (int index = 0; index < dataArray.length(); index++) 
			{	
 				Intent i = new Intent(MsgHandler.ACTION_NEW_MSG);
 				i.putExtra(MsgHandler.KEY_SENSOR_NAME, sensorName);
				i.putExtra(MsgHandler.KEY_SENSOR_DEVICE, deviceType);			
 				i.putExtra(MsgHandler.KEY_DATA_TYPE, dataType);
 				i.putExtra(MsgHandler.KEY_TIMESTAMP, (long)(((float)Double.parseDouble(dataArray.getJSONObject(index).getString("date")))*1000f));
				String value = dataArray.getJSONObject(index).getString("value");
				if (dataType.equals(Constants.SENSOR_DATA_TYPE_BOOL)) {
					i.putExtra(MsgHandler.KEY_VALUE, Boolean.getBoolean(value));
				} else if (dataType.equals(Constants.SENSOR_DATA_TYPE_FLOAT)) {
					i.putExtra(MsgHandler.KEY_VALUE,Float.parseFloat(value));
				} else if (dataType.equals(Constants.SENSOR_DATA_TYPE_INT)) {
					i.putExtra(MsgHandler.KEY_VALUE,Integer.parseInt(value));
				} else				
					i.putExtra(MsgHandler.KEY_VALUE, value);
 				context.startService(i);
 				Log.d(TAG, sensorName+" data requeued.");
 			}
 
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Error in sending sensor data:", e);
 		}
 	}
 
 	public void sendSensorData(String sensorName, String sensorValue, String dataType, String deviceType) {
 		// Log.d(TAG, "Send new sensor data");
 		try {
 			JSONObject sensorData = new JSONObject();
 			JSONArray dataArray = new JSONArray();
 			JSONObject data = new JSONObject();
 			data.put("value", sensorValue);
 			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
 			// otherSymbols.setDecimalSeparator('.');
 			// otherSymbols.setGroupingSeparator('.');
 			NumberFormat formatter = new DecimalFormat("##########.##", otherSymbols);
 			data.put("date", formatter.format(((double) System.currentTimeMillis()) / 1000.0d));
 			dataArray.put(data);
 			sensorData.put("data", dataArray);
 			sendSensorData(sensorName, sensorData, dataType, deviceType);
 		} catch (Exception e) {
 			Log.e(TAG, "Error in creating JSON POST data:" + e.getMessage());
 		}
 	}
 }
