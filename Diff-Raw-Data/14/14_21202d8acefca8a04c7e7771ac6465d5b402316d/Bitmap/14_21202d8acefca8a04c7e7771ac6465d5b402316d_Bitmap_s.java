 package org.mapsforge.skia;
 
 public class Bitmap {
 	int nativeObj;
 	
	public Bitmap() {
		this.nativeObj = createBitmap();
 		System.out.printf("[Java] Created native bitmap @ 0x%x\r\n", this.nativeObj);
 	}
 	
	private static native int createBitmap();
 	public native void delete();
 	public native void recycle();
 	public native void saveAsPNG(String path);
 }
