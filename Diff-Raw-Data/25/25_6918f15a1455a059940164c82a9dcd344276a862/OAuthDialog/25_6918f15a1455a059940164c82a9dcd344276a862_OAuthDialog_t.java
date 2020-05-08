 package com.expelabs.social.view;
 
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.webkit.CacheManager;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.LinearLayout;
 import com.expelabs.social.webclient.AuthClientInterface;
 import com.expelabs.tips.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Вадим
  * Date: 24.05.13
  * Time: 14:47
  * To change this template use File | Settings | File Templates.
  */
 public class OAuthDialog extends Dialog {
 
     private WebView webView;
     private WebViewClient webClient;
 
     public OAuthDialog(Context context, WebViewClient client) {
         super(context, android.R.style.Theme_NoTitleBar_Fullscreen);
 
         webView = new WebView(context);
	/*	webView.clearCache(true);
 		webView.clearSslPreferences();
 		CookieSyncManager.createInstance(context);
 		CookieManager cookieManager = CookieManager.getInstance();
 		cookieManager.removeAllCookie();
      */
		webView.setWebViewClient(client);
         webClient = client;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(webView);
     }
 
     @Override
     public void show() {
         super.show();
         webView.loadUrl(((AuthClientInterface) webClient).loginUrl());
     }
 }
 
 
 
