 package com.mridang.cyanight.functions;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.util.Log;
 
 /*
  * This class contains very simple helper-functions
  */
 public class HelperFunctions {
 	
 	/*
 	 * This method returns the version of the build
 	 * 
 	 * @returns   The version of the build
 	 */
 	public static String getVersion() {
 
 		String strBuild = getBuildString();
	    if (strBuild != null) {
 	    	return strBuild.split("-")[0];
 	    }
	    return null;
 		
 	}
 
 	/*
 	 * This method returns the type of the build
 	 * 
 	 * @returns   The type of the build
 	 */
 	public static String getType() {
 
 		String strType = getBuildString();
	    if (strType != null) {
 	    	return strType.split("-")[2];
 	    }
	    return null;
 		
 	}
 	
     /*
      * This method returns a system property using the getprop command
      *
      * @return   The value of the property if found, else null
      */
     public static String getBuildString() {
 
     	String strValue = null;
         BufferedReader birReader = null;
 
         try {
 
         	Process p = Runtime.getRuntime().exec("getprop ro.modversion");
             birReader = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
             strValue = birReader.readLine();
             birReader.close();
 
         } catch (IOException e) {
             Log.e("HelperFunctions", "Unable to read property", e);
         } finally {
 
         	if (birReader != null) {
 
         		try {
 
                 	birReader.close();
 
                 } catch (IOException e) {
                     Log.e("HelperFunctions", "Exception while closing the file", e);
                 }
 
         	}
 
         }
         
         return strValue;
 
     }
 
 	/*
 	 * This method returns the date of the build
 	 * 
 	 * @returns   The date of the build
 	 */
 	public static String getDate() {
 
 		String strDate = getBuildString();
	    if (strDate != null) {
 	    	return strDate.split("-")[1];
 	    }
	    return null;
 		
 	}
 
 	/*
 	 * This method returns the model of the build
 	 * 
 	 * @returns   The version of the build
 	 */
 	public static String getModel() {
 
 		String strModel = getBuildString();
	    if (strModel != null) {
 	    	return strModel.split("-")[3];
 	    }
	    return null;
 		
 	}
     
 }
