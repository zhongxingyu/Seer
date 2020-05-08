 package com.ladinc.showrss;
 
 //TODO: Fix imports
 import java.io.*;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.ladinc.showrss.utilities.HttpClientHelper;
 import com.ladinc.showrss.utilities.Utilities;
 
 import android.util.Log;
 
 public class LoginTask {
 	private static final String TAG = "LoginTask";
 	private final String userName, password;
 
 	public LoginTask(String userName, String password) {
 		this.userName = userName;
 		this.password = password;
 	}
 
 	public String attemptLogin() throws Exception {
 
 		String error = "Unknown Error";
 		Log.d(TAG, "Attempting to login as: " + this.userName);
 
 		// Should we throw an exception on failed logins?
		if (!validateUserName(this.userName)) {
 
 			error = "Invalid Login Name";
 			Log.d(TAG, error);
 			return error;
 		}
 
 		// TODO: Get this working
 		// String loginURL = getString(R.string.loginURL);
 
 		String loginURL = "http://showrss.karmorra.info/?cs=login";
 
 		try {
 			// Get the HttpClient and Post Header
 			HttpClient httpclient = HttpClientHelper.getHttpClient();
 
 			HttpPost httppost = new HttpPost(loginURL);
 
 			// Add your data
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 			nameValuePairs.add(new BasicNameValuePair("username", this.userName));
 			nameValuePairs.add(new BasicNameValuePair("password", this.password));
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			// Execute HTTP Post Request and get response
 			HttpResponse response = httpclient.execute(httppost);
 
 			// Header[] y = response.getAllHeaders();
 
 			Header[] locations = response.getHeaders("location");
 
 			String locationString = locations[0].toString();
 
 			System.out.println(locationString);
 			if (locationString.contains("err=")) {
 				if (locationString.contains("err=password")) {
 					error = "Password is incorrect";
 				} else if (locationString.contains("err=user")) {
 					error = "User Name does not exist";
 				}
 
 				Log.d(TAG, error);
 				return error;
 
 			}
 
 			if ("" != Utilities.getUserName()) {
 				return null;
 			}
 
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return error;
 	}
 
 	// ShowRSS rules on valid user names:
 	// "The username should be a word without strange symbols, between 2 and 12 characters, and not be in use by another user."
 	private boolean validateUserName(String user) {
 		// "between 2 and 12" sounds like 3-11 but im airing on the side of
 		// caution sayings its 2-12
 		return (user.length() >= 2 && user.length() <= 12);
 	}
 
 }
