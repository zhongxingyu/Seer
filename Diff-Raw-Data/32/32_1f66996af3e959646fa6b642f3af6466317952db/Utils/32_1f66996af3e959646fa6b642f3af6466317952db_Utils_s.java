 package edu.illinois.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 /**
  * @author Tianyi Wang
  */
 public class Utils {
 	
	/** Timeout (in ms) we specify for each HTTP request */
     public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
     
	/**
      * Configures the httpClient to connect to the URL provided.
      */
     public static HttpClient getHttpClient() {
         HttpClient httpClient = new DefaultHttpClient();
         final HttpParams params = httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         return httpClient;
     }
     
 	public static boolean isNetworkConnected(Activity activity) {
 		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		
 		if (ni == null) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static String inputStreamToString(InputStream is) {
 		String line;
 		StringBuilder total = new StringBuilder();
 		
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		
 		try {
 			while ((line = br.readLine()) != null) {
 				total.append(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return total.toString();
 	}
 	
 	/**
 	 * Convert a character to its binary form represented using {@link String}.
 	 * 
 	 * @param ch the character.
 	 * @return {@link String} of character 0's and 1's.
 	 */
 	public static String charToBinaryString(char ch) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (int i = 0; i < 8; i++) {
 			if ((ch & 1) == 1) {
 				sb.insert(0, '1');
 			} else {
 				sb.insert(0, '0');
 			}
 			ch >>= 1;
 		}
 		
 		return sb.toString();
 	}
 	
 	/**
 	 * Convert a short int to its binary form represented using {@link String}.
 	 * 
 	 * @param sh the short int.
 	 * @return {@link String} of short int in 0's and 1's.
 	 */
 	public static String shortToBinaryString(short sh) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (int i = 0; i < 16; i++) {
 			if ((sh & 1) == 1) {
 				sb.insert(0, '1');
 			} else {
 				sb.insert(0, '0');
 			}
 			sh >>= 1;
 		}
 		
 		return sb.toString();
 	}
 }
