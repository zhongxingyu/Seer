 package net.fhtagn.zoobgame;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.net.Uri;
 import android.opengl.GLSurfaceView;
 import android.util.Log;
 import android.view.MotionEvent;
 
 class ZoobGLSurface extends GLSurfaceView {
 	ZoobRenderer mRenderer;
 	private static final String TAG = "ZoobGLSurface";
 	private static MotionEventHandler motionHandler = null;
 	
 	private final Zoob zoob;
 	
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
 	
 	public ZoobGLSurface(Zoob context, ZoobApplication app, String levelsSerie) {
 		super(context);
 		zoob = context;
 		mRenderer = new ZoobRenderer(context, app, levelsSerie, this);
 		setRenderer(mRenderer);
 		setFocusableInTouchMode(true); //necessary to get trackball events
 	}
 	
 	//Will change the level the next time the renderer is either resumed or recreated
 	public void setLevel (int level) {
 		mRenderer.setLevel(level);
 	}
 	
 	@Override
 	public void onResume () {
 		super.onResume();
 		Log.i(TAG, "onResume");
 		mRenderer.triggerRestoreGL();
 	}
 	
 	public void onMenu () {
 		mRenderer.addCommand(new Command(Command.Type.EVENT_MENU));
 	}
 	
 	@Override
 	public void onPause () {
 		Log.i(TAG, "onPause");
 		mRenderer.addCommand(new Command(Command.Type.EVENT_PAUSE));
 		super.onPause();
 	}
 
 	@Override
 	public boolean onTouchEvent(final MotionEvent event) {
 		Command c = motionHandler.processEvent(event);
 		if (c != null)
 			mRenderer.addCommand(c);
 
 		//This is an advice from "Writing real time games for android" Google I/O presentation
 		//This avoids event flood when the screen is touched
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
 	static final String TAG = "ZoobRenderer";
 	private Zoob context;
 	private ZoobApplication app;
 	private String apkFilePath;
 	
 	private List<Command> commands = new ArrayList<Command>();
 	
 	private boolean initialized = false; //avoid multiple calls to nativeInit
 	
 	private final String levelsSerie;
 	
 	private final ZoobGLSurface glSurface;
 	
 	private boolean restoreGL = false; //notify this renderer that it should restore opengl context (the app was resumed from sleep)
 	
 	private int nextLevel = -1; //if set to something different than -1, indicate the next lvl to start (in the next onDrawFrame)
 
 	//This constructor is not ran in the rendering thread (but in the surface thread)
 	public ZoobRenderer (Zoob context, ZoobApplication app, String levelsSerie, ZoobGLSurface surface) {
 		this.glSurface = surface;
 		this.context = context;
 		this.levelsSerie = levelsSerie;
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
 		Log.i(TAG, "onSurfaceCreated");
 		//Call ALL nativeInit methods here, because we want the JNIEnv of the rendering thread
 		//(see comments in app-android.cpp)
 		if (!initialized) {
 			Log.i(TAG, "calling nativeInit");
 			nativeInit(apkFilePath, this, levelsSerie);
 			initialized = true;
 			//UGLY hack. In Zoob.java, we set a background on this view to avoid black screen.
 			//When this view is initialized, we have to REMOVE the opaque background otherwise, the opengl drawing
 			//won't be shown (for some strange reason).
 			context.runOnUiThread(new Runnable() {
 				@Override
         public void run() {
 					glSurface.setBackgroundColor(Color.parseColor("#00000000"));
         }
 			});
 		}
 		Log.i(TAG, "calling nativeInitGL");
     nativeInitGL(app.getLevel(), app.getDifficulty(), app.usesGamepad()?1:0, app.usesTrackball()?1:0);
     nativeStartGame(0);
 	}
 
 	public void onSurfaceChanged(GL10 gl, int w, int h) {
 		Log.i(TAG, "onSurfaceChanged");
 		nativeResize(w, h);
 	}
 	
 	public void triggerRestoreGL () {
 		restoreGL = true;
 	}
 	
 	public void setLevel (int level) {
 		nextLevel = level;
 		triggerRestoreGL(); //some stuff (such as preferences) might have changed
 	}
 
 	public void onDrawFrame(GL10 gl) {
 		if (restoreGL) {
 			Log.i(TAG, "restoreGL");
 			nativeInitGL(app.getLevel(), app.getDifficulty(), app.usesGamepad()?1:0, app.usesTrackball()?1:0);
 			restoreGL = false;
 		}
 		
 		if (nextLevel != -1) {
 			Log.i(TAG, "nextLevel = " + nextLevel);
 			nativeStartGame(nextLevel);
 			nextLevel = -1;
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
 						context.showMenu(Zoob.MENU_MAIN, -1);
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
 
   private static native void nativeInitGL(int level, int difficulty, int useGamepad, int useTrackball);
 	private static native void nativeInit(String apkPath, ZoobRenderer app, String serieJSON);
 	
 	private static native void nativeStartGame(int level);
 	
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
 	
 	//These are stubs for upcall from JNI because the Application object isn't in the 
 	//same thread as the JNI stuff (nativeRender) and this can lead to random crashes
 	public void saveProgress (int level) {
 		app.saveProgress(level);
 	}
 		
 	public void saveDifficulty (int level) {
 		app.saveDifficulty(level);
 	}
 
 	public void showMenu (int id, int currentLevel) {
 		context.showMenu(id, currentLevel);
 	}
 }
