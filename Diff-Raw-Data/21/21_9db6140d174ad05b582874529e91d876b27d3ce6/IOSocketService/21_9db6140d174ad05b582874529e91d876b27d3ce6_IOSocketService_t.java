 package com.geoloqi.services;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.clwillingham.socket.io.IOSocket;
 import com.clwillingham.socket.io.MessageCallback;
 import com.geoloqi.interfaces.GeoloqiConstants;
 import com.geoloqi.interfaces.LoggingConstants;
 import com.geoloqi.ui.GameListActivity;
 import com.geoloqi.ui.MapAttackActivity;
 
 public class IOSocketService extends Service implements GeoloqiConstants,
 		MessageCallback {
 
 	protected static final String TAG = LoggingConstants.RECORDING_TAG;
 
 	private IOSocket socket;
 
 	private boolean connected = false;
 	private boolean destroyed = false;
 
 	private String mGameID;
 
 	private String mUserID;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		System.setProperty("java.net.preferIPv6Addresses", "false");
 		String url = "http://" + IOSOCKET_ADDRESS + ":" + IOSOCKET_PORT + "/";
 		Log.i(TAG, "Starting IO socket service at " + url);
 		socket = new IOSocket(url, this);
 		mGameID = intent.getStringExtra(GameListActivity.PARAM_GAME_ID);
 		mUserID = intent.getStringExtra(MapAttackActivity.PARAM_USER_ID);
 		registerReceiver(mGPSReceiver, new IntentFilter(
 				GPSTrackingService.GPS_INTENT));
 		connectSocket();
 	}
 
 	private void connectSocket() {
 
 		Thread connector = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				int counter = 0;
 				connected = false;
 				while (!connected) {
 					try {
 						counter++;
 						socket.connect();
 						connected = true;
 						Log.i(TAG, "Connected to " + socket);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if (counter % 30 == 0) {
 						Log.e(TAG,
 								String.format(
 										"Tried to connect for %d seconds, but no connection yet.",
 										counter));
 					}
 				}
 
 			}
 		});
 
 		connector.start();
 
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		onStart(intent, startId);
 		return Service.START_REDELIVER_INTENT;
 	}
 
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
		unregisterReceiver(mGPSReceiver);
 		destroyed = true;
 		if (socket != null) {
 			socket.disconnect();
 		}
 		connected = false;
 	}
 
 	public void forward(String json) {
 		Intent notifyPush = new Intent("PUSH");
 		notifyPush.putExtra("json", json);
 		Log.i(TAG, "Pushing notification to UI: " + json);
 		sendBroadcast(notifyPush);
 	}
 
 	// ////// SOCKET CALLBACK METHODS ///////
 
 	@Override
 	public void on(String event, JSONObject... data) {
 		if (event == null) {
 			Log.e(TAG, "Received null event.");
 		} else {
 			Log.i(TAG, String.format("Received event %s from Web socket with objects %s",
 					event, Arrays.toString(data)));
 			if (event.equals("data")) {
 				for (JSONObject jsonObject : data) {
 					forward(jsonObject.toString());
 				}
 			} else if (event.equals("game")) {
 				joinGame();
 			} else {
 				Log.w(TAG, "Could not handle event " + event);
 			}
 		}
 	}
 
 	private void joinGame() {
 		try {
 			socket.emit("game-join", mGameID);
 			Log.i(TAG, "Connected to game " + mGameID);
 		} catch (IOException e) {
 			Log.e(TAG, "IOException in joinGame: " + e);
 		}
 
 	}
 
 	@Override
 	public void onMessage(String message) {
 		Log.i(TAG, String.format("Received message %s.", message));
 	}
 
 	@Override
 	public void onMessage(JSONObject json) {
 		Log.i(TAG, String.format("Received JSON %s.", json));
 	}
 
 	@Override
 	public void onConnect() {
 		Log.i(TAG, "Socket onConnect() called.");
 	}
 
 	@Override
 	public void onDisconnect() {
		unregisterReceiver(mGPSReceiver);
 		connected = false;
 		if (!destroyed) {
 			Log.e(TAG, "Lost connection to socket. Re-connecting.");
 			connectSocket();
 		} else {
 			Log.i(TAG, "Connection closed on destroy.");
 		}
 	}
 
 	/** The broadcast receiver used to push game data to the server. */
 	private BroadcastReceiver mGPSReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context ctxt, Intent intent) {
 			double longitude = intent.getExtras().getDouble(
 					GPSTrackingService.PARAM_LONGITUDE);
 			double latitude = intent.getExtras().getDouble(
 					GPSTrackingService.PARAM_LATITUDE);
 			Log.i("Testing IO", String.format(
 					"Received from local GPS: long:%f, lat:%f", longitude,
 					latitude));
 			JSONObject object = new JSONObject();
 
 			try {
 				object.put("longitude", longitude);
 				object.put("latitude", latitude);
 				object.put("player_id", mUserID);
 				socket.emit("location-push", object);
 			} catch (JSONException e) {
 				Log.e(TAG, "JSONException in gps push: " + e);
 			} catch (IOException e) {
 				Log.e(TAG, "IOException in gps push: " + e);
 			}
 		}
 	};
 
 }
