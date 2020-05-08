 package com.example.demo;
 
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.support.v4.app.NotificationCompat;
 import com.google.common.collect.ImmutableList;
 
 
 public class DemoService extends IntentService
 {
 	public static final String SCAN = "com.example.demo.action.SCAN";
 	public static final String SYNC = "com.example.demo.action.SYNC";
 	public static final int ON_PROGRESS = 0x01;
 	public static final int ON_COMPLETE = 0x02;
 
 	private static final int NOTIFY_ID = 0xF0000000;
 
 	private Message mLastMessage = null;
 	private Handler mHandler;
 	private NotificationManager mNotifyManager = null;
 	private NotificationCompat.Builder mNotify;
 
 	/**
 	 * A constructor is required, and must call the super IntentService(String)
 	 * constructor with a name for the worker thread.
 	 */
 	public DemoService()
 	{
 		super( "DemoService" );
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
 		onStart( intent, startId );
 		return START_STICKY;
 	}
 
 	/**
 	 * The IntentService calls this method from the default worker thread with
 	 * the intent that started the service. When this method returns, IntentService
 	 * stops the service, as appropriate.
 	 */
 	@Override
 	protected void onHandleIntent(Intent intent)
 	{
 		if ( intent == null )
 			return;
 
 		// Prepare notifications
 		setUpNotification();
 
 		String action = intent.getAction();
 		if ( action.equals( SYNC ) ) {
 			sync();
 		}
 		if ( action.equals( SCAN ) ) {
 			scan();
 		}
 		tearDownNotification();
 	}
 
 	private void sync()
 	{
 		doWhile( ImmutableList.<String>of( "sync-one", "sync-two", "sync-three" ) );
 		send( ON_COMPLETE, 0, SYNC );
 	}
 
 	private void scan()
 	{
 		doWhile( ImmutableList.<String>of( "scan-one", "scan-two", "scan-three" ) );
 		send( ON_COMPLETE, 0, SCAN );
 	}
 
 	private void doWhile(ImmutableList<String> files)
 	{
 		for ( String file : files ) {
 			updateNotification( file );
 			send( ON_PROGRESS, 0, file );
 			sleep();
 		}
 	}
 
 	/**
 	 *  Normally we would do some work here, like download a file.
 	 * For our demo, we just sleep for 5 seconds.
 	 */
 	private void sleep()
 	{
 		long endTime = System.currentTimeMillis() + 5 * 1000;
 		while ( System.currentTimeMillis() < endTime ) {
 			synchronized ( this ) {
 				try {
 					wait( endTime - System.currentTimeMillis() );
 				} catch ( Exception e ) {
 				}
 			}
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		return new LocalBinder();
 	}
 
 	private void send(int type, int arg1, String message)
 	{
 		// We do not obtain() a new message from the message pool
 		// because we want to save the last message sent, incase we
 		// must resend it when the client reconnects
 		mLastMessage = new Message();
 		mLastMessage.what = type;
 		mLastMessage.obj = message;
 		mLastMessage.arg1 = arg1;
 		sendMessage( mLastMessage );
 	}
 
 	private void sendMessage(Message message)
 	{
 		if ( message == null || mHandler == null ) {
 			return;
 		}
 		mHandler.sendMessage( Message.obtain( message ) );
 	}
 
 	public void updateNotification(String message)
 	{
 		if ( mNotify != null ) {
 			mNotify.setContentText( message );
 			mNotifyManager.notify( NOTIFY_ID, mNotify.getNotification() );
 		}
 	}
 
 	public void tearDownNotification()
 	{
 		if ( mNotifyManager != null ) {
 			mNotifyManager.cancelAll();
 		}
 		mNotifyManager = null;
 		mNotify = null;
 	}
 
 	public void setUpNotification()
 	{
 		mNotifyManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
 		// If user clicks on the notification, it should start the main SuperGNES activity
 		PendingIntent notifyIntent = PendingIntent.getActivity( getApplicationContext(), 0,
 				new Intent( getApplicationContext(), MainActivity.class ),
 				PendingIntent.FLAG_UPDATE_CURRENT );
 
 		// Build the notification
 		mNotify = new NotificationCompat.Builder( this );
 		mNotify.setContentIntent( notifyIntent );
 		mNotify.setContentTitle( getString( R.string.service_running ) )
 				.setSmallIcon( R.drawable.download );
 		// Set the notification
 		mNotifyManager.notify( NOTIFY_ID, mNotify.getNotification() );
 	}
 
 	/**
 	 * Binder interface that allows clients to control the service
 	 */
 	public class LocalBinder extends Binder
 	{
 		public void setHandler(Handler handler)
 		{
 			mHandler = handler;
 			sendMessage( mLastMessage );
 		}
 
 		public void clearHandler()
 		{
 			DemoService.this.mHandler = null;
 		}
 	}
 }
