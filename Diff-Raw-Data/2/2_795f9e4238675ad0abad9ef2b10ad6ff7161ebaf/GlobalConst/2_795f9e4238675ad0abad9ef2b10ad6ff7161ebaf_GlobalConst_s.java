 //
package com.bearingpoint.acs;
 
 
 
 
 public class GlobalConst  
 {
 	// Default / hardcode
 	private static String msFile = "File name not set";
 	//
 	public static String DataFile = "buildnumberdata.txt";
 	public static String EmailInjectMsg = "No error detected by the build.";
 	      
 	   
 	private GlobalConst(){}
 
 	
 	static public void setFileName( String psFileName )
 	{
 		  msFile = psFileName;
 	}
 	static public String getFileName()
 	{
 		  return msFile;
 	}
 	//
 	static public String getDataFileName()
 	{
 		  return msFile;
 	}
 	  
 
 }  // EOC
