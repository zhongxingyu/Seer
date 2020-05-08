 package com.example.day3;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 public class WebViewActivity extends Activity {
 	WebView view;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_web_view);
         
         view = (WebView) findViewById(R.id.webView1);
         WebSettings settings = view.getSettings();
         settings.setJavaScriptEnabled(true);
         
         view.setWebViewClient(new WebViewClient());
         view.addJavascriptInterface(new JavaScriptInterface(), "Brainmatics");
         
 //        view.loadUrl("http://saungandroid.com");
 //        view.loadUrl("http://detik.com");
         
         String html = "<body>" +
        				"<img src='file:///android_asset/icon.png'/>" +
         				"<p>Hello <b>World</b></p>" +
         				"<input type='button' value='Say Hello' onClick=showAndroidToast()></input>" +
         				"<script type='text/javascript'>" +
         					"function showAndroidToast() {" +
         						"Brainmatics.testJs('Hello');" +
         					"}" +
         				"</script>" +
         			"</body>";
         view.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
     }
 
     class JavaScriptInterface {
     	public void testJs(String text) {
     		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
     	}
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_web_view, menu);
         return true;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	if(keyCode == KeyEvent.KEYCODE_BACK && view.canGoBack()) {
     		view.goBack();
     		return true;
     	}
     	
     	return super.onKeyDown(keyCode, event);
     }
 }
