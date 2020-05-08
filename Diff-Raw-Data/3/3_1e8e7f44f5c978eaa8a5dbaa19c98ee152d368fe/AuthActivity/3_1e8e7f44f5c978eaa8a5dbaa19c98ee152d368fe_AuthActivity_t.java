 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.ac;
 
 import android.accounts.AccountAuthenticatorActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.net.http.SslError;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import eu.trentorise.smartcampus.ac.model.UserData;
 import eu.trentorise.smartcampus.ac.network.RemoteConnector;
 
 /**
  * Abstract android activity to handle the authentication interactions. 
  * Defines an embedded Web browser (WebView) where the authentication interactions
  * take place. Upon result obtained the token is retrieved from the WebView context.
  * The result is passed to the {@link AuthListener} instance that the concrete subclasses
  * should define.
  * @author raman
  *
  */
 public abstract class AuthActivity extends AccountAuthenticatorActivity {
 
     static final FrameLayout.LayoutParams FILL =
             new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                              ViewGroup.LayoutParams.FILL_PARENT); 
     protected WebView mWebView;
     private ProgressDialog mSpinner; 
     private ImageView mCrossImage; 
     private FrameLayout mContent;
     private AuthListener authListener = getAuthListener();
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setUp();
     }
     
     protected void setUp() {
         mSpinner = new ProgressDialog(this);
         mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
         mSpinner.setMessage("Loading..."); 
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
         mContent = new FrameLayout(this); 
 
         createCrossImage(); 
         int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
         setUpWebView(crossWidth / 2); 
         
         mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)); 
         
 //        requestWindowFeature(Window.FEATURE_PROGRESS);
 //        setContentView(R.layout.web);
 //        mWebView = (WebView) findViewById(R.id.webview);
 //        mWebView.getSettings().setJavaScriptEnabled(true);
 //        mWebView.setVisibility(View.VISIBLE);
 //        startWebView();
     }
 
     private void setUpWebView(int margin) {
         LinearLayout webViewContainer = new LinearLayout(this);
         mWebView = new WebView(this);
         mWebView.setVerticalScrollBarEnabled(false);
         mWebView.setHorizontalScrollBarEnabled(false);
         mWebView.getSettings().setJavaScriptEnabled(true);
        CookieSyncManager.createInstance(getApplicationContext());
         CookieManager cookieManager = CookieManager.getInstance(); 
         cookieManager.removeAllCookie();
         
         startWebView();
         mWebView.setLayoutParams(FILL);
         mWebView.setVisibility(View.INVISIBLE);
         
         webViewContainer.setPadding(margin, margin, margin, margin);
         webViewContainer.addView(mWebView);
         mContent.addView(webViewContainer);
     } 
     
     @Override
     public void onBackPressed() {
     	super.onBackPressed();
     	authListener.onAuthCancelled();
 
     }
     
     private void createCrossImage() {
         mCrossImage = new ImageView(this);
         // Dismiss the dialog when user click on the 'x'
         mCrossImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             	authListener.onAuthCancelled();
             }
         });
         Drawable crossDrawable = getResources().getDrawable(R.drawable.close);
         mCrossImage.setImageDrawable(crossDrawable);
         /* 'x' should not be visible while webview is loading
          * make it visible only after webview has fully loaded
         */
         mCrossImage.setVisibility(View.INVISIBLE);
     } 
 
 	private void startWebView() {
 		mWebView.setWebViewClient(new AuthWebViewClient());
         Intent intent = getIntent();
 		if (intent.getData() != null) {
 			String url = intent.getDataString();
 			if (intent.getStringExtra(Constants.KEY_AUTHORITY) != null && !intent.getStringExtra(Constants.KEY_AUTHORITY).equals(Constants.AUTHORITY_DEFAULT)) {
 				url += (url.endsWith("/")?intent.getStringExtra(Constants.KEY_AUTHORITY):"/"+intent.getStringExtra(Constants.KEY_AUTHORITY));
 			}
 		  mWebView.loadUrl(url);
 		}
 	}
 
 	protected abstract AuthListener getAuthListener();
 
 	public class AuthWebViewClient extends WebViewClient {
 		
 		public AuthWebViewClient() {
 			super();
 		}
 
 //		@Override
 //		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 //			verifyUrl(url);
 //			return false;
 //		}
 
 		private boolean verifyUrl(String url) throws NameNotFoundException {
 			if (url.startsWith(Constants.getOkUrl(AuthActivity.this))){
 				String fragment = Uri.parse(url).getFragment();
 				if (fragment != null) {
 					new ValidateAsyncTask().execute(fragment);
 				} else {
 					authListener.onAuthFailed("No token provided");
 				}
 				return true;
 			} 
 			if (url.startsWith(Constants.getCancelUrl(AuthActivity.this))) {
 				authListener.onAuthCancelled();
 				return true;
 			}
 			return false;
 		}
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             super.onPageStarted(view, url, favicon);
             mSpinner.show();
         }  
 		
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			try {
 				verifyUrl(url);
 			} catch (NameNotFoundException e) {
 				authListener.onAuthFailed("No auth url specified.");
 			}
 			super.onPageFinished(view, url);
             mSpinner.dismiss();
             /* 
              * Once webview is fully loaded, set the mContent background to be transparent
              * and make visible the 'x' image. 
              */
             mContent.setBackgroundColor(Color.TRANSPARENT);
             mWebView.setVisibility(View.VISIBLE);
             mCrossImage.setVisibility(View.VISIBLE);
         }
 
 		@Override
 		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) 
 		{
 			handler.proceed();
 		}
 	}
 
 	private class ValidateAsyncTask extends AsyncTask<String, Void, UserData> {
 
 		@Override
 		protected UserData doInBackground(String... params) {
 			try {
 				return RemoteConnector.validateAccessCode(Constants.getAuthUrl(AuthActivity.this), params[0]);
 			} catch (NameNotFoundException e) {
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(UserData user) {
 			if (user == null || user.getToken() == null) {
 				authListener.onAuthFailed("Token validation failed");
 			} else {
 				authListener.onTokenAcquired(user);
 			}
 		}
 		
 		
 	}
 }
