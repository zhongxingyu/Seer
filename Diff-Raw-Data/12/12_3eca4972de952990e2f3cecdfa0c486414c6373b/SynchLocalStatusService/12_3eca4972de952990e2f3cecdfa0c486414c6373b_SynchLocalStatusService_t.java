 package isel.leic.pdm;
 
 import isel.leic.pdm.dal.StatusData;
 import isel.leic.pdm.dal.UserOfflineStatusAdapter;
 
 import java.util.LinkedList;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 
 public class SynchLocalStatusService extends Service
 {
 	public static final String TAG = "SynchLocalStatusService";
 	public static final String REQUEST_ID = "requestid_key";
 	private PostHandler serviceHandler;
 	private HandlerThread postHandler;
 	private Looper loop;
 	private UserOfflineStatusAdapter userOfflineStatusAdapter;
 	
 	@Override
 	public IBinder onBind(Intent arg0)
 	{
 		return null;
 	}
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		Log.i(TAG, "onCreate");
 		userOfflineStatusAdapter = new UserOfflineStatusAdapter(this.getApplication());
 		postHandler = new HandlerThread("HandlerThread");
 		postHandler.start();
 		loop = postHandler.getLooper();
 		serviceHandler = new PostHandler(loop);
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		Log.i(TAG, "onDestroy");
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
 		Log.i(TAG, "onStartCommand");
 		Log.i("Thread", Thread.currentThread().getId() + "");
 		Message m = new Message();
 		m.getData().putInt(REQUEST_ID, startId);
 		serviceHandler.sendMessage(m);
 		return START_STICKY;
 	}
 	
 	private void postTwitt(String msg)
 	{
 		if(msg != null)
 		{
 			Twitter t = ((TimelineApplication) getApplication()).getTwitter();
 			t.updateStatus(msg);
 
 		}
 	}
 	
 	private class PostHandler extends Handler
 	{
 		public PostHandler(Looper l)
 		{
 			super(l);
 		}
 		
 		@Override
 		public void handleMessage(Message msg)
 		{
 			super.handleMessage(msg);
 			
 			if(ConnectivityUtils.checkConnectivity(SynchLocalStatusService.this))
 			{
 				userOfflineStatusAdapter.open();
 				
 				LinkedList<StatusData> status = (LinkedList<StatusData>) userOfflineStatusAdapter.getAll();
 				
 				userOfflineStatusAdapter.close();
 
 				if(status != null)
 				{
 					try
 					{
 						for(StatusData sd : status)
 						{
							postTwitt(sd._text);
 						}
 					}
 					catch (TwitterException e)
 					{
 
 					}
 				}
 			}
 			
 			stopSelf(msg.getData().getInt(REQUEST_ID));	
 		}
 	}
 }
