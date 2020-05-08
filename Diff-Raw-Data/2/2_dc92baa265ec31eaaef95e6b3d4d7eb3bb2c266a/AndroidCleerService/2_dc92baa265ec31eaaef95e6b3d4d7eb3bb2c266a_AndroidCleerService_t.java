 package pro.trousev.cleer.android.service;
 
 import pro.trousev.cleer.Database;
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Item.NoSuchTagException;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Playlist;
 import pro.trousev.cleer.Queue;
 import pro.trousev.cleer.Queue.EnqueueMode;
 import pro.trousev.cleer.android.AndroidMessages;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRequestMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRespondMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceTaskMessage;
 import pro.trousev.cleer.android.AndroidMessages.TypeOfResult;
 import pro.trousev.cleer.android.Constants;
 import pro.trousev.cleer.sys.QueueImpl;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class AndroidCleerService extends Service {
 	private static ServiceRespondMessage respondMessage = new ServiceRespondMessage();
 
 	private Queue queue = null;
 
 	private Database database = null;
 
 	// TODO Notification update on song change
 	private Notification notification = null;
 	
 	private NotificationManager notificationManager = null;
 
 	// Binder allow us get Service.this from the Activity
 	public class CleerBinder extends Binder {
 		public AndroidCleerService getService() {
 			return AndroidCleerService.this;
 		}
 	}
 
 	private Notification makeNotification() {
 		String _s = null;
 		Notification notification = null;
 		try {
 			_s = "Song: " + queue.playing_track().tag("name").value();
 			notification = new Notification.Builder(getApplicationContext())
 					.setContentTitle(Constants.NOTIFICATION_TITLE)
 					.setContentText(_s).build();
 		} catch (NoSuchTagException e) {
 			notification = new Notification.Builder(getApplicationContext())
 					.setContentTitle(Constants.NOTIFICATION_TITLE)
 					.setContentText("NO SONG NAME AVALIBLE").build();
 			// e.printStackTrace();
 		}
 		return notification;
 	}
 	
 	private void updateNotification() {
 		notification = makeNotification();
 		notificationManager.notify(Constants.PLAYER_NOTIFICATION_ID, notification);
 	}
 
 	public void onCreate() {
 		super.onCreate();
 		queue = new QueueImpl(new PlayerAndroid());
		//database = new DatabaseImpl(Constants.DATABASE_PATH);
 		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		Messaging.subscribe(AndroidMessages.ServiceRequestMessage.class,
 				new Messaging.Event() {
 
 					@Override
 					public void messageReceived(Message message) {
 						// TODO end implementation of that event
 						ServiceRequestMessage mes = (ServiceRequestMessage) message;
 						if (mes.type == TypeOfResult.Compositions)
 							;
 						switch (mes.type) {
 						case Compositions:
 							break;
 						case Queue:
 							break;
 						case Albums:
 							break;
 						case Genres:
 							break;
 						case Artists:
 							break;
 						case Playlists:
 							break;
 						case Playlist:
 							break;
 						case PlaylistsInDialog:
 							break;
 						}
 						respondMessage.typeOfContent = mes.type;
 						Messaging.fire(respondMessage);
 					}
 				});
 		Messaging.subscribe(AndroidMessages.ServiceTaskMessage.class,
 				new Messaging.Event() {
 
 					@Override
 					public void messageReceived(Message message) {
 						// TODO end implementation of that event
 						ServiceTaskMessage mes = (ServiceTaskMessage) message;
 						switch (mes.action) {
 						case Play:
 							notification = makeNotification();
 							startForeground(Constants.PLAYER_NOTIFICATION_ID,
 									notification);
 							queue.play();
 							break;
 						case Pause:
 							queue.pause();
 							stopForeground(false);
 							break;
 						case Next:
 							queue.next();
 							break;
 						case Previous:
 							queue.prev();
 							break;
 						case addToQueue:
 							queue.enqueue(mes.list, EnqueueMode.AfterAll);
 							break;
 						case setToQueue:
 							queue.enqueue(mes.list, EnqueueMode.ReplaceAll);
 							break;
 						default:
 							break;
 						// TODO add others...
 						}
 						updateNotification();
 					}
 				});
 
 		Log.d(Constants.LOG_TAG, "Service.onCreate()");
 	}
 
 	//
 	// private void play() {
 	// // TODO delete that
 	// PlayerChangeEvent changeEvent = new PlayerChangeEvent();
 	// changeEvent.status = Status.Playing;
 	// Messaging.fire(changeEvent);
 	// Log.d(Constants.LOG_TAG, "Service.play()");
 	// }
 	//
 	// private void pause() {
 	// // TODO delete that
 	// PlayerChangeEvent changeEvent = new PlayerChangeEvent();
 	// changeEvent.status = Status.Paused;
 	// Messaging.fire(changeEvent);
 	// Log.d(Constants.LOG_TAG, "Service.pause()");
 	// }
 	//
 	// private void next() {
 	// Log.d(Constants.LOG_TAG, "Service.next()");
 	// }
 	//
 	// private void prev() {
 	// Log.d(Constants.LOG_TAG, "Service.prev()");
 	// }
 	//
 	// // adds songs at the end of queue
 	// public void addToQueue(List<Item> tracks) {
 	//
 	// }
 	//
 	// // clear queue, set list<item> in it, start playing song with current
 	// index
 	// public void setToQueue(List<Item> tracks, int index) {
 	// Log.d(Constants.LOG_TAG, "Service.setToQueue");
 	// }
 	//
 	// // returns songs from the queue
 	// public List<Item> getQueueItems() {
 	// return null;
 	// }
 	//
 	// // add item into the user playlist
 	// public void addItemToList(Item item, Playlist playlist) {
 	//
 	// }
 	//
 	// // create new user playlist
 	// public void createNewList(String name) {
 	// // Do we need that method?
 	// }
 	//
 	// // returns list of user playlists
 	// public List<Playlist> getPlaylists() {
 	// return null;
 	// }
 	//
 	// // returns list of Items from database, which suit to the searchQuery
 	// public List<Item> getListOfTracks(String searchQuery) {
 	// return null;
 	// }
 	//
 	// // Returns list of items with all represented values of some tag tagName
 	// public List<Item> getListOfTagValues(String tagName) {
 	//
 	// return null;
 	// }
 	//
 	// // return all albums with information about artist
 	// // album == item
 	// public List<Item> getListOfAlbums() {
 	// return null;
 	// }
 
 	// This method is called every time UI starts
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(Constants.LOG_TAG, "Service.onStartCommand()");
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	public void onDestroy() {
 		Log.d(Constants.LOG_TAG, "Service.onDestroy()");
 		super.onDestroy();
 	}
 
 	public IBinder onBind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onBind()");
 		return new CleerBinder();
 	}
 
 	public boolean onUnbind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onUnbind()");
 		return true; // for onServiceConnected
 	}
 
 	public void onRebind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onRebind()");
 	}
 }
