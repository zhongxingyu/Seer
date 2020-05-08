 package yang.surface;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import yang.events.EventQueueHolder;
 import yang.events.YangEventQueue;
 import yang.events.eventtypes.YangEvent;
 import yang.events.listeners.RawEventListener;
 import yang.events.listeners.YangEventListener;
 import yang.events.macro.AbstractMacroIO;
 import yang.events.macro.DefaultMacroIO;
 import yang.events.macro.MacroExecuter;
 import yang.events.macro.MacroWriter;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.font.BitmapFont;
 import yang.graphics.interfaces.Clock;
 import yang.graphics.interfaces.InitializationCallback;
 import yang.graphics.interfaces.ScreenshotCallback;
 import yang.graphics.model.GFXDebug;
 import yang.graphics.stereovision.StereoRendering;
 import yang.graphics.stereovision.StereoVision;
 import yang.graphics.translator.AbstractGFXLoader;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.math.MathConst;
 import yang.model.App;
 import yang.model.DebugYang;
 import yang.model.enums.ByteFormat;
 import yang.model.enums.UpdateMode;
 import yang.sound.AbstractSoundManager;
 import yang.sound.nosound.NoSoundManager;
 import yang.systemdependent.AbstractResourceManager;
 import yang.systemdependent.AbstractVibrator;
 import yang.systemdependent.NoSensor;
 import yang.systemdependent.YangSensor;
 import yang.systemdependent.YangSystemCalls;
 import yang.util.ImageCaptureData;
 import yang.util.Util;
 
 public abstract class YangSurface implements EventQueueHolder,RawEventListener,Clock {
 
 	public static float PI = MathConst.PI;
 
 	public static boolean CATCH_EXCEPTIONS = false;
 	public static boolean NO_MACRO_OVERWRITE = false;
 	public static int ALWAYS_STEREO_VISION = 0;
 	public static boolean SHOW_MACRO_SIGN = true;
 
 	public final static int RUNTIME_STATE_RUNNING = 0;
 	public final static int RUNTIME_STATE_PAUSED = 1;
 	public final static int RUNTIME_STATE_STOPPED = 2;
 
 	public GraphicsTranslator mGraphics;
 	public StringsXML mStrings;
 	public AbstractResourceManager mResources;
 	public AbstractGFXLoader mGFXLoader;
 	public AbstractSoundManager mSounds;
 	public AbstractVibrator mVibrator;
 	public YangSensor mSensor;
 	public YangSystemCalls mSystemCalls;
 	public GFXDebug mGFXDebug;
 	public String mPlatformKey = "";
 	public float mScreenShotLeft = 0;
 	public float mScreenShotTop = 0;
 	public float mScreenShotWidth = 1;
 	public float mScreenShotHeight = 1;
 
 	public int mMaxStepsPerCycle = 100;
 
 	private UpdateMode mUpdateMode;
 	protected boolean mInitialized = false;
 	protected Object mInitializedNotifier;
 	protected InitializationCallback mInitCallback;
 
 	//Time
 	protected long mCatchUpTime;
 	public double mProgramTime;
 	public int mStepCount;
 	public static float deltaTimeSeconds;
 	protected static long deltaTimeNanos;
 	protected int mUpdateWaitMillis = 1000/70;
 	protected int mRuntimeState = 0;
 	protected boolean mInactive = false;
 
 	//Properties
 	public boolean mForceMetaMode = false;
 	public float mPlaySpeed = 1;
 	public float mFastForwardToTime = -1;
 	public boolean mFreezeAfterFastForward = false;
 	public String mMacroFilename;
 	private int mStereoResolution = 1024;
 	private boolean mForceStereoVision = false;
 
 	//State
 	public boolean mPaused = false;
 	private boolean mResuming = false;
 	private boolean mLoadedOnce = false;
 	private int mActiveEye = StereoVision.EYE_MONO;
 	public boolean mException = false;
	private int mStartupSteps = 2;
 	private int mLoadingSteps = 1;
 	private int mLoadingState = 0;
 	private ScreenshotCallback mScreenshotCallback = null;
 	private boolean mMakingScreenshot = false;
 
 	//Objects
 	private Thread mUpdateThread = null;
 	public YangEventListener mEventListener;
 	public YangEventListener mMetaEventListener;
 	public YangEventQueue mEventQueue;
 	public int mDebugSwitchKey = -1;
 	public StereoRendering mStereoVision = null;
 	public MacroExecuter mMacro;
 	public DefaultMacroIO mDefaultMacroIO;
 
 	//loading stuff
 	private Thread mLoaderThread;
 	private volatile float mLoadingProgress;
 	private boolean mInterrupted;
 
 	/**
 	 * GL-Thread
 	 */
 	protected abstract void initGraphics();
 	protected abstract void draw();
 
 	protected void onException(Exception ex) {
 
 	}
 
 	//Optional methods
 	protected void postInitGraphics() { }
 	protected void initGraphicsForResume() { }
 	protected void onLoadingInterrupted(boolean resuming) { }
 
 	public YangSurface() {
 		mInitializedNotifier = new Object();
 		mCatchUpTime = 0;
 		mEventQueue = new YangEventQueue(getMaxEventCount());
 		mStrings = new StringsXML();
 		setUpdatesPerSecond(120);
 		mUpdateMode = UpdateMode.SYNCHRONOUS;
 		mMacroFilename = null;
 		setStereoVision(ALWAYS_STEREO_VISION);
 	}
 
 	public void setStereoVision(int resolution) {
 		if(resolution==0) {
 			mForceStereoVision = false;
 		}else{
 			mForceStereoVision = true;
 			mStereoResolution = resolution;
 		}
 		if(mGraphics!=null)
 			this.onSurfaceChanged(mGraphics.mScreenWidth, mGraphics.mScreenHeight);
 
 	}
 
 	public int getActiveEye() {
 		return mActiveEye;
 	}
 
 	protected void setStartupSteps(int loadingSteps,int initSteps) {
 		mLoadingSteps = loadingSteps;
 		mStartupSteps = initSteps+loadingSteps;
 	}
 
 	protected void setStartupSteps(int loadingSteps) {
 		setStartupSteps(loadingSteps,0);
 	}
 
 	protected void exceptionOccurred(Exception ex) {
 		try{
 			mEventQueue.close();
 			if(mMacro!=null)
 				mMacro.close();
 		}catch(final Exception ex2) {
 
 		}
 		onException(ex);
 		if(!CATCH_EXCEPTIONS) {
 			if(ex instanceof RuntimeException)
 				throw (RuntimeException)ex;
 			else
 				throw new RuntimeException(ex);
 		}
 		mException = true;
 		mPaused = true;
 		ex.printStackTrace();
 		if(mGFXDebug!=null)
 			mGFXDebug.setErrorString(ex.getClass()+": "+ex.getMessage()+"\n\n"+Util.arrayToString(ex.getStackTrace(),"\n").replace("(", " ("));
 
 	}
 
 	public void setMacroFilename(String filename) {
 		mMacroFilename = filename;
 	}
 
 	@Override
 	public YangEventQueue getEventQueue() {
 		return mEventQueue;
 	}
 
 	protected int getMaxEventCount() {
 		return 2048;
 	}
 
 	public void setBackend(GraphicsTranslator graphics) {
 		mGraphics = graphics;
 		mGFXLoader = mGraphics.mGFXLoader;
 		mResources = mGraphics.mGFXLoader.mResources;
 		mSounds = App.soundManager==null?new NoSoundManager():App.soundManager;
 		mSounds.init(mResources);
 		mSensor = App.sensor==null?new NoSensor():App.sensor;
 	}
 
 	protected boolean assertMessage() {
 		System.out.println("ASSERTIONS ARE ACTIVATED");
 		return true;
 	}
 
 	public void recordMacro(String filename,AbstractMacroIO macroIO) throws FileNotFoundException {
 		final MacroWriter writer = new MacroWriter(mResources.getSystemOutputStream(filename), mDefaultMacroIO);
 		mEventQueue.registerEventWriter(writer);
 	}
 
 	public void recordMacro(String filename) throws FileNotFoundException {
 		recordMacro(filename,mDefaultMacroIO);
 	}
 
 	public void playMacro(String filename,AbstractMacroIO macroIO) {
 		final InputStream is = mResources.getSystemInputStream(filename);
 		if(is==null)
 			throw new RuntimeException("File not found: "+filename);
 		mMacro = new MacroExecuter(is, macroIO);
 	}
 
 	public void playMacro(String filename) {
 		playMacro(filename,mDefaultMacroIO);
 	}
 
 	public void onSurfaceCreated(boolean mainSurface) {
 		if(mInitialized) {
 			DebugYang.println("ALREADY INITIALIZED");
 			return;
 		}
 		mProgramTime = 0;
 		mStepCount = 0;
 		if(mainSurface) {
 			assert assertMessage();
 			mGraphics.init();
 		}
 
 //		mSounds = App.soundManager;
 
 		if(mainSurface) {
 			mSensor.init(this);
 			mEventQueue.setGraphics(mGraphics);
 		}
 		mSystemCalls = App.systemCalls;
 
 		if(mResources.assetExists("strings/strings.xml"))
 			mStrings.load(mResources.getAssetInputStream("strings/strings.xml"));
 		try{
 			initGraphics();
 
 			mDefaultMacroIO = new DefaultMacroIO(this);
 			if(mainSurface) {
 				if(mMacroFilename!=null) {
 					if(!mResources.fileExistsInFileSystem(mMacroFilename))
 						mMacroFilename += ".ym";
 					if(mResources.fileExistsInFileSystem(mMacroFilename)) {
 						playMacro(mMacroFilename);
 					}
 				}
 				if(mMacro==null && DebugYang.AUTO_RECORD_MACRO) {
 					String filename = "run.ym";
 					if(NO_MACRO_OVERWRITE) {
 						int i=0;
 						while(mResources.fileExistsInFileSystem(filename)) {
 							filename = "run"+i+".ym";
 							i++;
 						}
 					}
 					try {
 						recordMacro(filename);
 					} catch (final FileNotFoundException e) {
 						DebugYang.printerr("Could not create '"+filename+"'");
 					}
 				}
 			}
 
 			mGFXLoader.startEnqueuing();
 			postInitGraphics();
 
 			if(mInitCallback!=null)
 				mInitCallback.initializationFinished();
 			mInitialized = true;
 			synchronized(mInitializedNotifier) {
 				mInitializedNotifier.notifyAll();
 			}
 
 			if(mUpdateMode == UpdateMode.ASYNCHRONOUS)
 				mUpdateThread.start();
 		}catch(final Exception ex) {
 			exceptionOccurred(ex);
 		}
 
 		refreshMetaMode();
 
 		if(mFastForwardToTime>0) {
 			mPlaySpeed = 1/32f;
 		}
 	}
 
 	public void waitUntilInitialized() {
 		synchronized(mInitializedNotifier) {
 			try {
 				mInitializedNotifier.wait();
 			} catch (final InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onSurfaceChanged(int width,int height) {
 		try{
 			mGraphics.setSurfaceSize(width, height);
 			if(mGFXDebug!=null)
 				mGFXDebug.surfaceChanged();
 			if(mStereoVision!=null)
 				mStereoVision.surfaceChanged(mGraphics);
 		}catch(final Exception ex) {
 			exceptionOccurred(ex);
 		}
 	}
 
 	public boolean isInitialized() {
 		return mInitialized;
 	}
 
 	public void setInitializationCallback(InitializationCallback initCallback) {
 		mInitCallback = initCallback;
 	}
 
 	public static void setUpdatesPerSecond(int updatesPerSecond) {
 		deltaTimeSeconds = 1f/updatesPerSecond;
 		deltaTimeNanos = 1000000000/updatesPerSecond;
 	}
 
 	public void setUpdateMode(UpdateMode updateMode) {
 		mUpdateMode = updateMode;
 		if(mUpdateMode==UpdateMode.ASYNCHRONOUS) {
 			mUpdateThread = new Thread() {
 				@Override
 				public void run() {
 
 					while(true) {
 						try {
 							if(mRuntimeState>0) {
 								Thread.sleep(100);
 								continue;
 							}else
 								Thread.sleep(mUpdateWaitMillis);
 						} catch (final InterruptedException e) {
 							e.printStackTrace();
 						}
 						catchUp();
 					}
 				}
 			};
 		}
 	}
 
 	protected void initDebugOutput(DefaultGraphics<?> graphics, BitmapFont font) {
 		mGFXDebug = new GFXDebug(this,graphics,font);
 		mGFXDebug.surfaceChanged();
 	}
 
 	protected boolean isLoadingFinished() {
 		return mLoadingState>=mStartupSteps;
 	}
 
 	protected void handleEvents() {
 		assert mGraphics.preCheck("Handle events");
 		if(mEventListener!=null) {
 			if(!mPaused && mLoadingState>=mStartupSteps)
 				mEventQueue.handleEvents(mEventListener);
 			else
 				mEventQueue.clearEvents();
 		}
 		assert mGraphics.checkErrorInst("Handle events");
 	}
 
 	protected void catchUp() {
 //		if(alt) {
 //		alt = false;
 //		return;
 //	}
 //	alt = true;
 
 		if(!mInitialized || mRuntimeState>0 || mLoadingState<mStartupSteps)
 			return;
 		if(mFastForwardToTime>0 && mFastForwardToTime<mProgramTime) {
 			mPlaySpeed = 1;
 			mFastForwardToTime = -1;
 			if(mFreezeAfterFastForward) {
 				setPaused(true);
 				return;
 			}
 		}
 
 		if(mCatchUpTime==0)
 			mCatchUpTime = System.nanoTime()-1;
 
 		if(mPlaySpeed==0) {
 			handleEvents();
 			mCatchUpTime = 0;
 		}else{
 			int steps = 0;
 			while(mCatchUpTime<System.nanoTime()) {
 				mCatchUpTime += (long)(deltaTimeNanos*mPlaySpeed);
 				proceed();
 				steps++;
 				if(steps>mMaxStepsPerCycle) {
 					mCatchUpTime = 0;
 					System.err.println("Step emergency stop");
 					break;
 				}
 			}
 		}
 	}
 	boolean alt = false;
 
 	public void proceed() {
 
 		if(!mPaused && !mException) {
 			try{
 				if(mMacro!=null) {
 					mMacro.step();
 					if(mMacro.mFinished && mEventQueue.mMetaMode) {
 						refreshMetaMode();
 					}
 				}
 				handleEvents();
 				if(mLoadingState>=mStartupSteps) {
 					mStepCount ++;
 					mProgramTime += deltaTimeSeconds;
 					step(deltaTimeSeconds);
 				}
 			}catch(final Exception ex){
 				exceptionOccurred(ex);
 			}
 		}
 	}
 
 	protected void catchUpTimer() {
 		mCatchUpTime = 0;
 	}
 
 	protected void step(float deltaTime) {
 
 	}
 
 	protected void prepareLoading(boolean resuming) {
 		//if(resuming)
 			mGFXLoader.reenqueueTextures();
 		if(mStartupSteps>0)
 			mGFXLoader.divideQueueLoading(mStartupSteps-1);
 		mGFXLoader.mEnqueueMode = false;
 		startLoadingThread();
 	}
 
 	private void stopLoadingThread() {
 		if (mLoaderThread != null && mLoaderThread.isAlive()) {
 			try {
 				mLoaderThread.join(1000);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void startLoadingThread() {
 		mLoaderThread = new Thread() {
 			@Override
 			public void run() {
 				while (!mGFXLoader.loadingDone() && !mInterrupted) {
 					mLoadingProgress = mGFXLoader.loadEnqueuedTextures();
 				}
 			}
 		};
 		mLoaderThread.start();
 	}
 	protected void initializeAssets(int initStep,boolean resuming) {
 //		System.out.println(mLoadingState+" "+initStep);
 	}
 
 
 	protected void drawLoadingScreen(int loadState,float progress,boolean resuming) {
 
 	}
 
 	protected void onLoadingFinished(boolean resuming) {
 
 	}
 
 	protected void preDraw() {
 
 	}
 
 	public final void drawFrame() {
 
 		mMakingScreenshot = mScreenshotCallback!=null;
 		ImageCaptureData screenShotData = null;
 		if(mMakingScreenshot) {
 			screenShotData = mScreenshotCallback.getScreenshotTarget(mGraphics.mScreenWidth,mGraphics.mScreenHeight,mGraphics.mMinRatioX);
 			mGraphics.setTextureRenderTarget(screenShotData.mRenderTarget);
 		}
 
 		if(mForceStereoVision) {
 			//STEREO VISION
 			if(mStereoVision == null) {
 				mStereoVision = new StereoRendering();
 				mStereoVision.init(mGraphics,mStereoResolution);
 			}
 			mStereoVision.mPostTransform = mGraphics.getViewPostTransform();
 			mStereoVision.refreshTransforms();
 			try{
 				mGraphics.setTextureRenderTarget(mStereoVision.mStereoLeftRenderTarget);
 //				mGraphics.mCameraShiftX = mStereoVision.mInterOcularDistance*AbstractGraphics.METERS_PER_UNIT;
 //				mGraphics.setStereoTransform(mStereoVision.getLeftEyeTransform());
 
 				mActiveEye = StereoVision.EYE_LEFT;
 				drawContent(true);
 				mGraphics.leaveTextureRenderTarget();
 //				mGraphics.mCameraShiftX = -mStereoVision.mInterOcularDistance*AbstractGraphics.METERS_PER_UNIT;
 //				mGraphics.setStereoTransform(mStereoVision.getRightEyeTransform());
 				mGraphics.setTextureRenderTarget(mStereoVision.mStereoRightRenderTarget);
 				mActiveEye = StereoVision.EYE_RIGHT;
 				drawContent(false);
 			}finally{
 				mGraphics.leaveTextureRenderTarget();
 			}
 
 			mStereoVision.draw();
 		}else{
 			mActiveEye = StereoVision.EYE_MONO;
 			drawContent(true);
 		}
 
 		if(mMakingScreenshot) {
 			int surfW = mGraphics.mCurrentSurface.getSurfaceWidth();
 			int surfH = mGraphics.mCurrentSurface.getSurfaceHeight();
 			int l = (int)(mScreenShotLeft*surfW);
 			int t = (int)(mScreenShotTop*surfH);
 			int w = (int)(mScreenShotWidth*surfW);
 			int h = (int)(mScreenShotHeight*surfH);
 			screenShotData.mImage.mWidth = w;
 			screenShotData.mImage.mHeight = h;
 			mGraphics.readPixels(l,t,w,h, 4,ByteFormat.UNSIGNED_BYTE,screenShotData.mImage.mData);
 //			mGraphics.readPixels(screenShotData.mImage.mData,4,ByteFormat.UNSIGNED_BYTE);
 			mScreenshotCallback.onScreenshot(screenShotData.mImage);
 			mGraphics.leaveTextureRenderTarget();
 			mScreenshotCallback = null;
 			mMakingScreenshot = false;
 			catchUpTimer();
 		}
 	}
 
 	public final void drawContent(boolean callPreDraw) {
 
 		try{
 			assert mGraphics.preCheck("Draw content");
 			if(mMetaEventListener!=null)
 				mEventQueue.handleMetaEvents(mMetaEventListener);
 
 			if(mInactive) {
 				mGraphics.beginFrame();
 				mGraphics.clear(0,0,0);
 				mGraphics.endFrame();
 				return;
 			}
 
 			if(mException) {
 				mGraphics.beginFrame();
 				mGraphics.clear(0.1f,0,0);
 				if(mGFXDebug!=null && !mMakingScreenshot)
 					mGFXDebug.draw();
 				mGraphics.endFrame();
 				return;
 			}
 
 			if(mRuntimeState>0 && !mResuming) {
 				mResuming = mLoadedOnce;
 				if(mLoadedOnce) {
 					mGraphics.restart();
 					if(mGraphics.mCurDrawListener!=null)
 						mGraphics.mCurDrawListener.onRestartGraphics();
 				}
 			}
 
 			assert mGraphics.preCheck("Catchup");
 			if(mUpdateMode==UpdateMode.SYNCHRONOUS)
 				catchUp();
 			assert mGraphics.checkErrorInst("Catchup");
 
 			if(mLoadingState>=mStartupSteps && DebugYang.DEBUG_LEVEL>0 && mGFXDebug!=null && !mMakingScreenshot) {
 				assert mGraphics.preCheck("Debug values");
 				mGFXDebug.reset();
 				if(DebugYang.DRAW_GFX_VALUES)
 					mGFXDebug.printGFXDebugValues();
 				assert mGraphics.checkErrorInst("Debug values");
 			}
 			mGraphics.beginFrame();
 			if(mLoadingState>=mStartupSteps) {
 				assert mGraphics.preCheck("Draw call");
 				if(callPreDraw)
 					preDraw();
 				draw();
 				if(DebugYang.DEBUG_LEVEL>0 && mGFXDebug!=null && !mMakingScreenshot) {
 					mGFXDebug.draw();
 				}
 			}else{
 				if(mLoadingState==0) {
 					prepareLoading(mResuming);
 					mLoadingState = 1;
 				}
 				if(mGFXLoader.loadingDone()) {
 					initializeAssets(mLoadingState-mLoadingSteps,mResuming);
 					mLoadingState = mStartupSteps;
 					mGFXLoader.finishLoading();
 					mGraphics.unbindTextures();
 					onLoadingFinished(mResuming);
 					mLoadedOnce = true;
 					mEventQueue.clearEvents();
 					mRuntimeState = RUNTIME_STATE_RUNNING;
 					mResuming = false;
 					if(callPreDraw)
 						preDraw();
 					draw();
 				}else{
 					//upload new textures
 					mGFXLoader.uploadLoadedTextures();
 					drawLoadingScreen(0, mLoadingProgress,mResuming);	//TODO remove first param?
 				}
 				mCatchUpTime = 0;
 			}
 			mGraphics.endFrame();
 
 		}catch(final Exception ex){
 			exceptionOccurred(ex);
 		}
 	}
 
 	protected boolean isPlayingMacro() {
 		return mMacro!=null && !mMacro.mFinished;
 	}
 
 	public void stopMacro() {
 		if(mMacro==null)
 			return;
 		mMacro.close();
 		mMacro = null;
 		refreshMetaMode();
 	}
 
 	protected void refreshMetaMode() {
 		mEventQueue.mMetaMode = mPaused || (mMacro!=null && !mMacro.mFinished) || mForceMetaMode;
 	}
 
 	public void stop() {
 		mInactive = true;
 		mRuntimeState = RUNTIME_STATE_STOPPED;
 		mLoadingState = 0;
 	}
 
 	/**
 	 * Non-GL-Thread!
 	 */
 	public void pause() {
 //		if(mUpdateThread!=null)
 //			synchronized (mUpdateThread) {
 //				mUpdateThread.suspend();
 //			}
 		mInactive = true;
 
 		mCatchUpTime = 0;
 		mRuntimeState = RUNTIME_STATE_PAUSED;
 		if(mSystemCalls == null || mSystemCalls.reloadAfterPause()) {
 			mLoadingState = 0;
 		}
 		if(!mLoadedOnce) {
 			mInterrupted = true;
 			onLoadingInterrupted(false);
 		}
 		if(mResuming) {
 			mInterrupted = true;
 			onLoadingInterrupted(true);
 		}
 		if(mSensor!=null)
 			mSensor.pause();
 	}
 
 	/**
 	 * Non-GL-Thread!
 	 */
 	public void resume() {
 //		if(mUpdateThread!=null && mUpdateThread.isAlive())
 //			synchronized (mUpdateThread) {
 //				mUpdateThread.resume();
 //			}
 		mInactive = false;
 		mCatchUpTime = 0;
 		if(mSystemCalls==null || mRuntimeState>RUNTIME_STATE_STOPPED || mSystemCalls.reloadAfterPause()) {
 			mResuming = false;
 			mLoadingState = 0;
 		}else{
 			mRuntimeState = RUNTIME_STATE_RUNNING;
 			if (mInterrupted) {
 				mLoadingState = 0;
 				stopLoadingThread();
 				mInterrupted = false;
 			} else {
 				mLoadingState = mLoadingSteps;
 			}
 		}
 		if(mSensor!=null)
 			mSensor.resume();
 	}
 
 	public void setPaused(boolean paused) {
 		mPaused = paused;
 		refreshMetaMode();
 	}
 
 	public boolean isPaused() {
 		return mPaused;
 	}
 
 	public void dispose() {
 		mEventQueue.close();
 		if(mMacro!=null)
 			mMacro.close();
 	}
 
 	public void exit() {
 		App.systemCalls.exit();
 	}
 
 	public void handleArgs(String[] args,int startIndex) {
 		if(args==null)
 			return;
 		if(args.length>=1)
 			setMacroFilename(args[0]);
 	}
 
 	public void simulatePause() {
 		pause();
 	}
 
 	public void simulateStop() {
 		pause();
 		stop();
 		mGraphics.deleteAllTextures();
 	}
 
 	public void simulateResume() {
 		if(mInactive)
 			resume();
 	}
 
 	public boolean isInactive() {
 		return mInactive;
 	}
 
 	public boolean isStereoVision() {
 		return mForceStereoVision;
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
 		return false;
 	}
 
 	@Override
 	public double getTime() {
 		return mProgramTime;
 	}
 
 	/**
 	 * @return True, if and only if, there is no screenshot to be made at the moment.
 	 */
 	public boolean makeScreenshot(ScreenshotCallback callback) {
 		if(mScreenshotCallback!=null)
 			return false;
 		else{
 			mScreenshotCallback = callback;
 			return true;
 		}
 	}
 
 	public boolean isMakingScreenshot() {
 		return mMakingScreenshot;
 	}
 
 }
