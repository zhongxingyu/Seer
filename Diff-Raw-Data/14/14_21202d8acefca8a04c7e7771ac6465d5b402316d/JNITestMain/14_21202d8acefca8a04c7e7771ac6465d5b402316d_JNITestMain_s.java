 package org.mapsforge.playground;
 
 import org.mapsforge.skia.Bitmap;
 
 public class JNITestMain {
 
 	private static void printLibraryPaths(String paths) {
 		for (String path : paths.split(":")) {
 			System.out.println("  " + path);
 		}
 	}
 
 	public static void main(String[] args) {
 		final String workingDirectory = System.getProperty("user.dir");
 		final String customLibraryPath = workingDirectory + "/jni/";
 		final String javaLibraryPath = System.getProperty("java.library.path");
 
 		// Set library directory for our created libraries
 		// System.setProperty("java.library.path", javaLibraryPath + ":" +
 		// customLibraryPath);
 		// System.out.println("Java library path: ");
 		// printLibraryPaths(System.getProperty("java.library.path"));
 
 		// Load libraries
 		System.load(customLibraryPath + "libtest.so");
 		
 		// Perform tests
		Bitmap bmp = new Bitmap();
 		bmp.saveAsPNG("moep.png");
 		bmp.delete();
 	}
 
 }
