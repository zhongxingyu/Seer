 package pro.trousev.cleer.android.service;
 
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Queue;
 import pro.trousev.cleer.Queue.EnqueueMode;
 import pro.trousev.cleer.android.AndroidMessages;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRequestMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRespondMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceTaskMessage;
 import pro.trousev.cleer.android.AndroidMessages.TypeOfResult;
 import pro.trousev.cleer.android.Constants;
 import pro.trousev.cleer.sys.QueueImpl;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 //TODO Make notification and foreground job
 public class AndroidCleerService extends Service {
 	private static ServiceRespondMessage respondMessage = new ServiceRespondMessage();
 
 	private Queue queue = null;
 
 	// private Database database = null;
 
 	// Binder allow us get Service.this from the Activity
 	public class CleerBinder extends Binder {
 		public AndroidCleerService getService() {
 			return AndroidCleerService.this;
 		}
 	}
 
 	public void onCreate() {
 		super.onCreate();
 		queue = new QueueImpl(new PlayerAndroid());
 		// database = new DatabaseImpl(Constants.DATABASE_PATH);
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
 					}
 				});
 
 		Log.d(Constants.LOG_TAG, "Service.onCreate()");
 	}
 
 	// This method is called every time UI starts
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(Constants.LOG_TAG, "Service.onStartCommand()");
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	public void onDestroy() {
 		Log.d(Constants.LOG_TAG, "Service.onDestroy()");
		queue.clear();
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
