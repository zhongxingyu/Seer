 package com.parworks.mars.view.technology;
 
 import android.annotation.SuppressLint;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebSettings.PluginState;
 import android.webkit.WebView;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.parworks.mars.MarsMenuFragment;
 import com.parworks.mars.R;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 @SuppressLint("ValidFragment")
 public class TechnologyFragment extends MarsMenuFragment {
 	
 	private SlidingFragmentActivity mContext;
 	
 	public TechnologyFragment() {
 		super();
 	}
 
 	public TechnologyFragment(SlidingFragmentActivity context) {
 		super();
 		mContext = context;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_technology, null);
 		WebView webView = (WebView) v.findViewById(R.id.technologyWebView);
		webView.setWebViewClient(new TechnologyWebViewClient());
 		// how to enable the webview plugin changed in API 8
 		webView.getSettings().setPluginsEnabled(true); 
 		webView.getSettings().setJavaScriptEnabled(true); 
 		webView.getSettings().setAllowFileAccess(true);
 		
		webView.getSettings().setPluginState(PluginState.ON);
 		webView.loadUrl("file:///android_asset/technology.html");
 		return v;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		ImageButton button = (ImageButton) mContext.getSupportActionBar().getCustomView().findViewById(R.id.rightBarButton);
 		button.setBackgroundResource(R.drawable.ic_bar_item_intro);		
 	}
 	
 	public void rightBarButtonClicked(View v) {
 		super.rightBarButtonClicked(v);
 		Toast.makeText(mContext, "Insert Intro", Toast.LENGTH_SHORT).show();
 	}
 }
