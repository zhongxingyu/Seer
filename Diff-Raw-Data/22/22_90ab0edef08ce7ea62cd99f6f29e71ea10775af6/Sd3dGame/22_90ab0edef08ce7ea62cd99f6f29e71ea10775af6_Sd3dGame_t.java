 package org.nicolasmy.sd3d;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL11;
 
 import org.nicolasmy.sd3d.Sd3dGLSurfaceView;
 import org.nicolasmy.sd3d.gfx.Sd3dLight;
 import org.nicolasmy.sd3d.gfx.Sd3dObject;
 import org.nicolasmy.sd3d.gfx.Sd3dRessourceManager;
 import org.nicolasmy.sd3d.gfx.Sd3dScene;
 import org.nicolasmy.sd3d.gfx.entity.Sd3dGameCameraEntity;
 import org.nicolasmy.sd3d.gfx.entity.Sd3dGameEntity;
 import org.nicolasmy.sd3d.gfx.entity.Sd3dGameMobileEntity;
 import org.nicolasmy.sd3d.gfx.renderer.Sd3dRenderer;
 import org.nicolasmy.sd3d.gfx.renderer.Sd3dRendererGl20;
 import org.nicolasmy.sd3d.gfx.renderer.Sd3dRendererInterface;
 import org.nicolasmy.sd3d.interfaces.Sd3dCollisionAgainstInterface;
 import org.nicolasmy.sd3d.interfaces.Sd3dFrameProcessorInterface;
 import org.nicolasmy.sd3d.interfaces.Sd3dTouchListenerInterface;
 import org.nicolasmy.sd3d.utils.Sd3dLogger;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.pm.ConfigurationInfo;
 import android.graphics.Rect;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 
 public class Sd3dGame
 {	
 	public enum Sd3dInputEventType{ACCELEROMETER,TOUCH,KEYBOARD};
 	public boolean invalidateRenderElements;
 	private ArrayList<Sd3dFrameProcessorInterface> mArray = new ArrayList<Sd3dFrameProcessorInterface>();
 	
 	private Sd3dObject pickAt(int x,int y) {
 		return mRenderer.pickAt(x, y, mScene);
 	}
 	
 	public class Sd3dInputEvent
 	{
 		public boolean isActive;
 		public Sd3dInputEventType mType;
 		public int key;
 		public SensorEvent mEvent;
 		public MotionEvent mTouchEvent;
 		public int deviceRotation;
 	}
 	
 	public void addFrameProcessor(Sd3dFrameProcessorInterface processor)
 	{
 		mArray.add(processor);
 	}
 	
 	public Sd3dFrameProcessorInterface getFrameProcessor(int i)
 	{
 		return mArray.get(i);
 	}
 	
 	public class Sd3dGameEntityManager
 	{
 		public ArrayList<Sd3dGameEntity> mEntityList = new ArrayList<Sd3dGameEntity>();
 		//long mLastFrameTime = 0;
 		//long mElapsedTime = 0;
 		
 		public Sd3dGameEntity getEntityByObject(Sd3dObject object)
 		{
 			Sd3dGameEntity entity = null;
 			
 			for (int i = 0;i < mEntityList.size();i++)
 			{
 				if (mEntityList.get(i).mObject == object)
 				{
 					return mEntityList.get(i);
 				}
 			}
 			
 			return entity;
 		}		
 		
 		public void addEntity(Sd3dGameEntity entity)
 		{
			synchronized(this.mEntityList) {
				mEntityList.add(entity);	
			}
 		}
 		
 		public void reset()
 		{
 			synchronized(this.mEntityList) {
 				mEntityList.clear();
 			}
 		}
 		
 		public void processFrame(long dt,int currentTime,Sd3dScene scene,Sd3dInputEvent keyboardEvent,Sd3dGame game)
 		{	
 			synchronized(this.mEntityList) {
 			for (int i=0;i < this.mEntityList.size();i++)
 			{
 				if (mEntityList.get(i) != null)
 					if (mEntityList.get(i).isMarkForDeletion)
 					{
 						mEntityList.get(i).release(game);
 						mEntityList.set(i,null);
 					}
 					else
 					if(mEntityList.get(i).isActive)
 					{
 					if (mEntityList.get(i).hasOnProcessFrame)
 					{
 						mEntityList.get(i).onProcessFrame((int)mElapsedTime);
 					}
 					
 					if (mEntityList.get(i).isCamera)
 					{
 						scene.setCamera(mEntityList.get(i).getPosition(),mEntityList.get(i).getOrientation(),((Sd3dGameCameraEntity)mEntityList.get(i)).rotationMatrix);
 					}
 					
 					if (mEntityList.get(i).hasObject)
 					{
 						if (invalidateRenderElements)
 						{
 							for (int j = 0; j< mEntityList.get(i).mObject.mMesh.length;j++) {
 								mEntityList.get(i).mObject.mMesh[j].mRendererElementInterface = null;
 							}
 							mEntityList.get(i).mObject.mRenderElement = null;
 							for (int l = 0;l < mEntityList.get(i).mObject.mMaterial.length;l++) {
 							  mEntityList.get(i).mObject.mMaterial[l].mColorName = 0;
 							  mEntityList.get(i).mObject.mMaterial[l].mTextureName[0] = 0;
 							  mEntityList.get(i).mObject.mMaterial[l].mTextureName[1] = 0;
 							}
 							if (mEntityList.get(i).mObject.mIsPickable)
 							{
 								mEntityList.get(i).mObject.pickedMaterial.mColorName = 0;
 								mEntityList.get(i).mObject.prepickedMaterial.mColorName = 0;
 								mEntityList.get(i).mObject.unpickedMaterial.mColorName = 0;
 							}
 						}
 						scene.addObject(mEntityList.get(i).mObject);
 					}
 					
 					if (mEntityList.get(i).hasProcessTimer)
 					{
 						if (mEntityList.get(i).mLastTimer + mEntityList.get(i).mNextTimer > currentTime)
 						{
 							mEntityList.get(i).onProcessTimer();
 							mEntityList.get(i).mLastTimer = currentTime;
 						}
 					}
 
 					
 					if (mAccelEvent.isActive)
 					{
 						if (mEntityList.get(i).hasOnAccelerometerEvent)
 						{
 							if (mEntityList.get(i).isReceiveInput())
 							{
 								
 								
 								mEntityList.get(i).onAccelerometerEvent(mAccelEvent);
 							}
 						}						
 					}
 //					if (mTouchEvent.isActive)
 //					{
 //						if (mEntityList.get(i).hasOnTouchEvent)
 //						{
 //							if (mEntityList.get(i).isReceiveInput())
 //							{							
 //								mEntityList.get(i).onTouchEvent();
 //							}
 //						}						
 //					}					
 					
 					if (keyboardEvent != null)
 					{
 						if (mEntityList.get(i).hasOnKeyboardEvent)
 						{
 							mEntityList.get(i).onKeyboardEvent(keyboardEvent);
 						}						
 					}
 					
 					if (mEntityList.get(i) instanceof Sd3dCollisionAgainstInterface)
 					{
 						Sd3dCollisionAgainstInterface collisioner = (Sd3dCollisionAgainstInterface)mEntityList.get(i);
 						for (int j=0;j < mEntityList.size();j++)
 						{
 							if ((mEntityList.get(j) != null)&&(mEntityList.get(j).isActive))
 								if (mEntityList.get(j) instanceof Sd3dGameMobileEntity)
 									collisioner.collideAgainst((Sd3dGameMobileEntity)mEntityList.get(j));
 						}
 					}
 					
 					if (mEntityList.get(i) instanceof Sd3dLight)
 					{
 						scene.addLight((Sd3dLight)mEntityList.get(i));
 					}
 				}
 
 			}
 			
 			mTouchEvent.isActive = false;			
 			mAccelEvent.isActive = false;
 			
 			if (invalidateRenderElements == true)
 			{
 				game.mRenderer.invalidateRendererElements();
 				invalidateRenderElements = false;
 			}
 		}
 			
 			Sd3dLogger.render();
 		}
 
 	}	
 	
 	
 	public void reset() {
 		this.mGameEntityManager.reset();
 	}
 	
 	Sd3dInputEvent mKeyboardEvent;
 	Sd3dInputEvent mAccelEvent = new Sd3dInputEvent();
 	Sd3dInputEvent mTouchEvent = new Sd3dInputEvent();
 	private Sd3dScene mScene;
 	public Sd3dRendererInterface mRenderer;
 	private Sd3dGameEntityManager mGameEntityManager;
 	private Sd3dGLSurfaceView mGLSurfaceView; 
 
 	int mMaxScene = 5000;
 	int nMaxEntity = 5000;
 	public void updateRenderer(Activity activity)
 	{
         DisplayMetrics dm = new DisplayMetrics();
         activity.getWindowManager().getDefaultDisplay().getMetrics(dm);	
         mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
 		this.mRenderer.updateScreenResolution(dm.widthPixels,dm.heightPixels);
 	}
 	
 	public void init(SurfaceHolder holder)
 	{
 		Log.d("Sd3dGame","init from holder");	
 		Rect rect = holder.getSurfaceFrame();
 		this.mRenderer = new Sd3dRendererGl20(true,mMaxScene,rect.width(),rect.height());
 		this.mScene = new Sd3dScene(mMaxScene);
 		this.mGameEntityManager = new Sd3dGameEntityManager();
 	}
 	
 	
 	public void init(Activity activity)
 	{
 		Log.d("Sd3dGame","init()");
         DisplayMetrics dm = new DisplayMetrics();
         activity.getWindowManager().getDefaultDisplay().getMetrics(dm);		
      
         activity.getWindowManager().getDefaultDisplay().getMetrics(dm);	
         mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
 		
 		this.mScene = new Sd3dScene(mMaxScene);
 		//this.mRenderer = new Sd3dRenderer(true,mMaxScene,dm.widthPixels,dm.heightPixels);
 		
 		final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
 	    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
 	    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
 		if ((supportsEs2))
 		{
 			Log.d("Sd3dGame","Using OpenGL ES 2.0 renderer");
 			this.mRenderer = new Sd3dRendererGl20(true,mMaxScene,dm.widthPixels,dm.heightPixels);
 		}
 		else
 		{
 			Log.d("Sd3dGame","Using OpenGL ES 1.1 renderer");
 			this.mRenderer = new Sd3dRenderer(true,mMaxScene,dm.widthPixels,dm.heightPixels);
 		}
 		
 		this.mGameEntityManager = new Sd3dGameEntityManager();
 
 	}
 	
 	public long lasttick = 0;
 	public int count = 0;
 	public int calculatedFps = 0;
 	
 	//public boolean mHasPick = false;
 	//public boolean mHasPrePick = false;
 	public int mPickCount = 0;
 
 	public int mDeviceRotation;
 	private boolean mShowFps = false;
 	
 	private boolean mHandlePickRequest = true;
 	private long mLastFrameTime = 0;
 	private long mElapsedTime = 0;	
 	
 	/**
 	 * 
 	 */
 	private boolean mHasPickRequest = false;
 	private MotionEvent mPickRequestEvent;

 	public boolean isShowFps() {
 		return mShowFps;
 	}
 
 	public void setShowFps(boolean showFps) {
 		this.mShowFps = showFps;
 	}
 
 	public void processFrame(GL11 gl)
 	{
 		int currentTime = 0;
 		long lcurrenT = 0;
 		long elapsedTime = 0;
 		do {
 		if (mLastFrameTime == 0)
 			mLastFrameTime = java.lang.System.currentTimeMillis();
 		lcurrenT = java.lang.System.currentTimeMillis();
 		elapsedTime = lcurrenT - mLastFrameTime;
 		} while (elapsedTime < 16);
 		mElapsedTime = elapsedTime;
 		mLastFrameTime = lcurrenT;		
 		
 		mRenderer.setGL11Context(gl);
 		mScene.initScene();
 		
 		mGameEntityManager.processFrame(mElapsedTime,currentTime,mScene,mKeyboardEvent,this);
 	
 		if (lasttick == 0)
 			lasttick = java.lang.System.currentTimeMillis();		
 		else
 		{
 			if (count == 10)
 			{
 									
 				lasttick = java.lang.System.currentTimeMillis() - lasttick;
 				float fps = 10000.f/(float)lasttick;
 				calculatedFps = (int)fps;			
 				lasttick = java.lang.System.currentTimeMillis();
 				count = 0;
 			}
 		}
 	
 		if (mShowFps)
 			mRenderer.displayText(""+calculatedFps, Sd3dRenderer.ALIGN.RIGHT, Sd3dRenderer.ALIGN.BOTTOM, 20.f);
 		
 		count++;
 		for(Sd3dFrameProcessorInterface processor : this.mArray)
 		{
 			processor.processFrame(mElapsedTime);
 		}
 
 		if (mHasPickRequest){
 			
 			Sd3dObject obj = pickAt((int)mPickRequestEvent.getX(), (int)mPickRequestEvent.getY());
 			if (obj == null) {
 				Log.d("MineBoard","Object not found");
 			}			
 			Sd3dGameEntity entity = this.mGameEntityManager.getEntityByObject(obj);
 			if ((entity != null)&&(entity.getOnTouchListener() != null))
 				entity.getOnTouchListener().onTouch(mPickRequestEvent);
 			
 			if (entity == null) {
 				Log.d("MineBoard","Entity not found");
 			}
 			mHasPickRequest = false;
 		}
 
 		mRenderer.renderScene(mScene);	
 		mKeyboardEvent = null;
 	}
 
 	public Sd3dGLSurfaceView getMGLSurfaceView() {
 		return mGLSurfaceView;
 	}
 
 	public void setMGLSurfaceView(Sd3dGLSurfaceView surfaceView) {
 		mGLSurfaceView = surfaceView;
 	}
 	
     public void onPause() {
     	mGLSurfaceView.onPause();
     }
 
     /**
      * Inform the view that the activity is resumed.
      */
 
     public void onResume() {
     	mGLSurfaceView.onResume();
     }
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onSensorChanged(SensorEvent event) {
     	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
     	{
 			mAccelEvent.mEvent = event;
 			mAccelEvent.mType = Sd3dInputEventType.ACCELEROMETER;
 			mAccelEvent.isActive = true;
 			mAccelEvent.deviceRotation = this.mDeviceRotation;
     	}		
 	}	
 	
 	public void addEntity(Sd3dGameEntity entity)
 	{
 		this.mGameEntityManager.addEntity(entity);
 	}	
 	
 	public List<Sd3dGameEntity> getEntityList(){
 		return this.mGameEntityManager.mEntityList;
 	}
 	
 	private Sd3dTouchListenerInterface mTouchListener = null;
 	
 	public void registerTouchListener(Sd3dTouchListenerInterface listener){
 		mTouchListener = listener;
 	}
 		
 	public void onTouch(MotionEvent event)
 	{	
 		//Log.d("onTouch(MotionEvent event)","touch");
 		if (mTouchListener != null){
 			mTouchListener.onTouch(event);
 		}
 		
 		if ((mHandlePickRequest && mHasPickRequest) == false){
 			mPickRequestEvent = event;
 			mHasPickRequest = true;
 		}
 		
 		if (!mTouchEvent.isActive)
 		{
 			mTouchEvent.isActive = true;
 			mTouchEvent.mTouchEvent = event;
 			mTouchEvent.mType = Sd3dInputEventType.TOUCH;
 		}		
 	}
 	
 
 	
 	public void onKeyUp()
 	{
 		
 		if (mKeyboardEvent == null)
 		{		
 			
 			mKeyboardEvent = new Sd3dInputEvent();
 			mKeyboardEvent.key = 1;
 			mKeyboardEvent.mType = Sd3dInputEventType.KEYBOARD;		
 		}
 		
 		//this.mGuiManager.onEvent("UP");
 	}
 	
 	public void onKeyDown()
 	{
 		
 		if (mKeyboardEvent == null)
 		{		
 			
 			mKeyboardEvent = new Sd3dInputEvent();
 			mKeyboardEvent.key = 3;
 			mKeyboardEvent.mType = Sd3dInputEventType.KEYBOARD;		
 		}
 	}	
 	
 	public void onKeyRight()
 	{
 		if (mKeyboardEvent == null)
 		{		
 			mKeyboardEvent = new Sd3dInputEvent();
 			mKeyboardEvent.key = 2;
 			mKeyboardEvent.mType = Sd3dInputEventType.KEYBOARD;	
 		}
 	}
 	
 	public void onKeyLeft()
 	{
 		if (mKeyboardEvent == null)
 		{
 			mKeyboardEvent = new Sd3dInputEvent();
 			mKeyboardEvent.key = 4;
 			mKeyboardEvent.mType = Sd3dInputEventType.KEYBOARD;
 		}
 	}	
 	
 	public Sd3dFrameProcessorInterface getFrameProcessor(String frameProcessorClassName) {
 		for (Sd3dFrameProcessorInterface fp : mArray) {
 			if (fp.getClass().toString().contains(frameProcessorClassName)) {
 				return fp;
 			}
 		
 		}
 		return null;
 	}
 	
 	
 }
 
 
 
