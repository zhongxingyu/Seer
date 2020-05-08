 /*
  * Copyright 2012 Feedlr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.chalmers.feedlr.activity;
 
 import com.chalmers.feedlr.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 /**
 * Class description
 * 
  * @author Olle Werme
  */
 
 public class TwitterWebActivity extends Activity {
 
 	private static final String CALLBACK_URL = "feedlr://twitter";
 
 	private Intent intent;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.twitter_webview);
 
 		intent = getIntent();
 		String url = (String) intent.getExtras().get("URL");
 		WebView webView = (WebView) findViewById(R.id.webview);
 
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				if (url.contains(CALLBACK_URL)) {
 					Uri uri = Uri.parse(url);
 					String oauthVerifier = uri
 							.getQueryParameter("oauth_verifier");
 					intent.putExtra("oauth_verifier", oauthVerifier);
 					setResult(RESULT_OK, intent);
 					finish();
 					return true;
 				}
 				return false;
 			}
 		});
 		webView.loadUrl(url);
 	}
 }
