 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.connectivity;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpException;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.util.Log;
 
 /**
  * Provides methods for manipulating websites. 
  */
 
 public class WebsiteOpen {
 	
 	/**
 	 * Returns a {@link URLConnection} object. Converts the passed {@link String}
 	 * to {@link URL} and calls {@link URL#openConnection()}
 	 * 
 	 *	 
 	                          
 	@param  str  An object of the type String
 	 *  	                          	
 	                          
 	@return      URLConnection
 	 *  
 	
 	@see         URLConnection
 	 */
 	static synchronized public URLConnection openURLConnection(String str) throws IOException, HttpException {
 		
 
 		URL url = new URL(str);
 		URLConnection urlConnection = url.openConnection();
 		
		return urlConnection;
 	}
 
 	/**
 	 * Returns a {@link String} object by calling {@link URLConnection#getInputStream()} 
 	 * on the passed {@link URLConnection}. Passes the {@link InputStream} to
 	 * {@link WebsiteOpen#convertStreamToString}.
 	 * 
 	 *	 
 	                          
 	@param  urlConnection  An object of the type {@link URLConnection}
 	 *  	                          	
 	                          
 	@return      String
 	 *  
 	
 	@see         InputStream
 	 */
 	static synchronized public String getContent(URLConnection urlConnection) throws IOException, HttpException {
 		
 			InputStream inputStream = urlConnection.getInputStream();
 	     	return convertStreamToString(inputStream);
 	}
 	
 	/**
 	 * Returns a {@link InputStream} object by calling {@link URLConnection#getInputStream()} 
 	 * on the passed {@link URLConnection}.
 	 * 
 	 *	 
 	                          
 	@param  urlConnection  An object of the type {@link URLConnection}
 	 *  	                          	
 	                          
 	@return      InputStream
 	 *  
 	
 	@see         InputStream
 	 */
 	static synchronized public InputStream getContentStream(URLConnection urlConnection) throws IOException, HttpException {
 
         return urlConnection.getInputStream();
     }
 	
 	/**
 	 * Returns a {@link Bitmap} object by calling {@link WebsiteOpen#getContentStream} 
 	 * on the passed {@link String} by opening a connection to the website's 
 	 * favicon. Passes the {@link InputStream} to
 	 * {@link BitmapFactory#decodeStream(InputStream)}.
 	 * 
 	 *	 
 	                          
 	@param  str  An object of the type {@link String}
 	 *  	                          	
 	                          
 	@return      Bitmap
 	 *  
 	
 	@see         InputStream
 	*
 	
 	@see         BitmapFactory
 	 */
 	static synchronized public Bitmap getFavicon(String str) throws IOException, HttpException {
 		InputStream inputStream = getContentStream(openURLConnection(str + "/favicon.ico"));
 		return BitmapFactory.decodeStream(inputStream);
 	}
 	
 	/**
 	 * Returns a {@link Map} object of HTTP headers where key is the header 
 	 * type. Calls {@link URLConnection#getHeaderFieldKey(int)} and
 	 * {@link URLConnection#getHeaderField(int)}.
 	 * 
 	 *	 
 	                          
 	@param  urlConnection  An object of the type {@link URLConnection}
 	 *  	                          	
 	                          
 	@return      Map<String, String>
 	 *  
 	
 	@see         Map
 	*
 	
 	@see         URLConnection
 	 */
 	static synchronized public Map<String, String> getHeaders(URLConnection urlConnection) throws IOException, HttpException {
 
 		Map<String, String> headerMap = new HashMap <String, String>();
 		String key = new String();
 		String value = new String();
 		
 		for (int i=0 ;	; i++) {
 			key = urlConnection.getHeaderFieldKey(i);
 			value = urlConnection.getHeaderField(i);
 			if (key == null && value == null)
 				break;
 			if (key == null) 
 				key = "status";
 			headerMap.put(key, value);
 			key = "";
 			value = "";
 		}
  
      	return headerMap;
    	}
 	
 	/**
 	 * Returns a {@link String} object. Calls {@link BufferedReader} on the
 	 * {@link InputStream} and builds a {@link StringBuilder} object.
 	 * 
 	 *	 
 	                          
 	@param  inputStream  An object of the type {@link InputStream}
 	 *  	                          	
 	                          
 	@return      String
 	 *  
 	                          
 	@see         BufferedReader
 	 *
 	 
 	@see         StringBuilder
 	 */
 	private synchronized static String convertStreamToString(InputStream inputStream) throws IOException {
 	   
 	    BufferedReader bufferedReader 
 	    = new BufferedReader(new InputStreamReader(inputStream));
 	    StringBuilder stringBuilder = new StringBuilder();
 	    String line = null;
 	    
 	        while ((line = bufferedReader.readLine()) != null) {
 	            stringBuilder.append(line + "\n");
 	        }	   
 	    inputStream.close();	    	    
 	    return stringBuilder.toString();
 	}
 	
 	/**
 	 * Returns the website status code as an int object. Takes as parameter a 
 	 * website header and then parses the status code by regex matching.
 	 * 
 	 *	 
 	                          
 	@param  websiteHeader  An object of the type {@link Map}
 	 *  	                          	
 	                          
 	@return      int
 	 *  
 	                          
 	@see         Pattern
 	 *
 	 
 	@see         Matcher
 	 */
 	public synchronized static int getStatusCode(Map <String, String> websiteHeader) {
 		int statusCode = websiteHeader.size();
 		if(websiteHeader.size()!=0) {
 			Pattern httpCodePattern = 
 				Pattern.compile("10[0-1]|20[0-6]|30[0-7]|40[0-9]|41[0-7]|50[0-5]");
 			Matcher httpCodeMatcher = 
 				httpCodePattern.matcher(websiteHeader.get("status"));
 			while (httpCodeMatcher.find()) {
 			    statusCode = Integer.parseInt(httpCodeMatcher.group());
 			}
 		}
 		return statusCode;
 	}
 	
 	/**
 	 * Returns a {@link String} object to specify whether the connection
 	 * should be HTTP or HTTPS. Takes as parameter a website header and calls
 	 * {@link WebsiteOpen#getStatusCode(Map)}. Uses this status code to determine
 	 * the connection type.
 	 * 
 	 *	 
 	                          
 	@param  websiteHeader  An object of the type {@link Map}
 	 *  	                          	
 	                          
 	@return      String
 	 */
 	public synchronized static String httpOrHttps(Map <String, String> websiteHeader) {
 		String connectionType = "http";
 		int statusCode = getStatusCode(websiteHeader);
 		if (statusCode >= 300 && statusCode <= 307 && statusCode != 306)
 			connectionType = "https";
 		return connectionType;
 	}
 	
 	/**
 	 * Returns the a boolean object depending on whether Internet is available.
 	 * Calls {@link ConnectivityManager#getActiveNetworkInfo()}.
 	 * 
 	 *	 
 	                          
 	@param  connectivityManager  An object of the type {@link ConnectivityManager}
 	 *  	                          	
 	                          
 	@return      boolean
 	 *  
 	                          
 	@see        	ConnectivityManager
 	 */
 	public synchronized static boolean checkInternetAccess(
 			ConnectivityManager connectivityManager) {	    
 	    if (connectivityManager.getActiveNetworkInfo() != null
 	            && connectivityManager.getActiveNetworkInfo().isAvailable()
 	            && connectivityManager.getActiveNetworkInfo().isConnected()) {
 	        return true;
 	    } else {
 	        Log.w("####", "No Internet");
 	        return false;
 	    }
 	}   
 }
