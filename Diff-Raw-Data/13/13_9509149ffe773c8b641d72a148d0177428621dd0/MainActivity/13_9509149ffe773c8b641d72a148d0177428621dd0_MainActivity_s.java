 package be.iminds.mix.streamstore;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.webkit.ConsoleMessage;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     WebView myWebView;
     MyProgressDialog dialog;
     SensorData sensorData;
     String lastOriginalUrl = "";
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 //        Comment this for testing in emulator
        sensorData = new SensorData(MainActivity.this);
         myWebView = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         myWebView.setWebChromeClient(new WebChromeClient() {
             public boolean onConsoleMessage(ConsoleMessage cm) {
                 Log.d("MyApplication", cm.message() + " -- From line "
                         + cm.lineNumber() + " of "
                         + cm.sourceId());
                 return true;
             }
         });
 //        Comment this for testing in emulator
        myWebView.addJavascriptInterface(sensorData, "Android");
        myWebView.loadUrl("http://straalstroom.mixlab.be");
         myWebView.setWebViewClient(new WebViewClient(){
 //              problem with redirects in Android > 3 (http://www.catchingtales.com/android-webview-shouldoverrideurlloading-and-redirect/416/)
             @Override
             public boolean shouldOverrideUrlLoading(WebView wv, String url){
                 if(wv.getOriginalUrl()== null || !lastOriginalUrl.equals(wv.getOriginalUrl())){
                     dialog = MyProgressDialog.show(MainActivity.this, null, null, true, false, null);
                 }
                 Log.d("MyApplication", "shouldoverride: "+ url+" original: "+wv.getOriginalUrl());
                 lastOriginalUrl = (wv.getOriginalUrl() != null) ? wv.getOriginalUrl(): "";
                 wv.loadUrl(url);
                 return true;
             }
 
             @Override
             public void onPageFinished(WebView wv, String url){
                if(dialog.isShowing()) dialog.dismiss();
                 Log.d("MyApplication", "pagefinished: " + url +" original: "+wv.getOriginalUrl());
 //                Toast.makeText(MainActivity.this, sensorData.toString(), Toast.LENGTH_LONG).show();
             }
 
             @Override
             public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                if(dialog.isShowing()) dialog.dismiss();
                 Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
             }
         });
     }
 
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Check if the key event was the Back button and if there's history
         if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
             myWebView.goBack();
             return true;
         }
         // If it wasn't the Back key or there's no web page history, bubble up to the default
         // system behavior (probably exit the activity)
         return super.onKeyDown(keyCode, event);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
