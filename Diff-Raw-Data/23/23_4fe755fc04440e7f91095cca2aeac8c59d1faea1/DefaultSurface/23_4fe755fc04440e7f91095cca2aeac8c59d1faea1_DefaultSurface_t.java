 package yang.graphics.defaults;
 
 import yang.events.Keys;
 import yang.events.eventtypes.SurfacePointerEvent;
 import yang.events.eventtypes.YangSensorEvent;
 import yang.events.listeners.YangEventListener;
 import yang.graphics.font.BitmapFont;
 import yang.math.objects.YangMatrix;
 import yang.surface.YangSurface;
 import yang.systemdependent.YangSensor;
 
 public abstract class DefaultSurface extends YangSurface implements YangEventListener{
 
 	public static boolean INIT_DEFAULT_META_KEYS = true;
 	public boolean mExitOnEsc = true;
 
 	public Default2DGraphics mGraphics2D;
 	public Default3DGraphics mGraphics3D;
 	private final boolean mInit2DGraphics;
 	private final boolean mInit3DGraphics;
 	public boolean mAutoApplySensorToCamera = true;
 
 	protected DefaultSurface(boolean init2DGraphics,boolean init3DGraphics) {
 		super();
 		mInit2DGraphics = init2DGraphics;
 		mInit3DGraphics = init3DGraphics;
 		mEventListener = this;
 
 	}
 
 	@Override
 	protected final void initGraphics() {
 		if(mInit2DGraphics) {
 			mGraphics2D = new Default2DGraphics(mGraphics);
 			mGraphics2D.init();
 			if(!mInit3DGraphics) {
 				mGraphics2D.activate();
 				mGraphics2D.setDefaultProgram();
 			}
 		}
 		if(mInit3DGraphics) {
 			mGraphics3D = new Default3DGraphics(mGraphics);
 			if(mInit2DGraphics)
 				mGraphics3D.shareBuffers(mGraphics2D);
 			mGraphics3D.init();
 			if(!mInit2DGraphics) {
 				mGraphics3D.activate();
 				mGraphics3D.setDefaultProgram();
 			}
 		}
		if(INIT_DEFAULT_META_KEYS && mMetaEventListener==null)
 			mMetaEventListener = new DefaultMetaEventListener(this,mGraphics.getDefaultMetaBaseKey());
 	}
 
 	protected void initDebugOutput(BitmapFont font) {
 		initDebugOutput(mGraphics2D==null?mGraphics3D:mGraphics2D,font);
 	}
 
 	@Override
 	public void pointerDown(float x, float y, SurfacePointerEvent event) {
 
 	}
 
 	@Override
 	public void pointerDragged(float x, float y, SurfacePointerEvent event) {
 
 	}
 
 	@Override
 	public void pointerMoved(float x, float y, SurfacePointerEvent event) {
 
 	}
 
 	@Override
 	public void pointerUp(float x, float y, SurfacePointerEvent event) {
 
 	}
 
 	@Override
 	public void keyDown(int code) {
 
 	}
 
 	@Override
 	public void keyUp(int code) {
 		if(mExitOnEsc && code==Keys.ESC)
 			exit();
 	}
 
 	@Override
 	public void zoom(float factor) {
 
 	}
 
 	@Override
 	public void sensorChanged(YangSensorEvent event) {
 
 //		if(event.mType==YangSensor.TYPE_GYROSCOPE) {
 //			mGraphics.mStereoCameraMatrixEnabled = true;
 //			final YangMatrix mat = mGraphics.mStereoCameraMatrix;
 //			mat.rotateZ(-event.mZ);
 //			mat.rotateY(event.mY);
 //			mat.rotateX(-event.mX);
 //		}
 		final YangMatrix uMat = mGraphics.mSensorOrientationMatrix;
 
 		if(event.mType==YangSensor.TYPE_ROTATION_VECTOR) {
 			uMat.setFromQuaternion(event.mX,event.mY,event.mZ,event.mW);
 		}else if(event.mType==YangSensor.TYPE_EULER_ANGLES) {
 			uMat.setFromEulerAngles(event.mX,event.mY,event.mZ);
 		}else
 			return;
 		if(mAutoApplySensorToCamera) {
 			mGraphics.setDebugPostCameraEnabled(true);
 //			mGraphics.mSensorOrientationMatrix.asInverted(mGraphics.mSensorCameraMatrix.mValues);
 			mGraphics.setDebugPostCameraOrientation(uMat);
 		}
 
 	}
 
 }
