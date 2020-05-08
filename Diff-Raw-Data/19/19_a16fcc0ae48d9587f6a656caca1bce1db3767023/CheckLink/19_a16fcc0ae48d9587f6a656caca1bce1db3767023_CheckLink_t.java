 package com.im1x;
 
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 
 /**
  * 
  * @author Vitaly Batrakov
  *
  */
 public class CheckLink {
 	/**
 	 * 
 	 * @param urlp - url for check
 	 * @param timeout timeout (ms)
 	 * @return code: -1 - error, 0 - timeout, 1 - good, 2 - good&psw 
 	 * @throws ConnectException if wrong URL
 	 */
 	public int check(String urlp, int timeout) throws ConnectException{
 		try {
 			URL url = new URL(urlp);
 
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setConnectTimeout(timeout);
 			conn.connect();
			//System.out.println(conn.getResponseCode());
 
 			if (conn.getResponseCode() == 200) {
 				conn.disconnect();
 				return 1;
 			} else if (conn.getResponseCode() == 401) {
 				conn.disconnect();
 				return 2;
 			}
 		} catch (SocketTimeoutException e) {
			//System.out.println("timeout");
 			return 0;
 		} catch (ConnectException e) {
 			throw new ConnectException("Wrong URL");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 }
