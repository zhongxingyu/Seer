 package com.smp.funwithmusic.activities;
 
 import com.smp.funwithmusic.R;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 import static com.smp.funwithmusic.utilities.Constants.*;
 
 public class WebActivity extends Activity
 {
 	WebView webView;
 	String url;
 
 	@Override
 	protected void onPause()
 	{
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
 		setContentView(R.layout.activity_web);
 
 		webView = (WebView) findViewById(R.id.webview);
 
 		Intent intent = getIntent();
 		url = intent.getStringExtra(WEB_URL);
 
 		webView.getSettings().setJavaScriptEnabled(true);
		//webView.getSettings().setBuiltInZoomControls(true);
 		
 		final Activity activity = this;
 		webView.setWebChromeClient(new WebChromeClient()
 		{
 			public void onProgressChanged(WebView view, int progress)
 			{
 				// Activities and WebViews measure progress with different
 				// scales.
 				// The progress meter will automatically disappear when we reach
 				// 100%
 				activity.setProgress(progress * 100);
 			}
 		});
 		webView.setWebViewClient(new WebViewClient()
 		{
 			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
 			{
 				Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		webView.loadUrl(url);
 
 	}
 
 }
