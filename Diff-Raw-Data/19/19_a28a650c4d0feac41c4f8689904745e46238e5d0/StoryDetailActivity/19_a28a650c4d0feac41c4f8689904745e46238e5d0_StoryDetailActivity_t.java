 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2011, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
  File:   StoryDetailActivity.java
  Author: Lilli Szafranski - lilli@janrain.com
  Date:   April 22, 2011
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.quickshare;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Config;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 
 public class StoryDetailActivity extends Activity implements View.OnClickListener {
     private static final String TAG = StoryDetailActivity.class.getSimpleName();
 
     private FeedData mFeedData;
     private Story mStory;
 
     private Button mShareStory;
     private WebView mWebview;
 
     public void onCreate(Bundle savedInstanceState) {
        if (Config.LOGD)
            Log.d(TAG, "[onCreate] creating instance of StoryDetailActivity");

         super.onCreate(savedInstanceState);
         setContentView(R.layout.story_detail_webview);
 
        if (Config.LOGD)
            Log.d(TAG, "[onCreate] getting the FeedData instance");

         mFeedData = FeedData.getInstance(this);
 
        if (Config.LOGD)
            Log.d(TAG, "[onCreate] getting the share button resource");

         mShareStory = (Button)findViewById(R.id.share_button);
         mShareStory.setOnClickListener(this);
 
        if (Config.LOGD)
            Log.d(TAG, "[onCreate] getting the webview resource");

         mWebview = (WebView)findViewById(R.id.story_webview);
         mWebview.setWebViewClient(mWebviewClient);
 
        if (Config.LOGD)
            Log.d(TAG, "[onCreate] url: loading the current story");

         loadCurrentStory();
     }
 
     public void loadCurrentStory() {
         mStory = mFeedData.getCurrentStory();
 
         String styleCommon = getResources().getString(R.string.html_style_sheet_common);
         String stylePhone = getString(R.string.html_style_sheet_phone);
 
         String htmlString =
                 "<html>" +
                     "<head>" +
                         "<style type=\"text/css\">" +
                         styleCommon + // TODO: When adding support for tablet, change this to be device dependent
                         stylePhone +
                         "</style>" +
                     "</head>" +
                     "<body>" +
                         "<div class=\"main\">" +
                             "<div class=\"title\">" + mStory.getTitle() + "</div>" +
                             "<div class=\"date\">" + mStory.getDate() + "</div>" +
                             mStory.getDescription() +
                         "</div>" +
                     "</body>" +
                 "</html>";
 
         if (Config.LOGD)
             Log.d(TAG, "[loadCurrentStory] html: " + htmlString);
 
         mWebview.loadDataWithBaseURL("http://www.janrain.com/blogs/", htmlString, "text/html", "UTF-8", "");
     }
 
     public void onClick(View view) {
         mFeedData.shareCurrentStory();
     }
 
     private void openNewWebViewToUrl(String url) {
         mFeedData.setUrlToBeLoaded(url);
         this.startActivity(new Intent(this, DeeperWebViewActivity.class));
     }
 
     private WebViewClient mWebviewClient = new WebViewClient(){
         private final String TAG = this.getClass().getSimpleName();
 
         @Override
         public boolean shouldOverrideUrlLoading(WebView webView, String url) {
             if (Config.LOGD)
                 Log.d(TAG, "[shouldOverrideUrlLoading] url: " + url);
 
             openNewWebViewToUrl(url);
             return true;
         }
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             if (Config.LOGD)
                 Log.d(TAG, "[onPageStarted] url: " + url);
 
             super.onPageStarted(view, url, favicon);
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             if (Config.LOGD)
                 Log.d(TAG, "[onPageFinished] url: " + url);
 
             super.onPageFinished(view, url);
         }
 
         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String url) {
             super.onReceivedError(view, errorCode, description, url);
             Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                 + " | url: " + url);
         }
     };
 }
