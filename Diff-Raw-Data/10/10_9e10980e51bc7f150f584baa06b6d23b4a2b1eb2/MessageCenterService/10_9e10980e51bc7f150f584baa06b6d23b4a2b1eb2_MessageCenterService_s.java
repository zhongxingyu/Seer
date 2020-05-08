 package org.uniqush.android;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 
 import org.uniqush.client.Message;
 import org.uniqush.client.MessageCenter;
 
 import com.google.android.gcm.GCMRegistrar;
 
 public class MessageCenterService extends Service {
 
 	private String TAG = "UniqushMessageCenterService";
 	protected final static int CMD_CONNECT = 1;
 	protected final static int CMD_SEND_MSG_TO_SERVER = 2;
 	protected final static int CMD_SEND_MSG_TO_USER = 3;
 	protected final static int CMD_REQUEST_MSG = 4;
 	protected final static int CMD_CONFIG = 5;
 	protected final static int CMD_SET_VISIBILITY = 6;
 	protected final static int CMD_REQUEST_ALL_CACHED_MSG = 7;
 	protected final static int CMD_ERROR_ACCOUNT_MISSING = 8;
 	protected final static int CMD_SUBSCRIBE = 9;
 	protected final static int CMD_UNSUBSCRIBE = 10;
 	protected final static int CMD_MESSAGE_DIGEST = 11;
 	protected final static int CMD_USER_INFO_READY = 12;
 	protected final static int CMD_STOP = 13;
 	protected final static int CMD_REGID_READY = 14;
 	protected final static int CMD_MAX_CMD_ID = 15;
 
 	private UserInfoProvider userInfoProvider;
 	private Lock userInfoProviderLock;
 
 	private MessageCenter center;
 	private ConnectionInfo currentConn;
 	private MessageHandler handler;
 	private Thread readerThread;
 	private ReadWriteLock centerLock;
 
 	@Override
 	public void onCreate() {
 		userInfoProviderLock = new ReentrantLock();
 		centerLock = new ReentrantReadWriteLock();
 		userInfoProvider = null;
 		center = null;
 		readerThread = null;
 		handler = null;
 		Log.i(TAG, "service created");
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.i(TAG, "service destroyed");
 		centerLock.readLock().lock();
 		if (handler != null) {
 			handler.onServiceDestroyed();
 		}
 		centerLock.readLock().unlock();
 		this.disconnect();
 	}
 
 	private void setUserInfoProvider() {
 		userInfoProviderLock.lock();
 		userInfoProvider = ResourceManager.getUserInfoProvider(this);
 		userInfoProviderLock.unlock();
 	}
 
 	private void disconnect() {
 		centerLock.writeLock().lock();
 		if (this.center != null) {
 			this.center.stop();
 			try {
 				this.readerThread.join(3000);
 			} catch (InterruptedException e) {
 				this.readerThread.stop();
 				try {
 					this.readerThread.join();
 				} catch (InterruptedException e1) {
 					// XXX what should I do here?
 				}
 			}
 
 			this.center = null;
 			this.currentConn = null;
 			this.handler = null;
 		}
 		centerLock.writeLock().unlock();
 	}
 
 	private void connect(int callId, boolean onlyReportError) {
 		Log.i(TAG, "connect with call id " + callId);
 		userInfoProviderLock.lock();
 		if (this.userInfoProvider == null) {
 			this.userInfoProvider = ResourceManager.getUserInfoProvider(this);
 			if (this.userInfoProvider == null) {
 				Log.wtf(TAG,
 						"UserInfoProvider is missing. call MessageCenter.init() first!");
 				userInfoProviderLock.unlock();
 				return;
 			}
 		}
 		ConnectionInfo cinfo = this.userInfoProvider.getConnectionInfo();
 		MessageHandler msgHandler = this.userInfoProvider.getMessageHandler(
 				cinfo.getHostName(), cinfo.getPort(), cinfo.getServiceName(),
 				cinfo.getUserName());
 		UserInfoProvider uip = this.userInfoProvider;
 		userInfoProviderLock.unlock();
 
 		if (msgHandler == null || cinfo == null) {
 			Log.w(TAG, "msg handler or connection info is null");
 			return;
 		}
 
 		Log.i(TAG, "connect to " + cinfo.toString());
 		centerLock.writeLock().lock();
 		if (this.center != null) {
 			if (this.currentConn.equals(cinfo)) {
 				// We've already connected this server.
 				if (this.currentConn.shouldSubscribe() != cinfo
 						.shouldSubscribe()) {
 					centerLock.writeLock().unlock();
 					this.subscribe(callId, onlyReportError);
 				} else {
 					centerLock.writeLock().unlock();
 					if (!onlyReportError && callId > 0) {
 						msgHandler.onResult(callId, null);
 					}
 				}
 				return;
 			}
 			this.disconnect();
 		}
 
 		this.center = new MessageCenter(uip);
 		try {
 			this.center.connect(cinfo.getHostName(), cinfo.getPort(),
 					cinfo.getServiceName(), cinfo.getUserName(), msgHandler);
 		} catch (Exception e) {
 			Log.e(TAG, "Error on connection: " + e.toString());
 			this.center = null;
 			centerLock.writeLock().unlock();
 			if (callId >= 0) {
 				msgHandler.onResult(callId, e);
 			} else {
 				msgHandler.onError(e);
 			}
 			return;
 		}
 		readerThread = new Thread(this.center);
 		readerThread.start();
 		currentConn = cinfo;
		handler = msgHandler;
 		centerLock.writeLock().unlock();
 
 		Log.i(TAG, "connected");
 		this.subscribe(callId, onlyReportError);
 	}
 
 	private void subscribe(int callId, boolean onlyReportError) {
 		centerLock.readLock().lock();
 		if (center == null) {
 			return;
 		}
 		String regid = this.getRegId();
 		if (regid == null || regid.length() == 0) {
 			// We dot not have the registration id. Wait for it.
 			centerLock.readLock().unlock();
 			return;
 		}
 
 		if (center == null || handler == null || currentConn == null) {
 			centerLock.readLock().unlock();
 			Log.w(TAG, "regid is ready but the message center is not.");
 			return;
 		}
 		MessageHandler h = handler;
 		String service = this.currentConn.getServiceName();
 		String username = this.currentConn.getUserName();
 		HashMap<String, String> params = new HashMap<String, String>(3);
 		params.put("pushservicetype", "gcm");
 		params.put("service", service);
 		params.put("subscriber", username);
 		params.put("regid", regid);
 		if (currentConn.shouldSubscribe()
 				&& !ResourceManager.subscribed(this, service, username)) {
 			// should subscribe.
 			try {
 				center.subscribe(params);
 			} catch (Exception e) {
 				centerLock.readLock().unlock();
 				if (callId >= 0) {
 					h.onResult(callId, e);
 				} else {
 					h.onError(e);
 				}
 				return;
 			}
 			ResourceManager.setSubscribed(this, service, username, true);
 
 		} else if (!currentConn.shouldSubscribe()
 				&& ResourceManager.subscribed(this, service, username)) {
 			// should unsubscribe.
 			try {
 				center.unsubscribe(params);
 			} catch (Exception e) {
 				centerLock.readLock().unlock();
 				if (callId > 0) {
 					h.onResult(callId, e);
 				} else {
 					h.onError(e);
 				}
 				return;
 			}
 			ResourceManager.setSubscribed(this, service, username, false);
 		}
 		centerLock.readLock().unlock();
 
 		if (!onlyReportError && callId > 0 && h != null) {
 			h.onResult(callId, null);
 		}
 	}
 
 	abstract class MessageCenterOpt {
 		abstract protected void opt() throws InterruptedException, IOException;
 
 		public void execute(int callId) {
 			connect(callId, true);
 			centerLock.readLock().lock();
 			MessageHandler h = handler;
 			if (center == null) {
 				centerLock.readLock().unlock();
 				if (callId > 0 && h != null) {
 					h.onResult(callId, new Exception("Not ready"));
 				} else if (h != null) {
 					h.onError(new Exception("Not ready"));
 				}
 				return;
 			}
 
 			try {
 				this.opt();
 			} catch (IOException e) {
 				centerLock.readLock().unlock();
 				if (callId > 0 && h != null) {
 					h.onResult(callId, e);
 				} else if (h != null) {
 					h.onError(e);
 				}
 				return;
 			} catch (InterruptedException e) {
 				centerLock.readLock().unlock();
 				if (callId > 0 && h != null) {
 					h.onResult(callId, e);
 				} else if (h != null) {
 					h.onError(e);
 				}
 				return;
 			}
 			centerLock.readLock().unlock();
 			if (callId > 0 && h != null) {
 				h.onResult(callId, null);
 			}
 		}
 	}
 
 	private void sendMessageToServer(int callId, final Message msg) {
 		new MessageCenterOpt() {
 			protected void opt() throws InterruptedException, IOException {
 				center.sendMessageToServer(msg);
 			}
 		}.execute(callId);
 	}
 	
 	private void sendMessageToUser(final int callId, final String service,
 			final String username, final Message msg, final int ttl) {
 		new MessageCenterOpt() {
 			protected void opt() throws InterruptedException, IOException {
 				center.sendMessageToUser(service, username, msg, ttl);
 			}
 		}.execute(callId);
 	}
 
 	private String getRegId() {
 		userInfoProviderLock.lock();
 		String regId = GCMRegistrar.getRegistrationId(this);
 
 		if (regId.length() == 0) {
 			Log.i(TAG, "not registered");
 			GCMRegistrar.register(this, userInfoProvider.getSenderIds());
 			regId = null;
 		}
 		userInfoProviderLock.unlock();
 		return regId;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 		if (intent == null) {
 			this.stopSelf();
 			return START_NOT_STICKY;
 		}
 
 		int cmd = intent.getIntExtra("c", -1);
 		final int callId = intent.getIntExtra("id", -1);
 		final Message msg = (Message) intent.getSerializableExtra("msg");
 
 		if (cmd <= 0 || cmd >= MessageCenterService.CMD_MAX_CMD_ID) {
 			Log.i(TAG, "wrong command: " + cmd);
 			this.stopSelf();
 			return START_NOT_STICKY;
 		}
 
 		switch (cmd) {
 		// BOILERPLATE ALERT!
 		case CMD_USER_INFO_READY:
 			this.setUserInfoProvider();
 			this.getRegId();
 			break;
 		case CMD_REGID_READY:
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... params) {
 					subscribe(-1, false);
 					return null;
 				}
 			}.execute();
 			break;
 		case CMD_CONNECT:
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... params) {
 					connect(callId, false);
 					return null;
 				}
 			}.execute();
 			break;
 		case CMD_ERROR_ACCOUNT_MISSING:
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... params) {
 					centerLock.readLock().lock();
 					if (handler != null) {
 						handler.onMissingAccount();
 					}
 					centerLock.readLock().unlock();
 					return null;
 				}
 			}.execute();
 			break;
 		case CMD_SEND_MSG_TO_SERVER:
 			Log.i(TAG, "send message to server");
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... params) {
 					sendMessageToServer(callId, msg);
 					return null;
 				}
 			}.execute();
 			break;
 		case CMD_SEND_MSG_TO_USER:
 			Log.i(TAG, "send message to user");
 			final String receiver = intent.getStringExtra("username");
 			final String receiverService = intent.getStringExtra("service");
 			final int ttl = intent.getIntExtra("ttl", 0);
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 				protected Void doInBackground(Void... params) {
 					sendMessageToUser(callId, receiverService, receiver, msg, ttl);
 					return null;
 				}
 			}.execute();
 			break;
 		case CMD_STOP:
 			Log.i(TAG, "STOP");
 			this.disconnect();
 			this.stopSelf();
 			return START_NOT_STICKY;
 		}
 
 		return START_STICKY;
 	}
 }
