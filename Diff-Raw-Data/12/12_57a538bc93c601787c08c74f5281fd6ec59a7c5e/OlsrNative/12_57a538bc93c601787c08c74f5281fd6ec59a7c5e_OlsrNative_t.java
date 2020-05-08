 package net.wlanlj.meshapp;
 
 import android.util.Log;
 
 class OlsrNative {
 
     public static final String MSG_TAG = "[[MeshApp]]::[OlsrNative] -> ";
 
     static {
 	try {
 	    Log.i(MSG_TAG, "Attempting to load GUI Library...");
 	    System.loadLibrary("guilib");
 	}
 	catch (Exception ex) {
 	    Log.e(MSG_TAG, "GUI Library failed to load!");
 	}
     }
 
     public static native int startOlsrd();
 
     public static native int stopOlsrd();
 }
