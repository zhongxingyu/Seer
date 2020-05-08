 package io.github.maluta.libusbproject;
 
 public class NativeLib {
 	static {
		System.loadLibrary("usb-1.0");
		System.loadLibrary("libusbproject");
 	}
 	/**
 	 * List devices function
 	 * 
 	 */
 	public native String listDevices();
 }
