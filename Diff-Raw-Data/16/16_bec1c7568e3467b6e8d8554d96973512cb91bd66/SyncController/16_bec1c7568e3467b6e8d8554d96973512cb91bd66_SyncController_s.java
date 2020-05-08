 package org.kvj.lima1.sync.controller;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.kvj.lima1.sync.Lima1SyncApp;
 import org.kvj.lima1.sync.LoginForm;
 import org.kvj.lima1.sync.PJSONObject;
 import org.kvj.lima1.sync.QueryOperator;
 import org.kvj.lima1.sync.R;
 import org.kvj.lima1.sync.SyncServiceInfo;
 import org.kvj.lima1.sync.controller.data.AppInfo;
 import org.kvj.lima1.sync.controller.data.FKey;
 import org.kvj.lima1.sync.controller.data.TableInfo;
 import org.kvj.lima1.sync.controller.net.HttpClientTransport;
 import org.kvj.lima1.sync.controller.net.NetTransport.NetTransportException;
 import org.kvj.lima1.sync.controller.net.OAuthProvider;
 import org.kvj.lima1.sync.controller.net.OAuthProvider.OAuthProviderListener;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class SyncController implements OAuthProviderListener {
 
 	public static interface SyncControllerListener {
 
 		public void syncStarted();
 
 		public void syncCompleted(String error);
 	}
 
 	class AutoSyncTask extends TimerTask {
 
 		private String app;
 
 		public AutoSyncTask(String app) {
 			this.app = app;
 		}
 
 		@Override
 		public void run() {
 			sync(app);
 		}
 
 	}
 
 	private static final int LOGIN_ID = 1;
 	private static int AUTO_SYNC_SEC_DEFAULT = 30;
 	private static final String TAG = "Sync";
 	private static final int TYPE_NO_NETWORK = -1;
 	private HttpClientTransport transport;
 	private OAuthProvider net;
 	private Map<String, AppInfo> infos = new HashMap<String, AppInfo>();
 	private SyncControllerListener listener = null;
 	private Map<String, AutoSyncTask> autoSyncTasks = new HashMap<String, AutoSyncTask>();
 	private Map<String, Boolean> autoSyncLocks = new HashMap<String, Boolean>();
 	private Timer autoSyncTimer = new Timer("AutoSync");
 	private int imageWidth = 0;
 	private Lima1SyncApp context = null;
 
 	public SyncController(Lima1SyncApp context) {
 		this.context = context;
 		this.transport = new HttpClientTransport();
 		transport.setURL(context, "https://lima1-kvj.rhcloud.com");
 		this.net = new OAuthProvider(transport, "lima1android", context.getStringPreference(R.string.token,
 				R.string.tokenDefault), this);
 
 		imageWidth = Math.max(context.getResources().getDisplayMetrics().widthPixels, context.getResources()
 				.getDisplayMetrics().heightPixels);
 	}
 
 	@Override
 	public void onNeedToken() {
 		NotificationManager notificationManager = (NotificationManager) Lima1SyncApp.getInstance().getSystemService(
 				Context.NOTIFICATION_SERVICE);
 		Notification notification = new Notification(R.drawable.ic_login, "Lima1", System.currentTimeMillis());
 		PendingIntent intent = PendingIntent.getActivity(Lima1SyncApp.getInstance(), 0,
 				new Intent(Lima1SyncApp.getInstance(), LoginForm.class), PendingIntent.FLAG_CANCEL_CURRENT);
 		notification.setLatestEventInfo(Lima1SyncApp.getInstance(), "Lima1", "Login/password required", intent);
 		notification.defaults = Notification.DEFAULT_ALL;
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		notificationManager.notify(LOGIN_ID, notification);
 	}
 
 	public String verifyToken(String username, String password) {
 		try {
 			Log.i(TAG, "Verify: " + username + ", " + password);
 			String token = net.tokenByUsernamePassword(username, password);
 			Lima1SyncApp.getInstance().setStringPreference(R.string.token, token);
 			return null;
 		} catch (NetTransportException e) {
 			e.printStackTrace();
 			return e.getMessage();
 		}
 	}
 
 	private int hasConnection() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
 
 		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if (wifiNetwork != null && wifiNetwork.isConnected()) {
 			return ConnectivityManager.TYPE_WIFI;
 		}
 
 		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		if (mobileNetwork != null && mobileNetwork.isConnected()) {
 			return ConnectivityManager.TYPE_WIFI;
 		}
 
 		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 		if (activeNetwork != null && activeNetwork.isConnected()) {
 			return activeNetwork.getType();
 		}
 
 		return TYPE_NO_NETWORK;
 	}
 
 	public String sync(final String app) {
		int networkType = hasConnection();
		if (TYPE_NO_NETWORK == networkType) { // No connection
			Log.w(TAG, "Skipping sync because of no network");
			return null;
		}
		Log.i(TAG, "Network: " + networkType);
 		final AppInfo info = getInfo(app);
 		if (null == info.db) {
 			Log.e(TAG, "DB error");
 			return null;
 		}
 		synchronized (autoSyncLocks) { // Lock
 			Boolean syncNow = autoSyncLocks.get(app);
 			Log.i(TAG, "Sync lock: " + syncNow + ", " + app);
 			if (null != syncNow && syncNow) { // Already started
 				return null;
 			}
 			autoSyncLocks.put(app, true);
 		}
 		final AutoSyncTask task;
 		synchronized (autoSyncTasks) { // Remove itself
 			task = autoSyncTasks.get(app);
 			autoSyncTasks.put(app, null); // Removed
 		}
 		final String id = "sync" + info.id();
 		Thread syncThread = new Thread() {
 
 			@Override
 			public void run() {
 				String syncResult = _sync(info, app);
 				if (null != task) { // Not empty
 					task.cancel();
 				}
 				synchronized (autoSyncLocks) { // Unlock
 					autoSyncLocks.put(app, false);
 				}
 				Intent syncFinish = new Intent(SyncServiceInfo.SYNC_FINISH_INTENT);
 				syncFinish.putExtra(SyncServiceInfo.KEY_APP, app);
 				syncFinish.putExtra(SyncServiceInfo.KEY_TOKEN, id);
 				syncFinish.putExtra(SyncServiceInfo.KEY_RESULT, syncResult);
 				context.sendBroadcast(syncFinish);
 			}
 		};
 		syncThread.start();
 		return id;
 	}
 
 	private String _sync(AppInfo info, String app) {
 		if (null != listener) {
 			listener.syncStarted();
 		}
 		try {
 			info.db.getDatabase().beginTransaction();
 			JSONObject newSchema = net.rest(app, "/rest/schema?", null);
 			// Log.i(TAG, "Schema: " + newSchema);
 			boolean fullSync = false;
 			long inFrom = 0;
 			long outFrom = 0;
 			int itemSent = 0;
 			int itemReceived = 0;
 			Cursor c = info.db.getDatabase().query("updates", new String[] { "version_in", "version_out" }, null, null,
 					null, null, "id desc", "1");
 			if (c.moveToFirst()) {
 				inFrom = c.getLong(0);
 				outFrom = c.getLong(1);
 			}
 			c.close();
 			if (null == info.schemaInfo) {
 				// Save schema, reset DB
 				fullSync = true;
 				outFrom = 0;
 			}
 			String upgradeResult = info.upgradeSchema(newSchema);
 			if (null != upgradeResult) { // Upgrade failed
 				throw new Exception(upgradeResult);
 			}
 			// Upload files
 			c = info.db.getDatabase().query("uploads", new String[] { "id", "name", "path", "status" }, null, null,
 					null, null, "id");
 			if (c.moveToFirst()) { // Have data
 				File folder = info.getFilesFolder();
 				if (null == folder) { // No cache
 					Log.e(TAG, "No cache folder: " + app);
 					throw new Exception("Cache folder is not accessible");
 				}
 				do {
 					String id = c.getString(0);
 					String name = c.getString(1);
 					int status = c.getInt(3);
 					if (3 == status) { // Remove
 						String uri = String.format("/rest/file/remove?name=%s&", name);
 						net.rest(app, uri, null);
 						removeCacheFile(info, name);
 					} else {
 						File file = new File(folder, name);
 						if (file.exists()) { // Still here - upload
 							String uri = String.format("/rest/file/upload?name=%s&", name);
 							Map<String, Object> params = new HashMap<String, Object>();
 							params.put("file", new FileInputStream(file));
 							net.rest(app, uri, params);
 							removeCacheFile(info, name);
 						}
 					}
 					info.db.getDatabase().delete("uploads", "id=?", new String[] { id });
 				} while (c.moveToNext());
 			}
 			c.close();
 			// Send changes
 			int slots = 10;
 			JSONArray result = new JSONArray();
 			int slotsUsed = 0;
 			int index = 0;
 			StringBuffer sqls = new StringBuffer();
 			List<String> sqlArgs = new ArrayList<String>();
 			for (String stream : info.schemaInfo.tables.keySet()) {
 				// For every table
 				if (index > 0) { // Add union
 					sqls.append(" union ");
 				}
 				sqls.append("select id, '" + stream + "' stream, data, updated, status from t_" + stream
 						+ " where own=? and updated>?");
 				sqlArgs.add("1");
 				sqlArgs.add(Long.toString(inFrom));
 				index++;
 			}
 			sqls.append(" order by updated");
 			c = info.db.getDatabase().rawQuery(sqls.toString(), sqlArgs.toArray(new String[0]));
 			int slotsNeeded = 1;
 			if (c.moveToFirst()) {
 				do {
 					slotsUsed += slotsNeeded;
 					JSONObject json = new JSONObject();
 					if (slotsUsed > slots) {
 						// Send
 						json.put("a", result);
 						net.rest(app, "/rest/in?", json);
 						slotsUsed = 0;
 						result = new JSONArray();
 						json = new JSONObject();
 					}
 					json.put("s", c.getString(1));
 					json.put("st", c.getInt(4));
 					json.put("u", c.getLong(3));
 					json.put("o", c.getString(2));
 					json.put("i", c.getLong(0));
 					result.put(json);
 					itemSent++;
 					inFrom = c.getLong(3);
 				} while (c.moveToNext());
 			}
 			Log.i(TAG, "In data: " + result.length());
 			if (result.length() > 0) {
 				JSONObject json = new JSONObject();
 				json.put("a", result);
 				net.rest(app, "/rest/in?", json);
 			}
 			c.close();
 			// Receive data
 			while (true) {
 				String url = String.format("/rest/out?from=%d&%s", outFrom, fullSync ? "" : "inc=yes&");
 				JSONObject res = net.rest(app, url, null);
 				JSONArray arr = res.getJSONArray("a");
 				if (arr.length() == 0) {
 					outFrom = res.getLong("u");
 					break;
 				}
 				for (int i = 0; i < arr.length(); i++) {
 					JSONObject object = arr.getJSONObject(i);
 					outFrom = object.getLong("u");
 					JSONObject obj = new JSONObject(object.getString("o"));
 					create(app, object.getString("s"), obj, object.getInt("st"), object.getLong("u"));
 					itemReceived++;
 				}
 			}
 			// Finish sync: insert updates, remove removed data
 			ContentValues values = new ContentValues();
 			values.put("id", info.id());
 			values.put("version_in", inFrom);
 			values.put("version_out", outFrom);
 			info.db.getDatabase().insert("updates", null, values);
 			for (String stream : info.schemaInfo.tables.keySet()) {
 				info.db.getDatabase().delete("t_" + stream, "status=? and updated<=?",
 						new String[] { "3", Long.toString(inFrom) });
 			}
 			info.db.getDatabase().setTransactionSuccessful();
 			info.setSchema(newSchema);
 			Log.i(TAG, "Sync done: out: " + itemSent + ", in: " + itemReceived);
 			if (null != listener) {
 				listener.syncCompleted(null);
 			}
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (null != listener) {
 				listener.syncCompleted("Error in sync");
 			}
 			return e.getMessage();
 		} finally {
 			info.db.getDatabase().endTransaction();
 		}
 	}
 
 	private void scheduleAutoSync(String app) {
 		synchronized (autoSyncTasks) { // Lock
 			AutoSyncTask task = autoSyncTasks.get(app);
 			if (null != task) { // Not null
 				task.cancel();
 			}
 			task = new AutoSyncTask(app);
 			autoSyncTasks.put(app, task);
 			autoSyncTimer.schedule(task, 1000 * AUTO_SYNC_SEC_DEFAULT);
 		}
 	}
 
 	private Long create(String app, String stream, JSONObject obj, int status, Long updated) throws Exception {
 		AppInfo info = getInfo(app);
 		ContentValues values = new ContentValues();
 		TableInfo tinfo = info.getTableInfo(stream);
 		if (null == tinfo) {
 			throw new Exception("Not synchronized");
 		}
 		if (!obj.has("id")) {
 			obj.put("id", info.id());
 		}
 		values.put("id", obj.getLong("id"));
 		values.put("status", status);
 		values.put("data", obj.toString());
 		if (null != updated) {
 			values.put("updated", updated.longValue());
 			values.put("own", 0);
 		} else {
 			values.put("updated", info.id());
 			values.put("own", 1);
 			// Log.i(TAG, "Create/update: " + obj.getLong("id") + ", " +
 			// values.getAsLong("updated"));
 		}
 		for (String field : tinfo.numbers) { // Add numbers
 			values.put("f_" + field, obj.optLong(field));
 		}
 		for (String field : tinfo.texts) { // Add texts
 			values.put("f_" + field, obj.optString(field));
 		}
 		Cursor c = info.db.getDatabase().query("t_" + stream, new String[] { "id" }, "id=?",
 				new String[] { Long.toString(obj.getLong("id")) }, null, null, null);
 		if (c.moveToFirst()) { // Found - update
 			info.db.getDatabase().update("t_" + stream, values, "id=?",
 					new String[] { Long.toString(obj.getLong("id")) });
 		} else {
 			info.db.getDatabase().insert("t_" + stream, null, values);
 		}
 		c.close();
 		return obj.optLong("id");
 	}
 
 	private synchronized AppInfo getInfo(String app) {
 		AppInfo info = infos.get(app);
 		if (null == info) {
 			info = new AppInfo(Lima1SyncApp.getInstance(), app);
 			infos.put(app, info);
 		}
 		return info;
 	}
 
 	public PJSONObject createUpdate(String app, String stream, PJSONObject obj) {
 		AppInfo info = getInfo(app);
 		TableInfo tinfo = info.getTableInfo(stream);
 		if (null == tinfo) {
 			Log.e(TAG, "No DB");
 			return null;
 		}
 		try {
 			info.db.getDatabase().beginTransaction();
 			create(app, stream, obj, 1, null);
 			info.db.getDatabase().setTransactionSuccessful();
 			scheduleAutoSync(app);
 			return obj;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			info.db.getDatabase().endTransaction();
 		}
 	}
 
 	public PJSONObject remove(String app, String stream, PJSONObject obj) {
 		AppInfo info = getInfo(app);
 		TableInfo tinfo = info.getTableInfo(stream);
 		if (null == tinfo) {
 			Log.e(TAG, "No DB");
 			return null;
 		}
 		try {
 			info.db.getDatabase().beginTransaction();
 			ContentValues values = new ContentValues();
 			values.put("status", 3);
 			values.put("updated", info.id());
 			values.put("own", 1);
 			info.db.getDatabase().update("t_" + stream, values, "id=?", new String[] { obj.optString("id") });
 			info.db.getDatabase().setTransactionSuccessful();
 			scheduleAutoSync(app);
 			return obj;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			info.db.getDatabase().endTransaction();
 		}
 	}
 
 	public PJSONObject removeCascade(String app, String stream, PJSONObject obj) {
 		AppInfo info = getInfo(app);
 		TableInfo tinfo = info.getTableInfo(stream);
 		if (null == tinfo) {
 			Log.e(TAG, "No DB");
 			return null;
 		}
 		long id = obj.optLong("id");
 		for (FKey fkey : tinfo.fkeys) { // Remove cascade for every fkey
 			PJSONObject[] items = query(app, fkey.table, new QueryOperator[] { new QueryOperator(fkey.field, id) },
 					null, null);
 			if (null == items) { // Error selecting
 				Log.w(TAG, "Error selecting from: " + fkey.table + "::" + fkey.field);
 				return null;
 			}
 			Log.i(TAG, "Remove cascade: " + fkey.table + "::" + fkey.field + ": " + items.length);
 			for (PJSONObject object : items) { // Remove
 				PJSONObject result = removeCascade(app, fkey.table, object);
 				if (null == result) {
 					Log.e(TAG, "Error removing item: " + object);
 					return null;
 				}
 			}
 		}
 		return remove(app, stream, obj);
 	}
 
 	private int jsonIndexOf(JSONArray arr, String value) throws JSONException {
 		if (null == arr) {
 			return -1;
 		}
 		for (int i = 0; i < arr.length(); i++) {
 			if (arr.getString(i).equals(value)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private String parseOrder(String order, String def, TableInfo tinfo) throws JSONException {
 		if (TextUtils.isEmpty(order)) {
 			return def;
 		}
 		StringBuilder buffer = new StringBuilder();
 		String[] parts = order.split(",");
 		for (int i = 0; i < parts.length; i++) {
 			String part = parts[i].trim();
 			String[] arr = part.split("\\s");
 			String field = arr[0];
 			if (tinfo.numbers.contains(field) || tinfo.texts.contains(field)) {
 				field = "f_" + field;
 			}
 			Log.i(TAG, "parseOrder: " + part + ", " + field);
 			if (i > 0) {
 				buffer.append(", ");
 			}
 			buffer.append(field);
 			if (arr.length > 1) {
 				buffer.append(" " + arr[1]);
 			}
 		}
 		return buffer.toString();
 	}
 
 	private String arrayToQuery(QueryOperator[] arr, List<String> values, String orand, TableInfo info)
 			throws JSONException {
 		StringBuilder buffer = new StringBuilder();
 		for (int i = 0, fields = 0; i < arr.length; i++) {
 			QueryOperator op = arr[i];
 			String field = null;
 			if (info.numbers.contains(op.getName())) {
 				field = "f_" + op.getName();
 			} else {
 				if (info.texts.contains(op.getName())) {
 					field = "f_" + op.getName();
 				}
 			}
 			if ("id".equals(op.getName())) {
 				field = "id";
 			}
 			if ("updated".equals(op.getName())) {
 				field = "updated";
 			}
 			if (null != field) {
 				if (fields++ > 0) {
 					buffer.append(" " + orand + " ");
 				}
 				buffer.append(field);
 				buffer.append(op.getOperator());
 				buffer.append("?");
 				values.add(op.getValue());
 			}
 		}
 		return buffer.toString();
 	}
 
 	public PJSONObject[] query(String app, String stream, QueryOperator[] ops, String order, String limit) {
 		AppInfo info = getInfo(app);
 		if (null == info.db) {
 			Log.e(TAG, "No DB: " + app);
 			return null;
 		}
 		TableInfo tinfo = info.getTableInfo(stream);
 		if (null == tinfo) {
 			Log.e(TAG, "Unsupported stream: " + stream + "::" + app);
 			return null;
 		}
 		try {
 			List<String> values = new ArrayList<String>();
 			values.add("3");
 			String where = "status<>?";
 			if (null != ops) {
 				String cond = arrayToQuery(ops, values, "and", tinfo);
 				if (!TextUtils.isEmpty(cond)) {
 					where += " and (" + cond + ")";
 				}
 			}
 			// Log.i(TAG, "Query: " + stream + ", " + where + ", " + values);
 			Cursor c = info.db.getDatabase().query("t_" + stream, new String[] { "data" }, where,
 					values.toArray(new String[0]), null, null, parseOrder(order, "id", tinfo), limit);
 			List<PJSONObject> result = new ArrayList<PJSONObject>();
 			if (c.moveToFirst()) {
 				do {
 					result.add(new PJSONObject(c.getString(0)));
 				} while (c.moveToNext());
 			}
 			return result.toArray(new PJSONObject[0]);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void setListener(SyncControllerListener listener) {
 		this.listener = listener;
 	}
 
 	public String getFile(String app, String name) {
 		AppInfo info = getInfo(app);
 		if (null == info.db) {
 			Log.e(TAG, "No DB: " + app);
 			return null;
 		}
 		File folder = info.getFilesFolder();
 		if (null == folder) { // No cache
 			Log.e(TAG, "No cache folder: " + app);
 			return null;
 		}
 		try {
 			File file = new File(folder, name);
 			if (file.exists()) { // Already downloaded
 				return file.getAbsolutePath();
 			}
 			String url = String.format("/rest/file/download?name=%s&", name);
 			if (name.endsWith(".jpg")) { // Add with
 				url += String.format("width=%d&", imageWidth);
 			}
 			// Download
 			Log.i(TAG, "Downloading file: " + name + ", imageWidth: " + imageWidth);
 			InputStream stream = net.raw(app, url, null);
 			copyStreams(stream, new FileOutputStream(file));
 			return file.getAbsolutePath();
 		} catch (Exception e) {
 			Log.e(TAG, "Error getting file:", e);
 		}
 		return null;
 	}
 
 	private void copyStreams(InputStream in, OutputStream out) throws IOException {
 		BufferedInputStream bis = new BufferedInputStream(in);
 		BufferedOutputStream bos = new BufferedOutputStream(out);
 		byte[] buffer = new byte[4096];
 		int bytes = 0;
 		while ((bytes = bis.read(buffer)) > 0) {
 			bos.write(buffer, 0, bytes);
 		}
 		bis.close();
 		bos.close();
 	}
 
 	public boolean removeFile(String app, String name) {
 		AppInfo info = getInfo(app);
 		if (null == info.db) {
 			Log.e(TAG, "No DB: " + app);
 			return false;
 		}
 		removeCacheFile(info, name);
 		try {
 			info.db.getDatabase().beginTransaction();
 			Cursor c = info.db.getDatabase().query("uploads", new String[] { "id" }, "name=? and status=?",
 					new String[] { name, "1" }, null, null, null);
 			if (c.moveToFirst()) { // Have data - delete
 				info.db.getDatabase().delete("uploads", "id=?", new String[] { c.getString(0) });
 			} else {
 				ContentValues values = new ContentValues();
 				values.put("id", info.id());
 				values.put("name", name);
 				values.put("status", 3);
 				info.db.getDatabase().insert("uploads", null, values);
 			}
 			info.db.getDatabase().setTransactionSuccessful();
 			scheduleAutoSync(app);
 			return true;
 		} catch (Exception e) {
 			Log.e(TAG, "Error removing file:", e);
 		} finally {
 			info.db.getDatabase().endTransaction();
 		}
 		return false;
 	}
 
 	private boolean removeCacheFile(AppInfo info, String name) {
 		File folder = info.getFilesFolder();
 		if (null == folder) { // No cache
 			Log.e(TAG, "No cache folder: " + info.name);
 			return false;
 		}
 		try {
 			File file = new File(folder, name);
 			if (!file.exists()) { // Already downloaded
 				return true;
 			}
 			return file.delete();
 		} catch (Exception e) {
 			Log.e(TAG, "Error getting file:", e);
 		}
 		return false;
 	}
 
 	public String uploadFile(String app, String path) {
 		AppInfo info = getInfo(app);
 		if (null == info.db) {
 			Log.e(TAG, "No DB: " + app);
 			return null;
 		}
 		File folder = info.getFilesFolder();
 		if (null == folder) { // No cache
 			Log.e(TAG, "No cache folder: " + app);
 			return null;
 		}
 		File inFile = new File(path);
 		if (!inFile.exists() || !inFile.isFile()) { // Invalid input file
 			Log.e(TAG, "File not found: " + inFile);
 			return null;
 		}
 		String ext = ".bin";
 		if (-1 != path.lastIndexOf('.')) { // Have ext
 			ext = path.substring(path.lastIndexOf('.')).toLowerCase();
 		}
 		String name = "" + info.id() + ext;
 		File file = new File(folder, name);
 		try { //
 			copyStreams(new FileInputStream(inFile), new FileOutputStream(file)); // Copied
 		} catch (Exception e) {
 			Log.e(TAG, "Error copying files:", e);
 			return null;
 		}
 		try {
 			info.db.getDatabase().beginTransaction();
 			ContentValues values = new ContentValues();
 			values.put("id", info.id());
 			values.put("path", file.getAbsolutePath());
 			values.put("name", name);
 			values.put("status", 1);
 			info.db.getDatabase().insert("uploads", null, values);
 			info.db.getDatabase().setTransactionSuccessful();
 			scheduleAutoSync(app);
 			return name;
 		} catch (Exception e) {
 			Log.e(TAG, "Error getting file:", e);
 		} finally {
 			info.db.getDatabase().endTransaction();
 		}
 		return null;
 	}
 
 }
