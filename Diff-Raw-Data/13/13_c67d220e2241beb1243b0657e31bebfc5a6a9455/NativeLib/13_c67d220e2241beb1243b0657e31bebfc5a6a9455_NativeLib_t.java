 package io.github.maluta.libusbproject;
 
 public class NativeLib {
 	static {
			System.loadLibrary("usb-1.0");
			System.loadLibrary("usbproject_jni");
 	}

 	/**
 	 * List devices function
 	 * 
 	 */
 	public native String listDevices();
 }
