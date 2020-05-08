 package com.example.glscrollsample;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
import android.os.Debug;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 
 public class GLScrollSample extends Activity {
 
 	private MyGLSurfaceView mGLSurfaceView;
 	private Timer mTimer;
 	private Handler mHandler;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		Debug.startMethodTracing("GLScrollSample");
 		mGLSurfaceView = new MyGLSurfaceView(this);
 		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
 		setContentView(mGLSurfaceView);
 
 		if (!mGLSurfaceView.drawByEvent) {
 			startTimerEvent();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem item1, item2;
 		item1 = menu.add("Timer Draw Mode");
 		item1.setCheckable(true);
 		item2 = menu.add("Event Draw Mode");
 		item2.setCheckable(true);
 		if (mGLSurfaceView.drawByEvent) {
 			item1.setChecked(true);
 			item2.setChecked(false);	
 		} else {
 			item1.setChecked(false);
 			item2.setChecked(true);				
 		}
 			
 		item1.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 		    public boolean onMenuItemClick(MenuItem item) {
 		    	if (mGLSurfaceView.drawByEvent) {
 		    		mGLSurfaceView.drawByEvent = !mGLSurfaceView.drawByEvent;
 		    		startTimerEvent();
 		    	}
 				return false;
 		    }
 		});
 
 		item2.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 		    public boolean onMenuItemClick(MenuItem item) {
 		    	if (!mGLSurfaceView.drawByEvent) {
 		    		mGLSurfaceView.drawByEvent = !mGLSurfaceView.drawByEvent;
 					stopTimerEvent();
 				}
 				return false;
 		    }
 		});
 		
 		return true;
 	}
 
 	private void startTimerEvent() {
 		mTimer = new Timer(true);
 		mHandler = new Handler();
 		mTimer.schedule(new TimerTask() {
 			public void run() {
 				mHandler.post(new Runnable() {
 					public void run() {
 						mGLSurfaceView.requestRender();
 						}
 					});
 				}
 			}, 17, 17);
 	}
 
 	private void stopTimerEvent() {
 		mTimer.cancel();
 		mTimer = null;
 		mHandler = null;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mGLSurfaceView.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mGLSurfaceView.onPause();
 	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		mGLSurfaceView.onDestroy();
		Debug.stopMethodTracing();
	}
 }
