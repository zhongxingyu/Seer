 package com.evervoid.utils;
 
 import java.io.File;
import java.io.UnsupportedEncodingException;
 import java.net.URL;
import java.net.URLDecoder;
 
 public class ResourceUtils
 {
 	private static String sAppPath = null;
 	private static String sMainPath = null;
 
 	public static String getAppDir()
 	{
 		if (sAppPath == null) {
 			if (System.getProperty("os.name").toLowerCase().contains("win")) {
 				// windows
 				sAppPath = System.getenv("APPDATA") + "/everVoid/";
 			}
 			else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
 				// mac
 				sAppPath = System.getProperty("user.home") + "/Library/Application Support/everVoid/";
 			}
 			else {
 				// default - assume unix
 				sAppPath = System.getProperty("user.home") + "/.everVoid/";
 			}
 		}
 		return sAppPath;
 	}
 
 	public static String getMainDir()
 	{
 		if (sMainPath == null) {
 			final URL url = ResourceUtils.class.getResource("");
 			final String urlPath = url.toString();
 			String path = "";
 			if (url.getProtocol().equals("file")) {
 				// for testing
 				path = urlPath.substring("file:".length(), urlPath.lastIndexOf("bin"));
 			}
 			else if (url.getProtocol().equals("jar")) {
 				// for deployment
 				path = urlPath.substring("jar:file:/".length(), urlPath.lastIndexOf("everVoid.jar"));
 				// this gives the relative path, now time to find abs
 				final File f = new File(path);
 				path = f.getAbsolutePath() + "/";
 			}
 			else {
 				System.out.println("Could not find jar file! " + url.getProtocol() + " unrecognized");
 				System.exit(-1);
 			}
			try {
				System.out.println(path);
				// Decode URL
				sMainPath = URLDecoder.decode(path, "UTF-8");
				System.out.println(sMainPath);
			}
			catch (final UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
 		return sMainPath;
 	}
 
 	public static String getResourceDir()
 	{
 		return getMainDir() + "res/";
 	}
 }
