 package com.ybi.ragequit;
 
 import rajawali.RajawaliActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 
 /**
  * 
  *
  */
 
 public class MainActivity extends RajawaliActivity implements OnTouchListener {
 
 	private MainRenderer mRenderer;
 	private FeedService mBoundService;
 	private boolean mIsBound;
 	private int currentscreen;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		mRenderer = new MainRenderer(this);
 		mRenderer.setSurfaceView(mSurfaceView);
 		super.setRenderer(mRenderer);
 		mSurfaceView.setOnTouchListener(this);
 		Intent intent = new Intent(this, FeedService.class);
 		startService(intent);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 
 	private final ServiceConnection mConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service.  Because we have bound to a explicit
 			// service that we know is running in our own process, we can
 			// cast its IBinder to a concrete class and directly access it.
 			mBoundService = ((FeedService.LocalBinder) service).getService();
 
 			// Tell the user about this for our demo.
 			Toast.makeText(MainActivity.this, R.string.local_service_connected, Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			// Because it is running in our same process, we should never
 			// see this happen.
 			mBoundService = null;
 			Toast.makeText(MainActivity.this, R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	void doBindService() {
 		// Establish a connection with the service.  We use an explicit
 		// class name because we want a specific service implementation that
 		// we know will be running in our own process (and thus won't be
 		// supporting component replacement by other applications).
 		bindService(new Intent(MainActivity.this, FeedService.class), mConnection, Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 
 	void doUnbindService() {
 		if (mIsBound) {
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mIsBound = false;
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		doUnbindService();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
 		float pos = (event.getRawX() - width / 2.0f) / width;
 		//final int X = (int) event.getRawX();
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			break;
 		case MotionEvent.ACTION_UP:
 			Log.d("RageQuit", "Position = " + pos);
 			if (pos > 0.2) {
 				mRenderer.flip();
 				currentscreen += 1;
 				return true;
 			} else if (pos < -0.2 && currentscreen > 0) {
 				mRenderer.flip();
 				currentscreen -= 1;
 				return true;
 			}
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			break;
 		case MotionEvent.ACTION_POINTER_UP:
 			break;
 		case MotionEvent.ACTION_MOVE:
 			mRenderer.rotate(pos);
 			break;
 		}
 
 		return true;
 
 		//if(event.getAction() == MotionEvent.ACTION_DOWN)
 		//		{
 		//			// amount of left or right draggin
 		//			// 2/3 is triggering next or previous
 		//			int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
 		//			float pos = (event.getX() - width / 2.0f) / width;
 		//			Log.d("RageQuit", "Position = " + pos);
 		//			mRenderer.rotate(pos);
 		//		}
 		// return super.onTouchEvent(event);
 	}
 
 
 }
