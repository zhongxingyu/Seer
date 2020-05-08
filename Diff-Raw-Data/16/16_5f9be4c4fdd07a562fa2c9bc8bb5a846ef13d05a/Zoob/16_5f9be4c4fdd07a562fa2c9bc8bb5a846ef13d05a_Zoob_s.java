 package net.fhtagn.zoob;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.AssetFileDescriptor;
 import android.content.res.AssetManager;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 
 public class Zoob extends Activity {
 	private ZoobGLSurface mGLView;
 
 	static {
 		System.loadLibrary("zoob");
 	}
 	
 	private static native boolean isInMenu();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//EULA
 		Eula.show(this);
 		
 		mGLView = new ZoobGLSurface(this, (ZoobApplication)getApplication());
 		setContentView(mGLView);
 		
     //Force landscape
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mGLView.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mGLView.onResume();
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu (Menu menu) {
 		mGLView.onMenu();
 		return false;
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
     if (keyCode == KeyEvent.KEYCODE_BACK && !isInMenu()) {
     	mGLView.onMenu();
       return true;
     }
     return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 }
 
 /** Since the event listening thread doesn't run in the rendering thread,
  * we are buffering events in the rendering thread and they are processed (by
  * calling their JNI callbacks) just before rendering 
  * 
  * This class is used to store an event */
 class Command {
 	public enum Type {
 		EVENT_DOWN, EVENT_MOVE, EVENT_UP, EVENT_OTHER,
 		EVENT_PAUSE, EVENT_MENU
 	}
 	
 	public final float x;
 	public final float y;
 	
 	public final Type type;
 	
 	public Command (Type type) {
 		assert(type == Type.EVENT_PAUSE || type == Type.EVENT_MENU);
 		this.type = type;
 		this.x = this.y = 0;
 	}
 	
 	public Command (Type type, float x, float y) {
 		this.x = x;
 		this.y = y;
 		this.type = type;
 	}
 }
 
 class ZoobGLSurface extends GLSurfaceView {
 	ZoobRenderer mRenderer;
 	/**
 	 * These native methods MUST NOT CALL OPENGL. OpenGL/mRenderer is run in a separate
 	 * thread and calling opengl from these native methods will result in errors
 	 * like "call to OpenGL ES API with no current context" 
 	 */
 
 	
 	public ZoobGLSurface(Context context, ZoobApplication app) {
 		super(context);
 		mRenderer = new ZoobRenderer(context, app);
 		setRenderer(mRenderer);
 	}
 	
 	public void onResume () {
 		super.onResume();
 		mRenderer.triggerRestoreGL();
 	}
 	
 	public void onMenu () {
 		mRenderer.addCommand(new Command(Command.Type.EVENT_MENU));
 	}
 	
 	public void onPause () {
 		mRenderer.addCommand(new Command(Command.Type.EVENT_PAUSE));
 	}
 
 	public boolean onTouchEvent(final MotionEvent event) {
 		final float x = event.getX();
 		final float y = event.getY();
 
 		Command c = null;
 		switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				c = new Command(Command.Type.EVENT_DOWN, x, y);
 				break;
 			case MotionEvent.ACTION_MOVE:
 				c = new Command(Command.Type.EVENT_MOVE, x, y);
 				break;
 			case MotionEvent.ACTION_UP:
 				c = new Command(Command.Type.EVENT_UP, x, y);
 				break;
 			default:
 				c = new Command(Command.Type.EVENT_OTHER, x, y);
 				break;
 		}
 		assert(c != null);
 		mRenderer.addCommand(c);
 		//This is an advice from "Writing real time games for android" Google I/O presentation
 		//This avoid event flood when the screen is touched
 		try {
 	    Thread.sleep(16);
     } catch (InterruptedException e) {}
 		return true;
 	}
 }
 
 class ZoobRenderer implements GLSurfaceView.Renderer {
 	private Context context;
 	private ZoobApplication app;
 	private String apkFilePath;
 	
 	private List<Command> commands = new ArrayList<Command>();
 	
 	private boolean initialized = false; //avoid multiple calls to nativeInit
 	
 	private boolean restoreGL = false; //notify this renderer that it should restore opengl context (the app was resumed from sleep)
 
 	//This constructor is not ran in the rendering thread (but in the surface thread)
 	public ZoobRenderer (Context context, ZoobApplication app) {
 		this.context = context;
 		// return apk file path (or null on error)
 		ApplicationInfo appInfo = null;
 		PackageManager packMgmr = context.getPackageManager();
 		try {
	    appInfo = packMgmr.getApplicationInfo("net.fhtagn.zoob", 0);
     } catch (NameNotFoundException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException("Unable to locate assets, aborting...");
     }
 		apkFilePath = appInfo.sourceDir;
 		this.app = app;
 	}
 	
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		//Call ALL nativeInit methods here, because we want the JNIEnv of the rendering thread
 		//(see comments in app-android.cpp)
 		if (!initialized) {
 			nativeInit(apkFilePath, this);
 			initialized = true;
 		}
     nativeInitGL(app.getLevel(), app.getDifficulty());
 	}
 
 	public void onSurfaceChanged(GL10 gl, int w, int h) {
 		// gl.glViewport(0, 0, w, h);
 		nativeResize(w, h);
 	}
 	
 	public void triggerRestoreGL () {
 		restoreGL = true;
 	}
 
 	public void onDrawFrame(GL10 gl) {
 		if (restoreGL) {
 			nativeInitGL(app.getLevel(), app.getDifficulty());
 			restoreGL = false;
 		}
 		
 		//process commands
 		synchronized (commands) {
 			for (Command c : commands) {
 				switch (c.type) {
 					case EVENT_DOWN:
 						touchEventDown(c.x, c.y);
 						break;
 					case EVENT_UP:
 						touchEventUp(c.x, c.y);
 						break;
 					case EVENT_MOVE:
 						touchEventMove(c.x, c.y);
 						break;
 					case EVENT_OTHER:
 						touchEventOther(c.x, c.y);
 						break;
 					case EVENT_PAUSE:
 						nativePause();
 						break;
 					case EVENT_MENU:
 						nativeMenu();
 						break;
 				}
 			}
 			commands.clear();
 		}
 		
 		nativeRender();
 	}
 	
 	public void addCommand (Command c) {
 		synchronized (commands) {
 			commands.add(c);
 		}
 	}
 
   private static native void nativeInitGL(int level, int difficulty);
 	private static native void nativeInit(String apkPath, ZoobRenderer app);
 	private static native void nativeResize(int w, int h);
 	private static native void nativeRender();
 	
 	private static native void touchEventDown (float x, float y);
 	private static native void touchEventMove (float x, float y);
 	private static native void touchEventUp (float x, float y);
 	private static native void touchEventOther (float x, float y);
 	
 	private static native void nativePause();
 	private static native void nativeMenu();
 	
 	//These are stubs for upcall from JNI because the Application object isn't in the 
 	//same thread as the JNI stuff (nativeRender) and this can lead to random crashes
 	public void saveProgress (int level) {
 		app.saveProgress(level);
 	}
 		
 	public void saveDifficulty (int level) {
 		app.saveDifficulty(level);
 	}
 	
 	public void buyFullVersion () {
 		Uri fullVersionURI = Uri.parse("market://details?id=net.fhtagn.zoob");
 		context.startActivity(new Intent(Intent.ACTION_VIEW, fullVersionURI));
 	}
 	
 }
