 package com.rocel.adg;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public final class Globals {
 	
	private static final String RESSOURCES_FOLDER = "/"+"res";
 	//FILES & FOLDERS PATHS
	public static final String PATH_DROP_IMG = RESSOURCES_FOLDER + "/" + "download-6-256.png";
	public static final String PATH_WAITING_IMG = RESSOURCES_FOLDER + "/" + "loader.gif";
	public static final String PATH_SAVE_IMG = RESSOURCES_FOLDER + "/" + "upload-9-64.png";
	public static final String PATH_ICON_IMG = RESSOURCES_FOLDER + "/" + "ic_android.png";
	public static final String TMP_FOLDER = "/"+"com.rocel.adg";
	public static final String TMP_FOLDER_IMAGES = "/"+"images";
 	public static final String EXPORT_ZIP_NAME = "drawables.zip";
 	public static String PATH_TO_TMP_FOLDER;
 	public static final String EXPORT_ZIP_EXTENSION = "zip";
 	public static final String EXPORT_ZIP_FILTER = "Zip File";
 	
 
 	//FOLDERS NAMES
 	public static final String FOLDER_LDPI = "drawable-ldpi";
 	public static final String FOLDER_MDPI = "drawable-mdpi";
 	public static final String FOLDER_HDPI = "drawable-hdpi";
 	public static final String FOLDER_XHDPI = "drawable-xhdpi";
 	
 	//QUOTIENT
 //	//NORMAL QUOTIENTS : MDPI AS PATTERN
 //	public static final float QUOTIENT_LDPI = 0.75f;
 //	public static final float QUOTIENT_MDPI = 1f;
 //	public static final float QUOTIENT_HDPI = 1.5f;
 //	public static final float QUOTIENT_XHDPI = 2f;
 
 //	//XHDPI AS PATTERN
 	public static final float QUOTIENT_LDPI = (float) (0.75/2);
 	public static final float QUOTIENT_MDPI = 1/2f;
 	public static final float QUOTIENT_HDPI = (float) (1.5/2);
 	public static final float QUOTIENT_XHDPI = 1f;
 	
 	//MAP OF DEFINITIONS
 	public static Map<String,Float> MAP_DEFINITIONS = new HashMap<String,Float>();
 	static{
 		MAP_DEFINITIONS.put(FOLDER_LDPI, QUOTIENT_LDPI);
 		MAP_DEFINITIONS.put(FOLDER_MDPI, QUOTIENT_MDPI);
 		MAP_DEFINITIONS.put(FOLDER_HDPI, QUOTIENT_HDPI);
 		MAP_DEFINITIONS.put(FOLDER_XHDPI, QUOTIENT_XHDPI);
 	}
 	
 }
