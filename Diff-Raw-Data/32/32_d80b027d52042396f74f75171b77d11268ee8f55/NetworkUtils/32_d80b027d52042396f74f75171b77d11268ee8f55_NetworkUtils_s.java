 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt.util;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import cm.aptoide.pt.ApplicationAptoide;
 import cm.aptoide.pt.Configs;
 import cm.aptoide.pt.views.ViewIconDownloadPermissions;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class NetworkUtils {
 
 
 	private static int TIME_OUT = 10000;
 
 
 	public BufferedInputStream getInputStream(String url, String username, String password, Context mctx) throws IOException{
 
         HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
 
 
 
         if(username!=null && password!=null){
 			String basicAuth = "Basic " + new String(Base64.encode((username+":"+password).getBytes(),Base64.NO_WRAP ));
 			connection.setRequestProperty ("Authorization", basicAuth);
 		}
         connection.setConnectTimeout(TIME_OUT);
         connection.setReadTimeout(TIME_OUT);
 		connection.setRequestProperty("User-Agent", getUserAgentString(mctx));
         System.out.println("Using user-agent: " + (getUserAgentString(mctx)));
 		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream(), 8 * 1024);
 
 //		if(ApplicationAptoide.DEBUG_MODE)
             Log.i("Aptoide-NetworkUtils", "Getting: "+url);
 
 		return bis;
 
 	}
 
 	public static void setTimeout(int timeout){
 		NetworkUtils.TIME_OUT=timeout;
 	}
 
 	public int checkServerConnection(final String string, final String username, final String password) {
 		try {
 
 			HttpURLConnection client = (HttpURLConnection) new URL(string
 					+ "info.xml").openConnection();
 			if (username != null && password != null) {
 				String basicAuth = "Basic "
 						+ new String(Base64.encode(
 								(username + ":" + password).getBytes(),
 								Base64.NO_WRAP));
 				client.setRequestProperty("Authorization", basicAuth);
 			}
 			client.setConnectTimeout(TIME_OUT);
 			client.setReadTimeout(TIME_OUT);
 			if(ApplicationAptoide.DEBUG_MODE)Log.i("Aptoide-NetworkUtils-checkServerConnection", "Checking on: "+client.getURL().toString());
 			if (client.getContentType().equals("application/xml")) {
 				return 0;
 			} else {
 				return client.getResponseCode();
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public JSONObject getJsonObject(String url, Context mctx) throws IOException, JSONException{
 		String line = null;
 		BufferedReader br = new BufferedReader(new java.io.InputStreamReader(getInputStream(url, null, null, mctx)));
 		StringBuilder sb = new StringBuilder();
 		while ((line = br.readLine()) != null){
 			sb.append(line + '\n');
 		}
 
 		return new JSONObject(sb.toString());
 
 	}
 
 	public static String  getUserAgentString(Context mctx){
 		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mctx);
 		String myid = sPref.getString("myId", "NoInfo");
 		String myscr = sPref.getInt("scW", 0)+"x"+sPref.getInt("scH", 0);
        String verString = sPref.getString("versionName", "NoVersion");
         String partnerid = "";
         if(ApplicationAptoide.PARTNERID!=null){
             partnerid = "PartnerID:"+ApplicationAptoide.PARTNERID+";";
         }
 
 		return "aptoide-" + verString+";"+ Configs.TERMINAL_INFO+";"+myscr+";id:"+myid+";"+sPref.getString(Configs.LOGIN_USER_LOGIN, "")+";"+partnerid;
 	}
 
 
 	public static boolean isConnectionAvailable(Context context){
 		ConnectivityManager connectivityState = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		boolean connectionAvailable = false;
 		try {
 			connectionAvailable = connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable mobile: "+connectionAvailable);
 		} catch (Exception e) { }
 		try {
 			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable wifi: "+connectionAvailable);
 		} catch (Exception e) { }
 		try {
 			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable wimax: "+connectionAvailable);
 		} catch (Exception e) { }
 		try {
 			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable ethernet: "+connectionAvailable);
 		} catch (Exception e) { }
 
 		return connectionAvailable;
 	}
 
 	public static boolean isPermittedConnectionAvailable(Context context, ViewIconDownloadPermissions permissions){
 		ConnectivityManager connectivityState = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		boolean connectionAvailable = false;
 		if(permissions.isWiFi()){
 			try {
 				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
 				if(ApplicationAptoide.DEBUG_MODE)Log.d("ManagerDownloads", "isPermittedConnectionAvailable wifi: "+connectionAvailable);
 			} catch (Exception e) { }
 		}
 		if(permissions.isWiMax()){
 			try {
 				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
 				if(ApplicationAptoide.DEBUG_MODE)Log.d("ManagerDownloads", "isPermittedConnectionAvailable wimax: "+connectionAvailable);
 			} catch (Exception e) { }
 		}
 		if(permissions.isMobile()){
 			try {
 				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
 				if(ApplicationAptoide.DEBUG_MODE)Log.d("ManagerDownloads", "isPermittedConnectionAvailable mobile: "+connectionAvailable);
 			} catch (Exception e) { }
 		}
 		if(permissions.isEthernet()){
 			try {
 				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
 				if(ApplicationAptoide.DEBUG_MODE)Log.d("ManagerDownloads", "isPermittedConnectionAvailable ethernet: "+connectionAvailable);
 			} catch (Exception e) { }
 		}
 
 		if(ApplicationAptoide.DEBUG_MODE)Log.d("ManagerDownloads", "isPermittedConnectionAvailable: "+connectionAvailable+"  permissions: "+permissions);
 		return connectionAvailable;
 	}
 
     public long getLastModified(URL url) throws IOException {
 
         return url.openConnection().getLastModified();
 
     }
 
 
 }
