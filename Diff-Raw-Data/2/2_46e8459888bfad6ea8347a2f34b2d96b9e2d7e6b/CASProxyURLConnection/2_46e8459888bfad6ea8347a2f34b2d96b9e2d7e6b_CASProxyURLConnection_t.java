 package org.gcx.cas;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.net.CookieHandler;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * @author Jeff Edwards
  *
  */
 public class CASProxyURLConnection {
 	private static Log log = LogFactory.getLog(CASProxyURLConnection.class);
 
 	private String myCASUrl;
 
 	private String myCASIP;
 
 	private boolean mySuccess;
 
 	private String myLastError;
 
     private static DateFormat expiresFormat1
     = new SimpleDateFormat("E, dd MMM yyyy k:m:s 'GMT'", Locale.US);
     
     protected static final int READ_TIMEOUT_TIME = 1000; 
     
     protected static final int CONNECT_TIMEOUT_TIME = 1000; 
 
     
     /**
 	 * 
 	 * @param casURL The hostname of the CAS server being used
 	 * @throws UnknownHostException if CAS server specified cannot be found
 	 */
 	public CASProxyURLConnection(String casURL) throws UnknownHostException {
 		myCASUrl = casURL;
 		myCASIP = InetAddress.getByName(casURL).getHostAddress();
 		mySuccess = true;
 	}
 
 	/**
 	 * @param targeturl The CAS-protected GCX URL to retrieve
 	 * @param proxyticket The proxy ticket given for the specified target URL
 	 * @return the content of the URL, or null upon error
 	 * @throws Exception if problems occur
 	 * 
 	 */
 	public String getURL(String targeturl, String proxyticket) throws IOException {
 		HttpURLConnection.setFollowRedirects(false);
 		URL url = null;
 		HttpURLConnection connection = null;
 		String content = "";
 		BufferedReader br;
 
 		// save default cookieHandler
 		CookieHandler defaultCookieHandler = CookieHandler.getDefault();
 		
 		// prevent the default cookie handler (if it exists)
 		// from collecting our cookies or adding cookies that we don't want
 		CookieHandler.setDefault(null);  
 
 		
 		url = new URL(targeturl + "?ticket=" + proxyticket);
 
 		connection = (HttpURLConnection) url.openConnection();
 		connection.setConnectTimeout(CONNECT_TIMEOUT_TIME);
 		connection.setReadTimeout(READ_TIMEOUT_TIME);
 
 		if (!isCASRedirection(connection)) {
 
 			if (isModCASRedirection(connection)) {
 				String cookie = retrieveModCASCookie(connection);
 				HttpURLConnection.setFollowRedirects(true);
 				URL newURL = new URL(connection.getHeaderField("Location"));
 				connection = (HttpURLConnection) newURL.openConnection();
 				log.info("Setting cookie: " + cookie);
 				connection.addRequestProperty("Cookie", cookie);
 			}
 
 			br = new BufferedReader(new InputStreamReader(
 					(InputStream) connection.getContent()));
 			String line = br.readLine();
 			while (line != null) {
 
 				content += line + "\r\n";
 				line = br.readLine();
 			}
 		} else {
 			String redirectlocation = connection.getHeaderField("Location");
 			URL newurl = new URL(redirectlocation);
 			HttpURLConnection conn = (HttpURLConnection) newurl
 					.openConnection();
 			conn.setConnectTimeout(CONNECT_TIMEOUT_TIME);
 			conn.setReadTimeout(READ_TIMEOUT_TIME);
 			
 			
 			if (isNonCASRedirection(conn)) {
 				String lasturl = conn.getHeaderField("Location");
 				conn = (HttpURLConnection) new URL(connection
 						.getHeaderField("Location")).openConnection();
 				br = new BufferedReader(new InputStreamReader(
 						(InputStream) conn.getContent()));
 				String line = br.readLine();
 				while (line != null) {
 					content += line + "\r\n";
 
 					line = br.readLine();
 				}
 			} else {
 				mySuccess = false;
 				content = null;
 				myLastError = "Could not authenticate to CAS. ";
 			}
 		}
 
 		//restore default cookie handler
 		CookieHandler.setDefault(defaultCookieHandler);
 		
 		return content;
 	}
 
 	/**
 	 * 
 	 * @param conn 
 	 * @return
 	 * 
 	 */
 	private boolean isModCASRedirection(HttpURLConnection conn) {
 		boolean modcasredirect = false;
 		String cookie = conn.getHeaderField("Set-Cookie");
		log.info("Headers: " + conn.getHeaderFields());
 		log.info("Set-Cookie: " + cookie);
 		if (cookie != null) {
 
 			if (cookie.indexOf("modcasid") >= 0) {
 				modcasredirect = true;
 			}
 		}
 		return modcasredirect;
 	}
 
 	private String retrieveModCASCookie(HttpURLConnection conn) {
 		String setcookie = conn.getHeaderField("Set-Cookie");
 
 		return setcookie;
 	}
 
 	private boolean isNonCASRedirection(HttpURLConnection conn)
 			throws IOException {
 
 		boolean noncasredirect = false;
 
 		if (conn.getResponseCode() >= 300 && conn.getResponseCode() < 400) {
 			String casip = InetAddress.getByName(myCASUrl).getHostAddress();
 			String redirectlocation = conn.getHeaderField("Location");
 
 			if (redirectlocation.indexOf(myCASUrl) == -1
 					&& redirectlocation.indexOf(casip) == -1) {
 				noncasredirect = true;
 			}
 		}
 		return noncasredirect;
 	}
 
 	private boolean isCASRedirection(HttpURLConnection conn) throws IOException {
 
 		boolean casredirect = false;
 		int responsecode = conn.getResponseCode();
 		if (responsecode >= 300 && responsecode < 400) {
 			String casip = InetAddress.getByName(myCASUrl).getHostAddress();
 			String redirectlocation = conn.getHeaderField("Location");
 
 			if (redirectlocation.indexOf(myCASUrl) >= 0
 					|| redirectlocation.indexOf(casip) >= 0) {
 				casredirect = true;
 			}
 		}
 		return casredirect;
 	}
 
 	public String getError() {
 		return myLastError;
 	}
 
 	public boolean wasSuccess() {
 		return mySuccess;
 	}
 }
