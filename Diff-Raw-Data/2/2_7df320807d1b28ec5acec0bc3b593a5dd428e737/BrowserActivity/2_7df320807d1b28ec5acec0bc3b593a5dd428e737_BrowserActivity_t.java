 package org.sgnn7.ourobo;
 
 import org.sgnn7.ourobo.eventing.IChangeEventListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.ZoomDensity;
 import android.webkit.WebStorage;
 import android.webkit.WebView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 public class BrowserActivity extends Activity {
 	public static final String URL_PARAMETER_KEY = "image.location";
 
 	private WebView webView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.browser_page);
 
 		final RelativeLayout browserLayout = (RelativeLayout) findViewById(R.id.main_browser_view);
 
 		webView = new WebView(this);
 		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // Hack for faulty APIs
 
 		browserLayout.addView(webView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
 		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_meter);
 
 		EventingWebViewClient client = new EventingWebViewClient(this);
 		client.setPageLoadedListener(new IChangeEventListener() {
 			public void handle() {
 				progressBar.setVisibility(View.INVISIBLE);
 			}
 		});
 
 		client.setPageStartedListener(new IChangeEventListener() {
 			public void handle() {
 				progressBar.setVisibility(View.VISIBLE);
 			}
 		});
 
 		client.setErrorOccuredListener(new IChangeEventListener() {
 			public void handle() {
 				webView.setBackgroundColor(Color.WHITE);
 			}
 		});
 
 		webView.setWebChromeClient(new WebChromeClient() {
 			@Override
 			public void onProgressChanged(WebView view, int progress) {
 				progressBar.setProgress(progress);
 			}
 
 			@Override
 			public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
 					WebStorage.QuotaUpdater quotaUpdater) {
 				quotaUpdater.updateQuota(spaceNeeded * 2);
 			}
 		});
 
 		WebSettings webSettings = webView.getSettings();
 		webSettings.setBuiltInZoomControls(true);
 		webSettings.setJavaScriptEnabled(true);
 		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
 		webSettings.setLoadsImagesAutomatically(true);
 		webSettings.setPluginsEnabled(true);
 		webSettings.setSupportZoom(true);
 		webSettings.setDefaultZoom(ZoomDensity.FAR);
 		webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
 
 		webSettings.setAllowFileAccess(true);
 		webSettings.setDomStorageEnabled(true);
 		webSettings.setAppCacheMaxSize(16 * 1024 * 1024);
 		webSettings.setAppCacheEnabled(true);
 		webSettings.setAppCachePath("/data/data/org.sgnn7.ourobo/cache");
 		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
 
 		webView.setWebViewClient(client);
 		webView.setInitialScale(1);
 
 		loadIntentUrl();
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		loadIntentUrl();
 	}
 
 	private void loadIntentUrl() {
 		webView.clearHistory();
 		webView.setBackgroundColor(Color.BLACK);
 
 		webView.loadUrl(getIntent().getStringExtra(URL_PARAMETER_KEY));
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (webView.canGoBack()) {
 			webView.goBack();
 		} else {
			webView.stopLoading();
			webView.loadUrl("about:blank");
 			super.onBackPressed();
 		}
 	}
 }
