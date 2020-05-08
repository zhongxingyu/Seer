 package com.codingspezis.android.metalonly.player.utils.jsonapi;
 
 import org.springframework.http.client.*;
 import org.springframework.web.client.*;
 
 import android.net.*;
 import android.util.*;
 
 import com.googlecode.androidannotations.annotations.*;
 import com.googlecode.androidannotations.annotations.rest.*;
 import com.googlecode.androidannotations.api.*;
 
 @EBean(scope = Scope.Singleton)
 public class MetalOnlyAPIWrapper implements MetalOnlyAPI {
 
 	private static final int TIME_OUT = 30 * 1000;
 
 	private static final String TAG = MetalOnlyAPIWrapper.class.getSimpleName();
 
 	@RestService
 	MetalOnlyAPI api;
 
 	@SystemService
 	ConnectivityManager cm;
 
 	@AfterInject
	void adaptSettings() {
		changeTimeout();
		disableKeepAlive();
	}

	private void changeTimeout() {
		final ClientHttpRequestFactory requestFactory = api.getRestTemplate()
 				.getRequestFactory();
 		if (requestFactory instanceof SimpleClientHttpRequestFactory) {
 			Log.d("HTTP", "HttpUrlConnection is used");
 			((SimpleClientHttpRequestFactory) requestFactory)
 					.setConnectTimeout(TIME_OUT);
 			((SimpleClientHttpRequestFactory) requestFactory)
 					.setReadTimeout(TIME_OUT);
 		}
 		else if (requestFactory instanceof HttpComponentsClientHttpRequestFactory) {
 			Log.d("HTTP", "HttpClient is used");
 			((HttpComponentsClientHttpRequestFactory) requestFactory)
 					.setReadTimeout(TIME_OUT);
 			((HttpComponentsClientHttpRequestFactory) requestFactory)
 					.setConnectTimeout(TIME_OUT);
 		}
 	}
 
	private void disableKeepAlive() {
		System.setProperty("http.keepAlive", "false");
	}

 	@Override
 	public Stats getStats() {
 		checkConnectivity();
 		return api.getStats();
 	}
 
 	@Override
 	public Plan getPlan() {
 		checkConnectivity();
 		return api.getPlan();
 	}
 
 	@Override
 	public PlanWithStats getPlanWithStats() {
 		checkConnectivity();
 		return api.getPlanWithStats();
 	}
 
 	private void checkConnectivity() {
 		if (!hasConnection()) {
 			throw new NoInternetException();
 		}
 	}
 
 	/**
 	 * Checks if the device has Internet connection.
 	 * 
 	 * @return <code>true</code> if the phone is connected to the Internet.
 	 */
 	public boolean hasConnection() {
		final NetworkInfo wifiNetwork = cm
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if (wifiNetwork != null && wifiNetwork.isConnected()) {
 			return true;
 		}
 
		final NetworkInfo mobileNetwork = cm
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 		if (mobileNetwork != null && mobileNetwork.isConnected()) {
 			return true;
 		}
 
		final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 		if (activeNetwork != null && activeNetwork.isConnected()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Do not use.
 	 */
 	@Override
 	public RestTemplate getRestTemplate() {
 		Log.d(TAG,
 				"getRestTemplate in wrapper was called. This should not happen.");
 		return null;
 	}
 
 	/**
 	 * Do not use.
 	 */
 	@Override
	public void setRestTemplate(final RestTemplate restTemplate) {
 		Log.d(TAG,
 				"setRestTemplate in wrapper was called. This should not happen.");
 	}
 }
