 /**
  * @desc: constant fields for convinience.
  * 
  */
 package com.sohu.wuhan;
 
 /**
  * 
 * @author leon
  *
  */
 public class Constant {
 	
 	public static int HTTP_TIMEOUT 			= 5000;		// 30 seconds to timeout, adjust whenever.
 	
 //	public static String DOMAIN 			= "http://10.7.4.8"; //"http://kan.sohu.com";
	public static String DOMAIN 			= "http://10.7.4.7"; //"http://kan.sohu.com";
 	
 	public static String ARTICLE_CREATE 	= "/article/add.xml/";
 	public static String ARTICLE_READ		= "/article/show/";
 	public static String ARTICLE_UPDATE		= "/article/modify/";
 	public static String ARTICLE_DELETE		= "/article/delete/";
 	public static String ARTICLE_PROBE		= "/article/list.xml/?access_token=";
 	
 	public static String GET  = "GET";
 	public static String POST = "POST";
 	
 	/*
 	 * @desc: error code for I/O operation
 	 * 
 	 */
 	
 	public enum Error {
 		min,
 		OK,
 		Fail,
 		NetIO,
 		Timeout,
 		Encode,
 		ParamNull,
 		Url,
 		max,
 	};
 }
