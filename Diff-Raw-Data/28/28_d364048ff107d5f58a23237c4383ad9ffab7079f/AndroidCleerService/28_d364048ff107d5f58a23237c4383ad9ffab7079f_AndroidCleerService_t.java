 package pro.trousev.cleer.android.service;
 
 import java.util.ArrayList;
 
 
 import pro.trousev.cleer.Console;
 import pro.trousev.cleer.ConsoleOutput;
 import pro.trousev.cleer.Database;
 import pro.trousev.cleer.Database.DatabaseError;
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Item.NoSuchTagException;
 import pro.trousev.cleer.Library;
 import pro.trousev.cleer.Library.FolderScanCallback;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Messaging.Event;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Player;
 import pro.trousev.cleer.Player.PlayerChangeEvent;
 import pro.trousev.cleer.Player.Status;
 import pro.trousev.cleer.Plugin.Interface;
 import pro.trousev.cleer.Queue;
 import pro.trousev.cleer.Queue.EnqueueMode;
 import pro.trousev.cleer.android.AndroidMessages;
 import pro.trousev.cleer.android.AndroidMessages.PlayBarMessage;
 import pro.trousev.cleer.android.AndroidMessages.ProgressBarMessage;
 import pro.trousev.cleer.android.AndroidMessages.SeekBarMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRequestMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRespondMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceTaskMessage;
 import pro.trousev.cleer.android.CleerAndroidNotificationManager;
 import pro.trousev.cleer.android.Constants;
 import pro.trousev.cleer.sys.MediaItem;
 import pro.trousev.cleer.sys.QueueImpl;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.media.RemoteControlClient;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.ProgressBar;
 
 public class AndroidCleerService extends Service implements Interface{
 	
 	private static ServiceRespondMessage respondMessage = new ServiceRespondMessage();
 	private Queue queue = null;
 	private Player player;
 	// FIXME delete that kostil
 	// private List<Item> itemList = new ArrayList<Item>();
 	private Database database;
 	private Library library ;
 	private CleerAndroidNotificationManager mNotificationManager;
 
 	private void updatePlayerNotification() {
 		if (player == null)
 			return;
 		Boolean foreground = false;
 		String description = "";
 		switch (player.getStatus()) {
 		case Playing:
 			description = " (Playing)";
 			foreground = true;
 			break;
 		case Paused:
 			description = " (Paused)";
 			foreground = false;
 			break;
 		case Processing:
 			description = " (Processing)";
 			foreground = true;
 			break;
 		case Stopped:
 			foreground = false;
 			break;
 		case Closed:
 			foreground = false;
 			break;
 		default:
 			Log.e(Constants.LOG_TAG, "Service: Illegal player status");
 		}
 		Log.d(Constants.LOG_TAG, "Service: What's up?" + description);
 		if (!description.equals("")) {
 			String n = "";
 			try {
 				n = queue.playing_track().firstTag("title").value();
 			} catch (NoSuchTagException e1) {
 				n = "NO_NAME_AVALIBLE";
 				// e1.printStackTrace();
 			} finally {
 				mNotificationManager.postPlayerNotification(n + description);
 			}
 			if (foreground) {
 				Notification notification = mNotificationManager
 						.getPlayerNotification();
 				startForeground(Constants.PLAYER_NOTIFICATION_ID, notification);
 			} else {
 				stopForeground(false);
 			}
 		}
 	}
 
 
 	// This Event will change notification each time after player changed his
 	// status
 	private Event playerChangedStatus = new Event() {
 		@Override
 		public void messageReceived(Message message) {
 			updatePlayerNotification();
 		}
 	};
 
 
 
 
 	// Binder allow us get Service.this from the Activity
 	public class CleerBinder extends Binder {
 		public AndroidCleerService getService() {
 			return AndroidCleerService.this;
 		}
 	}
     
 	public void registerAsMediaPlayer()
 	{
 	}
 	public void onCreate() {
 		super.onCreate();
 		player = new PlayerAndroid();
 		queue = new QueueImpl(player);
 		database = new DatabaseAndroidImpl();
 		try {
 			library = new LibraryAndroidImpl(database, MediaItem.Factory, getApplication());
 			// FIXME Make scanning of particular dirs in next version
 			library.folder_add(Environment.getExternalStorageDirectory());
 		} catch (DatabaseError e) {
 			Log.e(Constants.LOG_TAG,
 					"Service: Cannot create library. Database error.");
 			e.printStackTrace();
 		}
 		mNotificationManager = new CleerAndroidNotificationManager(this);
 		Log.d(Constants.LOG_TAG, "Service: All service instances created");
		Messaging.subscribe(Player.PlayerChangeEvent.class, playerChangedStatus);
 		Log.d(Constants.LOG_TAG, "Service: Subscibed on several messages");
 		
 		
 		registerReceiver(new BroadcastReceiver() {
 			
 			boolean was_playing=false;
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				int state = (Integer) intent.getExtras().get("state");
 				if(state == 1)
 				{
 					if(was_playing)
 						player.resume();
 				}
 				if(state == 0)
 				{
 					if(player.getStatus() == Status.Playing)
 					{
 						was_playing = true;
 						player.pause();
 					}
 					else 
 						was_playing = false;
 				}
 			}
 		}, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
 	}
 
 	// This method is called every time UI starts
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(Constants.LOG_TAG, "Service.onStartCommand()");
 		Status status = player.getStatus();
 		if ((status == Status.Playing) || (status == Status.Processing)
 				|| (status == Status.Paused)) {
 			PlayerChangeEvent message = new PlayerChangeEvent();
 			message.status = status;
 			message.track = player.now_playing();
 			Messaging.fire(message);
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 	public void onDestroy() {
 		queue.clear();
 		Messaging.unSubscribe(Player.PlayerChangeEvent.class,
 				playerChangedStatus);
 		mNotificationManager.cancelAll();
 		super.onDestroy();
 		Log.d(Constants.LOG_TAG, "Service: Destroyed");
 	}
 
 	public IBinder onBind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onBind()");
 		return new CleerBinder();
 	}
 
 	public boolean onUnbind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onUnbind()");
		if(player.getStatus() != Status.Playing)
			stopSelf();	//Harakiri if service is no longer needed.
 		return true; // for onServiceConnected
 	}
 
 	public void onRebind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onRebind()");
 	}
 
 	@Override
 	public Library library() {
 		return library;
 	}
 
 	@Override
 	public Database storage() {
 		return database;
 	}
 
 	@Override
 	public Console console() {
 		return null;
 	}
 
 	@Override
 	public Player player() {
 		return player;
 	}
 
 	@Override
 	public Queue queue() {
 		return queue;
 	}
 
 	@Override
 	public ConsoleOutput output() {
 		return null;
 	}
 }
