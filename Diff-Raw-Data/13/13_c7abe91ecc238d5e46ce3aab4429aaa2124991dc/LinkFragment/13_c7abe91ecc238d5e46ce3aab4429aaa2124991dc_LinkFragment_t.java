 package com.btmura.android.reddit;
 
 import android.app.Fragment;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.PluginState;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ProgressBar;
 
 public class LinkFragment extends Fragment {
 	
 	private static final String ARG_THING = "thing";
 
 	private Entity thing;
 
 	private WebView webView;
 	private ProgressBar progress;
 	
 	public static LinkFragment newInstance(Entity thing) {
 		LinkFragment frag = new LinkFragment();
 		Bundle b = new Bundle(1);
 		b.putParcelable(ARG_THING, thing);
 		frag.setArguments(b);
 		return frag;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		thing = getArguments().getParcelable(ARG_THING);
 	}
 	
 	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.link, container, false);
 		webView = (WebView) view.findViewById(R.id.link);
 		progress = (ProgressBar) view.findViewById(R.id.progress);
 		setupWebView(webView);
 		return view;
 	}
 	
 	private void setupWebView(WebView webView) {
 		WebSettings settings = webView.getSettings();
 		settings.setBuiltInZoomControls(true);
 		settings.setDisplayZoomControls(false);
 		settings.setJavaScriptEnabled(true);
 		settings.setLoadWithOverviewMode(true);
 		settings.setSupportZoom(true);
 		settings.setPluginState(PluginState.ON_DEMAND);
 		settings.setUseWideViewPort(true);	
 		
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				super.onPageStarted(view, url, favicon);
 				progress.setVisibility(View.VISIBLE);
 			}
 			
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				super.onPageFinished(view, url);
 				progress.setVisibility(View.GONE);
 			}
 		});
 		
 		webView.setWebChromeClient(new WebChromeClient() {
 			@Override
 			public void onProgressChanged(WebView view, int newProgress) {
 				super.onProgressChanged(view, newProgress);
 				progress.setProgress(newProgress);
 			}
 		});
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		webView.loadUrl(thing.url);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		webView.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		webView.onPause();
 	}
 	
 	@Override
	public void onDestroyView() {
		super.onDestroyView();
 		webView.destroy();
		webView = null;
		progress = null;
 	}
 }
