 /*
  * Copyright 2012 Alexander Orlov <alexander.orlov@loxal.net>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package loxal.lom;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 public class WebAppWrapper extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 //        final String url = "file:///android_asset/main/main.html";
         final String url = "http://main.epvin.com/?channel=app";
         final Activity activity = this;
         final WebView webView = new WebView(this);
         webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
         webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
         webView.getSettings().setAppCacheEnabled(true);
         webView.getSettings().setBuiltInZoomControls(true);
         getWindow().requestFeature(Window.FEATURE_PROGRESS);
//        webView.setInitialScale(1);
 
         webView.setWebChromeClient(new WebChromeClient() {
             public void onProgressChanged(WebView webView, int progress) {
                 setProgress(progress * 100);
             }
         });
 
         webView.setWebViewClient(new WebViewClient() {
             public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                 Toast.makeText(activity, "Oops, this shouldn't happen! " + description,
                         Toast.LENGTH_SHORT).show();
             }
 
             @Override
             public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                 webView.loadUrl(url);
                 return true;
             }
         });
 
         webView.loadUrl(url);
         setContentView(webView);
     }
 }
