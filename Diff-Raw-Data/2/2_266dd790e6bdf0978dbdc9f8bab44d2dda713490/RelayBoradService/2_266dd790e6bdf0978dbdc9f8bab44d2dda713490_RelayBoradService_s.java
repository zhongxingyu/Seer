 package test.work.testcontrolborad;
 
 import java.io.IOException;
 
 import java.net.InetSocketAddress;
 import java.nio.channels.SocketChannel;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.Process;
 
 public class RelayBoradService extends Service {
 
 	private Looper mServiceLooper;
 	private RelayBoradServiceHandler mServiceHandler;
 	private RelayBoardServiceReader mServiceReader;
 	/**
 	 * Target we publish for clients to send messages to
 	 * RelayBoradServiceHandler.
 	 */
 	Messenger mMessenger;
 
 	int RELAY_BORAD_PORT = 6000;
 	String RELAY_BORAD_HOST = "192.168.1.110";
 
 	SocketChannel mSocketChannel;
 
 	public class RelayBoradServiceHandler extends Handler {
 
 		public RelayBoradServiceHandler(Looper looper) {
 			super(looper);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			super.handleMessage(msg);
 		}
 	}
 	
 	public class RelayBoardServiceReader implements Runnable {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			while(true){
 				
 			}
 				
 		}
 		
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return mMessenger.getBinder();
 	}
 
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		// Start up the thread running the service. Note that we create a
 		// separate thread because the service normally runs in the process's
 		// main thread, which we don't want to block. We also make it
 		// background priority so CPU-intensive work will not disrupt our UI.
 		// This thread handle messenger and send to relayborad.
 		HandlerThread thread = new HandlerThread("ServiceStartArguments",
 				Process.THREAD_PRIORITY_BACKGROUND);
 		thread.start();
 
 		// Get the HandlerThread's Looper and use it for our Handler
 		mServiceLooper = thread.getLooper();
 		mServiceHandler = new RelayBoradServiceHandler(mServiceLooper);
 		mMessenger = new Messenger(mServiceHandler);
 		
 		// Start another thread runing read from the socket.
 		mServiceReader = new RelayBoardServiceReader();
		thread2 = Thread(mServiceReader, "ServiceReader");
 		thread2.start();
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// TODO Auto-generated method stub
 
 		InetSocketAddress inet_addr = new InetSocketAddress(RELAY_BORAD_HOST,
 				RELAY_BORAD_PORT);
 		try {
 			mSocketChannel = SocketChannel.open(inet_addr);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		// TODO Auto-generated method stub
 		return super.onUnbind(intent);
 	}
 
 }
