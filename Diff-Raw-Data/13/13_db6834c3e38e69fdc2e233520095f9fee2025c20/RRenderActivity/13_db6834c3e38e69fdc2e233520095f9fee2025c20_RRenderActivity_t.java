 package com.room.render;
 
 import com.room.Global;
 import com.room.MainActivity;
 import com.room.R;
 import com.room.media.MMusic;
 
 import android.content.*;
 import android.opengl.*;
 import android.os.*;
 import android.app.*;
 import android.view.*;
 
 public class RRenderActivity extends Activity
 {
 	public RRenderView view;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
     	super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);           
         
         view = new RRenderView(this);
         setContentView(view);
     }
     
     private class RRenderView extends GLSurfaceView
     {
         public RRenderView(Context context)
         {
             super(context);
 
             // Create an OpenGL ES 2.0 context
             setEGLContextClientVersion(2);
 
             // Set the Renderer for drawing on the GLSurfaceView
             setRenderer(RRenderer.getInstance());
         }    
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent e)
     {
     	RTouchController.getInstance().processTouchEvent(e);
         return true;
     }    
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
     	switch(keyCode)
     	{
 	    	case KeyEvent.KEYCODE_BACK:
 	    		showDialog(1); //we're hard coding the id since there will be only one dialog
 	    	return true;
     	}
     	return RKeyController.getInstance().onKeyDown(keyCode, event);
     }    
     
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event)
     {
     	return RKeyController.getInstance().onKeyUp(keyCode, event);
     }    
 
     @Override
     protected Dialog onCreateDialog(int id) { //id is ignored
         Dialog dialog = null;
         AlertDialog.Builder builder;
         builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.return_home)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                 	   Global.RESUME_MUSIC = true;
                	   Intent intent = new Intent(Global.mainActivity, MainActivity.class);
                	   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                	   startActivity (intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
         dialog = builder.create();
         return dialog;
     }
     
 	@Override
 	protected void onResume() {
 	      super.onResume();
 	      MMusic.playBGmusic(Global.mainActivity, R.raw.wind);
 	      MMusic.playTimedSound(Global.mainActivity);
 	}
 }
 
 
 
