 package com.wifi.sapguestconnect.connection;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.wifi.sapguestconnect.LoginData;
 import com.wifi.sapguestconnect.common.WifiUtil;
 import com.wifi.sapguestconnect.data.DataBaseHelper;
 import com.wifi.sapguestconnect.log.LogManager;
 import com.wifi.sapguestconnect.preferences.PreferencesFacade;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 public class ConnectHelper { // TODO remove PUBLIC modifier
 	
 	private final int MAX_LOGIN_ATTEMPTS = 3; 
     private final String MY_DATABASE_TABLE = "DataTable";
 	private Context context;
     private LoginData loginData = new LoginData();
     private boolean isLoginDataChanged = false;
 	private WifiManager wm = null;
 	
     public ConnectHelper(final Context context){
     	LogManager.LogFunctionCall("ConnectHelper", "C'tor(Context)");
     	
     	this.context = context;
     	this.wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);	
     	
     	LoadLoginData();
     }
 	
     public ConnectHelper(final Context context, final WifiManager wm){
     	LogManager.LogFunctionCall("ConnectHelper", "C'tor(Context, WifiManager)");
     	
     	this.context = context;
     	this.wm = wm;
     	
     	LoadLoginData();
     }
     
 	boolean isLoginDataChanged() {
 		LogManager.LogFunctionCall("ConnectHelper", "isLoginDataChanged()");
 		return isLoginDataChanged;
 	}
 
 	public LoginData getLoginData(){ // TODO make private
 		LogManager.LogFunctionCall("ConnectHelper", "getLoginData()");
 		return loginData;
 	}
 	
 	void setLoginDataChanged(boolean isLoginDataChanged) {
 		LogManager.LogFunctionCall("ConnectHelper", "isLoginDataChanged()");
 		this.isLoginDataChanged = isLoginDataChanged;
 	}
 
     boolean isLoginDataExist(final String user, final String pass, final String ssid){
     	LogManager.LogFunctionCall("ConnectHelper", "isLoginDataExist()");
     	
 		if(loginData.getUser() != null && loginData.getPass() != null && loginData.getSSID() != null &&
 		   loginData.getUser() != "" && loginData.getPass() != "" && loginData.getSSID() != "" &&
 		   loginData.getUser().equals(user) && 
 		   loginData.getPass().equals(pass) && 
 		   loginData.getSSID().equals(ssid)){
 			return true;
 		}
 		return false;
 	}
 
     public boolean isConnectedToCorrectWiFi() 
     {
     	LogManager.LogFunctionCall("ConnectHelper", "isConnectedToCorrectWiFi()");
     	
 	    return isConnectedToCorrectWiFi(WifiUtil.getSSID(wm));
     }
     
     public boolean isConnectedToCorrectWiFi(final String ssID) 
     {
     	LogManager.LogFunctionCall("ConnectHelper", "isConnectedToCorrectWiFi(String)");
     	
     	if (wm.isWifiEnabled() && (ssID != null) && (ssID.trim().length() > 0))
     		return loginData.getSSID().compareToIgnoreCase(ssID) == 0;
     	else
     		return false;
     }
     
 	public void saveLoginData(final String user, final String pass, final String netID){
 		LogManager.LogFunctionCall("ConnectHelper", "saveLoginData()");
 		DataBaseHelper myDbHelper = new DataBaseHelper(context);
 		try {
 				myDbHelper.createDataBase();
 		} catch (IOException ioe) {
 			LogManager.LogException(ioe, "ConnectHelper", "saveLoginData() [createDataBase]");
 			throw new Error("Unable to create database");
 		}
 		try {
 			myDbHelper.openDataBase();
 		} catch (SQLException sqle) {
 			LogManager.LogException(sqle, "ConnectHelper", "saveLoginData() [openDataBase]");
 			throw sqle;
 		}
 		
 		loginData.setUser(user);
 		loginData.setPass(pass);
 		loginData.setSSID(netID);
 
 		myDbHelper.saveLoginInformation(MY_DATABASE_TABLE, loginData.getUser(), loginData.getPass(), loginData.getSSID());
 		loginData = myDbHelper.getLoginData(MY_DATABASE_TABLE);
 		
 		isLoginDataChanged = false;
 		myDbHelper.close();
 	}
 
 	public boolean LoadLoginData() { // TODO move to a different class
 		LogManager.LogFunctionCall("ConnectHelper", "LoadLoginData()");
 		
 		boolean retCode = false;
 		
 		DataBaseHelper myDbHelper = new DataBaseHelper(context);
 		try {
 				Log.e("WiFiConnect", ">>>>WiFiConnect>>>> 'LoadLoginData' before 'createDataBase' ...");
 				myDbHelper.createDataBase();
 				retCode = true;
 				Log.e("WiFiConnect", ">>>>WiFiConnect>>>> 'LoadLoginData' after 'createDataBase' ...");
 		} catch (IOException ioe) {
 			LogManager.LogException(ioe, "ConnectHelper", "LoadLoginData() [createDataBase]");
 			retCode = false;
 		}
 		try {
 			Log.e("WiFiConnect", ">>>>WiFiConnect>>>> 'LoadLoginData' before 'openDataBase' ...");
 			myDbHelper.openDataBase();
 			retCode = true;
 			Log.e("WiFiConnect", ">>>>WiFiConnect>>>> 'LoadLoginData' after 'openDataBase' ...");
 		} catch (SQLException sqle) {
 			LogManager.LogException(sqle, "ConnectHelper", "LoadLoginData() [openDataBase]");
         	retCode = false;
 		}
 		
 		if( retCode != false ){
 			loginData = myDbHelper.getLoginData(MY_DATABASE_TABLE);
 			String ssid = loginData.getSSID();
 			if(ssid != null && ssid.length() > 0){
 				loginData.setSSID(ssid);
 			}
 			myDbHelper.close();
 			retCode = true;
 		}
 		
 //		// create and save login information in database
 //		if(loginData == null){
 //			long res = myDbHelper.saveLoginInformation(MY_DATABASE_TABLE, user, pass, bssID);
 //			loginData = myDbHelper.getLoginData(MY_DATABASE_TABLE);
 //		}
 
     	return retCode;
     }
 
 
 
 	
 	public boolean isLoggedInToSAP(){
 		LogManager.LogFunctionCall("ConnectHelper", "isLoggedInToSAP()");
 		boolean isLoggedInToSAP = false;
 		
 		if(ifWifiEnabled() == true){
 			String connUrl = "https://www.google.com";
             
 	        HttpGet getMethod=new HttpGet(connUrl);
 	        
 	        try {
 	        	MyResponseHandler tmpHandler = new MyResponseHandler();
 	        	ResponseHandler<String> responseHandler=tmpHandler;
 	        	HttpClient httpclient = new DefaultHttpClient();
 	        	httpclient.execute(getMethod, responseHandler);
 	        	isLoggedInToSAP = (tmpHandler.getStatus() == 200);
 	        }
 	        catch (Throwable t) {
 	        	LogManager.LogInfoMsg("ConnectHelper", "isLoggedInToSAP()", "Not logged in to Guest WiFi");
 	        }
         }
 		return isLoggedInToSAP;
 	}
 	
 	private ConnectionErrorMessages loginToSAPWiFi(){
 		LogManager.LogFunctionCall("ConnectHelper", "loginToSAPWiFi()");
     	if(ifWifiEnabled() == true){
             String macAddress = getMacAddress();
             String ipAddress = getIPAddress();
             String hostName = PreferencesFacade.getLocation(context).getConnectionHostName();
             // "https://wlan.sap.com/cgi-bin/login?cmd=login&mac=00:18:de:14:20:91&ip=192.168.143.135&essid=SAP-Guest&url=http%3A%2F%2Fwww%2Egoogle%2Ecom%2F";
             String connUrl = "https://mobile-net.hp.com:8090/index.asp";
 
             if(getSSID().equals(loginData.getSSID())){
             	// https://github.com/tcg/comfort-sweet/blob/master/comfort_sweet.py
     	        HttpsURLConnection httpsConnection = openConnectionToHTTPS(connUrl);
     	        logInToWiFi(httpsConnection);
     	        if(isLoggedIn(httpsConnection)){
     	        	//show message login succeeded
     	        	return ConnectionErrorMessages.SUCCESS;
     	        }
     	        else{
     	        	//show message login failed
     	        	return ConnectionErrorMessages.FAIL;
     	        }
             }
             else{
             	//show message that it is not SAP WiFi currently connected
             	return ConnectionErrorMessages.UNKNOWN_WIFI;
             }
         }
         else{
         	return ConnectionErrorMessages.WIFI_TURNED_OFF;
         }
 	}
 	
 	public ConnectionErrorMessages connectToWifi()
 	{
 		LogManager.LogFunctionCall("ConnectHelper", "connectToWifi()");
 		
     	int attemptNumber = 0;
     	ConnectionErrorMessages lastLoginStatus = ConnectionErrorMessages.SUCCESS;
     	
     	while ((!isLoggedInToSAP()) && (attemptNumber < MAX_LOGIN_ATTEMPTS))
     	{
     		attemptNumber++;
     		lastLoginStatus = loginToSAPWiFi();
     		if (lastLoginStatus != ConnectionErrorMessages.FAIL)
     		{
     			break;
     		}
     	}
     	
     	if ((attemptNumber >= MAX_LOGIN_ATTEMPTS) && (!isLoggedInToSAP()))
     	{
     		LogManager.LogInfoMsg("ConnectHelper", "connectToWifi()", "Failed To Connect");
     	}
 		
 		return lastLoginStatus;
 	}
 	
 	HttpsURLConnection openConnectionToHTTPS(String connUrl)
 	{
 		LogManager.LogFunctionCall("ConnectHelper", "openConnectionToHTTPS()");
 		
 		HttpsURLConnection httpsConnection = null;
 		try {
 			// Trust every server - dont check for any certificate 
 	        // Create a trust manager that does not validate certificate chains 
 			TrustManager[] trustAllCerts = new TrustManager[] { 
 				new X509TrustManager(){
 	                public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
 	                        return new java.security.cert.X509Certificate[] {}; 
 	                } 
 	 
 	                public void checkClientTrusted(X509Certificate[] chain, 
 	                                String authType) throws CertificateException { 
 	                } 
 	 
 	                public void checkServerTrusted(X509Certificate[] chain, 
 	                                String authType) throws CertificateException { 
 	                } 
 				}
 			}; 
 
 			URL url = new URL(connUrl);
 			SSLContext sslcontext = SSLContext.getInstance("TLS");
 			// http://stackoverflow.com/questions/995514/https-connection-android
 			sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
 			
 			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext
 					.getSocketFactory());
 			httpsConnection = (HttpsURLConnection) url.openConnection();
 			/*
 			POST /cgi-bin/login HTTP/1.1
 			Accept: *//*
 			Referer: https://wlan.sap.com/cgi-bin/login?cmd=login&mac=00:18:de:14:20:91&ip=192.168.143.135&essid=SAP-Guest&url=http%3A%2F%2Fwww%2Egoogle%2Ecom%2F
 			Accept-Language: en-gb
 			User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)
 			Content-Type: application/x-www-form-urlencoded
 			Accept-Encoding: gzip, deflate
 			Host: wlan.sap.com
 			Content-Length: 60
 			Connection: Keep-Alive
 			Cache-Control: no-cache
 			Cookie: CPsession=http%3A%2F%2Fwww%2Egoogle%2Ecom%2F%26ip%3D192%2E168%2E143%2E135
 			
 			BODY:
 			user=USER&password=PASSWORD&cmd=authenticate&Login=Log+In
 			*/
 			httpsConnection.setRequestMethod("POST");
 			httpsConnection.setDoInput(true);
 			httpsConnection.setDoOutput(true);
 			httpsConnection.setRequestProperty("Accept", "*/*");
 			httpsConnection.setRequestProperty("Accept-Language", "en-gb");
 			httpsConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
 			httpsConnection.setRequestProperty("Host", PreferencesFacade.getLocation(context).getConnectionHostName());
 			httpsConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		}
 		catch(Exception e)
 		{
 			LogManager.LogException(e, "ConnectHelper", "openConnectionToHTTPS()");
 		}
 		return httpsConnection;
 	}
 	//http://androidforums.com/android-applications/60650-send-data-https-android-application.html
 	//http://www.anddev.org/viewtopic.php?p=40035
 	//http://www.softwarepassion.com/android-series-get-post-and-multipart-post-requests/
 	//http://stackoverflow.com/questions/995514/https-connection-android
 
 	private void logInToWiFi(HttpsURLConnection httpsConnection){
 	    try {          
 	    	HttpClient httpclient = new DefaultHttpClient();
 	    	HttpPost httppost = new HttpPost(new URI("https://mobile-net.hp.com:8090/goform/HtmlLoginRequest"));                                           
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 	        nameValuePairs.add(new BasicNameValuePair("error_url", "/login_fail.html"));
 	        nameValuePairs.add(new BasicNameValuePair("success_url", "http://mobile-net.hp.com:8080/transport.asp"));
 	        nameValuePairs.add(new BasicNameValuePair("original_url", "http://www.google.com/"));
 	        nameValuePairs.add(new BasicNameValuePair("subscription_url", "https://mobile-net.hp.com:8090/subscribe.asp"));
 	        nameValuePairs.add(new BasicNameValuePair("valid_fields", "access_type username password"));
 	        nameValuePairs.add(new BasicNameValuePair("access_type", "login"));
 	        nameValuePairs.add(new BasicNameValuePair("username", loginData.getUser()));
 	        nameValuePairs.add(new BasicNameValuePair("password", loginData.getPass()));
 	        nameValuePairs.add(new BasicNameValuePair("login", "Proceed"));
 	        
 	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 	        HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier(){
 				@Override
 				public boolean verify(String string, SSLSession ssls) {
 					return true;
 				}
         	});
 	        HttpResponse response = httpclient.execute(httppost);
 	        String responseStr = response.toString();
 	        Log.v("Response:", responseStr);
 
 	    } catch (ClientProtocolException e) {
 	        e.printStackTrace();
 	        return;
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	        return;
 	    } catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private boolean isLoggedIn(HttpsURLConnection httpsConnection){
 		LogManager.LogFunctionCall("ConnectHelper", "isLoggedIn()");
 		boolean isLoggedIn = false;
 		Object contents;
 		try {
 			//int responseCode = httpsConnection.getResponseCode();
 			//String u = httpsConnection.getContentType();
 			contents = httpsConnection.getContent();
 			if (contents != null) {
 				InputStream is = (InputStream) contents;
 				StringBuffer buf = new StringBuffer();
 				int c;
 				while ((c = is.read()) != -1) {
 					buf.append((char) c);
 				}
 				String response = buf.toString();
 				if(response.contains("User Authenticated")){
 					// Logged in successfully
 					isLoggedIn = true;
 					LogManager.LogInfoMsg("ConnectHelper", "isLoggedIn()", "User \"" + 
 							this.loginData.getUser() + "\"authentication SUCCEEDED.");
 				} else {
 					LogManager.LogInfoMsg("ConnectHelper", "isLoggedIn()", "User \"" + 
 							this.loginData.getUser() + "\"authentication FAILED.");
 				}
 				
 			}
 			httpsConnection.disconnect();
 		} catch (IOException e) {
 			LogManager.LogException(e, "ConnectHelper", "isLoggedIn()");
 		}
 		return isLoggedIn;
 	}
 	
 	String getMacAddress() {
 		LogManager.LogFunctionCall("ConnectHelper", "getMacAddress()");
 		String mac = null;
 		if (wm != null) {
 			WifiInfo wi = wm.getConnectionInfo();
 			mac = wi.getMacAddress();
 		}
 
 		return mac;
 	}
 	
 	String getSSID() {
 		LogManager.LogFunctionCall("ConnectHelper", "getSSID()");
 		String ssid = null;
 
 		if (wm != null) {
			ssid = WifiUtil.getSSID(wm);
 		}
 
 		return ssid;
 	}
 	
 	boolean ifWifiEnabled() {
 		LogManager.LogFunctionCall("ConnectHelper", "ifWifiEnabled()");
 		boolean isEnabled = false;
 
 		if (wm != null) {
 			isEnabled = wm.isWifiEnabled();
 		}
 
 		return isEnabled;
 	}
 	
 	String getIPAddress() {
 		LogManager.LogFunctionCall("ConnectHelper", "getIPAddress()");	
 		String strIP = null;
 		int ip = -1;
 
 		if (wm != null) {
 			WifiInfo wi = wm.getConnectionInfo();
 			ip = wi.getIpAddress();
 			StringBuilder binIP = new StringBuilder(Integer.toBinaryString(ip));
 			int len = binIP.length();
 			for(int i = 0 ; i < 32 - len; ++i) {
 				binIP.insert(0, '0');
 			}
 			
 			StringBuilder stringIP = new StringBuilder();
 			
 			for (int i = 3; i >= 0; --i) {
 				stringIP.append(Integer.toString(Integer.parseInt(binIP.substring(i * 8, i * 8 + 8), 2)));
 				if (i > 0) {
 					stringIP.append('.');
 				}
 			}
 			strIP = stringIP.toString();
 		}	
 		return strIP;
 	}
 	
 	
 	/******
 	 * Statics
 	 */
 	
 	public static ConnectionStatus IsOnline(Context context)
 	{
 		LogManager.LogFunctionCall("ConnectHelper", "IsOnline()");
 		
 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);	
 		ConnectHelper connectHelper = new ConnectHelper(context);
 		boolean isOnline = connectHelper.isLoggedInToSAP();
 		boolean isCorrectWifi = connectHelper.isConnectedToCorrectWiFi();
 		boolean isWifiEnabled = wifiManager.isWifiEnabled();
 		
 		if (!isWifiEnabled)
 		{
 			return ConnectionStatus.WIFI_DISABLED;
 		}
 		else if (isOnline)
 		{
 			if (isCorrectWifi)
 				return ConnectionStatus.CONNECTED;
 			else
 				return ConnectionStatus.CONNECTED_UNKNOWN_WIFI;
 		}
 		else
 		{
 			return ConnectionStatus.NOT_CONNECTED;
 		}
 	}
 	  
 }
