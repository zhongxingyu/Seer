 /* This is a port of the JGroups Draw demo
  * by Yann Sionneau <yann.sionneau@telecom-sudparis.eu>
  */
 
package net.sionneau.touchsurface;
 
 import org.jgroups.*;
 import org.jgroups.util.Util;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Bitmap.Config;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 public class TouchSurface extends Activity {
     /** Called when the activity is first created. */
 	
 	Bitmap    buffer;
 	Canvas    surface;
 	Paint	  paint;
 	MySurface view;
 	int		  tool;
 	MulticastLock lock;
 	
 	private Channel channel = null;
 	private String groupname = "DrawGroupDemo";
 	boolean no_channel = false;
     boolean jmx;
     private boolean use_state = false;
     private long state_timeout = 5000;
 	private boolean use_blocking = false;
 	private Draw draw = null;
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		   Log.e("touchsurface", "Hello !");
 
 		   
 		   
 		    //if (draw == null)
 		    	//Log.e("TouchSurface", "draw == null");
         
 	        super.onCreate(savedInstanceState);
 			view = new MySurface(this);
 			setContentView(view);
 		    
     }
     
     
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
         vib.vibrate(50);
     	
     	if (keyCode == KeyEvent.KEYCODE_MENU) {
 	    	paint.setColor(0xFFFFFFFF);
 	    	surface.drawPaint(paint);
 			view.invalidate();
 			draw.sendClearPanelMsg();
     	}
     	//this.finish();
     	return true;
     }
 
     
     public class MySurface extends SurfaceView implements SurfaceHolder.Callback {
     	
     	SurfaceHolder holder;
     	
 		MySurface(Context context) {
 			super(context);
 			holder = getHolder();
 			holder.addCallback(this);
 		}
     	
 
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			float cx = event.getX();
 			float cy = event.getY();
 			
 			switch(event.getAction()){
 				case MotionEvent.ACTION_DOWN:
 				case MotionEvent.ACTION_MOVE:
 				
 					Paint p = new Paint();
 					p.setColor(0xFF0000FF);
 					if (draw == null)
 						Log.e("TouchSurface", "draw == null");
 					else
 						draw.TouchEvent(cx, cy);	
 					surface.drawPoint(cx, cy, p);
 			}    	    
 
 			
 			view.invalidate();
 			return true;
 		}
 		
 		@Override
 		public void invalidate() {
 			if(holder!=null){
 				Canvas c = holder.lockCanvas();			
 				if(c!=null){					
 					c.drawBitmap(buffer,0,0,null);
 					holder.unlockCanvasAndPost(c);				
 				}
 			}
 		}
 		
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			// TODO Auto-generated method stub
 		}
 
 	
 		public void surfaceCreated(SurfaceHolder holder) {
 			   String           props="udp.xml";
 			   boolean          no_channel=false;
 			   boolean          jmx=false;
 			   boolean          use_state=false;
 			   boolean          use_blocking=false;
 			   String           group_name=null;
 			   long             state_timeout=5000;
 			   
 			   
 			   buffer  = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
 			   surface = new Canvas(buffer);
 			   paint   = new Paint();
 
 
 			/*   WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 			   lock = wifi.createMulticastLock("mylock");
 			   lock.setReferenceCounted(true);
 			   lock.acquire();
 			  */ 
 			
 			
 			  //System.out.println("LOL");
 			   Log.i("TouchSurface", "toto");
 			   try {
 			        draw = new Draw(props, no_channel, jmx, use_state, state_timeout, use_blocking, surface, view);
 			        if(group_name != null)
 			            draw.setGroupName(group_name);
 			        draw.go();
 			    }
 			    catch(Throwable e) {
 			        e.printStackTrace();
 			        System.exit(0);
 			    }
 			
 			paint.setColor(0xFFFFFFFF);
 			surface.drawPaint(paint);
 			view.invalidate();
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			// TODO Auto-generated method stub
 		}
 		
     }
     
}
