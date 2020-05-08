 package yang.android.graphics;
 
 import yang.android.AndroidVibrator;
 import yang.android.io.AndroidDataStorage;
 import yang.android.io.AndroidSystemCalls;
 import yang.android.sound.AndroidSoundManager;
 import yang.events.EventQueueHolder;
 import yang.events.Keys;
 import yang.events.YangEventQueue;
 import yang.events.eventtypes.YangKeyEvent;
 import yang.events.eventtypes.YangPointerEvent;
 import yang.model.App;
 import yang.model.DebugYang;
 import yang.surface.YangSurface;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.opengl.GLSurfaceView;
 import android.os.Build;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class YangTouchSurface extends GLSurfaceView {
 
 	public YangEventQueue mEventQueue;
 	public YangSceneRenderer mSceneRenderer;
 	public Context mContext;
 	public YangActivity mActivity;
 	public static boolean keepScreenOn = true;
 
 	public YangTouchSurface(YangActivity activity) {
 		super(activity);
 		mActivity = activity;
 		mContext = activity;
 		mEventQueue = null;
 		initGL(mContext);
 		setKeepScreenOn(keepScreenOn);
 	}
 
 	@TargetApi(11)
 	protected void initGL(Context context) {
 		super.setEGLContextClientVersion(2);
 		super.setEGLConfigChooser(8,8,8,8, 16,0);
 		//HTC working: RGBA_8888
 		super.getHolder().setFormat(PixelFormat.RGBA_8888);
 		if (Build.VERSION.SDK_INT>=11 && !AndroidSystemCalls.ALWAYS_RELOAD_AFTER_PAUSE)
 			super.setPreserveEGLContextOnPause(true);
 		DebugYang.println("INITIALIZE OPENGL");
 
 		mSceneRenderer = new YangSceneRenderer(context);
 		super.setRenderer(mSceneRenderer);
 
 		if(App.soundManager==null) {
 			App.soundManager = new AndroidSoundManager(context);
 			App.storage = new AndroidDataStorage(context);
 			App.gfxLoader = mSceneRenderer.mGraphicsTranslator.mGFXLoader;
 			App.resourceManager = App.gfxLoader.mResources;
 			App.vibrator = new AndroidVibrator(context);
 			App.systemCalls = new AndroidSystemCalls(mActivity);
 		}else{
 			DebugYang.println("App references already set");
 		}
 	}
 
 	public void setSurface(YangSurface surface) {
 		surface.mPlatformKey = "ANDROID";
 
 		mSceneRenderer.setSurface(surface);
 
 		if (surface instanceof EventQueueHolder)
 			setEventListener(surface);
 	}
 
 	public void setEventListener(EventQueueHolder eventQueue) {
 		mEventQueue = eventQueue.getEventQueue();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(mEventQueue==null)
 			return false;
 		final int action = event.getActionMasked();
 		final int idx = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
 		final int id = event.getPointerId(idx);
 
 		final int x = (int)event.getX(idx);
 		final int y = (int)event.getY(idx);
 
 		switch (action){
 		case MotionEvent.ACTION_DOWN:
 		case MotionEvent.ACTION_POINTER_DOWN:
 			mEventQueue.putSurfacePointerEvent(YangPointerEvent.ACTION_POINTERDOWN, x, y, YangPointerEvent.BUTTON_LEFT, id);
 			break;
 
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			mEventQueue.putSurfacePointerEvent(YangPointerEvent.ACTION_POINTERUP, x, y, YangPointerEvent.BUTTON_LEFT, id);
 			break;
 
 		case MotionEvent.ACTION_MOVE:
 
 			for(int i = 0; i < event.getPointerCount(); i++){
 				mEventQueue.putSurfacePointerEvent(YangPointerEvent.ACTION_POINTERDRAG, (int)event.getX(i), (int)event.getY(i), YangPointerEvent.BUTTON_LEFT, event.getPointerId(i));
 			}
 
 //			mEventQueue.putPointerEvent(AbstractPointerEvent.ACTION_POINTERDRAG, x, y, AbstractPointerEvent.BUTTON_LEFT, id);
 			break;
 		}
 
 		return true;
 	}
 
 	public void onBackPressed() {
 		if (mEventQueue!=null) {
 			mEventQueue.putKeyEvent(Keys.ESC, YangKeyEvent.ACTION_KEYDOWN);
 			mEventQueue.putKeyEvent(Keys.ESC, YangKeyEvent.ACTION_KEYUP);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		mSceneRenderer.mSurface.pause();
 	}
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mSceneRenderer.mSurface.resume();
 	}
 
 	public View getView() {
 		return this;
 	}
 }
