 package hk.hku.cs.srli.monkeydemo.demo;
 
 import android.annotation.SuppressLint;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 import hk.hku.cs.srli.monkeydemo.R;
 
 public class WebViewFragment extends DemoFragmentBase {
     
     private static final String URL = "file:///android_asset/parallax.html";
     
     private WebView webView;
 
     @Override
     // JavaScript XSS warning is suppressed here because only internal JS files are loaded.
     @SuppressLint("SetJavaScriptEnabled")
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = super.onCreateView(inflater, container, savedInstanceState);
         
        // TODO (LeeThree): This is a hack to hide Action Bar programmatically.
        getActivity().getActionBar().hide();
        
         webView = (WebView) rootView.findViewById(R.id.webview);
         
         WebSettings webSettings = webView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         webSettings.setBuiltInZoomControls(true);
         
         webView.setWebViewClient(new WebViewClient());
         
         webView.loadUrl(URL);
         return rootView;
     }
     
     @Override
     protected int getLayoutResource() {
         return R.layout.fragment_webview;
     }
     
 }
