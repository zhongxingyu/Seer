 package com.rc.ui;
 
 import static com.rc.base.Output.*;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.rc.MainExecutor;
 import com.rc.base.Executor;
 import com.rc.base.Output;
 import com.rc.base.OutputFunc;
 import com.rc.util.IntentFor;
 import com.rc.util.Keys;
 import com.rc.util.Preferences;
 import com.rc.util.TcpClient;
 
 public class RemoteControlService extends Service {
 	
 	public static final String TAG = RemoteControlService.class.getSimpleName();
 	public static final int NOTIF_ID = 1;
 	
 	public static boolean isRunning;
 	
 	public final IBinder mBinder = new ServiceBinder();
 	private Handler mHandler = new Handler();
 	
 	private Preferences mPrefs;
 	private Executor mMainExec;
 	private TcpClient mClient;
 	
 	private OutputFunc mOutputCallback = new OutputFunc() {
 		
 		@Override
 		public void write(String text) {
 			byte[] bytes = text.getBytes();
 			mClient.write(bytes);
 		}
 	};
 	
 	private ExecutorService mConnExec = Executors.newSingleThreadExecutor();
 	private ExecutorService mCommandExec = Executors.newSingleThreadExecutor();
 	private LinkedBlockingQueue<byte[]> mInputQueue = new LinkedBlockingQueue<byte[]>(); 
 	
 	public class ServiceBinder extends Binder {
 		
 		public RemoteControlService getService() {
 			return RemoteControlService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
         mMainExec = MainExecutor.getInstance(getApplicationContext());
 		mPrefs = Preferences.getInstance(getApplicationContext());
 		isRunning = true;
 		startTcpClient();
 	}
 
 	@SuppressLint("DefaultLocale")
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 		mCommandExec.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				try {
 					while (!Thread.interrupted()) {
 						
 						final byte[] input = mInputQueue.take();
 						
 						mHandler.post(new Runnable() {
 							
 							@Override
 							public void run() {
 								executeCommand(input);
 							}
 						});
 					}
 				} catch (InterruptedException e) {
 					Log.i(TAG, e.getLocalizedMessage(), e);
 				}				
 				
 			}
 		});
 		
 		return START_STICKY;
 	}
 	
 	private void executeCommand(byte[] input) {
 		
 		if (input == null || input.length == 0)
 			return;
 		
 		String commandText = Character.isLetterOrDigit(input[0]) ? 
 				new String(input) : String.valueOf(input[0]);
 				
 		print(commandText);
 		
 		try {
 			
 			if (!mMainExec.execute(commandText)) {
 				print("unknown command");
 			}
 			
 		} catch (Exception e) {
 			String msg = e.getCause() == null ? 
 					e.getMessage() : e.getCause().getMessage();
 			print("error: " + msg);
 		}		
 		
 		print();
 	}
 
 	private void startTcpClient() {
 		
 		mConnExec.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				try {
 					while (!Thread.interrupted()) {
 						
 						showNotification("Connecting...", 
 								String.format("Connecting to %s:%d...", mPrefs.host(), mPrefs.port()));
 						
 						try {
 							
 							mClient = new TcpClient(mPrefs.host(), mPrefs.port());
 							
							showNotification("Remote console", 
 									String.format("Connected to %s:%d", mPrefs.host(), mPrefs.port()));
 							
 							Output.registerOutput(mOutputCallback);
 							mClient.start(mInputQueue);
 							Output.unregisterOutput(mOutputCallback);
 							
 						} catch (IOException e) {
 							Log.e(TAG, e.getLocalizedMessage(), e);
 							
 							showNotification("No Connection", 
 									String.format("Connect failed. Will try in %d seconds", mPrefs.reconnectInterval()));
 							
 							TimeUnit.SECONDS.sleep(mPrefs.reconnectInterval());
 						}
 						
 					}
 				} catch (InterruptedException e) {
 					Log.i(TAG, e.getLocalizedMessage(), e);
 				}
 			}
 			
 		});
 		
 	}
 
 	@SuppressWarnings("deprecation")
 	private void showNotification(final String title, final String msg) {
 		
 		mHandler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				Context context = getBaseContext();
 				
 		        Intent intent = IntentFor.mainActivity(context);
 		        intent.putExtra(Keys.NOTIF_ID, NOTIF_ID);
 		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		        PendingIntent contentIntent = 
 		        		PendingIntent.getActivity(context, 0, intent, 0);
 		        
 				Notification notif = new Notification();
 				notif.icon = R.drawable.ic_notif;
 				notif.tickerText = msg;
 				notif.setLatestEventInfo(context, title, msg, contentIntent);
 				startForeground(NOTIF_ID, notif);
 			}
 		});
 	}
 
 	@Override
 	public void onDestroy() {
 		mConnExec.shutdownNow();
 		mCommandExec.shutdownNow();
 		
 		if (mClient != null)
 			mClient.stop();
 		
 		isRunning = false;
 		
 		Log.i(TAG, "Commands service shut down");
 	}
 }
