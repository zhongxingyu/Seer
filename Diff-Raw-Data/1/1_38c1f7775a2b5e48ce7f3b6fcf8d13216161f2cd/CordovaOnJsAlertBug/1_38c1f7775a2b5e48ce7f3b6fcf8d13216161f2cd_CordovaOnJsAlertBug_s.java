 /*
        Licensed to the Apache Software Foundation (ASF) under one
        or more contributor license agreements.  See the NOTICE file
        distributed with this work for additional information
        regarding copyright ownership.  The ASF licenses this file
        to you under the Apache License, Version 2.0 (the
        "License"); you may not use this file except in compliance
        with the License.  You may obtain a copy of the License at
 
          http://www.apache.org/licenses/LICENSE-2.0
 
        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.
  */
 
 package com.example.cordovajsalertbug;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.JsResult;
 import android.webkit.WebView;
 
 import org.apache.cordova.*;
 import org.apache.cordova.api.CordovaInterface;
 
 public class CordovaOnJsAlertBug extends DroidGap
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html");
     }
     
     @Override
     public void init() {
     	Log.e(TAG, "init()");
         CordovaWebView webView = new CordovaWebView(CordovaOnJsAlertBug.this);
         CordovaWebViewClient webViewClient;
         if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
         {
             webViewClient = new CordovaWebViewClient(this, webView);
         }
         else
         {
             webViewClient = new IceCreamCordovaWebViewClient(this, webView);
         }
         this.init(webView, webViewClient, new MyCordovaChromeClient(this, webView));
     }
     
     private class MyCordovaChromeClient extends CordovaChromeClient{
     	private CordovaInterface cordova;
 		public MyCordovaChromeClient(CordovaInterface ctx, CordovaWebView app) {
 			super(ctx, app);
 			this.cordova = ctx;
 		}
     	
 		
 		@Override
 		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
 			if(cordova.getActivity().isFinishing()){
 				Log.w(TAG, "Trying to alert while activity is finishing!! -> ignore");
 				return true;
 			}
 			
 			return super.onJsAlert(view, url, message, result);
 		}
     }
 }
 
