 /*
 Copyright 2013 Zirconi
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 
 package com.zirconi.huaxiaclient;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 
 import android.app.Application;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class SharedApplication extends Application {
 
 	private static final String TAG = SharedApplication.class.getSimpleName();
 	private static final int SOCKET_DELAY = 1000 * 6;
 	private static final int CONN_DELAY = 1000 * 10;
 	public static final String HTTP_TEL_ADDR = "http://59.173.2.28/";
 	public static final String HTTP_CER_ADDR = "http://210.42.141.4/";
 	public static final String HTTP_DEF_PAGE = "default3.aspx";
 	public static final String SCORE = "xscj.aspx";
 	public static final String INFO_PAGE = "xstop.aspx";
 	public static final String STU_NUM = "?xh=";
 	public static final String QUERY_SCORE_PAGE = SCORE + STU_NUM;
 	public static final String TEL_PAGE = HTTP_TEL_ADDR + HTTP_DEF_PAGE;
 	public static final String CER_PAGE = HTTP_CER_ADDR + HTTP_DEF_PAGE;
 
 	public static final String PASSWORD = "tbPSW";
 	public static final String USERNAME = "tbYHM";
 	public static final String IDENTITY = "ddlSF";
 	public static final String VIEWSTATE = "__VIEWSTATE";
 	public static final String IMG_X = "imgDL.x";
 	public static final String IMG_Y = "imgDL.y";
	public static final String VIEWSTATE_KEY = "dDwxOTA0NTQ3NDgwO3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDg+O2k8MTM+O2k8MTU+Oz47bDx0PHA8O3A8bDxvbmNsaWNrOz47bDx3aW5kb3cuY2xvc2UoKVw7Oz4+Pjs7Pjt0PHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs+O3Q8O2w8aTwwPjs+O2w8dDw7bDxpPDE+Oz47bDx0PHA8bDxpbm5lcmh0bWw7PjtsPFw8dGFibGUgd2lkdGg9JzEwMCUnIGJvcmRlcj0nMCcgY2VsbHNwYWNpbmc9JzAnIGNlbGxwYWRkaW5nPScwJ1w+XDwvdGFibGVcPlw8c2NyaXB0IHR5cGU9J3RleHQvamF2YXNjcmlwdCdcPnZhciBvTWFycXVlZSA9IGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKCdtcScpXDt2YXIgaUxpbmVIZWlnaHQgPSAxNlw7dmFyIGlMaW5lQ291bnQgPSAwXDt2YXIgaVNjcm9sbEFtb3VudCA9IDFcOyBmdW5jdGlvbiBydW4oKXtvTWFycXVlZS5zY3JvbGxUb3AgKz0gaVNjcm9sbEFtb3VudFw7aWYgKCBvTWFycXVlZS5zY3JvbGxUb3AgPT0gaUxpbmVDb3VudCAqIGlMaW5lSGVpZ2h0ICl7b01hcnF1ZWUuc2Nyb2xsVG9wID0gMFw7fWlmICggb01hcnF1ZWUuc2Nyb2xsVG9wICUgaUxpbmVIZWlnaHQgPT0gMCApIHt3aW5kb3cuc2V0VGltZW91dCggJ3J1bigpJywgMjAwMCApXDt9IGVsc2Uge3dpbmRvdy5zZXRUaW1lb3V0KCAncnVuKCknLCA1MCApXDt9fW9NYXJxdWVlLmlubmVySFRNTCArPSBvTWFycXVlZS5pbm5lckhUTUxcO3dpbmRvdy5zZXRUaW1lb3V0KCAncnVuKCknLCAyMDAwIClcO1w8L3NjcmlwdFw+Oz4+Ozs+Oz4+Oz4+Oz4+Oz4+O2w8aW1nREw7aW1nVEM7aW1nUU1NOz4+hxD4/jdz1BUmc1xl7POcfROPk9U=";
 
 	public static HttpClient client;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		initHttpClient();
 		Log.i(TAG, "ONCREATE");
 	}
 
 	@Override
 	public void onTerminate() {
 		super.onTerminate();
 	}
 
 
 	public static boolean networkIsAvailable(Context context) {
 		ConnectivityManager cManager = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo info = cManager.getActiveNetworkInfo();
 		if (info == null) {
 			return false;
 		}
 		if (info.isConnected()) {
 			return true;
 		}
 		return false;
 	}
 
 
 	private void initHttpClient() {
 		SharedApplication.client = new DefaultHttpClient();
 		SharedApplication.client.getParams().setIntParameter(
 				HttpConnectionParams.SO_TIMEOUT, SOCKET_DELAY);
 		SharedApplication.client.getParams().setIntParameter(
 				HttpConnectionParams.CONNECTION_TIMEOUT, CONN_DELAY);
 	}
 }
