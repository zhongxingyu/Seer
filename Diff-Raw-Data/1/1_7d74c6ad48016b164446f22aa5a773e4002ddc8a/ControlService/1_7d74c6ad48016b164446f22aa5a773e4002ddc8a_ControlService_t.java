 package net.djmacgyver.bgt.control;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import net.djmacgyver.bgt.event.Event;
 import net.djmacgyver.bgt.gps.GPSTrackingService;
 import net.djmacgyver.bgt.socket.HttpSocketConnection;
 import net.djmacgyver.bgt.socket.HttpSocketListener;
 import net.djmacgyver.bgt.socket.SocketCommand;
 import net.djmacgyver.bgt.socket.SocketService;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class ControlService extends Service implements HttpSocketListener {
 	private HttpSocketConnection socket;
 	private Event event;
 	
 	public class LocalBinder extends Binder {
 		public ControlService getService() {
 			return ControlService.this;
 		}
 	}
 	
 	private final Binder binder = new LocalBinder();
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder;
 	}
 	
 	private class SocketServiceConnection implements ServiceConnection{
 		public SocketService s;
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			s = ((SocketService.LocalBinder) service).getService();
 			setConnection(s.getSharedConnection(ControlService.this));
 		}
 	}
 	
 	private SocketServiceConnection conn = new SocketServiceConnection();
 
 	@Override
 	public void onCreate() {
 		System.out.println("ControlService created.");
 	}
 
 	@Override
 	public void onDestroy() {
 		System.out.println("ControlService destroyed");
 	}
 	
 	private void setConnection(HttpSocketConnection socket) {
 		this.socket = socket;
 		if (socket.getState() == HttpSocketConnection.STATE_CONNECTED) enableControlSession();
 		socket.addListener(this);
 	}
 	
 	private void enableControlSession()
 	{
 		JSONObject data = new JSONObject();
 		try {
 			data.put("eventId", event.getId());
 		} catch (JSONException e) {}
 		final SocketCommand command = new SocketCommand("enableControl", data);
 		command.setCallback(new Runnable() {
 			@Override
 			public void run() {
 				if (command.wasSuccessful()) return;
 				Log.e("ControlService", "Server did not accept control connection; error: " + command.getResponseData());
 				shutdown();
 			}
 		});
 		socket.sendCommand(command);
 	}
 	
 	public void shutdown() {
 		if (socket != null) {
 			socket.sendCommand("disableControl");
 			socket.removeListener(this);
 		}
 		stopTracking();
 		conn.s.removeStake(this);
 		unbindService(conn);
 		stopSelf();
 	}
 	
 	private boolean trackingEnabled = false;
 	
 	private void startTracking() {
 		if (trackingEnabled) return;
 		ServiceConnection conn = new ServiceConnection() {
 			@Override
 			public void onServiceDisconnected(ComponentName arg0) {
 			}
 			
 			@Override
 			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
 				GPSTrackingService l = ((GPSTrackingService.LocalBinder) arg1).getService();
 				l.enable();
 				unbindService(this);
 			}
 		};
 		
 		startService(new Intent(getApplicationContext(), GPSTrackingService.class));
 		bindService(new Intent(getApplicationContext(), GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
 		trackingEnabled = true;
 	}
 	
 	private void stopTracking() {
 		if (!trackingEnabled) return;
 		ServiceConnection conn = new ServiceConnection() {
 			@Override
 			public void onServiceDisconnected(ComponentName name) {}
 			
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder service) {
 				GPSTrackingService l = ((GPSTrackingService.LocalBinder) service).getService();
 				l.disable();
 				unbindService(this);
 			}
 		};
 		bindService(new Intent(getApplicationContext(), GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
		trackingEnabled = false;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		event = (Event) intent.getExtras().getParcelable("event");
 		bindService(new Intent(getApplicationContext(), SocketService.class), conn, Context.BIND_AUTO_CREATE);
 		return START_NOT_STICKY;
 	}
 	
 	@Override
 	public void receiveUpdate(JSONObject data) {
 	}
 	
 	@Override
 	public void receiveCommand(String command, JSONObject data) {
 		System.out.println("received command: " + command);
 		if (command.equals("shutdown")) {
 			shutdown();
 			return;
 		}
 		if (command.equals("disableGPS")) {
 			stopTracking();
 			return;
 		}
 		if (command.equals("enableGPS")) {
 			startTracking();
 			return;
 		}
 		System.out.println("received unknown command: \"" + command + "\"");
 	}
 
 	@Override
 	public void receiveStateChange(int newState) {
 		if (newState == HttpSocketConnection.STATE_CONNECTED) enableControlSession();
 	}
 }
