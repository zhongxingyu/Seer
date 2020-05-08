 /**
  * Copyright 2011 Mark Wyszomierski
  */
 package com.foursquare.android.oauth;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * https://developer.foursquare.com/docs/oauth.html
  * https://foursquare.com/oauth/
  * 
  * @date May 17, 2011
  * @author Mark Wyszomierski (markww@gmail.com)
  */
 public class ActivityWebView extends Activity 
 {
     private static final String TAG = "ActivityWebView";
 	
     /**
      * Get these values after registering your oauth app at: https://foursquare.com/oauth/
      */
     public static final String CALLBACK_URL = "https://www.p1.com/callback1";
     public static final String CLIENT_ID = "WA3WC32ZFC0ZNUFEIZLYQBJSVKTVRBCS10SAR1A4GHQ3BNI5";
 	public static String mytoken;
 	
     public static String getAccessToken() {
         return mytoken;
     }
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_webview);
 
         Button btn = (Button)findViewById(R.id.button2);
         btn.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(ActivityWebView.this, ActivityWebView2.class);
                 startActivity(intent);
             }
         });
         
         String url =
             "https://foursquare.com/oauth2/authenticate" + 
                 "?client_id=" + CLIENT_ID + 
                 "&response_type=token" + 
                 "&redirect_uri=" + CALLBACK_URL;
         
         // If authentication works, we'll get redirected to a url with a pattern like:  
         //
         //    http://YOUR_REGISTERED_REDIRECT_URI/#access_token=ACCESS_TOKEN
         //
         // We can override onPageStarted() in the web client and grab the token out.
 
         WebView webview = (WebView)findViewById(R.id.webview);
         webview.getSettings().setJavaScriptEnabled(true);
         webview.setWebViewClient(new WebViewClient() {
             public void onPageStarted(WebView view, String url, Bitmap favicon) {
                 String fragment = "#access_token=";
                 int start = url.indexOf(fragment);
                 if (start > -1) {
                     // You can use the accessToken for api calls now.
                     String accessToken = url.substring(start + fragment.length(), url.length());
         			
                     Log.v(TAG, "OAuth complete, token: [" + accessToken + "].");
                 	
                     Toast.makeText(ActivityWebView.this, "Token: " + accessToken, Toast.LENGTH_SHORT).show();
                     mytoken = accessToken;
                 }
             }
         });
         webview.loadUrl(url);
     }
 }
