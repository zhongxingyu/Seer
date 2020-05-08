 package com.lab3129.gltest;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.LinearLayout;
 
 public class GLTest extends Activity {
 	private GLRenderer glr;
 	private Timer timer;
 	private SListener slistener;
 	
 	class myTask extends TimerTask{
 		@Override
 		public void run() {
 			Log.i("GLTest","FPS: "+glr.getFPS());
 			Log.i("GLTest","err: " + slistener.getCurrERR() + "pos :" + slistener.pos2String());
 		}
 	}
 	
 	@Override
 	public void onStop()
 	{
 		timer.cancel();
 		super.onStop();
 	}
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         LinearLayout l = (LinearLayout)findViewById(R.id.llay);
         slistener = new SListener(this);
         GLSurfaceView view = new GLSurfaceView(this);
         glr = new GLRenderer(this, slistener);
         view.setRenderer(glr);
         mLockScreenRotation();
         l.addView(view,new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.MATCH_PARENT,
                 LinearLayout.LayoutParams.WRAP_CONTENT));
         
         timer = new Timer();
         timer.schedule(new myTask(), 1000, 1000);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     private void mLockScreenRotation()
     {
       // Stop the screen orientation changing during an event
         switch (this.getResources().getConfiguration().orientation)
         {
       case Configuration.ORIENTATION_PORTRAIT:
         this.setRequestedOrientation(
         		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         break;
       case Configuration.ORIENTATION_LANDSCAPE:
         this.setRequestedOrientation(
         		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         break;
         }
     }
 }
