 package com.kwc.itfornebulunchapp;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import com.kwc.itfornebulunchapp.handlers.InternalStorage;
 import com.kwc.itfornebulunchapp.jsaccessors.JSInterface;
 import com.kwc.itfornebulunchapp.utils.AlertBox;
 
 /**
  * The main activity.
  * @since 1.0
  * @author Marius Kristensen
  */
 
 public class LunsjActivity extends Activity {
 
     private WebView webView = null;
     private JSInterface jsInterface;
     public static InternalStorage internalStorage;
     private static final String LOG_TAG = "ITFORNEBU-LUNCH";
 
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         //initialize alertBox
         AlertBox alertBox = new AlertBox(LunsjActivity.this);
 
         //Initialize file reading/writing
         internalStorage = new InternalStorage(LunsjActivity.this);
 
         // Show progressdialog while the app loads.
         final ProgressDialog progressDialog = new ProgressDialog(LunsjActivity.this);
         progressDialog.setMessage("Laster ...");
         progressDialog.setCancelable(false);
         progressDialog.show();
 
         webView = (WebView) findViewById(R.id.webView);
         webView.getSettings().setJavaScriptEnabled(true);
         webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
         webView.setWebChromeClient(new WebChromeClient());
         webView.loadUrl("file:///android_asset/www/index.html");
 
         //Javascript accessors
         jsInterface = new JSInterface(webView);
         webView.addJavascriptInterface(jsInterface, "jsinterface");
 
         webView.setWebViewClient(new WebViewClient() {
             // Hide the prograssdialog when the app is loaded.
             @Override
             public void onPageFinished(WebView view, String url) {
                 super.onPageFinished(view, url);
                 progressDialog.hide();
             }
         });
     }
 
     /**
      * Create a options menu.
      * @param menu -
      * @return true
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.lunchmenu, menu);
         return true;
     }
 
     /**
      * Options menu OnClick handler.
      * @param item -
      * @return true/false
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.refresh:
                 internalStorage.deleteFile();
                 if (webView.canGoBack()) {
                     webView.goBack();
                 } else {
                     webView.loadUrl("file:///android_asset/www/index.html");
                 }
                 return true;
             case R.id.about:
                 webView.loadUrl("file:///android_asset/www/about.html");
 
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Back button. This makes it possible to go back from
      * About to Main screen.
      */
     @Override
     public void onBackPressed() {
         if (webView.canGoBack()) {
             webView.goBack();
         } else {
             // Return to home menu of phone.
             Intent intent = new Intent(Intent.ACTION_MAIN);
             intent.addCategory(Intent.CATEGORY_HOME);
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(intent);
 
         }
     }
 }
