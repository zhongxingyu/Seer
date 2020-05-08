 package de.h3ndrik.openlocation;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.h3ndrik.openlocation.util.Utils;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Base64;
 import android.util.Log;
 import android.widget.Toast;
 
 public class LocationReceiver extends BroadcastReceiver {
 	private static final String DEBUG_TAG = "LocationReceiver"; // for logging
 																// purposes
 
 	static LocationListener locationListener;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 
 		Log.d(DEBUG_TAG, "got a Broadcast");
 
 		if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
 			Location location = (Location) intent.getExtras().get(
 					LocationManager.KEY_LOCATION_CHANGED);
 			Log.d(DEBUG_TAG, "location update by " + location.getProvider());
 			// Toast.makeText(context, "Location changed : Lat: " +
 			// location.getLatitude() + " Long: " + location.getLongitude(),
 			// Toast.LENGTH_SHORT).show();
 			// in sqlite schreiben
 			DBAdapter db = new DBAdapter(context);
 			db.dbhelper.open_w();
 			db.dbhelper.insertLocation(location.getTime(),
 					location.getLatitude(), location.getLongitude(),
 					location.getAltitude(), location.getAccuracy(),
 					location.getSpeed(), location.getBearing(),
 					location.getProvider());
 			db.dbhelper.close();
 		} else if (intent.hasExtra("de.h3ndrik.openlocation.cancelgps")) {
 			Log.d(DEBUG_TAG, "Alarm: cancel GPS");
 
 			if (locationListener != null) {
 				// Toast.makeText(context, "canceling GPS",
 				// Toast.LENGTH_SHORT).show();
 				LocationManager locationManager = (LocationManager) context
 						.getSystemService(Context.LOCATION_SERVICE);
 				// Toast.makeText(context, "removing LocationListener",
 				// Toast.LENGTH_SHORT).show();
 				locationManager.removeUpdates(locationListener);
				//give LocationManager time to settle
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// continue
				}
 				locationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, 0, 0,
 						locationListener);	// Request network location instead
 			}
 		} else {
 			DBAdapter db = new DBAdapter(context);
 
 			// This update came (TODO: most probably?) from a recurring alarm
 			// We defined an inexact Alarm to send data only if phone is active
 			// anyway
 			// hence, save battery. So do network stuff immediately.
 			sendToServer(context, db);
 
 			// Finally Activate GPS if last location in sqlite is older than
 			// 15min
 			// This is kind of suboptimal at this time, because database is
 			// already synced with server
 			// But doing things in this order may save a little battery
 			db.dbhelper.open_r();
 			// Toast.makeText(context, "Last update " +
 			// Long.toString((System.currentTimeMillis() -
 			// db.dbhelper.lastUpdateMillis()) / 1000) + "s ago",
 			// Toast.LENGTH_SHORT).show();
 
 			final Context con = context;
 			if (locationListener == null) {
 
 				locationListener = new LocationListener() {
 					public void onLocationChanged(Location location) {
 						LocationManager locationManager = (LocationManager) con
 								.getSystemService(Context.LOCATION_SERVICE);
 						// Toast.makeText(con, "removing LocationListener",
 						// Toast.LENGTH_SHORT).show();
 						locationManager.removeUpdates(this);
 					}
 
 					public void onProviderDisabled(String provider) {
 						// TODO Auto-generated method stub
 
 					}
 
 					public void onProviderEnabled(String provider) {
 						// TODO Auto-generated method stub
 
 					}
 
 					public void onStatusChanged(String provider, int status,
 							Bundle extras) {
 						// TODO Auto-generated method stub
 
 					}
 				};
 			}
 
 			if (db.dbhelper.lastUpdateMillis() < System.currentTimeMillis() - 15 * 60 * 1000) {
 
 				LocationManager locationManager = (LocationManager) context
 						.getSystemService(Context.LOCATION_SERVICE);
 
 				SharedPreferences SP = PreferenceManager
 						.getDefaultSharedPreferences(context);
 				if (SP.getBoolean("activeupdate", false)) {
 
 					if (!locationManager
 							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 						Toast.makeText(
 								context,
 								context.getResources().getString(
 										R.string.msg_gpsdisabled),
 								Toast.LENGTH_SHORT).show();
 						locationManager.requestLocationUpdates(
 								LocationManager.NETWORK_PROVIDER, 0, 0,
 								locationListener);
 					} else {
 						// Toast.makeText(context, "getting GPS fix",
 						// Toast.LENGTH_SHORT).show();
 						locationManager.requestLocationUpdates(
 								LocationManager.GPS_PROVIDER, 0, 0,
 								locationListener); // workaround,
 													// requestSingleUpdate: only
 													// API Versions >= 9
 					}
 
 
 				} else {
 					// get update without gps
 					locationManager.requestLocationUpdates(
 							LocationManager.NETWORK_PROVIDER, 0, 0,
 							locationListener);
 				}
 				
 				AlarmManager alarmManager = (AlarmManager) context
 						.getSystemService(Context.ALARM_SERVICE);
 				Intent i = new Intent(context, LocationReceiver.class);
 				i.putExtra("de.h3ndrik.openlocation.cancelgps", "true");
 				PendingIntent pendingIntent = PendingIntent
 						.getBroadcast(context, 0, i,
 								PendingIntent.FLAG_UPDATE_CURRENT);
 				alarmManager.set(AlarmManager.RTC_WAKEUP,
 						System.currentTimeMillis() + 20000,
 						pendingIntent);
 				Log.d(DEBUG_TAG, "Alarm .cancelgps set");
 
 			}
 
 			db.dbhelper.close();
 
 		}
 	}
 
 	private void sendToServer(Context context, DBAdapter db) {
 
 		// Check if something is in database
 		db.dbhelper.open_r();
 		if (db.dbhelper.getLocalLocations() == null
 				|| db.dbhelper.getLocalLocations().getCount() < 1) {
 			Log.d(DEBUG_TAG, "sendToServer(): nothing in database");
 			db.dbhelper.close();
 			return;
 		}
 		db.dbhelper.close();
 
 		// Check the network connection
 		ConnectivityManager connMgr = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo == null || !networkInfo.isConnected()) {
 			// display error
 			Log.d(DEBUG_TAG, "sendToServer(): no internet connection");
 			Toast.makeText(
 					context,
 					context.getResources().getString(R.string.msg_noconnection),
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		if (Utils.getUsername(context) == null) {
 			Log.d(DEBUG_TAG,
 					"sendToServer(): username not set");
 			Toast.makeText(
 					context,
 					context.getResources().getString(
 							R.string.msg_usernotconfigured), Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 		Log.d(DEBUG_TAG, "sendToServer(): user: " + Utils.getUsername(context) + " @ " + Utils.getDomain(context));
 
 		// http
 
 		AsyncHttpTransfer asyncHttp = new AsyncHttpTransfer();
 		asyncHttp.init(context);
 		asyncHttp.execute(Utils.getDomain(context), Utils.getFullUsername(context), Utils.getPassword(context));
 
 	}
 
 	private class AsyncHttpTransfer extends AsyncTask<String, Void, String> {
 		private DBAdapter db;
 		private Context context;
 
 		public void init(Context con) {
 			context = con;
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
 
 			/* Do the database work */
 			DBAdapter db = new DBAdapter(context);
 
 			String deletionMarkerTmp = "";
 
 			db.dbhelper.open_r();
 			if (!db.db.isOpen()) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					// continue
 				}
 				db.dbhelper.open_r();
 			}
 			Log.d(DEBUG_TAG, "looking for data to upload");
 			Cursor cursor = db.dbhelper.getLocalLocations();
 			Log.d(DEBUG_TAG, "got " + Integer.toString(cursor.getCount())
 					+ " results");
 			if (cursor != null && cursor.getCount() > 0)
 				cursor.moveToFirst();
 			else {
 				db.dbhelper.close();
 				return null;
 			}
 
 			JSONObject json = new JSONObject();
 
 			try {
 				json.put("request", "setlocation");
 				json.put("sender", params[1]);
 				try {
 					json.put("version", Integer.toString(context
 							.getPackageManager().getPackageInfo(
 									context.getPackageName(), 0).versionCode));
 				} catch (NameNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} catch (JSONException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 
 			JSONArray data = new JSONArray();
 
 			do {
 				
 				JSONObject row = new JSONObject();
 
 				try {
 					
 					row.put(DBAdapter.LocationCacheContract.COLUMN_TIME,
 							Long.toString(cursor.getLong(0)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_LATITUDE,
 							Double.toString(cursor.getDouble(1)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_LONGITUDE,
 							Double.toString(cursor.getDouble(2)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_ALTITUDE,
 							Double.toString(cursor.getDouble(3)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_ACCURACY,
 							Float.toString(cursor.getFloat(4)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_SPEED,
 							Float.toString(cursor.getFloat(5)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_BEARING,
 							Float.toString(cursor.getFloat(6)));
 					row.put(DBAdapter.LocationCacheContract.COLUMN_PROVIDER,
 							cursor.getString(7));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				data.put(row);
 				if (!(deletionMarkerTmp.length() == 0))
 					deletionMarkerTmp += ", ";
 				deletionMarkerTmp += Long.toString(cursor.getLong(0));
 
 			} while (cursor.moveToNext());
 
 			try {
 				json.put("data", data);
 			} catch (JSONException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 
 			byte[] data_compressed = null;
 			try {
 				data_compressed = Utils.gzdeflate(json.toString().getBytes("UTF-8"));
 			} catch (UnsupportedEncodingException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 
            /* Garbage collection */
            data = null;
            json = null;
 
 			/* http */
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 
 			// httpclient.setReuseStrategy()
 
 			httpclient.getCredentialsProvider().setCredentials(
 					new AuthScope(params[0], 80),
 					new UsernamePasswordCredentials(params[1], params[2]));
 
 			HttpPost httppost = new HttpPost("http://" + params[0]
 					+ "/api.php");
 			
 			//httppost.setHeader("Content-Type", "multipart/form-data");
 			
 			//BasicHttpParams httpparams = new BasicHttpParams();
 			//httpparams.clear();
 			
 			//httpparams.setParameter("json", Base64.encodeToString(compressed, Base64.DEFAULT));	// Reference says HTTP POST can send "arbitrary" amounts of data
 			
 			//httppost.setParams(httpparams);
 			
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			pairs.add(new BasicNameValuePair("json", Base64.encodeToString(
 					data_compressed, Base64.DEFAULT)));
 			
 			try {
 				httppost.setEntity(new UrlEncodedFormEntity(pairs));
 				// httppost.setEntity(new StringEntity(json.toString()));
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				db.dbhelper.close();
 				e1.printStackTrace();
 			}
 
 			HttpResponse response = null;
 
 			Log.d(DEBUG_TAG, "executing request " + httppost.getRequestLine());
 
 			String deletionMarker = "";
 
 			try {
 				response = httpclient.execute(httppost);
 				Log.d(DEBUG_TAG, "got response " + response.getStatusLine());
 				switch (response.getStatusLine().getStatusCode()) {
 				case 200:
 					deletionMarker = deletionMarkerTmp;
 					break;
 				case 400:
 					Toast.makeText(context, "HTTP 400", Toast.LENGTH_SHORT).show();	// Does not work. Use Handler
 					break;
 				case 401:
 					Toast.makeText(context, "HTTP 401", Toast.LENGTH_SHORT).show();	// Does not work. Use Handler
 					break;
 				case 403:
 					Toast.makeText(context, "HTTP 403", Toast.LENGTH_SHORT).show();	// Does not work. Use Handler
 					break;
 				case 404:
 					Toast.makeText(context, "HTTP 404", Toast.LENGTH_SHORT).show();	// Does not work. Use Handler
 					break;
 				case 500:
 					Toast.makeText(context, "HTTP 500", Toast.LENGTH_SHORT).show();	// Does not work. Use Handler
 					break;
 				default:
 					Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();	// Does not work. Use Handler
 					break;
 				}
 
 			} catch (ClientProtocolException e) {
 				Log.i("Exception (ClientProtocol)", " " + e.getMessage());
 				db.dbhelper.close();
 				return null;
 			} catch (IOException e) {
 				Log.i("Exception (IO)", " " + e.getMessage());
 				db.dbhelper.close();
 				return null;
 			} catch (Exception e) {
 				Log.i("Exception", " " + e.getMessage());
 				db.dbhelper.close();
 				return null;
 			}
 
 			try {
 				response.getEntity().consumeContent();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			/* Garbage collection */
 			data_compressed = null;
 
 			db.dbhelper.close();
 
 			if (deletionMarker.length() == 0 || deletionMarker.equals("0"))
 				return null;
 
 			db.dbhelper.open_w();
 			Log.d(DEBUG_TAG, "marking as done: " + deletionMarker);
 			db.dbhelper.markDone(deletionMarker);
 			db.dbhelper.close();
 
 			// TODO: Refresh webview?
 
 			return deletionMarker;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// Log.d(DEBUG_TAG, "onPostExecute: Returned: " + result);
 
 		}
 	}
 
 	public LocationReceiver() {
 	}
 }
