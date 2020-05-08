 /**
  * 
  */
 package no.whg.whirc.helpers;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Random;
 
 import jerklib.Channel;
 import jerklib.ConnectionManager;
 import jerklib.Profile;
 import jerklib.Session;
 import jerklib.events.IRCEvent;
 import jerklib.events.IRCEvent.Type;
 import jerklib.listeners.IRCEventListener;
 import no.whg.whirc.activities.MainActivity;
 import android.R;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 public class ConnectionService extends Service implements IRCEventListener {
 	private static final String TAG = "ConnectionService";
 	private Handler handler;
 	private final ConnectionServiceBinder binder;
 	private ArrayList<Runnable> threads;
 	
 	
 	// irc object
	ConnectionManager connection;
 	Session qnet;
 	
 
 	public ConnectionService() {
 		super();
 		
		Random random = new Random();
		String name = "whirc";
		name += (String.valueOf(random.nextInt(9)) + String.valueOf(random.nextInt(9)) + String.valueOf(random.nextInt(9)));
		connection = new ConnectionManager(new Profile(name));
		
 		this.handler = new Handler();
 		this.threads = new ArrayList<Runnable>();
 		this.binder = new ConnectionServiceBinder(this);
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "Service bound! [onBind() called]");
 		
 		// lets see if shit keeps running or not now
 		startService(new Intent(this, ConnectionService.class));
 		return this.binder;
 	}
 	
 	
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onUnbind(android.content.Intent)
 	 */
 	@Override
 	public boolean onUnbind(Intent intent) {
 		// TODO Auto-generated method stub
 		return super.onUnbind(intent);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		Log.d("ConnectionService", "Service created! [onCreate() called]");
 		connect("irc.quakenet.org", this);
 		
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         
         PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
         
         Notification notification = 
                         new NotificationCompat.Builder(this)
                         .setSmallIcon(R.drawable.ic_menu_info_details)
                         .setTicker("Starting IRC")
                         
                         .setContentTitle("WHIRC")
                         .setContentText("You are connected to IRC [This should show hilight or some shit]")
                         .setContentIntent(pending)
                         .build();
         
         startForeground(1337, notification);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		Log.d(TAG, "Service destroyed! [onDestroy() called]");
 		for(Runnable t : threads) {
 			handler.removeCallbacks(t);
 			Log.d(TAG, "Removed callback on thread " + t.toString() + " (" + t.hashCode() + ")");
 		}
 		stopSelf();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "Service started! [onStartCommand() called]");
 		return START_STICKY;
 	}
 	
 	public void connect(final String server, final ConnectionService service) {
 		
 		final Runnable thread = new Runnable() {
 			@Override
 			public void run() {
 				qnet = connection.requestConnection(server);
 				qnet.addIRCEventListener(service);
 
 			}
 		};
 		threads.add(thread);
 		handler.postDelayed(thread, 1000);
 		Log.d(TAG, "Thread created! toString: " + thread.toString() + " - hash: " + thread.hashCode());
 		
 	}
 	
 	public void shutdown() {
 		for(Runnable t : threads) {
 			handler.removeCallbacks(t);
 		}
 		stopSelf();
 	}
 	
 	public List<Channel> getChannelsForSession(String name) {
 		return connection.getSession(name).getChannels();
 
 	}
 	
 	public List<Session> getSessions() {
 		return connection.getSessions();
 	}
 	
 	public boolean isSessionConnected(String name) {
 		return connection.getSession(name).isConnected();
 	}
 
 	@Override
 	public void receiveEvent(IRCEvent e) {
 		
 		if (e.getType() == Type.CONNECT_COMPLETE) {
 			e.getSession().join("#whg");
 			Log.d(TAG, "Server: irc.quakenet.org [as string] - Session name: " + qnet.getConnectedHostName());
 		} else {
 			System.out.println(e.getType() + " : " + e.getRawEventData());
 		}
 	}
 
 }
