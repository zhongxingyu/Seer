 package com.nvidia.fcamerapro;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  * This singleton class is responsible for communicating with its
  * C++-side counterpart. Calling its methods will generate a new message,
  * which is queued by the C++-side and then processed to invoke the
  * appropriate FCam API calls or to change the state of the C++ camera thread.
  */
 public final class FCamInterface {
 	static {
 		System.loadLibrary("FCamTegraHal");
 		System.loadLibrary("FCam");
 		System.loadLibrary("ImageStack");
 		System.loadLibrary("OpenCV");
 		System.loadLibrary("fcam_iface");
 	}
 
 	/* ====================================================================
 	 * Constants for message types. This list should be synchronized
 	 * with jni/ParamSetReqeust.h 
 	 * ==================================================================== */
 	final static private int PARAM_SHOT = 0;
 	final static private int PARAM_RESOLUTION = 1;
 	final static private int PARAM_BURST_SIZE = 2;
 	final static private int PARAM_OUTPUT_FORMAT = 3;
 	final static private int PARAM_VIEWER_ACTIVE = 4;
 	final static private int PARAM_OUTPUT_DIRECTORY = 5;
 	final static private int PARAM_OUTPUT_FILE_ID = 6;
 	final static private int PARAM_LUMINANCE_HISTOGRAM = 7;
 	final static private int PARAM_PREVIEW_EXPOSURE = 8;
 	final static private int PARAM_PREVIEW_FOCUS = 9;
 	final static private int PARAM_PREVIEW_GAIN = 10;
 	final static private int PARAM_PREVIEW_WB = 11;
 	final static private int PARAM_PREVIEW_AUTO_EXPOSURE_ON = 12;
 	final static private int PARAM_PREVIEW_AUTO_FOCUS_ON = 13;
 	final static private int PARAM_PREVIEW_AUTO_GAIN_ON = 14;
 	final static private int PARAM_PREVIEW_AUTO_WB_ON = 15;
 	final static private int PARAM_CAPTURE_FPS = 16;
 	final static private int PARAM_TAKE_PICTURE = 17;
	final static private int PARAM_PREVIEW_ALIGNMENT_ASSIST_ON = 20;
 	
 	final static private int SHOT_PARAM_EXPOSURE = 0;
 	final static private int SHOT_PARAM_FOCUS = 1;
 	final static private int SHOT_PARAM_GAIN = 2;
 	final static private int SHOT_PARAM_WB = 3;
 	final static private int SHOT_PARAM_FLASH = 4;
 	
 	final private Vector<FCamInterfaceEventListener> mEventListenters = new Vector<FCamInterfaceEventListener>();
 
 	public final static int PREVIEW_EXPOSURE = 0;
 	public final static int PREVIEW_GAIN = 1;
 	public final static int PREVIEW_WB = 2;
 	public final static int PREVIEW_FOCUS = 3;
 
 	/* ====================================================================
 	 * Singleton class model. Only one instance may exist. 
 	 * ==================================================================== */
 	static private FCamInterface sInstance = new FCamInterface();
 	static public FCamInterface GetInstance() {	return sInstance; }
 	private FCamInterface() { init(); }
 
 	/* ====================================================================
 	 * Deal with FCamInterfaceEventListeners. 
 	 * ==================================================================== */
 	public void addEventListener(FCamInterfaceEventListener listener) {
 		if (mEventListenters.contains(listener))
 			throw new IllegalArgumentException("listener already registered!");
 		mEventListenters.add(listener);
 	}
 	public void removeEventListener(FCamInterfaceEventListener listener) {
 		if (!mEventListenters.contains(listener))
 			throw new IllegalArgumentException("listener not registered!");
 		mEventListenters.remove(listener);
 	}
 	public void notifyCaptureStart() {
 		for (FCamInterfaceEventListener listener : mEventListenters)
 			listener.onCaptureStart();
 	}
 	public void notifyCaptureComplete() {
 		for (FCamInterfaceEventListener listener : mEventListenters)
 			listener.onCaptureComplete();
 	}
 	public void notifyFileSystemChange() {
 		for (FCamInterfaceEventListener listener : mEventListenters)
 			listener.onFileSystemChange();
 	}
 	public void notifyShotParamChange(int shotParamId) {
 		for (FCamInterfaceEventListener listener : mEventListenters)
 			listener.onShotParamChange(shotParamId);
 	}
 
 	/* ====================================================================
 	 * Java-side Interface. These methods are called by other Java
 	 * components, which in turn interact with the appropriate native-side
 	 * interface.
 	 * ==================================================================== */
 	// Fetch histogram data from FCam.
 	public void getHistogramData(float[] bins) {
 		getParamFloatArray(PARAM_LUMINANCE_HISTOGRAM, bins);
 	}
 
 	// Turn Auto[Exposure/Gain/WB/Focus] on or off.
 	public void enablePreviewParamEvaluator(int shotParamId, boolean value) {
 		int pvalue = value ? 1 : 0;
 		switch (shotParamId) {
 		case PREVIEW_EXPOSURE:
 			setParamInt(PARAM_PREVIEW_AUTO_EXPOSURE_ON, pvalue);
 			break;
 		case PREVIEW_GAIN:
 			setParamInt(PARAM_PREVIEW_AUTO_GAIN_ON, pvalue);
 			break;
 		case PREVIEW_WB:
 			setParamInt(PARAM_PREVIEW_AUTO_WB_ON, pvalue);
 			break;
 		case PREVIEW_FOCUS:
 			setParamInt(PARAM_PREVIEW_AUTO_FOCUS_ON, pvalue);
 			break;
 		}
 	}
 	
 	// Turn alignment assist on or off.
	public void enableAlignmentAssist(boolean value) {
		int pvalue = value ? 1 : 0;
		setParamInt(PARAM_PREVIEW_ALIGNMENT_ASSIST_ON, pvalue);
	}
 
 	// Get a parameter for the viewfinder frame.
 	public int getPreviewParam(int shotParamId) {
 		switch (shotParamId) {
 		case PREVIEW_EXPOSURE:
 			return getParamInt(PARAM_PREVIEW_EXPOSURE);
 		case PREVIEW_GAIN:
 			return getParamInt(PARAM_PREVIEW_GAIN);
 		case PREVIEW_WB:
 			return getParamInt(PARAM_PREVIEW_WB);
 		case PREVIEW_FOCUS:
 			return getParamInt(PARAM_PREVIEW_FOCUS);
 		}
 		return 0;
 	}
 
 	// Set a parameter for the viewfinder frame.
 	public void setPreviewParam(int shotParamId, double value) {
 		switch (shotParamId) {
 		case PREVIEW_EXPOSURE:
 			setParamInt(PARAM_PREVIEW_EXPOSURE, (int) value);
 			break;
 		case PREVIEW_GAIN:
 			setParamInt(PARAM_PREVIEW_GAIN, (int) value);
 			break;
 		case PREVIEW_WB:
 			setParamInt(PARAM_PREVIEW_WB, (int) value);
 			break;
 		case PREVIEW_FOCUS:
 			setParamInt(PARAM_PREVIEW_FOCUS, (int) value);
 			break;
 		}
 	}
 	 
 	public float getCaptureFps() {
 		return getParamFloat(PARAM_CAPTURE_FPS);
 	}
 	
 	public boolean isPreviewActive() {
 		return getParamInt(PARAM_VIEWER_ACTIVE) != 0;
 	}
 	
 	public void setPreviewActive(boolean enabled) {
 		setParamInt(PARAM_VIEWER_ACTIVE, enabled ? 1 : 0);
 	}
 
 	public void capture(ArrayList<FCamShot> shots) {
 		int[] shotArray = new int[5];
 		setParamInt(PARAM_BURST_SIZE, shots.size());
 		for (int i = 0; i < shots.size(); i++) {
 			FCamShot shot = shots.get(i);
 			shotArray[SHOT_PARAM_EXPOSURE] = (int) shot.exposure;
 			shotArray[SHOT_PARAM_FOCUS] = (int) shot.focus;
 			shotArray[SHOT_PARAM_GAIN] = (int) shot.gain;
 			shotArray[SHOT_PARAM_WB] = (int) shot.wb;
 			shotArray[SHOT_PARAM_FLASH] = shot.flashOn ? 1 : 0;
 			setParamIntArray(PARAM_SHOT | (i << 16), shotArray);
 		}
 		setParamInt(PARAM_TAKE_PICTURE, 1);
 	}
 
 	public boolean isCapturing() {
 		return getParamInt(PARAM_TAKE_PICTURE) != 0;
 	}
 
 	public void setStorageDirectory(String dir) {
 		setParamString(PARAM_OUTPUT_DIRECTORY, dir);
 	}
 
 	public void setOutputFileId(int id) {
 		setParamInt(PARAM_OUTPUT_FILE_ID, id);
 	}
 
 	/* ====================================================================
 	 * Native Interface. These mare defined in jni/FCamInterface.cpp.
 	 * ==================================================================== */
 	private native void init();
 	public native int getViewerTextureTarget();
 	public native int lockViewerTexture();
 	public native void unlockViewerTexture();
 	private native void setParamInt(int param, int value);
 	private native void setParamFloat(int param, float value);
 	private native void setParamString(int param, String value);
 	private native void setParamIntArray(int param, int[] value);
 	private native int getParamInt(int param);
 	private native float getParamFloat(int param);
 	private native String getParamString(int param);
 	private native void getParamIntArray(int param, float[] value);
 	private native void getParamFloatArray(int param, float[] value);
 	public native void enqueueMessageForAutofocus();
 	public native void enqueueMessageForFastAutofocus();
 	public native void enqueueMessageForAutofocusSpot(float x, float y);	
 	/* [CS478] Assignment #2
 	 * Add a new declaration for a method that requests a photograph
 	 * that is created by merging a flash and no-flash image pair. The
 	 * pair should be passed as file paths represented as Java String 
 	 * objects. This method should not return anything (its result will 
 	 * be written to the file system, instead.) 
 	 */
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 	public native void flashNoFlash(String file1, String file2);
 
 	
 	/* [CS478] Assignment #2
 	 * Add a new declaration for a method that enqueues a message representing a 
 	 * request for face-detection-based autofocus. There should be a corresponding
 	 * method in FCamInterface.cpp
 	 */
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 }
