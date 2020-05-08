 package edu.stanford.nativegraphics;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class NativeLib {
 	private static final String TAG = "NativeLib";
 	private Context mContext;
 	
 	public NativeLib(Context context, int width, int height) {
 		mContext = context;
 		init(width, height);
 	}
 	
     static {
         System.loadLibrary("nativegraphics");
     }
 
     // Called from native
     public String stringCallback(String fileName) {
     	String splitName = fileName.split("\\.")[0];
     	Log.i(TAG, "Looking up resource " + splitName);
     	int resID = mContext.getResources().getIdentifier(splitName, "raw", "edu.stanford.nativegraphics");
     	if(resID == 0) {
     		Log.e(TAG, "Resource " + fileName + " not found.");
     		return null;
     	}
         return RawResourceReader.readTextFileFromRawResource(mContext, resID);
     }
     
     // Called from native
    public Bitmap drawableCallback(String fileName) {
     	String splitName = fileName.split("\\.")[0];
     	Log.i(TAG, "Looking up resource " + splitName);
     	int resID = mContext.getResources().getIdentifier(splitName, "drawable", "edu.stanford.nativegraphics");
     	if(resID == 0) {
     		Log.e(TAG, "Resource " + fileName + " not found.");
     		return null;
     	}
        return BitmapFactory.decodeResource(mContext.getResources(), resID);
     }
 
     // Native Functions
     private native void init(int width, int height);
     public native void renderFrame();
     public static native void pointerDown(float x, float y);
     public static native void pointerMove(float x, float y);
     public static native void pointerUp(float x, float y);
 }
