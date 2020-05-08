 package com.kanasansoft.android.WSBroadcaster;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandler;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.component.LifeCycle;
 import org.eclipse.jetty.util.component.LifeCycle.Listener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class WSBroadcaster extends Activity implements Listener, OnClickListener, MyWebSocket.Listener {
 
 	String preferenceKeyHttpServerPath          = null;
 	String preferenceKeyWebSocketServerPath     = null;
 	String preferenceKeyPortNumber              = null;
 	String preferenceKeyResponseType            = null;
 	String preferenceKeyPeriodicMessage         = null;
 	String preferenceKeyPeriodicMessageInterval = null;
 	String preferenceKeyPeriodicMessageText     = null;
 
 	String directoryOfPathInSDCard = null;
 
 	Server server = null;
 
 	private static Set<MyWebSocket> members_ = new CopyOnWriteArraySet<MyWebSocket>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		preferenceKeyHttpServerPath          = getString(R.string.preference_key_http_server_path);
 		preferenceKeyWebSocketServerPath     = getString(R.string.preference_key_websocket_server_path);
 		preferenceKeyPortNumber              = getString(R.string.preference_key_port_number);
 		preferenceKeyResponseType            = getString(R.string.preference_key_response_type);
 		preferenceKeyPeriodicMessage         = getString(R.string.preference_key_periodic_message);
 		preferenceKeyPeriodicMessageInterval = getString(R.string.preference_key_periodic_message_interval);
 		preferenceKeyPeriodicMessageText     = getString(R.string.preference_key_periodic_message_text);
 
 		chackAndMakeDirectory();
 
 		Button buttonStartStop = (Button)findViewById(R.id.button_start_stop);
 		buttonStartStop.setOnClickListener(this);
 
 		displayServerStatus();
 		displayPreferenceValue();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.optionmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.optionmenu_preferences:
 			String currentValueServerStatus = Server.STOPPED;
 			if (server != null) {
 				currentValueServerStatus = server.getState();
 			}
 			if (currentValueServerStatus.equals(Server.STOPPED)) {
 				startActivityForResult(new Intent(this,MyPreferenceActivity.class), 0);
 			} else {
 				Builder alert = new AlertDialog.Builder(this);
 				alert.setMessage(R.string.message_only_when_a_server_is_stoped);
 				alert.setPositiveButton(android.R.string.ok, null);
 				alert.create().show();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		displayPreferenceValue();
 	}
 
 	public void lifeCycleFailure(LifeCycle event, Throwable cause) {
 	}
 
 	public void lifeCycleStarted(LifeCycle event) {
 		displayServerStatus();
 	}
 
 	public void lifeCycleStarting(LifeCycle event) {
 		displayServerStatus();
 	}
 
 	public void lifeCycleStopped(LifeCycle event) {
 		displayServerStatus();
 	}
 
 	public void lifeCycleStopping(LifeCycle event) {
 		displayServerStatus();
 	}
 
 	public void onClick(View view) {
 
 		String currentValueServerStatus = Server.STOPPED;
 		if (server != null) {
 			currentValueServerStatus = server.getState();
 		}
 
 		if (currentValueServerStatus.equals(Server.STOPPED)) {
 			startWebSocketServer();
 		} else {
 			stopWebSocketServer();
 		}
 
 	}
 
 	public void onOpen(MyWebSocket myWebSocket) {
 		members_.add(myWebSocket);
 	}
 
 	public void onClose(MyWebSocket myWebSocket, int closeCode, String message) {
 		members_.remove(myWebSocket);
 	}
 
 	public void onMessage(MyWebSocket myWebSocket, String data) {
 
 		Bundle prefData = getPreferenceData();
 
 		String  responseType = prefData.getString(preferenceKeyResponseType);
 		if (responseType.equals(getString(R.string.response_type_value_all))) {
 			sendAll(data);
 		} else if (responseType.equals(getString(R.string.response_type_value_other))) {
 			sendOther(myWebSocket, data);
 		} else if (responseType.equals(getString(R.string.response_type_value_echo))) {
 			sendEcho(myWebSocket, data);
 		}
 
 	}
 
 	public void onMessage(MyWebSocket myWebSocket, byte[] data, int offset, int length) {
 
 		Bundle prefData = getPreferenceData();
 
 		String  responseType = prefData.getString(preferenceKeyResponseType);
 		if (responseType.equals(getString(R.string.response_type_value_all))) {
 			sendAll(data, offset, length);
 		} else if (responseType.equals(getString(R.string.response_type_value_other))) {
 			sendOther(myWebSocket, data, offset, length);
 		} else if (responseType.equals(getString(R.string.response_type_value_echo))) {
 			sendEcho(myWebSocket, data, offset, length);
 		}
 
 	}
 
 	private void chackAndMakeDirectory() {
 		File dir = new File(Environment.getExternalStorageDirectory(),"WSBroadcaster");
 		if (!dir.exists()) {
 			if (!dir.mkdir()) {
 				Builder alert = new AlertDialog.Builder(this);
 				alert.setMessage(R.string.message_cannot_make_directory_in_sd_card);
 				alert.setPositiveButton(android.R.string.ok, null);
 				alert.create().show();
 				return;
 			}
 		}
 		directoryOfPathInSDCard = dir.getPath();
 	}
 
 	private void startWebSocketServer() {
 
 		Bundle prefData = getPreferenceData();
 
 		String  httpServerPath          = prefData.getString (preferenceKeyHttpServerPath);
 		String  webSocketServerPath     = prefData.getString (preferenceKeyWebSocketServerPath);
 		int     portNumber              = prefData.getInt    (preferenceKeyPortNumber);
 		boolean periodicMessage         = prefData.getBoolean(preferenceKeyPeriodicMessage);
 		int     periodicMessageInterval = prefData.getInt    (preferenceKeyPeriodicMessageInterval);
 		String  periodicMessageText     = prefData.getString (preferenceKeyPeriodicMessageText);
 
 		if (httpServerPath.equals(webSocketServerPath)) {
 			Builder alert = new AlertDialog.Builder(this);
 			alert.setMessage(R.string.message_cannot_same_path_http_and_websocket);
 			alert.setPositiveButton(android.R.string.ok, null);
 			alert.create().show();
 			return;
 		}
 
 		server = new Server(portNumber);
 
 		server.addLifeCycleListener(this);
 
 		ResourceHandler rh = new ResourceHandler();
 		rh.setResourceBase(directoryOfPathInSDCard);
 		ContextHandler chrh = new ContextHandler();
 		chrh.setHandler(rh);
 		chrh.setContextPath("/" + httpServerPath);
 
 		MyWebSocketServlet wss = new MyWebSocketServlet(this);
 		ServletHolder sh = new ServletHolder(wss);
 		ServletContextHandler sch = new ServletContextHandler();
 		sch.addServlet(sh, "/" + webSocketServerPath);
 
 		HandlerList hl = new HandlerList();
 		hl.setHandlers(new Handler[] {chrh, sch});
 		server.setHandler(hl);
 
		// http://code.google.com/p/android/issues/detail?id=9431
		System.setProperty("java.net.preferIPv6Addresses", "false");

 		try {
 			server.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		if (periodicMessage) {
 			final int interval = periodicMessageInterval * 1000;
 			final String text  = periodicMessageText;
 			Runnable runnable = new Runnable() {
 				public void run() {
 					while (true) {
 						try {
 							Thread.sleep(interval);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						String currentValueServerStatus = Server.STOPPED;
 						if (server != null) {
 							currentValueServerStatus = server.getState();
 						}
 						if (currentValueServerStatus.equals(Server.STOPPED)) {
 							break;
 						} else if (currentValueServerStatus.equals(Server.STARTED)) {
 							sendAll(text);
 						}
 					}
 				}
 			};
 			new Thread(runnable).start();
 		}
 
 	}
 
 	private void stopWebSocketServer() {
 
 		try {
 			server.stop();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private Bundle getPreferenceData() {
 
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		String  defaultValueHttpServerPath          =                 getString(R.string.default_value_http_server_path);
 		String  defaultValueWebSocketServerPath     =                 getString(R.string.default_value_websocket_server_path);
 		String  defaultValuePortNumber              =                 getString(R.string.default_value_port_number);
 		String  defaultValueResponseType            =                 getString(R.string.default_value_response_type);
 		boolean defaultValuePeriodicMessage         = Boolean.valueOf(getString(R.string.default_value_periodic_message));
 		String  defaultValuePeriodicMessageInterval =                 getString(R.string.default_value_periodic_message_interval);
 		String  defaultValuePeriodicMessageText     =                 getString(R.string.default_value_periodic_message_text);
 
 		String  currentValueHttpServerPath          =                 pref.getString (preferenceKeyHttpServerPath,          defaultValueHttpServerPath);
 		String  currentValueWebSocketServerPath     =                 pref.getString (preferenceKeyWebSocketServerPath,     defaultValueWebSocketServerPath);
 		int     currentValuePortNumber              = Integer.valueOf(pref.getString (preferenceKeyPortNumber,              defaultValuePortNumber), 10);
 		String  currentValueResponseType            =                 pref.getString (preferenceKeyResponseType,            defaultValueResponseType);
 		boolean currentValuePeriodicMessage         =                 pref.getBoolean(preferenceKeyPeriodicMessage,         defaultValuePeriodicMessage);
 		int     currentValuePeriodicMessageInterval = Integer.valueOf(pref.getString (preferenceKeyPeriodicMessageInterval, defaultValuePeriodicMessageInterval), 10);
 		String  currentValuePeriodicMessageText     =                 pref.getString (preferenceKeyPeriodicMessageText,     defaultValuePeriodicMessageText);
 
 		Bundle bundle = new Bundle();
 
 		bundle.putString(preferenceKeyHttpServerPath,       currentValueHttpServerPath);
 		bundle.putString(preferenceKeyWebSocketServerPath,  currentValueWebSocketServerPath);
 		bundle.putInt(preferenceKeyPortNumber,              currentValuePortNumber);
 		bundle.putString(preferenceKeyResponseType,         currentValueResponseType);
 		bundle.putBoolean(preferenceKeyPeriodicMessage,     currentValuePeriodicMessage);
 		bundle.putInt(preferenceKeyPeriodicMessageInterval, currentValuePeriodicMessageInterval);
 		bundle.putString(preferenceKeyPeriodicMessageText,  currentValuePeriodicMessageText);
 
 		return bundle;
 
 	}
 
 	private void displayServerStatus() {
 
 		String currentValueServerStatus = Server.STOPPED;
 		if (server != null) {
 			currentValueServerStatus = server.getState();
 		}
 
 		Button buttonStartStop = (Button)findViewById(R.id.button_start_stop);
 
 		if (currentValueServerStatus.equals(Server.STOPPED)) {
 			buttonStartStop.setText(getString(R.string.button_text_start));
 		} else {
 			buttonStartStop.setText(getString(R.string.button_text_stop));
 		}
 
 		TextView displayAreaServerStatus = (TextView)findViewById(R.id.display_area_server_status);
 
 		displayAreaServerStatus.setText(currentValueServerStatus);
 
 	}
 
 	private void displayPreferenceValue() {
 
 		Bundle prefData = getPreferenceData();
 
 		TextView displayAreaHttpServerPath          = (TextView)findViewById(R.id.display_area_http_server_path);
 		TextView displayAreaWebSocketServerPath     = (TextView)findViewById(R.id.display_area_websocket_server_path);
 		TextView displayAreaPortNumber              = (TextView)findViewById(R.id.display_area_port_number);
 		TextView displayAreaResponseType            = (TextView)findViewById(R.id.display_area_response_type);
 		TextView displayAreaPeriodicMessage         = (TextView)findViewById(R.id.display_area_periodic_message);
 		TextView displayAreaPeriodicMessageInterval = (TextView)findViewById(R.id.display_area_periodic_message_interval);
 		TextView displayAreaPeriodicMessageText     = (TextView)findViewById(R.id.display_area_periodic_message_text);
 
 		String  currentValueHttpServerPath          = prefData.getString (preferenceKeyHttpServerPath);
 		String  currentValueWebSocketServerPath     = prefData.getString (preferenceKeyWebSocketServerPath);
 		int     currentValuePortNumber              = prefData.getInt    (preferenceKeyPortNumber);
 		String  currentValueResponseTypeValue       = prefData.getString (preferenceKeyResponseType);
 		boolean currentValuePeriodicMessage         = prefData.getBoolean(preferenceKeyPeriodicMessage);
 		int     currentValuePeriodicMessageInterval = prefData.getInt    (preferenceKeyPeriodicMessageInterval);
 		String  currentValuePeriodicMessageText     = prefData.getString (preferenceKeyPeriodicMessageText);
 
 		String[] responseTypeValues = getResources().getStringArray(R.array.response_type_values);
 		String[] responseTypeNames = getResources().getStringArray(R.array.response_type_names);
 		String currentValueResponseTypeName = "null";
 		for (int i=0; i<responseTypeValues.length; i++) {
 			if (responseTypeValues[i].equals(currentValueResponseTypeValue)) {
 				currentValueResponseTypeName = responseTypeNames[i];
 				break;
 			}
 		}
 
 		displayAreaHttpServerPath         .setText(String.valueOf(currentValueHttpServerPath));
 		displayAreaWebSocketServerPath    .setText(String.valueOf(currentValueWebSocketServerPath));
 		displayAreaPortNumber             .setText(String.valueOf(currentValuePortNumber));
 		displayAreaResponseType           .setText(               currentValueResponseTypeName);
 		displayAreaPeriodicMessage        .setText(               currentValuePeriodicMessage?"ON":"OFF");
 		displayAreaPeriodicMessageInterval.setText(String.valueOf(currentValuePeriodicMessageInterval));
 		displayAreaPeriodicMessageText    .setText(               currentValuePeriodicMessageText);
 
 	}
 
 	private void sendAll(String data) {
 		for(MyWebSocket member : members_) {
 			try {
 				member.getConnection().sendMessage(data);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void sendAll(byte[] data, int offset, int length) {
 		for(MyWebSocket member : members_) {
 			try {
 				member.getConnection().sendMessage(data, offset, length);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void sendOther(MyWebSocket myWebSocket, String data) {
 		for(MyWebSocket member : members_) {
 			if (myWebSocket.equals(member)) {
 				continue;
 			}
 			try {
 				member.getConnection().sendMessage(data);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void sendOther(MyWebSocket myWebSocket, byte[] data, int offset, int length) {
 		for(MyWebSocket member : members_) {
 			if (myWebSocket.equals(member)) {
 				continue;
 			}
 			try {
 				member.getConnection().sendMessage(data, offset, length);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void sendEcho(MyWebSocket myWebSocket, String data) {
 			try {
 				myWebSocket.getConnection().sendMessage(data);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 
 	private void sendEcho(MyWebSocket myWebSocket, byte[] data, int offset, int length) {
 			try {
 				myWebSocket.getConnection().sendMessage(data, offset, length);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 
 }
