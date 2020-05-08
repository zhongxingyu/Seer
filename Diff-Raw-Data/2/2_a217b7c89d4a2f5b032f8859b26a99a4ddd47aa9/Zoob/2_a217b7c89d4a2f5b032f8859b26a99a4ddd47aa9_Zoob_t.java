 package net.fhtagn.zoobgame;
 
 import java.lang.reflect.Method;
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
 		EVENT_PAUSE, EVENT_MENU, EVENT_TRACKBALL, EVENT_TRACKBALL_CLICK,
 		EVENT_SECONDARY_DOWN, EVENT_SECONDARY_MOVE, EVENT_SECONDARY_UP
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
 
 interface MotionEventHandler {
 	public Command processEvent (MotionEvent e);
 }
 
 class SingleTouchMotionHandler implements MotionEventHandler {
 	public Command processEvent (MotionEvent e) {
 		Command c = null;
 		final float x = e.getX();
 		final float y = e.getY();
 		switch (e.getAction()) {
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
 		return null;
 	}
 }
 
 class MultiTouchMotionHandler implements MotionEventHandler {
 	private static final String TAG = "SingleTouchEventProcessor";
 	private static final int INVALID_POINTER_ID = -1;
 	
 	//We support at most 2 active pointers. The first one is always the one controlling the movements
 	private int [] activePointersID = {INVALID_POINTER_ID, INVALID_POINTER_ID};
 	
 	private static int getActionIndex (MotionEvent e) {
 		final int action = e.getAction();
 		return (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
 	}
 	
 	public Command processEvent (MotionEvent e) {
 		final int action = e.getAction();
 		switch (action & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN:
 				activePointersID[0] = e.getPointerId(0);
 				//Log.v(TAG, "Primary pointer down, id : " + activePointersID[0]);
 				return new Command(Command.Type.EVENT_DOWN, e.getX(0), e.getY(0));
 			case MotionEvent.ACTION_POINTER_DOWN: {
 				final int pointerIndex = getActionIndex(e);
 				final int pointerID = e.getPointerId(pointerIndex);
 				if (activePointersID[1] == INVALID_POINTER_ID) {
 					activePointersID[1] = pointerID;
					Log.v(TAG, "Secondary pointer down, id : " + pointerID + "("+e.getX(pointerIndex) + ","+e.getY(pointerIndex) +")");
 					return new Command(Command.Type.EVENT_SECONDARY_DOWN, e.getX(pointerIndex), e.getY(pointerIndex));
 				}
 				break;
 			}
 			case MotionEvent.ACTION_MOVE:
 				if (activePointersID[0] != INVALID_POINTER_ID) {
 					final int primaryIndex = e.findPointerIndex(activePointersID[0]);
 					//Log.v(TAG, "Move ("+activePointersID[0]+") ("+e.getX(primaryIndex) + ","+e.getY(primaryIndex)+")");
 					return new Command(Command.Type.EVENT_MOVE, e.getX(0), e.getY(0));
 				} 
 				if (activePointersID[1] != INVALID_POINTER_ID) {
 					final int secondaryIndex = e.findPointerIndex(activePointersID[1]);
 					//Log.v(TAG, "Move ("+activePointersID[1]+") ("+e.getX(secondaryIndex) + ","+e.getY(secondaryIndex)+")");
 					return new Command(Command.Type.EVENT_SECONDARY_MOVE, e.getX(secondaryIndex), e.getY(secondaryIndex));
 				}
 				break;
 			case MotionEvent.ACTION_UP:
 			case MotionEvent.ACTION_CANCEL:
 			case MotionEvent.ACTION_POINTER_UP: {
 				final int pointerIndex = getActionIndex(e);
 				final int pointerID = e.getPointerId(pointerIndex);
 				if (activePointersID[0] == pointerID) {
 					//Log.v(TAG, "Primary pointer up, id : " + activePointersID[0]);
 					activePointersID[0] = INVALID_POINTER_ID;
 					return new Command(Command.Type.EVENT_UP, e.getX(0), e.getY(0));
 				}
 				if (activePointersID[1] == pointerID) {
 					//Log.v(TAG, "Secondary pointer up, id : " + e.getPointerId(pointerIndex));
 					activePointersID[1] = INVALID_POINTER_ID;
 					return new Command(Command.Type.EVENT_SECONDARY_UP, e.getX(pointerIndex), e.getY(pointerIndex));
 				}
 				break;
 			}
 		}
 		return null;
 	}
 }
 
 
 class ZoobGLSurface extends GLSurfaceView {
 	ZoobRenderer mRenderer;
 	private static final String TAG = "ZoobGLSurface";
 	private static MotionEventHandler motionHandler = null;
 	
 	static {
 		initialize();
 	}
 	
 	public static void initialize () {
 		try {
 	    Method findPointerIndexMethod = MotionEvent.class.getMethod("findPointerIndex", new Class[] {int.class});
 	    motionHandler = new MultiTouchMotionHandler();
 	    Log.i(TAG, "multitouch");
     } catch (NoSuchMethodException e) {
 	    motionHandler = new SingleTouchMotionHandler();
 	    Log.i(TAG, "no multitouch");
     }
 	}
 	
 	/**
 	 * These native methods MUST NOT CALL OPENGL. OpenGL/mRenderer is run in a separate
 	 * thread and calling opengl from these native methods will result in errors
 	 * like "call to OpenGL ES API with no current context" 
 	 */
 	
 	public ZoobGLSurface(Context context, ZoobApplication app) {
 		super(context);
 		mRenderer = new ZoobRenderer(context, app);
 		setRenderer(mRenderer);
 		setFocusableInTouchMode(true); //necessary to get trackball events
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
 
 	@Override
 	public boolean onTouchEvent(final MotionEvent event) {
 		final float x = event.getX();
 		final float y = event.getY();
 		
 		Command c = motionHandler.processEvent(event);
 		if (c != null)
 			mRenderer.addCommand(c);
 
 		//This is an advice from "Writing real time games for android" Google I/O presentation
 		//This avoid event flood when the screen is touched
 		try {
 	    Thread.sleep(16);
     } catch (InterruptedException e) {}
 		return true;
 	}
 	
 	@Override
 	public boolean onTrackballEvent (final MotionEvent event) {
 		int action = event.getAction();
 		if (action == MotionEvent.ACTION_MOVE) { 
 			//Event returns RELATIVE x,y
 			mRenderer.addCommand(new Command(Command.Type.EVENT_TRACKBALL, event.getX(), event.getY()));
 		} else if (action == MotionEvent.ACTION_DOWN) { 
 			mRenderer.addCommand(new Command(Command.Type.EVENT_TRACKBALL_CLICK, event.getX(), event.getY()));
 		}
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
 	    appInfo = packMgmr.getApplicationInfo(app.getPackageName(), 0);
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
     nativeInitGL(app.getLevel(), app.getDifficulty(), app.getInputMethod());
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
 			nativeInitGL(app.getLevel(), app.getDifficulty(), app.getInputMethod());
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
 					case EVENT_SECONDARY_DOWN:
 						touchEventSecondaryDown(c.x, c.y);
 						break;
 					case EVENT_SECONDARY_MOVE:
 						touchEventSecondaryMove(c.x, c.y);
 						break;
 					case EVENT_SECONDARY_UP:
 						touchEventSecondaryUp(c.x, c.y);
 						break;
 					case EVENT_PAUSE:
 						nativePause();
 						break;
 					case EVENT_MENU:
 						nativeMenu();
 						break;
 					case EVENT_TRACKBALL:
 						trackballMove(c.x, c.y);
 						break;
 					case EVENT_TRACKBALL_CLICK:
 						trackballClick(c.x, c.y);
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
 
   private static native void nativeInitGL(int level, int difficulty, int inputMethod);
 	private static native void nativeInit(String apkPath, ZoobRenderer app);
 	private static native void nativeResize(int w, int h);
 	private static native void nativeRender();
 	
 	public static native void touchEventDown (float x, float y);
 	public static native void touchEventMove (float x, float y);
 	public static native void touchEventUp (float x, float y);
 	public static native void touchEventOther (float x, float y);
 	private static native void trackballMove (float rx, float ry);
 	private static native void trackballClick (float rx, float ry);
 	
 	//multitouch
 	public static native void touchEventSecondaryDown (float x, float y);
 	public static native void touchEventSecondaryUp (float x, float y);
 	public static native void touchEventSecondaryMove (float x, float y);
 	
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
 	
 	public void saveInputMethod (int method) {
 		app.saveInputMethod(method);
 	}
 	
 	public void buyFullVersion () {
 		Uri fullVersionURI = Uri.parse("market://details?id=net.fhtagn.zoobgame");
 		context.startActivity(new Intent(Intent.ACTION_VIEW, fullVersionURI));
 	}
 	
 }
