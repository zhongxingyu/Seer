 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class HelpActivity extends Activity {
 
     WebView webview;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 
 	setContentView(R.layout.help);
 	webview = (WebView) findViewById(R.id.helpwebview);
 	WebSettings websettings = webview.getSettings();
 	webview.setWebViewClient(new HelpWebViewClient());
 	webview.setBackgroundColor(255);
 	websettings.setSavePassword(false);
 	websettings.setSaveFormData(false);
 	websettings.setJavaScriptEnabled(false);
 	websettings.setSupportZoom(false);
 	websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
 	webview.loadUrl("file:///android_asset/index.html");
 
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 	if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
 	    webview.goBack();
 	    return true;
 	} else
 
 	    return super.onKeyDown(keyCode, event);
     }
 
     private class HelpWebViewClient extends WebViewClient {
 	@Override
 	public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	    if (url.contains("mailto:zanshin.g1@gmail.com")) {
 		Intent sendIntent = new Intent(Intent.ACTION_SEND);
 		sendIntent.setType("text/plain");
 		sendIntent.putExtra(Intent.EXTRA_EMAIL,
 			new String[] { "zanshin.g1@gmail.com" });
 		startActivity(Intent.createChooser(sendIntent, "Email:"));
 		return true;
	    } else if (url.contains("http://wififixer.wordpress.com")) {
 		Intent i = new Intent(Intent.ACTION_VIEW);
 		Uri u = Uri.parse("http://wififixer.wordpress.com");
 		i.setData(u);
 		startActivity(i);
 		return true;
	    } else if (url.contains("stop")) {
 		HelpActivity.this.finish();
 		return true;
	    } else if (url.contains("http://cyanogenmod.com")) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		Uri u = Uri.parse("http://cyanogenmod.com");
		i.setData(u);
		startActivity(i);
		return true;
 	    }
 	    view.loadUrl(url);
 	    return true;
 	}
 
     }
 }
