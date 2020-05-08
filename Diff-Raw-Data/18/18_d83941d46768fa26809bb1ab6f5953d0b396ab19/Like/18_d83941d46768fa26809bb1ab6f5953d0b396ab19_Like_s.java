 package com.layercake.facebookwrapper;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.webkit.ConsoleMessage;
 import android.webkit.CookieSyncManager;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class Like extends Activity {
 	
 	public static String TAG = "FacebookWrapper:Like";
 	
 	private IFacebookPluginContainer containerInterface;
 	
 	private WebView webView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.webview);
 		
 		// Get client interface
 		Intent i = getIntent();
 		IBinder b = i.getIBinderExtra("parentBinder");
 		containerInterface = IFacebookPluginContainer.Stub.asInterface(b);
         
         webView = (WebView) findViewById(R.id.webView1);
         webView.setFilterTouchesWhenObscured(true);
         webView.setFilterTouchesWhenNotFullyVisible(true);
         
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.getSettings().getBuiltInZoomControls();
 		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
 		webView.setVerticalScrollBarEnabled(true);
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 		        view.loadUrl(url);
 		        return true;
 		    }
 			@Override
 			public void onPageFinished(WebView view, String url) {
 		        CookieSyncManager.getInstance().sync();
 		    }
 		});
 		webView.setWebChromeClient(new WebChromeClient() {
 			public boolean onConsoleMessage(ConsoleMessage cm) {
 				Log.d(TAG, cm.message() + " -- From line "
                          + cm.lineNumber() + " of "
                          + cm.sourceId() );
 				return true;
 			}
 		});
         
         if (containerInterface == null) {
         	Log.i(TAG, "No containerInterface!");
         	
         	// Load WebView with Facebook to let user log in
 			webView.loadUrl("https://www.facebook.com");
         	
         	return;
         }
         
         // Register child (own) interface
         IBinder ownInterface = new IRemoteFacebookPlugin.Stub() {
 
 			@Override
 			public void createLikePlugin(String appId, String url)
 					throws RemoteException {
 				
 				// Write HTML directly to WebView
 				String html = getFacebookLikeHtml(appId, url);
 				String mime = "text/html";
 				String encoding = "utf-8";
 				
 				// Loading something prevents Facebook from causing DOM Exception 18
				webView.loadDataWithBaseURL("http://layercake.cs.washington.edu", html, mime, encoding, null);
 				
 			}
 
 			@Override
 			public void createCommentsPlugin(String appId, String url)
 					throws RemoteException {
 				// TODO Auto-generated method stub
 				
 			}
 
         };
         try {
         	containerInterface.registerChildInterface(ownInterface);
         } catch (RemoteException e) {
         	Log.e(TAG, "Error trying to register child interface: " + e.getMessage());
         }
 	}
 	
 	private String getFacebookLikeHtml(String appId, String url) {
 		
		String html = "<html><head></head><body>\n"
						+ "<div id=\"fb-root\"></div>\n"
 						+ "<script>\n"
 						+ "window.fbAsyncInit = function() {\n"
 						+ "  // init the FB JS SDK\n"
 						+ "  FB.init({\n"
 						+ "    appId      : '" + appId + "', // App ID from the App Dashboard\n"
 						+ "    status     : true, // check the login status upon init?\n"
 						+ "    cookie     : true, // set sessions cookies to allow your server to access the session?\n"
 						+ "    xfbml      : true  // parse XFBML tags on this page?\n"
 						+ "  }); \n"
 						+ "};\n"
 						+ "  // Load the SDK's source Asynchronously\n"
 						+ "  // Note that the debug version is being actively developed and might\n"
 						+ "  // contain some type checks that are overly strict.\n"
 						+ "  // Please report such bugs using the bugs tool.\n"
 						+ "  (function(d, debug){\n"
 						+ "     var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];\n"
 						+ "     if (d.getElementById(id)) {return;}\n"
 						+ "     js = d.createElement('script'); js.id = id; js.async = true;\n"
 						+ "     js.src = 'http://connect.facebook.net/en_US/all' + (debug ? '/debug' : '') + '.js';\n"
 						+ "     ref.parentNode.insertBefore(js, ref);\n"
 						+ "   }(document, false));\n"
 						+ "</script>\n"
						+ "<div class=\"fb-like\" data-href=\"" + url + "\" data-send=\"false\" data-width=\"450\" data-show-faces=\"false\"></div>\n"
 						+ "</body></html>";
 		
 		return html;
 	}
 
 }
