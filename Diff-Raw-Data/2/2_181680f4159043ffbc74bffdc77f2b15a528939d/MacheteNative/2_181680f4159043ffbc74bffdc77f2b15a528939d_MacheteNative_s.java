 package com.maton.machete;
 
 import android.util.Log;
 
 public class MacheteNative {
 	static {
 		Log.i("machete", "Loading OpenAL...");
 		System.loadLibrary("openal");
 		Log.i("machete", "Loading Game OpenGLES 2.0...");
 		try {
 			System.loadLibrary("game");
 			GL_VERSION = 2;
 		} catch (Throwable e) {
 			Log.i("machete", "Fallback into OpenGLES 1.1...");
 			System.loadLibrary("game11");
 			GL_VERSION = 1;
 		}
 	}
 	
 	public static int GL_VERSION = 2;
 	
 	public native void initialize(String name, String apkpath, int w, int h, int o, int opengles);
 	public native void resourceOffset(String resource, long offset, long size);
 	public native void resize(int w, int h, int o);
 	public native void render();
 	public native void pause();
 	public native void update(float time);
 	public native void resume();
 	public native void start();
 	public native void stop();
 	public native void touch(int id, int event, float x, float y);
 	public native void startup();
 	public native void onKeyTyped(int key);
 	public native void reconfigure();
 }
