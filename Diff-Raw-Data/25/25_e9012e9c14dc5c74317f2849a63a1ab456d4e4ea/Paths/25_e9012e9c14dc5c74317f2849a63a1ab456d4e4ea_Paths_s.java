 package com.tortel.externalize;
 
 import java.io.File;
 
 /**
  * Contains the internal and external path information
  * @author Scott Warner
  *
  */
 public class Paths {
 	//String
 	public static final String internal = "/sdcard/";
 	public static final String external = "/Removable/MicroSD/";
 	
 	public static final File intFile = new File("/sdcard/");
 	public static final File extFile = new File("/Removable/MicroSD/");
 	
 	//Types
 	public static final int IMAGES = 0;
 	public static final int DOWNLOADS = 1;
 	
	//Types are indicies for this
	public static final String[] dir = {"DCIM/", "Downloads/"};
 }
